package com.android.onlineshoppingapp;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.onlineshoppingapp.models.Cart;
import com.android.onlineshoppingapp.models.UserAddress;
import com.android.onlineshoppingapp.models.cartProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import android.app.Activity;


public class CheckoutActivity extends AppCompatActivity {
    private static final String PAYPAL_CLIENT_ID = "AdmGE3X9dxnJJOF-hp4APkLMt2M40xCa_Sha7PVnXwid0gmnsIsPsDjaH1_L3w9G6yi-lhW0CSMWAuaJ";
    private static final int REQUEST_CODE_PAYMENT = 1;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX) // for test, use ENVIRONMENT_PRODUCTION for real app
            .clientId(PAYPAL_CLIENT_ID);

    private TextView tvChange, tvName, tvPhone, tvAddress, tvOrderPrice, tvShipping, tvTotal, tvFastDeliveryTxtCheckout, tvFreeOptionCheckout, tvFastOptionCheckout;
    private RadioGroup rgOptions;
    private ImageView ivBackCheckout;
    private Button btnCheckout;
  private final UserAddress userAddress = new UserAddress();
    private List<UserAddress> userAddressList = new ArrayList<>();
    private final int fee = 10;
    public int total = 0;
    private Cart cart;
    private int indexAddress = 0;
    private Boolean ship = false;
    private static JSONObject getBaseRequest() throws JSONException {
        return new JSONObject()
                .put("apiVersion", 2)
                .put("apiVersionMinor", 0);
    }

    private static JSONObject getGatewayTokenizationSpecification() throws JSONException {
        return new JSONObject()
                .put("type", "PAYMENT_GATEWAY")
                .put("parameters", new JSONObject()
                        .put("gateway", "acceptblue")
                        .put("gatewayMerchantId", "exampleGatewayMerchantId")
                );
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Start PayPal Service
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        // ...

        Button payWithPayPalButton = findViewById(R.id.btnPayWithPayPal);
        payWithPayPalButton.setOnClickListener(view -> {
            float totalAmount = (float) total;
            PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(totalAmount)), "USD", "Test payment with PayPal", PayPalPayment.PAYMENT_INTENT_SALE);

            Intent paymentIntent = new Intent(this, PaymentActivity.class);
            paymentIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
            paymentIntent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
            startActivityForResult(paymentIntent, REQUEST_CODE_PAYMENT);
        });

        // ...
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_checkout);

        // init
        ivBackCheckout = findViewById(R.id.ivBackCheckout);
        tvChange = findViewById(R.id.tvChangeAddressCheckout);
        tvFastDeliveryTxtCheckout = findViewById(R.id.tvFastDeliveryTxtCheckout);
        tvName = findViewById(R.id.tvNameCheckout);
        tvPhone = findViewById(R.id.tvPhoneCheckout);
        tvAddress = findViewById(R.id.tvAddressCheckout);
        tvFreeOptionCheckout = findViewById(R.id.tvFreeOptionCheckout);
        tvFastOptionCheckout = findViewById(R.id.tvFastOptionCheckout);
        tvOrderPrice = findViewById(R.id.tvOrderPriceCheckout);
        tvShipping = findViewById(R.id.tvShippingFeeCheckout);
        tvTotal = findViewById(R.id.tvTotalCheckout);
        rgOptions = findViewById(R.id.rgOptionsCheckout);
        btnCheckout = findViewById(R.id.btnCheckout);
        cart = (Cart) getIntent().getSerializableExtra("cart");

        // click on back
        ivBackCheckout.setOnClickListener(view -> onBackPressed());

        // set fast delivery fee
        tvFastDeliveryTxtCheckout.setText(String.format("FAST DELIVERY (%,d₹)", fee));

        // click on change
        tvChange.setOnClickListener(view -> {
            if (userAddressList.isEmpty()) {
                Toast.makeText(CheckoutActivity.this, "Navigate to edit", Toast.LENGTH_SHORT).show();
            } else {
                showDialogChangeAddress();
            }
        });

        // set date shipping
        tvFreeOptionCheckout.setText(getDateDelivery(5));
        tvFastOptionCheckout.setText(getDateDelivery(3));

        // set price order
        int priceOrder = cart.getTotalPrice();
        tvOrderPrice.setText(String.format("%,d₹", priceOrder));

        // set shipping
        total = priceOrder;
        rgOptions.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.rbFreeDeliveryCheckout) {
                tvShipping.setText("+0₹");
                total = priceOrder;
                ship = false;
                tvTotal.setText(String.format("%,d₹", total));
            }
            if (i == R.id.rbFastDeliveryCheckout) {
                List<String> sellerList = new ArrayList<>();
                for (cartProduct item : cart.getCartProductList())
                    if (!sellerList.contains(item.getSeller()))
                        sellerList.add(item.getSeller());
                tvShipping.setText(String.format("+%,d₹", fee));
                total += fee * sellerList.size();
                ship = true;
                // set total
                tvTotal.setText(String.format("%,d₹", total));
            }
        });

        // set total
        tvTotal.setText(String.format("%,d₹", total));

        // click on button checkout
        btnCheckout.setOnClickListener(view -> showDialogConfirmCheckout());

        //set address
        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        userRef.collection("Addresses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userAddressList = queryDocumentSnapshots.toObjects(UserAddress.class);
                    Log.d(TAG, "Size of userAddressList: " + userAddressList.size());
                    if (!userAddressList.isEmpty()) {
                        setAddress(userAddressList.get(0));
                    } else {
                        // Handle the case when userAddressList is empty
                        Log.w(TAG, "userAddressList is empty");
                        // You may set default values or show a message to the user
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, Objects.requireNonNull(e.getMessage())));

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i("paymentExample", confirm.toJSONObject().toString(4));

                        // TODO: send 'confirm' to your server for verification

                    } catch (JSONException e) {
                        Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("paymentExample", "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }







//    @SuppressLint("DefaultLocale")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//    }

    private void setAddress(UserAddress userAddress) {
        tvName.setText(userAddress.getName());
        tvAddress.setText(userAddress.getFullAddress());
        tvPhone.setText(userAddress.getPhone());
    }

    @SuppressLint({"DefaultLocale", "SimpleDateFormat"})
    private void showDialogConfirmCheckout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Payment confirmation")
                .setMessage("Confirm payment of amount " + String.format("%,d₹", total) + " By form: Payment upon receipt.")
                .setCancelable(false)
                .setPositiveButton("Agree", (dialogInterface, i) -> {
                    List<String> sellerList = new ArrayList<>();
                    for (cartProduct item : cart.getCartProductList())
                        if (!sellerList.contains(item.getSeller()))
                            sellerList.add(item.getSeller());

                    for (String seller : sellerList) {
                        //get total price
                        long totalPrice = 0;
                        for (cartProduct cartProduct : cart.getCartProductList())
                            if (cartProduct.getSeller().equals(seller))
                                totalPrice += (long) cartProduct.getProductPrice() * cartProduct.getOrderQuantity();
                        if (ship)
                            totalPrice +=10;
                        Map<String, Object> order = new HashMap<>();
                        order.put("orderer", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                        order.put("seller", seller);
                        order.put("address", userAddressList.get(indexAddress));
                        order.put("totalPrice", totalPrice);
                        order.put("orderStatus", 0);
                        order.put("orderTime", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
                        CollectionReference orderRef = FirebaseFirestore.getInstance().collection("Orders");
                        String orderId = orderRef.document().getId();
                        //Create order
                        orderRef.document(orderId)
                                .set(order)
                                .addOnSuccessListener(unused -> {
                                    for (cartProduct item : cart.getCartProductList()) {
                                        if (item.getSeller().equals(seller)) {
                                            Map<String, Object> productOrder = new HashMap<>();
                                            productOrder.put("isRated", false);
                                            productOrder.put("productRef",
                                                    FirebaseFirestore.getInstance()
                                                            .document("Products/" + item.getProductId() + "/"));
                                            productOrder.put("orderQuantity", item.getOrderQuantity());
                                            productOrder.put("seller", item.getSeller());
                                            productOrder.put("productPrice", item.getProductPrice());

                                            //Add product to Bought Product
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("productRef", FirebaseFirestore.getInstance().collection("Products").document(item.getProductId()));
                                            FirebaseFirestore.getInstance()
                                                    .collection("Users")
                                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .collection("boughtProducts")
                                                    .document(item.getProductId())
                                                    .set(map)
                                                    .addOnFailureListener(e -> Log.e("add bought product", Objects.requireNonNull(e.getMessage())));

                                            //add product to order
                                            orderRef.document(orderId).collection("Products")
                                                    .document(item.getProductId())
                                                    .set(productOrder)
                                                    .addOnSuccessListener(unused1 -> {
                                                        Toast.makeText(CheckoutActivity.this,
                                                                "Payment success", Toast.LENGTH_SHORT).show();
                                                        //Delete cart
                                                        DocumentReference cartRef = FirebaseFirestore.getInstance()
                                                                .collection("Carts")
                                                                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                        cartRef.collection("Products")
                                                                .document(item.getProductId())
                                                                .delete();

                                                        //Reduce quantity and increase quantitySold product
                                                        DocumentReference productRef = FirebaseFirestore.getInstance()
                                                                .collection("Products")
                                                                .document(item.getProductId());
                                                        Map<String, Object> product = new HashMap<>();
                                                        product.put("quantitySold", item.getQuantitySold() + item.getOrderQuantity());
                                                        product.put("quantity", item.getQuantity() - item.getOrderQuantity());
                                                        productRef.set(product, SetOptions.merge());
                                                    }).addOnFailureListener(e -> {
                                                        Toast.makeText(CheckoutActivity.this,
                                                                "An error occurred", Toast.LENGTH_SHORT).show();
                                                        Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                                                    });
                                        }
                                    }


                                });
                    }
                    startActivity(new Intent(CheckoutActivity.this, MainActivity.class));
                    finishAffinity();
                }).setNegativeButton("Refuse", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    private void showDialogChangeAddress() {

        // get list of address info
        List<String> list = new ArrayList<>();
        for (UserAddress item : userAddressList)
            list.add("Address: " + item.getFullAddress() + "\n" + "Name: " + item.getName() + "\n" + "Phone number: " + item.getPhone());
        CharSequence[] charSequence = list.toArray(new CharSequence[userAddressList.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select billing address")
                .setCancelable(false)
                .setSingleChoiceItems(charSequence, indexAddress, (dialogInterface, i) -> {
                    tvName.setText(userAddressList.get(i).getName());
                    tvPhone.setText(userAddressList.get(i).getPhone());
                    tvAddress.setText(userAddressList.get(i).getFullAddress());
                    indexAddress = i;
                }).setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    public String getDateDelivery(int delay) {

        LocalDate currentDate = LocalDate.now();
        LocalDate dateAfter = currentDate.plus(delay, ChronoUnit.DAYS);

        return "Delivery in advance " + convertDateOfWeek(dateAfter.getDayOfWeek().getValue()) +
                ", day " + dateAfter.getDayOfMonth() + "/" + dateAfter.getMonth().getValue() + "/" + dateAfter.getYear();
    }

    public String convertDateOfWeek(int dow) {
        switch (dow) {
            case 1:
                return "rank 2";
            case 2:
                return "rank 3";
            case 3:
                return "rank 4";
            case 4:
                return "rank 5";
            case 5:
                return "rank 6";
            case 6:
                return "rank 7";
            case 7:
                return "Sunday";
        }
        return "";
    }


    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
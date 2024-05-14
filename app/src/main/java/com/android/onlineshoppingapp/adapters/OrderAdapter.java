package com.android.onlineshoppingapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.onlineshoppingapp.R;
import com.android.onlineshoppingapp.WriteReviewProductActivity;
import com.android.onlineshoppingapp.models.Order;
import com.android.onlineshoppingapp.models.OrderProduct;
import com.android.onlineshoppingapp.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Order> orderList;
    private final Context context;
    public boolean isShipper = false;
    public boolean isAdmin = false;
    public boolean isSeller = false;

    public OrderAdapter(List<Order> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {

        Order order = orderList.get(position);
        if (order == null) return;

        if (order.getOrderStatus() == 0 || order.getOrderStatus() == 1) {

            if (!isAdmin) {
                holder.btnCancel.setOnClickListener(view -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setCancelable(false).setTitle("Cancel order")
                            .setMessage("Order cancellation confirmation?")
                            .setPositiveButton("Agree", (dialogInterface, i) -> {
                                Toast.makeText(view.getContext(), "Order canceled successfully", Toast.LENGTH_SHORT).show();
                                Map<String, Object> update = new HashMap<>();
                                update.put("orderStatus", 4);
                                FirebaseFirestore.getInstance()
                                        .collection("Orders")
                                        .document(order.getOrderId())
                                        .set(update, SetOptions.merge())
                                        .addOnFailureListener(e -> Log.e("orderAdapter", Objects.requireNonNull(e.getMessage())));
                            }).setNegativeButton("Skip", (dialogInterface, i) -> dialogInterface.dismiss()).show();
                });
            } else {
                holder.btnCancel.setText("Confirm");
                if (order.getOrderStatus() == 0) {
                    holder.btnCancel.setOnClickListener(view -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setCancelable(false).setTitle("Order confirmation")
                                .setMessage("Do you want to confirm this order and move it to the Picking status?")
                                .setPositiveButton("Agree", (dialogInterface, i) -> {
                                    Toast.makeText(context, "Change order status successfully", Toast.LENGTH_SHORT).show();
                                    Map<String, Object> update = new HashMap<>();
                                    update.put("orderStatus", 1);
                                    FirebaseFirestore.getInstance()
                                            .collection("Orders")
                                            .document(order.getOrderId())
                                            .set(update, SetOptions.merge())
                                            .addOnFailureListener(e -> Log.e("orderAdapter", Objects.requireNonNull(e.getMessage())));
                                }).setNegativeButton("Skip", (dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                }).show();
                    });
                } else {
                    holder.btnCancel.setOnClickListener(view -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setCancelable(false).setTitle("Order confirmation")
                                .setMessage("Do you want to confirm this order and move it to the Shipping status?")
                                .setPositiveButton("Agree", (dialogInterface, i) -> {
                                    Toast.makeText(context, "Change order status successfully", Toast.LENGTH_SHORT).show();
                                    Map<String, Object> update = new HashMap<>();
                                    update.put("orderStatus", 2);
                                    FirebaseFirestore.getInstance()
                                            .collection("Orders")
                                            .document(order.getOrderId())
                                            .set(update, SetOptions.merge())
                                            .addOnFailureListener(e -> Log.e("orderAdapter", Objects.requireNonNull(e.getMessage())));
                                }).setNegativeButton("Skip", (dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                }).show();
                    });
                }
            }

        }

        if (order.getOrderStatus() == 2 || order.getOrderStatus() == 4) {
            holder.btnCancel.setVisibility(View.INVISIBLE);
            if (isShipper && order.getOrderStatus() == 2) {
                holder.btnCancel.setVisibility(View.VISIBLE);
                holder.btnCancel.setText("Delivered");
                holder.btnCancel.setOnClickListener(view -> {
                    Toast.makeText(view.getContext(), "Delivered successfully", Toast.LENGTH_SHORT).show();
                    Map<String, Object> update = new HashMap<>();
                    update.put("orderStatus", 3);
                    FirebaseFirestore.getInstance()
                            .collection("Orders")
                            .document(order.getOrderId())
                            .set(update, SetOptions.merge())
                            .addOnFailureListener(e -> Log.e("orderAdapter", Objects.requireNonNull(e.getMessage())));
                });
            }
        }

        holder.tvShopName.setText(order.getOrderId());
        String orderStatus = getOrderStatus(order);
        holder.tvOrderStatus.setText(orderStatus);
        if (order.getOrderStatus() == 4) {
            holder.tvOrderStatus.setTextColor(holder.itemView.getResources().getColor(R.color.red_error));
        }


        holder.tvPriceTotal.setText(String.format("₹%,d", order.getTotalPrice()));
//        holder.tvCategory.setText("Loại:");


        if (order.getOrderStatus() == 3) {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.getString("accountType").equals("Sell")) {
                            holder.btnCancel.setVisibility(View.INVISIBLE);
                        } else {
                            holder.btnCancel.setText("Evaluate");
                            getProduct(order.getOrderId(), holder);
                        }
                    });
        }

        //get product
        FirebaseFirestore.getInstance().collection("Orders")
                .document(order.getOrderId())
                .collection("Products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                    List<OrderProduct> orderProductList = new ArrayList<>();
                    if (!queryDocumentSnapshots1.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot1 : queryDocumentSnapshots1) {
                            //get product info
                            DocumentReference productRef = (DocumentReference) documentSnapshot1.get("productRef");
                            assert productRef != null;
                            productRef.get().addOnSuccessListener(documentSnapshot2 -> {
                                Product product = documentSnapshot2.toObject(Product.class);

                                assert product != null;
                                OrderProduct orderProduct = new OrderProduct(documentSnapshot2.getId(),
                                        product.getProductName(),
                                        product.getSeller(),
                                        product.getDescription(),
                                        product.getCategory(),
                                        product.getProductPrice(),
                                        product.getRate(),
                                        product.getLikeNumber(),
                                        product.getQuantitySold(),
                                        product.getQuantity(),
                                        Integer.parseInt(String.valueOf(documentSnapshot1.get("orderQuantity"))));
                                orderProductList.add(orderProduct);
                                order.setListOrderProduct(orderProductList);
                                OrderProductAdapter orderProductAdapter = new OrderProductAdapter(order.getListOrderProduct(), context);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                                holder.recyclerView.setLayoutManager(linearLayoutManager);
                                holder.recyclerView.setAdapter(orderProductAdapter);
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("get order info", Objects.requireNonNull(e.getMessage())));
    }

    private void getProduct(String orderId, OrderViewHolder holder) {
        FirebaseFirestore.getInstance()
                .collection("Orders")
                .document(orderId)
                .collection("Products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> productList = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot1 : queryDocumentSnapshots) {
                        if (Boolean.FALSE.equals(documentSnapshot1.getBoolean("isRated")))
                            productList.add(documentSnapshot1.getId());
                    }
                    if (productList.size() > 0) {
                        holder.btnCancel.setOnClickListener(view -> {
                            Intent intent = new Intent(context, WriteReviewProductActivity.class);
                            intent.putExtra("productList", (Serializable) productList);
                            intent.putExtra("orderId", orderId);
                            context.startActivity(intent);
                        });
                    } else {
                        holder.btnCancel.setVisibility(View.INVISIBLE);
                    }
                });
    }

    @Override
    public int getItemCount() {
        if (orderList.isEmpty()) {
            return 0;
        }
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvShopName;
        private final TextView tvOrderStatus;
        private TextView tvProductName;
        private TextView tvCategory;
        private TextView tvPrice;
        private TextView tvNumber;
        private TextView tvPriceTotal;
        private Button btnCancel;
        private ImageView ivProductImg;
        private RecyclerView recyclerView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            tvShopName = itemView.findViewById(R.id.tvShopNameOrder);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatusOrder);
            tvProductName = itemView.findViewById(R.id.tvProductNameOrder);
            tvCategory = itemView.findViewById(R.id.tvCategoryOrder);
            tvPrice = itemView.findViewById(R.id.tvProductPriceOrder);
            tvPriceTotal = itemView.findViewById(R.id.tvPriceTotalOrder);
            tvNumber = itemView.findViewById(R.id.tvNumberOrder);
            btnCancel = itemView.findViewById(R.id.btnCancelOrder);
            ivProductImg = itemView.findViewById(R.id.ivProductImageOrder);
            recyclerView = itemView.findViewById(R.id.rvProducts);
        }
    }

    public String getOrderStatus(Order order) {
        String orderStatus = "";
        switch (order.getOrderStatus()) {
            case 0:
                orderStatus = "Waiting for confirmation";
                break;
            case 1:
                orderStatus = "Pick up";
                break;
            case 2:
                orderStatus = "Delivering";
                break;
            case 3:
                orderStatus = "Delivered";
                break;
            case 4:
                orderStatus = "Cancelled";
                break;
        }
        return orderStatus;
    }
}

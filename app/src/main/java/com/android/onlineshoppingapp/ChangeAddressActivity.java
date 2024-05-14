package com.android.onlineshoppingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.android.onlineshoppingapp.models.UserAddress;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class ChangeAddressActivity extends AppCompatActivity {

    private ImageView ivBack;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switch1, switch2;
    private CheckBox cbAddress2;
    private Button btnChangeAddress;
    private LinearLayout layoutCAddress2;
    private TextInputEditText etName1, etName2, etPhone1, etPhone2, etDetail1, etDetail2,ctvCity1,ctvCity2,ctvDistrict1,ctvDistrict2,ctvTown1,ctvTown2;
    private TextInputLayout layoutName1, layoutName2, layoutPhone1, layoutPhone2, layoutDetail1, layoutDetail2,layoutCity1,layoutCity2,layoutDistrict1,layoutDistrict2,layoutTown1,layoutTown2;
    private FirebaseAuth fAuth;
    private FirebaseFirestore db;
    private List<UserAddress> userAddresses;
    private ArrayList<String> cityNameList, districtNameList, townNameList, districtNameList2, townNameList2;
    private List<Map<String, Object>> cityList, districtList, townList, districtList2, townList2;
    private Map<String, Object> data1 = new HashMap<>();
    private Map<String, Object> data2 = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_address);

        // init
        ivBack = findViewById(R.id.ivBackCAddress);
        switch1 = findViewById(R.id.switchAddress1);
        switch2 = findViewById(R.id.switchAddress2);
        etName1 = findViewById(R.id.etNameCAddress1);
        etName2 = findViewById(R.id.etNameCAddress2);
        etPhone1 = findViewById(R.id.etPhoneCAddress1);
        etPhone2 = findViewById(R.id.etPhoneCAddress2);
        etDetail1 = findViewById(R.id.etDetailCAddress1);
        etDetail2 = findViewById(R.id.etDetailCAddress2);
        layoutName1 = findViewById(R.id.layoutNameCAddress1);
        layoutName2 = findViewById(R.id.layoutNameCAddress2);
        layoutPhone1 = findViewById(R.id.layoutPhoneCAddress1);
        layoutPhone2 = findViewById(R.id.layoutPhoneCAddress2);
        layoutDetail1 = findViewById(R.id.layoutDetailCAddress1);
        layoutDetail2 = findViewById(R.id.layoutDetailCAddress2);


        ctvCity1 = findViewById(R.id.tvCityCAddress1);
        layoutCity1 = findViewById(R.id.layoutCityCAddress1);

        ctvCity2 = findViewById(R.id.tvCityCAddress2);
        layoutCity2 = findViewById(R.id.layoutCityCAddress2);

        ctvDistrict1 = findViewById(R.id.tvDistrictCAddress1);
        layoutDistrict1=findViewById(R.id.layoutDistrictCAddress1);

        ctvDistrict2 = findViewById(R.id.tvDistrictCAddress2);
        layoutDistrict2=findViewById(R.id.layoutDistrictCAddress2);

        ctvTown1 = findViewById(R.id.tvTownCAddress1);
        layoutTown1=findViewById(R.id.layoutTownCAddress1);
        ctvTown2 = findViewById(R.id.tvTownCAddress2);
        layoutTown2=findViewById(R.id.layoutTownCAddress2);
        cbAddress2 = findViewById(R.id.cbCAddress2);
        layoutCAddress2 = findViewById(R.id.layoutCAddress2);
        btnChangeAddress = findViewById(R.id.btnChangeAddress);

        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // click on back
        ivBack.setOnClickListener(view -> {
            onBackPressed();
        });

        // click on check box
        layoutCAddress2.setVisibility(View.GONE);
        cbAddress2.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                if (switch2.isChecked())
                    switch2.setChecked(false);
                layoutCAddress2.setVisibility(View.VISIBLE);
                switch2.setVisibility(View.VISIBLE);
            } else {
                if (!switch1.isChecked())
                    switch1.setChecked(true);
                layoutCAddress2.setVisibility(View.GONE);
                switch2.setVisibility(View.INVISIBLE);
            }
        });

        // click on switch
        switch1.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                switch2.setChecked(false);
            } else {
                switch2.setChecked(true);
                if (switch2.getVisibility() == View.INVISIBLE) {
                    switch1.setChecked(true);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChangeAddressActivity.this);
                    builder.setCancelable(false)
                            .setTitle("Default address")
                            .setMessage("There should be at least 1 address as default")
                            .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss()).show();
                }
            }
        });

        switch2.setOnCheckedChangeListener((compoundButton, b) -> switch1.setChecked(!b));

        // check name 1
        etName1.setOnFocusChangeListener((view, onFocus) -> {
            if (onFocus) {
                layoutName1.setHelperTextEnabled(false);
            } else {
                if (Objects.requireNonNull(etName1.getText()).toString().isEmpty()) {
                    layoutName1.setHelperText("Name cannot be left blank");
                } else if (!includeCharInAlphabet(etName1.getText().toString())) {
                    layoutName1.setHelperText("The name must contain at least 1 alphabetic character");
                } else {
                    layoutName1.setHelperTextEnabled(false);
                }
            }
        });

        // check name 2
        etName2.setOnFocusChangeListener((view, onFocus) -> {
            if (onFocus) {
                layoutName2.setHelperTextEnabled(false);
            } else {
                if (Objects.requireNonNull(etName2.getText()).toString().isEmpty()) {
                    layoutName2.setHelperText("Name cannot be left blank");
                } else if (!includeCharInAlphabet(etName2.getText().toString())) {
                    layoutName2.setHelperText("The name must contain at least 1 alphabetic character");
                } else {
                    layoutName2.setHelperTextEnabled(false);
                }
            }
        });

        // check phone 1
        etPhone1.setOnFocusChangeListener((view, onFocus) -> {
            if (onFocus) {
                layoutPhone1.setHelperTextEnabled(false);
            } else {
                if (Objects.requireNonNull(etPhone1.getText()).toString().isEmpty()) {
                    layoutPhone1.setHelperText("Phone number cannot be left blank");
                } else if (etPhone1.getText().toString().length() < 6) {
                    layoutPhone1.setHelperText("The phone number must have at least 6 digits");
                } else {
                    layoutPhone1.setHelperTextEnabled(false);
                }
            }
        });

        // check phone 2
        etPhone2.setOnFocusChangeListener((view, onFocus) -> {
            if (onFocus) {
                layoutPhone2.setHelperTextEnabled(false);
            } else {
                if (Objects.requireNonNull(etPhone2.getText()).toString().isEmpty()) {
                    layoutPhone2.setHelperText("Phone number cannot be left blank");
                } else if (etPhone2.getText().toString().length() < 6) {
                    layoutPhone2.setHelperText("The phone number must have at least 6 digits");
                } else {
                    layoutPhone2.setHelperTextEnabled(false);
                }
            }
        });





        //set district
        ctvCity1.setOnFocusChangeListener((view, onFocus) -> {
            if (!onFocus) {
                if (Objects.requireNonNull(ctvCity1.getText()).toString().isEmpty()) {
                    layoutCity1.setHelperText("Please fill city / province");
                } else {
                    layoutCity1.setHelperTextEnabled(false);
                }
            }
        });

        ctvCity2.setOnFocusChangeListener((view, onFocus) -> {
            if (!onFocus) {
                if (Objects.requireNonNull(ctvCity2.getText()).toString().isEmpty()) {
                    layoutCity2.setHelperText("Please fill city / province");
                } else {
                    layoutCity2.setHelperTextEnabled(false);
                }
            }
        });
        //set ward
        ctvDistrict1.setOnFocusChangeListener((view, onFocus) -> {
            if (!onFocus) {
                if (Objects.requireNonNull(ctvDistrict1.getText()).toString().isEmpty()) {
                    layoutDistrict1.setHelperText("Please fill city / province");
                } else {
                    layoutDistrict1.setHelperTextEnabled(false);
                }
            }
        });
        ctvDistrict2.setOnFocusChangeListener((view, onFocus) -> {
            if (!onFocus) {
                if (Objects.requireNonNull(ctvDistrict1.getText()).toString().isEmpty()) {
                    layoutDistrict2.setHelperText("Please fill city / province");
                } else {
                    layoutDistrict2.setHelperTextEnabled(false);
                }
            }
        });

        //get ward code
        ctvTown1.setOnFocusChangeListener((view, onFocus) -> {
            if (!onFocus) {
                if (Objects.requireNonNull(ctvTown1.getText()).toString().isEmpty()) {
                    layoutTown1.setHelperText("Please fill city / province");
                } else {
                    layoutTown1.setHelperTextEnabled(false);
                }
            }
        });
        ctvTown2.setOnFocusChangeListener((view, onFocus) -> {
            if (!onFocus) {
                if (Objects.requireNonNull(ctvTown2.getText()).toString().isEmpty()) {
                    layoutTown2.setHelperText("Please fill city / province");
                } else {
                    layoutTown2.setHelperTextEnabled(false);
                }
            }
        });
        // check detail 1
        etDetail1.setOnFocusChangeListener((view, onFocus) -> {
            if (onFocus) {
                layoutDetail1.setHelperTextEnabled(false);
            } else {
                if (Objects.requireNonNull(etDetail1.getText()).toString().isEmpty()) {
                    layoutDetail1.setHelperText("Street name and house number cannot be left blank");
                } else {
                    layoutDetail1.setHelperTextEnabled(false);
                }
            }
        });

        // check detail 2
        etDetail2.setOnFocusChangeListener((view, onFocus) -> {
            if (onFocus) {
                layoutDetail2.setHelperTextEnabled(false);
            } else {
                if (Objects.requireNonNull(etDetail2.getText()).toString().isEmpty()) {
                    layoutDetail2.setHelperText("Street name and house number cannot be left blank");
                } else {
                    layoutDetail2.setHelperTextEnabled(false);
                }
            }
        });

        userAddresses = new ArrayList<>();
        db.collection("Users").document(Objects.requireNonNull(fAuth.getCurrentUser()).getUid())
                .collection("Addresses")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        task.getResult().getDocuments().forEach(documentSnapshot -> {
                            UserAddress userAddress = documentSnapshot.toObject(UserAddress.class);
                            userAddresses.add(userAddress);
                        });


                        if (!userAddresses.isEmpty()) {
                            //set which address is default
                            if (!userAddresses.get(0).isDefaultAddress() && userAddresses.size() > 1) {
                                UserAddress temp = userAddresses.get(0);
                                userAddresses.set(0, userAddresses.get(1));
                                userAddresses.set(1, temp);
                            }
                            // set previous value for et
                            etName1.setText(userAddresses.get(0).getName());
                            etName1.setSelectAllOnFocus(true);
                            etPhone1.setText(userAddresses.get(0).getPhone());
                            etPhone1.setSelectAllOnFocus(true);
                            etDetail1.setText(userAddresses.get(0).getDetail());
                            etDetail1.setSelectAllOnFocus(true);
                            ctvCity1.setText(userAddresses.get(0).getCity());
                            data1.put("city", userAddresses.get(0).getCity());
                            data1.put("cityCode", userAddresses.get(0).getCityCode());
                       //     getDistrict(String.valueOf(userAddresses.get(0).getCityCode()), districtList, districtNameList, data1);
                            ctvDistrict1.setText(userAddresses.get(0).getDistrict());
                            data1.put("district", userAddresses.get(0).getDistrict());
                            data1.put("districtCode", userAddresses.get(0).getDistrictCode());
                      //      getTown(String.valueOf(userAddresses.get(0).getDistrictCode()), townList, townNameList, data1);
                            ctvTown1.setText(userAddresses.get(0).getTown());
                            data1.put("town", userAddresses.get(0).getTown());
                            data1.put("townCode", userAddresses.get(0).getTownCode());


                            if (userAddresses.size() > 1) {
                                // set previous value for et
                                etName2.setText(userAddresses.get(1).getName());
                                etName2.setSelectAllOnFocus(true);
                                etPhone2.setText(userAddresses.get(1).getPhone());
                                etPhone2.setSelectAllOnFocus(true);
                                etDetail2.setText(userAddresses.get(1).getDetail());
                                etDetail2.setSelectAllOnFocus(true);
                                ctvCity2.setText(userAddresses.get(1).getCity());
                                data2.put("city", userAddresses.get(1).getCity());
                                data2.put("cityCode", userAddresses.get(1).getCityCode());
                        //        getDistrict(String.valueOf(userAddresses.get(1).getCityCode()), districtList2, districtNameList2, data2);
                                ctvDistrict2.setText(userAddresses.get(1).getDistrict());
                                data2.put("district", userAddresses.get(1).getDistrict());
                                data2.put("districtCode", userAddresses.get(1).getDistrictCode());
                          //      getTown(String.valueOf(userAddresses.get(1).getDistrictCode()), townList2, townNameList2, data2);
                                ctvTown2.setText(userAddresses.get(1).getTown());
                                data2.put("town", userAddresses.get(1).getTown());
                                data2.put("townCode", userAddresses.get(1).getTownCode());
                            }
                        }

                    } else {
                        Log.e("getAddress", Objects.requireNonNull(Objects.requireNonNull(task.getException()).getMessage()));
                    }
                });


        // click on change
        btnChangeAddress.setOnClickListener(view -> {
            if (!validateData()) {
                db.collection("Users").document(fAuth.getCurrentUser().getUid())
                        .collection("Addresses")
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
//                                    Log.w("defaultAddress", (defaultAddress) ? "1" : "2");
                            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                            boolean defaultAddress = switch1.isChecked(); // true = 1, false = 2
                            data1.put("name", Objects.requireNonNull(etName1.getText()).toString());
                            data1.put("phone", Objects.requireNonNull(etPhone1.getText()).toString());
                            data1.put("city", Objects.requireNonNull(ctvCity1.getText()).toString());
                            data1.put("district", Objects.requireNonNull(ctvDistrict1.getText()).toString());
                            data1.put("town", Objects.requireNonNull(ctvTown1.getText()).toString());
                            data1.put("detail", Objects.requireNonNull(etDetail1.getText()).toString());
                            data1.put("defaultAddress", defaultAddress); // add city district town

                            data2.put("name", Objects.requireNonNull(etName2.getText()).toString());
                            data2.put("phone", Objects.requireNonNull(etPhone2.getText()).toString());
                            data2.put("city", Objects.requireNonNull(ctvCity2.getText()).toString());
                            data2.put("district", Objects.requireNonNull(ctvDistrict2.getText()).toString());
                            data2.put("town", Objects.requireNonNull(ctvTown2.getText()).toString());
                            data2.put("detail", Objects.requireNonNull(etDetail2.getText()).toString());
                            data2.put("defaultAddress", !defaultAddress); // add city district town
                            list.add(data1);
                            list.add(data2);
                            if (queryDocumentSnapshots.isEmpty()) {
                                db.collection("Users")
                                        .document(fAuth.getCurrentUser().getUid())
                                        .collection("Addresses")
                                        .add(data1);
                                if (cbAddress2.isChecked())
                                    db.collection("Users")
                                            .document(fAuth.getCurrentUser().getUid())
                                            .collection("Addresses")
                                            .add(data2);
                            } else {
                                int i = 0;
                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    Log.e("", String.valueOf(documentSnapshot.getData()));
                                    if ((i == 1 && cbAddress2.isChecked()) || i == 0)
                                        db.collection("Users")
                                                .document(fAuth.getCurrentUser().getUid())
                                                .collection("Addresses")
                                                .document(documentSnapshot.getId())
                                                .set(list.get(i))
                                                .addOnFailureListener(e -> Log.e("change address", Objects.requireNonNull(e.getMessage())));
                                    i++;
                                }
                                //add address 2 when user just have 1 address before
                                if (i == 1 && cbAddress2.isChecked()) {
                                    db.collection("Users")
                                            .document(fAuth.getCurrentUser().getUid())
                                            .collection("Addresses")
                                            .add(list.get(i))
                                            .addOnFailureListener(e -> Log.e("change address", Objects.requireNonNull(e.getMessage())));
                                }
                            }
                        });
                Toast.makeText(ChangeAddressActivity.this, "Update successful", Toast.LENGTH_SHORT).show();
                onBackPressed();
            } else {
                Toast.makeText(ChangeAddressActivity.this, "You have not filled in all the information", Toast.LENGTH_SHORT).show();
            }
        });

    }


    public boolean validateData() {
        if (cbAddress2.isChecked())
            return Objects.requireNonNull(etName1.getText()).toString().isEmpty() || Objects.requireNonNull(etPhone1.getText()).toString().isEmpty() ||
                    Objects.requireNonNull(etDetail1.getText()).toString().isEmpty() || Objects.requireNonNull(etName2.getText()).toString().isEmpty() ||
                    Objects.requireNonNull(etPhone2.getText()).toString().isEmpty() || Objects.requireNonNull(etDetail2.getText()).toString().isEmpty() ||
                    Objects.requireNonNull(ctvCity1.getText()).toString().equals("Select city / province") ||
                    Objects.requireNonNull(ctvCity2.getText()).toString().equals("Select city / province") ||
                    Objects.requireNonNull(ctvDistrict1.getText()).toString().equals("Select district") ||
                    Objects.requireNonNull(ctvDistrict2.getText()).toString().equals("Select district") ||
                    Objects.requireNonNull(ctvTown1.getText()).toString().equals("Select ward / commune") ||
                    Objects.requireNonNull(ctvTown2.getText()).toString().equals("Select ward / commune") ||
                    layoutName1.isHelperTextEnabled() || layoutPhone1.isHelperTextEnabled() ||
                    layoutDetail1.isHelperTextEnabled() || layoutName2.isHelperTextEnabled() ||
                    layoutPhone2.isHelperTextEnabled() || layoutDetail2.isHelperTextEnabled();
        else
            return Objects.requireNonNull(etName1.getText()).toString().isEmpty() || Objects.requireNonNull(etPhone1.getText()).toString().isEmpty() ||
                    Objects.requireNonNull(etDetail1.getText()).toString().isEmpty() || layoutName1.isHelperTextEnabled() ||
                    Objects.requireNonNull(ctvCity1.getText()).toString().equals("Select city / province") ||
                    Objects.requireNonNull(ctvDistrict1.getText()).toString().equals("Select district") ||
                    Objects.requireNonNull(ctvTown1.getText()).toString().equals("Select ward / commune") ||
                    layoutPhone1.isHelperTextEnabled() || layoutDetail1.isHelperTextEnabled();
    }

    public boolean includeCharInAlphabet(String str) {
        char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        for (int i = 0; i < str.length(); i++) {
            for (char c : alphabet) {
                if (str.toLowerCase().charAt(i) == c)
                    return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
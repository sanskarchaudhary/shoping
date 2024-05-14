package com.android.onlineshoppingapp.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.onlineshoppingapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class ChangePasswordFragment extends Fragment {

    private Button btnOk;
    private TextInputLayout inputLayoutEnterNewPass, inputLayoutEnterNewRePass;
    private TextInputEditText editTextEnterNewPass, editTextEnterNewRePass;
    private Bundle data;
    private String userEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        // change password
        inputLayoutEnterNewPass = view.findViewById(R.id.txtFieldNewPassFP);
        editTextEnterNewPass = view.findViewById(R.id.editTxtNewPassFP);

        editTextEnterNewPass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean onFocus) {
                if (!onFocus) {
                    if (Objects.requireNonNull(editTextEnterNewPass.getText()).toString().equals("")) {
                        inputLayoutEnterNewPass.setHelperText("Password can not be blank!");
                    } else if (!isLongEnough(editTextEnterNewPass.getText().toString(), 8)) {
                        inputLayoutEnterNewPass.setHelperText("Password must have at least 8 characters");
                    }
                } else {
                    inputLayoutEnterNewPass.setHelperTextEnabled(false);
                }
            }
        });

        inputLayoutEnterNewRePass = view.findViewById(R.id.txtFieldNewRePassFP);
        editTextEnterNewRePass = view.findViewById(R.id.editTxtNewRePassFP);

        // get user email
        data = getArguments();
        assert data != null;
        userEmail = data.getString("userEmail");

        btnOk = view.findViewById(R.id.btnOkFP);
        btnOk.setOnClickListener(view1 -> {
            if (Objects.requireNonNull(editTextEnterNewRePass.getText()).toString().equals(Objects.requireNonNull(editTextEnterNewPass.getText()).toString())) {
                inputLayoutEnterNewRePass.setHelperTextEnabled(false);

                Toast.makeText(getActivity(), "Email: " + userEmail +
                                "\nNew password: " + editTextEnterNewPass.getText(),
                        Toast.LENGTH_SHORT).show();

            } else {
                inputLayoutEnterNewRePass.setHelperText("Password was wrong!");
            }
        });

        // return view
        return view;
    }

    private boolean isLongEnough(String str, int num) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            count++;
        }
        if (count >= num) return true;
        else return false;
    }

}
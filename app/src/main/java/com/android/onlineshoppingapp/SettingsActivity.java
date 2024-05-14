package com.android.onlineshoppingapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.onlineshoppingapp.adapters.ViewPagerAdapterSettings;
import com.android.onlineshoppingapp.models.UserInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SettingsActivity extends AppCompatActivity {

    private ImageView ivBack;
    private CardView cardLogout, cardChangePass, cardDeleteAccount, cardPolicy, cardVersion;
    private ViewPagerAdapterSettings viewPagerAdapterSettings;
    private TabLayout tabLayoutSettings;
    private ViewPager2 viewPager2Settings;
    private String[] titles = new String[]{"Personal", "Address"};

    private FirebaseAuth fAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();
        viewPagerAdapterSettings = new ViewPagerAdapterSettings(this);

        //init
        ivBack = findViewById(R.id.ivBackToProfile);
        viewPager2Settings = findViewById(R.id.viewpagerSettings);
        tabLayoutSettings = findViewById(R.id.tablayoutSettings);
        cardChangePass = findViewById(R.id.cardChangePass);
        cardDeleteAccount = findViewById(R.id.cardDeleteAccount);
        cardPolicy = findViewById(R.id.cardPolicy);
        cardVersion = findViewById(R.id.cardVersion);
        cardLogout = findViewById(R.id.cardLogout);


        // click on back button
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // viewpager and tab layout
        viewPager2Settings.setAdapter(viewPagerAdapterSettings);
        new TabLayoutMediator(tabLayoutSettings, viewPager2Settings,
                ((tab, position) -> tab.setText(titles[position]))).attach();

        // click on change password
        cardChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });

        // click on delete account
        cardDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAccount();
            }
        });

        // click on policy
        cardPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsActivity.this);
                alertDialog.setTitle("Policy")
                        .setMessage(policyContent())
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        });

        // click on version
        cardVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "You have updated to the latest version", Toast.LENGTH_SHORT).show();
            }
        });

        // click on logout
        cardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //log out
                fAuth.signOut();
                // navigate to login activity
                finishAffinity();
                startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
            }
        });

    }

    // ------------------ Function --------------------------

    private void deleteAccount() {

        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_layout_deleteacc, null);
        BottomSheetDialog bottomSheetDialogDeleteAcc = new BottomSheetDialog(SettingsActivity.this);
        bottomSheetDialogDeleteAcc.setContentView(sheetView);
        bottomSheetDialogDeleteAcc.setCancelable(false);
        bottomSheetDialogDeleteAcc.show();

        // click on close bottom sheet
        sheetView.findViewById(R.id.bottomSheetDeleteAccClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // close bottom sheet dialog
                bottomSheetDialogDeleteAcc.dismiss();
            }
        });

        // check email
        TextInputEditText etEmail = sheetView.findViewById(R.id.bottomSheetDeleteAccEmail);
        TextInputLayout layoutEmail = sheetView.findViewById(R.id.layout_bottomSheetDeleteAccEmail);
        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean onFocus) {
                if (!onFocus) {
                    if (etEmail.getText().toString().trim().equals("")) {
                        layoutEmail.setHelperText("Email cannot be blank!");
                    } else if (!isCorrectEmailFormat(etEmail.getText().toString().trim())) {
                        layoutEmail.setHelperText("The email you just entered is not in the correct format");
                    } else if (!etEmail.getText().toString().trim().equals(fAuth.getCurrentUser().getEmail())) {
                        layoutEmail.setHelperText("Email is not registered");
                    }
                } else {
                    layoutEmail.setHelperTextEnabled(false);
                }
            }
        });

        //check password
        TextInputEditText etPassword = sheetView.findViewById(R.id.bottomSheetDeleteAccPass);
        TextInputLayout layoutPassword = sheetView.findViewById(R.id.layout_bottomSheetDeleteAccPass);
        etPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean onFocus) {
                if (!onFocus) {
                    if (etPassword.getText().toString().equals("")) {
                        layoutPassword.setHelperText("Password can not be blank");
                    } else {
                        AuthCredential credential = EmailAuthProvider
                                .getCredential(user.getEmail(), etPassword.getText().toString());
                        user.reauthenticate(credential).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                layoutPassword.setHelperText("The password you entered is incorrect");
                            }
                        });
                    }
                } else {
                    layoutPassword.setHelperTextEnabled(false);
                }
            }
        });

        // click on send code
        Button btnSendCode = sheetView.findViewById(R.id.btnSendDeleteCode);
        TextView tvTimer = sheetView.findViewById(R.id.tvDeleteAccTimer);
        Random random = new Random();
        String verificationCode = String.valueOf(random.nextInt(999999 - 100000) + 100000);
        btnSendCode.setOnClickListener(new View.OnClickListener() {
            private int time = 30;
            @Override
            public void onClick(View view) {
                // send code to user email
                sendCodeByEmail(etEmail.getText().toString().trim(), verificationCode);
                System.out.println("VERIFICATION CODE: " + verificationCode);
                closeKeyboard(sheetView);
                // delay button send code
                btnSendCode.setEnabled(false);
                tvTimer.setVisibility(View.VISIBLE);
                new CountDownTimer(30000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        tvTimer.setText(String.format("Resend later: %s", String.valueOf(time)));
                        time--;
                    }
                    public void onFinish() {
                        btnSendCode.setEnabled(true);
                        tvTimer.setVisibility(View.GONE);
                    }
                }.start();
            }
        });

        // check code
        TextInputEditText etVerify = sheetView.findViewById(R.id.bottomSheetDeleteAccVerify);
        TextInputLayout layoutVerify = sheetView.findViewById(R.id.layout_bottomSheetDeleteAccVerify);
        layoutVerify.setEndIconOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                if (!etVerify.getText().toString().equals(verificationCode)) {
                    layoutVerify.setHelperText("Verification code is incorrect");
                } else {
                    layoutVerify.setHelperText("Valid verification code");
                    layoutVerify.setHelperTextColor(ColorStateList.valueOf(R.color.light_green));
                }
                closeKeyboard(sheetView);
            }
        });

        // click on checkbox
        CheckBox cbConfirmDeleteAcc = sheetView.findViewById(R.id.cbConfirmDeleteAcc);
        Button confirmBtn = sheetView.findViewById(R.id.bottomSheetDeleteAccBtn);

        cbConfirmDeleteAcc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked && etVerify.getText().toString().equals(verificationCode)) {
                    confirmBtn.setEnabled(true);
                } else {
                    confirmBtn.setEnabled(false);
                }
            }
        });


        // click on confirm button
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // delete account here
                Toast.makeText(SettingsActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();

                // navigate to login activity
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bottomSheetDialogDeleteAcc.dismiss();
                        fAuth.signOut();
                        finishAffinity();
                        startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                    }
                }, 1000);
            }
        });

    }

    private void changePassword() {

        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_layout_changepass, null);
        BottomSheetDialog bottomSheetDialogChangePass = new BottomSheetDialog(SettingsActivity.this);
        bottomSheetDialogChangePass.setContentView(sheetView);
        bottomSheetDialogChangePass.setCancelable(false);
        bottomSheetDialogChangePass.show();

        // click on close bottom sheet
        sheetView.findViewById(R.id.bottomSheetChangePassClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // close bottom sheet dialog
                bottomSheetDialogChangePass.dismiss();
            }
        });

        //check old password
        TextInputEditText etOldPassword = sheetView.findViewById(R.id.bottomSheetChangePassOldPass);
        TextInputLayout layoutOldPassword = sheetView.findViewById(R.id.layout_bottomSheetChangePassOldPass);
        etOldPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean onFocus) {
                if (!onFocus) {
                    if (etOldPassword.getText().toString().equals("")) {
                        layoutOldPassword.setHelperText("Password can not be blank");
                    } else {
                        AuthCredential credential = EmailAuthProvider
                                .getCredential(user.getEmail(), etOldPassword.getText().toString());
                        user.reauthenticate(credential).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                layoutOldPassword.setHelperText("The password you entered is incorrect");
                            }
                        });
                    }
                } else {
                    layoutOldPassword.setHelperTextEnabled(false);
                }
            }
        });

        // Check input data: new password
        TextInputEditText etNewPassword = sheetView.findViewById(R.id.bottomSheetChangePassNewPass);
        TextInputLayout layoutNewPassword = sheetView.findViewById(R.id.layout_bottomSheetChangePassNewPass);
        etNewPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean onFocus) {
                if (!onFocus) {
                    if (etNewPassword.getText().toString().equals("")) {
                        layoutNewPassword.setHelperText("Password can not be blank!");
                    } else if (!isLongEnough(etNewPassword.getText().toString(), 8)) {
                        layoutNewPassword.setHelperText("Password must have at least 8 characters");
                    } else if (isCorrectTextFormat(etNewPassword.getText().toString())) {
                        layoutNewPassword.setHelperText("Passwords only include numbers, letters and the characters _ . -");
                    }
                } else {
                    layoutNewPassword.setHelperTextEnabled(false);
                }
            }
        });

        // Check input data: new re-password
        Button confirmBtn = sheetView.findViewById(R.id.bottomSheetChangePassBtn);
        TextInputEditText etReNewPassword = sheetView.findViewById(R.id.bottomSheetChangePassReNewPass);
        TextInputLayout layoutReNewPassword = sheetView.findViewById(R.id.layout_bottomSheetChangePassReNewPass);
        etReNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (etReNewPassword.getText().toString().equals(etNewPassword.getText().toString())) {
                    layoutReNewPassword.setHelperTextEnabled(false);
                    confirmBtn.setEnabled(true);
                } else {
                    layoutReNewPassword.setHelperText("Password was wrong!");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.updatePassword(etNewPassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User password updated.");
                                    Toast.makeText(SettingsActivity.this, "Password has been updated", Toast.LENGTH_SHORT).show();
                                    // require user re login
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            bottomSheetDialogChangePass.dismiss();
                                            fAuth.signOut();
                                            finishAffinity();
                                            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                                        }
                                    }, 1000);
                                }
                            }
                        });
            }
        });
    }

    private void sendCodeByEmail(String receiverEmail, String verificationCode) {

        try {
            String senderEmail = "hackdroid1001@gmail.com";
            String senderPassword = "hulwohxxyuihmesr";

            String stringHost = "smtp.gmail.com";

            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", stringHost);
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

            javax.mail.Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });

            // send email to user: receiverEmail
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(receiverEmail));

            mimeMessage.setSubject("[OnlineShoppingApp] Verification code to request account deletion");
            mimeMessage.setText("Hello," +
                    "\n\nYou just requested a verification code to delete your account " +
                    "with email address: " + receiverEmail +
                    "\n\nVerification code: " + verificationCode +
                    "\n\nPlease enter the verification code above so we can continue " +
                    "steps to delete your account." +
                    "\n\nSincerely,\nOnlineShoppingApp Team");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(mimeMessage);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    private void closeKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private boolean isCorrectEmailFormat(String str) {
        if (str.matches("[a-zA-Z0-9._-]+@[a-z]+\\.[a-zA-Z0-9.]+"))
            return true;
        return false;
    }

    private boolean isCorrectTextFormat(String str) {
        if (str.matches("[^a-zA-Z0-9._-]"))
            return true;
        return false;
    }

    private boolean isLongEnough(String str, int num) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            count++;
        }
        if (count >= num) return true;
        else return false;
    }

    private String policyContent() {
        String content = "1. In the course of providing you with the Services (as defined in the Terms of Use) or access to the Online Shopping Application Platform (as defined in the Terms of Use), we I will collect, use, disclose, store and/or process data, including your personal data. In this Privacy Policy, the application will also be referred to the relevant Merchant's platform(s) (as defined in the Operating Rules).\n" +
                "\n" +
                "2. This Privacy Policy is designed to help you know how we collect, use, disclose, store and/or process data that we collect and receive in the course of providing provide the Services or provide access to the application to You, our user, whether You are using our application as a Customer (as defined in our Policies and Procedures). ) or Vendor. We will only collect, use, disclose, store and/or process your personal data in accordance with this Privacy Policy.\n" +
                "\n" +
                "3. You should read this Privacy Policy in conjunction with any other applicable notices we may provide in specific circumstances when we collect, use, disclose and/or process personal data about you, so that you are fully aware of how and why we use your personal data.";
        return content;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
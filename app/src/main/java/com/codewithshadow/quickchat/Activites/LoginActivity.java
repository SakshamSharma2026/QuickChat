package com.codewithshadow.quickchat.Activites;


import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;
import com.codewithshadow.quickchat.R;
import com.codewithshadow.quickchat.Utils.AppSharedPreferences;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.thekhaeng.pushdownanim.PushDownAnim;
import java.util.List;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    TextView termsAndConditions;
    CardView btnContinue;
    TextInputEditText editText;
    AppSharedPreferences appSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        appSharedPreferences = new AppSharedPreferences(this);
        mAuth = FirebaseAuth.getInstance();

        //Hooks
        initializeViews();


        String text2 = "To continue, you agree to QuickChat Terms and Conditions.";
        SpannableString ss2 = new SpannableString(text2);
        StyleSpan styleSpan2 = new StyleSpan(Typeface.BOLD);
        ForegroundColorSpan fcsRed2 = new ForegroundColorSpan(Color.BLUE);
        ss2.setSpan(styleSpan2, 36, 57, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss2.setSpan(fcsRed2, 36, 57, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        termsAndConditions.setText(ss2);


        //----------------------------------RunTime Permissions----------------------------------//
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.INTERNET).withListener(
                new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        //Normal Function if user allow permission
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }
        ).check();


        //Continue Button
        btnContinue.setCardBackgroundColor(getResources().getColor(R.color.gray2));


        //EditText
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().equals("") && s.toString().length()!=10) {
                    btnContinue.setEnabled(false);
                    btnContinue.setCardBackgroundColor(getResources().getColor(R.color.gray2));

                } else if (!s.toString().isEmpty() && s.toString().length()==10){
                    btnContinue.setCardBackgroundColor(getResources().getColor(R.color.blue));
                    btnContinue.setEnabled(true);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("") && s.toString().length()!=10) {
                    btnContinue.setEnabled(false);
                    btnContinue.setCardBackgroundColor(getResources().getColor(R.color.gray2));

                } else if (!s.toString().isEmpty() && s.toString().length()==10){
                    btnContinue.setCardBackgroundColor(getResources().getColor(R.color.blue));
                    btnContinue.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("") && s.toString().length()!=10) {
                    btnContinue.setEnabled(false);
                    btnContinue.setCardBackgroundColor(getResources().getColor(R.color.gray2));

                } else if (!s.toString().isEmpty() && s.toString().length()==10){
                    btnContinue.setCardBackgroundColor(getResources().getColor(R.color.blue));
                    btnContinue.setEnabled(true);
                }
            }
        });


        PushDownAnim.setPushDownAnimTo(btnContinue).setOnClickListener(v -> {
            if (TextUtils.isEmpty(Objects.requireNonNull(editText.getText()).toString().trim())) {
                editText.setError("Enter Phone Number");
                editText.requestFocus();
            }
            else if(editText.getText().toString().trim().length()<10) {
                Toast.makeText(LoginActivity.this,"Enter Valid Phone Number",Toast.LENGTH_SHORT).show();
            }
            else{
                Intent intent = new Intent(LoginActivity.this, VerificationOTPActivity.class);
                intent.putExtra("phoneNumber",editText.getText().toString().trim());
                startActivity(intent);
                finish();
            }

        });

    }

    private void initializeViews() {
        termsAndConditions = findViewById(R.id.tandc);
        btnContinue = findViewById(R.id.continue_btn);
        editText = findViewById(R.id.edit_number);
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            updateUI();
        }
    }

    private void updateUI() {
        Intent intent=new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

}

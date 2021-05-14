package com.codewithshadow.quickchat.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.chaos.view.PinView;
import com.codewithshadow.quickchat.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thekhaeng.pushdownanim.PushDownAnim;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class VerificationOTPActivity extends AppCompatActivity {


    CardView btnContinue;
    PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private String mVerificationId;
    private FirebaseAuth firebaseAuth;
    FirebaseUser user;
    String  stringPhoneNumber;
    PinView checkOtp;
    ProgressBar progressBar;
    TextView btnResendOtp;
    TextView otpText;



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_o_t_p);

        //Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();


        //Intent Data
        Intent intent = getIntent();
        stringPhoneNumber = intent.getStringExtra("phoneNumber");

        //Hooks
        initializeViews();

        otpText.setText("Enter the OTP sent to +91 " + stringPhoneNumber);

        //Continue Button
        btnContinue.setEnabled(false);
        btnContinue.setCardBackgroundColor(getResources().getColor(R.color.gray2));

        //ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        btnResendOtp.setEnabled(false);

        //Functions
        startPhoneVerification(stringPhoneNumber);
        reverseTimer(60,btnResendOtp);

    }



    //-------------------------Phone Verification Start------------------------//

    private void startPhoneVerification(String phone) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber("+91" + phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        String code= phoneAuthCredential.getSmsCode();
                        checkOtp.setText(code);
                        btnContinue.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        btnContinue.setEnabled(true);
                        signInWithPhoneAuth(phoneAuthCredential);

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        progressBar.setVisibility(View.GONE);
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            otpText.setError("Invalid phone number.");
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            Toast.makeText(VerificationOTPActivity.this, "Trying too many times", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        super.onCodeSent(s, forceResendingToken);
                        mVerificationId = s;
                        forceResendingToken = token;
                        progressBar.setVisibility(View.GONE);
                        checkOtp.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                if (s.toString().equals("") && s.toString().length()!=6) {
                                    btnContinue.setEnabled(false);
                                    btnContinue.setCardBackgroundColor(getResources().getColor(R.color.gray2));

                                } else if (!s.toString().isEmpty() && s.toString().length()==6){
                                    btnContinue.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                    btnContinue.setEnabled(true);
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                        PushDownAnim.setPushDownAnimTo(btnContinue).setOnClickListener(v -> {
                            String code = Objects.requireNonNull(checkOtp.getText()).toString().trim();
                            if (TextUtils.isEmpty(code)) {
                                Toast.makeText(VerificationOTPActivity.this,"Enter OTP",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                VerificationOtp(mVerificationId,code);
                            }
                        });
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }


    //-------------------OTP Verification----------------//

    private void VerificationOtp(String verificationId,String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,code);
        signInWithPhoneAuth(credential);
    }


    //---------------------------Firebase SignIn Auth-----------------------------//

    private void signInWithPhoneAuth(PhoneAuthCredential credential) {
        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if (snapshot.child(Objects.requireNonNull(firebaseAuth.getUid())).exists()){
                                progressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(VerificationOTPActivity.this, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                progressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(VerificationOTPActivity.this, UserDetailsActivity.class);
                                intent.putExtra("phone",stringPhoneNumber);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                }).addOnFailureListener(e -> System.out.println(e.getMessage()));


    }



    //------------------------Reverse Timer------------------------//

    public void reverseTimer(int Seconds, final TextView tv) {

        new CountDownTimer(Seconds * 1000 + 1000, 1000) {

            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);

                int hours = seconds / (60 * 60);
                int tempMint = (seconds - (hours * 60 * 60));
                int minutes = tempMint / 60;
                seconds = tempMint - (minutes * 60);

                tv.setText("Request a new OTP in " + String.format("%02d", minutes)
                        + ":" + String.format("%02d", seconds));
            }

            @SuppressLint("SetTextI18n")
            public void onFinish() {
                tv.setText("Resend OTP");
                tv.setEnabled(true);
                tv.setOnClickListener(v -> {
                    tv.setTextColor(Color.BLACK);
                    resendOTP(stringPhoneNumber,forceResendingToken);
                });
            }
        }.start();
    }


    //----------------------------Resend OTP----------------------------//

    private void resendOTP(String phone,PhoneAuthProvider.ForceResendingToken token){
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber("+91" + phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setForceResendingToken(token)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        String code= phoneAuthCredential.getSmsCode();
                        checkOtp.setText(code);
                        Toast.makeText(VerificationOTPActivity.this,"Code Received" + code,Toast.LENGTH_LONG).show();
                        signInWithPhoneAuth(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        progressBar.setVisibility(View.GONE);
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            otpText.setError("Invalid phone number.");
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            Toast.makeText(VerificationOTPActivity.this, "Trying too many times", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        super.onCodeSent(s, forceResendingToken);
                        progressBar.setVisibility(View.GONE);
                        mVerificationId = s;
                        forceResendingToken = token;
                        PushDownAnim.setPushDownAnimTo(btnContinue).setOnClickListener(v -> {
                            String code = Objects.requireNonNull(checkOtp.getText()).toString().trim();
                            if (TextUtils.isEmpty(code)) {
                                Toast.makeText(VerificationOTPActivity.this,"Enter OTP",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                btnContinue.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                                btnContinue.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                VerificationOtp(mVerificationId,code);
                            }
                        });
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }


    //---------------------------Initialize Views----------------------------//

    private void initializeViews() {
        otpText = findViewById(R.id.textView);
        btnResendOtp = findViewById(R.id.btn_resend_otp);
        btnContinue = findViewById(R.id.continue_btn);
        checkOtp = findViewById(R.id.validuserotp);
        progressBar = findViewById(R.id.button_progress);
    }


}
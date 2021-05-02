package com.codewithshadow.quickchat.Activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.codewithshadow.quickchat.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.util.concurrent.TimeUnit;

public class VerificationOTPActivity extends AppCompatActivity {
    FrameLayout btnContinue;
    PhoneAuthProvider.ForceResendingToken forceResendingToken;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private String mVerificationId;
    private FirebaseAuth firebaseAuth;
    String  stringPhoneNumber;
    PinView checkOtp;
    ProgressBar progressBar;
    TextView btnResendOtp;
    TextView otpText;
    FirebaseUser user;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_o_t_p);
        firebaseAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = getIntent();
        stringPhoneNumber = intent.getStringExtra("phoneNumber");
        otpText = findViewById(R.id.textView);
        otpText.setText("Enter the OTP sent to +91 " + stringPhoneNumber);
        btnResendOtp = findViewById(R.id.btn_resend_otp);
        btnContinue = findViewById(R.id.continue_btn);
        btnContinue.setEnabled(false);
        btnContinue.setBackgroundColor(getResources().getColor(R.color.gray2));
        checkOtp = findViewById(R.id.validuserotp);
        progressBar = findViewById(R.id.button_progress);
        progressBar.setVisibility(View.VISIBLE);
        btnResendOtp.setEnabled(false);
        startPhoneVerification(stringPhoneNumber);
        reverseTimer(60,btnResendOtp);

    }

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
                        btnContinue.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        btnContinue.setEnabled(true);
                        signInWithPhoneAuth(phoneAuthCredential);

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        progressBar.setVisibility(View.GONE);
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            otpText.setError("Invalid phone number.");
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            Toast.makeText(VerificationOTPActivity.this, "Trying too many timeS", Toast.LENGTH_SHORT).show();
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

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                if (s.toString().equals("") && s.toString().length()!=6) {
                                    btnContinue.setEnabled(false);
                                    btnContinue.setBackgroundColor(getResources().getColor(R.color.gray2));

                                } else if (!s.toString().isEmpty() && s.toString().length()==6){
                                    btnContinue.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                    btnContinue.setEnabled(true);
                                }
                            }
                        });
                        PushDownAnim.setPushDownAnimTo(btnContinue).setOnClickListener(v -> {
                            String code = checkOtp.getText().toString().trim();
                            if (TextUtils.isEmpty(code)) {
                                Toast.makeText(VerificationOTPActivity.this,"Enter OTP",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                VerifitionOtp(mVerificationId,code);
                            }
                        });
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }


    private void VerifitionOtp(String verificationId,String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,code);
        signInWithPhoneAuth(credential);
    }

    private void signInWithPhoneAuth(PhoneAuthCredential credential) {
        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
                        ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.child(firebaseAuth.getUid()).exists()){
                                    progressBar.setVisibility(View.GONE);
                                    Intent intent = new Intent(VerificationOTPActivity.this, HomeActivity.class);
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
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public void reverseTimer(int Seconds, final TextView tv) {

        new CountDownTimer(Seconds * 1000 + 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);

                int hours = seconds / (60 * 60);
                int tempMint = (seconds - (hours * 60 * 60));
                int minutes = tempMint / 60;
                seconds = tempMint - (minutes * 60);

                tv.setText("Request a new OTP in " + String.format("%02d", minutes)
                        + ":" + String.format("%02d", seconds));
            }

            public void onFinish() {
                tv.setText("Resend OTP");
                tv.setEnabled(true);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
        }.start();
    }

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
                            Toast.makeText(VerificationOTPActivity.this, "Trying too many timeS", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        super.onCodeSent(s, forceResendingToken);
                        progressBar.setVisibility(View.GONE);
                        mVerificationId = s;
                        forceResendingToken = token;
                        PushDownAnim.setPushDownAnimTo(btnContinue).setOnClickListener(v -> {
                            String code = checkOtp.getText().toString().trim();
                            if (TextUtils.isEmpty(code)) {
                                Toast.makeText(VerificationOTPActivity.this,"Enter OTP",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                btnContinue.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                                btnContinue.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                VerifitionOtp(mVerificationId,code);
                            }
                        });
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }


}
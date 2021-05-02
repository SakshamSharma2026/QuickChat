package com.codewithshadow.quickchat.Activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
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
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codewithshadow.quickchat.R;
import com.codewithshadow.quickchat.Utils.AppSharedPreferences;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    FirebaseAuth mAuth;
    TextView tandc;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FrameLayout btnContinue;
    TextInputEditText editText;
    private FirebaseAuth auth;
    private GoogleApiClient googleApiClient;
    private static final int RESOLVE_HINT = 1;
    AppSharedPreferences appSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        appSharedPreferences = new AppSharedPreferences(this);
        mAuth = FirebaseAuth.getInstance();
        tandc = findViewById(R.id.tandc);
        String text2 = "To continue, you agree to QuickChat Terms and Conditions.";
        SpannableString ss2 = new SpannableString(text2);
        StyleSpan styleSpan2 = new StyleSpan(Typeface.BOLD);
        ForegroundColorSpan fcsRed2 = new ForegroundColorSpan(Color.BLUE);
        ss2.setSpan(styleSpan2, 36, 57, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss2.setSpan(fcsRed2, 36, 57, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tandc.setText(ss2);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();


        btnContinue = findViewById(R.id.continue_btn);
        btnContinue.setBackgroundColor(getResources().getColor(R.color.gray2));

        editText = findViewById(R.id.edit_number);
        editText.setOnFocusChangeListener((v,hasFocus) -> {
            if (hasFocus){
                try{
                    requestHint();
                }catch (IntentSender.SendIntentException e){
                    e.printStackTrace();
                }
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().equals("") && s.toString().length()!=10) {
                    btnContinue.setEnabled(false);
                    btnContinue.setBackgroundColor(getResources().getColor(R.color.gray2));

                } else if (!s.toString().isEmpty() && s.toString().length()==10){
                    btnContinue.setBackgroundColor(getResources().getColor(R.color.blue));
                    btnContinue.setEnabled(true);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("") && s.toString().length()!=10) {
                    btnContinue.setEnabled(false);
                    btnContinue.setBackgroundColor(getResources().getColor(R.color.gray2));

                } else if (!s.toString().isEmpty() && s.toString().length()==10){
                    btnContinue.setBackgroundColor(getResources().getColor(R.color.blue));
                    btnContinue.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("") && s.toString().length()!=10) {
                    btnContinue.setEnabled(false);
                    btnContinue.setBackgroundColor(getResources().getColor(R.color.gray2));

                } else if (!s.toString().isEmpty() && s.toString().length()==10){
                    btnContinue.setBackgroundColor(getResources().getColor(R.color.blue));
                    btnContinue.setEnabled(true);
                }
            }
        });


        PushDownAnim.setPushDownAnimTo(btnContinue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editText.getText().toString().trim())) {
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

            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                Pattern compile = Pattern.compile("\\+(?:998|996|995|994|993|992|977|976|975|974|973|972|971|970|968|967|966|965|964|963|962|961|960|886|880|856|855|853|852|850|692|691|690|689|688|687|686|685|683|682|681|680|679|678|677|676|675|674|673|672|670|599|598|597|595|593|592|591|590|509|508|507|506|505|504|503|502|501|500|423|421|420|389|387|386|385|383|382|381|380|379|378|377|376|375|374|373|372|371|370|359|358|357|356|355|354|353|352|351|350|299|298|297|291|290|269|268|267|266|265|264|263|262|261|260|258|257|256|255|254|253|252|251|250|249|248|246|245|244|243|242|241|240|239|238|237|236|235|234|233|232|231|230|229|228|227|226|225|224|223|222|221|220|218|216|213|212|211|98|95|94|93|92|91|90|86|84|82|81|66|65|64|63|62|61|60|58|57|56|55|54|53|52|51|49|48|47|46|45|44\\D?1624|44\\D?1534|44\\D?1481|44|43|41|40|39|36|34|33|32|31|30|27|20|7|1\\D?939|1\\D?876|1\\D?869|1\\D?868|1\\D?849|1\\D?829|1\\D?809|1\\D?787|1\\D?784|1\\D?767|1\\D?758|1\\D?721|1\\D?684|1\\D?671|1\\D?670|1\\D?664|1\\D?649|1\\D?473|1\\D?441|1\\D?345|1\\D?340|1\\D?284|1\\D?268|1\\D?264|1\\D?246|1\\D?242|1)\\D?");
                String number = credential.getId().replaceAll(compile.pattern(), "");
                editText.setText(number);
            }
        }
    }

    private void requestHint() throws IntentSender.SendIntentException {
        HintRequest hintRequest =
                new HintRequest.Builder()
                        .setPhoneNumberIdentifierSupported(true)
                        .build();
        PendingIntent mIntent = Auth.CredentialsApi.getHintPickerIntent(googleApiClient, hintRequest);
        try {
            startIntentSenderForResult(mIntent.getIntentSender(), RESOLVE_HINT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }

    }

//-----------------GOOGLE FUNCTION----------------//

//    private void googlesignIn() {
//        Intent intent = client.getSignInIntent();
//        startActivityForResult(intent, Rc_Sign_in);
//    }


//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        mCallbackManager.onActivityResult(requestCode, resultCode, data);
//
//        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
//        if (requestCode == Rc_Sign_in) {
//            Toast.makeText(LoginActivity.this, "Authentication is in progress...", Toast.LENGTH_LONG).show();
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                // Google Sign In was successful, authenticate with Firebase
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                String email = account.getEmail();
//                user_name = account.getDisplayName();
//                String imageUrl = account.getPhotoUrl().toString();
//                imageUrl = imageUrl.substring(0, imageUrl.length() - 5) + "s400-c";
//
//                //Url Change HoSakta hai
//
//                FirebaseGoogleAuth(account.getIdToken(), user_name, email, imageUrl);
//            } catch (ApiException e) {
//                Toast.makeText(LoginActivity.this, "Signin Error", Toast.LENGTH_LONG).show();
//                // Google Sign In failed, update UI appropriately
//                Log.w("Error", "Google sign in failed", e);
//            }
//        }
//
//    }

//    private void FirebaseGoogleAuth(String idToken, final String user_name, final String email, final String imageUrl) {
//        button_loginprogress.setVisibility(View.VISIBLE);
//        AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
//        mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()) {
//                    final FirebaseUser user = mAuth.getCurrentUser();
//                    DocumentReference documentReference = db.collection("Users").document(mAuth.getCurrentUser().getUid());
//                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                            String token = task.getResult().getToken();
//                            Map<String, Object> map = new HashMap<>();;
//                            map.put("name",user_name);
//                            map.put("email",email);
//                            map.put("imgurl",imageUrl);
//                            map.put("token",token);
//                            map.put("userid",mAuth.getCurrentUser().getUid());
//                            documentReference.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    Intent intent1 = new Intent(LoginActivity.this, HomeActivity.class);
//                                    startActivity(intent1);
//                                    finish();
//                                    MDToast mdToast = MDToast.makeText(LoginActivity.this, "Authentication Successful Welcome: " + user.getDisplayName(), MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS);
//                                    mdToast.show();
//                                }
//                            });
//                        }
//                    });
//
//                } else {
//                    // If sign in fails, display a message to the user.
//                    MDToast mdToast = MDToast.makeText(getApplicationContext(), "Signed In Failed", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR);
//                    mdToast.show();
//                    button_loginprogress.setVisibility(View.GONE);
//                }
//            }
//        });
//    }

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

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}

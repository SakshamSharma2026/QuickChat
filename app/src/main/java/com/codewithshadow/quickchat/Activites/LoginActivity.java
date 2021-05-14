package com.codewithshadow.quickchat.Activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.Manifest;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.thekhaeng.pushdownanim.PushDownAnim;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    FirebaseAuth mAuth;
    TextView termsAndConditions;
    CardView btnContinue;
    TextInputEditText editText;
    private GoogleApiClient googleApiClient;
    private static final int RESOLVE_HINT = 1;
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
        editText.setOnFocusChangeListener((v,hasFocus) -> {
            if (hasFocus){
                try{
                    requestHint();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

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


        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();

    }

    private void initializeViews() {
        termsAndConditions = findViewById(R.id.tandc);
        btnContinue = findViewById(R.id.continue_btn);
        editText = findViewById(R.id.edit_number);
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

    private void requestHint() {
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

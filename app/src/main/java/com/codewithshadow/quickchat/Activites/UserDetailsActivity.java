package com.codewithshadow.quickchat.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codewithshadow.quickchat.R;
import com.codewithshadow.quickchat.Utils.LoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.HashMap;

public class UserDetailsActivity extends AppCompatActivity {
    ImageView profileImage;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;
    private StorageReference mStorageRef;
    FirebaseAuth auth;
    FirebaseUser user;
    LoadingDialog loadingDialog;
    EditText editText;
    Button btnNext;
    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phone");
        loadingDialog = new LoadingDialog(this);
        profileImage = findViewById(R.id.item_profile_picture);
        btnNext  = findViewById(R.id.btn_next);
        editText = findViewById(R.id.editText);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editText.getText().toString())){
                    editText.setError("Enter name");
                    editText.requestFocus();
                }
                else{
                    uploadFile(editText.getText().toString());
                }
            }
        });
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null ) {
            mImageUri = data.getData();
            CropImage.activity(mImageUri)
                    .setAspectRatio(4,5)
//                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                Glide.with(this).load(mImageUri) .apply(RequestOptions.circleCropTransform())
                        .into(profileImage);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(UserDetailsActivity.this,"" + error,Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadFile(String editText) {
        if (mImageUri != null) {
            loadingDialog.startloadingDialog();
            final StorageReference reference = mStorageRef.child("Files/"+System.currentTimeMillis() );

            reference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                        @Override
                                        public void onComplete(@NonNull Task<String> task) {
                                            if (task.isComplete()) {

                                                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                DatabaseReference ref  = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
                                                String token = task.getResult();
                                                String imageUrl=uri.toString();
                                                HashMap<String, Object> map = new HashMap<>();;
                                                map.put("name",editText);
                                                map.put("imgurl",imageUrl);
                                                map.put("token",token);
                                                map.put("phone",phoneNumber);
                                                map.put("status","online");
                                                map.put("userid",user.getUid());
                                                ref.child("Info").setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        loadingDialog.dismissDialog();
                                                        Intent intent1 = new Intent(UserDetailsActivity.this, HomeActivity.class);
                                                        startActivity(intent1);
                                                        finish();
                                                        MDToast mdToast = MDToast.makeText(UserDetailsActivity.this, "Authentication Successful Welcome: " + editText, MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS);
                                                        mdToast.show();
                                                    }
                                                });

                                            }
                                        }
                                    });
                                    UserProfileChangeRequest profileUpdate=new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(uri)
                                            .build();
                                    user.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });

                                }
                            });
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadingDialog.dismissDialog();
                            Toast.makeText(UserDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            loadingDialog.dismissDialog();
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

}
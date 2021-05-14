package com.codewithshadow.quickchat.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.codewithshadow.quickchat.R;
import com.codewithshadow.quickchat.Utils.ImageCompressor;
import com.codewithshadow.quickchat.Utils.KeyboardUtils;
import com.codewithshadow.quickchat.Utils.LoadingDialog;
import com.codewithshadow.quickchat.Utils.UniversalImageLoderClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.thekhaeng.pushdownanim.PushDownAnim;
import org.jetbrains.annotations.NotNull;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    CircleImageView profilePic;
    TextView name,number;
    DatabaseReference ref;
    FirebaseUser user;
    RelativeLayout logout;
    FirebaseAuth auth;
    ImageView backBtn,btnEditName;
    BottomSheetDialog bottomSheetDialog;
    private static final int PICK_IMAGE_REQUEST = 1;
    CardView updateProfile;
    private Uri mImageUri;
    LoadingDialog loadingDialog;
    private StorageReference mStorageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        loadingDialog = new LoadingDialog(this);

        //Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();
        ref  = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //Hooks
        initializeViews();

         //FirebaseDatabase
        ref.child("Info").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                String imgUrl = snapshot.child("imgUrl").getValue(String.class);
                String nameString = snapshot.child("name").getValue(String.class);
                String phoneString = snapshot.child("phone").getValue(String.class);
                UniversalImageLoderClass.setImage(imgUrl,profilePic,null);
                name.setText(nameString);
                number.setText("+91 " + phoneString);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        btnEditName.setOnClickListener(v -> {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
            bottomSheetDialog=new BottomSheetDialog(UserProfileActivity.this,R.style.BottomSheetDialogStyle);
            View bottomsheetview = LayoutInflater.from(UserProfileActivity.this).inflate(R.layout.highlight_bottomsheet,(ScrollView)findViewById(R.id.rll));
            KeyboardUtils  k = new KeyboardUtils();
            k.KeyboardUtil(this,bottomsheetview);

            bottomSheetDialog.setContentView(bottomsheetview);
            bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            bottomSheetDialog.getWindow().getAttributes().gravity = Gravity.BOTTOM;
            bottomSheetDialog.show();

            TextView btnAdd = bottomSheetDialog.findViewById(R.id.btnAdd);;
            TextView btnCancel = bottomSheetDialog.findViewById(R.id.btnCancel);

            EditText btnEdit = bottomsheetview.findViewById(R.id.addtext);
            btnEdit.setText(name.getText().toString());
            btnAdd.setOnClickListener(v1 -> {
                final String name = btnEdit.getText().toString();
                Map<String,Object> map = new HashMap<String, Object>();
                map.put("name",name);
                reference.child("Info").updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        bottomSheetDialog.dismiss();
                    }
                });

            });

            btnCancel.setOnClickListener(v1 -> bottomSheetDialog.dismiss());

            //use a log message


        });


        //Logout Button
        PushDownAnim.setPushDownAnimTo(logout).setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  //makeSure user cant go back
            startActivity(intent);
            finish();
            Toast.makeText(UserProfileActivity.this,"Logout",Toast.LENGTH_LONG).show();
        });

        updateProfile.setOnClickListener(v -> openFileChooser());


    }

    private void initializeViews() {
        profilePic = findViewById(R.id.item_profile_picture);
        name = findViewById(R.id.item_text_1);
        number = findViewById(R.id.item_text_2);
        backBtn = findViewById(R.id.backbtn);
        backBtn.setOnClickListener(v -> finish());
        logout = findViewById(R.id.btnlogout);
        btnEditName = findViewById(R.id.btnEditName);
        updateProfile = findViewById(R.id.update_profile);
    }



    //-----------------------------Open Image Intent-----------------------------//
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    //-------------------------------Handle Activity Result------------------------------//
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null ) {
            mImageUri = data.getData();
            CropImage.activity(mImageUri)
                    .setAspectRatio(4,5)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                try {
                    ImageCompressor imageCompressor = new ImageCompressor();
                    Bitmap src = imageCompressor.decodeFile(mImageUri.getPath());
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    src.compress(Bitmap.CompressFormat.PNG, 80, os);
                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), src, "title", null);
                    mImageUri  = Uri.parse(path);
                    uploadFile(mImageUri);

                }
                catch (Exception e){
                    e.printStackTrace();
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(UserProfileActivity.this,"" + error,Toast.LENGTH_SHORT).show();
            }
        }
    }

    //-------------------------------Upload User Image-------------------------------//
    private void uploadFile(Uri mImageUri) {
        if (mImageUri!=null){
            loadingDialog.startloadingDialog();
            loadingDialog.textDiaglog("loading...");
            final StorageReference reference = mStorageRef.child("Files/"+System.currentTimeMillis() );
            reference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    DatabaseReference ref  = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
                                    HashMap<String, Object> map = new HashMap<>();
                                    String imageUrl=uri.toString();
                                    map.put("imgUrl",imageUrl);
                                    ref.child("Info").updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            loadingDialog.dismissDialog();
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
                            Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }
}
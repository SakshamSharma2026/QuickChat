package com.codewithshadow.quickchat.Activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.codewithshadow.quickchat.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity {
    private Uri mImageUri;
    String myUrl = "";
    private StorageReference mStorageReference;
    private StorageTask storageTask;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);
        mStorageReference = FirebaseStorage.getInstance().getReference("Story");

        CropImage.activity(mImageUri)
                .setAspectRatio(9,16)
                .start(AddStoryActivity.this);



    }




    private String getFileExtenstion(Uri uri)
    {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void publishStory()
    {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();

        if (mImageUri!=null)
        {
            StorageReference imageReference = mStorageReference.child(System.currentTimeMillis()+"."+
                    getFileExtenstion(mImageUri));

            storageTask = imageReference.putFile(mImageUri);
            storageTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful())
                    {
                        throw  task.getException();
                    }
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful())
                    {
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        String myid  = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        String storyId = FirebaseDatabase.getInstance().getReference().child("AllStories").child("StoryData").push().getKey();

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                                .child("Story").child(myid).child(storyId);

                        long timend = System.currentTimeMillis() + 86400000;
                        Long timestart = System.currentTimeMillis()/1000;


                        HashMap<String, Object> hashMap = new HashMap<>();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        Date date = new Date();
                        hashMap.put("storyimg",myUrl);
                        hashMap.put("timestart", timestart);
                        hashMap.put("timeend",timend);
                        hashMap.put("storyid",storyId);
                        hashMap.put("userId",myid);
                        hashMap.put("timeupload",sdf.format(date));
                        databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                finish();
                            }
                        });

//                        db.collection("AllStories").document("StoryData").collection(myid).document(storyId).set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                progressDialog.dismiss();
//                                finish();
//                            }
//                        });


                    }

                    else
                    {

                        Toast.makeText(AddStoryActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddStoryActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });
        }

        else

        {
            Toast.makeText(AddStoryActivity.this,"Image not Selected",Toast.LENGTH_SHORT).show();

        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();
            publishStory();

        }
        else
        {
            Toast.makeText(AddStoryActivity.this,"Something gone wrong",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddStoryActivity.this, HomeActivity.class));
            finish();
        }



    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
//        {
//            Bitmap photo = (Bitmap) data.getExtras().get("data");
//            imageView.setImageBitmap(photo);
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }


}
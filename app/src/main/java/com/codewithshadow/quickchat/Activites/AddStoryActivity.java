package com.codewithshadow.quickchat.Activites;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;
import com.codewithshadow.quickchat.R;
import com.codewithshadow.quickchat.Utils.ImageCompressor;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class AddStoryActivity extends AppCompatActivity {

    private StorageReference mStorageReference;
    FirebaseUser user;
    ArrayList<String> returnValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        //Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        mStorageReference = FirebaseStorage.getInstance().getReference("Story");


        ChooseImg();

    }

    private void ChooseImg() {
        Options options = Options.init()
                .setRequestCode(100)                                           //Request code for activity results
                .setFrontfacing(false)                                         //Front Facing camera on start
                .setMode(Options.Mode.Picture)                                 //Option to select only pictures or videos or both
                .setPath("/pix/images");                                       //Custom Path For media Storage

        Pix.start(AddStoryActivity.this, options);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {  // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Pix.start(this, Options.init().setRequestCode(100));
            } else {
                Toast.makeText(AddStoryActivity.this, "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 100 && data!=null) {
            returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            for (String s : returnValue) {
                try {
                    Uri image = Uri.fromFile(new File(s));

                    //Image Compressor
                    ImageCompressor imageCompressor = new ImageCompressor();
                    Bitmap src = imageCompressor.decodeFile(image.getPath());
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    src.compress(Bitmap.CompressFormat.PNG, 80, os);
                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), src, "title", null);
                    Uri uri  = Uri.parse(path);

                    //Function
                    publishStory(uri);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }

    private void publishStory(Uri mImageUri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();

        if (mImageUri!=null) {
            Bitmap bitmap= null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),mImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            assert bitmap != null;
            bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
            final StorageReference reference = mStorageReference.child("StoryImages/"+ user.getUid() +"post_"+System.currentTimeMillis() );
            reference.putBytes(data).addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri -> {
                String myId  = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                String storyId = FirebaseDatabase.getInstance().getReference().child("AllStories").child("StoryData").push().getKey();

                assert storyId != null;
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Story").child(myId).child(storyId);

                long timEnd = System.currentTimeMillis() + 86400000;
                Long timeStart = System.currentTimeMillis()/1000;


                HashMap<String, Object> hashMap = new HashMap<>();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date date = new Date();
                hashMap.put("storyImg",uri.toString());
                hashMap.put("timeStart", timeStart);
                hashMap.put("timeEnd",timEnd);
                hashMap.put("storyId",storyId);
                hashMap.put("userId",myId);
                hashMap.put("timeUpload",sdf.format(date));
                databaseReference.setValue(hashMap).addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    File file = new File(mImageUri.getPath());
                    if(file.delete()) {
                        System.out.println("File deleted successfully");
                    }
                    else {
                        System.out.println("Failed to delete the file");
                    }
                    finish();
                });
            })).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(AddStoryActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            });
        }

        else {
            Toast.makeText(AddStoryActivity.this,"Image not Selected",Toast.LENGTH_SHORT).show();

        }
    }
}
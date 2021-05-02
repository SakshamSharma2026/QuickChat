package com.codewithshadow.quickchat.Activites;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.codewithshadow.quickchat.Adapter.SendMessageAdapter;
import com.codewithshadow.quickchat.Models.SendMessageModel;
import com.codewithshadow.quickchat.R;
import com.codewithshadow.quickchat.Utils.AppSharedPreferences;
import com.codewithshadow.quickchat.Utils.LoadingDialog;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.codewithshadow.quickchat.SendNotification.NotiModel.NotificationReq;
import com.codewithshadow.quickchat.SendNotification.NotiModel.NotificationResponce;
import com.codewithshadow.quickchat.SendNotification.NotificationRequest;
import com.codewithshadow.quickchat.SendNotification.RetrofitClient;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;

import static com.codewithshadow.quickchat.SendNotification.Constants.BASE_URL;


public class SendMessageActivity extends AppCompatActivity {

    CircleImageView imageView;
    TextView username;
    String stringusername,stringuserid,stringimgurl;
    EditText typemessage;
    ImageView backbtn;
    FirebaseUser user;
    List<SendMessageModel> list;
    SendMessageAdapter adapter;
    RecyclerView recyclerView;
    LinearLayoutManager manager;
    private StorageReference mStorageRef;
    DatabaseReference ref;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;
    LoadingDialog loadingDialog;
    ArrayList<String> returnValue;
    AppSharedPreferences appSharedPreferences;
    RelativeLayout btn_SendMessage,choose_img;
    ValueEventListener seenListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        imageView = findViewById(R.id.button_image);
        appSharedPreferences = new AppSharedPreferences(this);
        loadingDialog = new LoadingDialog(this);
        username = findViewById(R.id.username);
        user = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        ref = FirebaseDatabase.getInstance().getReference();


        Intent intent = getIntent();
        stringusername = intent.getStringExtra("username");
        stringimgurl  = intent.getStringExtra("imgurl");
        stringuserid = intent.getStringExtra("userid");


        typemessage = findViewById(R.id.item_input);
        btn_SendMessage = findViewById(R.id.item_send_icon_container);
        recyclerView = findViewById(R.id.chatrecycler);
        list = new ArrayList<>();
        manager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);
        backbtn = findViewById(R.id.backbtn);
        ref = FirebaseDatabase.getInstance().getReference();
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        choose_img = findViewById(R.id.emoji_layout);
        PushDownAnim.setPushDownAnimTo(choose_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseImg();
            }
        });

        Read_Message(user.getUid(),stringuserid,stringimgurl);

        typemessage.setOnEditorActionListener(editorActionListener);







        PushDownAnim.setPushDownAnimTo(btn_SendMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String t1 = typemessage.getText().toString();

                if(TextUtils.isEmpty(t1)) {
                    typemessage.setError("Enter Message");
                    typemessage.requestFocus();
                }
                else {
                    Send_Message(t1);
                    typemessage.getText().clear();
                }
            }
        });

        username.setText(stringusername);

        Glide.with(this).load(stringimgurl).into(imageView);

        Seen_Message(stringuserid);

    }

    private TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            switch (actionId){
                case EditorInfo.IME_ACTION_DONE:
                    Send_Message(v.getText().toString());
                    v.setText("");
                    break;

            }
            return false;
        }
    };



    private void ChooseImg() {
        Pix.start(this, Options.init().setRequestCode(100));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(this, Options.init().setRequestCode(100));
                } else {
                    Toast.makeText(SendMessageActivity.this, "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void Seen_Message(String uid) {
        seenListener = ref.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    SendMessageModel sendMessageModel = dataSnapshot.getValue(SendMessageModel.class);
                    if (sendMessageModel.getReceiver().equals(user.getUid()) && sendMessageModel.getSender().equals(uid))
                    {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        dataSnapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        collectionReference = db.collection("Chats");
//        collectionReference.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                if (error!=null) {
//                    return;
//                }
//                SendMessageModel sendMessageModel = null;
//                for (DocumentSnapshot documentSnapshot : value) {
//                    sendMessageModel = documentSnapshot.toObject(SendMessageModel.class);
//                    if (sendMessageModel.getReceiver().equals(user.getUid()) && sendMessageModel.getSender().equals(uid)) {
//                        HashMap<String, Object> hashMap = new HashMap<>();
//                        hashMap.put("isseen",true);
//                        collectionReference.document(documentSnapshot.getId()).update(hashMap);
//                    }
//                }
//
//            }
//        });



    }

    private void Send_Message(String t1) {
        String key = ref.child("Chats").push().getKey();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm a");
        Map<String,Object> map = new HashMap<>();
        map.put("msg",t1);
        map.put("sender",user.getUid());
        map.put("key",key);
        map.put("time", sdf.format(date));
        map.put("type","text");
        map.put("isseen",false);
        map.put("receiver",stringuserid);
        ref.child("Chats").child(key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                sentByRest(stringuserid,t1);
            }
        });
//        db.collection("Chats").document(key).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                sentByRest(stringuserid,t1);
//            }
//        });

    }

    private void sentByRest(String publisherId,String msg) {
        db.collection("Users").document(publisherId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String token  = documentSnapshot.getString("token");
                NotificationReq req = new NotificationReq(token,
                        new NotificationReq.Notification(appSharedPreferences.getUserName(),
                                appSharedPreferences.getUserName() + ": " + msg));


                RetrofitClient.getRetrofit(BASE_URL)
                        .create(NotificationRequest.class)
                        .sent(req)
                        .enqueue(new Callback<NotificationResponce>() {
                            @Override
                            public void onResponse(Call<NotificationResponce> call, retrofit2.Response<NotificationResponce> response) {

                            }

                            @Override
                            public void onFailure(Call<NotificationResponce> call, Throwable t) {
                            }
                        });
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 100 && data!=null) {
            returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            for (String s : returnValue)
            {
                Uri image = Uri.fromFile(new File(s));
                uploadFile(image);
            }



        }
    }




    private void Read_Message(String myid,String userid,String imageurl)
    {
        ref.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot snap : snapshot.getChildren())
                {
                    SendMessageModel model = snap.getValue(SendMessageModel.class);

                    if (model.getReceiver().equals(myid) && model.getSender().equals(userid) || model.getReceiver().equals(userid) && model.getSender().equals(myid))
                    {
                        list.add(model);
                    }

                }
                adapter = new SendMessageAdapter(SendMessageActivity.this,list,imageurl);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        collectionReference = db.collection("Chats");
//        collectionReference.addSnapshotListener(MetadataChanges.INCLUDE,new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                if (error!=null)
//                {
//                    return;
//                }
//
//                list.clear();
//                SendMessageModel model = null;
//                for(DocumentSnapshot snapshot : value)
//                {
//                    model = snapshot.toObject(SendMessageModel.class);
//                    if (model.getReceiver().equals(myid) && model.getSender().equals(userid) || model.getReceiver().equals(userid) && model.getSender().equals(myid))
//                    {
//                        list.add(model);
//                    }
//                }
//                adapter = new SendMessageAdapter(SendMessageActivity.this,list,imageurl);
//                recyclerView.setAdapter(adapter);
//                adapter.notifyDataSetChanged();
//            }
//        });
    }




//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null ) {
//            mImageUri = data.getData();
//            Log.d("saksaham","ASda3d" + mImageUri);
////            uploadFile();
////            CropImage.activity(mImageUri)
////                    .setAspectRatio(4,5)
//////                    .setGuidelines(CropImageView.Guidelines.ON)
////                    .start(this);
//        }
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            if (resultCode == RESULT_OK) {
//                mImageUri = result.getUri();
//
//
//            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                Exception error = result.getError();
//                Toast.makeText(SendMessageActivity.this,"" + error,Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    private void uploadFile(Uri image) {
        loadingDialog.startloadingDialog();
        loadingDialog.textDiaglog("Sending image");
        if (image != null) {
            Bitmap bitmap= null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),image);

            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
            final StorageReference reference = mStorageRef.child("ChatImages/"+ user.getUid() +"post_"+System.currentTimeMillis() );

            reference.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    loadingDialog.dismissDialog();
                                    String key = ref.child("Chats").push().getKey();
                                    Date date = new Date();
                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm a");
                                    Map<String,Object> map = new HashMap<>();
                                    map.put("msg","Sent a photo");
                                    map.put("sender",user.getUid());
                                    map.put("key",key);
                                    map.put("time",sdf.format(date));
                                    map.put("msg_img",uri.toString());
                                    map.put("type","image");
                                    map.put("isseen",false);
                                    map.put("receiver",stringuserid);
                                    ref.child("Chats").child(key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });

//                                    db.collection("Chats").document(key).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//
//                                        }
//                                    });

                                }
                            });
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadingDialog.dismissDialog();
                            Toast.makeText(SendMessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }



    private void status(String status)
    {
        DocumentReference documentReference = db.collection("Users").document(user.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        documentReference.update(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}
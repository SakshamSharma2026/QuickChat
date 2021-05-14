package com.codewithshadow.quickchat.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.codewithshadow.quickchat.Adapter.SendMessageAdapter;
import com.codewithshadow.quickchat.Models.SendMessageModel;
import com.codewithshadow.quickchat.R;
import com.codewithshadow.quickchat.Utils.AESUtils;
import com.codewithshadow.quickchat.Utils.AppSharedPreferences;
import com.codewithshadow.quickchat.Utils.ImageCompressor;
import com.codewithshadow.quickchat.Utils.LoadingDialog;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.codewithshadow.quickchat.SendNotification.NotiModel.NotificationReq;
import com.codewithshadow.quickchat.SendNotification.NotiModel.NotificationResponce;
import com.codewithshadow.quickchat.SendNotification.NotificationRequest;
import com.codewithshadow.quickchat.SendNotification.RetrofitClient;
import com.thekhaeng.pushdownanim.PushDownAnim;
import org.jetbrains.annotations.NotNull;
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
    String stringUsername, stringUserid, stringImgUrl;
    EditText typeMessage;
    ImageView backBtn;
    FirebaseUser user;
    List<SendMessageModel> list;
    SendMessageAdapter adapter;
    RecyclerView recyclerView;
    LinearLayoutManager manager;
    private StorageReference mStorageRef;
    DatabaseReference ref;
    LoadingDialog loadingDialog;
    ArrayList<String> returnValue;
    AppSharedPreferences appSharedPreferences;
    RelativeLayout btn_SendMessage,choose_img;
    ValueEventListener seenListener;
    LinearLayout extra_tools,layout;
    boolean isUp;
    ImageView item_tools_icon;
    CardView layout_image_btn,layout_files_btn;
    private Uri fileUri;
    private String checker;
    private ImageView btnMore;
    LinearLayout block_layout,unblock_layout;
    TextView unblockBtn,block_acc_text;
    RelativeLayout send_message_layout;
    List<String> youBlockList;
    List<String> blockedByList;
    PopupMenu popup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        //Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        ref = FirebaseDatabase.getInstance().getReference();

        //Hooks
        initializeViews();

        //AppSharedPreferences
        appSharedPreferences = new AppSharedPreferences(this);

        //Loading Dialog
        loadingDialog = new LoadingDialog(this);


        //Intent Data
        Intent intent = getIntent();
        stringUsername = intent.getStringExtra("username");
        stringImgUrl = intent.getStringExtra("imgUrl");
        stringUserid = intent.getStringExtra("userid");


        //RecyclerView Data
        manager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);

        recyclerView.setLayoutManager(manager);


        //Back Button
        backBtn.setOnClickListener(view -> finish());


        //Edit Text
        typeMessage.setOnEditorActionListener(editorActionListener);




        isUp = false;



        //Choose Tools
        PushDownAnim.setPushDownAnimTo(choose_img).setOnClickListener(view -> { onSlideViewButtonClick(layout); });


        PushDownAnim.setPushDownAnimTo(btn_SendMessage).setOnClickListener(view -> {
            String t1 = typeMessage.getText().toString();

            if(TextUtils.isEmpty(t1)) {
                typeMessage.setError("Enter Message");
                typeMessage.requestFocus();
            }
            else {
                Send_Message(t1);
                typeMessage.getText().clear();
            }
        });


        username.setText(stringUsername);
        Glide.with(this).load(stringImgUrl).into(imageView);



        btnMore.setOnClickListener(v -> {
            popup = new PopupMenu(SendMessageActivity.this, btnMore);
            popup.inflate(R.menu.block_menu);
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.block) {
                    LayoutInflater factory = LayoutInflater.from(this);
                    final View deleteDialogView = factory.inflate(R.layout.dialog_block, null);
                    final AlertDialog dialog = new AlertDialog.Builder(this).create();
                    dialog.setView(deleteDialogView);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.getWindow().setLayout(720, 720);//Controlling width and height.
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    LinearLayout block;
                    LinearLayout cancel;
                    TextView name;
                    block=dialog.findViewById(R.id.alert_block);
                    cancel=dialog.findViewById(R.id.alert_cancel);
                    name = dialog.findViewById(R.id.name);
                    name.setText("Block  " + stringUsername);
                    PushDownAnim.setPushDownAnimTo(block).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ref.child("Users").child(user.getUid()).child("Blocked").child(stringUserid).setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    ref.child("Users").child(stringUserid).child("Blocked").child(stringUserid).setValue(true);
                                }
                            });
                            dialog.dismiss();
                        }
                    });
                    PushDownAnim.setPushDownAnimTo(cancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                }
                return false;
            });
            //displaying the popup
            popup.show();
        });


        //Functions
        Check_blockList(user.getUid());
        Read_Message(user.getUid(), stringUserid, stringImgUrl);
        Seen_Message(stringUserid);
        Check_List(stringUserid);

    }

    private void Check_blockList(String userID) {
        youBlockList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                youBlockList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.child(userID).child("Blocked").getChildren()) {
                        youBlockList.add(dataSnapshot.getKey());
                        if (youBlockList.contains(stringUserid) ) {
                            layout.setVisibility(View.GONE);
                            block_layout.setVisibility(View.VISIBLE);
                            unblockBtn.setOnClickListener(v -> {
                                reference.child(stringUserid).child("Blocked").child(user.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        layout.setVisibility(View.VISIBLE);
                                        block_layout.setVisibility(View.GONE);
                                    }
                                });
                            });
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });


    }

    private void Check_List(String stringUserid) {
        blockedByList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                youBlockList.clear();
                for (DataSnapshot dataSnapshot : snapshot.child(stringUserid).child("Blocked").getChildren()) {
                    youBlockList.add(dataSnapshot.getKey());
                    if (youBlockList.contains(user.getUid()) ) {
                        block_acc_text.setText("You Can't Reply To This Conversation");
                        unblock_layout.setVisibility(View.GONE);
                        layout.setVisibility(View.GONE);
                        block_layout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });


    }



    final private TextView.OnEditorActionListener editorActionListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            Send_Message(v.getText().toString());
            v.setText("");
            typeMessage.requestFocus();

        }
        return false;
    };


    // slide the view from below itself to the current position
    public void slideUp(View view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public void slideDown(View view){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }


    public void onSlideViewButtonClick(View view) {
        if (isUp) {
            slideDown(extra_tools);
            extra_tools.setVisibility(View.GONE);
            item_tools_icon.setImageResource(R.drawable.ic_paper_clip);


        } else {
            slideUp(extra_tools);
            item_tools_icon.setImageResource(R.drawable.ic_baseline_close_24);
            extra_tools.setVisibility(View.VISIBLE);
            PushDownAnim.setPushDownAnimTo(layout_image_btn).setOnClickListener(v -> ChooseImg());
            PushDownAnim.setPushDownAnimTo(layout_files_btn).setOnClickListener(v -> ChooseFile());

        }
        isUp = !isUp;
    }

    //----------------------------Choose Image------------------//
    private void ChooseImg() {
        Options options = Options.init()
                .setRequestCode(100)                                           //Request code for activity results
                .setMode(Options.Mode.Picture)                                 //Option to select only pictures or videos or both
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientation
                .setPath("/pix/images");                                       //Custom Path For media Storage

        Pix.start(SendMessageActivity.this, options);
        checker = "image";
    }


    //----------------------------Choose File------------------//
    private void ChooseFile(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(Intent.createChooser(intent,"Select PDF File"),100);
        checker = "file";
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Pix.start(this, Options.init().setRequestCode(100));
            } else {
                Toast.makeText(SendMessageActivity.this, "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show();
            }
        }
    }


    //----------------------------Seen Message------------------//

    private void Seen_Message(String uid) {
        seenListener = ref.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SendMessageModel sendMessageModel = dataSnapshot.getValue(SendMessageModel.class);
                    assert sendMessageModel != null;
                    if (sendMessageModel.getReceiver().equals(user.getUid()) && sendMessageModel.getSender().equals(uid)) {
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

    }


    //----------------------------Send Message------------------//

    private void Send_Message(String t1) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        String key = db.child("Chats").push().getKey();
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm a");
        String encrypted = "";
        try {
            encrypted = AESUtils.encrypt(t1);
            Map<String,Object> map = new HashMap<>();
            map.put("msg",encrypted);
            map.put("sender",user.getUid());
            map.put("key",key);
            map.put("time", sdf.format(date));
            map.put("type","text");
            map.put("isseen",false);
            map.put("receiver", stringUserid);
            assert key != null;
            db.child("Chats").child(key).setValue(map).addOnCompleteListener(task -> {
                sentByRest(stringUserid,t1);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //-----------------------------Send Notification-----------------------------//

    private void sentByRest(String publisherId,String msg) {
        ref.child("Users").child(publisherId).child("Info").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String token  = snapshot.child("token").getValue(String.class);
                NotificationReq req = new NotificationReq(token,
                        new NotificationReq.Notification(appSharedPreferences.getUserName(),
                                appSharedPreferences.getUserName() + ": " + msg));

                RetrofitClient.getRetrofit(BASE_URL)
                        .create(NotificationRequest.class)
                        .sent(req)
                        .enqueue(new Callback<NotificationResponce>() {
                            @Override
                            public void onResponse(@NotNull Call<NotificationResponce> call, @NotNull retrofit2.Response<NotificationResponce> response) {
                            }

                            @Override
                            public void onFailure(@NotNull Call<NotificationResponce> call, @NotNull Throwable t) {
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }


    //-------------------------Handle Activity Result----------------------------//

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 100 && data!=null) {
            if (checker.equals("file")){
                fileUri = data.getData();
                Cursor c = getContentResolver().query(fileUri, null, null, null, null);
                c.moveToFirst();
                String fileName = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                uploadFile(fileUri,fileName);

            }else{
                returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
                for (String s : returnValue)
                {
                    try {
                        Uri image = Uri.fromFile(new File(s));
                        ImageCompressor imageCompressor = new ImageCompressor();
                        Bitmap src = imageCompressor.decodeFile(image.getPath());
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        src.compress(Bitmap.CompressFormat.PNG, 80, os);

                        String path = MediaStore.Images.Media.insertImage(getContentResolver(), src, "title", null);
                        Uri uri  = Uri.parse(path);
                        uploadImage(uri);

                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        }
    }


    //------------------------------Read Message------------------------------------//

    private void Read_Message(String myId,String userid,String imageUrl) {
        list = new ArrayList<>();

        ref.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot snap : snapshot.getChildren()) {
                    SendMessageModel model = snap.getValue(SendMessageModel.class);
                    assert model != null;
                    if (model.getReceiver().equals(myId) && model.getSender().equals(userid) || model.getReceiver().equals(userid) && model.getSender().equals(myId)) {
                        list.add(model);
                        recyclerView.smoothScrollToPosition(model.getKey().length()-1);
                    }
                    adapter = new SendMessageAdapter(SendMessageActivity.this,list,imageUrl);
//                    adapter.setHasStableIds(true);
                    recyclerView.setAdapter(adapter);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    //----------------------------------Upload Image--------------------------------//

    private void uploadImage(Uri image) {
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
            assert bitmap != null;
            bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
            final StorageReference reference = mStorageRef.child("ChatImages/"+ user.getUid() +"post_"+System.currentTimeMillis() );

            reference.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri -> {
                        loadingDialog.dismissDialog();
                        String key = ref.child("Chats").push().getKey();
                        Date date = new Date();
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm a");
                        String encrypted = "";
                        try {
                            encrypted = AESUtils.encrypt("Sent a photo");
                            Map<String,Object> map = new HashMap<>();
                            map.put("msg",encrypted);
                            map.put("sender",user.getUid());
                            map.put("key",key);
                            map.put("time",sdf.format(date));
                            map.put("msg_img",uri.toString());
                            map.put("type","image");
                            map.put("isSeen",false);
                            map.put("receiver", stringUserid);
                            assert key != null;
                            ref.child("Chats").child(key).setValue(map).addOnCompleteListener(task -> {

                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }))

                    .addOnFailureListener(e -> {
                        loadingDialog.dismissDialog();
                        Toast.makeText(SendMessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }


    //----------------------------------Upload File--------------------------------//

    private void uploadFile(Uri file,String filename) {
        loadingDialog.startloadingDialog();
        loadingDialog.textDiaglog("Sending file...");
        if (file != null) {
            final StorageReference reference = mStorageRef.child("ChatFiles/"+ user.getUid() +"post_"+System.currentTimeMillis() );
            reference.putFile(file)
                    .addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri -> {
                        loadingDialog.dismissDialog();
                        String key = ref.child("Chats").push().getKey();
                        Date date = new Date();
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm a");
                        String encrypted = "";
                        try {
                            encrypted = AESUtils.encrypt("Sent a file");
                            Map<String,Object> map = new HashMap<>();
                            map.put("msg",encrypted);
                            map.put("sender",user.getUid());
                            map.put("key",key);
                            map.put("time",sdf.format(date));
                            map.put("msg_img",uri.toString());
                            map.put("type","file");
                            map.put("filename",filename);
                            map.put("isSeen",false);
                            map.put("receiver", stringUserid);
                            assert key != null;
                            ref.child("Chats").child(key).setValue(map).addOnCompleteListener(task -> {

                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }))

                    .addOnFailureListener(e -> {
                        loadingDialog.dismissDialog();
                        Toast.makeText(SendMessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }


    private void initializeViews() {
        username = findViewById(R.id.username);
        imageView = findViewById(R.id.button_image);
        typeMessage = findViewById(R.id.item_input);
        btn_SendMessage = findViewById(R.id.item_send_icon_container);
        recyclerView = findViewById(R.id.chatrecycler);
        backBtn = findViewById(R.id.backbtn);
        choose_img = findViewById(R.id.emoji_layout);
        extra_tools = findViewById(R.id.extra_tools);
        layout = findViewById(R.id.layout);
        item_tools_icon = findViewById(R.id.item_emoji_icon);
        layout_files_btn = findViewById(R.id.layout_file_btn);
        layout_image_btn = findViewById(R.id.layout_image_btn);
        btnMore = findViewById(R.id.btn_more);
        block_layout = findViewById(R.id.block_layout);
        unblockBtn = findViewById(R.id.unblock_btn);
        send_message_layout = findViewById(R.id.send_message_layout);
        block_acc_text = findViewById(R.id.block_account_text);
        unblock_layout = findViewById(R.id.unblock_layout);
    }


//    -----------------------------------Change User Status (Online / Offline)--------------------------------//

    private void status(String status) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        ref.child("Users").child(user.getUid()).child("Info").updateChildren(hashMap);
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
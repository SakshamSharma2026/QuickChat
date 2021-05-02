package com.codewithshadow.quickchat.Activites;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codewithshadow.quickchat.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    ImageView profilePic;
    TextView name,number;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference documentReference;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        user = FirebaseAuth.getInstance().getCurrentUser();

        profilePic = findViewById(R.id.item_image);
        name = findViewById(R.id.item_text);
        number = findViewById(R.id.item_label);
        documentReference = db.collection("Users").document(user.getUid());
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String imgurl = documentSnapshot.getString("imgurl");
                String nameString = documentSnapshot.getString("name");
                String phoneString = documentSnapshot.getString("phone");
                Glide.with(ProfileActivity.this).load(imgurl).into(profilePic);
                name.setText(nameString);
                number.setText("+91 " + phoneString);

            }
        });
    }
}
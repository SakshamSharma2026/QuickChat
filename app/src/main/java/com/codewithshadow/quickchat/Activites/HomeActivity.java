package com.codewithshadow.quickchat.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.codewithshadow.quickchat.Adapter.HomeScreenViewpagerAdapter;
import com.codewithshadow.quickchat.Fragments.HomeFragment;
import com.codewithshadow.quickchat.Fragments.StoryFragment;
import com.codewithshadow.quickchat.R;
import com.codewithshadow.quickchat.Utils.AppSharedPreferences;
import com.codewithshadow.quickchat.Utils.UniversalImageLoderClass;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {
    private ImageView btnMore;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference collectionReference;
    CircleImageView profileImage;
    AppSharedPreferences appSharedPreferences;
    FirebaseUser user;
    DatabaseReference ref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //Hooks
        appSharedPreferences = new AppSharedPreferences(this);
        user = FirebaseAuth.getInstance().getCurrentUser();
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.home_viewpager);
        btnMore = findViewById(R.id.btn_more);
        profileImage = findViewById(R.id.profile_img);
        ref = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot documentSnapshot) {
                String imgurl = documentSnapshot.child("Info").child("imgurl").getValue(String.class);
                String name = documentSnapshot.child("Info").child("name").getValue(String.class);
                Glide.with(HomeActivity.this)
                        .load(imgurl)
                        .into(profileImage);
                appSharedPreferences.setUsername(name);
                appSharedPreferences.setImgUrl(imgurl);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        UniversalImageLoderClass universalImageLoderClass = new UniversalImageLoderClass(this);
        ImageLoader.getInstance().init(universalImageLoderClass.getConfig());


        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(HomeActivity.this, btnMore);
                popup.inflate(R.menu.more);
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.profile:
                            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                            break;

                        default:
                    }
                    return false;
                });
                //displaying the popup
                popup.show();
            }
        });
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

    }
    private void setupViewPager(ViewPager pager) {
        HomeScreenViewpagerAdapter homeScreenViewpagerAdapter = new HomeScreenViewpagerAdapter(getSupportFragmentManager());
        homeScreenViewpagerAdapter.addFragment(new HomeFragment(),"Home");
        homeScreenViewpagerAdapter.addFragment(new StoryFragment(),"Story");
        pager.setAdapter(homeScreenViewpagerAdapter);
    }
}
package com.codewithshadow.quickchat.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
import com.nostra13.universalimageloader.core.ImageLoader;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {
    private ImageView btnMore;
    CircleImageView profileImage;
    AppSharedPreferences appSharedPreferences;
    FirebaseUser user;
    DatabaseReference ref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference();


        //Hooks
        initializeViews();

        //AppSharedPreferences
        appSharedPreferences = new AppSharedPreferences(this);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.home_viewpager);


        //UniversalImageLoaderClass
        UniversalImageLoderClass universalImageLoderClass = new UniversalImageLoderClass(this);
        ImageLoader.getInstance().init(universalImageLoderClass.getConfig());

        ref.child("Users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String imgUrl = snapshot.child("Info").child("imgUrl").getValue(String.class);
                String name = snapshot.child("Info").child("name").getValue(String.class);
                UniversalImageLoderClass.setImage(imgUrl,profileImage,null);

                appSharedPreferences.setUsername(name);
                appSharedPreferences.setImgUrl(imgUrl);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(HomeActivity.this, btnMore);
            popup.inflate(R.menu.more);
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.profile) {
                    startActivity(new Intent(HomeActivity.this, UserProfileActivity.class));
                }
                return false;
            });
            //displaying the popup
            popup.show();
        });


        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void initializeViews() {
        btnMore = findViewById(R.id.btn_more);
        profileImage = findViewById(R.id.profile_img);
    }

    private void setupViewPager(ViewPager pager) {
        HomeScreenViewpagerAdapter homeScreenViewpagerAdapter = new HomeScreenViewpagerAdapter(getSupportFragmentManager());
        homeScreenViewpagerAdapter.addFragment(new HomeFragment(),"Home");
        homeScreenViewpagerAdapter.addFragment(new StoryFragment(),"Story");
        pager.setAdapter(homeScreenViewpagerAdapter);
    }

    //-----------------------------------Change User Status (Online / Offline)--------------------------------//

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
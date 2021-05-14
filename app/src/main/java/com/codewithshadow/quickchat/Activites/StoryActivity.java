package com.codewithshadow.quickchat.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.codewithshadow.quickchat.Models.StoryModel;
import com.codewithshadow.quickchat.R;
import com.codewithshadow.quickchat.Utils.UniversalImageLoderClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import de.hdodenhof.circleimageview.CircleImageView;
import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {


    int counter = 0;
    long pressTime = 0L;
    long limit = 500L;
    StoriesProgressView storiesProgressView;
    ImageView story_photo;
    TextView story_username, timeText;
    ProgressBar progressBar;
    FirebaseUser user;
    DatabaseReference ref;
    RelativeLayout relativeLayout;
    ImageView deleteBtn;
    TextView seenText;
    List<String> images;
    List<String> storyIds;
    String userid;
    CircleImageView imageView;



    private final View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        //Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference();


        //Window
        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));

        //Hooks
        initializeViews();


        //Intent Data
        userid = getIntent().getStringExtra("userid");



        relativeLayout.setVisibility(View.GONE);
        if (userid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            relativeLayout.setVisibility(View.VISIBLE);
        }



        //Delete Button
        deleteBtn.setOnClickListener(view ->
                ref.child("Story").child(userid).child(storyIds.get(counter)).removeValue().addOnCompleteListener(task -> {
                Toast.makeText(StoryActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                finish();
            }));


        //Functions
        userInfo(userid);
        getStories(userid);


        //Reverse
        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(view -> storiesProgressView.reverse());
        reverse.setOnTouchListener(onTouchListener);


        //Skip
        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(view -> storiesProgressView.skip());
        skip.setOnTouchListener(onTouchListener);

    }

    //------------------------Initialize View --------------------------//

    private void initializeViews() {
        storiesProgressView = findViewById(R.id.stories);
        imageView = findViewById(R.id.img);
        story_photo = findViewById(R.id.storyimage);
        story_username = findViewById(R.id.username);
        progressBar = findViewById(R.id.progress_bar);
        timeText = findViewById(R.id.time);
        relativeLayout = findViewById(R.id.rrll);
        deleteBtn = findViewById(R.id.deletestory);
        seenText = findViewById(R.id.seentext);
    }


    //-------------------------------Get Stories From Database-----------------------------//

    private void getStories(String userid) {
        images = new ArrayList<>();
        storyIds = new ArrayList<>();
        ref.child("Story").child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                images.clear();
                storyIds.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    StoryModel model = snap.getValue(StoryModel.class);
                    long timeCurrent = System.currentTimeMillis();

                    assert model != null;
                    if (timeCurrent > model.getTimeStart() && timeCurrent < model.getTimeEnd()) {
                        images.add(model.getStoryImg());
                        storyIds.add(model.getStoryId());
                        covertTimeToText(model.getTimeUpload(), timeText);
                    }
                }

                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                storiesProgressView.startStories(counter);


                UniversalImageLoderClass.setImage(images.get(counter),story_photo,null);

                addView(storyIds.get(counter));
                seenNumber(storyIds.get(counter));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    //-------------------------------Get User Information From Database-----------------------------//

    private void userInfo(String userid) {
        ref.child("Users").child(userid).child("Info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("name").getValue(String.class);
                String img = snapshot.child("imgUrl").getValue(String.class);
                UniversalImageLoderClass.setImage(img,imageView,null);
                story_username.setText(username);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void addView(String storyId) {
        if (!userid.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
            ref.child("Story").child(userid).child(storyId)
                    .child("views").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
        }
    }

    //-------------------------------Seen Number -----------------------------//

    private void seenNumber(String storyId) {
        ref.child("Story").child(userid).child(storyId).child("views").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                seenText.setText("Seen by "+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //------------------------------Convert Time to Text--------------------------------//

    public void covertTimeToText(String dataDate, TextView timeText) {

        String convTime = null;

        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date pasTime = dateFormat.parse(dataDate);

            Date nowTime = new Date();

            long dateDiff = nowTime.getTime() - pasTime.getTime();

            long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
            long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
            long hour   = TimeUnit.MILLISECONDS.toHours(dateDiff);
            if (second < 60) {
                convTime = second + "s " ;
            } else if (minute < 60) {
                convTime = minute + "m ";
            } else if (hour < 24) {
                convTime = hour + "h ";
            }
            timeText.setText(convTime);


        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onNext() {
        UniversalImageLoderClass.setImage(images.get(++counter),story_photo,progressBar);
        addView(storyIds.get(counter));
        seenNumber(storyIds.get(counter));
    }

    @Override
    public void onPrev() {
        if ((counter-1)<0)
            return;
        UniversalImageLoderClass.setImage(images.get(--counter),story_photo,progressBar);
        seenNumber(storyIds.get(counter));
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

}
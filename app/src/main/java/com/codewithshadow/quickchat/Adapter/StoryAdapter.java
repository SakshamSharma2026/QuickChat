package com.codewithshadow.quickchat.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithshadow.quickchat.Activites.AddStoryActivity;
import com.codewithshadow.quickchat.Models.StoryModel;
import com.codewithshadow.quickchat.R;
import com.codewithshadow.quickchat.Activites.StoryActivity;
import com.codewithshadow.quickchat.Utils.AppSharedPreferences;
import com.codewithshadow.quickchat.Utils.UniversalImageLoderClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.MyViewHolder> {
    private Context aCtx;
    private List<StoryModel> list;
    AppSharedPreferences appSharedPreferences;

    public StoryAdapter(Context aCtx, List<StoryModel> list)
    {
        this.aCtx=aCtx;
        this.list=list;

    }

    @NonNull
    @Override
    public StoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==0)
        {
            View view = LayoutInflater.from(aCtx).inflate(R.layout.card_mystory, parent, false);
            return new StoryAdapter.MyViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(aCtx).inflate(R.layout.card_story, parent, false);
            return new StoryAdapter.MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull StoryAdapter.MyViewHolder holder, int position) {
        StoryModel model = list.get(position);
        appSharedPreferences = new AppSharedPreferences(aCtx);
        userInfo(holder, model.getUserId(),position);

        if (holder.getAdapterPosition()!=0)
        {
            seenstory(holder,model.getUserId());
        }
        if (holder.getAdapterPosition()==0)
        {
            mystory(holder.addstory_text,holder.story_plus,false,holder);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.getAdapterPosition()==0)
                {
                    mystory(holder.addstory_text,holder.story_plus,true,holder);
                }
                else
                {
                    Intent intent = new Intent(aCtx, StoryActivity.class);
                    intent.putExtra("userid",model.getUserId());
                    aCtx.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }



    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView story_username,addstory_text;
        ImageView story_plus;
        CircleImageView story_photo;
        RelativeLayout story_photo_seen_layout;
        CardView white_card;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            story_username = itemView.findViewById(R.id.story_username);
            addstory_text = itemView.findViewById(R.id.mystorytext);
            story_plus = itemView.findViewById(R.id.add_story);
            story_photo = itemView.findViewById(R.id.button_image);
            story_photo_seen_layout = itemView.findViewById(R.id.button_click_parent);
            white_card = itemView.findViewById(R.id.item_badge_card_parent);

        }
    }


    @Override
    public int getItemViewType(int position) {
        if (position==0)
        {
            return 0;
        }
        return 1;
    }



    private void userInfo(final MyViewHolder holder , final String userid,final int position)
    {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid).child("Info");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    String img  =snapshot.child("imgurl").getValue(String.class);
//                   Glide.with(aCtx).load(img).into(holder.story_photo);
                    UniversalImageLoderClass.setImage(img,holder.story_photo,null);

                    if(position !=0)
                    {
                          UniversalImageLoderClass.setImage(img,holder.story_photo,null);
//                        Glide.with(aCtx).load(img).into(holder.story_photo);
                        holder.story_username.setText(name);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
/*//            holder.story_photo_seen_layout.setBackground(aCtx.getResources().getDrawable(R.drawable.uigitdev_elements_profile_picture_gradient));
//            UniversalImageLoderClass.setImage(appSharedPreferences.getImgUrl(),holder.story_photo,null);
//            holder.story_username.setText(appSharedPreferences.getUserName());*/

    }


    private void mystory(TextView textView, ImageView imageView, boolean click,MyViewHolder holder)
    {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Story").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                long timecurrent = System.currentTimeMillis();
                for (DataSnapshot snap : snapshot.getChildren())
                {
                    StoryModel storyModel = snap.getValue(StoryModel.class);
                    if (timecurrent>storyModel.getTimestart() && timecurrent < storyModel.getTimeend())
                    {
                        count++;
                    }
                }

                if (click)
                {
                    if (count > 0)
                    {
                        AlertDialog alertDialog = new AlertDialog.Builder(aCtx).create();
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View Story",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(aCtx,StoryActivity.class);
                                        intent.putExtra("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        aCtx.startActivity(intent);
                                        dialogInterface.dismiss();
                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(aCtx, AddStoryActivity.class);
                                        aCtx.startActivity(intent);
                                        dialogInterface.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                    else
                    {
                        Intent intent = new Intent(aCtx, AddStoryActivity.class);
                        aCtx.startActivity(intent);
                    }
                }
                else
                {
                    if (count > 0)
                    {
                        textView.setText("My Story");
                        holder.story_photo_seen_layout.setBackground(aCtx.getResources().getDrawable(R.drawable.uigitdev_elements_profile_picture_gradient));
                        imageView.setVisibility(View.GONE);
                        holder.white_card.setVisibility(View.GONE);
                    }
                    else
                    {
                        textView.setText("Add Story");
                        imageView.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void seenstory(MyViewHolder holder,String userId)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Story").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i=0;
                for (DataSnapshot snapshot1 : snapshot.getChildren())
                {
                    if (!snapshot1.child("views")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists() && System.currentTimeMillis()  < snapshot1.getValue(StoryModel.class).getTimeend())
                    {
                        i++;
                    }
                }
                if(i > 0)
                {
                    holder.story_photo.setVisibility(View.VISIBLE);
                    holder.story_photo_seen_layout.setBackground(aCtx.getResources().getDrawable(R.drawable.uigitdev_elements_profile_picture_gradient));

                }
                else
                {
                    holder.story_photo_seen_layout.setBackgroundColor(Color.GRAY);

                    holder.story_photo.setVisibility(View.VISIBLE);

                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}







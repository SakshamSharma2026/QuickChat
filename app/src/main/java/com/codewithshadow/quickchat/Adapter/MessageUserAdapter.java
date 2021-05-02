package com.codewithshadow.quickchat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.codewithshadow.quickchat.R;
import com.codewithshadow.quickchat.Activites.SendMessageActivity;
import com.codewithshadow.quickchat.Models.SendMessageModel;
import com.codewithshadow.quickchat.Models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MessageUserAdapter extends RecyclerView.Adapter<MessageUserAdapter.MyViewHolder> {
    private Context aCtx;
    private List<UserModel> list;
    String theLastMessage,theLastTime;
    private boolean isChat;

    public MessageUserAdapter(Context aCtx, List<UserModel> list, boolean isChat)
    {
        this.aCtx=aCtx;
        this.list=list;
        this.isChat=isChat;

    }

    @NonNull
    @Override
    public MessageUserAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(aCtx).inflate(R.layout.card_useritem, parent, false);
        return new MessageUserAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageUserAdapter.MyViewHolder holder, int position) {
        UserModel model = list.get(position);
        holder.username.setText(model.getName());
        RequestOptions myOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(aCtx)
                .load(model.getImgurl())
                .thumbnail(0.05f)
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(myOptions)
                .into(holder.userImage);
        lastMessage(model.getUserid(),holder.msg,holder.last_time);
        unreadMessages(holder,model.getUserid());


        if (isChat)
        {
            if (model.getStatus().equals("online")) {
                holder.badge.setVisibility(View.VISIBLE);
            }
            else {
                holder.badge.setVisibility(View.GONE);
            }
        }
        else
        {
            holder.badge.setVisibility(View.GONE);
        }



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(aCtx, SendMessageActivity.class);
                intent.putExtra("imgurl",model.getImgurl());
                intent.putExtra("username",model.getName());
                intent.putExtra("userid",model.getUserid());

                aCtx.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView username, msg,unreadtext,last_time;
        ImageView userImage;
        CardView badge,card_unread;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.item_text);
            userImage = itemView.findViewById(R.id.item_image);
            msg = itemView.findViewById(R.id.item_label);
            badge = itemView.findViewById(R.id.item_badge_card_parent);
            unreadtext = itemView.findViewById(R.id.unreadtext);
            card_unread = itemView.findViewById(R.id.card_unread);
            last_time = itemView.findViewById(R.id.lastime);

        }
    }


    private void unreadMessages(MyViewHolder holder,String userid)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SendMessageModel model = null;
                int unread=0;
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    model = dataSnapshot.getValue(SendMessageModel.class);
                    if (model.getReceiver().equals(user.getUid()) && model.getSender().equals(userid) && !model.isIsseen())
                    {
                        unread++;
                    }
                }
                if (unread==0)
                {
                    holder.card_unread.setVisibility(View.GONE);
                }
                else
                {
                    holder.card_unread.setVisibility(View.VISIBLE);
                    holder.unreadtext.setText(unread+"");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void lastMessage(String userid , TextView last_msg,TextView last_time)
    {
        theLastMessage = "default";
        theLastTime = "default";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SendMessageModel model = null;
                for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                    model = snapshot1.getValue(SendMessageModel.class);

                    if (model.getReceiver().equals(user.getUid()) && model.getSender().equals(userid) || model.getReceiver().equals(userid) && model.getSender().equals(user.getUid())) {
                        theLastMessage = model.getMsg();
                        theLastTime = model.getTime();
                    }
                }

                switch (theLastMessage)
                {
                    case "default":
                        last_msg.setText("Active now");
                        break;
                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }
                theLastMessage = "default";

                switch (theLastTime)
                {
                    case "default":
                        last_time.setText(" ");
                        break;
                    default:
                        last_time.setText(theLastTime);
                        break;
                }
                theLastTime = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}



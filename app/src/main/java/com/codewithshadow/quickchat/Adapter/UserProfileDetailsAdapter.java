package com.codewithshadow.quickchat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.codewithshadow.quickchat.Models.SendMessageModel;
import com.codewithshadow.quickchat.R;
import com.codewithshadow.quickchat.Utils.AESUtils;
import com.codewithshadow.quickchat.Utils.UniversalImageLoderClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zolad.zoominimageview.ZoomInImageView;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class UserProfileDetailsAdapter extends RecyclerView.Adapter<UserProfileDetailsAdapter.MyViewHolder> {
    private Context aCtx;
    private List<SendMessageModel> list;

    public UserProfileDetailsAdapter(Context aCtx, List<SendMessageModel> list)
    {
        this.aCtx=aCtx;
        this.list=list;
    }

    @NonNull
    @Override
    public UserProfileDetailsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(aCtx).inflate(R.layout.card_imageview, parent, false);
            return new UserProfileDetailsAdapter.MyViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull UserProfileDetailsAdapter.MyViewHolder holder, int position) {

        SendMessageModel model = list.get(position);
        if (model.getType().equals("image")) {
            System.out.println(list.size());
            RequestOptions myOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
            Glide.with(aCtx)
                    .load(model.getMsg_img())
                    .thumbnail(0.05f)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(myOptions)
                    .into(holder.img);
        }
        else{
            holder.card.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        CardView card;



        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.msg_img);
            card = itemView.findViewById(R.id.card_image);

        }
    }
}


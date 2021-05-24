package com.codewithshadow.quickchat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.codewithshadow.quickchat.R;
import com.codewithshadow.quickchat.Models.SendMessageModel;
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
import java.util.List;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class SendMessageAdapter extends RecyclerView.Adapter<SendMessageAdapter.MyViewHolder> {
    private Context aCtx;
    private List<SendMessageModel> list;
    public static final int MSG_TYPE_RIGHT = 0;
    public static final int MSG_TYPE_LEFT = 1;
    private String imageUrl;
    FirebaseUser user;
    BottomSheetDialog bottomSheetDialog;



    public SendMessageAdapter(Context aCtx, List<SendMessageModel> list, String imageUrl) {
        this.aCtx=aCtx;
        this.list=list;
        this.imageUrl=imageUrl;

    }

    @NonNull
    @Override
    public SendMessageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT)
        {
            View view = LayoutInflater.from(aCtx).inflate(R.layout.chat_item_right, parent, false);
            return new SendMessageAdapter.MyViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(aCtx).inflate(R.layout.chat_item_left, parent, false);
            return new SendMessageAdapter.MyViewHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull SendMessageAdapter.MyViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        SendMessageModel model = list.get(position);
        GestureDetector gestureDetector;
        String decrypted = "";
        try {
            decrypted = AESUtils.decrypt(model.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (model.getType().equals("image")) {
            holder.card_img.setVisibility(View.VISIBLE);
            RequestOptions myOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
            Glide.with(aCtx)
                    .load(model.getMsg_img())
                    .thumbnail(0.05f)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(myOptions)
                    .into(holder.msg_img);
            Glide.with(aCtx).load(imageUrl).into(holder.userImage);
            holder.show_message.setVisibility(View.GONE);
            holder.text_seen.setVisibility(View.GONE);
            holder.li.setVisibility(View.GONE);
        }
        else if (model.getType().equals("text")){
            holder.show_message.setText(decrypted);
            holder.text_seen.setText(model.getTime());
            holder.timetext.setText(model.getTime());
            Glide.with(aCtx).load(imageUrl).into(holder.userImage);
        }
        else {
            holder.card_file.setVisibility(View.VISIBLE);
            holder.show_message.setVisibility(View.GONE);
            holder.text_seen.setVisibility(View.GONE);
            holder.li.setVisibility(View.GONE);
            holder.text_file_name.setText(model.getFilename());
            holder.card_file.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(model.getMsg_img()));
                aCtx.startActivity(intent);
            });
        }



        if (position == list.size()-1) {
            if (model.isIsseen()) {
                holder.text_seen.setText("seen");
            }
            else {
                holder.text_seen.setText("Delivered");
            }
        }
        else {
            holder.text_seen.setVisibility(View.GONE);
        }




        gestureDetector = new GestureDetector(aCtx, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }


            @Override
            public boolean onSingleTapUp(MotionEvent e) {

                if (model.getType().equals("image")){
                    LayoutInflater inflater = (LayoutInflater)
                            aCtx.getSystemService(LAYOUT_INFLATER_SERVICE);
                    View popupView = inflater.inflate(R.layout.full_screen_image, null);
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    ((WindowManager) aCtx.getSystemService(Context.WINDOW_SERVICE))
                            .getDefaultDisplay().getMetrics(displayMetrics);
                    int height = displayMetrics.heightPixels;
                    int width = displayMetrics.widthPixels;
                    boolean focusable = true;
                    final PopupWindow popupWindow = new PopupWindow(popupView, (int) (width - (width * 0.1)), (int) (height - (height * 0.12)), focusable);
                    popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
                    final ZoomInImageView expand = popupView.findViewById(R.id.fullscreen_view);
                    UniversalImageLoderClass.setImage(model.getMsg_img(),expand,null);

                    popupView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            popupWindow.dismiss();
                            return true;
                        }
                    });
                }
                return super.onSingleTapUp(e);

            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (model.getSender().equals(user.getUid())) {
                    bottomSheetDialog=new BottomSheetDialog(aCtx,R.style.BottomSheetDialogStyle);
                    View bottomsheetview = LayoutInflater.from(aCtx).inflate(R.layout.chat_bottom_menu,null);
                    bottomSheetDialog.setContentView(bottomsheetview);
                    try {
                        bottomSheetDialog.show();
                    }
                    catch (WindowManager.BadTokenException ee) {
                        //use a log message
                    }
                    TextView btn_unsend = bottomsheetview.findViewById(R.id.btn_unsend);
                    btn_unsend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                            ref.child("Chats").child(model.getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    bottomSheetDialog.cancel();

                                }
                            });

                        }
                    });
                }
                super.onLongPress(e);
            }
        }
        );

        holder.card_img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });

        holder.show_message.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });




    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView show_message,text_file_name;
        ImageView userImage;
        TextView text_seen,timetext;
        CardView card_img,card_file;
        ImageView msg_img;
        LinearLayout li;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.text);
            userImage = itemView.findViewById(R.id.button_image);
            text_seen = itemView.findViewById(R.id.text_seen);
            card_img = itemView.findViewById(R.id.card_image);
            msg_img = itemView.findViewById(R.id.msg_img);
            timetext = itemView.findViewById(R.id.texttime);
            li = itemView.findViewById(R.id.li);
            card_file = itemView.findViewById(R.id.card_file);
            text_file_name = itemView.findViewById(R.id.text_file_name);

        }
    }

    @Override
    public int getItemViewType(int position) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (list.get(position).getSender().equals(user.getUid()))
        {
            return MSG_TYPE_RIGHT;
        }
        else
        {
            return MSG_TYPE_LEFT;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }
}

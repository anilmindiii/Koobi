package com.mualab.org.user.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.activity.PreviewImageActivity;
import com.mualab.org.user.chat.listner.DateTimeScrollListner;
import com.mualab.org.user.chat.model.GroupChat;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupChattingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int VIEW_TYPE_ME  = 1;
    private int VIEW_TYPE_OTHER = 2;
    private Context context;
    private List<GroupChat> chatList;
    private String myUid ;
    private DateTimeScrollListner listener;

    public GroupChattingAdapter(Context context, List<GroupChat> chatList, String myId,
                                DateTimeScrollListner listener) {
        this.context = context;
        this.chatList = chatList;
        this.myUid = myId;
        this.listener=listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if(viewType == VIEW_TYPE_ME){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right_side_view,parent,false);
            return new MyViewHolder(view);
        }else if(viewType == VIEW_TYPE_OTHER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_chat_left_side_view,parent,false);
            return new OtherViewHolder(view);
        }

        return  new OtherViewHolder(null);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int pos = position - 1;
        int tempPos = (pos == -1) ? pos + 1 : pos;

        GroupChat chat = chatList.get(position);

        if(TextUtils.equals(chat.senderId,myUid)){
            ((MyViewHolder)holder).myBindData(chat,position,tempPos);
        }else {
            ((OtherViewHolder)holder).otherBindData(chat,position,tempPos);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (TextUtils.equals(chatList.get(position).senderId,myUid )) {
            return VIEW_TYPE_ME;
        } else {
            return VIEW_TYPE_OTHER;
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tv_sender_msg,tv_send_time,tv_my_date_label,tv_transparent;
        ImageView iv_for_sender,iv_msg_status;
       // ProgressBar progress_bar;

        MyViewHolder(View itemView) {
            super(itemView);
            tv_sender_msg = itemView.findViewById(R.id.tv_sender_msg);
            iv_msg_status = itemView.findViewById(R.id.iv_msg_status);
            iv_for_sender = itemView.findViewById(R.id.iv_for_sender);
            iv_for_sender.setEnabled(false);
            tv_send_time = itemView.findViewById(R.id.tv_send_time);
           // progress_bar = itemView.findViewById(R.id.progress_bar);
            tv_my_date_label = itemView.findViewById(R.id.tv_my_date_label);
            tv_transparent = itemView.findViewById(R.id.tv_transparent);

            iv_for_sender.setOnClickListener(this);

        }

        void myBindData(final GroupChat chat, int position,int tempPos){

            if(chat.messageType == 1){
                iv_for_sender.setVisibility(View.VISIBLE);
                tv_sender_msg.setVisibility(View.GONE);
                //progress_bar.setVisibility(View.VISIBLE);

                if(chat.message.contains(".gif")){
                    GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(iv_for_sender);
                    Glide.with(context)
                            .load(chat.message)
                            .listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                   // progress_bar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .into(imageViewTarget);
                }else {
                    Picasso.with(context)
                        .load(chat.message).placeholder(R.drawable.gallery_placeholder).into(iv_for_sender, new Callback() {
                    @Override
                    public void onSuccess() {
                        //progress_bar.setVisibility(View.GONE);
                        iv_for_sender.setEnabled(true);
                    }
                    @Override
                    public void onError() {
                        Picasso.with(context).load(chat.message)
                                .placeholder(R.drawable.gallery_placeholder)
                                .error(R.drawable.gallery_placeholder).into(iv_for_sender);
                       // progress_bar.setVisibility(View.GONE);
                        iv_for_sender.setEnabled(false);
                    }
                });
                }

            }else {
                iv_for_sender.setVisibility(View.GONE);
                tv_sender_msg.setVisibility(View.VISIBLE);
                tv_sender_msg.setText(chat.message);
                //progress_bar.setVisibility(View.GONE);
            }

            if(chat.isLongSelected){
                tv_transparent.setVisibility(View.VISIBLE);
            }else tv_transparent.setVisibility(View.GONE);

            SimpleDateFormat sd = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            try {
                String date = sd.format(new Date((Long) chat.timestamp));
                tv_send_time.setText(chat.banner_date +" "+ date);

                tv_my_date_label.setText(chat.banner_date);

                if (!chat.banner_date.equals(chatList.get(tempPos).banner_date)) {
                    tv_my_date_label.setVisibility(View.VISIBLE);
                } else {
                    if (position==0)
                        tv_my_date_label.setVisibility(View.VISIBLE);
                    else
                        tv_my_date_label.setVisibility(View.GONE);
                }

                /*String newDate = getDateBanner((Long) chat.timestamp);*/

            } catch (Exception e) {
                e.printStackTrace();
            }

            switch (chat.readStatus){
                case 0:
                    iv_msg_status.setImageResource(R.drawable.ico_msg_received);
                    break;
                case 1:
                    iv_msg_status.setImageResource(R.drawable.ic_ico_msg_sent);
                    break;
                case 2:
                    iv_msg_status.setImageResource(R.drawable.ico_msg_read);
                    break;
            }

            if (listener!=null)
                listener.onScrollChange(position,chat.timestamp);

            itemView.setOnLongClickListener(v -> {
                listener.onLongPress(getAdapterPosition(),chatList.get(getAdapterPosition()).ref_key);
                return false;
            });

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.iv_for_sender:
                    GroupChat chat = chatList.get(getAdapterPosition());
                    if(chat.messageType == 1) {
                        ArrayList<String> tempList = new ArrayList<>();
                        tempList.add(chat.message);
                        Intent intent = new Intent(context, PreviewImageActivity.class);
                        intent.putExtra("imageArray", tempList);
                        intent.putExtra("startIndex", 0);
                        context.startActivity(intent);
                    }
                    break;
            }
        }
    }

    public class OtherViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tv_other_msg,tvSenderName,tv_other_msg_time;
        ImageView iv_other_img,iv_othr_msg_status;
       // ProgressBar progress_bar;

        OtherViewHolder(View itemView) {
            super(itemView);
            tv_other_msg = itemView.findViewById(R.id.tv_other_msg);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tv_other_msg_time = itemView.findViewById(R.id.tv_other_msg_time);
            iv_other_img = itemView.findViewById(R.id.iv_other_img);
            iv_other_img.setEnabled(false);
            iv_othr_msg_status = itemView.findViewById(R.id.iv_othr_msg_status);
           // progress_bar = itemView.findViewById(R.id.progress_bar);

            iv_other_img.setOnClickListener(this);
        }

        void otherBindData(final GroupChat chat, int position,int tempPos){
            tvSenderName.setVisibility(View.VISIBLE);
            tvSenderName.setText(chat.userName);

            if(chat.messageType == 1){
                //progress_bar.setVisibility(View.VISIBLE);
                iv_other_img.setVisibility(View.VISIBLE);
                tv_other_msg.setVisibility(View.GONE);
               // progress_bar.setVisibility(View.VISIBLE);

                if(chat.message.contains(".gif")){
                    GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(iv_other_img);
                    Glide.with(context)
                            .load(chat.message)
                            .listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                  //  progress_bar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .into(imageViewTarget);
                }else{
                    Glide.with(context)
                            .load(chat.message).placeholder(R.drawable.gallery_placeholder)
                            .listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    iv_other_img.setEnabled(false);
                                    Picasso.with(context).load(chat.message)
                                            .placeholder(R.drawable.gallery_placeholder)
                                            .error(R.drawable.gallery_placeholder).into(iv_other_img);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    iv_other_img.setEnabled(true);
                                    return false;
                                }
                            })
                            .into(iv_other_img);

                   /* Picasso.with(context)
                            .load(chat.message).placeholder(R.drawable.gallery_placeholder).into(iv_other_img, new Callback() {
                        @Override
                        public void onSuccess() {
                            iv_other_img.setEnabled(true);
                            //progress_bar.setVisibility(View.GONE);
                        }
                        @Override
                        public void onError() {
                            iv_other_img.setEnabled(false);
                            Picasso.with(context).load(chat.message)
                                    .placeholder(R.drawable.gallery_placeholder)
                                    .error(R.drawable.gallery_placeholder).into(iv_other_img);
                            //progress_bar.setVisibility(View.GONE);
                        }
                    });*/
                }



                //  Glide.with(context).load(chat.message).fitCenter().placeholder(R.drawable.gallery_placeholder).into(iv_other_img);

            }else {
                tv_other_msg.setVisibility(View.VISIBLE);
                iv_other_img.setVisibility(View.GONE);
                tv_other_msg.setText(chat.message);
               // progress_bar.setVisibility(View.GONE);
            }

            SimpleDateFormat sd = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            try {
                String date = sd.format(new Date((Long) chat.timestamp));
                tv_other_msg_time.setText(chat.banner_date+" "+date);

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (listener!=null)
                listener.onScrollChange(position,chat.timestamp);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.iv_other_img:
                    GroupChat chat = chatList.get(getAdapterPosition());
                    if(chat.messageType == 1) {
                        ArrayList<String> tempList = new ArrayList<>();
                        tempList.add(chat.message);
                        Intent intent = new Intent(context, PreviewImageActivity.class);
                        intent.putExtra("imageArray", tempList);
                        intent.putExtra("startIndex", 0);
                        context.startActivity(intent);
                    }
                    break;
            }
        }
    }

}

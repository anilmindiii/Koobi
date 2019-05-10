package com.mualab.org.user.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.daimajia.swipe.SwipeLayout;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.ChatActivity;
import com.mualab.org.user.chat.ChatHistoryActivity;
import com.mualab.org.user.chat.GroupChatActivity;
import com.mualab.org.user.chat.GroupDetailActivity;
import com.mualab.org.user.chat.brodcasting.BroadCastChatActivity;
import com.mualab.org.user.chat.brodcasting.BroadcastDetails;
import com.mualab.org.user.chat.model.ChatHistory;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int VIEW_TYPE_SINGLE  = 1;
    private int VIEW_TYPE_GROUP = 2;
    private int VIEW_TYPE_BROADCAST = 3;
    private int lastPosition = -1;

    private Context context;
    private List<ChatHistory> chatHistories;
    //  private boolean isTyping = false;
    private long mLastClickTime = 0;

    public ChatHistoryAdapter(Context context, List<ChatHistory> chatHistories) {
        this.context = context;
        this.chatHistories = chatHistories;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if(viewType == VIEW_TYPE_SINGLE){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_history,parent,false);
            return new SingleChatViewHolder(view);
        }else if(viewType == VIEW_TYPE_GROUP) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_chat_history,parent,false);
            return new GroupChatViewHolder(view);
        }else if(viewType == VIEW_TYPE_BROADCAST){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_broadcast_chat_history,parent,false);
            return new BroadCastChatViewHolder(view);
        }

        return  new SingleChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatHistory chatHistory = chatHistories.get(position);
        int pos = position - 1;
        int tempPos = (pos == -1) ? pos + 1 : pos;

        if(chatHistory.type.equals("user")){
            ((SingleChatViewHolder)holder).myBindData(chatHistory,position,tempPos);
        }else  if(chatHistory.type.equals("broadcast") || chatHistory.type.equals("3")){
            ((BroadCastChatViewHolder)holder).myBindData(chatHistory,position,tempPos);

        }else {
            ((GroupChatViewHolder)holder).otherBindData(chatHistory,position,tempPos);
        }



    }

    /*private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated

        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_out);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }*/

    @Override
    public int getItemViewType(int position) {
        String type = chatHistories.get(position).type;
        if (type.equals("user")) {
            return VIEW_TYPE_SINGLE;
        }else  if (type.equals("broadcast") || type.equals("3")){
            return VIEW_TYPE_BROADCAST;
        }
        else {
            return VIEW_TYPE_GROUP;
        }
    }

    @Override
    public int getItemCount() {
        return chatHistories.size();
    }

    public class SingleChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvUname,tvMsg,tvChatType,tvUnReadCount,tvTime,tvHistoryTime;
        CircleImageView ivProfile;
        RelativeLayout rlChatHistory,llHistoryDate,rlMsgCount;
        View vBottom,viewTop;
        ImageView ivMuteIcon;
        LinearLayout ly_delete_chat;
        SwipeLayout swipeLayout;

        SingleChatViewHolder(View itemView) {
            super(itemView);
            tvUname = itemView.findViewById(R.id.tvUname);
            tvMsg = itemView.findViewById(R.id.tvMsg);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvChatType = itemView.findViewById(R.id.tvChatType);
            tvUnReadCount = itemView.findViewById(R.id.tvUnReadCount);
            rlChatHistory = itemView.findViewById(R.id.rlChatHistory);
            tvTime = itemView.findViewById(R.id.tvTime);
            llHistoryDate = itemView.findViewById(R.id.llHistoryDate);
            tvHistoryTime = itemView.findViewById(R.id.tvHistoryTime);
            vBottom = itemView.findViewById(R.id.vBottom);
            viewTop = itemView.findViewById(R.id.viewTop);
            rlMsgCount = itemView.findViewById(R.id.rlMsgCount);
            ivMuteIcon = itemView.findViewById(R.id.ivMuteIcon);
            ly_delete_chat = itemView.findViewById(R.id.ly_delete_chat);
            swipeLayout = itemView.findViewById(R.id.sample1);

            rlChatHistory.setOnClickListener(this);

            swipeLayout.setRightSwipeEnabled(false);
            swipeLayout.setLeftSwipeEnabled(false);

        }

        synchronized void myBindData(final ChatHistory chat,int position,int tempPos){

            tvUname.setText(chat.userName);

            if (chat.isTyping){
                tvMsg.setText("typing...");
                tvMsg.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            }else {
                if (chat.message.contains("https://firebasestorage.googleapis.com")| chat.messageType==1){
                    tvMsg.setText("Image");
                }else
                    tvMsg.setText(chat.message);
                tvMsg.setTextColor(context.getResources().getColor(R.color.grey));
            }

            if (chat.profilePic !=null && !chat.profilePic.isEmpty()) {
                Picasso.with(context).load(chat.profilePic).placeholder(R.drawable.default_placeholder).
                        into(ivProfile);
            }
            String num = String.valueOf(chat.unreadMessage);

            if (chat.unreadMessage!=0){
                rlMsgCount.setVisibility(View.VISIBLE);
                if (num.length()>2)
                    tvUnReadCount.setText(num.charAt(0)+num.charAt(1)+"+");
                else
                    tvUnReadCount.setText(num);
            }else {
                rlMsgCount.setVisibility(View.GONE);
            }

            if (chat.isMute){
                ivMuteIcon.setVisibility(View.VISIBLE);
            }else {
                ivMuteIcon.setVisibility(View.GONE);
            }


            if (!chat.banner_date.equals(chatHistories.get(tempPos).banner_date)) {
                tvHistoryTime.setText(chat.banner_date);
                viewTop.setVisibility(View.GONE);
                llHistoryDate.setVisibility(View.VISIBLE);
                vBottom.setVisibility(View.GONE);
            } else {
                if (chat.banner_date.equalsIgnoreCase("Today") && position==0){
                    vBottom.setVisibility(View.GONE);
                    viewTop.setVisibility(View.GONE);
                    llHistoryDate.setVisibility(View.GONE);
                }else if (chat.banner_date.equalsIgnoreCase("Yesterday") && position==0){
                    tvHistoryTime.setText(chat.banner_date);
                    vBottom.setVisibility(View.GONE);
                    viewTop.setVisibility(View.GONE);
                    llHistoryDate.setVisibility(View.VISIBLE);
                }else {
                    vBottom.setVisibility(View.GONE);
                    viewTop.setVisibility(View.VISIBLE);
                    llHistoryDate.setVisibility(View.GONE);
                }
            }

            if (position==(chatHistories.size()-1)){
                vBottom.setVisibility(View.VISIBLE);
            }

            SimpleDateFormat sd = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            try {
                String date = sd.format(new Date((Long) chat.timestamp));
                tvTime.setText(date);

            }catch (Exception e){
                e.printStackTrace();
            }
            //setAnimation(itemView, position);

            ly_delete_chat.setOnClickListener(v->{
                Toast.makeText(context, "ly_delete_chat", Toast.LENGTH_SHORT).show();
                swipeLayout.close(true,true);
            });
        }

        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 600) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (v.getId()){
                case R.id.rlChatHistory:
                    ChatHistory chatHistory = chatHistories.get(getAdapterPosition());
                    String otherId = "";
                    if (!chatHistory.reciverId.equals(String.valueOf(Mualab.currentUser.id)))
                        otherId = chatHistory.reciverId;
                    else
                        otherId = chatHistory.senderId;

                    Intent chat_intent = new Intent(context, ChatActivity.class);
                    chat_intent.putExtra("opponentChatId",otherId);
                    context.startActivity(chat_intent);
                    ((ChatHistoryActivity)context).finish();
                    break;
            }
        }
    }

    public class BroadCastChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvUname,tvMsg,tvChatType,tvUnReadCount,tvTime,tvHistoryTime;
        CircleImageView ivProfile;
        RelativeLayout rlChatHistory,llHistoryDate,rlMsgCount;
        LinearLayout ly_delete_broadcast;
        View vBottom,viewTop;
        ImageView ivMuteIcon;
        SwipeLayout swipeLayout;

        BroadCastChatViewHolder(View itemView) {
            super(itemView);
            tvUname = itemView.findViewById(R.id.tvUname);
            tvMsg = itemView.findViewById(R.id.tvMsg);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvChatType = itemView.findViewById(R.id.tvChatType);
            tvUnReadCount = itemView.findViewById(R.id.tvUnReadCount);
            tvTime = itemView.findViewById(R.id.tvTime);
            llHistoryDate = itemView.findViewById(R.id.llHistoryDate);
            tvHistoryTime = itemView.findViewById(R.id.tvHistoryTime);
            vBottom = itemView.findViewById(R.id.vBottom);
            viewTop = itemView.findViewById(R.id.viewTop);
            rlMsgCount = itemView.findViewById(R.id.rlMsgCount);
            ivMuteIcon = itemView.findViewById(R.id.ivMuteIcon);
            rlChatHistory = itemView.findViewById(R.id.rlChatHistory);
            ly_delete_broadcast = itemView.findViewById(R.id.ly_delete_broadcast);
            swipeLayout = itemView.findViewById(R.id.sample1);
            rlChatHistory.setOnClickListener(this);

            swipeLayout.setRightSwipeEnabled(false);
            swipeLayout.setLeftSwipeEnabled(false);

        }

        void myBindData(final ChatHistory chat,int position,int tempPos){

            tvUname.setText(chat.userName);

            ivProfile.setImageResource(R.drawable.koobi_round_ico);
           /* if (chat.profilePic !=null && !chat.profilePic.isEmpty()) {
                Picasso.with(context).load(chat.profilePic).placeholder(R.drawable.group_defoult_icon ).
                        into(ivProfile);
            }*/

            if (chat.message.contains("https://firebasestorage.googleapis.com") | chat.messageType==1
                    | chat.message.contains("content://media/external")
                    | chat.message.contains("content://com.google.android")){
                tvMsg.setText("Image");
            }else{
                tvMsg.setText(chat.message);
            }

            tvMsg.setTextColor(context.getResources().getColor(R.color.grey));

            ivProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChatHistory chatHistory = chatHistories.get(getAdapterPosition());
                    Intent intent = new Intent(context, BroadcastDetails.class);
                    intent.putExtra("broadcastId",chatHistory.reciverId);
                    context.startActivity(intent);
                }
            });

            if (chat.unreadMessage!=0){
                rlMsgCount.setVisibility(View.VISIBLE);
                tvUnReadCount.setText(""+chat.unreadMessage);
            }else {
                rlMsgCount.setVisibility(View.GONE);
            }

            if (chat.isMute){
                ivMuteIcon.setVisibility(View.VISIBLE);
            }else {
                ivMuteIcon.setVisibility(View.GONE);
            }

            if (!chat.banner_date.equals(chatHistories.get(tempPos).banner_date)) {
                tvHistoryTime.setText(chat.banner_date);
                viewTop.setVisibility(View.GONE);
                llHistoryDate.setVisibility(View.VISIBLE);
                vBottom.setVisibility(View.GONE);
            } else {
                if (chat.banner_date.equalsIgnoreCase("Today") && position==0){
                    vBottom.setVisibility(View.GONE);
                    viewTop.setVisibility(View.GONE);
                    llHistoryDate.setVisibility(View.GONE);
                }else if (chat.banner_date.equalsIgnoreCase("Yesterday") && position==0){
                    tvHistoryTime.setText(chat.banner_date);
                    vBottom.setVisibility(View.GONE);
                    viewTop.setVisibility(View.GONE);
                    llHistoryDate.setVisibility(View.VISIBLE);
                }else {
                    vBottom.setVisibility(View.GONE);
                    viewTop.setVisibility(View.VISIBLE);
                    llHistoryDate.setVisibility(View.GONE);
                }
            }

            if (position==(chatHistories.size()-1)){
                vBottom.setVisibility(View.VISIBLE);
            }

            SimpleDateFormat sd = new SimpleDateFormat("hh:mm a",Locale.getDefault());
            try {
                String date = sd.format(new Date((Long) chat.timestamp));
                tvTime.setText(date);

            }catch (Exception e){
                e.printStackTrace();
            }

            ly_delete_broadcast.setOnClickListener(v -> {
                Toast.makeText(context, "broadcast", Toast.LENGTH_SHORT).show();
                swipeLayout.close(true,true);
            });

           // setAnimation(itemView, position);
        }

        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 600) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (v.getId()){
                case R.id.rlChatHistory:
                    ChatHistory chatHistory = chatHistories.get(getAdapterPosition());
                    Intent chat_intent = new Intent(context, BroadCastChatActivity.class);
                    chat_intent.putExtra("broadcastId",chatHistory.reciverId);
                    context.startActivity(chat_intent);

                    break;
            }
        }
    }

    public class GroupChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvUname,tvMsg,tvChatType,tvUnReadCount,tvTime,tvHistoryTime;
        CircleImageView ivProfile;
        RelativeLayout rlChatHistory,llHistoryDate,rlMsgCount;
        View vBottom,viewTop;
        ImageView ivMuteIcon;
        LinearLayout ly_delete_group_chat;
        SwipeLayout swipeLayout;

        GroupChatViewHolder(View itemView) {
            super(itemView);
            tvUname = itemView.findViewById(R.id.tvUname);
            tvMsg = itemView.findViewById(R.id.tvMsg);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvChatType = itemView.findViewById(R.id.tvChatType);
            tvUnReadCount = itemView.findViewById(R.id.tvUnReadCount);
            tvTime = itemView.findViewById(R.id.tvTime);
            llHistoryDate = itemView.findViewById(R.id.llHistoryDate);
            tvHistoryTime = itemView.findViewById(R.id.tvHistoryTime);
            vBottom = itemView.findViewById(R.id.vBottom);
            viewTop = itemView.findViewById(R.id.viewTop);
            rlMsgCount = itemView.findViewById(R.id.rlMsgCount);
            ivMuteIcon = itemView.findViewById(R.id.ivMuteIcon);
            rlChatHistory = itemView.findViewById(R.id.rlChatHistory);
            ly_delete_group_chat = itemView.findViewById(R.id.ly_delete_group_chat);
            swipeLayout = itemView.findViewById(R.id.sample1);

            rlChatHistory.setOnClickListener(this);
            swipeLayout.setRightSwipeEnabled(false);
            swipeLayout.setLeftSwipeEnabled(false);

        }

        void otherBindData(final ChatHistory chat,int position,int tempPos){
            tvUname.setText(chat.userName);

            if (chat.profilePic !=null && !chat.profilePic.isEmpty()) {
                //group_defoult_icon
                Picasso.with(context).load(chat.profilePic).placeholder(R.drawable.group_defoult_icon ).
                        into(ivProfile);
            }
            /* if (chat.isTyping){
                tvMsg.setText("typing...");
                tvMsg.setTextColor(context.getResources().getColor(R.color.chatbox_blue));
            }else {
                if (chat.message.contains("https://firebasestorage.googleapis.com") | chat.messageType==1){
                    tvMsg.setText("Image");
                }else
                    tvMsg.setText(chat.message);
                tvMsg.setTextColor(context.getResources().getColor(R.color.grey));
            }*/
            if (chat.message.contains("https://firebasestorage.googleapis.com") | chat.messageType==1
                    | chat.message.contains("content://media/external")
                    | chat.message.contains("content://com.google.android")){
                tvMsg.setText("Image");
            }else{
                tvMsg.setText(chat.message);

            }

            tvMsg.setTextColor(context.getResources().getColor(R.color.grey));

            ivProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChatHistory chatHistory = chatHistories.get(getAdapterPosition());
                    Intent intent = new Intent(context, GroupDetailActivity.class);
                    intent.putExtra("groupId",chatHistory.reciverId);
                    context.startActivity(intent);
                }
            });

            if (chat.unreadMessage!=0){
                rlMsgCount.setVisibility(View.VISIBLE);
                tvUnReadCount.setText(""+chat.unreadMessage);
            }else {
                rlMsgCount.setVisibility(View.GONE);
            }

            if (chat.isMute){
                ivMuteIcon.setVisibility(View.VISIBLE);
            }else {
                ivMuteIcon.setVisibility(View.GONE);
            }

            if (!chat.banner_date.equals(chatHistories.get(tempPos).banner_date)) {
                tvHistoryTime.setText(chat.banner_date);
                viewTop.setVisibility(View.GONE);
                llHistoryDate.setVisibility(View.VISIBLE);
                vBottom.setVisibility(View.GONE);
            } else {
                if (chat.banner_date.equalsIgnoreCase("Today") && position==0){
                    vBottom.setVisibility(View.GONE);
                    viewTop.setVisibility(View.GONE);
                    llHistoryDate.setVisibility(View.GONE);
                }else if (chat.banner_date.equalsIgnoreCase("Yesterday") && position==0){
                    tvHistoryTime.setText(chat.banner_date);
                    vBottom.setVisibility(View.GONE);
                    viewTop.setVisibility(View.GONE);
                    llHistoryDate.setVisibility(View.VISIBLE);
                }else {
                    vBottom.setVisibility(View.GONE);
                    viewTop.setVisibility(View.VISIBLE);
                    llHistoryDate.setVisibility(View.GONE);
                }
            }

            if (position==(chatHistories.size()-1)){
                vBottom.setVisibility(View.VISIBLE);
            }

            SimpleDateFormat sd = new SimpleDateFormat("hh:mm a",Locale.getDefault());
            try {
                String date = sd.format(new Date((Long) chat.timestamp));
                tvTime.setText(date);

            }catch (Exception e){
                e.printStackTrace();
            }
           // setAnimation(itemView, position);

            ly_delete_group_chat.setOnClickListener(v ->{
                Toast.makeText(context, "ly_delete_group_chat", Toast.LENGTH_SHORT).show();
                swipeLayout.close(true,true);
            });
        }

        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 600) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (v.getId()){
                case R.id.rlChatHistory:
                    ChatHistory chatHistory = chatHistories.get(getAdapterPosition());
                    Intent chat_intent = new Intent(context, GroupChatActivity.class);
                    chat_intent.putExtra("groupId",chatHistory.reciverId);
                    context.startActivity(chat_intent);
                    ((ChatHistoryActivity)context).finish();
                    break;
            }
        }
    }

    public void setTyping(int position){
        // this.isTyping = isTyping;
        notifyItemChanged(position);
    }

    public void setMute(int position){
        // this.isTyping = isTyping;
        notifyItemChanged(position);
    }

    public void filterList(List<ChatHistory> filterdNames) {
        this.chatHistories = filterdNames;
        notifyDataSetChanged();
    }


}

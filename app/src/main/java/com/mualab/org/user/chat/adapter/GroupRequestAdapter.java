package com.mualab.org.user.chat.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.mualab.org.user.R;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.GroupRequestActivity;
import com.mualab.org.user.chat.model.ChatHistory;
import com.mualab.org.user.chat.model.FirebaseUser;
import com.mualab.org.user.chat.model.GroupMember;
import com.mualab.org.user.chat.model.Groups;
import com.mualab.org.user.utils.Helper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.logging.Handler;

public class GroupRequestAdapter extends RecyclerView.Adapter<GroupRequestAdapter.ViewHolder> {
    Context mContext;
    ArrayList<GroupRequestActivity.CustomData> dataArrayList;
    AccpetClick accpetClick;

    public GroupRequestAdapter(Context mContext, ArrayList<GroupRequestActivity.CustomData> dataArrayList, AccpetClick accpetClick) {
        this.mContext = mContext;
        this.dataArrayList = dataArrayList;
        this.accpetClick = accpetClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_group_request_chat, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        final GroupRequestActivity.CustomData customData = dataArrayList.get(i);

        viewHolder.tvUname.setText(customData.firebaseUser.userName);
        viewHolder.tvDate.setText(converteTimestamp(customData.firebaseUser.lastActivity + ""));
        viewHolder.tvMsg.setText(customData.groups.groupName + "");

        if (!customData.firebaseUser.profilePic.equals(""))
            Picasso.with(mContext)
                    .load(customData.firebaseUser.profilePic).fit()
                    .placeholder(R.drawable.default_placeholder)
                    .into(viewHolder.ivProfile);

        viewHolder.ivProfile.setOnClickListener(v -> {
            Helper.apiForgetUserIdFromUserName(customData.firebaseUser.userName,mContext);
        });

        viewHolder.tvReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myId = String.valueOf(Mualab.currentUser.id);

                FirebaseDatabase.getInstance().getReference().child("my_group_request")
                        .child(String.valueOf(customData.firebaseUser.uId)).child(customData.groups.groupId).removeValue();


                FirebaseDatabase.getInstance().getReference().child("group_request").
                        child(myId).child(customData.firebaseUser.authToken).removeValue();

                dataArrayList.remove(i);
                notifyDataSetChanged();
            }
        });

        viewHolder.tvAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accpetClick.RemoveListner();
                String myId = String.valueOf(Mualab.currentUser.id);
                String groupId = dataArrayList.get(i).groups.groupId;
                FirebaseUser user = dataArrayList.get(i).firebaseUser;
                Groups groups = dataArrayList.get(i).groups;
                String uId = String.valueOf(dataArrayList.get(i).firebaseUser.uId);

                FirebaseDatabase.getInstance().getReference().child("group_request").
                        child(myId).child(customData.firebaseUser.authToken).removeValue();

                FirebaseDatabase.getInstance().getReference().child("my_group_request")//pending
                        .child(String.valueOf(customData.firebaseUser.uId)).child(customData.groups.groupId).removeValue();

                //add data  to member table...
                GroupMember groupMember = new GroupMember();
                groupMember.createdDate = ServerValue.TIMESTAMP;
                groupMember.firebaseToken = user.firebaseToken;
                groupMember.memberId = user.uId;
                groupMember.mute = 0;
                groupMember.profilePic = user.profilePic;
                groupMember.type = "member";
                groupMember.userName = user.userName;

                FirebaseDatabase.getInstance().getReference().child("group")
                        .child(groupId).child("member").child(uId).setValue(groupMember);

                FirebaseDatabase.getInstance().getReference().child("myGroup")
                        .child(uId).child(groupId).setValue(groupId);

                //Add data to history table
                ChatHistory chatHistory = new ChatHistory();
                chatHistory.favourite = 0;
                chatHistory.memberCount = 0;
                chatHistory.memberType = "member";
                chatHistory.message = "";
                chatHistory.messageType = 0;
                chatHistory.profilePic = groups.groupImg;
                chatHistory.reciverId = groupId;
                chatHistory.senderId = uId;
                chatHistory.type = "group";
                chatHistory.unreadMessage = 0;
                chatHistory.userName = groups.groupName;
                chatHistory.timestamp = ServerValue.TIMESTAMP;

                FirebaseDatabase.getInstance().getReference().child("chat_history")
                        .child(uId).child(groupId).setValue(chatHistory);

                FirebaseDatabase.getInstance().getReference().child("group_msg_delete")
                        .child(uId).child(groupId).child("deleteBy").setValue(ServerValue.TIMESTAMP);


                accpetClick.ActionAccept(i);


            }
        });
    }

    public interface AccpetClick {
         void ActionAccept(int pos);
         void RemoveListner();
    }


    @Override
    public int getItemCount() {
        return dataArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvUname, tvMsg, tvDate, tvReject, tvAccept;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUname = itemView.findViewById(R.id.tvUname);
            tvMsg = itemView.findViewById(R.id.tvMsg);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvReject = itemView.findViewById(R.id.tvReject);
            tvAccept = itemView.findViewById(R.id.tvAccept);
            ivProfile = itemView.findViewById(R.id.ivProfile);
        }
    }

    private CharSequence converteTimestamp(String mileSegundos) {
        return DateUtils.getRelativeTimeSpanString(Long.parseLong(mileSegundos), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
    }


}

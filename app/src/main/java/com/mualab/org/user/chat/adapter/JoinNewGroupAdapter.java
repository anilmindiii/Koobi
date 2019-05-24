package com.mualab.org.user.chat.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.mualab.org.user.R;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.JoinNewGroupActivity;
import com.mualab.org.user.chat.model.GroupRequestInfo;
import com.mualab.org.user.chat.model.Groups;
import com.mualab.org.user.utils.Helper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class JoinNewGroupAdapter extends RecyclerView.Adapter<JoinNewGroupAdapter.ViewHolder> {
    Context mContext;
    ArrayList<Groups> arrayList;


    public JoinNewGroupAdapter(Context mContext, ArrayList<Groups> arrayList) {
        this.mContext = mContext;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_join_new_group,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        Groups groups = arrayList.get(i);
        viewHolder.tvUname.setText(groups.groupName);
        viewHolder.tvMsg.setText(groups.groupDescription);
        viewHolder.tvMemberCount.setText((groups.member.size())+" Members");



        if(groups.isPending){
            viewHolder.rlPending.setVisibility(View.VISIBLE);
            viewHolder.rlJoin.setVisibility(View.GONE);
        }else {
            viewHolder.rlJoin.setVisibility(View.VISIBLE);
            viewHolder.rlPending.setVisibility(View.GONE);

        }

        if(!groups.groupImg.equals(""))
        Picasso.with(mContext)
                .load(groups.groupImg).fit().placeholder(R.drawable.group_defoult_icon).into(viewHolder.ivProfile);



        viewHolder.rlJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myId = String.valueOf(Mualab.currentUser.id);
                String groupId = arrayList.get(i).groupId;
                String adminId = String.valueOf(arrayList.get(i).adminId);

                GroupRequestInfo requestInfo = new GroupRequestInfo();
                requestInfo.groupId = groupId;
                requestInfo.senderId = myId;
                requestInfo.timestamp = ServerValue.TIMESTAMP;;

                FirebaseDatabase.getInstance().getReference().child("my_group_request").
                        child(myId).child(groupId).setValue(groupId);


                FirebaseDatabase.getInstance().getReference().child("group_request").
                        child(adminId).push().setValue(requestInfo);

                arrayList.get(i).isPending = true;
                notifyDataSetChanged();
            }
        });
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvUname,tvMsg,tvMemberCount;
        ImageView ivProfile;
        RelativeLayout rlJoin,rlPending;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUname = itemView.findViewById(R.id.tvUname);
            tvMsg = itemView.findViewById(R.id.tvMsg);
            tvMemberCount = itemView.findViewById(R.id.tvMemberCount);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            rlJoin = itemView.findViewById(R.id.rlJoin);
            rlPending = itemView.findViewById(R.id.rlPending);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }
}

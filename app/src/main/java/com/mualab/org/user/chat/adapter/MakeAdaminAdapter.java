package com.mualab.org.user.chat.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.chat.MakeAdminActivity;
import com.mualab.org.user.chat.model.GroupMember;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MakeAdaminAdapter extends RecyclerView.Adapter<MakeAdaminAdapter.ViewHolder> {
    Context mContext;
    List<GroupMember> userList;
    valueListner listner;

    public MakeAdaminAdapter(Context mContext, ArrayList<GroupMember> userList,valueListner listner) {
        this.userList = userList;
        this.mContext = mContext;
        this.listner = listner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_userlist_chat,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        GroupMember member = userList.get(i);
        if(member.isChecked){
            holder.chat_checkbox.setImageResource(R.drawable.active_check_ico);
        }else{
            holder.chat_checkbox.setImageResource(R.drawable.inactive_check_ico);
        }

        holder.tvUserName.setText(member.userName);

        if (member.profilePic!=null && !member.profilePic.equals("")){
            Picasso.with(mContext).load(member.profilePic).
                    placeholder(R.drawable.default_placeholder).fit().into(holder.ivProfilePic);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView chat_checkbox;
        ImageView ivProfilePic;
        TextView tvUserName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            chat_checkbox = itemView.findViewById(R.id.chat_checkbox);
            ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
            tvUserName = itemView.findViewById(R.id.tvUserName);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            for(int i= 0;i<userList.size();i++){
                userList.get(i).isChecked = false;
            }
            userList.get(getAdapterPosition()).isChecked = true;

            listner.getValue(userList.get(getAdapterPosition()));
            notifyDataSetChanged();

        }
    }

    public interface valueListner{
        void getValue(GroupMember member);
    }

    public void filterList(List<GroupMember> groupMember) {
        this.userList = groupMember;
        notifyDataSetChanged();
    }
}

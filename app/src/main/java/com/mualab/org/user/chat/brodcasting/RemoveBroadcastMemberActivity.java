package com.mualab.org.user.chat.brodcasting;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.mualab.org.user.R;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.RemoveMemberActivity;
import com.mualab.org.user.chat.adapter.RemoveNewMemberListAdapter;
import com.mualab.org.user.chat.adapter.SelectedMemberListAdapter;
import com.mualab.org.user.chat.listner.OnCancleMemberClickListener;
import com.mualab.org.user.chat.listner.OnMemberSelectListener;
import com.mualab.org.user.chat.model.GroupMember;
import com.mualab.org.user.chat.model.Groups;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.KeyboardUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RemoveBroadcastMemberActivity extends AppCompatActivity implements View.OnClickListener,
        OnMemberSelectListener, OnCancleMemberClickListener {

    private List<GroupMember> groupMembers,selectedMemberList;
    private RemoveNewMemberListAdapter membersListAdapter;
    private SelectedMemberListAdapter selectedMemberListAdapter;
    private Map<String,GroupMember> map;
    private String myUid,broadcastId;
    private ProgressBar progress_bar;
    private TextView tv_no_chat;
    private View vSaperater;
    private DatabaseReference mFirebaseBroadcastRef,chatHitoryRef;
    private RecyclerView rvSelectedMember;
    private Groups groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_broadcast_member);

        Intent intent = getIntent();
        broadcastId = intent.getStringExtra("broadcastId");
        //   action = intent.getStringExtra("action");
        myUid = String.valueOf(Mualab.currentUser.id);

        mFirebaseBroadcastRef = FirebaseDatabase.getInstance().getReference().child("broadcast").child(broadcastId);

        init();
    }

    private void init() {
        groupMembers = new ArrayList<>();
        selectedMemberList = new ArrayList<>();
        map = new HashMap<>();
        //  fbTokenListForMobile = new ArrayList<>();
        //  fbTokenListForWeb = new ArrayList<>();

        membersListAdapter = new RemoveNewMemberListAdapter(this,groupMembers);
        selectedMemberListAdapter = new SelectedMemberListAdapter(this,selectedMemberList);
        membersListAdapter.setListener(this);
        selectedMemberListAdapter.setListener(this);

        RecyclerView rvMembers = findViewById(R.id.rvMembers);
        LinearLayoutManager layoutManager = new LinearLayoutManager(RemoveBroadcastMemberActivity.this,
                LinearLayoutManager.VERTICAL, false);
        rvMembers.setLayoutManager(layoutManager);
        rvMembers.setAdapter(membersListAdapter);

        rvSelectedMember = findViewById(R.id.rvSelectedMember);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(RemoveBroadcastMemberActivity.this,
                LinearLayoutManager.HORIZONTAL, false);
        rvSelectedMember.setLayoutManager(layoutManager2);
        rvSelectedMember.setAdapter(selectedMemberListAdapter);

        vSaperater = findViewById(R.id.vSaperater);
        progress_bar = findViewById(R.id.progress_bar);
        tv_no_chat = findViewById(R.id.tv_no_chat);

        TextView btnAddMem = findViewById(R.id.btnAddMem);
        TextView btnRemoveMem = findViewById(R.id.btnRemoveMem);
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvChatTitle = findViewById(R.id.tvHeaderTitle);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progress_bar.setVisibility(View.GONE);
            }
        },2000);

        tvChatTitle.setText("Remove Recipients");
        btnRemoveMem.setVisibility(View.VISIBLE);
        btnAddMem.setVisibility(View.GONE);

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(RemoveBroadcastMemberActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        getUserListForRemove();
                        //getting group data from group table
                        new Thread(new Runnable(){
                            @Override
                            public void run(){
                                getGroupDetail();
                            }
                        }).start();
                    }
                }
            }).show();
        }else {
            //getting group data from group table
            getUserListForRemove();
            //getting group data from group table
            new Thread(new Runnable(){
                @Override
                public void run(){
                    progress_bar.setVisibility(View.VISIBLE);
                    getGroupDetail();
                }
            }).start();
        }
        EditText searchview = findViewById(R.id.searchview);
        KeyboardUtil.hideKeyboard(searchview, RemoveBroadcastMemberActivity.this);
        searchview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString().trim();
                filter(str);
            }
        });

        btnRemoveMem.setOnClickListener(this);
        btnBack.setOnClickListener(this);

    }

    private void filter(String text) {
        List<GroupMember> filterdNames = new ArrayList<>();

        for (GroupMember s : groupMembers) {
            if (s.userName.toLowerCase(Locale.getDefault()).contains(text)) {
                filterdNames.add(s);
            }
        }

      /*  if (filterdNames.size()==0)
            tv_no_chat.setVisibility(View.VISIBLE);
        else
            tv_no_chat.setVisibility(View.GONE);*/
        membersListAdapter.filterList(filterdNames);


    }

    private void getUserListForRemove(){
        mFirebaseBroadcastRef.child("member").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    final GroupMember user = dataSnapshot.getValue(GroupMember.class);
                    assert user != null;
                    if (user. memberId!=Mualab.currentUser.id) {
                        getDataInMap(dataSnapshot.getKey(), user);

                    }
                } else {
                    if (groupMembers.size()==0)
                        tv_no_chat.setVisibility(View.VISIBLE);
                    else
                        tv_no_chat.setVisibility(View.GONE);

                    progress_bar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    final GroupMember user = dataSnapshot.getValue(GroupMember.class);
                    assert user != null;
                    if (user. memberId!=Mualab.currentUser.id) {
                        getDataInMap(dataSnapshot.getKey(), user);

                    }
                }else {
                    if (groupMembers.size()==0)
                        tv_no_chat.setVisibility(View.VISIBLE);
                    else
                        tv_no_chat.setVisibility(View.GONE);

                    progress_bar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                progress_bar.setVisibility(View.GONE);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress_bar.setVisibility(View.GONE);
            }
        });
    }

    private void getGroupDetail(){
        mFirebaseBroadcastRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    try {
                        groups = dataSnapshot.getValue(Groups.class);

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
                progress_bar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress_bar.setVisibility(View.GONE);
            }
        });
    }

    private void getDataInMap(String key, GroupMember user) {
        if (user != null) {
            user.isChecked = false;
            map.put(key, user);
            groupMembers.clear();
            Collection<GroupMember> values = map.values();
            groupMembers.addAll(values);
            membersListAdapter.notifyDataSetChanged();
        }

        if (groupMembers.size()==0)
            tv_no_chat.setVisibility(View.VISIBLE);
        else
            tv_no_chat.setVisibility(View.GONE);

        progress_bar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.btnRemoveMem:
                if (!ConnectionDetector.isConnected()) {
                    new NoConnectionDialog(RemoveBroadcastMemberActivity.this, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if(isConnected){
                                dialog.dismiss();
                                removeMembers();
                            }
                        }
                    }).show();
                }else {
                    removeMembers();
                }
                break;
        }
    }

    private void removeMembers(){
        if (selectedMemberList.size()!=0){

            int totalSize = groupMembers.size();
            int selectedSize = selectedMemberList.size();
            int result = (totalSize-selectedSize);

            if(result < 2){
                MyToast.getInstance(RemoveBroadcastMemberActivity.this).showDasuAlert("A broadcast group must have more than one recipient.");
                return;
            }

            /*if(groups.member != null){
                int size = ((groups.member.size()+selectedMemberList.size())-1);
                mFirebaseBroadcastRef.child("groupName").setValue(size+" Recipients");
            }*/
            for (int i=0;i<selectedMemberList.size();i++){
                GroupMember groupMemberVal = selectedMemberList.get(i);

                // myGroupRef.child(String.valueOf(groupMemberVal.memberId)).child(groupId).setValue(null);

                mFirebaseBroadcastRef.child("member").child(String.valueOf(groupMemberVal.
                        memberId)).setValue(null);


            }

            finish();
            // sendPushNotificationToReceiver();
        }else {
            MyToast.getInstance(RemoveBroadcastMemberActivity.this).showDasuAlert("Please select a group member to remove");
        }
    }

    @Override
    public void onMemberSelect(GroupMember user, int position) {
        if (selectedMemberList.size()!=0){
            if(!user.isChecked) {
                for (int i=0;i<selectedMemberList.size();i++){
                    GroupMember groupMember = selectedMemberList.get(i);
                    if (user.memberId==groupMember.memberId){
                        selectedMemberList.remove(i);
                        break;
                    }
                }
            }else {
                GroupMember groupMember = new GroupMember();
                groupMember.createdDate = ServerValue.TIMESTAMP;
                groupMember.firebaseToken = user.firebaseToken;
                groupMember.memberId = user.memberId;
                groupMember.mute = 0;
                groupMember.profilePic = user.profilePic;
                groupMember.type = "member";
                groupMember.userName = user.userName;
                selectedMemberList.add(groupMember);
            }
        }else {
            GroupMember groupMember = new GroupMember();
            groupMember.createdDate = ServerValue.TIMESTAMP;
            groupMember.firebaseToken = user.firebaseToken;
            groupMember.memberId = user.memberId;
            groupMember.mute = 0;
            groupMember.profilePic = user.profilePic;
            groupMember.type = "member";
            groupMember.userName = user.userName;
            selectedMemberList.add(groupMember);
        }

        if (selectedMemberList.size()==0) {
            rvSelectedMember.setVisibility(View.GONE);
            vSaperater.setVisibility(View.GONE);
        }
        else {
            rvSelectedMember.setVisibility(View.VISIBLE);
            vSaperater.setVisibility(View.VISIBLE);
        }

        selectedMemberListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMemberClicked(GroupMember user, int position) {
        if (groupMembers.size()!=0){
            for (int i=0;i<groupMembers.size();i++){
                GroupMember firebaseUser = groupMembers.get(i);
                if (firebaseUser.memberId==user.memberId){
                    firebaseUser.isChecked = false;
                    membersListAdapter.notifyItemChanged(i);
                    break;
                }
            }
        }
        if (selectedMemberList.size()==0)
            vSaperater.setVisibility(View.GONE);
        else
            vSaperater.setVisibility(View.VISIBLE);
    }
}

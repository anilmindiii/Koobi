package com.mualab.org.user.chat.brodcasting;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.mualab.org.user.R;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.AddMemberActivity;
import com.mualab.org.user.chat.adapter.AddNewMemberListAdapter;
import com.mualab.org.user.chat.adapter.SelectedMemberListAdapter;
import com.mualab.org.user.chat.listner.OnCancleMemberClickListener;
import com.mualab.org.user.chat.listner.OnUserClickListener;
import com.mualab.org.user.chat.model.ChatHistory;
import com.mualab.org.user.chat.model.FirebaseUser;
import com.mualab.org.user.chat.model.GroupMember;
import com.mualab.org.user.chat.model.Groups;
import com.mualab.org.user.chat.model.WebNotification;
import com.mualab.org.user.chat.notification_builder.FcmNotificationBuilder;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.KeyboardUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddMemberToBrodcastActivity extends AppCompatActivity implements View.OnClickListener,
        OnUserClickListener, OnCancleMemberClickListener {

    private List<FirebaseUser> groupMembers;
    private List<GroupMember> selectedMemberList;
    private AddNewMemberListAdapter membersListAdapter;
    private SelectedMemberListAdapter selectedMemberListAdapter;
    private Map<String, FirebaseUser> map;
    private String myUid;
    private String broadcastId;
    private ProgressBar progress_bar;
    private DatabaseReference mFirebaseUserDbRef, chatHitoryRef, myGroupRef,
            chatRefWebnotif, groupMsgDeleteRef;
    private DatabaseReference mFirebaseBrodcastRef, BrodcastRef;
    private EditText searchview;
    private RecyclerView rvSelectedMember;
    private Groups groups;
    private TextView tv_no_chat;
    private View vSaperater;
    private List<String> fbTokenListForMobile, fbTokenListForWeb;
    private LinkedList<String> lastGroupName;
    private boolean shouldRun;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member_to_brodcast);
        Intent intent = getIntent();
        broadcastId = intent.getStringExtra("broadcastId");
        // action = intent.getStringExtra("action");
        myUid = String.valueOf(Mualab.currentUser.id);
        lastGroupName = new LinkedList<>();
        shouldRun = true;
        if (broadcastId != null)
            mFirebaseBrodcastRef = FirebaseDatabase.getInstance().getReference().child("broadcast")
                    .child(broadcastId);

        BrodcastRef = FirebaseDatabase.getInstance().getReference().child("broadcast");
        mFirebaseUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");
        chatHitoryRef = FirebaseDatabase.getInstance().getReference().child("chat_history");
        myGroupRef = FirebaseDatabase.getInstance().getReference().child("myGroup");
        chatRefWebnotif = FirebaseDatabase.getInstance().getReference().child("webnotification");
        groupMsgDeleteRef = FirebaseDatabase.getInstance().getReference().child("group_msg_delete");

        getGroupNodeInfo();
        init();
        TextView tvChatTitle = findViewById(R.id.tvHeaderTitle);
        if (broadcastId == null) {
            tvChatTitle.setText("New Broadcast");
        } else {
            tvChatTitle.setText("Add New Recipients");
        }
    }

    private void init() {
        groupMembers = new ArrayList<>();
        selectedMemberList = new ArrayList<>();
        map = new HashMap<>();
        fbTokenListForMobile = new ArrayList<>();
        fbTokenListForWeb = new ArrayList<>();

        membersListAdapter = new AddNewMemberListAdapter(this, groupMembers);
        selectedMemberListAdapter = new SelectedMemberListAdapter(this, selectedMemberList);
        membersListAdapter.setListener(this);
        selectedMemberListAdapter.setListener(this);

        RecyclerView rvMembers = findViewById(R.id.rvMembers);
        LinearLayoutManager layoutManager = new LinearLayoutManager(AddMemberToBrodcastActivity.this,
                LinearLayoutManager.VERTICAL, false);
        rvMembers.setLayoutManager(layoutManager);
        rvMembers.setAdapter(membersListAdapter);

        vSaperater = findViewById(R.id.vSaperater);
        rvSelectedMember = findViewById(R.id.rvSelectedMember);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(AddMemberToBrodcastActivity.this,
                LinearLayoutManager.HORIZONTAL, false);
        rvSelectedMember.setLayoutManager(layoutManager2);
        rvSelectedMember.setAdapter(selectedMemberListAdapter);

        progress_bar = findViewById(R.id.progress_bar);
        tv_no_chat = findViewById(R.id.tv_no_chat);

        TextView btnAddMem = findViewById(R.id.btnAddMem);
        TextView btnRemoveMem = findViewById(R.id.btnRemoveMem);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnRemoveMem.setVisibility(View.GONE);
        btnAddMem.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progress_bar.setVisibility(View.GONE);
            }
        }, 2000);

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(AddMemberToBrodcastActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        getUserListForAdd();
                        //getting group data from group table
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (broadcastId != null)
                                    getGroupDetail();
                            }
                        }).start();
                    }
                }
            }).show();
        } else {
            //getting group data from group table
            getUserListForAdd();
            //getting group data from group table
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (broadcastId != null)
                        getGroupDetail();
                }
            }).start();
        }
        searchview = findViewById(R.id.searchview);
        KeyboardUtil.hideKeyboard(searchview, AddMemberToBrodcastActivity.this);
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


        btnAddMem.setOnClickListener(this);
        btnBack.setOnClickListener(this);

    }

    private void filter(String text) {
        List<FirebaseUser> filterdNames = new ArrayList<>();

        for (FirebaseUser s : groupMembers) {
            if (s.userName != null)
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


    private void getUserListForAdd() {
        mFirebaseUserDbRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    final FirebaseUser user = dataSnapshot.getValue(FirebaseUser.class);
                    assert user != null;
                    try {
                        if (user.uId != Mualab.currentUser.id) {

                            if (broadcastId == null) {
                                getDataInMap(dataSnapshot.getKey(), user);
                            } else {
                                mFirebaseBrodcastRef.child("member").child(String.valueOf(user.uId)).
                                        addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                String key = dataSnapshot.getKey();
                                                if (dataSnapshot.getValue() == null || !dataSnapshot.exists()) {
                                                    try {
                                                        getDataInMap(dataSnapshot.getKey(), user);

                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }

                                                } else {
                                                    if (groupMembers.size() == 0)
                                                        tv_no_chat.setVisibility(View.VISIBLE);
                                                    else
                                                        tv_no_chat.setVisibility(View.GONE);

                                                    progress_bar.setVisibility(View.GONE);
                                                }

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                progress_bar.setVisibility(View.GONE);

                                            }
                                        });
                            }


                        }
                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    final FirebaseUser user = dataSnapshot.getValue(FirebaseUser.class);
                    assert user != null;
                    if (user.uId != Mualab.currentUser.id) {
                        if (broadcastId == null) {
                            getDataInMap(dataSnapshot.getKey(), user);
                        } else {
                            FirebaseDatabase.getInstance().getReference().child("broadcast")
                                    .child(broadcastId).child("member").child(String.valueOf(user.uId)).
                                    addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String key = dataSnapshot.getKey();
                                            if (dataSnapshot.getValue() == null || !dataSnapshot.exists()) {
                                                try {
                                                    getDataInMap(dataSnapshot.getKey(), user);

                                                } catch (Exception e) {
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
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getGroupDetail() {
        mFirebaseBrodcastRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    try {
                        groups = dataSnapshot.getValue(Groups.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress_bar.setVisibility(View.GONE);
            }
        });
    }

    private void getDataInMap(String key, FirebaseUser user) {
        if (user != null) {
            user.isChecked = false;
            if (user.userName != null)
                if (!user.userName.equals(""))
                    map.put(key, user);

            groupMembers.clear();
            Collection<FirebaseUser> values = map.values();
            groupMembers.addAll(values);
            membersListAdapter.notifyDataSetChanged();

            if (groupMembers.size() == 0)
                tv_no_chat.setVisibility(View.VISIBLE);
            else
                tv_no_chat.setVisibility(View.GONE);
            progress_bar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.btnAddMem:

                if (!ConnectionDetector.isConnected()) {
                    new NoConnectionDialog(AddMemberToBrodcastActivity.this, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if (isConnected) {
                                dialog.dismiss();
                                if (broadcastId != null) {
                                    addAdditionalMembers();
                                } else addMembers();
                            }
                        }
                    }).show();
                } else {
                    if (broadcastId != null) {
                        addAdditionalMembers();
                    } else addMembers();
                }
                break;
        }
    }

    private void getGroupNodeInfo() {
        lastGroupName.clear();
        BrodcastRef.orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getKey();
                lastGroupName.add(key);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getKey();
                lastGroupName.add(key);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void addAdditionalMembers() {
        Progress.show(AddMemberToBrodcastActivity.this);
      /*  if(groups.member != null){
            int size = ((groups.member.size()+selectedMemberList.size())-1);
            BrodcastRef.child(broadcastId).child("groupName").setValue(size+" Recipients");
        }*/

        if (selectedMemberList.size() != 0) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 8000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            for (int i = 0; i < selectedMemberList.size(); i++) {
                final GroupMember groupMemberVal = selectedMemberList.get(i);

                BrodcastRef.child(broadcastId).child("member").child(String.valueOf(groupMemberVal.
                        memberId)).setValue(groupMemberVal);

                fbTokenListForMobile.add(groupMemberVal.firebaseToken);
                fbTokenListForWeb.add(groupMemberVal.firebaseToken);
            }


            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Progress.hide(AddMemberToBrodcastActivity.this);
                        finish();

                    } catch (Exception e) {

                    }

                }
            }, 100);


            // sendPushNotificationToReceiver();
        } else {
            MyToast.getInstance(AddMemberToBrodcastActivity.this).showDasuAlert("Please select a group member");
        }
    }

    private void addMembers() {
        if (selectedMemberList.size() != 0) {
            if (selectedMemberList.size() == 1) {
                MyToast.getInstance(AddMemberToBrodcastActivity.this).showDasuAlert("Please select atleast 2 member");
                return;
            }

            progress_bar.setVisibility(View.VISIBLE);
            if (SystemClock.elapsedRealtime() - mLastClickTime < 8000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            if (lastGroupName.size() != 0) {
                int groupId = -1;
                int gId = -1;
                for (int i = 0; i < lastGroupName.size(); i++) {
                    String[] namesList = lastGroupName.get(i).split("_");
                    int local = Integer.parseInt(namesList[1]);

                    if (groupId > local) {
                        gId = groupId;
                    }
                    groupId = Integer.parseInt(namesList[1]);

                }


                if (gId == -1)
                    gId = groupId;
                //String lGroupName = lastGroupName.getLast();
                //String[] namesList = lGroupName.split("_");
                //int groupId = Integer.parseInt(namesList[1]);
                broadcastId = "broadcast_" + (gId + 1);
            } else {
                broadcastId = "broadcast_1";
            }

            GroupMember groupMember = new GroupMember();
            groupMember.createdDate = ServerValue.TIMESTAMP;
            groupMember.firebaseToken = FirebaseInstanceId.getInstance().getToken();
            groupMember.memberId = Mualab.currentUser.id;
            groupMember.mute = 0;
            groupMember.profilePic = Mualab.currentUser.profileImage;
            groupMember.type = "admin";
            groupMember.userName = Mualab.currentUser.userName;

            selectedMemberList.add(groupMember);

            Groups group = new Groups();
            Map<String, Object> params = new HashMap<>();
            for (GroupMember groupMemberVal : selectedMemberList) {

                try {
                    // JSONObject jsonObject = new JSONObject();
                    Map<String, Object> jsonObject = new HashMap<>();
                    jsonObject.put("createdDate", groupMemberVal.createdDate);
                    jsonObject.put("firebaseToken", groupMemberVal.firebaseToken);
                    jsonObject.put("memberId", groupMemberVal.memberId);
                    jsonObject.put("mute", groupMemberVal.mute);
                    jsonObject.put("profilePic", groupMemberVal.profilePic);
                    jsonObject.put("type", groupMemberVal.type);
                    jsonObject.put("userName", groupMemberVal.userName);

                    params.put(String.valueOf(groupMemberVal.memberId), jsonObject);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // group.member = new JSONObject(params);
            group.member.putAll(params);
            group.adminId = Integer.parseInt(myUid);
            group.groupImg = "http://koobi.co.uk:3000/front/img/loader.png";
            group.groupName = ((selectedMemberList.size() - 1) + " Recipients");
            ;

            BrodcastRef.child(broadcastId).setValue(group);

            //update my history...

            ChatHistory chatHistory = new ChatHistory();
            chatHistory.favourite = 0;
            chatHistory.memberCount = selectedMemberList.size();
            chatHistory.memberType = "member";
            chatHistory.message = "";
            chatHistory.messageType = 0;
            chatHistory.profilePic = "http://koobi.co.uk:3000/front/img/loader.png";
            chatHistory.reciverId = broadcastId;
            chatHistory.senderId = myUid;
            chatHistory.type = "broadcast";
            chatHistory.unreadMessage = 0;
            chatHistory.userName = ((selectedMemberList.size() - 1) + " Recipients");
            chatHistory.timestamp = ServerValue.TIMESTAMP;

            chatHitoryRef.child(myUid).child(broadcastId).setValue(chatHistory);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 300);

            // mFirebaseGroupRef.child(myGroupId).setValue(group);
            //updateChatHistory();
        } else {
            MyToast.getInstance(AddMemberToBrodcastActivity.this).showDasuAlert("Please select group member");
        }

    }

    private void sendPushNotificationToReceiver() {

        for (int i = 0; i < fbTokenListForWeb.size(); i++) {

            WebNotification webNotification = new WebNotification();
            webNotification.body = Mualab.currentUser.userName + " added you";
            webNotification.title = Mualab.currentUser.userName;
            webNotification.url = "";
            chatRefWebnotif.child(fbTokenListForWeb.get(i)).push().setValue(webNotification);
        }
        if (broadcastId != null)
            FcmNotificationBuilder.initialize()
                    .title(Mualab.currentUser.userName + " @ " + groups.groupName)
                    .message(Mualab.currentUser.userName + " added you")
                    .uid(broadcastId)
                    .username(Mualab.currentUser.userName + " @ " + groups.groupName).
                    adminId(String.valueOf(groups.adminId))
                    .type("groupChat").clickAction("GroupChatActivity")
                    .registrationId(fbTokenListForMobile).send();

        finish();

    }

    @Override
    public void onUserClicked(FirebaseUser user, int position) {
        if (selectedMemberList.size() != 0) {
            if (!user.isChecked) {
                for (int i = 0; i < selectedMemberList.size(); i++) {
                    GroupMember groupMember = selectedMemberList.get(i);
                    if (user.uId == groupMember.memberId) {
                        selectedMemberList.remove(i);
                        break;
                    }
                }
            } else {
                GroupMember groupMember = new GroupMember();
                groupMember.createdDate = ServerValue.TIMESTAMP;
                groupMember.firebaseToken = user.firebaseToken;
                groupMember.memberId = user.uId;
                groupMember.mute = 0;
                groupMember.profilePic = user.profilePic;
                groupMember.type = "member";
                groupMember.userName = user.userName;
                selectedMemberList.add(groupMember);
            }
        } else {
            GroupMember groupMember = new GroupMember();
            groupMember.createdDate = ServerValue.TIMESTAMP;
            groupMember.firebaseToken = user.firebaseToken;
            groupMember.memberId = user.uId;
            groupMember.mute = 0;
            groupMember.profilePic = user.profilePic;
            groupMember.type = "member";
            groupMember.userName = user.userName;
            selectedMemberList.add(groupMember);
        }

        if (selectedMemberList.size() == 0) {
            rvSelectedMember.setVisibility(View.GONE);
            vSaperater.setVisibility(View.GONE);
        } else {
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
        if (groupMembers.size() != 0) {
            for (int i = 0; i < groupMembers.size(); i++) {
                FirebaseUser firebaseUser = groupMembers.get(i);
                if (firebaseUser.uId == user.memberId) {
                    firebaseUser.isChecked = false;
                    membersListAdapter.notifyItemChanged(i);
                    break;
                }
            }
            if (selectedMemberList.size() == 0)
                vSaperater.setVisibility(View.GONE);
            else
                vSaperater.setVisibility(View.VISIBLE);
        }
    }
}

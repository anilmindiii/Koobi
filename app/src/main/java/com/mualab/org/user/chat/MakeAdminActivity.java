package com.mualab.org.user.chat;

import android.content.Intent;
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
import com.mualab.org.user.R;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.adapter.MakeAdaminAdapter;
import com.mualab.org.user.chat.model.GroupMember;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.utils.KeyboardUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MakeAdminActivity extends AppCompatActivity implements MakeAdaminAdapter.valueListner {
    private RecyclerView rvSelectedMember;
    private String groupId = "";
    private String myUid;
    private DatabaseReference groupRef;
    private Map<String, GroupMember> map;
    private ArrayList<GroupMember> userList;
    private MakeAdaminAdapter adaminAdapter;
    private ProgressBar progress_bar;
    private GroupMember member;
    private TextView btnMakeAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_admin);

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
        if (Mualab.currentUser.id != 0)
            myUid = String.valueOf(Mualab.currentUser.id);


        userList = new ArrayList<>();
        map = new HashMap<>();
        adaminAdapter = new MakeAdaminAdapter(this, userList, this);
        groupRef = FirebaseDatabase.getInstance().getReference().child("group").child(groupId);


        btnMakeAdmin = findViewById(R.id.btnMakeAdmin);
        rvSelectedMember = findViewById(R.id.rvMembers);
        progress_bar = findViewById(R.id.progress_bar);
        progress_bar.setVisibility(View.VISIBLE);

        TextView tvChatTitle = findViewById(R.id.tvHeaderTitle);
        ImageView back = findViewById(R.id.btnBack);
        tvChatTitle.setText("Make Admin");

        EditText searchview = findViewById(R.id.searchview);
        KeyboardUtil.hideKeyboard(searchview, MakeAdminActivity.this);
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

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnMakeAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // make admin first
                if (member != null){
                    if (member.isChecked) {
                        FirebaseDatabase.getInstance().getReference().child("group").child(groupId).child("member").
                                child(String.valueOf(member.memberId)).child("type").setValue("admin");

                        FirebaseDatabase.getInstance().getReference().child("group").child(groupId).
                                child("adminId").setValue(member.memberId);

                        FirebaseDatabase.getInstance().getReference().child("group").child(groupId).child("member").
                                child(myUid).removeValue();

                        FirebaseDatabase.getInstance().getReference().child("myGroup").child(myUid).child(groupId).removeValue();

                        finish();
                    } else {

                        MyToast.getInstance(MakeAdminActivity.this).showDasuAlert("Please select admin");
                    }
                }else {

                    MyToast.getInstance(MakeAdminActivity.this).showDasuAlert("Please select admin");
                }


            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(this);
        rvSelectedMember.setLayoutManager(manager);
        rvSelectedMember.setAdapter(adaminAdapter);
        getMemberList();
    }

    private void filter(String text) {
        List<GroupMember> filterdNames = new ArrayList<>();

        for (GroupMember s : userList) {
            if (s.userName.toLowerCase(Locale.getDefault()).contains(text)) {
                filterdNames.add(s);
            }
        }

        adaminAdapter.filterList(filterdNames);


    }

    private void getMemberList() {
        groupRef.child("member").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    GroupMember member = dataSnapshot.getValue(GroupMember.class);
                    if (member.memberId != Mualab.currentUser.id)
                        getDataInMap(dataSnapshot.getKey(), member);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    GroupMember member = dataSnapshot.getValue(GroupMember.class);
                    if (member.memberId != Mualab.currentUser.id)
                        getDataInMap(dataSnapshot.getKey(), member);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                GroupMember member = dataSnapshot.getValue(GroupMember.class);
                assert member != null;
                if (member.memberId == Mualab.currentUser.id)
                    finish();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getDataInMap(String key, GroupMember user) {

        if (user != null) {
            //check are u admin of this group
            if (user.memberId == Mualab.currentUser.id && user.type.equals("admin")) {
            }

            user.isChecked = false;
            map.put(key, user);
            userList.clear();
            Collection<GroupMember> values = map.values();
            userList.addAll(values);
        }

        adaminAdapter.notifyDataSetChanged();
        progress_bar.setVisibility(View.GONE);
    }

    @Override
    public void getValue(GroupMember member) {
        this.member = member;
    }
}

package com.mualab.org.user.chat;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mualab.org.user.R;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.adapter.JoinNewGroupAdapter;
import com.mualab.org.user.chat.model.ChatHistory;
import com.mualab.org.user.chat.model.GroupChat;
import com.mualab.org.user.chat.model.Groups;
import com.mualab.org.user.dialogs.Progress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class JoinNewGroupActivity extends AppCompatActivity {
    JoinNewGroupAdapter adapter;
    RecyclerView recycler_view;
    DatabaseReference groupDataBaseRef, myGroupRef, myGroupRequestRef;
    Map<String, Groups> groupsMap;
    private String myId;
    private String myGroupRequestIds = "";
    private String myGroupIds = "";
    private ArrayList<Groups> arrayList;
    private EditText searchview;
    private TextView tv_no_chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_new_group);

        myId = String.valueOf(Mualab.currentUser.id);
        groupDataBaseRef = FirebaseDatabase.getInstance().getReference().child("group");
        myGroupRef = FirebaseDatabase.getInstance().getReference().child("myGroup").child(myId);
        myGroupRequestRef = FirebaseDatabase.getInstance().getReference().child("my_group_request").child(myId);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText("Join New Group");
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tv_no_chat = findViewById(R.id.tv_no_chat);
        searchview = findViewById(R.id.searchview);
        recycler_view = findViewById(R.id.recycler_view);
        arrayList = new ArrayList<>();
        groupsMap = new HashMap<>();


        adapter = new JoinNewGroupAdapter(this, arrayList);
        recycler_view.setAdapter(adapter);

        searchview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                ArrayList<Groups> tempList = new ArrayList<>();

                for (int i = 0; i < arrayList.size(); i++) {
                    if (arrayList.get(i).groupName.toLowerCase().contains(s.toString().toLowerCase().trim())) {
                        tempList.add(arrayList.get(i));
                    }
                }
                adapter = new JoinNewGroupAdapter(JoinNewGroupActivity.this, tempList);
                recycler_view.setAdapter(adapter);
            }
        });

        getMyGroupAndRequesData();

    }

    private void getMyGroupAndRequesData() {
        myGroupRequestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    myGroupRequestIds = dataSnapshot.getValue() + "," + myGroupRequestIds;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        myGroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    myGroupIds = dataSnapshot.getValue() + "," + myGroupIds;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Progress.show(this);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Progress.hide(JoinNewGroupActivity.this);
                getGroupData();
            }
        }, 2000);


    }

    private void getGroupData() {

        groupDataBaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                getData(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                getData(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }

            private void getData(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Groups groups = dataSnapshot.getValue(Groups.class);
                    if (groups != null) {
                        String key = dataSnapshot.getKey();


                        if (!myGroupIds.contains(key) && !myGroupRequestIds.contains(key))
                        {
                            groupsMap.put(key, groups);

                            Collection<Groups> demoValues = groupsMap.values();
                            arrayList.clear();
                            arrayList.addAll(demoValues);
                            adapter.notifyDataSetChanged();

                            if(arrayList.size() == 0){
                                tv_no_chat.setVisibility(View.VISIBLE);
                            } else tv_no_chat.setVisibility(View.GONE);

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }
}

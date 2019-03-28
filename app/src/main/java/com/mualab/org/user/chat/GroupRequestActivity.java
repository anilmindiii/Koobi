package com.mualab.org.user.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mualab.org.user.R;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.adapter.GroupRequestAdapter;
import com.mualab.org.user.chat.model.Chat;
import com.mualab.org.user.chat.model.FirebaseUser;
import com.mualab.org.user.chat.model.GroupChat;
import com.mualab.org.user.chat.model.GroupRequestInfo;
import com.mualab.org.user.chat.model.Groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class GroupRequestActivity extends AppCompatActivity {
    GroupRequestAdapter adapter;
    RecyclerView recyclerView;
    DatabaseReference mGroupReqRef,groupRef,mUserRef;
    String myId ;
    ArrayList<CustomData> dataArrayList;
    Map<String,CustomData> customDataMap;
    private TextView tv_no_chat;
    private ValueEventListener listenerGroup,listenerUser;
    boolean isAcceptClicked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_request);

        myId = String.valueOf(Mualab.currentUser.id);
        mGroupReqRef =  FirebaseDatabase.getInstance().getReference().child("group_request").child(myId);
        groupRef = FirebaseDatabase.getInstance().getReference().child("group");
        mUserRef = FirebaseDatabase.getInstance().getReference().child("users");

        customDataMap = new HashMap<>();
        dataArrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        tv_no_chat = findViewById(R.id.tv_no_chat);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText("Group Request");
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        adapter = new GroupRequestAdapter(this, dataArrayList, new GroupRequestAdapter.AccpetClick() {
            @Override
            public void ActionAccept(int pos) {
                isAcceptClicked = true;
                dataArrayList.remove(pos);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void RemoveListner() {
                groupRef.removeEventListener(listenerGroup);
                mUserRef.removeEventListener(listenerUser);
            }
        });
        recyclerView.setAdapter(adapter);

        getAllData();
    }

    private void getAllData(){
         mGroupReqRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){
                    tv_no_chat.setVisibility(View.VISIBLE);
                }else   tv_no_chat.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mGroupReqRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                getData(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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

    private void getData(DataSnapshot dataSnapshot) {
        final String key  = dataSnapshot.getKey();
        try {
            final GroupRequestInfo requestInfo = dataSnapshot.getValue(GroupRequestInfo.class);
            if(requestInfo != null){

                 listenerUser =  mUserRef.child(requestInfo.senderId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final FirebaseUser user = dataSnapshot.getValue(FirebaseUser.class);
                        Log.d("dsds",user+"");

                        listenerGroup =  groupRef.child(requestInfo.groupId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Groups groups = dataSnapshot.getValue(Groups.class);

                                CustomData customData = new CustomData();
                                customData.firebaseUser = user;
                                customData.firebaseUser.lastActivity = requestInfo.timestamp;
                                customData.firebaseUser.authToken = key;
                                customData.groups = groups;

                                if(!isAcceptClicked){
                                    customDataMap.put(key,customData);
                                    Collection<CustomData> CustomData = customDataMap.values();

                                    dataArrayList.clear();
                                    dataArrayList.addAll(CustomData);
                                    shortList();
                                }



                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public class CustomData{
        public FirebaseUser firebaseUser;
        public Groups groups;
    }

    private void shortList() {
        Collections.sort(dataArrayList, new Comparator<CustomData>() {

            @Override
            public int compare(CustomData o1, CustomData o2) {
                if (o1.firebaseUser.lastActivity == null || o2.firebaseUser.lastActivity == null)
                    return -1;
                else {
                    Long long1 = Long.valueOf(String.valueOf(o1.firebaseUser.lastActivity));
                    Long long2 = Long.valueOf(String.valueOf(o2.firebaseUser.lastActivity));
                    return long1.compareTo(long2);
                }
            }


        });
        adapter.notifyDataSetChanged();
    }

}

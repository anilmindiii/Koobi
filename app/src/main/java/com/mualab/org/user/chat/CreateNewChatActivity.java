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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.adapter.AddNewMemberListAdapter;
import com.mualab.org.user.chat.brodcasting.AddMemberToBrodcastActivity;
import com.mualab.org.user.chat.model.FirebaseUser;
import com.mualab.org.user.utils.KeyboardUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateNewChatActivity extends AppCompatActivity implements View.OnClickListener {
    private DatabaseReference mFirebaseUserDbRef;
    private Map<String, FirebaseUser> map;
    private List<FirebaseUser> allMembers;
    private ProgressBar progress_bar;
    private TextView tv_no_chat;
    private AddNewMemberListAdapter membersListAdapter;
    private RecyclerView rvMembers;
    private EditText searchview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_chat);
        mFirebaseUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");

        progress_bar = findViewById(R.id.progress_bar);
        tv_no_chat = findViewById(R.id.tv_no_chat);
        rvMembers = findViewById(R.id.rvMembers);
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText("Create New Chat");


        btnBack.setOnClickListener(this::onClick);

        map = new HashMap<>();
        allMembers = new ArrayList<>();

        membersListAdapter = new AddNewMemberListAdapter(this, allMembers, true, new AddNewMemberListAdapter.getUserData() {
            @Override
            public void getUser(FirebaseUser firebaseUser) {
                Intent chat_intent = new Intent(CreateNewChatActivity.this, ChatActivity.class);
                chat_intent.putExtra("opponentChatId", String.valueOf(firebaseUser.uId));
                startActivity(chat_intent);
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rvMembers.setLayoutManager(manager);
        rvMembers.setAdapter(membersListAdapter);
        getUserListForAdd();

        searchview = findViewById(R.id.searchview);
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
    }

    private void filter(String text) {
        List<FirebaseUser> filterdNames = new ArrayList<>();

        for (FirebaseUser s : allMembers) {
            if (s.userName != null)
                if (s.userName.toLowerCase(Locale.getDefault()).contains(text)) {
                    filterdNames.add(s);
                }
        }

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
                            getDataInMap(dataSnapshot.getKey(), user);
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
                        getDataInMap(dataSnapshot.getKey(), user);
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

    private void getDataInMap(String key, FirebaseUser user) {
        if (user != null) {
            user.isChecked = false;
            if (user.userName != null)
                if (!user.userName.equals(""))
                    map.put(key, user);

            allMembers.clear();
            Collection<FirebaseUser> values = map.values();
            allMembers.addAll(values);
            membersListAdapter.notifyDataSetChanged();

            if (allMembers.size() == 0)
                tv_no_chat.setVisibility(View.VISIBLE);
            else
                tv_no_chat.setVisibility(View.GONE);
            progress_bar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnBack:
                onBackPressed();
                break;
        }
    }
}

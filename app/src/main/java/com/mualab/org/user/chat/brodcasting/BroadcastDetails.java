package com.mualab.org.user.chat.brodcasting;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mualab.org.user.R;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.adapter.GroupMembersListAdapter;
import com.mualab.org.user.chat.model.GroupMember;
import com.mualab.org.user.chat.model.Groups;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.image.picker.ImagePicker;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.constants.Constant;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class BroadcastDetails extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivGroupImage, btn_edit_pencil;
    private TextView tvGroupMembers, tvMuteGroup;
    private TextView tvGroupName ;
    private boolean isMuteClicked = false;
    private DatabaseReference mBroadcastRef, memberRef,mBroadCastChatRef,mChatHistoryRef;
    private String myUid;
    private Groups groups;
    private int isMute;
    private ArrayList<GroupMember> userList;
    private long mLastClickTime = 0;
    private ProgressBar progress_bar;
    private GroupMembersListAdapter userListAdapter;
    private Map<String, GroupMember> map;
    private ImageView btn_edit_name;
    private boolean isEdit;
    private RelativeLayout ly_delete_group;
    private String broadcastId = "";
    private String matchImageUrl = "";
    private RelativeLayout ly_clear_chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcasst_details);

        Intent intent = getIntent();
        broadcastId = intent.getStringExtra("broadcastId");
        if (Mualab.currentUser.id != 0)
            myUid = String.valueOf(Mualab.currentUser.id);

        mBroadcastRef = FirebaseDatabase.getInstance().getReference().child("broadcast").child(broadcastId);
        memberRef = FirebaseDatabase.getInstance().getReference().child("broadcast").child(broadcastId).child("member").child(myUid);

        mBroadCastChatRef = FirebaseDatabase.getInstance().getReference().child("broadcastChat");
        mChatHistoryRef = FirebaseDatabase.getInstance().getReference().child("chat_history");


        initView();
    }

    private void initView() {
        userList = new ArrayList<>();
        map = new HashMap<>();
        userListAdapter = new GroupMembersListAdapter(BroadcastDetails.this, userList);
        ivGroupImage = findViewById(R.id.ivGroupImage);
        tvGroupName = findViewById(R.id.tvGroupName);
        btn_edit_name = findViewById(R.id.btn_edit_name);
        tvMuteGroup = findViewById(R.id.tvMuteGroup);
        tvGroupMembers = findViewById(R.id.tvGroupMembers);
        RecyclerView rycGroupMembers = findViewById(R.id.rycGroupMembers);
        btn_edit_pencil = findViewById(R.id.btn_edit_pencil);
        ly_delete_group = findViewById(R.id.ly_delete_group);
        ly_clear_chat = findViewById(R.id.ly_clear_chat);

        progress_bar = findViewById(R.id.progress_bar);
        progress_bar.setVisibility(View.VISIBLE);

        ImageView btnBack = findViewById(R.id.btnBack);
        rycGroupMembers.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(BroadcastDetails.this,
                LinearLayoutManager.VERTICAL, false);
        rycGroupMembers.setLayoutManager(layoutManager);
        rycGroupMembers.setAdapter(userListAdapter);

        //   userListAdapter = new UsersListAdapter(this,userList);
        btnBack.setOnClickListener(this);
        btn_edit_name.setOnClickListener(this);
        ly_delete_group.setOnClickListener(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progress_bar.setVisibility(View.GONE);
            }
        }, 4000);

        btn_edit_pencil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ConnectionDetector.isConnected()) {
                    new NoConnectionDialog(BroadcastDetails.this, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if (isConnected) {
                                dialog.dismiss();
                            }
                        }
                    }).show();
                } else {
                    getPermissionAndPicImage();
                }

            }
        });

        ly_clear_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertClearChat();
            }
        });

        getGroupDetail();
    }

    private void showAlertClearChat() {
        final Dialog dialog = new Dialog(BroadcastDetails.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_delete_chat);
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView title = dialog.findViewById(R.id.tv_title);
        title.setText("Clear Chat");
        dialog.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();

                mBroadCastChatRef.child(broadcastId).removeValue();
                mChatHistoryRef.child(myUid).child(broadcastId).child("message").setValue("");
                mChatHistoryRef.child(myUid).child(broadcastId).child("messageType").setValue("0");

                setResult(2);

            }
        });

        Button btn_no = dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                View view = dialog.getWindow().getDecorView();
                //for enter from left
                ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), 0.0f).start();
                //for enter from bottom
                //ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0.0f).start();
            }
        });

        dialog.show();
    }

    public void getPermissionAndPicImage() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) +
                    checkSelfPermission(android.Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.CAMERA,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);

            } else {
                ImagePicker.pickImage(this);
            }

        } else {
            ImagePicker.pickImage(this);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == 234) {
                Uri imageUri = ImagePicker.getImageURIFromResult(BroadcastDetails.this, requestCode, resultCode, data);
                Bitmap bitmap = ImagePicker.getImageFromResult(BroadcastDetails.this, requestCode, resultCode, data);
                if (imageUri != null) {

                    ivGroupImage.setImageBitmap(bitmap);
                    uploadImage(imageUri);

                } else {
                    MyToast.getInstance(BroadcastDetails.this).showDasuAlert(getString(R.string.msg_some_thing_went_wrong));
                }

            }
        }
    }

    private void uploadImage(Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        if (imageUri != null) {
            Progress.show(BroadcastDetails.this);

            StorageReference storageReference = storage.getReference();

            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
            ref.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //  progressDialog.dismiss();
                                    Uri fireBaseUri = uri;
                                    matchImageUrl = uri.toString();
                                    assert fireBaseUri != null;
                                    mBroadcastRef.child("groupImg").setValue(fireBaseUri.toString());
                                    //sProfileImgUrl = fireBaseUri.toString();

                                    Progress.hide(BroadcastDetails.this);
                                    //  tv_no_chat.setVisibility(View.GONE);
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //  progressDialog.dismiss();
                            Progress.hide(BroadcastDetails.this);
                            Toast.makeText(BroadcastDetails.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY: {
                boolean cameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                boolean readExternalFile = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                if (cameraPermission && readExternalFile) {
                    ImagePicker.pickImage(BroadcastDetails.this);
                } else
                    MyToast.getInstance(BroadcastDetails.this).showDasuAlert("Your  Permission Denied");
            }
            break;
        }
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (v.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.btn_edit_name:
                if (!isEdit) {
                    tvGroupName.setEnabled(true);
                    KeyboardUtil.showKeyboard(v, this);
                    btn_edit_name.setImageResource(R.drawable.right_icon);
                    isEdit = true;
                } else {
                    String groupName = tvGroupName.getText().toString().trim();
                    if (TextUtils.isEmpty(groupName)) {
                        MyToast.getInstance(this).showDasuAlert("Please enter group name");
                        return;
                    }

                    mBroadcastRef.child("groupName").setValue(groupName);

                    tvGroupName.setEnabled(false);
                    KeyboardUtil.hideKeyboard(v, this);
                    btn_edit_name.setImageResource(R.drawable.ic_pencil);
                    isEdit = false;
                }
                break;

            case R.id.ly_delete_group:
                showAlertDeleteBrodcast();
                break;
        }
    }

    private void showAlertDeleteBrodcast() {
        final Dialog dialog = new Dialog(BroadcastDetails.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_delete_chat);
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView title = dialog.findViewById(R.id.tv_title);
        title.setText("Delete Broadcast");
        dialog.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                DatabaseReference mBroadCastRef = FirebaseDatabase.getInstance().getReference().child("broadcast");
                DatabaseReference mBroadCastChatRef = FirebaseDatabase.getInstance().getReference().child("broadcastChat");
                DatabaseReference mChatHistoryRef = FirebaseDatabase.getInstance().getReference().child("chat_history");

                mBroadCastRef.child(broadcastId).removeValue();
                mBroadCastChatRef.child(broadcastId).removeValue();
                mChatHistoryRef.child(myUid).child(broadcastId).removeValue();

                setResult(RESULT_OK);
                progress_bar.setVisibility(View.VISIBLE);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progress_bar.setVisibility(View.GONE);
                        finish();
                    }
                }, 300);

            }
        });

        Button btn_no = dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                View view = dialog.getWindow().getDecorView();
                //for enter from left
                ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), 0.0f).start();
                //for enter from bottom
                //ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0.0f).start();
            }
        });

        dialog.show();
    }

    private void getGroupDetail() {
        mBroadcastRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    try {
                        groups = dataSnapshot.getValue(Groups.class);
                        if (groups != null) {
                            tvGroupName.setText((groups.member.size()-1)+" Recipients");
                            tvGroupMembers.setText((groups.member.size()-1) + " Recipients");

                            if (groups.groupImg != null) {
                                if (!groups.groupImg.equals(matchImageUrl)) {
                                   /* Picasso.with(BroadcastDetails.this).load(groups.groupImg).
                                            placeholder(R.drawable.gallery_placeholder).into(ivGroupImage);*/
                                    matchImageUrl = "";
                                }

                            }
                            // GroupMember member = (GroupMember) groups.member.get(myUid);
                            // isMute = member.mute;
                            Map<String, GroupMember> hashMap = (Map<String, GroupMember>)
                                    groups.member.get(myUid);



                            getMemberList();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        progress_bar.setVisibility(View.GONE);
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress_bar.setVisibility(View.GONE);
            }
        });
    }

    private void getMemberList() {
        mBroadcastRef.child("member").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    if(!dataSnapshot.getKey().equals(myUid)){
                        GroupMember member = dataSnapshot.getValue(GroupMember.class);
                        getDataInMap(dataSnapshot.getKey(), member);
                    }


                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    if(!dataSnapshot.getKey().equals(myUid)) {
                        GroupMember member = dataSnapshot.getValue(GroupMember.class);
                        getDataInMap(dataSnapshot.getKey(), member);
                    }
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
            user.isChecked = false;
            map.put(key, user);
            userList.clear();
            Collection<GroupMember> values = map.values();

            userList.addAll(values);
        }

        try {
            Iterator<GroupMember> iter = userList.iterator();
            GroupMember tempMember = null;
            while (iter.hasNext()) {
                GroupMember member = iter.next();
                if (member.memberId == Mualab.currentUser.id) {
                    tempMember = member;
                    iter.remove();
                }
            }
            if (tempMember != null)
                userList.add(0, tempMember);

        }catch (Exception e){

        }

        userListAdapter.notifyDataSetChanged();
        //shortList();
        progress_bar.setVisibility(View.GONE);
    }



}

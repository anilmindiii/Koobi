package com.mualab.org.user.chat;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import com.google.firebase.database.ServerValue;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GroupDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView ivMuteToggle, ivGroupImage, btn_edit_pencil,iv_place_holder;
    private TextView tvGroupMembers, tvMuteGroup,btn_edit_name,tv_title,tv_group_description;
    private EditText tvGroupName, tvGroupDesc;
    private boolean isMuteClicked = false;
    private DatabaseReference groupRef, memberRef;
    private String myUid;
    private Groups groups;
    private int isMute;
    private ArrayList<GroupMember> userList;
    private long mLastClickTime = 0;
    private ProgressBar progress_bar;
    private GroupMembersListAdapter userListAdapter;
    private Map<String, GroupMember> map;
    private ImageView profile_image;
    private RelativeLayout ly_clear_chat, ly_leave_group, ly_delete_group;
    private String groupId = "";
    private String matchImageUrl = "";
    private boolean areYouAdmin;
    private RelativeLayout ly_mute_group,ly_edit_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
        if (Mualab.currentUser.id != 0)
            myUid = String.valueOf(Mualab.currentUser.id);

        groupRef = FirebaseDatabase.getInstance().getReference().child("group").child(groupId);
        memberRef = FirebaseDatabase.getInstance().getReference().child("group").child(groupId).child("member").child(myUid);

        initView();
    }

    private void initView() {
        userList = new ArrayList<>();
        map = new HashMap<>();
        userListAdapter = new GroupMembersListAdapter(GroupDetailActivity.this, userList);
        ivGroupImage = findViewById(R.id.ivGroupImage);
        ivMuteToggle = findViewById(R.id.ivMuteToggle);
        tvGroupName = findViewById(R.id.tvGroupName);
        tv_title = findViewById(R.id.tv_title);
        btn_edit_name = findViewById(R.id.btn_edit_name);
        tvGroupDesc = findViewById(R.id.tvGroupDesc);
        tv_group_description = findViewById(R.id.tv_group_description);
        tvMuteGroup = findViewById(R.id.tvMuteGroup);
        tvGroupMembers = findViewById(R.id.tvGroupMembers);
        RecyclerView rycGroupMembers = findViewById(R.id.rycGroupMembers);
        btn_edit_pencil = findViewById(R.id.btn_edit_pencil);
        ly_clear_chat = findViewById(R.id.ly_clear_chat);
        ly_leave_group = findViewById(R.id.ly_leave_group);
        ly_delete_group = findViewById(R.id.ly_delete_group);
        ly_mute_group = findViewById(R.id.ly_mute_group);
        iv_place_holder = findViewById(R.id.iv_place_holder);
        ly_edit_view = findViewById(R.id.ly_edit_view);
        profile_image = findViewById(R.id.profile_image);
        FrameLayout ly_update_image = findViewById(R.id.ly_update_image);
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        ImageView btnBackActivity = findViewById(R.id.btnBackActivity);
        tvHeaderTitle.setText("Edit Group");


        progress_bar = findViewById(R.id.progress_bar);
        progress_bar.setVisibility(View.VISIBLE);

        ImageView btnBack = findViewById(R.id.btnBack);
        rycGroupMembers.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(GroupDetailActivity.this,
                LinearLayoutManager.VERTICAL, false);
        rycGroupMembers.setLayoutManager(layoutManager);
        rycGroupMembers.setAdapter(userListAdapter);

        //   userListAdapter = new UsersListAdapter(this,userList);
        ivMuteToggle.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnBackActivity.setOnClickListener(this);
        btn_edit_name.setOnClickListener(this);
        ly_clear_chat.setOnClickListener(this);
        ly_leave_group.setOnClickListener(this);
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
                    new NoConnectionDialog(GroupDetailActivity.this, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if (isConnected) {
                                dialog.dismiss();
                            }
                        }
                    }).show();
                } else {
                    ly_edit_view.setVisibility(View.VISIBLE);
                }

            }
        });



        ly_update_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPermissionAndPicImage();
            }
        });

        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    ly_delete_group.setVisibility(View.VISIBLE);
                } else ly_delete_group.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        getGroupDetail();
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
                Uri imageUri = ImagePicker.getImageURIFromResult(GroupDetailActivity.this, requestCode, resultCode, data);
                Bitmap bitmap = ImagePicker.getImageFromResult(GroupDetailActivity.this, requestCode, resultCode, data);
                if (imageUri != null) {

                    ivGroupImage.setImageBitmap(bitmap);
                    profile_image.setImageBitmap(bitmap);
                    uploadImage(imageUri);
                    iv_place_holder.setVisibility(View.GONE);

                } else {
                    MyToast.getInstance(GroupDetailActivity.this).showDasuAlert(getString(R.string.msg_some_thing_went_wrong));
                }

            }
        }
    }

    private void uploadImage(Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        if (imageUri != null) {
            Progress.show(GroupDetailActivity.this);

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
                                    groupRef.child("groupImg").setValue(fireBaseUri.toString());
                                    //sProfileImgUrl = fireBaseUri.toString();

                                    Progress.hide(GroupDetailActivity.this);
                                    //  tv_no_chat.setVisibility(View.GONE);
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //  progressDialog.dismiss();
                            Progress.hide(GroupDetailActivity.this);
                            Toast.makeText(GroupDetailActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    ImagePicker.pickImage(GroupDetailActivity.this);
                } else
                    MyToast.getInstance(GroupDetailActivity.this).showDasuAlert("Your  Permission Denied");
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
            case R.id.btnBackActivity:
            case R.id.btnBack:
                if(ly_edit_view.getVisibility() == View.VISIBLE){
                    ly_edit_view.setVisibility(View.GONE);
                }else onBackPressed();
                break;
            case R.id.ivMuteToggle:
                if (isMuteClicked) {
                    isMuteClicked = false;
                    ivMuteToggle.setImageResource(R.drawable.ic_switch_off);
                    groupRef.child("member").child(myUid).child("mute").
                            setValue(0);
                } else {
                    ivMuteToggle.setImageResource(R.drawable.ic_switch_on);
                    isMuteClicked = true;
                    groupRef.child("member").child(myUid).child("mute").
                            setValue(1);
                }
                break;

            case R.id.btn_edit_name:
                ly_edit_view.setVisibility(View.GONE);
                String groupName = tvGroupName.getText().toString().trim();
                String groupDescrition = tvGroupDesc.getText().toString().trim();
                if (TextUtils.isEmpty(groupName)) {
                    MyToast.getInstance(this).showDasuAlert("Please enter group name");
                    return;
                } else if (TextUtils.isEmpty(groupDescrition)) {
                    MyToast.getInstance(this).showDasuAlert("Please enter group descrition");
                    return;
                }

                groupRef.child("groupName").setValue(groupName);
                groupRef.child("groupDescription").setValue(groupDescrition);

                KeyboardUtil.hideKeyboard(v, this);
                break;

            case R.id.ly_clear_chat:
                showAlertClearChat();
                break;

            case R.id.ly_delete_group:
                showAlertDeleteGroup();
                break;

            case R.id.ly_leave_group:
                if (areYouAdmin) {
                    leaveGroupDialog();
                } else showAlertLeaveGroup();
                break;
        }
    }

    private void leaveGroupDialog(){
        final Dialog dialog = new Dialog(GroupDetailActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_delete_chat);
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView tv_delete_alert = dialog.findViewById(R.id.tv_delete_alert);
        tv_delete_alert.setText(getString(R.string.sure_leave_group));
        TextView title = dialog.findViewById(R.id.tv_title);
        title.setText("Leave Group?");
        dialog.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                Intent intent = new Intent(GroupDetailActivity.this, MakeAdminActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
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
                //ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), 0.0f).start();

            }
        });

        dialog.show();
    }

    private void getGroupDetail() {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    try {
                        groups = dataSnapshot.getValue(Groups.class);
                        if (groups != null) {
                            tvGroupName.setText(groups.groupName);
                            tv_title.setText(groups.groupName);

                            if (groups.groupImg != null) {
                                if (!groups.groupImg.equals(matchImageUrl)) {

                                    Picasso.with(GroupDetailActivity.this)
                                            .load(groups.groupImg)
                                            .into(ivGroupImage, new com.squareup.picasso.Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    iv_place_holder.setVisibility(View.GONE);
                                                }

                                                @Override
                                                public void onError() {
                                                    iv_place_holder.setVisibility(View.VISIBLE);
                                                }
                                            });

                                    Picasso.with(GroupDetailActivity.this)
                                            .load(groups.groupImg)
                                            .placeholder(R.drawable.user_img_ico_group)
                                            .into(profile_image);
                                    matchImageUrl = "";
                                }
                            }else {
                                profile_image.setImageResource(R.drawable.user_img_ico_group);
                            }
                            // GroupMember member = (GroupMember) groups.member.get(myUid);
                            // isMute = member.mute;
                            Map<String, GroupMember> hashMap = (Map<String, GroupMember>)
                                    groups.member.get(myUid);

                            if (hashMap != null) {
                                isMute = Integer.valueOf(String.valueOf(hashMap.get("mute")));
                                if (isMute == 1) {
                                    // tvMuteGroup.setText("Unmute Group");
                                    ivMuteToggle.setImageResource(R.drawable.ic_switch_on);
                                    isMuteClicked = true;
                                   // groupRef.child("member").child(myUid).child("mute").setValue(1);
                                } else {
                                    isMuteClicked = false;
                                    //   tvMuteGroup.setText(getString(R.string.mute_group));
                                    ivMuteToggle.setImageResource(R.drawable.ic_switch_off);
                                   // groupRef.child("member").child(myUid).child("mute").setValue(0);
                                }
                            }

                            tvGroupMembers.setText(groups.member.size() + " Members");
                            tvGroupDesc.setText(groups.groupDescription);
                            tv_group_description.setText(groups.groupDescription);

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
        groupRef.child("member").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    GroupMember member = dataSnapshot.getValue(GroupMember.class);
                    getDataInMap(dataSnapshot.getKey(), member);

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    GroupMember member = dataSnapshot.getValue(GroupMember.class);
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
                btn_edit_pencil.setVisibility(View.VISIBLE);
                btn_edit_name.setVisibility(View.VISIBLE);
                areYouAdmin = true;
            }

            if (user.memberId == Mualab.currentUser.id)
                ly_mute_group.setVisibility(View.VISIBLE);

            if (ly_delete_group.getVisibility() == View.VISIBLE) {
                ly_leave_group.setVisibility(View.GONE);
            }

          /*  if (user.memberId == Mualab.currentUser.id && !user.type.equals("admin")) {
                ly_leave_group.setVisibility(View.VISIBLE);
            }*/

            user.isChecked = false;
            map.put(key, user);
            userList.clear();
            Collection<GroupMember> values = map.values();
            userList.addAll(values);
        }

        try {
            Iterator<GroupMember> iter = userList.iterator();
            GroupMember tempMember = null;
            GroupMember admin = null;
            while (iter.hasNext()) {
                GroupMember member = iter.next();

                if (member.memberId == Mualab.currentUser.id) {
                    tempMember = member;
                    iter.remove();
                }

                if (member.memberId == Mualab.currentUser.id && !member.type.equals("admin")){
                    admin = member;
                    iter.remove();
                }
            }

            if(admin != null){
                userList.add(0, admin);
            }

            if (tempMember != null)
                userList.add(userList.size(), tempMember);



        } catch (Exception e) {

        }

        userListAdapter.notifyDataSetChanged();
        //shortList();
        progress_bar.setVisibility(View.GONE);
    }

    private void shortList() {
        Collections.sort(userList, new Comparator<GroupMember>() {

            @Override
            public int compare(GroupMember a1, GroupMember a2) {

                if (a1.memberId == 0 || a2.memberId == 0)
                    return -1;
                else {
                    Long long1 = Long.valueOf(String.valueOf(a1.memberId));
                    Long long2 = Long.valueOf(String.valueOf(a2.memberId));

                    if (a1.memberId == Mualab.currentUser.id && !a1.type.equals("admin")) {
                        //   int itemPos = userList.indexOf(user);
                        userList.remove(a1);
                        userList.add(0, a1);
                        //userList.set(0,a1);
                        return a1.memberId;
                    } else
                        return long1.compareTo(long2);
                }

               /* if (a1.memberId==Mualab.currentUser.id || a2.memberId==Mualab.currentUser.id) {
                    int itemPos = userList.indexOf(user);
                    userList.remove(user);
                    userList.add(0,user);
                }*/
            }
        });
        userListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void showAlertLeaveGroup() {
        final Dialog dialog = new Dialog(GroupDetailActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_delete_chat);
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView title = dialog.findViewById(R.id.tv_title);
        title.setText("Leave Group?");
        dialog.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();

                if (groups.banAdmin == 1 && groups.freezeGroup == 1) {
                    return;
                }
                if (groups.adminId != Mualab.currentUser.id || groups.member.size() == 1) {
                    FirebaseDatabase.getInstance().getReference().child("group").child(groupId).child("member").child(String.valueOf(Mualab.currentUser.id)).setValue(null);
                    FirebaseDatabase.getInstance().getReference().child("group_msg_delete").child(String.valueOf(Mualab.currentUser.id)).child(groupId).child("exitBy").setValue(ServerValue.TIMESTAMP);
                    FirebaseDatabase.getInstance().getReference().child("group").child(groupId).child("adminRequest").child(String.valueOf(Mualab.currentUser.id)).setValue(null);
                    FirebaseDatabase.getInstance().getReference().child("myGroup").child(String.valueOf(Mualab.currentUser.id)).child(groupId).setValue(null);

                }
                setResult(RESULT_OK);
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
                //ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), 0.0f).start();
                //for enter from bottom
                //ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0.0f).start();
            }
        });

        dialog.show();
    }

    private void showAlertDeleteGroup() {
        final Dialog dialog = new Dialog(GroupDetailActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_delete_chat);
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView title = dialog.findViewById(R.id.tv_title);
        title.setText("Delete Group?");
        dialog.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();

                FirebaseDatabase.getInstance().getReference().child("chat_history")
                        .child(String.valueOf(Mualab.currentUser.id)).child(groupId).setValue(null);

                setResult(RESULT_OK);
                finish();
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
               // ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), 0.0f).start();
                //for enter from bottom
                //ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0.0f).start();
            }
        });

        dialog.show();
    }

    private void showAlertClearChat() {
        final Dialog dialog = new Dialog(GroupDetailActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_delete_chat);
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView title = dialog.findViewById(R.id.tv_title);
        title.setText("Delete Conversation?");
        dialog.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                FirebaseDatabase.getInstance().getReference().child("group_msg_delete")
                        .child(String.valueOf(Mualab.currentUser.id)).child(groupId).child("deleteBy").setValue(ServerValue.TIMESTAMP);

                FirebaseDatabase.getInstance().getReference().child("chat_history")
                        .child(String.valueOf(Mualab.currentUser.id)).child(groupId).child("message").setValue("");

                setResult(RESULT_OK);
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
            //    ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), 0.0f).start();
                //for enter from bottom
                //ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0.0f).start();
            }
        });

        dialog.show();
    }
}

package com.mualab.org.user.chat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.dialogs.BottomSheetPopup;
import com.mualab.org.user.activity.dialogs.NameDisplayDialog;
import com.mualab.org.user.activity.dialogs.model.Item;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.adapter.GroupChattingAdapter;
import com.mualab.org.user.chat.adapter.MenuAdapter;
import com.mualab.org.user.chat.brodcasting.BroadCastChatActivity;
import com.mualab.org.user.chat.listner.CustomeClick;
import com.mualab.org.user.chat.listner.DateTimeScrollListner;
import com.mualab.org.user.chat.model.ChatHistory;
import com.mualab.org.user.chat.model.GroupChat;
import com.mualab.org.user.chat.model.GroupMember;
import com.mualab.org.user.chat.model.Groups;
import com.mualab.org.user.chat.model.RemoveFromGroup;
import com.mualab.org.user.chat.model.WebNotification;
import com.mualab.org.user.chat.notification_builder.FcmNotificationBuilder;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.image.picker.ImagePicker;
import com.mualab.org.user.utils.CommonUtils;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.constants.Constant;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity implements View.OnClickListener,
        DateTimeScrollListner {

    private EditText et_for_sendTxt;
    private TextView tv_no_chat,tvCantSendMsg, tvOnlineStatus,tvUserName;
    private ProgressBar progress_bar;
    private RecyclerView recycler_view;
    private GroupChattingAdapter chattingAdapter;
    private List<GroupChat> chatList;
    private Map<String, GroupChat> map;
    private boolean isFromGallery;
    private String myUid,groupId;
    private LinearLayout llDots;
    private RelativeLayout sendlayout;
    private PopupWindow popupWindow;
    private ArrayList<String>arrayList;
    private Boolean isActivityOpen = false;
    private DatabaseReference mFirebaseDatabaseReference,groupRef,groupChatRef,batchCountRef,mUserRef,
            chatRefWebnotif,chatHistoryRef,groupMsgDeleteRef;
    private long mLastClickTime = 0, addedTime = 0;
    private LinearLayoutManager layoutManager;
    private int unreadMsgCount = 0,isMyFavourite = 0;
    private Groups groups = new Groups();
    private List<String>fbTokenListForMobile,fbTokenListForWeb;
    private LinearLayout ly_group_info;
    private Uri ImageQuickUri;
    String holdKeyForImage = "",matchImageUrl = "";
    private boolean isGIF;
    private Map<String,Integer> badgeCountMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
        myUid = String.valueOf(Mualab.currentUser.id);

        Mualab.currentGroupId = groupId;
        badgeCountMap = new HashMap<>();

        isActivityOpen = true;

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        groupRef = mFirebaseDatabaseReference.child("group").child(groupId);

        groupChatRef = mFirebaseDatabaseReference.child("groupChat").child(groupId);

        chatRefWebnotif = mFirebaseDatabaseReference.child("webnotification");

        chatHistoryRef = mFirebaseDatabaseReference.child("chat_history");

        groupMsgDeleteRef = FirebaseDatabase.getInstance().getReference().child("group_msg_delete");

        batchCountRef = FirebaseDatabase.getInstance().getReference().child("socialBookingBadgeCount");

        mUserRef = FirebaseDatabase.getInstance().getReference().child("users");


        init();

        CustomeClick.getmInctance().setListner(new CustomeClick.ExploreSearchListener() {
            @Override
            public void onTextChange(InputContentInfoCompat inputContentInfo, int flags, Bundle opts) {
                isGIF = true;
                ImageQuickUri = inputContentInfo.getLinkUri();
                if(ImageQuickUri != null){
                    sendQuickImage(ImageQuickUri, queryName(getContentResolver(),ImageQuickUri));
                }

            }

        });

    }



    private void init(){
        // otherUserId = "17";
        chatList = new ArrayList<>();
        fbTokenListForMobile = new ArrayList<>();
        fbTokenListForWeb = new ArrayList<>();
        map = new HashMap<>();
        arrayList=new ArrayList<>();
        chattingAdapter = new GroupChattingAdapter(GroupChatActivity.this,chatList,myUid,this);

        et_for_sendTxt = findViewById(R.id.et_for_sendTxt);
        tv_no_chat = findViewById(R.id.tv_no_chat);
        tvCantSendMsg = findViewById(R.id.tvCantSendMsg);
        tvOnlineStatus = findViewById(R.id.tvOnlineStatus);
        progress_bar = findViewById(R.id.progress_bar);
        ly_group_info = findViewById(R.id.ly_group_info);

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView iv_capture_image = findViewById(R.id.iv_capture_image);

        TextView tv_for_send = findViewById(R.id.tv_for_send);
        tvUserName = findViewById(R.id.tvUserName);
        recycler_view = findViewById(R.id.recycler_view);
        AppCompatImageView iv_pickImage = findViewById(R.id.iv_pickImage);
        llDots = findViewById(R.id.llDots);
        sendlayout = findViewById(R.id.rlSendlayout);

        layoutManager = new LinearLayoutManager(GroupChatActivity.this, LinearLayoutManager.VERTICAL, false);
        // layoutManager.scrollToPositionWithOffset(0, 0);
        //  layoutManager.setStackFromEnd(true);
        recycler_view.setLayoutManager(layoutManager);
        recycler_view.setAdapter(chattingAdapter);

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(GroupChatActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        getMessageList();
                        //getting group data from group table
                        new Thread(new Runnable(){
                            @Override
                            public void run(){
                                getGroupDetail();
                            }
                        }).start();

                        new Thread(new Runnable(){
                            @Override
                            public void run(){
                                getChatHistory();
                            }
                        }).start();
                    }
                }
            }).show();
        }else {
            getMessageList();
            //getting group data from group table
            new Thread(new Runnable(){
                @Override
                public void run(){
                    getGroupDetail();
                }
            }).start();

            new Thread(new Runnable(){
                @Override
                public void run(){
                    getChatHistory();
                }
            }).start();

        }

        iv_pickImage.setOnClickListener(this);
        iv_capture_image.setOnClickListener(this);
        tv_for_send.setOnClickListener(this);
        llDots.setOnClickListener(this);
        //tvClearChat.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        ly_group_info.setOnClickListener(this);
    }

    private  void getChatHistory(){

        chatHistoryRef.child(myUid).child(groupId).
                orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String key = dataSnapshot.getKey();
                    ChatHistory messageOutput = dataSnapshot.getValue(ChatHistory.class);
                    assert messageOutput != null;
                    if (isActivityOpen) {
                        chatHistoryRef.child(myUid).child(groupId).child("unreadMessage").setValue(0);
                    }
                    isMyFavourite = messageOutput.favourite;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress_bar.setVisibility(View.GONE);
            }
        });

    }

    private void getGroupDetail(){
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {

                    try {
                        groups = dataSnapshot.getValue(Groups.class);
                        if (groups != null) {
                            CircleImageView ivUserProfile = findViewById(R.id.ivUserProfile);
                            tvUserName.setText(groups.groupName);
                            if (groups.groupImg !=null) {
                                Picasso.with(GroupChatActivity.this).load(groups.groupImg).placeholder(R.drawable.group_defoult_icon).
                                        into(ivUserProfile);
                            }

                            tvOnlineStatus.setText(groups.member.size()+" members");

                            Map<String,GroupMember> hashMap = (Map<String, GroupMember>)
                                    groups.member.get(myUid);

                            if (hashMap==null) {
                                sendlayout.setVisibility(View.GONE);
                                llDots.setVisibility(View.GONE);
                                tvCantSendMsg.setVisibility(View.VISIBLE);
                            }else {
                                addedTime = Long.parseLong(String.valueOf(hashMap.get("createdDate")));
                                sendlayout.setVisibility(View.VISIBLE);
                                llDots.setVisibility(View.VISIBLE);
                                tvCantSendMsg.setVisibility(View.GONE);
                            }

                            for (Map.Entry<String,Object> entry : groups.member.entrySet()) {
                                String groupMemberVal = entry.getKey();
                                getBadgeCount(groupMemberVal);
                            }

                        }
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

    private void getMessageList(){
        groupChatRef.orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ArrayList<Groups> groupsList = new ArrayList<>();
                try {
                    Map<String, Groups> objectMap = (HashMap<String, Groups>) dataSnapshot.getValue();

                    GroupChat messageOutput = new GroupChat();

                    messageOutput.readStatus = Integer.parseInt(String.valueOf(objectMap.get("readStatus")));
                    messageOutput.message = String.valueOf(objectMap.get("message"));
                    messageOutput.messageType = Integer.parseInt(String.valueOf(objectMap.get("messageType")));
                    messageOutput.reciverId = String.valueOf(objectMap.get("reciverId"));
                    messageOutput.senderId = String.valueOf(objectMap.get("senderId"));
                    messageOutput.memberCount = Integer.parseInt(String.valueOf(objectMap.get("memberCount")));
                    messageOutput.userName = String.valueOf(objectMap.get("userName"));
                    messageOutput.timestamp = (objectMap.get("timestamp"));
                    messageOutput.banner_date = String.valueOf(objectMap.get("banner_date"));

                  //  messageOutput.readMember.put(String.valueOf(messageOutput.timestamp), groupsList.add(objectMap.get("readMember")));

                    //GroupChat messageOutput = dataSnapshot.getValue(GroupChat.class);
                    // map.put(dataSnapshot.getKey(),messageOutput);
                    getChatDataInmap(dataSnapshot.getKey(),messageOutput);

                    if (chatList.size()==0) {
                        progress_bar.setVisibility(View.GONE);
                        //  tv_no_chat.setVisibility(View.VISIBLE);
                    }else {
                        progress_bar.setVisibility(View.GONE);
                        //   tv_no_chat.setVisibility(View.GONE);
                    }
                    recycler_view.scrollToPosition(chatList.size() - 1);
                    // chattingAdapter.notifyDataSetChanged();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try {
                    //  recycler_view.scrollToPosition(chatList.size() - 1);
                    GroupChat messageOutput = dataSnapshot.getValue(GroupChat.class);
                    getChatDataInmap(dataSnapshot.getKey(),messageOutput);
                    // map.put(dataSnapshot.getKey(),messageOutput);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                try {
                    //  recycler_view.scrollToPosition(chatList.size() - 1);
                    GroupChat messageOutput = dataSnapshot.getValue(GroupChat.class);
                    getChatDataInmapRemove(dataSnapshot.getKey(),messageOutput);
                    // map.put(dataSnapshot.getKey(),messageOutput);
                }catch (Exception e){
                    e.printStackTrace();
                }
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

    private void getChatDataInmapRemove(final String key, final GroupChat chat) {
        if (chat != null) {
            final long currentTime = (long) chat.timestamp;

            if(chat.message.equals(matchImageUrl)){
                chat.message = ImageQuickUri.toString();
                matchImageUrl = "";
            }
            groupMsgDeleteRef.child(myUid).child(groupId).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getValue()!=null){
                                //sendlayout.setVisibility(View.GONE);
                                long exitByTime = 0000;
                                RemoveFromGroup fromGroup = dataSnapshot.getValue(RemoveFromGroup.class);
                                Map<String, Object> objectMap = (HashMap<String, Object>)
                                        dataSnapshot.getValue();
                                Object o = objectMap.get("exitBy");


                                if(fromGroup.deleteBy != null){
                                    assert fromGroup != null;
                                    exitByTime = (long) fromGroup.deleteBy;
                                    if (currentTime>exitByTime){

                                        if(o != null){
                                            if (currentTime<(long) o){
                                                getRemovedNExitChat();
                                            }
                                        }else {

                                            getRemovedNExitChat();
                                        }
                                    }
                                }else {
                                    exitByTime = (long) o;
                                    if (currentTime<exitByTime){
                                        getRemovedNExitChat();
                                    }
                                }

                            }else {
                                if (currentTime>addedTime){
                                    getRemovedNExitChat();
                                }
                            }
                        }

                        private void getRemovedNExitChat() {
                            chat.ref_key = key;
                            map.remove(key);
                            chatList.clear();
                            Collection<GroupChat> values = map.values();
                            chat.banner_date = CommonUtils.getDateBanner((Long) chat.timestamp);
                            chatList.addAll(values);
                            chattingAdapter.notifyDataSetChanged();
                            recycler_view.scrollToPosition(map.size() - 1);
                            shortList();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

        }
        //  shortList();
    }

    private void getChatDataInmap(final String key, final GroupChat chat) {
        if (chat != null) {
            final long currentTime = (long) chat.timestamp;

            if(chat.message.equals(matchImageUrl)){
                chat.message = ImageQuickUri.toString();
                matchImageUrl = "";
            }
            groupMsgDeleteRef.child(myUid).child(groupId).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getValue()!=null){
                                //sendlayout.setVisibility(View.GONE);
                                long exitByTime = 0000;
                                RemoveFromGroup fromGroup = dataSnapshot.getValue(RemoveFromGroup.class);
                                Map<String, Object> objectMap = (HashMap<String, Object>)
                                        dataSnapshot.getValue();
                                Object o = objectMap.get("exitBy");


                                if(fromGroup.deleteBy != null){
                                    assert fromGroup != null;
                                    exitByTime = (long) fromGroup.deleteBy;
                                    if (currentTime>exitByTime){

                                        if(o != null){
                                            if (currentTime<(long) o){
                                                getRemovedNExitChat();
                                            }
                                        }else {

                                            getRemovedNExitChat();
                                        }
                                        }
                                }else {
                                    exitByTime = (long) o;
                                    if (currentTime<exitByTime){
                                        getRemovedNExitChat();
                                    }
                                }

                            }else {
                                if (currentTime>addedTime){
                                    getRemovedNExitChat();
                                }
                            }
                        }

                        private void getRemovedNExitChat() {
                            chat.ref_key = key;
                            map.put(key, chat);
                            chatList.clear();
                            Collection<GroupChat> values = map.values();
                            chat.banner_date = CommonUtils.getDateBanner((Long) chat.timestamp);
                            chatList.addAll(values);
                            chattingAdapter.notifyDataSetChanged();
                            recycler_view.scrollToPosition(map.size() - 1);
                            shortList();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

        }
        //  shortList();
    }

    private void shortList() {
        Collections.sort(chatList, new Comparator<GroupChat>() {

            @Override
            public int compare(GroupChat a1, GroupChat a2) {

                if (a1.timestamp == null || a2.timestamp == null)
                    return -1;
                else {
                    Long long1 = Long.valueOf(String.valueOf(a1.timestamp));
                    Long long2 = Long.valueOf(String.valueOf(a2.timestamp));
                    return long1.compareTo(long2);
                }
            }
        });
        chattingAdapter.notifyDataSetChanged();
    }

    private void getBadgeCount(String keyId){
        batchCountRef.child(keyId).child("totalCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    try {
                        int totalCount = dataSnapshot.getValue(Integer.class);
                        badgeCountMap.put(keyId,totalCount);

                    }catch (Exception e){

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (v.getId()){
            case R.id.llDots:
                KeyboardUtil.hideKeyboard(et_for_sendTxt,GroupChatActivity.this);
               /* int[] location = new int[2];
                llDots.getLocationOnScreen(location);

                //Initialize the Point with x, and y positions
                Display display = getWindowManager().getDefaultDisplay();
                Point p = new Point();
                display.getSize(p);
                p.x = location[0];
                p.y = location[1];*/
                arrayList.clear();

                if (groups.member!=null){
                    Map<String,GroupMember> hashMap = (Map<String, GroupMember>) groups.member.get(myUid);
                    if (hashMap!=null) {
                        String type = String.valueOf(hashMap.get("type"));
                        if (type.equals("admin"))
                            popupForAdmin();
                        else{
                            popupForUser();
                        }

                    }
                }

                break;

            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.ly_group_info:
                Intent intent = new Intent(GroupChatActivity.this, GroupDetailActivity.class);
                intent.putExtra("groupId",groupId);
                startActivityForResult(intent,Constant.REQUEST_CODE_CHOOSE);
                break;

            case R.id.iv_capture_image:
                if (!ConnectionDetector.isConnected()) {
                    new NoConnectionDialog(GroupChatActivity.this, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if(isConnected){
                                dialog.dismiss();
                            }
                        }
                    }).show();
                }else {
                    isFromGallery = false;

                    KeyboardUtil.hideKeyboard(et_for_sendTxt,GroupChatActivity.this);

                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkSelfPermission(android.Manifest.permission.CAMERA) !=
                                PackageManager.PERMISSION_GRANTED){
                            requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                    Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);
                        }
                        else {
                            takePhotoFromCamera();
                        }
                    }else {
                        takePhotoFromCamera();
                    }
                }
                break;

            case R.id.iv_pickImage:
                if (!ConnectionDetector.isConnected()) {
                    new NoConnectionDialog(GroupChatActivity.this, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if(isConnected){
                                dialog.dismiss();
                            }
                        }
                    }).show();
                }else {

                    KeyboardUtil.hideKeyboard(et_for_sendTxt,GroupChatActivity.this);
                    isFromGallery = true;

                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED){
                            requestPermissions(new String[]{
                                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                    Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);
                        }else {
                            choosePhotoFromGallary();
                        }
                    }else {
                        choosePhotoFromGallary();
                    }
                }

                break;

            case R.id.tv_for_send:
                if (!ConnectionDetector.isConnected()) {
                    new NoConnectionDialog(GroupChatActivity.this, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if(isConnected){
                                dialog.dismiss();
                            }
                        }
                    }).show();
                }else{
                    String txt = et_for_sendTxt.getText().toString().trim();
                    if (!txt.equals("")) {
                        if (groups!=null){
                            ArrayList<String>arrayList = new ArrayList<>();
                            arrayList.add(myUid);
                            GroupChat chatModel1 = new GroupChat();
                            chatModel1.message = txt;
                            chatModel1.memberCount = groups.member.size();
                            chatModel1.messageType = 0;
                            chatModel1.readMember.put(myUid,myUid);
                            chatModel1.readStatus = 0;
                            chatModel1.reciverId = groupId;
                            chatModel1.senderId = myUid;
                            chatModel1.timestamp = ServerValue.TIMESTAMP;
                            chatModel1.userName = Mualab.currentUser.userName ;

                            writeToDBProfiles(chatModel1,"");
                        }
                    }

                }
                break;
        }
    }

    private void writeToDBProfiles(final GroupChat chatModel1,String fileType) {
        fbTokenListForWeb.clear();
        fbTokenListForMobile.clear();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(GroupChatActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                    }
                }
            }).show();
        }else {

            if (ImageQuickUri != null){
                holdKeyForImage = groupChatRef.push().getKey();
                groupChatRef.child(holdKeyForImage).setValue(chatModel1);
            }else {
                groupChatRef.child(groupChatRef.push().getKey()).setValue(chatModel1);
            }



            if (ImageQuickUri != null){
                if(isGIF){
                    ImageQuickUri = null;
                    isGIF = false;
                }else{
                    Progress.show(GroupChatActivity.this);
                    uploadImage(ImageQuickUri,fileType);
                }
            }

            for (Map.Entry<String,Object> entry : groups.member.entrySet()) {

                String groupMemberVal = entry.getKey();

                //if (!groupMemberVal.equals(myUid))
                updateOtherChatHistory(groupMemberVal,chatModel1);

                Map<String,GroupMember> hashMap = (Map<String, GroupMember>) groups.member.
                        get(groupMemberVal);

                if (hashMap!=null) {
                    String mute = String.valueOf(hashMap.get("mute"));
                    String fbToken = String.valueOf(hashMap.get("firebaseToken"));

                    if (!mute.equals("1")) {
                        if (fbToken.equals("") || fbToken.isEmpty())
                            fbTokenListForWeb.add(groupMemberVal);
                        else {
                            if (!groupMemberVal.equals(myUid)){
                                fbTokenListForMobile.add(fbToken);


                                mUserRef.child(groupMemberVal).child("firebaseToken").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                       try {
                                           if(dataSnapshot.getValue() != null){

                                               String fbToken = dataSnapshot.getValue(String.class);
                                               if(badgeCountMap.containsKey(groupMemberVal)){
                                                   int count = badgeCountMap.get(groupMemberVal);
                                                   if (chatModel1.messageType == 0)
                                                       sendNotifications(fbToken, String.valueOf(count+1),chatModel1.message);
                                                   else
                                                       sendNotifications(fbToken, String.valueOf(count+1),"Image");
                                                   batchCountRef.child(groupMemberVal).child("totalCount").setValue(count+1);
                                               }else {
                                                   batchCountRef.child(groupMemberVal).child("totalCount").setValue(1);
                                               }
                                           }
                                       }catch (Exception e){

                                       }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });






                            }


                        }
                    }
                }
            }

            et_for_sendTxt.setText("");
           /* if (chatModel1.messageType == 0)
                sendPushNotificationToReceiver(chatModel1.message);
            else
                sendPushNotificationToReceiver("Image");*/
        }

    }



    private void popupForAdmin() {
        arrayList.add("Group Details");
        if (isMyFavourite==0)
            arrayList.add("Add To Favourite");
        else arrayList.add("Unfavourite");

        arrayList.add("Add New Member");
        arrayList.add("Remove Member");

        NameDisplayDialog.newInstance("Select Option", arrayList, pos -> {
            String data = arrayList.get(pos);
            switch (data){
                case "Group Details":
                    Intent intent = new Intent(GroupChatActivity.this, GroupDetailActivity.class);
                    intent.putExtra("groupId",groupId);
                    startActivityForResult(intent,Constant.REQUEST_CODE_CHOOSE);
                    break;

                case "Add New Member":
                    Intent intent2 = new Intent(GroupChatActivity.this, AddMemberActivity.class);
                    intent2.putExtra("groupId",groupId);
                    intent2.putExtra("action","add");
                    startActivity(intent2);
                    break;
                case "Remove Member":
                    Intent intent3 = new Intent(GroupChatActivity.this, RemoveMemberActivity.class);
                    intent3.putExtra("groupId",groupId);
                    intent3.putExtra("action","remove");
                    startActivity(intent3);
                    break;

                case "Add To Favourite":
                case "Unfavourite":
                    if (isMyFavourite==0){
                        chatHistoryRef.child(myUid).child(groupId).child("favourite").setValue(1);
                        isMyFavourite = 1;
                    }else {
                        chatHistoryRef.child(myUid).child(groupId).child("favourite")
                                .setValue(0);
                        isMyFavourite = 0;
                    }
                    break;
            }


        }).show(getSupportFragmentManager());

    }

    private void popupForAdminA(Point p) {
        try {
            LayoutInflater inflater = (LayoutInflater) GroupChatActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View layout = inflater.inflate(R.layout.layout_popup_menu,(ViewGroup) findViewById(R.id.parent));
            popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);

            String reqString = Build.MANUFACTURER
                    + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                    + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();

            int OFFSET_X;
            int OFFSET_Y;

            if (reqString.equals("motorola Moto G (4) 7.0 M")) {
                OFFSET_X = 200;
                OFFSET_Y = 96;
            } else {
                OFFSET_X = 200;
                OFFSET_Y = 58;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.setElevation(5);
            }

            arrayList.add("Group Details");
            if (isMyFavourite==0)
                arrayList.add("Add To Favourite");
            else arrayList.add("Unfavourite");

            arrayList.add("Add New Member");
            arrayList.add("Remove Member");


            popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
            RecyclerView recycler_view = layout.findViewById(R.id.recycler_view);

            MenuAdapter menuAdapter=new MenuAdapter(GroupChatActivity.this, arrayList, new MenuAdapter.Listener() {
                @Override
                public void onMenuClick( int pos) {
                    final String data=arrayList.get(pos);
                    popupWindow.dismiss();

                    if (!ConnectionDetector.isConnected()) {
                        new NoConnectionDialog(GroupChatActivity.this, new NoConnectionDialog.Listner() {
                            @Override
                            public void onNetworkChange(Dialog dialog, boolean isConnected) {
                                if(isConnected){
                                    dialog.dismiss();
                                }
                            }
                        }).show();
                    }
                    switch (data){
                        case "Group Details":
                            popupWindow.dismiss();
                            Intent intent = new Intent(GroupChatActivity.this, GroupDetailActivity.class);
                            intent.putExtra("groupId",groupId);
                            startActivityForResult(intent,Constant.REQUEST_CODE_CHOOSE);
                            break;

                        case "Add New Member":
                            popupWindow.dismiss();
                            Intent intent2 = new Intent(GroupChatActivity.this, AddMemberActivity.class);
                            intent2.putExtra("groupId",groupId);
                            intent2.putExtra("action","add");
                            startActivity(intent2);
                            break;
                        case "Remove Member":
                            popupWindow.dismiss();
                            Intent intent3 = new Intent(GroupChatActivity.this, RemoveMemberActivity.class);
                            intent3.putExtra("groupId",groupId);
                            intent3.putExtra("action","remove");
                            startActivity(intent3);
                            break;

                        case "Add To Favourite":
                        case "Unfavourite":
                            if (isMyFavourite==0){
                                chatHistoryRef.child(myUid).child(groupId).child("favourite").setValue(1);
                                isMyFavourite = 1;
                            }else {
                                chatHistoryRef.child(myUid).child(groupId).child("favourite")
                                        .setValue(0);
                                isMyFavourite = 0;
                            }
                            popupWindow.dismiss();
                            break;
                    }
                }
            });

            LinearLayoutManager layoutManager = new LinearLayoutManager(GroupChatActivity.this);
            recycler_view.setLayoutManager(layoutManager);
            recycler_view.setAdapter(menuAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void popupForUser() {
        arrayList.add("Group Details");

        if (isMyFavourite==0)
            arrayList.add("Add To Favourite");
        else arrayList.add("Unfavourite");

        arrayList.add("Report Group");

        NameDisplayDialog.newInstance("Select Option", arrayList, pos -> {
            String data = arrayList.get(pos);
            switch (data){
                case "Group Details":
                    Intent intent = new Intent(GroupChatActivity.this, GroupDetailActivity.class);
                    intent.putExtra("groupId",groupId);
                    startActivityForResult(intent,Constant.REQUEST_CODE_CHOOSE);
                    break;

                case "Add To Favourite":
                case "Unfavourite":

                    if (isMyFavourite==0){
                        chatHistoryRef.child(myUid).child(groupId).child("favourite").setValue(1);
                        isMyFavourite = 1;
                    }else {
                        chatHistoryRef.child(myUid).child(groupId).child("favourite")
                                .setValue(0);
                        isMyFavourite = 0;
                    }
                    break;

                case "Report Group":
                    intent = new Intent(GroupChatActivity.this, ReportChatActivity.class);
                    intent.putExtra("groupId",groupId);
                    intent.putExtra("groups",groups);
                    startActivity(intent);
                    break;
            }


        }).show(getSupportFragmentManager());
    }

    private void popupForUserA(Point p) {

        try {
            LayoutInflater inflater = (LayoutInflater) GroupChatActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View layout = inflater.inflate(R.layout.layout_popup_menu,(ViewGroup) findViewById(R.id.parent));
            popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);

            String reqString = Build.MANUFACTURER
                    + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                    + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();

            int OFFSET_X = 450;
            int OFFSET_Y;

            if (reqString.equals("motorola Moto G (4) 7.0 M")){
                OFFSET_Y = 110;
            }else {
                OFFSET_Y = 70;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.setElevation(5);
            }

            arrayList.add("Group Details");

            if (isMyFavourite==0)
                arrayList.add("Add To Favourite");
            else arrayList.add("Unfavourite");

            arrayList.add("Report Group");

            popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
            RecyclerView recycler_view = layout.findViewById(R.id.recycler_view);

            MenuAdapter menuAdapter=new MenuAdapter(GroupChatActivity.this, arrayList, new MenuAdapter.Listener() {
                @Override
                public void onMenuClick( int pos) {
                    final String data=arrayList.get(pos);
                    popupWindow.dismiss();

                    if (!ConnectionDetector.isConnected()) {
                        new NoConnectionDialog(GroupChatActivity.this, new NoConnectionDialog.Listner() {
                            @Override
                            public void onNetworkChange(Dialog dialog, boolean isConnected) {
                                if(isConnected){
                                    dialog.dismiss();
                                }
                            }
                        }).show();
                    }
                    switch (data){
                        case "Group Details":
                            popupWindow.dismiss();
                            Intent intent = new Intent(GroupChatActivity.this, GroupDetailActivity.class);
                            intent.putExtra("groupId",groupId);
                            startActivityForResult(intent,Constant.REQUEST_CODE_CHOOSE);
                            break;

                        case "Add To Favourite":
                        case "Unfavourite":

                            if (isMyFavourite==0){
                                chatHistoryRef.child(myUid).child(groupId).child("favourite").setValue(1);
                                isMyFavourite = 1;
                            }else {
                                chatHistoryRef.child(myUid).child(groupId).child("favourite")
                                        .setValue(0);
                                isMyFavourite = 0;
                            }
                            popupWindow.dismiss();
                            break;

                        case "Report Group":
                            intent = new Intent(GroupChatActivity.this, ReportChatActivity.class);
                            intent.putExtra("groupId",groupId);
                            intent.putExtra("groups",groups);
                            startActivity(intent);
                            break;
                    }
                }
            });

            LinearLayoutManager layoutManager = new LinearLayoutManager(GroupChatActivity.this);
            recycler_view.setLayoutManager(layoutManager);
            recycler_view.setAdapter(menuAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void updateOtherChatHistory(final String groupMemberVal, final GroupChat chatModel){

        chatHistoryRef.child(groupMemberVal).child(groupId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){
                            String key = dataSnapshot.getKey();
                            ChatHistory messageOutput = dataSnapshot.getValue(ChatHistory.class);
                            assert messageOutput != null;
                            unreadMsgCount = messageOutput.unreadMessage+1;

                            if (groups.adminId ==Integer.parseInt(myUid))
                                messageOutput.memberType = "admin";
                            else
                                messageOutput.memberType = "member";

                            messageOutput.timestamp = chatModel.timestamp;
                            messageOutput.message = chatModel.message;
                            messageOutput.senderId = chatModel.senderId;
                            messageOutput.messageType = chatModel.messageType;
                            messageOutput.userName = groups.groupName;
                            messageOutput.type = "group";

                            if (groupMemberVal.equals(myUid))
                                messageOutput.unreadMessage = 0;
                            else
                                messageOutput.unreadMessage = unreadMsgCount;

                            chatHistoryRef.child(String.valueOf(groupMemberVal)).
                                    child(groupId).setValue(messageOutput);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progress_bar.setVisibility(View.GONE);
                        tv_no_chat.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void sendPushNotificationToReceiver(String message) {

        FcmNotificationBuilder.initialize()
                .title(Mualab.currentUser.userName+" @ "+groups.groupName)
                .message(message).uid(groupId)
                .username(Mualab.currentUser.userName+" @ "+groups.groupName).adminId(String.valueOf(groups.adminId))
                .type("groupChat").clickAction("GroupChatActivity")
                .registrationId(fbTokenListForMobile).send();



    }

    private void sendNotifications(String fbToken, String count,String message) {
        FcmNotificationBuilder.initialize()
                .title(Mualab.currentUser.userName+" @ "+groups.groupName)
                .batchCount(count)
                .message(message).uid(groupId)
                .username(Mualab.currentUser.userName+" @ "+groups.groupName).adminId(String.valueOf(groups.adminId))
                .type("groupChat").clickAction("GroupChatActivity")
                .firebaseToken(FirebaseInstanceId.getInstance().getToken())
                .receiverFirebaseToken(fbToken).send();
    }

    private void showAlertDeleteChat(){
        final Dialog dialog = new Dialog(GroupChatActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_delete_chat);
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                progress_bar.setVisibility(View.VISIBLE);
                KeyboardUtil.hideKeyboard(et_for_sendTxt,GroupChatActivity.this);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        popupWindow.dismiss();
                        //myGroupChatRef.removeValue();
                        //mapKey.put(dataSnapshot.getKey(),dataSnapshot.getKey());
                        mFirebaseDatabaseReference.child("chat_history").child(myUid).child(groupId).
                                removeValue();
                        map.clear();
                        chatList.clear();
                        chattingAdapter.notifyDataSetChanged();
                        progress_bar.setVisibility(View.GONE);
                        //    tv_no_chat.setVisibility(View.VISIBLE);
                    }
                },400);
            }
        });

        Button btn_no=dialog.findViewById(R.id.btn_no);
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
              //  ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), 0.0f).start();
                //for enter from bottom
                //ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0.0f).start();
            }
        });

        dialog.show();
    }

    private void showAlert(String msg){
        final Dialog dialog = new Dialog(GroupChatActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.new_alert_dialog);

        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView tv_alert_msg = dialog.findViewById(R.id.tv_alert_msg);
        tv_alert_msg.setText(msg);

        dialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
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
             //   ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), 0.0f).start();
                //for enter from bottom
                //ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0.0f).start();
            }
        });

        dialog.show();
    }

    private void uploadImage(Uri imageUri, String fileType) {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        if (imageUri != null) {
            StorageReference storageReference = storage.getReference();
            if(fileType.equals("")){
                fileType = ".jpg";
            }else if(fileType.contains("gif")){
                fileType = ".gif";
            }else {
                fileType = ".jpg";
            }

            StorageReference ref = storageReference.child("images/" + ServerValue.TIMESTAMP + fileType);
            ref.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Progress.hide(GroupChatActivity.this);
                                    matchImageUrl = uri.toString().trim();
                                    groupChatRef.child(holdKeyForImage).child("message").setValue(uri.toString());
                                    ImageQuickUri = null;
                                    //  tv_no_chat.setVisibility(View.GONE);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Progress.hide(GroupChatActivity.this);
                            Log.e("TAG", "onFailure: " + e.getMessage());
                            Toast.makeText(GroupChatActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            //   progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }

    public void choosePhotoFromGallary() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, Constant.CAMERA_REQUEST);
    }

    private void takePhotoFromCamera() {
        ImagePicker.pickImageFromCamera(this);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isFromGallery)
                        choosePhotoFromGallary();
                    else
                        takePhotoFromCamera();
                } else
                    MyToast.getInstance(GroupChatActivity.this).showDasuAlert("Your  Permission Denied");

            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri;
        if (resultCode == RESULT_OK) {

            if(requestCode == Constant.REQUEST_CODE_CHOOSE){
                onBackPressed();
            }

            if (requestCode == Constant.CAMERA_REQUEST) {
                //Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                Uri imageUri = ImagePicker.getImageURIFromResult(this, requestCode, resultCode, data);


                if (imageUri != null) {
                    uri = imageUri;

                    if (imageUri.toString().startsWith("file:///storage/emulated")) {
                        Bitmap bitmap = ImagePicker.getImageFromResult(GroupChatActivity.this, requestCode, resultCode, data);
                        imageUri = getImageUri(GroupChatActivity.this, bitmap);
                    }
                    ImageQuickUri = imageUri;
                    sendQuickImage(imageUri, queryName(getContentResolver(),imageUri));


                  //  CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE).setAspectRatio(400, 400).start(this);
                } else {
                    MyToast.getInstance(GroupChatActivity.this).showDasuAlert(getString(R.string.msg_some_thing_went_wrong));
                }


            }
        }
    }

    private String queryName(ContentResolver resolver, Uri uri) {
        if(uri.toString().contains("http")){
            if(uri.toString().contains("gif")){
                return ".gif";
            }else  return ".jpg";
        }


        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    private void sendQuickImage(Uri imageUri,String fileType) {
        if (groups!=null){
            GroupChat chatModel1 = new GroupChat();
            chatModel1.message = imageUri.toString();
            chatModel1.memberCount = groups.member.size();
            chatModel1.messageType = 1;
            chatModel1.readMember.put(myUid,myUid);
            chatModel1.readStatus = 0;
            chatModel1.reciverId = groupId;
            chatModel1.senderId = myUid;
            chatModel1.timestamp = ServerValue.TIMESTAMP;
            chatModel1.userName = Mualab.currentUser.userName ;

            writeToDBProfiles(chatModel1,fileType);
        }
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        //inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityOpen = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isActivityOpen = false;
        KeyboardUtil.hideKeyboard(et_for_sendTxt,GroupChatActivity.this);
        Glide.get(this).clearMemory();
    }

    @Override
    public void onBackPressed() {
        KeyboardUtil.hideKeyboard(et_for_sendTxt,GroupChatActivity.this);
        isActivityOpen = false;

        chatHistoryRef.child(myUid).child(groupId).
                orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String key = dataSnapshot.getKey();
                    ChatHistory messageOutput = dataSnapshot.getValue(ChatHistory.class);
                    assert messageOutput != null;
                    //  if (isActivityOpen) {
                    chatHistoryRef.child(myUid).child(groupId).child("unreadMessage").setValue(0);
                    //    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress_bar.setVisibility(View.GONE);
            }
        });

        super.onBackPressed();
        finish();

        startActivity(new Intent(GroupChatActivity.this,ChatHistoryActivity.class));
    }

    @Override
    public void onScrollChange(final int position, final Object timestamp) {

        recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();

            }
        });

    }

    @Override
    public void onLongPress(int position,String refKey) {
        ArrayList<Item> items;
        Item item  = new Item();
        item.id = "1";
        item.name = "Delete";
        item.icon = R.drawable.ic_delete;

        items = new ArrayList<>();
        items.add(item);

        chatList.get(position).isLongSelected = true;
        chattingAdapter.notifyDataSetChanged();

        BottomSheetPopup.newInstance("", items, new BottomSheetPopup.ItemClick() {
            @Override
            public void onClickItem(int pos) {
                askDelete("Are you sure want to remove this message?",refKey,position);

            }

            @Override
            public void onDialogDismiss() {
                chatList.get(position).isLongSelected = false;
                chattingAdapter.notifyDataSetChanged();
            }
        }).show(getSupportFragmentManager());
    }

    @Override
    public void onPress(int position) {

    }

    public void askDelete(String msg, String refKey, int cell_pos) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton("Yes", (dialog, which) -> {
            dialog.cancel();
            groupChatRef.child(refKey).removeValue();

            if(chatList.size() == 1){

                for (Map.Entry<String,Object> entry : groups.member.entrySet()) {
                    String groupMemberVal = entry.getKey();
                    chatHistoryRef.child(String.valueOf(groupMemberVal)).
                            child(groupId).child("message").setValue("");
                }

            } else if(cell_pos == (chatList.size()-1)){
                String holdLastMessage =  chatList.get(chatList.size()-2).message;
                for (Map.Entry<String,Object> entry : groups.member.entrySet()) {
                    String groupMemberVal = entry.getKey();
                    chatHistoryRef.child(String.valueOf(groupMemberVal)).
                            child(groupId).child("message").setValue(holdLastMessage);
                }
            }
        });

        alertDialog.setNegativeButton("No", (dialog, which) -> {
            dialog.cancel();
        });
        alertDialog.show();
    }
}

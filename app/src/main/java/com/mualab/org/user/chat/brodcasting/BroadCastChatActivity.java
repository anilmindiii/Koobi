package com.mualab.org.user.chat.brodcasting;

import android.Manifest;
import android.animation.ObjectAnimator;
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
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
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
import com.mualab.org.user.chat.ChatActivity;
import com.mualab.org.user.chat.adapter.ChattingAdapter;
import com.mualab.org.user.chat.adapter.MenuAdapter;
import com.mualab.org.user.chat.listner.CustomeClick;
import com.mualab.org.user.chat.listner.DateTimeScrollListner;
import com.mualab.org.user.chat.model.BlockUser;
import com.mualab.org.user.chat.model.Chat;
import com.mualab.org.user.chat.model.ChatHistory;
import com.mualab.org.user.chat.model.FirebaseUser;
import com.mualab.org.user.chat.model.GroupMember;
import com.mualab.org.user.chat.model.Groups;
import com.mualab.org.user.chat.model.MuteUser;
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
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class BroadCastChatActivity extends AppCompatActivity implements
        DateTimeScrollListner, View.OnClickListener {
    private String myUid, broadcastId;
    private DatabaseReference mBroadCastChatRef, mChatHistoryRef, mBroadCastRef, mChatRef, chatRefMuteUser,batchCountRef;
    private ProgressBar progress_bar;
    private Map<String, Chat> map;
    private ChattingAdapter chattingAdapter;
    private List<Chat> chatList;
    private Uri ImageQuickUri;
    String holdKeyForImage = "", matchImageUrl = "";
    private RecyclerView recycler_view;
    private long mLastClickTime = 0;
    private EditText et_for_sendTxt;
    private Groups groups;
    private TextView tvUserName, tvOnlineStatus;
    private PopupWindow popupWindow;
    private ArrayList<String> arrayList;
    private LinearLayout llDots;
    private boolean isFromGallery;
    private LinearLayout ly_group_info;
    private int unreadMsgCount = 0, isMyFavourite = 0;
    private List<String> fbTokenListForMobile, fbTokenListForWeb;
    private Map<Integer, String> memberToken;
    private String allMemberName = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broad_cast_chat);

        Intent intent = getIntent();
        broadcastId = intent.getStringExtra("broadcastId");
        myUid = String.valueOf(Mualab.currentUser.id);
        progress_bar = findViewById(R.id.progress_bar);
        tvUserName = findViewById(R.id.tvUserName);
        tvOnlineStatus = findViewById(R.id.tvOnlineStatus);
        ly_group_info = findViewById(R.id.ly_group_info);


        mBroadCastRef = FirebaseDatabase.getInstance().getReference().child("broadcast");
        mBroadCastChatRef = FirebaseDatabase.getInstance().getReference().child("broadcastChat");
        mChatHistoryRef = FirebaseDatabase.getInstance().getReference().child("chat_history");
        mChatRef = FirebaseDatabase.getInstance().getReference().child("chat");
        chatRefMuteUser = FirebaseDatabase.getInstance().getReference().child("mute_user");
        batchCountRef = FirebaseDatabase.getInstance().getReference().child("socialBookingBadgeCount");

        init();
        getBroadCastDetails();
        getBroadcastChatList();

        CustomeClick.getmInctance().setListner(new CustomeClick.ExploreSearchListener() {
            @Override
            public void onTextChange(InputContentInfoCompat inputContentInfo, int flags, Bundle opts) {
                ImageQuickUri = inputContentInfo.getLinkUri();
                if (ImageQuickUri != null) {
                    sendQuickImage(ImageQuickUri, queryName(getContentResolver(), ImageQuickUri));
                }

            }

        });

    }


    private void getAllDeviceToken() {
        allMemberName = "";
        for (Map.Entry<String, Object> entry : groups.member.entrySet()) {
            getBlockUsers(Integer.parseInt(myUid), Integer.parseInt(entry.getKey()));
        }
    }

    private void getBlockUsers(int myId, final int otherId) {
        String note = "";
        if (myId > otherId) {
            note = otherId + "_" + myId;
        } else note = myId + "_" + otherId;

        FirebaseDatabase.getInstance().getReference().child("block_users")
                .child(note).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    BlockUser blockUser = dataSnapshot.getValue(BlockUser.class);
                    if (blockUser != null) {
                        //memberToken.remove(blockUser.blockedBy);
                        //blockUser.blockedBy
                    }
                } else {
                    getUserWithoutMute(otherId);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    private void getUserWithoutMute(int otherID) {
        FirebaseDatabase.getInstance().getReference().child("users").child(String.valueOf(otherID))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final FirebaseUser user = dataSnapshot.getValue(FirebaseUser.class);
                        if (user.uId != Mualab.currentUser.id) {
                            if (!user.firebaseToken.equals("")) {

                                chatRefMuteUser.child(String.valueOf(user.uId)).child(myUid).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        try {
                                            if (dataSnapshot.exists()) {
                                                if (dataSnapshot.getValue() != null) {
                                                    MuteUser muteUser = dataSnapshot.getValue(MuteUser.class);
                                                    assert muteUser != null;
                                                    if (muteUser.mute == 1) {
                                                        memberToken.remove(user.uId);
                                                    } else {
                                                        memberToken.put(user.uId, user.firebaseToken);
                                                    }
                                                }
                                            } else {
                                                memberToken.put(user.uId, user.firebaseToken);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                            allMemberName = user.userName + ", " + allMemberName;
                            String names = allMemberName.substring(0, allMemberName.lastIndexOf(", "));
                            tvOnlineStatus.setText("Broadcasting");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void getBroadCastDetails() {

        mBroadCastRef.child(broadcastId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Map<String, Groups> objectMap = (HashMap<String, Groups>)
                            dataSnapshot.getValue();
                    String groupName = String.valueOf(objectMap.get("groupName"));
                    String groupImg = String.valueOf(objectMap.get("groupImg"));

                    groups = dataSnapshot.getValue(Groups.class);
                    if (map != null) {
                        CircleImageView ivUserProfile = findViewById(R.id.ivUserProfile);
                        tvUserName.setText((groups.member.size() - 1) + " Recipients");
                       /* if (groups.groupImg != null) {
                            Picasso.with(BroadCastChatActivity.this).load(groups.groupImg).placeholder(R.drawable.group_defoult_icon).
                                    into(ivUserProfile);
                        }*/
                    }
                    getAllDeviceToken();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void init() {
        map = new HashMap<>();
        chatList = new ArrayList<>();
        groups = new Groups();
        arrayList = new ArrayList<>();
        fbTokenListForMobile = new ArrayList<>();
        fbTokenListForWeb = new ArrayList<>();
        memberToken = new HashMap<>();

        chattingAdapter = new ChattingAdapter(BroadCastChatActivity.this, chatList, myUid, this);

        llDots = findViewById(R.id.llDots);
        recycler_view = findViewById(R.id.recycler_view);
        AppCompatImageView iv_pickImage = findViewById(R.id.iv_pickImage);
        ImageView iv_capture_image = findViewById(R.id.iv_capture_image);

        LinearLayoutManager layoutManager = new LinearLayoutManager(BroadCastChatActivity.this, LinearLayoutManager.VERTICAL, false);
        recycler_view.setLayoutManager(layoutManager);
        recycler_view.setAdapter(chattingAdapter);

        et_for_sendTxt = findViewById(R.id.et_for_sendTxt);
        TextView tv_for_send = findViewById(R.id.tv_for_send);
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        tv_for_send.setOnClickListener(this);
        llDots.setOnClickListener(this);
        iv_pickImage.setOnClickListener(this);
        iv_capture_image.setOnClickListener(this);
        ly_group_info.setOnClickListener(this);
    }

    private void getBroadcastChatList() {

        mBroadCastChatRef.child(broadcastId).orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    Chat messageOutput = dataSnapshot.getValue(Chat.class);
                    // map.put(dataSnapshot.getKey(),messageOutput);
                    getChatDataInmap(dataSnapshot.getKey(), messageOutput);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try {
                    //  recycler_view.scrollToPosition(chatList.size() - 1);
                    Chat messageOutput = dataSnapshot.getValue(Chat.class);
                    getChatDataInmap(dataSnapshot.getKey(), messageOutput);
                    // map.put(dataSnapshot.getKey(),messageOutput);
                } catch (Exception e) {
                    e.printStackTrace();
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
                progress_bar.setVisibility(View.GONE);
            }
        });
    }

    private void getChatDataInmap(final String key, final Chat chat) {
        if (chat != null) {

            if (chat.message.equals(matchImageUrl)) {
                chat.message = ImageQuickUri.toString();
                matchImageUrl = "";
            }

            map.put(key, chat);
            chatList.clear();
            Collection<Chat> values = map.values();
            chat.banner_date = CommonUtils.getDateBanner((Long) chat.timestamp);
            chatList.addAll(values);
            chattingAdapter.notifyDataSetChanged();
            recycler_view.scrollToPosition(map.size() - 1);
            progress_bar.setVisibility(View.GONE);
           /* if (chatList.size()==0) {
                progress_bar.setVisibility(View.GONE);
                //  tv_no_chat.setVisibility(View.VISIBLE);
            }else {
                progress_bar.setVisibility(View.GONE);
                //   tv_no_chat.setVisibility(View.GONE);
            }*/
        }
        shortList();
    }

    private void shortList() {
        Collections.sort(chatList, new Comparator<Chat>() {

            @Override
            public int compare(Chat a1, Chat a2) {

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

    @Override
    public void onScrollChange(int position, Object timestamp) {

    }

    @Override
    public void onLongPress(int position, String Refkey) {
        /*ArrayList<Item> items;
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
                //askDelete();
                MyToast.getInstance(BroadCastChatActivity.this).showDasuAlert(Refkey);
            }

            @Override
            public void onDialogDismiss() {
                chatList.get(position).isLongSelected = false;
                chattingAdapter.notifyDataSetChanged();
            }
        }).show(getSupportFragmentManager());*/
    }

    @Override
    public void onPress(int position) {

    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return;
        }

        mLastClickTime = SystemClock.elapsedRealtime();

        switch (v.getId()) {
            case R.id.llDots:
                KeyboardUtil.hideKeyboard(et_for_sendTxt, BroadCastChatActivity.this);
                /*int[] location = new int[2];
                llDots.getLocationOnScreen(location);

                //Initialize the Point with x, and y positions
                Display display = getWindowManager().getDefaultDisplay();
                Point p = new Point();
                display.getSize(p);
                p.x = location[0];
                p.y = location[1];
              */
                arrayList.clear();

                arrayList.add("Broadcast Details");
                arrayList.add("Add Recipients");
                arrayList.add("Remove Recipients");

                NameDisplayDialog.newInstance("Select Option", arrayList, pos -> {
                    String data = arrayList.get(pos);
                    switch (data) {
                        case "Broadcast Details":
                            Intent intent = new Intent(BroadCastChatActivity.this, BroadcastDetails.class);
                            intent.putExtra("broadcastId", broadcastId);
                            startActivityForResult(intent, Constant.REQUEST_CODE_CHOOSE);
                            break;

                        case "Add Recipients":
                            intent = new Intent(BroadCastChatActivity.this, AddMemberToBrodcastActivity.class);
                            intent.putExtra("broadcastId", broadcastId);
                            startActivityForResult(intent, Constant.REQUEST_CODE_CHOOSE);
                            break;

                        case "Remove Recipients":
                            intent = new Intent(BroadCastChatActivity.this, RemoveBroadcastMemberActivity.class);
                            intent.putExtra("broadcastId", broadcastId);
                            startActivityForResult(intent, Constant.REQUEST_CODE_CHOOSE);

                            break;


                    }
                }).show(getSupportFragmentManager());


                break;

            case R.id.tv_for_send:
                String txt = et_for_sendTxt.getText().toString().trim();
                if (!txt.equals("")) {
                    // for broadcast table need to update broadcastChat and broadcast from history
                    Chat broadcastChat = new Chat();
                    broadcastChat.message = txt;
                    broadcastChat.timestamp = ServerValue.TIMESTAMP;
                    broadcastChat.reciverId = broadcastId;
                    broadcastChat.senderId = myUid;
                    broadcastChat.messageType = 0;
                    broadcastChat.readStatus = 0;

                    ChatHistory broadcastHistory = new ChatHistory();
                    broadcastHistory.favourite = 0;
                    broadcastHistory.memberCount = 0;
                    broadcastHistory.message = txt;
                    broadcastHistory.messageType = 0;
                    broadcastHistory.profilePic = "http://koobi.co.uk:3000/front/img/loader.png";
                    broadcastHistory.reciverId = broadcastId;
                    broadcastHistory.senderId = myUid;
                    broadcastHistory.type = "broadcast";
                    broadcastHistory.userName = Mualab.currentUser.userName;
                    broadcastHistory.timestamp = ServerValue.TIMESTAMP;
                    broadcastHistory.unreadMessage = 0;

                    holdKeyForImage = mBroadCastChatRef.child(broadcastId).push().getKey();
                    mBroadCastChatRef.child(broadcastId).child(holdKeyForImage).setValue(broadcastChat);
                    mChatHistoryRef.child(myUid).child(broadcastId).setValue(broadcastHistory);


                    sendMsgImg(txt, broadcastHistory, 0);


                    break;
                }

            case R.id.iv_pickImage:
                if (!ConnectionDetector.isConnected()) {
                    new NoConnectionDialog(BroadCastChatActivity.this, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if (isConnected) {
                                dialog.dismiss();
                            }
                        }
                    }).show();
                } else {
                    KeyboardUtil.hideKeyboard(et_for_sendTxt, BroadCastChatActivity.this);
                    isFromGallery = true;
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{
                                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                    Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);
                        } else {
                            choosePhotoFromGallary();
                        }
                    } else {
                        choosePhotoFromGallary();
                    }
                }
                break;

            case R.id.iv_capture_image:
                if (!ConnectionDetector.isConnected()) {
                    new NoConnectionDialog(BroadCastChatActivity.this, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if (isConnected) {
                                dialog.dismiss();
                            }
                        }
                    }).show();
                } else {
                    isFromGallery = false;
                    KeyboardUtil.hideKeyboard(et_for_sendTxt, BroadCastChatActivity.this);
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkSelfPermission(android.Manifest.permission.CAMERA) !=
                                PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                    Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);
                        } else {
                            takePhotoFromCamera();
                        }
                    } else {
                        takePhotoFromCamera();
                    }
                }
                break;

            case R.id.ly_group_info:
                Intent intent = new Intent(BroadCastChatActivity.this, BroadcastDetails.class);
                intent.putExtra("broadcastId", broadcastId);
                startActivityForResult(intent, Constant.REQUEST_CODE_CHOOSE);
                break;
        }
    }

    private void updateOtherChatHistory(final String groupMemberVal, final ChatHistory chatModel) {

        mChatHistoryRef.child(groupMemberVal).child(chatModel.senderId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            String key = dataSnapshot.getKey();
                            ChatHistory messageOutput = dataSnapshot.getValue(ChatHistory.class);
                            assert messageOutput != null;
                            unreadMsgCount = messageOutput.unreadMessage + 1;

                            if (groups.adminId == Integer.parseInt(myUid))
                                messageOutput.memberType = "admin";
                            else
                                messageOutput.memberType = "member";

                            messageOutput.timestamp = chatModel.timestamp;
                            messageOutput.message = chatModel.message;
                            messageOutput.reciverId = chatModel.reciverId;
                            messageOutput.senderId = myUid;
                            messageOutput.userName = chatModel.userName;
                            messageOutput.type = "user";

                            if (groupMemberVal.equals(myUid)) {
                                messageOutput.unreadMessage = 0;
                            } else {
                                messageOutput.unreadMessage = unreadMsgCount;
                                mChatHistoryRef.child(String.valueOf(groupMemberVal)).child(myUid).setValue(messageOutput);

                                holdKeyForImage = mChatRef.child(groupMemberVal).child(myUid).push().getKey();
                                mChatRef.child(groupMemberVal).child(myUid).child(holdKeyForImage).setValue(chatModel);
                            }
                        } else {

                            if (!groupMemberVal.equals(myUid)) {
                                mChatHistoryRef.child(groupMemberVal).child(myUid).setValue(chatModel);
                                holdKeyForImage = mChatRef.child(groupMemberVal).child(myUid).push().getKey();
                                mChatRef.child(groupMemberVal).child(myUid).child(holdKeyForImage).setValue(chatModel);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progress_bar.setVisibility(View.GONE);
                        //tv_no_chat.setVisibility(View.VISIBLE);
                    }
                });
    }


    public void choosePhotoFromGallary() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, Constant.CAMERA_REQUEST);
    }

    private void takePhotoFromCamera() {
        ImagePicker.pickImageFromCamera(this);
    }


    private void popupForUser(Point p) {
        try {
            LayoutInflater inflater = (LayoutInflater) BroadCastChatActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View layout = inflater.inflate(R.layout.layout_popup_menu, (ViewGroup) findViewById(R.id.parent));
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

            arrayList.add("Broadcast Details");
            arrayList.add("Add Recipients");
            arrayList.add("Remove Recipients");

            popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
            RecyclerView recycler_view = layout.findViewById(R.id.recycler_view);

            MenuAdapter menuAdapter = new MenuAdapter(BroadCastChatActivity.this, arrayList, new MenuAdapter.Listener() {
                @Override
                public void onMenuClick(int pos) {
                    final String data = arrayList.get(pos);
                    popupWindow.dismiss();

                    if (!ConnectionDetector.isConnected()) {
                        new NoConnectionDialog(BroadCastChatActivity.this, new NoConnectionDialog.Listner() {
                            @Override
                            public void onNetworkChange(Dialog dialog, boolean isConnected) {
                                if (isConnected) {
                                    dialog.dismiss();
                                }
                            }
                        }).show();
                    }
                    switch (data) {
                        case "Broadcast Details":
                            popupWindow.dismiss();
                            Intent intent = new Intent(BroadCastChatActivity.this, BroadcastDetails.class);
                            intent.putExtra("broadcastId", broadcastId);
                            startActivityForResult(intent, Constant.REQUEST_CODE_CHOOSE);
                            break;

                        case "Add Recipients":
                            intent = new Intent(BroadCastChatActivity.this, AddMemberToBrodcastActivity.class);
                            intent.putExtra("broadcastId", broadcastId);
                            startActivityForResult(intent, Constant.REQUEST_CODE_CHOOSE);
                            break;

                        case "Remove Recipients":
                            intent = new Intent(BroadCastChatActivity.this, RemoveBroadcastMemberActivity.class);
                            intent.putExtra("broadcastId", broadcastId);
                            startActivityForResult(intent, Constant.REQUEST_CODE_CHOOSE);

                            break;


                    }
                }
            });

            LinearLayoutManager layoutManager = new LinearLayoutManager(BroadCastChatActivity.this);
            recycler_view.setLayoutManager(layoutManager);
            recycler_view.setAdapter(menuAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlertClearChat() {
        final Dialog dialog = new Dialog(BroadCastChatActivity.this);
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
                chatList.clear();
                chattingAdapter.notifyDataSetChanged();
                mBroadCastChatRef.child(broadcastId).removeValue();
                mChatHistoryRef.child(myUid).child(broadcastId).child("message").setValue("");
                mChatHistoryRef.child(myUid).child(broadcastId).child("messageType").setValue("0");

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
                //  ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), 0.0f).start();
                //for enter from bottom
                //ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0.0f).start();
            }
        });

        dialog.show();
    }

    private void showAlertDeleteBrodcast() {
        final Dialog dialog = new Dialog(BroadCastChatActivity.this);
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
                mBroadCastRef.child(broadcastId).removeValue();
                mBroadCastChatRef.child(broadcastId).removeValue();
                mChatHistoryRef.child(myUid).child(broadcastId).removeValue();

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
                //     ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), 0.0f).start();
                //for enter from bottom
                //ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0.0f).start();
            }
        });

        dialog.show();
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
                    MyToast.getInstance(BroadCastChatActivity.this).showDasuAlert("Your  Permission Denied");

            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri;

        if (resultCode == 2) {
            chatList.clear();
            chattingAdapter.notifyDataSetChanged();
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == Constant.REQUEST_CODE_CHOOSE) {
                onBackPressed();
            }

            if (requestCode == Constant.CAMERA_REQUEST) {
                //Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                Uri imageUri = ImagePicker.getImageURIFromResult(this, requestCode, resultCode, data);
                if (imageUri != null) {
                    uri = imageUri;


                    if (imageUri.toString().startsWith("file:///storage/emulated")) {
                        Bitmap bitmap = ImagePicker.getImageFromResult(BroadCastChatActivity.this, requestCode, resultCode, data);
                        imageUri = getImageUri(BroadCastChatActivity.this, bitmap);
                    }
                    ImageQuickUri = imageUri;
                    sendQuickImage(imageUri, queryName(getContentResolver(), imageUri));


                    //  CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE).setAspectRatio(400, 400).start(this);
                } else {
                    MyToast.getInstance(BroadCastChatActivity.this).showDasuAlert(getString(R.string.msg_some_thing_went_wrong));
                }


            }
        }
    }

    private String queryName(ContentResolver resolver, Uri uri) {
        if (uri.toString().contains("http")) {
            if (uri.toString().contains("gif")) {
                return ".gif";
            } else return ".jpg";
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

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        //inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void sendQuickImage(Uri imageUri, String fileName) {


        if (ImageQuickUri != null) {
            uploadImage(ImageQuickUri, fileName);

        }
    }


    private void sendMsgImg(String imageUri, ChatHistory broadcastHistory, int msgType) {
        // for every member send msg to history and in chat node
        for (Map.Entry<String, Object> entry : groups.member.entrySet()) {
            String groupMemberVal = entry.getKey();
            getToKnowIsBlockUser(Integer.parseInt(myUid), Integer.parseInt(groupMemberVal), imageUri, msgType);

            batchCountRef.
                    child(groupMemberVal).child("totalCount").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.getValue() != null) {
                            int count = dataSnapshot.getValue(Integer.class);

                            if (memberToken.containsKey(Integer.parseInt(groupMemberVal))) {
                                String token = memberToken.get(Integer.parseInt(groupMemberVal));

                                if (broadcastHistory.messageType == 0)
                                    sendNotifications(token, String.valueOf(count + 1), broadcastHistory.message);
                                else
                                    sendNotifications(token, String.valueOf(count + 1), "Image");

                                batchCountRef.child(groupMemberVal).child("totalCount").setValue(count+1);
                            }
                        }else {
                            if (memberToken.containsKey(Integer.parseInt(groupMemberVal))) {
                                String token = memberToken.get(Integer.parseInt(groupMemberVal));

                                if (broadcastHistory.messageType == 0)
                                    sendNotifications(token, String.valueOf(1), broadcastHistory.message);
                                else
                                    sendNotifications(token, String.valueOf(1), "Image");

                                batchCountRef.child(groupMemberVal).child("totalCount").setValue(1);

                            }
                        }

                    } catch (Exception e) {

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }


        fbTokenListForMobile.clear();
        fbTokenListForWeb.clear();
        Collection<String> values = memberToken.values();
        fbTokenListForMobile.addAll(values);


        et_for_sendTxt.setText("");
       /* if (broadcastHistory.messageType == 0)
            sendPushNotificationToReceiver(broadcastHistory.message);
        else
            sendPushNotificationToReceiver("Image");*/
    }

    private void sendPushNotificationToReceiver(String message) {

      /*  for (int i = 0; i < fbTokenListForWeb.size(); i++) {

            WebNotification webNotification = new WebNotification();
            webNotification.body = message;
            webNotification.title = Mualab.currentUser.userName;
            webNotification.url = "";
            //chatRefWebnotif.child(fbTokenListForWeb.get(i)).setValue(webNotification); do it later
        }
*/
        FcmNotificationBuilder.initialize()
                .title(Mualab.currentUser.userName)
                .message(message).uid(myUid)
                .username(Mualab.currentUser.userName).adminId(String.valueOf(groups.adminId))
                .type("groupChat").clickAction("GroupChatActivity")
                .registrationId(fbTokenListForMobile).send();

    }

    private void sendNotifications(String fbToken, String count, String message) {
        FcmNotificationBuilder.initialize()
                .title(Mualab.currentUser.userName)
                .batchCount(count)
                .message(message).uid(myUid)
                .username(Mualab.currentUser.userName).adminId(String.valueOf(groups.adminId))
                .type("groupChat").clickAction("GroupChatActivity")
                .firebaseToken(FirebaseInstanceId.getInstance().getToken())
                .receiverFirebaseToken(fbToken).send();
    }


    private void getToKnowIsBlockUser(int myId, final int otherId, final String imageUri, final int msgType) {
        String note = "";
        if (myId > otherId) {
            note = otherId + "_" + myId;
        } else note = myId + "_" + otherId;

        FirebaseDatabase.getInstance().getReference().child("block_users").child(note).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    BlockUser blockUser = dataSnapshot.getValue(BlockUser.class);
                    if (blockUser.blockedBy != null) {

                    }
                } else {
                    ChatHistory chatHistory2 = new ChatHistory();
                    chatHistory2.favourite = 0;
                    chatHistory2.memberCount = 0;
                    chatHistory2.message = imageUri;
                    chatHistory2.messageType = msgType;
                    chatHistory2.profilePic = Mualab.currentUser.profileImage;
                    chatHistory2.reciverId = String.valueOf(otherId);
                    chatHistory2.senderId = myUid;
                    chatHistory2.type = "user";
                    chatHistory2.userName = Mualab.currentUser.userName;
                    chatHistory2.timestamp = ServerValue.TIMESTAMP;
                    chatHistory2.unreadMessage = unreadMsgCount;

                    updateOtherChatHistory(String.valueOf(otherId), chatHistory2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void uploadImage(final Uri imageUri, String fileName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        if (imageUri != null) {
            if (fileName.equals("")) {
                fileName = ".jpg";
            } else if (fileName.contains("gif")) {
                fileName = ".gif";
            } else {
                fileName = ".jpg";
            }

            if (imageUri.toString().contains("http")) {// case from google
                sendImage(imageUri);
            } else {
                Progress.show(BroadCastChatActivity.this);
                getImageUrl(imageUri, fileName, storageReference);
            }
        }
    }

    private void getImageUrl(Uri imageUri, String fileName, StorageReference storageReference) {
        StorageReference ref = storageReference.child("images/" + ServerValue.TIMESTAMP + fileName);
        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Progress.hide(BroadCastChatActivity.this);

                        // for broadcast table need to update broadcastChat and broadcast from history
                        sendImage(uri);
                    }
                }))
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Progress.hide(BroadCastChatActivity.this);
                        Log.e("TAG", "onFailure: " + e.getMessage());
                        Toast.makeText(BroadCastChatActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void sendImage(Uri uri) {
        Chat broadcastChat = new Chat();
        broadcastChat.message = uri.toString();
        broadcastChat.timestamp = ServerValue.TIMESTAMP;
        broadcastChat.reciverId = broadcastId;
        broadcastChat.senderId = myUid;
        broadcastChat.messageType = 1;
        broadcastChat.readStatus = 0;

        ChatHistory broadcastHistory = new ChatHistory();
        broadcastHistory.favourite = 0;
        broadcastHistory.memberCount = 0;
        broadcastHistory.message = uri.toString();
        broadcastHistory.messageType = 1;
        broadcastHistory.profilePic = "http://koobi.co.uk:3000/front/img/loader.png";
        broadcastHistory.reciverId = broadcastId;
        broadcastHistory.senderId = myUid;
        broadcastHistory.type = "broadcast";
        broadcastHistory.userName = Mualab.currentUser.userName;
        broadcastHistory.timestamp = ServerValue.TIMESTAMP;
        broadcastHistory.unreadMessage = 0;


        holdKeyForImage = mBroadCastChatRef.child(broadcastId).push().getKey();
        mBroadCastChatRef.child(broadcastId).child(holdKeyForImage).setValue(broadcastChat);

        mChatHistoryRef.child(myUid).child(broadcastId).setValue(broadcastHistory);

        sendMsgImg(uri.toString(), broadcastHistory, 1);
    }
}


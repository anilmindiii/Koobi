package com.mualab.org.user.activity.main;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.artist_profile.model.UserProfileData;
import com.mualab.org.user.activity.base.BaseActivity;
import com.mualab.org.user.activity.booking.BookingDetailsActivity;
import com.mualab.org.user.activity.businessInvitaion.activity.InvitationActivity;
import com.mualab.org.user.activity.explore.ExploreFragmentNew;
import com.mualab.org.user.activity.feeds.activity.FeedSingleActivity;
import com.mualab.org.user.activity.feeds.fragment.FeedsFragment;
import com.mualab.org.user.activity.gellery.GalleryActivity;
import com.mualab.org.user.activity.main.listner.CountClick;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
import com.mualab.org.user.activity.notification.fragment.NotificationFragment;
import com.mualab.org.user.activity.searchBoard.fragment.SearchBoardFragment;
import com.mualab.org.user.activity.story.StoriesActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.ChatActivity;
import com.mualab.org.user.chat.ChatHistoryActivity;
import com.mualab.org.user.chat.GroupChatActivity;
import com.mualab.org.user.chat.listner.CustomeClick;
import com.mualab.org.user.chat.model.ChatHistory;
import com.mualab.org.user.chat.model.Groups;
import com.mualab.org.user.data.feeds.LiveUserInfo;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.SearchBoard.RefineSearchBoard;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MySnackBar;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.LocationDetector;
import com.mualab.org.user.utils.constants.Constant;
import com.mualab.org.user.utils.network.NetworkChangeReceiver;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.mualab.org.user.data.local.prefs.Session.isLogout;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    public ImageView ivHeaderBack, ivHeaderUser, ivAppIcon, ibtnChat;
    public TextView tvHeaderTitle, tv_business_count,tv_badge_count;
    public RelativeLayout rootLayout;
    public CardView rlHeader1;
    public RefineSearchBoard item;
    private ImageView ibtnLeaderBoard, ibtnFeed, ibtnAddFeed, ibtnSearch, ibtnNotification;
    private int clickedId = 0;
    private long mLastClickTime = 0;
    // private ArrayList<LiveUserInfo> liveUserList;
    private boolean doubleBackToExitPressedOnce;
    private Runnable runnable;
    private DatabaseReference reference;
    private User user;
    public Double lat, lng;
    private UserProfileData profileData;
    protected FusedLocationProviderClient mFusedLocationClient;
    protected LocationRequest locationRequest;
    private String isFromFeedPost = "";
    Session session;
    RefineSearchBoard locationData;
    private ArrayList<LiveUserInfo> liveUserList;
    private int unReadCount = 0;
    private TextView tv_batch_count;
    private HashMap<String, Integer> batchCountMap;
    public static int businessBadge;
    private DatabaseReference batchCountRef;


    public void setBgColor(int color) {
        if (rlHeader1 != null)
            rlHeader1.setBackgroundColor(getResources().getColor(color));
        if (rootLayout != null) {
            rootLayout.setBackgroundColor(getResources().getColor(color));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        session = Mualab.getInstance().getSessionManager();
        user = session.getUser();
        profileData = new UserProfileData();
        liveUserList = new ArrayList<>();
        batchCountMap = new HashMap<>();
        tv_batch_count = findViewById(R.id.tv_batch_count);
        if (getIntent().getStringExtra("FeedPostActivity") != null) {
            isFromFeedPost = getIntent().getStringExtra("FeedPostActivity");
        }
        isLogout = false;

        if (user != null) {
            Mualab.currentUser = Mualab.getInstance().getSessionManager().getUser();
            Mualab.feedBasicInfo.put("userId", "" + user.id);
            Mualab.feedBasicInfo.put("age", "25");
            Mualab.feedBasicInfo.put("gender", "male");
            Mualab.feedBasicInfo.put("city", "indore");
            Mualab.feedBasicInfo.put("state", "MP");
            Mualab.feedBasicInfo.put("country", "India");
        }
        batchCountRef = FirebaseDatabase.getInstance().getReference().child("socialBookingBadgeCount").child(String.valueOf(Mualab.currentUser.id));

        final NoConnectionDialog network = new NoConnectionDialog(MainActivity.this, new NoConnectionDialog.Listner() {
            @Override
            public void onNetworkChange(Dialog dialog, boolean isConnected) {
                if (isConnected) {
                    dialog.dismiss();
                }
            }
        });


        NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
        networkChangeReceiver.setListner(new NetworkChangeReceiver.Listner() {
            @Override
            public void onNetworkChange(boolean isConnected) {
                if (isConnected) {
                    network.dismiss();
                } else network.show();
            }
        });


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            item = (RefineSearchBoard) bundle.getSerializable("refineSearchBoard");
            locationData = (RefineSearchBoard) bundle.getSerializable("locationData");
        }

        initView();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (!isFromFeedPost.equals("FeedPostActivity")) {
                    replaceFragment(SearchBoardFragment.newInstance(item, locationData), false);
                } else {
                    ibtnFeed.callOnClick();
                }

            }
        });


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (user != null) {
                    String myUid = String.valueOf(Mualab.currentUser.id);
                    reference = FirebaseDatabase.getInstance().getReference().child("users").child(myUid);
                    //getUserDetail();
                }
            }
        }, 400);

        getDeviceLocation();

        /*Manage Notification*/
        Intent intent = getIntent();
        if (intent != null) {

            try {
                //deeplinking
                Uri data = getIntent().getData();
                if (data != null) {
                    String url = data.toString();
                    String[] urlSplit = url.split("http://koobi.co.uk/feedDetail/");
                    int feedId = Integer.parseInt(urlSplit[1]);

                    intent = new Intent(this, FeedSingleActivity.class);
                    intent.putExtra("feedId", String.valueOf(feedId));
                    startActivity(intent);
                }

            } catch (Exception e) {

            }


            if (intent.getStringExtra("notifyId") != null) {
                String type = intent.getStringExtra("type");
                String notifyId = intent.getStringExtra("notifyId");
                String userName = intent.getStringExtra("userName");
                String urlImageString = intent.getStringExtra("urlImageString");
                String userType = intent.getStringExtra("userType");
                String notifincationType = (intent.hasExtra("notifincationType")) ?
                        intent.getStringExtra("notifincationType") : intent.getStringExtra("notificationType");

                notificationRedirections(notifyId, userName, urlImageString, userType, notifincationType);
            } else if (intent.hasExtra("notifincationType")) {

                if (intent.getStringExtra("notifincationType").equals("15")) {

                    if (intent.getStringExtra("opponentChatId").contains("group")) {
                        Intent chatIntent = new Intent(this, GroupChatActivity.class);
                        chatIntent.putExtra("groupId", intent.getStringExtra("opponentChatId"));
                        chatIntent.putExtra("body", intent.getStringExtra("body"));
                        chatIntent.putExtra("userName", intent.getStringExtra("userName"));
                        startActivity(chatIntent);
                    } else {
                        Intent chatIntent = new Intent(this, ChatActivity.class);
                        chatIntent.putExtra("opponentChatId", intent.getStringExtra("opponentChatId"));
                        chatIntent.putExtra("body", intent.getStringExtra("body"));
                        chatIntent.putExtra("userName", intent.getStringExtra("userName"));
                        startActivity(chatIntent);
                    }


                }
            }

            if (intent.hasExtra("FeedPostActivity")) {
                setInactiveTab();
                clickedId = 2;
                tvHeaderTitle.setText(getString(R.string.app_name));
                ibtnFeed.setImageResource(R.drawable.active_feeds_ico);
                ivHeaderUser.setVisibility(View.VISIBLE);
                ibtnChat.setVisibility(View.VISIBLE);
                tvHeaderTitle.setVisibility(View.GONE);
                ivAppIcon.setVisibility(View.VISIBLE);
                ivHeaderBack.setVisibility(View.GONE);
                replaceFragment(FeedsFragment.newInstance(1), false);
            }

        } else {
            addFragment(SearchBoardFragment.newInstance(item, locationData), false);
        }

        checkPermission();
        getBusinessInvitaionBadgeCount();
        getbatchCount();

        getHistoryList();
    }


    private void notificationRedirections(String notifyId, String userName, String urlImageString, String userType, String notifincationType) {
        switch (notifincationType) {
            case "13":
                ibtnFeed.callOnClick();
                replaceFragment(FeedsFragment.newInstance(1), false);
                LiveUserInfo me = new LiveUserInfo();
                me.id = Integer.parseInt(notifyId);
                me.userName = userName;
                me.profileImage = urlImageString;
                me.storyCount = 0;
                liveUserList.add(me);
                Intent intent_story = new Intent(MainActivity.this, StoriesActivity.class);
                Bundle args = new Bundle();
                args.putSerializable("ARRAYLIST", liveUserList);
                args.putInt("position", 0);
                intent_story.putExtra("BUNDLE", args);
                startActivityForResult(intent_story, Constant.FEED_FRAGMENT);
                break;

            case "7":
                ibtnFeed.callOnClick();
                replaceFragment(FeedsFragment.newInstance(1), false);
                Intent intent1 = new Intent(MainActivity.this, FeedSingleActivity.class);
                intent1.putExtra("feedId", notifyId);
                startActivityForResult(intent1, Constant.FEED_FRAGMENT);

                break;

            case "11":
                ibtnFeed.callOnClick();
                replaceFragment(FeedsFragment.newInstance(1), false);
                Intent intent_like_comment = new Intent(MainActivity.this, FeedSingleActivity.class);
                intent_like_comment.putExtra("feedId", notifyId);
                startActivityForResult(intent_like_comment, Constant.FEED_FRAGMENT);

                break;

            case "9":
                ibtnFeed.callOnClick();
                replaceFragment(FeedsFragment.newInstance(1), false);
                Intent intent_comment = new Intent(MainActivity.this, FeedSingleActivity.class);
                intent_comment.putExtra("feedId", notifyId);
                startActivityForResult(intent_comment, Constant.FEED_FRAGMENT);
                break;

            case "10":
                ibtnFeed.callOnClick();
                replaceFragment(FeedsFragment.newInstance(1), false);
                Intent intent_like_post = new Intent(MainActivity.this, FeedSingleActivity.class);
                intent_like_post.putExtra("feedId", notifyId);
                startActivityForResult(intent_like_post, Constant.FEED_FRAGMENT);

                break;

            case "20":
            case "25":
            case "26":
            case "21":
            case "2":
                Intent booking2 = new Intent(MainActivity.this, BookingDetailsActivity.class);

                if (!notifyId.equals(""))
                    booking2.putExtra("bookingId", Integer.parseInt(notifyId));
                booking2.putExtra("artistName", userName);
                booking2.putExtra("artistProfile", urlImageString);
                booking2.putExtra("key", "main");
                startActivity(booking2);
                break;

            case "1":
                Intent booking1 = new Intent(MainActivity.this, BookingDetailsActivity.class);
                if (!notifyId.equals(""))
                    booking1.putExtra("bookingId", Integer.parseInt(notifyId));
                booking1.putExtra("artistName", userName);
                booking1.putExtra("artistProfile", urlImageString);
                booking1.putExtra("key", "main");
                startActivity(booking1);
                break;


            case "3":
                Intent booking3 = new Intent(MainActivity.this, BookingDetailsActivity.class);
                if (!notifyId.equals(""))
                    booking3.putExtra("bookingId", Integer.parseInt(notifyId));
                booking3.putExtra("artistName", userName);
                booking3.putExtra("artistProfile", urlImageString);
                booking3.putExtra("key", "main");

                startActivity(booking3);
                break;

            case "4":
                Intent booking4 = new Intent(MainActivity.this, BookingDetailsActivity.class);
                if (!notifyId.equals(""))
                    booking4.putExtra("bookingId", Integer.parseInt(notifyId));
                booking4.putExtra("artistName", userName);
                booking4.putExtra("artistProfile", urlImageString);
                booking4.putExtra("key", "main");
                startActivity(booking4);
                break;

            case "6":
                Intent booking6 = new Intent(MainActivity.this, BookingDetailsActivity.class);
                if (!notifyId.equals(""))
                    booking6.putExtra("bookingId", Integer.parseInt(notifyId));
                booking6.putExtra("artistName", userName);
                booking6.putExtra("notification_list", "list");
                booking6.putExtra("key", "main");
                booking6.putExtra("artistProfile", urlImageString);
                booking6.putExtra("shouldPopupOpen", false);
                startActivity(booking6);
                break;
            case "5":
                Intent booking5 = new Intent(MainActivity.this, BookingDetailsActivity.class);
                if (!notifyId.equals(""))
                    booking5.putExtra("bookingId", Integer.parseInt(notifyId));
                booking5.putExtra("artistName", userName);
                booking5.putExtra("notification_list", "list");
                booking5.putExtra("key", "main");
                booking5.putExtra("artistProfile", urlImageString);
                booking5.putExtra("shouldPopupOpen", true);
                startActivity(booking5);
                break;

           /* case "6":
                // here we go for review list
                Intent booking6 = new Intent(MainActivity.this, ReviewRatingActivity.class);
                startActivity(booking6);
                break;*/
            case "14":
            case "12":
                ibtnFeed.callOnClick();
                replaceFragment(FeedsFragment.newInstance(1), false);
                if (userType.equals("user")) {
                    Intent intent_user_profile = new Intent(MainActivity.this, UserProfileActivity.class);
                    intent_user_profile.putExtra("userId", notifyId);
                    startActivityForResult(intent_user_profile, Constant.FEED_FRAGMENT);

                } else {
                    Intent intent_user_profile = new Intent(MainActivity.this, ArtistProfileActivity.class);
                    intent_user_profile.putExtra("artistId", notifyId);
                    startActivityForResult(intent_user_profile, Constant.FEED_FRAGMENT);
                }
                break;


            case "16":
                ibtnFeed.callOnClick();
                Intent intent_taged = new Intent(MainActivity.this, FeedSingleActivity.class);
                intent_taged.putExtra("feedId", notifyId);
                startActivityForResult(intent_taged, Constant.FEED_FRAGMENT);
                break;

            case "17":
                Intent intent = new Intent(MainActivity.this, InvitationActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 121);
            }
        }
    }


    private void initView() {

        //  liveUserList = new ArrayList<>();
        ibtnLeaderBoard = findViewById(R.id.ibtnLeaderBoard);
        ibtnFeed = findViewById(R.id.ibtnFeed);
        ibtnAddFeed = findViewById(R.id.ibtnAddFeed);
        ibtnSearch = findViewById(R.id.ibtnSearch);
        ibtnNotification = findViewById(R.id.ibtnNotification);
        ibtnChat = findViewById(R.id.ivChat);
        ibtnChat.setVisibility(View.VISIBLE);

        TextView tv_batch_count = findViewById(R.id.tv_batch_count);

        ivAppIcon = findViewById(R.id.ivAppIcon);
        ivHeaderBack = findViewById(R.id.btnBack);
        tv_business_count = findViewById(R.id.tv_business_count);
        tv_badge_count = findViewById(R.id.tv_badge_count);
        ivHeaderUser = findViewById(R.id.ivUserProfile);
        ivHeaderUser.setVisibility(View.VISIBLE);
        User user = Mualab.getInstance().getSessionManager().getUser();

        if (!user.profileImage.isEmpty()) {
            Picasso.with(this).load(user.profileImage).placeholder(R.drawable.celbackgroung).
                    fit().into(ivHeaderUser);
        }

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        rlHeader1 = findViewById(R.id.topLayout1);
        rootLayout = findViewById(R.id.rootLayout);

        ibtnLeaderBoard.setOnClickListener(this);
        ibtnAddFeed.setOnClickListener(this);
        ibtnSearch.setOnClickListener(this);
        ibtnNotification.setOnClickListener(this);
        ibtnFeed.setOnClickListener(this);
        ivHeaderBack.setOnClickListener(this);
        ibtnChat.setOnClickListener(this);
        ivAppIcon.setOnClickListener(this);
        ivHeaderUser.setOnClickListener(this);
        ibtnLeaderBoard.setImageResource(R.drawable.active_leaderbord_ico);
        ibtnLeaderBoard.callOnClick();
        ivHeaderBack.setVisibility(View.GONE);
    }

    public void openNewStoryActivity() {
        showToast(getString(R.string.under_development));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.FEED_FRAGMENT:
                setInactiveTab();
              /*  clickedId = 2;
                tvHeaderTitle.setText(getString(R.string.app_name));
                ibtnFeed.setImageResource(R.drawable.active_feeds_ico);
                ivHeaderUser.setVisibility(View.VISIBLE);
                ibtnChat.setVisibility(View.VISIBLE);
                tvHeaderTitle.setVisibility(View.GONE);
                ivAppIcon.setVisibility(View.VISIBLE);
                ivHeaderBack.setVisibility(View.GONE);
                replaceFragment(FeedsFragment.newInstance(1), false);*/
                break;

            case 734:
                setInactiveTab();
                /*clickedId = 2;
                tvHeaderTitle.setText(getString(R.string.app_name));
                ibtnFeed.setImageResource(R.drawable.active_feeds_ico);
                ivHeaderUser.setVisibility(View.VISIBLE);
                ibtnChat.setVisibility(View.VISIBLE);
                tvHeaderTitle.setVisibility(View.GONE);
                ivAppIcon.setVisibility(View.VISIBLE);
                ivHeaderBack.setVisibility(View.GONE);
                replaceFragment(FeedsFragment.newInstance(1), false);*/
                break;
            case 123:
                replaceFragment(SearchBoardFragment.newInstance(item, locationData), false);

                break;

            case 2000:
                if (!user.profileImage.isEmpty()) {
                    apiForGetProfile();

                }


                break;


        }


    }

            /*if(resultCode == 7891){

                Uri uri = data.getData();
                String dataType  = data.getType();
                if(dataType.equals("image/jpeg")){
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(),uri);
                        addMyStory(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }*/


    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        switch (v.getId()) {


            case R.id.ivChat:
                Intent intent_chat = new Intent(MainActivity.this, ChatHistoryActivity.class);
                startActivity(intent_chat);
                break;

            case R.id.ivAppIcon:
                break;

            case R.id.ivUserProfile:
                User user = Mualab.getInstance().getSessionManager().getUser();
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                intent.putExtra("userId", String.valueOf(user.id));
                startActivityForResult(intent, 2000);

                break;

            case R.id.ibtnLeaderBoard:
                if (clickedId != 1) {
                    setInactiveTab();
                    clickedId = 1;
                    tvHeaderTitle.setText(getString(R.string.title_searchboard));
                    ibtnLeaderBoard.setImageResource(R.drawable.active_leaderbord_ico);
                    ivHeaderBack.setVisibility(View.GONE);
                    ivHeaderUser.setVisibility(View.VISIBLE);

                    ibtnChat.setVisibility(View.GONE);
                    ivAppIcon.setVisibility(View.VISIBLE);
                    tvHeaderTitle.setVisibility(View.GONE);

                    replaceFragment(SearchBoardFragment.newInstance(item, locationData), false);
                    rlHeader1.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.ibtnFeed:
                if (clickedId != 2) {
                    setInactiveTab();
                    clickedId = 2;
                    tvHeaderTitle.setText(getString(R.string.app_name));
                    ibtnFeed.setImageResource(R.drawable.active_feeds_ico);
                    ivHeaderUser.setVisibility(View.GONE);
                    SearchBoardFragment.isFavClick = false;
                    ibtnChat.setVisibility(View.VISIBLE);
                    tvHeaderTitle.setVisibility(View.GONE);
                    ivAppIcon.setVisibility(View.VISIBLE);
                    ivHeaderBack.setVisibility(View.GONE);
                    session.saveFilter(null);
                    locationData = null;
                    item = null;

                    replaceFragment(FeedsFragment.newInstance(1), false);
                    rlHeader1.setVisibility(View.VISIBLE);
                    //replaceFragment(NotificationFragment.newInstance("", ""), false);

                }
                break;

            case R.id.ibtnAddFeed:
                if (clickedId != 6) {
                    setInactiveTab();
                    clickedId = 6;
                    SearchBoardFragment.isFavClick = false;
                    ibtnAddFeed.setImageResource(R.drawable.active_add_ico);
                    tvHeaderTitle.setText(R.string.title_explore);
                    ivHeaderUser.setVisibility(View.GONE);
                    tvHeaderTitle.setVisibility(View.VISIBLE);
                    ibtnChat.setVisibility(View.VISIBLE);
                    ivAppIcon.setVisibility(View.GONE);
                    session.saveFilter(null);
                    locationData = null;
                    item = null;

                    Intent in = new Intent(MainActivity.this, GalleryActivity.class);
                    startActivityForResult(in, 734);


                }

                break;

            case R.id.ibtnSearch:
                if (clickedId != 4) {
                    setInactiveTab();

                    clickedId = 4;
                    SearchBoardFragment.isFavClick = false;
                    tvHeaderTitle.setText(R.string.title_explore);
                    ibtnSearch.setImageResource(R.drawable.active_search_ico);
                    ivHeaderUser.setVisibility(View.VISIBLE);
                    tvHeaderTitle.setVisibility(View.VISIBLE);
                    ibtnChat.setVisibility(View.VISIBLE);
                    ivAppIcon.setVisibility(View.GONE);
                    session.saveFilter(null);
                    locationData = null;
                    item = null;
                    rlHeader1.setVisibility(View.GONE);
                    replaceFragment(ExploreFragmentNew.newInstance(), false);

                }
                break;

            case R.id.ibtnNotification:
                if (clickedId != 5) {
                    setInactiveTab();
                    clickedId = 5;
                    SearchBoardFragment.isFavClick = false;
                    tvHeaderTitle.setText(getString(R.string.title_searchboard));
                    ibtnNotification.setImageResource(R.drawable.active_notifications_ico);
                    tvHeaderTitle.setText(R.string.title_notification);
                    ivHeaderUser.setVisibility(View.VISIBLE);
                    tvHeaderTitle.setVisibility(View.VISIBLE);
                    ibtnChat.setVisibility(View.VISIBLE);
                    ivAppIcon.setVisibility(View.GONE);
                    session.saveFilter(null);
                    locationData = null;
                    item = null;
                    rlHeader1.setVisibility(View.VISIBLE);
                    // replaceFragment(new NotificationFragment(), false);

                    replaceFragment(NotificationFragment.newInstance("", ""), false);
                    rlHeader1.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    public void getDeviceLocation() {

        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constant.MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                LocationDetector locationDetector = new LocationDetector();
                FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                if (locationDetector.isLocationEnabled(MainActivity.this) &&
                        locationDetector.checkLocationPermission(MainActivity.this)) {

                    mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                Mualab.currentLocation.lat = latitude;
                                Mualab.currentLocation.lng = longitude;

                                Mualab.currentLocationForBooking.lat = latitude;
                                Mualab.currentLocationForBooking.lng = longitude;

                            }
                        }
                    });

                } else {
                }
            }
        } else {

        }

    }


    private void setInactiveTab() {
        rlHeader1.setVisibility(View.VISIBLE);
        ibtnLeaderBoard.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_leaderbord_ico));
        ibtnFeed.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_feeds_ico));
        ibtnAddFeed.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_add_ico));
        ibtnSearch.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_search_ico));
        ibtnNotification.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_notifications_ico));

    }

    @Override
    public void onBackPressed() {
        /* Handle double click to finish activity*/
        Handler handler = new Handler();
        FragmentManager fm = getSupportFragmentManager();
        int i = fm.getBackStackEntryCount();
        if (i > 0) {
            fm.popBackStack();
        } else if (!doubleBackToExitPressedOnce) {

            doubleBackToExitPressedOnce = true;
            //Toast.makeText(this, "Click again to exit", Toast.LENGTH_SHORT).show();
            MySnackBar.showSnackbar(this, findViewById(R.id.lyCoordinatorLayout), "Click again to exit");
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);

        } else {
            handler.removeCallbacks(runnable);
            super.onBackPressed();
        }
    }


    private void addMyStory(Bitmap bitmap) {

        if (ConnectionDetector.isConnected()) {
            Map<String, String> map = new HashMap<>();
            map.put("type", "image");

            HttpTask task = new HttpTask(new HttpTask.Builder(this, "addMyStory", new HttpResponceListner.Listener() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);
                        String status = js.getString("status");
                        String message = js.getString("message");
                        if (status.equalsIgnoreCase("success")) {
                            showToast(message);
                            finish();
                        } else showToast(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    Log.d("res:", "" + error.getLocalizedMessage());
                }
            })
                    .setParam(map)
                    .setAuthToken(Mualab.getInstance().getSessionManager().getUser().authToken)
                    .setProgress(true));
            task.postImage("myStory", bitmap);
        } else showToast(getString(R.string.error_msg_network));
    }

    private void showToast(String str) {
        if (!TextUtils.isEmpty(str))
            MyToast.getInstance(this).showDasuAlert(str);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        session.saveFilter(null);
        Mualab.getInstance().getSessionManager().setIsOutCallFilter(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // getDeviceLocation();

                        updateLocation();
                    }

                } else {
                   /* if (isFavClick)
                        apiForGetFavArtist(0, false);
                    else
                        apiForGetArtist(0, false);*/


                }
            }

        }
    }

    private void apiForGetProfile() {

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(MainActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetProfile();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));
        params.put("viewBy", "");
        params.put("loginUserId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(MainActivity.this, "getProfile", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {


                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    Progress.hide(MainActivity.this);
                    if (status.equalsIgnoreCase("success")) {
                        JSONArray userDetail = js.getJSONArray("userDetail");
                        JSONObject object = userDetail.getJSONObject(0);
                        Gson gson = new Gson();

                        profileData = gson.fromJson(String.valueOf(object), UserProfileData.class);
                        Picasso.with(MainActivity.this).load(profileData.profileImage).placeholder(R.drawable.default_placeholder).
                                fit().into(ivHeaderUser);

                        //   profileData = gson.fromJson(response, UserProfileData.class);

                        // updateViewType(profileData,R.id.ly_videos);

                    } else {
                        MyToast.getInstance(MainActivity.this).showDasuAlert(message);
                    }


                } catch (Exception e) {
                    Progress.hide(MainActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try {

                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")) {
                        Mualab.getInstance().getSessionManager().logout();
                        FirebaseInstanceId.getInstance().deleteInstanceId();
                        //      MyToast.getInstance(BookingActivity.this).showSmallCustomToast(helper.error_Messages(error));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        })
                .setAuthToken(user.authToken)
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(getClass().getName());
    }

    protected void onGpsAutomatic() {
        int permissionLocation = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {

            locationRequest = new LocationRequest();
            locationRequest.setInterval(3000);
            locationRequest.setFastestInterval(3000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);
            builder.setNeedBle(true);

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());

            Task<LocationSettingsResponse> task =
                    LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

            task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                @Override
                public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                    try {
                        //getting target response use below code
                        LocationSettingsResponse response = task.getResult(ApiException.class);

                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        int permissionLocation = ContextCompat
                                .checkSelfPermission(MainActivity.this,
                                        Manifest.permission.ACCESS_FINE_LOCATION);
                        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {

                            mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        // Logic to handle location object
                                        setLatLng(location);
                                    } else {
                                        //Location not available
                                        Log.e("Test", "Location not available");
                                    }
                                }
                            });
                        }
                    } catch (ApiException exception) {
                        switch (exception.getStatusCode()) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied. But could be fixed by showing the
                                // user a dialog.
                                try {
                                    // Cast to a resolvable exception.
                                    ResolvableApiException resolvable = (ResolvableApiException) exception;
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    resolvable.startResolutionForResult(
                                            MainActivity.this,
                                            Constant.REQUEST_CHECK_SETTINGS_GPS);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                } catch (ClassCastException e) {
                                    // Ignore, should be an impossible error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way to fix the
                                // settings so we won't show the dialog.
                                break;
                        }
                    }
                }
            });
        }
    }

    /**
     * this method get location when available and store in static variable
     */
    public void updateLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(@Nullable Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            setLatLng(location);
                        } else {
                            //Location not available
                            onGpsAutomatic();
                        }
                    }
                });

    }

    protected void setLatLng(@NonNull Location location) {
        Mualab.currentLocation.lat = location.getLatitude();
        Mualab.currentLocation.lng = location.getLongitude();


        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);

        if (currentFragment != null && currentFragment instanceof SearchBoardFragment) {
            SearchBoardFragment boardFragment = (SearchBoardFragment) currentFragment;
            boardFragment.hitApi(0, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        }
        /*if (address.isEmpty()) {
            address = getAddressFromLatLng(Agrinvest.LATITUDE, Agrinvest.LONGITUDE);
            AppLogger.e("Location ", address);
        }*/
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (mFusedLocationClient == null)
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());

        if (clickedId == 6) {
            ibtnFeed.callOnClick();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFusedLocationClient == null)
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @NonNull
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);

                setLatLng(location);
            }
        }
    };

    /*............get batchCount from chat history..............................................*/

    private void getHistoryList() {
        unReadCount = 0;
        DatabaseReference databaseReference;

        DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference = mFirebaseDatabaseReference.child("chat_history").
                child(String.valueOf(Mualab.currentUser.id));

        databaseReference.orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                try {
                    ChatHistory messageOutput = dataSnapshot.getValue(ChatHistory.class);

                    if (messageOutput != null) {
                        getChatDataInMap(key, messageOutput);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try {
                    final String key = dataSnapshot.getKey();
                    ChatHistory messageOutput = dataSnapshot.getValue(ChatHistory.class);

                    if (messageOutput != null) {
                        getChatDataInMap(key, messageOutput);
                    }
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
            }
        });

    }

    private void getChatDataInMap(final String key, final ChatHistory messageOutput) {
        if (messageOutput != null) {

            messageOutput.isTyping = false;
            batchCountMap.put(key, messageOutput.unreadMessage);

            unReadCount = 0;
            Iterator myVeryOwnIterator = batchCountMap.keySet().iterator();
            while (myVeryOwnIterator.hasNext()) {
                String keyMap = (String) myVeryOwnIterator.next();
                int value = batchCountMap.get(keyMap);

                unReadCount = value + unReadCount;
                tv_batch_count.setText(unReadCount + "");

                if (unReadCount == 0) {
                    tv_batch_count.setVisibility(View.GONE);
                } else tv_batch_count.setVisibility(View.VISIBLE);
            }
        } else tv_batch_count.setVisibility(View.GONE);

    }

    private void getBusinessInvitaionBadgeCount() {

        FirebaseDatabase.getInstance().getReference().
                child("socialBookingBadgeCount").child(Mualab.currentUser.id + "").child("businessInvitationCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    try {
                        businessBadge = dataSnapshot.getValue(Integer.class);
                        if (ivHeaderUser.getVisibility() == View.VISIBLE) {
                            if (businessBadge > 0) {
                                tv_business_count.setVisibility(View.VISIBLE);
                                tv_business_count.setText(businessBadge + "");
                                CountClick.getmCountClick().onCountChange(businessBadge);

                            } else tv_business_count.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        String Badge = dataSnapshot.getValue(String.class);
                        businessBadge = Integer.parseInt(Badge);
                        if (ivHeaderUser.getVisibility() == View.VISIBLE) {
                            if (businessBadge > 0) {
                                tv_business_count.setVisibility(View.VISIBLE);
                                tv_business_count.setText(businessBadge + "");
                                CountClick.getmCountClick().onCountChange(businessBadge);
                            } else tv_business_count.setVisibility(View.GONE);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getbatchCount(){
        int totalCount = 0;
        batchCountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    if(dataSnapshot.getValue() != null){
                        HashMap<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();

                        for(String key : objectMap.keySet()){
                            String value =  String.valueOf(objectMap.get(key));
                            Log.d("message",value+"");
                        }

                        String bookingCount =  String.valueOf(objectMap.get("bookingCount"));
                        String socialCount =  String.valueOf(objectMap.get("socialCount"));

                        if(bookingCount.equals("null")){
                            bookingCount = "0";
                        }

                        if(socialCount.equals("null")){
                            socialCount = "0";
                        }

                        int bookingCountInt = Integer.parseInt(bookingCount);
                        int socialCountInt = Integer.parseInt(socialCount);
                        int sum = bookingCountInt+socialCountInt;
                        if(sum == 0){
                            tv_badge_count.setVisibility(View.GONE);
                        }else{
                            tv_badge_count.setText(sum+"");
                            tv_badge_count.setVisibility(View.VISIBLE);
                        }


                       // tv_batch_booking_count.setText(bookingCount+"");

                    }
                }catch (Exception e){
                    Log.d("dd",e+"");
                    //String  bookingCount =  dataSnapshot.getValue(String.class);
                   // tv_batch_booking_count.setText(bookingCount+"");


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


}


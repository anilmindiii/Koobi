package com.mualab.org.user.activity.userProfile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mualab.org.user.R;
import com.mualab.org.user.Views.refreshviews.CircleHeaderView;
import com.mualab.org.user.Views.refreshviews.OnRefreshListener;
import com.mualab.org.user.Views.refreshviews.RjRefreshLayout;
import com.mualab.org.user.activity.artist_profile.activity.FollowersActivity;
import com.mualab.org.user.activity.artist_profile.adapter.ArtistFeedAdapter;
import com.mualab.org.user.activity.artist_profile.model.UserProfileData;
import com.mualab.org.user.activity.dialogs.NameDisplayDialog;
import com.mualab.org.user.activity.explore.GrideToListFragment;
import com.mualab.org.user.activity.feeds.activity.CommentsActivity;
import com.mualab.org.user.activity.feeds.activity.LikeFeedActivity;
import com.mualab.org.user.activity.feeds.adapter.ViewPagerAdapter;
import com.mualab.org.user.activity.feeds.listener.MyClickOnPostMenu;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.activity.main.listner.CountClick;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
import com.mualab.org.user.activity.myprofile.activity.adapter.NavigationMenuAdapter;
import com.mualab.org.user.activity.myprofile.activity.model.NavigationItem;
import com.mualab.org.user.activity.review_rating.ReviewRatingActivity;
import com.mualab.org.user.activity.tag_module.instatag.InstaTag;
import com.mualab.org.user.activity.tag_module.instatag.TagDetail;
import com.mualab.org.user.activity.tag_module.instatag.TagToBeTagged;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.ChatActivity;
import com.mualab.org.user.chat.model.FirebaseUser;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.image.cropper.CropImage;
import com.mualab.org.user.image.cropper.CropImageView;
import com.mualab.org.user.image.picker.ImagePicker;
import com.mualab.org.user.listener.RecyclerViewScrollListenerProfile;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.WrapContentLinearLayoutManager;
import com.mualab.org.user.utils.constants.Constant;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.mualab.org.user.activity.main.MainActivity.businessBadge;


public class UserProfile extends Fragment implements View.OnClickListener,
        ArtistFeedAdapter.Listener, NavigationMenuAdapter.Listener,ArtistFeedAdapter.getGrideClick{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private DrawerLayout drawer;
    private String TAG = this.getClass().getName();
    private User user;
    private TextView tvImages, tvVideos, tvFeeds, tv_msg, tv_no_data_msg, tv_dot1, tv_dot2,tv_block_msg, tv_profile_followers,tv_profile_followers_txt, tv_business_count;
    private LinearLayout ll_progress, llRating,ly_block_view;
    private RecyclerView rvFeed;
    private RjRefreshLayout mRefreshLayout;
    private RecyclerViewScrollListenerProfile endlesScrollListener;
    private int CURRENT_FEED_STATE = 0, lastFeedTypeId;
    private String feedType = "", userId;
    private ArtistFeedAdapter feedAdapter;
    private List<Feeds> feeds;
    private boolean isPulltoRefrash = false;
    private long mLastClickTime = 0;
    private UserProfileData profileData = null;
    private List<NavigationItem> navigationItems;
    private AppCompatButton btnFollow, btnMsg;
    private ViewPagerAdapter.LongPressListner longPressListner;
    private TextView tvFilter;
    private ImageView ivFilter;
    private PopupWindow popupWindow;
    private ArrayList<String> arrayList = new ArrayList<>();
    private boolean isMenuOpen;
    private ImageView iv_gride_view, iv_list_view;
    private boolean isGrideView;
    private EditText ed_search;
    private ImageView iv_search_icon;
    private int page = 0;
    private boolean isRespoceget;
    public boolean isInvitation;
    //NavigationMenuAdapter listAdapter;
    private Bitmap profileImageBitmap;
    private CircleImageView iv_Profile, user_image;
    private String nodeForBlockUser = "";
    private int myId ;
    private DatabaseReference mBlockUser;
    private Context mContext;

    MainActivity.getIsInvitaion isInvitaionListner;

    public void  setInterFace(MainActivity.getIsInvitaion isInvitaionListnerFace) {
    isInvitaionListner = isInvitaionListnerFace;
    }


    public static UserProfile newInstance(MainActivity.getIsInvitaion param1, String param2) {
        UserProfile fragment = new UserProfile();
        Bundle args = new Bundle();
        fragment.setInterFace(param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Intent i = getIntent();
      //  if (i != null)
        {
            userId = String.valueOf(Mualab.currentUser.id);

            myId = Mualab.currentUser.id;
            if(Integer.parseInt(userId) < myId){
                nodeForBlockUser = userId + "_" + myId;
            }else nodeForBlockUser = myId + "_" + userId;
        }
        init(view);

        if (userId != null)
            if (userId.equals(String.valueOf(user.id)))
                if (businessBadge > 0) {
                    tv_business_count.setVisibility(View.VISIBLE);
                    tv_business_count.setText(businessBadge + "");
                }

        CountClick.getmCountClick().setListner(new CountClick.InviationCount() {
            @Override
            public void onCountChange(int count) {
                if (userId != null)
                    if (userId.equals(String.valueOf(user.id)))
                        if (count > 0) {
                            tv_business_count.setVisibility(View.VISIBLE);
                            tv_business_count.setText(count + "");
                        } else tv_business_count.setVisibility(View.GONE);

                if(count == 0){
                    tv_business_count.setVisibility(View.GONE);
                }

            }
        });

        apiForGetProfile(view);
    }

    @Override
    public void onAttach(Context context) {
        this.mContext = context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        super.onDetach();
    }

    private void init(View view) {
        Session session = Mualab.getInstance().getSessionManager();
        user = session.getUser();
        feeds = new ArrayList<>();
        navigationItems = new ArrayList<>();

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        LinearLayout lyImage = toolbar.findViewById(R.id.ly_images);
        LinearLayout lyVideos = toolbar.findViewById(R.id.ly_videos);
        LinearLayout lyFeed = toolbar.findViewById(R.id.ly_feeds);

        tvImages = toolbar.findViewById(R.id.tv_image);
        tvFilter = toolbar.findViewById(R.id.tvFilter);
        LinearLayout ll_filter;
        tvVideos = view.findViewById(R.id.tv_videos);
        tvFeeds = view.findViewById(R.id.tv_feed);
        ivFilter = view.findViewById(R.id.ivFilter);
        ed_search = view.findViewById(R.id.ed_search);
        iv_search_icon = view.findViewById(R.id.iv_search_icon);
        iv_gride_view = toolbar.findViewById(R.id.iv_gride_view);
        iv_list_view = toolbar.findViewById(R.id.iv_list_view);
        llRating = view.findViewById(R.id.llRating);
        ly_block_view = view.findViewById(R.id.ly_block_view);
        tv_business_count = view.findViewById(R.id.tv_invite_count);

        CardView headerCard = view.findViewById(R.id.header);
        headerCard.setVisibility(View.GONE);

        /*tv_dot1 =  view.findViewById(R.id.tv_dot1);
        tv_dot2 =  view.findViewById(R.id.tv_dot2);
*/
        ImageView btnBack = view.findViewById(R.id.btnBack);
        ImageView ivChat = view.findViewById(R.id.ivChat);
        // ivChat.setVisibility(View.VISIBLE);
        ImageView ivUserProfile = view.findViewById(R.id.ivUserProfile);
        ImageView ivDrawer = view.findViewById(R.id.btnNevMenu);
        ivUserProfile.setVisibility(View.GONE);

        btnFollow = view.findViewById(R.id.btnFollow);
        btnMsg = view.findViewById(R.id.btnMsg);
        //   final AppBarLayout mainView = view.findViewById(R.id.appbar);
        drawer = getActivity().findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        if (userId.equals(String.valueOf(user.id))) {
            ivDrawer.setVisibility(View.VISIBLE);
            btnFollow.setVisibility(View.GONE);
            btnMsg.setVisibility(View.GONE);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            ivDrawer.setVisibility(View.GONE);
            btnFollow.setVisibility(View.VISIBLE);
            btnMsg.setVisibility(View.VISIBLE);
        }

        NavigationView navigationView = view.findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        // navigationView.setNavigationItemSelectedListener(this);
        // drawer.setScrimColor(getResources().getColor(android.R.color.transparent));
        navigationView.setItemIconTintList(null);


        RecyclerView rycslidermenu = view.findViewById(R.id.rycslidermenu);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        rycslidermenu.setLayoutManager(layoutManager);
        //listAdapter = new NavigationMenuAdapter(getActivity(), navigationItems, drawer, this);

        //rycslidermenu.setAdapter(listAdapter);

        final RelativeLayout rlContent = view.findViewById(R.id.rlContent);
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                // mainView.setTranslationX(slideOffset * drawerView.getWidth());
                drawer.bringChildToFront(drawerView);
                drawer.requestLayout();
                float slideX = drawerView.getWidth() * slideOffset;
                rlContent.setTranslationX(-slideX);
            }
        });
        TextView user_name = view.findViewById(R.id.user_name);
        final User user = Mualab.getInstance().getSessionManager().getUser();
        user_image = view.findViewById(R.id.user_image);

        user_name.setText(user.userName);


        rvFeed = view.findViewById(R.id.rvFeed);

        LinearLayout llFollowers = view.findViewById(R.id.llFollowers);
        LinearLayout llFollowing = view.findViewById(R.id.llFollowing);
        LinearLayout llPost = view.findViewById(R.id.llPost);

        tv_msg = view.findViewById(R.id.tv_msg);
        ll_filter = view.findViewById(R.id.ll_filter);
        ll_filter.setOnClickListener(this);

        tv_no_data_msg = view.findViewById(R.id.tv_no_data_msg);
        tv_block_msg = view.findViewById(R.id.tv_block_msg);
        ll_progress = view.findViewById(R.id.ll_progress);

        WrapContentLinearLayoutManager lm = new WrapContentLinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        rvFeed.setItemAnimator(null);
        rvFeed.setLayoutManager(lm);
        rvFeed.setHasFixedSize(true);

        feedAdapter = new ArtistFeedAdapter(mContext, feeds, this);
        feedAdapter.setGrideClick(this::gridClick);

        endlesScrollListener = new RecyclerViewScrollListenerProfile() {
            @Override
            public void onLoadMore() {
                if (feedAdapter != null) {
                    ++page;
                    setAdapterLoading(true);
                    apiForGetAllFeeds(page, 20, true, "");


                }
            }
        };

        rvFeed.setAdapter(feedAdapter);
        rvFeed.addOnScrollListener(endlesScrollListener);

        mRefreshLayout = view.findViewById(R.id.mSwipeRefreshLayout);
        final CircleHeaderView header = new CircleHeaderView(mContext);
        mRefreshLayout.addHeader(header);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                endlesScrollListener.resetState();
                isPulltoRefrash = true;
                page = 0;
                apiForGetAllFeeds(0, 20, false, "");

            }

            @Override
            public void onLoadMore() {

            }
        });

        rvFeed.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP)
                    hideQuickView();
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });


        ed_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                page = 0;
                feeds.clear();
                endlesScrollListener.resetState();
                Mualab.getInstance().cancelPendingRequests("artistProfileApi");
                apiForGetAllFeeds(0, 20, false, s.toString().trim());
            }
        });

        iv_search_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ed_search.getVisibility() == View.VISIBLE) {
                    ed_search.setVisibility(View.GONE);
                    if (!ed_search.getText().toString().trim().equals(""))
                        ed_search.setText("");
                } else ed_search.setVisibility(View.VISIBLE);

            }
        });

        view.findViewById(R.id.ly_main).setOnClickListener(this);
        lyImage.setOnClickListener(this);
        lyVideos.setOnClickListener(this);
        lyFeed.setOnClickListener(this);
        llFollowers.setOnClickListener(this);
        llFollowing.setOnClickListener(this);
        llPost.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        ivUserProfile.setOnClickListener(this);
        btnMsg.setOnClickListener(this);
        ivDrawer.setOnClickListener(this);
        btnFollow.setOnClickListener(this);
        llRating.setOnClickListener(this);
        rlContent.setOnClickListener(this);


        iv_gride_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page = 0;
                feeds.clear();
                isGrideView = true;
                ed_search.setText("");
                rvFeed.setLayoutManager(new GridLayoutManager(mContext, 3));
                feedAdapter.isGrideView(true);
                endlesScrollListener.resetState();

                apiForGetAllFeeds(page, 20, true, ""); //last limit 200
                iv_gride_view.setImageResource(R.drawable.active_grid_icon);
                iv_list_view.setImageResource(R.drawable.inactive_list);
            }
        });

        iv_list_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_gride_view.setImageResource(R.drawable.inactive_grid_icon);
                iv_list_view.setImageResource(R.drawable.active_list);


                page = 0;
                feeds.clear();
                isGrideView = false;
                ed_search.setText("");
                rvFeed.setLayoutManager(new LinearLayoutManager(mContext));
                feedAdapter.isGrideView(false);
                endlesScrollListener.resetState();

                apiForGetAllFeeds(page, 20, true, "");//last limit 200
                iv_gride_view.setImageResource(R.drawable.inactive_grid_icon);
                iv_list_view.setImageResource(R.drawable.active_list);

            }
        });

        arrayList.add("All");
        arrayList.add("Photo");
        arrayList.add("Video");
        addItems();


        mBlockUser = FirebaseDatabase.getInstance().getReference().child("blockUserArtist");
        mBlockUser.child(nodeForBlockUser).child("blockedBy").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    try{
                        String blockedId = dataSnapshot.getValue(String.class);

                        if(blockedId.equals(userId) || blockedId.equals("Both")){// case of hide screen
                            hideShowView(true);
                        }else  hideShowView(false);

                    }catch (Exception e){ }

                }else {
                    // case of show screen
                    hideShowView(false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void hideShowView(boolean shouldHide){
        if(shouldHide){
            ly_block_view.setVisibility(View.VISIBLE);
        }else {
            ly_block_view.setVisibility(View.GONE);
        }
    }

    private void setAdapterLoading(boolean isLoad) {
        if (feedAdapter != null) {
            feedAdapter.showLoading(isLoad);
            feedAdapter.notifyDataSetChanged();
        }
    }

    private void apiForGetProfile(View view) {
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetProfile(view);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("viewBy", "");
        params.put("loginUserId", String.valueOf(Mualab.currentUser.id));


        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "getProfile",
                new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    if (feeds != null && feeds.size() == 0)
                        updateViewType(R.id.ly_feeds);

                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    Progress.hide(mContext);
                    if (status.equalsIgnoreCase("success")) {
                        JSONArray userDetail = js.getJSONArray("userDetail");
                        JSONObject object = userDetail.getJSONObject(0);
                        Gson gson = new Gson();

                        profileData = gson.fromJson(String.valueOf(object), UserProfileData.class);
                        if (profileData.isInvitation == 1) {
                            isInvitation = true;
                        } else isInvitation = false;
                        tv_block_msg.setText(profileData.firstName+" "+profileData.lastName+" has blocked you");

                        //listAdapter.getisInvitation(isInvitation);

                       if(isInvitaionListner != null) isInvitaionListner.getInvitationAvail(isInvitation);

                        //addItems();
                        //listAdapter.notifyDataSetChanged();

                        // profileData = gson.fromJson(response, UserProfileData.class);
                        setProfileData(profileData,view);
                        // updateViewType(profileData,R.id.ly_videos);

                    } else {
                        MyToast.getInstance(mContext).showDasuAlert(message);
                    }
                    updateViewType(R.id.ly_feeds);

                } catch (Exception e) {
                    Progress.hide(mContext);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try {
                    updateViewType(R.id.ly_feeds);
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
                .setAuthToken(Mualab.currentUser.authToken)
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(getClass().getName());
    }

    @SuppressLint("SetTextI18n")
    private void setProfileData(UserProfileData profileData,View view) {
        TextView tv_ProfileNameBlock = view.findViewById(R.id.tv_ProfileNameBlock);
        TextView tv_usernameBlock = view.findViewById(R.id.tv_usernameBlock);

        TextView tv_ProfileName = view.findViewById(R.id.tv_ProfileName);
        TextView tv_username = view.findViewById(R.id.tv_username);
        TextView tvRatingCount = view.findViewById(R.id.tvRatingCount);
        final CircleImageView user_image = view.findViewById(R.id.user_image);
        TextView tvRatingCountBlock = view.findViewById(R.id.tvRatingCountBlock);

        TextView tv_distance = view.findViewById(R.id.tv_distance);
        TextView tv_profile_post = view.findViewById(R.id.tv_profile_post);
        TextView tv_profile_post_txt = view.findViewById(R.id.tv_profile_post_txt);
        TextView tv_profile_following = view.findViewById(R.id.tv_profile_following);
        tv_profile_followers = view.findViewById(R.id.tv_profile_followers);
        tv_profile_followers_txt = view.findViewById(R.id.tv_profile_followers_txt);
        iv_Profile = view.findViewById(R.id.iv_Profile);
        ImageView ivActive = view.findViewById(R.id.ivActive);
        RatingBar rating = view.findViewById(R.id.rating);
        RatingBar ratingBlock = view.findViewById(R.id.ratingBlock);

        if (profileData.followerStatus.equals("1")) {
            btnFollow.setText("Unfollow");
        } else {
            btnFollow.setText("Follow");
        }


        if (profileData != null) {
            tv_ProfileName.setText(profileData.firstName + " " + profileData.lastName);
            tv_ProfileNameBlock.setText(profileData.firstName + " " + profileData.lastName);

            tv_distance.setText(profileData.radius + " Miles");
            tv_username.setText("@" + profileData.userName);
            tv_usernameBlock.setText("@" + profileData.userName);
            tv_profile_followers.setText(profileData.followersCount);
            tv_profile_followers_txt.setText(Constant.adds(Integer.parseInt(profileData.followersCount),"Follower"));
            tv_profile_following.setText(profileData.followingCount);
            tv_profile_post.setText(profileData.postCount);
            tv_profile_post_txt.setText(Constant.adds(Integer.parseInt(profileData.postCount),"Post"));
            tvRatingCount.setText("(" + profileData.reviewCount + ")");
            tvRatingCountBlock.setText("(" + profileData.reviewCount + ")");
            float ratingCount = Float.parseFloat(profileData.ratingCount);
            ratingBlock.setRating(Float.parseFloat(profileData.ratingCount));
            rating.setRating(ratingCount);

            if (!profileData.profileImage.isEmpty()) {
                Picasso.with(mContext).load(profileData.profileImage).
                        placeholder(R.drawable.default_placeholder).fit().into(iv_Profile);

                Picasso.with(mContext).load(profileData.profileImage).placeholder(R.drawable.default_placeholder).
                        fit().into(user_image);
            }
        }

        iv_Profile.setOnClickListener(v -> {
            if (userId.equals(String.valueOf(user.id))) {
                getPermissionAndPicImage();
            } else user_image.callOnClick();
        });


    }

    private void updateViewType(int id) {
        tvVideos.setTextColor(getResources().getColor(R.color.text_color));
        tvImages.setTextColor(getResources().getColor(R.color.text_color));
        tvFeeds.setTextColor(getResources().getColor(R.color.text_color));
        endlesScrollListener.resetState();
        int prevSize = feeds.size();
        switch (id) {
            case R.id.ly_feeds:
                //addRemoveHeader(true);
                tvFeeds.setTextColor(getResources().getColor(R.color.colorPrimary));

                if (lastFeedTypeId != R.id.ly_feeds) {
                    page = 0;
                    feeds.clear();
                    endlesScrollListener.resetState();
                    feedType = "";
                    CURRENT_FEED_STATE = Constant.FEED_STATE;
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                    apiForGetAllFeeds(0, 20, true, "");
                }
                break;

            case R.id.ly_images:
                tvImages.setTextColor(getResources().getColor(R.color.colorPrimary));
                // addRemoveHeader(false);
                if (lastFeedTypeId != R.id.ly_images) {
                    page = 0;
                    feeds.clear();
                    endlesScrollListener.resetState();
                    feedType = "image";
                    CURRENT_FEED_STATE = Constant.IMAGE_STATE;
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                    apiForGetAllFeeds(0, 20, true, "");
                }

                break;

            case R.id.ly_videos:
                tvVideos.setTextColor(getResources().getColor(R.color.colorPrimary));
                // addRemoveHeader(false);
                if (lastFeedTypeId != R.id.ly_videos) {
                    page = 0;
                    feeds.clear();
                    endlesScrollListener.resetState();
                    feedType = "video";
                    CURRENT_FEED_STATE = Constant.VIDEO_STATE;
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                    apiForGetAllFeeds(0, 20, true, "");
                }
                break;
        }

        lastFeedTypeId = id;
    }

    @Override
    public void onCommentBtnClick(Feeds feed, int pos) {
        Intent intent = new Intent(mContext, CommentsActivity.class);
        intent.putExtra("feed_id", feed._id);
        intent.putExtra("feedPosition", pos);
        intent.putExtra("feed", feed);
        intent.putExtra("commentCount", feed.commentCount);
        startActivityForResult(intent, Constant.ACTIVITY_COMMENT);
    }

    @Override
    public void onLikeListClick(Feeds feed) {
        //addFragment(LikeFragment.newInstance(feed._id, user.id), true);

        Intent intent = new Intent(mContext, LikeFeedActivity.class);
        intent.putExtra("feedId", feed._id);
        intent.putExtra("userId", user.id);
        startActivity(intent);


    }

    @Override
    public void onFeedClick(Feeds feed, int index, View v) {
        /*Intent intent = new Intent(mContext, FeedSingleActivity.class);
        intent.putExtra("feedId",String.valueOf(feed._id));
        startActivity(intent);*/
     /*   ArrayList<String> tempList = new ArrayList<>();
        for (int i = 0; i < feed.feedData.size(); i++) {
            tempList.add(feed.feedData.get(i).feedPost);
        }

        Intent intent = new Intent(mContext, PreviewImageActivity.class);
        intent.putExtra("imageArray", tempList);
        intent.putExtra("startIndex", index);
        startActivity(intent);*/
        //publicationQuickView(feed, index);
        //showLargeImage(feed, index);
    }

    @Override
    public void onClickProfileImage(Feeds feed, ImageView v) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       // super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            page = 0;
            apiForGetProfile(getView());
            apiForGetAllFeeds(page, 20, true, "");
        } else if (data != null) {
            if (requestCode == Constant.ACTIVITY_COMMENT) {
                if (CURRENT_FEED_STATE == Constant.FEED_STATE) {
                    int pos = data.getIntExtra("feedPosition", 0);
                    Feeds feed = (Feeds) data.getSerializableExtra("feed");
                    // feeds.get(pos).commentCount = feed.commentCount;
                    feeds.get(pos).commentCount = feed.commentCount;
                    feedAdapter.notifyItemChanged(pos);
                }
            }
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == 234) {
                //Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                Uri imageUri = ImagePicker.getImageURIFromResult(mContext, requestCode, resultCode, data);
                if (imageUri != null) {
                    CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE).setAspectRatio(400, 400).start(mContext,UserProfile.this);
                } else {
                    MyToast.getInstance(mContext).showDasuAlert(getString(R.string.msg_some_thing_went_wrong));
                }

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                try {
                    if (result != null)
                        profileImageBitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), result.getUri());
                    assert result != null;

                    if (profileImageBitmap != null) {
                        iv_Profile.setImageBitmap(profileImageBitmap);
                        user_image.setImageBitmap(profileImageBitmap);
                        callEditProfileApi();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == Constant.REQUEST_CHECK_ISLASTINVITAION) {
            if (data != null)
                if (data.getBooleanExtra("noInvitation", false) == true) {
                    navigationItems.clear();
                    isInvitation = false;

                    //listAdapter.getisInvitation(isInvitation);
                    addItems();
                    //listAdapter.notifyDataSetChanged();
                }
        }
    }

    /* frangment replace code */
    public void addFragment(Fragment fragment, boolean addToBackStack) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_in, 0, 0);
            transaction.add(R.id.container, fragment, backStackName);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }

    }

    private Dialog builder;

    public void hideQuickView() {
        if (builder != null) builder.dismiss();
    }

    @Override
    public void onClick(View view) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 800) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (view.getId()) {
            case R.id.ly_feeds:
            case R.id.ly_images:
            case R.id.ly_videos:
                updateViewType(view.getId());
                break;

            case R.id.btnBack:

                break;

            case R.id.btnFollow:
                int followersCount = Integer.parseInt(profileData.followersCount);

                if (profileData.followerStatus.equals("1")) {
                    profileData.followerStatus = "0";
                    btnFollow.setText("Follow");
                    followersCount--;
                } else {
                    profileData.followerStatus = "1";
                    btnFollow.setText("Unfollow");
                    followersCount++;
                }
                profileData.followersCount = String.valueOf(followersCount);
                tv_profile_followers.setText("" + followersCount);
                apiForGetFollowUnFollow();
                break;

            case R.id.btnNevMenu:
                if (drawer.isDrawerOpen(GravityCompat.END)) {
                    drawer.closeDrawer(GravityCompat.END);
                } else {
                    drawer.openDrawer(GravityCompat.END);
                }
                break;

            case R.id.llRating:
                Intent intent = new Intent(mContext, ReviewRatingActivity.class);
                intent.putExtra("id", userId);
                intent.putExtra("userType", "user");
                startActivity(intent);
                break;

            case R.id.ly_main:
            case R.id.rlContent:
                if (ed_search.getText().toString().trim().equals("")) {
                    ed_search.setVisibility(View.GONE);
                }

                MyClickOnPostMenu.getMentIntance().setMenuClick();
                break;

          /*  case R.id.llAboutUs:
                MyToast.getInstance(mContext).showDasuAlert("Under development");
                break;
*/
            case R.id.btnMsg:
                Intent chat_intent = new Intent(mContext, ChatActivity.class);
                if (profileData != null) {
                    chat_intent.putExtra("opponentChatId", profileData._id);
                    startActivity(chat_intent);
                }
                break;

            case R.id.llFollowing:
               /* Intent intent1 = new Intent(mContext,FollowersActivity.class);
                intent1.putExtra("isFollowers",false);
                intent1.putExtra("artistId",user.id);
                startActivityForResult(intent1,10);*/
                Bundle bundle1 = new Bundle();
                bundle1.putBoolean("isFollowers", false);
                bundle1.putString("artistId", String.valueOf(user.id));
                Intent intent1 = new Intent(mContext, FollowersActivity.class);
                intent1.putExtras(bundle1);
                startActivityForResult(intent1, 10);
                break;

            case R.id.llFollowers:
                Bundle bundle2 = new Bundle();
                bundle2.putBoolean("isFollowers", true);
                bundle2.putString("artistId", String.valueOf(user.id));
                Intent intent2 = new Intent(mContext, FollowersActivity.class);
                intent2.putExtras(bundle2);
                startActivityForResult(intent2, 10);
                //startActivity(new Intent(mContext,FollowersActivity.class));
                break;

            case R.id.llPost:
                updateViewType(R.id.ly_feeds);
                break;

            case R.id.ll_filter:
               /* int[] location = new int[2];
                // Get the x, y location and store it in the location[] array
                // location[0] = x, location[1] = y.
                ivFilter.getLocationOnScreen(location);

                //Initialize the Point with x, and y positions
                Point p = new Point();
                p.x = location[0];
                p.y = location[1];
                arrayList.clear();

                if (!isMenuOpen) {
                    initiatePopupWindow(p);
                } else {
                    isMenuOpen = false;
                    popupWindow.dismiss();
                }*/

                NameDisplayDialog.newInstance(getString(R.string.post_type), arrayList, pos -> {
                    String data = arrayList.get(pos);
                    int prevSize = feeds.size();
                    switch (data) {
                        case "All":

                            page = 0;
                            tvFilter.setText(R.string.all);
                            /// ed_search.setText("");
                            feeds.clear();
                            endlesScrollListener.resetState();
                            feedType = "";
                            CURRENT_FEED_STATE = Constant.FEED_STATE;
                            feedAdapter.notifyItemRangeRemoved(0, prevSize);
                            apiForGetAllFeeds(page, 20, true, "");

                            //popupWindow.dismiss();
                            break;

                        case "Photo":
                            endlesScrollListener.resetState();
                            page = 0;
                            tvFilter.setText(R.string.photo);
                            //  ed_search.setText("");
                            feeds.clear();

                            feedType = "image";
                            CURRENT_FEED_STATE = Constant.IMAGE_STATE;
                            feedAdapter.notifyItemRangeRemoved(0, prevSize);
                            apiForGetAllFeeds(page, 20, true, "");
                            // popupWindow.dismiss();
                            break;

                        case "Video":
                            endlesScrollListener.resetState();
                            page = 0;
                            tvFilter.setText(R.string.video);
                            // ed_search.setText("");
                            // popupWindow.dismiss();
                            feeds.clear();
                            feedType = "video";
                            CURRENT_FEED_STATE = Constant.VIDEO_STATE;
                            feedAdapter.notifyItemRangeRemoved(0, prevSize);
                            apiForGetAllFeeds(page, 20, true, "");
                            break;

                    }
                }).show(getFragmentManager());


                break;


        }
    }

    private void addItems() {
        navigationItems.clear();
        NavigationItem item;

        if (!isInvitation) {
            for (int i = 0; i < 9; i++) {
                item = new NavigationItem();
                switch (i) {
                    case 0:
                        item.itemName = getString(R.string.edit_profile);
                        item.itemImg = R.drawable.profile_ico;

                        break;
                    case 1:
                        item.itemName = getString(R.string.inbox);
                        item.itemImg = R.drawable.chat_ico;

                        break;

                    case 2:
                        item.itemName = getString(R.string.title_booking);
                        item.itemImg = R.drawable.booking_ico;
                        break;

                    case 3:
                        item.itemName = getString(R.string.payment_info);
                        item.itemImg = R.drawable.payment_history_ico;
                        break;

                    case 4:
                        item.itemName = getString(R.string.rate_this_app);
                        item.itemImg = R.drawable.rating_star_ico;
                        break;

                    case 5:
                        item.itemName = getString(R.string.my_foldera);
                        item.itemImg = R.drawable.folder_ico;
                        break;

                    case 6:
                        item.itemName = getString(R.string.setting);
                        item.itemImg = R.drawable.settings_ico;
                        break;

                    case 7:
                        item.itemName = getString(R.string.about_us);
                        item.itemImg = R.drawable.slider_about_us_ico;
                        break;

                    case 8:
                        item.itemName = getString(R.string.log_out);
                        item.itemImg = R.drawable.logout_ico;
                        break;

                }
                navigationItems.add(item);
            }

        } else {
            for (int i = 0; i < 10; i++) {
                item = new NavigationItem();
                switch (i) {
                    case 0:
                        item.itemName = getString(R.string.edit_profile);
                        item.itemImg = R.drawable.profile_ico;

                        break;
                    case 1:
                        item.itemName = getString(R.string.inbox);
                        item.itemImg = R.drawable.chat_ico;

                        break;

                    case 2:
                        item.itemName = getString(R.string.title_booking);
                        item.itemImg = R.drawable.booking_ico;
                        break;

                    case 3:
                        item.itemName = getString(R.string.payment_info);
                        item.itemImg = R.drawable.payment_history_ico;
                        break;

                    case 4:
                        item.itemName = getString(R.string.business_invitation);
                        item.itemImg = R.drawable.id_card_ico;
                        break;

                    case 5:
                        item.itemName = getString(R.string.rate_this_app);
                        item.itemImg = R.drawable.rating_star_ico;
                        break;

                    case 6:
                        item.itemName = getString(R.string.my_foldera);
                        item.itemImg = R.drawable.folder_ico;
                        break;


                    case 7:
                        item.itemName = getString(R.string.setting);
                        item.itemImg = R.drawable.settings_ico;
                        break;

                    case 8:
                        item.itemName = getString(R.string.about_us);
                        item.itemImg = R.drawable.slider_about_us_ico;
                        break;

                    case 9:
                        item.itemName = getString(R.string.log_out);
                        item.itemImg = R.drawable.logout_ico;
                        break;

                }
                navigationItems.add(item);
            }
        }


    }

    boolean isShow = false;

    @Override
    public void OnClick(int pos) {
        //apifortokenUpdate();
        MyToast.getInstance(mContext).showDasuAlert(getString(R.string.under_development));
    }

    private void apiForGetFollowUnFollow() {
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetFollowUnFollow();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));
        //  params.put("followerId", String.valueOf(user.id));
        params.put("followerId", userId);
        // params.put("loginUserId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "followFollowing", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {

                    } else {
                        MyToast.getInstance(mContext).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(mContext);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try {
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")) {
                        Mualab.getInstance().getSessionManager().logout();
                        // MyToast.getInstance(BookingActivity.this).showDasuAlert(helper.error_Messages(error));
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

        task.execute(this.getClass().getName());
    }

    private InstaTag.TaggedImageEvent taggedImageEvent = new InstaTag.TaggedImageEvent() {

        @Override
        public void singleTapConfirmedAndRootIsInTouch(float x, float y) {

        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (longPressListner != null)
                longPressListner.onLongPress();
        }

        @Override
        public void onSinglePress(MotionEvent e) {
        }
    };

    // Update profile image
    public void getPermissionAndPicImage() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (mContext.checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);
            } else {
                ImagePicker.pickImage(this);
            }
        } else {
            ImagePicker.pickImage(this);
        }
    }

    public void callEditProfileApi() {
        Progress.show(mContext);

        //progress_bar.setVisibility(View.VISIBLE);
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        callEditProfileApi();
                    }

                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(Mualab.currentUser.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "profileUpdate", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    //progress_bar.setVisibility(View.GONE);
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    Progress.hide(mContext);
                    if (status.equalsIgnoreCase("success")) {
                        Gson gson = new Gson();
                        JSONObject userObj = js.getJSONObject("users");
                        User newuser = gson.fromJson(String.valueOf(userObj), User.class);
                        Session session = new Session(mContext);
                        session.createSession(newuser);
                        writeNewUser(newuser);
                        Mualab.currentUser = newuser;

                        MyToast.getInstance(mContext).showDasuAlert("Profile picture updated successfully");
                    } else {
                    }

                } catch (Exception e) {
                    Progress.hide(mContext);
                    e.printStackTrace();

                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(mContext);
            }
        })
                .setAuthToken(Mualab.currentUser.authToken)
                .setParam(params)
                .setProgress(true));
        task.postImage("profileImage", profileImageBitmap);

    }

    private void writeNewUser(User user) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = new FirebaseUser();
        firebaseUser.firebaseToken = FirebaseInstanceId.getInstance().getToken();
        ;
        firebaseUser.isOnline = 1;
        firebaseUser.lastActivity = ServerValue.TIMESTAMP;
        if (user.profileImage.isEmpty())
            firebaseUser.profilePic = "http://koobi.co.uk:3000/uploads/default_user.png";
        else
            firebaseUser.profilePic = user.profileImage;

        firebaseUser.userName = user.userName;
        firebaseUser.uId = user.id;
        firebaseUser.authToken = user.authToken;
        firebaseUser.userType = user.userType;
        firebaseUser.banAdmin = 0;
        /*User user1 = Mualab.getInstance().getSessionManager().getUser();*/
        mDatabase.child("users").child(String.valueOf(user.id)).setValue(firebaseUser);

    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String notificationType = intent.getStringExtra("notificationType");
            String notifyId = intent.getStringExtra("notifyId");//userId
            String title = intent.getStringExtra("title");

            if (notificationType.equals("17")) {// open Popupcase
                apiForGetProfile(getView());
            }

        }
    };

    /*...................................................................*/

    private void apiForGetAllFeeds(final int page, final int feedLimit, final boolean isEnableProgress, String searchText) {

        setFeedLoading(isEnableProgress);

        Map<String, String> params = new HashMap<>();
        params.put("feedType", feedType);
        params.put("page", String.valueOf(page));
        params.put("limit", String.valueOf(feedLimit));
        params.put("type", "");
        params.put("userId", userId);
        params.put("loginUserId", String.valueOf(user.id));
        params.put("viewBy", "");
        params.put("search", searchText);


        Mualab.getInstance().cancelPendingRequests(this.getClass().getName());
        new HttpTask(new HttpTask.Builder(mContext,
                "profileFeed", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                setFeedLoading(false);
                setAdapterLoading(false);
                mRefreshLayout.stopRefresh(false, 500);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
//String message = js.getString("message");


                    if (status.equalsIgnoreCase("success")) {
//removeProgress();
                        ParseAndUpdateUI(page, response);

                    } else  {
//rvFeed.setVisibility(View.GONE);
                        if(feeds.size() == 0){
                            tv_no_data_msg.setVisibility(View.VISIBLE);
                        }else  tv_no_data_msg.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
// MyToast.getInstance(mContext).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                setFeedLoading(false);
                setAdapterLoading(false);
                if(isPulltoRefrash){
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(false, 500);
                    int prevSize = feeds.size();
                    feeds.clear();
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                }

                try {
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")) {
                        Mualab.getInstance().getSessionManager().logout();
//MyToast.getInstance(BookingActivity.this).showSmallCustomToast(helper.error_Messages(error));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//MyToast.getInstance(mContext).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
            }})
                .setAuthToken(user.authToken)
                .setParam(params)
                .setMethod(Request.Method.POST)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute("profileFeed");
    }

    private void ParseAndUpdateUI(int page, final String response) {
        try {
            JSONObject js = new JSONObject(response);
            String status = js.getString("status");
// String message = js.getString("message");

            if (status.equalsIgnoreCase("success")) {
//rvFeed.setVisibility(View.VISIBLE);
                JSONArray array = js.getJSONArray("AllFeeds");
                if (isPulltoRefrash) {
                    isPulltoRefrash = false;
//mRefreshLayout.stopRefresh(true, 500);
                    int prevSize = feeds.size();
                    feeds.clear();
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                }

                Gson gson = new Gson();
                if (array.length() != 0) {

                    if (page == 0) {
                        feeds.clear();
                    }

/*if (isGrideView) {
feeds.clear();
}*/
                    int feedNewSize = 0;
                    int feedOldSize = feeds.size() + 1;
                    for (int i = 0; i < array.length(); i++) {
                        try {
                            JSONObject jsonObject = array.getJSONObject(i);
                            Feeds feed = gson.fromJson(String.valueOf(jsonObject), Feeds.class);
// feed.taggedImgMap = new HashMap<>();
                            /*tmp get data and set into actual json format*/
                            if (feed.userInfo != null && feed.userInfo.size() > 0) {
                                Feeds.User user = feed.userInfo.get(0);
                                feed.userName = user.userName;
                                feed.fullName = user.firstName + " " + user.lastName;
                                feed.profileImage = user.profileImage;
                                feed.userId = user._id;
                                feed.crd = feed.timeElapsed;
                            }

                            if (feed.feedData != null && feed.feedData.size() > 0) {

                                feed.feed = new ArrayList<>();
                                feed.feedThumb = new ArrayList<>();

                                for (Feeds.Feed tmp : feed.feedData) {
                                    feed.feed.add(tmp.feedPost);
                                    if (!TextUtils.isEmpty(feed.feedData.get(0).videoThumb))
                                        feed.feedThumb.add(tmp.feedPost);
                                }

                                if (feed.feedType.equals("video"))
                                    feed.videoThumbnail = feed.feedData.get(0).videoThumb;
                            }

                            JSONArray jsonArray = jsonObject.getJSONArray("peopleTag");
                            if (jsonArray.length() != 0) {

                                for (int j = 0; j < jsonArray.length(); j++) {

                                    feed.peopleTagList = new ArrayList<>();
                                    JSONArray arrayJSONArray = jsonArray.getJSONArray(j);

                                    for (int k = 0; k < arrayJSONArray.length(); k++) {
                                        JSONObject object = arrayJSONArray.getJSONObject(k);

// HashMap<String, TagDetail> tagDetails = new HashMap<>();

                                        String unique_tag_id = object.getString("unique_tag_id");
                                        float x_axis = Float.parseFloat(object.getString("x_axis"));
                                        float y_axis = Float.parseFloat(object.getString("y_axis"));

                                        JSONObject tagOjb = object.getJSONObject("tagDetails");
                                        TagDetail tag;
                                        if (tagOjb.has("tabType")) {
                                            tag = gson.fromJson(String.valueOf(tagOjb), TagDetail.class);
                                        } else {
                                            JSONObject details = tagOjb.getJSONObject(unique_tag_id);
                                            tag = gson.fromJson(String.valueOf(details), TagDetail.class);
                                        }
//tagDetails.put(tag.title, tag);
                                        TagToBeTagged tagged = new TagToBeTagged();
                                        tagged.setUnique_tag_id(unique_tag_id);
                                        tagged.setX_co_ord(x_axis);
                                        tagged.setY_co_ord(y_axis);
//tagged.setTagDetails(tagDetails);
                                        tagged.setTagDetails(tag);

                                        feed.peopleTagList.add(tagged);
                                    }
                                    feed.taggedImgMap.put(j, feed.peopleTagList);
                                }
                            }

                            if (jsonObject.has("serviceTag")) {
                                JSONArray serviceTagArray = jsonObject.getJSONArray("serviceTag");
                                if (serviceTagArray.length() != 0) {

                                    for (int j = 0; j < serviceTagArray.length(); j++) {

                                        feed.serviceTagList = new ArrayList<>();
                                        JSONArray arrayJSONArray = serviceTagArray.getJSONArray(j);

                                        for (int k = 0; k < arrayJSONArray.length(); k++) {
                                            JSONObject object = arrayJSONArray.getJSONObject(k);

//HashMap<String, TagDetail> tagDetails = new HashMap<>();

                                            String unique_tag_id = object.getString("unique_tag_id");
                                            float x_axis = Float.parseFloat(object.getString("x_axis"));
                                            float y_axis = Float.parseFloat(object.getString("y_axis"));

                                            JSONObject tagOjb = object.getJSONObject("tagDetails");
                                            TagDetail tag;
                                            if (tagOjb.has("tabType")) {
                                                tag = gson.fromJson(String.valueOf(tagOjb), TagDetail.class);
                                            } else {
                                                JSONObject details = tagOjb.getJSONObject(unique_tag_id);
                                                tag = gson.fromJson(String.valueOf(details), TagDetail.class);
                                            }
//tagDetails.put(tag.title, tag);
                                            TagToBeTagged tagged = new TagToBeTagged();
                                            tagged.setUnique_tag_id(unique_tag_id);
                                            tagged.setX_co_ord(x_axis);
                                            tagged.setY_co_ord(y_axis);
//tagged.setTagDetails(tagDetails);
                                            tagged.setTagDetails(tag);

                                            feed.serviceTagList.add(tagged);
                                        }
                                        feed.serviceTaggedImgMap.put(j, feed.serviceTagList);
                                    }
                                }
                            }

                            if (isGrideView) {
                                if (!feed.feedType.equals("text")) {
                                    ++feedNewSize;
                                    feeds.add(feed);
                                }
                            } else {
                                ++feedNewSize;
                                feeds.add(feed);
                            }


                        } catch (JsonParseException e) {
                            setFeedLoading(false);
                            tv_no_data_msg.setVisibility(View.VISIBLE);
                            tv_no_data_msg.setText("Something went wrong!");
                            e.printStackTrace();
                        }
                    }// loop end.

                    feedAdapter.notifyItemRangeInserted(feedOldSize, feedNewSize);
                } else if (feeds.size() == 0) {
//rvFeed.setVisibility(View.GONE);
                    feedAdapter.notifyDataSetChanged();
                }

/* if (isGrideView) {
rvFeed.setLayoutManager(new GridLayoutManager(getActivity(), 3));
feedAdapter.isGrideView(true);
endlesScrollListener.resetState();
} else {
rvFeed.setLayoutManager(new LinearLayoutManager(getActivity()));
feedAdapter.isGrideView(false);
endlesScrollListener.resetState();
}*/

            } else if (status.equals("fail") && feeds.size() == 0) {
// rvFeed.setVisibility(View.GONE);
                tv_msg.setVisibility(View.VISIBLE);

                if (isPulltoRefrash) {
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(false, 500);

                }
                feedAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
            feedAdapter.notifyDataSetChanged();
//MyToast.getInstance(mContext).showSmallCustomToast(getString(R.string.alert_something_wenjt_wrong));
        }

        if(feeds.size() == 0){
            tv_no_data_msg.setVisibility(View.VISIBLE);
        }else  tv_no_data_msg.setVisibility(View.GONE);

    }

    private void setFeedLoading(boolean isLoad) {
        if (ll_progress != null)
            ll_progress.setVisibility(isLoad ? View.VISIBLE : View.GONE);

    }

    @Override
    public void gridClick(List<Feeds> feedItems, int position) {
        addFragmentProfile(GrideToListFragment.newInstance(feedItems,position,true), true);
    }

    public void addFragmentProfile(Fragment fragment, boolean addToBackStack) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fragmentManager = getFragmentManager();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_in,0,0);
           /* transaction.setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right,
                    R.anim.slide_in_from_right, R.anim.slide_out_to_left);*/
            transaction.add(R.id.drawer_layout, fragment, backStackName);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }
}

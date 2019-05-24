package com.mualab.org.user.activity.artist_profile.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
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
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mualab.org.user.R;
import com.mualab.org.user.Views.refreshviews.CircleHeaderView;
import com.mualab.org.user.Views.refreshviews.OnRefreshListener;
import com.mualab.org.user.Views.refreshviews.RjRefreshLayout;
import com.mualab.org.user.activity.artist_profile.adapter.ArtistFeedAdapter;
import com.mualab.org.user.activity.artist_profile.fragments.ProfileComboFragment1;
import com.mualab.org.user.activity.artist_profile.fragments.ProfileComboFragment2;
import com.mualab.org.user.activity.artist_profile.model.UserProfileData;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.dialogs.NameDisplayDialog;
import com.mualab.org.user.activity.feeds.activity.CommentsActivity;
import com.mualab.org.user.activity.feeds.activity.FeedSingleActivity;
import com.mualab.org.user.activity.feeds.activity.PreviewImageActivity;
import com.mualab.org.user.activity.feeds.adapter.ViewPagerAdapter;
import com.mualab.org.user.activity.feeds.fragment.LikeFragment;
import com.mualab.org.user.activity.review_rating.ReviewRatingActivity;
import com.mualab.org.user.activity.tag_module.instatag.InstaTag;
import com.mualab.org.user.activity.tag_module.instatag.TagDetail;
import com.mualab.org.user.activity.tag_module.instatag.TagToBeTagged;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.ChatActivity;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.listener.RecyclerViewScrollListenerProfile;
import com.mualab.org.user.menu.MenuAdapter;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.WrapContentLinearLayoutManager;
import com.mualab.org.user.utils.constants.Constant;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ArtistProfileActivity extends AppCompatActivity implements View.OnClickListener, ArtistFeedAdapter.Listener {
    public String artistId;
    private String TAG = this.getClass().getName();
    private User user;
    private TextView tvImages, tvVideos, tvFeeds, tv_msg, tv_no_data_msg;
    private ImageView ivActive, ivFav;
    private LinearLayout lowerLayout1, lowerLayout2, ll_progress, llRating;
    private RecyclerView rvFeed;
    private RjRefreshLayout mRefreshLayout;
    private RecyclerViewScrollListenerProfile endlesScrollListener;
    private int CURRENT_FEED_STATE = 0, lastFeedTypeId;
    private String feedType = "";
    private ArtistFeedAdapter feedAdapter;
    private List<Feeds> feeds;
    private boolean isPulltoRefrash = false;
    private long mLastClickTime = 0;
    public UserProfileData profileData = null;
    private ArtistsSearchBoard item;
    private AppCompatButton btnFollow;
    private ViewPagerAdapter.LongPressListner longPressListner;
    private EditText ed_search;
    private LinearLayout ll_filter;
    private ImageView ivFilter;
    private boolean isMenuOpen;
    private TextView tvFilter;
    private ArrayList<String> arrayList = new ArrayList<>();
    private PopupWindow popupWindow;
    private ImageView iv_search_icon;
    private ImageView iv_gride_view, iv_list_view;
    private boolean isGrideView;
    private CollapsingToolbarLayout ly_main;
    private RelativeLayout ly_main_layout;
    private int page = 0;
    private LinearLayout llDots;
    private DatabaseReference mBlockUserWeb,mBlockUser;
    private String nodeForBlockUser = "";
    private int myId ;
    private String blockedId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_profile);
        Intent i = getIntent();
        //  item = (ArtistsSearchBoard) i.getSerializableExtra("item");
        //  artistId =  item._id;
        artistId = i.getStringExtra("artistId");
        mBlockUserWeb = FirebaseDatabase.getInstance().getReference().child("blockUserArtistWeb");
        mBlockUser = FirebaseDatabase.getInstance().getReference().child("blockUserArtist");

        myId = Mualab.currentUser.id;

        if(Integer.parseInt(artistId) < myId){
            nodeForBlockUser = artistId + "_" + myId;
        }else nodeForBlockUser = myId + "_" + artistId;

        mBlockUser.child(nodeForBlockUser).child("blockedBy").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    try{
                        blockedId = dataSnapshot.getValue(String.class);

                        if(blockedId.equals(artistId)){// case of hide screen

                        }
                    }catch (Exception e){

                    }

                }else {
                    blockedId = "";
                    // case of show screen
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        init();
    }

    private void init() {
        Session session = Mualab.getInstance().getSessionManager();
        user = session.getUser();
        feeds = new ArrayList<>();
        item = new ArtistsSearchBoard();

        tvImages = findViewById(R.id.tv_image);
        tvVideos = findViewById(R.id.tv_videos);
        tvFeeds = findViewById(R.id.tv_feed);
        ed_search = findViewById(R.id.ed_search);
        iv_search_icon = findViewById(R.id.iv_search_icon);
        ly_main = findViewById(R.id.ly_main);
        ly_main_layout = findViewById(R.id.ly_main_layout);
        llDots = findViewById(R.id.llDots);
        llDots.setVisibility(View.VISIBLE);

        btnFollow = findViewById(R.id.btnFollow);
        AppCompatButton btnBook = findViewById(R.id.btnBook);
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView ivChat2 = findViewById(R.id.ivChat2);
        ivChat2.setVisibility(View.GONE);
        ivFav = findViewById(R.id.ivFav);
        ivFilter = findViewById(R.id.ivFilter);
        ivFav.setVisibility(View.VISIBLE);
        ImageView ivUserProfile = findViewById(R.id.ivUserProfile);

        rvFeed = findViewById(R.id.rvFeed);
        LinearLayout lyImage = findViewById(R.id.ly_images);
        LinearLayout lyVideos = findViewById(R.id.ly_videos);
        LinearLayout lyFeed = findViewById(R.id.ly_feeds);

        lowerLayout2 = findViewById(R.id.lowerLayout2);
        lowerLayout1 = findViewById(R.id.lowerLayout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        tvFilter = toolbar.findViewById(R.id.tvFilter);
        iv_gride_view = toolbar.findViewById(R.id.iv_gride_view);
        iv_list_view = toolbar.findViewById(R.id.iv_list_view);

        tv_msg = findViewById(R.id.tv_msg);
        tv_no_data_msg = findViewById(R.id.tv_no_data_msg);
        ll_progress = findViewById(R.id.ll_progress);
        llRating = findViewById(R.id.llRating);

        WrapContentLinearLayoutManager lm = new WrapContentLinearLayoutManager(ArtistProfileActivity.this, LinearLayoutManager.VERTICAL, false);
        rvFeed.setItemAnimator(null);
        rvFeed.setLayoutManager(lm);
        rvFeed.setHasFixedSize(true);

        feedAdapter = new ArtistFeedAdapter(ArtistProfileActivity.this, feeds, this);

        endlesScrollListener = new RecyclerViewScrollListenerProfile() {
            @Override
            public void onLoadMore() {
                if (feedAdapter != null) {
                    ++page;
                    setAdapterLoading(true);
                    apiForGetAllFeeds(page, 20, false, "");
                }
            }
        };

        rvFeed.setAdapter(feedAdapter);
        rvFeed.addOnScrollListener(endlesScrollListener);

        mRefreshLayout = findViewById(R.id.mSwipeRefreshLayout);
        final CircleHeaderView header = new CircleHeaderView(ArtistProfileActivity.this);
        mRefreshLayout.addHeader(header);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                endlesScrollListener.resetState();
                isPulltoRefrash = true;
                apiForGetAllFeeds(0, 20, false, "");
            }

            @Override
            public void onLoadMore() {
                Log.e(TAG, "onLoadMore: ");
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
            public void onTouchEvent(RecyclerView rv, MotionEvent event) {
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

        iv_gride_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page = 0;
                feeds.clear();
                isGrideView = true;
                ed_search.setText("");
                rvFeed.setLayoutManager(new GridLayoutManager(ArtistProfileActivity.this, 3));
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
                rvFeed.setLayoutManager(new LinearLayoutManager(ArtistProfileActivity.this));
                feedAdapter.isGrideView(false);
                endlesScrollListener.resetState();

                apiForGetAllFeeds(page, 20, true, "");//last limit 200
                iv_gride_view.setImageResource(R.drawable.inactive_grid_icon);
                iv_list_view.setImageResource(R.drawable.active_list);

            }
        });


        ll_filter = findViewById(R.id.ll_filter);
        ll_filter.setOnClickListener(this);


        lyImage.setOnClickListener(this);
        lyVideos.setOnClickListener(this);
        lyFeed.setOnClickListener(this);

        btnBack.setOnClickListener(this);
        btnFollow.setOnClickListener(this);
        btnBook.setOnClickListener(this);
        ivUserProfile.setOnClickListener(this);
        ivChat2.setOnClickListener(this);
        ivFav.setOnClickListener(this);
        ly_main.setOnClickListener(this);
        ly_main_layout.setOnClickListener(this);
        llRating.setOnClickListener(this);
        llDots.setOnClickListener(this);


        apiForGetProfile();

    }

    private void setAdapterLoading(boolean isLoad) {
        if (feedAdapter != null) {
            feedAdapter.showLoading(isLoad);
            feedAdapter.notifyDataSetChanged();
        }
    }

    private void initProfilePager() {
        final LinearLayout ll_Dot = findViewById(R.id.ll_Dot);
        ViewPager profileItemPager = findViewById(R.id.profileItemPager);
        ViewPagerAdapterSwip viewPagerAdapter = new ViewPagerAdapterSwip(getSupportFragmentManager());
        viewPagerAdapter.addFragment(ProfileComboFragment1.newInstance(profileData));
        viewPagerAdapter.addFragment(ProfileComboFragment2.newInstance(profileData));
        profileItemPager.setOffscreenPageLimit(2);
        profileItemPager.setAdapter(viewPagerAdapter);

        addBottomDots(ll_Dot, 2, 0);
        profileItemPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int pos, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int pos) {
                addBottomDots(ll_Dot, 2, pos);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void addBottomDots(LinearLayout ll_dots, int totalSize, int currentPage) {
        TextView tvDot1 = (TextView) ll_dots.getChildAt(0);
        TextView tvDot2 = (TextView) ll_dots.getChildAt(1);
        switch (currentPage) {
            case 0:
                tvDot1.setBackgroundResource(R.drawable.black_circle);
                tvDot2.setBackgroundResource(R.drawable.bg_blank_black_circle);
                break;

            case 1:
                tvDot1.setBackgroundResource(R.drawable.bg_blank_black_circle);
                tvDot2.setBackgroundResource(R.drawable.black_circle);
                break;
        }
    }

    class ViewPagerAdapterSwip extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        ViewPagerAdapterSwip(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }

    }

    private void apiForGetProfile() {

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ArtistProfileActivity.this, new NoConnectionDialog.Listner() {
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
        params.put("userId", artistId);
        params.put("loginUserId", String.valueOf(user.id));
        params.put("viewBy", "user");

        HttpTask task = new HttpTask(new HttpTask.Builder(ArtistProfileActivity.this, "getProfile", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    if (feeds != null && feeds.size() == 0)
                        updateViewType(R.id.ly_feeds);

                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    Progress.hide(ArtistProfileActivity.this);
                    if (status.equalsIgnoreCase("success")) {
                        JSONArray userDetail = js.getJSONArray("userDetail");
                        JSONObject object = userDetail.getJSONObject(0);
                        Gson gson = new Gson();

                        profileData = gson.fromJson(String.valueOf(object), UserProfileData.class);
                        item.businessType = profileData.businessType;
                        //   profileData = gson.fromJson(response, UserProfileData.class);
                        setProfileData(profileData);
                        initProfilePager();
                        // updateViewType(profileData,R.id.ly_videos);

                    } else {
                        MyToast.getInstance(ArtistProfileActivity.this).showDasuAlert(message);
                    }
                    updateViewType(R.id.ly_feeds);

                } catch (Exception e) {
                    Progress.hide(ArtistProfileActivity.this);
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

    @Override
    public void onClick(View view) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
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
                onBackPressed();
                break;

            case R.id.ivFav:
                if (profileData != null) {
                    if (profileData.favoriteStatus.equals("1")) {
                        profileData.favoriteStatus = "0";
                        ivFav.setImageDrawable(getResources().getDrawable(R.drawable.inactive_star_co));
                    } else {
                        profileData.favoriteStatus = "1";
                        ivFav.setImageDrawable(getResources().getDrawable(R.drawable.active_star_icon));
                    }
                    apiForFavourite();
                }

                break;

            case R.id.btnBook:
                if (profileData != null){
                    if (profileData.serviceCount.equals("0")) {
                        MyToast.getInstance(ArtistProfileActivity.this).showDasuAlert("No services added by the artist!");
                        return;
                    }

                    Intent intent = new Intent(ArtistProfileActivity.this, BookingActivity.class);
                    intent.putExtra("_id", 0);
                    intent.putExtra("artistId", artistId);
                    intent.putExtra("callType", "In Call");

                    intent.putExtra("mainServiceName", "");
                    intent.putExtra("subServiceName", "");
                    startActivity(intent);
                }

                break;

            case R.id.btnFollow:
                if (btnFollow.getText().toString().equals("Message")) {
                    Intent chat_intent = new Intent(ArtistProfileActivity.this, ChatActivity.class);
                    chat_intent.putExtra("opponentChatId", profileData._id);
                    startActivity(chat_intent);
                    return;
                }
                int followersCount = Integer.parseInt(profileData.followersCount);
                if (profileData.followerStatus.equals("1")) {
                    profileData.followerStatus = "0";
                    btnFollow.setText("Follow");
                    followersCount--;
                    profileData.followersCount = String.valueOf(followersCount);
                    apiForGetFollowUnFollow();
                } else {
                    btnFollow.setText("Message");
                    profileData.followerStatus = "1";
                    followersCount++;
                    profileData.followersCount = String.valueOf(followersCount);
                    apiForGetFollowUnFollow();
                }

                break;

            case R.id.llDots:
                arrayList = new ArrayList<>();
                if(blockedId.equals(String.valueOf(myId)) || blockedId.equals("Both")){
                    arrayList.add("Unblock");
                }else arrayList.add("Block");

                arrayList.add("Report");

                if (profileData.followerStatus.equals("1")) {
                    arrayList.add("Unfollow");
                }
                NameDisplayDialog.newInstance("Select Option", arrayList, pos -> {
                    String data = arrayList.get(pos);
                    getClickedData(data);
                }).show(getSupportFragmentManager());

                break;

            case R.id.llServices:
                if (profileData != null)
                    if (!profileData.serviceCount.equals("0")) {
                        Intent intent4 = new Intent(ArtistProfileActivity.this, ArtistServicesActivity.class);
                        intent4.putExtra("artistId", artistId);
                        startActivity(intent4);
                    } else MyToast.getInstance(this).showDasuAlert("No searvice added");

                break;

            case R.id.llAboutUs:
                if (profileData != null)
                    if (!profileData.bio.equals("")) {
                        Intent intent = new Intent(this, AboutUsActivity.class);
                        intent.putExtra("bio", profileData.bio);
                        startActivity(intent);
                    } else MyToast.getInstance(this).showDasuAlert("No about us added");

                break;

            case R.id.ivChat2:
                if (profileData != null) {
                    Intent chat_intent = new Intent(ArtistProfileActivity.this, ChatActivity.class);
                    chat_intent.putExtra("opponentChatId", profileData._id);
                    startActivity(chat_intent);
                }
                break;

            case R.id.llCertificate:
                if (profileData != null)
                    if (!profileData.certificateCount.equals("0") && !profileData.certificateCount.equals("")) {
                        Intent intent3 = new Intent(ArtistProfileActivity.this, CertificateActivity.class);
                        intent3.putExtra("artistId", artistId);
                        startActivityForResult(intent3, 10);
                    } else MyToast.getInstance(this).showDasuAlert("No certificate added");


                break;

            case R.id.llFollowing:
                Intent intent1 = new Intent(ArtistProfileActivity.this, FollowersActivity.class);
                intent1.putExtra("isFollowers", false);
                intent1.putExtra("artistId", artistId);
                startActivityForResult(intent1, 10);
                break;

            case R.id.llFollowers:
                Intent intent2 = new Intent(ArtistProfileActivity.this, FollowersActivity.class);
                intent2.putExtra("isFollowers", true);
                intent2.putExtra("artistId", artistId);
                startActivityForResult(intent2, 10);
                //startActivity(new Intent(mContext,FollowersActivity.class));
                break;

            case R.id.llPost:
                updateViewType(R.id.ly_feeds);
                break;

            case R.id.ll_filter:
                int[] location = new int[2];
               /* // Get the x, y location and store it in the location[] array
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
                arrayList.clear();
                arrayList.add("All");
                arrayList.add("Photo");
                arrayList.add("Video");

                NameDisplayDialog.newInstance(getString(R.string.post_type), arrayList, pos -> {
                    String data = arrayList.get(pos);
                    int prevSize = feeds.size();
                    switch (data) {
                        case "All":
                            page = 0;
                            tvFilter.setText(R.string.all);
                            ed_search.setText("");
                            feeds.clear();
                            endlesScrollListener.resetState();
                            feedType = "";
                            CURRENT_FEED_STATE = Constant.FEED_STATE;
                            feedAdapter.notifyDataSetChanged();
                            apiForGetAllFeeds(0, 20, true, "");

                            // popupWindow.dismiss();
                            break;

                        case "Photo":
                            page = 0;
                            tvFilter.setText(R.string.photo);
                            ed_search.setText("");
                            feeds.clear();
                            endlesScrollListener.resetState();
                            feedType = "image";
                            CURRENT_FEED_STATE = Constant.IMAGE_STATE;
                            feedAdapter.notifyDataSetChanged();
                            apiForGetAllFeeds(0, 20, true, "");
                            // popupWindow.dismiss();
                            break;

                        case "Video":
                            page = 0;
                            tvFilter.setText(R.string.video);
                            ed_search.setText("");
                            // popupWindow.dismiss();
                            feeds.clear();
                            endlesScrollListener.resetState();
                            feedType = "video";
                            CURRENT_FEED_STATE = Constant.VIDEO_STATE;
                            feedAdapter.notifyDataSetChanged();
                            apiForGetAllFeeds(0, 20, true, "");
                            break;

                    }
                }).show(getSupportFragmentManager());

                break;

            case R.id.llRating:
               Intent intent = new Intent(this, ReviewRatingActivity.class);
                intent.putExtra("id", artistId);
                intent.putExtra("userType", "artist");
                startActivity(intent);
                break;


            case R.id.ly_main_layout:
            case R.id.ly_main:
                if (ed_search.getText().toString().trim().equals("")) {
                    ed_search.setVisibility(View.GONE);
                }

                break;


        }
    }

    private void getClickedData(String data) {
        switch (data) {
            case "Unfollow":
                int followersCount = Integer.parseInt(profileData.followersCount);

                profileData.followerStatus = "0";
                btnFollow.setText("Follow");
                followersCount--;
                profileData.followersCount = String.valueOf(followersCount);
                apiForGetFollowUnFollow();
                break;

            case "Block":

                if(blockedId.equals(artistId)){
                    mBlockUserWeb.child(artistId).child(String.valueOf(myId)).setValue("Both");
                }else {
                    mBlockUserWeb.child(artistId).child(String.valueOf(myId)).setValue(String.valueOf(myId));
                }
                mBlockUser.child(nodeForBlockUser).child("blockedBy").setValue(String.valueOf(myId));
                break;

            case "Unblock":
                mBlockUser.child(nodeForBlockUser).removeValue();
                mBlockUserWeb.child(artistId).child(String.valueOf(myId)).removeValue();
                break;
        }
    }

    private void initiatePopupWindow(Point p) {

        try {
            LayoutInflater inflater = (LayoutInflater) ArtistProfileActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View layout = inflater.inflate(R.layout.layout_popup_menu, (ViewGroup) findViewById(R.id.parent));
            popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);
            int OFFSET_X = 460;
            int OFFSET_Y = 35;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.setElevation(5);
            }
            arrayList.add("All");
            arrayList.add("Photo");
            arrayList.add("Video");
            popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
            RecyclerView recycler_view = layout.findViewById(R.id.recycler_view);
            MenuAdapter menuAdapter = new MenuAdapter(ArtistProfileActivity.this, arrayList, new MenuAdapter.Listener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onMenuClick(int pos) {
                    String data = arrayList.get(pos);
                    int prevSize = feeds.size();
                    switch (data) {
                        case "All":
                            page = 0;
                            tvFilter.setText(R.string.all);


                            feeds.clear();
                            endlesScrollListener.resetState();
                            feedType = "";
                            CURRENT_FEED_STATE = Constant.FEED_STATE;
                            feedAdapter.notifyItemRangeRemoved(0, prevSize);
                            apiForGetAllFeeds(0, 20, true, "");

                            popupWindow.dismiss();
                            break;

                        case "Photo":
                            page = 0;
                            tvFilter.setText(R.string.photo);
                            feeds.clear();
                            endlesScrollListener.resetState();
                            feedType = "image";
                            CURRENT_FEED_STATE = Constant.IMAGE_STATE;
                            feedAdapter.notifyItemRangeRemoved(0, prevSize);
                            apiForGetAllFeeds(0, 20, true, "");
                            popupWindow.dismiss();
                            break;

                        case "Video":
                            page = 0;
                            tvFilter.setText(R.string.video);
                            popupWindow.dismiss();
                            feeds.clear();
                            endlesScrollListener.resetState();
                            feedType = "video";
                            CURRENT_FEED_STATE = Constant.VIDEO_STATE;
                            feedAdapter.notifyItemRangeRemoved(0, prevSize);
                            apiForGetAllFeeds(0, 20, true, "");
                            break;

                    }
                }
            });

            LinearLayoutManager layoutManager = new LinearLayoutManager(ArtistProfileActivity.this);
            recycler_view.setLayoutManager(layoutManager);
            recycler_view.setAdapter(menuAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setProfileData(final UserProfileData profileData) {
        TextView tv_ProfileName = findViewById(R.id.tv_ProfileName);
        TextView tv_username = findViewById(R.id.tv_username);
        TextView tvRatingCount = findViewById(R.id.tvRatingCount);
        TextView tv_distance = findViewById(R.id.tv_distance);

        CircleImageView iv_Profile = findViewById(R.id.iv_Profile);
        ImageView ivActive = findViewById(R.id.ivActive);
        RatingBar rating = findViewById(R.id.rating);

        if (profileData != null) {
            if (profileData.favoriteStatus.equals("1")) {
                ivFav.setImageDrawable(getResources().getDrawable(R.drawable.active_star_icon));
            } else {
                ivFav.setImageDrawable(getResources().getDrawable(R.drawable.inactive_star_co));
            }

            if (profileData.followerStatus.equals("1")) {
                btnFollow.setText("Message");
            } else {
                btnFollow.setText("Follow");
            }


            tv_ProfileName.setText(profileData.firstName + " " + profileData.lastName);
            tv_distance.setText(profileData.radius + " Miles");
            tv_username.setText("@" + profileData.userName);
            tvRatingCount.setText("(" + profileData.reviewCount + ")");

            rating.setRating(Float.parseFloat(profileData.ratingCount));

            if (profileData.isCertificateVerify.equals("1")) {
                ivActive.setVisibility(View.VISIBLE);
            } else ivActive.setVisibility(View.GONE);

            if (!profileData.profileImage.isEmpty() && !profileData.profileImage.equals("")) {
                Picasso.with(ArtistProfileActivity.this).load(profileData.profileImage).placeholder(R.drawable.default_placeholder).
                        fit().into(iv_Profile);
            }


            iv_Profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                 /*   ArrayList<String> tempList = new ArrayList<>();
                    tempList.add(profileData.profileImage);

                    Intent intent = new Intent(ArtistProfileActivity.this, PreviewImageActivity.class);
                    intent.putExtra("imageArray", tempList);
                    intent.putExtra("startIndex", 0);
                    startActivity(intent);*/
                }
            });

        }

    }

    public void updateViewType(int id) {
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
                    feedType = "video";
                    CURRENT_FEED_STATE = Constant.VIDEO_STATE;
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                    apiForGetAllFeeds(0, 20, true, "");
                }
                break;
        }

        lastFeedTypeId = id;
    }

    private void apiForGetAllFeeds(final int page, final int feedLimit, final boolean isEnableProgress, final String searchText) {

        if (ll_progress != null)
            ll_progress.setVisibility(isEnableProgress ? View.VISIBLE : View.GONE);

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ArtistProfileActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetAllFeeds(page, feedLimit, isEnableProgress, searchText);
                    }

                }
            }).show();

            if (ll_progress != null) ll_progress.setVisibility(View.GONE);
            setAdapterLoading(false);
        }

        Map<String, String> params = new HashMap<>();
        params.put("feedType", feedType);
        params.put("search", "");
        params.put("page", String.valueOf(page));
        params.put("limit", String.valueOf(feedLimit));
        params.put("type", "");
        params.put("userId", artistId);
        params.put("viewBy", "user");
        params.put("loginUserId", String.valueOf(user.id));
        params.put("search", searchText);
        // params.put("appType", "user");
        Mualab.getInstance().cancelPendingRequests(TAG);

        new HttpTask(new HttpTask.Builder(ArtistProfileActivity.this, "profileFeed", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                if (ll_progress != null) ll_progress.setVisibility(View.GONE);
                setAdapterLoading(false);
                if (feedAdapter != null) feedAdapter.showHideLoading(false);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        //removeProgress();
                        if (page == 0) {
                            feeds.clear();
                        }
                        ParseAndUpdateUI(response);

                    } else if (page == 0) {
                        rvFeed.setVisibility(View.GONE);
                        tv_no_data_msg.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // MyToast.getInstance(mContext).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                if (ll_progress != null) ll_progress.setVisibility(View.GONE);
                setAdapterLoading(false);
                if (isPulltoRefrash) {
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(false, 500);
                    int prevSize = feeds.size();
                    feeds.clear();
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                }
                //MyToast.getInstance(mContext).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
            }
        })
                .setAuthToken(Mualab.currentUser.authToken)
                .setParam(params)
                .setMethod(Request.Method.POST)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute("artistProfileApi");
        //ll_progress.setVisibility(isEnableProgress ? View.VISIBLE : View.GONE);
    }

    private void ParseAndUpdateUI(final String response) {

        try {
            JSONObject js = new JSONObject(response);
            String status = js.getString("status");
            // String message = js.getString("message");

            if (status.equalsIgnoreCase("success")) {
                rvFeed.setVisibility(View.VISIBLE);
                JSONArray array = js.getJSONArray("AllFeeds");

                if (isPulltoRefrash) {
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(true, 500);
                    int prevSize = feeds.size();
                    feeds.clear();
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                }

                Gson gson = new Gson();
                if (array.length() != 0) {
                    if (isGrideView) {
                        feeds.clear();
                    }
                    tv_no_data_msg.setVisibility(View.GONE);
                    for (int i = 0; i < array.length(); i++) {

                        try {
                            JSONObject jsonObject = array.getJSONObject(i);
                            Feeds feed = gson.fromJson(String.valueOf(jsonObject), Feeds.class);
                            //   feed.taggedImgMap = new HashMap<>();
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

                                        HashMap<String, TagDetail> tagDetails = new HashMap<>();

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
                                        tagDetails.put(tag.title, tag);
                                        TagToBeTagged tagged = new TagToBeTagged();
                                        tagged.setUnique_tag_id(unique_tag_id);
                                        tagged.setX_co_ord(x_axis);
                                        tagged.setY_co_ord(y_axis);
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
// tagged.setTagDetails(tagDetails);
                                            tagged.setTagDetails(tag);

                                            feed.serviceTagList.add(tagged);
                                        }
                                        feed.serviceTaggedImgMap.put(j, feed.serviceTagList);
                                    }
                                }
                            }


                            if (isGrideView) {
                                if (!feed.feedType.equals("text")) {
                                    feeds.add(feed);
                                }
                            } else {
                                feeds.add(feed);
                            }

                        } catch (JsonParseException e) {
                            e.printStackTrace();
                            // FirebaseCrash.log(e.getLocalizedMessage());
                        }
                    } // loop end.
                } else if (feeds.size() == 0) {
                    rvFeed.setVisibility(View.GONE);
                    tv_no_data_msg.setVisibility(View.VISIBLE);
                }

            /*    if (isGrideView) {
                    rvFeed.setLayoutManager(new GridLayoutManager(ArtistProfileActivity.this, 3));
                    feedAdapter.isGrideView(true);
                    endlesScrollListener.resetState();
                } else {
                    rvFeed.setLayoutManager(new LinearLayoutManager(ArtistProfileActivity.this));
                    feedAdapter.isGrideView(false);
                    endlesScrollListener.resetState();
                }*/


                feedAdapter.notifyDataSetChanged();

            } else if (status.equals("fail") && feeds.size() == 0) {
                rvFeed.setVisibility(View.GONE);
                tv_msg.setVisibility(View.VISIBLE);

                if (isPulltoRefrash) {
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(false, 500);

                }
                feedAdapter.notifyDataSetChanged();
            } else {
                rvFeed.setVisibility(View.GONE);
                tv_no_data_msg.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            feedAdapter.notifyDataSetChanged();
            //MyToast.getInstance(mContext).showSmallCustomToast(getString(R.string.alert_something_wenjt_wrong));
        }
    }

    private void apiForGetFollowUnFollow() {
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ArtistProfileActivity.this, new NoConnectionDialog.Listner() {
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
        params.put("followerId", artistId);

        HttpTask task = new HttpTask(new HttpTask.Builder(ArtistProfileActivity.this, "followFollowing", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        /*if (profileData.followerStatus.equals("1")) {
                            profileData.followerStatus = "0";
                            btnFollow.setText("Follow");
                        }
                        else {
                            profileData.followerStatus = "1";
                            btnFollow.setText("Unfollow");
                        }*/
                    } else {
                        MyToast.getInstance(ArtistProfileActivity.this).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(ArtistProfileActivity.this);
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

    private void apiForFavourite() {
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ArtistProfileActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForFavourite();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));
        params.put("artistId", artistId);
        if (profileData.favoriteStatus.equals("1"))
            params.put("type", "favorite");
        else
            params.put("type", "unfavorite");


        HttpTask task = new HttpTask(new HttpTask.Builder(ArtistProfileActivity.this, "addFavorite", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                       /* if (profileData.favoriteStatus.equals("1")) {
                            profileData.favoriteStatus = "0";
                            ivFav.setImageDrawable(getResources().getDrawable(R.drawable.inactive_like_ico));
                        }
                        else {
                            profileData.favoriteStatus = "1";
                            ivFav.setImageDrawable(getResources().getDrawable(R.drawable.active_like_ico));
                        }*/
                    } else {
                        MyToast.getInstance(ArtistProfileActivity.this).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(ArtistProfileActivity.this);
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

    private void ParseAndUpdateUI(final int page, final String response) {

        try {
            JSONObject js = new JSONObject(response);
            String status = js.getString("status");
            // String message = js.getString("message");

            if (status.equalsIgnoreCase("success")) {
                rvFeed.setVisibility(View.VISIBLE);
                JSONArray array = js.getJSONArray("AllFeeds");
                if (isPulltoRefrash) {
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(true, 500);
                    int prevSize = feeds.size();
                    feeds.clear();
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                }

                Gson gson = new Gson();
                if (array.length() != 0) {
                    for (int i = 0; i < array.length(); i++) {

                        try {
                            JSONObject jsonObject = array.getJSONObject(i);
                            Feeds feed = gson.fromJson(String.valueOf(jsonObject), Feeds.class);

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

                            feeds.add(feed);

                        } catch (JsonParseException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (page == 0 || feeds.size() == 0) {
                    tv_no_data_msg.setVisibility(View.VISIBLE);
                    rvFeed.setVisibility(View.GONE);
                }
                // loop end.

                feedAdapter.notifyDataSetChanged();
                //updateViewType(R.id.ly_feeds);

            } else if (status.equals("fail") && feeds.size() == 0) {
                rvFeed.setVisibility(View.GONE);
                tv_msg.setVisibility(View.VISIBLE);

                if (isPulltoRefrash) {
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(false, 500);

                }
                feedAdapter.notifyDataSetChanged();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            feedAdapter.notifyDataSetChanged();
            //MyToast.getInstance(mContext).showSmallCustomToast(getString(R.string.alert_something_wenjt_wrong));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Mualab.getInstance().cancelPendingRequests(TAG);
        tvImages = null;
        tvVideos = null;
        tvFeeds = null;
        tv_msg = null;
        ll_progress = null;
        endlesScrollListener = null;
        feedAdapter = null;
        feeds = null;
        rvFeed = null;
        item = null;
        user = null;
        profileData = null;
    }

    @Override
    public void onCommentBtnClick(Feeds feed, int pos) {
        Intent intent = new Intent(ArtistProfileActivity.this, CommentsActivity.class);
        intent.putExtra("feed_id", feed._id);
        intent.putExtra("feedPosition", pos);
        intent.putExtra("feed", feed);
        intent.putExtra("commentCount", feed.commentCount);
        startActivityForResult(intent, Constant.ACTIVITY_COMMENT);
    }

    @Override
    public void onLikeListClick(Feeds feed) {
        addFragment(LikeFragment.newInstance(feed._id, user.id), true);
    }

    @Override
    public void onFeedClick(Feeds feed, int index, View v) {
        /*Intent intent = new Intent(ArtistProfileActivity.this, FeedSingleActivity.class);
        intent.putExtra("feedId", String.valueOf(feed._id));
        startActivity(intent);*/
       /* ArrayList<String> tempList = new ArrayList<>();
        for (int i = 0; i < feed.feedData.size(); i++) {
            tempList.add(feed.feedData.get(i).feedPost);
        }

        Intent intent = new Intent(ArtistProfileActivity.this, PreviewImageActivity.class);
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
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            page = 0;
            apiForGetProfile();
            apiForGetAllFeeds(page, 20, true, "");
        } else if (data != null) {
            if (requestCode == Constant.ACTIVITY_COMMENT) {
                if (CURRENT_FEED_STATE == Constant.FEED_STATE) {
                    int pos = data.getIntExtra("feedPosition", 0);
                    Feeds feed = (Feeds) data.getSerializableExtra("feed");
                    feeds.get(pos).commentCount = feed.commentCount;
                    feedAdapter.notifyItemChanged(pos);
                }
            }
        }
    }

    /* frangment replace code */
    public void addFragment(Fragment fragment, boolean addToBackStack) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fragmentManager = getSupportFragmentManager();
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

    public void publicationQuickView(Feeds feeds, int index) {
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.dialog_image_detail_view, null);

        ImageView postImage = view.findViewById(R.id.ivFeedCenter);
        ImageView profileImage = view.findViewById(R.id.ivUserProfile);
        TextView tvUsername = view.findViewById(R.id.txtUsername);
        tvUsername.setText(feeds.userName);

        view.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideQuickView();
            }
        });

        view.findViewById(R.id.tvUnfollow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyToast.getInstance(ArtistProfileActivity.this).showSmallCustomToast(getString(R.string.under_development));
            }
        });

        Picasso.with(ArtistProfileActivity.this).load(feeds.feed.get(index)).priority(Picasso.Priority.HIGH).noPlaceholder().into(postImage);

        if (TextUtils.isEmpty(feeds.profileImage))
            Picasso.with(ArtistProfileActivity.this).load(R.drawable.default_placeholder).noPlaceholder().into(profileImage);
        else
            Picasso.with(ArtistProfileActivity.this).load(feeds.profileImage).noPlaceholder().into(profileImage);

        builder = new Dialog(ArtistProfileActivity.this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //noinspection ConstantConditions
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        builder.setContentView(view);
        builder.setCancelable(true);
        builder.show();
    }

    boolean isShow = false;

    private void showLargeImage(Feeds feeds, int index) {
        View dialogView = View.inflate(ArtistProfileActivity.this, R.layout.dialog_large_image_view, null);
        final Dialog dialog = new Dialog(ArtistProfileActivity.this, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.InOutAnimation;
        dialog.setContentView(dialogView);
        final InstaTag postImage = dialogView.findViewById(R.id.post_image);
        ImageView btnBack = dialogView.findViewById(R.id.btnBack);
        TextView tvHeaderTitle = dialogView.findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText("Images");

        postImage.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        postImage.setRootWidth(postImage.getMeasuredWidth());
        postImage.setRootHeight(postImage.getMeasuredHeight());

        if (feeds.feed.get(index) != null) {
            Glide.with(ArtistProfileActivity.this).load(feeds.feed.get(index)).placeholder(R.drawable.gallery_placeholder)
                    .skipMemoryCache(false).into(postImage.getTagImageView());
        }

        postImage.setImageToBeTaggedEvent(taggedImageEvent);

        ArrayList<TagToBeTagged> tags = feeds.taggedImgMap.get(index);
        if (tags != null && tags.size() != 0) {
            postImage.addTagViewFromTagsToBeTagged("", tags, false);
            postImage.hideTags("");
        }

        //   Picasso.with(mContext).load(feeds.feed.get(index)).priority(Picasso.Priority.HIGH).noPlaceholder().into(postImage);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        longPressListner = new ViewPagerAdapter.LongPressListner() {
            @Override
            public void onLongPress() {
                if (!isShow) {
                    isShow = true;
                    postImage.showTags("");
                } else {
                    isShow = false;
                    postImage.hideTags("");
                }

            }
        };

        dialog.show();
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

    public void hideQuickView() {
        if (builder != null) builder.dismiss();
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        FragmentManager fm = getSupportFragmentManager();
        int i = fm.getBackStackEntryCount();
        if (i > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
            finish();
        }
    }
}

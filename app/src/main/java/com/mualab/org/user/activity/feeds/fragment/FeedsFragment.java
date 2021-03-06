package com.mualab.org.user.activity.feeds.fragment;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.hendraanggrian.socialview.SocialView;
import com.mualab.org.user.R;
import com.mualab.org.user.Views.refreshviews.CircleHeaderView;
import com.mualab.org.user.Views.refreshviews.OnRefreshListener;
import com.mualab.org.user.Views.refreshviews.RjRefreshLayout;
import com.mualab.org.user.activity.authentication.LoginActivity;
import com.mualab.org.user.activity.base.BaseListner;
import com.mualab.org.user.activity.camera.CameraActivity;
import com.mualab.org.user.activity.feeds.activity.CommentsActivity;
import com.mualab.org.user.activity.feeds.activity.FeedPostActivity;
import com.mualab.org.user.activity.feeds.activity.FeedSingleActivity;
import com.mualab.org.user.activity.feeds.activity.LikeFeedActivity;
import com.mualab.org.user.activity.feeds.activity.PreviewImageActivity;
import com.mualab.org.user.activity.feeds.adapter.FeedAdapter;
import com.mualab.org.user.activity.feeds.adapter.HashtagAdapter;
import com.mualab.org.user.activity.feeds.adapter.LiveUserAdapter;
import com.mualab.org.user.activity.feeds.adapter.UserSuggessionAdapter;
import com.mualab.org.user.activity.feeds.adapter.ViewPagerAdapter;
import com.mualab.org.user.activity.feeds.adapter.ViewPagerFeedAdapter;
import com.mualab.org.user.activity.feeds.enums.PermissionType;
import com.mualab.org.user.activity.feeds.listener.MyClickOnPostMenu;
import com.mualab.org.user.activity.gellery.GalleryActivity;
import com.mualab.org.user.activity.story.StoriesActivity;
import com.mualab.org.user.activity.story.camera.util.ImageUtil;
import com.mualab.org.user.activity.tag_module.instatag.InstaTag;
import com.mualab.org.user.activity.tag_module.instatag.TagDetail;
import com.mualab.org.user.activity.tag_module.instatag.TagToBeTagged;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.data.feeds.LiveUserInfo;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.MediaUri;
import com.mualab.org.user.data.remote.API;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.dialogs.SelectableDialog;
import com.mualab.org.user.image.picker.ImagePicker;
import com.mualab.org.user.image.picker.ImageRotator;
import com.mualab.org.user.listener.RecyclerViewScrollListener;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.WrapContentLinearLayoutManager;
import com.mualab.org.user.utils.constants.Constant;
import com.mualab.org.user.utils.media.ImageVideoUtil;
import com.mualab.org.user.utils.media.PathUtil;
import com.viven.imagezoom.ImageZoomHelper;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;


import static android.app.Activity.RESULT_OK;

/*
 * Dharmraj acharya
 * */
public class FeedsFragment extends FeedBaseFragment implements View.OnClickListener,
        FeedAdapter.Listener, LiveUserAdapter.Listner {

    public static String TAG = FeedsFragment.class.getName();

    // ui components delecration
    private Context mContext;
    private AppBarLayout appbar;
    private CoordinatorLayout coordinatorLayout;
    private CollapsingToolbarLayout collapsing_toolbar;
    private TextView tv_msg, tv_no_data_msg;
    private LinearLayout ll_header;
    private RjRefreshLayout mRefreshLayout;
    public boolean isPulltoRefrash = false;

    private Bitmap thumbImage = null;
    private ViewPagerAdapter.LongPressListner longPressListner;
    /*Adapters*/
    private LiveUserAdapter liveUserAdapter;
    private ArrayList<LiveUserInfo> liveUserList;
    private ProgressBar progress_bar;
    private RecyclerView rvMyStory;
    private FeedAdapter feedAdapter;
    private RecyclerView rvFeed;
    private List<Feeds> feeds;
    public RecyclerViewScrollListener endlesScrollListener;

    /*required variables*/
    private String feedType = "feeds";
    private int lastFeedTypeId;
    private PermissionType permissionType;

    private MediaUri mediaUri;
    private BaseListner baseListner;
    // private int fragCount;
    private int CURRENT_FEED_STATE = 0;
    private boolean isEditTextFocaused;

    private ViewPager viewPager;
    private LinearLayout sliderDotspanel;
    private int dotscount;
    private ViewPagerFeedAdapter viewPagerAdapter1;
    private ImageView[] dots;


    public FeedsFragment() {
        // Required empty public constructor
    }

    public static FeedsFragment newInstance(int instance) {
        FeedsFragment fragment = new FeedsFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_INSTANCE, instance);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof BaseListner) {
            baseListner = (BaseListner) mContext;
        }

        /*if(context instanceof MainActivity)
            ((MainActivity)context).setBgColor(R.color.white);*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Mualab.getInstance().cancelPendingRequests(TAG);
        mContext = null;
        tv_msg = null;
        ll_header = null;
        //edCaption = null;
        Progress.hide(mContext);
        endlesScrollListener = null;
        feedAdapter = null;
        liveUserList = null;
        feeds = null;
        rvFeed = null;
        collapsing_toolbar = null;
        rvMyStory = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       /* if (getArguments() != null) {
            fragCount = getArguments().getInt(ARGS_INSTANCE);
        }*/

        feeds = new ArrayList<>();
        liveUserList = new ArrayList<>();
        liveUserList.clear();

        LiveUserInfo me = new LiveUserInfo();
        if (Mualab.currentUser != null) {
            me.id = Mualab.currentUser.id;
            me.userName = "Your Story";
            me.profileImage = Mualab.currentUser.profileImage;
            me.storyCount = 0;
            liveUserList.add(me);
            Progress.hide(mContext);

        }


       /* LiveUserInfo me = new LiveUserInfo();
        me.id = Mualab.currentUser.id;
        me.userName = "My Story";
        me.profileImage = Mualab.currentUser.profileImage;
        me.storyCount = 0;
        liveUserList.add(me);
        Progress.hide(mContext);*/
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feeds, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View view) {
        ll_header = view.findViewById(R.id.ll_header);
        rvMyStory = view.findViewById(R.id.recyclerView);
        rvFeed = view.findViewById(R.id.rvFeed);
        tv_msg = view.findViewById(R.id.tv_msg);
        tv_no_data_msg = view.findViewById(R.id.tv_no_data_msg);
        progress_bar = view.findViewById(R.id.progress_bar);

        collapsing_toolbar = view.findViewById(R.id.collapsing_toolbar);
        coordinatorLayout = view.findViewById(R.id.parentView);
        appbar = view.findViewById(R.id.appbar);
        ImageView ivnotification = getActivity().findViewById(R.id.ivnotification);
        ivnotification.setVisibility(View.GONE);
        ImageView ivUserProfile = getActivity().findViewById(R.id.ivUserProfile);
        ivUserProfile.setVisibility(View.GONE);

        appbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyClickOnPostMenu.getMentIntance().setMenuClick();

            }
        });

        addRemoveHeader();
        liveUserAdapter = new LiveUserAdapter(mContext, liveUserList, this);
        rvMyStory.setAdapter(liveUserAdapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        WrapContentLinearLayoutManager lm = new WrapContentLinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        rvFeed.setItemAnimator(null);
        rvFeed.setLayoutManager(lm);
        rvFeed.setHasFixedSize(true);

        feedAdapter = new FeedAdapter(tv_no_data_msg, mContext, "", feeds, this);
        endlesScrollListener = new RecyclerViewScrollListener(lm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (feedAdapter != null) {
                    feedAdapter.showHideLoading(true);
                    apiForGetAllFeeds(page, 10, false, "");
                }
            }

            @Override
            public void onScroll(RecyclerView view, int dx, int dy) {
                if (dy > 0 && isEditTextFocaused) {
                    //edCaption.clearFocus();
                    //collapsing_toolbar.scrollTo(0, 0);
                    KeyboardUtil.hideKeyboard(view, mContext);
                }
            }
        };

        rvFeed.setAdapter(feedAdapter);
        rvFeed.addOnScrollListener(endlesScrollListener);

        if (feeds != null && feeds.size() == 0)
            updateViewType(R.id.ly_feeds);
        getStoryList();

        mRefreshLayout = view.findViewById(R.id.mSwipeRefreshLayout);
        final CircleHeaderView header = new CircleHeaderView(getContext());
        mRefreshLayout.addHeader(header);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                endlesScrollListener.resetState();
                isPulltoRefrash = true;
                apiForGetAllFeeds(0, 10, false, "");
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

       /* edCaption.setHashtagTextChangedListener(
                new Function2<SocialView, CharSequence, Unit>() {
                    @Override
                    public Unit invoke(SocialView socialView, CharSequence s) {
                        Log.d("editing", s.toString());
                        lastTxt = s.toString();
                        getDropDown(lastTxt, "");
               *//* if (lastTxt.length() > 1)
                    getDropDown(lastTxt, "");*//*
                        return null;
                    }
                });

        edCaption.setMentionTextChangedListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence s) {
                Log.d("editing", s.toString());
                lastTxt = s.toString();
                getDropDown(lastTxt, "user");
                *//*if (lastTxt.length() > 1)
                    getDropDown(lastTxt, "user");*//*
                return null;
            }
        });*/

        progress_bar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }

    private void addRemoveHeader() {
        if (ll_header.getChildCount() == 0) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            @SuppressLint("InflateParams")
            View inflatedLayout = inflater.inflate(R.layout.post_add_header_layout, null, false);
            ll_header.addView(inflatedLayout);

            mentionAdapter = new UserSuggessionAdapter(mContext);
            mentionAdapter.clear();
            //edCaption.setMentionAdapter(mentionAdapter);

            Resources.Theme theme = getResources().newTheme();
            theme.applyStyle(R.style.Theme_AppCompat_Light, true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mentionAdapter.setDropDownViewTheme(theme);
            }
            tagAdapter = new HashtagAdapter(mContext);


        } else if (ll_header != null && ll_header.getChildCount() > 0) {
            ll_header.removeAllViews();
        }

        viewPager = ll_header.findViewById(R.id.viewPager);
        sliderDotspanel = ll_header.findViewById(R.id.SliderDots);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < 2; i++) {
                    dots[i].setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.non_active_dot));
                }
                dots[position].setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.active_dot));

                if (position == 1)
                    KeyboardUtil.hideKeyboard(viewPager, mContext);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPagerAdapter1 = new ViewPagerFeedAdapter(mContext, new ViewPagerFeedAdapter.getClick() {
            @Override
            public void viewpagerClick() {
                // caption = edCaption.getText().toString().trim();
                Intent intent = null;
                ActivityOptionsCompat options = null;
                EditText edCaption = new EditText(mContext);
                intent = new Intent(mContext, FeedPostActivity.class);
                intent.putExtra("caption", "");
                intent.putExtra("feedType", Constant.TEXT_STATE);
                intent.putExtra("requestCode", Constant.POST_FEED_DATA);
                if (intent != null) {
                    startActivityForResult(intent, Constant.POST_FEED_DATA);
                }
            }

            @Override
            public void OnTextChange(String text) {
                feeds.clear();
                apiForGetAllFeeds(0, 10, false, text.trim());
            }
        });
        viewPager.setAdapter(viewPagerAdapter1);

        dotscount = viewPagerAdapter1.getCount();
        dots = new ImageView[dotscount];
        sliderDotspanel.removeAllViews();

        for (int i = 0; i < dotscount; i++) {
            dots[i] = new ImageView(mContext);
            dots[i].setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.non_active_dot));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(18, 18);
            params.setMargins(8, 0, 8, 0);
            sliderDotspanel.addView(dots[i], params);

        }

        if (dots.length != 0)
            dots[0].setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.active_dot));

    }

    private void updateViewType(int id) {
        endlesScrollListener.resetState();
        int prevSize = feeds.size();

        if (lastFeedTypeId != R.id.ly_feeds) {
            feeds.clear();
            feedType = "";
            CURRENT_FEED_STATE = Constant.FEED_STATE;
            feedAdapter.notifyItemRangeRemoved(0, prevSize);
            apiForGetAllFeeds(0, 10, true, "");
        }

        switch (id) {
           /* case R.id.ly_feeds:
                //addRemoveHeader(true);
                tvFeeds.setTextColor(getResources().getColor(R.color.colorPrimary));

                if (lastFeedTypeId != R.id.ly_feeds){
                    feeds.clear();
                    feedType = "";
                    CURRENT_FEED_STATE = Constant.FEED_STATE;
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                    apiForGetAllFeeds(0, 10, true);
                }
                break;

            case R.id.ly_images:
                tvImages.setTextColor(getResources().getColor(R.color.colorPrimary));
                // addRemoveHeader(false);
                if (lastFeedTypeId != R.id.ly_images){
                    feeds.clear();
                    feedType = "image";
                    CURRENT_FEED_STATE = Constant.IMAGE_STATE;
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                    apiForGetAllFeeds( 0, 10, true);
                }

                break;

            case R.id.ly_videos:
                tvVideos.setTextColor(getResources().getColor(R.color.colorPrimary));
                // addRemoveHeader(false);
                if (lastFeedTypeId != R.id.ly_videos){
                    feeds.clear();
                    feedType = "video";
                    CURRENT_FEED_STATE = Constant.VIDEO_STATE;
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                    apiForGetAllFeeds( 0, 10, true);
                }
                break;*/
        }

        lastFeedTypeId = id;
    }

    public void apiForGetAllFeeds(final int page, final int feedLimit, final boolean isEnableProgress,
                                  final String searchText) {
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetAllFeeds(page, feedLimit, isEnableProgress, searchText);
                    }

                }
            }).show();
        }
        Map<String, String> params = new HashMap<>();
        params.put("feedType", feedType);
        params.put("search", "");
        params.put("page", String.valueOf(page));
        params.put("limit", String.valueOf(feedLimit));
        params.put("type", "newsFeed");
        params.put("userId", "" + Mualab.currentUser.id);
        params.put("search", searchText);
        // params.put("appType", "user");
        Mualab.getInstance().cancelPendingRequests(this.getClass().getName());
        new HttpTask(new HttpTask.Builder(mContext, "getAllFeeds", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                progress_bar.setVisibility(View.GONE);
                if (feedAdapter != null) feedAdapter.showHideLoading(false);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        //removeProgress();
                        ParseAndUpdateUI(response);

                    } else MyToast.getInstance(mContext).showSmallMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    // MyToast.getInstance(mContext).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                progress_bar.setVisibility(View.GONE);
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
                .execute(TAG);

    }

    @SuppressLint("UseSparseArrays")
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
                        if (jsonArray != null && jsonArray.length() != 0) {

                            for (int j = 0; j < jsonArray.length(); j++) {

                                feed.peopleTagList = new ArrayList<>();

                                JSONArray arrayJSONArray = jsonArray.getJSONArray(j);

                                if (arrayJSONArray != null && arrayJSONArray.length() != 0) {

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
                                      /*  tag.tabType = tagOjb.getString("tabType");
                                        tag.tagId = tagOjb.getString("tagId");
                                        tag.title = tagOjb.getString("title");
                                        tag.userType = tagOjb.getString("userType");*/
                                        } else {
                                            JSONObject details = tagOjb.getJSONObject(unique_tag_id);
                                            tag = gson.fromJson(String.valueOf(details), TagDetail.class);
                                      /*  tag.tabType = details.getString("tabType");
                                        tag.tagId = details.getString("tagId");
                                        tag.title = details.getString("title");
                                        tag.userType = details.getString("userType");*/
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


                        feeds.add(feed);


                    } catch (JsonParseException e) {
                        e.printStackTrace();
                    }

                } // loop end.


                if (feeds.size() == 0) {
                    tv_no_data_msg.setText(R.string.no_res_found);
                    tv_no_data_msg.setVisibility(View.VISIBLE);

                    if (isPulltoRefrash) {
                        isPulltoRefrash = false;
                        mRefreshLayout.stopRefresh(false, 500);
                    }
                } else {
                    tv_no_data_msg.setText(R.string.no_res_found);
                    tv_no_data_msg.setVisibility(View.GONE);
                }

                feedAdapter.notifyDataSetChanged();

            } else if (status.equals("fail") && feeds.size() == 0) {
                rvFeed.setVisibility(View.GONE);
                tv_no_data_msg.setText(R.string.no_res_found);
                tv_no_data_msg.setVisibility(View.VISIBLE);

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

    private String getEmojis(String commets) {
        String myString = "";
        try {
            myString = URLDecoder.decode(commets, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return myString;
    }

  /*  private void removeProgress(){

        if(feeds!=null && feeds.size()>0 && feeds.get(feeds.size()-1)==null){
            int lastIndex = feeds.size()-1;
            feeds.remove(lastIndex);
            feedAdapter.notifyDataSetChanged();
        }
    }
*/

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
      /*  if (baseListner != null) {
            baseListner.addFragment(LikeFragment.newInstance(feed._id, Mualab.currentUser.id), true);
        }*/

        Intent intent = new Intent(mContext, LikeFeedActivity.class);
        intent.putExtra("feedId", feed._id);
        intent.putExtra("userId", Mualab.currentUser.userId);
        startActivity(intent);
    }

    @Override
    public void onFeedClick(Feeds feed, int index, View view) {
       /* Intent intent = new Intent(mContext, FeedSingleActivity.class);
        intent.putExtra("feedId",String.valueOf(feed._id));
        mContext.startActivity(intent);*/

        /*ArrayList<String> tempList = new ArrayList<>();
        for(int i=0; i<feed.feedData.size(); i++){
            tempList.add(feed.feedData.get(i).feedPost);
        }

        Intent intent = new Intent(mContext, PreviewImageActivity.class);
        intent.putExtra("imageArray", tempList);
        intent.putExtra("startIndex", index);
        startActivity(intent);
*/
        // publicationQuickView(feed, index);
        //showLargeImage(feed, index);
        /* */
    }

    @Override
    public void onClickProfileImage(Feeds feed, ImageView v) {
        /*Intent intent = new Intent(mContext, ImageViewDialogActivity.class);
        intent.putExtra("feed", feed);
        intent.putExtra("index", 0);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation((Activity) mContext, v, "image");
        startActivityForResult(intent, Constant.POST_FEED_DATA, options.toBundle());*/
    }

    private void getStoryList() {
        Map<String, String> params = new HashMap<>();
        params.put("userId", Mualab.currentUser.id + "");

        new HttpTask(new HttpTask.Builder(mContext, "getMyStoryUser", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    //String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        JSONArray array = js.getJSONArray("myStoryList");
                        Gson gson = new Gson();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            LiveUserInfo live = gson.fromJson(String.valueOf(jsonObject), LiveUserInfo.class);

                            if (live.id == Mualab.currentUser.id) {
                                LiveUserInfo me = liveUserList.get(0);
                                me.firstName = live.firstName;
                                me.lastName = live.lastName;
                                me.fullName = live.firstName + " " + live.lastName;
                                me.storyCount = live.storyCount;
                                me.userName = "You";

                            } else liveUserList.add(live);
                        }

                        liveUserAdapter.notifyDataSetChanged();
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    Progress.hide(mContext);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
            }
        })
                .setParam(params)
                .setProgress(false)
                .setAuthToken(Mualab.currentUser.authToken)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute(TAG);
    }

    public void checkPermissionAndPicImageOrVideo(String title) {
        SelectableDialog dialog = new SelectableDialog(mContext, new SelectableDialog.Listener() {
            @Override
            public void onGalleryClick() {

                if (permissionType == PermissionType.IMAGE) {
                    Matisse.from(FeedsFragment.this)
                            .choose(MimeType.allOf())
                            .countable(true)
                            .maxSelectable(10)
                            //  .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                            .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                            .thumbnailScale(0.85f)
                            .imageEngine(new GlideEngine())
                            .forResult(Constant.REQUEST_CODE_CHOOSE);
                } else {
                    Intent intent = new Intent();
                    intent.setType("video/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Video"),
                            Constant.GALLERY_INTENT_CALLED);
                }
            }

            @Override
            public void onCameraClick() {
                if (permissionType == PermissionType.IMAGE) {
                    ImagePicker.pickImageFromCamera(FeedsFragment.this);
                } else {
                    //mediaUri = null;
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                        long maxVideoSize = 20 * 1024 * 1024; // 30 MB
                        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
                        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize);
                        startActivityForResult(intent, Constant.REQUEST_VIDEO_CAPTURE);
                    }
                }
            }

            @Override
            public void onCancel() {

            }
        });
        dialog.setTitle(title);
        if (Build.VERSION.SDK_INT >= 23) {
            if (mContext.checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    mContext.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);
            } else dialog.show();
        } else dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // String filePath;

        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case Constant.CAMERA_REQUEST:
                    try {
                        Bitmap bitmap = ImagePicker.getImageFromResult(mContext, requestCode, resultCode, data);

                        Uri picUri = ImagePicker.getImageURIFromResult(mContext, requestCode, resultCode, data);

                        if (bitmap != null && picUri != null) {
                           /* mediaUri = new MediaUri();
                            mediaUri.isFromGallery = false;
                            mediaUri.mediaType = Constant.IMAGE_STATE;
                            mediaUri.addUri(String.valueOf(picUri));
                            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(
                                    BitmapFactory.decodeFile(
                                            ImageVideoUtil.generatePath(picUri, mContext)), 150, 150);
                            updatePostImageUI(ThumbImage);*/

                            File imageFile = ImageRotator.getTemporalFile(mContext);
                            Uri photoURI = Uri.fromFile(imageFile);

                            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(
                                    ImageVideoUtil.generatePath(photoURI, mContext)),
                                    150, 150);

                            bitmap = ImagePicker.getImageResized(mContext, photoURI);
                            int rotation = ImageRotator.getRotation(mContext, photoURI, true);
                            bitmap = ImageRotator.rotate(bitmap, rotation);

                            File file = new File(mContext.getExternalCacheDir(), UUID.randomUUID() + ".jpg");
                            picUri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName()
                                    + ".provider", file);

                            try {
                                OutputStream outStream;
                                outStream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 80, outStream);
                                // ThumbImage.compress(Bitmap.CompressFormat.PNG, 80, outStream);
                                outStream.flush();
                                outStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            mediaUri = new MediaUri();
                            mediaUri.isFromGallery = false;
                            mediaUri.mediaType = Constant.IMAGE_STATE;
                            mediaUri.addUri(String.valueOf(picUri));
                            thumbImage = ThumbImage;


                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case Constant.REQUEST_CODE_CHOOSE:
                    List<Uri> tmpUri = Matisse.obtainResult(data);
                    if (tmpUri.size() > 0) {
                        mediaUri = new MediaUri();
                        mediaUri.isFromGallery = true;
                        mediaUri.mediaType = Constant.IMAGE_STATE;

                        List<String> uriList = new ArrayList<>();
                        for (Uri tmp : tmpUri) {
                            String[] projection = {MediaStore.MediaColumns.DATA};
                            CursorLoader cursorLoader = new CursorLoader(mContext, tmp, projection, null, null, null);
                            Cursor cursor = cursorLoader.loadInBackground();
                            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                            cursor.moveToFirst();
                            String selectedImagePath = cursor.getString(column_index);

                            Bitmap imgBitmap = BitmapFactory.decodeFile(selectedImagePath);
                            try {
                                imgBitmap = ImageUtil.modifyOrientation(imgBitmap, selectedImagePath);
                                Uri imageUri = ImageUtil.getImageUri(mContext, imgBitmap);
                                uriList.add(String.valueOf(imageUri));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // uriList.add(String.valueOf(tmp));
                        }
                        mediaUri.addUri(uriList);
                        try {
                            Bitmap ThumbImage = ThumbnailUtils
                                    .extractThumbnail(BitmapFactory.decodeFile(
                                            ImageVideoUtil.generatePath(tmpUri.get(0), mContext)), 100, 100);
                            // Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), Uri.parse(mediaUri.uri));
                            thumbImage = ThumbImage;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;


                case Constant.POST_FEED_DATA:
                    resetView();
                    endlesScrollListener.resetState();
                    feeds.clear();
                    //first clear the recycler view so items are not populated twice
                    feedAdapter.clear();
                    apiForGetAllFeeds(0, 10, true, "");
                    break;

                case Constant.REQUEST_VIDEO_CAPTURE:
                    try {
                        mediaUri = new MediaUri();
                        mediaUri.isFromGallery = false;
                        mediaUri.mediaType = Constant.VIDEO_STATE;
                        mediaUri.addUri(String.valueOf(data.getData()));
                        Bitmap bitmapThumb = ImageVideoUtil.getVideoToThumbnil(data.getData(), mContext);
                        //Bitmap bitmapThumb = ThumbnailUtils.createVideoThumbnail(String.valueOf(data.getData()), MediaStore.Images.Thumbnails.MICRO_KIND);
                        thumbImage = bitmapThumb;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case Constant.GALLERY_INTENT_CALLED:
                    try {
                        mediaUri = new MediaUri();
                        mediaUri.isFromGallery = true;
                        mediaUri.mediaType = Constant.VIDEO_STATE;
                        mediaUri.addUri(String.valueOf(data.getData()));

                        String filePath = PathUtil.getPath(mContext, Uri.parse(mediaUri.uri));
                        assert filePath != null;
                        File file = new File(filePath);
                        // Get length of file in bytes
                        long fileSizeInBytes = file.length();
                        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                        long fileSizeInKB = fileSizeInBytes / 1024;
                        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                        long fileSizeInMB = fileSizeInKB / 1024;

                       /* if (fileSizeInMB > 50) {
                            mediaUri = null;
                            MyToast.getInstance(mContext).showSmallMessage("You can't upload more then 50mb.");
                        } else {
                            filePath = ImageVideoUtil.generatePath(Uri.parse(mediaUri.uri), mContext);
                            Bitmap thumbBitmap = ImageVideoUtil.getVidioThumbnail(filePath); //ImageVideoUtil.getCompressBitmap();
                            thumbImage = thumbBitmap;
                        }*/

                        filePath = ImageVideoUtil.generatePath(Uri.parse(mediaUri.uri), mContext);
                        Bitmap thumbBitmap = ImageVideoUtil.getVidioThumbnail(filePath); //ImageVideoUtil.getCompressBitmap();
                        thumbImage = thumbBitmap;
                    } catch (Exception e) {
                        e.printStackTrace();
                        mediaUri = null;
                    }

                    break;

                case Constant.ACTIVITY_COMMENT:
                    if (CURRENT_FEED_STATE == Constant.FEED_STATE) {
                        int pos = data.getIntExtra("feedPosition", 0);
                        Feeds feed = (Feeds) data.getSerializableExtra("feed");
                        feeds.get(pos).commentCount = feed.commentCount;
                        feedAdapter.notifyItemChanged(pos);
                    }
                    break;

                case Constant.ADD_STORY:
                    liveUserList.clear();
                    LiveUserInfo me = new LiveUserInfo();
                    me.id = Mualab.currentUser.id;
                    me.userName = "You";
                    me.profileImage = Mualab.currentUser.profileImage;
                    me.storyCount = 0;
                    liveUserList.add(me);
                    getStoryList();
                    break;


            }

        } else {

            switch (requestCode) {
                case Constant.CAMERA_REQUEST:
                case Constant.REQUEST_CODE_CHOOSE:
                case Constant.REQUEST_VIDEO_CAPTURE:
                case Constant.GALLERY_INTENT_CALLED:
                case Constant.GALLERY_KITKAT_INTENT_CALLED:

                    resetView();
                    break;

                case Constant.ADD_STORY:
                    liveUserList.clear();
                    LiveUserInfo me = new LiveUserInfo();
                    me.id = Mualab.currentUser.id;
                    me.userName = "You";
                    me.profileImage = Mualab.currentUser.profileImage;
                    me.storyCount = 0;
                    liveUserList.add(me);
                    getStoryList();
                    break;


                case Constant.POST_FEED_DATA:
                case Constant.ACTIVITY_COMMENT:
                    break;
            }
        }
    }

    private void resetView() {
        mediaUri = null;
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissionAndPicImageOrVideo("Select Image");
                } else {
                    MyToast.getInstance(mContext).showSmallMessage("YOUR  PERMISSION DENIED ");
                }
            }
            break;
        }
    }

    @Override
    public void onClickedUserStory(LiveUserInfo storyUser, int position) {
        if (storyUser.id == Mualab.currentUser.id && storyUser.storyCount == 0) {

            if (Mualab.isStoryUploaded) {
                Intent intent = new Intent(mContext, StoriesActivity.class);
                Bundle args = new Bundle();
                args.putSerializable("ARRAYLIST", liveUserList);
                args.putInt("position", position);
                intent.putExtra("BUNDLE", args);
                startActivity(intent);
            } else {
                startActivityForResult(new Intent(mContext, CameraActivity.class), Constant.ADD_STORY);
            }

        } else {
            Intent intent = new Intent(mContext, StoriesActivity.class);
            Bundle args = new Bundle();
            args.putSerializable("ARRAYLIST", liveUserList);
            args.putInt("position", position);
            intent.putExtra("BUNDLE", args);
            startActivityForResult(intent, Constant.ADD_STORY);
        }
    }

    boolean isShow = false;

    private void showLargeImage(Feeds feeds, int index) {
        View dialogView = View.inflate(mContext, R.layout.dialog_large_image_view, null);
        final Dialog dialog = new Dialog(mContext, R.style.InOutAnimation);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.InOutAnimation;
        dialog.setContentView(dialogView);
        final InstaTag postImage = dialogView.findViewById(R.id.post_image);
        //. postImage.setTouchListnerDisable();
        ImageView btnBack = dialogView.findViewById(R.id.btnBack);
        TextView tvHeaderTitle = dialogView.findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText("Images");


        postImage.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        postImage.setRootWidth(postImage.getMeasuredWidth());
        postImage.setRootHeight(postImage.getMeasuredHeight());

        Glide.with(mContext).load(feeds.feed.get(index)).placeholder(R.drawable.gallery_placeholder)
                .into(postImage.getTagImageView());

        postImage.setImageToBeTaggedEvent(taggedImageEvent);

        ArrayList<TagToBeTagged> tags = feeds.taggedImgMap.get(index);
        if (tags != null && tags.size() != 0) {
            postImage.addTagViewFromTagsToBeTagged("people", tags, false);
            postImage.hideTags("people");
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
                    postImage.showTags("people");
                } else {
                    isShow = false;
                    postImage.hideTags("people");
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

}
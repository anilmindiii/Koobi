package com.mualab.org.user.activity.explore;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mualab.org.user.R;
import com.mualab.org.user.Views.refreshviews.CircleHeaderView;
import com.mualab.org.user.activity.base.BaseFragment;
import com.mualab.org.user.activity.explore.adapter.ExploreGridViewAdapter;
import com.mualab.org.user.activity.explore.adapter.ExploreServiceAdapter;
import com.mualab.org.user.activity.explore.adapter.HeaderGrideAdapter;
import com.mualab.org.user.activity.explore.adapter.ServiceCategoryFilterAdapter;
import com.mualab.org.user.activity.explore.adapter.ServiceFilterAdapter;
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.mualab.org.user.activity.explore.model.ExploreCategoryInfo;
import com.mualab.org.user.activity.feeds.adapter.LiveUserAdapter;
import com.mualab.org.user.activity.tag_module.instatag.TagDetail;
import com.mualab.org.user.activity.tag_module.instatag.TagToBeTagged;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.data.feeds.LiveUserInfo;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.listener.FeedsListner;
import com.mualab.org.user.listener.RecyclerViewScrollListenerProfile;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.ScreenUtils;
import com.mualab.org.user.utils.WrapContentGridLayoutManager;
import com.squareup.picasso.Picasso;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.mualab.org.user.utils.constants.Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE;

public class ExploreFragmentNew extends BaseFragment implements View.OnClickListener,
        ExploreGridViewAdapter.Listener, LiveUserAdapter.Listner {

    public static String TAG = ExploreFragmentNew.class.getName();
    private Context mContext;
    private TextView tv_msg;
    private LinearLayout ll_progress;
    private ProgressBar progress_bar;
    private IndicatorSeekBar seekBarLocation;
    HeaderGrideAdapter feedAdapter;
    private RecyclerView rvFeed;
    private List<Feeds> feeds;
    private BottomSheetBehavior bottomSheetBehavior;
    private String feedType = "image";
    private boolean isPulltoRefrash;
    private FeedsListner feedsListner;
    private RecyclerView rcv_service;
    private ExploreServiceAdapter exploreServiceAdapter;
    private ArrayList<ExploreCategoryInfo.DataBean> dataBeans;
    private ImageView ivFilter;
    private int page = 0;
    private RelativeLayout bottomSheetLayout, rlSelectradiusSeekbar, rlSelectLocation;
    private TextView tv_refine_loc, btn_apply_filter, tv_service_category, tv_services;
    private String location = "", lat = "", lng = "", categoryIds = "", serviceIds = "";
    private String CategoryName = "", ServiceName = "";
    private int seekBarrange = 5;
    private Handler seekBarHandler;
    private RatingBar rating;
    private NestedScrollView nested_scroll_view;
    private int lastCategoryPos = 0;
    private ServiceCategoryFilterAdapter categoryFilterAdapter;
    private RecyclerViewScrollListenerProfile endlesScrollListener;
    private ImageView imageViewHeader;
    private CardView main_layout_header;

    public ExploreFragmentNew() {
    }

    public static ExploreFragmentNew newInstance() {
        ExploreFragmentNew fragment = new ExploreFragmentNew();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof FeedsListner) {
            feedsListner = (FeedsListner) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Mualab.getInstance().cancelPendingRequests(TAG);
        mContext = null;
        tv_msg = null;
        ll_progress = null;
        page = 0;
        endlesScrollListener = null;
        feedAdapter = null;
        feeds = null;
        rvFeed = null;
    }

    @Override
    public void onClickedUserStory(LiveUserInfo storyUser, int position) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feeds = new ArrayList<>();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore_fragment_new, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        //RecyclerView rvMyStory = view.findViewById(R.id.recyclerView);
        rvFeed = view.findViewById(R.id.rvFeed);
        tv_msg = view.findViewById(R.id.tv_msg_noRecord);
        ll_progress = view.findViewById(R.id.ll_loadingBox);
        progress_bar = view.findViewById(R.id.progress_bar);
        rcv_service = view.findViewById(R.id.rcv_service);
        ivFilter = view.findViewById(R.id.ivFilter);
        bottomSheetLayout = view.findViewById(R.id.bottomSheetLayout);
        imageViewHeader = view.findViewById(R.id.imageView);
        main_layout_header = view.findViewById(R.id.main_layout_header);

        view.findViewById(R.id.ed_search).setOnClickListener(this);


        // filter screen views
        TextView tv_reset = view.findViewById(R.id.tv_reset);
        ImageView iv_close_filter = view.findViewById(R.id.iv_close_filter);
        LinearLayout lyServiceCategory = view.findViewById(R.id.lyServiceCategory);
        LinearLayout lyArtistService = view.findViewById(R.id.lyArtistService);
        tv_services = view.findViewById(R.id.tv_services);
        LinearLayout lyRefineLocation = view.findViewById(R.id.lyRefineLocation);
        tv_refine_loc = view.findViewById(R.id.tv_refine_loc);
        rating = view.findViewById(R.id.rating);
        seekBarLocation = view.findViewById(R.id.seekBarLocation);
        rlSelectLocation = view.findViewById(R.id.rlSelectLocation);
        rlSelectradiusSeekbar = view.findViewById(R.id.rlSelectradiusSeekbar);
        btn_apply_filter = view.findViewById(R.id.btn_apply_filter);
        tv_service_category = view.findViewById(R.id.tv_service_category);
        nested_scroll_view = view.findViewById(R.id.nested_scroll_view);

        dataBeans = new ArrayList<>();
        exploreServiceAdapter = new ExploreServiceAdapter(mContext, dataBeans,
                () -> {
                    categoryIds = "";
                    CategoryName = "";
                    serviceIds = "";
                    ServiceName = "";

                    for (int i = 0; i < dataBeans.size(); i++) {
                        if (dataBeans.get(i).isCheckedCategory) {
                            CategoryName = dataBeans.get(i).title + ", " + CategoryName;
                            categoryIds = dataBeans.get(i)._id + "," + categoryIds;

                            for (ExploreCategoryInfo.DataBean.ArtistservicesBean bean : dataBeans.get(i).artistservices) {
                                if (bean.isCheckedservices) {
                                    serviceIds = bean._id + "," + serviceIds;
                                    ServiceName = bean.title + ", " + ServiceName;
                                }
                            }
                        } else {
                            for (ExploreCategoryInfo.DataBean.ArtistservicesBean artistservices : dataBeans.get(i).artistservices) {
                                artistservices.isCheckedservicesLocal = false;
                                artistservices.isCheckedservices = false;
                            }
                        }
                    }


                    if (serviceIds.endsWith(",")) {
                        serviceIds = serviceIds.substring(0, serviceIds.length() - 1);
                    }

                    if (ServiceName.endsWith(", ")) {
                        ServiceName = ServiceName.substring(0, ServiceName.length() - 2);
                    }

                    if (ServiceName.equals("")) {
                        tv_services.setVisibility(View.GONE);
                    } else tv_services.setVisibility(View.VISIBLE);

                    tv_services.setText(ServiceName + "");

                    if (categoryIds.contains(","))
                        categoryIds = categoryIds.substring(0, categoryIds.lastIndexOf(","));

                    if (CategoryName.endsWith(", ")) {
                        CategoryName = CategoryName.substring(0, CategoryName.length() - 2);
                    }
                    if (categoryIds.equals("")) {
                        tv_service_category.setVisibility(View.GONE);
                    } else tv_service_category.setVisibility(View.VISIBLE);
                    tv_service_category.setText(CategoryName);

                    endlesScrollListener.resetState();
                    feeds.clear();
                    page = 0;
                    apiForGetAllFeeds(0, 50, true);
                });
        rcv_service.setAdapter(exploreServiceAdapter);

        tv_reset.setOnClickListener(this);
        iv_close_filter.setOnClickListener(this);
        lyServiceCategory.setOnClickListener(this);
        lyArtistService.setOnClickListener(this);
        lyRefineLocation.setOnClickListener(this);
        btn_apply_filter.setOnClickListener(this);
        main_layout_header.setOnClickListener(this);

        tv_refine_loc.setText(Mualab.currentUser.address);
        lat = Mualab.currentUser.latitude;
        lng = Mualab.currentUser.longitude;

        seekBarLocation.with(mContext)
                .max(20)
                .min(1)
                .build();

        seekBarLocation.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                seekBarrange = seekParams.progress;
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
            }
        });

        seekBarHandler = new Handler();
        seekBarHandler.post(() -> {
            if (seekBarLocation != null) {
                seekBarLocation.setMin(1);
                seekBarLocation.setMax(20);
                seekBarLocation.setProgress(5);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ed_search:
                startActivity(new Intent(mContext, ExplorSearchActivity.class));
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;

            case R.id.ivFilter:
                nested_scroll_view.fullScroll(ScrollView.FOCUS_UP);
                if (bottomSheetLayout.getVisibility() == View.VISIBLE) {
                    bottomSheetLayout.setVisibility(View.GONE);
                } else bottomSheetLayout.setVisibility(View.VISIBLE);

                break;

            case R.id.iv_close_filter:

                exploreServiceAdapter.notifyDataSetChanged();
                bottomSheetLayout.setVisibility(View.GONE);
                page = 0;
                feeds.clear();
                if (categoryFilterAdapter != null)
                    rcv_service.scrollToPosition(categoryFilterAdapter.getLastPos());
                apiForGetAllFeeds(0, 50, true);
                break;

            case R.id.lyRefineLocation:
                if (!ConnectionDetector.isConnected()) {
                    new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if (isConnected) {
                                dialog.dismiss();
                                getAddress();
                            }
                        }
                    }).show();
                } else {
                    getAddress();
                }
                break;

            case R.id.lyServiceCategory:
                serviceCategoryDialog();
                break;

            case R.id.main_layout_header:
                addFragment(GrideToListFragment.newInstance(feeds, 0), true);
                break;

            case R.id.lyArtistService:
                if (categoryIds.equals("")) {
                    MyToast.getInstance(mContext).showDasuAlert("Please select service category");
                    return;
                } else serviceServiceDialog();

                break;


            case R.id.btn_apply_filter:

                exploreServiceAdapter.notifyDataSetChanged();
                bottomSheetLayout.setVisibility(View.GONE);
                page = 0;
                feeds.clear();
                if (categoryFilterAdapter != null)
                    rcv_service.scrollToPosition(categoryFilterAdapter.getLastPos());
                apiForGetAllFeeds(0, 50, true);
                break;

            case R.id.tv_reset:
                categoryIds = "";
                CategoryName = "";
                tv_service_category.setText("");
                tv_service_category.setVisibility(View.GONE);

                serviceIds = "";
                ServiceName = "";
                tv_services.setText("");
                tv_services.setVisibility(View.GONE);

                for (ExploreCategoryInfo.DataBean services : dataBeans) {
                    services.isCheckedCategory = false;
                    services.isCheckedCategoryLocal = false;

                    for (ExploreCategoryInfo.DataBean.ArtistservicesBean bean : services.artistservices) {
                        bean.isCheckedservices = false;
                        bean.isCheckedservicesLocal = false;
                    }
                }

                // feedAdapter.isHideFirstIndex(false);
                tv_refine_loc.setText(Mualab.currentUser.address);
                lat = Mualab.currentUser.latitude;
                lng = Mualab.currentUser.longitude;

                rating.setRating(0);

                seekBarHandler.post(() -> {
                    if (seekBarLocation != null) {
                        seekBarLocation.setMin(1);
                        seekBarLocation.setMax(20);
                        seekBarLocation.setProgress(5);
                    }
                });

                exploreServiceAdapter.notifyDataSetChanged();
                bottomSheetLayout.setVisibility(View.GONE);

                page = 0;
                feeds.clear();
                apiForGetAllFeeds(0, 50, true);
                break;
        }
    }

    private void getAddress() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(getActivity());
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
        } catch (GooglePlayServicesNotAvailableException e) {
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Place place = PlaceAutocomplete.getPlace(mContext, data);

            seekBarHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (seekBarLocation != null) {
                        seekBarLocation.setMin(1);
                        seekBarLocation.setMax(20);
                        seekBarLocation.setProgress(5);
                    }
                }
            });

            rlSelectLocation.setVisibility(View.VISIBLE);
            rlSelectradiusSeekbar.setVisibility(View.VISIBLE);

            tv_refine_loc.setText(place.getName());
            location = "" + place.getName();
            LatLng latLng = place.getLatLng();
            lat = String.valueOf(latLng.latitude);
            lng = String.valueOf(latLng.longitude);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

       /* int mNoOfColumns = ScreenUtils.calculateNoOfColumns(mContext.getApplicationContext());
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL);

        WrapContentGridLayoutManager wgm = new WrapContentGridLayoutManager(mContext,
                mNoOfColumns < 3 ? 3 : mNoOfColumns, LinearLayoutManager.VERTICAL, false);
        rvFeed.setItemAnimator(null);
        rvFeed.setLayoutManager(staggeredGridLayoutManager);
        rvFeed.setHasFixedSize(true);*/

        /////////////////
        feedAdapter = new HeaderGrideAdapter(mContext, new ExSearchTag(), feeds, this,
                feedsListner, true);
        GridLayoutManager manager = new GridLayoutManager(mContext, 3);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return feedAdapter.isHeader(position) ? manager.getSpanCount() : 1;
            }
        });
        rvFeed.setLayoutManager(manager);
        rvFeed.setAdapter(feedAdapter);

        /////////

       /* feedAdapter = new ExploreGridViewAdapter(mContext, new ExSearchTag(), feeds, this,
                feedsListner, true);*/

        endlesScrollListener = new RecyclerViewScrollListenerProfile() {
            @Override
            public void onLoadMore() {
                if (feedAdapter != null) {
                    apiForGetAllFeeds(++page, 50, false);


                }
            }
        };


        rvFeed.setAdapter(feedAdapter);

        final CircleHeaderView header = new CircleHeaderView(getContext());

        if (feeds != null && feeds.size() == 0) {
            updateViewType();
        } else feedAdapter.notifyDataSetChanged();

        GetAllServiceCategory(true);
        ivFilter.setOnClickListener(this);
    }


    private void updateViewType() {
        page = 0;
        endlesScrollListener.resetState();
        int prevSize = feeds.size();

        feeds.clear();
        feedType = "image";
        feedAdapter.notifyItemRangeRemoved(0, prevSize);
        showLoading();
        apiForGetAllFeeds(0, 50, true);

    }

    private void showLoading() {
        ll_progress.setVisibility(View.VISIBLE);
        progress_bar.setVisibility(View.VISIBLE);
        tv_msg.setText(getString(R.string.loading));
    }


    private void GetAllServiceCategory(final boolean isEnableProgress) {
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        GetAllServiceCategory(isEnableProgress);
                    }

                }
            }).show();
        }

        new HttpTask(new HttpTask.Builder(mContext, "allExploreCategory", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                dataBeans.clear();
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        Gson gson = new Gson();
                        ExploreCategoryInfo categoryInfo = gson.fromJson(response, ExploreCategoryInfo.class);

                        dataBeans.addAll(categoryInfo.data);
                        exploreServiceAdapter.notifyDataSetChanged();

                    } else {
                        // goto 3rd screen for register
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }


            }

            @Override
            public void ErrorListener(VolleyError error) {

            }
        })
                .setAuthToken(Mualab.currentUser.authToken)
                .setMethod(Request.Method.GET)
                .setProgress(false))
                .execute("ExploreCategory");
        ll_progress.setVisibility(isEnableProgress ? View.VISIBLE : View.GONE);
    }

    private void apiForGetAllFeeds(final int page, final int feedLimit, final boolean isEnableProgress) {
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetAllFeeds(page, feedLimit, isEnableProgress);
                    }
                }
            }).show();
        }
        Map<String, String> params = new HashMap<>();
        params.put("feedType", feedType);
        params.put("search", "");
        params.put("page", String.valueOf(page));
        params.put("limit", String.valueOf(feedLimit));
        params.put("type", "explore");
        params.put("userId", "" + Mualab.currentUser.id);

        //Aditional key for filter
        params.put("serviceTagId", categoryIds);
        params.put("artistServiceId", serviceIds);
        params.put("radius", String.valueOf(seekBarrange));
        params.put("latitude", lat);
        params.put("longitude", lng);

        String providRating = String.valueOf(rating.getRating());
        if (providRating.contains(".")) {
            providRating = providRating.replace(".0", "");
        }
        params.put("rating", providRating);


        Mualab.getInstance().cancelPendingRequests(this.getClass().getName());
        new HttpTask(new HttpTask.Builder(mContext, "exploreFeeds", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                if (ll_progress != null) ll_progress.setVisibility(View.GONE);
                //if (feedAdapter != null)  feedAdapter.showHideLoading(false);
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
                if (ll_progress != null) ll_progress.setVisibility(View.GONE);
                if (isPulltoRefrash) {
                    isPulltoRefrash = false;
                    //mRefreshLayout.stopRefresh(false, 500);
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
                .setProgress(true)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute(TAG);
        ll_progress.setVisibility(isEnableProgress ? View.VISIBLE : View.GONE);
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
                    //mRefreshLayout.stopRefresh(true, 500);
                    int prevSize = feeds.size();
                    feeds.clear();
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                }

                Gson gson = new Gson();
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
                        JSONArray jsonArray = jsonObject.getJSONArray("peopleTag");
                        if (jsonArray.length() != 0) {

                            for (int j = 0; j < jsonArray.length(); j++) {
                                JSONArray arrayJSONArray = jsonArray.getJSONArray(j);
                                feed.peopleTagList = new ArrayList<>();
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


                        feeds.add(feed);
                        if (feeds.size() != 0) {
                            feeds.get(0).isShow = true;
                        }

                    } catch (JsonParseException e) {
                        e.printStackTrace();
                        //FirebaseCrash.log(e.getLocalizedMessage());
                    }

                } // loop end.

                if (feeds.size() == 0) {
                    rvFeed.setVisibility(View.GONE);
                    tv_msg.setVisibility(View.VISIBLE);
                    tv_msg.setText(getString(R.string.no_data_found));
                } else {
                    tv_msg.setVisibility(View.GONE);

                }

                if (feeds.size() == 1) {
                    rvFeed.setVisibility(View.GONE);
                    main_layout_header.setVisibility(View.VISIBLE);
                    if (!feeds.get(0).feedData.get(0).feedPost.equals("")) {
                        Picasso.with(mContext).load(feeds.get(0).feedData.get(0).feedPost)
                                .placeholder(R.color.gray2)
                                .into(imageViewHeader);
                    }

                } else {
                    main_layout_header.setVisibility(View.GONE);
                }


                if (array.length() != 0)
                    feedAdapter.notifyDataSetChanged();

            } else if (status.equals("fail") && feeds.size() == 0) {
                rvFeed.setVisibility(View.GONE);
                tv_msg.setText(getString(R.string.no_data_found));
                tv_msg.setVisibility(View.VISIBLE);

                if (isPulltoRefrash) {
                    isPulltoRefrash = false;
                    // mRefreshLayout.stopRefresh(false, 500);

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
    public void onFeedClick(List<Feeds> feed, int index) {
        // baseListner.addFragment();
       /* Intent intent = new Intent(mContext, FeedDetailActivity.class);
        Bundle args = new Bundle();
        args.putSerializable("feed",  feed);
        args.putInt("index", index);
        args.putSerializable("feeds", (Serializable) feeds);
        intent.putExtra("BUNDLE",args);
        mContext.startActivity(intent);*/

        addFragment(GrideToListFragment.newInstance(feed, index), true);
    }


    public void addFragment(Fragment fragment, boolean addToBackStack) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fragmentManager = getFragmentManager();
        assert fragmentManager != null;
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            //transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_in, 0, 0);
           /* transaction.setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right,
                    R.anim.slide_in_from_right, R.anim.slide_out_to_left);*/
            transaction.add(R.id.container, fragment, backStackName);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }

    public void serviceCategoryDialog() {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_show_service);
        TextView tvHeader = dialog.findViewById(R.id.tvHeader);
        android.support.v7.widget.SearchView searchview = dialog.findViewById(R.id.searchview);
        searchview.setQueryHint(getResources().getString(R.string.select_service_category));
        tvHeader.setText(getResources().getString(R.string.select_service_category));
        final RecyclerView recyclerView = dialog.findViewById(R.id.recyclerview);
        ImageView img_cancel = dialog.findViewById(R.id.img_cancel);
        Button btn_done = dialog.findViewById(R.id.btn_done);
        final TextView tv_msg = dialog.findViewById(R.id.tv_msg);
        img_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        for (ExploreCategoryInfo.DataBean services : dataBeans) {
            services.isCheckedCategoryLocal = services.isCheckedCategory;
        }

        final ArrayList<ExploreCategoryInfo.DataBean> searchList = new ArrayList<>();
        searchview.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList.clear();
                for (ExploreCategoryInfo.DataBean services : dataBeans) {
                    if (services.title.toLowerCase().trim().contains(newText.toLowerCase().trim())) {
                        searchList.add(services);
                    }
                }
                categoryFilterAdapter = new ServiceCategoryFilterAdapter(searchList, mContext, lastCategoryPos);
                recyclerView.setAdapter(categoryFilterAdapter);
                if (searchList.size() == 0) {
                    tv_msg.setVisibility(View.VISIBLE);
                } else {
                    tv_msg.setVisibility(View.GONE);
                }
                return false;
            }
        });


        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        categoryFilterAdapter = new ServiceCategoryFilterAdapter(dataBeans, mContext, lastCategoryPos);
        recyclerView.setAdapter(categoryFilterAdapter);

        dialog.setOnDismissListener(dialog1 -> {
            for (ExploreCategoryInfo.DataBean services : dataBeans) {
                services.isCheckedCategoryLocal = false;
            }
        });

        btn_done.setOnClickListener(v -> {

            categoryIds = "";
            serviceIds = "";
            ServiceName = "";
            CategoryName = "";
            for (ExploreCategoryInfo.DataBean services : dataBeans) {
                services.isCheckedCategory = services.isCheckedCategoryLocal;
                if (services.isCheckedCategory) {
                    CategoryName = services.title + ", " + CategoryName;
                    categoryIds = services._id + "," + categoryIds;
                    for (ExploreCategoryInfo.DataBean.ArtistservicesBean bean : services.artistservices) {
                        if (bean.isCheckedservices) {
                            serviceIds = bean._id + "," + serviceIds;
                            ServiceName = bean.title + ", " + ServiceName;
                        }
                    }
                } else {
                    for (ExploreCategoryInfo.DataBean.ArtistservicesBean bean : services.artistservices) {
                        bean.isCheckedservices = false;
                        bean.isCheckedservicesLocal = false;
                    }
                }
            }

            if (serviceIds.endsWith(",")) {
                serviceIds = serviceIds.substring(0, serviceIds.length() - 1);
            }

            if (ServiceName.endsWith(", ")) {
                ServiceName = ServiceName.substring(0, ServiceName.length() - 2);
            }
            if (ServiceName.equals("")) {
                tv_services.setVisibility(View.GONE);
            } else tv_services.setVisibility(View.VISIBLE);

            tv_services.setText(ServiceName + "");

            if (categoryIds.endsWith(",")) {
                categoryIds = categoryIds.substring(0, categoryIds.length() - 1);
            }

            if (CategoryName.endsWith(", ")) {
                CategoryName = CategoryName.substring(0, CategoryName.length() - 2);
            }

            if (CategoryName.equals("")) {
                tv_service_category.setVisibility(View.GONE);
            } else tv_service_category.setVisibility(View.VISIBLE);

            tv_service_category.setText(CategoryName + "");
            dialog.dismiss();


        });

        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();


    }

    public void serviceServiceDialog() {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_show_service);
        TextView tvHeader = dialog.findViewById(R.id.tvHeader);
        android.support.v7.widget.SearchView searchview = dialog.findViewById(R.id.searchview);
        searchview.setQueryHint("Search Service");
        tvHeader.setText(getResources().getString(R.string.text_services));
        final RecyclerView recyclerView = dialog.findViewById(R.id.recyclerview);
        ImageView img_cancel = dialog.findViewById(R.id.img_cancel);
        Button btn_done = dialog.findViewById(R.id.btn_done);
        final TextView tv_msg = dialog.findViewById(R.id.tv_msg);
        img_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ArrayList<ExploreCategoryInfo.DataBean.ArtistservicesBean> artistservicesBeans =
                new ArrayList<>();

        for (ExploreCategoryInfo.DataBean services : dataBeans) {
            if (services.isCheckedCategory) {
                artistservicesBeans.addAll(services.artistservices);
            }
        }

        for (ExploreCategoryInfo.DataBean.ArtistservicesBean services : artistservicesBeans) {
            services.isCheckedservicesLocal = services.isCheckedservices;
        }

        if (artistservicesBeans.size() == 0) {
            tv_msg.setVisibility(View.VISIBLE);
        } else {
            tv_msg.setVisibility(View.GONE);
        }

        final ArrayList<ExploreCategoryInfo.DataBean.ArtistservicesBean> searchList = new ArrayList<>();
        searchview.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList.clear();
                for (ExploreCategoryInfo.DataBean.ArtistservicesBean services : artistservicesBeans) {
                    if (services.title.toLowerCase().trim().contains(newText.toLowerCase().trim())) {
                        searchList.add(services);
                    }
                }
                recyclerView.setAdapter(new ServiceFilterAdapter(searchList, mContext));
                if (searchList.size() == 0) {
                    tv_msg.setVisibility(View.VISIBLE);
                } else {
                    tv_msg.setVisibility(View.GONE);
                }
                return false;
            }
        });


        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new ServiceFilterAdapter(artistservicesBeans, mContext));

        dialog.setOnDismissListener(dialog1 -> {
            for (ExploreCategoryInfo.DataBean.ArtistservicesBean services : artistservicesBeans) {
                services.isCheckedservicesLocal = false;
            }
        });

        btn_done.setOnClickListener(v -> {
            serviceIds = "";
            ServiceName = "";
            for (ExploreCategoryInfo.DataBean.ArtistservicesBean services : artistservicesBeans) {
                services.isCheckedservices = services.isCheckedservicesLocal;
                if (services.isCheckedservices) {
                    ServiceName = services.title + ", " + ServiceName;
                    serviceIds = services._id + "," + serviceIds;
                }
            }

            if (serviceIds.endsWith(",")) {
                serviceIds = serviceIds.substring(0, serviceIds.length() - 1);
            }

            if (ServiceName.endsWith(", ")) {
                ServiceName = ServiceName.substring(0, ServiceName.length() - 2);
            }

            if (ServiceName.equals("")) {
                tv_services.setVisibility(View.GONE);
            } else tv_services.setVisibility(View.VISIBLE);

            tv_services.setText(ServiceName + "");

            dialog.dismiss();


        });

        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();


    }
}

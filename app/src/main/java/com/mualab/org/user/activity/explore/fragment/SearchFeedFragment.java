package com.mualab.org.user.activity.explore.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mualab.org.user.R;
import com.mualab.org.user.Views.refreshviews.CircleHeaderView;
import com.mualab.org.user.Views.refreshviews.OnRefreshListener;
import com.mualab.org.user.Views.refreshviews.RjRefreshLayout;
import com.mualab.org.user.activity.explore.GrideToListFragment;
import com.mualab.org.user.activity.explore.adapter.ExploreGridViewAdapter;
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.mualab.org.user.activity.tag_module.instatag.TagDetail;
import com.mualab.org.user.activity.tag_module.instatag.TagToBeTagged;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.listener.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.ScreenUtils;
import com.mualab.org.user.utils.WrapContentGridLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SearchFeedFragment extends Fragment implements ExploreGridViewAdapter.Listener {
    public static String TAG = SearchFeedFragment.class.getName();

    private Context mContext;
    private TextView tv_msg, title;
    private ProgressBar progress_bar;
    private LinearLayout ll_progress;
    private RecyclerView rvFeed;
    private RjRefreshLayout mRefreshLayout;
    private EndlessRecyclerViewScrollListener endlesScrollListener;
    private List<Feeds> feeds;
    private ExploreGridViewAdapter feedAdapter;

    // private int fragCount;
    private ExSearchTag exSearchTag;
    private boolean isPulltoRefrash;


    public SearchFeedFragment() {
        // Required empty public constructor
    }


    public static SearchFeedFragment newInstance(ExSearchTag searchKey) {
        SearchFeedFragment fragment = new SearchFeedFragment();
        Bundle args = new Bundle();
        args.putSerializable("searchKey", searchKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feeds = new ArrayList<>();

        if (getArguments() != null) {
            //fragCount = getArguments().getInt("fragCount");
            exSearchTag = (ExSearchTag) getArguments().getSerializable("searchKey");


        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_feed, container, false);
        title = view.findViewById(R.id.title);
        return view;
    }

    private void initView(View view) {
        rvFeed = view.findViewById(R.id.rvFeed);
        tv_msg = view.findViewById(R.id.tv_msg);
        progress_bar = view.findViewById(R.id.progress_bar);
        ll_progress = view.findViewById(R.id.ll_loadingBox);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        int mNoOfColumns = ScreenUtils.calculateNoOfColumns(mContext.getApplicationContext());
        WrapContentGridLayoutManager wgm = new WrapContentGridLayoutManager(mContext,
                mNoOfColumns < 3 ? 3 : mNoOfColumns, LinearLayoutManager.VERTICAL, false);
        rvFeed.setItemAnimator(null);
        rvFeed.setLayoutManager(wgm);
        rvFeed.setHasFixedSize(true);

        if (exSearchTag.type == 0) {
            title.setText("Service Tags");
        }

       /* Drawable divider = ContextCompat.getDrawable(mContext, R.drawable.divider_transprant);
        rvFeed.addItemDecoration(new GridDividerItemDecoration(divider, divider, mNoOfColumns));*/

        feedAdapter = new ExploreGridViewAdapter(mContext, exSearchTag, feeds, this);
        endlesScrollListener = new EndlessRecyclerViewScrollListener(wgm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (feedAdapter != null) feedAdapter.showHideLoading(true);
                searchFeed(page, false);
            }
        };

        rvFeed.setAdapter(feedAdapter);
        rvFeed.addOnScrollListener(endlesScrollListener);


        mRefreshLayout = view.findViewById(R.id.mSwipeRefreshLayout);
        final CircleHeaderView header = new CircleHeaderView(getContext());
        mRefreshLayout.addHeader(header);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                endlesScrollListener.resetState();
                isPulltoRefrash = true;
                searchFeed(0, false);
            }

            @Override
            public void onLoadMore() {
                Log.e(TAG, "onLoadMore: ");
            }
        });

        showLoading();
        searchFeed(0, true);
    }

    private void showLoading() {
        ll_progress.setVisibility(View.VISIBLE);
        progress_bar.setVisibility(View.VISIBLE);
        tv_msg.setText(getString(R.string.loading));
    }

    /*  @Override
      public void onFeedClick(Feeds feed, int index) {
        *//*  Intent intent = new Intent(mContext, FeedDetailActivity.class);
        intent.putExtra("feed",  feed);
        intent.putExtra("feeds", (Serializable) feeds);
        intent.putExtra("index", index);
        startActivity(intent);*//*

      //Anil...
        *//*Intent intent = new Intent(mContext, FeedDetailActivity.class);
        Bundle args = new Bundle();
        args.putSerializable("feed",  feed);
        args.putInt("index", index);
        args.putSerializable("feeds", (Serializable) feeds);
        intent.putExtra("BUNDLE",args);
        startActivity(intent);*//*
        //startActivity(new Intent(mContext, FeedDetailActivity.class).putExtra("feed",feed));
    }
*/
    /*Api call and parse methods */
    private void searchFeed(final int page, final boolean isEnableProgress) {

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        searchFeed(page, isEnableProgress);
                    }

                }
            }).show();
        }


        Map<String, String> params = new HashMap<>();
        if (exSearchTag.type == ExSearchTag.SearchType.TOP || exSearchTag.type == ExSearchTag.SearchType.PEOPLE) {
            params.put("userId", "" + "" + Mualab.currentUser.id);
            params.put("findData", "" + exSearchTag.id);
        } else {
            params.put("userId", "" + Mualab.currentUser.id);
            //  params.put("userId", ""+exSearchTag.id);
            params.put("findData", "" + exSearchTag.title.replace("#", ""));
        }
        params.put("loginUserId", "" + Mualab.currentUser.id);
        params.put("type", exSearchTag.getType());
        params.put("feedType", "");
        params.put("search", "");
        params.put("page", String.valueOf(page));
        params.put("limit", "20");

        if (exSearchTag.type == 0)
            params.put("peopleType", "top");
        // params.put("appType", "user");
        Mualab.getInstance().cancelPendingRequests(this.getClass().getName());
        new HttpTask(new HttpTask.Builder(mContext, "userFeed", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                if (ll_progress != null) ll_progress.setVisibility(View.GONE);
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
                if (ll_progress != null) ll_progress.setVisibility(View.GONE);
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
        ll_progress.setVisibility(isEnableProgress ? View.VISIBLE : View.GONE);
    }

    private void ParseAndUpdateUI(final String response) {
        progress_bar.setVisibility(View.GONE);
        try {
            JSONObject js = new JSONObject(response);
            String status = js.getString("status");
            // String message = js.getString("message");

            if (status.equalsIgnoreCase("success")) {
                rvFeed.setVisibility(View.VISIBLE);
                JSONArray array = js.getJSONArray("AllUserFeeds");
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


                        feeds.add(feed);

                    } catch (JsonParseException e) {
                        e.printStackTrace();
                        //FirebaseCrash.log(e.getLocalizedMessage());
                    }

                } // loop end.

                feedAdapter.notifyDataSetChanged();

                if (feeds.size() == 0) {
                    rvFeed.setVisibility(View.GONE);
                    ll_progress.setVisibility(View.VISIBLE);
                    tv_msg.setVisibility(View.VISIBLE);
                    tv_msg.setText(getString(R.string.no_data_found));
                }


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
            progress_bar.setVisibility(View.GONE);
            tv_msg.setText(getString(R.string.msg_some_thing_went_wrong));
            feedAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFeedClick(List<Feeds> feed, int index) {
        /*Intent intent1 = new Intent(mContext, FeedSingleActivity.class);
        intent1.putExtra("feedId", feed.get(index)._id + "");
        intent1.putExtra("userType", feed.get(index).userInfo.get(0).userType);
        mContext.startActivity(intent1);*/

        addFragment(GrideToListFragment.newInstance(feed,index, false), true);    }

    public void addFragment(Fragment fragment, boolean addToBackStack) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fragmentManager = getFragmentManager();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_in, 0, 0);
           /* transaction.setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right,
                    R.anim.slide_in_from_right, R.anim.slide_out_to_left);*/
            transaction.add(R.id.container , fragment, backStackName);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }
}

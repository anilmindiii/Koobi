package com.mualab.org.user.activity.explore;

import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.base.BaseActivity;
import com.mualab.org.user.activity.explore.adapter.ExploreGridViewAdapter;
import com.mualab.org.user.activity.explore.adapter.HeaderGrideAdapter;
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.mualab.org.user.activity.tag_module.instatag.TagDetail;
import com.mualab.org.user.activity.tag_module.instatag.TagToBeTagged;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.listener.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.listener.FeedsListner;
import com.mualab.org.user.utils.ConnectionDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopDetailsActivity extends BaseActivity {
    private ExSearchTag exSearchTag;
    private LinearLayout ll_progress;
    HeaderGrideAdapter feedAdapter;
    private RecyclerView rvFeed;
    private List<Feeds> feeds;
    private ProgressBar progress_bar;
    private TextView tv_msg,tvHeaderTitle;
    private ImageView btnBack;
    private EndlessRecyclerViewScrollListener endlesScrollListener;


    private FeedsListner feedsListner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_details);

        ll_progress = findViewById(R.id.ll_loadingBox);
        rvFeed = findViewById(R.id.rvFeed);
        progress_bar = findViewById(R.id.progress_bar);
        tv_msg = findViewById(R.id.tv_msg);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);


        feeds = new ArrayList<>();

        if (getIntent().getSerializableExtra("exSearchTag") != null) {
            //fragCount = getArguments().getInt("fragCount");
            exSearchTag = (ExSearchTag) getIntent().getSerializableExtra("exSearchTag");

            tvHeaderTitle.setText(exSearchTag.title);
        }

        feedAdapter = new HeaderGrideAdapter(getActivity(), new ExSearchTag(), feeds,
                new ExploreGridViewAdapter.Listener() {
            @Override
            public void onFeedClick(List<Feeds> feed, int index) {
                addFragment(GrideToListFragment.newInstance(feed, index), true);
            }
        },
                feedsListner, true);

        GridLayoutManager manager = new GridLayoutManager(getActivity(), 2);

        endlesScrollListener = new EndlessRecyclerViewScrollListener(manager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
               // if (feedAdapter != null) feedAdapter.showHideLoading(true);
                searchFeed(page, false);
            }
        };

        rvFeed.setAdapter(feedAdapter);
        rvFeed.addOnScrollListener(endlesScrollListener);



        rvFeed.setLayoutManager(manager);
        rvFeed.setAdapter(feedAdapter);
        searchFeed(0, true);

    }

    public void addFragment(Fragment fragment, boolean addToBackStack) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fragmentManager = getSupportFragmentManager();
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

    /*Api call and parse methods */
    private void searchFeed(final int page, final boolean isEnableProgress) {

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(getActivity(), new NoConnectionDialog.Listner() {
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
        new HttpTask(new HttpTask.Builder(getActivity(), "userFeed", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                if (ll_progress != null) ll_progress.setVisibility(View.GONE);
              //  if (feedAdapter != null) feedAdapter.showHideLoading(false);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        //removeProgress();
                        ParseAndUpdateUI(response);
                    } else MyToast.getInstance(getActivity()).showSmallMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    // MyToast.getInstance(getActivity()).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                MyToast.getInstance(getActivity()).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
            }
        })
                .setAuthToken(Mualab.currentUser.authToken)
                .setParam(params)
                .setMethod(Request.Method.POST)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute("userFeed");
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
                feedAdapter.notifyDataSetChanged();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            progress_bar.setVisibility(View.GONE);
            tv_msg.setText(getString(R.string.msg_some_thing_went_wrong));
            feedAdapter.notifyDataSetChanged();
        }
    }
}

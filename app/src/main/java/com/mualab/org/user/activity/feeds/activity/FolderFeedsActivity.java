package com.mualab.org.user.activity.feeds.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
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
import com.mualab.org.user.Views.refreshviews.CircleHeaderView;
import com.mualab.org.user.Views.refreshviews.OnRefreshListener;
import com.mualab.org.user.Views.refreshviews.RjRefreshLayout;
import com.mualab.org.user.activity.base.BaseListner;
import com.mualab.org.user.activity.dialogs.NameDisplayDialog;
import com.mualab.org.user.activity.feeds.adapter.FeedAdapter;
import com.mualab.org.user.activity.feeds.fragment.FeedsFragment;
import com.mualab.org.user.activity.feeds.fragment.LikeFragment;
import com.mualab.org.user.activity.feeds.fragment.SingleFeedLikeFragment;
import com.mualab.org.user.activity.feeds.listener.MyClickOnPostMenu;
import com.mualab.org.user.activity.tag_module.instatag.TagDetail;
import com.mualab.org.user.activity.tag_module.instatag.TagToBeTagged;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.WrapContentLinearLayoutManager;
import com.mualab.org.user.utils.constants.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderFeedsActivity extends AppCompatActivity implements View.OnClickListener,
        FeedAdapter.Listener{

    public static String TAG = FolderFeedsActivity.class.getName();
    private int folderId = 0;
    private List<Feeds> feeds;
    private RecyclerView rvFeed;
    private ProgressBar progress_bar;
    private FeedAdapter feedAdapter;
    private TextView tvFilter,tv_no_data_msg;
    private boolean isPulltoRefrash = false;
    private RjRefreshLayout mRefreshLayout;
    private String folderName;
    private LinearLayout ll_filter;
    private String feedType = "";
    private ArrayList<String> arrayList = new ArrayList<>();
    private CardView header;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_feeds);

        folderId = getIntent().getIntExtra("folderId",0);
        folderName = getIntent().getStringExtra("folderName");

        feeds = new ArrayList<>();
        rvFeed = findViewById(R.id.rvFeed);
        progress_bar = findViewById(R.id.progress_bar);
        tv_no_data_msg = findViewById(R.id.tv_msg);
        ll_filter = findViewById(R.id.ll_filter);
        tvFilter = findViewById(R.id.tvFilter);
        header = findViewById(R.id.header);
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(folderName);

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        header.setOnClickListener(v->{
            MyClickOnPostMenu.getMentIntance().setMenuClick();
        });


        WrapContentLinearLayoutManager lm = new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvFeed.setItemAnimator(null);
        rvFeed.setLayoutManager(lm);
        rvFeed.setHasFixedSize(true);


        feedAdapter = new FeedAdapter(this,"", feeds, this);
        feedAdapter.isFromFolderActivity = true;
        rvFeed.setAdapter(feedAdapter);

        mRefreshLayout =  findViewById(R.id.mSwipeRefreshLayout);
        final CircleHeaderView header = new CircleHeaderView(this);
        mRefreshLayout.addHeader(header);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                isPulltoRefrash = true;
                apiForGetAllFeeds();
            }

            @Override
            public void onLoadMore() {
                Log.e(TAG, "onLoadMore: ");
            }
        });

        arrayList.add("All");
        arrayList.add("Photo");
        arrayList.add("Video");

        progress_bar.setVisibility(View.VISIBLE);
        apiForGetAllFeeds();

        ll_filter.setOnClickListener(this::onClick);
    }


    private void apiForGetAllFeeds() {

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(FolderFeedsActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetAllFeeds();
                    }

                }
            }).show();
        }
        Map<String, String> params = new HashMap<>();
        params.put("feedType", feedType);
        params.put("folderId", String.valueOf(folderId));
        params.put("userId", "" + Mualab.currentUser.id);
        params.put("loginUserId", "" + Mualab.currentUser.id);
        Mualab.getInstance().cancelPendingRequests(this.getClass().getName());
        new HttpTask(new HttpTask.Builder(FolderFeedsActivity.this, "getFolderFeed", new HttpResponceListner.Listener() {
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
                    } else {
                        if (feeds.size() == 0) {
                            tv_no_data_msg.setText(R.string.no_res_found);
                            tv_no_data_msg.setVisibility(View.VISIBLE);

                            if (isPulltoRefrash) {
                                isPulltoRefrash = false;
                                mRefreshLayout.stopRefresh(false, 500);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // MyToast.getInstance(FolderFeedsActivity.this).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
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
                //MyToast.getInstance(FolderFeedsActivity.this).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
            }
        })
                .setAuthToken(Mualab.currentUser.authToken)
                .setParam(params)
                .setMethod(Request.Method.POST)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute("getFolderFeed");

    }

    @SuppressLint("UseSparseArrays")
    private void ParseAndUpdateUI(final String response) {

        try {
            JSONObject js = new JSONObject(response);
            String status = js.getString("status");
            // String message = js.getString("message");

            if (status.equalsIgnoreCase("success")) {
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
                       // tmp get data and set into actual json format
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
                                        tag.tabType = tagOjb.getString("tabType");
                                        tag.tagId = tagOjb.getString("tagId");
                                        tag.title = tagOjb.getString("title");
                                        tag.userType = tagOjb.getString("userType");
                                        } else {
                                            JSONObject details = tagOjb.getJSONObject(unique_tag_id);
                                            tag = gson.fromJson(String.valueOf(details), TagDetail.class);
                                        tag.tabType = details.getString("tabType");
                                        tag.tagId = details.getString("tagId");
                                        tag.title = details.getString("title");
                                        tag.userType = details.getString("userType");
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
                feedAdapter.notifyDataSetChanged();

                if (feeds.size() == 0) {
                    tv_no_data_msg.setText(R.string.no_res_found);
                    tv_no_data_msg.setVisibility(View.VISIBLE);

                    if (isPulltoRefrash) {
                        isPulltoRefrash = false;
                        mRefreshLayout.stopRefresh(false, 500);
                    }
                }else  {
                    tv_no_data_msg.setText(R.string.no_res_found);
                    tv_no_data_msg.setVisibility(View.GONE);
                }



            } else if (status.equals("fail") && feeds.size() == 0) {
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
            //MyToast.getInstance(FolderFeedsActivity.this).showSmallCustomToast(getString(R.string.alert_something_wenjt_wrong));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case Constant.ACTIVITY_COMMENT:
                    int pos = data.getIntExtra("feedPosition", 0);
                    Feeds feed = (Feeds) data.getSerializableExtra("feed");
                    feeds.get(pos).commentCount = data.getIntExtra("commentCount", 0);
                    feedAdapter.notifyItemChanged(pos);
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_filter:

                NameDisplayDialog.newInstance(getString(R.string.post_type), arrayList, pos -> {
                    String data = arrayList.get(pos);
                    int prevSize = feeds.size();
                    switch (data) {
                        case "All":
                            tvFilter.setText(R.string.all);
                            feeds.clear();
                            feedType = "";
                            feedAdapter.notifyItemRangeRemoved(0, prevSize);
                            apiForGetAllFeeds();
                            break;

                        case "Photo":
                            tvFilter.setText(R.string.photo);
                            feeds.clear();
                            feedType = "image";
                            feedAdapter.notifyItemRangeRemoved(0, prevSize);
                            apiForGetAllFeeds();
                            break;

                        case "Video":
                            tvFilter.setText(R.string.video);
                            feeds.clear();
                            feedType = "video";
                            feedAdapter.notifyItemRangeRemoved(0, prevSize);
                            apiForGetAllFeeds();
                            break;

                    }
                }).show(getSupportFragmentManager());

                break;
        }
    }

    @Override
    public void onCommentBtnClick(Feeds feed, int pos) {
        Intent intent = new Intent(this, CommentsActivity.class);
        intent.putExtra("feed_id", feed._id);
        intent.putExtra("feedPosition", pos);
        intent.putExtra("feed", feed);
        intent.putExtra("commentCount", feed.commentCount);
        startActivityForResult(intent, Constant.ACTIVITY_COMMENT);

    }

    @Override
    public void onLikeListClick(Feeds feed) {
        Intent intent = new Intent(this, LikeFeedActivity.class);
        intent.putExtra("feedId", feed._id);
        intent.putExtra("userId", Mualab.currentUser.userId);
        startActivity(intent);
    }

    @Override
    public void onFeedClick(Feeds feed, int index, View v) {
        ArrayList<String> tempList = new ArrayList<>();
        for(int i=0; i<feed.feedData.size(); i++){
            tempList.add(feed.feedData.get(i).feedPost);
        }

        Intent intent = new Intent(this, PreviewImageActivity.class);
        intent.putExtra("imageArray", tempList);
        intent.putExtra("startIndex", index);
        startActivity(intent);
    }

    @Override
    public void onClickProfileImage(Feeds feed, ImageView v) {

    }

}

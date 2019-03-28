package com.mualab.org.user.activity.artist_profile.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.Views.refreshviews.CircleHeaderView;
import com.mualab.org.user.Views.refreshviews.OnRefreshListener;
import com.mualab.org.user.Views.refreshviews.RjRefreshLayout;
import com.mualab.org.user.activity.artist_profile.adapter.FollowersAdapter;
import com.mualab.org.user.activity.artist_profile.model.Followers;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.listener.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowersActivity extends AppCompatActivity {
    private RecyclerView rycFollowers;
    private TextView tvNoData;
    private List<Followers> followers;
    private FollowersAdapter followersAdapter;
    private boolean isFollowers;
    private EndlessRecyclerViewScrollListener scrollListener;
    private String userId;

    private RjRefreshLayout mRefreshLayout;
    private boolean isPulltoRefrash = false;
    private TextView tv_no_data_msg;
    private ProgressBar progress_bar;
    private EditText ed_search;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        initView();


        ed_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String quary = ed_search.getText().toString().trim();
                //scrollListener.resetState();

                followers.clear();
                followersAdapter.notifyDataSetChanged();

                if (isFollowers)
                    apiForGetFollowers(0, quary);
                else
                    apiForGetFollowing(0, quary);


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initView() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isFollowers = extras.getBoolean("isFollowers", isFollowers);
            userId = extras.getString("artistId");
        }

        followers = new ArrayList<>();
        followersAdapter = new FollowersAdapter(FollowersActivity.this, followers, isFollowers);

        setView();
    }

    private void setView() {

        tv_no_data_msg = findViewById(R.id.tv_no_data_msg);
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        if (isFollowers)
            tvHeaderTitle.setText(getString(R.string.text_follower));
        else
            tvHeaderTitle.setText(getString(R.string.text_following));
        tvNoData = findViewById(R.id.tvNoData);
        progress_bar = findViewById(R.id.progress_bar);
        ed_search = findViewById(R.id.ed_search);

        rycFollowers = findViewById(R.id.rycFollowers);
        LinearLayoutManager layoutManager = new LinearLayoutManager(FollowersActivity.this, LinearLayoutManager.VERTICAL, false);
        layoutManager.scrollToPositionWithOffset(0, 0);
        rycFollowers.setLayoutManager(layoutManager);
        rycFollowers.setAdapter(followersAdapter);
        mRefreshLayout = findViewById(R.id.mSwipeRefreshLayout);
        final CircleHeaderView header = new CircleHeaderView(FollowersActivity.this);
        mRefreshLayout.addHeader(header);


        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                scrollListener.resetState();
                isPulltoRefrash = true;
                if (isFollowers)
                    apiForGetFollowers(0, "");
                else
                    apiForGetFollowing(0, "");
            }

            @Override
            public void onLoadMore() {

            }
        });


        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (scrollListener == null) {
            scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    // followersAdapter.showLoading(true);
                    String search = ed_search.getText().toString().trim();
                    if (isFollowers) {
                        if (search.equals(""))
                            apiForGetFollowers(page, "");
                    } else {
                        if (search.equals(""))
                            apiForGetFollowing(page, "");
                    }


                }
            };
        }

        rycFollowers.addOnScrollListener(scrollListener);

        if (followers.size() == 0) {
            progress_bar.setVisibility(View.VISIBLE);
            if (isFollowers)
                apiForGetFollowers(0, "");
            else
                apiForGetFollowing(0, "");
        }
    }

    private void apiForGetFollowers(final int page, final String search) {
        progress_bar.setVisibility(View.VISIBLE);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(FollowersActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetFollowers(page, search);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("loginUserId", String.valueOf(user.id));
        params.put("page", String.valueOf(page));
        params.put("limit", "10");
        params.put("userName", search);

        HttpTask task = new HttpTask(new HttpTask.Builder(FollowersActivity.this, "followerList", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    progress_bar.setVisibility(View.GONE);
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");


                    if (status.equalsIgnoreCase("success")) {
                        // followersAdapter.showLoading(false);

                        if (page == 0) {
                            followers.clear();
                        }

                        tvNoData.setVisibility(View.GONE);

                        JSONArray jsonArray = js.getJSONArray("followerList");
                        if (isPulltoRefrash) {
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(true, 500);
                            int prevSize = followers.size();
                            followers.clear();
                            followersAdapter.notifyItemRangeRemoved(0, prevSize);
                        }

                        if (jsonArray != null && jsonArray.length() != 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Gson gson = new Gson();
                                JSONObject object = jsonArray.getJSONObject(i);
                                Followers item = gson.fromJson(String.valueOf(object), Followers.class);
                                followers.add(item);
                            }
                        }

                        if (followers.size() == 0) {
                            tv_no_data_msg.setVisibility(View.VISIBLE);
                        } else tv_no_data_msg.setVisibility(View.GONE);

                        followersAdapter.notifyDataSetChanged();
                    } else if (page == 0) {
                        followers.clear();
                        followersAdapter.notifyDataSetChanged();

                        if (followers.size() == 0) {
                            tv_no_data_msg.setVisibility(View.VISIBLE);
                        } else tv_no_data_msg.setVisibility(View.GONE);


                        tvNoData.setText(message);
                        if (isPulltoRefrash) {
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(false, 500);
                        }
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(FollowersActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try {

                    if (isPulltoRefrash) {
                        isPulltoRefrash = false;
                        mRefreshLayout.stopRefresh(false, 500);
                        int prevSize = followers.size();
                        followers.clear();
                        followersAdapter.notifyItemRangeRemoved(0, prevSize);
                    }
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")) {
                        Mualab.getInstance().getSessionManager().logout();
                        // MyToast.getInstance(BookingActivity.this).showDasuAlert(helper.error_Messages(error));
                    }
                } catch (Exception e) {

                    tvNoData.setText("Something went wrong!");
                    progress_bar.setVisibility(View.GONE);
                    e.printStackTrace();
                }


            }
        })
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

    private void apiForGetFollowing(final int page, final String search) {
        progress_bar.setVisibility(View.VISIBLE);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(FollowersActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetFollowing(page, search);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("loginUserId", String.valueOf(user.id));
        params.put("page", String.valueOf(page));
        params.put("limit", "10");
        params.put("userName", search);

        HttpTask task = new HttpTask(new HttpTask.Builder(FollowersActivity.this, "followingList", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    progress_bar.setVisibility(View.GONE);
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        // followersAdapter.showLoading(false);

                        if (page == 0) {
                            followers.clear();
                        }

                        tv_no_data_msg.setVisibility(View.GONE);

                        JSONArray jsonArray = js.getJSONArray("followingList");
                        if (isPulltoRefrash) {
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(true, 500);
                            int prevSize = followers.size();
                            followers.clear();
                            followersAdapter.notifyItemRangeRemoved(0, prevSize);
                        }

                        if (jsonArray != null && jsonArray.length() != 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Gson gson = new Gson();
                                JSONObject object = jsonArray.getJSONObject(i);
                                Followers item = gson.fromJson(String.valueOf(object), Followers.class);
                                followers.add(item);
                            }
                        }

                        if (followers.size() == 0) {
                            tv_no_data_msg.setVisibility(View.VISIBLE);
                        } else tv_no_data_msg.setVisibility(View.GONE);

                        followersAdapter.notifyDataSetChanged();
                    } else if (page == 0) {
                        followers.clear();
                        followersAdapter.notifyDataSetChanged();


                        if (followers.size() == 0) {
                            tv_no_data_msg.setVisibility(View.VISIBLE);
                        } else tv_no_data_msg.setVisibility(View.GONE);

                        if (isPulltoRefrash) {
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(false, 500);

                        }
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    progress_bar.setVisibility(View.GONE);
                    tv_no_data_msg.setVisibility(View.VISIBLE);
                    tvNoData.setText("Something went wrong!");
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try {
                    if (isPulltoRefrash) {
                        isPulltoRefrash = false;
                        mRefreshLayout.stopRefresh(false, 500);
                        int prevSize = followers.size();
                        followers.clear();
                        followersAdapter.notifyItemRangeRemoved(0, prevSize);
                    }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}

package com.mualab.org.user.activity.notification.fragment;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.base.BaseFragment;
import com.mualab.org.user.activity.feeds.activity.CommentsActivity;
import com.mualab.org.user.activity.feeds.model.Comment;
import com.mualab.org.user.activity.notification.adapter.NotificationAdapter;
import com.mualab.org.user.activity.notification.model.NotificationInfo;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.listener.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.utils.ConnectionDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mindiii on 17/9/18.
 */

public class NotificationFragment extends BaseFragment {
    public static String TAG = NotificationFragment.class.getName();
    private String mParam1;
    private RecyclerView recyclerView;
    private TextView tv_no_notifications;
    private ArrayList<NotificationInfo.NotificationListBean> NotificationList;
    private EndlessRecyclerViewScrollListener scrollListener;
    private NotificationAdapter adapter;
    private int count;


    public NotificationFragment() {
        // Required empty public constructor
    }


    public static NotificationFragment newInstance(String param1, String param2) {
        NotificationFragment fragment = new NotificationFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString("param1");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.activity_user_profile,container,false);

        recyclerView = view.findViewById(R.id.recyclerView);
        tv_no_notifications = view.findViewById(R.id.tv_no_notifications);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(linearLayoutManager);
        NotificationList = new ArrayList<>();
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                getNotificationList(page);
            }
        };

        // Adds the scroll listener to RecyclerView
        recyclerView.addOnScrollListener(scrollListener);
        adapter = new NotificationAdapter(NotificationList,mContext);
        recyclerView.setAdapter(adapter);

        getNotificationList(0);
    }



    private void getNotificationList(int pageNo) {
        Progress.show(mContext);

        HashMap<String, String> map = new HashMap<>();
        map.put("userId", String.valueOf(Mualab.getInstance().getSessionManager().getUser().id));
        map.put("page", String.valueOf(pageNo));
        map.put("limit", "20");


        new HttpTask(new HttpTask.Builder(mContext, "getNotificationList", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Log.d("Responce", response);
                Progress.hide(mContext);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        Gson gson = new Gson();
                        NotificationInfo notificationInfo = gson.fromJson(response,NotificationInfo.class);
                        NotificationList.addAll(notificationInfo.notificationList);
                    } else {


                    }

                    if (NotificationList.size() == 0) {
                        tv_no_notifications.setVisibility(View.VISIBLE);
                    } else {
                        tv_no_notifications.setVisibility(View.GONE);
                    }

                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Progress.hide(mContext);
                    if (NotificationList.size() == 0) {
                        tv_no_notifications.setVisibility(View.VISIBLE);
                    }else {
                        tv_no_notifications.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(mContext);
                if (NotificationList.size() == 0) {
                    tv_no_notifications.setVisibility(View.VISIBLE);
                }else {
                    tv_no_notifications.setVisibility(View.GONE);
                }

            }
        }).setProgress(false)
                .setAuthToken(Mualab.currentUser.authToken)
                .setParam(map)).execute(TAG);
    }

}



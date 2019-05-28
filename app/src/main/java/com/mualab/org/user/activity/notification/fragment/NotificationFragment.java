package com.mualab.org.user.activity.notification.fragment;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.base.BaseFragment;
import com.mualab.org.user.activity.feeds.activity.CommentsActivity;
import com.mualab.org.user.activity.feeds.model.Comment;
import com.mualab.org.user.activity.notification.adapter.NotificationAdapter;
import com.mualab.org.user.activity.notification.model.NotificationInfo;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.Util.TimeAgo;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mindiii on 17/9/18.
 */

public class NotificationFragment extends BaseFragment implements View.OnClickListener {
    public static String TAG = NotificationFragment.class.getName();
    private String mParam1;
    private RecyclerView recyclerView;
    private TextView tv_no_notifications;
    private ArrayList<NotificationInfo.NotificationListBean> NotificationListSocial;
    private ArrayList<NotificationInfo.NotificationListBean> NotificationListBooking;
    private EndlessRecyclerViewScrollListener scrollListener;
    private NotificationAdapter adapterSocial,adapterBoooking;
    private int count;
    private ProgressBar progress_bar;
    private String lastDateStatusSocial = "" ,lastDateStatusBooking = "";
    private TextView tv_social, tv_booking,tv_bootom_view,tv_batch_social_count,tv_batch_booking_count;
    private View view_div_booking,view_div_social;
    private DatabaseReference batchCountRef;
    private boolean isSocialTabSelected = true;

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
        View view = inflater.inflate(R.layout.activity_user_profile, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progress_bar = view.findViewById(R.id.progress_bar);
        tv_social = view.findViewById(R.id.tv_social);
        tv_booking = view.findViewById(R.id.tv_booking);
        tv_no_notifications = view.findViewById(R.id.tv_no_notifications);
        view_div_booking = view.findViewById(R.id.view_div_bookibg);
        view_div_social = view.findViewById(R.id.view_div_social);

        tv_batch_social_count = view.findViewById(R.id.tv_batch_social_count);
        tv_batch_booking_count = view.findViewById(R.id.tv_batch_booking_count);

        tv_bootom_view = getActivity().findViewById(R.id.tv_bootom_view);
        tv_bootom_view.setVisibility(View.GONE);

        tv_booking.setOnClickListener(this);
        tv_social.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(linearLayoutManager);
        NotificationListSocial = new ArrayList<>();
        NotificationListBooking = new ArrayList<>();
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                getNotificationList(page);
            }
        };
        String myId = String.valueOf(Mualab.currentUser.id);
        batchCountRef = FirebaseDatabase.getInstance().getReference().child("socialBookingBadgeCount").child(myId);

        // Adds the scroll listener to RecyclerView
        recyclerView.addOnScrollListener(scrollListener);
        adapterSocial = new NotificationAdapter(NotificationListSocial, mContext);
        adapterBoooking = new NotificationAdapter(NotificationListBooking, mContext);


        recyclerView.setAdapter(adapterSocial);
       // recyclerView.setAdapter(adapterBoooking);
        getbatchCount();
        getNotificationList(0);

    }

    private void getbatchCount(){
        batchCountRef.child("bookingCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    if(dataSnapshot.getValue() != null){
                        int bookingCount =  dataSnapshot.getValue(Integer.class);
                        tv_batch_booking_count.setText(bookingCount+"");

                        if(!isSocialTabSelected){
                            tv_batch_booking_count.setVisibility(View.GONE);
                            batchCountRef.child("bookingCount").setValue(0);
                        } else {
                            if(bookingCount == 0){
                                tv_batch_booking_count.setVisibility(View.GONE);
                            }else tv_batch_booking_count.setVisibility(View.VISIBLE);
                        }

                    }
                }catch (Exception e){
                    String  bookingCount =  dataSnapshot.getValue(String.class);
                    tv_batch_booking_count.setText(bookingCount+"");

                    if(!isSocialTabSelected){
                        tv_batch_booking_count.setVisibility(View.GONE);
                        batchCountRef.child("bookingCount").setValue(0);
                    } else {
                        if(Integer.parseInt(bookingCount) == 0){
                            tv_batch_booking_count.setVisibility(View.GONE);
                        }else tv_batch_booking_count.setVisibility(View.VISIBLE);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        batchCountRef.child("socialCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue() != null){
                    try {
                        int socialCount = dataSnapshot.getValue(Integer.class);
                        tv_batch_social_count.setText(socialCount+"");

                        if(isSocialTabSelected){
                            tv_batch_social_count.setVisibility(View.GONE);
                            batchCountRef.child("socialCount").setValue(0);
                        }else {
                            if(socialCount == 0){
                                tv_batch_social_count.setVisibility(View.GONE);
                            }else tv_batch_social_count.setVisibility(View.VISIBLE);
                        }
                    }catch (Exception e){
                        String socialCount = dataSnapshot.getValue(String.class);
                        tv_batch_social_count.setText(socialCount+"");

                        if(isSocialTabSelected){
                            tv_batch_social_count.setVisibility(View.GONE);
                            batchCountRef.child("socialCount").setValue(0);
                        }else {
                            if(Integer.parseInt(socialCount) == 0){
                                tv_batch_social_count.setVisibility(View.GONE);
                            }else tv_batch_social_count.setVisibility(View.VISIBLE);
                        }
                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        batchCountRef.child("socialCount").setValue(0);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_social:
                tv_social.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                tv_booking.setTextColor(ContextCompat.getColor(mContext,R.color.gray));
                view_div_social.setVisibility(View.VISIBLE);
                view_div_booking.setVisibility(View.GONE);

                scrollListener.resetState();
                lastDateStatusSocial = "";
                NotificationListSocial.clear();
                NotificationListBooking.clear();
                getNotificationList(0);

                recyclerView.setAdapter(adapterSocial);
                batchCountRef.child("socialCount").setValue(0);
                isSocialTabSelected = true;
                break;

            case R.id.tv_booking:
                tv_booking.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                tv_social.setTextColor(ContextCompat.getColor(mContext,R.color.gray));
                view_div_social.setVisibility(View.GONE);
                view_div_booking.setVisibility(View.VISIBLE);
                scrollListener.resetState();
                lastDateStatusBooking = "";
                NotificationListBooking.clear();
                NotificationListSocial.clear();

                getNotificationList(0);
                recyclerView.setAdapter(adapterBoooking);


                batchCountRef.child("bookingCount").setValue(0);
                isSocialTabSelected = false;
                break;
        }
    }

    private void getNotificationList(int pageNo) {
        // Progress.show(mContext);
        progress_bar.setVisibility(View.VISIBLE);
        HashMap<String, String> map = new HashMap<>();
        map.put("userId", String.valueOf(Mualab.getInstance().getSessionManager().getUser().id));
        map.put("page", String.valueOf(pageNo));
        map.put("limit", "20");


        new HttpTask(new HttpTask.Builder(mContext, "getNotificationList", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Log.d("Responce", response);
                //Progress.hide(mContext);
                progress_bar.setVisibility(View.GONE);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        Gson gson = new Gson();
                        NotificationInfo notificationInfo = gson.fromJson(response, NotificationInfo.class);
                        //NotificationList.addAll(notificationInfo.notificationList);


                        for (NotificationInfo.NotificationListBean notyInfoItem : notificationInfo.notificationList) {

                            if(notyInfoItem.type.equals("social")){
                                String ago = TimeAgo.covertTimeToText(notyInfoItem.crd);
                                if (!lastDateStatusSocial.equals(ago)) {
                                    notyInfoItem.shouldShowLable = true;
                                } else notyInfoItem.shouldShowLable = false;

                                notyInfoItem.dateStaus = ago;
                                lastDateStatusSocial = ago;
                                NotificationListSocial.add(notyInfoItem);
                            }else {
                                String ago = TimeAgo.covertTimeToText(notyInfoItem.crd);
                                if (!lastDateStatusBooking.equals(ago)) {
                                    notyInfoItem.shouldShowLable = true;
                                } else notyInfoItem.shouldShowLable = false;

                                notyInfoItem.dateStaus = ago;
                                lastDateStatusBooking = ago;
                                NotificationListBooking.add(notyInfoItem);
                            }

                        }
                    } else {

                    }

                    if(isSocialTabSelected){
                        if (NotificationListSocial.size() == 0) {
                            tv_no_notifications.setVisibility(View.VISIBLE);
                        } else {
                            tv_no_notifications.setVisibility(View.GONE);
                        }

                    }else {
                        if (NotificationListBooking.size() == 0) {
                            tv_no_notifications.setVisibility(View.VISIBLE);
                        } else {
                            tv_no_notifications.setVisibility(View.GONE);
                        }
                    }

                    if(isSocialTabSelected){
                        adapterSocial.notifyDataSetChanged();
                    }else adapterBoooking.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                    progress_bar.setVisibility(View.GONE);
                    if (NotificationListSocial.size() == 0) {
                        tv_no_notifications.setVisibility(View.VISIBLE);
                    } else {
                        tv_no_notifications.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                progress_bar.setVisibility(View.GONE);
                if (NotificationListSocial.size() == 0) {
                    tv_no_notifications.setVisibility(View.VISIBLE);
                } else {
                    tv_no_notifications.setVisibility(View.GONE);
                }

            }
        }).setProgress(false)
                .setAuthToken(Mualab.currentUser.authToken)
                .setParam(map)).execute(TAG);
    }


    @Override
    public void onDestroyView() {
        tv_bootom_view.setVisibility(View.GONE);
        super.onDestroyView();
    }
}



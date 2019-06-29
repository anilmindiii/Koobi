package com.mualab.org.user.activity.notification;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.dialogs.NameDisplayDialog;
import com.mualab.org.user.activity.notification.adapter.NotificationAdapter;
import com.mualab.org.user.activity.notification.fragment.NotificationFragment;
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
import com.mualab.org.user.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity implements View.OnClickListener {

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
    private ValueEventListener mListenerBooking, mListenerSocial ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.recyclerView);
        progress_bar = findViewById(R.id.progress_bar);
        tv_social = findViewById(R.id.tv_social);
        tv_booking = findViewById(R.id.tv_booking);
        tv_no_notifications = findViewById(R.id.tv_no_notifications);
        view_div_booking = findViewById(R.id.view_div_bookibg);
        view_div_social = findViewById(R.id.view_div_social);

        tv_batch_social_count = findViewById(R.id.tv_batch_social_count);
        tv_batch_booking_count = findViewById(R.id.tv_batch_booking_count);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(R.string.title_notification);
        LinearLayout llDots = findViewById(R.id.llDots);
        llDots.setVisibility(View.VISIBLE);

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        llDots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add("Clear All Completed");
                NameDisplayDialog.newInstance("Notifications", arrayList, pos -> {
                    String data = arrayList.get(pos);
                    getClickedData(data);
                }).show(getSupportFragmentManager());
            }
        });

        // tv_bootom_view = findViewById(R.id.tv_bootom_view);
       // tv_bootom_view.setVisibility(View.GONE);

        tv_booking.setOnClickListener(this);
        tv_social.setOnClickListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
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
        adapterSocial = new NotificationAdapter(NotificationListSocial, this);
        adapterBoooking = new NotificationAdapter(NotificationListBooking, this);


        recyclerView.setAdapter(adapterSocial);
        // recyclerView.setAdapter(adapterBoooking);
        getbatchCount();
        getNotificationList(0);
    }

    private void getClickedData(String data) {
        switch (data) {
            case "Clear All Completed":
                apiForClearNotification();
                break;
        }


    }

    private void apiForClearNotification() {
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(NotificationActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForClearNotification();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));
        params.put("userType", "user");

        HttpTask task = new HttpTask(new HttpTask.Builder(NotificationActivity.this, "notificationClear", new HttpResponceListner.Listener() {
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

                        if(isSocialTabSelected){
                            tv_social.callOnClick();
                        }else tv_booking.callOnClick();

                    } else {
                        MyToast.getInstance(NotificationActivity.this).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(NotificationActivity.this);
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
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

    private void getbatchCount(){
        mListenerBooking =  batchCountRef.child("bookingCount").addValueEventListener(new ValueEventListener() {
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

        mListenerSocial =  batchCountRef.child("socialCount").addValueEventListener(new ValueEventListener() {
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
    protected void onDestroy() {
        batchCountRef.child("bookingCount").removeEventListener(mListenerBooking);
        batchCountRef.child("socialCount").removeEventListener(mListenerSocial);

        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_social:
                tv_social.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
                tv_booking.setTextColor(ContextCompat.getColor(this,R.color.gray));
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
                tv_booking.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
                tv_social.setTextColor(ContextCompat.getColor(this,R.color.gray));
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


        new HttpTask(new HttpTask.Builder(this, "getNotificationList", new HttpResponceListner.Listener() {
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
}

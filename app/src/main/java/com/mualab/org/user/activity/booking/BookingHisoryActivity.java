package com.mualab.org.user.activity.booking;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.adapter.BookingHistoryAdapter;
import com.mualab.org.user.activity.booking.adapter.ScheduledAdapter;
import com.mualab.org.user.activity.booking.model.BookingHistoryInfo;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.menu.MenuAdapter;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.constants.Constant;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class BookingHisoryActivity extends AppCompatActivity {

    private RecyclerView recycler_view;
    ArrayList<BookingHistoryInfo.DataBean> dataBean;
    ScheduledAdapter adapter;
    private BookingHistoryAdapter historyAdapter;
    private TextView tv_bookingHistory, tv_booking_scheduled, tv_msg;
    private View iv_bookingHistory, iv_booking_scheduled;
    private String type = "";
    private TextView tvFilter;
    private String tabSelected = "tv_booking_scheduled";
    RelativeLayout ly_main;
    LinearLayout ll_filter;
    private ArrayList<String> arrayList = new ArrayList<>();
    private boolean isMenuOpen;
    private PopupWindow popupWindow;
    private ImageView ivFilter, iv_search_icon;
    EditText ed_search;
    LinearLayout ll_progress;
    String call_status = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_hisory);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.my_booking));

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        recycler_view = findViewById(R.id.recycler_view);
        tv_msg = findViewById(R.id.tv_msg);
        tv_bookingHistory = findViewById(R.id.tv_bookingHistory);
        tv_booking_scheduled = findViewById(R.id.tv_booking_scheduled);

        iv_bookingHistory = findViewById(R.id.iv_bookingHistory);
        iv_booking_scheduled = findViewById(R.id.iv_booking_scheduled);
        ly_main = findViewById(R.id.ly_main);
        ll_filter = findViewById(R.id.ll_filter);
        tvFilter = findViewById(R.id.tvFilter);
        ivFilter = findViewById(R.id.ivFilter);
        ed_search = findViewById(R.id.ed_search);
        iv_search_icon = findViewById(R.id.iv_search_icon);
        ll_progress = findViewById(R.id.ll_progress);

        arrayList = new ArrayList<>();
        dataBean = new ArrayList<>();
        adapter = new ScheduledAdapter(this, dataBean, "");
        recycler_view.setAdapter(adapter);


        iv_search_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ed_search.getVisibility() == View.GONE) {
                    ed_search.setVisibility(View.VISIBLE);

                } else {
                    ed_search.setVisibility(View.GONE);
                    KeyboardUtil.hideKeyboard(v, BookingHisoryActivity.this);
                    if (!ed_search.getText().toString().trim().equals(""))
                        ed_search.setText("");

                }
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
                tvFilter.setText(R.string.all);
                bookingHistory(type, s.toString().trim());
            }
        });


        tv_bookingHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_bookingHistory.setVisibility(View.GONE);
                iv_booking_scheduled.setVisibility(View.VISIBLE);

                tv_bookingHistory.setTextColor(ContextCompat.getColor(BookingHisoryActivity.this, R.color.colorPrimary));
                tv_booking_scheduled.setTextColor(ContextCompat.getColor(BookingHisoryActivity.this, R.color.gray));
                type = "Past";
                tabSelected = "tv_bookingHistory";

                dataBean.clear();

              /*  tv_all_type.setText("All");
                tv_in_type.setText("In Call");
                tv_out_type.setText("Out Call");

                tv_in_type.setVisibility(View.GONE);
                tv_out_type.setVisibility(View.GONE);*/

                bookingHistory(type, "");
                ed_search.setText("");
                KeyboardUtil.hideKeyboard(v, BookingHisoryActivity.this);
            }
        });

        tv_booking_scheduled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_bookingHistory.setVisibility(View.VISIBLE);
                iv_booking_scheduled.setVisibility(View.GONE);

                tv_bookingHistory.setTextColor(ContextCompat.getColor(BookingHisoryActivity.this, R.color.gray));
                tv_booking_scheduled.setTextColor(ContextCompat.getColor(BookingHisoryActivity.this, R.color.colorPrimary));
                type = "";

                tabSelected = "tv_booking_scheduled";
                dataBean.clear();

               /* tv_all_type.setText("All");
                tv_in_type.setText("In Call");
                tv_out_type.setText("Out Call");

                tv_in_type.setVisibility(View.GONE);
                tv_out_type.setVisibility(View.GONE);*/

                bookingHistory(type, "");
                ed_search.setText("");
                KeyboardUtil.hideKeyboard(v, BookingHisoryActivity.this);
            }
        });

        ly_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ed_search.getText().toString().trim().equals("")){
                    ed_search.setVisibility(View.GONE);
                }
            }
        });

      /*  tv_all_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = tv_all_type.getText().toString();
                setFilter(text);
            }
        });

        tv_in_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = tv_in_type.getText().toString();
                setFilter(text);
            }
        });

        tv_out_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = tv_out_type.getText().toString();
                setFilter(text);
            }
        });*/

        ll_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] location = new int[2];
                // Get the x, y location and store it in the location[] array
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
                }

            }
        });

        bookingHistory(type, "");

    }

    private void initiatePopupWindow(Point p) {

        try {
            LayoutInflater inflater = (LayoutInflater) BookingHisoryActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View layout = inflater.inflate(R.layout.layout_popup_menu_booking, (ViewGroup) findViewById(R.id.parent));
            popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);
            int OFFSET_X = -150;
            int OFFSET_Y = 20;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.setElevation(5);
            }
            arrayList.add("All");
            arrayList.add("In Call");
            arrayList.add("Out Call");
            popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
            RecyclerView recycler_view_popupWindow = layout.findViewById(R.id.recycler_view);
            MenuAdapter menuAdapter = new MenuAdapter(BookingHisoryActivity.this, arrayList, new MenuAdapter.Listener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onMenuClick(int pos) {
                    String data = arrayList.get(pos);
                    final ArrayList<BookingHistoryInfo.DataBean> tempList = new ArrayList<>();

                    switch (data) {
                        case "All":
                            tvFilter.setText(R.string.all);
                            call_status = "";
                            tempList.addAll(dataBean);
                            popupWindow.dismiss();
                            break;

                        case "In Call":
                            tvFilter.setText("In Call");
                            call_status = "In Call";
                            for (BookingHistoryInfo.DataBean bean : dataBean) {
                                if (bean.bookingType == 1) {
                                    tempList.add(bean);
                                }
                            }
                            popupWindow.dismiss();
                            break;

                        case "Out Call":
                            tvFilter.setText("Out Call");
                            call_status = "Out Call";
                            for (BookingHistoryInfo.DataBean bean : dataBean) {
                                if (bean.bookingType == 2) {
                                    tempList.add(bean);
                                }
                            }
                            popupWindow.dismiss();
                            break;
                    }

                    if (tabSelected.equals("tv_booking_scheduled")) {
                        adapter = new ScheduledAdapter(BookingHisoryActivity.this, tempList, call_status);
                        recycler_view.setAdapter(adapter);
                    } else if (tabSelected.equals("tv_bookingHistory")) {
                        historyAdapter = new BookingHistoryAdapter(BookingHisoryActivity.this, tempList, new BookingHistoryAdapter.CallApis() {
                            @Override
                            public void call(int artistId, int bookingId, String comments, float rating,String type) {
                                bookingReviewRating(artistId, bookingId, comments, rating, type);
                            }
                        });
                        recycler_view.setAdapter(historyAdapter);
                    }
                }
            });

            LinearLayoutManager layoutManager = new LinearLayoutManager(BookingHisoryActivity.this);
            recycler_view_popupWindow.setLayoutManager(layoutManager);
            recycler_view_popupWindow.setAdapter(menuAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private void setFilter(String text) {
        final ArrayList<BookingHistoryInfo.DataBean> tempList = new ArrayList<>();
        String call_status = "";

        if (text.equals("All")) {
            tv_all_type.setText("All");
            tv_in_type.setText("In Call");
            tv_out_type.setText("Out Call");

            if (tv_in_type.getVisibility() == View.VISIBLE && tv_out_type.getVisibility() == View.VISIBLE) {
                tv_in_type.setVisibility(View.GONE);
                tv_out_type.setVisibility(View.GONE);
            } else {
                tv_in_type.setVisibility(View.VISIBLE);
                tv_out_type.setVisibility(View.VISIBLE);
            }
            call_status = "";
            tempList.addAll(dataBean);
        } else if (text.equals("In Call")) {

            tv_all_type.setText("In Call");
            tv_in_type.setText("All");
            tv_out_type.setText("Out Call");

            if (tv_in_type.getVisibility() == View.VISIBLE && tv_out_type.getVisibility() == View.VISIBLE) {
                tv_in_type.setVisibility(View.GONE);
                tv_out_type.setVisibility(View.GONE);
            } else {
                tv_in_type.setVisibility(View.VISIBLE);
                tv_out_type.setVisibility(View.VISIBLE);
            }
            call_status = "In Call";
            for (BookingHistoryInfo.DataBean bean : dataBean) {
                if (bean.bookingType == 1) {
                    tempList.add(bean);
                }
            }

        } else if (text.equals("Out Call")) {

            tv_all_type.setText("Out Call");
            tv_in_type.setText("In Call");
            tv_out_type.setText("All");

            if (tv_in_type.getVisibility() == View.VISIBLE && tv_out_type.getVisibility() == View.VISIBLE) {
                tv_in_type.setVisibility(View.GONE);
                tv_out_type.setVisibility(View.GONE);
            } else {
                tv_in_type.setVisibility(View.VISIBLE);
                tv_out_type.setVisibility(View.VISIBLE);
            }
            call_status = "Out Call";
            for (BookingHistoryInfo.DataBean bean : dataBean) {
                if (bean.bookingType == 2) {
                    tempList.add(bean);
                }
            }
        }

        if (tabSelected.equals("tv_booking_scheduled")) {
            adapter = new ScheduledAdapter(BookingHisoryActivity.this, tempList, call_status);
            recycler_view.setAdapter(adapter);
        } else if (tabSelected.equals("tv_bookingHistory")) {
            historyAdapter = new BookingHistoryAdapter(BookingHisoryActivity.this, tempList, new BookingHistoryAdapter.CallApis() {
                @Override
                public void call(int artistId, int bookingId, String comments, float rating) {
                    bookingReviewRating(artistId, bookingId, comments, rating);
                }
            });
            recycler_view.setAdapter(historyAdapter);
        }


    }*/

    @Override
    protected void onResume() {
        super.onResume();
        bookingHistory(type, "");

    }

    private void bookingHistory(final String strBookingType, final String search) {
        ll_progress.setVisibility(View.VISIBLE);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingHisoryActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        bookingHistory(strBookingType, search);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("type", strBookingType);
        params.put("search", search);

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingHisoryActivity.this, "userBookingHistory", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                ll_progress.setVisibility(View.GONE);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    dataBean.clear();
                    if (status.equals("success")) {
                        Gson gson = new Gson();
                        BookingHistoryInfo historyInfo = gson.fromJson(response, BookingHistoryInfo.class);
                        dataBean.addAll(historyInfo.data);

                        for (BookingHistoryInfo.DataBean bean : dataBean) {
                            bean.bookingDate = Helper.formateDateFromstring("yyyy-MM-dd", "dd/MM/yyyy", bean.bookingDate);
                        }

                    } else {
                        MyToast.getInstance(BookingHisoryActivity.this).showDasuAlert(message);
                    }

                    if (dataBean.size() == 0) {
                        tv_msg.setVisibility(View.VISIBLE);
                    } else tv_msg.setVisibility(View.GONE);

                    final ArrayList<BookingHistoryInfo.DataBean> tempList = new ArrayList<>();
                    String data  = tvFilter.getText().toString().trim();
                    switch (data) {
                        case "All":
                            call_status = "";
                            tempList.addAll(dataBean);
                            break;

                        case "In Call":
                            call_status = "In Call";
                            for (BookingHistoryInfo.DataBean bean : dataBean) {
                                if (bean.bookingType == 1) {
                                    tempList.add(bean);
                                }
                            }
                            break;

                        case "Out Call":
                            call_status = "Out Call";
                            for (BookingHistoryInfo.DataBean bean : dataBean) {
                                if (bean.bookingType == 2) {
                                    tempList.add(bean);
                                }
                            }
                            break;
                    }


                    if (type.equals("Past")) {
                        historyAdapter = new BookingHistoryAdapter(BookingHisoryActivity.this, tempList, new BookingHistoryAdapter.CallApis() {
                            @Override
                            public void call(int artistId, int bookingId, String comments, float rating,String type) {
                                bookingReviewRating(artistId, bookingId, comments, rating,type);
                            }
                        });
                        recycler_view.setAdapter(historyAdapter);
                    } else if (type.equals("")) {
                        adapter = new ScheduledAdapter(BookingHisoryActivity.this, tempList, call_status);
                        recycler_view.setAdapter(adapter);
                    }

                } catch (Exception e) {
                    ll_progress.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                ll_progress.setVisibility(View.GONE);
                try {
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")) {
                        Mualab.getInstance().getSessionManager().logout();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        })
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(this.getClass().getName());
    }


    private void bookingReviewRating(final int artistId, final int bookingId,
                                     final String reviewByUser, final float rating, final String type) {
        Progress.show(BookingHisoryActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingHisoryActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        bookingReviewRating(artistId, bookingId, reviewByUser, rating,type);
                    }
                }
            }).show();
        }
        Map<String, String> params = new HashMap<>();
        params.put("bookingId", String.valueOf(bookingId));
        params.put("reviewByUser", reviewByUser);
        params.put("reviewByArtist", "");
        params.put("rating", String.valueOf(rating));
        params.put("userId", String.valueOf(Mualab.currentUser.id));
        params.put("artistId", String.valueOf(artistId));
        params.put("ratingDate", giveDate());
        params.put("type", type );

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingHisoryActivity.this, "bookingReviewRating", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingHisoryActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    dataBean.clear();
                    if (status.equals("success")) {
                        bookingHistory(type, "");
                    } else {
                        MyToast.getInstance(BookingHisoryActivity.this).showDasuAlert(message);
                    }

                } catch (Exception e) {
                    Progress.hide(BookingHisoryActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingHisoryActivity.this);
                try {
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")) {
                        Mualab.getInstance().getSessionManager().logout();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        })
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(this.getClass().getName());
    }

    public String giveDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

}

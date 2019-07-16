package com.mualab.org.user.activity.feeds.activity;

import android.app.Dialog;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.core.Repo;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.authentication.ForgotPasswordActivity;
import com.mualab.org.user.activity.authentication.LoginActivity;
import com.mualab.org.user.activity.authentication.Registration2Activity;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.BookingDetailsActivity;
import com.mualab.org.user.activity.booking.adapter.BookingHistoryDetailsAdapter;
import com.mualab.org.user.activity.booking.model.BookingListInfo;
import com.mualab.org.user.activity.feeds.adapter.ReportAdapter;
import com.mualab.org.user.activity.feeds.model.ReportInfo;
import com.mualab.org.user.activity.feeds.model.SubmitReportInfo;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.model.Report;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.API;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.Util;
import com.mualab.org.user.utils.constants.Constant;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {
    private TextView tv_link, btn_submit, tv_date;
    private TextView tv_reason;
    private EditText ed_description;
    private RecyclerView rev_reason;
    private ReportInfo reportInfo;
    private ReportAdapter adapter;
    private RelativeLayout ly_reason;
    ArrayList<ReportInfo.DataBean> dataBeans;
    private CardView cv_reason;
    private ImageView iv_back, iv_down_arrow,header_image;
    ReportInfo.DataBean dataBean;
    int feedOwenerId = 0;
    int feedId = 0;
    int staffId = 0;
    private TextView tv_msg, btn_submit_booking, btn_reSubmit;
    private LinearLayout ly_posted_link;
    private int bookingId, bookingInfoId, artistId, artistServiceId, reportForUser;
    private BookingListInfo.DataBean.BookingInfoBean.BookingReportBean bookingReport;
    private LinearLayout ly_report_status;
    private boolean shouldApiRun,isforbooking;
    private TextView tv_header_title;
    private  String artistServiceName = "";
    private BottomSheetDialog mCatTypeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        tv_link = findViewById(R.id.tv_link);
        tv_reason = findViewById(R.id.tv_reason);
        ed_description = findViewById(R.id.ed_description);
        rev_reason = findViewById(R.id.rev_reason);
        //cv_reason = findViewById(R.id.cv_reason);
        ly_reason = findViewById(R.id.ly_reason);
        iv_back = findViewById(R.id.iv_back);
        btn_submit = findViewById(R.id.btn_submit);
        tv_msg = findViewById(R.id.tv_msg);
        ly_posted_link = findViewById(R.id.ly_posted_link);
        btn_submit_booking = findViewById(R.id.btn_submit_booking);
        iv_down_arrow = findViewById(R.id.iv_down_arrow);
        ly_report_status = findViewById(R.id.ly_report_status);
        tv_date = findViewById(R.id.tv_date);
        tv_header_title = findViewById(R.id.tv_header_title);
        btn_reSubmit  =findViewById(R.id.btn_reSubmit);
        header_image  =findViewById(R.id.header_image);
        final LinearLayout ly_note = findViewById(R.id.ly_note);
        TextView tv_note = findViewById(R.id.tv_note);
        TextView tv_report_status = findViewById(R.id.tv_report_status);

        if (getIntent().getIntExtra("feedOwenerId", 0) != 0) {
            feedOwenerId = getIntent().getIntExtra("feedOwenerId", 0);
            feedId = getIntent().getIntExtra("feedId", 0);
            tv_link.setText(API.BASE_URL + "feedDetails/" + feedId + "");
            shouldApiRun = true;
            isforbooking = false;
            header_image.setImageResource(R.drawable.ico_report);
        }else {
            if (getIntent().getStringExtra("writeReport") != null)
                isforbooking = true;{

                tv_msg.setText(R.string.please_provide_service);
                ly_posted_link.setVisibility(View.GONE);
                btn_submit.setVisibility(View.GONE);
                ly_note.setVisibility(View.GONE);

                staffId = getIntent().getIntExtra("staffId",0);
                artistServiceName = getIntent().getStringExtra("artistServiceName");
                reportForUser = getIntent().getIntExtra("reportForUser", 0);
                bookingId = getIntent().getIntExtra("bookingId", 0);
                bookingInfoId = getIntent().getIntExtra("bookingInfoId", 0);
                artistId = getIntent().getIntExtra("artistId", 0);
                artistServiceId = getIntent().getIntExtra("artistServiceId", 0);

                if (getIntent().getStringExtra("writeReport").equals("yes")) {
                    shouldApiRun = true;
                    btn_submit_booking.setVisibility(View.VISIBLE);


                } else {
                    tv_header_title.setText("Report Details");
                    shouldApiRun = false;
                    btn_submit_booking.setVisibility(View.INVISIBLE);

                    bookingReport = getIntent().getParcelableExtra("bookingReport");

                    tv_reason.setText(bookingReport.title);
                    tv_reason.setEnabled(false);
                    ed_description.setText(bookingReport.description);
                    ed_description.setEnabled(false);
                    iv_down_arrow.setVisibility(View.GONE);

                    ly_report_status.setVisibility(View.VISIBLE);
                    tv_date.setText(Helper.formateDateFromstring("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "dd/MM/yyy", bookingReport.crd));


                    if(bookingReport.status == 2){
                        ly_note.setVisibility(View.VISIBLE);

                        tv_note.setText(bookingReport.adminReason);
                        tv_report_status.setText("Resolved");
                        tv_report_status.setTextColor(ContextCompat.getColor(ReportActivity.this,R.color.main_green_color));
                        btn_reSubmit.setVisibility(View.VISIBLE);
                        btn_submit.setVisibility(View.GONE);
                        btn_submit_booking.setVisibility(View.GONE);
                        ly_reason.setEnabled(true);
                    }else {
                        btn_reSubmit.setVisibility(View.GONE);
                        ly_reason.setEnabled(false);
                }
                }

            }
        }


        setReportReasonDialog(false);

        dataBeans = new ArrayList<>();
        adapter = new ReportAdapter(dataBeans, ReportActivity.this, new ReportAdapter.getClick() {
            @Override
            public void OnClikcItem(ReportInfo.DataBean bean) {
                dataBean = bean;
                mCatTypeDialog.dismiss();
              //  cv_reason.setVisibility(View.GONE);
                tv_reason.setText(bean.title);
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rev_reason.setLayoutManager(manager);
        rev_reason.setAdapter(adapter);

        if (shouldApiRun){
            if(isforbooking){
                reportReason("reportReason?type=booking");
            }else  reportReason("reportReason");

        }

        btn_reSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ed_description.setEnabled(true);
                ed_description.setText("");

                tv_header_title.setText("Report");

                tv_reason.setEnabled(true);
                tv_reason.setText("");

                iv_down_arrow.setVisibility(View.VISIBLE);
                ly_note.setVisibility(View.GONE);
                ly_report_status.setVisibility(View.GONE);
                reportReason("reportReason?type=booking");

                btn_reSubmit.setVisibility(View.GONE);
                btn_submit_booking.setVisibility(View.VISIBLE);
            }
        });

        btn_submit_booking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tv_reason.getText().toString().trim().equals("")) {
                    MyToast.getInstance(ReportActivity.this).showDasuAlert("Please select reason");
                    return;
                } else if (ed_description.getText().toString().trim().equals("")) {
                    MyToast.getInstance(ReportActivity.this).showDasuAlert("Please enter description");
                    return;
                }
                sendReportBooking();
            }
        });

        ly_reason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                KeyboardUtil.hideKeyboard(ly_reason, ReportActivity.this);

               /* if (cv_reason.getVisibility() == View.VISIBLE) {
                    cv_reason.setVisibility(View.GONE);
                } else cv_reason.setVisibility(View.VISIBLE);*/

                setReportReasonDialog(true);
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (tv_reason.getText().toString().trim().equals("")) {
                    MyToast.getInstance(ReportActivity.this).showDasuAlert("Please select reason");
                    return;
                } else if (ed_description.getText().toString().trim().equals("")) {
                    MyToast.getInstance(ReportActivity.this).showDasuAlert("Please enter description");
                    return;
                }

                SubmitReportInfo submitReportInfo = new SubmitReportInfo();
                submitReportInfo.description = ed_description.getText().toString().trim();
                submitReportInfo.feedId = feedId;
                submitReportInfo.feedOwnerId = feedOwenerId;
                submitReportInfo.myId = Mualab.currentUser.id + "";
                submitReportInfo.reason = tv_reason.getText().toString().trim();
                submitReportInfo.link = API.BASE_URL + "feedDetails/" + feedId + "";
                submitReportInfo.timeStamp = ServerValue.TIMESTAMP;

                String key = FirebaseDatabase.getInstance().getReference().child("feed_report").push().getKey();

                FirebaseDatabase.getInstance().getReference().child("feed_report").child(key).setValue(submitReportInfo);
                MyToast.getInstance(ReportActivity.this).showDasuAlert("Report submitted successfully");
                finish();
            }
        });
    }


    private void reportReason(final String url) {
        Progress.show(ReportActivity.this);
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ReportActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        reportReason(url);
                    }

                }
            }).show();
        }

        new HttpTask(new HttpTask.Builder(this, url, new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {

                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        Gson gson = new Gson();
                        reportInfo = gson.fromJson(response, ReportInfo.class);
                        dataBeans.addAll(reportInfo.data);
                    } else {
                        // goto 3rd screen for register
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Progress.hide(ReportActivity.this);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(ReportActivity.this);
            }
        })
                .setMethod(Request.Method.GET)
                .setProgress(true))
                .execute(this.getClass().getName());

    }

    private void sendReportBooking() {
        Progress.show(ReportActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ReportActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        sendReportBooking();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("staffId", String.valueOf(staffId));
        params.put("serviceName", artistServiceName);
        params.put("bookingInfoId", String.valueOf(bookingInfoId));
        params.put("description", ed_description.getText().toString().trim());
        params.put("bookingId", String.valueOf(bookingId));
        params.put("businessId", String.valueOf(artistId));
        params.put("reportByUser", String.valueOf(Mualab.currentUser.id));
        params.put("reportForUser", String.valueOf(reportForUser));
        params.put("serviceId", String.valueOf(artistServiceId));
        params.put("reportDate", giveDate());
        params.put("title", tv_reason.getText().toString().trim());

        HttpTask task = new HttpTask(new HttpTask.Builder(ReportActivity.this, "bookingReport", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(ReportActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equals("success")) {
                        MyToast.getInstance(ReportActivity.this).showDasuAlert("Report Submitted successfully");
                        finish();
                    } else {
                        MyToast.getInstance(ReportActivity.this).showDasuAlert(message);
                    }

                } catch (Exception e) {
                    Progress.hide(ReportActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(ReportActivity.this);
                try {
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")) {
                        Mualab.getInstance().getSessionManager().logout();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(this.getClass().getName());
    }

    public String giveDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    private void setReportReasonDialog(boolean isShow) {
        if (mCatTypeDialog == null) {
            mCatTypeDialog = new BottomSheetDialog(ReportActivity.this, R.style.CustomBottomSheetDialogTheme);
            View sheetView = getLayoutInflater().inflate(R.layout.dialog_bottom, null);
            TextView tvTitle = sheetView.findViewById(R.id.tvTitle);
            tvTitle.setText(getString(R.string.select_reason));
            rev_reason = sheetView.findViewById(R.id.recyclerView);
            mCatTypeDialog.setContentView(sheetView);
        }

        if (isShow)
            mCatTypeDialog.show();
    }
}

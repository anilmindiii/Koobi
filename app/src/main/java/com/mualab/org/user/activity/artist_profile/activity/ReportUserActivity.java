package com.mualab.org.user.activity.artist_profile.activity;

import android.app.Dialog;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.activity.ReportActivity;
import com.mualab.org.user.activity.feeds.adapter.ReportAdapter;
import com.mualab.org.user.activity.feeds.model.ReportInfo;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.KeyboardUtil;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ReportUserActivity extends AppCompatActivity {
    private ReportInfo reportInfo;
    ArrayList<ReportInfo.DataBean> dataBeans;
    private ReportAdapter adapter;
    ReportInfo.DataBean dataBean;
    private BottomSheetDialog mCatTypeDialog;
    private RelativeLayout ly_reason;
    private TextView tv_reason;
    private RecyclerView rev_reason;
    private String reportForUser;
    private TextView btn_submit;
    private EditText ed_description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_user);

        ly_reason = findViewById(R.id.ly_reason);
        tv_reason = findViewById(R.id.tv_reason);
        rev_reason = findViewById(R.id.rev_reason);
        btn_submit = findViewById(R.id.btn_submit);
        ed_description = findViewById(R.id.ed_description);


        reportForUser = getIntent().getStringExtra("reportForUser");

        dataBeans = new ArrayList<>();
        setReportReasonDialog(false);
        adapter = new ReportAdapter(dataBeans, this, new ReportAdapter.getClick() {
            @Override
            public void OnClikcItem(ReportInfo.DataBean bean) {
                dataBean = bean;
                mCatTypeDialog.dismiss();
                tv_reason.setText(bean.title);
            }
        });
        rev_reason.setAdapter(adapter);
        reportReason();

        ly_reason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardUtil.hideKeyboard(ly_reason, ReportUserActivity.this);
                setReportReasonDialog(true);
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tv_reason.getText().toString().trim().equals("")) {
                    MyToast.getInstance(ReportUserActivity.this).showDasuAlert("Please select reason");
                    return;
                } else if (ed_description.getText().toString().trim().equals("")) {
                    MyToast.getInstance(ReportUserActivity.this).showDasuAlert("Please enter description");
                    return;
                }

                sendReportBooking();
            }
        });

        findViewById(R.id.iv_back).setOnClickListener(v->{
            onBackPressed();
        });
    }

    private void reportReason() {
        Progress.show(ReportUserActivity.this);
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ReportUserActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        reportReason();
                    }

                }
            }).show();
        }


        new HttpTask(new HttpTask.Builder(this, "reportReason?type=user", new HttpResponceListner.Listener() {
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
                    Progress.hide(ReportUserActivity.this);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(ReportUserActivity.this);
            }
        })
                .setMethod(Request.Method.GET)
                .setProgress(true))
                .execute(this.getClass().getName());

    }

    private void setReportReasonDialog(boolean isShow) {
        if (mCatTypeDialog == null) {
            mCatTypeDialog = new BottomSheetDialog(ReportUserActivity.this, R.style.CustomBottomSheetDialogTheme);
            View sheetView = getLayoutInflater().inflate(R.layout.dialog_bottom, null);
            TextView tvTitle = sheetView.findViewById(R.id.tvTitle);
            tvTitle.setText(getString(R.string.select_reason));
            rev_reason = sheetView.findViewById(R.id.recyclerView);
            mCatTypeDialog.setContentView(sheetView);
        }

        if (isShow)
            mCatTypeDialog.show();
    }

    private void sendReportBooking() {
        Progress.show(ReportUserActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ReportUserActivity.this, new NoConnectionDialog.Listner() {
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

        params.put("description", ed_description.getText().toString().trim());
        params.put("reportByUser", String.valueOf(Mualab.currentUser.id));
        params.put("reportForUser", reportForUser);
        params.put("reportDate", giveDate());
        params.put("title", tv_reason.getText().toString().trim());

        HttpTask task = new HttpTask(new HttpTask.Builder(ReportUserActivity.this, "bookingReport", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(ReportUserActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equals("success")) {
                        MyToast.getInstance(ReportUserActivity.this).showDasuAlert("Report Submitted successfully");
                        finish();
                    } else {
                        MyToast.getInstance(ReportUserActivity.this).showDasuAlert(message);
                    }

                } catch (Exception e) {
                    Progress.hide(ReportUserActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(ReportUserActivity.this);
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
}

package com.mualab.org.user.chat;

import android.app.Dialog;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.activity.ReportActivity;
import com.mualab.org.user.activity.feeds.adapter.ReportAdapter;
import com.mualab.org.user.activity.feeds.model.ReportInfo;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.model.Groups;
import com.mualab.org.user.chat.model.Report;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.KeyboardUtil;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class ReportChatActivity extends AppCompatActivity {
    private EditText ed_description;
    private TextView btn_submit,tv_msg;
    private TextView tv_reason;
    private RecyclerView rev_reason;
    String groupId = "";
    private Groups groups = new Groups();
    private ReportAdapter adapter;
    ArrayList<ReportInfo.DataBean> dataBeans;
    private BottomSheetDialog mCatTypeDialog;
    private ReportInfo reportInfo;
    private RelativeLayout ly_reason;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_chat);

        groupId = getIntent().getStringExtra("groupId");
        groups =  getIntent().getParcelableExtra("groups");

        ly_reason = findViewById(R.id.ly_reason);
        ed_description = findViewById(R.id.ed_description);
        rev_reason = findViewById(R.id.rev_reason);
        btn_submit = findViewById(R.id.btn_submit);
        tv_msg = findViewById(R.id.tv_msg);
        ImageView iv_back = findViewById(R.id.iv_back);
        tv_reason = findViewById(R.id.tv_reason);
        tv_msg.setText(getString(R.string.group_chat_report_msg));

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setReportReasonDialog(false);
        dataBeans = new ArrayList<>();
        adapter = new ReportAdapter(dataBeans, this, new ReportAdapter.getClick() {
            @Override
            public void OnClikcItem(ReportInfo.DataBean bean) {
                mCatTypeDialog.dismiss();
                //  cv_reason.setVisibility(View.GONE);
                tv_reason.setText(bean.title);
            }
        });

        ly_reason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                KeyboardUtil.hideKeyboard(ly_reason, ReportChatActivity.this);

               /* if (cv_reason.getVisibility() == View.VISIBLE) {
                    cv_reason.setVisibility(View.GONE);
                } else cv_reason.setVisibility(View.VISIBLE);*/

                setReportReasonDialog(true);
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rev_reason.setLayoutManager(manager);
        rev_reason.setAdapter(adapter);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tv_reason.getText().toString().trim().equals("")) {
                    MyToast.getInstance(ReportChatActivity.this).showDasuAlert("Please select reason");
                    return;
                } else if (ed_description.getText().toString().trim().equals("")) {
                    MyToast.getInstance(ReportChatActivity.this).showDasuAlert("Please enter description");
                    return;
                }

                Report report = new Report();
                report.adminId = groups.adminId;
                report.groupDescription = groups.groupDescription;
                report.groupId = groups.groupId;
                report.senderId = String.valueOf(Mualab.currentUser.id);
                report.timeStamp = ServerValue.TIMESTAMP;
                report.title = groups.groupName;
                report.type = "group";

                String key = FirebaseDatabase.getInstance().getReference().child("group_report").child(groupId).push().getKey();
               FirebaseDatabase.getInstance().getReference().
                       child("group_report").child(groupId).child(key).setValue(report);


                MyToast.getInstance(ReportChatActivity.this).showDasuAlert("Report Submitted successfully");
                finish();

            }
        });

        reportReason("reportReason?type=group");
    }

    private void reportReason(final String url) {
        Progress.show(ReportChatActivity.this);
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ReportChatActivity.this, new NoConnectionDialog.Listner() {
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
                    Progress.hide(ReportChatActivity.this);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(ReportChatActivity.this);
            }
        })
                .setMethod(Request.Method.GET)
                .setProgress(true))
                .execute(this.getClass().getName());

    }

    private void setReportReasonDialog(boolean isShow) {
        if (mCatTypeDialog == null) {
            mCatTypeDialog = new BottomSheetDialog(ReportChatActivity.this, R.style.CustomBottomSheetDialogTheme);
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

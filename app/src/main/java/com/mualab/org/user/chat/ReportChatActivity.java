package com.mualab.org.user.chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.activity.ReportActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.model.Groups;
import com.mualab.org.user.chat.model.Report;
import com.mualab.org.user.dialogs.MyToast;

import java.io.Serializable;

public class ReportChatActivity extends AppCompatActivity {
    private EditText ed_reason,ed_description;
    private TextView btn_submit,tv_msg;
    String groupId = "";
    private Groups groups = new Groups();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_chat);

        groupId = getIntent().getStringExtra("groupId");
        groups = (Groups) getIntent().getSerializableExtra("groups");

        ed_reason = findViewById(R.id.ed_reason);
        ed_description = findViewById(R.id.ed_description);
        btn_submit = findViewById(R.id.btn_submit);
        tv_msg = findViewById(R.id.tv_msg);
        ImageView iv_back = findViewById(R.id.iv_back);

        tv_msg.setText(getString(R.string.group_chat_report_msg));

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ed_reason.getText().toString().trim().equals("")) {
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
    }



}

package com.mualab.org.user.activity.settings;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView iv_toggle_btn;
     //1 , On 0: Off
    Session session;
    private RelativeLayout ly_reset_password;
    private DatabaseReference mUserRef;
    private String myId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        iv_toggle_btn = findViewById(R.id.iv_toggle_btn);
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.setting));

        myId = String.valueOf(Mualab.currentUser.id);

        session = new Session(this);
        mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(myId).child("firebaseToken");

        if(session.getUser().notificationStatus == 1){
            iv_toggle_btn.setImageResource(R.drawable.ic_switch_on);
        }else  iv_toggle_btn.setImageResource(R.drawable.ic_switch_off);

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ly_reset_password = findViewById(R.id.ly_reset_password);

        iv_toggle_btn.setOnClickListener(this::onClick);
        ly_reset_password.setOnClickListener(this::onClick);
    }

    private void apiforUpdateNotification(int NotifyStatus,String firebaseToken) {
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(SettingsActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiforUpdateNotification(NotifyStatus,firebaseToken);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("notificationStatus", String.valueOf(NotifyStatus));
        params.put("firebaseToken", firebaseToken);

        HttpTask task = new HttpTask(new HttpTask.Builder(SettingsActivity.this, "updateRecord", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        Gson gson = new Gson();
                        JSONObject userObj = js.getJSONObject("user");
                        User u = gson.fromJson(String.valueOf(userObj), User.class);
                        user.notificationStatus = u.notificationStatus ;
                        // session.createSession(user);
                    } else {
                        MyToast.getInstance(SettingsActivity.this).showDasuAlert(message);
                    }
                    //showToast(message);
                } catch (Exception e) {
                    Progress.hide(SettingsActivity.this);
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
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_toggle_btn:
                User user = session.getUser();
                if(session.getUser().notificationStatus == 1){
                    apiforUpdateNotification(0,"");
                }else apiforUpdateNotification(1,FirebaseInstanceId.getInstance().getToken());

                if(session.getUser().notificationStatus == 0){
                    iv_toggle_btn.setImageResource(R.drawable.ic_switch_on);
                    user.notificationStatus = 1;
                    mUserRef.setValue(FirebaseInstanceId.getInstance().getToken());
                }else {
                    iv_toggle_btn.setImageResource(R.drawable.ic_switch_off);
                    user.notificationStatus = 0;
                    mUserRef.setValue("");

                }
                session.createSession(user);

                break;

            case R.id.ly_reset_password:
                Intent intent = new Intent(this,ChangePasswordActivity.class);
                startActivity(intent);
                break;
        }

    }
}

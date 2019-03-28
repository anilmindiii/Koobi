package com.mualab.org.user.activity.account_merge;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.authentication.RegistrationActivity;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.model.FirebaseUser;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MergeAccountActivity extends AppCompatActivity {
    private User user;
    private String str1 = "You already have an existing biz account using this email and phone number! Would you like to merge your existing biz profile ", str2 = " with your social account or create a new account for your social profile?";
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merge_account);
        session = new Session(this);

        user = (User) getIntent().getSerializableExtra("user");

        TextView tvMessege = findViewById(R.id.tvMessege);

        String textToHighlight = "<b>" + "@" + user.userName + "</b> ";

        // Construct the formatted text
        String replacedWith = "<font color='black'>" + textToHighlight + "</font>";

        // Update the TextView text
        tvMessege.setText(Html.fromHtml(str1 + replacedWith + str2));

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.btnMerge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user != null)
                    if (user.status.equals("0")) {
                        showToast("You are currenlty inactive by admin");
                    } else {
                        user.userId = String.valueOf(user.id);
                        session.createSession(user);
                        session.setPassword(user.password);
                        writeNewUser(user);
                    }
            }
        });

        findViewById(R.id.btnCreateNew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }
        });


    }

    private void writeNewUser(User user) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = new FirebaseUser();
        firebaseUser.firebaseToken = FirebaseInstanceId.getInstance().getToken();
        ;
        firebaseUser.isOnline = 1;
        firebaseUser.lastActivity = ServerValue.TIMESTAMP;
        if (user.profileImage.isEmpty())
            firebaseUser.profilePic = "http://koobi.co.uk:3000/uploads/default_user.png";
        else
            firebaseUser.profilePic = user.profileImage;

        firebaseUser.userName = user.userName;
        firebaseUser.uId = user.id;
        firebaseUser.authToken = user.authToken;
        firebaseUser.userType = user.userType;
        firebaseUser.banAdmin = 0;

        mDatabase.child("users").child(String.valueOf(user.id)).setValue(firebaseUser);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("user", user);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void apiForMergeRecord() {

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(MergeAccountActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForMergeRecord();
                    }
                }
            }).show();
        }

        Map<String, String> body = new HashMap<>();
        body.put("businessType", "independent");
        //   body.put("contactNo", user.contactNo);
        //   body.put("email", user.email);
        body.put("userType", "artist");

        new HttpTask(new HttpTask.Builder(this, "updateRecord", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                System.out.println("response = " + response);
                try {
                    /*{"status":"success","otp":5386,"users":null}*/
                    JSONObject object = new JSONObject(response);
                    String status = object.getString("status");
                    String message = object.getString("message");

                    if (status.equalsIgnoreCase("success")) {

                        Gson gson = new Gson();
                        JSONObject userObj = object.getJSONObject("user");
                        User user = gson.fromJson(String.valueOf(userObj), User.class);
                        session.createSession(user);
                        /*session.setBusinessProfileComplete(false);
                        Intent intent = new Intent(MergeAccountActivity.this, NewBaseActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("user", user);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();*/

                    } else {
                        showToast(message);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
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
                .setBody(body, HttpTask.ContentType.APPLICATION_JSON)
                .setAuthToken(user.authToken)
                .setProgress(true))
                .execute(this.getClass().getName());
    }

    private void showToast(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            MyToast.getInstance(this).showDasuAlert(msg);
        }
    }

}

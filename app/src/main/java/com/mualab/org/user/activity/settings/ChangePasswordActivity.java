package com.mualab.org.user.activity.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
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
import com.mualab.org.user.utils.Util;
import com.mualab.org.user.utils.constants.Constant;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity {
    private ImageView iv_back;
    private EditText old_pwd,edNewPwd,edConfirmPwd;
    private TextView btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        iv_back = findViewById(R.id.iv_back);
        old_pwd = findViewById(R.id.old_pwd);
        edNewPwd = findViewById(R.id.edNewPwd);
        edConfirmPwd = findViewById(R.id.edConfirmPwd);
        btnContinue = findViewById(R.id.btnContinue);

        iv_back.setOnClickListener(v -> onBackPressed());

        btnContinue.setOnClickListener(v -> {
            if(checkNotempty(old_pwd)){
                if (isValidPassword(edNewPwd,"new") && isValidPassword(edConfirmPwd,"confirm")) {
                    if(matchPassword()){
                        apiForgetUserIdFromUserName();
                    }
                }
            }



        });


    }

    private boolean checkNotempty(@NonNull EditText editText/*, TextInputLayout inputLayout*/) {
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            //inputLayout.setError(getString(R.string.error_required_field));
            showToast("Please enter your old password");
            editText.requestFocus();
            return false;
        }/*else {
            inputLayout.setErrorEnabled(false);
        }*/
        return true;
    }

    private boolean isValidPassword(@NonNull EditText edPwd /*TextInputLayout inputLayout*/,String type) {
        // Pattern regex = Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]");
        String password = edPwd.getText().toString().trim();
        // Pattern specailCharPatten = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Pattern UpperCasePatten = Pattern.compile("[A-Z ]");
        // Pattern lowerCasePatten = Pattern.compile("[a-z ]");
        Pattern digitCasePatten = Pattern.compile("[0-9 ]");

        if (TextUtils.isEmpty(password)) {
            //inputLayout.setError(getString(R.string.error_password_required));
            if(type.equals("new"))
            showToast("Please enter your new password");
            else if(type.equals("confirm"))
                showToast("Please enter your confirm password");
            edPwd.requestFocus();
            return false;
        } else if (password.length() < 8) {
            //inputLayout.setError(getString(R.string.error_password_vailidation));
            showToast(getString(R.string.error_password_vailidation));
            edPwd.requestFocus();
            return false;
        } else if (!UpperCasePatten.matcher(password).find()) {
            //inputLayout.setError(getString(R.string.error_password_vailidation));
            showToast(getString(R.string.error_password_vailidation));
            edPwd.requestFocus();
            return false;
        } else if (!digitCasePatten.matcher(password).find()) {
            //inputLayout.setError(getString(R.string.error_password_vailidation));
            showToast(getString(R.string.error_password_vailidation));
            edPwd.requestFocus();
            return false;
        }

       /* else {
            inputLayout.setErrorEnabled(false);
        }*/
        return true;
    }

    private boolean matchPassword() {
        if (!edNewPwd.getText().toString().equals(edConfirmPwd.getText().toString())) {
            //input_layout_cnfPwd.setError(getString(R.string.error_confirm_password_not_match));
            showToast("New password & confirm password don't match");
            return false;
        }
        return true;
    }

    private void showToast(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            MyToast.getInstance(this).showDasuAlert(msg);
        }
    }

    private void apiForgetUserIdFromUserName() {
        final Map<String, String> params = new HashMap<>();
        /*if(userName.contains("@")){
            userName = userName.replace("@","");
        }*/
        params.put("oldPassword", old_pwd.getText().toString().trim());
        params.put("password", edConfirmPwd.getText().toString().trim());
        new HttpTask(new HttpTask.Builder(ChangePasswordActivity.this, "changepassword", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Log.d("hfjas", response);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        apifortokenUpdate();
                        //MyToast.getInstance(ChangePasswordActivity.this).showDasuAlert("Password updated successfully. Please login again!");
                    } else {
                        MyToast.getInstance(ChangePasswordActivity.this).showDasuAlert(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
            }
        }).setBody(params, HttpTask.ContentType.APPLICATION_JSON)
                .setMethod(Request.Method.POST)
                .setProgress(true))
                .execute("FeedAdapter");


    }

    private void apifortokenUpdate() {
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ChangePasswordActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apifortokenUpdate();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("deviceToken", "");
        params.put("firebaseToken", "");
        //  params.put("followerId", String.valueOf(user.id));
        // params.put("loginUserId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(ChangePasswordActivity.this, "updateRecord", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        String myId = String.valueOf(Mualab.currentUser.id);

                        Util.goToOnlineStatus(ChangePasswordActivity.this, Constant.offline);
                        NotificationManager notificationManager = (NotificationManager) ChangePasswordActivity.this.getSystemService(NOTIFICATION_SERVICE);
                        assert notificationManager != null;
                        notificationManager.cancelAll();
                        FirebaseAuth.getInstance().signOut();

                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(myId)
                                .child("authToken").setValue("");

                        showExitDialog(ChangePasswordActivity.this);

                    } else {
                        MyToast.getInstance(ChangePasswordActivity.this).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(ChangePasswordActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try {
                    com.mualab.org.user.utils.Helper helper = new com.mualab.org.user.utils.Helper();
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

    public void showExitDialog(Context mContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext,R.style.MyDialogTheme);
        builder.setTitle("Alert");
        builder.setMessage("Password updated successfully. Please login again to continue");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Mualab.getInstance().getSessionManager().logout();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

}

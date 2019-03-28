package com.mualab.org.user.activity.authentication;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.KeyboardUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView btn_submit;
    private EditText ed_username;
    private LinearLayout ly_sign_in;
    private ImageView iv_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        btn_submit = findViewById(R.id.btn_submit);
        ed_username = findViewById(R.id.ed_username);
        ly_sign_in = findViewById(R.id.ly_sign_in);
        iv_back = findViewById(R.id.iv_back);

        iv_back.setOnClickListener(this);
        btn_submit.setOnClickListener(this);
        ly_sign_in.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_submit:
                KeyboardUtil.hideKeyboard(btn_submit, ForgotPasswordActivity.this);
                if(!TextUtils.isEmpty(ed_username.getText().toString())){
                    getForgotPassword();
                }else   MyToast.getInstance(ForgotPasswordActivity.this).showDasuAlert(getResources().getString(R.string.error_email_or_username_required));

                break;

            case R.id.ly_sign_in:
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();

                break;

            case R.id.iv_back:
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();

                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }


    private void getForgotPassword(){
        boolean isValidInput = true;
        String username = ed_username.getText().toString().trim().toLowerCase();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ForgotPasswordActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                    }

                }
            }).show();

            isValidInput = false;
        }

        if (isValidInput) {
            Map<String, String> params = new HashMap<>();
            params.put("email", username);


            new HttpTask(new HttpTask.Builder(this, "forgotPassword", new HttpResponceListner.Listener() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);
                        String status = js.getString("status");
                        String message = js.getString("message");
                        if (status.equalsIgnoreCase("success")) {
                            MyToast.getInstance(ForgotPasswordActivity.this).showDasuAlert(message);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                    finish();
                                }
                            },2000);

                        }else{
                            MyToast.getInstance(ForgotPasswordActivity.this).showDasuAlert(message);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                }})
                    .setBody(params, HttpTask.ContentType.APPLICATION_JSON)
                    .setProgress(true))
                    .execute(this.getClass().getName());
        }
    }
}

package com.mualab.org.user.activity.myprofile.activity.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.Views.calender.CalendarHelper;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.model.FirebaseUser;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OtpVerification extends AppCompatActivity implements View.OnClickListener,TextWatcher {
   private EditText tvOtp1;
   private EditText tvOtp2;
   private EditText tvOtp3;
   private EditText tvOtp4;
   private AppCompatButton tv_resend_otp;
   private TextView btnVerifyOtp;
   private String apiOTP;
   private String verifyStatus;
   private User user;
   private Bitmap profileImageBitmap;
    private Session session;
    private long mLastClickTime = 0;
    private User userSession;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        Intent intent=getIntent();
         //session = Mualab.getInstance().getSessionManager();



        session = Mualab.getInstance().getSessionManager();
        userSession = session.getUser();


        init();

        if (intent!=null){
          apiOTP=intent.getStringExtra("OTP");
            user = (User) intent.getSerializableExtra("user");


           Uri uri = (Uri)OtpVerification.this.getIntent().getParcelableExtra("byteArrary");

            try {
                if (uri!=null){
                    profileImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }



    }

    public void init(){
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tv_resend_otp = findViewById(R.id.tv_resend_otp);
        tvOtp1=findViewById(R.id.tvOtp1);
        tvOtp2=findViewById(R.id.tvOtp2);
        tvOtp3=findViewById(R.id.tvOtp3);
        tvOtp4=findViewById(R.id.tvOtp4);
        tvHeaderTitle.setText(R.string.verify);
        btnBack.setOnClickListener(this);
        tv_resend_otp.setOnClickListener(this);
        btnVerifyOtp.setOnClickListener(this);
        tvOtp1.addTextChangedListener(this);
        tvOtp2.addTextChangedListener(this);
        tvOtp3.addTextChangedListener(this);
        tvOtp4.addTextChangedListener(this);
        tvOtp1.setOnKeyListener(new KeyListener());
        tvOtp2.setOnKeyListener(new KeyListener());
        tvOtp3.setOnKeyListener(new KeyListener());
        tvOtp4.setOnKeyListener(new KeyListener());

    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 800) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        switch (v.getId()){


            case R.id.btnVerifyOtp:
                boolean cancel = false;
                String firstNumber = tvOtp1.getText().toString();
                String secondNumber = tvOtp2.getText().toString();
                String thirdNumber = tvOtp3.getText().toString();
                String fourthNumber = tvOtp4.getText().toString();
                String finalNumber = firstNumber + secondNumber + thirdNumber + fourthNumber;
                if (finalNumber.isEmpty()) {
                    cancel = true;
                    showToast(getString(R.string.error_otp_reuired));

                } else if (firstNumber.isEmpty()) {
                    cancel = true;
                    showToast(getString(R.string.error_otp_reuired));

                } else if (firstNumber.length() < 1) {
                    cancel = true;

                    showToast(getString(R.string.error_otp_reuired));

                } else if (secondNumber.isEmpty()) {
                    cancel = true;
                    showToast(getString(R.string.error_otp_reuired));

                } else if (secondNumber.length() < 1) {
                    cancel = true;
                    showToast(getString(R.string.error_otp_reuired));
                } else if (thirdNumber.isEmpty()) {
                    cancel = true;
                    showToast(getString(R.string.error_otp_reuired));

                } else if (thirdNumber.toString().length() < 1) {
                    cancel = true;
                    showToast(getString(R.string.error_otp_reuired));

                } else if (fourthNumber.isEmpty()) {
                    cancel = true;
                    showToast(getString(R.string.error_otp_reuired));

                } else if (fourthNumber.toString().length() < 1) {
                    cancel = true;
                    showToast(getString(R.string.error_otp_reuired));

                }
                if (!cancel) {
                    String number = firstNumber + secondNumber + thirdNumber + fourthNumber;
                    if (number.equals(apiOTP)){
                        user.otp = apiOTP;
                        user.otpVerified = true;
                        Intent resultIntent = new Intent();
                        // TODO Add extras or a data URI to this intent as appropriate.
                        //resultIntent.putExtra("some_key", "String data");
                        verifyStatus="yes";
                        callEditProfileApi();
/*
                        resultIntent.putExtra("verify", verifyStatus);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
*/



                    }else {
                        showToast(getString(R.string.error_otp_invalid));
                        resetOTP();

                    }



                }

                break;

            case R.id.tv_resend_otp:
                resetOTP();
                apiCallForDataVerify();

                break;
            case R.id.btnBack:
                onBackPressed();
                break;

        }

    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        // TODO Add extras or a data URI to this intent as appropriate.
        //resultIntent.putExtra("some_key", "String data");
        verifyStatus="no";
        resultIntent.putExtra("verify", verifyStatus);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();

    }

    private void resetOTP(){
        //ed.clear();

        tvOtp1.setText("");
        tvOtp2.setText("");
        tvOtp3.setText("");
        tvOtp4.setText("");
        tvOtp1.setHint("*");
        tvOtp2.setHint("*");
        tvOtp3.setHint("*");
        tvOtp4.setHint("*");
        tvOtp1.requestFocus();
       /* for (EditText aTvOtp : otp1) {
            aTvOtp.setHint("*");
        }*/
    }

    private void apiCallForDataVerify() {

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(OtpVerification.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiCallForDataVerify();
                    }

                }
            }).show();
        }

        Map<String, String> header = new HashMap<>();
        header.put("countryCode", user.countryCode);
        header.put("contactNo", user.contactNo);
        header.put("email", "");
        header.put("socialId", TextUtils.isEmpty(user.socialId)?"":user.socialId);

        new HttpTask(new HttpTask.Builder(this, "phonVerification", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                parseApiResponce(response);

            }

            @Override
            public void ErrorListener(VolleyError error) {
            }})
                .setBody(header, HttpTask.ContentType.APPLICATION_JSON)
                .setProgress(true))
                .execute(this.getClass().getName());
    }


    private void parseApiResponce(String response){
        try {
            JSONObject object = new JSONObject(response);
            String status = object.getString("status");
            String message = object.getString("message");

            if (status.equalsIgnoreCase("success")) {


                apiOTP = object.getString("otp");
                showToast(getResources().getString(R.string.error_otp_send));
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("email", user.email);
                    jsonObject.put("phone", user.contactNo);
                    jsonObject.put("code", user.countryCode);
                    jsonObject.put("otp", apiOTP);



                    //startTimear();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if (apiOTP.equals("already exist")) {
                showToast("This number already registered");
            } else if (status.equalsIgnoreCase("fail")) {

                showToast(message);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    private void showToast(String msg){
        MyToast.getInstance(this).showDasuAlert(msg);
        // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {


    }

    @Override
    public void afterTextChanged(Editable s) {
        if (tvOtp1.getText().toString().length() == 1) {
            tvOtp2.requestFocus();
        }
        if (tvOtp2.getText().toString().length() == 1) {
            tvOtp3.requestFocus();
        }
        if (tvOtp3.getText().toString().length() == 1) {
            tvOtp4.requestFocus();
        }
    }

    private class KeyListener implements View.OnKeyListener {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            int size = 0;
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                //this is for backspace
                if (tvOtp4.getText().length() == size) {
                    Log.e("Test","DEL 3");
                    tvOtp3.requestFocus();
                }
                if (tvOtp3.getText().length() == size) {
                    Log.e("Test","DEL 2");
                    tvOtp2.requestFocus();
                }
                if (tvOtp2.getText().length() == size) {
                    Log.e("Test","DEL 1");
                    tvOtp1.requestFocus();
                }
            }else {
                if (tvOtp1.getText().toString().length() == 1) {
                    Log.e("Test","else 2");
                    tvOtp2.requestFocus();
                } if (tvOtp2.getText().toString().length() == 1) {
                    Log.e("Test","else 3");
                    tvOtp3.requestFocus();
                }  if (tvOtp3.getText().toString().length() == 1) {
                    Log.e("Test","else 4");
                    tvOtp4.requestFocus();
                }
            }

            return false;
        }
    }


    public void callEditProfileApi() {
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(OtpVerification.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        callEditProfileApi();
                    }

                }
            }).show();
        }

        String deviceToken = FirebaseInstanceId.getInstance().getToken();//"android without firebase";
        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(Mualab.currentUser.id));
        params.put("firstName", user.firstName);
        params.put("lastName", user.lastName);
        params.put("email", user.email);
        params.put("countryCode", user.countryCode);
        params.put("contactNo", user.contactNo);
        params.put("gender", user.gender);
        params.put("dob", CalendarHelper.getStringYMDformatter(user.dob));
        params.put("address", user.address);
        params.put("city", user.city);

        params.put("state", user.state);
        params.put("country", user.country);
        params.put("latitude", user.latitude);
        params.put("longitude", user.longitude);

        //api.signUpTask(params, profileImageBitmap);
        HttpTask task = new HttpTask(new HttpTask.Builder(this, "profileUpdate", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        //Progress.hide(Registration2Activity.this);
                        Gson gson = new Gson();
                        JSONObject userObj = js.getJSONObject("users");
                        User user = gson.fromJson(String.valueOf(userObj), User.class);
                        if (!userSession.email.equals(user.email)){
                            session.createSession(user);
                            apifortokenUpdate();
                        }else {
                            session.createSession(user);
                            showToast(message);
                            writeNewUser(user);

                        }




                    } else {
                        showToast(message);
                        findViewById(R.id.btnContinue2).setEnabled(true);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    findViewById(R.id.btnContinue2).setEnabled(true);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {

            }
        })
                .setAuthToken(Mualab.currentUser.authToken)

                .setParam(params)
                .setProgress(true));
       task.postImage("profileImage", profileImageBitmap);

    }

    private void writeNewUser(User user) {
        DatabaseReference mDatabase  = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = new FirebaseUser();
        firebaseUser.firebaseToken = FirebaseInstanceId.getInstance().getToken();;
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
        User user1 = Mualab.getInstance().getSessionManager().getUser();
        Intent intent = new Intent(OtpVerification.this, UserProfileActivity.class);

        intent.putExtra("userId", String.valueOf(user1.id));

        finish();

        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);


        mDatabase.child("users").child(String.valueOf(user.id)).setValue(firebaseUser);
/*
        Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("user", user);
        intent.putExtra("userId", String.valueOf(Mualab.currentUser.userId));
        startActivity(intent);
*/
    }

    private void apifortokenUpdate() {
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(OtpVerification.this, new NoConnectionDialog.Listner() {
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

        HttpTask task = new HttpTask(new HttpTask.Builder(OtpVerification.this, "updateRecord", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {


                        NotificationManager notificationManager = (NotificationManager) OtpVerification.this.getSystemService(Context.NOTIFICATION_SERVICE);
                        assert notificationManager != null;
                        notificationManager.cancelAll();
                        FirebaseAuth.getInstance().signOut();
                        Mualab.getInstance().getSessionManager().logout();


                    } else {
                        MyToast.getInstance(OtpVerification.this).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(OtpVerification.this);
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





}

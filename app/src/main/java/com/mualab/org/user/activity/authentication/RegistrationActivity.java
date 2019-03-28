package com.mualab.org.user.activity.authentication;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.account_merge.MergeAccountActivity;
import com.mualab.org.user.broadcast.OnSmsCatchListener;
import com.mualab.org.user.broadcast.SmsVerifyCatcher;
import com.mualab.org.user.data.local.prefs.SharedPreferanceUtils;
import com.mualab.org.user.data.model.Country;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.JsonUtils;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.StatusBarUtil;
import com.mualab.org.user.utils.constants.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener,TextWatcher {

    private ViewSwitcher viewSwitcher;
    private View progressView1, progressView2;
    //Reg_View1
    //private TextInputLayout input_layout_email, input_layout_phone;
    private TextView tvCountryCode;
    private EditText ed_email, edPhoneNumber;

    //Reg_View2
    private EditText tvOtp[] = new EditText[4];
    private EditText otp1 ;
    private EditText otp2 ;
    private EditText otp3 ;
    private EditText otp4 ;

    //private static int REG_VIEW1=1, REG_VIEW2= 2, REG_VIEW3 =3, REG_VIEW4=4;
    private int CURRENT_VIEW_STATE = 1;

    private ArrayList<Integer> inputList = new ArrayList<>();
    private List<Country> countries;

    private String countryCode;
    private String apiOTP="";
    private User user;
    private SmsVerifyCatcher smsVerifyCatcher;
    private boolean isResendOTP;
    private CountDownTimer countDownTimer;
    private boolean timerIsRunning;
    private String otp, message = "";
    private User newUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        StatusBarUtil.setColorNoTranslucent(this, getResources().getColor(R.color.colorPrimary));
        //if(ScreenUtils.hasSoftKeys(getWindowManager(), this)) findViewById(R.id.nevSoftBar).setVisibility(View.VISIBLE);
        initViews();
        countries = JsonUtils.loadCountries(this);
        getCountryZipCode();
        user = new User();

        Intent intent = getIntent();
        if(intent.getExtras()!=null){
            user.socialId = intent.getStringExtra(Constant.SOCIAL_ID);
            user.email = intent.getStringExtra(Constant.EMAIL_ID);
        }

        smsVerifyCatcher = new SmsVerifyCatcher(this, new OnSmsCatchListener<String>() {
            @Override
            public void onSmsCatch(String message) {
                String code = parseCode(message);//Parse verification code

                if(CURRENT_VIEW_STATE==2){

                    inputList.clear();
                    user.otp = code;
                    for(int i=0; i<code.length(); i++){
                        tvOtp[i].setText(String.format("%s", code.charAt(i)));
                        inputList.add(Character.getNumericValue(code.charAt(i)));
                    }

                    if(apiOTP.equals(user.otp)){
                        user.otp = code;
                        otp=code;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                nextScreen();
                            }
                        }, 700);
                    }
                }


            }
        });

    }

    private void showToast(String msg){
        MyToast.getInstance(this).showDasuAlert(msg);
        // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.alreadyHaveAnAccount:
                finish();
                break;

            case R.id.btnContinue1:
                if(validateEmail() && validatePhone()){

                    String contactNo = edPhoneNumber.getText().toString().trim();
                    String email = ed_email.getText().toString().trim();
                    KeyboardUtil.hideKeyboard(edPhoneNumber, this);

                    if(user.countryCode!=null && user.countryCode.equals(countryCode) &&  user.email.equals(email)
                            && user.contactNo.equals(contactNo)
                            && countDownTimer!=null && timerIsRunning){
                        nextScreen();
                    }else {
                        isResendOTP = false;
                        user.countryCode = countryCode;
                        user.contactNo = contactNo;
                        user.email = email;
                        resetOTP();
                        apiCallForDataVerify();
                    }
                }

                break;

            case R.id.btnVerifyOtp:

                boolean cancel = false;
                String firstNumber = otp1.getText().toString();
                String secondNumber = otp2.getText().toString();
                String thirdNumber = otp3.getText().toString();
                String fourthNumber = otp4.getText().toString();
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

                        if (message.equals("Email already exist by social app")) {
                            Intent intent = new Intent(RegistrationActivity.this, MergeAccountActivity.class);
                            intent.putExtra("user", newUser);
                            startActivityForResult(intent, Constant.REQUEST_CODE_PICK);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                            return;
                        }

                        nextScreen();

                    }else {
                        showToast(getString(R.string.error_otp_invalid));
                        resetOTP();
                    }

                } else {
                    showToast(getString(R.string.error_otp_invalid));

                }
                break;

            case R.id.tv_resend_otp:
                isResendOTP = true;
                timerIsRunning = false;
                resetOTP();
                apiCallForDataVerify();
                break;



            case R.id.tvCountryCode:
                Intent intent=new Intent(RegistrationActivity.this,ChooseCountryActivity.class);
                intent.putExtra("activity", "Registration");
                startActivityForResult(intent,1003);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        message = "";
    }

    private boolean validateEmail() {
        String email = ed_email.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            //input_layout_email.setError(getString(R.string.error_email_required));
            showToast(getString(R.string.error_email_required));
            ed_email.requestFocus();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //input_layout_email.setError(getString(R.string.error_invalid_email));
            showToast(getString(R.string.error_invalid_email));
            ed_email.requestFocus();
            return false;
        } /*else {
            input_layout_email.setErrorEnabled(false);
        }*/

        return true;
    }

    private boolean validatePhone() {
        String phone = edPhoneNumber.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            //input_layout_phone.setError(getString(R.string.error_phone_no_reuired));
            showToast(getString(R.string.error_phone_no_reuired));
            edPhoneNumber.requestFocus();
            return false;
        } else if (phone.length() < 4 || phone.length()>15) {
            showToast(getString(R.string.error_phone_no_length));
            //input_layout_phone.setError(getString(R.string.error_phone_no_length));
            edPhoneNumber.requestFocus();
            return false;
        } /*else {
            input_layout_phone.setErrorEnabled(false);
        }*/
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==2){
            finish();
        }else if (resultCode == Activity.RESULT_OK) {
            switch(requestCode) {
                case 1003: {
                    Country country = (Country) data.getSerializableExtra("country");
                    tvCountryCode.setText(String.format("+%s", country.phone_code));
                    countryCode = "+"+country.phone_code;
                    break;
                }
            }
        }

        if(resultCode == RESULT_OK){
            if(requestCode == Constant.REQUEST_CODE_PICK){
                onBackPressed();
            }
        }

    }

    private void inputOtp(int otpKey){
        int size = inputList.size();

        if(otpKey==-1 && size>0){

            switch (size){
                case 1:
                    otp1.setText("*");
                    break;
                case 2:
                    otp2.setText("*");
                    break;
                case 3:
                    otp3.setText("*");
                    break;
                case 4:
                    otp4.setText("*");
                    break;
            }inputList.remove(size-1);

        }else if(otpKey!=-1 && !(size>3)){
            inputList.add(otpKey);
            switch (size){
                case 0:
                    otp1.setText(String.valueOf(otpKey));
                    break;
                case 1:
                    otp2.setText(String.valueOf(otpKey));
                    break;
                case 2:
                    otp3.setText(String.valueOf(otpKey));
                    break;
                case 3:
                    otp4.setText(String.valueOf(otpKey));
                    break;
            }
        }

    }

    private void initViews(){
        viewSwitcher = findViewById(R.id.viewSwitcher);
        progressView1 = findViewById(R.id.progressView1);
        progressView2 = findViewById(R.id.progressView2);
        progressView1.setBackgroundColor(ContextCompat.getColor(this, R.color.darkpink));

        /* view 1 */
       /* input_layout_email = findViewById(R.id.input_layout_email);
        input_layout_phone = findViewById(R.id.input_layout_phone);*/
        tvCountryCode = findViewById(R.id.tvCountryCode);
        ed_email = findViewById(R.id.ed_email);
        edPhoneNumber = findViewById(R.id.edPhoneNumber);

        findViewById(R.id.btnContinue1).setOnClickListener(this);
        findViewById(R.id.tvCountryCode).setOnClickListener(this);
        findViewById(R.id.tv_resend_otp).setOnClickListener(this);
        findViewById(R.id.alreadyHaveAnAccount).setOnClickListener(this);
        tvCountryCode.setOnClickListener(this);

        //Reg_View2
        otp1 = findViewById(R.id.tvOtp1);
        otp2 = findViewById(R.id.tvOtp2);
        otp3 = findViewById(R.id.tvOtp3);
        otp4 = findViewById(R.id.tvOtp4);
/*
        tvOtp[0] = findViewById(R.id.tvOtp1);
        tvOtp[1] = findViewById(R.id.tvOtp2);
        tvOtp[2] = findViewById(R.id.tvOtp3);
        tvOtp[3] = findViewById(R.id.tvOtp4);
*/
        otp1.addTextChangedListener(this);
        otp2.addTextChangedListener(this);
        otp3.addTextChangedListener(this);
        otp4.addTextChangedListener(this);
        findViewById(R.id.btnVerifyOtp).setOnClickListener(this);

        otp2.setOnKeyListener(new KeyListener());
        otp1.setOnKeyListener(new KeyListener());
        otp3.setOnKeyListener(new KeyListener());
        otp4.setOnKeyListener(new KeyListener());

    }


    private void nextScreen(){
        resetProgressView();
        switch (CURRENT_VIEW_STATE){
            case 1:
                CURRENT_VIEW_STATE = 2;
                viewSwitcher.showNext();
                progressView2.setBackgroundColor(ContextCompat.getColor(this, R.color.darkpink));
                break;

            case 2:
                startActivityForResult(new Intent(RegistrationActivity.this, Registration2Activity.class)
                        .putExtra(Constant.USER, user),2);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
        }

    }

    private void resetProgressView(){
        progressView1.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        progressView2.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
    }

    @Override
    public void onBackPressed() {
        resetProgressView();
        switch (CURRENT_VIEW_STATE){
            case 1:
                progressView1.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                super.onBackPressed();
                break;

            case 2:
                CURRENT_VIEW_STATE = 1;
                progressView1.setBackgroundColor(ContextCompat.getColor(this, R.color.darkpink));
                viewSwitcher.showPrevious();
                break;
        }
    }

    /**
     * Parse verification code
     *
     * @param message sms message
     * @return only four numbers from massage string
     */
    private String parseCode(String message) {
        Pattern p = Pattern.compile("\\b\\d{4}\\b");
        Matcher m = p.matcher(message);
        String code = "";
        while (m.find()) {
            code = m.group(0);
        }
        return code;
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(smsVerifyCatcher!=null) smsVerifyCatcher.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(smsVerifyCatcher!=null) smsVerifyCatcher.onStop();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        smsVerifyCatcher.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //*******country code alert ***********
    public void getCountryZipCode() {
        String CountryID;
        // String CountryZipCode = "";
        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        assert manager != null;
        CountryID = manager.getSimCountryIso().toUpperCase();
        if (CountryID.equals("")) {
            tvCountryCode.setText("+1");
            countryCode = "+1";
        }else {

            for (int i = 0; i < countries.size(); i++) {
                Country country = countries.get(i);
                if (CountryID.equalsIgnoreCase(country.code)) {
                    // CountryZipCode = countries.get(i).phone_code;
                    countryCode = "+" + country.phone_code;
                    tvCountryCode.setText(String.format("+%s", country.phone_code));
                    break;
                }
            }
        }
        // return CountryZipCode + " " + CountryID;
    }


    private void apiCallForDataVerify() {

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(RegistrationActivity.this, new NoConnectionDialog.Listner() {
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
        header.put("email", user.email);
        header.put("userType", "user");
        header.put("socialId", TextUtils.isEmpty(user.socialId)?"":user.socialId);

        new HttpTask(new HttpTask.Builder(this, "phonVerification", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                parseApiResponce(response);

            }

            @Override
            public void ErrorListener(VolleyError error) {
                Log.d("error",error.toString());
            }})
                .setBody(header, HttpTask.ContentType.APPLICATION_JSON)
                .setProgress(true))
                .execute(this.getClass().getName());
    }


    private void parseApiResponce(String response){
        try {
            JSONObject object = new JSONObject(response);
            String status = object.getString("status");
            message = object.getString("message");

            if (status.equalsIgnoreCase("success")) {

                if (message.equals("Email already exist by social app")) {
                    Gson gson = new Gson();
                    JSONObject userObj = object.getJSONObject("users");
                    newUser = gson.fromJson(String.valueOf(userObj), User.class);
                }

                if (isResendOTP){
                    showToast(getString(R.string.otp));
                }
                apiOTP = object.getString("otp");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("email", user.email);
                    jsonObject.put("phone", user.contactNo);
                    jsonObject.put("code", user.countryCode);
                    jsonObject.put("otp", apiOTP);
                    jsonObject.put("userType", "user");
                    MyToast.getInstance(RegistrationActivity.this).showDasuAlert("OTP : "+apiOTP);
                    SharedPreferanceUtils.setParam(RegistrationActivity.this, "OTP", jsonObject.toString());
                    final AppCompatButton btnResendOtp = findViewById(R.id.tv_resend_otp);
                    btnResendOtp.setEnabled(true);
                    //startTimear();
                    if(!isResendOTP)
                        nextScreen();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if (apiOTP.equals("already exist")) {
                showToast("This number already registered");
            } else if (status.equalsIgnoreCase("fail")) {
                resetOTP();
                showToast(message);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    private void startTimear(){
        final AppCompatButton btnResendOtp = findViewById(R.id.tv_resend_otp);
        btnResendOtp.setEnabled(false);
        if(countDownTimer!=null)
            countDownTimer.cancel();
        timerIsRunning = true;
        countDownTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                btnResendOtp.setText("00:" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                timerIsRunning = false;
                btnResendOtp.setText(R.string.resent_code);
                btnResendOtp.setEnabled(true);
            }
        }.start();
    }

    private void resetOTP(){
        //ed.clear();

        otp1.setText("");
        otp2.setText("");
        otp3.setText("");
        otp4.setText("");
        otp1.setHint("*");
        otp2.setHint("*");
        otp3.setHint("*");
        otp4.setHint("*");
        otp1.requestFocus();
       /* for (EditText aTvOtp : otp1) {
            aTvOtp.setHint("*");
        }*/
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    /*    Log.e("Test",start+" "+before+" "+" "+count);
        int size = 0;
        if (otp1.getText().length() == size) {
            Log.e("Test","onText 2");
            otp2.requestFocus();
        }
         if (otp2.getText().length() == size) {
            Log.e("Test","onText 3");
            otp3.requestFocus();
        }
         if (otp3.getText().length() == size) {
            Log.e("Test","onText 4");
            otp4.requestFocus();
        }*/
    }

    private class KeyListener implements View.OnKeyListener {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            int size = 0;
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                //this is for backspace
                if (otp4.getText().length() == size) {
                    Log.e("Test","DEL 3");
                    otp3.requestFocus();
                }
                 if (otp3.getText().length() == size) {
                    Log.e("Test","DEL 2");
                    otp2.requestFocus();
                }
                 if (otp2.getText().length() == size) {
                    Log.e("Test","DEL 1");
                    otp1.requestFocus();
                }
            }else {
                if (otp1.getText().toString().length() == 1) {
                    Log.e("Test","else 2");
                    otp2.requestFocus();
                } if (otp2.getText().toString().length() == 1) {
                    Log.e("Test","else 3");
                    otp3.requestFocus();
                }  if (otp3.getText().toString().length() == 1) {
                    Log.e("Test","else 4");
                    otp4.requestFocus();
                }
            }

            return false;
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (otp1.getText().toString().length() == 1) {
            otp2.requestFocus();
        }
        if (otp2.getText().toString().length() == 1) {
            otp3.requestFocus();
        }
        if (otp3.getText().toString().length() == 1) {
            otp4.requestFocus();
        }
    }
}

package com.mualab.org.user.activity.myprofile.activity.activity;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.mualab.org.user.R;

import com.mualab.org.user.Views.calender.CalendarHelper;
import com.mualab.org.user.activity.authentication.AddAddressActivity;
import com.mualab.org.user.activity.authentication.ChooseCountryActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.model.FirebaseUser;
import com.mualab.org.user.customdobcalender.DatePicker;
import com.mualab.org.user.customdobcalender.DatePickerDialog;
import com.mualab.org.user.customdobcalender.SpinnerDatePickerDialogBuilder;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.local.prefs.SharedPreferanceUtils;
import com.mualab.org.user.data.model.Country;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.model.booking.Address;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.image.cropper.CropImage;
import com.mualab.org.user.image.cropper.CropImageView;
import com.mualab.org.user.image.picker.ImagePicker;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.constants.Constant;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private User user;
    private EditText ed_firstName;
    private EditText ed_lastName;
    private EditText edEmail;
    private EditText edPhoneNumber;
    private TextView tv_dobDate;
    private TextView txt_address;
    private TextView tvCountryCode;
    private Address address;
    private Bitmap profileImageBitmap;
    private CircleImageView profile_image;
    private ImageView cameraImg;
    private int yearShow = 1980, monthShow = 0, dayShow = 1;
    private SimpleDateFormat simpleDateFormat;
    //private String formattedTime;
    private String countryCode;
    private CountDownTimer countDownTimer;
    private boolean timerIsRunning;
    private String apiOTP = "";
    private ImageView imgVerifyStatus;
    private List<Country> countries;
    private String verify = "";
    private RadioGroup radioGroup;
    private Session session;
    private long mLastClickTime = 0;
    private static EditProfileActivity activity;
    private Uri uri;
    private ProgressBar progress_bar;
    private TextView btnContinue1;

    public EditProfileActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        setContentView(R.layout.activity_edit_profile);
        session = Mualab.getInstance().getSessionManager();
        user = session.getUser();
        if (user != null) {
            Mualab.currentUser = Mualab.getInstance().getSessionManager().getUser();
            Mualab.feedBasicInfo.put("userId", "" + user.id);
            Mualab.feedBasicInfo.put("age", "25");
            Mualab.feedBasicInfo.put("gender", "male");
            Mualab.feedBasicInfo.put("city", "indore");
            Mualab.feedBasicInfo.put("state", "MP");
            Mualab.feedBasicInfo.put("country", "India");
        }
        init();
      /*  countries = JsonUtils.loadCountries(this);
        getCountryZipCode();
*/

        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);


    }

    public void init() {
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        TextView tv_username = findViewById(R.id.tv_username);
        ImageView btnBack = findViewById(R.id.btnBack);
        LinearLayout ll_profileImage = findViewById(R.id.ll_profileImage);
        ed_firstName = findViewById(R.id.ed_firstName);
        imgVerifyStatus = findViewById(R.id.imgVerifyStatus);
        cameraImg = findViewById(R.id.cameraImg);
        RelativeLayout rl_dob = findViewById(R.id.rl_dob);

        ed_firstName = findViewById(R.id.ed_firstName);
        progress_bar = findViewById(R.id.progress_bar);
        btnContinue1 = findViewById(R.id.btnContinue1);
        txt_address = findViewById(R.id.txt_address);
        ed_lastName = findViewById(R.id.ed_lastName);
        edEmail = findViewById(R.id.edEmail);
        edPhoneNumber = findViewById(R.id.edPhoneNumber);
        final RadioButton rbMale = findViewById(R.id.rbMale);
        final RadioButton rbFemale = findViewById(R.id.rbFemale);
        tvCountryCode = findViewById(R.id.tvCountryCode);
        tv_dobDate = findViewById(R.id.tv_dobDate);
        profile_image = findViewById(R.id.profile_image);
        radioGroup = findViewById(R.id.radioGroup);
        if (!user.profileImage.isEmpty()) {
            Picasso.with(this).load(user.profileImage).placeholder(R.drawable.default_placeholder).
                    fit().into(profile_image);
        }
        tvHeaderTitle.setText(R.string.edit);
        btnBack.setOnClickListener(this);
        tv_username.setText(user.userName);
        ed_firstName.setText(user.firstName);
        ed_lastName.setText(user.lastName);
        edPhoneNumber.setText(user.contactNo);
        edEmail.setText(user.email);

        txt_address.setText(user.address);
        if (user.gender.equals("Female")) {
            rbFemale.setChecked(true);
            rbFemale.setTextColor(getResources().getColor(R.color.black));
            rbMale.setTextColor(getResources().getColor(R.color.gray));

        }
        if (user.gender.equals("male")) {
            rbMale.setChecked(true);
            rbMale.setTextColor(getResources().getColor(R.color.black));
            rbFemale.setTextColor(getResources().getColor(R.color.gray));


        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (rbFemale.isChecked()) {
                    rbFemale.setTextColor(getResources().getColor(R.color.black));
                    rbMale.setTextColor(getResources().getColor(R.color.gray));
                }
                if (rbMale.isChecked()) {
                    rbMale.setTextColor(getResources().getColor(R.color.black));
                    rbFemale.setTextColor(getResources().getColor(R.color.gray));

                }
            }
        });

       /* radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (rbFemale.isChecked()) {
                    rbFemale.setTextColor(getResources().getColor(R.color.black));
                    rbFemale.setHighlightColor(Color.parseColor("#1bd3c9"));
                    rbMale.setHighlightColor(Color.parseColor("#E0E0E0"));
                    rbMale.setTextColor(getResources().getColor(R.color.gray));
                }
                if (rbMale.isChecked()){
                    rbMale.setTextColor(getResources().getColor(R.color.black));
                    rbFemale.setTextColor(getResources().getColor(R.color.gray));
                    rbMale.setHighlightColor(Color.parseColor("#1bd3c9"));
                    rbFemale.setHighlightColor(Color.parseColor("#E0E0E0"));

                }

            }
        });
*/
/*
        rbMale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    rbMale.setTextColor(getResources().getColor(R.color.black));
                    rbFemale.setTextColor(getResources().getColor(R.color.gray));



                }else {

                    rbFemale.setTextColor(getResources().getColor(R.color.gray));
                    rbMale.setTextColor(getResources().getColor(R.color.black));



                }
            }
        });

        rbFemale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    rbFemale.setTextColor(getResources().getColor(R.color.black));
                    rbMale.setTextColor(getResources().getColor(R.color.gray));



                }else {
                    rbMale.setTextColor(getResources().getColor(R.color.gray));
                    rbFemale.setTextColor(getResources().getColor(R.color.black));


                }
            }
        });*/


        txt_address.setOnClickListener(this);
        imgVerifyStatus.setOnClickListener(this);
        ll_profileImage.setOnClickListener(this);
        tvCountryCode.setOnClickListener(this);
        profile_image.setOnClickListener(this);
        cameraImg.setOnClickListener(this);
        btnContinue1.setOnClickListener(this);
        rl_dob.setOnClickListener(this);

        if (user.dob != null) {
            tv_dobDate.setText(user.dob);
            if (user.dob.contains("/")) {
                String[] s = user.dob.split("/");
                monthShow = (Integer.parseInt(s[1])-1);
                dayShow = Integer.parseInt(s[0]);
                yearShow = Integer.parseInt(s[2]);
            }
        }


    }


    private boolean validatePhone() {
        String phone = edPhoneNumber.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            //input_layout_phone.setError(getString(R.string.error_phone_no_reuired));
            showToast(getString(R.string.error_phone_no_reuired));
            edPhoneNumber.requestFocus();
            return false;
        } else if (phone.length() < 4 || phone.length() > 15) {
            showToast(getString(R.string.error_phone_no_length));
            //input_layout_phone.setError(getString(R.string.error_phone_no_length));
            edPhoneNumber.requestFocus();
            return false;
        } /*else {
            input_layout_phone.setErrorEnabled(false);
        }*/
        return true;
    }

    private boolean checkNotempty(EditText editText/*, TextInputLayout inputLayout*/) {
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            //inputLayout.setError(getString(R.string.error_required_field));
            showToast(getString(R.string.error_required_field));
            editText.requestFocus();
            return false;
        }/*else {
            inputLayout.setErrorEnabled(false);
        }*/
        return true;
    }

    private boolean checkNotemptyDate(TextView editText/*, TextInputLayout inputLayout*/) {
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            //inputLayout.setError(getString(R.string.error_required_field));
            showToast(getString(R.string.error_required_field));
            editText.requestFocus();
            return false;
        }/*else {
            inputLayout.setErrorEnabled(false);
        }*/
        return true;
    }

    private boolean checkAddress() {
        if (TextUtils.isEmpty(txt_address.getText())) {
            //inputLayout.setError(getString(R.string.error_password_required));
            showToast(getString(R.string.error_address_required));
            txt_address.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        switch (v.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.txt_address:

                Intent intent = new Intent(EditProfileActivity.this, AddAddressActivity.class);
                // if (address != null) {
                intent.putExtra("address", address);
                intent.putExtra("activity", "EditProfile");
                startActivityForResult(intent, 1001);
                ///   }

                break;

            case R.id.tvCountryCode:
                Intent intentcountry = new Intent(EditProfileActivity.this, ChooseCountryActivity.class);
                intentcountry.putExtra("activity", "EditProfile");
                startActivityForResult(intentcountry, 1003);
                break;


            case R.id.profile_image:
                getPermissionAndPicImage();
                break;
            case R.id.cameraImg:
                getPermissionAndPicImage();
                break;


            case R.id.btnContinue1:


                if (checkNotempty(ed_firstName)
                        && checkNotempty(ed_lastName)
                        && checkNotempty(edEmail) && checkNotempty(edPhoneNumber)
                        && checkNotemptyDate(tv_dobDate) && checkNotemptyDate(txt_address) &&
                        validateEmail() && validatePhone()) {
                    String nu = edPhoneNumber.getText().toString().trim();
                    String countrycode = tvCountryCode.getText().toString().trim();
                    if (!user.contactNo.equals(nu) || !user.countryCode.equals(countrycode)) {
                        apiCallForDataVerify();

                    } else if (verify.equals("no")) {

                        apiCallForDataVerify();

                    } else {
                        int selectedId = radioGroup.getCheckedRadioButtonId();
                        RadioButton radioSexButton = findViewById(selectedId);
                        User user1 = new User();
                        user1.firstName = ed_firstName.getText().toString().trim();
                        user1.lastName = ed_lastName.getText().toString().trim();
                        user1.fullName = user.firstName.concat(" ").concat(user.lastName);
                        user1.dob = tv_dobDate.getText().toString().trim();
                        user1.gender = radioSexButton.getText().toString().trim();
                        user1.email = edEmail.getText().toString().trim();
                        user1.contactNo = edPhoneNumber.getText().toString().trim();
                        user1.countryCode = tvCountryCode.getText().toString().trim();

                        if (address != null) {
                            user1.address = address.stAddress1;
                            user1.city = address.city;
                            user1.state = address.state;
                            user1.country = address.country;
                            user1.latitude = address.latitude;
                            user1.longitude = address.longitude;
                        } else {
                            user1.address = user.address;
                            user1.city = user.city;
                            user1.state = user.state;
                            user1.country = user.country;
                            user1.latitude = user.latitude;
                            user1.longitude = user.longitude;
                        }
                        callEditProfileApi(user1);
                        btnContinue1.setEnabled(false);


                    }


                }


                break;

            case R.id.imgVerifyStatus:
                apiCallForDataVerify();
                break;

            case R.id.rl_dob:


             /*   String[] s = user.dob.split("-");
                dayShow = Integer.parseInt(s[1]);
                monthShow = Integer.parseInt(s[0]);
                yearShow = Integer.parseInt(s[2]);*/

                if (user.dob.contains("/")) {
                    showDate(yearShow, monthShow, dayShow);
                } else showDate(yearShow, monthShow, dayShow);


                break;


        }
    }

    //*******country code alert ***********
    public void getCountryZipCode() {
        String CountryID;
        // String CountryZipCode = "";
        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        assert manager != null;
        CountryID = manager.getSimCountryIso().toUpperCase();
        if (CountryID.equals("")) {
            tvCountryCode.setText("");
            //countryCode = "+1";
        } else {

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
            new NoConnectionDialog(EditProfileActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiCallForDataVerify();
                    }

                }
            }).show();
        }

        Map<String, String> header = new HashMap<>();
        header.put("countryCode", tvCountryCode.getText().toString());
        header.put("contactNo", edPhoneNumber.getText().toString());
        header.put("email", "");
        header.put("socialId", TextUtils.isEmpty(user.socialId) ? "" : user.socialId);

        new HttpTask(new HttpTask.Builder(this, "phonVerification", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                parseApiResponce(response);

            }

            @Override
            public void ErrorListener(VolleyError error) {
            }
        })
                .setBody(header, HttpTask.ContentType.APPLICATION_JSON)
                .setProgress(true))
                .execute(this.getClass().getName());
    }


    private void parseApiResponce(String response) {
        try {
            JSONObject object = new JSONObject(response);
            String status = object.getString("status");
            String message = object.getString("message");

            if (status.equalsIgnoreCase("success")) {
                apiOTP = object.getString("otp");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("email", user.email);
                    jsonObject.put("phone", user.contactNo);
                    jsonObject.put("code", user.countryCode);
                    jsonObject.put("otp", apiOTP);
                    SharedPreferanceUtils.setParam(EditProfileActivity.this, "OTP", jsonObject.toString());
                    Intent intent = new Intent(EditProfileActivity.this, OtpVerification.class);
                    intent.putExtra("OTP", apiOTP);
                    intent.putExtra("user", user);
                    if (profileImageBitmap != null)
                        intent.putExtra("byteArrary", uri);
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    RadioButton radioSexButton = findViewById(selectedId);
                    user.contactNo = edPhoneNumber.getText().toString().trim();
                    user.email = edEmail.getText().toString().trim();
                    user.countryCode = tvCountryCode.getText().toString().trim();
                    user.firstName = ed_firstName.getText().toString().trim();
                    user.lastName = ed_lastName.getText().toString().trim();
                    user.fullName = user.firstName.concat(" ").concat(user.lastName);
                    user.dob = tv_dobDate.getText().toString().trim();
                    user.gender = radioSexButton.getText().toString();
                    user.email = edEmail.getText().toString().trim();
                    intent.putExtra("user", user);
                    startActivityForResult(intent, 1004);


                    //startTimear();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if (apiOTP.equals("already exist")) {
                showToast("This number already registered");
            } else if (status.equalsIgnoreCase("fail")) {

                showToast(message);
                //edPhoneNumber.setText("");

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    public void callEditProfileApi(final User edituser) {
        progress_bar.setVisibility(View.VISIBLE);
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(EditProfileActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        callEditProfileApi(user);
                    }

                }
            }).show();
        }

        String deviceToken = FirebaseInstanceId.getInstance().getToken();//"android without firebase";
        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(Mualab.currentUser.id));
        params.put("firstName", edituser.firstName);
        params.put("lastName", edituser.lastName);
        params.put("email", edituser.email);
        params.put("countryCode", edituser.countryCode);
        params.put("contactNo", edituser.contactNo);
        params.put("gender", edituser.gender);

        Calendar calendar_new = new GregorianCalendar(yearShow, monthShow, dayShow);
        String dob = simpleDateFormat.format(calendar_new.getTime());
        dob = Helper.formateDateFromstring("dd/MM/yyyy", "yyyy-MM-dd", dob);//2019-02-01


        params.put("dob", dob);
        params.put("address", edituser.address);
        params.put("city", edituser.city);
        params.put("state", edituser.state);
        params.put("country", edituser.country);
        params.put("latitude", edituser.latitude);
        params.put("longitude", edituser.longitude);

        //api.signUpTask(params, profileImageBitmap);
        HttpTask task = new HttpTask(new HttpTask.Builder(this, "profileUpdate", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    progress_bar.setVisibility(View.GONE);
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        //Progress.hide(Registration2Activity.this);
                        Gson gson = new Gson();
                        JSONObject userObj = js.getJSONObject("users");
                        User newuser = gson.fromJson(String.valueOf(userObj), User.class);
                        if (!user.email.equals(edEmail.getText().toString().trim())) {
                            session.createSession(newuser);
                            showToast(message);
                            writeNewUser(newuser);
                            apifortokenUpdate();
                            btnContinue1.setEnabled(true);
                            Mualab.currentUser = newuser;

                        } else {
                            session.createSession(newuser);
                            writeNewUser(newuser);
                            Mualab.currentUser = newuser;
                            showToast(message);

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
        btnContinue1.setEnabled(true);
        task.postImage("profileImage", profileImageBitmap);

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
        /*User user1 = Mualab.getInstance().getSessionManager().getUser();*/
        Intent intent = new Intent(EditProfileActivity.this, UserProfileActivity.class);
        intent.putExtra("userId", String.valueOf(user.id));

        startActivity(intent);


        mDatabase.child("users").child(String.valueOf(user.id)).setValue(firebaseUser);
/*
        Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("user", user);
        intent.putExtra("userId", String.valueOf(Mualab.currentUser.userId));
        startActivity(intent);
*/
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    void showDate(int year, int month, int day) {
        new SpinnerDatePickerDialogBuilder()
                .context(EditProfileActivity.this)
                .callback(EditProfileActivity.this)
                .isActivity("EditProfile")
                .dialogTheme(R.style.AppTheme)
                .defaultDate(year, month, day)
                .build()

                .show();
    }

 /*   private void resetOTP(){
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
       *//* for (EditText aTvOtp : otp1) {
            aTvOtp.setHint("*");
        }*//*
    }*/

    public void getPermissionAndPicImage() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);
            } else {
                ImagePicker.pickImage(this);
            }
        } else {
            ImagePicker.pickImage(this);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == 234) {
                //Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                Uri imageUri = ImagePicker.getImageURIFromResult(this, requestCode, resultCode, data);
                if (imageUri != null) {
                    CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE).setAspectRatio(400, 400).start(this);
                } else {
                    showToast(getString(R.string.msg_some_thing_went_wrong));
                }

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                try {
                    if (result != null)
                        profileImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getUri());
                    assert result != null;
                    uri = result.getUri();

                    if (profileImageBitmap != null) {
                        profile_image.setImageBitmap(profileImageBitmap);


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (requestCode == 1003) {
                Country country = (Country) data.getSerializableExtra("country");
                tvCountryCode.setText(String.format("+%s", country.phone_code));
                countryCode = "+" + country.phone_code;


            } else if (requestCode == 1001) {
                address = (Address) data.getSerializableExtra("address");
                if (address != null) {
                    txt_address.setVisibility(View.VISIBLE);
                    txt_address.setText(String.format("%s",
                            TextUtils.isEmpty(address.placeName) ? address.stAddress1 : address.placeName));
                    user.address = address.stAddress1;
                    user.city = address.city;
                    user.country = address.country;
                    user.lastName = address.latitude;
                    user.longitude = address.longitude;


                }

            } else if (requestCode == 1004) {
                edPhoneNumber.setText(user.contactNo);
                verify = data.getStringExtra("verify");
                if (verify.equals("no")) {
                    imgVerifyStatus.setVisibility(View.VISIBLE);

                } else {
                    imgVerifyStatus.setVisibility(View.GONE);
                }


            }

        }
    }

    private void showToast(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            MyToast.getInstance(this).showDasuAlert(msg);
        }
    }

    private boolean validateEmail() {
        String email = edEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            //input_layout_email.setError(getString(R.string.error_email_required));
            showToast(getString(R.string.error_email_required));
            edEmail.requestFocus();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //input_layout_email.setError(getString(R.string.error_invalid_email));
            showToast(getString(R.string.error_invalid_email));
            edEmail.requestFocus();
            return false;
        } /*else {
            input_layout_email.setErrorEnabled(false);
        }*/

        return true;
    }


    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth, int type, boolean isClicked) {
        Calendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);


        if (type == 0) {
            if (!isClicked) {
                if (!tv_dobDate.getText().toString().equals(user.dob)) {
                    Calendar calendar_new = new GregorianCalendar(yearShow, monthShow, dayShow);
                    tv_dobDate.setText(simpleDateFormat.format(calendar_new.getTime()));
                } else tv_dobDate.setText(user.dob);

            } else {
                Calendar calendar_new = new GregorianCalendar(yearShow, monthShow, dayShow);
                tv_dobDate.setText(simpleDateFormat.format(calendar_new.getTime()));
            }

        } else {
            tv_dobDate.setText(simpleDateFormat.format(calendar.getTime()));
            user.dob = simpleDateFormat.format(calendar.getTime());
            yearShow = year;
            monthShow = monthOfYear;
            dayShow = dayOfMonth;


        }
    }

    private void apifortokenUpdate() {
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(EditProfileActivity.this, new NoConnectionDialog.Listner() {
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

        HttpTask task = new HttpTask(new HttpTask.Builder(EditProfileActivity.this, "updateRecord", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {


                        NotificationManager notificationManager = (NotificationManager) EditProfileActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
                        assert notificationManager != null;
                        notificationManager.cancelAll();
                        FirebaseAuth.getInstance().signOut();
                        Mualab.getInstance().getSessionManager().logout();


                    } else {
                        MyToast.getInstance(EditProfileActivity.this).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(EditProfileActivity.this);
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


    public static String convertStringToData(String stringData)
            throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/mm/dd");//yyyy-MM-dd'T'HH:mm:ss
        SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date data = sdf.parse(stringData);
        String formattedTime = output.format(data);
        return formattedTime;
    }

}

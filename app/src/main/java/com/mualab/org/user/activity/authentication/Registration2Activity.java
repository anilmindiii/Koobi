package com.mualab.org.user.activity.authentication;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.Views.calender.CalendarHelper;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.chat.model.FirebaseUser;
import com.mualab.org.user.customdobcalender.DatePicker;
import com.mualab.org.user.customdobcalender.DatePickerDialog;
import com.mualab.org.user.customdobcalender.OnDateChangedListener;
import com.mualab.org.user.customdobcalender.OnDateListener;
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
import com.mualab.org.user.image.cropper.CropImage;
import com.mualab.org.user.image.cropper.CropImageView;
import com.mualab.org.user.image.picker.ImagePicker;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.StatusBarUtil;
import com.mualab.org.user.utils.constants.Constant;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class Registration2Activity extends AppCompatActivity implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener, OnDateChangedListener {

    public static String TAG = Registration2Activity.class.getName();
    private ViewSwitcher viewSwitcher;
    private View progressView3, progressView4;
    //Reg_View1
    private CircleImageView profile_image;
    //private TextInputLayout input_layout_firstName, input_layout_lastName, input_layout_userName;
    private EditText ed_firstName, ed_lastName, ed_userName;
    private TextView tv_dob;
    private RadioGroup radioGroup;

    //Reg_View2
    // private TextInputLayout input_layout_pwd, input_layout_cnfPwd;
    private EditText edPwd, edConfirmPwd;

    private int CURRENT_VIEW_STATE = 3;
    private OnDateListener dateListener;
    private User user;
    private Bitmap profileImageBitmap;

    private Session session;
    SimpleDateFormat simpleDateFormat;
    private Address address;
    private TextView txt_address;
    private int yearShow = 1980, monthShow = 0, dayShow = 1;
    private CheckBox cb_tnc;
    private RelativeLayout ly_term_n_policies;
    //private WebServiceAPI api;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration2);
        StatusBarUtil.setColorNoTranslucent(this, getResources().getColor(R.color.colorPrimary));
        initViews();
        user = new User();
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            user = (User) intent.getSerializableExtra(Constant.USER);
            if (user != null) {
                if (user.socialId != null) {
                    if (!user.socialId.equals("")) {
                        ed_firstName.setText(user.firstName);
                        ed_firstName.setSelection(user.firstName.length());
                        ed_lastName.setText(user.lastName);
                        ed_userName.setText(user.userName);
                        Picasso.with(this).load(user.profileImage).placeholder(R.drawable.default_placeholder).into(profile_image);
                        Glide.with(this)
                                .load(user.profileImage)
                                .asBitmap()  // переводим его в нужный формат
                                .fitCenter()
                                .into(new SimpleTarget<Bitmap>(100,100) {
                                    @Override
                                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                        profileImageBitmap = bitmap;

                                    }
                                });
                    }

                }
            }

        }
        session = new Session(this);
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        hasSoftKeys(getWindowManager());


    }


/*    private void setDateField() {
        Calendar now = Calendar.getInstance();
        //create new DateDialogFragment
        DateDialogFragment ddf = DateDialogFragment.newInstance(this, R.string.set_date, now);
        ddf.setDateDialogFragmentListener(new DateDialogFragment.DateDialogFragmentListener() {
            @Override
            public void dateDialogFragmentDateSet(Calendar calendar) {
                // update the fragment
                int year = calendar.get(Calendar.YEAR);
                int monthOfYear = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                //String date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                String dateToShow = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                tv_dob.setText(dateToShow);
                //findViewById(R.id.tvHintDOB).setVisibility(View.VISIBLE);
            }
        });

        ddf.show(getSupportFragmentManager(), "date picker dialog fragment");
    }*/

    private void initViews() {
        viewSwitcher = findViewById(R.id.viewSwitcher);
        progressView3 = findViewById(R.id.progressView3);
        progressView4 = findViewById(R.id.progressView4);
        progressView3.setBackgroundColor(ContextCompat.getColor(this, R.color.darkpink));

        /* view 1 */
        profile_image = findViewById(R.id.profile_image);
        profile_image.setOnClickListener(this);

        ed_firstName = findViewById(R.id.ed_firstName);
        ed_lastName = findViewById(R.id.ed_lastName);
        ed_userName = findViewById(R.id.ed_userName);
        tv_dob = findViewById(R.id.tv_dob);
        radioGroup = findViewById(R.id.radioGroup);
        cb_tnc = findViewById(R.id.cb_tnc);
        ly_term_n_policies = findViewById(R.id.ly_term_n_policies);
        findViewById(R.id.btnContinue1).setOnClickListener(this);
        tv_dob.setOnClickListener(this);

        /* view 1 */
       /* input_layout_pwd = findViewById(R.id.input_layout_pwd);
        input_layout_cnfPwd = findViewById(R.id.input_layout_cnfPwd);*/
        edPwd = findViewById(R.id.edPwd);
        txt_address = findViewById(R.id.txt_address);
        txt_address.setOnClickListener(this);
        edConfirmPwd = findViewById(R.id.edConfirmPwd);
        findViewById(R.id.btnContinue2).setOnClickListener(this);
        findViewById(R.id.alreadyHaveAnAccount).setOnClickListener(this);

        ly_term_n_policies.setOnClickListener(this::onClick);

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        yearShow = calendar.get(Calendar.YEAR);
        monthShow = calendar.get(Calendar.MONTH);
        dayShow = calendar.get(Calendar.DAY_OF_MONTH);

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.alreadyHaveAnAccount:
                finish();
                break;

            case R.id.txt_address:
                Intent intent = new Intent(Registration2Activity.this, AddAddressActivity.class);
                if (address != null)
                    intent.putExtra("address", address);
                intent.putExtra("activity", "Registration");
                startActivityForResult(intent, 1001);

                break;

            case R.id.btnContinue1:
               /* if(profileImageBitmap==null){
                    showToast(getString(R.string.error_profile_image));
                }else*/
                if (checkNotempty(ed_firstName/*, input_layout_firstName*/)
                        && checkNotempty(ed_lastName/*, input_layout_lastName*/)
                        && validUserName(ed_userName/*, input_layout_userName*/)
                        && checkDOB() && checkAddress()) {

                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    RadioButton radioSexButton = findViewById(selectedId);

                    user.userName = ed_userName.getText().toString().trim();
                    user.firstName = ed_firstName.getText().toString().trim();
                    user.lastName = ed_lastName.getText().toString().trim();
                    user.fullName = user.firstName.concat(" ").concat(user.lastName);
                    user.password = edPwd.getText().toString().trim();
                    user.dob = tv_dob.getText().toString().trim();
                    user.gender = radioSexButton.getText().toString();

                    if(cb_tnc.isChecked()){
                        registration();
                    }else MyToast.getInstance(this).showDasuAlert("Please accept term and policies");



                }

                break;


            case R.id.btnContinue2:
                if (isValidPassword(edPwd /*, input_layout_pwd*/) && matchPassword()) {
                    user.password = edPwd.getText().toString().trim();
                    nextScreen();
                    //createMualabAccount();
                }
                break;

            case R.id.profile_image:
                getPermissionAndPicImage();
                break;

            case R.id.tv_dob:
                showDate(yearShow, monthShow, dayShow);
                //datePicker();
                //  setDateField();
                break;

            case R.id.ly_term_n_policies:
                intent  = new Intent(this,WebViewActivity.class);
                startActivity(intent);
                break;

        }
    }

    private void registration() {
        boolean isValidInput = true;

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(Registration2Activity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        registration();
                    }

                }
            }).show();

            isValidInput = false;
        }

        if (isValidInput) {

            String userName = user.userName.toLowerCase();
            final Map<String, String> params = new HashMap<>();
            params.put("userName", userName);
            new HttpTask(new HttpTask.Builder(this, "checkUser", new HttpResponceListner.Listener() {
                @Override
                public void onResponse(String response, String apiName) {
                    Log.d("hfjas", response);
                    try {
                        JSONObject js = new JSONObject(response);
                        String status = js.getString("status");
                        //String message = js.getString("message");
                        if (status.equalsIgnoreCase("success")) {
                            nextScreen();
                        } else {
                            showToast(getString(R.string.error_user_name_exist));
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
                    .execute(Registration2Activity.this.getClass().getName());
        }
    }

    private void nextScreen() {
        progressView3.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        progressView4.setBackgroundColor(ContextCompat.getColor(this, R.color.darkpink));

        switch (CURRENT_VIEW_STATE) {
            case 3:
                CURRENT_VIEW_STATE = 4;
                viewSwitcher.showNext();
                ((ImageView) findViewById(R.id.iv_bg)).setImageResource(R.drawable.k6_bg);
                progressView4.setBackgroundColor(ContextCompat.getColor(this, R.color.darkpink));
                break;

            case 4:
                findViewById(R.id.btnContinue2).setEnabled(false);
                callregistrationApi();
                break;

        }

    }


    public void callregistrationApi() {
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(Registration2Activity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        callregistrationApi();
                    }

                }
            }).show();
        }
        String deviceToken = FirebaseInstanceId.getInstance().getToken();//"android without firebase";
        Map<String, String> params = new HashMap<>();
        params.put("userName", user.userName.toLowerCase());
        params.put("firstName", user.firstName);
        params.put("lastName", user.lastName);
        params.put("email", user.email);
        params.put("password", user.password);

        if (user.contactNo != null && !user.contactNo.equals("")) {
            params.put("countryCode", user.countryCode);
            params.put("contactNo", user.contactNo);
        } else {
            params.put("countryCode", "");
            params.put("contactNo", "");
        }

        String gender = user.gender.toLowerCase();
        //  params.put("businessName", user.businessName);
        params.put("gender", gender);

        Calendar calendar_new = new GregorianCalendar(yearShow, monthShow, dayShow);
        String dob = simpleDateFormat.format(calendar_new.getTime());

        dob =  Helper.formateDateFromstring("dd/MM/yyyy","yyyy-MM-dd",dob);//2019-02-01

        params.put("dob", dob);
        params.put("address", address.stAddress1);
        params.put("city", address.city);
        params.put("state", address.state);
        params.put("country", address.country);
        params.put("businessPostCode", address.postalCode);
        params.put("latitude", address.latitude);
        params.put("longitude", address.longitude);
        params.put("userType", "user");
        params.put("businessType", "N/A");
        params.put("deviceType", "2");
        params.put("deviceToken", deviceToken);
        params.put("appType", "social");

        if (user.socialId != null) {
            if (!user.socialId.equals("")) {
                params.put("socialId", user.socialId);
                params.put("socialType", user.userType);
            }
        } else {
            params.put("socialId", "");
            params.put("socialType", "");
        }

        params.put("firebaseToken", deviceToken);

        //api.signUpTask(params, profileImageBitmap);
        HttpTask task = new HttpTask(new HttpTask.Builder(this, "userRegistration", new HttpResponceListner.Listener() {
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
                        session.createSession(user);
                        user.password = edConfirmPwd.getText().toString();
                        session.setPassword(user.password);
                        user.id = userObj.getInt("_id");
                        // checkUserRember(user);
                        writeNewUser(user);
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
                findViewById(R.id.btnContinue2).setEnabled(true);
            }
        })
                .setParam(params)
                .setProgress(true));
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

        mDatabase.child("users").child(String.valueOf(user.id)).setValue(firebaseUser);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("user", user);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    // check permission or Get image from camera or gallery
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
                   // CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.OVAL).setAspectRatio(400, 400).start(this);

                    CropImage.activity(imageUri).setCropShape(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? CropImageView.CropShape.RECTANGLE : CropImageView.CropShape.OVAL).setAspectRatio(400, 400).start(this);

                } else {
                    showToast(getString(R.string.msg_some_thing_went_wrong));
                }

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                try {
                    if (result != null)
                        profileImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getUri());

                    if (profileImageBitmap != null) {
                        profile_image.setImageBitmap(profileImageBitmap);


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (requestCode == 1003) {
                Country country = (Country) data.getSerializableExtra("country");
                txt_address.setText(String.format("+%s", country.phone_code));
                //String countryCode = "+" + country.phone_code;


            } else if (requestCode == 1001) {
                address = (Address) data.getSerializableExtra("address");
                if (address != null) {
                    txt_address.setVisibility(View.VISIBLE);
                    txt_address.setText(String.format("%s",
                            TextUtils.isEmpty(address.placeName) ? address.stAddress1 : address.placeName));
                }

            }
        }
    }


 /*   case 1003: {
        Country country = (Country) data.getSerializableExtra("country");
        tvCountryCode.setText(String.format("+%s", country.phone_code));
        countryCode = "+"+country.phone_code;
        break;
    }

                case 1001: {
        address = (Address) data.getSerializableExtra("address");
        if(address!=null){
            tvAddressHint.setVisibility(View.VISIBLE);
            tvAddress.setText(String.format("%s",
                    TextUtils.isEmpty(address.placeName)?address.stAddress1:address.placeName));
        }
        break;
    }*/

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(Registration2Activity.this);
                } else showToast("YOUR  PERMISSION DENIED");
            }
            break;
        }
    }

    private boolean isValidPassword(EditText edPwd /*TextInputLayout inputLayout*/) {
        // Pattern regex = Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]");
        String password = edPwd.getText().toString().trim();
        // Pattern specailCharPatten = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Pattern UpperCasePatten = Pattern.compile("[A-Z ]");
        // Pattern lowerCasePatten = Pattern.compile("[a-z ]");
        Pattern digitCasePatten = Pattern.compile("[0-9 ]");

        if (TextUtils.isEmpty(password)) {
            //inputLayout.setError(getString(R.string.error_password_required));
            showToast(getString(R.string.error_password_required));
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
        if (!edPwd.getText().toString().equals(edConfirmPwd.getText().toString())) {
            //input_layout_cnfPwd.setError(getString(R.string.error_confirm_password_not_match));
            showToast(getString(R.string.error_confirm_password_not_match));
            return false;
        }
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


    private boolean validUserName(EditText editText) {
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            showToast(getString(R.string.error_required_field));
            //inputLayout.setError(getString(R.string.error_required_field));
            editText.requestFocus();
            return false;
        } else if (text.contains(" ")) {
            showToast(getString(R.string.error_username_contain_space));
            //inputLayout.setError(getString(R.string.error_username_contain_space));
            editText.requestFocus();
            return false;
        } else if (text.length() < 4) {
            showToast(getString(R.string.error_username_length));
            //inputLayout.setError(getString(R.string.error_username_length));
            editText.requestFocus();
            return false;
        } else if (text.length() > 20) {
            showToast(getString(R.string.username_twenty));
            //inputLayout.setError(getString(R.string.error_username_length));
            editText.requestFocus();
            return false;
        }
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

    private boolean checkDOB() {
        if (tv_dob.getText().toString().equals(getString(R.string.date_of_birth))) {
            showToast(getString(R.string.error_dob));
            return false;
        }
        return true;
    }

    /*private void datePicker(){
        // Get Current Date
        final Calendar c = GregorianCalendar.getInstance();
        int mYear = c.get(GregorianCalendar.YEAR);
        int mMonth = c.get(GregorianCalendar.MONTH);
        int mDay = c.get(GregorianCalendar.DAY_OF_MONTH);
        final int[] dayId = {c.get(GregorianCalendar.DAY_OF_WEEK) - 1};
        String weekday = new DateFormatSymbols().getShortWeekdays()[dayId[0]];

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Date date = new Date(year, monthOfYear, dayOfMonth-1);
                        dayId[0] = date.getDay()-1;
                        String sDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                        tv_dob.setText(sDate);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }
*/
    private void showToast(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            MyToast.getInstance(this).showDasuAlert(msg);
        }
    }

    @Override
    public void onBackPressed() {
        if (CURRENT_VIEW_STATE == 4) {
            CURRENT_VIEW_STATE = 3;
            viewSwitcher.showPrevious();
            ((ImageView) findViewById(R.id.iv_bg)).setImageResource(R.drawable.profile_pic_bg);
            edPwd.setText("");
            edConfirmPwd.setText("");
            // input_layout_pwd.setError(null);
            // input_layout_cnfPwd.setError(null);
            progressView4.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            progressView3.setBackgroundColor(ContextCompat.getColor(this, R.color.darkpink));

        }
        if (user.socialId != null) {
            if (!user.socialId.equals("")) {
                Intent intent = new Intent(Registration2Activity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }else {
                Intent intent = new Intent(Registration2Activity.this, RegistrationActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        } else {
            Intent intent = new Intent(Registration2Activity.this, RegistrationActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void checkUserRember(User user) {
        SharedPreferanceUtils sp = new SharedPreferanceUtils(this);
        // boolean isRemind = true;
        sp.setParam(Constant.isLoginReminder, true);
        sp.setParam(Constant.USER_ID, user.userName);
        sp.setParam(Constant.USER_PASSWORD, user.password);
    }



    void showDate(int year, int month, int day) {
        new Handler().post(() -> new SpinnerDatePickerDialogBuilder()
                .context(Registration2Activity.this)
                .callback(Registration2Activity.this)
                .isActivity("Registration")
                .dialogTheme(R.style.AppTheme)
                .defaultDate(year, month, day)
                .build()
                .show());


    }

    @Override
    public void onDateSet(final com.mualab.org.user.customdobcalender.DatePicker view, int year, int monthOfYear, int dayOfMonth, int type, boolean isClicked) {
        Calendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);

        if (type == 0) {
            if (!isClicked) {
                if (!tv_dob.getText().toString().equals("Date of Birth")) {
                    Calendar calendar_new = new GregorianCalendar(yearShow, monthShow, dayShow);
                    tv_dob.setText(simpleDateFormat.format(calendar_new.getTime()));
                } else tv_dob.setText(R.string.date_of_birth);

            } else {
                Calendar calendar_new = new GregorianCalendar(yearShow, monthShow, dayShow);
                tv_dob.setText(simpleDateFormat.format(calendar_new.getTime()));
            }

        } else {
            tv_dob.setText(simpleDateFormat.format(calendar.getTime()));
            user.dob = simpleDateFormat.format(calendar.getTime());
            yearShow = year;
            monthShow = monthOfYear;
            dayShow = dayOfMonth;


        }


    }


    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {


    }


    private boolean hasSoftKeys(WindowManager windowManager) {
        boolean hasSoftwareKeys = true;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display d = windowManager.getDefaultDisplay();

            DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            d.getRealMetrics(realDisplayMetrics);

            int realHeight = realDisplayMetrics.heightPixels;
            int realWidth = realDisplayMetrics.widthPixels;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            d.getMetrics(displayMetrics);

            int displayHeight = displayMetrics.heightPixels;
            int displayWidth = displayMetrics.widthPixels;

            hasSoftwareKeys = (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
        } else {
            boolean hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey();
            boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            hasSoftwareKeys = !hasMenuKey && !hasBackKey;
        }
        return hasSoftwareKeys;
    }


}

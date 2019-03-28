package com.mualab.org.user.activity.authentication;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.authentication.instagramLogin.ApplicationData;
import com.mualab.org.user.activity.authentication.instagramLogin.InstagramApp;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.model.FirebaseUser;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.local.prefs.SharedPreferanceUtils;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.StatusBarUtil;
import com.mualab.org.user.utils.constants.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.mualab.org.user.data.local.prefs.Session.isLogout;

public class LoginActivity extends AppCompatActivity {
    private TextView ed_username, ed_password;
    private SharedPreferanceUtils sp;
    private Session session;
    private long mLastClickTime = 0;
    private boolean isRemind = true;
    private CallbackManager callbackManager;
    InstagramApp mApp;
    private HashMap<String, String> userInfoHashmap = new HashMap<String, String>();

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == InstagramApp.WHAT_FINALIZE) {
                userInfoHashmap = mApp.getUserInfo();
                String socialId = userInfoHashmap.get("id");
                String full_name = userInfoHashmap.get("full_name");
                String username = userInfoHashmap.get("username");
                String profileImage = userInfoHashmap.get("profile_picture");

                if (full_name.contains(" ")) {
                    String[] splited = full_name.split("\\s+");
                    String firstname = splited[0];
                    String lastname = splited[1];
                    checkSocialUser(socialId, "insta", "", firstname, lastname, profileImage, username);
                } else {
                    checkSocialUser(socialId, "insta", "", full_name, "", profileImage, username);
                }
            } else if (msg.what == InstagramApp.WHAT_FINALIZE) {
                Toast.makeText(LoginActivity.this, "Check your network.",
                        Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    });


    public static Intent newIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(LoginActivity.this);
        setContentView(R.layout.activity_login);
        sp = new SharedPreferanceUtils(this);
        session = Mualab.getInstance().getSessionManager();
        initView();
        if ((Boolean) sp.getParam(Constant.isLoginReminder, Boolean.FALSE)) {
            isRemind = true;
            ed_username.setText(String.valueOf(sp.getParam(Constant.USER_ID, "")));
            ed_password.setText(String.valueOf(sp.getParam(Constant.USER_PASSWORD, "")));
        }
        StatusBarUtil.setColorNoTranslucent(this, getResources().getColor(R.color.colorPrimary));

        findViewById(R.id.createNewAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


        findViewById(R.id.createaccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        callbackManager = CallbackManager.Factory.create();
        facebookLogin();

        mApp = new InstagramApp(this, ApplicationData.CLIENT_ID,
                ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);

        mApp.setListener(new InstagramApp.OAuthAuthenticationListener() {

            @Override
            public void onSuccess() {
                // tvSummary.setText("Connected as " + mApp.getUserName());
                //btnConnect.setText("Disconnect");
                //llAfterLoginView.setVisibility(View.VISIBLE);
                // userInfoHashmap = mApp.
                mApp.fetchUserName(handler);
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void initView() {
        ed_username = findViewById(R.id.ed_username);
        ed_password = findViewById(R.id.ed_password);
        ImageView ivFacebook = findViewById(R.id.ivFacebook);
        ImageView ivInstragram = findViewById(R.id.ivInstragram);
        findViewById(R.id.tvForgotPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
                    return;
                }

                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                /*new ForgotPassword(LoginActivity.this, new ForgotPassword.Listner() {
                    @Override
                    public void onSubmitClick(final Dialog dialog, final String emailId) {
                        forgotPassword(dialog, emailId);

                    }
                    @Override
                    public void onDismis(Dialog dialog) {
                        dialog.dismiss();
                    }
                }).show();*/
            }
        });

        ivFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ConnectionDetector.isConnected()) {
                    new NoConnectionDialog(LoginActivity.this, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if (isConnected) {
                                dialog.dismiss();
                            }

                        }
                    }).show();
                }
                Progress.show(LoginActivity.this);
                final LoginManager loginManager = LoginManager.getInstance();
                loginManager.logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "email"));

            }
        });

        ivInstragram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mApp.hasAccessToken()) {
                    mApp.fetchUserName(handler);
                } else mApp.authorize();
            }
        });

        findViewById(R.id.tvCustomerApp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* String appPackageName = "com.mualab.org.biz";//getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    showToast(getString(R.string.under_development));
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
*/

            }
        });

        final TextView btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
                    return;
                }
                KeyboardUtil.hideKeyboard(btn_login, LoginActivity.this);
                loginProcess();
            }
        });
    }


    private void showToast(String msg) {
        if (!TextUtils.isEmpty(msg))
            MyToast.getInstance(this).showDasuAlert(msg);
        //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private boolean validatePassword() {
        String password = ed_password.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            //input_layout_password.setError(getString(R.string.error_password_required));
            showToast(getString(R.string.error_password_required));
            ed_password.requestFocus();
            return false;
        } else if (password.length() < 8) {
            //input_layout_password.setError(getString(R.string.error_invalid_password_length));
            showToast(getString(R.string.error_invalid_password_length));
            ed_password.requestFocus();
            return false;
        } /*else {
            input_layout_password.setErrorEnabled(false);
        }*/
        return true;
    }


    private void checkSocialUser(final String socialId, final String socialType,
                                 final String email, final String firstname,
                                 final String lastname, final String profileImage, final String username) {

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(LoginActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        checkSocialUser(socialId, socialType, email, firstname, lastname, profileImage, username);
                    }
                }
            }).show();
        }

        String deviceToken = FirebaseInstanceId.getInstance().getToken();//"androidTest";

        Map<String, String> params = new HashMap<>();
        params.put("socialId", socialId);
        params.put("socialType", socialType);
        params.put("deviceToken", deviceToken);


        new HttpTask(new HttpTask.Builder(this, "checksocialLogin", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(LoginActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        Gson gson = new Gson();
                        JSONObject userObj = js.getJSONObject("data");
                        User user = gson.fromJson(String.valueOf(userObj), User.class);
                        user.id = userObj.getInt("_id");

                        if (user.status.equals("0")) {
                            showToast("You are currenlty inactive by admin");
                        } else {
                            session.createSession(user);
                            session.setPassword(user.password);
                            // checkUserRember(user);
                            writeNewUser(user);
                        }
                    } else {
                        // goto 3rd screen for register

                        User user = new User();
                        user.socialId = socialId;
                        user.userType = socialType;
                        user.email = email;
                        user.firstName = firstname;
                        user.lastName = lastname;
                        user.profileImage = profileImage;
                        user.userName = username;
                        startActivityForResult(new Intent(LoginActivity.this, Registration2Activity.class)
                                .putExtra(Constant.USER, user), 2);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Progress.hide(LoginActivity.this);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(LoginActivity.this);
            }
        })
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON)
                .setProgress(true))
                .execute(this.getClass().getName());

    }


    private void writeNewUser(User user) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = new FirebaseUser();
        firebaseUser.firebaseToken = FirebaseInstanceId.getInstance().getToken();

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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("user", user);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private boolean validateName() {
        if (ed_username.getText().toString().trim().isEmpty()) {
            //input_layout_UserName.setError(getString(R.string.error_email_or_username_required));
            ed_username.requestFocus();
            showToast(getString(R.string.error_email_or_username_required));
            return false;
        } /*else {
            input_layout_UserName.setErrorEnabled(false);
        }*/

        return true;
    }

    private void checkUserRember(User user) {
        if (isRemind) {
            sp.setParam(Constant.isLoginReminder, true);
            sp.setParam(Constant.USER_ID, user.userName);
            sp.setParam(Constant.USER_PASSWORD, ed_password.getText().toString());
        } else {
            sp.setParam(Constant.isLoginReminder, false);
            sp.setParam(Constant.USER_ID, "");
            sp.setParam(Constant.USER_PASSWORD, "");
        }
    }

    private void facebookLogin() {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                String accessToken = loginResult.getAccessToken().getToken();
                final String sSocialId = loginResult.getAccessToken().getUserId();
                final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        if (response.getError() != null) {
                            MyToast.getInstance(LoginActivity.this).showDasuAlert(getResources().getString(R.string.msg_some_thing_went_wrong));
                        } else {
                            try {
                                String email = "";
                                final String socialId = object.getString("id");
                                final String firstname = object.getString("first_name");
                                final String lastname = object.getString("last_name");
                                final String fullname = firstname + " " + lastname;
                                final String profileImage = "https://graph.facebook.com/" + sSocialId + "/picture?width=200&height=200";
                                final String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                if (object.has("email")) {
                                    email = object.getString("email");
                                }


                                checkSocialUser(socialId, "facebook", email, firstname, lastname, profileImage, "");


                            } catch (JSONException e) {
                                e.printStackTrace();
                                Progress.hide(LoginActivity.this);
                            }
                        }

                    }

                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, email, picture");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Progress.hide(LoginActivity.this);
            }

            @Override
            public void onError(FacebookException error) {
                Progress.hide(LoginActivity.this);
                if (error instanceof FacebookAuthorizationException) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut();
                    }
                }
            }
        });
    }

    private void loginProcess() {
        boolean isValidInput = true;
        FirebaseApp.initializeApp(LoginActivity.this);
        String username = ed_username.getText().toString().trim().toLowerCase();
        String password = ed_password.getText().toString().trim();
        String deviceToken = FirebaseInstanceId.getInstance().getToken();//"androidTest";

        if (!validateName() || !validatePassword()) {
            isValidInput = false;
        }

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(LoginActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        loginProcess();
                    }

                }
            }).show();

            isValidInput = false;
        }

        if (isValidInput) {
            Map<String, String> params = new HashMap<>();
            params.put("userName", username);
            params.put("password", password);
            params.put("deviceToken", deviceToken);
            params.put("firebaseToken", deviceToken);
            params.put("deviceType", "2");
            params.put("userType", "user");
            params.put("appType", "social");

            new HttpTask(new HttpTask.Builder(this, "userLogin", new HttpResponceListner.Listener() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);
                        String status = js.getString("status");
                        String message = js.getString("message");
                        if (status.equalsIgnoreCase("success")) {
                            Gson gson = new Gson();
                            JSONObject userObj = js.getJSONObject("users");
                            User user = gson.fromJson(String.valueOf(userObj), User.class);
                            user.id = userObj.getInt("_id");

                            if (user.status.equals("0")) {
                                showToast("You are currenlty inactive by admin");
                            } else {
                                // showToast(message);
                                session.createSession(user);
                                session.setPassword(user.password);
                                // checkUserRember(user);
                                writeNewUser(user);
                     /*           Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("user", user);
                                startActivity(intent);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();*/
                            }
                        } else
                            showToast(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                }
            })
                    .setBody(params, HttpTask.ContentType.APPLICATION_JSON)
                    .setProgress(true))
                    .execute(this.getClass().getName());
        }
    }


}

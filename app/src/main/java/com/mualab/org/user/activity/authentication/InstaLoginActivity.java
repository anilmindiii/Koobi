package com.mualab.org.user.activity.authentication;

import android.app.Application;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.main.MainActivity;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.net.ssl.HttpsURLConnection;

import static com.mualab.org.user.utils.constants.Constant.AUTHURL;
import static com.mualab.org.user.utils.constants.Constant.CLIENT_ID;
import static com.mualab.org.user.utils.constants.Constant.CLIENT_SECRET;
import static com.mualab.org.user.utils.constants.Constant.REDIRECT_URI;
import static com.mualab.org.user.utils.constants.Constant.SP;
import static com.mualab.org.user.utils.constants.Constant.SP_DP;
import static com.mualab.org.user.utils.constants.Constant.SP_NAME;
import static com.mualab.org.user.utils.constants.Constant.SP_TOKEN;
import static com.mualab.org.user.utils.constants.Constant.TOKENURL;


public class InstaLoginActivity extends AppCompatActivity {

    private String TAG = "MyApp";
    private String authURLFull;
    private String tokenURLFull;
    private String code;
    private String accessTokenString;
    private String dp;
    private String fullName;

    private Dialog dialog;
    ProgressBar progressBar;

    SharedPreferences spUser;
    SharedPreferences.Editor spEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insta_login);

        spUser = getSharedPreferences(SP, MODE_PRIVATE);
        if (isLoggedIn()){
           startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        authURLFull = AUTHURL + "client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI + "&response_type=code&display=touch";
        tokenURLFull = TOKENURL + "?client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&redirect_uri=" + REDIRECT_URI + "&grant_type=authorization_code";
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        progressBar = new ProgressBar(this);
        onClickLogin();
    }

    /*****  When login button is clicked **************************************/
    public void onClickLogin() {
        setupWebviewDialog(authURLFull);
        progressBar.setVisibility(View.VISIBLE);
    }

    /*****  Show Instagram login page in a dialog *****************************/
    public void setupWebviewDialog(String url) {
        dialog = new Dialog(this);
        dialog.setTitle("Insta Login");

        WebView webView = new WebView(this);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new MyWVClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);

        dialog.setContentView(webView);
    }

    /*****  A client to know about WebView navigations  ***********************/
    class MyWVClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.bringToFront();
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (request.getUrl().toString().contains(REDIRECT_URI)) {
                handleUrl(request.getUrl().toString());
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(REDIRECT_URI)) {
                handleUrl(url);
                return true;
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.INVISIBLE);
            dialog.show();
        }
    }

    /*****  Check webview url for access token code or error ******************/
    public void handleUrl(String url) {

        if (url.contains("code")) {
            String temp[] = url.split("=");
            code = temp[1];
            new MyAsyncTask(code).execute();

        } else if (url.contains("error")) {
            String temp[] = url.split("=");
            Log.e(TAG, "Login error: "+temp[temp.length - 1]);
        }
    }

    /*****  AsyncTast to get user details after successful authorization ******/
    public class MyAsyncTask extends AsyncTask<URL, Integer, Long> {
        String code;

        public MyAsyncTask(String code) {
            this.code = code;
        }

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        protected Long doInBackground(URL... urls) {
            long result = 0;

            try {
                URL url = new URL(tokenURLFull);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setRequestMethod("GET");
                httpsURLConnection.setDoInput(true);
                httpsURLConnection.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpsURLConnection.getOutputStream());
                outputStreamWriter.write("client_id=" + CLIENT_ID +
                        "&client_secret=" + CLIENT_SECRET +
                        "&grant_type=authorization_code" +
                        "&redirect_uri=" + REDIRECT_URI +
                        "&code=" + code);

                outputStreamWriter.flush();
                String response = streamToString(httpsURLConnection.getInputStream());
                JSONObject jsonObject = (JSONObject) new JSONTokener(response).nextValue();
                accessTokenString = jsonObject.getString("access_token"); //Here is your ACCESS TOKEN
                dp = jsonObject.getJSONObject("user").getString("profile_picture");
                fullName = jsonObject.getJSONObject("user").getString("full_name"); //This is how you can get the user info. You can explore the JSON sent by Instagram as well to know what info you got in a response

                spEdit = spUser.edit();
                spEdit.putString(SP_TOKEN, accessTokenString);
                spEdit.putString(SP_NAME, fullName);
                spEdit.putString(SP_DP, dp);
                spEdit.commit();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        protected void onPostExecute(Long result) {
            dialog.dismiss();
            progressBar.setVisibility(View.INVISIBLE);
          //  startActivity(new Intent(InstaLoginActivity.this, WelcomeActivity.class));
            //finish();
        }
    }

    /*****  Converting stream to string ***************************************/
    public static String streamToString(InputStream is) throws IOException {
        String str = "";

        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

            } finally {
                is.close();
            }
            str = sb.toString();
        }
        return str;
    }

    private boolean isLoggedIn(){
        String token = spUser.getString(SP_TOKEN, null);
        if (token != null){
            return true;
        }
        return false;
    }



}

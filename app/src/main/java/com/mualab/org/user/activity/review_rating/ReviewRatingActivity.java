package com.mualab.org.user.activity.review_rating;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingConfirmActivity;
import com.mualab.org.user.activity.review_rating.adapter.ReviewRatingAdapter;
import com.mualab.org.user.activity.review_rating.model.ReviewRating;
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

public class ReviewRatingActivity extends AppCompatActivity implements View.OnClickListener {

    private String userId = "";
    private String userType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_rating);

        userId = getIntent().getStringExtra("id");
        userType = getIntent().getStringExtra("userType");

        initView();
    }

    private void initView() {
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.reviews));

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        doGetReviewRating();
    }

    private void doGetReviewRating() {
        Progress.show(ReviewRatingActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ReviewRatingActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        doGetReviewRating();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        //params.put("userId", String.valueOf(Mualab.currentUser.id));


        HttpTask task = new HttpTask(new HttpTask.Builder(ReviewRatingActivity.this, "getRatingReview?userType="+userType+"&userId="+userId+"", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(ReviewRatingActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equals("success")) {
                        Gson gson = new Gson();
                        ReviewRating reviewRating = gson.fromJson(response, ReviewRating.class);
                        if (reviewRating.getStatus().equals("success"))
                            updateUI(reviewRating);
                        else MyToast.getInstance(ReviewRatingActivity.this).showDasuAlert(reviewRating.getMessage());
                    }
                } catch (Exception e) {
                    Progress.hide(ReviewRatingActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(ReviewRatingActivity.this);
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
                .setMethod(Request.Method.GET)
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(this.getClass().getName());
    }

    private void updateUI(ReviewRating reviewRating) {
        TextView tvNoRecord = findViewById(R.id.tvNoRecord);
        RecyclerView rvReviewRating = findViewById(R.id.rvReviewRating);

        tvNoRecord.setVisibility(reviewRating.getData().isEmpty() ? View.VISIBLE : View.GONE);
        ReviewRatingAdapter adapter = new ReviewRatingAdapter(reviewRating.getData());
        rvReviewRating.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }
}

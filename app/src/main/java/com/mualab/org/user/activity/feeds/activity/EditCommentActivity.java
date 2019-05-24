package com.mualab.org.user.activity.feeds.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.base.BaseActivity;
import com.mualab.org.user.activity.feeds.model.Comment;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.API;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.KeyboardUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;

public class EditCommentActivity extends BaseActivity implements View.OnClickListener {

    private EditText etComments;

    private Comment comment;
    private Feeds feed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_comment);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.edit_comment));

        findViewById(R.id.btnBack).setOnClickListener(this);
        findViewById(R.id.btnCancel).setOnClickListener(this);
        findViewById(R.id.btnUpdate).setOnClickListener(this);

        ImageView iv_profileImage = findViewById(R.id.iv_profileImage);
        etComments = findViewById(R.id.etComments);

        if (getIntent().hasExtra("CommentInfo")) {
            comment = (Comment) getIntent().getSerializableExtra("CommentInfo");
            feed = (Feeds) getIntent().getSerializableExtra("FeedInfo");

            if (!comment.profileImage.isEmpty()) {
                Picasso.with(getActivity()).load(comment.profileImage)
                        .fit()
                        .placeholder(R.drawable.default_placeholder)
                        .error(R.drawable.default_placeholder)
                        .into(iv_profileImage);
            } else
                Picasso.with(getActivity()).load(R.drawable.default_placeholder).into(iv_profileImage);

            etComments.setText(comment.comment);
        }

    }

    private void doPostEditComment(final String comments) {
        if (!ConnectionDetector.isConnected()) {
           // NoConnectionDialog.newInstance(() -> doPostEditComment(comments)).show(getSupportFragmentManager(), "EditCommentActivity");
            return;
        }
        Progress.show(getActivity());

        User user = Mualab.getInstance().getSessionManager().getUser();
        HashMap<String, String> header = new HashMap<>();
        header.put("authToken", user.authToken);

        HashMap<String, String> params = new HashMap<>();

        params.put("feedId", "" + feed._id);
        params.put("comment", comments);
        params.put("postUserId", "" + feed.userId);
        params.put("userId", "" + user.id);
        params.put("id", "" + comment.id);
        params.put("type", "text");


        AndroidNetworking.upload(API.BASE_URL+"editComment")
                .addHeaders(header)
                .addMultipartParameter(params)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Progress.hide(getActivity());
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        MyToast.getInstance(getActivity()).showDasuAlert(message);

                        onBackPressed();
                    } else {
                        MyToast.getInstance(getActivity()).showDasuAlert(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ANError anError) {
                Progress.hide(getActivity());
                Helper.parseError(getActivity(), anError);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
            case R.id.btnCancel:
                onBackPressed();
                break;

            case R.id.btnUpdate:
                KeyboardUtil.hideKeyboard(v,getActivity());
                String commentTxt = etComments.getText().toString().trim();

                if (commentTxt.isEmpty()) {
                    MyToast.getInstance(getActivity()).showDasuAlert("Please enter comment");
                } else if (commentTxt.equals(comment.comment)) {
                    onBackPressed();
                } else doPostEditComment(commentTxt);
                break;
        }
    }


}

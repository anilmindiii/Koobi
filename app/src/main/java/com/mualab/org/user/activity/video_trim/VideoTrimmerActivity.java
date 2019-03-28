package com.mualab.org.user.activity.video_trim;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.deep.videotrimmer.DeepVideoTrimmer;
import com.deep.videotrimmer.interfaces.OnTrimVideoListener;
import com.deep.videotrimmer.view.RangeSeekBarView;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.camera.CameraActivity;
import com.mualab.org.user.activity.feeds.activity.FeedPostActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.model.MediaUri;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.databinding.ActivityVideoTrimmerBinding;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.constants.Constant;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class VideoTrimmerActivity extends AppCompatActivity implements OnTrimVideoListener {
    ActivityVideoTrimmerBinding mBinder;
    private DeepVideoTrimmer mVideoTrimmer;
    TextView textSize, tvCroppingMessage;
    RangeSeekBarView timeLineBar;
    private MediaUri mediaUri;
    private String caption;
    private int feedType;
    private Bitmap thumbImage = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinder = DataBindingUtil.setContentView(this, R.layout.activity_video_trimmer);

        String path = "";

        Intent intent = getIntent();
        if (intent != null) {
            caption = intent.getStringExtra("caption");
            thumbImage = intent.getParcelableExtra("thumbImage");
            mediaUri = (MediaUri) intent.getSerializableExtra("mediaUri");
            feedType = intent.getIntExtra("feedType", Constant.IMAGE_STATE);


            if (!mediaUri.isFromGallery) {
                path = mediaUri.videoFile.toString();
            } else path = mediaUri.uri;

            /*file:///storage/emulated/0/Android/data/com.mualab.org.user/cache/tmp.mp4*/
        }


        mVideoTrimmer = findViewById(R.id.timeLine);
        timeLineBar = findViewById(R.id.timeLineBar);
        textSize = findViewById(R.id.textSize);
        tvCroppingMessage = findViewById(R.id.tvCroppingMessage);

        if (mVideoTrimmer != null && path != null) {
            mVideoTrimmer.setMaxDuration(60);
            mVideoTrimmer.setMaxFileSize(100);
            mVideoTrimmer.setOnTrimVideoListener(this);
            mVideoTrimmer.setVideoURI(Uri.parse(path));
        } else {
            // showToastLong(getString(R.string.toast_cannot_retrieve_selected_video));
        }
    }

    @Override
    public void getResult(final Uri uri) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvCroppingMessage.setVisibility(View.GONE);
            }
        });
        Constant.croppedVideoURI = uri.toString();

        if (feedType == 90) {
           final File photoPath = new File(uri.toString());
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    addMyStory(photoPath);

                }
            });

          /*   Intent  intent = new Intent(VideoTrimmerActivity.this, CameraActivity.class);
            intent.putExtra("new_uri",uri.toString());
            setResult(RESULT_OK,intent);*/

        } else if (mediaUri != null) {

            Intent intent = new Intent(this, FeedPostActivity.class);

            mediaUri.uri = uri.toString();
            mediaUri.uriList.set(0, uri.toString());

            intent.putExtra("caption", "");
            intent.putExtra("mediaUri", mediaUri);
            intent.putExtra("thumbImage", thumbImage);
            intent.putExtra("feedType", mediaUri.mediaType);
            intent.putExtra("requestCode", Constant.POST_FEED_DATA);

            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setData(uri);
            setResult(RESULT_OK, intent);
            startActivityForResult(intent, Constant.POST_FEED_DATA);

        }


       /* Intent intent = new Intent();
        intent.setData(uri);
        setResult(RESULT_OK, intent);
        finish();*/

    }

    @Override
    public void cancelAction() {
        mVideoTrimmer.destroy();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvCroppingMessage.setVisibility(View.GONE);
            }
        });
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Mualab.getInstance().cancelAllPendingRequests();
    }

    private void addMyStory(File photoPath) {

        if (ConnectionDetector.isConnected()) {

            Map<String, String> map = new HashMap<>();
            map.put("userId", "" + Mualab.currentUser.id);
            map.put("type", "video");

            //ivTakenPhoto.setDrawingCacheEnabled(true);
            Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),
                    R.drawable.default_placeholder);
              /* Bitmap bitmap = ImageVideoUtil.getVideoToThumbnil(photoPath., this,
                       MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);*/
            HttpTask task = new HttpTask(new HttpTask.Builder(this, "addMyStory", new HttpResponceListner.Listener() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);
                        String status = js.getString("status");
                        String message = js.getString("message");
                        if (status.equalsIgnoreCase("success")) {

                            Intent  intent = new Intent(VideoTrimmerActivity.this, CameraActivity.class);
                            setResult(RESULT_OK,intent);
                            finish();
                        } else showToast(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    Log.d("res:", "" + error.getLocalizedMessage());
                }
            })
                    .setParam(map)
                    .setAuthToken(Mualab.currentUser.authToken)
                    .setProgress(true));
            task.postFile("myStory", photoPath, bitmap);

        } else showToast(getString(R.string.error_msg_network));
    }

    private void showToast(String str) {
        if (!TextUtils.isEmpty(str))
            MyToast.getInstance(this).showSmallCustomToast(str);
    }
}

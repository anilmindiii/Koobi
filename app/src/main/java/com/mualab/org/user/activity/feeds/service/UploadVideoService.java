package com.mualab.org.user.activity.feeds.service;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.mualab.org.user.activity.feeds.activity.FeedPostActivity;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.remote.API;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.constants.Constant;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;


/**
 * Created by hemant
 * Date: 07/08/18.
 */

public class UploadVideoService extends IntentService {

    private int notificationId = 0;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;

    public UploadVideoService() {
        super("UploadVideoService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UploadVideoService(String name) {
        super(name);
    }

    private void doPostVideoFeed(HashMap<String, String> header, HashMap<String, String> params, HashMap<String, File> files) {

        AndroidNetworking.upload(API.BASE_URL + "addFeed")
                .addHeaders(header)
                .addMultipartParameter(params)
                .addMultipartFile(files)
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                        int a = (int) (bytesUploaded * 100L / totalBytes);
                        // videoBgUploadNotification("upload", a);

                        broadcastData("uploading", a);
                    }
                })
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        // do anything with response

                        try {
                            JSONObject js = new JSONObject(response);
                            String status = js.getString("status");
                            String message = js.getString("message");
                            if (status.equalsIgnoreCase("success")) {
                                //  setResult(Activity.RESULT_OK);
                                MyToast.getInstance(getApplicationContext()).showSmallMessage(message);
                                //videoBgUploadNotification("upload", 100);

                                Activity activity = Mualab.getInstance().getActiveActivity();
                                if (activity instanceof MainActivity) {
                                    broadcastData("uploaded success", 100);
                                }

                                //removeNotification();

                            } else {
                                //   MyToast.getInstance(getApplicationContext()).showSmallMessage(message);
                                broadcastData("uploaded fail", 100);
                                //videoBgUploadNotification("fail", 100);
                                //removeNotification();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        broadcastData("uploaded fail", 100);
                        // handle error
                        //videoBgUploadNotification("fail", 100);
                        //removeNotification();
                        Helper.parseErrorWithoutMsg(getApplicationContext(), error);
                    }
                });
    }

    private void broadcastData(String flag, int a) {
        Intent intent = new Intent(Constant.INTENT_FILTER_BIZ_PROFILE_ACTIVITY);
        intent.putExtra("status", flag);
        intent.putExtra("progress", a);

        if (flag.equals("uploaded success")) {
            //intent.putExtra(Constant.NOTIFICATION_NOTIFY_ID, Mualab.getInstance().getSessionManager().getUser().id);
            //intent.putExtra(Constant.NOTIFICATION_USER_TYPE, "artist");
        }
        sendBroadcast(intent);
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        assert intent != null;
        Bundle bundle = intent.getBundleExtra("payload");
        /*th = new LocationThread();
        handler.post(th);*/
        if (bundle != null) {

            HashMap<String, String> header = (HashMap<String, String>) bundle.getSerializable("header");
            HashMap<String, String> params = (HashMap<String, String>) bundle.getSerializable("params");
            HashMap<String, File> files = (HashMap<String, File>) bundle.getSerializable("files");



            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.putExtra("FeedPostActivity", "FeedPostActivity");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);

            doPostVideoFeed(header, params, files);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //videoBgUploadNotification("fail", 100);
    }
}

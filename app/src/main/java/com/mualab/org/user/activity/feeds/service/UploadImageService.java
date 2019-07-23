package com.mualab.org.user.activity.feeds.service;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.activity.FeedPostActivity;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.remote.API;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.image.compressor.ImageCompressor;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.constants.Constant;
import com.mualab.org.user.utils.media.FileUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by hemant
 * Date: 07/08/18.
 */

public class UploadImageService extends IntentService {

    private int notificationId = 0;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;

    public UploadImageService() {
        super("UploadImageService");
    }


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UploadImageService(String name) {
        super(name);
    }

    private void doPostImageFeed(HashMap<String, String> header, HashMap<String, String> params, HashMap<String, List<File>> files) {

        AndroidNetworking.upload(API.BASE_URL + "addFeed")
                .addHeaders(header)
                .addMultipartParameter(params)
                .addMultipartFileList(files)
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                        int a = (int) (bytesUploaded * 100L / totalBytes);
                        //imageBgUploadNotification("upload", a);
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
                                //imageBgUploadNotification("upload", 100);

                                Activity activity = Mualab.getInstance().getActiveActivity();
                                if (activity instanceof MainActivity) {
                                    broadcastData("uploaded success", 100);
                                }

                                //removeNotification();
                            } else {
                                //   MyToast.getInstance(getApplicationContext()).showSmallMessage(message);
                                broadcastData("uploaded fail", 100);
                                //imageBgUploadNotification("fail", 100);
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
                        //imageBgUploadNotification("fail", 100);
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
           // intent.putExtra(Constants.NOTIFICATION_NOTIFY_ID, Mualab.getInstance().getSessionManager().getUser().id);
            //intent.putExtra(Constants.NOTIFICATION_USER_TYPE, "artist");
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
            ArrayList<String> uris = (ArrayList<String>) bundle.getSerializable("uris");

            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.putExtra("FeedPostActivity", "FeedPostActivity");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);

            ArrayList<Uri> mSelectedImages = new ArrayList<>();
            assert uris != null;
            for (String uri : uris)
                mSelectedImages.add(Uri.parse(uri));


            //initNotification();
            prepareDataToUpload(header, params, mSelectedImages);
        }
    }


    private void prepareDataToUpload(HashMap<String, String> header, HashMap<String, String> params,
                                     ArrayList<Uri> mSelectedImages) {
        List<File> images = new ArrayList<>();
        HashMap<String, List<File>> files = new HashMap<>();
        for (int index = 0, size = mSelectedImages.size(); index < size; index++) {

            Uri uri = mSelectedImages.get(index);
            if (uri.toString().contains("/storage/emulated/0/Android/data/com.mualab.org.user/cache/i_prefix")) {
                String str = uri.toString().replace("/storage/emulated/0/Android/data/com.mualab.org.user/cache/", "");
                //images.add(new File(mContext.getExternalCacheDir(), str));
                try {

                    images.add(new ImageCompressor(getApplicationContext()).compressToFile(new File(getApplicationContext().getExternalCacheDir(), str)));
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }
            } else {
                //String authority = uri.getAuthority();
                Bitmap bitmap;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), uri);
                    File file = FileUtils.savebitmap(getApplicationContext(), bitmap, "tmp" + index);
                    //images.add(file);

                    images.add(new ImageCompressor(getApplicationContext()).compressToFile(file));
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }
        }
        files.put("feed", images);
        doPostImageFeed(header, params, files);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //imageBgUploadNotification("fail", 100);
    }
}

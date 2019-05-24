package com.mualab.org.user.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.androidnetworking.error.ANError;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
import com.mualab.org.user.activity.share_module.ShareActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.remote.API;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.ServerErrorDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Neha on 28/9/17.
 */

public class Helper {

    public static void parseError(Context context, ANError anError) {
        if (anError.getErrorBody() != null) {
            try {
                JSONObject jsonObject = new JSONObject(anError.getErrorBody());
                String status = jsonObject.getString("status");
                String message = "";
                if (jsonObject.has("message")) message = jsonObject.getString("message");

                if (message.equals("Invalid Auth Token")) {
                    Mualab.getInstance().getSessionManager().logout();
                }

            } catch (Exception e) {
                e.printStackTrace();
                MyToast.getInstance(context).showDasuAlert(context.getString(R.string.msg_some_thing_went_wrong));
            }
        } else if (anError.getErrorDetail() != null && anError.getErrorDetail().equalsIgnoreCase("connectionError")) {
            try {
                new ServerErrorDialog(context).show();
            } catch (Exception ignored) {
            }
        }
    }

    public static void apiForgetUserIdFromUserName(final CharSequence userName,Context mContext) {
        String user_name = "";

        final Map<String, String> params = new HashMap<>();
        if (userName.toString().startsWith("@")) {

            user_name = userName.toString().replaceFirst("@", "");
            params.put("userName", user_name + "");
        } else params.put("userName", userName + "");
        new HttpTask(new HttpTask.Builder(mContext, "profileByUserName", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Log.d("hfjas", response);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {

                        JSONObject userDetail = js.getJSONObject("userDetail");
                        String userType = userDetail.getString("userType");
                        int userId = userDetail.getInt("_id");

                        if (userType.equals("user")) {
                            Intent intent = new Intent(mContext, UserProfileActivity.class);
                            intent.putExtra("userId", String.valueOf(userId));
                            mContext.startActivity(intent);
                        } else if (userType.equals("artist") && userId == Mualab.currentUser.id) {
                            Intent intent = new Intent(mContext, UserProfileActivity.class);
                            intent.putExtra("userId", String.valueOf(userId));
                            mContext.startActivity(intent);
                        } else {
                            Intent intent = new Intent(mContext, ArtistProfileActivity.class);
                            intent.putExtra("artistId", String.valueOf(userId));
                            mContext.startActivity(intent);
                        }


                    } else {
                        MyToast.getInstance(mContext).showDasuAlert(message);
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
                .execute("FeedAdapter");
    }

    public  String error_Messages(VolleyError error) {
        NetworkResponse networkResponse = error.networkResponse;
        String errorMessage = "Something went wrong.";

        if (networkResponse == null) {
            if (error.getClass().equals(TimeoutError.class)) {
                errorMessage = "Request timeout error";
            } else if (error.getClass().equals(NoConnectionError.class)) {
                errorMessage = "Failed to connect server";
            }
        } else {
            String result = new String(networkResponse.data);
            try {
                JSONObject response = new JSONObject(result);
                String status = response.getString("status");
                String message = response.getString("message");

                Log.e("Error Message", message);

                if (networkResponse.statusCode == 404) {
                    errorMessage = "Resource not found";
                    return errorMessage;
                } else if (networkResponse.statusCode == 401) {
                    //errorMessage = message + " Check your inputs";
                    errorMessage =  "Your Session is expired,please login again.";
                    return errorMessage;

                } else if (networkResponse.statusCode == 400) {
                    errorMessage = "Your Session is expired,please login again.";
                    return errorMessage;

                } else if (networkResponse.statusCode == 500) {
                    errorMessage = message + " Something is getting wrong";
                    return errorMessage;
                }
                else if (networkResponse.statusCode == 300) {
                    errorMessage = message+" Session is expire.";
                    return errorMessage;
                }
                else if (message.equals("Invalid Auth Token")){
                    errorMessage = "Your Session is expired,please login again.";
                    return errorMessage;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("Error", errorMessage);
        error.printStackTrace();
        return errorMessage;
    }

    public static HashMap<String, String> getQueryString(String url) {
        Uri uri= Uri.parse(url);

        HashMap<String, String> map = new HashMap<>();
        for (String paramName : uri.getQueryParameterNames()) {
            if (paramName != null) {
                String paramValue = uri.getQueryParameter(paramName);
                if (paramValue != null) {
                    map.put(paramName, paramValue);
                }
            }
        }
        return map;
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, 92,92);
        drawable.draw(canvas);

        return bitmap;
    }

    public static String formateDateFromstring(String inputFormat, String outputFormat, String inputDate) {

        Date parsed = null;
        String outputDate = "";

        SimpleDateFormat df_input = new SimpleDateFormat(inputFormat, java.util.Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat(outputFormat, java.util.Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
            outputDate = df_output.format(parsed);

        } catch (ParseException e) {
            //Log(TAG, "ParseException - dateFormat");
        }

        return outputDate;

    }


    public static void shareOnSocial(Context mContext, File imageFile, String text, String imageNvideoUrl, int feedId) {


        if (imageFile == null) {
            Intent intent = new Intent(mContext, ShareActivity.class);
            intent.putExtra("from", "post");
            intent.putExtra("type", "text");
            intent.putExtra("filePath", "");
            intent.putExtra("link", text + "\n" + API.DEEP_LINK_BASE_URL + "feedDetail/" + feedId);
            mContext.startActivity(intent);

        } else {
            Intent intent = new Intent(mContext, ShareActivity.class);
            intent.putExtra("from", "post");
            intent.putExtra("type", "file");
            intent.putExtra("filePath", imageFile.toString());
            intent.putExtra("link", text + "\n" + imageNvideoUrl + "\n\n" + API.DEEP_LINK_BASE_URL + "feedDetail/" + feedId);
            mContext.startActivity(intent);
        }
    }




}

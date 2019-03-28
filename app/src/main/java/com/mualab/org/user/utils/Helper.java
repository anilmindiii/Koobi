package com.mualab.org.user.utils;

import android.content.Context;
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
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Neha on 28/9/17.
 */

public class Helper {
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


}

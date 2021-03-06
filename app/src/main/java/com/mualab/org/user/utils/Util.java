package com.mualab.org.user.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mualab.org.user.chat.model.FirebaseUser;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Mindiii on 1/29/2018.
 */

public class Util {
    private Context mContext;
    public Util(Context context){
        this.mContext = context;
    }

    public String roundRatingWithSuffix(long count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f %c",
                count / Math.pow(1000, exp),
                "kMGTPE".charAt(exp-1));
    }

    public int getTimeInMin(int hours,int min){
        return hours*60 + min;
    }


    public static byte[] getByteArray(Bitmap bitmap) {
        if(bitmap != null){
            //Convert to byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        }else return  null;

    }

    public static Bitmap getBitmapFromByteArray(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public static String getTwoDigitDecimal(Double value){
        double d = 0.0;
        String decimalValue = "";
        try {
            d = value;
            decimalValue = String.format("%.2f", d);
        }catch (Exception e){

        }

        return decimalValue;
    }

    public static String getTwoDigitDecimal(String value){
        double d = 0.0;
        String decimalValue = "";
        try {
            d = Double.parseDouble(value);
            decimalValue = String.format("%.2f", d);
        }catch (Exception e){

        }

        return decimalValue;
    }


    public static String getDeviceID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    public static final String getVersionName(Context context) {

        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pInfo.versionName;

    }


    public static void setButtonBackgroundColor(Context context, Button button, int color) {

        if (Build.VERSION.SDK_INT >= 23) {
            button.setBackgroundColor(context.getResources().getColor(color, null));
        } else {
            button.setBackgroundColor(context.getResources().getColor(color));
        }
    }


    public static void setButtonBackgroundColor(Context context, TextView textView, int color) {
        if (Build.VERSION.SDK_INT >= 23) {
            textView.setBackgroundColor(context.getResources().getColor(color, null));
        } else {
            textView.setBackgroundColor(context.getResources().getColor(color));
        }
    }


    public static Drawable setDrawableSelector(Context context, int normal, int selected) {
        Drawable state_normal = ContextCompat.getDrawable(context, normal);
        Drawable state_pressed = ContextCompat.getDrawable(context, selected);
        Bitmap state_normal_bitmap = ((BitmapDrawable)state_normal).getBitmap();

        // Setting alpha directly just didn't work, so we draw a new bitmap!
        Bitmap disabledBitmap = Bitmap.createBitmap(
                state_normal.getIntrinsicWidth(),
                state_normal.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(disabledBitmap);
        Paint paint = new Paint();
        paint.setAlpha(126);
        canvas.drawBitmap(state_normal_bitmap, 0, 0, paint);
        BitmapDrawable state_normal_drawable = new BitmapDrawable(context.getResources(), disabledBitmap);
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_selected}, state_pressed);
        drawable.addState(new int[]{android.R.attr.state_enabled}, state_normal_drawable);
        return drawable;
    }


    public static StateListDrawable selectorRadioImage(Context context, Drawable normal, Drawable pressed) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_checked}, pressed);
        states.addState(new int[]{}, normal);
        //                imageView.setImageDrawable(states);
        return states;
    }

    public static StateListDrawable selectorRadioButton(Context context, int normal, int pressed) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_checked}, new ColorDrawable(pressed));
        states.addState(new int[]{}, new ColorDrawable(normal));
        return states;
    }

    public static ColorStateList selectorRadioText(Context context, int normal, int pressed) {
        ColorStateList colorStates = new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}}, new int[]{pressed, normal});
        return colorStates;
    }


    public static StateListDrawable selectorRadioDrawable(Drawable normal, Drawable pressed) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_checked}, pressed);
        states.addState(new int[]{}, normal);
        return states;
    }

    public static StateListDrawable selectorBackgroundColor(Context context, int normal, int pressed) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(pressed));
        states.addState(new int[]{}, new ColorDrawable(normal));
        return states;
    }

    public static StateListDrawable selectorBackgroundDrawable(Drawable normal, Drawable pressed) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, pressed);
        states.addState(new int[]{}, normal);
        return states;
    }

    public static ColorStateList selectorText(Context context, int normal, int pressed) {
        ColorStateList colorStates = new ColorStateList(new int[][]{new int[]{android.R.attr.state_pressed}, new int[]{}}, new int[]{pressed, normal});
        return colorStates;
    }

    public   String changeDateFormate(String sDate){
        SimpleDateFormat inputDf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputtDf = new SimpleDateFormat("dd/MM/yyyy");
        Date formatedDate = null;
        String date = "";
        try {
            formatedDate = inputDf.parse(sDate);
            date =  outputtDf.format(formatedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static void goToOnlineStatus(Context context,String status) {
        Session session = new Session(context);
        User user = session.getUser();

        if(session.getUser().authToken != null){

            DatabaseReference mDatabase  = FirebaseDatabase.getInstance().getReference();
            FirebaseUser firebaseUser = new FirebaseUser();

            if (status.equals("online"))
                firebaseUser.isOnline = 1;
            else
                firebaseUser.isOnline = 0;

            firebaseUser.lastActivity = ServerValue.TIMESTAMP;
            if (user.profileImage != null && user.profileImage.isEmpty())
                firebaseUser.profilePic = "http://koobi.co.uk:3000/uploads/default_user.png";
            else
                firebaseUser.profilePic = user.profileImage;

            firebaseUser.userName = user.userName;
            firebaseUser.uId = user.id;
            if(user.notificationStatus == 1){
                firebaseUser.firebaseToken = FirebaseInstanceId.getInstance().getToken();
            }else firebaseUser.firebaseToken = "";
            firebaseUser.authToken = user.authToken;
            firebaseUser.userType = user.userType;
            mDatabase.child("users").child(String.valueOf(user.id)).setValue(firebaseUser);

          /*  //  if(!user.id.equals("")){
            OnlineInfo onlineInfo = new OnlineInfo();
            onlineInfo.lastOnline = status;
            onlineInfo.email = session.getUser().email;

            database.child(Constant.online)
                    .child(String.valueOf(session.getUser().id)).setValue(onlineInfo);
            //}*/
        }

    }
}

package com.mualab.org.user.customdobcalender;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.authentication.Registration2Activity;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Objects;

/**
 * A fork of the Android Open Source Project DatePickerDialog class
 */
@SuppressLint("ValidFragment")
public class DatePickerDialog extends Dialog implements OnClickListener,
        OnDateChangedListener,OnDateListener {

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";
    private static final String TITLE_SHOWN = "title_enabled";

    private final DatePicker mDatePicker;
    private OnDateSetListener mCallBack;
    private final DateFormat mTitleDateFormat;

    private boolean mIsDayShown = true;
    private boolean mIsTitleShown = true;
    private OnDateChangedListener mOnDateChangedListener;
    private boolean isclicked;

    private String dobText = "";


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    DatePickerDialog(final Context context,
                     int theme,
                     int spinnerTheme,
                     OnDateSetListener callBack,
                     Calendar defaultDate,
                     Calendar minDate,
                     Calendar maxDate,
                     boolean isDayShown,
                     boolean isTitleShown,String activity) {
        super(context, theme);
        mCallBack = callBack;
        mTitleDateFormat = DateFormat.getDateInstance(DateFormat.LONG);
        mIsDayShown = isDayShown;
        mIsTitleShown = isTitleShown;
        updateTitle(defaultDate);
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View view = inflater.inflate(R.layout.date_picker_dialog_container, null);
        setContentView(view);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(Objects.requireNonNull(getWindow()).getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        getWindow().setAttributes(lp);

        setContentView(view);
        hasSoftKeys(getWindow().getWindowManager());
        mDatePicker = new DatePicker((ViewGroup) view, spinnerTheme,activity);
        ImageView image_close = mDatePicker .findViewById(R.id.cross_icon);
        final TextView textView = mDatePicker.findViewById(R.id.txt_done);
        image_close.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               dismiss();
               if (mCallBack != null) {
                   mDatePicker.clearFocus();
                  /* mCallBack.onDateSet(mDatePicker, mDatePicker.getYear(),
                           mDatePicker.getMonth(), mDatePicker.getDayOfMonth(),0,isclicked);*/
                   if(!dobText.equals(context.getResources().getString(R.string.date_of_birth))){
                       mCallBack.onDateSet(mDatePicker, mDatePicker.getYear(),
                               mDatePicker.getMonth(), mDatePicker.getDayOfMonth(),0,isclicked);
                   }
               }

           }
       });

        mDatePicker.setMaxDate(System.currentTimeMillis() - 1000);
        mDatePicker.setMinDate(minDate.getTimeInMillis());
/*
        mDatePicker.setMaxDate(maxDate.getTimeInMillis());
*/
        mDatePicker.init(defaultDate.get(Calendar.YEAR), defaultDate.get(Calendar.MONTH), defaultDate.get(Calendar.DAY_OF_MONTH), isDayShown, this);
       // mDatePicker.updateDate(defaultDate.get(Calendar.YEAR), defaultDate.get(Calendar.MONTH), defaultDate.get(Calendar.DAY_OF_MONTH));
      //  setCancelable(false);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                isclicked=true;
                if (mCallBack != null) {
                    mDatePicker.clearFocus();
                    mCallBack.onDateSet(mDatePicker, mDatePicker.getYear(),
                            mDatePicker.getMonth(), mDatePicker.getDayOfMonth(),1,isclicked);
                }

               // onDateChanged(mDatePicker,mDatePicker.getYear(),mDatePicker.getDayOfMonth(),mDatePicker.getDayOfMonth());
            }
        });
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar updatedDate = Calendar.getInstance();
        updatedDate.set(Calendar.YEAR, year);
        updatedDate.set(Calendar.MONTH, monthOfYear);
        updatedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateTitle(updatedDate);

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mCallBack != null) {
            mDatePicker.clearFocus();
            mCallBack.onDateSet(mDatePicker, mDatePicker.getYear(),
                    mDatePicker.getMonth(), mDatePicker.getDayOfMonth(),0,false);
        }
    }

    private void updateTitle(Calendar updatedDate) {
        if (mIsTitleShown) {
            final DateFormat dateFormat = mTitleDateFormat;
            setTitle(dateFormat.format(updatedDate.getTime()));
        } else {
            setTitle(" ");
        }
    }

    @NonNull
    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, mDatePicker.getYear());
        state.putInt(MONTH, mDatePicker.getMonth());
        state.putInt(DAY, mDatePicker.getDayOfMonth());
        state.putBoolean(TITLE_SHOWN, mIsTitleShown);
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int year = savedInstanceState.getInt(YEAR);
        int month = savedInstanceState.getInt(MONTH);
        int day = savedInstanceState.getInt(DAY);
        mIsTitleShown = savedInstanceState.getBoolean(TITLE_SHOWN);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        updateTitle(c);
        mDatePicker.init(day, month, year, mIsDayShown, this);
    }

    private  boolean hasSoftKeys(WindowManager windowManager){
        boolean hasSoftwareKeys = true;


        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR1){
            Display d = windowManager.getDefaultDisplay();

            DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            d.getRealMetrics(realDisplayMetrics);

            int realHeight = realDisplayMetrics.heightPixels;
            int realWidth = realDisplayMetrics.widthPixels;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            d.getMetrics(displayMetrics);

            int displayHeight = displayMetrics.heightPixels;
            int displayWidth = displayMetrics.widthPixels;

            hasSoftwareKeys =  (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
        }else{
            boolean hasMenuKey = ViewConfiguration.get(getContext()).hasPermanentMenuKey();
            boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            hasSoftwareKeys = !hasMenuKey && !hasBackKey;
        }
        return hasSoftwareKeys;
    }


    @Override
    public void onDateChanged(String date) {
        dobText = date;
    }

    /**
     * The callback used to indicate the user is done filling in the date.
     */
    public interface OnDateSetListener {
        /**
         * @param view        The view associated with this listener.
         * @param year        The year that was set
         * @param monthOfYear The month that was set (0-11) for compatibility
         *                    with {@link Calendar}.
         * @param dayOfMonth  The day of the month that was set.
         */
        void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth,int type,boolean isClicked);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
package com.mualab.org.user.Views.calender.widget.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import com.mualab.org.user.R;
import com.mualab.org.user.Views.calender.CalendarHelper;
import com.mualab.org.user.Views.calender.data.CalendarAdapter;
import com.mualab.org.user.Views.calender.data.Day;
import com.mualab.org.user.Views.calender.data.Event;
import com.mualab.org.user.activity.artist_profile.model.Services;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.WorkingHourActivity;
import com.mualab.org.user.activity.booking.adapter.WorkingHoursAdapter;
import com.mualab.org.user.activity.booking.customSeekBar.Utils;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.Util;
import com.mualab.org.user.utils.constants.Constant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class MyFlexibleCalendar extends MyUICalendar {

    public boolean isFirstimeLoad = true;
    public boolean isTodayClick = false;
    private CalendarAdapter mAdapter;
    private CalendarListener mListener;
    private int mInitHeight = 0;
    ArrayList<Services.ArtistDetailBean.BusineshoursBean> daysList;
    private Handler mHandler = new Handler();
    private boolean mIsWaitingForUpdate = false;
    private ArrayList<Day> tempDaysArray;
    private int mCurrentWeekIndex, childId;
    private boolean isFrist;


    public void setDayToGray(ArrayList<Services.ArtistDetailBean.BusineshoursBean> days) {
        this.daysList = days;
        tempDaysArray = new ArrayList<>();

        Day day = new Day();
        day.dayId = 0;
        day.dayName = "Mon";
        day.isAvailable = false;
        tempDaysArray.add(day);

        day = new Day();
        day.dayId = 1;
        day.dayName = "Tue";
        day.isAvailable = false;
        tempDaysArray.add(day);

        day = new Day();
        day.dayId = 2;
        day.dayName = "Wed";
        day.isAvailable = false;
        tempDaysArray.add(day);

        day = new Day();
        day.dayId = 3;
        day.dayName = "Thu";
        day.isAvailable = false;
        tempDaysArray.add(day);

        day = new Day();
        day.dayId = 4;
        day.dayName = "Fri";
        day.isAvailable = false;
        tempDaysArray.add(day);

        day = new Day();
        day.dayId = 5;
        day.dayName = "Sat";
        day.isAvailable = false;
        tempDaysArray.add(day);

        day = new Day();
        day.dayId = 6;
        day.dayName = "Sun";
        day.isAvailable = false;
        tempDaysArray.add(day);

        for (int d = 0; d < daysList.size(); d++) {
            switch (daysList.get(d).day) {
                case 0:
                    tempDaysArray.get(0).isAvailable = true;
                    break;

                case 1:
                    tempDaysArray.get(1).isAvailable = true;
                    break;

                case 2:
                    tempDaysArray.get(2).isAvailable = true;
                    break;

                case 3:
                    tempDaysArray.get(3).isAvailable = true;
                    break;

                case 4:
                    tempDaysArray.get(4).isAvailable = true;
                    break;

                case 5:
                    tempDaysArray.get(5).isAvailable = true;
                    break;

                case 6:
                    tempDaysArray.get(6).isAvailable = true;
                    break;
            }

        }

        redraw();
    }

    public MyFlexibleCalendar(Context context) {
        super(context);
    }

    public MyFlexibleCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyFlexibleCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(Context context) {
        super.init(context);

        if (isInEditMode()) {
            Calendar cal = Calendar.getInstance();
            CalendarAdapter adapter = new CalendarAdapter(context, cal);
            setAdapter(adapter);
        }

        setStateWithUpdateUI(getState());

        // bind events
        mBtnPrevMonth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                prevMonth();

            }
        });

        mBtnNextMonth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                nextMonth();
            }
        });

        mBtnPrevWeek.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                prevWeek();
            }
        });

        mBtnNextWeek.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                nextWeek();
            }
        });

        // bind events collaps/expand
        ivDropDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getState() == STATE_COLLAPSED) {
                    expand(500);
                } else {
                    collapse(500);
                }
            }
        });

        mTxtTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getState() == STATE_COLLAPSED) {
                    expand(500);
                } else {
                    collapse(500);
                }
            }
        });

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mInitHeight = mTableBody.getMeasuredHeight();

        if (mIsWaitingForUpdate) {
            redraw();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    collapseTo(mCurrentWeekIndex);
                }
            });
            mIsWaitingForUpdate = false;
            if (mListener != null) {
                mListener.onDataUpdate();
            }
        }
    }

    @Override
    protected void redraw() {

        // redraw all views of week
        TableRow rowWeek = (TableRow) mTableHead.getChildAt(0);
        if (rowWeek != null) {
            for (int i = 0; i < rowWeek.getChildCount(); i++) {
                ((TextView) rowWeek.getChildAt(i)).setTextColor(getTextPrimaryColor());
            }
        }
        // redraw all views of day
        if (mAdapter != null) {
            for (int i = 0; i < mAdapter.getCount(); i++) {
                Day day = mAdapter.getItem(i);
                View view = mAdapter.getView(i);
                TextView txtDay = view.findViewById(R.id.txt_day);
                txtDay.setBackgroundColor(Color.TRANSPARENT);
                txtDay.setTextColor(getTextColor());

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
                dateFormat.setTimeZone(mAdapter.getCalendar().getTimeZone());

                String input = day.getDay() + "/" + (day.getMonth() + 1) + "/" + day.getYear();
                String dayaName = Helper.formateDateFromstring("dd/MM/yyyy", "EEE", input);
                Log.d("name", dayaName);
                // set today's item

                if (isToady(day)) {
                    mTxtTitle.setText(day.getDay() +" "+ dateFormat.format(mAdapter.getCalendar().getTime()));

                    txtDay.setBackground(getTodayItemBackgroundDrawable());
                    txtDay.setTextColor(getTodayItemTextColor());

                    if (isFirstimeLoad) {
                        txtDay.setTextColor(getResources().getColor(R.color.white));
                        txtDay.setBackground(getSelectedItemBackgroundOrangeUnService());
                    } else{
                        txtDay.setBackgroundColor(getResources().getColor(R.color.white));
                        txtDay.setTextColor(getResources().getColor(R.color.google_green));
                    }

                } else {
                    if (tempDaysArray != null)
                        for (int d = 0; d < tempDaysArray.size(); d++) {
                            if (!tempDaysArray.get(d).isAvailable) {
                                if (tempDaysArray.get(d).dayName.equals(dayaName)) {
                                    if (isToady(day)){
                                        txtDay.setBackground(getSelectedItemBackgroundOrangeUnService());
                                        txtDay.setTextColor(getSelectedItemTextColor());
                                    }else{
                                        txtDay.setBackground(getSelectedItemBackgroundUnservice());
                                        txtDay.setTextColor(getSelectedItemTextColor());
                                    }
                                }
                            }
                        }
                }
                // set the selected item
                if (isSelectedDay(day)) {
                    if (!isFirstimeLoad) {
                        Calendar todayCal = Calendar.getInstance();
                        int cYear = todayCal.get(Calendar.YEAR);
                        int cMonth = todayCal.get(Calendar.MONTH) + 1;
                        int cDay = todayCal.get(Calendar.DAY_OF_MONTH);

                        int year = day.getYear();
                        int month = day.getMonth() + 1;
                        int dayOfMonth = day.getDay();

                        if (year >= cYear && month >= cMonth) {
                            if (year == cYear && month == cMonth && dayOfMonth < cDay) {
                                //  MyToast.getInstance(mContext).showDasuAlert("You can't select previous date");
                            } else {
                                mTxtTitle.setText(dayOfMonth + " " +dateFormat.format(mAdapter.getCalendar().getTime()));
                                if (isToady(day)){
                                    txtDay.setBackground(getSelectedItemBackgroundOrangeUnService());
                                    txtDay.setTextColor(getSelectedItemTextColor());
                                }else{
                                    txtDay.setBackgroundDrawable(getSelectedItemBackgroundDrawable());
                                    txtDay.setTextColor(getSelectedItemTextColor());
                                }
                            }
                        } else {
                            //MyToast.getInstance(mContext).showDasuAlert("You can't select previous date");
                        }

                        //mTxtTitle.setText(dateFormat.format(mAdapter.getCalendar().getTime()));
                        //txtDay.setBackground(getSelectedItemBackgroundDrawable());
                        //txtDay.setTextColor(getSelectedItemTextColor());
                    }
                }
            }
        }


    }

    @Override
    protected void reload() {
        if (mAdapter != null) {
            mAdapter.refresh();

            // reset UI
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
            dateFormat.setTimeZone(mAdapter.getCalendar().getTimeZone());

            mTxtTitle.setText(dateFormat.format(mAdapter.getCalendar().getTime()));
            mTableHead.removeAllViews();
            mTableBody.removeAllViews();

            TableRow rowCurrent;

            // set day of week
            int[] dayOfWeekIds = {
                    R.string.sun,
                    R.string.mon,
                    R.string.tues,
                    R.string.wednes,
                    R.string.thurs,
                    R.string.fri,
                    R.string.satur
            };
            rowCurrent = new TableRow(mContext);
            rowCurrent.setLayoutParams(new TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));



            for (int i = 0; i < 7; i++) {
                View view = mInflater.inflate(R.layout.layout_day_of_week, null);
                TextView txtDayOfWeek = view.findViewById(R.id.txt_day_of_week);
                txtDayOfWeek.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                txtDayOfWeek.setText(dayOfWeekIds[(i + getFirstDayOfWeek()) % 7]);


                //ViewGroup.LayoutParams.WRAP_CONTENT
                view.setLayoutParams(new TableRow.LayoutParams(
                        0,
                        (int) getResources().getDimension(R.dimen.dp50),
                        1));
                rowCurrent.addView(view);
            }
            mTableHead.addView(rowCurrent);

            // set day view
            for (int i = 0; i < mAdapter.getCount(); i++) {
                final int position = i;

                if (position % 7 == 0) {
                    rowCurrent = new TableRow(mContext);
                    rowCurrent.setLayoutParams(new TableLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    mTableBody.addView(rowCurrent);
                }
                final View view = mAdapter.getView(position);
                view.setLayoutParams(new TableRow.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1));
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isFirstimeLoad = false;
                        onItemClicked(v, mAdapter.getItem(position));
                    }
                });
                rowCurrent.addView(view);
            }

            redraw();
            mIsWaitingForUpdate = true;
        }
    }



   /* private int getSuitableRowIndex() {
        if (getSelectedItemPosition() != -1) {
            View view = mAdapter.getView(getSelectedItemPosition());
            TableRow row = (TableRow) view.getParent();

            return mTableBody.indexOfChild(row);
        } else if (getTodayItemPosition() != -1) {
            View view = mAdapter.getView(getTodayItemPosition());
            TableRow row = (TableRow) view.getParent();

            return mTableBody.indexOfChild(row);
        } else {
            return 0;
        }
    }*/

    public void setToday(boolean isTodayClick) {
        this.isTodayClick = isTodayClick;
    }

    private int getSuitableRowIndex() {
        if (isTodayClick) {
            isTodayClick = false;
            if (getTodayItemPosition() != -1) {
                View view = mAdapter.getView(getTodayItemPosition());
                TableRow row = (TableRow) view.getParent();

                return mTableBody.indexOfChild(row);
            } else {
                return 0;
            }
        } else {
            if (getSelectedItemPosition() != -1) {
                View view = mAdapter.getView(getSelectedItemPosition());
                TableRow row = (TableRow) view.getParent();

                return mTableBody.indexOfChild(row);
            } else if (getTodayItemPosition() != -1) {
                View view = mAdapter.getView(getTodayItemPosition());
                TableRow row = (TableRow) view.getParent();

                return mTableBody.indexOfChild(row);
            } else {
                return 0;
            }
        }
    }

    public int isServiceSelected(int childId) {
        this.childId = childId;
        return childId;
    }

    public void onItemClicked(View view, Day day) {
        if (isServiceSelected(childId) == 0) {
            MyToast.getInstance(mContext).showDasuAlert("Please select service");

            return;
        }

        select(day, true);
        isFirstimeLoad = false;
        //  Day day1 = getSelectedDay();

        Calendar cal = mAdapter.getCalendar();

        int newYear = day.getYear();
        int newMonth = day.getMonth();
        int oldYear = cal.get(Calendar.YEAR);
        int oldMonth = cal.get(Calendar.MONTH);

        if (newMonth != oldMonth) {
            cal.set(day.getYear(), day.getMonth(), 1);

            if (newYear > oldYear || newMonth > oldMonth) {
                mCurrentWeekIndex = 0;
            }
            if (newYear < oldYear || newMonth < oldMonth) {
                mCurrentWeekIndex = -1;
            }
            if (mListener != null) {
                mListener.onMonthChange();
            }
            reload();
        }

        if (mListener != null) {
            mListener.onItemClick(view);
        }
        collapse(500);
    }

    // public methods
    public void setAdapter(CalendarAdapter adapter) {
        mAdapter = adapter;
        adapter.setFirstDayOfWeek(getFirstDayOfWeek());
        reload();
        // init week
        mCurrentWeekIndex = getSuitableRowIndex();
    }

    public void addEventTag(int numYear, int numMonth, int numDay) {
        mAdapter.addEvent(new Event(numYear, numMonth, numDay));
        reload();
    }

    public void prevMonth() {
        mScrollViewBody.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_in_from_left));
        Calendar cal = mAdapter.getCalendar();
        if (cal.get(Calendar.MONTH) == cal.getActualMinimum(Calendar.MONTH)) {
            cal.set((cal.get(Calendar.YEAR) - 1), cal.getActualMaximum(Calendar.MONTH), 1);
        } else {
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
        }
        reload();
        if (mListener != null) {
            mListener.onMonthChange();
        }
    }

    public void nextMonth() {
        mScrollViewBody.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_in_from_right));
        Calendar cal = mAdapter.getCalendar();
        if (cal.get(Calendar.MONTH) == cal.getActualMaximum(Calendar.MONTH)) {
            cal.set((cal.get(Calendar.YEAR) + 1), cal.getActualMinimum(Calendar.MONTH), 1);
        } else {
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
        }
        reload();
        if (mListener != null) {
            mListener.onMonthChange();
        }
    }



    public void prevWeek() {
        mScrollViewBody.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_in_from_left));
        if (mCurrentWeekIndex - 1 < 0) {
            mCurrentWeekIndex = -1;
            prevMonth();
        } else {
            mCurrentWeekIndex--;
            collapseTo(mCurrentWeekIndex);
        }
    }

    public void nextWeek() {
        mScrollViewBody.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_in_from_right));
        if (mCurrentWeekIndex + 1 >= mTableBody.getChildCount()) {
            mCurrentWeekIndex = 0;
            nextMonth();
        } else {
            mCurrentWeekIndex++;
            collapseTo(mCurrentWeekIndex);
        }
    }

    public int getYear() {
        return mAdapter.getCalendar().get(Calendar.YEAR);
    }

    public int getMonth() {
        return mAdapter.getCalendar().get(Calendar.MONTH);
    }

    public Day getSelectedDay() {
        return new Day(
                getSelectedItem().getYear(),
                getSelectedItem().getMonth(),
                getSelectedItem().getDay());
    }

    public boolean isSelectedDay(Day day) {
        return day != null
                && getSelectedItem() != null
                && day.getYear() == getSelectedItem().getYear()
                && day.getMonth() == getSelectedItem().getMonth()
                && day.getDay() == getSelectedItem().getDay();
    }

    public boolean isToady(Day day) {
        Calendar todayCal = Calendar.getInstance();
        return day != null
                && day.getYear() == todayCal.get(Calendar.YEAR)
                && day.getMonth() == todayCal.get(Calendar.MONTH)
                && day.getDay() == todayCal.get(Calendar.DAY_OF_MONTH);
    }

    public int getSelectedItemPosition() {
        int position = -1;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            Day day = mAdapter.getItem(i);

            if (isSelectedDay(day)) {
                position = i;
                break;
            }
        }
        return position;
    }

    public int getTodayItemPosition() {
        int position = -1;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            Day day = mAdapter.getItem(i);

            if (isToady(day)) {
                position = i;
                break;
            }
        }
        return position;
    }

    public void collapse(int duration) {
        if (getState() == STATE_EXPANDED) {
            setState(STATE_PROCESSING);

            mLayoutBtnGroupMonth.setVisibility(GONE);
            mLayoutBtnGroupWeek.setVisibility(VISIBLE);
            mBtnPrevWeek.setClickable(false);
            mBtnNextWeek.setClickable(false);

            int index = getSuitableRowIndex();
            mCurrentWeekIndex = index;

            final int currentHeight = mInitHeight;
            final int targetHeight = mTableBody.getChildAt(index).getMeasuredHeight();
            int tempHeight = 0;
            for (int i = 0; i < index; i++) {
                tempHeight += mTableBody.getChildAt(i).getMeasuredHeight();
            }
            final int topHeight = tempHeight;

            Animation anim = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {

                    mScrollViewBody.getLayoutParams().height = (interpolatedTime == 1)
                            ? targetHeight
                            : currentHeight - (int) ((currentHeight - targetHeight) * interpolatedTime);
                    mScrollViewBody.requestLayout();

                    if (mScrollViewBody.getMeasuredHeight() < topHeight + targetHeight) {
                        int position = topHeight + targetHeight - mScrollViewBody.getMeasuredHeight();
                        mScrollViewBody.smoothScrollTo(0, position);
                    }

                    if (interpolatedTime == 1) {
                        setState(STATE_COLLAPSED);
                        mBtnPrevWeek.setClickable(true);
                        mBtnNextWeek.setClickable(true);
                    }
                }
            };
            anim.setDuration(duration);
            startAnimation(anim);
            ivDropDown.setRotation(360);
        }
    }

    private void collapseTo(int index) {
        if (getState() == STATE_COLLAPSED || getState() == 1) {
            if (index == -1) {
                index = mTableBody.getChildCount() - 1;
            }
            mCurrentWeekIndex = index;
            try {
                final int targetHeight = mTableBody.getChildAt(index).getMeasuredHeight();
                int tempHeight = 0;
                for (int i = 0; i < index; i++) {
                    tempHeight += mTableBody.getChildAt(i).getMeasuredHeight();
                }
                final int topHeight = tempHeight;

                mScrollViewBody.getLayoutParams().height = targetHeight;
                mScrollViewBody.requestLayout();

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mScrollViewBody.smoothScrollTo(0, topHeight);
                    }
                });
            }catch (Exception e){

            }

            if (mListener != null) {
                mListener.onWeekChange(mCurrentWeekIndex);
            }
        }
    }

    public void expand(int duration) {
        if (getState() == STATE_COLLAPSED) {
            setState(STATE_PROCESSING);

            mLayoutBtnGroupMonth.setVisibility(VISIBLE);
            mLayoutBtnGroupWeek.setVisibility(GONE);
            mBtnPrevMonth.setClickable(false);
            mBtnNextMonth.setClickable(false);

            final int currentHeight = mScrollViewBody.getMeasuredHeight();
            final int targetHeight = mInitHeight;

            Animation anim = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {

                    mScrollViewBody.getLayoutParams().height = (interpolatedTime == 1)
                            ? LayoutParams.WRAP_CONTENT
                            : currentHeight - (int) ((currentHeight - targetHeight) * interpolatedTime);
                    mScrollViewBody.requestLayout();

                    if (interpolatedTime == 1) {
                        setState(STATE_EXPANDED);

                        mBtnPrevMonth.setClickable(true);
                        mBtnNextMonth.setClickable(true);
                    }
                }
            };
            anim.setDuration(duration);
            startAnimation(anim);
            ivDropDown.setRotation(180);
        }
    }

    public void select(Day day, boolean isRunApi) {
        setSelectedItem(new Day(day.getYear(), day.getMonth(), day.getDay()));
        redraw();

        if (isRunApi) {
            if (mListener != null) {
                mListener.onDaySelect();
            }
        }

    }

    public void setStateWithUpdateUI(int state) {
        setState(state);
        if (getState() != state) {
            mIsWaitingForUpdate = true;
            requestLayout();
        }
    }

    // callback
    public void setCalendarListener(CalendarListener listener) {
        mListener = listener;
    }

    public interface CalendarListener {

        // triggered when a day is selected programmatically or clicked by user.
        void onDaySelect();

        // triggered only when the views of day on calendar are clicked by user.
        void onItemClick(View v);

        // triggered when the data of calendar are updated by changing month or adding events.
        void onDataUpdate();

        // triggered when the month are changed.
        void onMonthChange();

        // triggered when the week position are changed.
        void onWeekChange(int position);
    }


    public static String getDayById(int dayId) {
        String day = "";
        switch (dayId) {
            case 6:
                day = "Sunday";
                break;

            case 0:
                day = "Monday";
                break;

            case 1:
                day = "Tuesday";
                break;

            case 2:
                day = "Wednesday";
                break;

            case 3:
                day = "Thursday";
                break;

            case 4:
                day = "Friday";
                break;

            case 5:
                day = "Saturday";
                break;

        }
        return day;
    }

}

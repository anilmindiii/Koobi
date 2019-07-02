package com.mualab.org.user.activity.booking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.Views.calender.CalendarHelper;
import com.mualab.org.user.Views.calender.data.CalendarAdapter;
import com.mualab.org.user.Views.calender.data.Day;
import com.mualab.org.user.Views.calender.widget.widget.MyFlexibleCalendar;
import com.mualab.org.user.activity.artist_profile.adapter.CustomStringAdapter;
import com.mualab.org.user.activity.artist_profile.adapter.IncallOutCallAdapter;
import com.mualab.org.user.activity.artist_profile.model.Services;
import com.mualab.org.user.activity.booking.adapter.StaffAdapter;
import com.mualab.org.user.activity.booking.adapter.TimeSlotAdapter;
import com.mualab.org.user.activity.booking.listner.DeleteServiceListener;
import com.mualab.org.user.activity.booking.listner.TimeSlotClickListener;
import com.mualab.org.user.activity.booking.model.ServiceInfoBooking;
import com.mualab.org.user.activity.booking.model.TimeSlotInfo;
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
import com.mualab.org.user.utils.constants.Constant;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BookingActivity extends AppCompatActivity implements View.OnClickListener
        , TimeSlotClickListener, DeleteServiceListener, StaffAdapter.click {
    private String artistId;
    private int childId;
    private LinearLayout ly_biz_type, ly_category;
    private CardView cv_ly_biz_type, cv_ly_category;
    private CustomStringAdapter adapterBizType, adapterCategory;
    private RecyclerView rcv_biz_type, rcv_category_type, rcv_incall;
    private Services services;
    private TextView tv_bizType, tv_category, tvArtistName, tvbizDate, btnCOnfirmBooking, tv_review_count;
    private ImageView iv_down_arrow_bizType, iv_down_arrow_category, ivProfile;
    private ArrayList<Services.ArtistServicesBean.SubServiesBean.ArtistservicesBean> inCallList, outCallList;
    private ScrollView main_scroll_view;
    private TextView tv_msg, tvNoSlot;
    private LinearLayout ly_incall, lyArtistTopView;
    private IncallOutCallAdapter inCallAdapter;
    private IncallOutCallAdapter.childItemClick click;
    private boolean isOpenCategory;
    private RelativeLayout ly_outcall, ly_main;
    private CheckBox chbOutcall;
    private ImageView checkbox_img;
    private boolean isOutCallSelected;
    private int childPos = 0;
    private String checkPositon = "", callType = "";
    private String mainServiceName = "", subServiceName = "", artistServiceId = "";
    private RecyclerView rcv_staff;
    private StaffAdapter staffAdapter;
    private ArrayList<ServiceInfoBooking.StaffInfoBean> staffInfoBeanList;
    private RecyclerView rcv_timeSlot;
    private TimeSlotAdapter timeSlotAdapter;
    private ArrayList<TimeSlotInfo> timeSlotList;
    private String businessType = "", preprationTime = "";
    private String selectedDate, sMonth = "", sDay, currentTime, lat = "", lng = "", startTime = "", bookingId = "", tempMainService = "";
    private MyFlexibleCalendar viewCalendar;
    private boolean isTodayClicked = false;
    private SimpleDateFormat input, dateFormat;
    private int dayId, staff = 0, serviceId, subServiceId;
    private SimpleDateFormat dateSdf, timeSdf;
    private RatingBar rating;
    private LinearLayout ly_staff_main, ly_time_slot_main;
    private boolean outcallStaff, incallStaff, isBankAdded, isAlreadybooked;
    private int totalTime, endTime,payOption = 0,bookingSetting = 0;
    private Double price;
    private String radius = "", artistLat, artistLng;
    private ArrayList<Services.ArtistDetailBean.BusineshoursBean> busineshoursList;
    private boolean isEditService;
    private CalendarAdapter adapter;
    private boolean isFromSearchBoard, isFromServiceTag, isToastShow;
    private String bookingDate;
    private BottomSheetDialog mBizTypeDialog;
    private BottomSheetDialog mCatTypeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        Intent i = getIntent();

        childId = i.getIntExtra("_id", 0);
        artistServiceId = String.valueOf(childId);
        artistId = i.getStringExtra("artistId");
        callType = i.getStringExtra("callType");

        mainServiceName = i.getStringExtra("mainServiceName");
        subServiceName = i.getStringExtra("subServiceName");

        serviceId = getIntent().getIntExtra("serviceId", 0);
        subServiceId = getIntent().getIntExtra("subServiceId", 0);

        outcallStaff = i.getBooleanExtra("outcallStaff", false);
        incallStaff = i.getBooleanExtra("incallStaff", false);

        //for comes from searchboard...
        isEditService = i.getBooleanExtra("isEditService", false);
        isFromSearchBoard = i.getBooleanExtra("isFromSearchBoard", false);
        bookingDate = i.getStringExtra("bookingDate");

        isFromServiceTag = i.getBooleanExtra("isFromServiceTag", false);
        staff = i.getIntExtra("staffId", 0);

        if (isFromServiceTag)
            if (Integer.parseInt(artistId) == staff) {
                staff = 0;
            }
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.booking));

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnCOnfirmBooking = findViewById(R.id.btnCOnfirmBooking);
        tvNoSlot = findViewById(R.id.tvNoSlot);
        ly_staff_main = findViewById(R.id.ly_staff_main);
        ly_time_slot_main = findViewById(R.id.ly_time_slot_main);
        ly_time_slot_main.setVisibility(View.GONE);
        tvbizDate = findViewById(R.id.tvbizDate);
        rating = findViewById(R.id.rating);
        rcv_timeSlot = findViewById(R.id.rcv_timeSlot);
        rcv_staff = findViewById(R.id.rcv_staff);
        chbOutcall = findViewById(R.id.chbOutcall);
        ly_outcall = findViewById(R.id.ly_outcall);
        ivProfile = findViewById(R.id.ivProfile);
        tvArtistName = findViewById(R.id.tvArtistName);
        ly_biz_type = findViewById(R.id.ly_biz_type);
        ly_category = findViewById(R.id.ly_category);
        //rcv_biz_type = findViewById(R.id.rcv_biz_type);
        //rcv_category_type = findViewById(R.id.rcv_category_type);
        rcv_incall = findViewById(R.id.rcv_incall);
        tv_bizType = findViewById(R.id.tv_bizType);
        tv_category = findViewById(R.id.tv_category);
        iv_down_arrow_bizType = findViewById(R.id.iv_down_arrow_bizType);
        iv_down_arrow_category = findViewById(R.id.iv_down_arrow_category);
        main_scroll_view = findViewById(R.id.main_scroll_view);
        ly_incall = findViewById(R.id.ly_incall);
        tv_msg = findViewById(R.id.tv_msg);
        lyArtistTopView = findViewById(R.id.lyArtistTopView);
        ly_main = findViewById(R.id.ly_main);
        AppCompatButton btnToday = findViewById(R.id.btnToday);

        setBizTypeDialog(false);
        setCatTypeDialog(false);

        cv_ly_category = findViewById(R.id.cv_ly_category);
        cv_ly_biz_type = findViewById(R.id.cv_ly_biz_type);
        tv_review_count = findViewById(R.id.tv_review_count);
        checkbox_img = findViewById(R.id.checkbox_img);

        busineshoursList = new ArrayList<>();


        click = new IncallOutCallAdapter.childItemClick() {
            @Override
            public void childClick(Services.ArtistServicesBean.SubServiesBean.ArtistservicesBean artistservicesBean, String callType, int adapterPosition) {
                childId = artistservicesBean._id;
                viewCalendar.isServiceSelected(childId);
                artistServiceId = String.valueOf(artistservicesBean._id);

                // clear staff and slot
                staff = 0;
                startTime = "";

                String completeTime = Helper.formateDateFromstring("HH:mm", "mm", artistservicesBean.completionTime);
                String preprationminutes = Helper.formateDateFromstring("HH:mm", "mm", preprationTime);

                if (completeTime.equals("") && !preprationminutes.equals("")) {
                    totalTime = Integer.parseInt(preprationminutes);
                } else if (!completeTime.equals("") && preprationminutes.equals("")) {
                    totalTime = Integer.parseInt(completeTime);
                } else {
                    totalTime = Integer.parseInt(completeTime) + Integer.parseInt(preprationminutes);
                }


                apiForserviceStaff(String.valueOf(artistservicesBean._id));

                if (isOutCallSelected) {
                    if (artistservicesBean.outcallStaff) {
                        ly_staff_main.setVisibility(View.VISIBLE);
                    } else ly_staff_main.setVisibility(View.GONE);
                } else {
                    if (artistservicesBean.incallStaff) {
                        ly_staff_main.setVisibility(View.VISIBLE);
                    } else ly_staff_main.setVisibility(View.GONE);
                }
            }
        };


        ly_biz_type.setOnClickListener(this);
        ly_category.setOnClickListener(this);
        main_scroll_view.setOnClickListener(this);
        // ly_outcall.setOnClickListener(this); // here is comments for new changes
        btnCOnfirmBooking.setOnClickListener(this);
        lyArtistTopView.setOnClickListener(this);
        ly_main.setOnClickListener(this);
        btnToday.setOnClickListener(this);


        staffInfoBeanList = new ArrayList<>();
        staffAdapter = new StaffAdapter(this, staffInfoBeanList, callType, this);
        rcv_staff.setAdapter(staffAdapter);

        timeSlotList = new ArrayList<>();
        timeSlotAdapter = new TimeSlotAdapter(this, timeSlotList, new TimeSlotAdapter.getSelectTime() {
            @Override
            public void getSelectedTimeSlot(TimeSlotInfo slotInfo) {
                if (slotInfo != null) {
                    startTime = slotInfo.timeSlots;
                } else startTime = "";
                viewCalendar.collapse(500);
            }
        });


        rcv_timeSlot.setLayoutManager(new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false));
        rcv_timeSlot.setAdapter(timeSlotAdapter);

        inCallList = new ArrayList<>();
        outCallList = new ArrayList<>();

        if (callType.equals("Out Call")) {
            isOutCallSelected = true;
            chbOutcall.setChecked(true);
            ly_outcall.setVisibility(View.VISIBLE);
            checkbox_img.setImageResource(R.drawable.active_check_box_ico);
        }

        viewCalendar = findViewById(R.id.calendar);
        viewCalendar.setFirstDayOfWeek(CalendarHelper.getTimestamp(Constant.TIMESTAMP_FORMAT_DAY));

        // init calendar
        Calendar cal = Calendar.getInstance();
        adapter = new CalendarAdapter(this, cal);
        viewCalendar.setAdapter(adapter);
        dateFormat = new SimpleDateFormat("EEE, d MMMM yyyy");
        dateFormat.setTimeZone(cal.getTimeZone());
        input = new SimpleDateFormat("yyyy-MM-dd");


        dateSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        timeSdf = new SimpleDateFormat("hh:mm a", Locale.US);
        // init calendar

        setCalenderClickListner(viewCalendar);

        //selectedDate = CalanderUtils.formatDate(CalanderUtils.getCurrentDate(), dateFormat.toString(),dateFormat.toString());
        dayId = cal.get(GregorianCalendar.DAY_OF_WEEK) - 2;

        selectedDate = getCurrentDate();
        currentTime = getCurrentTime();

        viewCalendar.isServiceSelected(childId);


        if (isFromSearchBoard) {
            if (bookingDate != null)
                if (!bookingDate.equals("")) {
                    selectedDate = Helper.formateDateFromstring("dd/MM/yyyy", "yyyy-MM-dd", bookingDate);//2019-02-01
                    setDate(bookingDate);
                    viewCalendar.expand(500);
                }
        }
        viewCalendar.collapse(500);


        apiForGetAllServices();
    }

    @Override
    public void OnClickAdapter(ServiceInfoBooking.StaffInfoBean bean) {
        staff = bean.staffId;
        startTime = "";

        String completeTime = Helper.formateDateFromstring("HH:mm", "mm", bean.completionTime);
        String preprationminutes = Helper.formateDateFromstring("HH:mm", "mm", preprationTime);

        endTime = (getMinutes(bean.completionTime) + getMinutes(preprationTime));

        endTime = Integer.parseInt(completeTime) + Integer.parseInt(preprationminutes);
        if (isOutCallSelected) {
            price = bean.outCallPrice;
        } else price = bean.inCallPrice;


        apiForstaffSlot(String.valueOf(bean.staffId));
    }


    private String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("hh:mm a");
        System.out.println("currentTime" + date.format(currentLocalTime));
        return date.format(currentLocalTime);
    }

    private String getCurrentDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        if (month < 10) {
            sMonth = "0" + month;
        } else {
            sMonth = String.valueOf(month);
        }

        if (day < 10) {
            sDay = "0" + day;
        } else {
            sDay = String.valueOf(day);
        }
        return year + "-" + sMonth + "-" + sDay;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ly_biz_type:
                if (services != null)
                    if (services.artistServices.size() == 1) {
                        return;
                    }
                /*if (cv_ly_biz_type.getVisibility() == View.VISIBLE) {
                    cv_ly_biz_type.setVisibility(View.GONE);
                } else {
                    cv_ly_biz_type.setVisibility(View.VISIBLE);
                }

                cv_ly_category.setVisibility(View.GONE);*/
                if (!tv_bizType.getText().toString().equals("No business type available"))
                    setBizTypeDialog(true);

                break;

            case R.id.ly_category:
                if (isOpenCategory) {
                    /*if (cv_ly_category.getVisibility() == View.VISIBLE) {
                        cv_ly_category.setVisibility(View.GONE);
                    } else {
                        cv_ly_category.setVisibility(View.VISIBLE);
                    }

                    cv_ly_biz_type.setVisibility(View.GONE);*/

                    if (!tv_category.getText().toString().equals("No category available"))
                        setCatTypeDialog(true);


                }
                break;

            case R.id.ly_outcall:
                if (chbOutcall.isChecked()) {
                    callType = "In Call";
                    chbOutcall.setChecked(false);
                    isOutCallSelected = false;

                    if (!mainServiceName.equals("")) {
                        for (int i = 0; i < services.artistServices.size(); i++) {
                            if (services.artistServices.get(i).serviceId == (serviceId)) {
                                adapterBizType.clickItem(i);
                            }

                        }
                    } else adapterBizType.clickItem();

                } else {
                    callType = "Out Call";
                    chbOutcall.setChecked(true);
                    isOutCallSelected = true;

                    if (!mainServiceName.equals("")) {
                        for (int i = 0; i < services.artistServices.size(); i++) {
                            if (services.artistServices.get(i).serviceId == (serviceId)) {
                                adapterBizType.clickItem(i);
                            }
                        }
                    } else adapterBizType.clickItem();

                }
                resetAllServices();

                break;

            case R.id.btnToday:
                viewCalendar.setToday(true);
                String currentData = CalendarHelper.getTimestamp(Constant.TIMESTAMP_FORMAT_DAY);
                viewCalendar.setFirstDayOfWeek(currentData);

                viewCalendar.collapse(500);
                selectedDate = getCurrentDate();
                viewCalendar.isFirstimeLoad = true;
                if (selectedDate.contains("-")) {
                    isTodayClicked = true;
                    int year;
                    String month, day;
                    String[] separated = selectedDate.split("-");
                    year = Integer.parseInt(separated[0]);
                    month = String.valueOf(Integer.parseInt(separated[1]));
                    day = String.valueOf(Integer.parseInt(separated[2]));

                    Calendar cal = Calendar.getInstance();
                    CalendarAdapter adapter = new CalendarAdapter(this, cal);

                    viewCalendar.setAdapter(adapter);
                    setCalenderClickListner(viewCalendar);
                    dayId = cal.get(GregorianCalendar.DAY_OF_WEEK) - 2;
                    if (Integer.parseInt(month) < 10) {
                        month = "0" + Integer.parseInt((month));
                    }

                    if (Integer.parseInt(day) < 10) {
                        day = "0" + Integer.parseInt((day));
                    }

                    selectedDate = year + "-" + month + "-" + day;

                    staff = 0;
                    startTime = "";
                    apiForserviceStaff(String.valueOf(childId));
                    setTodaysDay(dayId);
                }
                //     apiForGetSlots();
                break;

            case R.id.btnCOnfirmBooking:
                // viewCalendar.select(new Day(2019, 1, 8),true);
                if (childId != 0 && !TextUtils.isEmpty(startTime) && !TextUtils.isEmpty(bookingId)) {
                    apiForContinueBooking(bookingId);
                } else if (isAlreadybooked) {
                    Intent intent = new Intent(BookingActivity.this, BookingConfirmActivity.class);
                    intent.putExtra("artistId", artistId);
                    intent.putExtra("isBankAdded", isBankAdded);
                    intent.putExtra("payOption", payOption);
                    intent.putExtra("bookingSetting", bookingSetting);
                    intent.putExtra("isOutCallSelected", isOutCallSelected);
                    intent.putExtra("artistLat", artistLat);
                    intent.putExtra("artistLng", artistLng);
                    intent.putExtra("radius", radius);
                    startActivityForResult(intent, Constant.REQUEST_Select_Service);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else {
                    if (childId == 0) {
                        MyToast.getInstance(BookingActivity.this).showDasuAlert("Please select service");
                    } else if (TextUtils.isEmpty(startTime)) {
                        MyToast.getInstance(BookingActivity.this).showDasuAlert("Please select time slot");
                    } else apiForContinueBooking("0");
                }

                break;

            case R.id.lyArtistTopView:
                Intent intent = new Intent(BookingActivity.this, WorkingHourActivity.class);
                intent.putExtra("busineshoursList", busineshoursList);
                startActivity(intent);
                break;
            case R.id.ly_main:
                cv_ly_biz_type.setVisibility(View.GONE);
                cv_ly_category.setVisibility(View.GONE);
                break;
        }
    }



    private void resetAllServices() {
        childId = 0;
        staffInfoBeanList.clear();
        if (staffAdapter != null)
            staffAdapter.notifyDataSetChanged();

        timeSlotList.clear();
        if (timeSlotAdapter != null)
            timeSlotAdapter.notifyDataSetChanged();

        for (int i = 0; i < outCallList.size(); i++) {
            outCallList.get(i).isSelected = false;
        }

        for (int i = 0; i < inCallList.size(); i++) {
            inCallList.get(i).isSelected = false;
        }

        if (inCallAdapter != null)
            inCallAdapter.notifyDataSetChanged();

        ly_time_slot_main.setVisibility(View.GONE);
        ly_staff_main.setVisibility(View.GONE);

        isEditService = false;
    }


    private void setCalenderClickListner(final MyFlexibleCalendar viewCalendar) {
        viewCalendar.setCalendarListener(new MyFlexibleCalendar.CalendarListener() {
            @Override
            public void onDaySelect() {

                Day day = viewCalendar.getSelectedDay();
                viewCalendar.isFirstimeLoad = false;
                Log.i(getClass().getName(), "Selected Day: "
                        + day.getYear() + "/" + (day.getMonth() + 1) + "/" + day.getDay());

                Date date = new Date(day.getYear(), day.getMonth(), day.getDay() - 1);
                dayId = date.getDay() - 1;

                if (dayId == -1) {
                    dayId = 6;
                }

                int month = day.getMonth() + 1;

                if (month < 10) {
                    sMonth = "0" + month;
                } else {
                    sMonth = String.valueOf(month);
                }

                if (day.getDay() < 10) {
                    sDay = "0" + day.getDay();
                } else {
                    sDay = String.valueOf(day.getDay());
                }
                selectedDate = day.getYear() + "-" + sMonth + "-" + sDay;

                setTodaysDay(dayId);

                if (viewCalendar.isSelectedDay(day)) {
                    Calendar todayCal = Calendar.getInstance();
                    int cYear = todayCal.get(Calendar.YEAR);
                    int cMonth = todayCal.get(Calendar.MONTH) + 1;
                    int cDay = todayCal.get(Calendar.DAY_OF_MONTH);

                    int year = day.getYear();
                    int dayOfMonth = day.getDay();

                    if (year >= cYear && month >= cMonth) {
                        if (year == cYear && month == cMonth && dayOfMonth < cDay) {
                            Log.i("Date Test", "You can't select previous date.");
                            MyToast.getInstance(BookingActivity.this).showDasuAlert("You can't select previous date.");
                        } else {
                            if (childId == 0) {
                                MyToast.getInstance(BookingActivity.this).showDasuAlert("Please select service");
                            } else {
                                staff = 0;
                                startTime = "";
                                apiForserviceStaff(String.valueOf(childId));
                            }
                        }
                    } else {
                        Log.i("Date Test", "You can't select previous date.");
                    }

                }
            }

            @Override
            public void onItemClick(View v) {
                viewCalendar.isFirstimeLoad = false;
                Day day = viewCalendar.getSelectedDay();

                viewCalendar.setFirstDayOfWeek(day.getDay());
                viewCalendar.setAdapter(adapter);


                Log.i(getClass().getName(), "The Day of Clicked View: "
                        + day.getYear() + "/" + (day.getMonth() + 1) + "/" + day.getDay());




            }

            @Override
            public void onDataUpdate() {

            }

            @Override
            public void onMonthChange() {
                Log.i(getClass().getName(), "Month Changed"
                        + ". Current Year: " + viewCalendar.getYear()
                        + ", Current Month: " + (viewCalendar.getMonth() + 1));
            }

            @Override
            public void onWeekChange(int position) {
                Log.i(getClass().getName(), "Week Changed"
                        + ". Current Year: " + viewCalendar.getYear()
                        + ", Current Month: " + (viewCalendar.getMonth() + 1)
                        + ", Current Week position of Month: " + position);
            }

        });
    }

    @Override
    public void onRemoveClick(int position) {

    }

    @Override
    public void onButtonClick(int position, String buttonText, int selectedCount) {

    }

    @Override
    public void onBackPressed() {
        if (isAlreadybooked) {
            // give popup here...
            confirmDialog();
            return;
        }
        super.onBackPressed();
    }

    private void confirmDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(BookingActivity.this);
        builder.setTitle("Alert")
                .setMessage("Are you sure you want to permanently remove all selected services?")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with Apis

                        BookingConfirmActivity.stopTimer();
                        apiForRemoveAllBooking();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog a= builder.create();
        a.show();
        Button BN = a.getButton(DialogInterface.BUTTON_NEGATIVE);
        BN.setTextColor(ContextCompat.getColor(this,R.color.red));

    }

    @Override
    protected void onResume() {
        super.onResume();
        main_scroll_view.fullScroll(ScrollView.FOCUS_UP);
        main_scroll_view.smoothScrollTo(0, 0);

        if (isFromSearchBoard) {
            if (callType.equals("Out Call")) {
                ly_outcall.setEnabled(false);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == Constant.REQUEST_Select_Service) {
                resetAllServices();
                apiForGetAllServices();
                isOutCallSelected = data.getBooleanExtra("isOutCallSelected", false);

                if (isOutCallSelected) {
                    chbOutcall.setChecked(true);
                    ly_outcall.setEnabled(false);
                    checkbox_img.setImageResource(R.drawable.active_check_box_ico);
                } else {
                    ly_outcall.setVisibility(View.GONE);
                }
            }
        }

        //case card expaire
        if(resultCode == -3 && requestCode == Constant.REQUEST_Select_Service){
            finish();
        }

        // Case of edit service
        if (resultCode == -2) {
            if (requestCode == Constant.REQUEST_Select_Service) {
                if (data != null) {
                    resetAllServices();

                    childId = data.getIntExtra("_id", 0);
                    artistServiceId = String.valueOf(childId);
                    viewCalendar.isServiceSelected(childId);
                    artistId = data.getStringExtra("artistId");
                    callType = data.getStringExtra("callType");

                    mainServiceName = data.getStringExtra("mainServiceName");
                    subServiceName = data.getStringExtra("subServiceName");

                    serviceId = data.getIntExtra("serviceId", 0);
                    subServiceId = data.getIntExtra("subServiceId", 0);

                    staff = data.getIntExtra("staffId", 0);


                    outcallStaff = data.getBooleanExtra("outcallStaff", false);
                    incallStaff = data.getBooleanExtra("incallStaff", false);
                    dayId = data.getIntExtra("dayId", 0);

                    startTime = data.getStringExtra("startTime");
                    String bookingDate = data.getStringExtra("bookingDate"); //28/01/2019
                    selectedDate = Helper.formateDateFromstring("dd/MM/yyyy", "yyyy-MM-dd", bookingDate);//2019-02-01


                    isEditService = true;


                    if (isOutCallSelected) {
                        if (outcallStaff) {
                            ly_staff_main.setVisibility(View.VISIBLE);
                        } else ly_staff_main.setVisibility(View.GONE);
                    } else {
                        if (incallStaff) {
                            ly_staff_main.setVisibility(View.VISIBLE);
                        } else ly_staff_main.setVisibility(View.GONE);
                    }

                    setDate(bookingDate);

                    apiForGetAllServices();
                }

            }
        }
    }

    private void setDate(String bookingDate) {

        String dd = Helper.formateDateFromstring("dd/MM/yyyy", "dd", bookingDate);
        String mm = Helper.formateDateFromstring("dd/MM/yyyy", "M", bookingDate);
        String yyyy = Helper.formateDateFromstring("dd/MM/yyyy", "yyyy", bookingDate);


        viewCalendar.isFirstimeLoad = false;
        Calendar calendar = Calendar.getInstance();
        System.out.println("Before - " + calendar.getTime());
        calendar.set(Calendar.MONTH, Integer.parseInt(mm) - 1);
        calendar.set(Calendar.DATE, Integer.parseInt(dd));
        calendar.set(Calendar.YEAR, Integer.parseInt(yyyy));
        System.out.println("After - " + calendar.getTime());
        adapter = new CalendarAdapter(this, calendar);
        viewCalendar.setAdapter(adapter);
        setCalenderClickListner(viewCalendar);

        viewCalendar.select(new Day(Integer.parseInt(yyyy), Integer.parseInt(mm) - 1, Integer.parseInt(dd)), false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BookingConfirmActivity.stopTimer();
    }

    private void apiForGetAllServices() {
        Progress.show(BookingActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetAllServices();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", artistId);

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingActivity.this, "artistService", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    busineshoursList.clear();
                    if (status.equals("success")) {
                        Gson gson = new Gson();
                        services = gson.fromJson(response, Services.class);
                        busineshoursList.addAll(services.artistDetail.busineshours);
                        viewCalendar.setDayToGray(busineshoursList);

                        if (isEditService) {
                            for (int i = 0; i < services.artistServices.size(); i++) {
                                if (serviceId == services.artistServices.get(i).serviceId) {
                                    mainServiceName = services.artistServices.get(i).serviceName;
                                }
                            }

                        }

                        radius = services.artistDetail.radius;
                        artistLat = services.artistDetail.latitude;
                        artistLng = services.artistDetail.longitude;

                        businessType = services.artistDetail.businessType;

                        rating.setRating(Float.parseFloat(services.artistDetail.ratingCount));
                        tv_review_count.setText("(" + services.artistDetail.reviewCount + ")");
                        isAlreadybooked = services.artistDetail.isAlreadybooked;
                        if (isOutCallSelected) {
                            preprationTime = services.artistDetail.outCallpreprationTime;
                        } else preprationTime = services.artistDetail.inCallpreprationTime;

                        if (services.artistDetail.bankStatus == 0) {
                            isBankAdded = false;
                        } else isBankAdded = true;

                        payOption = services.artistDetail.payOption;
                        bookingSetting = services.artistDetail.bookingSetting;

                        if (!services.artistDetail.profileImage.isEmpty() && !services.artistDetail.profileImage.equals("")) {
                            Picasso.with(BookingActivity.this).load(services.artistDetail.profileImage).placeholder(R.drawable.default_placeholder).
                                    fit().into(ivProfile);
                        } else {
                            ivProfile.setImageResource(R.drawable.default_placeholder);
                        }
                        tvArtistName.setText(services.artistDetail.userName + "");

                        setTodaysDay(dayId);

                        /*.......................................................................................................................*/

                        adapterBizType = new CustomStringAdapter("bizType", services, null, BookingActivity.this, new CustomStringAdapter.onClickItem() {
                            @Override
                            public void onclick(final Services.ArtistServicesBean artistServicesBean, int adapterPosition) {
                                if (mBizTypeDialog != null) mBizTypeDialog.dismiss();

                                if (!checkPositon.equals(artistServicesBean.serviceName)) {
                                    childPos = 0;
                                }

                                serviceId = artistServicesBean.serviceId;

                                if (!mainServiceName.equals(artistServicesBean.serviceName)) {
                                    if (!TextUtils.isEmpty(mainServiceName)) {
                                        resetAllServices();
                                    }
                                    mainServiceName = "";
                                }
                                // for other screen coming check
                                if (tempMainService.equals("")) {
                                    tempMainService = artistServicesBean.serviceName;
                                } else if (!artistServicesBean.serviceName.equals(tempMainService)) {
                                    tempMainService = artistServicesBean.serviceName;
                                    if (!isEditService) {
                                        resetAllServices();
                                    }

                                }

                                checkPositon = artistServicesBean.serviceName;

                                /* if (!subServiceName.equals("")) {
                                    for (int i = 0; i < artistServicesBean.subServies.size(); i++) {
                                        if (artistServicesBean.subServies.get(i).subServiceName.equals(subServiceName)) {
                                            childPos = i;
                                            subServiceId = artistServicesBean.subServies.get(i).subServiceId;
                                        }
                                    }
                                    subServiceName = "";
                                } else {
                                    if (artistServicesBean.subServies.size() > 0)
                                        subServiceId = artistServicesBean.subServies.get(childPos).subServiceId;
                                }*/

                                if (!subServiceName.equals("")) {
                                    for (int i = 0; i < artistServicesBean.subServies.size(); i++) {
                                        if (artistServicesBean.subServies.get(i).subServiceId == (subServiceId)) {
                                            childPos = i;
                                            subServiceId = artistServicesBean.subServies.get(i).subServiceId;
                                        }
                                    }
                                    subServiceName = "";
                                } else {
                                    if (artistServicesBean.subServies.size() > 0)
                                        subServiceId = artistServicesBean.subServies.get(childPos).subServiceId;
                                }


                                tv_bizType.setText(artistServicesBean.serviceName + "");
                                cv_ly_biz_type.setVisibility(View.GONE);

                                if (artistServicesBean.subServies.size() == 1) {
                                    isOpenCategory = false;
                                } else isOpenCategory = true;

                                if (artistServicesBean.subServies.size() > 0) {
                                    if (artistServicesBean.subServies.get(childPos) != null) {
                                        tv_category.setText(artistServicesBean.subServies.get(childPos).subServiceName + "");

                                        inCallList.clear();
                                        outCallList.clear();

                                        for (int i = 0; i < artistServicesBean.subServies.get(childPos).artistservices.size(); i++) {

                                            if (artistServicesBean.subServies.get(childPos).artistservices.get(i).bookingType.equals("Both")) {
                                                if (childId == (artistServicesBean.subServies.get(childPos).artistservices.get(i)._id)) {
                                                    artistServicesBean.subServies.get(childPos).artistservices.get(i).isSelected = true;
                                                    totalTime = (getMinutes(artistServicesBean.subServies.get(childPos).artistservices.get(i).completionTime) + getMinutes(preprationTime));
                                                }
                                                inCallList.add(artistServicesBean.subServies.get(childPos).artistservices.get(i));
                                                outCallList.add(artistServicesBean.subServies.get(childPos).artistservices.get(i));
                                            } else if (artistServicesBean.subServies.get(childPos).artistservices.get(i).bookingType.equals("Incall")) {
                                                if (childId == (artistServicesBean.subServies.get(childPos).artistservices.get(i)._id)) {
                                                    artistServicesBean.subServies.get(childPos).artistservices.get(i).isSelected = true;

                                                    totalTime = (getMinutes(artistServicesBean.subServies.get(childPos).artistservices.get(i).completionTime) + getMinutes(preprationTime));
                                                }
                                                inCallList.add(artistServicesBean.subServies.get(childPos).artistservices.get(i));
                                            } else if (artistServicesBean.subServies.get(childPos).artistservices.get(i).bookingType.equals("Outcall")) {
                                                if (childId == (artistServicesBean.subServies.get(childPos).artistservices.get(i)._id)) {
                                                    artistServicesBean.subServies.get(childPos).artistservices.get(i).isSelected = true;
                                                    totalTime = (getMinutes(artistServicesBean.subServies.get(childPos).artistservices.get(i).completionTime) + getMinutes(preprationTime));
                                                }
                                                outCallList.add(artistServicesBean.subServies.get(childPos).artistservices.get(i));
                                            }

                                        }

                                       /* if (inCallList.size() == 0) {
                                            ly_incall.setVisibility(View.GONE);
                                        } else ly_incall.setVisibility(View.VISIBLE);*/

                                        ly_incall.setVisibility(View.VISIBLE);

                                        if (inCallList.size() == 0 && outCallList.size() == 0) {
                                            main_scroll_view.setVisibility(View.GONE);
                                            tv_msg.setVisibility(View.VISIBLE);
                                        } else {
                                            tv_msg.setVisibility(View.GONE);
                                            main_scroll_view.setVisibility(View.VISIBLE);

                                            if (isOutCallSelected) {
                                                if (outCallList.size() == 0) {
                                                    main_scroll_view.setVisibility(View.GONE);
                                                    tv_msg.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        }

                                        if (!isOutCallSelected) {
                                            if (inCallList.size() == 0) {
                                                main_scroll_view.setVisibility(View.GONE);
                                                tv_msg.setVisibility(View.VISIBLE);
                                                if (isFromServiceTag) { // this is the case when service is not avail when comes from service tag
                                                    if (!isToastShow) {
                                                        MyToast.getInstance(BookingActivity.this).showDasuAlert("Service is no longer available. Please select another service for booking");
                                                        isToastShow = true;
                                                    }
                                                }
                                            }
                                        } else {
                                            if (outCallList.size() == 0) {
                                                main_scroll_view.setVisibility(View.GONE);
                                                tv_msg.setVisibility(View.VISIBLE);
                                                if (isFromServiceTag) { // this is the case when service is not avail when comes from service tag
                                                    if (!isToastShow) {
                                                        MyToast.getInstance(BookingActivity.this).showDasuAlert("Service is no longer available. Please select another service for booking");
                                                        isToastShow = true;
                                                    }
                                                }
                                            }
                                        }


                                        if (isOutCallSelected) {
                                            inCallAdapter = new IncallOutCallAdapter(BookingActivity.this, outCallList, "Out Call", true);
                                        } else {
                                            inCallAdapter = new IncallOutCallAdapter(BookingActivity.this, inCallList, "In Call", true);
                                        }

                                        inCallAdapter.setClickListner(click);
                                        rcv_incall.setAdapter(inCallAdapter);

                                    }
                                } else {
                                    tv_category.setText("No category available");
                                    iv_down_arrow_category.setVisibility(View.GONE);
                                    main_scroll_view.setVisibility(View.GONE);
                                    tv_msg.setVisibility(View.VISIBLE);
                                }


                                if (artistServicesBean.subServies.size() > 1) {
                                    iv_down_arrow_category.setVisibility(View.VISIBLE);
                                } else iv_down_arrow_category.setVisibility(View.GONE);


                                if (services.artistServices.size() == 1) {
                                    iv_down_arrow_bizType.setVisibility(View.GONE);
                                } else iv_down_arrow_bizType.setVisibility(View.VISIBLE);


                                adapterCategory = new CustomStringAdapter("categoryType", null, artistServicesBean.subServies, BookingActivity.this, null, new CustomStringAdapter.onClickItemCategory() {
                                    @Override
                                    public void onclick(Services.ArtistServicesBean.SubServiesBean bean, int position) {
                                        if (mCatTypeDialog != null) mCatTypeDialog.dismiss();

                                        subServiceId = bean.subServiceId;
                                        resetAllServices();
                                        childPos = position;
                                        tv_category.setText(bean.subServiceName + "");
                                        cv_ly_category.setVisibility(View.GONE);

                                        inCallList.clear();
                                        outCallList.clear();

                                        // clear staff and slot
                                        staff = 0;
                                        startTime = "";


                                        for (int i = 0; i < bean.artistservices.size(); i++) {

                                            if (bean.artistservices.get(i).bookingType.equals("Both")) {

                                                inCallList.add(bean.artistservices.get(i));
                                                outCallList.add(bean.artistservices.get(i));

                                            } else if (bean.artistservices.get(i).bookingType.equals("Incall")) {
                                                inCallList.add(bean.artistservices.get(i));
                                            } else if (bean.artistservices.get(i).bookingType.equals("Outcall")) {
                                                outCallList.add(bean.artistservices.get(i));
                                            }
                                        }

                                        /*......................................................................*/
                                        if (isOutCallSelected) {
                                            inCallAdapter = new IncallOutCallAdapter(BookingActivity.this, outCallList, "Out Call", true);
                                        } else {
                                            inCallAdapter = new IncallOutCallAdapter(BookingActivity.this, inCallList, "In Call", true);
                                        }
                                        inCallAdapter.setClickListner(click);
                                        rcv_incall.setAdapter(inCallAdapter);

                                       /* if (inCallList.size() == 0) {
                                            ly_incall.setVisibility(View.GONE);
                                        } else ly_incall.setVisibility(View.VISIBLE);*/

                                        ly_incall.setVisibility(View.VISIBLE);
                                        if (inCallList.size() == 0 && outCallList.size() == 0) {
                                            main_scroll_view.setVisibility(View.GONE);
                                            tv_msg.setVisibility(View.VISIBLE);
                                        } else {
                                            tv_msg.setVisibility(View.GONE);
                                            main_scroll_view.setVisibility(View.VISIBLE);

                                            if (isOutCallSelected) {
                                                if (outCallList.size() == 0) {
                                                    main_scroll_view.setVisibility(View.GONE);
                                                    tv_msg.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        }


                                    }
                                });

                                rcv_category_type.setAdapter(adapterCategory);

                            }
                        }, null);

//this is the case when we are coming from service tab
                        if (services.artistServices.size() != 0) {
                            for (int i = 0; i < services.artistServices.size(); i++) {
                                if (services.artistServices.get(i).serviceId == (serviceId)) {
                                    adapterBizType.clickItem(i);
                                }
                            }
                        } else if (services.artistServices.size() == 0) {
                            tv_bizType.setText("No Business Type Available");
                            tv_category.setText("No Category Available");
                            iv_down_arrow_bizType.setVisibility(View.GONE);
                            iv_down_arrow_category.setVisibility(View.GONE);

                            main_scroll_view.setVisibility(View.GONE);
                            tv_msg.setVisibility(View.VISIBLE);
                        }

                       /* if (services.artistServices.size() != 0) {
                            for (int i = 0; i < services.artistServices.size(); i++) {
                                if (services.artistServices.get(i).serviceName.equals(mainServiceName)) {
                                    adapterBizType.clickItem(i);
                                }
                            }
                        } else if (services.artistServices.size() == 0) {
                            tv_bizType.setText("No business type available");
                            tv_category.setText("No category available");
                            iv_down_arrow_bizType.setVisibility(View.GONE);
                            iv_down_arrow_category.setVisibility(View.GONE);
                        }*/


//this is the case when we are coming from other tab not from service tab
                        if (mainServiceName.equals("")) {
                            adapterBizType.clickItem();
                        } else {
                            if (childId != 0)
                                if (!isAlreadybooked) {
                                    apiForserviceStaff(String.valueOf(childId));
                                } else if (isEditService) {
                                    apiForserviceStaff(String.valueOf(childId));
                                    isEditService = false;
                                }

                        }
                        rcv_biz_type.setAdapter(adapterBizType);

                        // Calling api fot staff
                        if (isOutCallSelected) {
                            if (!isAlreadybooked)
                                if (outcallStaff) {
                                    ly_staff_main.setVisibility(View.VISIBLE);
                                } else ly_staff_main.setVisibility(View.GONE);
                        } else {
                            if (!isAlreadybooked)
                                if (incallStaff) {
                                    ly_staff_main.setVisibility(View.VISIBLE);
                                } else ly_staff_main.setVisibility(View.GONE);
                        }


                    }

                } catch (Exception e) {
                    Progress.hide(BookingActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingActivity.this);
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
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(this.getClass().getName());
    }

    private void setTodaysDay(int dayId) {
        String from = "";
        String end = "";

        for (int i = 0; i < services.artistDetail.busineshours.size(); i++) {
            if (services.artistDetail.busineshours.get(i).day == dayId) {
                from = services.artistDetail.busineshours.get(i).startTime + " - " + services.artistDetail.busineshours.get(i).endTime;
                if (!from.equals("")) {
                    if (end.equals("")) {
                        end = from;
                        from = "";
                    }
                }
            }
        }

        String dayName = MyFlexibleCalendar.getDayById(dayId);
        if (getCurrentDate().equals(selectedDate)) {
            dayName = "Today";
        }

        if (!from.equals(""))
            tvbizDate.setText(dayName +" : "+end + " & " + from);
        else if (end.equals("")) {
            tvbizDate.setText("Today : Close");
        } else tvbizDate.setText(dayName +" : "+ end);
    }

    private void apiForserviceStaff(final String artistServiceId) {

        Progress.show(BookingActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForserviceStaff(artistServiceId);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("businessId", artistId);
        params.put("artistServiceId", artistServiceId);
        params.put("day", String.valueOf(dayId));
        params.put("date", selectedDate);

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingActivity.this, "serviceStaff", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    Progress.hide(BookingActivity.this);

                    if (status.equals("success")) {
                        staffInfoBeanList.clear();
                        Gson gson = new Gson();
                        ServiceInfoBooking infoBooking = gson.fromJson(response, ServiceInfoBooking.class);


                        if (isOutCallSelected) {
                            price = infoBooking.staffInfo.get(0).outCallPrice;
                        } else price = infoBooking.staffInfo.get(0).inCallPrice;

                        endTime = (getMinutes(infoBooking.staffInfo.get(0).completionTime) + getMinutes(preprationTime));

                        if (infoBooking.staffInfo.size() == 1) {
                            ly_staff_main.setVisibility(View.GONE);
                        } else if (infoBooking.staffInfo.size() > 1) {
                            ly_staff_main.setVisibility(View.VISIBLE);
                        }

                        staffInfoBeanList.addAll(infoBooking.staffInfo);

                        for (int i = 0; i < staffInfoBeanList.size(); i++) {

                            if (callType.equals("In Call")) {
                                if (staffInfoBeanList.get(i).inCallPrice == 0.0) {
                                    staffInfoBeanList.remove(i);
                                }
                            } else {
                                if (staffInfoBeanList.get(i).outCallPrice == 0.0) {
                                    staffInfoBeanList.remove(i);
                                }
                            }
                        }

                        for (int i = 0; i < staffInfoBeanList.size(); i++) {
                            if (Mualab.currentUser != null) {
                                if (staffInfoBeanList.get(i).staffId == Mualab.currentUser.id) {
                                    staffInfoBeanList.remove(i);
                                }
                            }
                        }


                        if (staff != 0) {
                            for (int i = 0; i < staffInfoBeanList.size(); i++) {

                                if (staff == staffInfoBeanList.get(i).staffId) {
                                    staffInfoBeanList.get(i).isSelected = true;
                                }
                            }
                            //staff = 0;
                        } else {
                            if (staffInfoBeanList.size() > 0)
                                staffInfoBeanList.get(0).isSelected = true;
                        }

                        if (staffInfoBeanList.size() == 1) {
                            ly_staff_main.setVisibility(View.GONE);
                        }else {
                            staffAdapter.notifyDataSetChanged();
                        }




                        if (isFromServiceTag) {
                            apiForstaffSlot(String.valueOf(staff));
                        } else {
                            apiForstaffSlot(String.valueOf(0));
                        }


                        ly_time_slot_main.setVisibility(View.VISIBLE);
                    }

                } catch (Exception e) {
                    Progress.hide(BookingActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingActivity.this);
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
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(this.getClass().getName());
    }

    /*.................................Apis for getting staff info..............................*/

    private int getMinutes(String input) {
        int totalMinues = 0;
        try {
            String hours = Helper.formateDateFromstring("HH:mm", "HH", input);
            String minutes = Helper.formateDateFromstring("HH:mm", "mm", input);

            int hoursInMinutes = (Integer.parseInt(hours) * 60);
            totalMinues = (Integer.parseInt(minutes) + hoursInMinutes);

        } catch (Exception e) {

        }

        return totalMinues;
    }

    private void apiForstaffSlot(final String staffId) {
        Progress.show(BookingActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForstaffSlot(staffId);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", artistId);
        params.put("artistServiceId", artistServiceId);
        params.put("staffId", staffId);
        params.put("userId", String.valueOf(Mualab.currentUser.id));

        params.put("businessType", businessType);
        params.put("currentTime", currentTime);
        params.put("date", selectedDate);
        params.put("day", String.valueOf(dayId));
        params.put("latitude", String.valueOf(Mualab.currentLocation.lat));
        params.put("longitude", String.valueOf(Mualab.currentLocation.lng));
        params.put("serviceTime", ("00:" + String.valueOf(totalTime)));

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingActivity.this, "artistTimeSlotNew", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    timeSlotAdapter.lastPos = -1;
                    timeSlotAdapter.isSelectedFirst = false;

                    timeSlotList.clear();
                    int tempos = 0;
                    if (status.equals("success")) {
                        JSONArray array = js.getJSONArray("timeSlots");

                        for (int i = 0; i < array.length(); i++) {
                            TimeSlotInfo timeSlotInfo = new TimeSlotInfo();
                            timeSlotInfo.timeSlots = array.get(i).toString();
                            if (startTime.equals(timeSlotInfo.timeSlots)) {
                                timeSlotInfo.isSelectSlot = true;
                                timeSlotAdapter.lastPos = i;
                                timeSlotAdapter.isSelectedFirst = true;
                                tempos = i;
                            } else timeSlotInfo.isSelectSlot = false;

                            timeSlotList.add(timeSlotInfo);
                        }
                    } else {
                        MyToast.getInstance(BookingActivity.this).showDasuAlert(message);
                    }

                    if (timeSlotList.size() == 0) {
                        tvNoSlot.setVisibility(View.VISIBLE);
                        ly_time_slot_main.setVisibility(View.GONE);
                    } else {
                        tvNoSlot.setVisibility(View.GONE);
                        ly_time_slot_main.setVisibility(View.VISIBLE);
                    }

                    rcv_timeSlot.scrollToPosition(tempos);

                    timeSlotAdapter.notifyDataSetChanged();

                    if (js.has("bookingId")) {
                        bookingId = String.valueOf(js.getInt("bookingId"));
                    }


                } catch (Exception e) {
                    Progress.hide(BookingActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingActivity.this);
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
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(this.getClass().getName());
    }

    private void apiForContinueBooking(final String bookingId) {
        Progress.show(BookingActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForContinueBooking(bookingId);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("startTime", startTime);

        String myTime = startTime;
        SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
        Date d = new Date();
        try {
            d = df.parse(myTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.MINUTE, endTime);
        String newTime = df.format(cal.getTime());

        params.put("endTime", newTime);
        params.put("artistId", artistId);
        params.put("staff", String.valueOf(staff));
        params.put("userId", String.valueOf(Mualab.currentUser.id));

        params.put("serviceId", String.valueOf(serviceId));// business type Id
        params.put("subServiceId", String.valueOf(subServiceId));// category Id
        params.put("artistServiceId", artistServiceId);
        params.put("bookingDate", selectedDate);

        if (isOutCallSelected) {
            params.put("serviceType", "2");// incall = 1 or out call = 2
        } else params.put("serviceType", "1");// incall = 1 or out call = 2


        params.put("price", String.valueOf(price));
        params.put("bookingId", bookingId);// first blank this is use for update service


        HttpTask task = new HttpTask(new HttpTask.Builder(BookingActivity.this, "bookArtist", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");


                    if (status.equals("success")) {
                        Intent intent = new Intent(BookingActivity.this, BookingConfirmActivity.class);
                        intent.putExtra("artistId", artistId);
                        intent.putExtra("isBankAdded", isBankAdded);
                        intent.putExtra("payOption", payOption);
                        intent.putExtra("bookingSetting", bookingSetting);
                        intent.putExtra("isOutCallSelected", isOutCallSelected);
                        intent.putExtra("artistLat", artistLat);
                        intent.putExtra("artistLng", artistLng);
                        intent.putExtra("radius", radius);
                        startActivityForResult(intent, Constant.REQUEST_Select_Service);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);


                        startTime = "";
                    } else {
                        MyToast.getInstance(BookingActivity.this).showDasuAlert(message);
                    }

                } catch (Exception e) {
                    Progress.hide(BookingActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingActivity.this);
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
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(this.getClass().getName());
    }

    private void apiForRemoveAllBooking() {
        Progress.show(BookingActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForRemoveAllBooking();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(Mualab.currentUser.id));


        HttpTask task = new HttpTask(new HttpTask.Builder(BookingActivity.this, "deleteUserBookService", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");


                    if (status.equals("success")) {
                        finish();
                    } else {
                        MyToast.getInstance(BookingActivity.this).showDasuAlert(message);
                    }

                } catch (Exception e) {
                    Progress.hide(BookingActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingActivity.this);
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
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(this.getClass().getName());
    }
    /*.......................................................................................*/

    private void setBizTypeDialog(boolean isShow) {
        if (mBizTypeDialog == null) {
            mBizTypeDialog = new BottomSheetDialog(BookingActivity.this, R.style.CustomBottomSheetDialogTheme);
            View sheetView = getLayoutInflater().inflate(R.layout.dialog_bottom, null);
            TextView tvTitle = sheetView.findViewById(R.id.tvTitle);
            tvTitle.setText(getString(R.string.business_types));
            rcv_biz_type = sheetView.findViewById(R.id.recyclerView);
            mBizTypeDialog.setContentView(sheetView);
        }

        if (isShow)
            mBizTypeDialog.show();
    }

    private void setCatTypeDialog(boolean isShow) {
        if (mCatTypeDialog == null) {
            mCatTypeDialog = new BottomSheetDialog(BookingActivity.this, R.style.CustomBottomSheetDialogTheme);
            View sheetView = getLayoutInflater().inflate(R.layout.dialog_bottom, null);
            TextView tvTitle = sheetView.findViewById(R.id.tvTitle);
            tvTitle.setText(getString(R.string.category_types));
            rcv_category_type = sheetView.findViewById(R.id.recyclerView);
            mCatTypeDialog.setContentView(sheetView);
        }

        if (isShow)
            mCatTypeDialog.show();
    }


}

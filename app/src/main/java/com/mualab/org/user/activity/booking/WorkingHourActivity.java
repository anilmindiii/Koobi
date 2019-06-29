package com.mualab.org.user.activity.booking;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.model.Services;
import com.mualab.org.user.activity.booking.adapter.WorkingHoursAdapter;

import java.util.ArrayList;

public class WorkingHourActivity extends AppCompatActivity {
    RecyclerView rcv_sunday;
    RecyclerView rcv_monday;
    RecyclerView rcv_tuesday;
    RecyclerView rcv_wednesday;
    RecyclerView rcv_thursday;
    RecyclerView rcv_friday;
    RecyclerView rcv_saturday;

    TextView tv_close_monday;
    TextView tv_close_tuesday;
    TextView tv_close_wednesday;
    TextView tv_close_thursday;
    TextView tv_close_friday;
    TextView tv_close_saturday;
    TextView tv_close_sunday;

    private ArrayList<Services.ArtistDetailBean.BusineshoursBean> busineshoursList;
    private WorkingHoursAdapter hoursAdapter;
    ArrayList<WorkingInfo> workingarrayList;
    ArrayList<WorkingInfo> workingTemp0;
    ArrayList<WorkingInfo> workingTemp1;
    ArrayList<WorkingInfo> workingTemp2;
    ArrayList<WorkingInfo> workingTemp3;
    ArrayList<WorkingInfo> workingTemp4;
    ArrayList<WorkingInfo> workingTemp5;
    ArrayList<WorkingInfo> workingTemp6;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_working_hour);

        rcv_sunday = findViewById(R.id.rcv_sunday);
        rcv_monday = findViewById(R.id.rcv_monday);
        rcv_tuesday = findViewById(R.id.rcv_tuesday);
        rcv_wednesday = findViewById(R.id.rcv_wednesday);
        rcv_thursday = findViewById(R.id.rcv_thursday);
        rcv_friday = findViewById(R.id.rcv_friday);
        rcv_saturday = findViewById(R.id.rcv_saturday);

        tv_close_sunday = findViewById(R.id.tv_close_sunday);
        tv_close_monday = findViewById(R.id.tv_close_monday);
        tv_close_tuesday = findViewById(R.id.tv_close_tuesday);
        tv_close_wednesday = findViewById(R.id.tv_close_wednesday);
        tv_close_thursday = findViewById(R.id.tv_close_thursday);
        tv_close_friday = findViewById(R.id.tv_close_friday);
        tv_close_saturday = findViewById(R.id.tv_close_saturday);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.text_working_hours));

        workingarrayList = new ArrayList<>();
        workingTemp0 = new ArrayList<>();
        workingTemp1 = new ArrayList<>();
        workingTemp2 = new ArrayList<>();
        workingTemp3 = new ArrayList<>();
        workingTemp4 = new ArrayList<>();
        workingTemp5 = new ArrayList<>();
        workingTemp6 = new ArrayList<>();

        busineshoursList = (ArrayList<Services.ArtistDetailBean.BusineshoursBean>) getIntent().getSerializableExtra("busineshoursList");


        for(int i=0;i<busineshoursList.size();i++){
            switch (busineshoursList.get(i).day){
                case 6:
                   WorkingInfo  workingInfo = new WorkingInfo();
                   workingInfo.DayId = busineshoursList.get(i).day;
                   workingInfo.DayName = getDayById(busineshoursList.get(i).day);

                   workingInfo.StartTime = busineshoursList.get(i).startTime;
                   workingInfo.End = busineshoursList.get(i).endTime;

                   workingTemp0.add(workingInfo);
                   hoursAdapter = new WorkingHoursAdapter(workingTemp0);
                   rcv_sunday.setAdapter(hoursAdapter);
                   break;

               case 0:
                   workingInfo = new WorkingInfo();
                   workingInfo.DayId = busineshoursList.get(i).day;
                   workingInfo.DayName = getDayById(busineshoursList.get(i).day);

                   workingInfo.StartTime = busineshoursList.get(i).startTime;
                   workingInfo.End = busineshoursList.get(i).endTime;

                   workingTemp1.add(workingInfo);
                   hoursAdapter = new WorkingHoursAdapter(workingTemp1);
                   rcv_monday.setAdapter(hoursAdapter);

                   break;

               case 1:
                   workingInfo = new WorkingInfo();
                   workingInfo.DayId = busineshoursList.get(i).day;
                   workingInfo.DayName = getDayById(busineshoursList.get(i).day);

                   workingInfo.StartTime = busineshoursList.get(i).startTime;
                   workingInfo.End = busineshoursList.get(i).endTime;

                   workingTemp2.add(workingInfo);
                   hoursAdapter = new WorkingHoursAdapter(workingTemp2);
                   rcv_tuesday.setAdapter(hoursAdapter);
                   break;

               case 2:
                   workingInfo = new WorkingInfo();
                   workingInfo.DayId = busineshoursList.get(i).day;
                   workingInfo.DayName = getDayById(busineshoursList.get(i).day);

                   workingInfo.StartTime = busineshoursList.get(i).startTime;
                   workingInfo.End = busineshoursList.get(i).endTime;

                   workingTemp3.add(workingInfo);
                   hoursAdapter = new WorkingHoursAdapter(workingTemp3);
                   rcv_wednesday.setAdapter(hoursAdapter);
                   break;

               case 3:
                   workingInfo = new WorkingInfo();
                   workingInfo.DayId = busineshoursList.get(i).day;
                   workingInfo.DayName = getDayById(busineshoursList.get(i).day);

                   workingInfo.StartTime = busineshoursList.get(i).startTime;
                   workingInfo.End = busineshoursList.get(i).endTime;

                   workingTemp4.add(workingInfo);
                   hoursAdapter = new WorkingHoursAdapter(workingTemp4);
                   rcv_thursday.setAdapter(hoursAdapter);
                   break;

               case 4:
                   workingInfo = new WorkingInfo();
                   workingInfo.DayId = busineshoursList.get(i).day;
                   workingInfo.DayName = getDayById(busineshoursList.get(i).day);

                   workingInfo.StartTime = busineshoursList.get(i).startTime;
                   workingInfo.End = busineshoursList.get(i).endTime;

                   workingTemp5.add(workingInfo);
                   hoursAdapter = new WorkingHoursAdapter(workingTemp5);
                   rcv_friday.setAdapter(hoursAdapter);

                   break;

               case 5:
                   workingInfo = new WorkingInfo();
                   workingInfo.DayId = busineshoursList.get(i).day;
                   workingInfo.DayName = getDayById(busineshoursList.get(i).day);

                   workingInfo.StartTime = busineshoursList.get(i).startTime;
                   workingInfo.End = busineshoursList.get(i).endTime;

                   workingTemp6.add(workingInfo);
                   hoursAdapter = new WorkingHoursAdapter(workingTemp6);
                   rcv_saturday.setAdapter(hoursAdapter);
                   break;
           }
        }

        tv_close_sunday = findViewById(R.id.tv_close_sunday);
        tv_close_monday = findViewById(R.id.tv_close_monday);
        tv_close_tuesday = findViewById(R.id.tv_close_tuesday);
        tv_close_wednesday = findViewById(R.id.tv_close_wednesday);
        tv_close_thursday = findViewById(R.id.tv_close_thursday);
        tv_close_friday = findViewById(R.id.tv_close_friday);
        tv_close_saturday = findViewById(R.id.tv_close_saturday);


        if(workingTemp0.size() == 0){
            tv_close_sunday.setVisibility(View.VISIBLE);
        }else  tv_close_sunday.setVisibility(View.GONE);

        if(workingTemp1.size() == 0){
            tv_close_monday.setVisibility(View.VISIBLE);
        }else  tv_close_monday.setVisibility(View.GONE);

        if(workingTemp2.size() == 0){
            tv_close_tuesday.setVisibility(View.VISIBLE);
        }else  tv_close_tuesday.setVisibility(View.GONE);

        if(workingTemp3.size() == 0){
            tv_close_wednesday.setVisibility(View.VISIBLE);
        }else  tv_close_wednesday.setVisibility(View.GONE);

        if(workingTemp4.size() == 0){
            tv_close_thursday.setVisibility(View.VISIBLE);
        }else  tv_close_thursday.setVisibility(View.GONE);

        if(workingTemp5.size() == 0){
            tv_close_friday.setVisibility(View.VISIBLE);
        }else  tv_close_friday.setVisibility(View.GONE);

        if(workingTemp6.size() == 0){
            tv_close_saturday.setVisibility(View.VISIBLE);
        }else  tv_close_saturday.setVisibility(View.GONE);



        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }


    public static String getDayById(int dayId){
        String day = "";
        switch (dayId){
            case 6:
                day =  "Sunday";
                break;

            case 0:
                day =  "Monday";
                break;

            case 1:
                day =  "Tuesday";
                break;

            case 2:
                day =  "Wednesday";
                break;

            case 3:
                day =  "Thursday";
                break;

            case 4:
                day =  "Friday";
                break;

            case 5:
                day =  "Saturday";
                break;

        }
        return day;
    }

    public class WorkingInfo{
        public int DayId;
        public String DayName;
        public String StartTime;
        public String End;

    }
}

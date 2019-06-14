package com.mualab.org.user.activity.businessInvitaion.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.VolleyError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.model.Services;
import com.mualab.org.user.activity.businessInvitaion.adapter.InvitationAdapter;
import com.mualab.org.user.activity.businessInvitaion.model.BusinessDayForStaff;
import com.mualab.org.user.activity.businessInvitaion.model.BusinessProfile;
import com.mualab.org.user.activity.businessInvitaion.model.ComapnySelectedServices;
import com.mualab.org.user.activity.businessInvitaion.model.CompanyDetail;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.model.booking.BusinessDay;
import com.mualab.org.user.data.model.booking.TimeSlot;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InvitationActivity extends AppCompatActivity {
    private RecyclerView rvInvitation;
    private List<CompanyDetail> invitationList;
    private InvitationAdapter invitationAdapter;
    private TextView tvNoDataFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);
        initView();

        FirebaseDatabase.getInstance().getReference().child("socialBookingBadgeCount")
                .child(Mualab.currentUser.id+"").child("businessInvitationCount").setValue(0);
    }

    private void initView() {
        invitationList = new ArrayList<>();

        invitationAdapter = new InvitationAdapter(InvitationActivity.this,
                invitationList, new InvitationAdapter.onDetailClickListner() {
            @Override
            public void onClick(int position, CompanyDetail companyDetail) {
                if (companyDetail.status.equals("0")) { // should be 0
                    Intent intent = new Intent(InvitationActivity.this, InvitationDetailActivity.class);
                    intent.putExtra("companyDetail", companyDetail);
                    startActivityForResult(intent, 99);
                }
            }
        });

        ImageView ivHeaderBack = findViewById(R.id.btnBack);
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.invitation));

        rvInvitation = findViewById(R.id.rvInvitation);
        LinearLayoutManager layoutManager = new LinearLayoutManager(InvitationActivity.this);
        rvInvitation.setLayoutManager(layoutManager);
        rvInvitation.setAdapter(invitationAdapter);

        tvNoDataFound = findViewById(R.id.tvNoDataFound);

        ivHeaderBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             onBackPressed();
            }
        });

        apiForCompanyInvitation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99 && resultCode != 0) {
            if(data.getStringExtra("type").equals("accept")){
                String bizName = data.getStringExtra("name");
                showCongratsAlert(bizName);
            }
            apiForCompanyInvitation();
        }
    }

    public void showCongratsAlert(String name){
        String s1 = "You are a member of ";
        String s2 = " You can use the same credential to login in our \nKoobi app";
        View dialogView = View.inflate(this, R.layout.new_alert_dialog, null);
        final Dialog dialog = new Dialog(this,android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(dialogView);
        TextView tv_bizAppLink = dialogView.findViewById(R.id.tv_bizAppLink);
        tv_bizAppLink.setVisibility(View.VISIBLE);
        TextView tv_alert_msg = dialogView.findViewById(R.id.tv_alert_msg);
        TextView tv_header_title = dialogView.findViewById(R.id.tv_header_title);
        tv_header_title.setText(R.string.congratulations);

        String textToHighlight = "<b>" + ""+ name + "</b> ";
        String replacedWith = "<font color='black'>" + textToHighlight + "</font>";

        tv_alert_msg.setText(Html.fromHtml(s1+" "+replacedWith+" "+s2));
        dialogView.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialogView.findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private void apiForCompanyInvitation() {
        Progress.show(InvitationActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(InvitationActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForCompanyInvitation();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(InvitationActivity.this, "companyInfo", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        rvInvitation.setVisibility(View.VISIBLE);
                        tvNoDataFound.setVisibility(View.GONE);
                        invitationList.clear();

                        JSONArray jsonArray = js.getJSONArray("businessList");

                        if (jsonArray!=null && jsonArray.length()!=0) {

                            for (int i=0; i<jsonArray.length(); i++){
                                JSONObject object = jsonArray.getJSONObject(i);
                                CompanyDetail item = new CompanyDetail();
                                item._id = object.getString("_id");
                                item.artistId = object.getString("artistId");
                                item.businessId = object.getString("businessId");
                                item.holiday = object.getString("holiday");

                                if (object.has("salaries"))
                                    item.salaries = object.getString("salaries");
                                if (object.has("message"))
                                    item.message = object.getString("message");

                                item.job = object.getString("job");
                                item.mediaAccess = object.getString("mediaAccess");
                                item.businessName = object.getString("businessName");
                                item.profileImage = object.getString("profileImage");
                                item.address = object.getString("address");
                                item.userName = object.getString("userName");
                                item.status = object.getString("status");

                                JSONArray businessTypeArray = object.getJSONArray("businessType");
                                if (businessTypeArray!=null && businessTypeArray.length()!=0) {
                                    for (int l=0; l<businessTypeArray.length(); l++) {
                                        JSONObject jsonObject = businessTypeArray.getJSONObject(l);
                                        item.businessType.add(jsonObject.getString("serviceName"));
                                    }
                                }

/* JSONArray staffHoursArray = object.getJSONArray("staffHours");
if (staffHoursArray!=null && staffHoursArray.length()!=0) {
for (int j=0; j<staffHoursArray.length(); j++){
JSONObject object2 = staffHoursArray.getJSONObject(j);
BusinessDayForStaff item2 = new BusinessDayForStaff();
item2.day = Integer.parseInt(object2.getString("day"));
item2.endTime = object2.getString("endTime");
item2.startTime = object2.getString("startTime");
item.staffHoursList.add(item2);
}
}*/

                                JSONArray staffHoursArray = object.getJSONArray("staffHours");

                                BusinessProfile bsp = new BusinessProfile();

                                if (staffHoursArray!=null && staffHoursArray.length()!=0) {
                                    bsp.businessDays = getBusinessDay();

                                    Set<BusinessDay> daySet = new HashSet<>();

                                    for (int j=0; j<staffHoursArray.length(); j++){
                                        JSONObject object2 = staffHoursArray.getJSONObject(j);
                                        BusinessDayForStaff item2 = new BusinessDayForStaff();

                                        BusinessDay day = new BusinessDay();
                                        int dayId = object2.getInt("day");
                                        TimeSlot slot = new TimeSlot(dayId);
                                        slot.startTime = object2.getString("startTime");
                                        slot.minStartTime = object2.getString("startTime");
                                        slot.endTime = object2.getString("endTime");
                                        slot.maxEndTime = object2.getString("endTime");

                                        item2.day = Integer.parseInt(object2.getString("day"));
                                        item2.endTime = object2.getString("endTime");
                                        item2.startTime = object2.getString("startTime");
                                        item.staffHoursList.add(item2);

                                        for(BusinessDay tmpDay : bsp.businessDays){
                                            if(tmpDay.dayId == dayId){
                                                tmpDay.isOpen = true;
                                                day.isExpand = true;

                                                tmpDay.addTimeSlot(slot);
                                                daySet.add(tmpDay);
// item.businessDays.add(tmpDay);
                                                break;
                                            }
                                        }
                                    }

                                    item.businessDays.addAll(daySet);

                                    Collections.sort(item.businessDays, new Comparator<BusinessDay>() {

                                        @Override
                                        public int compare(BusinessDay a1, BusinessDay a2) {

                                            if (a1.dayId == 0 || a2.dayId == 0)
                                                return -1;
                                            else {
                                                Long long1 = (long) a1.dayId;
                                                Long long2 = (long) a2.dayId;
                                                return long1.compareTo(long2);
                                            }
                                        }
                                    });
                                }

                                JSONArray staffServiceArray = object.getJSONArray("staffService");
                                for (int k=0; k<staffServiceArray.length(); k++){
                                    JSONObject object3 = staffServiceArray.getJSONObject(k);
                                    Gson gson = new Gson();
                                    ComapnySelectedServices item3 = gson.fromJson(String.valueOf(object3), ComapnySelectedServices.class);
                                    item.staffService.add(item3);
                                }

                                if (item.status.equals("0"))
                                    invitationList.add(item);
                            }
                            invitationAdapter.notifyDataSetChanged();

                            if (invitationList.size()==0){
                                rvInvitation.setVisibility(View.GONE);
                                tvNoDataFound.setVisibility(View.VISIBLE);
                            }
                        }else {
                            rvInvitation.setVisibility(View.GONE);
                            tvNoDataFound.setVisibility(View.VISIBLE);
                        }

                    }else {
                        rvInvitation.setVisibility(View.GONE);
                        tvNoDataFound.setVisibility(View.VISIBLE);
                    }
// showToast(message);
                } catch (Exception e) {
                    Progress.hide(InvitationActivity.this);
                    e.printStackTrace();
                }
                Progress.hide(InvitationActivity.this);
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(InvitationActivity.this);
                try {
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")) {
                        Mualab.getInstance().getSessionManager().logout();
                        // MyToast.getInstance(BookingActivity.this).showDasuAlert(helper.error_Messages(error));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        })
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent();
        if(invitationList.size() == 0){
            intent.putExtra("noInvitation",true);
        }
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    private List<BusinessDay> getBusinessDay(){

        List<BusinessDay>businessDays = new ArrayList<>();

        BusinessDay day1 = new BusinessDay();
        day1.dayName = getString(R.string.monday1);
        day1.dayId = 0;
//day1.addTimeSlot(new TimeSlot(1));

        BusinessDay day2 = new BusinessDay();
        day2.dayName = getString(R.string.tuesday1);
        day2.dayId = 1;

        BusinessDay day3 = new BusinessDay();
        day3.dayName = getString(R.string.wednesday1);
        day3.dayId = 2;

        BusinessDay day4 = new BusinessDay();
        day4.dayName = getString(R.string.thursday1);
        day4.dayId = 3;

        BusinessDay day5 = new BusinessDay();
        day5.dayName = getString(R.string.frieday1);
        day5.dayId = 4;

        BusinessDay day6 = new BusinessDay();
        day6.dayName = getString(R.string.saturday1);
        day6.dayId = 5;

        BusinessDay day7 = new BusinessDay();
        day7.dayName = getString(R.string.sunday1);
        day7.dayId = 6;

        businessDays.add(day1);
        businessDays.add(day2);
        businessDays.add(day3);
        businessDays.add(day4);
        businessDays.add(day5);
        businessDays.add(day6);
        businessDays.add(day7 );
        return businessDays;
    }
}

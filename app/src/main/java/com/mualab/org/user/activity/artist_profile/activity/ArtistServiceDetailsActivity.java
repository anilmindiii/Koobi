package com.mualab.org.user.activity.artist_profile.activity;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.adapter.CustomStringAdapter;
import com.mualab.org.user.activity.artist_profile.adapter.IncallOutCallAdapter;
import com.mualab.org.user.activity.artist_profile.adapter.StaffListAdapter;
import com.mualab.org.user.activity.artist_profile.model.Services;
import com.mualab.org.user.activity.artist_profile.model.StaffDetailsInfo;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArtistServiceDetailsActivity extends AppCompatActivity {

    private String artistId;
    private int _id;
    private TextView tv_description,callType,btn_book;
    private RecyclerView recycler_view;
    private StaffListAdapter adapter;
    private ArrayList<StaffDetailsInfo.StaffInfoBean> detailsInfoList;
    private TextView id_tv_staff_text;
    private String callTypeString =  "";
    String  mainServiceName = "", subServiceName = "";
    private boolean outcallStaff,incallStaff;
    private int serviceId, subServiceId;
    private ImageView btnBack;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_service_details);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.details));

        tv_description = findViewById(R.id.tv_description);
        callType = findViewById(R.id.callType);
        recycler_view = findViewById(R.id.recycler_view);
        id_tv_staff_text = findViewById(R.id.id_tv_staff_text);
        btn_book = findViewById(R.id.btn_book);
        btnBack = findViewById(R.id.btnBack);

        _id  = getIntent().getIntExtra("_id",0);
        artistId = getIntent().getStringExtra("artistId");
        callTypeString =  getIntent().getStringExtra("callType");

        mainServiceName =  getIntent().getStringExtra("mainServiceName");
        subServiceName =  getIntent().getStringExtra("subServiceName");

        serviceId =  getIntent().getIntExtra("serviceId",0);
        subServiceId =  getIntent().getIntExtra("subServiceId",0);

        incallStaff =  getIntent().getBooleanExtra("incallStaff",false);
        outcallStaff =  getIntent().getBooleanExtra("outcallStaff",false);

        callType.setText(callTypeString);
        detailsInfoList = new ArrayList<>();

        adapter = new StaffListAdapter(this,detailsInfoList,callTypeString);
        recycler_view.setAdapter(adapter);

        btnBack.setOnClickListener(view -> onBackPressed());

        btn_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ArtistServiceDetailsActivity.this,BookingActivity.class);

                intent.putExtra("_id",_id);
                intent.putExtra("artistId",artistId);
                intent.putExtra("callType",callTypeString);

                intent.putExtra("mainServiceName",mainServiceName);
                intent.putExtra("subServiceName",subServiceName);

                intent.putExtra("serviceId", serviceId);
                intent.putExtra("subServiceId", subServiceId);

                intent.putExtra("incallStaff",incallStaff);
                intent.putExtra("outcallStaff",outcallStaff);

                startActivity(intent);
            }
        });

        apiForServicesDetails();
    }

    private void apiForServicesDetails() {
        Progress.show(ArtistServiceDetailsActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ArtistServiceDetailsActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForServicesDetails();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();

        params.put("businessId", artistId);
        params.put("artistServiceId", String.valueOf(_id));
        HttpTask task = new HttpTask(new HttpTask.Builder(ArtistServiceDetailsActivity.this, "serviceStaff", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(ArtistServiceDetailsActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    detailsInfoList.clear();

                    if (status.equals("success")) {
                        Gson gson = new Gson();
                        StaffDetailsInfo services = gson.fromJson(response, StaffDetailsInfo.class);

                        tv_description.setText(services.serviceInfo.description+"");
                        detailsInfoList.addAll(services.staffInfo);

                        for (int i = 0; i < detailsInfoList.size(); i++) {

                            if(callTypeString.equals("In Call")){
                                if(detailsInfoList.get(i).inCallPrice == 0.0){
                                    detailsInfoList.remove(i);
                                }
                            }else {
                                if(detailsInfoList.get(i).outCallPrice == 0.0){
                                    detailsInfoList.remove(i);
                                }
                            }
                        }



                        adapter.notifyDataSetChanged();

                        if(detailsInfoList.size() == 1){
                            id_tv_staff_text.setVisibility(View.GONE);
                        }else  id_tv_staff_text.setVisibility(View.VISIBLE);
                    }

                } catch (Exception e) {
                    Progress.hide(ArtistServiceDetailsActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(ArtistServiceDetailsActivity.this);
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
}

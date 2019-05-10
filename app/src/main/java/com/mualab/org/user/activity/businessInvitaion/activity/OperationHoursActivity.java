package com.mualab.org.user.activity.businessInvitaion.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.businessInvitaion.adapter.CompanyOperationHoursAdapter;
import com.mualab.org.user.data.model.booking.BusinessDay;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OperationHoursActivity extends AppCompatActivity implements View.OnClickListener {
    private List<BusinessDay> businessDays;
    private boolean isEditable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_working_hours);
        init();
    }

    private void init(){

        //  businessDays =  getBusinessdays();
        Bundle args = getIntent().getBundleExtra("BUNDLE");
        businessDays = (List<BusinessDay>) args.getSerializable("workingHours");
        isEditable = args.getBoolean("isEditable", false);
        //List<BusinessDay> businessDays = companyDetail.businessDays;
        ImageView iv_back = findViewById(R.id.btnBack);
        TextView tvHeaderText = findViewById(R.id.tvHeaderTitle);
        TextView tvNoDataFound = findViewById(R.id.tvNoDataFound);
        AppCompatButton btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        tvHeaderText.setVisibility(View.VISIBLE);
        tvHeaderText.setText(getString(R.string.operation_hours));

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        assert businessDays != null;
        Collections.sort(businessDays, new Comparator<BusinessDay>() {

            @Override
            public int compare(BusinessDay a1, BusinessDay a2) {
                Long long1 = (long) a1.dayId;
                Long long2 = (long) a2.dayId;
                return long1.compareTo(long2);
              /*  if (a1.dayId == 0 || a2.dayId == 0)
                    return -1;
                else {
                    Long long1 = (long) a1.dayId;
                    Long long2 = (long) a2.dayId;
                    return long1.compareTo(long2);
                }*/
            }
        });

        if (businessDays.size() == 0)
            tvNoDataFound.setVisibility(View.VISIBLE);
        else
            tvNoDataFound.setVisibility(View.GONE);

        RecyclerView rvBusinessDay = findViewById(R.id.rvBusinessDay);
        CompanyOperationHoursAdapter adapter = new CompanyOperationHoursAdapter(OperationHoursActivity.this, businessDays, isEditable);
        rvBusinessDay.setLayoutManager(new LinearLayoutManager(OperationHoursActivity.this));
        rvBusinessDay.setAdapter(adapter);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {



        }
    }

}

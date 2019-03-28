package com.mualab.org.user.activity.businessInvitaion.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.businessInvitaion.adapter.BusinessTypeAdapter;
import com.mualab.org.user.activity.businessInvitaion.model.CompanyDetail;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class InvitationDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private CompanyDetail companyDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_detail);
        initView();
    }

    private void initView(){

        String s1 = "We invite you to join";
        String s2 = "as a staff member Accept the invitation and get start login in biz app with the same social credential";

        companyDetail = (CompanyDetail) getIntent().getSerializableExtra("companyDetail");

        //   invitationAdapter = new InvitationAdapter(InvitationDetailActivity.this, invitationList);
        ImageView ivHeaderBack = findViewById(R.id.btnBack);
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.invitation));

        TextView tvBusinessName = findViewById(R.id.tvBusinessName);
        TextView tvAddress = findViewById(R.id.tvAddress);
        TextView tvMessage = findViewById(R.id.tvMessage);

        AppCompatButton btnReject = findViewById(R.id.btnReject);
        AppCompatButton btnAccept = findViewById(R.id.btnAccept);
        ImageView ivProfile = findViewById(R.id.ivProfile);

        RecyclerView rvBizType = findViewById(R.id.rvBizType);
        LinearLayoutManager layoutManager = new LinearLayoutManager(InvitationDetailActivity.this,
                LinearLayoutManager.HORIZONTAL, false);
        rvBizType.setLayoutManager(layoutManager);

        if (companyDetail !=null) {
            BusinessTypeAdapter adapter = new BusinessTypeAdapter(InvitationDetailActivity.this,
                    companyDetail.businessType);
            rvBizType.setAdapter(adapter);

            tvBusinessName.setText(companyDetail.userName);
            tvAddress.setText(companyDetail.address);

            String textToHighlight = "<b>" + ""+companyDetail.businessName + "</b> ";

            // Construct the formatted text
            String replacedWith = "<font color='black'>" + textToHighlight + "</font>";

            // Update the TextView text
            tvMessage.setText(Html.fromHtml(s1+" "+replacedWith+" "+s2));

            if (!companyDetail.profileImage.equals("")){
                Picasso.with(InvitationDetailActivity.this).load(companyDetail.profileImage).placeholder(R.drawable.default_placeholder).
                        fit().into(ivProfile);
            }
        }


        ivHeaderBack.setOnClickListener(this);
        btnReject.setOnClickListener(this);
        btnAccept.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnAccept:
                apiForAcceptReject("accept");
                break;

            case R.id.btnReject:
                apiForAcceptReject("reject");
                break;

            case R.id.btnBack:
                finish();
                break;
        }
    }

    private void apiForAcceptReject(final String type) {
        Progress.show(InvitationDetailActivity.this);

        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(InvitationDetailActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForAcceptReject(type);
                    }
                }
            }).show();
        }

        Map<String, String> body = new HashMap<>();
        body.put("type", type);
        body.put("id", companyDetail._id);

        new HttpTask(new HttpTask.Builder(this, "invitationUpdate", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                System.out.println("response = "+response);
                Progress.hide(InvitationDetailActivity.this);
                try {
                    /*{"status":"success","otp":5386,"users":null}*/
                    JSONObject object = new JSONObject(response);
                    String status = object.getString("status");
                    String   message = object.getString("message");

                    if (status.equalsIgnoreCase("success")) {

                        Intent intent = new Intent();
                        intent.putExtra("type",type);
                        intent.putExtra("name",companyDetail.userName);
                        setResult(RESULT_OK, intent);
                        finish();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(InvitationDetailActivity.this);
                try{
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")){
                        Mualab.getInstance().getSessionManager().logout();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }})
                .setBody(body, HttpTask.ContentType.APPLICATION_JSON)
                .setAuthToken(user.authToken).setMethod(Request.Method.POST)
                .setProgress(true))
                .execute(this.getClass().getName());
    }

}

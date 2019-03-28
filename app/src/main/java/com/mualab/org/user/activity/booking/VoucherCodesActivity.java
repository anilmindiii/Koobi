package com.mualab.org.user.activity.booking;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.adapter.VoucherAdapter;
import com.mualab.org.user.activity.booking.model.BookingConfirmInfo;
import com.mualab.org.user.activity.booking.model.VoucherInfo;
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

public class VoucherCodesActivity extends AppCompatActivity {
    private RecyclerView rcv_voucher;
    private VoucherAdapter voucherAdapter;
    private String artistId;
    private List<VoucherInfo.DataBean> beanList;
    private TextView tv_msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher_codes);

        if (getIntent().getStringExtra("artistId") != null) {
            artistId = getIntent().getStringExtra("artistId");
        }

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.text_voucher_code));

        rcv_voucher = findViewById(R.id.rcv_voucher);
        tv_msg = findViewById(R.id.tv_msg);

        beanList = new ArrayList<>();
        voucherAdapter = new VoucherAdapter(this, beanList, new VoucherAdapter.getVoucher() {
            @Override
            public void Voucher(VoucherInfo.DataBean voucherItem) {
                Intent intent = new Intent();
                intent.putExtra("voucherItem",voucherItem);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        rcv_voucher.setAdapter(voucherAdapter);

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        GetVoucherCode();
    }

    private void GetVoucherCode() {
        Progress.show(VoucherCodesActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(VoucherCodesActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        GetVoucherCode();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", artistId);

        HttpTask task = new HttpTask(new HttpTask.Builder(VoucherCodesActivity.this, "voucherList", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(VoucherCodesActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    beanList.clear();

                    if (status.equals("success")) {
                        Gson gson = new Gson();
                        VoucherInfo voucherInfo = gson.fromJson(response,VoucherInfo.class);
                        beanList.addAll(voucherInfo.data);
                        tv_msg.setVisibility(View.GONE);
                    } else {
                        tv_msg.setVisibility(View.VISIBLE);
                    }

                    voucherAdapter.notifyDataSetChanged();

                } catch (Exception e) {
                    Progress.hide(VoucherCodesActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(VoucherCodesActivity.this);
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

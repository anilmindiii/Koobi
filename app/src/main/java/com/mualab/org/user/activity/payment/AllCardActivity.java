package com.mualab.org.user.activity.payment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;

import com.mualab.org.user.Views.SweetAlert.SweetAlertDialog;
import com.mualab.org.user.activity.booking.adapter.BookingHistoryDetailsAdapter;
import com.mualab.org.user.activity.booking.model.BookingListInfo;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.activity.payment.adapter.CardAdapter;
import com.mualab.org.user.activity.payment.model.StripeSaveCardResponce;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.databinding.ActivityAllCardBinding;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.constants.Constant;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.ExternalAccountCollection;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class AllCardActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityAllCardBinding binding;
    private CardAdapter cardAdapter;
    private TextView btnAdd;
    private StripeSaveCardResponce cardResponce;
    String from = "",token = "",amount = "";
    int bookingId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_all_card);

        if (getIntent().getStringExtra("from") != null) {
            from = getIntent().getStringExtra("from");
            binding.btnCOnfirmBooking.setVisibility(View.GONE);
        }else {
            bookingId = getIntent().getIntExtra("bookingId",0);
            amount = getIntent().getStringExtra("amount");
        }

        btnAdd = findViewById(R.id.btnAdd);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.payment));
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //cardAdapter = new CardAdapter();
        //recyclerView.setAdapter(cardAdapter);

        btnAdd.setOnClickListener(this);
        binding.btnCOnfirmBooking.setOnClickListener(this);
        showCreditCardInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        token = "";
        showCreditCardInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == Constant.REQUEST_CODE_CHOOSE){
                finish();
            }

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAdd:
                Intent intent = new Intent(this, PaymentCheckOutActivity.class);
                intent.putExtra("from",from);
                intent.putExtra("bookingId",bookingId);
                intent.putExtra("amount",amount);
                startActivityForResult(intent,Constant.REQUEST_CODE_CHOOSE);
                break;
            case R.id.btnCOnfirmBooking:
                if(bookingId != 0){
                    if(!TextUtils.isEmpty(token)){
                        makePayment();
                    }else MyToast.getInstance(this).showDasuAlert(getString(R.string.please_select_card));

                }
                break;
        }
    }

    // """"""""" Saved credit card api """""""" //
    @SuppressLint("StaticFieldLeak")
    private void showCreditCardInfo() {
        Progress.show(AllCardActivity.this);
        new AsyncTask<Void, Void, ExternalAccountCollection>() {
            @Override
            protected ExternalAccountCollection doInBackground(Void... voids) {

                Stripe.apiKey = "sk_test_8yF1axC0w9jPs6rlmAK3LQh1";

                ExternalAccountCollection customer = null;
                try {

                    Map<String, Object> cardParams = new HashMap<String, Object>();
                    cardParams.put("limit", 100);
                    cardParams.put("object", "card");
                    String custId = Mualab.currentUser.customerId;
                    customer = Customer.retrieve(custId).getSources().all(cardParams);

                } catch (final StripeException e) {
                    Progress.hide(AllCardActivity.this);

                    new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message message) {
                            // This is where you do your work in the UI thread.
                            // Your worker tells you in the message what to do.
                            MyToast.getInstance(AllCardActivity.this).showDasuAlert(e.getLocalizedMessage());

                        }
                    };

                }
                return customer;
            }

            @Override
            protected void onPostExecute(final ExternalAccountCollection externalAccountCollection) {
                super.onPostExecute(externalAccountCollection);
                Progress.hide(AllCardActivity.this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (externalAccountCollection != null) {

                            cardResponce = new Gson().fromJson(externalAccountCollection.toJson(), StripeSaveCardResponce.class);
                            //  Log.e("Size: ", "" + cardResponce.getData().size());

                            if (cardResponce.getData().size() != 0) {
                                /* binding.tvSelectCard.setVisibility(View.VISIBLE);
                                 binding.btnPay.setVisibility(View.VISIBLE);*/
                                binding.tvNoCardAdd.setVisibility(View.GONE);
                            } else binding.tvNoCardAdd.setVisibility(View.VISIBLE);

                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(AllCardActivity.this);
                            cardAdapter = new CardAdapter(from,AllCardActivity.this, cardResponce.getData(), new CardAdapter.getValue() {
                                @Override
                                public void getCardData(StripeSaveCardResponce.DataBean dataBean) {
                                    token = dataBean.getId();
                                }
                            });


                            /* new CreditCardAdapter.CardDetailInterface() {



                     //"""""""""" click on holo circle img and then show card details """"""""""//
                                @Override
                                public void moreDetailOnClick(int pos, boolean value) {
                                    for (int j = 0; j < cardResponce.getData().size(); j++) {
                                        if (j == pos) {
                                            cardResponce.getData().get(pos).setMoreDetail(value);
                                            cardId = cardResponce.getData().get(pos).getId();
                                        } else {
                                            cardResponce.getData().get(j).setMoreDetail(true);
                                        }
                                    }
                                    cardAdapter.notifyDataSetChanged();
                                }


                                //"""""""""" user want to delete saved card """"""""""""//
                                @Override
                                public void deleteSaveCard(int pos, final String customerId) {

                                    removedSaveCardApi(customerId);

                                }
                            });*/
                            binding.recyclerView.setLayoutManager(layoutManager);
                            binding.recyclerView.setAdapter(cardAdapter);

                        }

                    }
                });

            }

        }.execute();
    }


    //server api for sending token for payment
    private void makePayment() {
        Progress.show(AllCardActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(AllCardActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        makePayment();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(bookingId));
        params.put("token", token);
        params.put("sourceType", "card");
        params.put("customerId", String.valueOf(Mualab.currentUser.customerId));


        HttpTask task = new HttpTask(new HttpTask.Builder(AllCardActivity.this, "cardPayment", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(AllCardActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equals("success")) {
                        dialogNew();
                    } else {
                        MyToast.getInstance(AllCardActivity.this).showDasuAlert(message);
                    }


                } catch (Exception e) {
                    Progress.hide(AllCardActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(AllCardActivity.this);
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

    private void dialogNew() {
        SweetAlertDialog dialog = new SweetAlertDialog(AllCardActivity.this,
                SweetAlertDialog.SUCCESS_TYPE);

        dialog.setTitleText("Payment Successful !");
        dialog.setContentText("Thank You! Your payment has been received.");
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();
               finish();
            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}

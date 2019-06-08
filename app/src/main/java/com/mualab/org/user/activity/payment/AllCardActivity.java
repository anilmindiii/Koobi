package com.mualab.org.user.activity.payment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
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
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;

import com.mualab.org.user.Views.SweetAlert.SweetAlertDialog;
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
import java.util.List;
import java.util.Map;


public class AllCardActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityAllCardBinding binding;
    private CardAdapter cardAdapter;
    ImageView ic_add_chat;
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

        ic_add_chat = findViewById(R.id.ic_add_chat);
        ic_add_chat.setVisibility(View.VISIBLE);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.payment_info));
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //cardAdapter = new CardAdapter();
        //recyclerView.setAdapter(cardAdapter);

        ic_add_chat.setOnClickListener(this);
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
            case R.id.ic_add_chat:
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

                                @Override
                                public void getCardDataForDetele(StripeSaveCardResponce.DataBean dataBean, List<StripeSaveCardResponce.DataBean> dataList, int pos) {
                                    confirmDialog(dataBean.getId(),dataList,pos);
                                }

                                @Override
                                public void makeDefaultcard(StripeSaveCardResponce.DataBean dataBean) {
                                    makeDefaultcardApi(dataBean.getId());
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

    // Remove card case
    private void confirmDialog(final String cardId, List<StripeSaveCardResponce.DataBean> dataList, int pos) {
        Session session = new Session(AllCardActivity.this);
        User user = session.getUser();
        final Dialog dialog = new Dialog(AllCardActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_delete_chat);
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView tv_delete_alert = dialog.findViewById(R.id.tv_delete_alert);
        tv_delete_alert.setText("Are you sure you want to delete this card?");
        TextView title = dialog.findViewById(R.id.tv_title);
        title.setText("Alert !");
        dialog.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                removedSaveCardApi(cardId);

                if(dataList.size()>1){
                    if(user.cardId.equals(cardId)){
                        if(dataList.size() == (pos+1)){ //case of last index
                            makeDefaultcardApi(dataList.get(pos-1).getId());
                        }else makeDefaultcardApi(dataList.get(pos+1).getId());

                    }


                }
            }
        });

        Button btn_no = dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    //""""""""""  Remove Saved credit card """"""""""""//
    @SuppressLint("StaticFieldLeak")
    private void removedSaveCardApi(final String cardId) {
        Progress.show(AllCardActivity.this);
        new AsyncTask<Void, Void, Customer>() {
            @Override
            protected Customer doInBackground(Void... voids) {
                Stripe.apiKey = "sk_test_8yF1axC0w9jPs6rlmAK3LQh1";

                Customer customer = null;
                try {
                    String custId = Mualab.currentUser.customerId;
                    customer = Customer.retrieve(custId);
                    customer.getSources().retrieve(cardId).delete();
                } catch (StripeException e) {
                    e.printStackTrace();
                    Progress.hide(AllCardActivity.this);
                    MyToast.getInstance(AllCardActivity.this).showDasuAlert(e.getLocalizedMessage());
                }
                return customer;
            }

            @Override
            protected void onPostExecute(Customer customer) {
                super.onPostExecute(customer);
                Progress.hide(AllCardActivity.this);
                if (customer != null) {
                    showCreditCardInfo();

                }
            }
        }.execute();
    }

    // make default card
    private void makeDefaultcardApi(String cardId) {
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(AllCardActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        makeDefaultcardApi(cardId);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("cardId", cardId);

        HttpTask task = new HttpTask(new HttpTask.Builder(AllCardActivity.this, "updateRecord", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        user.cardId = cardId;
                        session.createSession(user);

                    } else {
                        //MyToast.getInstance(mContext).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    //Progress.hide(mContext);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
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
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }
}

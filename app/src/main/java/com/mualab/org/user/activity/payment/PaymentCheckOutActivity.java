package com.mualab.org.user.activity.payment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.Views.SweetAlert.SweetAlertDialog;
import com.mualab.org.user.activity.booking.BookingConfirmActivity;
import com.mualab.org.user.activity.booking.BookingDetailsActivity;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.activity.payment.model.StripeSaveCardResponce;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.databinding.ActivityPaymentCheckOutBinding;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.constants.Constant;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Token;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class PaymentCheckOutActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityPaymentCheckOutBinding binding;
    private int width;
    private int year1;
    private int month1;
    private String from = "";
    private int bookingId = 0;
    private long mLastClickTime = 0;
    private String amount;

    PaymentCheckOutActivity  activity;
    private StripeSaveCardResponce.DataBean card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment_check_out);

        if (getIntent().getStringExtra("from") != null &&
                !getIntent().getStringExtra("from").equals("")) {
            from = getIntent().getStringExtra("from");
            binding.btnSubmit.setVisibility(View.GONE);
            binding.lyCheckBox.setVisibility(View.GONE);
            binding.btnSaveCard.setVisibility(View.VISIBLE);

            binding.textMaount.setVisibility(View.GONE);
            binding.tvAmount.setVisibility(View.GONE);

        }else {
            bookingId = getIntent().getIntExtra("bookingId",0);
            amount = getIntent().getStringExtra("amount");
            binding.btnSubmit.setVisibility(View.VISIBLE);
            binding.btnSaveCard.setVisibility(View.GONE);
            binding.lyCheckBox.setVisibility(View.VISIBLE);

            binding.textMaount.setVisibility(View.VISIBLE);
            binding.tvAmount.setVisibility(View.VISIBLE);
            binding.tvAmount.setText(amount);
        }

        this.activity = PaymentCheckOutActivity.this;

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        width = displaymetrics.widthPixels;
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);

        if (from.equals("profile")){
            tvHeaderTitle.setText(getString(R.string.add_debit_credit_card));
        }else if(from.equals("details")){
            tvHeaderTitle.setText(getString(R.string.card_details));
            card = getIntent().getParcelableExtra("card");
            binding.cardNum.setText("XXXX XXXX XXXX "+card.getLast4());
            binding.cardNum.setEnabled(false);

            binding.tvDate.setText(card.getExp_month()+"/"+card.getExp_year());
            binding.tvDate.setEnabled(false);

            binding.edCvv.setText("****");
            binding.edCvv.setEnabled(false);

            binding.btnSaveCard.setText(getString(R.string.remove_card));

        }
        else tvHeaderTitle.setText(getString(R.string.payment_checkout));

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMonthYearDialog();
            }
        });

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(payValidations()){
                   // mis-clicking prevention, using threshold of 5000 ms
                   if (SystemClock.elapsedRealtime() - mLastClickTime < 5000){
                       return;
                   }
                   mLastClickTime = SystemClock.elapsedRealtime();


                       String cardNumber = binding.cardNum.getText().toString().trim();
                       if(bookingId != 0)
                           saveCardAndPay(cardNumber, month1, year1, binding.edCvv.getText().toString().trim());





               }
            }
        });

        binding.btnSaveCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btnName = binding.btnSaveCard.getText().toString().trim();
                if(btnName.equals("Remove Card")){
                    confirmDialog(card.getId());

                }else {
                    if(payValidations()){
                        String cardNumber = binding.cardNum.getText().toString().trim();
                        createStripeToken(cardNumber, month1, year1, binding.edCvv.getText().toString().trim());
                    }
                }



            }
        });
    }

    public boolean isEmpty(TextView textView) {
        if (textView.getText().toString().trim().isEmpty()) {
            textView.requestFocus();
            return true;
        }
        return false;
    }

    private boolean payValidations() {
        if (isEmpty(binding.cardNum)) {
            MyToast.getInstance(PaymentCheckOutActivity.this).showDasuAlert(getString(R.string.please_enter_card_number));
            return false;
        } else if (isEmpty(binding.tvDate)) {
            MyToast.getInstance(PaymentCheckOutActivity.this).showDasuAlert(getString(R.string.please_select_mm_yy));
            return false;
        } else if (isEmpty(binding.edCvv)) {
            MyToast.getInstance(PaymentCheckOutActivity.this).showDasuAlert(getString(R.string.please_enter_cvv));
            return false;
        }
        else if(binding.edCvv.getText().length() != 3){
            MyToast.getInstance(PaymentCheckOutActivity.this).showDasuAlert(getString(R.string.please_enter_valid_cvv));
            return false;
        }
        else return true;
    }

    @SuppressLint("StaticFieldLeak")
    private void saveCardAndPay(final String cardNumber, final int month1, final int year1, final String cvv) {
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(PaymentCheckOutActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        saveCardAndPay(cardNumber, month1, year1, binding.edCvv.getText().toString().trim());

                    }
                }
            }).show();
        }

        Progress.show(PaymentCheckOutActivity.this);

        new AsyncTask<Void, Void, Token>() {
            @Override
            protected Token doInBackground(Void... voids) {
                Stripe.apiKey = "sk_test_8yF1axC0w9jPs6rlmAK3LQh1";
                Token token = null;
                Map<String, Object> tokenParams = new HashMap<String, Object>();
                Map<String, Object> cardParams = new HashMap<String, Object>();
                cardParams.put("number", cardNumber);
                cardParams.put("exp_month", month1);
                cardParams.put("exp_year", year1);
                cardParams.put("cvc", cvv);
                tokenParams.put("card", cardParams);

                try {
                    token = Token.create(tokenParams);

                } catch (final StripeException e) {
                    Progress.hide(PaymentCheckOutActivity.this);
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            MyToast.getInstance(activity).showDasuAlert(e.getLocalizedMessage());
                        }
                    });
                }
                return token;
            }

            @Override
            protected void onPostExecute(final Token token) {
                super.onPostExecute(token);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Progress.hide(PaymentCheckOutActivity.this);
                        if (token != null /*&& binding.cbSaveCard.isChecked()*/) {
                            if (binding.cbSaveCard.isChecked()) {
                                    createStripeToken(cardNumber, month1,
                                            year1, binding.edCvv.getText().toString().trim());
                                    // saveCreditCard(token.getId());
                            }
                            makePayment(token.getId());

                        }
                    }
                });
            }
        }.execute();
    }


    @SuppressLint("StaticFieldLeak")
    private void createStripeToken(final String cardNumber, final int month1, final int year1, final String cvv) {

        Progress.show(PaymentCheckOutActivity.this);

            new AsyncTask<Void, Void, com.stripe.model.Token>() {
                @Override
                protected com.stripe.model.Token doInBackground(Void... voids) {
                    com.stripe.Stripe.apiKey = "sk_test_8yF1axC0w9jPs6rlmAK3LQh1";
                    com.stripe.model.Token token = null;
                    Map<String, Object> tokenParams = new HashMap<String, Object>();
                    Map<String, Object> cardParams = new HashMap<String, Object>();
                    cardParams.put("number", cardNumber);
                    cardParams.put("exp_month", month1);
                    cardParams.put("exp_year", year1);
                    cardParams.put("cvc", cvv);
                    tokenParams.put("card", cardParams);

                    try {
                        token = com.stripe.model.Token.create(tokenParams);

                    } catch (final StripeException e) {
                        Progress.hide(PaymentCheckOutActivity.this);
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                MyToast.getInstance(activity).showDasuAlert(e.getLocalizedMessage());
                            }
                        });
                    }
                    return token;
                }

                @Override
                protected void onPostExecute(final com.stripe.model.Token token) {
                    super.onPostExecute(token);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Progress.hide(PaymentCheckOutActivity.this);
                            if (token != null) {
                                saveCreditCard(token.getId());
                            }
                        }
                    });
                }
            }.execute();

    }

    @SuppressLint("StaticFieldLeak")
    private void saveCreditCard(final String id) {
        Progress.show(PaymentCheckOutActivity.this);
        new AsyncTask<Void, Void, Customer>() {
            @Override
            protected Customer doInBackground(Void... voids) {
                Stripe.apiKey = "sk_test_8yF1axC0w9jPs6rlmAK3LQh1";

                Customer customer = null;
                try {
                    String custId = Mualab.currentUser.customerId;
                    // here we provide customern Id
                    customer = Customer.retrieve(custId);
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("source", id);
                    customer.getSources().create(params);

                } catch (final StripeException e) {
                    e.printStackTrace();
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            MyToast.getInstance(activity).showDasuAlert(e.getLocalizedMessage());
                        }
                    });                }
                return customer;
            }

            @Override
            protected void onPostExecute(Customer customer) {
                super.onPostExecute(customer);
                Progress.hide(PaymentCheckOutActivity.this);
                if (customer != null) {
                    if (from.equals("profile")){
                        onBackPressed();
                    }
                    // Toast.makeText(CreditCardActivity.this, "Your card is saved successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    MyToast.getInstance(PaymentCheckOutActivity.this).showDasuAlert("Stripe Error");

                }
            }
        }.execute();

    }

    //*************show  MonthYear  Dialog *******************//
    private void showMonthYearDialog() {
        final int year = Calendar.getInstance().get(Calendar.YEAR);
        final int month = Calendar.getInstance().get(Calendar.MONTH);
        final Dialog yearDialog = new Dialog(this);
        yearDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        yearDialog.setContentView(R.layout.dialog_month_year);
        yearDialog.getWindow().setLayout((width * 10) / 11, WindowManager.LayoutParams.WRAP_CONTENT);
        //yearDialog.getWindow().getAttributes().windowAnimations = R.style.CustomDialog;
        Button set = yearDialog.findViewById(R.id.button1);
        Button cancel = yearDialog.findViewById(R.id.button2);
        final NumberPicker yearPicker = yearDialog.findViewById(R.id.numberPicker1);
        final NumberPicker monthPicker = yearDialog.findViewById(R.id.numberPicker2);

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setWrapSelectorWheel(false);
        monthPicker.setValue(month);
        monthPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);


        yearPicker.setMaxValue(2050);
        yearPicker.setMinValue(year);
        yearPicker.setWrapSelectorWheel(false);
        yearPicker.setValue(year);
        yearPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String year = String.valueOf(yearPicker.getValue());
                year1 = yearPicker.getValue();
                month1 = monthPicker.getValue();
                //  year = year.substring(2);
                binding.tvDate.setText(monthPicker.getValue() + "/" + year);

                yearDialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yearDialog.dismiss();
            }
        });
        yearDialog.show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_date:
                showMonthYearDialog();
                break;
        }
    }

        //server api for sending token for payment
    private void makePayment(final String token) {
        Progress.show(PaymentCheckOutActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(PaymentCheckOutActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        makePayment(token);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(bookingId));
        params.put("token", token);
        params.put("sourceType", "");
        params.put("customerId", String.valueOf(Mualab.currentUser.customerId));


        HttpTask task = new HttpTask(new HttpTask.Builder(PaymentCheckOutActivity.this, "cardPayment", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(PaymentCheckOutActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equals("success")) {
                        dialogNew();
                    } else {
                        MyToast.getInstance(PaymentCheckOutActivity.this).showDasuAlert(message);
                    }


                } catch (Exception e) {
                    Progress.hide(PaymentCheckOutActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(PaymentCheckOutActivity.this);
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

    //""""""""""  Remove Saved credit card """"""""""""//
    @SuppressLint("StaticFieldLeak")
    private void removedSaveCardApi(final String cardId) {
        Progress.show(PaymentCheckOutActivity.this);
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
                    Progress.hide(PaymentCheckOutActivity.this);
                    MyToast.getInstance(PaymentCheckOutActivity.this).showDasuAlert(e.getLocalizedMessage());
                }
                return customer;
            }

            @Override
            protected void onPostExecute(Customer customer) {
                super.onPostExecute(customer);
                Progress.hide(PaymentCheckOutActivity.this);
                if (customer != null) {
                    finish();

                }
            }
        }.execute();
    }



    private void dialogNew() {
        SweetAlertDialog dialog = new SweetAlertDialog(PaymentCheckOutActivity.this, SweetAlertDialog.SUCCESS_TYPE);

        dialog.setTitleText("Payment Successful !");
        dialog.setContentText("Thank You! Your payment has been received.");
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();
               setResult(RESULT_OK);
               finish();
            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void confirmDialog(final String cardId){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(PaymentCheckOutActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(PaymentCheckOutActivity.this);
        }

        builder.setCancelable(false);
        builder.setTitle("Alert !")
                .setMessage("Are you sure you want to delete this card?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        removedSaveCardApi(cardId);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

}

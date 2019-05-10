package com.mualab.org.user.activity.booking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.Views.SweetAlert.SweetAlertDialog;
import com.mualab.org.user.activity.authentication.AddAddressActivity;
import com.mualab.org.user.activity.booking.adapter.ConfirmServiceAdapter;
import com.mualab.org.user.activity.booking.model.BookingConfirmInfo;
import com.mualab.org.user.activity.booking.model.VoucherInfo;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.model.booking.Address;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.constants.Constant;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class BookingConfirmActivity extends AppCompatActivity {
    private RecyclerView rcv_service;
    private ConfirmServiceAdapter adapter;
    private String artistId;
    private List<BookingConfirmInfo.DataBean> bookingList;
    private boolean isBankAdded, isOutCallSelected;
    private RadioGroup radioGroup;
    private RadioButton rb_case, rb_online;
    private RelativeLayout ly_location;
    private TextView tv_address, tv_apply, tv_new_amount, tv_amount;
    private EditText ed_vouchar_code;
    private ImageView iv_voucher_arrow;
    private double total_price = 0.0;
    private FrameLayout ly_amount;
    private JSONObject voucher;
    private String bookingType = "1", paymentType = "2", discountPrice = "", bookingDate = "", bookingTime = "";
    private AppCompatButton btn_confirm_booking, brn_add_more;
    private ImageView iv_location_arrow, iv_voucher_cancel;
    private TextView tv_call_type;
    private String radius = "", artistLat = "0.0", artistLng = "0.0";
    private Double commisiion = 0.0;
    //private SoftKeyboard softKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirm);

        rcv_service = findViewById(R.id.rcv_service);
        radioGroup = findViewById(R.id.radioGroup);
        rb_case = findViewById(R.id.rb_case);
        rb_online = findViewById(R.id.rb_online);
        ly_location = findViewById(R.id.ly_location);
        tv_address = findViewById(R.id.tv_address);
        tv_apply = findViewById(R.id.tv_apply);
        ed_vouchar_code = findViewById(R.id.ed_vouchar_code);
        ed_vouchar_code.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        iv_voucher_arrow = findViewById(R.id.iv_voucher_arrow);
        tv_new_amount = findViewById(R.id.tv_new_amount);
        tv_amount = findViewById(R.id.tv_amount);
        ly_amount = findViewById(R.id.ly_amount);
        btn_confirm_booking = findViewById(R.id.btn_confirm_booking);
        iv_location_arrow = findViewById(R.id.iv_location_arrow);
        iv_voucher_cancel = findViewById(R.id.iv_voucher_cancel);
        brn_add_more = findViewById(R.id.brn_add_more);
        tv_call_type = findViewById(R.id.tv_call_type);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.booking_confirm));

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        bookingList = new ArrayList<>();
        adapter = new ConfirmServiceAdapter(this, bookingList, new ConfirmServiceAdapter.getValue() {
            @Override
            public void deleteService(int bookingId, int position) {
                deleteBookService(String.valueOf(bookingId), position);
            }

            @Override
            public void editService(BookingConfirmInfo.DataBean dataBean) {
                updateTimer();
                Intent intent = new Intent();

                intent.putExtra("_id", dataBean.artistServiceId);
                intent.putExtra("artistId", artistId);

                if (isOutCallSelected) {
                    intent.putExtra("callType", "Out Call");
                    intent.putExtra("outcallStaff", true);
                    intent.putExtra("incallStaff", false);
                } else {
                    intent.putExtra("callType", "In Call");
                    intent.putExtra("outcallStaff", false);
                    intent.putExtra("incallStaff", true);
                }


                intent.putExtra("serviceId", dataBean.serviceId);
                intent.putExtra("subServiceId", dataBean.subServiceId);

                intent.putExtra("staffId", dataBean.staffId);
                intent.putExtra("startTime", dataBean.startTime);
                intent.putExtra("bookingDate", dataBean.bookingDate);//28/01/2019

                intent.putExtra("mainServiceName", "");// should be empty
                intent.putExtra("subServiceName", dataBean.artistServiceName);


                int day = getDayFromDate(dataBean.bookingDate);
                intent.putExtra("dayId", day);

                setResult(-2, intent);
                finish();
            }
        });
        rcv_service.setAdapter(adapter);

        if (getIntent().getStringExtra("artistId") != null) {
            artistId = getIntent().getStringExtra("artistId");
            isBankAdded = getIntent().getBooleanExtra("isBankAdded", false);

            radius = getIntent().getStringExtra("radius");
            artistLat = getIntent().getStringExtra("artistLat");
            artistLng = getIntent().getStringExtra("artistLng");

            isOutCallSelected = getIntent().getBooleanExtra("isOutCallSelected", false);

            if (isOutCallSelected) {
                bookingType = "2";
                iv_location_arrow.setVisibility(View.VISIBLE);
                tv_call_type.setText("Out Call");
            } else {
                bookingType = "1";
                iv_location_arrow.setVisibility(View.GONE);
                tv_call_type.setText("In Call");
            }
        }

        if (!isBankAdded) {
            rb_case.setChecked(true);
            rb_online.setVisibility(View.GONE);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_case:
                        paymentType = "2";
                        break;

                    case R.id.rb_online:
                        paymentType = "1";
                        break;

                }

            }
        });

        ly_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookingConfirmActivity.this, AddAddressActivity.class);
                startActivityForResult(intent, 1001);
            }
        });

        tv_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardUtil.hideKeyboard(v, BookingConfirmActivity.this);
                if (!tv_apply.getText().toString().equals("Applied")) {
                    String voucherCode = ed_vouchar_code.getText().toString().trim();
                    if (!TextUtils.isEmpty(voucherCode)) {
                        applyVoucherCode(artistId, voucherCode);
                    } else
                        MyToast.getInstance(BookingConfirmActivity.this).showDasuAlert("Please enter voucher code");
                }

            }
        });

        iv_voucher_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookingConfirmActivity.this, VoucherCodesActivity.class);
                intent.putExtra("artistId", artistId);
                startActivityForResult(intent, Constant.REQUEST_CODE_CHOOSE);
            }
        });

        iv_voucher_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ed_vouchar_code.setText("");
                iv_voucher_arrow.setVisibility(View.VISIBLE);
                iv_voucher_cancel.setVisibility(View.INVISIBLE);

                voucher = null;

                tv_new_amount.setText("£" + total_price);

                tv_new_amount.setVisibility(View.VISIBLE);
                tv_amount.setVisibility(View.GONE);
                discountPrice = String.valueOf(total_price);
                tv_apply.setText("Apply");
                tv_apply.setTextColor(ContextCompat.getColor(BookingConfirmActivity.this, R.color.colorPrimaryDark));
            }
        });

        btn_confirm_booking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmBooking();
            }
        });

        brn_add_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        /*ScrollView main_scroll_view = findViewById(R.id.main_scroll_view);
        RelativeLayout mainLayout = findViewById(R.id.main_layout); // You must use your root layout
        InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);

        softKeyboard = new SoftKeyboard(mainLayout, im);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged()
        {

            @Override
            public void onSoftKeyboardHide()
            {
                // Code here
            }

            @Override
            public void onSoftKeyboardShow()
            {
                main_scroll_view.fullScroll(ScrollView.FOCUS_DOWN);
                main_scroll_view.smoothScrollTo(0, 0);
            }
        });*/

        GetServices(artistId);
        startTimer();

        apiForComission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001) {
            if (data != null)
                if (data.getSerializableExtra("address") != null) {
                    Address address = (Address) data.getSerializableExtra("address");
                    artistLat = address.latitude;
                    artistLng = address.longitude;
                    tv_address.setVisibility(View.VISIBLE);
                    tv_address.setText(String.format("%s",
                            TextUtils.isEmpty(address.placeName) ? address.stAddress1 : address.placeName));
                }

        }

        if (requestCode == Constant.REQUEST_CODE_CHOOSE) {
            if (data != null) {
                if (data.getParcelableExtra("voucherItem") != null) {
                    VoucherInfo.DataBean voucherItem = data.getParcelableExtra("voucherItem");

                    ed_vouchar_code.setText(voucherItem.voucherCode);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        updateTimer();
        Intent intent = new Intent();
        intent.putExtra("isOutCallSelected", isOutCallSelected);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    private void GetServices(final String artistId) {
        Progress.show(BookingConfirmActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingConfirmActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        GetServices(artistId);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", artistId);

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingConfirmActivity.this, "getBookedServices", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingConfirmActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    bookingList.clear();
                    if (status.equals("success")) {
                        Gson gson = new Gson();
                        BookingConfirmInfo confirmInfo = gson.fromJson(response, BookingConfirmInfo.class);
                        bookingList.addAll(confirmInfo.data);

                        if (isOutCallSelected) {
                            tv_address.setText(Mualab.currentUser.address + "");
                            ly_location.setEnabled(true);
                        } else {
                            ly_location.setEnabled(false);
                            if (bookingList.size() > 0)
                                tv_address.setText(bookingList.get(0).companyAddress);
                        }

                        if (bookingList.size() > 0) {
                            bookingDate = bookingList.get(0).bookingDate;
                            bookingTime = bookingList.get(0).startTime;

                        }


                        total_price = 0.0;
                        for (BookingConfirmInfo.DataBean bean : bookingList) {
                            total_price = Double.parseDouble(bean.bookingPrice) + total_price;
                        }
                        tv_new_amount.setText("£" + total_price + "");

                    } else {

                    }

                    adapter.notifyDataSetChanged();

                } catch (Exception e) {
                    Progress.hide(BookingConfirmActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingConfirmActivity.this);
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

    private void applyVoucherCode(final String artistId, final String voucherCode) {
        Progress.show(BookingConfirmActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingConfirmActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        applyVoucherCode(artistId, voucherCode);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", artistId);
        params.put("voucherCode", voucherCode);

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingConfirmActivity.this, "applyVoucher", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingConfirmActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");


                    if (status.equals("success")) {
                        Gson gson = new Gson();

                        JSONObject object = new JSONObject(response);
                        JSONObject data = object.getJSONObject("data");
                        voucher = data;

                        VoucherInfo.DataBean voucherItem = gson.fromJson(data.toString(), VoucherInfo.DataBean.class);

                        tv_apply.setText("Applied");
                        tv_apply.setTextColor(ContextCompat.getColor(BookingConfirmActivity.this, R.color.green_color));

                        ly_amount.setVisibility(View.VISIBLE);
                        tv_amount.setVisibility(View.VISIBLE);
                        tv_amount.setText("£" + total_price + "");
                        tv_amount.setTextColor(ContextCompat.getColor(BookingConfirmActivity.this, R.color.red));

                        if (voucherItem.discountType.equals("1")) {// for fix amount
                            Double newDiscountedPrice = (total_price - (Double.parseDouble(voucherItem.amount)));

                            if (newDiscountedPrice < 0.0) {
                                // negative
                                discountPrice = String.valueOf(0.0);
                            } else {
                                // it's a positive
                                discountPrice = String.valueOf(newDiscountedPrice);
                            }
                            String roundValue = String.format("%.2f", Double.parseDouble(discountPrice));
                            tv_new_amount.setText("£" + roundValue + "");

                        } else if (voucherItem.discountType.equals("2")) {// for % percentage
                            Double newDiscountedPrice = ((total_price * (Double.parseDouble(voucherItem.amount))) / 100);

                            newDiscountedPrice = (total_price - newDiscountedPrice);
                            if (newDiscountedPrice < 0.0) {
                                // negative
                                discountPrice = String.valueOf(0.0);
                            } else {
                                // it's a positive
                                discountPrice = String.valueOf(newDiscountedPrice);
                            }
                            String roundValue = String.format("%.2f", Double.parseDouble(discountPrice));
                            tv_new_amount.setText("£" + roundValue + "");
                        }

                        iv_voucher_arrow.setVisibility(View.INVISIBLE);
                        iv_voucher_cancel.setVisibility(View.VISIBLE);


                    } else {
                        MyToast.getInstance(BookingConfirmActivity.this).showDasuAlert(message);
                    }


                } catch (Exception e) {
                    Progress.hide(BookingConfirmActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingConfirmActivity.this);
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

    private void deleteBookService(final String bookingId, final int position) {
        Progress.show(BookingConfirmActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingConfirmActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        deleteBookService(bookingId, position);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("bookingId", bookingId);


        HttpTask task = new HttpTask(new HttpTask.Builder(BookingConfirmActivity.this, "deleteBookService", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingConfirmActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equals("success")) {
                        bookingList.remove(position);
                        if (bookingList.size() == 0) {
                            onBackPressed();
                            return;
                        } else {
                            GetServices(artistId);
                        }


                    } else {
                        MyToast.getInstance(BookingConfirmActivity.this).showDasuAlert(message);
                    }


                } catch (Exception e) {
                    Progress.hide(BookingConfirmActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingConfirmActivity.this);
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

    private void confirmBooking() {
        stopTimer();
        // check service availble or not particular location
        if (isOutCallSelected) {

            Location startPoint = new Location("locationA");
            startPoint.setLatitude(Mualab.currentLocation.lat);
            startPoint.setLongitude(Mualab.currentLocation.lng);

            Location endPoint = new Location("locationA");
            endPoint.setLatitude(Double.parseDouble(artistLat));
            endPoint.setLongitude(Double.parseDouble(artistLng));
            double distance = startPoint.distanceTo(endPoint);
            distance = (distance / 1609.344);

            if (Double.parseDouble(radius) <= distance) {
                MyToast.getInstance(BookingConfirmActivity.this).showDasuAlert("Selected artist services is not available at this location");
                return;
            }
        }

        Progress.show(BookingConfirmActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingConfirmActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        confirmBooking();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", artistId);
        params.put("bookingDate", bookingDate);
        params.put("bookingTime", bookingTime);
        params.put("location", tv_address.getText().toString().trim());

        params.put("paymentType", paymentType);
        params.put("bookingType", bookingType);

        params.put("latitude", artistLat);
        params.put("longitude", artistLng);

        if (bookingList.size() != 0){
            params.put("bookingSetting", String.valueOf(bookingList.get(0).bookingSetting));
        }else   params.put("bookingSetting", String.valueOf(0));

        if (voucher != null) {
            params.put("voucher", voucher.toString());
        }
        double v;
        v = ((total_price * commisiion) / 100);

        if (!discountPrice.equals("")) {
            double disCountPrice = Double.parseDouble(discountPrice);
            discountPrice = String.valueOf((disCountPrice + v));
            discountPrice = String.format("%.2f", Double.parseDouble(discountPrice));
        } else {
            // total amount
            total_price = (total_price + v);
            total_price = Double.parseDouble(String.format("%.2f", total_price));
        }

        params.put("discountPrice", discountPrice);
        params.put("totalPrice", String.valueOf(total_price));

        params.put("adminAmount", String.valueOf(v));
        params.put("adminCommision", String.valueOf(commisiion));

     /*   Map<String, JSONObject> paramsobj = new HashMap<>();
        paramsobj.put("voucher", voucher);*/
        HttpTask task = new HttpTask(new HttpTask.Builder(BookingConfirmActivity.this, "confirmBooking", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingConfirmActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    String bookingId = js.getString("bookingId");

                    if (status.equals("success")) {
                        SweetAlertDialog dialog = new SweetAlertDialog(BookingConfirmActivity.this, SweetAlertDialog.SUCCESS_TYPE);

                        dialog.setTitleText("Congratulation!");
                        dialog.setContentText("Your booking request has been successfully sent to " + bookingList.get(0).artistName + " for confirmation. Click here to see the status of your booking");
                        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                Intent showLogin = new Intent(BookingConfirmActivity.this, MainActivity.class);
                                showLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                showLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(showLogin);

                                showLogin = new Intent(BookingConfirmActivity.this, BookingDetailsActivity.class);
                                showLogin.putExtra("bookingId", Integer.parseInt(bookingId));
                                showLogin.putExtra("shouldPopupOpen", false);
                                startActivity(showLogin);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                            }
                        });

                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                    } else {
                        MyToast.getInstance(BookingConfirmActivity.this).showDasuAlert(message);
                    }


                } catch (Exception e) {
                    Progress.hide(BookingConfirmActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingConfirmActivity.this);
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
                //.setBodyJson(paramsobj, HttpTask.ContentType.APPLICATION_JSON)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(this.getClass().getName());
    }

    private int getDayFromDate(String date) {
        String input_date = date;
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        Date dt1 = null;
        try {
            dt1 = format1.parse(input_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat format2 = new SimpleDateFormat("EEEE");
        String finalDay = format2.format(dt1);

        return getDayByName(finalDay);
    }

    private int getDayByName(String dayName) {
        int dayId = 0;
        switch (dayName) {
            case "Sunday":
                dayId = 6;
                break;

            case "Monday":
                dayId = 0;
                break;

            case "Tuesday":
                dayId = 1;
                break;

            case "Wednesday":
                dayId = 2;
                break;

            case "Thursday":
                dayId = 3;
                break;

            case "Friday":
                dayId = 4;
                break;

            case "Saturday":
                dayId = 5;
                break;
        }
        return dayId;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //softKeyboard.unRegisterSoftKeyboardCallback();
        stopTimer();
    }

    public static void stopTimer() {
        if (Mualab.getInstance().getMyTimer() != null) {
            Mualab.getInstance().getMyTimer().cancel();
            Mualab.getInstance().getMyTimer().purge();
        }
    }

    public void updateTimer() {
        stopTimer();
        startTimer();
    }

    public void startTimer() {
        Mualab.getInstance().myTimer = new Timer();
        if (Mualab.getInstance().getMyTimer() != null) {
            Mualab.getInstance().getMyTimer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(Timer_Tick);
                }

            }, 300000);
        }

    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            dialogNew();
        }
    };

    private void dialogNew() {
        final AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext())
                .setTitle("Alert")
                .setMessage("Booking session has been expired. Please try again.")
                .setCancelable(false)
                .create();
        alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent dialogIntent = new Intent(getApplicationContext(), MainActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                dialogIntent.putExtra("type", "LaunchAppAlarm");
                getApplicationContext().startActivity(dialogIntent);
                alertDialog.dismiss();
                apiForRemoveAllBooking();
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }

        alertDialog.show();
    }

    private void apiForRemoveAllBooking() {
        Progress.show(BookingConfirmActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingConfirmActivity.this, new NoConnectionDialog.Listner() {
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


        HttpTask task = new HttpTask(new HttpTask.Builder(BookingConfirmActivity.this, "deleteUserBookService", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingConfirmActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equals("success")) {

                    } else {
                        MyToast.getInstance(BookingConfirmActivity.this).showDasuAlert(message);
                    }

                } catch (Exception e) {
                    Progress.hide(BookingConfirmActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingConfirmActivity.this);
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


    private void apiForComission() {
        Progress.show(BookingConfirmActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingConfirmActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForComission();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", artistId);


        HttpTask task = new HttpTask(new HttpTask.Builder(BookingConfirmActivity.this, "adminCommision", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingConfirmActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    String adminCommision = js.getString("adminCommision");
                    if (status.equals("success")) {
                        if (!adminCommision.equals("")) {
                            commisiion = Double.valueOf(adminCommision);
                        }
                    } else {
                        MyToast.getInstance(BookingConfirmActivity.this).showDasuAlert(message);
                    }

                } catch (Exception e) {
                    Progress.hide(BookingConfirmActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingConfirmActivity.this);
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


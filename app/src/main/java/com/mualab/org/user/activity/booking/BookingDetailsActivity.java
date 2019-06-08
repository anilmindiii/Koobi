package com.mualab.org.user.activity.booking;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.booking.adapter.BookingHistoryDetailsAdapter;
import com.mualab.org.user.activity.booking.dialog.BookingReviewRatingCallback;
import com.mualab.org.user.activity.booking.dialog.BookingReviewRatingDialog;
import com.mualab.org.user.activity.booking.model.BookingListInfo;
import com.mualab.org.user.activity.booking.model.TrackInfo;
import com.mualab.org.user.activity.payment.AllCardActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class BookingDetailsActivity extends AppCompatActivity {

    private BookingHistoryDetailsAdapter adapter;
    private RecyclerView rcv_service;
    private ImageView iv_profile_artist;
    private TextView tv_artist_name, tv_address, booking_status, tv_payment_method, tv_new_amount, tv_amount,
            tv_voucher_code, tv_discounted_price, tv_call_type, tv_pay;
    private FrameLayout ly_amount;
    private RelativeLayout ly_voucher_code, ly_map_direction, ly_cancel_booking, ly_give_review,ly_call;
    private BookingListInfo historyInfo;
    private int bookingId, serviceId, subServiceId, artistServiceId, artistId;
    private boolean isOutCallType;
    private RelativeLayout ly_txt_Id, ly_txt_status;
    private TextView tv_txt_id, tv_txt_status,tvDone;
    private boolean shouldPopupOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("com.mualab.org.user"));
        setContentView(R.layout.activity_booking_details);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.appoinment));

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ly_txt_Id = findViewById(R.id.ly_txt_Id);
        ly_txt_status = findViewById(R.id.ly_txt_status);
        rcv_service = findViewById(R.id.rcv_service);
        iv_profile_artist = findViewById(R.id.iv_profile_artist);
        tv_artist_name = findViewById(R.id.tv_artist_name);
        tv_address = findViewById(R.id.tv_address);
        booking_status = findViewById(R.id.booking_status);
        tv_payment_method = findViewById(R.id.tv_payment_method);
        tv_new_amount = findViewById(R.id.tv_new_amount);
        tv_amount = findViewById(R.id.tv_amount);
        ly_amount = findViewById(R.id.ly_amount);
        ly_voucher_code = findViewById(R.id.ly_voucher_code);
        ly_map_direction = findViewById(R.id.ly_map_direction);
        tv_voucher_code = findViewById(R.id.tv_voucher_code);
        tv_discounted_price = findViewById(R.id.tv_discounted_price);
        tv_call_type = findViewById(R.id.tv_call_type);
        ly_cancel_booking = findViewById(R.id.ly_cancel_booking);
        tv_pay = findViewById(R.id.tv_pay);
        ly_give_review = findViewById(R.id.ly_give_review);
        tv_txt_id = findViewById(R.id.tv_txt_id);
        ly_call = findViewById(R.id.ly_call);
        tv_txt_status = findViewById(R.id.tv_txt_status);
        tvDone = findViewById(R.id.tvDone);
        tvDone.setVisibility(View.VISIBLE);


        if (getIntent().getIntExtra("bookingId", 0) != 0) {
            bookingId = getIntent().getIntExtra("bookingId", 0);
            shouldPopupOpen = getIntent().getBooleanExtra("shouldPopupOpen", false);
        }

        historyInfo = new BookingListInfo();

        ly_map_direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<TrackInfo> mapBeanArrayList = new ArrayList<>();
                if (historyInfo.data != null) {
                    TrackInfo trackUser = new TrackInfo();
                    trackUser.address = historyInfo.data.location;
                    trackUser.latitude = historyInfo.data.latitude;
                    trackUser.longitude = historyInfo.data.longitude;

                    if (historyInfo.data.userDetail.get(0).profileImage.equals("")) {
                        trackUser.profileImage = "https://image.shutterstock.com/image-vector/male-default-placeholder-avatar-profile-260nw-387516193.jpg";

                    } else trackUser.profileImage = historyInfo.data.userDetail.get(0).profileImage;

                    TrackInfo trackArtist = new TrackInfo();
                    trackArtist.address = "";
                    trackArtist.latitude = historyInfo.data.bookingInfo.get(0).trackingLatitude;
                    trackArtist.longitude = historyInfo.data.bookingInfo.get(0).trackingLongitude;
                    trackArtist.staffName = historyInfo.data.bookingInfo.get(0).staffName;
                    trackArtist.bookingDate = historyInfo.data.bookingInfo.get(0).bookingDate;
                    trackArtist.startTime = historyInfo.data.bookingInfo.get(0).startTime;
                    trackArtist.artistServiceName = historyInfo.data.bookingInfo.get(0).artistServiceName;
                    trackArtist.contactNo = historyInfo.data.artistDetail.get(0).contactNo;
                    trackArtist.ratingCount = historyInfo.data.artistDetail.get(0).ratingCount;

                    if (historyInfo.data.bookingInfo.get(0).staffImage.equals("")) {
                        trackArtist.profileImage = "https://image.shutterstock.com/image-vector/male-default-placeholder-avatar-profile-260nw-387516193.jpg";
                    } else
                        trackArtist.profileImage = historyInfo.data.bookingInfo.get(0).staffImage;

                    Intent intent = new Intent(BookingDetailsActivity.this, TrackingActivity.class);
                    intent.putExtra("trackUser", trackUser);
                    intent.putExtra("trackArtist", trackArtist);
                    intent.putExtra("bookingId", bookingId);
                    startActivity(intent);
                }


            }
        });

        tvDone.setOnClickListener(value->{
            onBackPressed();
        });

        ly_cancel_booking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBookingAction("reject");
            }
        });

        tv_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = "";
                Intent intent = new Intent(BookingDetailsActivity.this, AllCardActivity.class);
                intent.putExtra("bookingId", bookingId);

                if (historyInfo.data.discountPrice.equals("")) {
                    String roundValue = String.format("%.2f", Double.parseDouble(historyInfo.data.totalPrice));
                    amount = "£" + roundValue;
                } else {
                    String roundValue = String.format("%.2f", Double.parseDouble(historyInfo.data.discountPrice));
                    amount = "£" + roundValue;
                }

                intent.putExtra("amount", amount);

                startActivity(intent);
            }
        });

        ly_give_review.setOnClickListener(v -> {
           // askForReviewRating();
            bottomSheetDialog();
        });

        iv_profile_artist.setOnClickListener(v->{
            Intent intent = new Intent(this, ArtistProfileActivity.class);
            intent.putExtra("artistId", String.valueOf(historyInfo.data.artistDetail.get(0)._id));
            startActivity(intent);
        });

        ly_call.setOnClickListener(v->{
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+historyInfo.data.artistDetail.get(0).countryCode+
                        " "+historyInfo.data.artistDetail.get(0).contactNo));
                startActivity(intent);
            }catch (Exception e){

            }

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bookingId != 0) {
            getDetailsBookService(bookingId);
        }
    }

    private void getDetailsBookService(final int bookingId) {
        Progress.show(BookingDetailsActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingDetailsActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        getDetailsBookService(bookingId);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("bookingId", String.valueOf(bookingId));


        HttpTask task = new HttpTask(new HttpTask.Builder(BookingDetailsActivity.this, "bookingDetail", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingDetailsActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equals("success")) {
                        Gson gson = new Gson();
                        historyInfo = gson.fromJson(response, BookingListInfo.class);
                        adapter = new BookingHistoryDetailsAdapter(BookingDetailsActivity.this, historyInfo);
                        rcv_service.setAdapter(adapter);

                        //get key
                        artistId = historyInfo.data.artistId;
                        serviceId = historyInfo.data.bookingInfo.get(0).serviceId;
                        subServiceId = historyInfo.data.bookingInfo.get(0).subServiceId;
                        artistServiceId = historyInfo.data.bookingInfo.get(0).artistServiceId;

                        setData(historyInfo);
                        adapter.notifyDataSetChanged();

                    } else {
                        MyToast.getInstance(BookingDetailsActivity.this).showDasuAlert(message);
                    }


                } catch (Exception e) {
                    Progress.hide(BookingDetailsActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingDetailsActivity.this);
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

    void setData(BookingListInfo historyInfo) {
        if (historyInfo.data.bookingType == 2) {
            //bookingType = "2";
            tv_call_type.setText("Out Call");
            isOutCallType = true;
        } else {
            //bookingType = "1";
            tv_call_type.setText("In Call");
            isOutCallType = false;
        }


        if (!historyInfo.data.artistDetail.get(0).profileImage.isEmpty() && !historyInfo.data.artistDetail.get(0).profileImage.equals("")) {
            Picasso.with(BookingDetailsActivity.this).load(historyInfo.data.artistDetail.get(0).profileImage).placeholder(R.drawable.default_placeholder).
                    fit().into(iv_profile_artist);
        } else {
            iv_profile_artist.setImageResource(R.drawable.default_placeholder);
        }

        tv_artist_name.setText(historyInfo.data.artistDetail.get(0).userName + "");
        tv_address.setText(historyInfo.data.location);


        if (historyInfo.data.bookStatus.equals("0")) {
            booking_status.setText(R.string.pending);
            ly_map_direction.setVisibility(View.GONE);
            ly_cancel_booking.setVisibility(View.VISIBLE);
            ly_give_review.setVisibility(View.GONE);
            booking_status.setTextColor(ContextCompat.getColor(BookingDetailsActivity.this, R.color.main_orange_color));
        } else if (historyInfo.data.bookStatus.equals("1")) {
            booking_status.setText(R.string.confirm);
            ly_map_direction.setVisibility(View.GONE);
            ly_cancel_booking.setVisibility(View.GONE);
            ly_give_review.setVisibility(View.GONE);
            booking_status.setTextColor(ContextCompat.getColor(BookingDetailsActivity.this, R.color.main_green_color));
        } else if (historyInfo.data.bookStatus.equals("2")) {
            booking_status.setText("Cancelled");
            ly_cancel_booking.setVisibility(View.GONE);
            booking_status.setTextColor(ContextCompat.getColor(BookingDetailsActivity.this, R.color.red));
            ly_map_direction.setVisibility(View.GONE);
            ly_give_review.setVisibility(View.GONE);
        } else if (historyInfo.data.bookStatus.equals("3")) {
            booking_status.setText("Completed");
            ly_cancel_booking.setVisibility(View.GONE);
            booking_status.setTextColor(ContextCompat.getColor(BookingDetailsActivity.this, R.color.main_green_color));
            ly_map_direction.setVisibility(View.GONE);
            ly_give_review.setVisibility(View.VISIBLE);
        } else if (historyInfo.data.bookStatus.equals("5")) {
            ly_map_direction.setVisibility(View.VISIBLE);
            ly_cancel_booking.setVisibility(View.GONE);
            booking_status.setText("In progress");
            ly_give_review.setVisibility(View.GONE);
            booking_status.setTextColor(ContextCompat.getColor(BookingDetailsActivity.this, R.color.main_green_color));
        }

        if (historyInfo.data.paymentType == 1) {
            if (historyInfo.data.paymentStatus == 0) {
                tv_txt_status.setText("Pending");
                tv_txt_status.setTextColor(ContextCompat.getColor(BookingDetailsActivity.this, R.color.main_orange_color));
                tv_txt_id.setText("NA");
            } else {
                tv_txt_status.setText("Completed");
                tv_txt_status.setTextColor(ContextCompat.getColor(BookingDetailsActivity.this, R.color.main_green_color));
                tv_txt_id.setText(historyInfo.data.transjectionId);
            }

            if (historyInfo.data.bookStatus.equals("1") || historyInfo.data.bookStatus.equals("5")) {
                tv_pay.setVisibility(View.VISIBLE);
                if (historyInfo.data.paymentStatus == 0) {
                    tv_pay.setText("Pay");
                    tv_txt_status.setText("Pending");
                    tv_txt_status.setTextColor(ContextCompat.getColor(BookingDetailsActivity.this, R.color.main_orange_color));
                    tv_txt_id.setText("NA");
                    tv_pay.setEnabled(true);
                } else {
                    tv_pay.setText("Paid");
                    tv_txt_status.setText("Completed");
                    tv_txt_status.setTextColor(ContextCompat.getColor(BookingDetailsActivity.this, R.color.main_green_color));
                    tv_txt_id.setText(historyInfo.data.transjectionId);
                    tv_pay.setEnabled(false);
                    tv_pay.setVisibility(View.GONE);
                }
            } else tv_pay.setVisibility(View.GONE);
        } else {
            tv_pay.setVisibility(View.GONE);
        }

        if (!isOutCallType) {
            ly_map_direction.setVisibility(View.GONE);
        }

        if (historyInfo.data.paymentType == 2) {
            ly_txt_Id.setVisibility(View.GONE);
            ly_txt_status.setVisibility(View.GONE);
            tv_payment_method.setText(R.string.cash);
        } else {
            ly_txt_Id.setVisibility(View.VISIBLE);
            ly_txt_status.setVisibility(View.VISIBLE);
            tv_payment_method.setText(getString(R.string.card));
        }


        if (historyInfo.data.discountPrice.equals("")) {
            ly_amount.setVisibility(View.GONE);
            String roundValue = String.format("%.2f", Double.parseDouble(historyInfo.data.totalPrice));
            tv_new_amount.setText("£" + roundValue + "");
        } else {
            ly_amount.setVisibility(View.VISIBLE);
            tv_amount.setTextColor(ContextCompat.getColor(BookingDetailsActivity.this, R.color.red));
            tv_amount.setText("£" + historyInfo.data.totalPrice + "");
            String roundValue = String.format("%.2f", Double.parseDouble(historyInfo.data.discountPrice));
            tv_new_amount.setText(getString(R.string.pond_symbol) + roundValue + "");
        }

        if (historyInfo.data.voucher.voucherCode == null) {
            ly_voucher_code.setVisibility(View.GONE);
        } else {
            ly_voucher_code.setVisibility(View.VISIBLE);
            tv_voucher_code.setText(historyInfo.data.voucher.voucherCode);
            if (historyInfo.data.voucher.discountType.equals("1")) { // 1 for fix amount
                tv_discounted_price.setText("-£" + historyInfo.data.voucher.amount);
            } else if (historyInfo.data.voucher.discountType.equals("2")) {// 2 for percentage
                tv_discounted_price.setText("-" + historyInfo.data.voucher.amount + "%");
            }
        }

        //payment time
        if (historyInfo.data.paymentType == 1 && historyInfo.data.paymentStatus == 0) { // case of online and payment not pay
            paymentTimer(historyInfo);
        }


        if (shouldPopupOpen) {
            //askForReviewRating();
            bottomSheetDialog();
            shouldPopupOpen = false;
        }

    }

    private void paymentTimer(BookingListInfo historyInfo) {
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");//Thu Feb 14 2019 12:29:42 GMT+0000 (UTC)
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));   // This line converts the given date into UTC time zone
        Date dateObj = null;
        try {
            dateObj = sdf.parse(historyInfo.data.paymentTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String aRevisedDate = new SimpleDateFormat("dd/MM/yyyy KK:mm:ss a").format(dateObj);
        System.out.println(aRevisedDate);/*...server booking confirm time....*/

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy KK:mm:ss a");
        Date d = new Date();
        try {
            d = df.parse(aRevisedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.MINUTE, 30);
        String newTime = df.format(cal.getTime());
        System.out.println(newTime);/*...here we add 30 minute in booking confirm time...*/

        SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy KK:mm:ss a");
        String currentDateandTime = sdf2.format(new Date());
        System.out.println(currentDateandTime);/*... this is current date and time we have to compare...*/

        try {

            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy KK:mm:ss a");
            String str1 = newTime;
            Date date1 = formatter.parse(str1);

            String str2 = currentDateandTime;
            Date date2 = formatter.parse(str2);

            if (date1.compareTo(date2) < 0) {
                System.out.println("date2 is Greater than date1");
                setBookingAction("reject");
            } else {
                System.out.println("date1 is Greater than date2");
            }

        } catch (ParseException e1) {
            e1.printStackTrace();
        }
    }

    private void setBookingAction(final String type) {
        //Progress.show(BookingDetailsActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingDetailsActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        setBookingAction(type);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("bookingId", String.valueOf(bookingId));
        params.put("artistId", String.valueOf(artistId));
        params.put("userId", String.valueOf(user.id));
        params.put("serviceId", String.valueOf(serviceId));
        params.put("subserviceId", String.valueOf(subServiceId));
        params.put("artistServiceId", String.valueOf(artistServiceId));
        params.put("type", type);


        HttpTask task = new HttpTask(new HttpTask.Builder(BookingDetailsActivity.this, "bookingAction", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingDetailsActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equals("success")) {

                        MyToast.getInstance(BookingDetailsActivity.this).showDasuAlert(message);
                        getDetailsBookService(bookingId);
                    } else {
                        MyToast.getInstance(BookingDetailsActivity.this).showDasuAlert(message);
                    }


                } catch (Exception e) {
                    Progress.hide(BookingDetailsActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingDetailsActivity.this);
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
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(this.getClass().getName());
    }

    private void askForReviewRating() {
        BookingReviewRatingDialog.newInstance(historyInfo, new BookingReviewRatingCallback() {
            @Override
            public void onSubmitClick(String rating, String comment, String type) {
                bookingReviewRating(artistId, bookingId, comment, rating, type);
            }

            @Override
            public void onReportThisClick() {
                MyToast.getInstance(BookingDetailsActivity.this).showSmallMessage(getString(R.string.under_development));
            }
        }).show(getSupportFragmentManager());
    }

    private void bookingReviewRating(final int artistId, final int bookingId,
                                     final String reviewByUser, final String rating, final String type) {
        Progress.show(BookingDetailsActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingDetailsActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        bookingReviewRating(artistId, bookingId, reviewByUser, rating, type);
                    }
                }
            }).show();
        }
        Map<String, String> params = new HashMap<>();
        params.put("bookingId", String.valueOf(bookingId));
        params.put("reviewByUser", reviewByUser);
        params.put("reviewByArtist", "");
        params.put("rating", rating);
        params.put("userId", String.valueOf(Mualab.currentUser.id));
        params.put("artistId", String.valueOf(artistId));
        params.put("ratingDate", giveDate());
        params.put("type", type);

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingDetailsActivity.this, "bookingReviewRating", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingDetailsActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equals("success")) {
                        getDetailsBookService(bookingId);
                        MyToast.getInstance(BookingDetailsActivity.this).showDasuAlert("Review Submitted Successfully");
                    } else {
                        MyToast.getInstance(BookingDetailsActivity.this).showDasuAlert(message);
                    }

                } catch (Exception e) {
                    Progress.hide(BookingDetailsActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingDetailsActivity.this);
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

    public String giveDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (bookingId != 0) {
                String notificationType = intent.getStringExtra("notificationType");
                String notifyId = intent.getStringExtra("notifyId");//userId
                String title = intent.getStringExtra("title");

                if (notificationType.equals("5")) {// open Popupcase
                    shouldPopupOpen = true;
                }
                getDetailsBookService(bookingId);

            }
        }
    };

    private void bottomSheetDialog() {
       final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this,R.style.CustomBottomSheetDialogTheme);

        final View bottomSheetLayout = getLayoutInflater().inflate(R.layout.rating_bottom_sheet, null);
      //  bottomSheetLayout.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        EditText etComments  = bottomSheetLayout.findViewById(R.id.etComments);
        RatingBar userRating = bottomSheetLayout.findViewById(R.id.userRating);
        TextView tv_msg = bottomSheetLayout.findViewById(R.id.tv_msg);
        LinearLayout llAddComment = bottomSheetLayout.findViewById(R.id.llAddComment);
        ImageView ivDropDown = bottomSheetLayout.findViewById(R.id.ivDropDown);



        llAddComment.setOnClickListener(v -> {
            if(etComments.getVisibility() == View.GONE){
                ivDropDown.setRotation(0);
                etComments.setVisibility(View.VISIBLE);
            }else {
                ivDropDown.setRotation(180);
                etComments.setVisibility(View.GONE);
            }
        });

        if(!historyInfo.data.reviewByUser.equals("") && !historyInfo.data.userRating.equals("")){
            etComments.setText(historyInfo.data.reviewByUser);
            etComments.setSelection(historyInfo.data.reviewByUser.length());
            userRating.setRating(Float.parseFloat(historyInfo.data.userRating));
        }

        String artistUserName = " @"+historyInfo.data.artistDetail.get(0).userName+" 's";
        String msg = getString(R.string.your_service_has_been_completed_nplease_review_how_satisfied_you_are_nwith)
                .concat(artistUserName).concat(" service");

        int startingPosition = msg.indexOf(artistUserName);
        int endingPosition = startingPosition + artistUserName.length();

        Spannable msgSpan = new SpannableString(msg);
        msgSpan.setSpan(new ForegroundColorSpan(Color.BLACK), startingPosition, endingPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        msgSpan.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startingPosition, endingPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tv_msg.setText(msgSpan);


        (bottomSheetLayout.findViewById(R.id.btnSumbit)).setOnClickListener(view -> {
            if (verifyInputs((int) userRating.getRating(), etComments.getText().toString().trim())) {

                String type = "insert";
                if(!historyInfo.data.reviewByUser.equals("") && !historyInfo.data.userRating.equals("0")){
                    type = "edit";
                }
                mBottomSheetDialog.dismiss();
                bookingReviewRating(artistId, bookingId, etComments.getText().toString().trim(),
                        String.valueOf(userRating.getRating()), type);
            }
        });

        mBottomSheetDialog.setContentView(bottomSheetLayout);
        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) bottomSheetLayout.getParent());
       // mBehavior.setPeekHeight(160);
        mBottomSheetDialog.show();
    }

    private boolean verifyInputs(int rating, String comments) {
        if (rating == 0) {
            MyToast.getInstance(BookingDetailsActivity.this).showDasuAlert(getString(R.string.give_rating));
            return false;
        } else if (comments.isEmpty()) {
            MyToast.getInstance(BookingDetailsActivity.this).showDasuAlert(getString(R.string.give_review));
            return false;
        } else return true;
    }
}

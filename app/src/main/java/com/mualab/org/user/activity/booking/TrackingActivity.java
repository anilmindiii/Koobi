package com.mualab.org.user.activity.booking;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.customSeekBar.RangeSeekBar;
import com.mualab.org.user.activity.booking.model.BookingListInfo;
import com.mualab.org.user.activity.booking.model.TrackInfo;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.constants.Constant;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private BalloonAdapter balloonAdapter;
    protected GoogleMap mGoogleMap;
    private MapFragment mapFragment;
    private TrackInfo trackUser;
    private TrackInfo trackArtist;
    private ArrayList<TrackInfo> mapBeanArrayList;
    private TextView tvDistance, tvstaffName, tv_date_n_time_text, tvServices, tv_artist_address, tv_customer_address, tvStatus;
    private ImageView ivartistProfilePic, imgCustomer, complete_tracking;
    private RelativeLayout ly_satelite_view, ly_my_location;
    private boolean isSatelliteMode;
    private Timer timer;
    private int bookingId;
    private Marker moveMarker;
    private static MoveThread moveThread;
    private static Handler handler;
    private View markerView;
    private RangeSeekBar rangeSeekBar, rangeSeekBarTransparent;
    private float totalDistance;
    private LinearLayout ly_Call;
    private String artistMobNumber = "";
    private boolean isUnderTemMeter;
    private RatingBar rating;
    private Bitmap userBitmap, artistBitmap;
    private boolean isReached, isCompleteService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        mapBeanArrayList = new ArrayList<>();
        if (getIntent() != null) {
            trackUser = (TrackInfo) getIntent().getSerializableExtra("trackUser");
            trackArtist = (TrackInfo) getIntent().getSerializableExtra("trackArtist");

            bookingId = getIntent().getIntExtra("bookingId", 0);
            artistMobNumber = trackArtist.contactNo;

            mapBeanArrayList.add(trackUser);
            mapBeanArrayList.add(trackArtist);
        }

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        tvDistance = findViewById(R.id.tvDistance);
        tvstaffName = findViewById(R.id.tvstaffName);
        rating = findViewById(R.id.rating);
        ivartistProfilePic = findViewById(R.id.ivartistProfilePic);
        tv_date_n_time_text = findViewById(R.id.tv_date_n_time_text);
        tvServices = findViewById(R.id.tvServices);
        ly_satelite_view = findViewById(R.id.ly_satelite_view);
        ly_my_location = findViewById(R.id.ly_my_location);
        imgCustomer = findViewById(R.id.imgCustomer);
        rangeSeekBar = findViewById(R.id.rangeSeekBar);
        rangeSeekBarTransparent = findViewById(R.id.rangeSeekBarTransparent);
        ly_Call = findViewById(R.id.ly_Call);
        complete_tracking = findViewById(R.id.complete_tracking);
        tv_artist_address = findViewById(R.id.tv_artist_address);
        tv_customer_address = findViewById(R.id.tv_customer_address);
        tvStatus = findViewById(R.id.tvStatus);

        tv_customer_address.setText(trackUser.address + "");

        View tracking_bottom_sheet = findViewById(R.id.tracking_bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(tracking_bottom_sheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        final ImageView imgSheetUpDown = findViewById(R.id.imgSheetUpDown);
        imgCustomer = findViewById(R.id.imgCustomer);
        //imgReached = findViewById(R.id.imgReached);

        rangeSeekBar.setEnabled(false);
        rangeSeekBar.setIndicatorText("");

        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    imgSheetUpDown.setRotation(270);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    imgSheetUpDown.setRotation(90);
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    imgSheetUpDown.setRotation(90);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });


        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ly_my_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapBeanArrayList.size() != 0)
                    if (!mapBeanArrayList.get(0).latitude.equals("") && !mapBeanArrayList.get(0).longitude.equals(""))
                        setUpMap(mapBeanArrayList.get(0).latitude, mapBeanArrayList.get(0).longitude);// my locaton
            }
        });

        ly_satelite_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isSatelliteMode) {
                    isSatelliteMode = false;
                    //for normal view of map:
                    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else {
                    isSatelliteMode = true;
                    //for satellite view of map:
                    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }

            }
        });

        ly_Call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callToNumber(artistMobNumber);
            }
        });

        balloonAdapter = new BalloonAdapter(getLayoutInflater());

        tvServices.setText(trackArtist.artistServiceName);
        //String selectedDate = Helper.formateDateFromstring("yyyy-MM-dd", "dd/MM/yyyy", trackArtist.bookingDate);//2019-02-01
        tv_date_n_time_text.setText(trackArtist.bookingDate + ", " + trackArtist.startTime);
        Glide.with(this).load(trackArtist.profileImage).placeholder(R.drawable.default_placeholder).into(ivartistProfilePic);
        tvstaffName.setText(trackArtist.staffName);
        if (!trackArtist.ratingCount.equals("")) {
            rating.setRating(Float.parseFloat(trackArtist.ratingCount));
        }

        handler = new Handler();

        moveThread = new MoveThread();
        if (!trackArtist.latitude.equals("")) {
            moveThread.setNewPoint(new LatLng(Double.parseDouble(trackArtist.latitude), Double.parseDouble(trackArtist.longitude)), 16);
            handler.post(moveThread);
        }

        rangeSeekBar.getLeftSeekBar().setIndicatorBitmap(Helper.getBitmapFromVectorDrawable(this, R.drawable.ic_map_marker));

        startTimer();
        getDetailsBookService(bookingId);
    }

    private void callToNumber(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(TrackingActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(TrackingActivity.this, new String[]{Manifest.permission.CALL_PHONE}, Constant.REQUEST_PHONE_CALL);
        } else {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mGoogleMap.setMyLocationEnabled(false);
            }
        } else {
            mGoogleMap.setMyLocationEnabled(false);
        }

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
               /* if (marker != null) {
                    int position = Integer.parseInt(marker.getTitle());

                }*/
            }
        });

        if (!trackArtist.latitude.equals("") && !trackUser.latitude.equals("")) {
            Double distance = Double.parseDouble(getDistance()) / 1609.344;
            String roundValue = String.format("%.2f", distance);
            tvDistance.setText(roundValue + " Miles");
            totalDistance = Float.parseFloat(getDistance());
            rangeSeekBar.setMaxProgress(totalDistance);

            if (totalDistance <= 10f) {
                // reached to destination case
                isUnderTemMeter = true;
                imgCustomer.setVisibility(View.GONE);
                complete_tracking.setVisibility(View.VISIBLE);
                rangeSeekBar.setVisibility(View.GONE);
                rangeSeekBarTransparent.setVisibility(View.VISIBLE);
                rangeSeekBarTransparent.setValue(100);

                tvStatus.setText("On the way");
                tvStatus.setTextColor(ContextCompat.getColor(TrackingActivity.this, R.color.main_orange_color));

                reachedToDestination(R.drawable.mett_user_ico, "", Double.parseDouble(trackArtist.latitude), Double.parseDouble(trackArtist.longitude));
            } else {
                isUnderTemMeter = false;
                imgCustomer.setVisibility(View.VISIBLE);
                complete_tracking.setVisibility(View.GONE);
                wattingForImage(mapBeanArrayList);

                rangeSeekBar.setVisibility(View.VISIBLE);
                rangeSeekBarTransparent.setVisibility(View.GONE);

                tvStatus.setText("Reached");
                tvStatus.setTextColor(ContextCompat.getColor(TrackingActivity.this, R.color.main_green_color));

            }

        }


        if (mapBeanArrayList.size() != 0)
            if (!mapBeanArrayList.get(0).latitude.equals("") && !mapBeanArrayList.get(0).longitude.equals(""))
                setUpMap(mapBeanArrayList.get(0).latitude, mapBeanArrayList.get(0).longitude);// my locaton

    }

    private void setUpMap(String appointLatitude, String appointLongitude) {
        LatLngBounds bounds = new LatLngBounds(new LatLng(Double.parseDouble(appointLatitude),
                Double.parseDouble(appointLongitude)), new LatLng(Double.parseDouble(appointLatitude),
                Double.parseDouble(appointLongitude)));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 10));
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);


    }

    private void againGoAway(final ArrayList<TrackInfo> mapBeanArrayList, final int i) {
        if (i < mapBeanArrayList.size()) {

            final View markerView = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custommarkerlayout, null);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            markerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            markerView.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
            markerView.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
            markerView.buildDrawingCache();

            ImageView imageuser = markerView.findViewById(R.id.marker_image);
            ImageView iv_outer = markerView.findViewById(R.id.iv_outer);

            if (mapBeanArrayList.get(i).staffName == null) {
                // case of user
                iv_outer.setColorFilter(ContextCompat.getColor(this, R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                iv_outer.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            final TrackInfo mapBean = mapBeanArrayList.get(i);
            if (!mapBean.profileImage.equals("")) {
                Picasso.with(TrackingActivity.this)
                        .load(mapBean.profileImage).placeholder(R.drawable.default_placeholder)
                        .into(imageuser, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                if (!mapBean.latitude.equals("")) {
                                    Bitmap finalBitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                                    Canvas canvas = new Canvas(finalBitmap);
                                    markerView.draw(canvas);
                                    if (mapBean.staffName == null) {
                                        // case of user
                                        if (userBitmap == null)
                                            userBitmap = finalBitmap;
                                    } else {
                                        //seekbar
                                        if (artistBitmap == null)
                                            artistBitmap = finalBitmap;
                                    }
                                }
                            }

                            @Override
                            public void onError() {
                                if (!mapBean.latitude.equals("")) {
                                    Bitmap finalBitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                                    Canvas canvas = new Canvas(finalBitmap);
                                    markerView.draw(canvas);
                                    if (mapBean.staffName == null) {
                                        // case of user
                                        if (userBitmap == null)
                                            userBitmap = finalBitmap;
                                    } else {
                                        //seekbar
                                        if (artistBitmap == null)
                                            artistBitmap = finalBitmap;
                                    }
                                }
                            }
                        });
                if (i < mapBeanArrayList.size()) {
                    againGoAway(mapBeanArrayList, i + 1);
                }
            }


        }
    }


    private void moveToNext(final ArrayList<TrackInfo> mapBeanArrayList, final int i) {

        if (i < mapBeanArrayList.size()) {

            final View markerView = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custommarkerlayout, null);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            markerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            markerView.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
            markerView.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
            markerView.buildDrawingCache();

            ImageView imageuser = markerView.findViewById(R.id.marker_image);
            ImageView iv_outer = markerView.findViewById(R.id.iv_outer);


            if (mapBeanArrayList.get(i).staffName == null) {
                // case of user
                iv_outer.setColorFilter(ContextCompat.getColor(this, R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                iv_outer.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
            }


            /*.............info window Adapter...........................*/
            mGoogleMap.setInfoWindowAdapter(balloonAdapter);

            final TrackInfo mapBean = mapBeanArrayList.get(i);

            if (!mapBean.profileImage.equals("")) {
                Picasso.with(TrackingActivity.this)
                        .load(mapBean.profileImage).placeholder(R.drawable.default_placeholder)
                        .into(imageuser, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                if (!mapBean.latitude.equals(""))
                                    getMatkerDrop(markerView, mapBean, i);
                            }

                            @Override
                            public void onError() {
                                if (!mapBean.latitude.equals(""))
                                    getMatkerDrop(markerView, mapBean, i);
                            }
                        });

            }


        }
    }

    private void reachedToDestination(int imageUrl,
                                      final String status,
                                      final double latitude,
                                      final double longitude) {
        mGoogleMap.clear();
        isReached = true;
        final View markerView = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custommarkerlayout, null);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        markerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        markerView.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        markerView.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        markerView.buildDrawingCache();

        final ImageView imageuser = markerView.findViewById(R.id.marker_image);
        final ImageView iv_outer = markerView.findViewById(R.id.iv_outer);

        BalloonAdapterComplete adapterComplete = new BalloonAdapterComplete(getLayoutInflater());
        mGoogleMap.setInfoWindowAdapter(adapterComplete);

        iv_outer.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);

        if (imageUrl != 0) {
            Picasso.with(TrackingActivity.this)
                    .load(imageUrl).placeholder(R.drawable.default_placeholder)
                    .into(imageuser, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {

                            Bitmap finalBitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(finalBitmap);
                            markerView.draw(canvas);

                            // update views
                            LatLng point;
                            double newLat = latitude + (Math.random() - .5) / 1500; // * (Math.random() * (max - min) + min);
                            double newLng = longitude + (Math.random() - .5) / 1500;// * (Math.random() * (max - min) + min);
                            point = new LatLng(newLat, newLng);

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(point);
                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(finalBitmap));
                            dropPinEffect(mGoogleMap.addMarker(markerOptions));

                            if (trackArtist.staffName == null) {
                                // case of user
                                imgCustomer.setImageBitmap(finalBitmap);
                            } else {
                                //seekbar
                                rangeSeekBar.getLeftSeekBar().setIndicatorBitmap(finalBitmap);
                            }
                        }

                        @Override
                        public void onError() {
                            Bitmap finalBitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(finalBitmap);
                            markerView.draw(canvas);
                            if (trackArtist.staffName == null) {
                                // case of user
                                imgCustomer.setImageBitmap(finalBitmap);
                            } else {
                                //seekbar
                                rangeSeekBar.getLeftSeekBar().setIndicatorBitmap(finalBitmap);
                            }
                        }
                    });

        }


    }


    private void getMatkerDrop(View markerView, TrackInfo mapBean, int i) {
        if (trackArtist.latitude != null) {
            this.markerView = markerView;
        }

        Bitmap finalBitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(finalBitmap);
        markerView.draw(canvas);

        // setSeekBarMarker(mapBean.profileImage,"");

        if (mapBean.staffName == null) {
            // case of user
            if (userBitmap == null)
                userBitmap = finalBitmap;
            imgCustomer.setImageBitmap(finalBitmap);
        } else {
            //seekbar
            if (artistBitmap == null)
                artistBitmap = finalBitmap;
            rangeSeekBar.getLeftSeekBar().setIndicatorBitmap(finalBitmap);
        }


        // update views
        LatLng point;
        double newLat = Double.parseDouble(mapBean.latitude) + (Math.random() - .5) / 1500;// * (Math.random() * (max - min) + min);
        double newLng = Double.parseDouble(mapBean.longitude) + (Math.random() - .5) / 1500;// * (Math.random() * (max - min) + min);
        point = new LatLng(newLat, newLng);
        // point = new LatLng(Double.parseDouble(mapBean.latitude), Double.parseDouble(mapBean.longitude));

        // Creating an instance of MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.title("" + i);
        markerOptions.snippet(mapBean.address);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(finalBitmap));

        dropPinEffect(mGoogleMap.addMarker(markerOptions));

        if (i < mapBeanArrayList.size()) {
            moveToNext(mapBeanArrayList, i + 1);
        }
    }

    private void dropPinEffect(final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;
        final Interpolator interpolator = new BounceInterpolator();

        if (trackUser.staffName == null) {
            moveMarker = marker;
        }


        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                marker.setAnchor(0.5f, 1.0f + 14 * t);
                if (t > 0.0) {
                    // Post again 15ms later.
                    handler.postDelayed(this, 15);
                }
            }
        });
    }

    private class BalloonAdapter implements GoogleMap.InfoWindowAdapter {
        LayoutInflater inflater = null;

        public BalloonAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            View v = inflater.inflate(R.layout.info_window_layout, null);
            if (marker != null) {
                String pos = marker.getTitle();
                if (pos != null) {
                    int position = Integer.parseInt(pos);
                    TextView tv_title = v.findViewById(R.id.tv_title);

                    if (mapBeanArrayList.get(position).staffName == null) {
                        // case of user
                        tv_title.setText(mapBeanArrayList.get(position).address);
                    } else {
                        tv_title.setText("On the way");
                    }
                }
            }
            return (v);
        }

        @Override
        public View getInfoContents(Marker marker) {
            return (null);
        }
    }

    private class BalloonAdapterComplete implements GoogleMap.InfoWindowAdapter {
        LayoutInflater inflater = null;

        public BalloonAdapterComplete(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            View v = inflater.inflate(R.layout.info_window_layout, null);
            TextView tv_title = v.findViewById(R.id.tv_title);
            tv_title.setText("Complete");
            return (v);
        }

        @Override
        public View getInfoContents(Marker marker) {
            return (null);
        }
    }


    private void wattingForImage(final ArrayList<TrackInfo> mapBeanArrayList) {
        moveToNext(mapBeanArrayList, 0);
    }

    private String getDistance() {
        Location startPoint = new Location("locationA");

        startPoint.setLatitude(Double.parseDouble(trackArtist.latitude));
        startPoint.setLongitude(Double.parseDouble(trackArtist.longitude));

        Location endPoint = new Location("locationA");
        endPoint.setLatitude(Double.parseDouble(trackUser.latitude));
        endPoint.setLongitude(Double.parseDouble(trackUser.longitude));

        double distance = startPoint.distanceTo(endPoint);
        // distance = (distance / 1609.344);
        String roundValue = String.format("%.2f", distance);
        return roundValue;
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timer != null)
            startTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        timer = null;
    }

    public void startTimer() {
        if (timer != null) {
            stopTimer();
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(Timer_Tick);
            }

        }, 0, 10000);//10 sec

    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            if (!isCompleteService)
                getDetailsBookService(bookingId);
        }
    };

    private void getDetailsBookService(final int bookingId) {
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(TrackingActivity.this, new NoConnectionDialog.Listner() {
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

        HttpTask task = new HttpTask(new HttpTask.Builder(TrackingActivity.this, "bookingDetail", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equals("success")) {
                        Gson gson = new Gson();
                        BookingListInfo historyInfo = gson.fromJson(response, BookingListInfo.class);

                        String trackingLat = historyInfo.data.bookingInfo.get(0).trackingLatitude;
                        String trackingLng = historyInfo.data.bookingInfo.get(0).trackingLongitude;

                        trackUser.latitude = historyInfo.data.latitude;
                        trackUser.longitude = historyInfo.data.longitude;

                        tv_artist_address.setText(historyInfo.data.bookingInfo.get(0).trackingAddress + "");

                        LatLng finalPosition = new LatLng((Double.parseDouble(trackingLat)),
                                (Double.parseDouble(trackingLng)));

                        moveThread.setNewPoint(finalPosition, mGoogleMap.getCameraPosition().zoom); // set the map zoom to current map's zoom level as user may zoom the map while tracking.
                        if (moveMarker != null)
                            animateMarkerToICS(moveMarker, finalPosition); // animate the marker smoothly

                        Location startPoint = new Location("locationA");
                        startPoint.setLatitude(finalPosition.latitude);
                        startPoint.setLongitude(finalPosition.longitude);

                        Location endPoint = new Location("locationA");
                        endPoint.setLatitude(Double.parseDouble(trackUser.latitude));
                        endPoint.setLongitude(Double.parseDouble(trackUser.longitude));

                        double distance = startPoint.distanceTo(endPoint);
                        String roundValue = String.format("%.2f", distance);

                        float minDistance = (totalDistance - Float.parseFloat(roundValue));

                        if (Float.parseFloat(roundValue) <= 10f) {
                            isUnderTemMeter = true;
                            imgCustomer.setVisibility(View.GONE);
                            complete_tracking.setVisibility(View.VISIBLE);
                            rangeSeekBar.setVisibility(View.GONE);
                            rangeSeekBarTransparent.setVisibility(View.VISIBLE);
                            rangeSeekBarTransparent.setValue(100);

                            if (historyInfo.data.bookStatus.equals("3")) {
                                isCompleteService = true;
                                tvStatus.setText("Completed");
                            } else {
                                tvStatus.setText("Reached");
                            }
                            tvStatus.setTextColor(ContextCompat.getColor(TrackingActivity.this, R.color.main_green_color));
                            reachedToDestination(R.drawable.mett_user_ico, "", Double.parseDouble(trackingLat), Double.parseDouble(trackingLng));
                        } else {
                            if (userBitmap == null && artistBitmap == null) {
                                againGoAway(mapBeanArrayList, 0);
                            }

                            if (isReached) {
                                mGoogleMap.clear();
                                wattingForImage(mapBeanArrayList);
                                isReached = false;
                            }

                            isUnderTemMeter = false;
                            if (minDistance < 0) {
                                rangeSeekBar.setValue(0, totalDistance);
                            } else rangeSeekBar.setValue(minDistance, totalDistance);
                            complete_tracking.setVisibility(View.GONE);

                            imgCustomer.setVisibility(View.VISIBLE);
                            if (userBitmap != null)
                                imgCustomer.setImageBitmap(userBitmap);

                            rangeSeekBar.setVisibility(View.VISIBLE);
                            if (artistBitmap != null)
                                rangeSeekBar.getLeftSeekBar().setIndicatorBitmap(artistBitmap);
                            rangeSeekBarTransparent.setVisibility(View.GONE);

                            if (historyInfo.data.bookStatus.equals("3")) {
                                tvStatus.setText("Completed");
                                isCompleteService = true;
                                tvStatus.setTextColor(ContextCompat.getColor(TrackingActivity.this, R.color.main_green_color));
                            } else {
                                tvStatus.setText("On the way");
                                tvStatus.setTextColor(ContextCompat.getColor(TrackingActivity.this, R.color.main_orange_color));
                            }


                        }

                        // rangeSeekBar.setValue(Float.parseFloat(roundValue), totalDistance);
                        if (tvDistance != null){
                            Double distance2 = Double.parseDouble(roundValue) / 1609.344;
                            String roundValue2 = String.format("%.2f", distance2);
                            tvDistance.setText(roundValue2 + " Miles");
                        }



                    } else {
                        MyToast.getInstance(TrackingActivity.this).showDasuAlert(message);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
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
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(this.getClass().getName());
    }

    /*.......................................................................................*/

    static void animateMarkerToICS(Marker marker, LatLng finalPosition) {
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return interpolate(fraction, startValue, endValue);
            }
        };
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        animator.setDuration(3000);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                handler.post(moveThread);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }

    public static LatLng interpolate(float fraction, LatLng a, LatLng b) {
        // function to calculate the in between values of old latlng and new latlng.
        // To get more accurate tracking(Car will always be in the road even when the latlng falls away from road), use roads api from Google apis.
        // As it has quota limits I didn't have used that method.
        double lat = (b.latitude - a.latitude) * fraction + a.latitude;
        double lngDelta = b.longitude - a.longitude;

        // Take the shortest path across the 180th meridian.
        if (Math.abs(lngDelta) > 180) {
            lngDelta -= Math.signum(lngDelta) * 360;
        }
        double lng = lngDelta * fraction + a.longitude;
        return new LatLng(lat, lng);
    }

    private class MoveThread implements Runnable {
        LatLng newPoint;
        float zoom = 16;

        void setNewPoint(LatLng latLng, float zoom) {
            this.newPoint = latLng;
            this.zoom = zoom;
        }

        @Override
        public void run() {
            final CameraUpdate point = CameraUpdateFactory.newLatLngZoom(newPoint, zoom);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGoogleMap.animateCamera(point);
                }
            });

        }
    }


}

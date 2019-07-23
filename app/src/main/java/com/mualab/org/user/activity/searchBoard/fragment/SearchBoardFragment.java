package com.mualab.org.user.activity.searchBoard.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.Views.refreshviews.CircleHeaderView;
import com.mualab.org.user.Views.refreshviews.OnRefreshListener;
import com.mualab.org.user.Views.refreshviews.RjRefreshLayout;
import com.mualab.org.user.activity.base.BaseFragment;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.searchBoard.activity.RefineArtistActivity;
import com.mualab.org.user.activity.searchBoard.adapter.SearchBoardAdapter;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.ArtistServices;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.data.model.SearchBoard.RefineSearchBoard;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.listener.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.LocationDetector;
import com.mualab.org.user.utils.constants.Constant;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.mualab.org.user.utils.constants.Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE;

public class SearchBoardFragment extends BaseFragment implements View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static String TAG = SearchBoardFragment.class.getName();
    private RecyclerView rvSearchBoard;
    private ImageView ivFav;
    private TextView tv_msg;
    private ProgressBar progress_bar;
    private LinearLayout ll_loadingBox;
    private EditText searchview;
    private SearchBoardAdapter listAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private List<ArtistsSearchBoard> artistsList;
    private LinkedHashMap<String,ArtistsSearchBoard> mapArtistList;
    public static boolean isFavClick = false;
    private boolean isPulltoRefrash;
    private RefineSearchBoard item;
    private String subServiceId = "", mainServId = "", searchKeyword = "", sortType = "0", sortSearch = "distance", serviceType = "", time = "", day = "", date;
    private String lat = "", lng = "";
    private RjRefreshLayout mRefreshLayout;
    protected LocationRequest locationRequest;
    private ImageView ivChat, ivFilter;
    protected FusedLocationProviderClient mFusedLocationClient;
    private TextView tv_location;
    private RelativeLayout ly_change_location;
    private boolean isAlreadySelectedLocation = false;
    private Session session;
    private RefineSearchBoard locationData = null;
    private boolean isFilterNChngLocaApply = false;
    private int pagenum = 0;
    private long mLastClickTime = 0;


    public static SearchBoardFragment newInstance(RefineSearchBoard item, RefineSearchBoard locationData) {
        SearchBoardFragment fragment = new SearchBoardFragment();
        Bundle args = new Bundle();
        args.putSerializable("param1", item);
        args.putSerializable("locationData", locationData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        //if(context instanceof MainActivity)
        //((MainActivity)context).setBgColor(R.color.p);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            item = (RefineSearchBoard) getArguments().getSerializable("param1");
            locationData = (RefineSearchBoard) getArguments().getSerializable("locationData");
        }

        if (artistsList == null)
            artistsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_board_demo, container, false);
        // Inflate the layout for this fragment
        rvSearchBoard = view.findViewById(R.id.rvSearchBoard);
        ivFav = view.findViewById(R.id.ivFav);
        ivFilter = view.findViewById(R.id.ivFilter);
        tv_msg = view.findViewById(R.id.tv_msg);
        progress_bar = view.findViewById(R.id.progress_bar);
        ll_loadingBox = view.findViewById(R.id.ll_loadingBox);
        searchview = view.findViewById(R.id.searchview);
        tv_location = view.findViewById(R.id.tv_location);
        ly_change_location = view.findViewById(R.id.ly_change_location);
        ImageView ivChat = getActivity().findViewById(R.id.ivChat);
        ivChat.setVisibility(View.VISIBLE);
        initView();

        ivFav.setOnClickListener(this);
        ivFilter.setOnClickListener(this);
        ly_change_location.setOnClickListener(this);

        session = new Session(mContext);
        mapArtistList = new LinkedHashMap<>();

        if (session.getSaveSearch() != null) {
            isFilterNChngLocaApply = true;
            ivFilter.setImageResource(R.drawable.active_filter_ico);
        } else isFilterNChngLocaApply = false;


        if (session.getCurrentLatltude() != null && session.getCurrentLongitude() != null) {
            if (item == null) {
                if (locationData != null) {
                    lat = locationData.latitude;
                    lng = locationData.longitude;
                    tv_location.setText(locationData.location);
                } else {
                    lat = session.getCurrentLatltude();
                    lng = session.getCurrentLongitude();
                }
            }
        }

        return view;
    }

    private void initView() {
        if (item != null) {
            lat = item.latitude;
            lng = item.longitude;
            subServiceId = item.subservice;
            mainServId = item.service;
            serviceType = item.serviceType;
            sortSearch = item.sortSearch;
            sortType = item.sortType;
            isFavClick = item.isFavClick;
            time = item.time;
            day = item.day;
            date = item.date;

            isFavClick = item.isFavClick;

            if (!lat.equals("") && !lng.equals("")) {
                Mualab.currentLocationForBooking.lat = Double.parseDouble(lat);
                Mualab.currentLocationForBooking.lng = Double.parseDouble(lng);
                tv_location.setText(item.location);

            }
        }


        if (isFavClick) {
            ivFav.setImageResource(R.drawable.active_star_icon);
        } else {
            ivFav.setImageResource(R.drawable.inactive_star_co);
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRefreshLayout = view.findViewById(R.id.mSwipeRefreshLayout);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        rvSearchBoard.setLayoutManager(layoutManager);

        listAdapter = new SearchBoardAdapter(mContext, artistsList, new SearchBoardAdapter.getClick() {
            @Override
            public void bookData(ArtistsSearchBoard searchBoard) {
                Intent intent = new Intent(mContext, BookingActivity.class);

                if (!mainServId.equals("")) {// Apply file case
                    intent.putExtra("_id", 0);
                    intent.putExtra("artistId", searchBoard._id);
                    intent.putExtra("callType", "In Call");

                    intent.putExtra("mainServiceName", "");
                    intent.putExtra("subServiceName", "yes");
                    intent.putExtra("isEditService", true);
                    intent.putExtra("isFromSearchBoard", true);

                    if (!mainServId.equals("")) {
                        if (mainServId.contains(",")) {
                            String id = mainServId.split(",")[0];
                            intent.putExtra("serviceId", Integer.parseInt(id));
                        } else {
                            intent.putExtra("serviceId", Integer.parseInt(mainServId));
                        }
                    }

                    if (!subServiceId.equals("")) {
                        if (subServiceId.contains(",")) {
                            String id = subServiceId.split(",")[0];
                            intent.putExtra("subServiceId", Integer.parseInt(id));
                        } else intent.putExtra("subServiceId", Integer.parseInt(subServiceId));
                    }

                    if (item.serviceType != null)
                        if (item.serviceType.equals("1")) {
                            searchBoard.isOutCallSelected = true;
                            intent.putExtra("callType", "Out Call");
                        }

                    intent.putExtra("incallStaff", searchBoard.isOutCallSelected);
                    intent.putExtra("outcallStaff", searchBoard.isOutCallSelected);
                    intent.putExtra("bookingDate", item.date);
                    startActivity(intent);
                } else {
                    apiForGetLatestService(searchBoard._id, intent);
                }


            }

            @Override
            public void ClickFirstTag(ArtistsSearchBoard searchBoard) {

                ArtistServices services = searchBoard.service.get(0);

                tagToBookService(searchBoard, services);
            }

            @Override
            public void ClickSecandTag(ArtistsSearchBoard searchBoard) {
                ArtistServices services = searchBoard.service.get(1);
                tagToBookService(searchBoard, services);
            }
        });

        rvSearchBoard.setAdapter(listAdapter);
        rvSearchBoard.setItemAnimator(null);
        rvSearchBoard.setHasFixedSize(true);
        searchKeyword = "";
        KeyboardUtil.hideKeyboard(searchview, mContext);

        apiForDeleteAllPendingBooking();

        if (scrollListener == null)
            scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    // if (page==1 && totalItemsCount>5){
                    listAdapter.showLoading(true);
                    pagenum = page;
                    hitApi(page, lat, lng);
                    //  }
                }
            };

        // Adds the scroll listener to RecyclerView
        rvSearchBoard.addOnScrollListener(scrollListener);

        final CircleHeaderView header = new CircleHeaderView(getContext());

        mRefreshLayout.addHeader(header);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                pagenum = 0;
                scrollListener.resetState();
                isPulltoRefrash = true;
                if (isFavClick)
                    apiForGetFavArtist(0, false);
                else
                    apiForGetArtist(0, false);
                // apiForGetArtist(0, false);
            }

            @Override
            public void onLoadMore() {
                Log.e(TAG, "onLoadMore: ");
            }
        });

        searchview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchKeyword = s.toString();
                Mualab.getInstance().cancelPendingRequests(TAG);
                ll_loadingBox.setVisibility(View.VISIBLE);

                progress_bar.setVisibility(View.VISIBLE);
                tv_msg.setVisibility(View.GONE);
                pagenum = 0;
                mapArtistList.clear();
                scrollListener.resetState();
                if (isFavClick)
                    apiForGetFavArtist(0, false);
                else
                    apiForGetArtist(0, false);

                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        updateLocation();

        if (artistsList.size() == 0) {
            tv_msg.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
            ll_loadingBox.setVisibility(View.VISIBLE);
            progress_bar.setVisibility(View.VISIBLE);
            tv_msg.setText(getString(R.string.loading));
            if (!String.valueOf(Mualab.currentLocation.lat).equals(null)) {
                if (isFavClick)
                    apiForGetFavArtist(0, false);
                else
                    apiForGetArtist(0, false);
            }
           /* if (isFavClick)
                apiForGetFavArtist(0, false);
            else
                apiForGetArtist(0, false);*/

            getDeviceLocation();

        }

    }


    private synchronized void apiForGetLatestService(final String artistId, final Intent intent) {
        Progress.show(mContext);
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetLatestService(artistId, intent);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        try {
            params.put("userId", String.valueOf(Mualab.currentUser.id));
            params.put("artistId", artistId);
        } catch (Exception e) {

        }
        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "getMostBookedService", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(mContext);
                try {

                    tv_msg.setVisibility(View.GONE);
                    tv_msg.setTextColor(mContext.getResources().getColor(R.color.text_color));
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {

                        JSONObject object = js.getJSONObject("artistServices");
                        int _id = object.getInt("_id");
                        int serviceId = object.getInt("serviceId");
                        int subserviceId = object.getInt("subserviceId");
                        int bookingCount = object.getInt("bookingCount");

                        intent.putExtra("_id", _id);
                        intent.putExtra("artistId", artistId);
                        intent.putExtra("callType", "In Call");

                        intent.putExtra("mainServiceName", "");
                        intent.putExtra("subServiceName", "yes");

                        intent.putExtra("serviceId", serviceId);
                        intent.putExtra("subServiceId", subserviceId);

                        intent.putExtra("isEditService", true);
                        intent.putExtra("isFromSearchBoard", true);


                        if (item != null) {
                            if (item.serviceType != null)
                                if (item.serviceType.equals("1")) {
                                    //searchBoard.isOutCallSelected = true;
                                    intent.putExtra("callType", "Out Call");
                                }

                            if (item.date != null)
                                if (!item.date.isEmpty())
                                    intent.putExtra("bookingDate", item.date);
                        }


                        startActivity(intent);

                    } else if (status.equals("fail")) {
                        progress_bar.setVisibility(View.GONE);
                    }

                    //  showToast(message);
                } catch (Exception e) {
                    tv_msg.setText(getString(R.string.msg_some_thing_went_wrong));
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(mContext);
            }
        })
                .setAuthToken(Mualab.currentUser.authToken)
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(TAG);
    }

    private void tagToBookService(ArtistsSearchBoard searchBoard, ArtistServices services) {
        Intent intent = new Intent(mContext, BookingActivity.class);
        intent.putExtra("_id", Integer.parseInt(services._id));
        intent.putExtra("artistId", searchBoard._id);
        intent.putExtra("callType", "In Call");

        intent.putExtra("mainServiceName", "");
        intent.putExtra("subServiceName", "yes");

        intent.putExtra("serviceId", Integer.parseInt(services.serviceId));
        intent.putExtra("subServiceId", Integer.parseInt(services.subserviceId));

        intent.putExtra("isEditService", true);
        intent.putExtra("isFromSearchBoard", true);

        if (item != null) {
            if (item.serviceType != null)
                if (item.serviceType.equals("1")) {
                   // searchBoard.isOutCallSelected = true;
                    intent.putExtra("callType", "Out Call");
                }
            intent.putExtra("bookingDate", item.date);
        }

        //intent.putExtra("incallStaff", searchBoard.isOutCallSelected);
        intent.putExtra("outcallStaff", searchBoard.isOutCallSelected);

        startActivity(intent);
    }

    public void hitApi(int page, String latitude, String longitude) {
        lat = latitude;
        lng = longitude;
        if (item == null) {
            if (locationData != null) {
                lat = locationData.latitude;
                lng = locationData.longitude;
                tv_location.setText(locationData.location);
            } else
                getTextAddress(Double.parseDouble(latitude), Double.parseDouble(longitude));
        }

        if (isFavClick)
            apiForGetFavArtist(page, true);
        else
            apiForGetArtist(page, true);
    }

    private void showProgress() {
        ll_loadingBox.setVisibility(View.VISIBLE);
        tv_msg.setVisibility(View.GONE);
        progress_bar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        ll_loadingBox.setVisibility(View.GONE);
        progress_bar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivFilter:
                Intent intent = new Intent(mContext, RefineArtistActivity.class);
                item = new RefineSearchBoard();
                item.latitude = lat;
                item.longitude = lng;
                item.location = tv_location.getText().toString().trim();

                intent.putExtra("params", item);
                intent.putExtra("param2", isFavClick);

                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                getActivity().finish();
                break;

            case R.id.ivFav:
                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                pagenum = 0;
                artistsList.clear();
                mapArtistList.clear();
                if (!isFavClick) {
                    showProgress();
                    isFavClick = true;
                    ivFav.setImageResource(R.drawable.active_star_icon);
                    apiForGetFavArtist(0, false);
                } else {
                    showProgress();
                    isFavClick = false;
                    ivFav.setImageResource(R.drawable.inactive_star_co);
                    apiForGetArtist(0, false);

                }

                break;

            case R.id.ly_change_location:
                if (!ConnectionDetector.isConnected()) {
                    new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if (isConnected) {
                                dialog.dismiss();
                                getAddress();
                                isAlreadySelectedLocation = true;
                            }
                        }
                    }).show();
                } else {
                    getAddress();
                }
                KeyboardUtil.hideKeyboard(ly_change_location, mContext);

                break;

        }
    }

    private void getAddress() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(getBaseActivity());
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        getDeviceLocation();
                    }

                } else {
                    /* *//* if (isFavClick)
                        apiForGetFavArtist(0, false);
                    else
                        apiForGetArtist(0, false);*//*
                    lat="0.0";
                    lng="0.0";
*/

                }
            }

        }
    }


    private synchronized void apiForGetArtist(final int page, final boolean isLoadMore) {
        progress_bar.setVisibility(View.VISIBLE);
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetArtist(page, isLoadMore);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        try {
            params.put("latitude", lat);
            params.put("longitude", lng);


            if (item == null) {
                params.put("minPrice", "0");
                params.put("maxPrice", "0");
                params.put("rating", "");
                params.put("distance", "");
                params.put("certificate", "");
            } else {
                params.put("minPrice", item.priceMinFilter);
                params.put("maxPrice", item.priceFilter);
                params.put("distance", item.LocationFilter);
                params.put("maxPrice", item.priceFilter);
                params.put("certificate", item.certificate);

                if (item.rating != null) {
                    if (item.rating.contains(".")) {
                        item.rating = item.rating.substring(0, item.rating.indexOf("."));
                    }
                    params.put("rating", item.rating);
                } else params.put("rating", "");

            }

            // params.put("distance", "10");

            params.put("page", "" +pagenum);
            params.put("limit", "10");
            params.put("service", mainServId);
            params.put("serviceType", serviceType);
            params.put("day", day);
            params.put("time", time);
            params.put("subservice", subServiceId);
            params.put("sortSearch", sortSearch);
            params.put("sortType", sortType);
            params.put("text", searchKeyword);
            params.put("userId", String.valueOf(Mualab.currentUser.id));
            //params.put("appType", "user");

        } catch (Exception e) {

        }


        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "artistSearch", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    progress_bar.setVisibility(View.GONE);
                    tv_msg.setVisibility(View.GONE);
                    tv_msg.setTextColor(mContext.getResources().getColor(R.color.text_color));
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    int totalCount = 1;

                    if (js.has("totalCount")) {
                        totalCount = js.getInt("totalCount");
                    }

                    System.out.println("searchKeyword====" + searchKeyword + "&page " + page);

                    //if (page == 0)artistsList.clear();

                    if (status.equalsIgnoreCase("success")) {

                        listAdapter.showLoading(false);
                        JSONArray artistArray = js.getJSONArray("artistList");

                        if (totalCount == 0) {
                            ll_loadingBox.setVisibility(View.VISIBLE);
                            progress_bar.setVisibility(View.GONE);
                            tv_msg.setVisibility(View.VISIBLE);
                            tv_msg.setText(R.string.no_artist_found);
                            artistsList.clear();
                            mapArtistList.clear();
                        } else {
                            tv_msg.setVisibility(View.GONE);
                        }

                        Gson gson = new Gson();
                        if (artistArray != null && artistArray.length() > 0) {
                            if (isPulltoRefrash) {
                                isPulltoRefrash = false;
                                mRefreshLayout.stopRefresh(true, 500);
                                int prevSize = artistsList.size();
                                pagenum = 0;
                                artistsList.clear();
                                mapArtistList.clear();
                                listAdapter.notifyItemRangeRemoved(0, prevSize);
                            }
                            //rvSearchBoard.setVisibility(View.VISIBLE);
                            ll_loadingBox.setVisibility(View.GONE);

                            for (int i = 0; i < artistArray.length(); i++) {
                                JSONObject jsonObject = artistArray.getJSONObject(i);
                                ArtistsSearchBoard item = gson.fromJson(String.valueOf(jsonObject), ArtistsSearchBoard.class);
                                String services = "";
                                if (item.subcate.size() != 0) {
                                    if (item.subcate.size() < 2) {
                                        services = item.subcate.get(0).subServiceName;
                                    } else {
                                        for (int j = 0; j < 2; j++) {
                                            if (services.equals("")) {
                                                services = item.subcate.get(j).subServiceName;
                                            } else {
                                                services = services + ", " + item.subcate.get(j).subServiceName;
                                            }
                                        }
                                    }
                                } else {
                                    services = "NA";
                                }
                                item.categoryName = services;
                                item.isFav = false;
                                mapArtistList.put(item._id,item);
                                //artistsList.add(item);
                            }
                        }

                        artistsList.clear();
                        Collection<ArtistsSearchBoard> values = mapArtistList.values();
                        artistsList.addAll(values);

                    } else if (status.equals("fail")) {
                      //  progress_bar.setVisibility(View.GONE);
                        //tv_msg.setVisibility(View.VISIBLE);
                        //tv_msg.setText(R.string.no_artist_found);

                    }

                    listAdapter.notifyDataSetChanged();
                    if (artistsList.size() == 0) {
                        progress_bar.setVisibility(View.GONE);
                        ll_loadingBox.setVisibility(View.VISIBLE);


                        Handler handler = new Handler();
                        Runnable r = new Runnable() {
                            public void run() {
                                tv_msg.setVisibility(View.VISIBLE);
                                tv_msg.setText(R.string.no_artist_found);
                            }
                        };
                        handler.postDelayed(r, 300);


                        if (isPulltoRefrash) {
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(false, 500);

                        }
                    }

                    //  showToast(message);
                } catch (Exception e) {
                    tv_msg.setText(getString(R.string.msg_some_thing_went_wrong));
                    e.printStackTrace();
                    listAdapter.notifyDataSetChanged();
                    if (artistsList.size() == 0) {
                        tv_msg.setText(R.string.no_artist_found);
                        progress_bar.setVisibility(View.GONE);
                        tv_msg.setVisibility(View.VISIBLE);
                        ll_loadingBox.setVisibility(View.VISIBLE);
                        if (isPulltoRefrash) {
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(false, 500);

                        }
                    }
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                progress_bar.setVisibility(View.GONE);

                if (mContext != null && getActivity() != null) {
                    if (isAdded()) {
                        if (tv_msg != null)
                            tv_msg.setText(getString(R.string.msg_some_thing_went_wrong) + "");
                        if (isPulltoRefrash) {
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(false, 500);
                            int prevSize = artistsList.size();
                            pagenum = 0;
                            artistsList.clear();
                            mapArtistList.clear();
                            listAdapter.notifyItemRangeRemoved(0, prevSize);
                        }
                    }

                }
            }
        })
                .setAuthToken(Mualab.currentUser.authToken)
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(TAG);
    }


    private synchronized void apiForGetFavArtist(final int page, final boolean isLoadMore) {

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetFavArtist(page, isLoadMore);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        try {
            params.put("latitude", lat);
            params.put("longitude", lng);
            // params.put("distance", "10");


            if (item == null) {
                params.put("minPrice", "0");
                params.put("maxPrice", "0");
                params.put("rating", "");
                params.put("distance", "");
                params.put("certificate", "");
            } else {
                params.put("minPrice", item.priceMinFilter);
                params.put("maxPrice", item.priceFilter);

                params.put("distance", item.LocationFilter);
                params.put("certificate", item.certificate);

                if (item.rating.contains(".")) {
                    item.rating = item.rating.substring(0, item.rating.indexOf("."));
                }
                params.put("rating", item.rating);
            }


            params.put("page", "" + pagenum);
            params.put("limit", "10");
            params.put("service", mainServId);
            params.put("serviceType", serviceType);
            params.put("day", day);
            params.put("time", time);
            params.put("subservice", subServiceId);
            params.put("sortSearch", sortSearch);
            params.put("sortType", sortType);
            params.put("text", searchKeyword);

            params.put("userId", String.valueOf(Mualab.currentUser.id));
            // params.put("appType", "user");
        } catch (Exception e) {

        }


        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "favoriteList", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    progress_bar.setVisibility(View.GONE);
                    tv_msg.setVisibility(View.VISIBLE);
                    tv_msg.setTextColor(mContext.getResources().getColor(R.color.text_color));
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    System.out.println("searchKeyword====" + searchKeyword + "&page " + page);

                    //if (page == 0) artistsList.clear();

                    if (status.equalsIgnoreCase("success")) {

                        listAdapter.showLoading(false);
                        JSONArray artistArray = js.getJSONArray("artistList");
                        Gson gson = new Gson();
                        if (artistArray != null && artistArray.length() > 0) {
                            //rvSearchBoard.setVisibility(View.VISIBLE);
                            if (isPulltoRefrash) {
                                isPulltoRefrash = false;
                                mRefreshLayout.stopRefresh(true, 500);
                                pagenum = 0;
                                int prevSize = artistsList.size();
                                artistsList.clear();
                                mapArtistList.clear();
                                listAdapter.notifyItemRangeRemoved(0, prevSize);
                            }
                            ll_loadingBox.setVisibility(View.GONE);

                            for (int i = 0; i < artistArray.length(); i++) {
                                JSONObject jsonObject = artistArray.getJSONObject(i);
                                ArtistsSearchBoard item = gson.fromJson(String.valueOf(jsonObject), ArtistsSearchBoard.class);
                                String services = "";
                                if (item.service.size() != 0) {
                                    if (item.service.size() < 2) {
                                        services = item.service.get(0).title;
                                    } else {
                                        for (int j = 0; j < 2; j++) {
                                            if (services.equals("")) {
                                                services = item.service.get(j).title;
                                            } else {
                                                services = services + ", " + item.service.get(j).title;
                                            }
                                        }
                                    }
                                } else {
                                    services = "NA";
                                }
                                item.categoryName = services;
                                item.isFav = true;
                                mapArtistList.put(item._id,item);

                            }

                            artistsList.clear();
                            Collection<ArtistsSearchBoard> values = mapArtistList.values();
                            artistsList.addAll(values);
                        }else {
                            //artistsList.clear();
                        }
                    } else if (status.equals("fail")) {
                        progress_bar.setVisibility(View.GONE);
                        tv_msg.setVisibility(View.VISIBLE);
                        tv_msg.setText(R.string.no_artist_found);

                    }

                    listAdapter.notifyDataSetChanged();
                    if (artistsList.size() == 0) {
                        tv_msg.setText(R.string.no_artist_found);
                        ll_loadingBox.setVisibility(View.VISIBLE);
                        if (isPulltoRefrash) {
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(false, 500);

                        }
                    }


                    //  showToast(message);
                } catch (Exception e) {
                    tv_msg.setText(getString(R.string.msg_some_thing_went_wrong));
                    e.printStackTrace();
                    listAdapter.notifyDataSetChanged();
                    if (artistsList.size() == 0) {
                        tv_msg.setText(R.string.no_artist_found);
                        ll_loadingBox.setVisibility(View.VISIBLE);
                        if (isPulltoRefrash) {
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(false, 500);

                        }
                    }
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try {
                    progress_bar.setVisibility(View.GONE);
                    tv_msg.setText(getString(R.string.msg_some_thing_went_wrong));
                    if (isPulltoRefrash) {
                        isPulltoRefrash = false;
                        mRefreshLayout.stopRefresh(false, 500);
                        pagenum = 0;
                        int prevSize = artistsList.size();
                        artistsList.clear();
                        listAdapter.notifyItemRangeRemoved(0, prevSize);
                    }
                } catch (Exception e) {

                }

            }
        })
                .setAuthToken(Mualab.currentUser.authToken)
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(TAG);
    }

    private void apiForDeleteAllPendingBooking() {
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForDeleteAllPendingBooking();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "deleteUserBookService", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        //    BookingFragment4.arrayListbookingInfo.clear();
                        Session session = Mualab.getInstance().getSessionManager();
                        session.setUserChangedLocName("");

                    } else {
                    }
                } catch (Exception e) {
                    Progress.hide(mContext);
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
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

    private void getDeviceLocation() {

        if (Build.VERSION.SDK_INT >= 23) {

            if (mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constant.MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                LocationDetector locationDetector = new LocationDetector();
                FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
                if (locationDetector.isLocationEnabled(getActivity()) &&
                        locationDetector.checkLocationPermission(getActivity())) {

                    mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                Mualab.currentLocation.lat = latitude;
                                Mualab.currentLocation.lng = longitude;
                                Mualab.currentLocationForBooking.lat = latitude;
                                Mualab.currentLocationForBooking.lng = longitude;

                                if (lng.equals("") && lat.equals("")) {
                                    lat = String.valueOf(latitude);
                                    lng = String.valueOf(longitude);
                                }


                                if (locationData != null) {
                                    lat = locationData.latitude;
                                    lng = locationData.longitude;
                                    tv_location.setText(locationData.location);
                                } else {
                                    if (item == null) {
                                        getTextAddress(latitude, longitude);
                                    }
                                }


                                session.saveCurrentLocation(String.valueOf(latitude), String.valueOf(longitude));

                                if (isFavClick)
                                    apiForGetFavArtist(0, false);
                                else
                                    apiForGetArtist(0, false);


                            }
                        }
                    });

                } else {
                    progress_bar.setVisibility(View.GONE);
                    tv_msg.setText(R.string.gps_permission_alert);
                    locationDetector.showLocationSettingDailod(getActivity());
                }
            }
        } else {
            if (isFavClick)
                apiForGetFavArtist(0, false);
            else
                apiForGetArtist(0, false);
        }

    }

    private void getTextAddress(Double lat, Double lng) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try {

            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);

            String add = obj.getAddressLine(0);
            add = add + "," + obj.getAdminArea();
            tv_location.setText(add);
            Mualab.currentLocation.CurrrentAddress = add;

            if (!isFilterNChngLocaApply){
                if (isFavClick)
                    apiForGetFavArtist(0, false);
                else
                    apiForGetArtist(0, false);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 123:
                pagenum = 0;
                if (isFavClick)
                    apiForGetFavArtist(0, false);
                else
                    apiForGetArtist(0, false);
        }


        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(mContext, data);
                if (isAlreadySelectedLocation) {
                    isAlreadySelectedLocation = false;
                }
                tv_location.setText(place.getAddress());
                //location = "" + place.getName();
                LatLng latLng = place.getLatLng();
                lat = String.valueOf(latLng.latitude);
                lng = String.valueOf(latLng.longitude);
                isFilterNChngLocaApply = true;
                pagenum = 0;
                mapArtistList.clear();
                if (isFavClick)
                    apiForGetFavArtist(0, false);
                else
                    apiForGetArtist(0, false);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(mContext, data);
                // TODO: Handle the error.

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        KeyboardUtil.hideKeyboard(ly_change_location, mContext);

    }


    protected void onGpsAutomatic() {
        int permissionLocation = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {

            locationRequest = new LocationRequest();
            locationRequest.setInterval(3000);
            locationRequest.setFastestInterval(3000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);
            builder.setNeedBle(true);

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());

            Task<LocationSettingsResponse> task =
                    LocationServices.getSettingsClient(mContext).checkLocationSettings(builder.build());

            task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                @Override
                public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                    try {
                        //getting target response use below code
                        LocationSettingsResponse response = task.getResult(ApiException.class);

                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        int permissionLocation = ContextCompat
                                .checkSelfPermission(mContext,
                                        Manifest.permission.ACCESS_FINE_LOCATION);
                        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                            mFusedLocationClient.getLastLocation().addOnSuccessListener((Activity) mContext, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        // Logic to handle location object
                                        setLatLng(location);
                                    } else {
                                        //Location not available
                                        Log.e("Test", "Location not available");
                                    }
                                }
                            });


                        }
                    } catch (ApiException exception) {
                        switch (exception.getStatusCode()) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied. But could be fixed by showing the
                                // user a dialog.
                                try {
                                    // Cast to a resolvable exception.
                                    ResolvableApiException resolvable = (ResolvableApiException) exception;
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    resolvable.startResolutionForResult(
                                            (Activity) mContext,
                                            Constant.REQUEST_CHECK_SETTINGS_GPS);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                } catch (ClassCastException e) {
                                    // Ignore, should be an impossible error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way to fix the
                                // settings so we won't show the dialog.
                                break;
                        }
                    }
                }
            });
        }
    }

    /**
     * this method get location when available and store in static variable
     */
    public void updateLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Logic to handle location object
                    setLatLng(location);
                } else {
                    //Location not available
                    onGpsAutomatic();
                }
            }
        });
    }

    protected void setLatLng(@NonNull Location location) {
        Mualab.currentLocation.lat = location.getLatitude();
        Mualab.currentLocation.lng = location.getLongitude();


        /*if (address.isEmpty()) {
            address = getAddressFromLatLng(Agrinvest.LATITUDE, Agrinvest.LONGITUDE);
            AppLogger.e("Location ", address);
        }*/
    }

    @Override
    public void onResume() {
        searchview.clearFocus();
        KeyboardUtil.hideKeyboard(getView(), mContext);
        super.onResume();
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (!isFilterNChngLocaApply)
            getDeviceLocation();

        if (mFusedLocationClient == null)
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mFusedLocationClient == null)
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @NonNull
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                setLatLng(location);
            }
        }
    };


}
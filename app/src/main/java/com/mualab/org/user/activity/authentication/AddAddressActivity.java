package com.mualab.org.user.activity.authentication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mualab.org.user.R;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.LocationDetector;
import com.mualab.org.user.utils.StatusBarUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddAddressActivity extends AppCompatActivity {

    private int PLACE_PICKER_REQUEST = 1;
    private EditText ed_locality;
    private EditText edInputPostcode;
    private RelativeLayout rl_parent;


    private String country, state, city, stAddress1, stAddress2, postalCode, placeName, houseNumber;// fullAddress;
    private String latitude, longitude;

    private Intent intent;
    private String errorMsg;
    private String activity;
    private ImageView iv_picode_search;
    //private FetchAddressIntentService fetchAddressIntentService;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);
        iv_picode_search = findViewById(R.id.iv_picode_search);
        StatusBarUtil.setColorNoTranslucent(this, getResources().getColor(R.color.white));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }


        Intent intentA = getIntent();
        if (intentA != null) {
            activity = intentA.getStringExtra("activity");
        }

        LocationDetector locationDetector = new LocationDetector();
        if (locationDetector.isLocationEnabled(AddAddressActivity.this)) {

        } else {
            locationDetector.showLocationSettingDailod(AddAddressActivity.this);
        }

        rl_parent = findViewById(R.id.rl_parent);
        ed_locality = findViewById(R.id.ed_locality);
        edInputPostcode = findViewById(R.id.edInputPostcode);

        findViewById(R.id.img_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.txt_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // postalCode = ed_pinCode.getText().toString().trim();
                stAddress1 = ed_locality.getText().toString().trim();
                //fullAddress = ed_locality.getText().toString().trim();
                if (validateAddress()) {

                    if (TextUtils.isEmpty(placeName) || placeName.contains("S") ||
                            placeName.contains("N") || placeName.contains("E") || placeName.contains("°"))
                        placeName = stAddress1;
                    com.mualab.org.user.data.model.booking.Address address = new com.mualab.org.user.data.model.booking.Address();
                    address.city = city;
                    address.country = country;
                    address.state = state;
                    address.postalCode = postalCode;
                    address.stAddress1 = stAddress1;
                    address.stAddress2 = stAddress2;
                    address.placeName = placeName;
                    address.houseNumber = houseNumber;
                    //address.fullAddress = fullAddress;
                    address.latitude = latitude;
                    address.longitude = longitude;
                    setResult(address);
                } else
                    MyToast.getInstance(AddAddressActivity.this).showDasuAlert(getString(R.string.error_required_field));

            }
        });


/*
        if (activity.equals("EditProfile")){
           rl_parent.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }else
            rl_parent.setBackground(getDrawable(R.drawable.profile_pic_bg));

*/
      /*  Intent dataIntent = getIntent();
        if(intent!=null){
            Address address = (Address) dataIntent.getSerializableExtra("address");

        }*/


        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            intent = builder.build(AddAddressActivity.this);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
            errorMsg = e.getLocalizedMessage();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            errorMsg = e.getLocalizedMessage();
        }

        findViewById(R.id.ll_picAddress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intent != null)
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                else Toast.makeText(AddAddressActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        edInputPostcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().equals("")) {
                    iv_picode_search.setVisibility(View.GONE);
                } else iv_picode_search.setVisibility(View.VISIBLE);
            }
        });

        iv_picode_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postcode = edInputPostcode.getText().toString().trim();
                hideKeyboard(edInputPostcode);
                if (!TextUtils.isEmpty(postcode))
                    getAddressByPostCode(postcode);
            }
        });

        edInputPostcode.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    String postcode = edInputPostcode.getText().toString().trim();
                    hideKeyboard(edInputPostcode);
                    if (!TextUtils.isEmpty(postcode))
                        getAddressByPostCode(postcode);
                    return true;
                }
                return false;
            }
        });

        ed_locality.setEnabled(false);

    }


    private void getAddressByPostCode(String postalCode) {
        String api = "https://api.postcodes.io/postcodes/" + postalCode + "";
        new HttpTask(new HttpTask.Builder(this, api, new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Log.d("responce", response);
                try {

                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getInt("status") == 200) {
                        JSONObject o = jsonObject.getJSONObject("result");
                        country = o.getString("country");
                        latitude = o.getString("latitude");
                        longitude = o.getString("longitude");
                        Double lat = Double.parseDouble(latitude);
                        Double lng = Double.parseDouble(longitude);
                        iv_picode_search.setVisibility(View.GONE);

                        city = "";
                        state = "";
                        country = "";
                        country = "";
                        stAddress1 = "";
                        stAddress2 ="";
                        placeName = "";

                        new GioAddress(AddAddressActivity.this, lat, lng).execute();

                    } else MyToast.getInstance(AddAddressActivity.this).showDasuAlert(getString(R.string.msg_some_thing_went_wrong));

                   /* if (latitude != null && longitude != null) {
                        ed_locality.setEnabled(true);
                    } else ed_locality.setEnabled(false);*/

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void ErrorListener(VolleyError error) {
                Log.d("responce", "e");
                ed_locality.setText("");
            }
        })
                .setBaseURL(api)
                .setProgress(true)
                .setMethod(Request.Method.GET))
                .execute("TAG");
    }


    private void setResult(com.mualab.org.user.data.model.booking.Address address) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("address", address);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save_btn, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_save:
                stAddress1 = ed_locality.getText().toString().trim();
                //fullAddress = ed_locality.getText().toString().trim();
                if (validateAddress()) {

                    if (TextUtils.isEmpty(placeName) || placeName.contains("S") ||
                            placeName.contains("N") || placeName.contains("E") || placeName.contains("°"))
                        placeName = stAddress1;
                    com.mualab.org.user.data.model.booking.Address address = new com.mualab.org.user.data.model.booking.Address();
                    address.city = city;
                    address.country = country;
                    address.state = state;
                    address.postalCode = postalCode;
                    address.stAddress1 = stAddress1;
                    address.stAddress2 = stAddress2;
                    address.placeName = placeName;
                    address.houseNumber = houseNumber;
                    //address.fullAddress = fullAddress;
                    address.latitude = latitude;
                    address.longitude = longitude;
                    setResult(address);
                } else
                    MyToast.getInstance(this).showDasuAlert(getString(R.string.error_required_field));

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private boolean validateAddress() {
        return !(TextUtils.isEmpty(stAddress1)
                || TextUtils.isEmpty(latitude) || TextUtils.isEmpty(longitude)
                //|| TextUtils.isEmpty(fullAddress)
        );
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                edInputPostcode.setText("");

                //String toastMsg = String.format("Place: %s", place.getName());
                getAddressDetails(place);
                // fullAddress = place.getAddress().toString();
                placeName = place.getName().toString();

                // ed_pinCode.setText(postalCode);
                stAddress1 = placeName+" "+stAddress1;


                // this was comments i am enable because of new changes may be it is wrong
                ed_locality.setText(stAddress1);

               // new GioAddress(AddAddressActivity.this, place.getLatLng().latitude, place.getLatLng().longitude).execute();

             /*   if(latitude != null && longitude != null){
                    ed_locality.setEnabled(true);
                }else ed_locality.setEnabled(false);*/
            }
        }
    }

    public void hideKeyboard(View view) {
        KeyboardUtil.hideKeyboard(view, this);
    }


    @SuppressLint("StaticFieldLeak")
    class GioAddress extends AsyncTask<Void, Void, Void> {

        Context mContext;
        Double lat, lng;
        android.location.Address address;

        GioAddress(Context mContext, Double lat, Double lng) {
            this.mContext = mContext;
            this.lat = lat;
            this.lng = lng;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses.size() > 0) {
                    address = addresses.get(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (address != null) {
                city = address.getLocality();
                state = address.getAdminArea();
                country = address.getCountryName();
                postalCode = address.getPostalCode();
                country = address.getCountryName();
                stAddress1 = address.getAddressLine(0);
                stAddress2 = address.getAddressLine(1);

                if(placeName.equals("null")){
                    placeName = "";
                }

                ed_locality.setText(TextUtils.isEmpty( placeName+" "+stAddress1) ? "" :  placeName+" "+stAddress1);
                //ed_locality.setText(TextUtils.isEmpty(stAddress1) ? "" :  stAddress2);
                // ed_pinCode.setText(postalCode);

            }
        }
    }


    public void getAddressDetails(Place place) {
        city = state = country = postalCode = stAddress1 = stAddress2 = latitude = longitude = "";
        if (place.getAddress() != null) {
            String[] addressSlice = place.getAddress().toString().split(", ");
            country = addressSlice[addressSlice.length - 1];
            if (addressSlice.length > 1) {
                String[] stateAndPostalCode = addressSlice[addressSlice.length - 2].split(" ");
                if (stateAndPostalCode.length > 1) {
                    postalCode = stateAndPostalCode[stateAndPostalCode.length - 1];
                    state = "";
                    for (int i = 0; i < stateAndPostalCode.length - 1; i++) {
                        state += (i == 0 ? "" : " ") + stateAndPostalCode[i];
                    }
                } else {
                    state = stateAndPostalCode[stateAndPostalCode.length - 1];
                }
            }
            if (addressSlice.length > 2)
                city = addressSlice[addressSlice.length - 3];
            if (addressSlice.length == 4)
                stAddress1 = addressSlice[0];
            else if (addressSlice.length > 3) {
                stAddress2 = addressSlice[addressSlice.length - 4];
                stAddress1 = "";
                for (int i = 0; i < addressSlice.length - 4; i++) {
                    stAddress1 += (i == 0 ? "" : ", ") + addressSlice[i];
                }
            }
        }

        if (place.getLatLng() != null) {
            latitude = "" + place.getLatLng().latitude;
            longitude = "" + place.getLatLng().longitude;
        }
    }

    public void showLocationSettingDailod() {

        android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(AddAddressActivity.this);
        builder1.setTitle("GPS Services Not Active");
        builder1.setMessage("Please enable Location Services and GPS");
        builder1.setCancelable(true);
        builder1.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        // context.startActivity(intent);
                        startActivityForResult(intent, 123);

                    }
                });

        android.app.AlertDialog alert11 = builder1.create();
        alert11.show();
        Button b = alert11.getButton(AlertDialog.BUTTON_POSITIVE);

        if (b != null) {
            b.setTextColor(AddAddressActivity.this.getResources().getColor(R.color.colorPrimary));
        }


    }

}

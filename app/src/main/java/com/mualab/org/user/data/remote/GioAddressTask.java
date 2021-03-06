package com.mualab.org.user.data.remote;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

/**
 * Created by dharmraj on 23/3/18.
 */

public class GioAddressTask extends AsyncTask<Void, Void, Void> {

    private WeakReference<Context> mContext;
    private LatLng latLng;
    private Address address;

    private LocationListner listner;

    public interface LocationListner{
        void onSuccess(com.mualab.org.user.data.model.booking.Address address);
    }

    public GioAddressTask(Context mContext, LatLng latLng, LocationListner listner){
        this.mContext = new WeakReference<>(mContext);
        this.latLng = latLng;
        this.listner = listner;
    }


    @Override
    protected Void doInBackground(Void... voids) {

        Geocoder geocoder = new Geocoder(mContext.get(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            //addresses.toString();
            if(addresses.size()>0){
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
        if(address!=null){
            com.mualab.org.user.data.model.booking.Address adr = new com.mualab.org.user.data.model.booking.Address();
            adr.city = address.getLocality();
            adr.state = address.getAdminArea();
            adr.country = address.getCountryName();
            adr.postalCode = address.getPostalCode();
            adr.stAddress1 = address.getAddressLine(0);
            adr.stAddress2 = address.getAddressLine(1);
            adr.latitude = String.valueOf(latLng.latitude);
            adr.longitude = String.valueOf(latLng.longitude);
            adr.locality =  address.getSubLocality();

            if(adr.city == null){
                adr.placeName = adr.state+", "+adr.country;
            }else adr.placeName = adr.city+", "+adr.country;

            if(listner!=null) listner.onSuccess(adr);
        }
    }
}

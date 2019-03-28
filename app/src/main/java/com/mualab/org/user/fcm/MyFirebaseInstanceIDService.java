package com.mualab.org.user.fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String device_token = FirebaseInstanceId.getInstance().getToken();

        Log.d("Device Token Service", device_token);
    }
}

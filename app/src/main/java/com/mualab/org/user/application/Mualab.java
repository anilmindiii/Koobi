package com.mualab.org.user.application;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.interceptors.HttpLoggingInterceptor;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;
import com.mualab.org.user.BuildConfig;
import com.mualab.org.user.R;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.Location;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.utils.LocationDetector;
import com.mualab.org.user.utils.Util;
import com.mualab.org.user.utils.constants.Constant;

import io.fabric.sdk.android.Fabric;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

/**
 * Created by mindiii on 21/12/17.
 **/

@SuppressLint("Registered")
public class Mualab extends Application  implements LifecycleObserver {

    public static final String TAG = Mualab.class.getSimpleName();
    public static boolean IS_DEBUG_MODE = BuildConfig.DEBUG;

    public static Mualab mInstance;
    public static User currentUser;
    public static Location currentLocation;
    public static Location currentLocationForBooking;
    // public static DatabaseReference ref;
    public static boolean isStoryUploaded;
    public static String currentChatUserId = "",currentGroupId = "";
    private Util utility;
    private Session session;
    private RequestQueue mRequestQueue;
    public Timer myTimer;

    //service tag
    private SharedPreferences mSharedPreferences;
    private static final String SHARED_PREF_NAME = "koobi_tag_preferences";

    public static Mualab getInstance() {
        if(mInstance != null){
            if (mInstance.mSharedPreferences == null) {
                mInstance.mSharedPreferences =
                        mInstance.getSharedPreferences(SHARED_PREF_NAME,
                                Context.MODE_PRIVATE);
            }

        }

        return mInstance;
    }

    public static Map<String, String> feedBasicInfo = new HashMap<>();


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mInstance = this;
        mInstance.getSessionManager();
        currentLocation = new Location();
        currentLocationForBooking = new Location();
        FirebaseApp.initializeApp(this);
        session.setIsOutCallFilter(false);
        myTimer = new Timer();
        getMyTimer();

        AndroidNetworking.initialize(this);

        utility = new Util(getApplicationContext());

        // ref = FirebaseDatabase.getInstance().getReference();

        // register observer
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }




    public Timer getMyTimer(){
        return myTimer;
    }



    public Session getSessionManager() {
        if (session == null)
            session = new Session(getApplicationContext());
        return session;
    }


    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        return mRequestQueue;
    }


    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public  void cancelAllPendingRequests() {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
       // MultiDex.install(this);
    }


    /*service tag*/
    private String getString(String key) {
        if (mSharedPreferences != null) {
            return mSharedPreferences.getString(key, "");
        }

        return "";
    }

    private void putString(String key, String value) {
        try {
            if (mSharedPreferences != null) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(key, value);
                editor.apply();
            }
        } catch (Exception e) {
            Log.e(SHARED_PREF_NAME, "Unable Put String in Shared preference", e);
        }
    }


/*    public ArrayList<TaggedPhoto> getTaggedPhotos() {
        String json = getString(Keys.TAGGED_PHOTOS.getKeyName());
        ArrayList<TaggedPhoto> taggedPhotoArrayList;
        if (!json.equals("")) {
            taggedPhotoArrayList =
                    new Gson().fromJson(json, new TypeToken<ArrayList<TaggedPhoto>>() {
                    }.getType());
        } else {
            taggedPhotoArrayList = new ArrayList<>();
        }
        return taggedPhotoArrayList;
    }

    public void setTaggedPhotos(ArrayList<TaggedPhoto> taggedPhotoArrayList) {
        putString(Keys.TAGGED_PHOTOS.getKeyName(), toJson(taggedPhotoArrayList));
    }*/

    public void clear() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        // app moved to foreground
        utility.goToOnlineStatus(mInstance, Constant.online);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        // app moved to background
        utility.goToOnlineStatus(mInstance, Constant.offline);
    }


    private enum Keys {
        TAGGED_PHOTOS("TAGGED_PHOTOS");
        private final String keyName;

        Keys(String label) {
            this.keyName = label;
        }

        public String getKeyName() {
            return keyName;
        }
    }


    public static String toJson(Object object) {
        try {
            Gson gson = new Gson();
            return gson.toJson(object);
        } catch (Exception e) {
            Log.e(SHARED_PREF_NAME, "Error In Converting ModelToJson", e);
        }
        return "";
    }

}

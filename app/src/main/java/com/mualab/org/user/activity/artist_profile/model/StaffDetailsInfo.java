package com.mualab.org.user.activity.artist_profile.model;

import java.util.List;

/**
 * Created by mindiii on 10/1/19.
 */

public class StaffDetailsInfo {

    public String status;
    public String message;
    public ServiceInfoBean serviceInfo;
    public List<StaffInfoBean> staffInfo;

    public static class ServiceInfoBean {

        public String title;
        public String description;
        public String inCallPrice;
        public String outCallPrice;
        public String completionTime;
        public int _id;
        public int artistId;
        public int serviceId;
        public int subserviceId;

    }

    public static class StaffInfoBean {

        public String _id;
        public int staffId;
        public String staffName;
        public String job;
        public double inCallPrice;
        public double outCallPrice;
        public String completionTime;
        public String staffImage;
        public String bookingType;
        public boolean isSelected;
        public List<?> staffHours;
    }
}

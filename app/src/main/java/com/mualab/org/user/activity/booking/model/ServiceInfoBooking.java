package com.mualab.org.user.activity.booking.model;

import java.util.List;

/**
 * Created by mindiii on 16/1/19.
 */

public class ServiceInfoBooking {

    public String status;
    public String message;
    public ServiceInfoBean serviceInfo;
    public List<StaffInfoBean> staffInfo;


    public static class ServiceInfoBean {

        public String title;
        public String description;
        public double inCallPrice;
        public double outCallPrice;
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
        public int status;
        public List<StaffHoursBean> staffHours;


        public static class StaffHoursBean {

            public int day;
            public String endTime;
            public String startTime;
        }
    }

/*    public String status;
    public String message;
    public ServiceInfoBean serviceInfo;
    public List<StaffInfoBean> staffInfo;

    public static class ServiceInfoBean {
        public String title;
        public String description;
        public Double inCallPrice;
        public Double outCallPrice;
        public String completionTime;
        public int _id;
        public int artistId;
        public int serviceId;
        public int subserviceId;

    }

    public static class StaffInfoBean {

        public int _id;
        public int staffId;
        public String staffName;
        public String job;
        public int status;
        public Double inCallPrice;
        public Double outCallPrice;
        public String completionTime;
        public String staffImage;
        public String bookingType;
        public boolean isSelected;
        public List<StaffHoursBean> staffHours;

        public static class StaffHoursBean {


            public int day;
            public String endTime;
            public String startTime;
        }
    }*/
}

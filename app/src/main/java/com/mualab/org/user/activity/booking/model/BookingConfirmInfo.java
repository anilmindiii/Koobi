package com.mualab.org.user.activity.booking.model;

import java.util.List;

/**
 * Created by mindiii on 22/1/19.
 */

public class BookingConfirmInfo {

    public String status;
    public String message;
    public List<DataBean> data;

    public static class DataBean {
        public int _id;
        public String bookingPrice;
        public int serviceId;
        public int subServiceId;
        public int artistServiceId;
        public String bookingDate;
        public String startTime;
        public String endTime;
        public String artistServiceName;
        public int staffId;
        public String staffName;
        public String staffImage;
        public int companyId;
        public String companyName;
        public String companyAddress;
        public String companyImage;
        public int status;
    }
}

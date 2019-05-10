package com.mualab.org.user.activity.booking.model;

import java.util.List;

/**
 * Created by mindiii on 29/1/19.
 */

public class BookingHistoryInfo {


    public String status;
    public String message;
    public List<DataBean> data;


    public static class DataBean {

        public int _id;
        public String bookingDate;
        public int bookingType;
        public String bookingTime;
        public String bookStatus;
        public String location;
        public int paymentType;
        public int paymentStatus;
        public int timeCount;
        public String paymentTime;
        public String reviewByUser;
        public int userRating;
        public Double artistRating;
        public int artistId;
        public String totalPrice;
        public List<UserDetailBean> userDetail;
        public List<ArtistDetailBean> artistDetail;
        public List<BookingInfoBean> bookingInfo;

        public static class UserDetailBean {

            public int _id;
            public String userName;
            public String profileImage;

        }

        public static class ArtistDetailBean {

            public int _id;
            public String userName;
            public String profileImage;

        }

        public static class BookingInfoBean {

            public int _id;
            public String bookingPrice;
            public int serviceId;
            public int subServiceId;
            public int artistServiceId;
            public String bookingDate;
            public String startTime;
            public String endTime;
            public int status;
            public String artistServiceName;
            public int staffId;
            public String staffName;
            public String staffImage;
            public int companyId;
            public String companyName;
            public String subServiceName;

        }
    }
}

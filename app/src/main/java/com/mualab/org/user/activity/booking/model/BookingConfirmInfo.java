package com.mualab.org.user.activity.booking.model;

import java.util.List;

/**
 * Created by mindiii on 22/1/19.
 */

public class BookingConfirmInfo {

    public String status;
    public String message;
    public UserDataBean userData;
    public List<DataBean> data;
    
    public static class UserDataBean {
        public String firstName;
        public String lastName;
        public String userName;
        public String businessName;
        public String businesspostalCode;
        public String buildingNumber;
        public String businessType;
        public String profileImage;
        public String email;
        public String password;
        public String gender;
        public String dob;
        public String address;
        public String address2;
        public String city;
        public String state;
        public String country;
        public String countryCode;
        public String contactNo;
        public String userType;
        public String socialId;
        public String socialType;
        public String deviceType;
        public String deviceToken;
        public String authToken;
        public String latitude;
        public String longitude;
        public String otpVerified;
        public String OTP;
        public String mailVerified;
        public String chatId;
        public String firebaseToken;
        public String followersCount;
        public String followingCount;
        public String serviceCount;
        public String certificateCount;
        public String postCount;
        public String reviewCount;
        public String ratingCount;
        public String bio;
        public int bankStatus;
        public int notificationStatus;
        public String status;
        public String radius;
        public int serviceType;
        public String inCallpreprationTime;
        public String outCallpreprationTime;
        public String businessEmail;
        public String businessContactNo;
        public String appType;
        public String trackingLatitude;
        public String trackingLongitude;
        public String trackingAddress;
        public String customerId;
        public int isDocument;
        public int commission;
        public String cardId;
        public int walkingBlock;
        public int bookingSetting;
        public String crd;
        public String upd;
        public String outCallLatitude;
        public String outCallLongitude;
        public int payOption;
        public int _id;
        public int __v;
        public List<Double> location;
        
    }

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
        public String artistName;
        public String companyAddress;
        public int bookingSetting;
        public String companyImage;
        
    }
}

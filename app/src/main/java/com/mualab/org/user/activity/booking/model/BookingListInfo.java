package com.mualab.org.user.activity.booking.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mindiii on 30/1/19.
 */

public class BookingListInfo implements Serializable {

    public String status;
    public String message;
    public DataBean data;

    public static class DataBean {
        public int _id;
        public String discountPrice;
        public String bookingDate;
        public String customerType;
        public int bookingType;
        public String bookingTime;
        public String bookStatus;
        public String transjectionId;
        public String location;
        public int paymentType;
        public int paymentStatus;
        public VoucherBean voucher;
        public int timeCount;
        public String paymentTime;
        public int artistId;
        public String totalPrice;
        public String latitude;
        public String longitude;

        public String reviewByUser;
        public String reviewByArtist;
        public String userRating;
        public String artistRating;

        public List<UserDetailBean> userDetail;
        public List<ArtistDetailBean> artistDetail;
        public List<BookingInfoBean> bookingInfo;

        public static class VoucherBean {

            public String voucherCode;
            public String endDate;
            public String artistId;
            public String deleteStatus;
            public String __v;
            public String _id;
            public String status;
            public String startDate;
            public String amount;
            public String discountType;

        }

        public static class UserDetailBean {

            public int _id;
            public String userName;
            public String profileImage;
            public String contactNo;


        }

        public static class ArtistDetailBean {

            public int _id;
            public String userName;
            public String profileImage;
            public String countryCode;
            public String contactNo;
            public String ratingCount;

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
            public String trackingLatitude;
            public String trackingLongitude;
            public String trackingAddress;
            public int companyId;
            public String companyName;
            public String companyImage;
            public List<BookingReportBean> bookingReport;


            public static class BookingReportBean implements Parcelable {

                public int _id;
                public String title;
                public String description;
                public int status;
                public String crd;
                public String adminReason;
                public String favored;
                public int bookingInfoId;
                public int bookingId;
                public int businessId;
                public int reportByUser;
                public int reportForUser;

                protected BookingReportBean(Parcel in) {
                    _id = in.readInt();
                    title = in.readString();
                    description = in.readString();
                    status = in.readInt();
                    crd = in.readString();
                    adminReason = in.readString();
                    favored = in.readString();
                    bookingInfoId = in.readInt();
                    bookingId = in.readInt();
                    businessId = in.readInt();
                    reportByUser = in.readInt();
                    reportForUser = in.readInt();
                }

                public static final Creator<BookingReportBean> CREATOR = new Creator<BookingReportBean>() {
                    @Override
                    public BookingReportBean createFromParcel(Parcel in) {
                        return new BookingReportBean(in);
                    }

                    @Override
                    public BookingReportBean[] newArray(int size) {
                        return new BookingReportBean[size];
                    }
                };

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {
                    dest.writeInt(_id);
                    dest.writeString(title);
                    dest.writeString(description);
                    dest.writeInt(status);
                    dest.writeString(crd);
                    dest.writeString(adminReason);
                    dest.writeString(favored);
                    dest.writeInt(bookingInfoId);
                    dest.writeInt(bookingId);
                    dest.writeInt(businessId);
                    dest.writeInt(reportByUser);
                    dest.writeInt(reportForUser);
                }
            }


        }
    }
}

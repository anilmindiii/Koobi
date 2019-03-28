package com.mualab.org.user.activity.booking.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by mindiii on 23/1/19.
 */

public class VoucherInfo implements Parcelable{

    public String status;
    public String message;
    public List<DataBean> data;

    protected VoucherInfo(Parcel in) {
        status = in.readString();
        message = in.readString();
    }

    public static final Creator<VoucherInfo> CREATOR = new Creator<VoucherInfo>() {
        @Override
        public VoucherInfo createFromParcel(Parcel in) {
            return new VoucherInfo(in);
        }

        @Override
        public VoucherInfo[] newArray(int size) {
            return new VoucherInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(status);
        dest.writeString(message);
    }

    public static class DataBean implements Parcelable{
        /**
         * voucherCode : OFF50
         * discountType : 2
         * amount : 50
         * startDate : 2019-01-15
         * endDate : 2019-01-31
         * artistId : 2
         * status : 1
         * deleteStatus : 1
         * _id : 1
         * __v : 0
         */

        public String voucherCode;
        public String discountType;
        public String amount;
        public String startDate;
        public String endDate;
        public int artistId;
        public int status;
        public int deleteStatus;
        public int _id;
        public int __v;


        protected DataBean(Parcel in) {
            voucherCode = in.readString();
            discountType = in.readString();
            amount = in.readString();
            startDate = in.readString();
            endDate = in.readString();
            artistId = in.readInt();
            status = in.readInt();
            deleteStatus = in.readInt();
            _id = in.readInt();
            __v = in.readInt();
        }

        public static final Creator<DataBean> CREATOR = new Creator<DataBean>() {
            @Override
            public DataBean createFromParcel(Parcel in) {
                return new DataBean(in);
            }

            @Override
            public DataBean[] newArray(int size) {
                return new DataBean[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(voucherCode);
            dest.writeString(discountType);
            dest.writeString(amount);
            dest.writeString(startDate);
            dest.writeString(endDate);
            dest.writeInt(artistId);
            dest.writeInt(status);
            dest.writeInt(deleteStatus);
            dest.writeInt(_id);
            dest.writeInt(__v);
        }
    }
}

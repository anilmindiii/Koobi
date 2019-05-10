package com.mualab.org.user.chat.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Groups  implements Parcelable {
    public int adminId = 0;
    public int banAdmin = 0 ;
    public int freezeGroup = 0;
    public String groupDescription = "";
    public String groupImg = "";
    public String groupName = "";
    public String groupId = "";
    public boolean isPending = false;
    //public JSONObject member;
    public HashMap<String,Object> member = new HashMap<>();

    public Groups(){

    }


    protected Groups(Parcel in) {
        adminId = in.readInt();
        banAdmin = in.readInt();
        freezeGroup = in.readInt();
        groupDescription = in.readString();
        groupImg = in.readString();
        groupName = in.readString();
        groupId = in.readString();
        isPending = in.readByte() != 0;
    }

    public static final Creator<Groups> CREATOR = new Creator<Groups>() {
        @Override
        public Groups createFromParcel(Parcel in) {
            return new Groups(in);
        }

        @Override
        public Groups[] newArray(int size) {
            return new Groups[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(adminId);
        dest.writeInt(banAdmin);
        dest.writeInt(freezeGroup);
        dest.writeString(groupDescription);
        dest.writeString(groupImg);
        dest.writeString(groupName);
        dest.writeString(groupId);
        dest.writeByte((byte) (isPending ? 1 : 0));
    }
}

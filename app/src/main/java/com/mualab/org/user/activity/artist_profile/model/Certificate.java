package com.mualab.org.user.activity.artist_profile.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dharmraj on 2/2/18.
 */

public class Certificate implements Parcelable{
/*"certificateImage":"http://koobi.co.uk:5000/uploads/certificateImage/1526306052177.jpg",
"status":0,
"crd":"2018-05-14T12:50:01.233Z",
"upd":"2018-05-14T12:50:01.233Z",
"_id":6,
"artistId":49,
"__v":0*/
    public int id;
    public int status;
    public boolean isSelected;
    public String imageUri,certificateImage;
    public String title;
    public String description;
    public String crd;
    public String upd;
    public String artistId;

    public Certificate(){

    }

    public Certificate(int id){
        this.id = id;
    }

    protected Certificate(Parcel in) {
        id = in.readInt();
        status = in.readInt();
        isSelected = in.readByte() != 0;
        imageUri = in.readString();
        certificateImage = in.readString();
        title = in.readString();
        description = in.readString();
        crd = in.readString();
        upd = in.readString();
        artistId = in.readString();
    }

    public static final Creator<Certificate> CREATOR = new Creator<Certificate>() {
        @Override
        public Certificate createFromParcel(Parcel in) {
            return new Certificate(in);
        }

        @Override
        public Certificate[] newArray(int size) {
            return new Certificate[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(status);
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeString(imageUri);
        dest.writeString(certificateImage);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(crd);
        dest.writeString(upd);
        dest.writeString(artistId);
    }
}

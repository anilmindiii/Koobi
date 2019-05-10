package com.mualab.org.user.activity.tag_module.instatag;

import java.io.Serializable;

public class TagDetail implements Serializable {
    public String tabType, tagId, title, userType;  //people tag
    public String incallPrice = "", description, businessTypeName,
            businessTypeId, categoryId, artistId, completionTime, outcallPrice, categoryName,staffId;


    public TagDetail() {

    }

    public TagDetail(String tabType, String tagId, String title, String userType) {
        this.tabType = tabType;
        this.tagId = tagId;
        this.title = title;
        this.userType = userType;
    }

    public TagDetail(String tabType, String tagId, String title, String userType, String incallPrice,
                     String description, String businessTypeName, String businessTypeId, String categoryId, String artistId,
                     String completionTime, String outcallPrice, String categoryName,String staffId) {
        this.tabType = tabType;
        this.tagId = tagId; //use _id as tagId
        this.title = title;
        this.userType = userType;
        this.incallPrice = incallPrice;
        this.description = description;
        this.businessTypeName = businessTypeName;
        this.businessTypeId = businessTypeId;
        this.categoryId = categoryId;
        this.artistId = artistId;
        this.completionTime = completionTime;
        this.outcallPrice = outcallPrice;
        this.categoryName = categoryName;
        this.staffId = staffId;
    }

/*    private TagDetail(Parcel in) {
        tabType = in.readString();
        tagId = in.readString();
        title = in.readString();
    }

    public static final Creator<TagDetail> CREATOR = new Creator<TagDetail>() {
        @Override
        public TagDetail createFromParcel(Parcel in) {
            return new TagDetail(in);
        }

        @Override
        public TagDetail[] newArray(int size) {
            return new TagDetail[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tabType);
        dest.writeString(tagId);
        dest.writeString(title);
    }*/
}

package com.mualab.org.user.data.feeds;

import com.google.gson.annotations.SerializedName;
import com.mualab.org.user.activity.tag_module.instatag.TagToBeTagged;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mindiii on 9/8/17.
 */

public class Feeds  implements Serializable{

    //public FeedType FEED_TYPE;
    // public static Feeds tmpFeed;
    public int _id;
    public String feedType;
    public boolean isShow;
    public boolean ispopupOpen;
    public List<String> feed = new ArrayList<>();
    public List<Feed> feedData = new ArrayList<>();
    public List<String> feedThumb = new ArrayList<>();
    public ArrayList<TagToBeTagged> peopleTagList = new ArrayList<>();
    public ArrayList<TagToBeTagged> serviceTagList = new ArrayList<>();
    public HashMap<Integer,ArrayList<TagToBeTagged>> taggedImgMap = new HashMap<>();
    public HashMap<Integer,ArrayList<TagToBeTagged>> serviceTaggedImgMap = new HashMap<>();

    public String caption;
    @SerializedName("location")

    public String location;
    public int likeCount;
    public int commentCount;

    @SerializedName("followerStatus")
    public int followingStatus;
    public String crd;

    public List<User> userInfo;
    public String timeElapsed;

    public int isSave;

    /*tmp*/
    public String fullName;
    public String userName;
    public String profileImage;

    public int isLike;
    public String videoThumbnail;

    // public String city;
    public int userId;

    public String longitude;
    public String latitude;
    public int viewPagerlastPos = 0;




    public class Feed  implements Serializable{
        public String feedPost;
        public String videoThumb;
    }

    public  class User implements Serializable{
        public int _id;
        public String firstName;
        public String lastName;
        public String userName;
        public String profileImage;
        public String userType;

    }
}

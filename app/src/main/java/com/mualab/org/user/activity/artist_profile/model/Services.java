package com.mualab.org.user.activity.artist_profile.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mindiii on 8/1/19.
 */

public class Services {
    
    public String status;
    public String message;
    
    public ArtistDetailBean artistDetail;
    public List<ArtistServicesBean> artistServices ;

    public static class ArtistDetailBean {
        
        public int _id;
        public String userName;
        public String firstName;
        public String lastName;
        public String profileImage;
        public String ratingCount;
        public String reviewCount;
        public String postCount;
        public String businessName;
        public int serviceType;
        public String inCallpreprationTime;
        public String outCallpreprationTime;
        public String address;
        public String latitude;
        public String longitude;
        public String radius;
        public String businessType;
        public int bankStatus;
        public List<BusineshoursBean> busineshours;
        public boolean isAlreadybooked;


        public static class BusineshoursBean implements Serializable{
           
            public int _id;
            public String fullStartTime;
            public String fullEndTime;
            public int status;
            public String crd;
            public String upd;
            public int day;
            public int artistId;
            public String startTime;
            public String endTime;
            public int __v;

        }
    }

    public static class ArtistServicesBean {
        
        public int serviceId;
        public String serviceName;
        public boolean isSelected;
        public List<SubServiesBean> subServies;
        
        public static class SubServiesBean {
           
            public int _id;
            public int serviceId;
            public int subServiceId;
            public String subServiceName;
            public boolean isSelected;
            public List<ArtistservicesBean> artistservices;
            
            public static class ArtistservicesBean {
             
                public int _id;
                public String title;
                public Double inCallPrice;
                public Double outCallPrice;
                public String completionTime;
                public String description;
                public String bookingType;
                public boolean isStaff;
                public boolean incallStaff;
                public boolean outcallStaff;
                public boolean isSelected;
                
            }
        }
    }
}

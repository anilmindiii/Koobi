package com.mualab.org.user.activity.explore.model;

import java.util.ArrayList;
import java.util.List;

public class ExploreCategoryInfo {
    public String status;
    public String message;
    public List<DataBean> data;

    public static class DataBean {
        public int _id;
        public String title;
        public int type;
        public int deleteStatus;
        public String created_date;
        public String updated_date;
        public String status;
        public boolean isCheckedCategory;
        public boolean isCheckedCategoryLocal;
        public ArrayList<ArtistservicesBean> artistservices;
        

        public static class ArtistservicesBean {
            public String title;
            public String description;
            public String inCallPrice;
            public String outCallPrice;
            public String completionTime;
            public int _id;
            public int serviceId;
            public int subserviceId;
            public boolean isCheckedservices;
            public boolean isCheckedservicesLocal;
        }
    }
}

package com.mualab.org.user.activity.feeds.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mindiii on 19/12/18.
 */

public class ReportInfo {
    
    public String status;
    public String message;
    public ArrayList<DataBean> data;

    public static class DataBean {
      
        public String title;
        public int _id;
        public int __v;
        
    }
}

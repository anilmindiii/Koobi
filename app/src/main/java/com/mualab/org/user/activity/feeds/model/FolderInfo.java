package com.mualab.org.user.activity.feeds.model;

import java.util.List;

public class FolderInfo  {
    
    public String status;
    public String message;
    public List<DataBean> data;

    public static class DataBean {
        public int userId;
        public String folderName;
        public int status;
        public String crd;
        public String upd;
        public int _id;
        public int __v;
        public boolean isSelected;
    }
}

package com.mualab.org.user.activity.notification.model;

import java.util.List;

public class NotificationInfo {

    public String status;
    public String message;
    public int total;
    public List<NotificationListBean> notificationList;
    
    public static class NotificationListBean {
        public int _id;
        public int notifyId;
        public int senderId;
        public int notifincationType;
        public int readStatus;
        public String type;
        public String crd;
        public String userName;
        public String firstName;
        public String lastName;
        public String profileImage;
        public String userType;
        public String redirectId;
        public String timeElapsed;
        public String message;
        public String dateStaus;
        public boolean shouldShowLable;

    }
}

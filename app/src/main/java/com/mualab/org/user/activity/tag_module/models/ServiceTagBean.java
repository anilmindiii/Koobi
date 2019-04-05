package com.mualab.org.user.activity.tag_module.models;

import java.util.List;

public class ServiceTagBean {

    /**
     * _id : 8
     * title : color tech
     * description : ywyssbhsks
     * inCallPrice : 10.0
     * outCallPrice : 20.0
     * completionTime : 00:10
     * bookingCount : 15
     * status : 1
     * deleteStatus : 1
     * crd : 2019-02-27T13:28:30.616Z
     * upd : 2019-02-27T13:28:30.616Z
     * artistId : 1
     * serviceId : 2
     * subserviceId : 4
     * __v : 0
     * businessType : [{"_id":2,"active_hash":"$2a$10$K/KiWx.PdZ6dPlY50Z.GW.H9PjkMA.e2/MnIr0PWO1F9AXyUgr8HK","deleteStatus":1,"role_id":1,"type":0,"title":"Colour Technician","created_date":"2019-02-27T06:05:38.000Z","updated_date":"2019-02-27T06:05:38.000Z","status":"1","__v":0}]
     * category : [{"_id":4,"deleteStatus":1,"role_id":2,"type":0,"title":"Color Tech","created_date":"2019-02-27T11:28:34.000Z","updated_date":"2019-02-27T11:28:34.000Z","serviceId":2,"status":"1","active_hash":"ross","__v":0}]
     */

    public int _id;
    public String title;
    public String description;
    public String inCallPrice;
    public String outCallPrice;
    public String completionTime;
    public int bookingCount;
    public int status;
    public int deleteStatus;
    public String crd;
    public String upd;
    public int artistId;
    public int serviceId;
    public int subserviceId;
    public int __v;
    public List<BusinessTypeBean> businessType;
    public List<CategoryBean> category;

    public static class BusinessTypeBean {
        /**
         * _id : 2
         * active_hash : $2a$10$K/KiWx.PdZ6dPlY50Z.GW.H9PjkMA.e2/MnIr0PWO1F9AXyUgr8HK
         * deleteStatus : 1
         * role_id : 1
         * type : 0
         * title : Colour Technician
         * created_date : 2019-02-27T06:05:38.000Z
         * updated_date : 2019-02-27T06:05:38.000Z
         * status : 1
         * __v : 0
         */

        public int _id;
        public String active_hash;
        public int deleteStatus;
        public int role_id;
        public int type;
        public String title;
        public String created_date;
        public String updated_date;
        public String status;
        public int __v;

    }

    public static class CategoryBean {
        /**
         * _id : 4
         * deleteStatus : 1
         * role_id : 2
         * type : 0
         * title : Color Tech
         * created_date : 2019-02-27T11:28:34.000Z
         * updated_date : 2019-02-27T11:28:34.000Z
         * serviceId : 2
         * status : 1
         * active_hash : ross
         * __v : 0
         */

        public int _id;
        public int deleteStatus;
        public int role_id;
        public int type;
        public String title;
        public String created_date;
        public String updated_date;
        public int serviceId;
        public String status;
        public String active_hash;
        public int __v;
    }
}

package com.mualab.org.user.activity.businessInvitaion.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CompanyDetail implements Serializable {
    public String _id, job, mediaAccess, holiday, businessId, artistId, businessName,
            userName, profileImage, address, status;
    public List<BusinessDayForStaff> staffHoursList = new ArrayList<>();
     public List<ComapnySelectedServices> staffService = new ArrayList<>();
    //public List<BusinessDay>businessDays = new ArrayList<>();
    public List<String> businessType = new ArrayList<>();

}

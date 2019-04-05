package com.mualab.org.user.activity.businessInvitaion.model;



import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



public class Services implements Serializable,Cloneable{


    public int id;


    public int serviceId;


    public String bizTypeName;


    public String serviceName = "";


    public int subserviceId;


    public String subserviceName = "";


    public String description = "";


    public double inCallPrice = 0.00;


    public double outCallPrice = 0.00;


    public String completionTime = "HH:MM";


    public String preprationTime = "HH:MM";


    public String bookingType = "";



    public boolean isSelected;



    public List<MyBusinessType> businessType = new ArrayList<>();

    public Services(){

    }

    public Services(int id, String subCategoryName, String description){
        this.id = id;
        this.subserviceName = subCategoryName;
        this.description = description;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

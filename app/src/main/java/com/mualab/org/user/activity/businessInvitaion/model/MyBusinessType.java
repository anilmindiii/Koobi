package com.mualab.org.user.activity.businessInvitaion.model;



import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;


public class MyBusinessType implements Serializable {

    public MyBusinessType(){

    }


    public String bookingCount,deleteStatus,status,artistId, __v;

    public boolean isChecked = false;


    public int serviceId;


    public String serviceName;

/*
    @SerializedName("description")
    public String description;
*/


   // public List<AddedCategory> categorys;


    public boolean isSelected;

    public MyBusinessType(int id, String title/*,String description*/){
        this.serviceId = id;
        this.serviceName = title;
        //   this.description = description;
    }

   /* public void addCategory(AddedCategory category){
        if(categorys==null)
            categorys = new ArrayList<>();
        categorys.add(category);
    }

    public AddedCategory getCategory(int categoryId){
        if(categorys!=null){

            for (AddedCategory category : categorys){
                if(Integer.parseInt(category.subServiceId)==categoryId){
                    return category;
                }
            }
        }
        return null;
    }*/


    /*"bookingCount":"0",
"status":0,
"deleteStatus":1,
"_id":25,
"serviceId":16,
"serviceName":"Pediure",
"artistId":20,
"__v":0*/
}

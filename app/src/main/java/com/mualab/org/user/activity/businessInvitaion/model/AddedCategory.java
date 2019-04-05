package com.mualab.org.user.activity.businessInvitaion.model;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*@Entity(tableName = "categorys", foreignKeys = @ForeignKey(entity = MyBusinessType.class,
        parentColumns = "_id",
        childColumns = "serviceId",
        onDelete = CASCADE))*/




public class AddedCategory implements Serializable {

    public int _id;


    public int serviceId;


    public String subServiceId;


    public String subServiceName;


    public List<Services> servicesList = new ArrayList<>();

    public AddedCategory(){

    }

    public AddedCategory(int id, String categoryName){
        this._id = id;
        this.subServiceName = categoryName;
        //this.description = description;
    }

/*    public void addService(company_services services){
        if(servicesList==null)
            servicesList = new ArrayList<>();
        services.subserviceName = this.subServiceName;
        servicesList.add(services);
    }

    public company_services getService(int subCategoryId){
        if(servicesList!=null){
            for (company_services services : servicesList){
                if(services.id==subCategoryId){
                    return services;
                }
            }
        }
        return null;
    }

    public boolean deleteSubCategory(company_services subCategory){
        return servicesList.remove(subCategory);
    }*/


    public String bookingCount,deleteStatus, status,artistId, __v;

    public boolean isChecked = false;

    /*"status":1,
"deleteStatus":1,
"_id":24,
"serviceId":3,
"subServiceId":4,
"subServiceName":"Nail",
"artistId":21,
"__v":0*/
}

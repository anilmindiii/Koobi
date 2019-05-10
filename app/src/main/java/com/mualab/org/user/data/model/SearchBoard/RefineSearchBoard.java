package com.mualab.org.user.data.model.SearchBoard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Mindiii on 2/24/2018.
 */

@SuppressWarnings("serial")
public class RefineSearchBoard implements Serializable{
    public String latitude, longitude,page,limit,service,serviceType,
            day,time,subservice,date,location,sortSearch,sortType,priceFilter,priceMinFilter,
            LocationFilter,PriceFilter,rating,certificate;
    public Date oldDate;
    public ArrayList<RefineServices> refineServices = new ArrayList<>();
    public ArrayList<RefineServices> tempSerevice = new ArrayList<>();
    public boolean isFavClick;
}

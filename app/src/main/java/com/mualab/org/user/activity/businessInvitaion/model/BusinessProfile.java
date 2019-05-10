package com.mualab.org.user.activity.businessInvitaion.model;

import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.model.booking.Address;
import com.mualab.org.user.data.model.booking.BusinessDay;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dharmraj on 24/1/18.
 */

public class BusinessProfile {

    public boolean isBusinessHoursAdded;
    public int bookingSetting = 1; // Manual = 1, Auto=0;
    public int serviceType = 1; // inCall = 1, outCall=2, both = 3;
    public int radius = 1; // min = 1 mile and max = 100 mile
    public int bankStatus = 0; //

    public String inCallpreprationTime;
    public String outCallpreprationTime;
    public String bio;
    public int certificateCount;

    public int stepComplete; // stepComplete is current fragment index, totel step is no of fragment count

    public User user;
    public Address address;
    public List<BusinessDay> businessDays = new ArrayList<>();
    public List<BusinessDay> edtStaffDays;
    public List<BusinessDayForStaff> dayForStaffs = new ArrayList<>();
}

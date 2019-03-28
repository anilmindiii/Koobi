package com.mualab.org.user.chat.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class GroupMember implements Serializable {

    public String firebaseToken,profilePic,userName,type;
    public int memberId,mute;
    public Object createdDate;
    @Exclude
    public boolean isChecked = false;
}

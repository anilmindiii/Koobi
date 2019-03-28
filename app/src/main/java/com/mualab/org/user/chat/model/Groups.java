package com.mualab.org.user.chat.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Groups  implements Serializable{
    public int adminId = 0;
    public int banAdmin = 0 ;
    public int freezeGroup = 0;
    public String groupDescription = "";
    public String groupImg = "";
    public String groupName = "";
    public String groupId = "";
    //public JSONObject member;
    public HashMap<String,Object> member = new HashMap<>();
}

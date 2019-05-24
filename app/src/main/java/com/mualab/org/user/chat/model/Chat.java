package com.mualab.org.user.chat.model;

import java.io.Serializable;

public class Chat implements Serializable {
    public int readStatus;
    public String message;
    public int messageType;
    public String reciverId;
    public String senderId;
    public Object timestamp;
    public String banner_date;
    public String ref_key;
    public boolean isLongSelected;
}

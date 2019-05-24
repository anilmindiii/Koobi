package com.mualab.org.user.chat.listner;

public interface DateTimeScrollListner {
    void onScrollChange(int position,Object timestamp);
    void onLongPress(int position,String ref_Key);
    void onPress(int position);
}

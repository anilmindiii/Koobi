package com.mualab.org.user.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeShow {

    public static  String setTimeAgo(String inputDateFormat,String date) {
        try
        {
            SimpleDateFormat format = new SimpleDateFormat(inputDateFormat);
            Date past = format.parse(date);
            Date now = new Date();
            long seconds=TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
            long minutes=TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
            long hours=TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
            long days=TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());

            if(seconds<60)
            {
                return seconds+" seconds ago";
            }
            else if(minutes<60)
            {
                return minutes+" minutes ago";
            }
            else if(hours<24)
            {
                return hours+" hours ago";
            }
            else
            {
                return days+" days ago";
            }

        }
        catch (Exception j){
            j.printStackTrace();
        }
        return "";
    }
}

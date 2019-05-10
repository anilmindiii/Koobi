package com.mualab.org.user.chat.Util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.mualab.org.user.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeAgo {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    protected Context context;

    public TimeAgo(Context context) {
        this.context = context;
    }

    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            if ((diff / SECOND_MILLIS)==0)
                return "a few second ago";
            else
                return diff / SECOND_MILLIS+" second ago";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    public static String timeAgo(long millis) {
        long diff = new Date().getTime() - millis;

       // Resources r = context.getResources();

        String prefix = "time ago";
        String suffix = "time ago";

        double seconds = Math.abs(diff) / 1000;
        double minutes = seconds / 60;
        double hours = minutes / 60;
        double days = hours / 24;
        double years = days / 365;

        String words;

        if (seconds < 45) {
            words = Math.round(seconds)+" seconds ago";//r.getString(R.string.time_ago_seconds, Math.round(seconds));
        } else if (seconds < 90) {
            words = "1 minutes ago";//r.getString(R.string.time_ago_minute, 1);
        } else if (minutes < 45) {
            words = Math.round(minutes)+" minutes ago";//r.getString(R.string.time_ago_minutes, Math.round(minutes));
        } else if (minutes < 90) {
            words = "1 hours ago";//r.getString(R.string.time_ago_hour, 1);
        } else if (hours < 24) {
            words = Math.round(hours)+" hours ago";//r.getString(R.string.time_ago_hours, Math.round(hours));
        } else if (hours < 42) {
            words = "1 days ago";//r.getString(R.string.time_ago_day, 1);
        } else if (days < 30) {
            words = Math.round(days)+" days ago";//r.getString(R.string.time_ago_days, Math.round(days));
        } else if (days < 45) {
            words = "1 months ago";//+r.getString(R.string.time_ago_month, 1);
        } else if (days < 365) {
            words = Math.round(days / 30)+" months ago";//r.getString(R.string.time_ago_months, Math.round(days / 30));
        } else if (years < 1.5) {
            words = "1 year ago";//r.getString(R.string.time_ago_year, 1);
        } else {
            words = Math.round(years)+" years ago";//r.getString(R.string.time_ago_years, Math.round(years));
        }

        StringBuilder sb = new StringBuilder();

        if (prefix != null && prefix.length() > 0) {
            sb.append(prefix).append(" ");
        }

        sb.append(words);

        if (suffix != null && suffix.length() > 0) {
            sb.append(" ").append(suffix);
        }

       // return sb.toString().trim();
        return words.trim();
    }

    /*...........................notifications.....................................*/

    public static String covertTimeToText(String dataDate) {

        String convTime = null;
        String prefix = "";
        String suffix = "";

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date pasTime = dateFormat.parse(dataDate);

            Date nowTime = new Date();

            long dateDiff = nowTime.getTime() - pasTime.getTime();

            long day  = TimeUnit.MILLISECONDS.toDays(dateDiff);

            if (day >= 7) {
                if (day > 30) {
                    convTime = "This Months "+ suffix;
                } else if (day > 360) {
                    convTime = "This Years "+ suffix;
                } else {
                    convTime =   "This Months "+ suffix;
                }
            } else if (day < 7) {
                convTime = day+" Days "+suffix;

                if(day == 0){
                    convTime = "New";
                }else if(day == 1 ||day == 2){
                    convTime = "Yesterday";
                }else convTime = "This week";
            }

        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("ConvTimeE", e.getMessage());
        }

        return convTime;

    }

    public String covertTimeToTextR(String dataDate) {

        String convTime = null;
        String prefix = "";
        String suffix = "";

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date pasTime = dateFormat.parse(dataDate);

            Date nowTime = new Date();

            long dateDiff = nowTime.getTime() - pasTime.getTime();

            long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
            long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
            long hour   = TimeUnit.MILLISECONDS.toHours(dateDiff);
            long day  = TimeUnit.MILLISECONDS.toDays(dateDiff);

            if (second < 60) {
                convTime = "New";
            }
            else if (minute < 60) {
                convTime = "New";
            }
            else if (hour < 24) {
                convTime = hour+" Hours "+suffix;
            }
            else if (day >= 7) {
                if (day > 30) {
                    convTime = (day / 30)+" Months "+suffix;
                } else if (day > 360) {
                    convTime = (day / 360)+" Years "+suffix;
                } else {
                    convTime = (day / 7) + " Week "+suffix;
                }
            }
            else if (day < 7) {
                convTime = day+" Days "+suffix;

                if(day == 1 || day == 0){
                    convTime = "New";
                }else if(day == 2){
                    convTime = "Yesterday";
                }else convTime = "This week";
            }

        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("ConvTimeE", e.getMessage());
        }

        return convTime;

    }













    /*public String covertTimeToTextR(String dataDate) {

        String convTime = null;
        String prefix = "";
        String suffix = "Ago";

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date pasTime = dateFormat.parse(dataDate);

            Date nowTime = new Date();

            long dateDiff = nowTime.getTime() - pasTime.getTime();

            long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
            long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
            long hour   = TimeUnit.MILLISECONDS.toHours(dateDiff);
            long day  = TimeUnit.MILLISECONDS.toDays(dateDiff);

            if (second < 60) {
                convTime = second+" Seconds "+suffix;
            } else if (minute < 60) {
                convTime = minute+" Minutes "+suffix;
            } else if (hour < 24) {
                convTime = hour+" Hours "+suffix;
            } else if (day >= 7) {
                if (day > 30) {
                    convTime = (day / 30)+" Months "+suffix;
                } else if (day > 360) {
                    convTime = (day / 360)+" Years "+suffix;
                } else {
                    convTime = (day / 7) + " Week "+suffix;
                }
            } else if (day < 7) {
                convTime = day+" Days "+suffix;
            }

        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("ConvTimeE", e.getMessage());
        }

        return convTime;

    }*/


}

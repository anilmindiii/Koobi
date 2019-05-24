package com.mualab.org.user.activity.main.listner;

public class CountClick {
    public static CountClick mCountClick;

    private InviationCount inviationCountListner;

    public static CountClick getmCountClick() {
        if(mCountClick == null){
            mCountClick = new CountClick();
        }
        return mCountClick;
    }



    public void setListner(InviationCount listner){
        inviationCountListner = listner;
    }


    public void onCountChange(int count){
        if (inviationCountListner != null)
        inviationCountListner.onCountChange(count);

    }
    public interface InviationCount{
        void onCountChange(int count);
    }
}

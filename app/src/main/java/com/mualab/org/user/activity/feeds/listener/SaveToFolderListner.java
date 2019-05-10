package com.mualab.org.user.activity.feeds.listener;

public class SaveToFolderListner {
    private static SaveToFolderListner mInctance;
    private ExploreSearchListener mListner;


    public static SaveToFolderListner getmInctance() {
        if (mInctance==null){
            mInctance = new SaveToFolderListner();
        }
        return mInctance;
    }


    public void setListner(ExploreSearchListener mListner){
        this.mListner = mListner;
    }

    public void onSave(String tag,int position){

        if(mListner!=null){
            mListner.onTextChange(tag,position);
        }
    }


    public interface ExploreSearchListener {
        void onTextChange(String tag,int position);
    }


}

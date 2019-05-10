package com.mualab.org.user.chat.listner;

import android.os.Bundle;
import android.support.v13.view.inputmethod.InputContentInfoCompat;

public class CustomeClick {

    /*...............................................*/

    private static CustomeClick mInctance;
    private ExploreSearchListener mListner;


    public static CustomeClick getmInctance() {
        if (mInctance == null) {
            mInctance = new CustomeClick();
        }
        return mInctance;
    }


    public void setListner(ExploreSearchListener mListner) {
        this.mListner = mListner;
    }

    public void onTextChange(InputContentInfoCompat inputContentInfo,
                             int flags, Bundle opts) {

        if (mListner != null) {
            mListner.onTextChange(inputContentInfo,flags,opts);
        }
    }


    public interface ExploreSearchListener {
        void onTextChange(InputContentInfoCompat inputContentInfo,
                          int flags, Bundle opts);
    }
    /*.............................................................*/


}

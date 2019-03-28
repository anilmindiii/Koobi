package com.mualab.org.user.activity.booking.dialog;

/**
 * Created by mindiii on 6/2/19.
 */

public interface BookingReviewRatingCallback {
    void onSubmitClick(String rating, String comment,String typr);
    void onReportThisClick();
}

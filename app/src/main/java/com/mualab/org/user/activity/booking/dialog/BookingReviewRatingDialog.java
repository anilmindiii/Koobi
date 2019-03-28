package com.mualab.org.user.activity.booking.dialog;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;


import com.mualab.org.user.R;
import com.mualab.org.user.activity.base.BaseDialog;
import com.mualab.org.user.activity.booking.model.BookingListInfo;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.squareup.picasso.Picasso;


public class BookingReviewRatingDialog extends BaseDialog implements View.OnClickListener {

    private static final String TAG = BookingReviewRatingDialog.class.getSimpleName();
    private BookingReviewRatingCallback callback;
    private EditText etComments;
    private boolean isCommentShow = false;
    private ImageView ivDropDown;
    private RatingBar ratingBar;
    private Activity activity ;
    BookingListInfo historyInfo;


    public static BookingReviewRatingDialog newInstance(BookingListInfo historyInfo, BookingReviewRatingCallback callback) {
        Bundle args = new Bundle();
        BookingReviewRatingDialog fragment = new BookingReviewRatingDialog();
        fragment.setInstanceData(callback);
        fragment.setArguments(args);
        args.putSerializable("historyInfo",historyInfo);
        return fragment;
    }

    public void setInstanceData(BookingReviewRatingCallback callback) {
        this.callback = callback;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_booking_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ratingBar = view.findViewById(R.id.rating);
        etComments = view.findViewById(R.id.etComments);
        ivDropDown = view.findViewById(R.id.ivDropDown);
        view.findViewById(R.id.iv_cancel).setOnClickListener(this);
        view.findViewById(R.id.llAddComment).setOnClickListener(this);
        view.findViewById(R.id.btnSumbit).setOnClickListener(this);
       // view.findViewById(R.id.btnReport).setOnClickListener(this);
        ImageView ivProfilePic = view.findViewById(R.id.ivProfilePic);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvMsg = view.findViewById(R.id.tvMsg);
        Button btnSubmit =  view.findViewById(R.id.btnSumbit);

        if(getArguments() != null){
            historyInfo = (BookingListInfo) getArguments().getSerializable("historyInfo");

            if(!historyInfo.data.reviewByUser.equals("") && !historyInfo.data.userRating.equals("0")){
                etComments.setText(historyInfo.data.reviewByUser);

                ratingBar.setIsIndicator(false);
                ratingBar.setRating(Float.parseFloat(historyInfo.data.userRating));
            }else {
            }

        }

        if (Mualab.getInstance().getSessionManager().getUser().profileImage != null &&
                !Mualab.getInstance().getSessionManager().getUser().profileImage .isEmpty())
            Picasso.with(getActivity()).load(Mualab.getInstance().getSessionManager().getUser().profileImage ).placeholder(R.drawable.default_placeholder).into(ivProfilePic);

        tvName.setText(Mualab.getInstance().getSessionManager().getUser().userName);

        String artistUserName = " @"+historyInfo.data.artistDetail.get(0).userName+" 's";
        String msg = getString(R.string.your_service_has_been_completed_nplease_review_how_satisfied_you_are_nwith)
                .concat(artistUserName).concat(" service");

        int startingPosition = msg.indexOf(artistUserName);
        int endingPosition = startingPosition + artistUserName.length();

        Spannable msgSpan = new SpannableString(msg);
        msgSpan.setSpan(new ForegroundColorSpan(Color.BLACK), startingPosition, endingPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        msgSpan.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startingPosition, endingPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvMsg.setText(msgSpan);
    }

    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, TAG);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_cancel:
                dismissDialog(TAG);
                break;

            case R.id.llAddComment:
                if (isCommentShow) {
                    ivDropDown.setRotation(0);
                    etComments.setVisibility(View.GONE);
                    isCommentShow = false;
                    hideKeyboard();
                } else {
                    ivDropDown.setRotation(180);
                    etComments.setVisibility(View.VISIBLE);
                    isCommentShow = true;
                }
                break;

            case R.id.btnSumbit:
                String comments = etComments.getText().toString();
                int rating = (int) ratingBar.getRating();

                if (verifyInputs(rating, comments)) {
                    dismissDialog(TAG);
                    assert callback != null;
                    String type = "insert";
                    if(!historyInfo.data.reviewByUser.equals("") && !historyInfo.data.userRating.equals("0")){
                        type = "edit";
                    }
                    callback.onSubmitClick(String.valueOf(rating), comments,type);
                }

                dismissDialog(TAG);




                break;

           /* case R.id.btnReport:
                dismissDialog(TAG);
                assert callback != null;
                callback.onReportThisClick();
                break;*/
        }
    }

    private boolean verifyInputs(int rating, String comments) {
        if (rating == 0) {
            MyToast.getInstance(getActivity()).showDasuAlert(getString(R.string.give_rating));
            return false;
        } else if (comments.isEmpty()) {
            MyToast.getInstance(getActivity()).showDasuAlert(getString(R.string.give_review));
            return false;
        } else return true;
    }

}

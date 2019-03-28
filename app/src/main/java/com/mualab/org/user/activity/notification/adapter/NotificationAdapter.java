package com.mualab.org.user.activity.notification.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.booking.BookingDetailsActivity;
import com.mualab.org.user.activity.feeds.activity.FeedSingleActivity;
import com.mualab.org.user.activity.feeds.fragment.FeedsFragment;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
import com.mualab.org.user.activity.notification.model.NotificationInfo;
import com.mualab.org.user.activity.review_rating.ReviewRatingActivity;
import com.mualab.org.user.activity.story.StoriesActivity;
import com.mualab.org.user.data.feeds.LiveUserInfo;
import com.mualab.org.user.utils.constants.Constant;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    List<NotificationInfo.NotificationListBean> listBeans;
    Context mContext;

    public NotificationAdapter(List<NotificationInfo.NotificationListBean> listBeans, Context mContext) {
        this.listBeans = listBeans;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notifications,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        NotificationInfo.NotificationListBean bean = listBeans.get(i);

        String myFirstWord = getFirstWord(bean.message);
        int start = 0; // bold will start at index 0
        int end = myFirstWord.length();

        SpannableStringBuilder fancySentence = new SpannableStringBuilder(bean.message);
        fancySentence.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        viewHolder.tvMsg.setText(fancySentence);


        viewHolder.tvDate.setText(bean.timeElapsed);

        if(!TextUtils.isEmpty(bean.profileImage)){
            Picasso.with(mContext).load(bean.profileImage)
                    .fit()
                    .placeholder(R.drawable.default_placeholder)
                    .error(R.drawable.default_placeholder)
                    .into(viewHolder.ivProfilePic);
        }else Picasso.with(mContext).load(R.drawable.default_placeholder).into(viewHolder.ivProfilePic);
    }

    private String getFirstWord(String input){

        for(int i = 0; i < input.length(); i++){

            if(input.charAt(i) == ' '){

                return input.substring(0, i);
            }
        }

        return input;
    }

    @Override
    public int getItemCount() {
        return listBeans.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvMsg,tvDate;
        ImageView ivProfilePic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMsg = itemView.findViewById(R.id.tvMsg);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivProfilePic = itemView.findViewById(R.id.ivProfilePic);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            String type = listBeans.get(getAdapterPosition()).type;
            int notifyId = listBeans.get(getAdapterPosition()).notifyId;
            String userName = listBeans.get(getAdapterPosition()).userName;
            String urlImageString = listBeans.get(getAdapterPosition()).profileImage;
            String userType = listBeans.get(getAdapterPosition()).userType;
            int notifincationType = listBeans.get(getAdapterPosition()).notifincationType;

            notificationRedirections(String.valueOf(notifyId), userName, urlImageString, userType, String.valueOf(notifincationType));

        }

        private void notificationRedirections(String notifyId, String userName, String urlImageString, String userType, String notifincationType) {
            switch (notifincationType) {
                case "13":
                    Intent intent_story = new Intent(mContext, StoriesActivity.class);
                    Bundle args = new Bundle();
                   // args.putSerializable("ARRAYLIST", liveUserList);
                    args.putInt("position", 0);
                    intent_story.putExtra("BUNDLE", args);
                    mContext.startActivity(intent_story);
                    break;

                case "7":
                    Intent intent1 = new Intent(mContext, FeedSingleActivity.class);
                    intent1.putExtra("feedId", notifyId);
                    mContext.startActivity(intent1);

                    break;

                case "11":
                    Intent intent_like_comment = new Intent(mContext, FeedSingleActivity.class);
                    intent_like_comment.putExtra("feedId", notifyId);
                    mContext.startActivity(intent_like_comment);

                    break;

                case "9":
                    Intent intent_comment = new Intent(mContext, FeedSingleActivity.class);
                    intent_comment.putExtra("feedId", notifyId);
                    mContext.startActivity(intent_comment);
                    break;

                case "16":
                    case "10":

                    Intent intent_like_post = new Intent(mContext, FeedSingleActivity.class);
                    intent_like_post.putExtra("feedId", notifyId);
                    mContext.startActivity(intent_like_post);

                    break;

                case "20":
                case "21":
                case "25":
                case "26":
                case "2":
                    Intent booking2 = new Intent(mContext, BookingDetailsActivity.class);

                    if (!notifyId.equals(""))
                        booking2.putExtra("bookingId", Integer.parseInt(notifyId));
                    booking2.putExtra("artistName", userName);
                    booking2.putExtra("artistProfile", urlImageString);
                    booking2.putExtra("key", "main");
                    mContext.startActivity(booking2);
                    break;

                case "1":
                    Intent booking1 = new Intent(mContext, BookingDetailsActivity.class);
                    if (!notifyId.equals(""))
                        booking1.putExtra("bookingId", Integer.parseInt(notifyId));
                    booking1.putExtra("artistName", userName);
                    booking1.putExtra("artistProfile", urlImageString);
                    booking1.putExtra("key", "main");
                    mContext.startActivity(booking1);
                    break;


                case "3":
                    Intent booking3 = new Intent(mContext, BookingDetailsActivity.class);
                    if (!notifyId.equals(""))
                        booking3.putExtra("bookingId", Integer.parseInt(notifyId));
                    booking3.putExtra("artistName", userName);
                    booking3.putExtra("artistProfile", urlImageString);
                    booking3.putExtra("key", "main");

                    mContext. startActivity(booking3);
                    break;

                case "4":
                    Intent booking4 = new Intent(mContext, BookingDetailsActivity.class);
                    if (!notifyId.equals(""))
                        booking4.putExtra("bookingId", Integer.parseInt(notifyId));
                    booking4.putExtra("artistName", userName);
                    booking4.putExtra("artistProfile", urlImageString);
                    booking4.putExtra("key", "main");
                    mContext.startActivity(booking4);
                    break;

                case "5":
                    Intent booking5 = new Intent(mContext, BookingDetailsActivity.class);
                    if (!notifyId.equals(""))
                        booking5.putExtra("bookingId", Integer.parseInt(notifyId));
                    booking5.putExtra("artistName", userName);
                    booking5.putExtra("notification_list", "list");
                    booking5.putExtra("key", "main");
                    booking5.putExtra("artistProfile", urlImageString);
                    mContext.startActivity(booking5);
                    break;

                case "6":
                    // here we go for review list
                    Intent booking6 = new Intent(mContext, ReviewRatingActivity.class);
                    mContext.startActivity(booking6);
                    break;

                case "12":

                    if (userType.equals("user")) {
                        Intent intent_user_profile = new Intent(mContext, UserProfileActivity.class);
                        intent_user_profile.putExtra("userId", notifyId);
                        mContext.startActivity(intent_user_profile);

                    } else {
                        Intent intent_user_profile = new Intent(mContext, ArtistProfileActivity.class);
                        intent_user_profile.putExtra("feedId", notifyId);
                        intent_user_profile.putExtra("artistId", notifyId);
                        mContext.startActivity(intent_user_profile);
                    }

                    break;

            }
        }

    }
}

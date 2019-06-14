package com.mualab.org.user.activity.booking.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.BookingDetailsActivity;
import com.mualab.org.user.activity.booking.model.BookingHistoryInfo;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {
    Context mContext;
    ArrayList<BookingHistoryInfo.DataBean> dataBean;
    CallApis callApis;
    RecyclerView recycler_view;

    public BookingHistoryAdapter(RecyclerView recycler_view,Context mContext,
                                 ArrayList<BookingHistoryInfo.DataBean> dataBean,
                                 CallApis callApis) {
        this.mContext = mContext;
        this.dataBean = dataBean;
        this.callApis = callApis;
        this.recycler_view = recycler_view;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_booking_history, viewGroup, false);
        return new ViewHolder(view);
    }

    public interface CallApis {
        void call(int artistId, int bookingId, String comments, float rating, String type);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final BookingHistoryInfo.DataBean bean = dataBean.get(position);

        if (bean.bookingInfo.size() > 1) {
            holder.newText.setVisibility(View.VISIBLE);
            holder.tvServicesnew.setText(bean.bookingInfo.get(1).subServiceName + "");
        } else holder.newText.setVisibility(View.GONE);

        if (bean.bookingInfo.size() > 0) {
            holder.tvServices.setText(bean.bookingInfo.get(0).subServiceName + "");
            holder.tvArtistName.setText(bean.artistDetail.get(0).userName+"");
        }

       /* holder.rlServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagToBookService(bean.bookingInfo.get(0).artistServiceId, String.valueOf(bean.artistDetail.get(0)._id),
                        bean.bookingInfo.get(0).serviceId, bean.bookingInfo.get(0).subServiceId);
            }
        });

        holder.newText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagToBookService(bean.bookingInfo.get(1).artistServiceId, String.valueOf(bean.artistDetail.get(0)._id),
                        bean.bookingInfo.get(1).serviceId, bean.bookingInfo.get(1).subServiceId);
            }
        });*/

        holder.artist_total_Rating.setIsIndicator(true);


        holder.tv_view_more.setVisibility(View.GONE);

       /* if (bean.bookingInfo.size() == 1) {
            holder.tvArtistName.setText(bean.bookingInfo.get(0).staffName + "");
            holder.tv_view_more.setVisibility(View.GONE);
        } else if (bean.bookingInfo.size() == 2) {
            holder.tvArtistName.setText(bean.bookingInfo.get(0).staffName
                    + ", " + bean.bookingInfo.get(1).staffName);
            holder.tv_view_more.setVisibility(View.GONE);
        } else if (bean.bookingInfo.size() == 3) {
            holder.tvArtistName.setText(bean.bookingInfo.get(0).staffName
                    + ", " + bean.bookingInfo.get(1).staffName);
            holder.tv_view_more.setVisibility(View.VISIBLE);
        }*/

        holder.tv_price.setText("£" + bean.totalPrice + "");
        holder.tvDateTime.setText(bean.bookingDate + ", " + bean.bookingTime);

     /*   if(bean.artistRating != 0){
            double d = bean.artistRating;
            float rating = (float)d;
            holder.artist_total_Rating.setRating(rating);

        }*/

        if (bean.artistDetail.get(0).profileImage != null && !bean.artistDetail.get(0).profileImage.equals("")) {
            Picasso.with(mContext).load(bean.artistDetail.get(0).profileImage).placeholder(R.drawable.default_placeholder).fit().into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageDrawable(mContext.getResources().getDrawable(R.drawable.default_placeholder));
        }

        if (bean.reviewByUser.equals("") && bean.userRating == 0) {
            holder.tv_give_view_review.setText("Give Review");
            holder.tv_give_view_review.setVisibility(View.VISIBLE);
            holder.ly_edit_review.setVisibility(View.GONE);
            holder.artist_total_Rating.setVisibility(View.GONE);
        } else {
            holder.ly_edit_review.setVisibility(View.VISIBLE);
            holder.tv_give_view_review.setVisibility(View.GONE);
            holder.tv_give_view_review.setText("Edit Review");
            holder.ed_comments.setText(bean.reviewByUser);
            holder.ed_comments.setSelection(holder.ed_comments.getText().toString().length());
            holder.artistRatingbar.setRating(bean.userRating);

            holder.artist_total_Rating.setVisibility(View.VISIBLE);
            holder.artist_total_Rating.setRating(bean.userRating);
        }

        holder.tv_give_view_review.setOnClickListener(v -> {
            if (holder.ly_review_view.getVisibility() == View.VISIBLE) {
                holder.ly_review_view.setVisibility(View.GONE);
            } else {
                holder.ly_review_view.setVisibility(View.VISIBLE);
            }

            if(position == (dataBean.size()-1)){
                recycler_view.scrollToPosition(position);
            }
        });

        holder.ly_edit_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.tv_give_view_review.callOnClick();
            }
        });

        holder.btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comments = holder.ed_comments.getText().toString();
                float rating = holder.artistRatingbar.getRating();
                if (rating == 0.0) {
                    MyToast.getInstance(mContext).showDasuAlert("Please give rating");
                    return;
                } else if (comments.equals("")) {
                    MyToast.getInstance(mContext).showDasuAlert(mContext.getString(R.string.give_review));
                    return;
                } else {
                    if (!bean.reviewByUser.equals("") && bean.userRating != 0) {
                        callApis.call(bean.artistDetail.get(0)._id, bean._id, comments, rating, "edit");
                    } else
                        callApis.call(bean.artistDetail.get(0)._id, bean._id, comments, rating, "insert");


                }
            }
        });

        holder.ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiForgetUserIdFromUserName(bean.artistDetail.get(0).userName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataBean.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivProfile;
        TextView tvArtistName, tv_price, tvServices, tvServicesnew, tvDateTime, btn_submit, tv_give_view_review, tv_view_more;
        RelativeLayout newText, rlServices;
        LinearLayout ly_review_view;
        EditText ed_comments;
        RatingBar artistRatingbar, artist_total_Rating;
        RelativeLayout ly_edit_review;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvArtistName = itemView.findViewById(R.id.tvArtistName);
            tv_view_more = itemView.findViewById(R.id.tv_view_more);
            tv_price = itemView.findViewById(R.id.tv_price);
            tvServices = itemView.findViewById(R.id.tvServices);
            newText = itemView.findViewById(R.id.newText);
            tvServicesnew = itemView.findViewById(R.id.tvServicesnew);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            btn_submit = itemView.findViewById(R.id.btn_submit);
            ed_comments = itemView.findViewById(R.id.ed_comments);
            tv_give_view_review = itemView.findViewById(R.id.tv_give_view_review);
            ly_review_view = itemView.findViewById(R.id.ly_review_view);
            artistRatingbar = itemView.findViewById(R.id.artistRatingbar);
            artist_total_Rating = itemView.findViewById(R.id.artist_total_Rating);
            rlServices = itemView.findViewById(R.id.rlServices);
            ly_edit_review = itemView.findViewById(R.id.ly_edit_review);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, BookingDetailsActivity.class);
            intent.putExtra("bookingId", dataBean.get(getAdapterPosition())._id);
            mContext.startActivity(intent);
        }
    }

    private void apiForgetUserIdFromUserName(String userName) {


        final Map<String, String> params = new HashMap<>();
        /*if(userName.contains("@")){
            userName = userName.replace("@","");
        }*/
        params.put("userName", userName + "");
        new HttpTask(new HttpTask.Builder(mContext, "profileByUserName", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Log.d("hfjas", response);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {

                        JSONObject userDetail = js.getJSONObject("userDetail");
                        String userType = userDetail.getString("userType");
                        int userId = userDetail.getInt("_id");

                        if (userType.equals("user")) {
                            Intent intent = new Intent(mContext, UserProfileActivity.class);
                            intent.putExtra("userId", String.valueOf(userId));
                            mContext.startActivity(intent);
                        } else if (userType.equals("artist") && userId == Mualab.currentUser.id) {
                            Intent intent = new Intent(mContext, UserProfileActivity.class);
                            intent.putExtra("userId", String.valueOf(userId));
                            mContext.startActivity(intent);
                        } else {
                            Intent intent = new Intent(mContext, ArtistProfileActivity.class);
                            intent.putExtra("artistId", String.valueOf(userId));
                            mContext.startActivity(intent);
                        }
                    } else {
                        MyToast.getInstance(mContext).showDasuAlert(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
            }
        }).setBody(params, HttpTask.ContentType.APPLICATION_JSON)
                .setMethod(Request.Method.POST)
                .setProgress(true))
                .execute("FeedAdapter");


    }


    private void tagToBookService(int _id, String artistId, int serviceId, int subserviceId) {
        Intent intent = new Intent(mContext, BookingActivity.class);
        intent.putExtra("_id", _id);
        intent.putExtra("artistId", artistId);
        intent.putExtra("callType", "In Call");

        intent.putExtra("mainServiceName", "");
        intent.putExtra("subServiceName", "yes");

        intent.putExtra("serviceId", serviceId);
        intent.putExtra("subServiceId", subserviceId);

        intent.putExtra("isEditService", true);
        intent.putExtra("isFromSearchBoard", true);
        mContext.startActivity(intent);
    }


}

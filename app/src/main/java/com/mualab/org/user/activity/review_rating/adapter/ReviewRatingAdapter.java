package com.mualab.org.user.activity.review_rating.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;


import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
import com.mualab.org.user.activity.review_rating.model.ReviewRating;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.utils.Helper;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by hemant
 * Date: 17/4/18
 * Time: 4:03 PM
 */

public class ReviewRatingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEWTYPE_ITEM = 1;
    private final int VIEWTYPE_LOADER = 2;
    private List<ReviewRating.DataBean> list;
    private boolean showLoader;
    Context mContext;

    public ReviewRatingAdapter(Context mContext,List<ReviewRating.DataBean> list) {
        this.list = list;
        this.mContext = mContext;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder rvHolder, int position) {

        ViewHolder holder = ((ViewHolder) rvHolder);

        ReviewRating.DataBean mainBean = list.get(position);
        holder.rating.setRating(mainBean.getRating());
        holder.tvMsg.setText(mainBean.getReview());

        holder.ivProfilePic.setOnClickListener(v->{
            apiForgetUserIdFromUserName(mainBean.getUserDetail().get(0).getUserName());
        });


        try {

            ReviewRating.DataBean.UserDetailBean userDetail = mainBean.getUserDetail().get(0);
            holder.tvName.setText(userDetail.getUserName());
            if (!userDetail.getProfileImage().isEmpty())
                Picasso.with(holder.ivProfilePic.getContext()).load(userDetail.getProfileImage()).placeholder(R.drawable.default_placeholder).
                        into(holder.ivProfilePic);

            holder.tvDate.setText(Helper.formateDateFromstring("yyyy-MM-dd", "dd/MM/yyyy", mainBean.getCrd()));

        } catch (Exception ignored) {
        }
    }


    public void showLoading(boolean status) {
        showLoader = status;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_rating, parent, false);
        return new ViewHolder(view);
        /*switch (viewType) {
            case VIEWTYPE_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_rating, parent, false);
                return new ViewHolder(view);
            case VIEWTYPE_LOADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new LoadingViewHolder(view);
        }         return null;     */




    }

   /* @Override
    public int getItemViewType(int position) {
        if (position == list.size() - 1) {
            return showLoader ? VIEWTYPE_LOADER : VIEWTYPE_ITEM;
        }
        return VIEWTYPE_ITEM;
    }*/

    @Override
    public int getItemCount() {
        return list.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivProfilePic;
        private TextView tvName, tvDate, tvMsg;
        private RatingBar rating;

        ViewHolder(@NonNull View view) {
            super(view);

            ivProfilePic = view.findViewById(R.id.ivProfilePic);
            tvName = view.findViewById(R.id.tvName);
            tvDate = view.findViewById(R.id.tvDate);
            tvMsg = view.findViewById(R.id.tvMsg);
            rating = view.findViewById(R.id.rating);
        }
    }

    private void apiForgetUserIdFromUserName(final CharSequence userName) {
        String user_name = "";

        final Map<String, String> params = new HashMap<>();
        if (userName.toString().startsWith("@")) {

            user_name = userName.toString().replaceFirst("@", "");
            params.put("userName", user_name + "");
        } else params.put("userName", userName + "");
        new HttpTask(new HttpTask.Builder(mContext, "profileByUserName", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
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
}


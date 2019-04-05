package com.mualab.org.user.activity.review_rating.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;


import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.activity.review_rating.model.ReviewRating;
import com.mualab.org.user.utils.Helper;
import com.squareup.picasso.Picasso;

import java.util.List;


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

    public ReviewRatingAdapter(List<ReviewRating.DataBean> list) {
        this.list = list;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder rvHolder, int position) {

        ViewHolder holder = ((ViewHolder) rvHolder);

        ReviewRating.DataBean mainBean = list.get(position);
        holder.rating.setRating(mainBean.getRating());
        holder.tvMsg.setText(mainBean.getReview());
        holder.tvDate.setText(Helper.formateDateFromstring("yyyy-MM-dd", "dd/MM/yyyy", mainBean.getCrd()));

        try {
            ReviewRating.DataBean.UserDetailBean userDetail = mainBean.getUserDetail().get(0);
            holder.tvName.setText(userDetail.getUserName());
            if (!userDetail.getProfileImage().isEmpty())
                Picasso.with(holder.ivProfilePic.getContext()).load(userDetail.getProfileImage()).placeholder(R.drawable.default_placeholder).
                        into(holder.ivProfilePic);
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
}


package com.mualab.org.user.activity.artist_profile.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mualab.org.user.R;
import com.mualab.org.user.data.feeds.Feeds;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by mindiii on 4/10/18.
 */

public class ArtistFeedGridAdapter extends RecyclerView.Adapter<ArtistFeedGridAdapter.ViewHolder> {
    protected boolean showLoader;
    private final int TEXT_TYPE = 0;
    private final int IMAGE_TYPE = 1;
    private final int VIDEO_TYPE = 2;
    private final int VIEW_TYPE_LOADING = 3;

    private Context mContext;
    private List<Feeds> feedItems;
    private ArtistFeedAdapter.Listener listener;
    private boolean loading;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_feed, parent, false);
        return new ViewHolder(view);

    }



    public void showHideLoading(boolean b) {
        loading = b;
    }


    public interface Listener{
        void onCommentBtnClick(Feeds feed, int pos);
        void onLikeListClick(Feeds feed);
        void onFeedClick(Feeds feed, int index, View v);
        void onClickProfileImage(Feeds feed, ImageView v);
    }

    public void clear(){
        final int size = feedItems.size();
        feedItems.clear();
        notifyItemRangeRemoved(0, size);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      Feeds feeds=feedItems.get(position);
        Picasso.with(mContext).load(feeds.viewPagerlastPos);
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFeedImg;
        public ViewHolder(View itemView) {
            super(itemView);
            ivFeedImg=itemView.findViewById(R.id.ivFeedImg);
        }
    }
}

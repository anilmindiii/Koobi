package com.mualab.org.user.activity.explore.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.listener.FeedsListner;
import com.squareup.picasso.Picasso;
import java.util.List;

public class ExploreGridViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private boolean showLoader;
    private final int FEED_TYPE = 1;
    private final int VIEW_TYPE_LOADING = 2;

    private Context mContext;
    private List<Feeds> feedItems;
    private Listener listener;
    ExSearchTag exSearchTag;
    private FeedsListner feedsListner;
    boolean hideFirstIndex,isTaskDone;

    public interface Listener {
        void onFeedClick(List<Feeds> feed, int index);
    }

    public void isHideFirstIndex(boolean isHide){
        hideFirstIndex = isHide ;
    }

    public ExploreGridViewAdapter(Context mContext, ExSearchTag exSearchTag, List<Feeds> feedItems,
                                  Listener listener, FeedsListner feedsListner, boolean hideFirstIndex) {
        this.mContext = mContext;
        this.feedItems = feedItems;
        this.listener = listener;
        this.exSearchTag = exSearchTag;
        this.feedsListner = feedsListner;
        this.hideFirstIndex = hideFirstIndex;


    }

    public void removeFirstItem(boolean isRemove) {
        if (isRemove)
            if (this.feedItems.size() > 0)
                this.feedItems.remove(0);
    }

    public ExploreGridViewAdapter(Context mContext, ExSearchTag exSearchTag, List<Feeds> feedItems, Listener listener) {
        this.mContext = mContext;
        this.feedItems = feedItems;
        this.listener = listener;
        this.exSearchTag = exSearchTag;
    }

    public void showHideLoading(boolean bool) {
        showLoader = bool;
    }

    public void clear() {
        final int size = feedItems.size();
        feedItems.clear();
        notifyItemRangeRemoved(0, size);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case FEED_TYPE:
                return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_explore_gridview, parent, false));
            case VIEW_TYPE_LOADING:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        Feeds feed = feedItems.get(position);
        return feed == null ? VIEW_TYPE_LOADING : FEED_TYPE;
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loaderViewHolder = (LoadingViewHolder) holder;
            if (showLoader) {
                loaderViewHolder.progressBar.setVisibility(View.VISIBLE);
            } else {
                loaderViewHolder.progressBar.setVisibility(View.GONE);
            }
            return;
        }


        final Feeds feeds = feedItems.get(position);
        final Holder h = ((Holder) holder);

        if (hideFirstIndex)
            if (feedItems.get(position).isShow) {
                h.main_layout.setVisibility(View.GONE);
                h.main_layout.setLayoutParams(new RecyclerView.LayoutParams(0, 0));

            } else h.main_layout.setVisibility(View.VISIBLE);

        feedItems.get(position).userInfo.get(0).userType = exSearchTag.userType;

        if (feeds.feedType.equals("image")) {
            h.videoIcon.setVisibility(View.GONE);
            if (feeds.feedData.size() != 0) {
                Picasso.with(mContext).load(feeds.feedData.get(0).feedPost)
                        .resize(200, 200)
                        .centerCrop().placeholder(R.drawable.gallery_placeholder)
                        .into(h.imageView);
            }

        } else if (feeds.feedType.equals("video")) {
            h.videoIcon.setVisibility(View.VISIBLE);

            if (!feeds.feedData.get(0).videoThumb.equals("")) {
                Picasso.with(mContext).load(feeds.feedData.get(0).videoThumb)
                        .resize(200, 200).centerCrop()
                        .placeholder(R.color.gray2)
                        .into(h.imageView);
            }

        }

    }

    private class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imageView, videoIcon;
        CardView main_layout;

        private Holder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            videoIcon = itemView.findViewById(R.id.videoIcon);
            main_layout = itemView.findViewById(R.id.main_layout);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            // Feeds feed = feedItems.get(pos);
            listener.onFeedClick(feedItems, pos);


        }
    }


}

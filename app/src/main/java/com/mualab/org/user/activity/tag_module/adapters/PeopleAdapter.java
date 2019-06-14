package com.mualab.org.user.activity.tag_module.adapters;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.mualab.org.user.R;
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.mualab.org.user.activity.tag_module.callback.PeopleClickListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class PeopleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int FEED_TYPE = 1;
    private final int VIEW_TYPE_LOADING = 2;
    private boolean showLoader;
    private long mLastClickTime = 0;
    private Context mContext;
    private List<ExSearchTag> mSomeOneList;
    private PeopleClickListener mSomeOneClickListener = null;

    /*private RequestOptions requestOptions = new RequestOptions().centerCrop()
                    .placeholder(R.drawable.defoult_user_img).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.defoult_user_img);*/

    public PeopleAdapter(Context mContext, List<ExSearchTag> mSomeOneList) {
        this.mContext = mContext;
        this.mSomeOneList = mSomeOneList;
    }

    public void setListener(PeopleClickListener mSomeOneClickListener) {
        this.mSomeOneClickListener = mSomeOneClickListener;
    }

    public void showHideLoading(boolean bool) {
        showLoader = bool;
    }

    public void clear() {
        final int size = mSomeOneList.size();
        mSomeOneList.clear();
        notifyItemRangeRemoved(0, size);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case FEED_TYPE:
                return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_explore_top, parent, false));
           /* case VIEW_TYPE_LOADING:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new FooterLoadingViewHolder(view);*/
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        ExSearchTag feed = mSomeOneList.get(position);
        return feed == null ? VIEW_TYPE_LOADING : FEED_TYPE;
    }

    @Override
    public int getItemCount() {
        return mSomeOneList.size();
    }


    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
     /*   if (holder instanceof FooterLoadingViewHolder) {
            FooterLoadingViewHolder loaderViewHolder = (FooterLoadingViewHolder) holder;
            if (showLoader) {
                loaderViewHolder.progressBar.setVisibility(View.VISIBLE);
            } else {
                loaderViewHolder.progressBar.setVisibility(View.GONE);
            }
            return;
        }*/

        final ExSearchTag searchTag = mSomeOneList.get(position);
        final Holder h = ((Holder) holder);

        h.btnFollow.setVisibility(View.GONE);

        if (searchTag.imageUrl != null && !searchTag.imageUrl.isEmpty()) {
            Picasso.with(mContext).load(searchTag.imageUrl).
                    placeholder(R.drawable.default_placeholder).fit().into(h.ivProfile);
        } else Picasso.with(mContext).load(R.drawable.default_placeholder).fit().into(h.ivProfile);

        h.tvHeader.setText(searchTag.title);
        h.tvDesc.setVisibility(View.GONE);
    }

    private class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CircleImageView ivProfile;
        private TextView tvHeader;
        private TextView tvDesc;
        private AppCompatButton btnFollow;

        private Holder(View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvHeader = itemView.findViewById(R.id.tvHeader);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            btnFollow = itemView.findViewById(R.id.btnFollow);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            int pos = getAdapterPosition();
            if (mSomeOneList.size() > pos) {
                ExSearchTag searchTag = mSomeOneList.get(getAdapterPosition());
                if (mSomeOneClickListener != null)
                    mSomeOneClickListener.onPeopleClicked(searchTag, getAdapterPosition());
            }
        }
    }
}


package com.mualab.org.user.activity.tag_module.adapters;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.tag_module.callback.ServiceClickListener;
import com.mualab.org.user.activity.tag_module.models.ServiceTagBean;

import java.util.List;


public class ServiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int FEED_TYPE = 1;
    private final int VIEW_TYPE_LOADING = 2;
    private boolean showLoader;
    private long mLastClickTime = 0;
    private List<ServiceTagBean> serviceTagList;
    private ServiceClickListener listener;

    public ServiceAdapter(List<ServiceTagBean> serviceTagList) {
        this.serviceTagList = serviceTagList;
    }

    public void setListener(ServiceClickListener listener) {
        this.listener = listener;
    }

    public void showHideLoading(boolean bool) {
        showLoader = bool;
    }

    public void clear() {
        final int size = serviceTagList.size();
        serviceTagList.clear();
        notifyItemRangeRemoved(0, size);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case FEED_TYPE:
                return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_tag_list, parent, false));
        /*    case VIEW_TYPE_LOADING:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new FooterLoadingViewHolder(view);*/
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        ServiceTagBean feed = serviceTagList.get(position);
        return feed == null ? VIEW_TYPE_LOADING : FEED_TYPE;
    }

    @Override
    public int getItemCount() {
        return serviceTagList.size();
    }


    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        /*if (holder instanceof FooterLoadingViewHolder) {
            FooterLoadingViewHolder loaderViewHolder = (FooterLoadingViewHolder) holder;
            if (showLoader) {
                loaderViewHolder.progressBar.setVisibility(View.VISIBLE);
            } else {
                loaderViewHolder.progressBar.setVisibility(View.GONE);
            }
            return;
        }*/

        final ServiceTagBean searchTag = serviceTagList.get(position);
        final Holder h = ((Holder) holder);

        h.tvName.setText(searchTag.title);
        h.tvServiceAmt.setText(h.tvServiceAmt.getContext().getString(R.string.pond_symbol)
                .concat(searchTag.inCallPrice.equals("0.0") || searchTag.inCallPrice.isEmpty() ? searchTag.outCallPrice : searchTag.inCallPrice));
        h.tvServiceTime.setText(searchTag.completionTime);
    }

    private class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvName, tvServiceTime, tvServiceAmt;

        private Holder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvServiceAmt = itemView.findViewById(R.id.tvServiceAmt);
            tvServiceTime = itemView.findViewById(R.id.tvServiceTime);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            int pos = getAdapterPosition();
            if (serviceTagList.size() > pos) {
                ServiceTagBean searchTag = serviceTagList.get(getAdapterPosition());
                if (listener != null)
                    listener.onServiceClicked(searchTag, getAdapterPosition());
            }
        }
    }
}


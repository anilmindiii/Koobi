package com.mualab.org.user.activity.artist_profile.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.model.Services;

import java.util.List;

/**
 * Created by mindiii on 8/1/19.
 */

public class CustomStringAdapter extends RecyclerView.Adapter<CustomStringAdapter.ViewHolder> {

    Context mContext;
    onClickItem onClickItemListner;
    onClickItemCategory onClickCategoryListner;
    Services services;
    List<Services.ArtistServicesBean.SubServiesBean> artistServicesBean;
    String type = "";
    public int pos = 0;

    public CustomStringAdapter(String type, Services services,
                               List<Services.ArtistServicesBean.SubServiesBean> artistServicesBean,
                               Context mContext, onClickItem onClickItemListner,
                               onClickItemCategory onClickCategoryListner) {
        this.mContext = mContext;
        this.onClickItemListner = onClickItemListner;
        this.onClickCategoryListner = onClickCategoryListner;
        this.services = services;
        this.artistServicesBean = artistServicesBean;
        this.type = type;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_string, parent, false);
        return new ViewHolder(view);
    }

    public interface onClickItem {
        void onclick(Services.ArtistServicesBean artistServicesBean, int adapterPosition);
    }

    public interface onClickItemCategory {
        void onclick(Services.ArtistServicesBean.SubServiesBean bean, int pos);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (type.equals("bizType")) {
            Services.ArtistServicesBean servicesBean = services.artistServices.get(position);
            holder.tv_service.setText(servicesBean.serviceName);
        } else {
            holder.tv_service.setText(artistServicesBean.get(position).subServiceName);
        }
    }

    public void clickItem() {
        if (services.artistServices.size() != 0)
            onClickItemListner.onclick(services.artistServices.get(pos), pos);
    }

    public void clickItem(int customPosition) {
        onClickItemListner.onclick(services.artistServices.get(customPosition), customPosition);
    }

    @Override
    public int getItemCount() {
        if (type.equals("bizType")) {
            return services.artistServices.size();
        } else return artistServicesBean.size();


    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_service;

        public ViewHolder(View itemView) {
            super(itemView);

            tv_service = itemView.findViewById(R.id.tv_service);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            pos = getAdapterPosition();
            if (type.equals("bizType")) {
                onClickItemListner.onclick(services.artistServices.get(getAdapterPosition()), getAdapterPosition());
            } else {
                onClickCategoryListner.onclick(artistServicesBean.get(getAdapterPosition()), getAdapterPosition());
            }
        }
    }
}

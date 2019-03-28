package com.mualab.org.user.activity.artist_profile.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.model.Services;
import com.mualab.org.user.utils.Helper;
import java.util.ArrayList;

/**
 * Created by mindiii on 9/1/19.
 */

public class IncallOutCallAdapter extends RecyclerView.Adapter<IncallOutCallAdapter.ViewHolder> {
    Context mContext;
    childItemClick clickListner;
    ArrayList<Services.ArtistServicesBean.SubServiesBean.ArtistservicesBean> inCallList;
    String callType;
    boolean isBookingView;


    public IncallOutCallAdapter(Context mContext,
                                ArrayList<Services.ArtistServicesBean.SubServiesBean.ArtistservicesBean> inCallList,
                                String callType,
                                boolean isBookingView) {
        this.mContext = mContext;
        this.inCallList = inCallList;
        this.callType = callType;
        this.isBookingView = isBookingView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incall_outcall, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.tv_inoutcall_service.setText(inCallList.get(position).title + "");

        if (isBookingView) {
            holder.service_view.setVisibility(View.GONE);
            holder.ly_booking_view.setVisibility(View.VISIBLE);

            if (callType.equals("In Call")) {

                if (inCallList.get(position).incallStaff) {
                    holder.service_name.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                    holder.duration.setVisibility(View.GONE);
                    holder.service_price.setVisibility(View.GONE);
                } else {
                    holder.service_name.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    holder.duration.setVisibility(View.VISIBLE);
                    holder.service_price.setVisibility(View.VISIBLE);

                }
            }else {
                if (inCallList.get(position).outcallStaff) {
                    holder.service_name.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                    holder.duration.setVisibility(View.GONE);
                    holder.service_price.setVisibility(View.GONE);
                } else {
                    holder.service_name.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    holder.duration.setVisibility(View.VISIBLE);
                    holder.service_price.setVisibility(View.VISIBLE);

                }
            }

        } else {
            holder.ly_booking_view.setVisibility(View.GONE);
            holder.service_view.setVisibility(View.VISIBLE);
        }

        if (inCallList.get(position).isSelected) {
            holder.service_name.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else holder.service_name.setTextColor(ContextCompat.getColor(mContext, R.color.black));

        holder.service_name.setText(inCallList.get(position).title + "");

        if (callType.equals("In Call")) {
            holder.service_price.setText("£" + inCallList.get(position).inCallPrice + "");
        } else holder.service_price.setText("£" + inCallList.get(position).outCallPrice + "");


        String hour = Helper.formateDateFromstring("HH:mm", "HH", inCallList.get(position).completionTime);
        String min = Helper.formateDateFromstring("HH:mm", "mm", inCallList.get(position).completionTime);

        if (hour.equals("00")) {
            holder.duration.setText(min + " min");
        } else holder.duration.setText(hour + " hr " + min + " min");
    }

    public interface childItemClick {
        void childClick(Services.ArtistServicesBean.SubServiesBean.ArtistservicesBean artistservicesBean, String callType, int adapterPosition);
    }

    public void setClickListner(childItemClick click) {
        this.clickListner = click;

    }

    @Override
    public int getItemCount() {
        return inCallList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_inoutcall_service;
        RelativeLayout service_view, ly_booking_view;
        TextView service_name, service_price, duration;

        public ViewHolder(View itemView) {
            super(itemView);

            tv_inoutcall_service = itemView.findViewById(R.id.tv_inoutcall_service);
            service_view = itemView.findViewById(R.id.service_view);
            ly_booking_view = itemView.findViewById(R.id.ly_booking_view);
            service_name = itemView.findViewById(R.id.service_name);
            service_price = itemView.findViewById(R.id.service_price);
            duration = itemView.findViewById(R.id.duration);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListner.childClick(inCallList.get(getAdapterPosition()), callType, getAdapterPosition());

            for (int i = 0; i < inCallList.size(); i++) inCallList.get(i).isSelected = false;
            inCallList.get(getAdapterPosition()).isSelected = true;

            notifyDataSetChanged();

        }
    }
}

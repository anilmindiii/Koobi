package com.mualab.org.user.activity.booking.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.model.ServiceInfoBooking;
import com.mualab.org.user.utils.Helper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mindiii on 16/1/19.
 **/

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.ViewHolder>{
    List<ServiceInfoBooking.StaffInfoBean> staffInfoBeanList;
    Context mContext;
    String typeCall;
    click clickListner;

    public StaffAdapter(Context mContext, ArrayList<ServiceInfoBooking.StaffInfoBean> staffInfoBeanList,String typeCall, click clickListner) {
        this.mContext = mContext;
        this.staffInfoBeanList = staffInfoBeanList;
        this.typeCall = typeCall;
        this.clickListner = clickListner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_staff,parent,false);
        return new ViewHolder(view);
    }

    public interface click{
        void OnClickAdapter(ServiceInfoBooking.StaffInfoBean bean);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceInfoBooking.StaffInfoBean bean = staffInfoBeanList.get(position);

        if (!bean.staffImage.isEmpty() && !bean.staffImage.equals("")) {
            Picasso.with(mContext).load(bean.staffImage).placeholder(R.drawable.default_placeholder).
                    fit().into(holder.iv_profileImage);
        }else {
            holder.iv_profileImage.setImageResource(R.drawable.default_placeholder);
        }


        String hour = Helper.formateDateFromstring("HH:mm", "HH", bean.completionTime);
        String min = Helper.formateDateFromstring("HH:mm", "mm", bean.completionTime);
        if(hour.equals("00")){
            holder.tv_time_duration.setText(min+" min");
        }else holder.tv_time_duration.setText(hour +" hr "+min+" min");

        if(staffInfoBeanList.get(position).bookingType.equals("Incall")){
            holder.tv_price.setText("£" + bean.inCallPrice+"");
        }else {
            holder.tv_price.setText("£" + bean.outCallPrice+"");
        }

        holder.tv_user_name.setText(bean.staffName);

        if(!bean.isSelected){
            holder.tv_user_name.setTextColor(ContextCompat.getColor(mContext,R.color.black));
        }else {
            holder.tv_user_name.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
        }
    }

    @Override
    public int getItemCount() {
        return staffInfoBeanList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView iv_profileImage;
        TextView tv_user_name,tv_price,tv_time_duration;

        public ViewHolder(View itemView) {
            super(itemView);

            iv_profileImage = itemView.findViewById(R.id.iv_profileImage);
            tv_user_name = itemView.findViewById(R.id.tv_user_name);
            tv_price = itemView.findViewById(R.id.tv_price);
            tv_time_duration = itemView.findViewById(R.id.tv_time_duration);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(getAdapterPosition() == 0){
                staffInfoBeanList.get(getAdapterPosition()).staffId = 0;
            }

            clickListner.OnClickAdapter(staffInfoBeanList.get(getAdapterPosition()));

            for(int i=0;i<staffInfoBeanList.size();i++){
                staffInfoBeanList.get(i).isSelected = false;
            }
            staffInfoBeanList.get(getAdapterPosition()).isSelected = true;
            notifyDataSetChanged();

        }
    }


}

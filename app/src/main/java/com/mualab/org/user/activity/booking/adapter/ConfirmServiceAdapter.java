package com.mualab.org.user.activity.booking.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.model.BookingConfirmInfo;
import com.mualab.org.user.utils.Helper;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by mindiii on 22/1/19.
 */

public class ConfirmServiceAdapter extends RecyclerView.Adapter<ConfirmServiceAdapter.ViewHolder> {

    Context mContext;
    List<BookingConfirmInfo.DataBean> bookingList;
    getValue valueListner;

    public ConfirmServiceAdapter(Context mContext,List<BookingConfirmInfo.DataBean> bookingList, getValue valueListner) {
        this.mContext = mContext;
        this.bookingList = bookingList;
        this.valueListner = valueListner;
    }


    public interface getValue{
        void deleteService(int bookingId,int position);
        void editService(BookingConfirmInfo.DataBean dataBean);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.confirm_service_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        BookingConfirmInfo.DataBean bean = bookingList.get(position);

        if (!bean.staffImage.isEmpty() && !bean.staffImage.equals("")) {
            Picasso.with(mContext).load(bean.staffImage).placeholder(R.drawable.default_placeholder).
                    fit().into(holder.iv_profile);
        }else {
            holder.iv_profile.setImageResource(R.drawable.default_placeholder);
        }


        bean.bookingDate = Helper.formateDateFromstring("yyyy-MM-dd","dd/MM/yyyy",bean.bookingDate);
        holder.tv_date_time.setText(bean.bookingDate + ", " + bean.startTime);
        holder.tv_service_name.setText(bean.artistServiceName);
        holder.tv_name.setText(bean.staffName);
        holder.tv_price.setText("£"+bean.bookingPrice);

        holder.tv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valueListner.editService(bookingList.get(position));
            }
        });

        holder.tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valueListner.deleteService(bookingList.get(position)._id,position);
            }
        });

        holder.iv_service_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valueListner.editService(bookingList.get(position));
            }
        });

        holder.iv_price_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valueListner.editService(bookingList.get(position));
            }
        });
        holder.iv_time_date_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valueListner.editService(bookingList.get(position));
            }
        });
    }



    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name,tv_service_name,tv_date_time,tv_edit,tv_delete,tv_price;
        ImageView iv_profile,iv_service_arrow,iv_price_arrow,iv_time_date_arrow;

        public ViewHolder(View itemView) {
            super(itemView);

            iv_profile = itemView.findViewById(R.id.iv_profile);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_service_name = itemView.findViewById(R.id.tv_service_name);
            tv_date_time = itemView.findViewById(R.id.tv_date_time);
            tv_edit = itemView.findViewById(R.id.tv_edit);
            tv_delete = itemView.findViewById(R.id.tv_delete);
            tv_price = itemView.findViewById(R.id.tv_price);

            iv_service_arrow = itemView.findViewById(R.id.iv_service_arrow);
            iv_price_arrow = itemView.findViewById(R.id.iv_price_arrow);
            iv_time_date_arrow = itemView.findViewById(R.id.iv_time_date_arrow);
        }
    }
}

package com.mualab.org.user.activity.booking.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.booking.model.BookingListInfo;
import com.mualab.org.user.activity.feeds.activity.ReportActivity;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.Util;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by mindiii on 1/2/19.
 */

public class BookingHistoryDetailsAdapter extends RecyclerView.Adapter<BookingHistoryDetailsAdapter.ViewHolder>{

    Context mContext;
    BookingListInfo bookingListInfo;
    getBookingInfo bookingInfoListner;

    public BookingHistoryDetailsAdapter(Context mContext, BookingListInfo bookingListInfo
            , getBookingInfo bookingInfoListner) {
        this.mContext = mContext;
        this.bookingListInfo = bookingListInfo;
        this.bookingInfoListner = bookingInfoListner;
    }

    public interface getBookingInfo{
        public void bookInfo(int pos);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_details_booking,parent,false);
        return new BookingHistoryDetailsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final BookingListInfo.DataBean.BookingInfoBean bean = bookingListInfo.data.bookingInfo.get(position);

        if (!bean.staffImage.isEmpty() && !bean.staffImage.equals("")) {
            Picasso.with(mContext).load(bean.staffImage).placeholder(R.drawable.default_placeholder).
                    fit().into(holder.iv_profile);
        }else {
            holder.iv_profile.setImageResource(R.drawable.default_placeholder);
        }


        if(bookingListInfo.data.bookingType == 2){
            if(bean.status == 0 || bean.status == 3 ){
                holder.ivLocation.setVisibility(View.GONE);
            }else holder.ivLocation.setVisibility(View.VISIBLE);
        }



        holder.iv_profile.setOnClickListener(v->{
            Intent intent = new Intent(mContext, ArtistProfileActivity.class);
            intent.putExtra("artistId", String.valueOf(bean.staffId));
            mContext.startActivity(intent);
        });

        boolean isTimeNotPass = checktimings(bean.bookingDate+ " " + bean.startTime);
        bean.bookingDate = Helper.formateDateFromstring("yyyy-MM-dd","dd/MM/yyyy",bean.bookingDate);
        holder.tv_date_time.setText(bean.bookingDate + ", " + bean.startTime +" - "+bean.endTime);
        holder.tv_service_name.setText(bean.artistServiceName);
        holder.tv_name.setText(bean.staffName);
        holder.tv_price.setText("£"+ Util.getTwoDigitDecimal(bean.bookingPrice));

        if(bookingListInfo.data.bookStatus.equals("0") || isTimeNotPass){
            holder.iv_report_service.setVisibility(View.GONE);
        }else {
            holder.iv_report_service.setVisibility(View.VISIBLE);
        }


        holder.ivLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookingInfoListner.bookInfo( position);
            }
        });

        holder.iv_report_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,ReportActivity.class);
                intent.putExtra("staffId",bean.staffId);
                intent.putExtra("artistServiceName",bean.artistServiceName);
                intent.putExtra("reportForUser",bean.staffId);
                intent.putExtra("bookingId",bookingListInfo.data._id);
                intent.putExtra("bookingInfoId",bean._id);
                intent.putExtra("artistId",bookingListInfo.data.artistId);
                intent.putExtra("artistServiceId",bookingListInfo.data.artistDetail.get(0)._id);

                if(bean.bookingReport.size() == 0){
                    intent.putExtra("writeReport","yes");
                }else  {
                    intent.putExtra("writeReport","no");
                    int size  = (bean.bookingReport.size()-1);
                    intent.putExtra("bookingReport",bean.bookingReport.get(size));
                }



                mContext.startActivity(intent);
            }
        });


        /*........................................................*/
        try {
            if (bookingListInfo.data.bookStatus.equals("1") || bookingListInfo.data.bookStatus.equals("5")) {
                for (BookingListInfo.DataBean.BookingInfoBean tempBookBean : bookingListInfo.data.bookingInfo) {
                    if (tempBookBean.status != 0) {
                        holder.rlStatus.setVisibility(View.VISIBLE);
                        holder.tvStatus.setText(getBookingStatus(holder, bookingListInfo.data.bookingInfo.get(position)));
                        break;
                    }
                }
            }
            else{
                holder.rlStatus.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
            Log.d("key",ignored.toString());
        }


    }


    private String getBookingStatus(ViewHolder holder, BookingListInfo.DataBean.BookingInfoBean mainBean) {
        String tempStatus = "";
        if (bookingListInfo.data.bookStatus.equals("1") || bookingListInfo.data.bookStatus.equals("3")
                || bookingListInfo.data.bookStatus.equals("5")) {

            switch (mainBean.status) {
                case 0:
                    tempStatus = "Confirm";
                    holder.tvStatus.setTextColor(holder.tvStatus.getContext().getResources().getColor(R.color.main_green_color));
                    break;

                case 1:
                    tempStatus = "On the way";
                    holder.tvStatus.setTextColor(holder.tvStatus.getContext().getResources().getColor(R.color.main_green_color));
                    break;

                case 2:
                    tempStatus = "On going";
                    holder.tvStatus.setTextColor(holder.tvStatus.getContext().getResources().getColor(R.color.colorPrimary));
                    break;

                case 3:
                case 5:
                    tempStatus = "Service end";
                    holder.tvStatus.setTextColor(holder.tvStatus.getContext().getResources().getColor(R.color.main_green_color));
                    break;

                default:
                    tempStatus = "Service end";
                    holder.tvStatus.setTextColor(holder.tvStatus.getContext().getResources().getColor(R.color.main_green_color));
                    break;
            }
        }

        return tempStatus;
    }

    @Override
    public int getItemCount() {
        try {
            return bookingListInfo.data.bookingInfo.size();
        }catch (Exception e){

        }
        return 0;

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name,tv_service_name,tv_date_time,tv_edit,tv_delete,tv_price,tvStatus;
        ImageView iv_profile,iv_service_arrow,iv_price_arrow,iv_time_date_arrow,iv_report_service,ivLocation;
        LinearLayout rlStatus;

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
            iv_report_service = itemView.findViewById(R.id.iv_report_service);
            ivLocation = itemView.findViewById(R.id.ivLocation);

            tvStatus = itemView.findViewById(R.id.tvStatus);
            rlStatus = itemView.findViewById(R.id.rlStatus);
        }
    }

    public String giveDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd h:mm");
        return sdf.format(cal.getTime());
    }

    private boolean checktimings(String endtime) {

        String pattern = "yyyy-MM-dd h:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        try {
            Date date1 = sdf.parse(giveDate());//start time
            Date date2 = sdf.parse(endtime);

            if(date1.before(date2)) {
                return true;
            } else {

                return false;
            }
        } catch (ParseException e){
            e.printStackTrace();
        }
        return false;
    }
}

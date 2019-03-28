package com.mualab.org.user.activity.booking.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.model.ServiceInfoBooking;
import com.mualab.org.user.activity.booking.model.TimeSlotInfo;

import java.util.ArrayList;

/**
 * Created by mindiii on 16/1/19.
 */

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {

    Context mContext;
    ArrayList<TimeSlotInfo> timeSlotList;
    getSelectTime selectTimeListner;
    public  int lastPos = -1;
    public boolean isSelectedFirst;

    public TimeSlotAdapter(Context mContext, ArrayList<TimeSlotInfo> timeSlotList,getSelectTime selectTimeListner) {
        this.mContext = mContext;
        this.timeSlotList = timeSlotList;
        this.selectTimeListner = selectTimeListner;
    }

    public interface getSelectTime{
        void getSelectedTimeSlot(TimeSlotInfo slotInfo);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_slot,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.tv_time_slot.setText(timeSlotList.get(position).timeSlots);

        if(timeSlotList.get(position).isSelectSlot){
            holder.tv_time_slot.setTextColor(ContextCompat.getColor(mContext,R.color.white));
            holder.clock.setColorFilter(ContextCompat.getColor(mContext, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
            holder.ly_main.setBackgroundResource(R.drawable.rounded_border_dark_selected);

        }else if(isSelectedFirst){
            holder.tv_time_slot.setTextColor(ContextCompat.getColor(mContext,R.color.gray));
            holder.clock.setColorFilter(ContextCompat.getColor(mContext, R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN);
            holder.ly_main.setBackgroundResource(R.drawable.rounded_border_round_gray);
        }else {
            holder.tv_time_slot.setTextColor(ContextCompat.getColor(mContext,R.color.black));
            holder.clock.setColorFilter(ContextCompat.getColor(mContext, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
            holder.ly_main.setBackgroundResource(R.drawable.rounded_border_dark);
        }

        holder.ly_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int pos= holder.getAdapterPosition();
                if (lastPos==-1){
                    for (int i = 0; i < timeSlotList.size(); i++) {
                        isSelectedFirst = false;
                        timeSlotList.get(i).isSelectSlot = false;
                    }

                    isSelectedFirst = true;
                    timeSlotList.get(pos).isSelectSlot = true;
                    lastPos = pos;
                    selectTimeListner.getSelectedTimeSlot(timeSlotList.get(pos));
                    notifyDataSetChanged();
                    selectTimeListner.getSelectedTimeSlot(timeSlotList.get(position));
                }else if (lastPos==pos){
                    for (int i = 0; i < timeSlotList.size(); i++) {
                        isSelectedFirst = false;
                        timeSlotList.get(i).isSelectSlot = false;
                    }

                    isSelectedFirst = false;
                    timeSlotList.get(pos).isSelectSlot = false;
                    lastPos = -1;
                    selectTimeListner.getSelectedTimeSlot(timeSlotList.get(pos));
                    notifyDataSetChanged();
                    selectTimeListner.getSelectedTimeSlot(timeSlotList.get(position));
                }


            }
        });


    }

    @Override
    public int getItemCount() {
        return timeSlotList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_time_slot;
        LinearLayout ly_main;
        ImageView clock;

        public ViewHolder(View itemView) {
            super(itemView);

            tv_time_slot = itemView.findViewById(R.id.tv_time_slot);
            ly_main = itemView.findViewById(R.id.ly_main);
            clock = itemView.findViewById(R.id.clock);
        }
    }
}

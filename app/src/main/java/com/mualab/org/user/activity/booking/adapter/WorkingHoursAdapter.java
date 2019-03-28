package com.mualab.org.user.activity.booking.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.WorkingHourActivity;

import java.util.ArrayList;

/**
 * Created by mindiii on 25/1/19.
 */

public class WorkingHoursAdapter extends RecyclerView.Adapter<WorkingHoursAdapter.ViewHolder> {
    ArrayList<WorkingHourActivity.WorkingInfo> workingTemp0;

    public WorkingHoursAdapter(ArrayList<WorkingHourActivity.WorkingInfo> workingTemp0) {
        this.workingTemp0 = workingTemp0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_working_hours,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_time_start.setText(workingTemp0.get(position).StartTime);
        holder.tv_time_end.setText(workingTemp0.get(position).End);
    }

    @Override
    public int getItemCount() {
        return workingTemp0.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_time_start,tv_time_end;

        public ViewHolder(View itemView) {
            super(itemView);

            tv_time_start = itemView.findViewById(R.id.tv_time_start);
            tv_time_end = itemView.findViewById(R.id.tv_time_end);
        }
    }
}

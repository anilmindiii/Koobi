package com.mualab.org.user.activity.businessInvitaion.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.data.model.booking.BusinessDay;
import com.mualab.org.user.data.model.booking.TimeSlot;
import com.mualab.org.user.dialogs.MyToast;

import java.util.ArrayList;
import java.util.List;


public class CompanyOperationHoursAdapter extends RecyclerView.Adapter<CompanyOperationHoursAdapter.ViewHolder> {

    private List<BusinessDay> businessDaysList;
    private Context mContext;
    private boolean isEditable;

    public CompanyOperationHoursAdapter(Context mContext, List<BusinessDay> businessHours, boolean isEditable) {
        this.mContext = mContext;
        this.businessDaysList = businessHours;
        this.isEditable = isEditable;
    }

    private synchronized static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_business_days, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        BusinessDay day = businessDaysList.get(position);
        holder.tv_dayName.setText(day.dayName);
        holder.tv_workingStatus.setVisibility(day.isOpen ? View.INVISIBLE : View.VISIBLE);
        holder.listView.setVisibility(day.isOpen ? View.VISIBLE : View.GONE);
        holder.checkbox.setChecked(day.isOpen);

        if (!isEditable) {
            holder.checkbox.setEnabled(false);
        }

        if (day.isExpand) {
            RelativeLayout.LayoutParams layout_description =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            holder.rlParent.setLayoutParams(layout_description);
        } else {
            RelativeLayout.LayoutParams layout_description =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            holder.rlParent.setLayoutParams(layout_description);
        }

        if (day.isOpen) {
            holder.tv_dayName.setTextColor(mContext.getResources().getColor(R.color.black));
        } else {
            holder.tv_dayName.setTextColor(mContext.getResources().getColor(R.color.gray));
        }

        holder.checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final boolean isChecked = holder.checkbox.isChecked();
                // Do something here.
                int pos = holder.getAdapterPosition();
                BusinessDay day = businessDaysList.get(pos);
                day.isOpen = isChecked;

                if (isChecked && (day.slots == null || day.slots.size() == 0)) {
                    assert day.slots != null;
                    day.slots.add(new TimeSlot(day.dayId));
                } else if (!isChecked) {
                    day.slots.clear();
                    //day.addTimeSlot(new TimeSlot(-1));
                }
                notifyItemChanged(pos);
            }
        });

        if (day.isOpen) {
            AdapterTimeSlot adapterTimeSlot = new AdapterTimeSlot(mContext, day.slots);
            holder.listView.setAdapter(adapterTimeSlot);
            setListViewHeightBasedOnChildren(holder.listView);
        }
    }

    @Override
    public int getItemCount() {
        return businessDaysList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CheckBox checkbox;
        // ImageView ivAddTimeSlot;
        RelativeLayout rlParent;
        ListView listView;
        TextView tv_dayName, tv_workingStatus;

        private ViewHolder(View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox);
            tv_dayName = itemView.findViewById(R.id.tv_dayName);
            tv_workingStatus = itemView.findViewById(R.id.tv_workingStatus);
            //ivAddTimeSlot =  itemView.findViewById(R.id.ivAddTimeSlot);
            rlParent = itemView.findViewById(R.id.rlParent);
            listView = itemView.findViewById(R.id.listView);
            checkbox.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            switch (v.getId()) {

                case R.id.checkbox:
                    break;


                case R.id.action_search:
                    break;
            }
        }
    }

    public class AdapterTimeSlot extends ArrayAdapter<TimeSlot> {

        private ArrayList<TimeSlot> timeSlots = new ArrayList<>();

        private AdapterTimeSlot(Context context, ArrayList<TimeSlot> objects) {
            super(context, 0, objects);
            timeSlots = objects;
        }

        @Override
        public int getCount() {
            return timeSlots.size();
        }

        @NonNull
        @Override
        public View getView(final int position, View v, @NonNull ViewGroup parent) {
            // Get the data item for this position
            final TimeSlot timeSlot = timeSlots.get(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (v == null) {
                v = LayoutInflater.from(getContext()).inflate(R.layout.adapter_business_hours, parent, false);
            }
            // Lookup view for data population

            TextView tv_from = v.findViewById(R.id.tv_from);
            TextView tv_to = v.findViewById(R.id.tv_to);
            // View viewDivider = v.findViewById(R.id.viewDivider);
            //ImageView iv_delete = v.findViewById(R.id.iv_delete);
            LinearLayout ll_delete = v.findViewById(R.id.ll_delete);

            //  if (timeSlot.isFirst){
            //  }else
            //     ll_delete.setVisibility(View.VISIBLE);

            // Populate the data into the template view using the data object
            //  tv_from.setText(String.format("From: %s", timeSlot.startTime));
            //   tv_to.setText(String.format("To: %s", timeSlot.endTime));
            tv_from.setText(timeSlot.startTime);
            tv_to.setText(timeSlot.endTime);
            //   viewDivider.setVisibility(timeSlots.size()==1?View.GONE:View.VISIBLE);
            if (isEditable)
                ll_delete.setVisibility(timeSlots.size() == 1 ? View.GONE : View.VISIBLE);
            else ll_delete.setVisibility(View.GONE);


            // Return the completed view to render on screen
            return v;
        }

    }
}

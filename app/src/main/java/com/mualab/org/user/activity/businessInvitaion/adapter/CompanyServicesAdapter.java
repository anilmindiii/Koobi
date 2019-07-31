package com.mualab.org.user.activity.businessInvitaion.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.daimajia.swipe.SwipeLayout;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.businessInvitaion.model.Services;
import com.mualab.org.user.utils.Util;

import java.util.List;


public class CompanyServicesAdapter extends RecyclerView.Adapter<CompanyServicesAdapter.ViewHolder> {
    private Context context;
    private List<Services> servicesList;
    private onClickListener listener;

    public interface onClickListener{
        //  void onEditClick(int pos);
        //   void onDelClick(int pos);
        void onClick(int pos);
    }
    // Constructor of the class
    public CompanyServicesAdapter(Context context, List<Services> servicesList,onClickListener listener) {
        this.context = context;
        this.servicesList = servicesList;
        this.listener = listener;
    }

    // get the size of the list
    @Override
    public int getItemCount() {
        return servicesList.size();
    }


    // specify the row layout file and click for each row
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_services_layout, parent, false);
        return new ViewHolder(view);
    }

    // load data in each row element
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int listPosition) {
        int pos = listPosition - 1;
        int tempPos = (pos == -1) ? pos + 1 : pos;

        Services item = servicesList.get(listPosition);

        holder.tvServiceTitle.setText(item.serviceName);

        holder.sample1.setRightSwipeEnabled(false);
        holder.dotView.setBackground(context.getResources().getDrawable(R.drawable.gray_circle));

        String totalTime = item.completionTime;
        if (totalTime.contains(":")){
            String[] separated = totalTime.split(":");
            String hours = separated[0]+" hr ";
            String min = separated[1]+" min";

            if (hours.equals("00 hr "))
                holder.tvServiceTime.setText(min);
            else if (!hours.equals("00 hr ") && min.equals("00 min"))
                holder.tvServiceTime.setText(hours);
            else
                holder.tvServiceTime.setText(hours+min);

        }

        // holder.tvServiceTime.setText(item.completionTime);
        holder.tvServiceTitle.setText(item.serviceName);

        if (item.bookingType.equals("Outcall"))
            holder.tvPrice.setText("£"+ Util.getTwoDigitDecimal(item.outCallPrice));
        else
            holder.tvPrice.setText("£"+Util.getTwoDigitDecimal(item.inCallPrice));

        if (listPosition==0){
            holder.tvCategoryTitle.setText(item.subserviceName);
            holder.tvBizTypeTitle.setText(item.bizTypeName);
            holder.llCategory.setVisibility(View.VISIBLE);
            holder.tvBizTypeTitle.setVisibility(View.VISIBLE);
            holder.viewLine.setVisibility(View.VISIBLE);

        }else {
            if (item.serviceId!=servicesList.get(tempPos).serviceId) {
                holder.tvBizTypeTitle.setVisibility(View.VISIBLE);
                holder.tvBizTypeTitle.setText(item.bizTypeName);

                if (item.subserviceId!=servicesList.get(tempPos).subserviceId) {
                    holder.tvCategoryTitle.setText(item.subserviceName);
                    holder.llCategory.setVisibility(View.VISIBLE);
                }else {
                    holder.llCategory.setVisibility(View.GONE);
                    holder.viewLine.setVisibility(View.GONE);
                }

            }else {
                holder.tvBizTypeTitle.setVisibility(View.GONE);

                if (item.subserviceId!=servicesList.get(tempPos).subserviceId) {
                    holder.tvCategoryTitle.setText(item.subserviceName);
                    holder.llCategory.setVisibility(View.VISIBLE);
                }else {
                    holder.llCategory.setVisibility(View.GONE);
                    holder.viewLine.setVisibility(View.GONE);
                }
            }
        }

        holder.sample1.setShowMode(SwipeLayout.ShowMode.PullOut);

    }

    // Static inner class to initialize the views of rows
    class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        private TextView tvServiceTime,tvPrice,tvCategoryTitle,tvBizTypeTitle,tvServiceTitle;
        private SwipeLayout sample1;
        private ImageView ivEdit,ivRemove;
        private RelativeLayout rlItem;
        private LinearLayout llCategory;
        private View viewLine,dotView;

        private ViewHolder(View itemView)
        {
            super(itemView);
            tvBizTypeTitle =  itemView.findViewById(R.id.tvBizTypeTitle);
            tvCategoryTitle = itemView.findViewById(R.id.tvCategoryTitle);
            tvServiceTitle = itemView.findViewById(R.id.tvServiceTitle);
            tvServiceTime =  itemView.findViewById(R.id.tvServiceTime);
            tvPrice =  itemView.findViewById(R.id.tvPrice);
            viewLine =  itemView.findViewById(R.id.viewLine);
            dotView =  itemView.findViewById(R.id.view);

            rlItem =  itemView.findViewById(R.id.rlItem);
            llCategory =  itemView.findViewById(R.id.llCategory);

            ivEdit =  itemView.findViewById(R.id.ivEdit);
            ivRemove =  itemView.findViewById(R.id.ivRemove);


            ivEdit.setOnClickListener(this);
            ivRemove.setOnClickListener(this);
            rlItem.setOnClickListener(this);

            sample1 =  itemView.findViewById(R.id.sample1);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){

             /*   case R.id.ivEdit:
                    int position=getAdapterPosition();
                    if (position!= -1){
                        listener.onEditClick(position);
                    }
                    break;

                case  R.id.ivRemove :
                    position=getAdapterPosition();
                    if (position!= -1){
                        listener.onDelClick(position);
                    }

                    break;*/

                case  R.id.rlItem :
                    if (getAdapterPosition()!= -1){
                        listener.onClick(getAdapterPosition());
                    }

                    break;
            }
        }
    }

}

package com.mualab.org.user.activity.explore.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.explore.model.ExploreCategoryInfo;

import java.util.ArrayList;

public class ExploreServiceAdapter extends RecyclerView.Adapter<ExploreServiceAdapter.ViewHolder> {
    Context mContext;
    ArrayList<ExploreCategoryInfo.DataBean> dataBeans;
    getValueListner valueListner;


    public ExploreServiceAdapter(Context mContext, ArrayList<ExploreCategoryInfo.DataBean> dataBeans,getValueListner valueListner) {
        this.mContext = mContext;
        this.dataBeans = dataBeans;
        this.valueListner = valueListner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_explore_service,viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        ExploreCategoryInfo.DataBean dataBean = dataBeans.get(i);
        viewHolder.category_name.setText(dataBean.title);

        if(dataBean.isCheckedCategory){
            viewHolder.category_name.setTextColor(ContextCompat.getColor(mContext,R.color.white));
            viewHolder.cv_category.setBackgroundResource(R.drawable.rounded_border_gray_selected);
        }else {
            viewHolder.category_name.setTextColor(ContextCompat.getColor(mContext,R.color.black));
            viewHolder.cv_category.setBackgroundResource(R.drawable.rounded_border_dark_unselected);

        }
    }

    @Override
    public int getItemCount() {
        return dataBeans.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView category_name;
        RelativeLayout cv_category;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            category_name = itemView.findViewById(R.id.category_name);
            cv_category = itemView.findViewById(R.id.cv_category);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(dataBeans.get(getAdapterPosition()).isCheckedCategory){
                dataBeans.get(getAdapterPosition()).isCheckedCategory = false;
            }else {
                dataBeans.get(getAdapterPosition()).isCheckedCategory = true;
            }

            notifyDataSetChanged();
            valueListner.getValue();
        }
    }

    public interface getValueListner{
        void getValue();
    }
}

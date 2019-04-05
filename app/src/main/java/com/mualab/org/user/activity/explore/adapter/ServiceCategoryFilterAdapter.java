package com.mualab.org.user.activity.explore.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.explore.model.ExploreCategoryInfo;
import com.mualab.org.user.activity.searchBoard.fragment.RefineSubServiceAdapter;

import java.util.ArrayList;

public class ServiceCategoryFilterAdapter extends RecyclerView.Adapter<ServiceCategoryFilterAdapter.ViewHolder> {
    ArrayList<ExploreCategoryInfo.DataBean> searchList;
    Context mContext;
    int lastCatPosition;


    public ServiceCategoryFilterAdapter(ArrayList<ExploreCategoryInfo.DataBean> searchList, Context mContext,int lastCatPosition) {
        this.searchList = searchList;
        this.mContext = mContext;
        this.lastCatPosition = lastCatPosition;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_refine_service, parent, false);
        return new ServiceCategoryFilterAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ExploreCategoryInfo.DataBean dataBean = searchList.get(position);

        holder.tvSubSerName.setText(dataBean.title);

        if(dataBean.isCheckedCategoryLocal){
            holder.checkbox2.setChecked(true);
        }else  holder.checkbox2.setChecked(false);

        holder.rlCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(dataBean.isCheckedCategoryLocal){
                    dataBean.isCheckedCategoryLocal = false;

                }else {
                    dataBean.isCheckedCategoryLocal =true;
                    lastCatPosition = position;
                }
                notifyDataSetChanged();
            }
        });
    }

    public int getLastPos(){
        return lastCatPosition;
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvSubSerName;
        private CheckBox checkbox2;
        private LinearLayout rlCheckBox;

        ViewHolder(View itemView) {
            super(itemView);
            tvSubSerName = itemView.findViewById(R.id.tvSubSerName);
            checkbox2 = itemView.findViewById(R.id.checkbox2);
            rlCheckBox = itemView.findViewById(R.id.rlCheckBox);
        }

    }
}

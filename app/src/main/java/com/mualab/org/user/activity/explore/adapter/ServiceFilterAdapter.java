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

import java.util.ArrayList;

public class ServiceFilterAdapter extends RecyclerView.Adapter<ServiceFilterAdapter.ViewHolder> {
    ArrayList<ExploreCategoryInfo.DataBean.ArtistservicesBean> artistservicesBeans;
    Context mContext;

    public ServiceFilterAdapter(ArrayList<ExploreCategoryInfo.DataBean.ArtistservicesBean> artistservicesBeans, Context mContext) {
        this.artistservicesBeans = artistservicesBeans;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_refine_service, parent, false);
        return new ServiceFilterAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ExploreCategoryInfo.DataBean.ArtistservicesBean dataBean = artistservicesBeans.get(position);

        holder.tvSubSerName.setText(dataBean.title);

        if(dataBean.isCheckedservicesLocal){
            holder.checkbox2.setChecked(true);
        }else  holder.checkbox2.setChecked(false);

        holder.rlCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(dataBean.isCheckedservicesLocal){
                    dataBean.isCheckedservicesLocal = false;
                }else {
                    dataBean.isCheckedservicesLocal =true;
                }
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return artistservicesBeans.size();
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

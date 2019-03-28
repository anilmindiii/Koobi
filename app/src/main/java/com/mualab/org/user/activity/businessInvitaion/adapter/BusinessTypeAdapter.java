package com.mualab.org.user.activity.businessInvitaion.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.mualab.org.user.R;

import java.util.List;

public  class BusinessTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<String> businessTypeList;
    //private OnBottomReachedListener onBottomReachedListener;


    public BusinessTypeAdapter(Context context, List<String> businessTypeList) {
        this.context = context;
        this.businessTypeList = businessTypeList;
    }

    @Override
    public int getItemCount() {
        return businessTypeList.size();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_businesstype_list, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

        final ViewHolder holder = ((ViewHolder) viewHolder);
        
        holder.tvBusinessType.setText(businessTypeList.get(position));

    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvBusinessType;

        private ViewHolder(View itemView)
        {
            super(itemView);

            tvBusinessType = itemView.findViewById(R.id.tvBusinessType);
        }

    }

}

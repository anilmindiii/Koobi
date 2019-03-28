package com.mualab.org.user.activity.feeds.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.model.ReportInfo;

import java.util.ArrayList;

/**
 * Created by mindiii on 19/12/18.
 */

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
    ArrayList<ReportInfo.DataBean> dataBeans;
    Context mContext;
    getClick click;

    public ReportAdapter(ArrayList<ReportInfo.DataBean> dataBeans, Context mContext,getClick click) {
        this.dataBeans = dataBeans;
        this.mContext = mContext;
        this.click = click;
    }

    public  interface getClick{
        void OnClikcItem(ReportInfo.DataBean bean);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportInfo.DataBean bean = dataBeans.get(position);
        holder.tv_report.setText(bean.title+"");
    }

    @Override
    public int getItemCount() {
        return dataBeans.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_report;

        public ViewHolder(View itemView) {
            super(itemView);

            tv_report = itemView.findViewById(R.id.tv_report);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            click.OnClikcItem(dataBeans.get(getLayoutPosition()));
        }
    }
}

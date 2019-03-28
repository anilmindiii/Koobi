package com.mualab.org.user.activity.booking.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.VoucherCodesActivity;
import com.mualab.org.user.activity.booking.model.VoucherInfo;

import java.util.List;

/**
 * Created by mindiii on 23/1/19.
 */

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {

    List<VoucherInfo.DataBean> beanList;
    Context mContext;
    getVoucher voucherListner;

    public VoucherAdapter(Context mContext, List<VoucherInfo.DataBean> beanList,getVoucher voucherListner) {
        this.mContext = mContext;
        this.beanList = beanList;
        this.voucherListner = voucherListner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.voucher_code_item,parent,false);
        return new ViewHolder(view);
    }

    public interface getVoucher{
        void Voucher(VoucherInfo.DataBean voucherItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VoucherInfo.DataBean bean = beanList.get(position);
        holder.tv_code.setText(bean.voucherCode);
        holder.tv_date.setText(bean.endDate);

        if(bean.discountType.trim().equals("1")){ // case of fix amount
            holder.tv_amount.setText("£"+bean.amount);
        }else if(bean.discountType.trim().equals("2")){// case of % amount
            holder.tv_amount.setText(bean.amount+"%");
        }

    }

    @Override
    public int getItemCount() {
        return beanList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tv_code,tv_amount,tv_date;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            tv_code = itemView.findViewById(R.id.tv_code);
            tv_amount = itemView.findViewById(R.id.tv_amount);
            tv_date = itemView.findViewById(R.id.tv_date);
        }

        @Override
        public void onClick(View v) {
            voucherListner.Voucher(beanList.get(getAdapterPosition()));
        }
    }
}

package com.mualab.org.user.activity.payment.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.payment.PaymentCheckOutActivity;
import com.mualab.org.user.activity.payment.model.StripeSaveCardResponce;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
    List<StripeSaveCardResponce.DataBean> dataList;
    Context mContext;
    getValue getValueLisner;
    String from;

    public CardAdapter(String from,Context allCardActivity, List<StripeSaveCardResponce.DataBean> data, getValue getValueLisner) {
        this.mContext = allCardActivity;
        this.dataList = data;
        this.getValueLisner = getValueLisner;
        this.from = from;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_card,viewGroup,false);
        return new ViewHolder(view);
    }

    public interface getValue{
        void getCardData(StripeSaveCardResponce.DataBean dataBean);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        StripeSaveCardResponce.DataBean bean = dataList.get(i);
        viewHolder.tv_card_number.setText(mContext.getString(R.string.xxx)+bean.getLast4());

        if(bean.isSelected){
            viewHolder.iv_card_selceted.setVisibility(View.VISIBLE);
        }else  viewHolder.iv_card_selceted.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_card_number;
        ImageView iv_card_selceted;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_card_number = itemView.findViewById(R.id.tv_card_number);
            iv_card_selceted = itemView.findViewById(R.id.iv_card_selceted);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(!from.equals("profile")){
                for(int i= 0;i< dataList.size();i++){
                    dataList.get(i).isSelected = false;
                }
                dataList.get(getAdapterPosition()).isSelected = true;
                getValueLisner.getCardData(dataList.get(getAdapterPosition()));
                notifyDataSetChanged();
            }else {
                Intent intent = new Intent(mContext,PaymentCheckOutActivity.class);
                intent.putExtra("card",dataList.get(getAdapterPosition()));
                intent.putExtra("from","details");
                mContext.startActivity(intent);
            }

        }
    }
}

package com.mualab.org.user.activity.payment.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.payment.model.StripeSaveCardResponce;
import com.mualab.org.user.data.local.prefs.Session;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
    List<StripeSaveCardResponce.DataBean> dataList;
    Context mContext;
    getValue getValueLisner;
    String from;
    Session session;

    public CardAdapter(String from,Context allCardActivity, List<StripeSaveCardResponce.DataBean> data, getValue getValueLisner) {
        this.mContext = allCardActivity;
        this.dataList = data;
        this.getValueLisner = getValueLisner;
        this.from = from;
        session = new Session(mContext);
        if(!TextUtils.isEmpty(session.getUser().cardId)){
            String cardId = session.getUser().cardId;
            for(int i=0;i<dataList.size();i++){
                if (cardId.equals(dataList.get(i).getId())) {
                    dataList.get(i).isSelected = true;
                }
            }

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_card,viewGroup,false);
        return new ViewHolder(view);
    }

    public interface getValue{
        void getCardData(StripeSaveCardResponce.DataBean dataBean);
        void getCardDataForDetele(StripeSaveCardResponce.DataBean dataBean, List<StripeSaveCardResponce.DataBean> dataList, int pos);
        void makeDefaultcard(StripeSaveCardResponce.DataBean dataBean);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int pos) {
        StripeSaveCardResponce.DataBean bean = dataList.get(pos);
        viewHolder.tv_card_number.setText(mContext.getString(R.string.xxx)+bean.getLast4());

        if(bean.isSelected){
            viewHolder.iv_card_selceted.setVisibility(View.VISIBLE);
        }else viewHolder.iv_card_selceted.setVisibility(View.GONE);

        viewHolder.sliding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getValueLisner.getCardDataForDetele(dataList.get(pos),dataList,pos);
            }
        });
        if(dataList.size()==1){
            viewHolder.swipeLayout.setRightSwipeEnabled(false);
        }else  viewHolder.swipeLayout.setRightSwipeEnabled(true);

        viewHolder.cv_main_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!from.equals("profile")){
                    for(int i= 0;i< dataList.size();i++){
                        dataList.get(i).isSelected = false;
                    }
                    dataList.get(pos).isSelected = true;
                    getValueLisner.getCardData(dataList.get(pos));
                    notifyDataSetChanged();
                }else {
                    for(int i= 0;i< dataList.size();i++){
                        dataList.get(i).isSelected = false;
                    }
                    dataList.get(pos).isSelected = true;
                    getValueLisner.makeDefaultcard(dataList.get(pos));
                    notifyDataSetChanged();

                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_card_number;
        ImageView iv_card_selceted;
        LinearLayout sliding;
        RelativeLayout cv_main_view;
        SwipeLayout swipeLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_card_number = itemView.findViewById(R.id.tv_card_number);
            iv_card_selceted = itemView.findViewById(R.id.iv_card_selceted);
            sliding = itemView.findViewById(R.id.sliding);
            cv_main_view = itemView.findViewById(R.id.cv_main_view);
            swipeLayout = itemView.findViewById(R.id.sample1);


        }
    }


}

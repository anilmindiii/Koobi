package com.mualab.org.user.activity.businessInvitaion.adapter;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.mualab.org.user.R;
import com.mualab.org.user.activity.businessInvitaion.model.CompanyDetail;
import com.squareup.picasso.Picasso;

import java.util.List;


public class InvitationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<CompanyDetail> invitationList;
    private onDetailClickListner detailClickListner;


    public InvitationAdapter(Context context, List<CompanyDetail> invitationList,onDetailClickListner detailClickListner) {
        this.context = context;
        this.invitationList = invitationList;
        this.detailClickListner = detailClickListner;
    }

    public interface onDetailClickListner{

        void onClick(int position, CompanyDetail companyDetail);

    }


    @Override
    public int getItemCount() {
        return invitationList.size();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invitation_list_layout, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

        final ViewHolder holder = ((ViewHolder) viewHolder);
        final CompanyDetail item = invitationList.get(position);

        holder.tvBusinessName.setText(item.businessName);

        if (!item.profileImage.equals("")){
            Picasso.with(context).load(item.profileImage).placeholder(R.drawable.default_placeholder).
                    fit().into(holder.ivBusinessProfile);
        }else {
            holder.ivBusinessProfile.setImageDrawable(context.getResources().getDrawable(R.drawable.default_placeholder));
        }

        holder.rvBizType.setAdapter(new BusinessTypeAdapter(context,item.businessType));

    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView tvBusinessName;
        RecyclerView rvBizType;
        ImageView ivBusinessProfile;
        RelativeLayout rlItem;
        private ViewHolder(View itemView)
        {
            super(itemView);

            rvBizType = itemView.findViewById(R.id.rvBizType);
            tvBusinessName = itemView.findViewById(R.id.tvBusinessName);
            ivBusinessProfile = itemView.findViewById(R.id.ivBusinessProfile);
            rlItem = itemView.findViewById(R.id.rlItem);

            LinearLayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);
            rvBizType.setLayoutManager(layoutManager);

            rlItem.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.rlItem:
                    CompanyDetail companyDetail = invitationList.get(getAdapterPosition());
                    if (detailClickListner!=null)
                        detailClickListner.onClick(getAdapterPosition(),companyDetail);
                    //  apiForGetStaffDetail(staff);
                  /*  if (companyDetail.status.equals("0")) {
                        Intent intent = new Intent(context,InvitationDetailActivity.class);
                        intent.putExtra("companyDetail",companyDetail);
                        context.startActivity(intent);
                    }*/
                    break;
            }

        }
    }

    class BusinessTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Context context;
        private List<String> businessTypeList;
        //private OnBottomReachedListener onBottomReachedListener;


        BusinessTypeAdapter(Context context, List<String> businessTypeList) {
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

        class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView tvBusinessType;

            private ViewHolder(View itemView)
            {
                super(itemView);

                tvBusinessType = itemView.findViewById(R.id.tvBusinessType);
            }

        }

    }




}
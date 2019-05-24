package com.mualab.org.user.activity.share_module.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.dialogs.ItemClickListener;
import com.mualab.org.user.activity.share_module.model.Contact;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by hemant
 * Date: 17/4/18
 * Time: 4:03 PM
 */

public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Contact> list;
    private ItemClickListener itemClickListener;

    public ContactAdapter(List<Contact> list, ItemClickListener itemClickListener) {
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder rvHolder, int position) {
        ViewHolder holder = ((ViewHolder) rvHolder);

        Contact mainBean = list.get(position);

        if (mainBean.photo != null && !mainBean.photo.isEmpty())
            Picasso.with(holder.ivProfile.getContext()).load(mainBean.photo).placeholder(R.drawable.default_placeholder).fit().into(holder.ivProfile);
        else
            Picasso.with(holder.ivProfile.getContext()).load(R.drawable.default_placeholder).fit().into(holder.ivProfile);

        holder.tvName.setText(mainBean.name);
        holder.tvNum.setText(mainBean.phone);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView ivProfile;
        private TextView tvName, tvNum;

        ViewHolder(@NonNull View view) {
            super(view);

            ivProfile = view.findViewById(R.id.ivProfile);
            tvName = view.findViewById(R.id.tvName);
            tvNum = view.findViewById(R.id.tvNum);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null && getAdapterPosition() != -1)
                itemClickListener.onItemClick(getAdapterPosition());
        }
    }
}


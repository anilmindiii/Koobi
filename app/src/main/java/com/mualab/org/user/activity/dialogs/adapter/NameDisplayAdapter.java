package com.mualab.org.user.activity.dialogs.adapter;


import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.dialogs.ItemClickListener;

import java.util.List;


public class NameDisplayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> bookingTypeListA;
    private ItemClickListener itemClickListener;
    private long mLastClickTime = 0;

    public NameDisplayAdapter(List<String> bookingTypeList, ItemClickListener itemClickListener) {
        this.bookingTypeListA = bookingTypeList;
        this.itemClickListener = itemClickListener;
    }


    @Override
    public int getItemCount() {
        return bookingTypeListA.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_businesstype, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {

        final ViewHolder holder = ((ViewHolder) viewHolder);

        holder.tvName.setText(bookingTypeListA.get(position));
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvName;

        private ViewHolder(View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 600) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            switch (view.getId()) {
                default:
                    if (itemClickListener != null && getAdapterPosition() != -1) {
                        itemClickListener.onItemClick(getAdapterPosition());
                    }
                    break;
            }
        }
    }

}
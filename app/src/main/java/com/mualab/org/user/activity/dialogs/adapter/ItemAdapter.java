package com.mualab.org.user.activity.dialogs.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.dialogs.BottomSheetPopup;
import com.mualab.org.user.activity.dialogs.ItemClickListener;
import com.mualab.org.user.activity.dialogs.model.Item;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder>{

    private List<Item> bookingTypeListA;
    private ItemClickListener itemClickListener;

    public ItemAdapter(List<Item> bookingTypeList, ItemClickListener itemClickListener) {
        this.bookingTypeListA = bookingTypeList;
        this.itemClickListener = itemClickListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_bottom_sheet, viewGroup, false);
        return new ItemAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        Item item =  bookingTypeListA.get(i);

        viewHolder.image.setImageResource(item.icon);
        viewHolder.tv_Name .setText(item.name);

        if((bookingTypeListA.size() - 1) == i){
            viewHolder.view_div.setVisibility(View.GONE);
        }else  viewHolder.view_div.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return bookingTypeListA.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_Name;
        ImageView image;
        View view_div;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            tv_Name = itemView.findViewById(R.id.tv_Name);
            view_div = itemView.findViewById(R.id.view_div);

            itemView.setOnClickListener(this::onClick);
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onItemClick(getAdapterPosition());
        }
    }
}

package com.mualab.org.user.activity.feeds.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by mindiii on 21/12/18.
 */

public class TagTextHorizontalAdapter extends RecyclerView.Adapter<TagTextHorizontalAdapter.ViewHolder> {

    private List<ExSearchTag> tempTxtTagHoriList;
    private Context mContext;
    onDataChangeListner listner;

    public TagTextHorizontalAdapter(List<ExSearchTag> tempTxtTagHoriList, Context mContext,onDataChangeListner listner) {
        this.tempTxtTagHoriList = tempTxtTagHoriList;
        this.mContext = mContext;
        this.listner = listner;
    }

    public interface onDataChangeListner{
        void dataChange(ExSearchTag searchTag);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text_horizontal_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final ExSearchTag searchTag = tempTxtTagHoriList.get(position);

        /*if (searchTag.imageUrl != null && !searchTag.imageUrl.equals("")) {
            Picasso.with(mContext).load(searchTag.imageUrl).
                    placeholder(R.drawable.default_placeholder).into(holder.iv_user_image);
        } else {
            holder.iv_user_image.setImageResource(R.drawable.default_placeholder);
        }*/

        Glide.with(mContext).load(searchTag.imageUrl).placeholder(R.drawable.default_placeholder).into(holder.iv_user_image);


        holder.tv_user_name.setText(searchTag.title);

        holder.iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listner.dataChange(searchTag);
                tempTxtTagHoriList.remove(position);
                notifyDataSetChanged();

            }
        });
    }

    @Override
    public int getItemCount() {
        return tempTxtTagHoriList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_user_image, iv_cancel;
        TextView tv_user_name;

        public ViewHolder(View itemView) {
            super(itemView);

            iv_user_image = itemView.findViewById(R.id.iv_user_image);
            iv_cancel = itemView.findViewById(R.id.iv_cancel);
            tv_user_name = itemView.findViewById(R.id.tv_user_name);

        }
    }
}

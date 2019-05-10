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
import com.mualab.org.user.activity.feeds.activity.TextTagActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mindiii on 21/12/18.
 */

public class TextTagAdapter extends RecyclerView.Adapter<TextTagAdapter.ViewHolder> {
    List<ExSearchTag> txtTagList;
    List<ExSearchTag> tempTxtTagHoriList;
    Context mContext;
   // String ids = "";
    getValue valueListner;
    private Map<String,ExSearchTag> idsMap;

    public TextTagAdapter(Map<String,ExSearchTag> idsMap,Context textTagActivity, List<ExSearchTag> list,
                          getValue valueListner, List<ExSearchTag> tempTxtTagHoriList) {
        this.mContext = textTagActivity;
        this.txtTagList = list;
        this.valueListner = valueListner;
        this.tempTxtTagHoriList =  tempTxtTagHoriList;
        this.idsMap =  idsMap;
        /*this.ids = allIds;
        if(allIds == null){
            ids = "";
        }*/
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExSearchTag searchTag = txtTagList.get(position);

        if (searchTag.imageUrl != null && !searchTag.imageUrl.equals("")) {
            Picasso.with(mContext).load(searchTag.imageUrl). fit().
                    placeholder(R.drawable.default_placeholder).into(holder.iv_user_image);

        } else {
            holder.iv_user_image.setImageResource(R.drawable.default_placeholder);
        }


        holder.tv_user_name.setText(searchTag.title);

        if (txtTagList.get(position).isCheck) {
            holder.iv_checkbox.setImageResource(R.drawable.active_check_ico);
        } else {
            holder.iv_checkbox.setImageResource(R.drawable.checkbox_selector);
        }

    }

    @Override
    public int getItemCount() {
        return txtTagList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView iv_user_image, iv_checkbox;
        TextView tv_user_name;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            iv_user_image = itemView.findViewById(R.id.iv_user_image);
            iv_checkbox = itemView.findViewById(R.id.iv_checkbox);
            tv_user_name = itemView.findViewById(R.id.tv_user_name);
        }

        @Override
        public void onClick(View v) {
            try {

                int pos = getAdapterPosition();
                String id = String.valueOf(txtTagList.get(pos).id);

                if (!txtTagList.get(pos).isCheck) {
                    idsMap.put(id,txtTagList.get(pos));
                    txtTagList.get(pos).isCheck = true;
                    tempTxtTagHoriList.add(txtTagList.get(pos));

                } else {
                    idsMap.remove(id);
                    txtTagList.get(pos).isCheck = false;
                    for(int i=0;i<tempTxtTagHoriList.size();i++){
                        if(txtTagList.get(pos).id == tempTxtTagHoriList.get(i).id){
                            tempTxtTagHoriList.remove(i);
                        }
                    }
                }

                valueListner.getTextTagData(tempTxtTagHoriList);
                notifyDataSetChanged();

            }catch (IndexOutOfBoundsException e){

            }


        }
    }

    public interface getValue {
        void getTextTagData( List<ExSearchTag> tempTxtTagHoriList );
    }
}

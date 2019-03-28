package com.mualab.org.user.activity.feeds.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.mualab.org.user.activity.feeds.activity.TextTagActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mindiii on 21/12/18.
 */

public class TextTagAdapter extends RecyclerView.Adapter<TextTagAdapter.ViewHolder> {
    List<ExSearchTag> txtTagList;
    List<ExSearchTag> tempTxtTagHoriList;
    Context mContext;
    String ids = "";
    getValue valueListner;

    public TextTagAdapter(Context textTagActivity, List<ExSearchTag> list,
                          getValue valueListner, List<ExSearchTag> tempTxtTagHoriList, String allIds) {
        this.mContext = textTagActivity;
        this.txtTagList = list;
        this.valueListner = valueListner;
        this.tempTxtTagHoriList =  tempTxtTagHoriList;
        this.ids = allIds;
        if(allIds == null){
            ids = "";
        }
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
            Picasso.with(mContext).load(searchTag.imageUrl).
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
            int pos = getAdapterPosition();
            String id = String.valueOf(txtTagList.get(pos).id);

            if (!txtTagList.get(pos).isCheck) {

                txtTagList.get(pos).isCheck = true;

                if(ids.contains(id)){
                    ids = ids.replace(id,"");
                }
                else if(ids.contains(id+",")){
                    ids = ids.replace(id+",","");
                }
                else if(ids.contains(","+id)){
                    ids = ids.replace(","+id,"");
                }
                else {
                    ids = String.valueOf(txtTagList.get(pos).id) + "," + ids;
                }

                tempTxtTagHoriList.add(txtTagList.get(pos));

            } else {

                if (ids.contains(id)) {
                    ids = ids.replace(id, "");
                }else
                if (ids.contains(id + ",")) {
                    ids = ids.replace((id + ","), "");
                } else if (ids.contains("," + id)) {
                    ids = ids.replace(("," + id), "");
                }

                txtTagList.get(pos).isCheck = false;

                for(int i=0;i<tempTxtTagHoriList.size();i++){
                    if(txtTagList.get(pos).id == tempTxtTagHoriList.get(i).id){
                        tempTxtTagHoriList.remove(i);
                    }
                }
            }

            if (ids.endsWith(",")) {
                ids = ids.substring(0, ids.length() - 1);
            }

            valueListner.getTextTagData(ids,tempTxtTagHoriList);

            notifyDataSetChanged();
        }
    }

    public interface getValue {
        void getTextTagData(String ids, List<ExSearchTag> tempTxtTagHoriList );
    }
}

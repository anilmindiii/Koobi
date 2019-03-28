package com.mualab.org.user.activity.searchBoard.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.data.model.SearchBoard.RefineServices;
import com.mualab.org.user.data.model.SearchBoard.RefineSubServices;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mindiii on 20/9/18.
 */

public class RefineSubServiceAdapter extends RecyclerView.Adapter<RefineSubServiceAdapter.ViewHolder> {
    private List<RefineSubServices> childtems;

    private Context context;

    public RefineSubServiceAdapter(ArrayList<RefineSubServices> childtems, Context context) {
        this.childtems = childtems;

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_refine_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final RefineSubServices refineSubService=childtems.get(position);

        holder.tvSubSerName.setText(childtems.get(position).title);

        if(refineSubService.isSubItemChecked){
            holder.checkbox2.setChecked(true);
        }else  holder.checkbox2.setChecked(false);


        holder.rlCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(refineSubService.isSubItemChecked){
                    holder.checkbox2.setChecked(false);
                    refineSubService.isSubItemChecked = false;
                }else {
                    holder.checkbox2.setChecked(true);
                    refineSubService.isSubItemChecked =true;
                }

                notifyDataSetChanged();

/*
                if (b){
                    holder.checkbox2.setChecked(true);
                    refineServices.isChecked = "1";
                    refineServices.isSubItemChecked = true;
                    selectedStrings.add(refineServices);
                }else {
                    holder.checkbox2.setChecked(false);
                    refineServices.isChecked = "0";
                    refineServices.isSubItemChecked = false;
                }*/


            }
        });
  /*      holder.checkbox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    holder.checkbox2.setChecked(true);
                    refineSubService.isChecked = "1";
                    refineSubService.isSubItemChecked = true;
                } else {
                    holder.checkbox2.setChecked(false);
                    refineSubService.isChecked = "0";
                    refineSubService.isSubItemChecked = false;
                }
            }
        });*/
        /*if (refineSubService.isChecked.equals("1")) {
            holder.checkbox2.setChecked(true);

        } else {
            holder.checkbox2.setChecked(false);
        }*/


/*        holder.rlCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isChecked){
                    isChecked = true;
                    holder.checkbox2.setChecked(true);
                    subServices.isChecked = "1";
                    subServices.isSubItemChecked = true;
                }else {
                    isChecked = false;
                    holder.checkbox2.setChecked(false);
                    subServices.isChecked = "0";
                    subServices.isSubItemChecked = false;
                }
            }
        });*/

    }

    @Override
    public int getItemCount() {
        return childtems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSubSerName;
        private CheckBox checkbox2;
        private LinearLayout rlCheckBox;

        ViewHolder(View itemView) {
            super(itemView);
            tvSubSerName = itemView.findViewById(R.id.tvSubSerName);
            checkbox2 = itemView.findViewById(R.id.checkbox2);
            rlCheckBox = itemView.findViewById(R.id.rlCheckBox);
        }
    }


}

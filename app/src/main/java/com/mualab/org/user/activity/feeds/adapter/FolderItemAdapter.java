package com.mualab.org.user.activity.feeds.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.daimajia.swipe.SwipeLayout;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.activity.SaveToFolderActivity;
import com.mualab.org.user.activity.feeds.model.FolderInfo;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.Progress;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FolderItemAdapter extends RecyclerView.Adapter<FolderItemAdapter.ViewHolder> {
    Context mContext;
    ArrayList<FolderInfo.DataBean> folderList;
    clickListner clickListner;
    boolean fromSlider;

    public FolderItemAdapter(boolean fromSlider,Context mContext, ArrayList<FolderInfo.DataBean> folderList, clickListner clickListner) {
        this.mContext = mContext;
        this.folderList = folderList;
        this.clickListner = clickListner;
        this.fromSlider = fromSlider;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_save_folder, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        FolderInfo.DataBean bean = folderList.get(i);
        viewHolder.ed_folder_name.setText(bean.folderName);

        if (bean.isSelected)
            viewHolder.iv_folder_selceted.setVisibility(View.VISIBLE);
        else viewHolder.iv_folder_selceted.setVisibility(View.GONE);

     /*   if(fromSlider){
            viewHolder.swipeLayout.setRightSwipeEnabled(false);
        }else viewHolder.swipeLayout.setRightSwipeEnabled(true);*/

        viewHolder.ivRemove.setOnClickListener(v -> {
            removeFolder(String.valueOf(folderList.get(i)._id),i);
        });

        viewHolder.ivEdit.setOnClickListener(v -> {
            clickListner.getClick(folderList.get(i),"editFolderName",i);
        });

        viewHolder.ly_main.setOnClickListener(view->{
            boolean isCurrentPosChecked = folderList.get(i).isSelected;

            if(!isCurrentPosChecked){
                clickListner.getClick(folderList.get(i),"OnClickCell",i);
            }else {
                clickListner.getClick(folderList.get(i),"OnClickCell",i);

            }
        });
    }

    public interface clickListner {
        void getClick(FolderInfo.DataBean dataBean,String Tag,int pos);
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView ed_folder_name;
        ImageView iv_folder_selceted, ivEdit, ivRemove;
        SwipeLayout swipeLayout;
        LinearLayout ly_main;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ed_folder_name = itemView.findViewById(R.id.ed_folder_name);
            iv_folder_selceted = itemView.findViewById(R.id.iv_folder_selceted);
            swipeLayout = itemView.findViewById(R.id.sample1);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivRemove = itemView.findViewById(R.id.ivRemove);
            ly_main = itemView.findViewById(R.id.ly_main);

            itemView.setOnClickListener(this::onClick);
        }

        @Override
        public void onClick(View v) {

        }
    }

    private void removeFolder( final String folderId, final int pos) {
        Progress.show(mContext);
        Map<String, String> map = new HashMap<>();
        map.put("folderId", folderId);

        new HttpTask(new HttpTask.Builder(mContext, "deleteFolder", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(mContext);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    //String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        folderList.remove(pos);
                        notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Progress.hide(mContext);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(mContext);
            }
        }).setAuthToken(Mualab.currentUser.authToken)
                .setParam(map)
                .setMethod(Request.Method.POST)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute("deleteFolder");



    }

}

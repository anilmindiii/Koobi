package com.mualab.org.user.activity.artist_profile.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.artist_profile.model.StaffDetailsInfo;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.utils.Helper;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by mindiii on 10/1/19.
 */

public class StaffListAdapter extends RecyclerView.Adapter<StaffListAdapter.ViewHOlder> {

    Context mContext;
    List<StaffDetailsInfo.StaffInfoBean> detailsInfoList;
    String typeCall;

    public StaffListAdapter(Context mContext, List<StaffDetailsInfo.StaffInfoBean> detailsInfoList,
                            String typeCall) {
        this.mContext = mContext;
        this.detailsInfoList = detailsInfoList;
        this.typeCall = typeCall;
    }

    @NonNull
    @Override
    public ViewHOlder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff_list,parent,false);
        return new ViewHOlder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHOlder holder, int position) {
        final StaffDetailsInfo.StaffInfoBean bean = detailsInfoList.get(position);
        holder.tvArtistName.setText(bean.staffName);

        String hour = Helper.formateDateFromstring("HH:mm", "HH", bean.completionTime);
        String min = Helper.formateDateFromstring("HH:mm", "mm", bean.completionTime);

        if(hour.equals("00")){
            holder.tvtime.setText(min+" min");
        }else holder.tvtime.setText(hour +" hr "+min+" min");

        if(typeCall.equals("In Call")){
            holder.tvprice.setText("£" + bean.inCallPrice+"");
        }else holder.tvprice.setText("£" + bean.outCallPrice+"");



        if (!bean.staffImage.isEmpty() && !bean.staffImage.equals("")) {
            Picasso.with(mContext).load(bean.staffImage).placeholder(R.drawable.default_placeholder).
                    fit().into(holder.ivProfile);
        }else {
            holder.ivProfile.setImageResource(R.drawable.default_placeholder);
        }

        holder.ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiForgetUserIdFromUserName(bean.staffName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return detailsInfoList.size();
    }

    class ViewHOlder extends RecyclerView.ViewHolder {
        TextView tvArtistName,tvtime,tvprice;
        ImageView ivProfile;

        public ViewHOlder(View itemView) {
            super(itemView);

            tvArtistName = itemView.findViewById(R.id.tvArtistName);
            tvtime = itemView.findViewById(R.id.tvtime);
            tvprice = itemView.findViewById(R.id.tvprice);
            ivProfile = itemView.findViewById(R.id.ivProfile);
        }
    }

    private void apiForgetUserIdFromUserName(final CharSequence userName) {
        String user_name="";

        final Map<String, String> params = new HashMap<>();
        if(userName.toString().startsWith("@")){
            user_name = userName.toString().replace("@","");
            params.put("userName", user_name+"");
        }else params.put("userName", userName+"");
        new HttpTask(new HttpTask.Builder(mContext, "profileByUserName", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Log.d("hfjas", response);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {

                        JSONObject userDetail = js.getJSONObject("userDetail");
                        String userType = userDetail.getString("userType");
                        int userId = userDetail.getInt("_id");

                        if (userType.equals("user")) {
                            Intent intent = new Intent(mContext, UserProfileActivity.class);
                            intent.putExtra("userId", String.valueOf(userId));
                            mContext.startActivity(intent);
                        }else if (userType.equals("artist") && userId== Mualab.currentUser.id){
                            Intent intent = new Intent(mContext, UserProfileActivity.class);
                            intent.putExtra("userId", String.valueOf(userId));
                            mContext.startActivity(intent);
                        }
                        else {
                            Intent intent = new Intent(mContext, ArtistProfileActivity.class);
                            intent.putExtra("artistId", String.valueOf(userId));
                            mContext.startActivity(intent);
                        }



                    } else {
                        MyToast.getInstance(mContext).showDasuAlert(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
            }
        }).setBody(params,HttpTask.ContentType.APPLICATION_JSON)
                .setMethod(Request.Method.POST)
                .setProgress(true))
                .execute("FeedAdapter");


    }

}

package com.mualab.org.user.activity.booking.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.BookingDetailsActivity;
import com.mualab.org.user.activity.booking.model.BookingHistoryInfo;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.model.ArtistServices;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mindiii on 29/1/19.
 */

public class ScheduledAdapter extends RecyclerView.Adapter<ScheduledAdapter.ViewHolder> {
    Context mContext;
    ArrayList<BookingHistoryInfo.DataBean> dataBean;
    String call_status;

    public ScheduledAdapter(Context mContext, ArrayList<BookingHistoryInfo.DataBean> dataBean, String call_status) {
        this.mContext = mContext;
        this.dataBean = dataBean;
        this.call_status = call_status;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_scheduled,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final BookingHistoryInfo.DataBean bean = dataBean.get(position);

        if(bean.bookingInfo.size() > 1){
            holder.newText.setVisibility(View.VISIBLE);
            holder.tvServicesnew.setText(bean.bookingInfo.get(1).subServiceName+"");
        }else holder.newText.setVisibility(View.GONE);

        if(bean.bookingInfo.size() != 0){
            holder.tvServices.setText(bean.bookingInfo.get(0).subServiceName+"");
        }



       /* holder.rlServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagToBookService(bean.bookingInfo.get(0).artistServiceId, String.valueOf(bean.artistDetail.get(0)._id),
                        bean.bookingInfo.get(0).serviceId,bean.bookingInfo.get(0).subServiceId);
            }
        });

        holder.newText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagToBookService(bean.bookingInfo.get(1).artistServiceId, String.valueOf(bean.artistDetail.get(0)._id),
                        bean.bookingInfo.get(1).serviceId,bean.bookingInfo.get(1).subServiceId);
            }
        });*/

        holder.tvArtistName.setText( bean.artistDetail.get(0).userName+"");
        holder.tv_view_more.setVisibility(View.GONE);

      /*  if(bean.bookingInfo.size() == 1){
            holder.tvArtistName.setText(bean.bookingInfo.get(0).staffName+"");
            holder.tv_view_more.setVisibility(View.GONE);
        }else if(bean.bookingInfo.size() == 2){
            holder.tvArtistName.setText(bean.bookingInfo.get(0).staffName
                    +", "+bean.bookingInfo.get(1).staffName);
            holder.tv_view_more.setVisibility(View.GONE);
        }else if(bean.bookingInfo.size() == 3){
            holder.tvArtistName.setText(bean.bookingInfo.get(0).staffName
                    +", "+bean.bookingInfo.get(1).staffName);
            holder.tv_view_more.setVisibility(View.VISIBLE);
        }*/

        holder.tv_price.setText("£"+bean.totalPrice+"");

        holder.tvDateTime.setText(bean.bookingDate+", "+bean.bookingTime);

        if (bean.artistDetail.get(0).profileImage!= null && !bean.artistDetail.get(0).profileImage.equals("")){
            Picasso.with(mContext).load(bean.artistDetail.get(0).profileImage).placeholder(R.drawable.default_placeholder).fit().into(holder.ivProfile);
        }else {
            holder.ivProfile.setImageDrawable(mContext.getResources().getDrawable(R.drawable.default_placeholder));
        }

         if(call_status.equals("")){
            holder.tv_call_status.setVisibility(View.VISIBLE);
             if(bean.bookingType == 1){
                 holder.tv_call_status.setText("In Call");
             }else  holder.tv_call_status.setText("Out Call");
        }else {
             holder.tv_call_status.setVisibility(View.GONE);
         }



        if(bean.bookStatus.equals("0")){
            holder.tv_status.setText(R.string.pending);
            holder.tv_status.setTextColor(ContextCompat.getColor(mContext,R.color.main_orange_color));
        }else  if(bean.bookStatus.equals("1")){
            holder.tv_status.setText(R.string.confirm);
            holder.tv_status.setTextColor(ContextCompat.getColor(mContext,R.color.main_green_color));
        }else if(bean.bookStatus.equals("2")){
            //holder.tv_status.setText("Cancelled");
            holder.tv_status.setText("");
            holder.tv_status.setTextColor(ContextCompat.getColor(mContext,R.color.red));
        }
        else if(bean.bookStatus.equals("3")){
            holder.tv_status.setText("Completed");
            holder.tv_status.setTextColor(ContextCompat.getColor(mContext,R.color.main_green_color));
        }
        else if(bean.bookStatus.equals("5")){
            holder.tv_status.setText("In progress");
            holder.tv_status.setTextColor(ContextCompat.getColor(mContext,R.color.main_green_color));
        }

        holder.ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiForgetUserIdFromUserName(bean.artistDetail.get(0).userName);
            }
        });



    }

    @Override
    public int getItemCount() {
        return dataBean.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView ivProfile;
        TextView tvArtistName,tv_status,tv_price,tvServices,tvServicesnew,tvDateTime,tv_call_status,tv_view_more;
        RelativeLayout rlServices,newText;

        public ViewHolder(View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvArtistName = itemView.findViewById(R.id.tvArtistName);
            tv_status = itemView.findViewById(R.id.tv_status);
            tv_price = itemView.findViewById(R.id.tv_price);
            tvServices = itemView.findViewById(R.id.tvServices);
            newText = itemView.findViewById(R.id.newText);
            tvServicesnew = itemView.findViewById(R.id.tvServicesnew);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tv_call_status = itemView.findViewById(R.id.tv_call_status);
            tv_view_more = itemView.findViewById(R.id.tv_view_more);
            rlServices = itemView.findViewById(R.id.rlServices);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(mContext, BookingDetailsActivity.class);
            intent.putExtra("bookingId",dataBean.get(getAdapterPosition())._id);
            mContext.startActivity(intent);
        }
    }

    private void apiForgetUserIdFromUserName(String userName) {


        final Map<String, String> params = new HashMap<>();
        /*if(userName.toString().contains("@")){
            userName = userName.toString().replace("@","");
        }*/
        params.put("userName", userName+"");
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

    private void tagToBookService(int _id, String artistId,int serviceId,int subserviceId) {
        Intent intent = new Intent(mContext, BookingActivity.class);
        intent.putExtra("_id", _id);
        intent.putExtra("artistId", artistId);
        intent.putExtra("callType", "In Call");

        intent.putExtra("mainServiceName", "");
        intent.putExtra("subServiceName", "yes");

        intent.putExtra("serviceId", serviceId);
        intent.putExtra("subServiceId", subserviceId);

        intent.putExtra("isEditService", true);
        intent.putExtra("isFromSearchBoard", true);
        mContext.startActivity(intent);
    }
}

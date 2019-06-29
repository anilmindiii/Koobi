package com.mualab.org.user.activity.feeds.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.gmail.samehadar.iosdialog.CamomileSpinner;
import com.gmail.samehadar.iosdialog.utils.DialogUtils;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.feeds.model.FeedLike;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dharmraj Acharya on 16/8/17.
 **/

public class LikeListAdapter extends RecyclerView.Adapter<LikeListAdapter.ViewHolder> {


    private List<FeedLike> likedList;
    private Context mContext;
    private int myUserId;

    public LikeListAdapter(Context mContext, List<FeedLike> likedList, int myUserId) {
        this.likedList = likedList;
        this.mContext = mContext;
        this.myUserId = myUserId;
    }

    @Override
    public LikeListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.likes_item_layout, parent, false);
        return new LikeListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final FeedLike feedLike = likedList.get(position);
        if(TextUtils.isEmpty(feedLike.profileImage)){
            Picasso.with(mContext).load(R.drawable.default_placeholder).fit().into(holder.iv_profileImage);
        }else {
            Picasso.with(mContext).load(feedLike.profileImage).fit()
                    .placeholder(R.drawable.default_placeholder)
                    .error(R.drawable.default_placeholder)
                    .into(holder.iv_profileImage);
        }

        holder.tv_user_name.setText(feedLike.userName);
        holder.btn_follow.setVisibility(feedLike.id==myUserId?View.GONE:View.VISIBLE);

        if(feedLike.likeById== Mualab.currentUser.id){
            holder.btn_follow.setVisibility(View.GONE);
        }else {
            holder.btn_follow.setVisibility(View.VISIBLE);
            if (feedLike.followerStatus == 1) {
                holder.btn_follow.setBackgroundResource(R.drawable.btn_bg_blue_broder);
                holder.btn_follow.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                holder.btn_follow.setText(R.string.following);
            } else if (feedLike.followerStatus == 0) {
                holder.btn_follow.setBackgroundResource(R.drawable.button_effect_invert);
                holder.btn_follow.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                holder.btn_follow.setText(R.string.follow);
            }
        }

        holder.iv_profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiForgetUserIdFromUserName(feedLike.userName);
            }
        });


        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followUnfollow(feedLike, position, holder);
            }
        });

    }

    private void followUnfollow(final FeedLike feedLike, final int position, final ViewHolder holder ){

        if(feedLike.followerStatus==1){
            /*new UnfollowDialog(mContext, feedLike, new UnfollowDialog.UnfollowListner() {
                @Override
                public void onUnfollowClick(Dialog dialog) {
                    dialog.dismiss();
                    apiForFollowUnFollow(feedLike, position, holder);
                }
            });*/

            apiForFollowUnFollow(feedLike, position, holder);

        }else apiForFollowUnFollow(feedLike, position, holder);

    }

    @Override
    public int getItemCount() {
        return likedList.size();
    }

    private void apiForFollowUnFollow(final FeedLike feedLike, final int position, final ViewHolder h ) {
        h.spinner3.setVisibility(View.VISIBLE);
        h.spinner3.start();
        h.spinner3.recreateWithParams(
                mContext,
                DialogUtils.getColor(mContext, R.color.gray2),
                50,
                true
        );

        Map<String, String> map = new HashMap<>();
        map.put("userId", ""+ Mualab.currentUser.id);
        map.put("followerId", ""+feedLike.likeById);


        new HttpTask(new HttpTask.Builder(mContext, "followFollowing", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                h.spinner3.stop();
                h.spinner3.setVisibility(View.GONE);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        if (feedLike.followerStatus==0) {
                            feedLike.followerStatus = 1;
                        } else if (feedLike.followerStatus==1) {
                            feedLike.followerStatus = 0;
                        }
                    }
                    notifyItemChanged(position);
                } catch (JSONException e) {
                    e.printStackTrace();
                    notifyItemChanged(position);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                h.spinner3.stop();
                h.spinner3.setVisibility(View.GONE);
                notifyItemChanged(position);
            }
        }).setProgress(false)
                .setAuthToken(Mualab.currentUser.authToken)
                .setParam(map)).execute("");
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_profileImage;
        Button btn_follow;
        TextView tv_user_name;
        private CamomileSpinner spinner3;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_user_name =  itemView.findViewById(R.id.tv_user_name);
            iv_profileImage = itemView.findViewById(R.id.iv_profileImage);
            btn_follow =  itemView.findViewById(R.id.btn_follow);
            spinner3 =  itemView.findViewById(R.id.spinner3);

            btn_follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                }
            });
        }
    }

    private void apiForgetUserIdFromUserName(String userName) {
        final Map<String, String> params = new HashMap<>();
        if(userName.toString().contains("@")){
            userName = userName.toString().replace("@","");
        }
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
}

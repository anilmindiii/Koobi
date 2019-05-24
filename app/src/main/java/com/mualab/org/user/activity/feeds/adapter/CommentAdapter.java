package com.mualab.org.user.activity.feeds.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.dialogs.BottomSheetPopup;
import com.mualab.org.user.activity.dialogs.model.Item;
import com.mualab.org.user.activity.feeds.model.Comment;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
import com.mualab.org.user.activity.story.StoriesActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.squareup.picasso.Picasso;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mindiii on 16/8/17.
 **/

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<Comment> commentList;
    private Context mContext;
    private Listner listner;
    private Feeds feed;


    public interface Listner{
        void onItemChange();
        void getLongClick(int commentsPos);
    }

    public void setFeedId(Feeds feed){
        this.feed = feed;
    }

    public CommentAdapter(Context mContext, List<Comment> commentList, Listner listner) {
        this.commentList = commentList;
        this.mContext = mContext;
        this.listner = listner;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_item_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final Comment commentListInfo = commentList.get(position);

        holder.iv_like.setImageResource(commentListInfo.isLike==1?
                R.drawable.active_like_ico:R.drawable.inactive_like_ico);
        holder.iv_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiForCommentLike(commentListInfo, position, holder);
            }
        });

        if(!TextUtils.isEmpty(commentListInfo.profileImage)){
            Picasso.with(mContext).load(commentListInfo.profileImage)
                    .fit()
                    .placeholder(R.drawable.default_placeholder)
                    .error(R.drawable.default_placeholder)
                    .into(holder.iv_profileImage);
        }else Picasso.with(mContext).load(R.drawable.default_placeholder).into(holder.iv_profileImage);


        holder.iv_profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiForgetUserIdFromUserName(commentListInfo.userName);
            }
        });

        if (commentListInfo.isSelected){
            holder.tv_transparent.setVisibility(View.VISIBLE);
        }else  holder.tv_transparent.setVisibility(View.GONE);


        if(commentListInfo.type.equals("image")){
            holder.ivImg.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(commentListInfo.comment)
                    .placeholder(R.drawable.gallery_placeholder)
                    .error(R.drawable.gallery_placeholder)
                    .into(holder.ivImg);
            holder.tv_comments.setVisibility(View.GONE);
        }else {
            holder.tv_comments.setVisibility(View.VISIBLE);
            holder.ivImg.setVisibility(View.GONE);
            holder.tv_comments.setText(commentListInfo.comment);
        }

        holder.tv_user_name.setText(commentListInfo.userName);
        holder.tv_comments.setText(commentListInfo.comment);
        holder.tv_like_count.setText(String.format("%s Like", commentListInfo.commentLikeCount));
        holder.tv_comments_time.setText(commentListInfo.timeElapsed);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        ImageView iv_profileImage,ivImg, iv_like;
        TextView tv_user_name, tv_comments, tv_comments_time, tv_like_count,tv_transparent;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_user_name = itemView.findViewById(R.id.tv_user_name);
            tv_comments = itemView.findViewById(R.id.tv_comments);
            tv_comments_time = itemView.findViewById(R.id.tv_comments_time);
            tv_like_count = itemView.findViewById(R.id.tv_like_count);
            iv_profileImage = itemView.findViewById(R.id.iv_profileImage);
            ivImg = itemView.findViewById(R.id.ivImg);
            iv_like = itemView.findViewById(R.id.iv_like);
            tv_transparent = itemView.findViewById(R.id.tv_transparent);

            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            if ((commentList.get(getAdapterPosition()).commentById) == (Mualab.currentUser.id)) {
                if (getAdapterPosition() != -1 && listner != null) {
                    listner.getLongClick(getAdapterPosition());
                }
            }




            return false;
        }
    }



    private void apiForCommentLike(final Comment comment, final int position, final ViewHolder holder) {

        Map<String, String> map = new HashMap<>();
        map.putAll(Mualab.feedBasicInfo);
        map.put("commentId", ""+comment.id);
        map.put("feedId", ""+feed._id);
        map.put("likeById", ""+Mualab.currentUser.id);
        map.put("userId", ""+comment.commentById);
        map.put("type", "comment");

       /* map.put("age", "25");
        map.put("gender", "male");
        map.put("city", "indore");
        map.put("state", "MP");
        map.put("country", "India");*/


        for (Map.Entry<String,String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Log.d(key, value);
            // do stuff
        }


        holder.iv_like.setEnabled(false);

        new HttpTask(new HttpTask.Builder(mContext, "commentLike", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    holder.iv_like.setEnabled(true);
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    //String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        if (comment.isLike==0) {
                            comment.isLike = 1;
                            comment.commentLikeCount++;
                        } else {
                            comment.isLike = 0;
                            comment.commentLikeCount--;
                        }
                    }
                    notifyItemChanged(position);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                holder.iv_like.setEnabled(true);
            }
        }).setParam(map).setProgress(true)).execute("commentLike");
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

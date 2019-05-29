package com.mualab.org.user.activity.explore.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
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
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by dharmraj on 2/4/18.
 **/

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private boolean showLoader;
    private final int FEED_TYPE = 1;
    private final int VIEW_TYPE_LOADING = 2;

    private Context mContext;
    private List<ExSearchTag> feedItems;
    private Listener listener;

    public interface Listener{
        void onItemClick(ExSearchTag searchTag, int index);
    }

    public SearchAdapter(Context mContext, List<ExSearchTag> feedItems, Listener listener) {
        this.mContext = mContext;
        this.feedItems = feedItems;
        this.listener = listener;
    }

    public void showHideLoading(boolean bool){
        showLoader = bool;
    }

    public void clear(){
        final int size = feedItems.size();
        feedItems.clear();
        notifyItemRangeRemoved(0, size);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case FEED_TYPE:
                return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_explore_top, parent, false));
            case VIEW_TYPE_LOADING:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        ExSearchTag feed = feedItems.get(position);
        return feed==null?VIEW_TYPE_LOADING: FEED_TYPE;
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }


    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loaderViewHolder = (LoadingViewHolder) holder;
            if (showLoader) {
                loaderViewHolder.progressBar.setVisibility(View.VISIBLE);
            } else {
                loaderViewHolder.progressBar.setVisibility(View.GONE);
            }
            return;
        }



        final ExSearchTag searchTag = feedItems.get(position);
        final Holder h = ((Holder) holder);
        h.tvDesc.setVisibility(View.VISIBLE);
        h.tvHeader.setText(searchTag.title);
        h.tvDesc.setText(searchTag.desc);

        if(searchTag.type == 0 || searchTag.type == 1){
            h.btnFollow.setVisibility(View.VISIBLE);
        }else h.btnFollow.setVisibility(View.GONE);

        if (searchTag.id == Mualab.currentUser.id) {
            h.btnFollow.setVisibility(View.GONE);
        } else {
            if (searchTag.followerStatus == 1) {
                h.btnFollow.setBackgroundResource(R.drawable.btn_bg_blue_broder);
                h.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                h.btnFollow.setText(R.string.following);
            } else {
                h.btnFollow.setBackgroundResource(R.drawable.button_effect_invert);
                h.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                h.btnFollow.setText(R.string.follow);
            }
        }

        h.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ExSearchTag searchTag = feedItems.get(position);
                apiForFollowUnFollow(searchTag, position);
            }
        });

        switch (searchTag.type){
            case 0:
            case 1:
                if(!TextUtils.isEmpty(searchTag.imageUrl)){
                    Picasso.with(mContext).load(searchTag.imageUrl).placeholder(R.drawable.default_placeholder).fit().into(h.ivProfile);
                }else Picasso.with(mContext).load(R.drawable.default_placeholder).into(h.ivProfile);

                h.ivProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (searchTag.userType.equals("user")) {
                            Intent intent = new Intent(mContext, UserProfileActivity.class);
                            intent.putExtra("userId", String.valueOf(searchTag.id));
                            mContext.startActivity(intent);
                        }else if (searchTag.userType.equals("artist") && searchTag.id== Mualab.currentUser.id){
                            Intent intent = new Intent(mContext, UserProfileActivity.class);
                            intent.putExtra("userId", String.valueOf(searchTag.id));
                            mContext.startActivity(intent);
                        }
                        else {
                            Intent intent = new Intent(mContext, ArtistProfileActivity.class);
                            intent.putExtra("artistId", String.valueOf(searchTag.id));
                            mContext.startActivity(intent);
                        }

                    }
                });

                break;

            case 2:
                Picasso.with(mContext).load(R.drawable.hag_tag_ico).placeholder(R.drawable.hag_tag_ico).fit().into(h.ivProfile);
                break;

            case 4:
                h.tvDesc.setVisibility(View.GONE);
                Picasso.with(mContext).load(R.drawable.ic_location_tag).placeholder(R.drawable.ic_location_tag).fit().into(h.ivProfile);
                break;
        }
    }

    private class Holder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private CircleImageView ivProfile;
        private TextView tvHeader;
        private TextView tvDesc;
        protected AppCompatButton btnFollow;

        private Holder(View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvHeader = itemView.findViewById(R.id.tvHeader);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            btnFollow = itemView.findViewById(R.id.btnFollow);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if(feedItems.size()>pos){
                ExSearchTag searchTag = feedItems.get(pos);
                listener.onItemClick(searchTag, pos);
            }
        }
    }
    private void apiForFollowUnFollow(final ExSearchTag feeds, final int position) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", "" + Mualab.currentUser.id);
        map.put("followerId", "" + feeds.id);

        new HttpTask(new HttpTask.Builder(mContext, "followFollowing", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    //String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        if (feeds.followerStatus == 0) {
                            feeds.followerStatus = 1;
                        } else if (feeds.followerStatus == 1) {
                            feeds.followerStatus = 0;
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
                notifyItemChanged(position);
            }
        }).setParam(map)).execute("followFollowing");
    }
}

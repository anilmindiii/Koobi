package com.mualab.org.user.activity.artist_profile.adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.hendraanggrian.socialview.SocialView;
import com.hendraanggrian.widget.SocialTextView;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.explore.SearchFeedActivity;
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.mualab.org.user.activity.feeds.activity.FeedSingleActivity;
import com.mualab.org.user.activity.feeds.activity.ReportActivity;
import com.mualab.org.user.activity.feeds.activity.SaveToFolderActivity;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.activity.feeds.adapter.ViewPagerAdapter;
import com.mualab.org.user.activity.feeds.listener.SaveToFolderListner;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.data.remote.API;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;

import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.listener.OnDoubleTapListener;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.constants.Constant;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;


@SuppressWarnings("WeakerAccess")
public class ArtistFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected boolean showLoader;
    private final int TEXT_TYPE = 0;
    private final int IMAGE_TYPE = 1;
    private final int VIDEO_TYPE = 2;
    private final int VIEW_TYPE_LOADING = 3;
    private Context mContext;
    public List<Feeds> feedItems;
    private Listener listener;
    private boolean loading;
    public boolean isGrideView;

    public void showHideLoading(boolean b) {
        loading = b;
    }

    public void showLoading(boolean status) {
        showLoader = status;
    }


    public interface Listener {
        void onCommentBtnClick(Feeds feed, int pos);

        void onLikeListClick(Feeds feed);

        void onFeedClick(Feeds feed, int index, View v);

        void onClickProfileImage(Feeds feed, ImageView v);
    }

    public void isGrideView(boolean isGrideView) {
        this.isGrideView = isGrideView;
    }

    public void clear() {
        final int size = feedItems.size();
        feedItems.clear();
        notifyItemRangeRemoved(0, size);
    }

    public ArtistFeedAdapter(Context mContext, List<Feeds> feedItems, Listener listener) {
        this.mContext = mContext;

        this.feedItems = feedItems;
        this.listener = listener;



    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TEXT_TYPE:
                if (!isGrideView){
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_feed_text_item_layout, parent, false);
                    FeedTextHolder textHolder = new FeedTextHolder(view);
                    setupTextFeedClickableViews(textHolder);
                    return textHolder;
                }

            case IMAGE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_feed_image_item_layout, parent, false);
                CellFeedViewHolder cellFeedViewHolder = new CellFeedViewHolder(view);
                setupClickableViews(cellFeedViewHolder);
                return cellFeedViewHolder;
            case VIDEO_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_feed_video_item_layout, parent, false);
                FeedVideoHolder feedViepoHolder = new FeedVideoHolder(view);
                setupFeedVideoClickableViews(feedViepoHolder);
                return feedViepoHolder;

            case VIEW_TYPE_LOADING:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new LoadingViewHolder(view);
        }
        return null;
    }


    @Override
    public int getItemViewType(int position) {
        Feeds feed = feedItems.get(position);
       /* if(position==feedItems.size()-1) {
            if (loading) {
                return VIEW_TYPE_LOADING;
            }
        }
*/
        if (position == feedItems.size() - 1) {
            switch (feed.feedType) {
                case "text":
                    return showLoader ? VIEW_TYPE_LOADING : TEXT_TYPE;
                case "image":
                    return showLoader ? VIEW_TYPE_LOADING : IMAGE_TYPE;
                case "video":
                    return showLoader ? VIEW_TYPE_LOADING : VIDEO_TYPE;
                default:
                    return VIEW_TYPE_LOADING;
            }
        }

        switch (feed.feedType) {
            case "text":
                return TEXT_TYPE;
            case "image":
                return IMAGE_TYPE;
            case "video":
                return VIDEO_TYPE;
            default:
                return VIEW_TYPE_LOADING;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loaderViewHolder = (LoadingViewHolder) holder;
            if (showLoader) {
                loaderViewHolder.progressBar.setVisibility(View.VISIBLE);
            } else {
                loaderViewHolder.progressBar.setVisibility(View.GONE);
            }
            return;
        }
        final Feeds feeds = feedItems.get(position);

        if (!isGrideView)
            if (holder instanceof Holder) {
                final Holder h = (Holder) holder;
                if (!TextUtils.isEmpty(feeds.profileImage)) {
                    Picasso.with(mContext).load(feeds.profileImage).fit().into(h.ivProfile);
                } else Picasso.with(mContext).load(R.drawable.celbackgroung).into(h.ivProfile);

                if (feeds.userId == Mualab.currentUser.id) {
                    h.tv_report.setVisibility(View.GONE);
                    h.view_divider.setVisibility(View.GONE);
                    h.tv_delete.setVisibility(View.VISIBLE);
                } else {
                    h.tv_report.setVisibility(View.VISIBLE);
                    h.view_divider.setVisibility(View.VISIBLE);
                    h.tv_delete.setVisibility(View.GONE);
                }


                h.tvUserName.setText(feeds.userName);
                h.tvPostTime.setText(feeds.crd);
                h.tvUserLocation.setText(TextUtils.isEmpty(feeds.location) ? "N/A" : feeds.location);
                h.tv_like_count.setText(String.valueOf(feeds.likeCount));
                h.tv_comments_count.setText(String.valueOf(feeds.commentCount));
                h.likeIcon.setChecked(feeds.isLike == 1);
                h.tv_text.setText(feeds.caption);

                if (feedItems.get(h.getAdapterPosition()).isSave == 1) {
                    h.iv_save_to_folder.setImageResource(R.drawable.active_book_mark_ico1);
                } else h.iv_save_to_folder.setImageResource(R.drawable.inactive_book_mark_ico1);

                SaveToFolderListner.getmInctance().setListner((Text, pos) -> {
                    feedItems.get(pos).isSave = 1;
                    notifyItemChanged(pos);
                });

                if (!TextUtils.isEmpty(feeds.caption)) {
                    h.tv_text.setVisibility(View.VISIBLE);
                    h.tv_text.setText(feeds.caption);
                } else h.tv_text.setVisibility(View.GONE);

                if (feeds.userId == Mualab.currentUser.id) {
                    h.btnFollow.setVisibility(View.GONE);
                } else {
                    h.btnFollow.setVisibility(View.GONE);
                    if (feeds.followingStatus == 1) {
                        h.btnFollow.setBackgroundResource(R.drawable.btn_bg_blue_broder);
                        h.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                        h.btnFollow.setText(R.string.following);
                    } else {
                        h.btnFollow.setBackgroundResource(R.drawable.button_effect_invert);
                        h.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                        h.btnFollow.setText(R.string.follow);
                    }
                }

                if(feeds.ispopupOpen){
                    h.ly_share.setVisibility(View.VISIBLE);
                }else  h.ly_share.setVisibility(View.GONE);
            }


        switch (feeds.feedType) {

            case "image": {

                final CellFeedViewHolder imageHolder = ((CellFeedViewHolder) holder);

                if (isGrideView) {
                    imageHolder.grid_view.setVisibility(View.VISIBLE);
                    imageHolder.list_view.setVisibility(View.GONE);
                    Glide.with(mContext).load(feeds.feed.get(0)).placeholder(R.drawable.gallery_placeholder).into(imageHolder.imageView);

                } else {
                    imageHolder.grid_view.setVisibility(View.GONE);
                    imageHolder.list_view.setVisibility(View.VISIBLE);
                }

                imageHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent1 = new Intent(mContext, FeedSingleActivity.class);
                        intent1.putExtra("feedId", feeds._id + "");
                        intent1.putExtra("userType", feedItems.get(position).userInfo.get(0).userType);
                        mContext.startActivity(intent1);
                    }
                });

                imageHolder.weakRefAdapter = new WeakReference<>(new ViewPagerAdapter(mContext, feeds, false,
                        new ViewPagerAdapter.Listner() {
                            @Override
                            public void onSingleTap() {
                                int pos = imageHolder.weakRefViewPager.get().getCurrentItem();
                                if (feeds.feedType.equalsIgnoreCase("image")) {
                                    listener.onFeedClick(feeds, pos, imageHolder.rl_imageView);

                                }
                            }

                            @Override
                            public void onDoubleTap() {
                                int pos = imageHolder.getAdapterPosition();
                                Feeds feed = feedItems.get(pos);
                        if(feed.isLike==0){
                            feed.isLike = 1;
                            feed.likeCount = ++feed.likeCount;
                            apiForLikes(feeds);
                        }
                                notifyItemChanged(pos);
                            }

                        }));

                imageHolder.weakRefViewPager.get().setAdapter(imageHolder.weakRefAdapter.get());

                if (feeds.feed.size() > 1) {
                    addBottomDots(imageHolder.ll_Dot, feeds.feed.size(), 0);
                    imageHolder.ll_Dot.setVisibility(View.VISIBLE);
                    imageHolder.weakRefViewPager.get().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int pos, float positionOffset, int positionOffsetPixels) {

                        }

                        @Override
                        public void onPageSelected(int pos) {
                            Feeds feed = feedItems.get(imageHolder.getAdapterPosition());
                            feed.viewPagerlastPos = pos;
                            addBottomDots(imageHolder.ll_Dot, feed.feed.size(), pos);
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {
                        }
                    });
                } else imageHolder.ll_Dot.setVisibility(View.GONE);

                imageHolder.weakRefViewPager.get().setCurrentItem(feeds.viewPagerlastPos);
            }
            break;


            case "video":
                final FeedVideoHolder videoHolder = ((FeedVideoHolder) holder);

                if (isGrideView) {
                    videoHolder.videoIcon.setVisibility(View.VISIBLE);
                    videoHolder.grid_view.setVisibility(View.VISIBLE);
                    videoHolder.list_view.setVisibility(View.GONE);

                    if (!TextUtils.isEmpty(feeds.videoThumbnail)) {
                        Picasso.with(mContext)
                                .load(feeds.videoThumbnail)
                                .placeholder(R.drawable.gallery_placeholder)
                                .into(videoHolder.imageView);
                    } else Picasso.with(mContext)
                            .load(R.drawable.gallery_placeholder)
                            .into(videoHolder.imageView);

                } else {
                    videoHolder.videoIcon.setVisibility(View.GONE);
                    videoHolder.grid_view.setVisibility(View.GONE);
                    videoHolder.list_view.setVisibility(View.VISIBLE);
                }

                videoHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent1 = new Intent(mContext, FeedSingleActivity.class);
                        intent1.putExtra("feedId", feeds._id+"");
                        intent1.putExtra("userType",feedItems.get(position).userInfo.get(0).userType);
                        mContext.startActivity(intent1);
                    }
                });

                if (!TextUtils.isEmpty(feeds.videoThumbnail)) {
                    Picasso.with(mContext)
                            .load(feeds.videoThumbnail)
                            .placeholder(R.drawable.gallery_placeholder)
                            .into(videoHolder.ivFeedCenter);
                } else Picasso.with(mContext)
                        .load(R.drawable.gallery_placeholder)
                        .into(videoHolder.ivFeedCenter);


                break;
        }
        // }
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    private void setupTextFeedClickableViews(final FeedTextHolder holder) {
        holder.tv_text.setHashtagColor(ContextCompat.getColor(mContext, R.color.text_color));
        holder.tv_text.setMentionColor(ContextCompat.getColor(mContext, R.color.text_color));
        holder.tv_text.setOnHyperlinkClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                holder.ly_share.setVisibility(View.GONE);
                String url = charSequence.toString();
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mContext.startActivity(browserIntent);
                return null;
            }
        });

        holder.tv_text.setOnHashtagClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                goHashTag(charSequence);
                hide_menu_Text(holder);
                return null;
            }
        });

        holder.iv_save_to_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (feedItems.get(holder.getAdapterPosition()).isSave == 1) {
                    removeToFolder(feedItems.get(holder.getAdapterPosition())._id,holder.getAdapterPosition());
                } else {
                    Intent intent = new Intent(mContext, SaveToFolderActivity.class);
                    intent.putExtra("feedId", feedItems.get(holder.getAdapterPosition())._id);
                    intent.putExtra("pos", holder.getAdapterPosition());
                    mContext.startActivity(intent);
                }

                hide_menu_Text(holder);
            }
        });

        holder.main_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide_menu_Text(holder);
            }
        });

        holder.tv_text.setOnMentionClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                for(Feeds feed:feedItems){
                    feed.ispopupOpen = false;
                }
                apiForgetUserIdFromUserName(charSequence);
                hide_menu_Text(holder);
                return null;
            }
        });

        holder.ly_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int pos = holder.getAdapterPosition();
                Feeds feed = feedItems.get(pos);
                if (listener != null) {
                    listener.onCommentBtnClick(feed, pos);
                }
                hide_menu_Text(holder);
            }
        });

        holder.ly_like_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int adapterPosition = holder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                if (listener != null) {
                    listener.onLikeListClick(feed);
                }
                hide_menu_Text(holder);
            }
        });

        holder.likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int adapterPosition = holder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                feed.isLike = feed.isLike == 1 ? 0 : 1;
                feed.likeCount = feed.isLike == 1 ? ++feed.likeCount : --feed.likeCount;
                notifyItemChanged(adapterPosition);
                apiForLikes(feed);
                hide_menu_Text(holder);
            }
        });

        holder.tv_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ReportActivity.class);
                intent.putExtra("feedOwenerId", feedItems.get(holder.getAdapterPosition()).userId);
                intent.putExtra("feedId", feedItems.get(holder.getAdapterPosition())._id);
                mContext.startActivity(intent);
                hide_menu_Text(holder);
            }
        });

        holder.tv_delete.setOnClickListener(v -> {
            apiForDeletePost(feedItems.get(holder.getAdapterPosition()), holder.getAdapterPosition());

        });

        holder.tv_share_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int adapterPosition = holder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                getPermissionForTakepicture(null, holder.tv_text.getText().toString(),
                        null,feed._id);
                hide_menu_Text(holder);
            }
        });

        holder.iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(feedItems.get(holder.getAdapterPosition()).ispopupOpen){
                        for(Feeds feed:feedItems){
                            feed.ispopupOpen = false;
                        }
                    }else {
                        for(Feeds feed:feedItems){
                            feed.ispopupOpen = false;
                        }
                        feedItems.get(holder.getAdapterPosition()).ispopupOpen = true;
                    }

                    notifyDataSetChanged();
                }catch (Exception e){

                }





            }
        });

        holder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // holder.btnFollow.setEnabled(false);
                int adapterPosition = holder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                followUnfollow(feed, adapterPosition);
                hide_menu_Text(holder);
            }
        });

        holder.ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listener != null) {
                    int adapterPosition = holder.getAdapterPosition();
                    Feeds feed = feedItems.get(adapterPosition);
                    listener.onClickProfileImage(feed, holder.ivProfile);
                }
                hide_menu_Text(holder);
            }
        });
    }

    private void hide_menu_Text(FeedTextHolder holder) {
        try {
            for(Feeds feed:feedItems){
                feed.ispopupOpen = false;
            }
            notifyDataSetChanged();
            holder.ly_share.setVisibility(View.GONE);
        }catch (Exception e){

        }

    }

    private void setupFeedVideoClickableViews(final FeedVideoHolder videoHolder) {
        videoHolder.tv_text.setHashtagColor(ContextCompat.getColor(mContext, R.color.text_color));
        videoHolder.tv_text.setMentionColor(ContextCompat.getColor(mContext, R.color.text_color));
        videoHolder.tv_text.setOnHyperlinkClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                String url = charSequence.toString();
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mContext.startActivity(browserIntent);
                return null;
            }
        });

        videoHolder.tv_text.setOnHashtagClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {

                goHashTag(charSequence);
                hide_menu_Video(videoHolder);
                return null;
            }
        });


        videoHolder.tv_text.setOnMentionClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {

                apiForgetUserIdFromUserName(charSequence);
                hide_menu_Video(videoHolder);
                return null;
            }
        });

        videoHolder.iv_save_to_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (feedItems.get(videoHolder.getAdapterPosition()).isSave == 1) {
                    removeToFolder(feedItems.get(videoHolder.getAdapterPosition())._id,videoHolder.getAdapterPosition());
                } else {
                    Intent intent = new Intent(mContext, SaveToFolderActivity.class);
                    intent.putExtra("feedId", feedItems.get(videoHolder.getAdapterPosition())._id);
                    intent.putExtra("pos", videoHolder.getAdapterPosition());
                    mContext.startActivity(intent);
                }

                hide_menu_Video(videoHolder);
            }
        });

        videoHolder.main_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide_menu_Video(videoHolder);
            }
        });

        videoHolder.ly_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int pos = videoHolder.getAdapterPosition();
                Feeds feed = feedItems.get(pos);
                if (listener != null) {
                    listener.onCommentBtnClick(feed, pos);
                }
                hide_menu_Video(videoHolder);

            }
        });

        videoHolder.ly_like_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int adapterPosition = videoHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                if (listener != null) {
                    listener.onLikeListClick(feed);
                }
                hide_menu_Video(videoHolder);
            }
        });

        videoHolder.likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int adapterPosition = videoHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                feed.isLike = feed.isLike == 1 ? 0 : 1;
                feed.likeCount = feed.isLike == 1 ? ++feed.likeCount : --feed.likeCount;
                notifyItemChanged(adapterPosition);
                apiForLikes(feed);
                hide_menu_Video(videoHolder);
            }
        });

        videoHolder.tv_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ReportActivity.class);
                intent.putExtra("feedOwenerId", feedItems.get(videoHolder.getAdapterPosition()).userId);
                intent.putExtra("feedId", feedItems.get(videoHolder.getAdapterPosition())._id);
                mContext.startActivity(intent);
                hide_menu_Video(videoHolder);
            }
        });

        videoHolder.tv_delete.setOnClickListener(v -> {
            apiForDeletePost(feedItems.get(videoHolder.getAdapterPosition()), videoHolder.getAdapterPosition());

        });

        videoHolder.tv_share_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int adapterPosition = videoHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                getPermissionForTakepicture(videoHolder.rl_imageView,
                        videoHolder.tv_text.getText().toString(),feed.feedData.get(0).feedPost,feed._id);

                hide_menu_Video(videoHolder);
            }
        });

        videoHolder.iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(feedItems.get(videoHolder.getAdapterPosition()).ispopupOpen){
                        for(Feeds feed:feedItems){
                            feed.ispopupOpen = false;
                        }
                    }else {
                        for(Feeds feed:feedItems){
                            feed.ispopupOpen = false;
                        }
                        feedItems.get(videoHolder.getAdapterPosition()).ispopupOpen = true;
                    }
                    notifyDataSetChanged();

                }catch (Exception e){

                }


            }
        });

        videoHolder.ivFeedCenter.setOnTouchListener(new MyOnDoubleTapListener(mContext, videoHolder));

        videoHolder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = videoHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                followUnfollow(feed, adapterPosition);
            }
        });

        videoHolder.ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listener != null) {
                    int adapterPosition = videoHolder.getAdapterPosition();
                    Feeds feed = feedItems.get(adapterPosition);
                    listener.onClickProfileImage(feed, videoHolder.ivProfile);
                }

                hide_menu_Video(videoHolder);
            }
        });

    }

    private void hide_menu_Video(FeedVideoHolder videoHolder) {
        try {
            for(Feeds feed:feedItems){
                feed.ispopupOpen = false;
            }
            notifyDataSetChanged();
            videoHolder.ly_share.setVisibility(View.GONE);
        }catch (Exception e){

        }

    }

    private void setupClickableViews(final CellFeedViewHolder cellFeedViewHolder) {
        cellFeedViewHolder.tv_text.setHashtagColor(ContextCompat.getColor(mContext, R.color.text_color));
        cellFeedViewHolder.tv_text.setMentionColor(ContextCompat.getColor(mContext, R.color.text_color));
        cellFeedViewHolder.tv_text.setOnHyperlinkClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                String url = charSequence.toString();
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mContext.startActivity(browserIntent);
                return null;
            }
        });

        cellFeedViewHolder.tv_text.setOnHashtagClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {

                goHashTag(charSequence);
                hide_menu_cellFeed(cellFeedViewHolder);
                return null;
            }
        });

        cellFeedViewHolder.iv_save_to_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (feedItems.get(cellFeedViewHolder.getAdapterPosition()).isSave == 1) {
                    removeToFolder(feedItems.get(cellFeedViewHolder.getAdapterPosition())._id,cellFeedViewHolder.getAdapterPosition());
                } else {
                    Intent intent = new Intent(mContext, SaveToFolderActivity.class);
                    intent.putExtra("feedId", feedItems.get(cellFeedViewHolder.getAdapterPosition())._id);
                    intent.putExtra("pos", cellFeedViewHolder.getAdapterPosition());
                    mContext.startActivity(intent);
                }

                hide_menu_cellFeed(cellFeedViewHolder);
            }
        });


        cellFeedViewHolder.main_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide_menu_cellFeed(cellFeedViewHolder);
            }
        });


        cellFeedViewHolder.tv_text.setOnMentionClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {

                apiForgetUserIdFromUserName(charSequence);
                hide_menu_cellFeed(cellFeedViewHolder);
                return null;
            }
        });

        cellFeedViewHolder.ly_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int pos = cellFeedViewHolder.getAdapterPosition();
                Feeds feed = feedItems.get(pos);
                if (listener != null) {
                    listener.onCommentBtnClick(feed, pos);
                }
                hide_menu_cellFeed(cellFeedViewHolder);
            }
        });


        cellFeedViewHolder.rl_imageView.setOnTouchListener(new MyOnDoubleTapListener(mContext, cellFeedViewHolder));

        cellFeedViewHolder.ly_like_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                if (listener != null) {
                    listener.onLikeListClick(feed);
                }
                hide_menu_cellFeed(cellFeedViewHolder);
            }
        });

        cellFeedViewHolder.likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                feed.isLike = feed.isLike == 1 ? 0 : 1;
                feed.likeCount = feed.isLike == 1 ? ++feed.likeCount : --feed.likeCount;
                notifyItemChanged(adapterPosition);
                apiForLikes(feed);
                hide_menu_cellFeed(cellFeedViewHolder);
            }
        });

        cellFeedViewHolder.tv_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ReportActivity.class);
                intent.putExtra("feedOwenerId", feedItems.get(cellFeedViewHolder.getAdapterPosition()).userId);
                intent.putExtra("feedId", feedItems.get(cellFeedViewHolder.getAdapterPosition())._id);
                mContext.startActivity(intent);
                hide_menu_cellFeed(cellFeedViewHolder);
            }
        });

        cellFeedViewHolder.tv_delete.setOnClickListener(v -> {
            apiForDeletePost(feedItems.get(cellFeedViewHolder.getAdapterPosition()), cellFeedViewHolder.getAdapterPosition());
        });

        cellFeedViewHolder.tv_share_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                getPermissionForTakepicture(cellFeedViewHolder.rl_imageView,
                        cellFeedViewHolder.tv_text.getText().toString(),feed.feedData.get(0).feedPost,
                        feed._id);

                hide_menu_cellFeed(cellFeedViewHolder);

            }
        });

        cellFeedViewHolder.iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(feedItems.get(cellFeedViewHolder.getAdapterPosition()).ispopupOpen){
                        for(Feeds feed:feedItems){
                            feed.ispopupOpen = false;
                        }
                    }else {
                        for(Feeds feed:feedItems){
                            feed.ispopupOpen = false;
                        }

                        feedItems.get(cellFeedViewHolder.getAdapterPosition()).ispopupOpen = true;
                    }

                    notifyDataSetChanged();
                }catch (Exception e){

                }



            }
        });

        cellFeedViewHolder.btnFollow.setOnClickListener(v -> {

            //cellFeedViewHolder.btnFollow.setEnabled(false);
            int adapterPosition = cellFeedViewHolder.getAdapterPosition();
            Feeds feed = feedItems.get(adapterPosition);
            followUnfollow(feed, adapterPosition);
            hide_menu_cellFeed(cellFeedViewHolder);
        });

        cellFeedViewHolder.ivProfile.setOnClickListener(v -> {

            if (listener != null) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                listener.onClickProfileImage(feed, cellFeedViewHolder.ivProfile);
            }
            hide_menu_cellFeed(cellFeedViewHolder);
        });
    }

    private void hide_menu_cellFeed(CellFeedViewHolder cellFeedViewHolder) {
        try {
            for(Feeds feed:feedItems){
                feed.ispopupOpen = false;
            }
            notifyDataSetChanged();
            cellFeedViewHolder.ly_share.setVisibility(View.GONE);
        }catch (Exception e){

        }

    }

    // Anil's work
    private void goHashTag(CharSequence charSequence) {
        Intent intent = new Intent(mContext, SearchFeedActivity.class);
        String tag = charSequence.toString().replace("#", "");
        ExSearchTag e = new ExSearchTag();
        e.title = tag;
        e.id = 0;
        e.type = ExSearchTag.SearchType.HASH_TAG;
        intent.putExtra("searchKey", e);
        mContext.startActivity(intent);
    }

    private void apiForgetUserIdFromUserName(final CharSequence userName) {
        String user_name = "";

        final Map<String, String> params = new HashMap<>();
        if (userName.toString().startsWith("@")) {

            user_name = userName.toString().replaceFirst("@", "");
            params.put("userName", user_name + "");
        } else params.put("userName", userName + "");

        params.put("userName", userName + "");
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
                        } else if (userType.equals("artist") && userId == Mualab.currentUser.id) {
                            Intent intent = new Intent(mContext, UserProfileActivity.class);
                            intent.putExtra("userId", String.valueOf(userId));
                            mContext.startActivity(intent);
                        } else {
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
        }).setBody(params, HttpTask.ContentType.APPLICATION_JSON)
                .setMethod(Request.Method.POST)
                .setProgress(true))
                .execute("FeedAdapter");
    }

    private void addBottomDots(LinearLayout ll_dots, int totalSize, int currentPage) {
        TextView[] dots = new TextView[totalSize];
        ll_dots.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(mContext);
            dots[i].setText(Html.fromHtml("•"));
            dots[i].setTextSize(25);
            dots[i].setTextColor(Color.parseColor("#999999"));
            ll_dots.addView(dots[i]);
        }
        if (dots.length > 0)
            dots[currentPage].setTextColor(Color.parseColor("#212121"));
    }

    private void apiForLikes(final Feeds feed) {
        /*  let dicParam  = ["feedId": objFeeds._id,
                             "userId":objFeeds.userInfo?._id ?? 0,
                             "likeById":self.myId,
                             "age":objAppShareData.getAge(from: dicUser["dob"] as! String),
                             "gender":dicUser["gender"] as! String,
                             "city":address.locality ?? "",
                             "state":address.administrativeArea ?? "",
                             "country":address.country ?? "",
                             "type":"feed"]  as [String : Any]*/
        Map<String, String> map = new HashMap<>();
        map.putAll(Mualab.feedBasicInfo);
        map.put("feedId", "" + feed._id);
        map.put("likeById", "" + Mualab.currentUser.id);
        map.put("userId", "" + feed.userId);
        map.put("type", "feed");// feed or comment
        Mualab.getInstance().getRequestQueue().cancelAll("like" + feed._id);

        new HttpTask(new HttpTask.Builder(mContext, "like",
                new HttpResponceListner.Listener() {
                    @Override
                    public void onResponse(String response, String apiName) {

                    }

                    @Override
                    public void ErrorListener(VolleyError error) {

                    }
                })
                .setAuthToken(Mualab.currentUser.authToken)
                .setParam(map)).execute("like" + feed._id);

    }

    static class Holder extends RecyclerView.ViewHolder {
        protected CheckBox likeIcon;
        protected ImageView ivLike,iv_menu,iv_save_to_folder;
        protected ImageView ivProfile, ivComments; //btnLike
        protected LinearLayout ly_like_count, ly_comments;
        protected TextView tvUserName, tvUserLocation, tvPostTime ,tv_delete;
        protected TextView tv_like_count, tv_comments_count;
        protected SocialTextView tv_text;
        protected AppCompatButton btnFollow;
        protected TextView tv_report, tv_share_post;
        protected RelativeLayout ly_share,rl_imageView,main_layout;
        protected View view_divider;

        public Holder(View itemView) {
            super(itemView);
            /*Common ui*/
            ivProfile = itemView.findViewById(R.id.iv_user_image);
            ly_share = itemView.findViewById(R.id.ly_report);
            iv_menu = itemView.findViewById(R.id.iv_menu);
            rl_imageView = itemView.findViewById(R.id.rl_imageView);
            ivComments = itemView.findViewById(R.id.iv_comments);
            tv_text = itemView.findViewById(R.id.tv_text);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserLocation = itemView.findViewById(R.id.tv_location);
            tvPostTime = itemView.findViewById(R.id.tv_post_time);
            tv_like_count = itemView.findViewById(R.id.tv_like_count);
            tv_comments_count = itemView.findViewById(R.id.tv_comments_count);
            ly_like_count = itemView.findViewById(R.id.ly_like_count);
            ly_comments = itemView.findViewById(R.id.ly_comments);
            ivLike = itemView.findViewById(R.id.ivLike);
            btnFollow = itemView.findViewById(R.id.btnFollow);
            likeIcon = itemView.findViewById(R.id.likeIcon);
            main_layout = itemView.findViewById(R.id.main_layout);
            tv_report = itemView.findViewById(R.id.tv_report);
            tv_share_post = itemView.findViewById(R.id.tv_share_post);
            view_divider = itemView.findViewById(R.id.view_divider);
            iv_save_to_folder = itemView.findViewById(R.id.iv_save_to_folder);
            tv_delete = itemView.findViewById(R.id.tv_delete);
        }
    }

    static class FeedTextHolder extends Holder {
        private FeedTextHolder(View itemView) {
            super(itemView);
        }
    }

    public static class FeedVideoHolder extends Holder {
        public ImageView ivFeedCenter, videoIcon;

        private LinearLayout list_view;
        private CardView grid_view;
        private ImageView imageView;

        private FeedVideoHolder(View itemView) {
            super(itemView);

            ivFeedCenter = itemView.findViewById(R.id.ivFeedCenter);
            grid_view = itemView.findViewById(R.id.grid_view);
            list_view = itemView.findViewById(R.id.list_view);
            imageView = itemView.findViewById(R.id.imageView);
            videoIcon = itemView.findViewById(R.id.videoIcon);
        }
    }

    static class CellFeedViewHolder extends Holder {
        private LinearLayout ll_Dot, list_view;
        private CardView grid_view;
        private RelativeLayout rl_imageView;
        private ImageView imageView;
        private WeakReference<ViewPager> weakRefViewPager;
        private WeakReference<ViewPagerAdapter> weakRefAdapter;

        private CellFeedViewHolder(View itemView) {
            super(itemView);

            ll_Dot = itemView.findViewById(R.id.ll_Dot);
            rl_imageView = itemView.findViewById(R.id.rl_imageView);
            grid_view = itemView.findViewById(R.id.grid_view);
            list_view = itemView.findViewById(R.id.list_view);
            imageView = itemView.findViewById(R.id.imageView);
            weakRefViewPager = new WeakReference<>((ViewPager) itemView.findViewById(R.id.viewpager));
        }
    }

    private class MyOnDoubleTapListener extends OnDoubleTapListener {
        private CellFeedViewHolder holder;
        private FeedVideoHolder feedVieoHolder;

        private MyOnDoubleTapListener(Context c, CellFeedViewHolder holder) {
            super(c);
            this.holder = holder;
        }

        private MyOnDoubleTapListener(Context c, FeedVideoHolder feedVieoHolder) {
            super(c);
            this.feedVieoHolder = feedVieoHolder;
        }

        @Override
        public void onClickEvent(MotionEvent e) {
            int adapterPosition = getPosition();
            Feeds feed = feedItems.get(adapterPosition);
            if (feed.feedType.equalsIgnoreCase("image")) {

            } else if (feed.feedType.equalsIgnoreCase("video")) {
                if (feed.feedThumb != null && feed.feedThumb.size() > 0) {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW)
                            .setDataAndType(Uri.parse(feed.feed.get(0)), "video/mp4")
                            .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
                }
            }
        }

        @Override
        public void onDoubleTap(MotionEvent e) {
            int adapterPosition = getPosition();
            Feeds feed = feedItems.get(adapterPosition);
            if (feed.isLike == 0) {
                feed.isLike = 1;
                feed.likeCount = ++feed.likeCount;
                apiForLikes(feed);
            }
            notifyItemChanged(adapterPosition);
        }

        private int getPosition() {
            if (holder != null) {
                return holder.getAdapterPosition();

            } else if (feedVieoHolder != null) {
                return feedVieoHolder.getAdapterPosition();
            }
            return 0;
        }
    }

    private void followUnfollow(final Feeds feeds, final int position) {

        if (feeds.followingStatus == 1) {
            apiForFollowUnFollow(feeds, position);
           /* new UnfollowDialog(mContext, feeds, new UnfollowDialog.UnfollowListner() {
                @Override
                public void onUnfollowClick(Dialog dialog) {
                    dialog.dismiss();

                }
            });*/
        } else apiForFollowUnFollow(feeds, position);

    }

    private void apiForFollowUnFollow(final Feeds feeds, final int position) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", "" + Mualab.currentUser.id);
        map.put("followerId", "" + feeds.userId);

        new HttpTask(new HttpTask.Builder(mContext, "followFollowing", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    //String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        if (feeds.followingStatus == 0) {
                            feeds.followingStatus = 1;
                        } else if (feeds.followingStatus == 1) {
                            feeds.followingStatus = 0;
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

    public void getPermissionForTakepicture(RelativeLayout scr_shot_view,
                                            String about_string,String imageNvideoUrl,int feedId){
        Activity activity = (Activity) mContext;
        if(Build.VERSION.SDK_INT >= 23) {
            if (mContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);
            }
            else {
                screenShot(scr_shot_view,about_string,imageNvideoUrl,feedId);
            }
        }else {
            screenShot(scr_shot_view,about_string,imageNvideoUrl,feedId);
        }
    }

    private void screenShot(RelativeLayout scr_shot_view, String text,String imageNvideoUrl,int feedId) {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        if(imageNvideoUrl == null|| scr_shot_view == null){
            //sharOnsocial(null,text,null,feedId);
            Helper.shareOnSocial(mContext,null, text, null, feedId);
        }else {
            try {
                String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".png";
                scr_shot_view.setDrawingCacheEnabled(true);
                scr_shot_view.buildDrawingCache(true);
                File imageFile = new File(mPath);
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                Bitmap bitmap = Bitmap.createBitmap(scr_shot_view.getDrawingCache());
                bitmap.compress(Bitmap.CompressFormat.PNG, 60, outputStream);
                scr_shot_view.destroyDrawingCache();
                //sharOnsocial(imageFile,text,imageNvideoUrl,feedId);
                Helper.shareOnSocial(mContext,imageFile, text, imageNvideoUrl, feedId);
                //onShareClick(imageFile,text);
                //doShareLink(text,otherProfileInfo.UserDetail.profileUrl);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

    }


    private void sharOnsocial(File imageFile, String text,String imageNvideoUrl,int feedId) {
        if(imageFile == null){
            Intent sharIntent = new Intent(Intent.ACTION_SEND);
            sharIntent.setType("text/plain");
            sharIntent.putExtra(Intent.EXTRA_SUBJECT, "Koobi Social");
            sharIntent.putExtra(Intent.EXTRA_TEXT, text+"\n"+imageNvideoUrl+"\n\n"+"http://koobi.co.uk/feedDetail/"+feedId+"");
            mContext.startActivity(Intent.createChooser(sharIntent, "Share:"));
        }else {
            Uri uri;
            Intent sharIntent = new Intent(Intent.ACTION_SEND);
            String ext = imageFile.getName().substring(imageFile.getName().lastIndexOf(".") + 1);
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String type = mime.getMimeTypeFromExtension(ext);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                sharIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".provider",imageFile);
                sharIntent.setDataAndType(uri, type);
            } else {
                uri = Uri.fromFile(imageFile);
                sharIntent.setDataAndType(uri, type);
            }

            sharIntent.setType("image/png");
            sharIntent.setType("text/plain");
            sharIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sharIntent.putExtra(Intent.EXTRA_SUBJECT, "Koobi Social");
            sharIntent.putExtra(Intent.EXTRA_TEXT, text+"\n"+imageNvideoUrl+"\n\n"+"http://koobi.co.uk/feedDetail/"+feedId+"");
            mContext.startActivity(Intent.createChooser(sharIntent, "Share:"));
        }


    }

    private void removeToFolder(final int feedId,int pos) {
        Progress.show(mContext);
        Map<String, String> map = new HashMap<>();
        map.put("feedId", String.valueOf(feedId));
        map.put("folderId", "");

        new HttpTask(new HttpTask.Builder(mContext, "removeToFolder", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(mContext);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    //String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        feedItems.get(pos).isSave = 0;
                        notifyItemChanged(pos);
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
                .execute("removeToFolder");


    }

    private void apiForDeletePost(final Feeds feeds, final int position) {
        new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.delete_post))
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Progress.show(mContext);
                        Map<String, String> map = new HashMap<>();
                        map.put("id", "" + feeds._id);
                        map.put("feedType", "" + feeds.feedType);
                        map.put("userId", "" + Mualab.currentUser.id);

                        new HttpTask(new HttpTask.Builder(mContext, "deleteFeed", new HttpResponceListner.Listener() {
                            @Override
                            public void onResponse(String response, String apiName) {
                                Progress.hide(mContext);
                                try {
                                    JSONObject js = new JSONObject(response);
                                    String status = js.getString("status");
                                    //String message = js.getString("message");

                                    if (status.equalsIgnoreCase("success")) {
                                        feedItems.remove(position);
                                    }
                                    notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Progress.hide(mContext);
                                    notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void ErrorListener(VolleyError error) {
                                notifyItemChanged(position);
                                Progress.hide(mContext);
                            }
                        }).setAuthToken(Mualab.currentUser.authToken)
                                .setParam(map)
                                .setMethod(Request.Method.POST)
                                .setProgress(false)
                                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                                .execute("deletePostApi");
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();


    }
}

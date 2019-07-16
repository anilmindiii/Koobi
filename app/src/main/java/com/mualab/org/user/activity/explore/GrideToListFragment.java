package com.mualab.org.user.activity.explore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.Views.refreshviews.CircleHeaderView;
import com.mualab.org.user.Views.refreshviews.RjRefreshLayout;
import com.mualab.org.user.activity.feeds.activity.CommentsActivity;
import com.mualab.org.user.activity.feeds.activity.LikeFeedActivity;
import com.mualab.org.user.activity.feeds.adapter.FeedAdapter;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.listener.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.utils.constants.Constant;

import java.io.Serializable;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class GrideToListFragment extends Fragment implements View.OnClickListener,
        FeedAdapter.Listener {

    private static final String ARG_PARAM1 = "feedItems";
    private static final String ARG_PARAM2 = "index";

    Context mContext;
    private ProgressBar progress_bar;
    private RjRefreshLayout mRefreshLayout;

    private FeedAdapter feedAdapter;
    private RecyclerView rvFeed;
    private EndlessRecyclerViewScrollListener endlesScrollListener;
    private boolean isHeaderShow;
    private List<Feeds> feedItems;
    int index;
    private ImageView ivUserProfile, btnBack;
    public CardView rlHeader1;


    public static GrideToListFragment newInstance(List<Feeds> feedItems, int index, boolean isHeaderShow) {
        GrideToListFragment fragment = new GrideToListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, (Serializable) feedItems);
        args.putSerializable(ARG_PARAM2, index);
        args.putSerializable("isHeaderShow", isHeaderShow);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            feedItems = (List<Feeds>) getArguments().getSerializable(ARG_PARAM1);
            index = getArguments().getInt(ARG_PARAM2);
            isHeaderShow = getArguments().getBoolean("isHeaderShow");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gride_to_list, container, false);

        ivUserProfile = getActivity().findViewById(R.id.ivUserProfile);

        rlHeader1 = view.findViewById(R.id.topLayout1);


        CardView header = view.findViewById(R.id.header);
        if (isHeaderShow)
            header.setVisibility(View.VISIBLE);
        TextView tvHeaderTitleLocal = view.findViewById(R.id.tvHeaderTitle);
        tvHeaderTitleLocal.setText("Post");
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            getActivity().onBackPressed();
        });


        ImageView ivChat = getActivity().findViewById(R.id.ivChat);
        ivChat.setVisibility(View.GONE);

        TextView tv_batch_count = getActivity().findViewById(R.id.tv_batch_count);
        tv_batch_count.setVisibility(View.GONE);

        ivUserProfile.setVisibility(View.GONE);

        //  if(rlHeader1 != null)


        if (rlHeader1 != null) {  //  this is the case where we come from explore
            rlHeader1.setVisibility(View.VISIBLE);

            btnBack = getActivity().findViewById(R.id.btnBack);
            btnBack.setVisibility(View.VISIBLE);
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        } else {
            TextView tvHeaderTitle = getActivity().findViewById(R.id.tvHeaderTitle);
            tvHeaderTitle.setText("Post");
        }


        initView(view);
        return view;
    }

    @Override
    public void onDestroy() {
        if (rlHeader1 != null) {
            //ivUserProfile.setVisibility(View.VISIBLE);
            btnBack.setVisibility(View.GONE);
        }

        // rlHeader1.setVisibility(View.GONE);
        super.onDestroy();
    }


    private void initView(View view) {
        rvFeed = view.findViewById(R.id.rvFeed);
        progress_bar = view.findViewById(R.id.progress_bar);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        rvFeed.setItemAnimator(null);
        rvFeed.setLayoutManager(manager);
        rvFeed.setHasFixedSize(true);

        feedAdapter = new FeedAdapter(mContext, "", feedItems, this);
        endlesScrollListener = new EndlessRecyclerViewScrollListener(manager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (feedAdapter != null) feedAdapter.showHideLoading(true);
                //apiForGetAllFeeds(page, 10, false);
            }
        };

        rvFeed.setAdapter(feedAdapter);
        rvFeed.scrollToPosition(index);
        rvFeed.addOnScrollListener(endlesScrollListener);


        final CircleHeaderView header = new CircleHeaderView(getContext());
       /*
        mRefreshLayout =  view.findViewById(R.id.mSwipeRefreshLayout);
       mRefreshLayout.addHeader(header);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                endlesScrollListener.resetState();
                isPulltoRefrash = true;
                //apiForGetAllFeeds(0, 10, false);
            }

            @Override
            public void onLoadMore() {
              //  Log.e(TAG, "onLoadMore: ");
            }
        });*/
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

    }

    @Override
    public void onDetach() {
        try {
            if (rlHeader1 != null) {
                rlHeader1.setVisibility(View.GONE);
            } else {
                TextView tvHeaderTitle = getActivity().findViewById(R.id.tvHeaderTitle);
                tvHeaderTitle.setText(feedItems.get(index).userName);
            }
        }catch (Exception e){

        }
        super.onDetach();
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCommentBtnClick(Feeds feed, int pos) {
        Intent intent = new Intent(mContext, CommentsActivity.class);
        intent.putExtra("feed_id", feed._id);
        intent.putExtra("feedPosition", pos);
        intent.putExtra("feed", feed);
        intent.putExtra("commentCount", feed.commentCount);
        startActivityForResult(intent, Constant.ACTIVITY_COMMENT);
    }

    @Override
    public void onLikeListClick(Feeds feed) {
        Intent intent = new Intent(mContext, LikeFeedActivity.class);
        intent.putExtra("feedId", feed._id);
        intent.putExtra("userId", Mualab.currentUser.userId);
        startActivity(intent);
    }

    @Override
    public void onFeedClick(Feeds feed, int index, View v) {

      /*  ArrayList<String> tempList = new ArrayList<>();
        for(int i=0; i<feed.feedData.size(); i++){
            tempList.add(feed.feedData.get(i).feedPost);
        }

        Intent intent = new Intent(mContext, PreviewImageActivity.class);
        intent.putExtra("imageArray", tempList);
        intent.putExtra("startIndex", index);
        startActivity(intent);*/
    }

    @Override
    public void onClickProfileImage(Feeds feed, ImageView v) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            int pos = data.getIntExtra("feedPosition", 0);
            Feeds feed = (Feeds) data.getSerializableExtra("feed");
            feedItems.get(pos).commentCount = feed.commentCount;
            feedAdapter.notifyItemChanged(pos);
        }

    }
}

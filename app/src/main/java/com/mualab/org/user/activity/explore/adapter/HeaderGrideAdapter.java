package com.mualab.org.user.activity.explore.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.mualab.org.user.activity.tag_module.instatag.TagDetail;
import com.mualab.org.user.activity.tag_module.instatag.TagToBeTagged;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.listener.FeedsListner;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class HeaderGrideAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW_TYPE_HEADER = 0;
    private static final int ITEM_VIEW_TYPE_ITEM = 1;

    private boolean showLoader;
    private final int FEED_TYPE = 1;
    private final int VIEW_TYPE_LOADING = 2;

    private Context mContext;
    private List<Feeds> feedItems;
    private ExploreGridViewAdapter.Listener listener;
    ExSearchTag exSearchTag;
    private FeedsListner feedsListner;
    boolean hideFirstIndex, isTaskDone;


    public HeaderGrideAdapter(Context mContext, ExSearchTag exSearchTag, List<Feeds> feedItems,
                              ExploreGridViewAdapter.Listener listener, FeedsListner feedsListner, boolean hideFirstIndex) {
        this.mContext = mContext;
        this.feedItems = feedItems;
        this.listener = listener;
        this.exSearchTag = exSearchTag;
        this.feedsListner = feedsListner;
        this.hideFirstIndex = hideFirstIndex;


    }

    public boolean isHeader(int position) {
        return position == 0;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
       /* if (isHeader(position)) {

            final HeaderHolder headerHolder = ((HeaderHolder) holder);
            if (feedItems.size() != 0) {
                final Feeds feeds = feedItems.get(position);
                if (feeds.feedType.equals("image")) {
                    Picasso.with(mContext).load(feeds.feedData.get(position).feedPost)
                           // .resize(200, 200)
                            .placeholder(R.drawable.gallery_placeholder)
                            .into(headerHolder.imageViewHeader);
                }

            }
            return;
        }*/
        try{
            final Feeds feeds = feedItems.get(position);
            final Holder h = ((Holder) holder);



            if (feeds.feedType.equals("image")) {
                h.videoIcon.setVisibility(View.GONE);
                if (feeds.feedData.size() != 0) {
                    Picasso.with(mContext).load(feeds.feedData.get(0).feedPost)
                            .resize(200, 200)
                            .centerCrop().placeholder(R.drawable.gallery_placeholder)
                            .into(h.imageView);
                }
            } else if (feeds.feedType.equals("video")) {
                h.videoIcon.setVisibility(View.VISIBLE);

                if (!feeds.feedData.get(0).videoThumb.equals("")) {
                    Picasso.with(mContext).load(feeds.feedData.get(0).videoThumb)
                            .resize(200, 200).centerCrop()
                            .placeholder(R.color.gray2)
                            .into(h.imageView);
                }



            }

            ChildServiceAdapter serviceAdapter = new ChildServiceAdapter(feeds.serviceTagList);
            h.rcv_service.setAdapter(serviceAdapter);

           /* h.tv_category.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, BookingActivity.class);
                    TagDetail tagDetail = feeds.serviceTagList.get(0).getTagDetails();

                    if (!tagDetail.tagId.equals("")) {// Apply file case
                        intent.putExtra("_id", Integer.parseInt(tagDetail.tagId));
                        intent.putExtra("artistId", tagDetail.artistId);
                        intent.putExtra("callType", "In Call");

                        intent.putExtra("mainServiceName", "");
                        intent.putExtra("subServiceName", "yes");
                        intent.putExtra("isEditService", true);
                        intent.putExtra("isFromSearchBoard", true);

                        if (!tagDetail.businessTypeId.equals("")) {
                            if (tagDetail.tagId.contains(",")) {
                                String id = tagDetail.businessTypeId.split(",")[0];
                                intent.putExtra("serviceId", Integer.parseInt(id));
                            } else {
                                intent.putExtra("serviceId", Integer.parseInt(tagDetail.businessTypeId));
                            }
                        }

                        if (!tagDetail.categoryId.equals("")) {
                            if (tagDetail.categoryId.contains(",")) {
                                String id = tagDetail.categoryId.split(",")[0];
                                intent.putExtra("subServiceId", Integer.parseInt(id));
                            } else
                                intent.putExtra("subServiceId", Integer.parseInt(tagDetail.categoryId));
                        }


                        boolean isOutCallSelected = false;
                        boolean isInCallSelected = false;

                        if (Double.parseDouble(tagDetail.incallPrice) != 0.0 &&
                                Double.parseDouble(tagDetail.outcallPrice) != 0){
                            //services.bookingType = "Incall";
                            intent.putExtra("callType", "In Call");
                            isInCallSelected = true;
                        } else if (Double.parseDouble(tagDetail.incallPrice) != 0.0){
                            //services.bookingType = "Incall";
                            isInCallSelected = true;
                            intent.putExtra("callType", "In Call");
                        }

                        else if (Double.parseDouble(tagDetail.outcallPrice) != 0.0){
                            isOutCallSelected = true;
                            //services.bookingType = "Outcall";
                            intent.putExtra("callType", "Out Call");
                        }



                        if(tagDetail.staffId == null){
                            tagDetail.staffId = "0";
                        }
                        intent.putExtra("staffId", Integer.parseInt(tagDetail.staffId));
                        intent.putExtra("isFromServiceTag", true);
                        intent.putExtra("incallStaff", isInCallSelected);
                        intent.putExtra("outcallStaff", isOutCallSelected);
                        //intent.putExtra("bookingDate", item.date);
                        mContext.startActivity(intent);


                    }
                }
            });*/

        }catch (Exception e){

        }






        // populate your holder with data from your model as usual
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

     /*   if (viewType == ITEM_VIEW_TYPE_HEADER) {
            View headerView = inflater.inflate(R.layout.adapter_explore_header, parent, false);
            return new HeaderHolder(headerView);
        }*/

        View cellView = inflater.inflate(R.layout.adapter_explore_gridview_new, parent, false);
        return new Holder(cellView);
    }

   /* @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_ITEM;
    }*/

    @Override
    public int getItemCount() {
        return feedItems.size() ; // add one for the header
    }

    private class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imageView, videoIcon;
        FrameLayout main_layout;
        RecyclerView rcv_service;

        private Holder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            videoIcon = itemView.findViewById(R.id.videoIcon);
            main_layout = itemView.findViewById(R.id.main_layout);
            rcv_service = itemView.findViewById(R.id.rcv_service);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            // Feeds feed = feedItems.get(pos);
            listener.onFeedClick(feedItems, pos);


        }
    }

    private class HeaderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageViewHeader;

        public HeaderHolder(View headerView) {
            super(headerView);
            imageViewHeader = itemView.findViewById(R.id.imageView);

            headerView.setOnClickListener(this::onClick);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            // Feeds feed = feedItems.get(pos);
            listener.onFeedClick(feedItems, pos);


        }
    }


/*..................................................... child adapter  .......................................*/

    class ChildServiceAdapter extends RecyclerView.Adapter<ChildServiceAdapter.ViewHolder>{
        ArrayList<TagToBeTagged> serviceTagList;

        public ChildServiceAdapter(ArrayList<TagToBeTagged> serviceTagList) {
            this.serviceTagList = serviceTagList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.child_service_item,viewGroup,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            viewHolder.tv_category.setText(serviceTagList.get(i).getTagDetails().title.trim());
        }

        @Override
        public int getItemCount() {
            return serviceTagList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView tv_category;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                tv_category = itemView.findViewById(R.id.tv_category);
                itemView.setOnClickListener(this::onClick);
            }

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, BookingActivity.class);
                TagDetail tagDetail = serviceTagList.get(getAdapterPosition()).getTagDetails();

                if (!tagDetail.tagId.equals("")) {// Apply file case
                    intent.putExtra("_id", Integer.parseInt(tagDetail.tagId));
                    intent.putExtra("artistId", tagDetail.artistId);
                    intent.putExtra("callType", "In Call");

                    intent.putExtra("mainServiceName", "");
                    intent.putExtra("subServiceName", "yes");
                    intent.putExtra("isEditService", true);
                    intent.putExtra("isFromSearchBoard", true);

                    if (!tagDetail.businessTypeId.equals("")) {
                        if (tagDetail.tagId.contains(",")) {
                            String id = tagDetail.businessTypeId.split(",")[0];
                            intent.putExtra("serviceId", Integer.parseInt(id));
                        } else {
                            intent.putExtra("serviceId", Integer.parseInt(tagDetail.businessTypeId));
                        }
                    }

                    if (!tagDetail.categoryId.equals("")) {
                        if (tagDetail.categoryId.contains(",")) {
                            String id = tagDetail.categoryId.split(",")[0];
                            intent.putExtra("subServiceId", Integer.parseInt(id));
                        } else
                            intent.putExtra("subServiceId", Integer.parseInt(tagDetail.categoryId));
                    }


                    boolean isOutCallSelected = false;
                    boolean isInCallSelected = false;

                    if (Double.parseDouble(tagDetail.incallPrice) != 0.0 &&
                            Double.parseDouble(tagDetail.outcallPrice) != 0) {
                        //services.bookingType = "Incall";
                        intent.putExtra("callType", "In Call");
                        isInCallSelected = true;
                    } else if (Double.parseDouble(tagDetail.incallPrice) != 0.0) {
                        //services.bookingType = "Incall";
                        isInCallSelected = true;
                        intent.putExtra("callType", "In Call");
                    } else if (Double.parseDouble(tagDetail.outcallPrice) != 0.0) {
                        isOutCallSelected = true;
                        //services.bookingType = "Outcall";
                        intent.putExtra("callType", "Out Call");
                    }


                    if (tagDetail.staffId == null) {
                        tagDetail.staffId = "0";
                    }
                    intent.putExtra("staffId", Integer.parseInt(tagDetail.staffId));
                    intent.putExtra("isFromServiceTag", true);
                    intent.putExtra("incallStaff", isInCallSelected);
                    intent.putExtra("outcallStaff", isOutCallSelected);
                    //intent.putExtra("bookingDate", item.date);
                    mContext.startActivity(intent);
                }}
        }
    }

}

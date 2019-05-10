package com.mualab.org.user.activity.searchBoard.adapter;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.os.SystemClock;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.Util;
import com.squareup.picasso.Picasso;

import java.util.List;


public class SearchBoardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<ArtistsSearchBoard> artistsList;
    private Util utility;
    private boolean showLoader;
    private  long mLastClickTime = 0;

    private  final int VIEWTYPE_ITEM = 1;
    private  final int VIEWTYPE_LOADER = 2;
    private  getClick getClickListner;
    // Constructor of the class
    public SearchBoardAdapter(Context context, List<ArtistsSearchBoard> artistsList,getClick getClickListner) {
        this.context = context;
        this.artistsList = artistsList;
        this.getClickListner = getClickListner;
        utility = new Util(context);
    }

    @Override
    public int getItemCount() {
        return artistsList.size();
    }

    public void showLoading(boolean status) {
        showLoader = status;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        switch (viewType) {
            case VIEWTYPE_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.searchboard_item_layout, parent, false);
                return new ViewHolder(view);

            case VIEWTYPE_LOADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new LoadingViewHolder(view);
        }
        return null;

    }

    @Override
    public int getItemViewType(int position) {
        if(position == artistsList.size()){
            return showLoader?VIEWTYPE_LOADER:VIEWTYPE_ITEM;
        }
        return VIEWTYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof LoadingViewHolder) {
            LoadingViewHolder loaderViewHolder = (LoadingViewHolder) viewHolder;
            if (showLoader) {
                loaderViewHolder.progressBar.setVisibility(View.GONE);
            } else {
                loaderViewHolder.progressBar.setVisibility(View.GONE);
            }
            return;
        }

        final ViewHolder holder = ((ViewHolder) viewHolder);
        final ArtistsSearchBoard item = artistsList.get(position);

        holder.tvArtistName.setText(item.userName);
        holder.rating.setRating(Float.parseFloat(item.ratingCount));

        try {
            double d = Double.parseDouble(item.distance);
            item.distance = String.format("%.2f", d);
            holder.tvDistance.setText(String.format("%s miles", item.distance));
        }catch (NumberFormatException e){

        }

        long rCount = Long.parseLong(item.reviewCount);

        holder.tvRating.setText(String.format("(%s)", utility.roundRatingWithSuffix(rCount)));


        if (!item.profileImage.equals("")){
        Picasso.with(context).load(item.profileImage).placeholder(R.drawable.default_placeholder).fit().into(holder.ivProfile);
        }else {
            holder.ivProfile.setImageDrawable(context.getResources().getDrawable(R.drawable.default_placeholder));
        }

        if (item.isFav)
            holder.ivFav.setVisibility(View.VISIBLE);
        else
            holder.ivFav.setVisibility(View.GONE);
        if (item.categoryName.contains(",")){
            String []category=item.categoryName.split(",");
            if (!(category.length ==0)){
                String name = category[0];
                String namenew = category[1];

                if (namenew.equals("")){
                    holder.newText.setVisibility(View.GONE);
                    holder.tvServicesnew.setVisibility(View.GONE);
                    holder.tvServices.setText("");

                }else {
                    holder.newText.setVisibility(View.VISIBLE);
                    holder.tvServicesnew.setVisibility(View.VISIBLE);
                    holder.tvServicesnew.setText(namenew);
                    holder.tvServices.setText(name);
                }
            }
        }else {
            holder.tvServicesnew.setVisibility(View.GONE);
            holder.tvServices.setText(item.categoryName);
        }

       /* holder.rlServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getClickListner.ClickFirstTag(item);
            }
        });

        holder.newText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getClickListner.ClickSecandTag(item);
            }
        });*/


    }

    public interface getClick {
        void bookData(ArtistsSearchBoard item);
        void ClickFirstTag(ArtistsSearchBoard item);
        void ClickSecandTag(ArtistsSearchBoard item);
    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvDistance,tvServices,tvArtistName,tvRating,tvServicesnew;
        ImageView ivProfile,ivFav;
        RelativeLayout rlServices,newText;
        AppCompatButton btnBook;
        RatingBar rating;
        RelativeLayout lyContainer;
        private ViewHolder(View itemView)
        {
            super(itemView);

            ivProfile = itemView.findViewById(R.id.ivProfile);
            ivFav = itemView.findViewById(R.id.ivFav);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvArtistName = itemView.findViewById(R.id.tvArtistName);
            tvServices = itemView.findViewById(R.id.tvServices);
            tvServicesnew = itemView.findViewById(R.id.tvServicesnew);
            newText = itemView.findViewById(R.id.newText);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnBook = itemView.findViewById(R.id.btnBook);
            rating = itemView.findViewById(R.id.rating);
            lyContainer = itemView.findViewById(R.id.lyContainer);
            rlServices = itemView.findViewById(R.id.rlServices);

            btnBook.setOnClickListener(this);
            //  ivProfile.setOnClickListener(this);
            lyContainer.setOnClickListener(this);
            ivProfile.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            KeyboardUtil.hideKeyboard(v,context);

            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            ArtistsSearchBoard item = artistsList.get(getAdapterPosition());
            switch (v.getId()){
                case R.id.btnBook:
                    getClickListner.bookData(item);
                    break;

                case R.id.ivProfile:
                    Intent intent2 = new Intent(context, ArtistProfileActivity.class);
                    intent2.putExtra("item",item);
                    intent2.putExtra("artistId",item._id);
                    context.startActivity(intent2);
                    break;
                case R.id.lyContainer:
                   /* Intent intent3 = new Intent(context, ArtistProfileActivity.class);
                    intent3.putExtra("item",item);
                    intent3.putExtra("artistId",item._id);
                    context.startActivity(intent3);*/
                    break;
            }
        }
    }

}
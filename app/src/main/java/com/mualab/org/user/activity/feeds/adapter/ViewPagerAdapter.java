package com.mualab.org.user.activity.feeds.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ablanco.zoomy.DoubleTapListener;
import com.ablanco.zoomy.LongPressListener;
import com.ablanco.zoomy.TapListener;
import com.ablanco.zoomy.Zoomy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.businessInvitaion.activity.ServiceDetailActivity;
import com.mualab.org.user.activity.businessInvitaion.model.Services;
import com.mualab.org.user.activity.feeds.listener.OnImageSwipeListener;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
import com.mualab.org.user.activity.tag_module.instatag.InstaTag;
import com.mualab.org.user.activity.tag_module.instatag.TagDetail;
import com.mualab.org.user.activity.tag_module.instatag.TagToBeTagged;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.listener.OnDoubleTapListener;
import com.mualab.org.user.utils.ScreenUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewPagerAdapter extends PagerAdapter implements OnImageSwipeListener {

    private LayoutInflater mLayoutInflater;
    private Context context;
    private List<String> ImagesList;
    private HashMap<Integer, ArrayList<TagToBeTagged>> taggedImgMap;
    private HashMap<Integer, ArrayList<TagToBeTagged>> serviceTaggedImgMap;
    private Listner listner;
    //private Feeds feeds;
    private ViewGroup container;
    private boolean isShow, isFromFeed;
    InstaTag mInstaTag = null;
    private InstaTag.TaggedImageEvent taggedImageEvent = new InstaTag.TaggedImageEvent() {



        @Override
        public void singleTapConfirmedAndRootIsInTouch(float x, float y) {
/* if (listner != null)
listner.onSingleTap();*/
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (listner != null)
                listner.onDoubleTap();
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
// View view = container.getRootView();
            ViewPager viewPager = (ViewPager) container;
            View view = viewPager.findViewWithTag("myview" + viewPager.getCurrentItem());

            if (view != null) {
                mInstaTag = view.findViewById(R.id.post_image);
            }
            if (mInstaTag != null) {
                showHideTag(mInstaTag, viewPager.getCurrentItem());
            }
        }

        @Override
        public void onSinglePress(MotionEvent e) {
            if (listner != null)
                listner.onSingleTap();
        }
    };

    private void showHideTag(InstaTag mInstaTag, int pos) {
//people tag
        if (taggedImgMap.containsKey(pos)) {
            ArrayList<TagToBeTagged> tags = taggedImgMap.get(pos);
            if (!mInstaTag.isTagsShow("people")) {
                mInstaTag.addTagViewFromTagsToBeTagged("people", tags, false);
                mInstaTag.showTags("people");
                isShow = true;
            } else {
                mInstaTag.hideTags("people");
                isShow = false;
            }
        }

//service tag
        if (serviceTaggedImgMap.containsKey(pos)) {
            ArrayList<TagToBeTagged> tags = serviceTaggedImgMap.get(pos);
            if (!mInstaTag.isTagsShow("service")) {
                mInstaTag.addTagViewFromTagsToBeTagged("service", tags, false);
                mInstaTag.showTags("service");
                isShow = true;
            } else {
                mInstaTag.hideTags("service");
                isShow = false;
            }

        }
    }

    @Override
    public int getCount() {
        return ImagesList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    public ViewPagerAdapter(Context context, Feeds feeds, boolean isFromFeed, Listner listner) {
        this.context = context;
// this.feeds = feeds;
        this.isFromFeed = isFromFeed;
        this.ImagesList = feeds.feed;
        this.taggedImgMap = feeds.taggedImgMap;
        this.serviceTaggedImgMap = feeds.serviceTaggedImgMap;
        this.listner = listner;
        this.isShow = false;
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_layout, container, false);
        final InstaTag postImages = itemView.findViewById(R.id.post_image);
        RelativeLayout rlShowTag = itemView.findViewById(R.id.lyShowTag);

        postImages.setImageToBeTaggedEvent(taggedImageEvent);
        this.container = container;

        this.isShow = false;

        postImages.getTagImageView().setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(context).load(ImagesList.get(position)).
                placeholder(R.drawable.gallery_placeholder).into(postImages.getTagImageView());


        Zoomy.Builder zoomingBuilder = new Zoomy.Builder((Activity)context)
                .target(postImages.getTagImageView())
                .tapListener(new TapListener() {
                    @Override
                    public void onTap(View v) {
                        if (listner != null)
                            listner.onSingleTap();
                    }
                })
                .longPressListener(new LongPressListener() {
                    @Override
                    public void onLongPress(View v) {
// View view = container.getRootView();
                        ViewPager viewPager = (ViewPager) container;
                        View view = viewPager.findViewWithTag("myview" + viewPager.getCurrentItem());

                        if (view != null) {
                            mInstaTag = view.findViewById(R.id.post_image);
                        }
                        if (mInstaTag != null) {
                            showHideTag(mInstaTag, viewPager.getCurrentItem());
                        }
                    }
                }).doubleTapListener(new DoubleTapListener() {
                    @Override
                    public void onDoubleTap(View v) {
                        if (listner != null)
                            listner.onDoubleTap();
                    }
                }).enableImmersiveMode(false)
                .animateZooming(false);
        zoomingBuilder.register();

        ArrayList<TagToBeTagged> tempPeople = taggedImgMap.get(position);
        ArrayList<TagToBeTagged> tempService = serviceTaggedImgMap.get(position);

        if ((tempPeople != null && tempPeople.size() != 0) || (tempService != null && tempService.size() != 0 &&serviceTaggedImgMap.size() != 0)) {
            rlShowTag.setVisibility(View.VISIBLE);
        } else
            rlShowTag.setVisibility(View.GONE);

        rlShowTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*if (isShow) {
postImages.hideTags();
isShow = false;
}
else {
postImages.showTags();
isShow = true;
}*/
                showHideTag(postImages, position);
            }
        });


        postImages.setListener(new InstaTag.Listener() {

            @Override
            public void onTagCliked(final TagDetail tagDetail) {
// if (isFromFeed) {
                if (tagDetail.tabType.equalsIgnoreCase("people")) {
                    if (tagDetail.userType != null && !tagDetail.userType.isEmpty()) {
                        if (tagDetail.tagId != null && !tagDetail.tagId.isEmpty()) {
                            if (!tagDetail.tagId.equals(String.valueOf(Mualab.currentUser.id))) {
                                if (tagDetail.userType.equals("user")) {
                                    Intent intent = new Intent(context, UserProfileActivity.class);
                                    intent.putExtra("userId", tagDetail.tagId);
                                    context.startActivity(intent);
                                } else {
                                    Intent intent = new Intent(context, ArtistProfileActivity.class);
                                    intent.putExtra("artistId", tagDetail.tagId);
                                    context.startActivity(intent);
                                }
                            }
                        } else apiForgetUserIdFromUserName(tagDetail.title);
                    } else apiForgetUserIdFromUserName(tagDetail.title);
                } else {

                    //service tag
                    Intent intent = new Intent(context, BookingActivity.class);

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
                        context.startActivity(intent);


                    }}

//}
            }

            @Override
            public void onTagRemoved(TagDetail tagDetail) {
            }
        });

// }
        itemView.setTag("myview" + position);
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }

    private void apiForgetUserIdFromUserName(final CharSequence userName) {
        String user_name;

        final Map<String, String> params = new HashMap<>();
        if (userName.toString().startsWith("@")) {
            user_name = userName.toString().replace("@", "");
            params.put("userName", user_name + "");
        } else params.put("userName", userName + "");
        new HttpTask(new HttpTask.Builder(context, "profileByUserName", new HttpResponceListner.Listener() {
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
                            Intent intent = new Intent(context, UserProfileActivity.class);
                            intent.putExtra("userId", String.valueOf(userId));
                            context.startActivity(intent);
                        } else if (userType.equals("artist") && userId == Mualab.currentUser.id) {
                            Intent intent = new Intent(context, ArtistProfileActivity.class);
                            intent.putExtra("artistId", String.valueOf(userId));
                            context.startActivity(intent);
                        } else {
                            Intent intent = new Intent(context, ArtistProfileActivity.class);
                            intent.putExtra("artistId", String.valueOf(userId));
                            context.startActivity(intent);
                        }
                    } else {
                        MyToast.getInstance(context).showDasuAlert(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
            }
        }).setAuthToken(Mualab.currentUser.authToken)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON)
                .setMethod(Request.Method.POST)
                .setProgress(true))
                .execute("ArtistFeedAdapter");


    }

    @Override
    public void OnImageSwipe(int position) {
        isShow = false;
    }

    public interface Listner {
        void onSingleTap();

        void onDoubleTap();
    }

    public interface LongPressListner {
        void onLongPress();
    }

    private class MyOnDoubleTapListener extends OnDoubleTapListener {
        private MyOnDoubleTapListener(Context c) {
            super(c);
        }

        @Override
        public void onClickEvent(MotionEvent e) {
            if (listner != null)
                listner.onSingleTap();
        }

        @Override
        public void onDoubleTap(MotionEvent e) {
            if (listner != null)
                listner.onDoubleTap();
        }
    }
}
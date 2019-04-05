package com.mualab.org.user.activity.tag_module.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.base.BaseActivity;
import com.mualab.org.user.activity.tag_module.adapters.ServiceAdapter;
import com.mualab.org.user.activity.tag_module.adapters.TagListAdapter;
import com.mualab.org.user.activity.tag_module.callback.RemoveDuplicateTagListener;
import com.mualab.org.user.activity.tag_module.callback.ServiceClickListener;
import com.mualab.org.user.activity.tag_module.callback.TagListClickListener;
import com.mualab.org.user.activity.tag_module.instatag.InstaTag;
import com.mualab.org.user.activity.tag_module.instatag.TagDetail;
import com.mualab.org.user.activity.tag_module.instatag.TagImageView;
import com.mualab.org.user.activity.tag_module.instatag.TagToBeTagged;
import com.mualab.org.user.activity.tag_module.models.ServiceTagBean;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.utils.Helper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceTagActivity extends BaseActivity implements View.OnClickListener {
    public static HashMap<Integer, ArrayList<TagToBeTagged>> taggedImgMap = new HashMap<>();
    private RecyclerView mRecyclerViewSomeOneToBeTagged;
    private LinearLayout mHeaderSearchSomeOne, llSearchPeople;
    /* private RelativeLayout mHeaderSomeOneToBeTagged; */

    //Todo hs
    private float mAddTagInX, mAddTagInY;
    private ServiceAdapter serviceAdapter;
    private List<String> images;
    private int startIndex, currentIndex;
    private LinearLayout ll_Dot;
    private ViewPager viewPager;
    private ViewPagerAdapterForTag viewPagerAdapter;
    private CardView mHeaderSomeOneToBeTagged;
    final InstaTag.TaggedImageEvent taggedImageEvent = new InstaTag.TaggedImageEvent() {
        @Override
        public void singleTapConfirmedAndRootIsInTouch(final float x, final float y) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (serviceTagList.size() == 0)
                        callSearchServiceAPI("", 0);

                    //Todo hs
                    Log.e("Step2 X,Y - ", x + "," + y);
                    mAddTagInX = x;
                    mAddTagInY = y;
                    mHeaderSearchSomeOne.setVisibility(View.VISIBLE);
                    mRecyclerViewSomeOneToBeTagged.setVisibility(View.VISIBLE);
                    llSearchPeople.setVisibility(View.VISIBLE);
                    mHeaderSomeOneToBeTagged.setVisibility(View.GONE);

                }
            });
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public void onSinglePress(MotionEvent e) {

        }
    };
    private List<ServiceTagBean> serviceTagList;
    private LinearLayout ll_loadingBox;
    private ProgressBar progress_bar;
    private TextView tv_msg;
    private String searchKeyword = "";
    private long mLastClickTime = 0;
    private TagListAdapter tagListAdapter;
    private ArrayList<TagToBeTagged> taggedArrayList;
    private boolean isFullImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* StatusBarUtil.setColorNoTranslucent(this, getResources().getColor(R.color.colorPrimary)); */

        setContentView(R.layout.activity_service_tag);
        serviceTagList = new ArrayList<>();

        Intent intent = getIntent();
        if (intent != null) {
            startIndex = intent.getIntExtra("startIndex", 0);
            images = (ArrayList<String>) intent.getSerializableExtra("imageArray");
            isFullImage = intent.getBooleanExtra("imageType", true);
            /* taggedImgMap = (HashMap<Integer, ArrayList<TagToBeTagged>>) intent.getSerializableExtra("hashmap"); */
        }

        //int widthPixels = ScreenUtils.getScreenWidth(ServiceTagActivity.this);
        //widthPixels = widthPixels >= 1080 ? 1080 : widthPixels;

        ll_loadingBox = findViewById(R.id.ll_loadingBox);
        progress_bar = findViewById(R.id.progress_bar);
        tv_msg = findViewById(R.id.tv_msg);
        ll_Dot = findViewById(R.id.ll_Dot);
        //  topLayout = findViewById(R.id.topLayout);

        TagImageView imgBack = findViewById(R.id.imgBack);

        // final TextView cancelTextView = findViewById(R.id.cancel);
        final TagImageView doneImageView = findViewById(R.id.done);
        final TextView tvDone = findViewById(R.id.tvDone);
        final TagImageView backImageView = findViewById(R.id.get_back);

        mRecyclerViewSomeOneToBeTagged = findViewById(R.id.rv_some_one_to_be_tagged);
        //  mTapPhotoToTagSomeOneTextView = findViewById(R.id.tap_photo_to_tag_someone);
        mHeaderSomeOneToBeTagged = findViewById(R.id.header_tag_photo);
        mHeaderSearchSomeOne = findViewById(R.id.header_search_someone);
        llSearchPeople = findViewById(R.id.llSearchPeople);
        SearchView searchview = findViewById(R.id.searchview);

        RecyclerView rycTags = findViewById(R.id.rycTags);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        rycTags.setLayoutManager(layoutManager);
        taggedArrayList = new ArrayList<>();
        tagListAdapter = new TagListAdapter(ServiceTagActivity.this, taggedArrayList, taggedImgMap);

        rycTags.setAdapter(tagListAdapter);

        if (taggedImgMap.size() != 0) {
            taggedArrayList.clear();
            ArrayList<TagToBeTagged> taggeds = taggedImgMap.get(startIndex);
            if (taggeds != null)
                taggedArrayList.addAll(taggeds);
        }

        viewPager = findViewById(R.id.viewpager);
        viewPagerAdapter = new ViewPagerAdapterForTag(this, images, taggedArrayList);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(startIndex);

        //  viewPager.setOffscreenPageLimit(10);

        if (images.size() > 1) {
            addBottomDots(ll_Dot, images.size(), startIndex);
            ll_Dot.setVisibility(View.VISIBLE);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(final int position) {
                    taggedArrayList.clear();
                    tagListAdapter.notifyDataSetChanged();

                    currentIndex = position;

                    if (taggedImgMap.size() != 0) {
                        if (taggedImgMap.containsKey(position)) {
                            ArrayList<TagToBeTagged> taggs = taggedImgMap.get(position);
                            assert taggs != null;
                            taggedArrayList.addAll(taggs);
                            tagListAdapter.notifyDataSetChanged();
                        }
                    }

                    addBottomDots(ll_Dot, images.size(), position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    System.out.println("state===" + state);
                }
            });

        } else ll_Dot.setVisibility(View.GONE);

        doneImageView.setOnClickListener(this);
        backImageView.setOnClickListener(this);
        tvDone.setOnClickListener(this);
        imgBack.setOnClickListener(this);

        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchKeyword = newText;
                serviceTagList.clear();
                serviceAdapter.notifyDataSetChanged();
                callSearchServiceAPI(newText, 0);
                return false;
            }
        });

        serviceAdapter = new ServiceAdapter(serviceTagList);
        mRecyclerViewSomeOneToBeTagged.setAdapter(serviceAdapter);
        LinearLayoutManager lm = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mRecyclerViewSomeOneToBeTagged.setLayoutManager(lm);

        if (serviceTagList.size() == 0)
            callSearchServiceAPI("", 0);
    }

    private void addBottomDots(LinearLayout ll_dots, int totalSize, int currentPage) {
        TextView[] dots = new TextView[totalSize];
        ll_dots.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("•"));
            dots[i].setTextSize(25);
            dots[i].setTextColor(Color.parseColor("#999999"));
            ll_dots.addView(dots[i]);
        }
        if (dots.length > 0)
            dots[currentPage].setTextColor(Color.parseColor("#212121"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ll_Dot = null;
        viewPager.destroyDrawingCache();
        viewPager = null;
        viewPagerAdapter = null;
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        switch (v.getId()) {
            case R.id.tvDone:

                ArrayList<TagToBeTagged> tags = new ArrayList<>();

                for (Map.Entry map : taggedImgMap.entrySet()) {
                    int i = (int) map.getKey();
                    tags.addAll(taggedImgMap.get(i));
                }

                List<ArrayList<TagToBeTagged>> listOfValues2 = new ArrayList<>();

                for (int i = 0; i < images.size(); i++) {
                    if (taggedImgMap.size() != 0) {
                        if (taggedImgMap.containsKey(i)) {
                            ArrayList<TagToBeTagged> taggeds = taggedImgMap.get(i);
                            listOfValues2.add(taggeds);
                        } else {
                            ArrayList<TagToBeTagged> taggeds = new ArrayList<>();
                            listOfValues2.add(taggeds);
                        }
                    }
                }

                //   if (listOfValues2.size()!=0) {

                //  if (tags.size()!=0){
                Gson gson = new GsonBuilder().create();
                String jsonArray = gson.toJson(listOfValues2);
                Intent intent = new Intent();
                intent.putExtra("tagJson", jsonArray);
                intent.putExtra("listOfValues", (Serializable) listOfValues2);
                intent.putExtra("tagCount", String.valueOf(tags.size()));
                setResult(RESULT_OK, intent);
                finish();

                //}
                //   }

                break;
            case R.id.get_back:
                onBackPressed();
                break;

            case R.id.imgBack:
                mRecyclerViewSomeOneToBeTagged.setVisibility(View.GONE);
                mHeaderSearchSomeOne.setVisibility(View.GONE);
                llSearchPeople.setVisibility(View.GONE);
                mHeaderSomeOneToBeTagged.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void callSearchServiceAPI(final String searchKeyWord, int pageNo) {
        User user = Mualab.getInstance().getSessionManager().getUser();
        Map<String, String> params = new HashMap<>();
        params.put("artistId", String.valueOf(user.id));
        params.put("search", searchKeyWord);
        //String tag = TAG + exSearchType;
        Mualab.getInstance().cancelPendingRequests("artistServices");
        new HttpTask(new HttpTask.Builder(ServiceTagActivity.this, "artistServices", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    progress_bar.setVisibility(View.GONE);
                    serviceAdapter.showHideLoading(false);
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    //String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {

                        Gson gson = new Gson();
                        JSONArray array = js.getJSONArray("artistServices");

                        if (array != null) {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                ServiceTagBean serviceTagBean = gson.fromJson(String.valueOf(jsonObject), ServiceTagBean.class);

                                serviceTagList.add(serviceTagBean);
                            }
                        }
                        serviceAdapter.notifyDataSetChanged();
                    }
                    tv_msg.setVisibility(View.GONE);
                    if (serviceTagList.size() == 0) {
                        tv_msg.setText(getString(R.string.no_data_available));
                    } else {
                        ll_loadingBox.setVisibility(View.GONE);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    progress_bar.setVisibility(View.GONE);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                progress_bar.setVisibility(View.GONE);
                serviceAdapter.showHideLoading(false);
                try {
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")) {
                        Mualab.getInstance().getSessionManager().logout();
                        //      MyToast.getInstance(BookingActivity.this).showSmallCustomToast(helper.error_Messages(error));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).setAuthToken(user.authToken)
                .setParam(params)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute("artistServices");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        taggedImgMap.clear();
        taggedArrayList.clear();
        Intent intent = new Intent();
        intent.putExtra("tagJson", "");
        intent.putExtra("tagCount", "0");
        setResult(RESULT_OK, intent);
        finish();

    }

    class ViewPagerAdapterForTag extends PagerAdapter implements
            TagListClickListener, RemoveDuplicateTagListener, ServiceClickListener {

        LayoutInflater mLayoutInflater;
        Context context;
        List<String> imagesList;
        ArrayList<TagToBeTagged> tags;
        InstaTag mInstaTag = null;

        private ViewPagerAdapterForTag(Context context, List<String> imagesList, ArrayList<TagToBeTagged> tags) {
            this.context = context;
            this.imagesList = imagesList;
            this.tags = tags;
            this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return imagesList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {

            View itemView = mLayoutInflater.inflate(R.layout.viewpager_taging_layout, container, false);
            //final ProgressBar progress_bar = itemView.findViewById(R.id.progress_bar);
            //     final ImageView photoView = itemView.findViewById(R.id.photo_view);
            final InstaTag mInstaTag = itemView.findViewById(R.id.insta_tag);

           /* mInstaTag.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            mInstaTag.setRootWidth(mInstaTag.getMeasuredWidth());
            mInstaTag.setRootHeight(mInstaTag.getMeasuredHeight());*/

            if (isFullImage) {
                mInstaTag.getTagImageView().setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else mInstaTag.getTagImageView().setScaleType(ImageView.ScaleType.FIT_CENTER);

            mInstaTag.setImageToBeTaggedEvent(taggedImageEvent);

            mInstaTag.setRemoveDuplicateTagListener(this);

            serviceAdapter.setListener(this);
            itemView.setTag("myview" + position);
            container.addView(itemView);

            tagListAdapter.setCustomListener(this);
/*
            Picasso.with(context).load(imagesList.get(position)).resize(widthPixels,
                    320).centerInside().
                    into(mInstaTag.getTagImageView());*/

            //Glide.with(context).load(imagesList.get(position)).placeholder(R.drawable.ic_gallery_placeholder).into(mInstaTag.getTagImageView());

            Glide.with(context).load(imagesList.get(position)).
                    placeholder(R.drawable.gallery_placeholder).into(mInstaTag.getTagImageView());


            if (taggedImgMap.size() != 0) {
                if (taggedImgMap.containsKey(position)) {
                    ArrayList<TagToBeTagged> tags = taggedImgMap.get(position);
                    mInstaTag.addTagViewFromTagsToBeTagged("people", tags, false);
                    mInstaTag.showTags("people");
                }
            }

            return itemView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        // RecyclerView rycTags;

        @Override
        public void onItemRemoveClick(int position, TagToBeTagged tag) {
            View view = viewPager.findViewWithTag("myview" + viewPager.getCurrentItem());

            if (view != null) {
                mInstaTag = view.findViewById(R.id.insta_tag);
            }
            if (mInstaTag != null) {

                mInstaTag.removeSingleTags(tag);

                taggedArrayList.remove(position);
                tagListAdapter.notifyDataSetChanged();

                ArrayList<TagToBeTagged> newtags = taggedImgMap.get(currentIndex);
                assert newtags != null;
                newtags.remove(tag);

                taggedImgMap.put(currentIndex, newtags);
            }

        }

        @Override
        public void onDuplicateTagRemoved(final TagToBeTagged tag) {

            if (taggedArrayList != null && taggedArrayList.size() != 0) {
                for (int i = 0; i < taggedArrayList.size(); i++) {
                    if (taggedArrayList.get(i).getUnique_tag_id().equals(tag.getUnique_tag_id())) {
                        taggedArrayList.remove(i);
                        tagListAdapter.notifyDataSetChanged();
                        break;
                    }
                }

                ArrayList<TagToBeTagged> newtags = taggedImgMap.get(currentIndex);
                assert newtags != null;
                newtags.remove(tag);

                taggedImgMap.put(currentIndex, newtags);
            }
        }

        @Override
        public void onServiceClicked(ServiceTagBean serviceTagBean, int position) {
            hideKeyboard();
            View view = viewPager.findViewWithTag("myview" + viewPager.getCurrentItem());

            if (view != null) {
                mInstaTag = view.findViewById(R.id.insta_tag);
                // rycTags = view.findViewById(R.id.rycTags);
            }
            if (mInstaTag != null) {

                final TagDetail tag = new TagDetail("service", String.valueOf(serviceTagBean._id),
                        serviceTagBean.title, "artist", serviceTagBean.inCallPrice, serviceTagBean.description, serviceTagBean.businessType.get(0).title,
                        String.valueOf(serviceTagBean.businessType.get(0)._id), String.valueOf(serviceTagBean.category.get(0)._id), String.valueOf(serviceTagBean.artistId),
                        serviceTagBean.completionTime, serviceTagBean.outCallPrice, serviceTagBean.category.get(0).title);


                      /*  if (taggedArrayList!=null && taggedArrayList.size()!=0){
                            for (int i=0;i<taggedArrayList.size();i++){
                                if (taggedArrayList.get(i).getUnique_tag_id().equals(someOne.title)){
                                    taggedArrayList.remove(i);
                                    tagListAdapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                        }*/


                mInstaTag.addTag(mAddTagInX, mAddTagInY, serviceTagBean.title, tag);

                taggedArrayList.clear();
                tagListAdapter.notifyDataSetChanged();

                new Handler().postDelayed(() -> {
                    mRecyclerViewSomeOneToBeTagged.setVisibility(View.GONE);
                    mHeaderSearchSomeOne.setVisibility(View.GONE);
                    llSearchPeople.setVisibility(View.GONE);
                    mHeaderSomeOneToBeTagged.setVisibility(View.VISIBLE);

                    ArrayList<TagToBeTagged> tagsToBeTagged = new
                            ArrayList<>(mInstaTag.getListOfTagsToBeTagged());
                    //tagHashmap.put(tagDetail.title, tagDetail);
                    taggedImgMap.put(viewPager.getCurrentItem(), tagsToBeTagged);

                    taggedArrayList.addAll(tagsToBeTagged);
                    tagListAdapter.notifyDataSetChanged();

                }, 300);
            }
        }
    }
}
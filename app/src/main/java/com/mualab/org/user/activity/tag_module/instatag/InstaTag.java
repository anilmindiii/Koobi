/*
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mualab.org.user.activity.tag_module.instatag;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RotateDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.tag_module.callback.RemoveDuplicateTagListener;
import com.mualab.org.user.activity.tag_module.models.TagViewBean;
import com.mualab.org.user.utils.ArrowView;
import com.viven.imagezoom.ImageZoomHelper;
import java.util.ArrayList;
import java.util.HashMap;


public class InstaTag extends RelativeLayout {

    private final ArrayList<TagViewBean> mTagList = new ArrayList<>();
    private int mPosX;
    private int mPosY;
    private static int mRootWidth;
    private static int mRootHeight;
    private Context mContext;
    private int mTagTextColor;
    private int mTagBackgroundColor;
    private int mCarrotTopBackGroundColor;
    private int mCarrotLeftBackGroundColor;
    private int mCarrotRightBackGroundColor;
    private int mCarrotBottomBackGroundColor;
    private Drawable mTagTextDrawable;
    private Drawable mCarrotTopDrawable;
    private Drawable mCarrotLeftDrawable;
    private Drawable mCarrotRightDrawable;
    private Drawable mCarrotBottomDrawable;

    private final TagGestureListener mTagGestureListener = new TagGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (canWeAddTags) {
                if (mIsRootIsInTouch) {

                    //Todo hs
                    //int x = (int) e.getX();
                    //int y = (int) e.getY();
                    float x = (e.getX() * 100) / mRootWidth;   //left
                    float y = (e.getY() * 100) / mRootHeight;  //top
                    //float y = (e.getY() * 100) / (mRootHeight*4/3);  //top

                    // AppLogger.e("Step1 X,Y - ", e.getX() + "," + e.getY() + " = " + mRootWidth + " " + mRootHeight);
                    // AppLogger.e("Step1 X,Y - ", x + "," + y);

                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_MOVE:
                        case MotionEvent.ACTION_UP:
                    }
                    if (mTaggedImageEvent != null) {
                        mTaggedImageEvent.singleTapConfirmedAndRootIsInTouch(x, y);
                    }
                } else {
                    hideRemoveButtonFromAllTagView();
                    mIsRootIsInTouch = true;
                }
            } else {
                if (mTaggedImageEvent != null) {
                    mTaggedImageEvent.onSinglePress(e);
                }
            }
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mTaggedImageEvent != null) {
                return mTaggedImageEvent.onDoubleTap(e);
            } else {
                return true;
            }
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (mTaggedImageEvent != null) {
                return mTaggedImageEvent.onDoubleTapEvent(e);
            } else {
                return true;
            }
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mTaggedImageEvent != null) {
                mTaggedImageEvent.onLongPress(e);
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }
    };
    private boolean peopleTagsAreAdded;
    private boolean isAddingTag = true;
    private boolean canWeAddTags;
    private boolean showAllCarrots;
    private final Runnable mSetRootHeightWidth = new Runnable() {
        @Override
        public void run() {
            int tempW = mTagImageView.getWidth();
            int tempH = mTagImageView.getHeight();
            //int tempH = tempW * 3 / 4;
            if (tempH != 0 && tempW != 0) {
                mRootHeight = tempH;
                mRootWidth = tempW;

                //mTagImageView.measure(mRootWidth, mRootHeight);

                //mTagImageView.setLayoutParams();

                //int width = ScreenUtils.getScreenWidth(context);
                //int height = width*3/4;
            }
            // AppLogger.e("StepWH", mRootWidth + " " + mRootHeight);
            // AppLogger.e("StepWH", tempW + " " + tempH);
        }
    };
    private boolean isShowPeopleTag = false;
    private boolean mIsRootIsInTouch = true;
    private Animation mShowAnimation;
    private Animation mHideAnimation;
    private GestureDetector mGestureDetector;
    private final OnTouchListener mTagOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return mGestureDetector.onTouchEvent(event);
        }
    };
    private TaggedImageEvent mTaggedImageEvent;
    private boolean serviceTagsAreAdded;
    // private AspectRatioImageView mTagImageView;
    private ViewGroup mRoot;
    private ImageView mLikeImage;
    private TagImageView mTagImageView;
    private boolean isShowServiceTag = false;
    private HashMap<String, TagDetail> tagMap = new HashMap<>();
    private Listener listener;
    private RemoveDuplicateTagListener removeDuplicateTagListener;

    public InstaTag(Context context) {
        super(context);
        initViewWithId(context, null);
    }

    public InstaTag(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            initView(attrs, context);
        } else {
            initView(attrs, context);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setRemoveDuplicateTagListener(RemoveDuplicateTagListener removeDuplicateTagListener) {
        this.removeDuplicateTagListener = removeDuplicateTagListener;
    }

    private void initViewWithId(Context context, TypedArray obtainStyledAttributes) {
        mContext = context;

        LayoutInflater inflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            inflater.inflate(R.layout.tag_root, this);
        }

        mRoot = findViewById(R.id.tag_root);
        mTagImageView = findViewById(R.id.tag_image_view);

        mLikeImage = new ImageView(context);
        int likeColor, likeSrc, likeSize;
        if (obtainStyledAttributes != null) {
            likeColor = obtainStyledAttributes.getColor(R.styleable.InstaTag_likeColor,
                    ContextCompat.getColor(context, R.color.colorAccent));
            likeSrc = obtainStyledAttributes.getResourceId(R.styleable.InstaTag_likeSrc,
                    R.drawable.like_icon);
            likeSize = obtainStyledAttributes
                    .getDimensionPixelSize(R.styleable.InstaTag_likeSize,
                            getResources().getDimensionPixelSize(R.dimen.dp256));
        } else {
            likeColor = ContextCompat.getColor(context, R.color.colorAccent);
            likeSrc = R.drawable.like_icon;
            likeSize = getResources().getDimensionPixelSize(R.dimen.dp256);
        }
        LayoutParams heartParams = new LayoutParams(likeSize, likeSize);
        heartParams.addRule(CENTER_IN_PARENT, TRUE);

        mLikeImage.setLayoutParams(heartParams);
        mLikeImage.setVisibility(GONE);
        mLikeImage.setImageResource(likeSrc);
        mLikeImage.setColorFilter(likeColor);

        setLayoutParamsToBeSetForRootLayout(mContext);
        mRoot.post(mSetRootHeightWidth);
        mTagImageView.setOnTouchListener(mTagOnTouchListener);
        mGestureDetector = new GestureDetector(mRoot.getContext(), mTagGestureListener);
    }

    private void initView(AttributeSet attrs, Context context) {
        mContext = context;

        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attrs,
                R.styleable.InstaTag, 0, 0);

        mTagTextDrawable = obtainStyledAttributes.
                getDrawable(R.styleable.InstaTag_tagTextBackground);
        mCarrotTopDrawable = obtainStyledAttributes.
                getDrawable(R.styleable.InstaTag_carrotTopBackground);
        mCarrotLeftDrawable = obtainStyledAttributes.
                getDrawable(R.styleable.InstaTag_carrotLeftBackground);
        mCarrotRightDrawable = obtainStyledAttributes.
                getDrawable(R.styleable.InstaTag_carrotRightBackground);
        mCarrotBottomDrawable = obtainStyledAttributes.
                getDrawable(R.styleable.InstaTag_carrotBottomBackground);

        canWeAddTags = obtainStyledAttributes.
                getBoolean(R.styleable.InstaTag_canWeAddTags, false);
        showAllCarrots = obtainStyledAttributes.
                getBoolean(R.styleable.InstaTag_showAllCarrots, false);

        mTagTextColor = obtainStyledAttributes.
                getColor(R.styleable.InstaTag_instaTextColor, Constants.TAG_TEXT_COLOR);

        int overrideDefaultColor = obtainStyledAttributes.
                getColor(R.styleable.InstaTag_overrideDefaultColor, Constants.DEFAULT_COLOR);

        if (overrideDefaultColor == Constants.DEFAULT_COLOR) {
            mCarrotTopBackGroundColor = obtainStyledAttributes.
                    getColor(R.styleable.InstaTag_carrotTopColor, Constants.DEFAULT_COLOR);
            mCarrotLeftBackGroundColor = obtainStyledAttributes.
                    getColor(R.styleable.InstaTag_carrotLeftColor, Constants.DEFAULT_COLOR);
            mCarrotRightBackGroundColor = obtainStyledAttributes.
                    getColor(R.styleable.InstaTag_carrotRightColor, Constants.DEFAULT_COLOR);
            mCarrotBottomBackGroundColor = obtainStyledAttributes.
                    getColor(R.styleable.InstaTag_carrotBottomColor, Constants.DEFAULT_COLOR);
            mTagBackgroundColor = obtainStyledAttributes.
                    getColor(R.styleable.InstaTag_instaBackgroundColor, Constants.DEFAULT_COLOR);
        } else {
            mTagBackgroundColor = overrideDefaultColor;
            mCarrotTopBackGroundColor = overrideDefaultColor;
            mCarrotLeftBackGroundColor = overrideDefaultColor;
            mCarrotRightBackGroundColor = overrideDefaultColor;
            mCarrotBottomBackGroundColor = overrideDefaultColor;
        }

        mHideAnimation = AnimationUtils.loadAnimation(context, obtainStyledAttributes.
                getResourceId(R.styleable.InstaTag_hideAnimation, R.anim.fade_out));  //zoom_out

        mShowAnimation = AnimationUtils.loadAnimation(context, obtainStyledAttributes.
                getResourceId(R.styleable.InstaTag_showAnimation, R.anim.fade_in));  //zoom_in

        initViewWithId(context, obtainStyledAttributes);
        obtainStyledAttributes.recycle();
    }

    private void hideRemoveButtonFromAllTagView() {
        if (!mTagList.isEmpty()) {
            for (TagViewBean tagViewBean : mTagList) {
                tagViewBean.view.findViewById(R.id.remove_tag_image_view).setVisibility(View.GONE);
            }
        }
    }

    private boolean tagNotTaggedYet(String tagName) {
        boolean tagFound = true;
        if (!mTagList.isEmpty()) {
            for (TagViewBean tagViewBean : mTagList) {
                if (((TextView) tagViewBean.view.findViewById(R.id.tag_text_view)).
                        getText().toString().equals(tagName)) {
                    tagFound = false;
                    break;
                }
            }
        }
        return tagFound;
    }

    private boolean isTagFound(final String tagName) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean tagFound = false;
                if (!mTagList.isEmpty()) {
                    for (TagViewBean tagViewBean : mTagList) {

                        String text = ((TextView) tagViewBean.view.findViewById(R.id.tag_text_view)).getText().toString();

                        if (text.equals(tagName)) {

                            mRoot.removeView(tagViewBean.view);

                            for (TagToBeTagged tagToBeTagged : getListOfTagsToBeTagged()) {
                                if (tagToBeTagged.getUnique_tag_id().equals(text)) {
                                    if (removeDuplicateTagListener != null)
                                        removeDuplicateTagListener.onDuplicateTagRemoved(tagToBeTagged);
                                    break;
                                }
                            }
                            tagFound = true;
                            break;
                        }
                    }
                } else {
                    tagFound = false;
                }
            }
        }, 200);
        return true;
    }

    private void setColor(Drawable drawable, int color) {
        if (drawable instanceof ShapeDrawable) {
            ((ShapeDrawable) drawable).getPaint().setColor(color);
        } else if (drawable instanceof GradientDrawable) {
            ((GradientDrawable) drawable).setColor(color);
        } else if (drawable instanceof ColorDrawable) {
            ((ColorDrawable) drawable).setColor(color);
        } else if (drawable instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            RotateDrawable rotateDrawable =
                    (RotateDrawable) layerDrawable.findDrawableByLayerId(R.id.carrot_shape_top);
            setColor(rotateDrawable.getDrawable(), color);
        } else if (drawable instanceof RotateDrawable) {
            setColor(((RotateDrawable) drawable).getDrawable(), color);
        }
    }

    private void setLayoutParamsToBeSetForRootLayout(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        int rootLayoutHeightWidth =
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, dpWidth, getResources().getDisplayMetrics());

        ViewGroup.LayoutParams params = mRoot.getLayoutParams();

        params.height = rootLayoutHeightWidth;
        params.width = rootLayoutHeightWidth;

        mRoot.setLayoutParams(params);
    }

    /*Tag Plot Start here*/
    public synchronized void addTag(float x, float y, String tagText, final TagDetail tag /*,HashMap<String,TagDetail> tagDetails*/) {
        if (tag.tabType.equalsIgnoreCase("people"))
            addPeopleTag(x, y, tagText, tag);
        else addServiceTag(x, y, tagText, tag);
    }

    /*People Tag Start here*/
    private void addPeopleTag(float x, float y, String tagText, final TagDetail tag) {
        if (!mTagList.isEmpty()) {
            for (TagViewBean tagViewBean : mTagList) {
                String text = ((TextView) tagViewBean.view.findViewById(R.id.tag_text_view)).getText().toString();
                if (text.equals(tagText)) {
                    mTagList.remove(tagViewBean);
                    mRoot.removeView(tagViewBean.view);
                    break;
                }
            }
        }

        //   if (isTagFound(tagText)) {

        //mTag = tag;
        if (tagMap != null) {
            this.tagMap.put(tag.title, tag);
        }

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View tagView = layoutInflater.
                inflate(R.layout.view_for_people_tag, mRoot, false);
        TextView tagTextView = tagView.findViewById(R.id.tag_text_view);
        FrameLayout flTag = tagView.findViewById(R.id.flTag);

        ArrowView carrot_top = tagView.findViewById(R.id.carrot_top);
        ArrowView carrot_bottom = tagView.findViewById(R.id.carrot_bottom);
        carrot_top.setColor(getResources().getColor(R.color.dark_transperant2));
        carrot_bottom.setColor(getResources().getColor(R.color.dark_transperant2));

        tagTextView.setText(tagText);

        flTag.measure(0, 0);
        int w = flTag.getMeasuredWidth();
        int h = flTag.getMeasuredHeight();

       /* if(x < 10 && y < 10){ //for left top
            // AppLogger.e("Step", "Left Top");
            updateLeftTopTagUI(tagView, left, top, w, h);
        }else */

        float left, top;
        if (x > 90 && y < 10) {  //for right top 1
            if (y < 1) y = 1;
            left = (mRootWidth * x) / 100;
            top = (mRootHeight * y) / 100;

            // AppLogger.e("Step", "Top Right");
            updateTopRightPeopleTagUI(tagView, left, top, w, h);
        } else if (x < 10 && y > 90) { //for bottom left 1
            left = (mRootWidth * x) / 100;
            top = (mRootHeight * y) / 100;

            // AppLogger.e("Step", "bottom Left");
            updateBottomLeftPeopleTagUI(tagView, left, top, w, h);
        } else if (x > 90 && y > 90) { //for bottom right 1
            left = (mRootWidth * x) / 100;
            top = (mRootHeight * y) / 100;

            // AppLogger.e("Step", "bottom Right");
            updateBottomRightPeopleTagUI(tagView, left, top, w, h);
        } else if (x < 10) { //for left
            left = (mRootWidth * x) / 100;
            top = (mRootHeight * y) / 100;

            // AppLogger.e("Step", "Left");
            updateLeftPeopleTagUI(tagView, left, top, w, h);
        } else if (x > 90) {  //for right
            left = (mRootWidth * x) / 100;
            top = (mRootHeight * y) / 100;

            // AppLogger.e("Step", "Right");
            updateRightPeopleTagUI(tagView, left, top, w, h);
        } else if (y > 90) {//for bottom
            if (y > 100) y = 100;
            left = (mRootWidth * x) / 100;
            top = (mRootHeight * y) / 100;

            // AppLogger.e("Step", "Bottom");
            updateBottomPeopleTagUI(tagView, left, top, w, h);
        } else {  //for top
            if (y < 1) y = 1;  //set top y max is 1
            left = (mRootWidth * x) / 100;
            top = (mRootHeight * y) / 100;

            // AppLogger.e("Step", "Center or top");
            updatePeopleTagUI(tagView, left, top, w, h);
        }


        // AppLogger.e("Step3 X,Y - ", x + "," + y + " = " + mRootWidth + " " + mRootHeight);
        // AppLogger.e("Step3 X,Y - ", left + "," + top);


       /* mPosX = (int)x - layoutParams.leftMargin;
        mPosY = (int)y - layoutParams.topMargin;

        actionTagMove(tagView, 0, (int)x, (int)y);*/

            /*removeTagImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener!=null) listener.onTagRemoved(tag);
                    mTagList.remove(tagView);
                    mRoot.removeView(tagView);
                }
            });*/

          /*  tagView.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(final View view, MotionEvent event) {
                    if (canWeAddTags) {
                        mIsRootIsInTouch = false;

                        final int X = (int) event.getRawX();
                        final int Y = (int) event.getRawY();

                        int pointerCount = event.getPointerCount();

                        switch (event.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_DOWN:
                                LayoutParams layoutParams =
                                        (LayoutParams) view.getLayoutParams();

                                mPosX = X - layoutParams.leftMargin;
                                mPosY = Y - layoutParams.topMargin;

                                removeTagImageView.setVisibility(View.GONE);

                                break;
                            case MotionEvent.ACTION_UP:
                                break;
                            case MotionEvent.ACTION_POINTER_DOWN:
                                break;
                            case MotionEvent.ACTION_POINTER_UP:
                                break;
                            case MotionEvent.ACTION_MOVE:
                                //actionTagMove(tagView, pointerCount, X, Y);
                                break;
                        }
                        mRoot.invalidate();
                    }
                    return true;
                }
            });*/

        tagView.setOnClickListener(v -> {
            if (listener != null) listener.onTagCliked(tag);
        });
    }

    private void updateTopRightPeopleTagUI(View tagView, float left, float top, int w, int h) {
        ArrowView carrot_top = tagView.findViewById(R.id.carrot_top);
        LinearLayout.LayoutParams lllp = (LinearLayout.LayoutParams) carrot_top.getLayoutParams();
        lllp.rightMargin = 5;
        lllp.gravity = Gravity.END;
        carrot_top.setLayoutParams(lllp);

        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        tagView.setX(left);
        tagView.setY(top);

        /*int l1 = w + w / 2 - 10;
        int h1 = (h / 2) - 15;*/

        int l1 = w;
        int h1 = 0;

        layoutParams.setMargins(-l1, -h1, 0, 0);
        // AppLogger.e("Step3 S left,top - ", -l1 + "," + -h1);
        tagView.setLayoutParams(layoutParams);

        TagViewBean tagViewBean = new TagViewBean();
        tagViewBean.view = tagView;
        tagViewBean.width = l1;
        tagViewBean.height = h1;

        mTagList.add(tagViewBean);
        mRoot.addView(tagView);
    }

    private void updateRightPeopleTagUI(View tagView, float left, float top, int w, int h) {
        ArrowView carrot_top = tagView.findViewById(R.id.carrot_top);
        LinearLayout.LayoutParams lllp = (LinearLayout.LayoutParams) carrot_top.getLayoutParams();
        lllp.rightMargin = 5;
        lllp.gravity = Gravity.END;
        carrot_top.setLayoutParams(lllp);

        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        tagView.setX(left);
        tagView.setY(top);

        /*int l1 = w + w / 2 - 10;
        int h1 = (h / 2) - 15;*/
        int l1 = w;
        int h1 = 0;

        layoutParams.setMargins(-l1, -h1, 0, 0);
        // AppLogger.e("Step3 S left,top - ", -l1 + "," + -h1);
        tagView.setLayoutParams(layoutParams);

        TagViewBean tagViewBean = new TagViewBean();
        tagViewBean.view = tagView;
        tagViewBean.width = l1;
        tagViewBean.height = h1;

        mTagList.add(tagViewBean);
        mRoot.addView(tagView);
    }

    private void updateLeftPeopleTagUI(View tagView, float left, float top, int w, int h) {
        ArrowView carrot_top = tagView.findViewById(R.id.carrot_top);
        LinearLayout.LayoutParams lllp = (LinearLayout.LayoutParams) carrot_top.getLayoutParams();
        lllp.leftMargin = 5;
        lllp.gravity = Gravity.START;
        carrot_top.setLayoutParams(lllp);

        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        tagView.setX(left);
        tagView.setY(top);

       /* int l1 = w / 2 + 15;
        int h1 = (h / 2) - 15;*/

        int l1 = w / 6;
        int h1 = 0;

        layoutParams.setMargins(-l1, -h1, 0, 0);
        // AppLogger.e("Step3 S left,top - ", -l1 + "," + -h1);
        tagView.setLayoutParams(layoutParams);

        TagViewBean tagViewBean = new TagViewBean();
        tagViewBean.view = tagView;
        tagViewBean.width = l1;
        tagViewBean.height = h1;

        mTagList.add(tagViewBean);
        mRoot.addView(tagView);
    }

    private void updateBottomRightPeopleTagUI(View tagView, float left, float top, int w, int h) {
        tagView.findViewById(R.id.carrot_top).setVisibility(GONE);
        tagView.findViewById(R.id.carrot_bottom).setVisibility(VISIBLE);

        ArrowView carrot_bottom = tagView.findViewById(R.id.carrot_bottom);
        LinearLayout.LayoutParams lllp = (LinearLayout.LayoutParams) carrot_bottom.getLayoutParams();
        lllp.rightMargin = 5;
        lllp.gravity = Gravity.END;
        carrot_bottom.setLayoutParams(lllp);

        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        tagView.setX(left);
        tagView.setY(top);

        /*int l1 = w + w / 2 - 10;
        int h1 = h * 2;*/
        int l1 = w;
        int h1 = h;

        layoutParams.setMargins(-l1, -h1, 0, 0);
        // AppLogger.e("Step3 S left,top - ", -l1 + "," + -h1);
        tagView.setLayoutParams(layoutParams);

        TagViewBean tagViewBean = new TagViewBean();
        tagViewBean.view = tagView;
        tagViewBean.width = l1;
        tagViewBean.height = h1;

        mTagList.add(tagViewBean);
        mRoot.addView(tagView);
    }

    private void updateBottomLeftPeopleTagUI(View tagView, float left, float top, int w, int h) {
        tagView.findViewById(R.id.carrot_top).setVisibility(GONE);
        tagView.findViewById(R.id.carrot_bottom).setVisibility(VISIBLE);

        ArrowView carrot_bottom = tagView.findViewById(R.id.carrot_bottom);
        LinearLayout.LayoutParams lllp = (LinearLayout.LayoutParams) carrot_bottom.getLayoutParams();
        lllp.leftMargin = 5;
        lllp.gravity = Gravity.START;
        carrot_bottom.setLayoutParams(lllp);

        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        tagView.setX(left);
        tagView.setY(top);

        /*int l1 = w / 2 + 15;
        int h1 = h * 2;*/

        int l1 = w / 6;
        int h1 = h;

        layoutParams.setMargins(-l1, -h1, 0, 0);
        // AppLogger.e("Step3 S left,top - ", -l1 + "," + -h1);
        tagView.setLayoutParams(layoutParams);

        TagViewBean tagViewBean = new TagViewBean();
        tagViewBean.view = tagView;
        tagViewBean.width = l1;
        tagViewBean.height = h1;

        mTagList.add(tagViewBean);
        mRoot.addView(tagView);
    }

    private void updateBottomPeopleTagUI(View tagView, float left, float top, int w, int h) {
        tagView.findViewById(R.id.carrot_top).setVisibility(GONE);
        tagView.findViewById(R.id.carrot_bottom).setVisibility(VISIBLE);

        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        tagView.setX(left);
        tagView.setY(top);

        /*int l1 = w;
        int h1 = h * 2;*/

        int l1 = w / 2;
        int h1 = h;

        layoutParams.setMargins(-l1, -h1, 0, 0);
        // AppLogger.e("Step3 S left,top - ", -l1 + "," + -h1);
        tagView.setLayoutParams(layoutParams);

        TagViewBean tagViewBean = new TagViewBean();
        tagViewBean.view = tagView;
        tagViewBean.width = l1;
        tagViewBean.height = h1;

        mTagList.add(tagViewBean);
        mRoot.addView(tagView);
    }

    private void updatePeopleTagUI(View tagView, float left, float top, int w, int h) {
        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        tagView.setX(left);
        tagView.setY(top);

       /* int l1 = w;
        int h1 = (h / 2) - 15;*/

        int l1 = w / 2;
        int h1 = 0;

        layoutParams.setMargins(-l1, h1, 0, 0);
        // AppLogger.e("Step3 S left,top - ", -l1 + "," + -h1);
        tagView.setLayoutParams(layoutParams);

        TagViewBean tagViewBean = new TagViewBean();
        tagViewBean.view = tagView;
        tagViewBean.width = l1;
        tagViewBean.height = h1;

        mTagList.add(tagViewBean);
        mRoot.addView(tagView);
    }
    /*People Tag End here*/

    /*Service Tag Start here*/
    private void addServiceTag(float x, float y, String tagText, final TagDetail tag) {
        if (!mTagList.isEmpty()) {
            for (TagViewBean tagViewBean : mTagList) {
                String text = ((TextView) tagViewBean.view.findViewById(R.id.tag_text_view)).getText().toString();
                if (text.equals(tagText)) {
                    mTagList.remove(tagViewBean);
                    mRoot.removeView(tagViewBean.view);
                    break;
                }
            }
        }

        if (tagMap != null) {
            this.tagMap.put(tag.title, tag);
        }

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);

        View tagView = layoutInflater.
                inflate(R.layout.view_for_service_tag, mRoot, false);
        TextView tagTextView = tagView.findViewById(R.id.tag_text_view);
        TextView tagPrice = tagView.findViewById(R.id.tv_price);
        FrameLayout flTag = tagView.findViewById(R.id.flTag);
        ArrowView carrot_top = tagView.findViewById(R.id.carrot_top);
        ArrowView carrot_bottom = tagView.findViewById(R.id.carrot_bottom);
        carrot_top.setColor(getResources().getColor(R.color.white));
        carrot_bottom.setColor(getResources().getColor(R.color.white));

        tagTextView.setText(tagText);
        tagPrice.setText(getContext().getString(R.string.pond_symbol).concat(tag.incallPrice.equals("0.0") || tag.incallPrice.isEmpty() ? tag.outcallPrice : tag.incallPrice));
        //setColorForTag(tagView);

        //Todo hs
        flTag.measure(0, 0);
        int w = flTag.getMeasuredWidth();
        int h = flTag.getMeasuredHeight();

        float left, top;

        if (x > 80 && y < 20) {//for right top 1
            if (y < 1) y = 1;  //set top y max is 1
            left = (mRootWidth * x) / 100;
            top = (mRootHeight * y) / 100;

            // AppLogger.e("Step", "Top Right");
            updateTopRightServiceTagUI(tagView, left, top, w, h); //ok
        } else if (x < 20 && y > 80) { //for bottom left 1
            left = (mRootWidth * x) / 100;
            top = (mRootHeight * y) / 100;

            // AppLogger.e("Step", "bottom Left");
            updateBottomLeftServiceTagUI(tagView, left, top, w, h);
        } else if (x > 80 && y > 80) { //for bottom right 1
            left = (mRootWidth * x) / 100;
            top = (mRootHeight * y) / 100;

            // AppLogger.e("Step", "bottom Right");
            updateBottomRightServiceTagUI(tagView, left, top, w, h); //ok
        } else if (x < 20) { //for left
            left = (mRootWidth * x) / 100;
            top = (mRootHeight * y) / 100;

            // AppLogger.e("Step", "Left");
            updateLeftServiceTagUI(tagView, left, top, w, h);
        } else if (x > 80) {  //for right
            left = (mRootWidth * x) / 100;
            top = (mRootHeight * y) / 100;

            // AppLogger.e("Step", "Right");
            updateRightServiceTagUI(tagView, left, top, w, h, x);
        } else if (y > 80) { //for bottom
            if (y > 100) y = 100;  //set top y max is 100

            left = (mRootWidth * x) / 100;
            top = (mRootHeight * y) / 100;

            // AppLogger.e("Step", "Bottom");
            updateBottomServiceTagUI(tagView, left, top, w, h);
        } else {  //for top
            if (y < 1) y = 1;  //set top y max is 1

            left = (mRootWidth * x) / 100;
            top = (mRootHeight * y) / 100;

            // AppLogger.e("Step", "Center or top");
            updateServiceTagUI(tagView, left, top, w, h);  //ok
        }
        // AppLogger.e("Step3 S X,Y - ", x + "," + y + " = " + mRootWidth + " " + mRootHeight);
        // AppLogger.e("Step3 S X,Y - ", left + "," + top);


        flTag.setOnClickListener(v -> {
            if (listener != null) listener.onTagCliked(tag);
        });
    }

    private void updateTopRightServiceTagUI(View tagView, float left, float top, int w, int h) {
        ArrowView carrot_top = tagView.findViewById(R.id.carrot_top);
        LinearLayout.LayoutParams lllp = (LinearLayout.LayoutParams) carrot_top.getLayoutParams();
        lllp.gravity = Gravity.END;
        carrot_top.setLayoutParams(lllp);

        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        tagView.setX(left);
        tagView.setY(top);

        /*int l1 = w + 30;
        int h1 = (h / 2) - 20;*/

        int l1 = w;
        int h1 = 0;

        layoutParams.setMargins(-l1, -h1, 0, 0);
        // AppLogger.e("Step3 S left,top - ", -l1 + "," + -h1);
        tagView.setLayoutParams(layoutParams);

        TagViewBean tagViewBean = new TagViewBean();
        tagViewBean.view = tagView;
        tagViewBean.width = l1;
        tagViewBean.height = h1;

        mTagList.add(tagViewBean);
        mRoot.addView(tagView);
    }

    private void updateRightServiceTagUI(View tagView, float left, float top, int w, int h, float x) {
        ArrowView carrot_top = tagView.findViewById(R.id.carrot_top);
        LinearLayout.LayoutParams lllp = (LinearLayout.LayoutParams) carrot_top.getLayoutParams();
        lllp.gravity = Gravity.END;
        carrot_top.setLayoutParams(lllp);

        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        tagView.setX(left);
        tagView.setY(top);

        /*int l1;

        if (x > 90) {
            l1 = w + 30;
        } else {
            l1 = w;
        }
        int h1 = (h / 2) - 15;*/

        int l1 = w;
        int h1 = 0;

        layoutParams.setMargins(-l1, -h1, 0, 0);
        // AppLogger.e("Step3 S left,top - ", -l1 + "," + -h1);
        tagView.setLayoutParams(layoutParams);

        TagViewBean tagViewBean = new TagViewBean();
        tagViewBean.view = tagView;
        tagViewBean.width = l1;
        tagViewBean.height = h1;

        mTagList.add(tagViewBean);
        mRoot.addView(tagView);
    }

    private void updateLeftServiceTagUI(View tagView, float left, float top, int w, int h) {
        ArrowView carrot_top = tagView.findViewById(R.id.carrot_top);
        LinearLayout.LayoutParams lllp = (LinearLayout.LayoutParams) carrot_top.getLayoutParams();
        lllp.gravity = Gravity.START;
        carrot_top.setLayoutParams(lllp);

        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        tagView.setX(left);
        tagView.setY(top);

        /*int l1 = 60;
        int h1 = (h / 2) - 20;*/

        int l1 = 15;
        int h1 = 0;
        // AppLogger.e("What - ", -l1 + "," + -h1);

        layoutParams.setMargins(-l1, -h1, 0, 0);
        // AppLogger.e("Step3 S left,top - ", -l1 + "," + -h1);
        tagView.setLayoutParams(layoutParams);

        TagViewBean tagViewBean = new TagViewBean();
        tagViewBean.view = tagView;
        tagViewBean.width = l1;
        tagViewBean.height = h1;

        mTagList.add(tagViewBean);
        mRoot.addView(tagView);
    }

    private void updateBottomRightServiceTagUI(View tagView, float left, float top, int w, int h) {
        tagView.findViewById(R.id.carrot_top).setVisibility(GONE);
        tagView.findViewById(R.id.carrot_bottom).setVisibility(VISIBLE);

        ArrowView carrot_bottom = tagView.findViewById(R.id.carrot_bottom);
        LinearLayout.LayoutParams lllp = (LinearLayout.LayoutParams) carrot_bottom.getLayoutParams();
        lllp.gravity = Gravity.END;
        carrot_bottom.setLayoutParams(lllp);

        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        tagView.setX(left);
        tagView.setY(top);

        /*int l1 = w + 30;
        int h1 = h * 2 - 40;*/

        int l1 = w;
        int h1 = h;

        layoutParams.setMargins(-l1, -h1, 0, 0);
        // AppLogger.e("Step3 S left,top - ", -l1 + "," + -h1);
        tagView.setLayoutParams(layoutParams);

        TagViewBean tagViewBean = new TagViewBean();
        tagViewBean.view = tagView;
        tagViewBean.width = l1;
        tagViewBean.height = h1;

        mTagList.add(tagViewBean);
        mRoot.addView(tagView);
    }

    private void updateBottomLeftServiceTagUI(View tagView, float left, float top, int w, int h) {
        tagView.findViewById(R.id.carrot_top).setVisibility(GONE);
        tagView.findViewById(R.id.carrot_bottom).setVisibility(VISIBLE);

        ArrowView carrot_bottom = tagView.findViewById(R.id.carrot_bottom);
        LinearLayout.LayoutParams lllp = (LinearLayout.LayoutParams) carrot_bottom.getLayoutParams();
        lllp.leftMargin = 5;
        lllp.gravity = Gravity.START;
        carrot_bottom.setLayoutParams(lllp);

        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        tagView.setX(left);
        tagView.setY(top);

        /*int l1 = 60;
        int h1 = h * 2 - 40;*/

        int l1 = 15;
        int h1 = h;

        layoutParams.setMargins(-l1, -h1, 0, 0);
        // AppLogger.e("Step3 S left,top - ", -l1 + "," + -h1);
        tagView.setLayoutParams(layoutParams);

        TagViewBean tagViewBean = new TagViewBean();
        tagViewBean.view = tagView;
        tagViewBean.width = l1;
        tagViewBean.height = h1;

        mTagList.add(tagViewBean);
        mRoot.addView(tagView);
    }

    private void updateBottomServiceTagUI(View tagView, float left, float top, int w, int h) {
        tagView.findViewById(R.id.carrot_top).setVisibility(GONE);
        tagView.findViewById(R.id.carrot_bottom).setVisibility(VISIBLE);

        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        tagView.setX(left);
        tagView.setY(top);

        /*int l1 = w - w / 3 + 10;
        int h1 = h * 2 - 40;*/

        int l1 = w / 2;
        int h1 = h;

        layoutParams.setMargins(-l1, -h1, 0, 0);
        // AppLogger.e("Step3 S left,top - ", -l1 + "," + -h1);
        tagView.setLayoutParams(layoutParams);

        TagViewBean tagViewBean = new TagViewBean();
        tagViewBean.view = tagView;
        tagViewBean.width = l1;
        tagViewBean.height = h1;

        mTagList.add(tagViewBean);
        mRoot.addView(tagView);
    }

    private void updateServiceTagUI(View tagView, float left, float top, int w, int h) {
        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        tagView.setX(left);
        tagView.setY(top);

        /*int l1 = w - w / 3 + 10;
        int h1 = 10;*/

        int l1 = w / 2;
        int h1 = 0;

        layoutParams.setMargins(-l1, -h1, 0, 0);
        // AppLogger.e("Step3 S left,top - ", -l1 + "," + -h1);
        tagView.setLayoutParams(layoutParams);

        TagViewBean tagViewBean = new TagViewBean();
        tagViewBean.view = tagView;
        tagViewBean.width = l1;
        tagViewBean.height = h1;

        mTagList.add(tagViewBean);
        mRoot.addView(tagView);
    }
    /*Service Tag End here*/
    /*Tag Plot End here*/

    public void addTagViewFromTagsToBeTagged(String tagType, ArrayList<TagToBeTagged> tagsToBeTagged, boolean isAddingTag) {
        this.isAddingTag = isAddingTag;
        if (tagType.equalsIgnoreCase("people")) {
            if (!peopleTagsAreAdded) {
                for (TagToBeTagged tagToBeTagged : tagsToBeTagged) {

                    //Todo hs
               /* double x = getXWhileAddingTag(tagToBeTagged.getX_co_ord());
                double y = getYWhileAddingTag(tagToBeTagged.getY_co_ord());*/

                    // // AppLogger.e("Step","x==" + x + "  " + "y==" + y);

                /*addTag(getXWhileAddingTag(tagToBeTagged.getX_co_ord()), getYWhileAddingTag(tagToBeTagged.getY_co_ord()),
                        tagToBeTagged.getUnique_tag_id(), tagToBeTagged.getTagDetails().get(tagToBeTagged.getUnique_tag_id()));*/

                    addTag(tagToBeTagged.getX_co_ord(),
                            tagToBeTagged.getY_co_ord(),
                            tagToBeTagged.getUnique_tag_id(), tagToBeTagged.getTagDetails());
                }
                peopleTagsAreAdded = true;
            }
        } else {
            if (!serviceTagsAreAdded) {
                for (TagToBeTagged tagToBeTagged : tagsToBeTagged) {

                    //Todo hs
               /* double x = getXWhileAddingTag(tagToBeTagged.getX_co_ord());
                double y = getYWhileAddingTag(tagToBeTagged.getY_co_ord());*/

                    // // AppLogger.e("Step","x==" + x + "  " + "y==" + y);

                /*addTag(getXWhileAddingTag(tagToBeTagged.getX_co_ord()), getYWhileAddingTag(tagToBeTagged.getY_co_ord()),
                        tagToBeTagged.getUnique_tag_id(), tagToBeTagged.getTagDetails().get(tagToBeTagged.getUnique_tag_id()));*/

                    addTag(tagToBeTagged.getX_co_ord(),
                            tagToBeTagged.getY_co_ord(),
                            tagToBeTagged.getUnique_tag_id(), tagToBeTagged.getTagDetails());
                }
                serviceTagsAreAdded = true;
            }
        }
    }

    public TagImageView getTagImageView() {
        return mTagImageView;
    }

    public View getParentView() {
        return mRoot;
    }

    public ArrayList<TagToBeTagged> getListOfTagsToBeTagged() {
        ArrayList<TagToBeTagged> tagsToBeTagged = new ArrayList<>();
        if (!mTagList.isEmpty()) {
            for (int i = 0; i < mTagList.size(); i++) {
                TagViewBean tagViewBean = mTagList.get(i);

                //Todo hs
               /* double x = view.getX();
                x = (x / mRootWidth) * 100;
                double y = view.getY();
                y = (y / mRootHeight) * 100;*/
                float xm = tagViewBean.view.getX() + tagViewBean.width;
                float ym = tagViewBean.view.getY() + tagViewBean.height;

                float x = (xm * 100) / mRootWidth;   //left
                float y = (ym * 100) / mRootHeight;  //top
                // float y = (ym * 100) / (mRootHeight*4/3);  //top
                //float x= view.getX();
                //float y = view.getY();
                Log.e("Step1 X,Y - ", xm + "," + ym + " = " + mRootWidth + " " + mRootHeight);
                Log.e("Step final X,Y - ", x + "," + y);

                String txt = ((TextView) tagViewBean.view.findViewById(R.id.tag_text_view)).getText().toString();

                //HashMap<String, TagDetail> map = new HashMap();
                //map.put(txt, tagMap.get(txt));

                //tagsToBeTagged.add(new TagToBeTagged(txt, x, y, map));
                tagsToBeTagged.add(new TagToBeTagged(txt, x, y, tagMap.get(txt)));
            }
        }
        return tagsToBeTagged;
    }

    public void setImageToBeTaggedEvent(TaggedImageEvent taggedImageEvent) {
        if (this.mTaggedImageEvent == null) {
            this.mTaggedImageEvent = taggedImageEvent;
        }
    }

    public void setTouchListnerDisable() {
        mRoot.setOnTouchListener(null);
    }

    public synchronized void showTags(String type) {
        if (!mTagList.isEmpty()) {
            for (TagViewBean tagViewBean : mTagList) {
                tagViewBean.view.setVisibility(VISIBLE);
                tagViewBean.view.startAnimation(mShowAnimation);
            }
            if (type.equalsIgnoreCase("people")) this.isShowPeopleTag = true;
            else this.isShowServiceTag = true;
        }
    }

    public synchronized void hideTags(String type) {
        if (!mTagList.isEmpty()) {
            for (TagViewBean tagViewBean : mTagList) {
                tagViewBean.view.startAnimation(mHideAnimation);
                tagViewBean.view.setVisibility(GONE);
            }
            if (type.equalsIgnoreCase("people")) this.isShowPeopleTag = false;
            else this.isShowServiceTag = false;
        }
    }

    public synchronized boolean isTagsShow(String type) {
        if (type.equalsIgnoreCase("people"))
            return isShowPeopleTag;
        else return isShowServiceTag;
    }

    public void removeTags() {
        if (!mTagList.isEmpty()) {
            for (TagViewBean tagViewBean : mTagList) {
                mRoot.removeView(tagViewBean.view);
            }
            mTagList.clear();
        }
    }

    public void removeSingleTags(TagToBeTagged tag) {
        if (!mTagList.isEmpty()) {
            for (TagViewBean tagViewBean : mTagList) {
                String txt = ((TextView) tagViewBean.view.findViewById(R.id.tag_text_view)).getText().toString();
                if (txt.equals(tag.getUnique_tag_id())) {
                    mTagList.remove(tagViewBean);
                    mRoot.removeView(tagViewBean.view);
                    break;
                }
            }
        }
    }

    public int getRootWidth() {
        return mRootWidth;
    }

    public void setRootWidth(int mRootWidth) {
        InstaTag.mRootWidth = mRootWidth;
    }

    public int getRootHeight() {
        return mRootHeight;
    }

    public void setRootHeight(int mRootHeight) {
        InstaTag.mRootHeight = mRootHeight;
    }

    public boolean canWeAddTags() {
        return canWeAddTags;
    }

    public void setCanWeAddTags(boolean mCanWeAddTags) {
        this.canWeAddTags = mCanWeAddTags;
    }

    public boolean isShowAllCarrots() {
        return showAllCarrots;
    }

    public interface Listener {
        void onTagCliked(TagDetail tagDetail);

        void onTagRemoved(TagDetail tagDetail);
    }

    public interface Constants {
        int DEFAULT_COLOR = Color.DKGRAY;
        int TAG_TEXT_COLOR = Color.WHITE;

        String CARROT_TOP = "CARROT_TOP";
        String CARROT_LEFT = "CARROT_LEFT";
        String CARROT_RIGHT = "CARROT_RIGHT";
        String CARROT_BOTTOM = "CARROT_BOTTOM";
    }

    public interface TaggedImageEvent {

        void singleTapConfirmedAndRootIsInTouch(float x, float y);

        boolean onDoubleTap(MotionEvent e);

        boolean onDoubleTapEvent(MotionEvent e);

        void onLongPress(MotionEvent e);

        void onSinglePress(MotionEvent e);
    }

}


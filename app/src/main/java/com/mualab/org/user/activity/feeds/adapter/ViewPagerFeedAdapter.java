package com.mualab.org.user.activity.feeds.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.gellery.GalleryActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mindiii on 10/12/18.
 */

public class ViewPagerFeedAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    getClick click;


    public ViewPagerFeedAdapter(Context context,getClick click) {
        this.context = context;
        this.click = click;

    }

    public interface getClick{
         void viewpagerClick();
         void OnTextChange(String text);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.view_pager_item_layout, null);
        ImageView gallay_icon = view.findViewById(R.id.gallay_icon);
        EditText ed_search = view.findViewById(R.id.ed_search);
        RelativeLayout ly_main = view.findViewById(R.id.ly_main);

        if(position == 0){
            gallay_icon.setVisibility(View.GONE);
            ed_search.setEnabled(true);
            ed_search.setHint(R.string.search);
            ly_main.setVisibility(View.GONE);

        }else {
            gallay_icon.setVisibility(View.VISIBLE);
            ed_search.setEnabled(false);
            ed_search.setHint(R.string.text_something_in_your_mind);
            ly_main.setVisibility(View.VISIBLE);

        }

        ed_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                click.OnTextChange(s.toString());
            }
        });

        ly_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click.viewpagerClick();
            }
        });

        gallay_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(context, GalleryActivity.class);
                context.startActivity(in);
            }
        });

        ViewPager vp = (ViewPager) container;
        vp.addView(view, 0);
        return view;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        ViewPager vp = (ViewPager) container;
        View view = (View) object;
        vp.removeView(view);

    }
}
package com.example.a18145288.watermac.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by 18145288 on 2019/6/24.
 */

public class WatConAdapter extends PagerAdapter {
    private List<View> views;
    public WatConAdapter(List<View> views){
        this.views = views;
    }

    @Override
    public int getCount() {
        if (views == null || views.size() == 0){
            return 0;
        }
        return views.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = views.get(position);
        if (views != null){
            container.removeView(view);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = views.get(position);
        if (view != null){
            container.addView(view);
        }
        return view;
    }
}

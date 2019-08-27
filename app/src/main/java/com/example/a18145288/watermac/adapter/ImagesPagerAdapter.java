package com.example.a18145288.watermac.adapter;

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import com.facebook.drawee.view.SimpleDraweeView;
import java.util.List;
/**
 * Created by 18145288 on 2019/6/18.
 */

public class ImagesPagerAdapter extends PagerAdapter {
    private String TAG = "ImagesPagerAdapter";
    private List<SimpleDraweeView> simpleDraweeViewList;
    private ViewPager viewPager;
    private Context context;

    private SimpleDraweeView simpleDraweeView;

    public ImagesPagerAdapter(List<SimpleDraweeView> simpleDraweeViewList, ViewPager viewPager, Context context) {
        this.simpleDraweeViewList = simpleDraweeViewList;
        this.viewPager = viewPager;
        this.context = context;
    }

    @Override
    public int getCount() {
        return simpleDraweeViewList.size();
    }

    //删除指定位置的页面；适配器负责从view容器中删除view，然而它只保证在finishUpdate(ViewGroup)返回时才完成。
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // 把ImageView从ViewPager中移除掉
        viewPager.removeView(simpleDraweeViewList.get(position));
        //super.destroyItem(container, position, object);
    }

    //是否获取缓存
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    //实例化Item
    //在指定的位置创建页面；适配器负责添加view到这个容器中，然而它只保证在finishUpdate(ViewGroup)返回时才完成。
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        simpleDraweeView = simpleDraweeViewList.get(position);
        viewPager.addView(simpleDraweeView);
        return simpleDraweeView;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    //无论是创建view添加到容器中  还是销毁view 都是在此方法结束之后执行的
    @Override
    public void finishUpdate(ViewGroup container) {
        super.finishUpdate(container);

        int position = viewPager.getCurrentItem();
//        Log.i("MainActivity", "finishUpdate:" + position + " count:" + simpleDraweeViewList.size());

        if (position == 0) {
            position = simpleDraweeViewList.size() - 2;
            viewPager.setCurrentItem(position,false);//没有平滑效果，瞬间移动
        } else if (position == simpleDraweeViewList.size() - 1) {
            position = 1;
            viewPager.setCurrentItem(position,false);//没有平滑效果，瞬间移动
        }
    }



/*    private int mChildCount = 0;
    @Override
    public void notifyDataSetChanged() {
        mChildCount = getCount();
        super.notifyDataSetChanged();
    }
    @Override
    public int getItemPosition(Object object) {
        if (mChildCount > 0) {
            mChildCount--;
            Log.e("image","getItemPosition");
            return POSITION_NONE;
        }
        return super.getItemPosition(object);
    }*/

}

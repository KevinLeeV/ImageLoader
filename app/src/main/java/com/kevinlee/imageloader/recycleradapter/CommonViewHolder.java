package com.kevinlee.imageloader.recycleradapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kevinlee.imageloader.imageloader.utils.CompressBitmapFromPath;

/**
 * ClassName: CommonViewHolder
 * Description: 封装了Item中各种View的类
 * Author:KevinLee
 * Date:2016/11/24 0024
 * Time:上午 11:51
 * Email:KevinLeeV@163.com
 */
public class CommonViewHolder extends RecyclerView.ViewHolder {

    // Item中各种View的集合，SparseArray类似于Map，key是Integer类型，但是效率比Map要高
    private SparseArray<View> mViews;
    public View itemView;
    private Context mContext;

    private CommonViewHolder(Context context, View itemView) {
        super(itemView);
        this.mContext = context;
        this.itemView = itemView;
        mViews = new SparseArray<>();
    }

    public static CommonViewHolder getInstance(Context context, ViewGroup parent, int layoutId) {

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new CommonViewHolder(context, view);
    }

    /**
     * 通过view的id获取View
     *
     * @param viewId
     * @param <T>
     * @return
     */
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 为TextView设置文本
     *
     * @param viewId view的id
     * @param text   文本信息
     * @return ViewHolder是为了实现链式代码
     */
    public CommonViewHolder setTextWithTextView(int viewId, String text) {
        TextView view = getView(viewId);
        view.setText(text);
        return this;
    }

    /**
     * 为View设置背景图
     *
     * @param viewId view的id
     * @param resId  资源Id
     * @return ViewHolder是为了实现链式代码
     */
    public CommonViewHolder setBackgroundResource(int viewId, int resId) {
        View view = getView(viewId);
        view.setBackgroundResource(resId);
        return this;
    }

    /**
     * 为View设置背景色
     *
     * @param viewId view的id
     * @param color  颜色
     * @return ViewHolder是为了实现链式代码
     */
    public CommonViewHolder setBackgroundColor(int viewId, int color) {
        View view = getView(viewId);
        view.setBackgroundColor(color);
        return this;
    }

    /**
     * 为ImageView设置前景图
     *
     * @param viewId view的id
     * @param url    图片地址
     * @return ViewHolder是为了实现链式代码
     */
    public CommonViewHolder setImageUrl(int viewId, String url) {
        ImageView view = getView(viewId);
        Glide.with(mContext).load(url).skipMemoryCache(true).into(view);
        return this;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId view的id
     * @param resId  资源Id
     * @return ViewHolder是为了实现链式代码
     */
    public CommonViewHolder setImageResource(int viewId, int resId) {
        ImageView view = getView(viewId);
        view.setImageResource(resId);
        return this;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId view的id
     * @param bitmap
     * @return ViewHolder是为了实现链式代码
     */
    public CommonViewHolder setImageBitmap(int viewId, Bitmap bitmap) {
        ImageView view = getView(viewId);
        view.setImageBitmap(bitmap);
        return this;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId   view的id
     * @param filePath 图片路径
     * @return ViewHolder是为了实现链式代码
     */
    public CommonViewHolder setImageBitmap(int viewId, String filePath) {
        ImageView view = getView(viewId);
        Bitmap bitmap = CompressBitmapFromPath.getInstance().decodeSampledBitmapFromPath(filePath, view);
        view.setImageBitmap(bitmap);
        return this;
    }


}

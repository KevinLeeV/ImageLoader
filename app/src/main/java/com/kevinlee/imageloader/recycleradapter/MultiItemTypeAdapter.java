package com.kevinlee.imageloader.recycleradapter;

import android.content.Context;
import android.view.ViewGroup;

import java.util.List;

/**
 * ClassName:
 * Description:
 * Author:KevinLee
 * Date:2016/12/15 0015
 * Time:下午 6:23
 * Email:KevinLeeV@163.com
 */
public abstract class MultiItemTypeAdapter<T> extends CommonRecyclerAdapter<T> {

    private Context context;
    private List<T> datas;
    private MultiItemTypeListener<T> multiItemTypeListener;

    public MultiItemTypeAdapter(Context context, List<T> datas, MultiItemTypeListener<T> multiItemTypeListener) {
        super(context, -1, datas);
        this.context = context;
        this.datas = datas;
        this.multiItemTypeListener = multiItemTypeListener;
    }

    @Override
    public int getItemViewType(int position) {
        return multiItemTypeListener.getItemType(position, datas.get(position));
    }

    @Override
    public CommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = multiItemTypeListener.getItemLayoutId(viewType);
        CommonViewHolder holder = CommonViewHolder.getInstance(context, parent, layoutId);
        return holder;
    }
}

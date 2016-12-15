package com.kevinlee.imageloader.recycleradapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * ClassName:
 * Description:
 * Author:KevinLee
 * Date:2016/12/15 0015
 * Time:下午 4:12
 * Email:KevinLeeV@163.com
 */
public abstract class CommonRecyclerAdapter<T> extends RecyclerView.Adapter<CommonViewHolder> {

    private Context context;
    private List<T> datas;
    private int layoutId;

    public CommonRecyclerAdapter(Context context, int layoutId, List<T> datas) {
        this.context = context;
        this.datas = datas;
        this.layoutId = layoutId;
    }



    @Override
    public CommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CommonViewHolder holder = CommonViewHolder.getInstance(context, parent, layoutId);
        return holder;
    }

    @Override
    public void onBindViewHolder(CommonViewHolder holder, int position) {
        convert(holder, datas.get(position));
    }

    @Override
    public int getItemCount() {
        if (datas != null && datas.size() > 0)
            return datas.size();
        return 0;
    }

    public abstract void convert(CommonViewHolder holder, T t);
}

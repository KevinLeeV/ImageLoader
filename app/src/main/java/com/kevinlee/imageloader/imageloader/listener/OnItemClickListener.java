package com.kevinlee.imageloader.imageloader.listener;

import android.view.View;

/**
 * ClassName:
 * Description:
 * Author:KevinLee
 * Date:2016/12/17 0017
 * Time:上午 11:31
 * Email:KevinLeeV@163.com
 */
public interface OnItemClickListener {

    void onItemClick(View itemView, int position);

    void onItemLongClick(View itemView,int position);

}

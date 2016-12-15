package com.kevinlee.imageloader.recycleradapter;

/**
 * ClassName:
 * Description:
 * Author:KevinLee
 * Date:2016/12/15 0015
 * Time:下午 7:30
 * Email:KevinLeeV@163.com
 */
public interface MultiItemTypeListener<T> {

    int getItemLayoutId(int itemType);

    int getItemType(int position, T t);

}

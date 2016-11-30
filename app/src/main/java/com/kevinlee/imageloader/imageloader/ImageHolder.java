package com.kevinlee.imageloader.imageloader;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * ClassName:ImageHolder
 * Description: 存储Image的信息
 * Author:KevinLee
 * Date:2016/11/28 0028
 * Time:下午 3:33
 * Email:KevinLeeV@163.com
 */
public class ImageHolder {

    private ImageView imageView;
    private String path;
    private Bitmap bitmap;

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}

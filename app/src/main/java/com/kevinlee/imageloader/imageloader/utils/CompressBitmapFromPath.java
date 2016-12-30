package com.kevinlee.imageloader.imageloader.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kevinlee.imageloader.imageloader.bean.ImageSize;

import java.lang.reflect.Field;

/**
 * ClassName:
 * Description:
 * Author:KevinLee
 * Date:2016/12/17 0017
 * Time:上午 9:18
 * Email:KevinLeeV@163.com
 */
public class CompressBitmapFromPath {

    private static CompressBitmapFromPath instance;

    private CompressBitmapFromPath() {
    }

    public static CompressBitmapFromPath getInstance() {
        if (instance == null) {
            synchronized (CompressBitmapFromPath.class) {
                if (instance == null) {
                    instance = new CompressBitmapFromPath();
                }
            }
        }
        return instance;
    }

    /**
     * 根据图片的需求宽高压缩图片
     *
     * @param path
     * @param imageView
     * @return
     */
    public Bitmap decodeSampledBitmapFromPath(String path, ImageView imageView) {


        // 获取到图片的实际宽高
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 不加载到内存中
        options.inJustDecodeBounds = true;
        // options获得图片的实际宽高
        BitmapFactory.decodeFile(path, options);
        ImageSize imageSize = getImageSize(imageView);
        // 获取图片压缩比例
        options.inSampleSize = getSampleSize(options, imageSize.getWidth(), imageSize.getHeight());
        // 将图片加载到内存,并通过inSampleSize再次解析图片
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    /**
     * 根据图片的实际宽高与需求宽高获取到压缩比例
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private int getSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;// 图片实际宽度
        int height = options.outHeight;// 图片实际高度
        int sampleSize = 1;
        if (width > reqWidth || height > reqHeight) {
            int widthRadio = Math.round(width * 1.0f / reqWidth);// 获取到宽度比例
            int heightRadio = Math.round(height * 1.0f / reqHeight);// 获取到高度比例
            sampleSize = Math.max(widthRadio, heightRadio);
        }
        return sampleSize;
    }

    /**
     * 获取到Image的宽高
     *
     * @param imageView
     * @return
     */
    private ImageSize getImageSize(ImageView imageView) {
        ImageSize imageSize = new ImageSize();
        DisplayMetrics dm = null;
        // 获取imageView 的实际宽度
        int width = imageView.getWidth();
        // 如果小于等于0，则看LayoutParams中是否定义了width
        if (width <= 0) {
            ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            width = lp.width;
        }
        // 如果小于等于0，则看ImageView中是否定义了最大宽度
        if (width <= 0) {
            width = getImageViewValueFromField(imageView, "mMaxWidth");
        }
        // 如果小于等于0，则将width设置为屏幕宽度
        if (width <= 0) {
            if (dm == null)
                dm = imageView.getContext().getResources().getDisplayMetrics();
            width = dm.widthPixels;
        }

        // 获取imageView 的实际高度
        int height = imageView.getHeight();
        // 如果小于等于0，则看LayoutParams中是否定义了height
        if (height <= 0) {
            ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            height = lp.height;
        }
        // 如果小于等于0，则看ImageView中是否定义了最大高度
        if (height <= 0) {
            height = getImageViewValueFromField(imageView, "mMaxHeight");
        }
        // 如果小于等于0，则将width设置为屏幕高度
        if (height <= 0) {
            if (dm == null)
                dm = imageView.getContext().getResources().getDisplayMetrics();
            height = dm.heightPixels;
        }

        imageSize.setHeight(height);
        imageSize.setWidth(width);
        return imageSize;
    }

    /**
     * 通过反射获取到ImageView的某个属性值
     */
    private int getImageViewValueFromField(Object obj, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = field.getInt(obj);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

}

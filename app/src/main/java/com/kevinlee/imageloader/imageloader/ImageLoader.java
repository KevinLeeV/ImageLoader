package com.kevinlee.imageloader.imageloader;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * ClassName:ImageLoader
 * Description: 图片加载的工具类
 * Author:KevinLee
 * Date:2016/11/28 0028
 * Time:下午 2:04
 * Email:KevinLeeV@163.com
 */
public class ImageLoader {

    private static ImageLoader mImageLoader;

    // lruCache缓存对象
    private LruCache<String, Bitmap> mLruCache;

    // 默认线程数量为1
    private static final int DEFAULT_THREAD_COUNT = 1;

    // 线程数
    private static int mThreadCount = DEFAULT_THREAD_COUNT;

    // 线程池
    private ExecutorService mThreadPool;

    // 队列的调度模式，默认为后进先出
    private static LoadType mType = LoadType.LIFO;

    // 任务队列
    private LinkedList<Runnable> mTaskQueue;

    // 后台轮询线程
    private Thread mPoolThread;

    // 后台轮询线程的handler
    private Handler mPoolThreadHandler;

    // 主线程handler
    private Handler mUIHandler;

    // 信号量，用来处理多线程防止空指针
    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);

    private ImageLoader(int threadCount, LoadType type) {
        initLoader(threadCount, type);
    }

    // 获取ImageLoader的实例化
    public static ImageLoader getInstance(int threadCount, LoadType type) {
        if (mImageLoader == null) {
            // 使用同步对象锁，防止多线程同时访问
            synchronized (mImageLoader) {
                if (mImageLoader == null) {
                    mImageLoader = new ImageLoader(threadCount, type);
                }
            }
        }
        return mImageLoader;
    }

    // 初始化图片加载
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void initLoader(int threadCount, LoadType type) {
        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                // 后台轮询线程
                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        // 从线程池中取出一个任务执行
                        mThreadPool.execute(getTask());
                    }
                };
                mSemaphorePoolThreadHandler.release();
                Looper.loop();
            }
        };
        mPoolThread.start();

        /**
         * 初始化LruCache
         */
        // 应用的最大内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        // 缓存内存
        int cacheMemory = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };

        mThreadCount = threadCount;
        // 创建线程池
        mThreadPool = Executors.newFixedThreadPool(mThreadCount);
        // 创建任务队列
        mTaskQueue = new LinkedList<>();
        mType = type;

    }

    /**
     * 获取任务
     *
     * @return
     */
    private Runnable getTask() {
        if (mType == LoadType.LIFO) {
            mTaskQueue.removeLast();
        } else {
            mTaskQueue.removeLast();
        }
        return null;
    }

    /**
     * 加载图片
     *
     * @param path      图片的路径
     * @param imageView
     */
    public void loadImage(final String path, final ImageView imageView) {
        // 为imageView设置tag，防止复用时有问题
        imageView.setTag(path);
        // 初始化UIHandler
        mUIHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // 更新ImageView
                ImageHolder holder = (ImageHolder) msg.obj;
                if (holder.getPath().equals(holder.getImageView().getTag().toString())) {
                    imageView.setImageBitmap(holder.getBitmap());
                }
            }
        };

        // 优先从缓存中加载图片
        Bitmap bitmap = getBitmapFromLruCache(path);
        if (bitmap != null) {
            refreshImageView(imageView, path, bitmap);
        } else {
            // 从网络加载图片,则需要添加一个异步任务
            addTask(new Runnable() {
                @Override
                public void run() {
                    // 加载图片
                    // 1、得获取到图片需要显示的尺寸
                    ImageSize imageSize = getImageSize(imageView);
                    // 2、压缩图片
                    Bitmap bm = decodeSampledBitmapFromPath(path, imageSize.getWidth(), imageSize.getHeight());
                    // 3、将图片加载到缓存中
                    addBitmapToLruCache(path, bm);
                    // 4、将图片显示到imageView中
                    refreshImageView(imageView, path, bm);
                }
            });
        }
    }

    /**
     * 更新imageView
     *
     * @param imageView
     * @param path
     * @param bm
     */
    private void refreshImageView(ImageView imageView, String path, Bitmap bm) {
        Message msg = Message.obtain();
        ImageHolder holder = new ImageHolder();
        holder.setBitmap(bm);
        holder.setImageView(imageView);
        holder.setPath(path);
        msg.obj = holder;
        mUIHandler.sendMessage(msg);
    }

    /**
     * 将图片加载到缓存中
     *
     * @param path
     * @param bm
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void addBitmapToLruCache(String path, Bitmap bm) {
        if (getBitmapFromLruCache(path) == null) {
            if (bm != null)
                mLruCache.put(path, bm);
        }
    }

    /**
     * 根据图片的需求宽高压缩图片
     *
     * @param path
     * @param width
     * @param height
     * @return
     */
    private Bitmap decodeSampledBitmapFromPath(String path, int width, int height) {
        // 获取到图片的实际宽高
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 不加载到内存中
        options.inJustDecodeBounds = true;
        // options获得图片的实际宽高
        BitmapFactory.decodeFile(path, options);
        // 获取图片压缩比例
        options.inSampleSize = getSampleSize(options, width, height);
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
     * 添加异步任务
     * synchronized 防止多线程同时访问时，一直设置信号量，导致卡死
     * @param runnable
     */
    private synchronized void addTask(Runnable runnable) {
        // 将任务添加到任务队列中
        mTaskQueue.add(runnable);
        // 当mPoolThreadHandler为空时，设置一个信号量，阻塞线程，直到实例化mPoolThreadHandler后，执行下一步
        if (mPoolThreadHandler == null)
            try {
                mSemaphorePoolThreadHandler.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        // 通知后台线程
        mPoolThreadHandler.sendEmptyMessage(0X111);
    }

    /**
     * 获取到Image的宽高
     *
     * @param imageView
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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
            width = imageView.getMaxWidth();
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
            height = imageView.getMaxHeight();
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
     * 优先从缓存中加载图片
     *
     * @param path 图片的路径
     * @return
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private Bitmap getBitmapFromLruCache(String path) {
        if (!TextUtils.isEmpty(path))
            return mLruCache.get(path);
        return null;
    }
}

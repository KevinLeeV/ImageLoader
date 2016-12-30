package com.kevinlee.imageloader.imageloader;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.LruCache;
import android.widget.ImageView;

import com.kevinlee.imageloader.imageloader.bean.LoadType;
import com.kevinlee.imageloader.imageloader.utils.CompressBitmapFromPath;

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

    // 信号量，设置线程池中的线程将当前任务执行完，再去任务队列中取任务
    private Semaphore mSemaphorePoolThread;

    private ImageLoader(int threadCount, LoadType type) {
        initLoader(threadCount, type);
    }

    // 获取ImageLoader的实例化
    public static ImageLoader getInstance(int threadCount, LoadType type) {
        if (mImageLoader == null) {
            // 使用同步对象锁，防止多线程同时访问
            synchronized (ImageLoader.class) {
                if (mImageLoader == null) {
                    mImageLoader = new ImageLoader(threadCount, type);
                }
            }
            mImageLoader = new ImageLoader(threadCount, type);
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
                        try {
                            // 在mThreadPool取任务时设置信号量，只有当上一个任务执行完，才可以获取许可取任务，否则等待。
                            mSemaphorePoolThread.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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

        // 设置信号量的数量与线程池中的线程数一致
        mSemaphorePoolThread = new Semaphore(mThreadCount);
    }

    /**
     * 获取任务
     *
     * @return
     */
    private Runnable getTask() {
        if (mType == LoadType.LIFO) {
            return mTaskQueue.removeLast();
        } else {
            return mTaskQueue.removeLast();
        }
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
                ImageView iv = holder.getImageView();
                Bitmap bitmap = holder.getBitmap();
                String imagePath = holder.getPath();
                if (iv.getTag().toString().equals(imagePath)) {
                    iv.setImageBitmap(bitmap);
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
                    // 1、获取到图片需要显示的尺寸,并压缩图片
                    Bitmap bm = CompressBitmapFromPath.getInstance().decodeSampledBitmapFromPath(path, imageView);
                    // 2、将图片加载到缓存中
                    addBitmapToLruCache(path, bm);
                    // 3、将图片显示到imageView中
                    refreshImageView(imageView, path, bm);

                    // 执行完一个任务后，释放信号量
                    mSemaphorePoolThread.release();
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
     * 添加异步任务
     * synchronized 防止多线程同时访问时，一直设置信号量，导致卡死
     *
     * @param runnable
     */
    private synchronized void addTask(Runnable runnable) {
        // 将任务添加到任务队列中
        mTaskQueue.add(runnable);
        try {
            // 当mPoolThreadHandler为空时，设置一个信号量，阻塞线程，直到实例化mPoolThreadHandler后，执行下一步
            if (mPoolThreadHandler == null)
                mSemaphorePoolThreadHandler.acquire();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 通知后台线程
        mPoolThreadHandler.sendEmptyMessage(0X111);
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

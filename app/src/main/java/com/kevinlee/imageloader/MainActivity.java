package com.kevinlee.imageloader;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kevinlee.imageloader.imageloader.ImageLoader;
import com.kevinlee.imageloader.imageloader.LoadType;
import com.kevinlee.imageloader.imageloader.bean.Folder;
import com.kevinlee.imageloader.recycleradapter.CommonRecyclerAdapter;
import com.kevinlee.imageloader.recycleradapter.CommonViewHolder;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int DATA_LOADED = 0X111;

    private RecyclerView recyclerView;
    private RelativeLayout rlBottom;
    private TextView tvFolder, tvImgCount;

    private List<String> imageList;
    private List<Folder> folderList = new ArrayList<>();

    private File mCurrentFolder;
    private int mMaxCount;

    private ProgressDialog mProgressDialog;

    private CommonRecyclerAdapter<String> adapter;

    private Set<String> seletedData = new HashSet<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == DATA_LOADED) {
                mProgressDialog.dismiss();
                data2View();
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initData();
        initEvent();

    }

    /**
     * 初始化View
     */
    private void initViews() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        rlBottom = (RelativeLayout) findViewById(R.id.rl_bottom);
        tvFolder = (TextView) findViewById(R.id.tv_folder);
        tvImgCount = (TextView) findViewById(R.id.tv_img_count);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        /**
         * 利用ContentProvider获取手机中的所有图片
         */
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "当前手机存储卡不可用", Toast.LENGTH_SHORT).show();
        } else {
            // 显示对话框
            mProgressDialog = ProgressDialog.show(this, null, "正在加载...");
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    Uri mImgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    ContentResolver resolver = MainActivity.this.getContentResolver();
                    // 获取手机中格式为jpeg、png的图片，并按图片的时间排序
                    Cursor cursor = resolver.query(mImgUri, null, MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?", new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);
                    // 将父文件夹存储，防止重复遍历
                    Set<String> mParentFileSet = new HashSet<String>();
                    while (cursor.moveToNext()) {
                        // 获取到图片的路径
                        String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        // 获取到父文件夹
                        File parentFile = new File(imagePath).getParentFile();
                        if (parentFile == null)
                            continue;
                        String parentFilePath = parentFile.getAbsolutePath();

                        if (mParentFileSet.contains(parentFilePath)) {
                            continue;
                        }
                        mParentFileSet.add(parentFilePath);
                        Folder folder = new Folder();
                        folder.setDir(parentFilePath);
                        folder.setFirstImagePath(imagePath);
                        if (parentFile.list() == null)
                            continue;
                        int folderSize = parentFile.list(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String filename) {
                                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png"))
                                    return true;
                                return false;
                            }
                        }).length;
                        folder.setImageCount(folderSize);
                        folderList.add(folder);

                        if (folderSize > mMaxCount) {
                            mMaxCount = folderSize;
                            mCurrentFolder = parentFile;
                        }
                    }
                    cursor.close();
                    handler.sendEmptyMessage(DATA_LOADED);
                }
            }.start();
        }
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
    }

    /**
     * 将数据集设置到View中
     */
    private void data2View() {

        if (mCurrentFolder == null) {
            Toast.makeText(this, "未扫描到图片！", Toast.LENGTH_SHORT).show();
            return;
        }
        imageList = Arrays.asList(mCurrentFolder.list());
        tvFolder.setText(mCurrentFolder.getName());
        tvImgCount.setText(mMaxCount + "张");

        final ImageLoader loader = ImageLoader.getInstance(3, LoadType.LIFO);
        adapter = new CommonRecyclerAdapter<String>(this, R.layout.recyclerview_item_layout, imageList) {
            @Override
            public void convert(CommonViewHolder holder, String data) {
                final String filePath = mCurrentFolder.getAbsolutePath() + File.separator + data;
                final ImageView iv = holder.getView(R.id.iv);
                final ImageView ivSelect = holder.getView(R.id.iv_select);
                // 重置状态
                iv.setImageResource(R.drawable.no_picture);
                ivSelect.setImageResource(R.drawable.unselected);
                iv.setColorFilter(null);
                loader.loadImage(filePath, iv);
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 已被选中，清楚状态
                        if (seletedData.contains(filePath)) {
                            seletedData.remove(filePath);
                            iv.setColorFilter(null);
                            ivSelect.setImageResource(R.drawable.unselected);
                        } else {// 未被选中，保留状态
                            seletedData.add(filePath);
                            iv.setColorFilter(Color.parseColor("#77000000"));
                            ivSelect.setImageResource(R.drawable.selected);
                        }
                    }
                });
                // 更新ImageView
                if (seletedData.contains(filePath)) {
                    iv.setColorFilter(Color.parseColor("#77000000"));
                    ivSelect.setImageResource(R.drawable.selected);
                }

            }
        };
        recyclerView.setAdapter(adapter);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
    }

}

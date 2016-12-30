package com.kevinlee.imageloader.imageloader.popupwindow;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.kevinlee.imageloader.R;
import com.kevinlee.imageloader.imageloader.ImageLoader;
import com.kevinlee.imageloader.imageloader.bean.LoadType;
import com.kevinlee.imageloader.imageloader.bean.Folder;
import com.kevinlee.imageloader.imageloader.listener.OnGetCurrentFolderListener;
import com.kevinlee.imageloader.imageloader.listener.OnItemClickListener;
import com.kevinlee.imageloader.recycleradapter.CommonRecyclerAdapter;
import com.kevinlee.imageloader.recycleradapter.CommonViewHolder;

import java.io.File;
import java.util.List;

/**
 * ClassName:
 * Description:
 * Author:KevinLee
 * Date:2016/12/16 0016
 * Time:下午 3:22
 * Email:KevinLeeV@163.com
 */
public class ChooseFolderWindow extends PopupWindow {

    private RecyclerView recyclerView;
    private List<Folder> mList;
    private Context context;
    private File mCurrentFolder;
    private int prePosition = -1;
    private OnGetCurrentFolderListener mListener;

    public ChooseFolderWindow(Context context, List<Folder> list, File currentFolder, OnGetCurrentFolderListener listener) {
        super(context);
        this.mList = list;
        this.context = context;
        this.mCurrentFolder = currentFolder;
        this.mListener = listener;
        View view = LayoutInflater.from(context).inflate(R.layout.folder_popupwindow_layout, null);
        initViews(view);
        initData();
        this.setContentView(view);
        //设置宽和高
        this.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        this.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        //设置可获得焦点
        this.setFocusable(true);
        //设置背景色
        ColorDrawable dw = new ColorDrawable(0x88000000);
        this.setBackgroundDrawable(dw);
        //设置动画
        this.setAnimationStyle(R.style.ChooseFolderWindowAnim);
        //设置触摸事件,即当点击PopupWindow的外面时,弹窗消失
        view.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = v.findViewById(R.id.recyclerview).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

    }

    /**
     * 初始化View
     *
     * @param view
     */
    private void initViews(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        final ImageLoader loader = ImageLoader.getInstance(3, LoadType.LIFO);
        final CommonRecyclerAdapter<Folder> adapter = new CommonRecyclerAdapter<Folder>(context, R.layout.folder_popupwindow_item_layout, mList) {
            @Override
            public void convert(CommonViewHolder holder, Folder folder, int position) {
                holder.getView(R.id.iv_select).setVisibility(View.GONE);
                ImageView iv = holder.getView(R.id.iv);
                iv.setImageResource(R.drawable.no_picture);
                loader.loadImage(folder.getFirstImagePath(), iv);
                holder.setTextWithTextView(R.id.tv_folder_name, folder.getFolderName());
                holder.setTextWithTextView(R.id.tv_file_count, folder.getImageCount() + "张");
                if (folder.getDir().equals(mCurrentFolder.getAbsolutePath())) {
                    holder.getView(R.id.iv_select).setVisibility(View.VISIBLE);
                    prePosition = position;
                }
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                if (prePosition != position) {
                    Folder folder = mList.get(position);
                    String dir = folder.getDir();
                    mCurrentFolder = new File(dir);
                    adapter.notifyItemChanged(prePosition);
                    adapter.notifyItemChanged(position);
                    mListener.getFolder(mCurrentFolder);
                }
                dismiss();
            }

            @Override
            public void onItemLongClick(View itemView, int position) {

            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
    }
}

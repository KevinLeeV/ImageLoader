package com.kevinlee.imageloader.imageloader.bean;

/**
 * ClassName:
 * Description:
 * Author:KevinLee
 * Date:2016/12/8 0008
 * Time:下午 3:34
 * Email:KevinLeeV@163.com
 */
public class Folder {

    private String firstImagePath;// 第一张图片的路径
    private String folderName; // 文件夹名
    private String dir; // 文件夹路径
    private int imageCount;// 图片数量

    public String getFirstImagePath() {
        return firstImagePath;
    }

    public void setFirstImagePath(String firstImagePath) {
        this.firstImagePath = firstImagePath;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
        int lastIndexOf = this.dir.lastIndexOf("/");
        this.folderName = this.dir.substring(lastIndexOf);
    }

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }
}

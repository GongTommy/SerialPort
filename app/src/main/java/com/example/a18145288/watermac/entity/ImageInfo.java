package com.example.a18145288.watermac.entity;

/**
 * Created by 18145288 on 2019/6/18.
 */

public class ImageInfo {
    private String title;
    private String imageUrl;
    public ImageInfo(int index, String title, String s1, String imageUrl, String s3) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getTitle(){
        return title;
    }

    public String getImage(){
        return imageUrl;
    }
}

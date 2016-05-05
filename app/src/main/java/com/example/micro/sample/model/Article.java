package com.example.micro.sample.model;

public class Article {

    private String title;
    private String thumbSrc;
    private int thumbWidth;
    private int thumbHeight;

    public Article(String title, String thumbSrc, int thumbWidth, int thumbHeight) {
        this.title = title;
        this.thumbSrc = thumbSrc;
        this.thumbWidth = thumbWidth;
        this.thumbHeight = thumbHeight;
    }

    public String getTitle() {
        return title;
    }

    public String getThumbSrc() {
        return thumbSrc;
    }

    public int getThumbWidth() {
        return thumbWidth;
    }

    public int getThumbHeight() {
        return thumbHeight;
    }
}
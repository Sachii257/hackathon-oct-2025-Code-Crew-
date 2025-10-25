package com.hackathon.nyaymitra.models;

public class NewsItem {
    private String title;
    private String snippet;

    public NewsItem(String title, String snippet) {
        this.title = title;
        this.snippet = snippet;
    }

    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }
}
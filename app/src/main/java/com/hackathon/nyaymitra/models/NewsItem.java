package com.hackathon.nyaymitra.models;

public class NewsItem {
    private String headline;
    // Optional: Add a String url if you have actual links

    public NewsItem(String headline /*, String url */) {
        this.headline = headline;
        // this.url = url;
    }

    public String getHeadline() {
        return headline;
    }

    // public String getUrl() { return url; }
}
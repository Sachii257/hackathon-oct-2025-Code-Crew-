package com.hackathon.nyaymitra.models;

public class NewsItem {
    // Kept title and snippet from main
    private String title;
    private String snippet;

    public NewsItem(String title, String snippet) {
        this.title = title;
        this.snippet = snippet;
    }

    // Kept getters from main
    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }
}
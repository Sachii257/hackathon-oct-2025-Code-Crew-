package com.hackathon.nyaymitra.models;

public class ChatMessage {

    private String text;
    private boolean isUser;

    public ChatMessage(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
    }

    // --- ADD THESE TWO METHODS ---

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }
}
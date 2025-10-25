package com.hackathon.nyaymitra.network;

import com.google.gson.annotations.SerializedName;

public class ChatResponse {

    @SerializedName("reply")
    private String reply;

    public String getReply() {
        return reply;
    }
}
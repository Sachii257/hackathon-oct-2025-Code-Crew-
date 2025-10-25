package com.hackathon.nyaymitra.network;

import com.google.gson.annotations.SerializedName;
import com.hackathon.nyaymitra.models.ChatMessage;

import java.util.List;

// This object will be sent as the JSON body
public class ChatRequest {

    @SerializedName("prompt")
    private String prompt;

    @SerializedName("history")
    private List<ChatMessage> history; // We can re-use your ChatMessage model

    public ChatRequest(String prompt, List<ChatMessage> history) {
        this.prompt = prompt;
        this.history = history;
    }
}
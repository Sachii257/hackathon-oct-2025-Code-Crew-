package com.hackathon.nyaymitra.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    // --- ADD THIS NEW ENDPOINT ---
    @POST("api/chat")
    Call<ChatResponse> getChatReply(@Body ChatRequest chatRequest);
}
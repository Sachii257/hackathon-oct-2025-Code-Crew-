package com.hackathon.nyaymitra.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    // --- IMPORTANT ---
    // Make sure this URL matches your ngrok or server address
    // It must end with a "/"
    private static final String BASE_URL = "https://nyay-mitra-flask.onrender.com/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Optional: Increase timeout for long-running AI requests
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS) // 30 second connection timeout
                    .readTimeout(30, TimeUnit.SECONDS)    // 30 second read timeout
                    .writeTimeout(30, TimeUnit.SECONDS)   // 30 second write timeout
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient) // Use the custom client with timeouts
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
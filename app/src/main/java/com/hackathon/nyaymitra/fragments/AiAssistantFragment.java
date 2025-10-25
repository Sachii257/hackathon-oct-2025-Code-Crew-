package com.hackathon.nyaymitra.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hackathon.nyaymitra.network.ApiClient;
import com.hackathon.nyaymitra.network.ApiService;
import com.hackathon.nyaymitra.network.ChatRequest;
import com.hackathon.nyaymitra.network.ChatResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.adapters.ChatAdapter;
import com.hackathon.nyaymitra.models.ChatMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AiAssistantFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnScan;
    private ImageButton btnClearChat; // *** NEW BUTTON ***

    private static final String TAG = "AiAssistantFragment";
    private static final String CHAT_HISTORY_FILE = "chat_history.json";

    // --- Networking ---
    private ApiService apiService;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    // Activity Result Launcher for Camera Permission
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(getContext(), "Camera permission is required", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_assistant, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_chat);
        etMessage = view.findViewById(R.id.edit_text_message);
        btnSend = view.findViewById(R.id.btn_send);
        btnScan = view.findViewById(R.id.btn_scan);
        btnClearChat = view.findViewById(R.id.btn_clear_chat); // *** INITIALIZE BUTTON ***

        // Setup RecyclerView
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        loadChatHistory();

        if (chatMessageList.isEmpty()) {
            addMessageToChat("Hello! Ask me a question or scan a document.", false, true);
        }

        // Send Button Click
        btnSend.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
                etMessage.setText("");
            }
        });

        // Scan Button Click
        btnScan.setOnClickListener(v -> checkCameraPermissionAndStart());

        // *** NEW CLICK LISTENER FOR CLEAR BUTTON ***
        btnClearChat.setOnClickListener(v -> {
            clearChat();
        });

        return view;
    }

    // *** NEW METHOD TO CLEAR THE CHAT ***
    private void clearChat() {
        // 1. Clear the list in memory
        chatMessageList.clear();

        // 2. Notify the adapter to update the screen
        chatAdapter.notifyDataSetChanged();

        // 3. Delete the saved chat history file
        if (getContext() != null) {
            getContext().deleteFile(CHAT_HISTORY_FILE);
        }

        // 4. Add the default welcome message back and save it
        addMessageToChat("Hello! Ask me a question or scan a document.", false, true);

        Toast.makeText(getContext(), "Chat cleared", Toast.LENGTH_SHORT).show();
    }

    // (Rest of your methods: sendMessage, getAiResponse, addMessageToChat, etc.)

    private void sendMessage(String messageText) {
        addMessageToChat(messageText, true, true);
        getAiResponse(messageText);
    }

    private void getAiResponse(String userMessage) {
        addMessageToChat("...", false, false);

        List<ChatMessage> history = new ArrayList<>();
        if (chatMessageList.size() > 1) {
            history.addAll(chatMessageList.subList(0, chatMessageList.size() - 1));
        }

        ChatRequest chatRequest = new ChatRequest(userMessage, history);

        apiService.getChatReply(chatRequest).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                chatMessageList.remove(chatMessageList.size() - 1);
                chatAdapter.notifyItemRemoved(chatMessageList.size());

                if (response.isSuccessful() && response.body() != null) {
                    String aiReply = response.body().getReply();
                    addMessageToChat(aiReply, false, true);
                } else {
                    Log.e(TAG, "Backend Error: " + response.code());
                    addMessageToChat("Error: Could not connect to the server.", false, false);
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Log.e(TAG, "Retrofit Failure: " + t.getMessage());
                chatMessageList.remove(chatMessageList.size() - 1);
                chatAdapter.notifyItemRemoved(chatMessageList.size());
                addMessageToChat("Sorry, I couldn't get a response. Please check your connection.", false, false);
            }
        });
    }

    private void addMessageToChat(String text, boolean isUser, boolean save) {
        handler.post(() -> {
            ChatMessage message = new ChatMessage(text, isUser);
            chatMessageList.add(message);
            chatAdapter.notifyItemInserted(chatMessageList.size() - 1);
            recyclerView.scrollToPosition(chatMessageList.size() - 1);

            if (save) {
                saveChatHistory();
            }
        });
    }

    private void saveChatHistory() {
        backgroundExecutor.execute(() -> {
            try {
                JSONArray jsonArray = new JSONArray();
                for (ChatMessage msg : chatMessageList) {
                    JSONObject msgJson = new JSONObject();
                    msgJson.put("text", msg.getText());
                    msgJson.put("isUser", msg.isUser());
                    jsonArray.put(msgJson);
                }

                if (getContext() != null) {
                    try (FileOutputStream fos = getContext().openFileOutput(CHAT_HISTORY_FILE, Context.MODE_PRIVATE)) {
                        fos.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to save chat history", e);
            }
        });
    }

    private void loadChatHistory() {
        if (getContext() == null) return;
        try (FileInputStream fis = getContext().openFileInput(CHAT_HISTORY_FILE);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject msgJson = jsonArray.getJSONObject(i);
                ChatMessage msg = new ChatMessage(
                        msgJson.getString("text"),
                        msgJson.getBoolean("isUser")
                );
                chatMessageList.add(msg);
            }
            chatAdapter.notifyDataSetChanged();
            Log.d(TAG, "Chat history loaded successfully.");

        } catch (Exception e) {
            Log.i(TAG, "No chat history file found or failed to read.", e);
        }
    }

    private void checkCameraPermissionAndStart() {
        if (getContext() != null && ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        // TODO: Implement CameraX logic here.
        Toast.makeText(getContext(), "Camera Opening... (Implement CameraX)", Toast.LENGTH_SHORT).show();
        sendMessage("Summarize this document.");
    }
}
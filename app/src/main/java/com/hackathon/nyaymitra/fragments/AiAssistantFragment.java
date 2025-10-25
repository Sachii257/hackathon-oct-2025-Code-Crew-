package com.hackathon.nyaymitra.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
// Removed duplicate/unused imports
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
import androidx.appcompat.app.AppCompatActivity; // Kept for RESULT_OK
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// --- Kept Retrofit Imports ---
import com.hackathon.nyaymitra.network.ApiClient;
import com.hackathon.nyaymitra.network.ApiService;
import com.hackathon.nyaymitra.network.ChatRequest;
import com.hackathon.nyaymitra.network.ChatResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
// -----------------------------

import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.activities.ScannerActivity; // Added from HEAD
import com.hackathon.nyaymitra.adapters.ChatAdapter;
import com.hackathon.nyaymitra.models.ChatMessage;
import com.hackathon.nyaymitra.utils.NotificationHelper; // Added from HEAD

// --- Kept File I/O and JSON Imports ---
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
// ------------------------------------

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService; // Kept ExecutorService
import java.util.concurrent.Executors;

public class AiAssistantFragment extends Fragment {

    // --- Kept UI Elements from main (includes btnClearChat) ---
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnScan;
    private ImageButton btnClearChat;
    // ---------------------------------------------------------

    private static final String TAG = "AiAssistantFragment";
    private static final String CHAT_HISTORY_FILE = "chat_history.json";

    // --- Kept Networking Elements from main ---
    private ApiService apiService;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    // ----------------------------------------

    // --- Activity Launchers ---
    // Kept Camera Permission Launcher (modified callback)
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchScanner(); // Changed to call launchScanner like in HEAD
                } else {
                    Toast.makeText(getContext(), "Camera permission is required", Toast.LENGTH_SHORT).show();
                }
            });

    // Added Scanner Activity Launcher from HEAD
    private final ActivityResultLauncher<Intent> scannerActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String scannedText = data.getStringExtra(ScannerActivity.EXTRA_SCANNED_TEXT);
                        if (scannedText != null && !scannedText.isEmpty()) {
                            // Text received! Add message and ask AI to summarize.
                            String prompt = "Please summarize this legal document: " + scannedText;
                            addMessageToChat(prompt, true, true); // Display the full prompt for clarity
                            getAiResponse(prompt); // Send the combined prompt
                        } else {
                            addMessageToChat("Received empty text from scanner.", false, false);
                        }
                    } else {
                        addMessageToChat("No data received from scanner.", false, false);
                    }
                } else {
                    addMessageToChat("Scanning cancelled or failed.", false, false);
                }
            });
    // -------------------------


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_assistant, container, false);

        // --- Kept Initialization structure from main ---
        recyclerView = view.findViewById(R.id.recycler_view_chat);
        etMessage = view.findViewById(R.id.edit_text_message);
        btnSend = view.findViewById(R.id.btn_send);
        btnScan = view.findViewById(R.id.btn_scan);
        btnClearChat = view.findViewById(R.id.btn_clear_chat);

        // Setup RecyclerView
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);

        // Initialize Retrofit
        apiService = ApiClient.getClient().create(ApiService.class);
        // Load chat history
        loadChatHistory();

        // Conditional welcome message
        if (chatMessageList.isEmpty()) {
            addMessageToChat("Hello! Ask me a question or scan a document.", false, true);
        }

        // Setup Click Listeners (kept from main, includes btnClearChat)
        btnSend.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
                etMessage.setText("");
            }
        });

        // Scan button now calls checkCameraPermissionAndStart (like HEAD, but main's method name)
        btnScan.setOnClickListener(v -> checkCameraPermissionAndStart());

        // Clear button listener
        btnClearChat.setOnClickListener(v -> clearChat());
        // ------------------------------------------------

        return view;
    }

    // --- Kept Clear Chat Method from main ---
    private void clearChat() {
        chatMessageList.clear();
        chatAdapter.notifyDataSetChanged();
        if (getContext() != null) {
            getContext().deleteFile(CHAT_HISTORY_FILE);
        }
        addMessageToChat("Hello! Ask me a question or scan a document.", false, true);
        Toast.makeText(getContext(), "Chat cleared", Toast.LENGTH_SHORT).show();
    }
    // ----------------------------------------

    // --- Core Logic Methods (kept sendMessage and getAiResponse from main) ---
    private void sendMessage(String messageText) {
        addMessageToChat(messageText, true, true);
        getAiResponse(messageText);
    }

    private void getAiResponse(String userMessage) {
        addMessageToChat("...", false, false); // "Typing..." indicator

        // Build history list
        List<ChatMessage> history = new ArrayList<>();
        if (chatMessageList.size() > 1) {
            history.addAll(chatMessageList.subList(0, chatMessageList.size() - 1));
        }

        ChatRequest chatRequest = new ChatRequest(userMessage, history);

        // Call Flask backend via Retrofit
        apiService.getChatReply(chatRequest).enqueue(new Callback<ChatResponse>() {
            @SuppressLint("RestrictedApi") // Suppress warning for isVisible() check
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> response) {
                removeLastMessage(); // Remove "Typing..."

                if (response.isSuccessful() && response.body() != null) {
                    String aiReply = response.body().getReply();
                    addMessageToChat(aiReply, false, true); // Add AI response

                    // --- Added Notification Logic from HEAD ---
                    // Check if the fragment is currently visible to the user
                    if (!isVisible() && getContext() != null) {
                        NotificationHelper.showNotification(
                                getContext(),
                                "AI Response Ready",
                                "Your AI assistant has responded. Tap to view.",
                                102 // Notification ID
                        );
                    }
                    // ---------------------------------------

                } else {
                    Log.e(TAG, "Backend Error: " + response.code());
                    addMessageToChat("Error: Could not connect to the server.", false, false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Retrofit Failure: " + t.getMessage());
                removeLastMessage(); // Remove "Typing..."
                addMessageToChat("Sorry, I couldn't get a response. Please check your connection.", false, false);
            }
        });
    }
    // --------------------------------------------------------------------------

    // --- UI Update Helper Methods ---
    // Kept addMessageToChat from main (3 args) - uses handler
    private void addMessageToChat(String text, boolean isUser, boolean save) {
        if (text == null || text.isEmpty()) return; // Added null check from HEAD

        // Ensure UI updates happen on the main thread
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

    // Added removeLastMessage from HEAD (adapted to use handler)
    private void removeLastMessage() {
        // Ensure UI updates happen on the main thread
        handler.post(() -> {
            if (!chatMessageList.isEmpty()) {
                int lastIndex = chatMessageList.size() - 1;
                chatMessageList.remove(lastIndex);
                chatAdapter.notifyItemRemoved(lastIndex);
            }
        });
    }
    // -----------------------------

    // --- Kept Caching Logic from main ---
    private void saveChatHistory() {
        backgroundExecutor.execute(() -> {
            try {
                JSONArray jsonArray = new JSONArray();
                // Use a copy to avoid ConcurrentModificationException if list changes
                List<ChatMessage> messagesToSave = new ArrayList<>(chatMessageList);
                for (ChatMessage msg : messagesToSave) {
                    if (msg.getText().equals("...")) continue; // Don't save "typing..."
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
            chatMessageList.clear(); // Clear before loading
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject msgJson = jsonArray.getJSONObject(i);
                ChatMessage msg = new ChatMessage(
                        msgJson.getString("text"),
                        msgJson.getBoolean("isUser")
                );
                chatMessageList.add(msg);
            }
            // Use handler to ensure notifyDataSetChanged runs on main thread
            handler.post(() -> {
                chatAdapter.notifyDataSetChanged();
                if (!chatMessageList.isEmpty()) {
                    recyclerView.scrollToPosition(chatMessageList.size() - 1);
                }
            });
            Log.d(TAG, "Chat history loaded successfully.");

        } catch (Exception e) {
            Log.i(TAG, "No chat history file found or failed to read.", e);
            // Don't clear the list here, allow the initial welcome message if needed
        }
    }
    // ---------------------------------

    // --- Camera/Scanner Logic (Merged) ---
    // Kept checkCameraPermissionAndStart (using requireContext)
    private void checkCameraPermissionAndStart() {
        if (getContext() == null) return; // Add null check for safety
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchScanner(); // Permission already granted
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA); // Request permission
        }
    }

    // Added launchScanner from HEAD
    private void launchScanner() {
        Intent intent = new Intent(getActivity(), ScannerActivity.class);
        scannerActivityLauncher.launch(intent);
    }
    // ----------------------------------
}
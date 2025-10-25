package com.hackathon.nyaymitra.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.appcompat.app.AppCompatActivity; // Import needed for RESULT_OK
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// --- Imports needed for Gemini ---
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
// ---------------------------------

import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.activities.ScannerActivity; // Import needed for Scanner
import com.hackathon.nyaymitra.adapters.ChatAdapter;
import com.hackathon.nyaymitra.models.ChatMessage;
import com.hackathon.nyaymitra.utils.NotificationHelper; // Import needed for Notifications

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AiAssistantFragment extends Fragment {

    // --- PASTE YOUR API KEY HERE ---
    private final String API_KEY = "YOUR_API_KEY_HERE"; // IMPORTANT: Replace this

    // --- UI Elements ---
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnScan;

    // --- Gemini AI Elements ---
    private GenerativeModelFutures geminiModel;
    private Executor mainExecutor; // To run UI updates from background threads

    // --- Activity Launchers ---
    // Launcher for Camera Permission request
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchScanner(); // Permission granted, launch the scanner
                } else {
                    Toast.makeText(getContext(), "Camera permission is required", Toast.LENGTH_SHORT).show();
                }
            });

    // Launcher for getting the result back from ScannerActivity
    private final ActivityResultLauncher<Intent> scannerActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String scannedText = data.getStringExtra(ScannerActivity.EXTRA_SCANNED_TEXT);
                        if (scannedText != null && !scannedText.isEmpty()) {
                            // Text received! Add to chat and ask AI to summarize.
                            addMessageToChat(scannedText, true);
                            sendMessage("Please summarize this legal document: " + scannedText);
                        }
                    }
                }
            });


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_assistant, container, false);

        initViews(view);
        initChatList();
        initGemini(); // Initialize the AI model
        setupClickListeners();

        addMessageToChat("Hello! Ask me a question or scan a document.", false); // Initial welcome message

        return view;
    }

    // --- Initialization Methods ---
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_chat);
        etMessage = view.findViewById(R.id.edit_text_message);
        btnSend = view.findViewById(R.id.btn_send);
        btnScan = view.findViewById(R.id.btn_scan);
    }

    private void initChatList() {
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true); // New messages appear at the bottom
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);
    }

    private void initGemini() {
        // Use a Handler to post UI updates back to the main thread
        mainExecutor = new Handler(Looper.getMainLooper())::post;

        // Initialize the Generative Model (using Futures API for Java compatibility)
        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash", // Or another suitable model like "gemini-pro"
                API_KEY
        );
        geminiModel = GenerativeModelFutures.from(gm);
    }

    private void setupClickListeners() {
        // Send button listener
        btnSend.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText); // Send the text to the AI
                etMessage.setText(""); // Clear the input field
            }
        });

        // Scan button listener
        btnScan.setOnClickListener(v -> {
            checkCameraPermissionAndStart(); // Start the camera/scanner flow
        });
    }

    // --- Core Logic Methods ---
    private void sendMessage(String messageText) {
        addMessageToChat(messageText, true); // Show user's message immediately
        addMessageToChat("Typing...", false); // Show a temporary "Typing..." indicator
        callGeminiApi(messageText); // Call the actual AI
    }

    // This method handles the asynchronous call to the Gemini API
    private void callGeminiApi(String prompt) {
        // Create the content object for the API
        Content content = new Content.Builder().addText(prompt).build();

        // Make the asynchronous call using ListenableFuture
        ListenableFuture<GenerateContentResponse> future = geminiModel.generateContent(content);

        // Add callbacks for success and failure, executing on a background thread
        Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String aiResponse = result.getText(); // Get the AI's response text

                // Switch back to the main thread to update the UI
                mainExecutor.execute(() -> {
                    removeLastMessage(); // Remove the "Typing..." message
                    addMessageToChat(aiResponse, false); // Add the AI's actual response

                    // If the user isn't looking at the chat, send a notification
                    if (!isVisible() && getContext() != null) {
                        NotificationHelper.showNotification(
                                getContext(),
                                "AI Response Ready", // Or "Scan Analysis Complete" if prompt contained scan data
                                "Your AI assistant has responded. Tap to view.",
                                102 // Use a consistent ID or generate unique ones
                        );
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace(); // Log the error

                // Switch back to the main thread to update the UI with an error message
                mainExecutor.execute(() -> {
                    removeLastMessage(); // Remove the "Typing..." message
                    addMessageToChat("Error: Could not get response. " + t.getMessage(), false);
                    // Optionally show an error notification if the fragment isn't visible
                    // if (!isVisible() && getContext() != null) { ... }
                });
            }
        }, Executors.newSingleThreadExecutor()); // Use a background executor for the network call
    }

    // --- UI Update Helper Methods ---
    private void addMessageToChat(String text, boolean isUser) {
        if (text == null || text.isEmpty()) return; // Avoid adding empty messages

        // Ensure UI updates happen on the main thread (especially if called from background)
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainExecutor.execute(() -> addMessageToChat(text, isUser));
            return;
        }

        ChatMessage message = new ChatMessage(text, isUser);
        chatMessageList.add(message);
        chatAdapter.notifyItemInserted(chatMessageList.size() - 1);
        recyclerView.scrollToPosition(chatMessageList.size() - 1); // Scroll to the newest message
    }

    private void removeLastMessage() {
        // Ensure UI updates happen on the main thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainExecutor.execute(this::removeLastMessage);
            return;
        }

        if (!chatMessageList.isEmpty()) {
            int lastIndex = chatMessageList.size() - 1;
            chatMessageList.remove(lastIndex);
            chatAdapter.notifyItemRemoved(lastIndex);
        }
    }

    // --- Camera/Scanner Logic ---
    private void checkCameraPermissionAndStart() {
        // Use requireContext() for non-null context within fragment lifecycle methods
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchScanner(); // Permission already granted
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA); // Request permission
        }
    }

    private void launchScanner() {
        // Launch the ScannerActivity using the dedicated launcher
        Intent intent = new Intent(getActivity(), ScannerActivity.class);
        scannerActivityLauncher.launch(intent);
    }
}
package com.hackathon.nyaymitra.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.adapters.ChatAdapter;
import com.hackathon.nyaymitra.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class AiAssistantFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnScan; // New button

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
        btnScan = view.findViewById(R.id.btn_scan); // Init the new button

        // Setup RecyclerView
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);

        // Add a welcome message
        addMessageToChat("Hello! Ask me a question or scan a document.", false);

        // Send Button Click
        btnSend.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
                etMessage.setText("");
            }
        });

        // *** SCAN BUTTON CLICK LISTENER ***
        btnScan.setOnClickListener(v -> {
            checkCameraPermissionAndStart();
        });

        return view;
    }

    private void sendMessage(String messageText) {
        addMessageToChat(messageText, true);
        getMockAIResponse(messageText);
    }

    private void getMockAIResponse(String userMessage) {
        String response = "You asked about: '" + userMessage + "'. This is a placeholder AI response.";
        addMessageToChat(response, false);
    }

    private void addMessageToChat(String text, boolean isUser) {
        ChatMessage message = new ChatMessage(text, isUser);
        chatMessageList.add(message);
        chatAdapter.notifyItemInserted(chatMessageList.size() - 1);
        recyclerView.scrollToPosition(chatMessageList.size() - 1);
    }

    // *** CAMERA LOGIC (MOVED HERE) ***
    private void checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        // TODO: Implement CameraX logic here.
        Toast.makeText(getContext(), "Camera Opening... (Implement CameraX)", Toast.LENGTH_SHORT).show();

        // This is a placeholder for what happens *after* scanning
        addMessageToChat("[Placeholder: Scanned document text would appear here]", true);
        getMockAIResponse("Summarize this document.");
    }
}
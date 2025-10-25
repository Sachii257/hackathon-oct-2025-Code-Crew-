package com.hackathon.nyaymitra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    private List<ChatMessage> chatMessageList;

    public ChatAdapter(List<ChatMessage> chatMessageList) {
        this.chatMessageList = chatMessageList;
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessageList.get(position).isUser()) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_AI;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_USER) {
            // Inflate user message layout
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_chat_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            // Inflate AI message layout
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_chat_ai, parent, false);
            return new AiMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessageList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_USER) {
            ((UserMessageViewHolder) holder).bind(message);
        } else {
            ((AiMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    // --- ViewHolder for User Messages ---
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserMessage;

        UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            // This ID MUST match the one in list_item_chat_user.xml
            tvUserMessage = itemView.findViewById(R.id.tv_user_message);
        }

        void bind(ChatMessage message) {
            tvUserMessage.setText(message.getText());
        }
    }

    // --- ViewHolder for AI Messages ---
    static class AiMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvAiMessage;

        AiMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            // This ID MUST match the one in list_item_chat_ai.xml
            tvAiMessage = itemView.findViewById(R.id.tv_ai_message);
        }

        void bind(ChatMessage message) {
            tvAiMessage.setText(message.getText());
        }
    }
}
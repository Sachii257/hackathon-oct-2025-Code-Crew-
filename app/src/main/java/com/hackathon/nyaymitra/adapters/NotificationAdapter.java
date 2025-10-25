package com.hackathon.nyaymitra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.models.NotificationItem;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationItem> notificationList;

    public NotificationAdapter(List<NotificationItem> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    // Method to update the list if needed
    public void updateData(List<NotificationItem> newList) {
        this.notificationList = newList;
        notifyDataSetChanged(); // Simple way to refresh, consider DiffUtil for efficiency
    }


    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvContent;
        TextView tvTimestamp;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvContent = itemView.findViewById(R.id.tv_notification_content);
            tvTimestamp = itemView.findViewById(R.id.tv_notification_timestamp);
        }

        void bind(NotificationItem item) {
            tvTitle.setText(item.getTitle());
            tvContent.setText(item.getContent());
            tvTimestamp.setText(item.getFormattedTimestamp());
        }
    }
}
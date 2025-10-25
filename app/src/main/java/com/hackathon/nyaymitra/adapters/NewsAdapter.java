package com.hackathon.nyaymitra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast; // Kept Toast import for click feedback
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hackathon.nyaymitra.R;
// Keep explicit import from main
import com.hackathon.nyaymitra.models.NewsItem;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    // Keep list declaration (functionally identical)
    private static List<NewsItem> newsList;

    public NewsAdapter(List<NewsItem> newsList) {
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem newsItem = newsList.get(position);
        // Kept direct text setting from main (consistent with NewsItem model)
        holder.tvTitle.setText(newsItem.getTitle());
        holder.tvSnippet.setText(newsItem.getSnippet());
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    // --- ViewHolder ---
    // Keep structure from main (includes tvSnippet)
    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvSnippet;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_news_title);
            tvSnippet = itemView.findViewById(R.id.tv_news_snippet);

            // --- Merged Item Click Listener from HEAD ---
            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition(); // Get the clicked item's position safely
                if (position != RecyclerView.NO_POSITION) {
                    NewsItem clickedItem = newsList.get(position);
                    // TODO: Replace Toast with opening a link or detail activity
                    // if (clickedItem.getUrl() != null && !clickedItem.getUrl().isEmpty()) { ... }
                    Toast.makeText(itemView.getContext(), "Clicked: " + clickedItem.getTitle(), Toast.LENGTH_SHORT).show();
                }
            });
            // ------------------------------------------
        }

        // Removed bind method from HEAD as onBindViewHolder handles setting text directly
    }
}
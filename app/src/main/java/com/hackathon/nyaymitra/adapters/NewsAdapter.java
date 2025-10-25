package com.hackathon.nyaymitra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast; // Import Toast for click feedback
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.models.NewsItem;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<NewsItem> newsList;

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
        holder.bind(newsItem);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        // Snippet TextView removed

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_news_title);
            // tvSnippet = itemView.findViewById(R.id.tv_news_snippet); // Removed
        }

        void bind(NewsItem newsItem) {
            tvTitle.setText(newsItem.getHeadline());
            // tvSnippet.setText(newsItem.getSnippet()); // Removed

            // --- IMPORTANT: Activate Marquee ---
            // The TextView needs to be selected for marquee to start
            tvTitle.setSelected(true);
            // ------------------------------------

            // --- Handle Item Click ---
            itemView.setOnClickListener(v -> {
                // TODO: Replace Toast with opening a link (Intent with ACTION_VIEW)
                // if (newsItem.getUrl() != null && !newsItem.getUrl().isEmpty()) {
                //     Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.getUrl()));
                //     itemView.getContext().startActivity(browserIntent);
                // } else {
                //     Toast.makeText(itemView.getContext(), "No link available for this item.", Toast.LENGTH_SHORT).show();
                // }
                Toast.makeText(itemView.getContext(), "Clicked: " + newsItem.getHeadline(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
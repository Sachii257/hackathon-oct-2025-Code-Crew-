package com.hackathon.nyaymitra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.models.NewsItem; // <-- This is the missing line

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<NewsItem> newsList; // <-- This line will now work

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
        holder.tvTitle.setText(newsItem.getTitle());
        holder.tvSnippet.setText(newsItem.getSnippet());
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvSnippet;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_news_title);
            tvSnippet = itemView.findViewById(R.id.tv_news_snippet);
            // TODO: Add itemView.setOnClickListener to open the full news
        }
    }
}
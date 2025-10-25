package com.hackathon.nyaymitra.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.adapters.NewsAdapter;
import com.hackathon.nyaymitra.models.NewsItem;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private List<NewsItem> newsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_news);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load data from strings.xml
        loadNewsHeadlines();

        newsAdapter = new NewsAdapter(newsList);
        recyclerView.setAdapter(newsAdapter);

        return view;
    }

    private void loadNewsHeadlines() {
        newsList = new ArrayList<>();
        // Get the headlines array from resources
        String[] headlines = getResources().getStringArray(R.array.legal_news_headlines);

        // Create NewsItem objects from the headlines
        for (String headline : headlines) {
            // TODO: If you have URLs associated with headlines, add them here
            newsList.add(new NewsItem(headline /*, correspondingUrl */));
        }
    }
}
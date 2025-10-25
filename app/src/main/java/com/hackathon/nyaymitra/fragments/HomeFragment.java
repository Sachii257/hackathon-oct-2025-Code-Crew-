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
// Kept explicit import from main
import com.hackathon.nyaymitra.models.NewsItem;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    // Kept declaration (functionally identical)
    private List<NewsItem> newsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_news);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Kept loading mock data from main (provides title + snippet)
        loadMockNews();

        newsAdapter = new NewsAdapter(newsList);
        recyclerView.setAdapter(newsAdapter);

        return view;
    }

    // Kept the loadMockNews method from main
    private void loadMockNews() {
        // TODO: Replace this with data from a real API
        newsList = new ArrayList<>();
        newsList.add(new NewsItem("New Consumer Protection Act Rules", "The government has notified new rules for e-commerce..."));
        newsList.add(new NewsItem("Supreme Court Ruling on Property", "A recent ruling clarifies inheritance laws for daughters..."));
        newsList.add(new NewsItem("What is an FIR?", "Learn the basics of a First Information Report and your rights..."));
        newsList.add(new NewsItem("Daily Legal Update: 25th Oct", "A summary of important legal happenings from today..."));
        // Add more mock items if desired
    }
}
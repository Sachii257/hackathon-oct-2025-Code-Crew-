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
import com.hackathon.nyaymitra.models.NewsItem; // <-- This is the missing line

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private List<NewsItem> newsList; // <-- This line will now work

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_news);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load mock data
        loadMockNews();

        newsAdapter = new NewsAdapter(newsList);
        recyclerView.setAdapter(newsAdapter);

        return view;
    }

    private void loadMockNews() {
        // TODO: Replace this with data from a real API
        newsList = new ArrayList<>();
        newsList.add(new NewsItem("New Consumer Protection Act Rules", "The government has notified new rules for e-commerce..."));
        newsList.add(new NewsItem("Supreme Court Ruling on Property", "A recent ruling clarifies inheritance laws for daughters..."));
        newsList.add(new NewsItem("What is an FIR?", "Learn the basics of a First Information Report and your rights..."));
        newsList.add(new NewsItem("Daily Legal Update: 25th Oct", "A summary of important legal happenings from today..."));
    }
}
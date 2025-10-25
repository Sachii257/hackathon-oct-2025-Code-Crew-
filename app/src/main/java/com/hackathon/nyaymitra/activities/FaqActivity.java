package com.hackathon.nyaymitra.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.adapters.FaqAdapter;

public class FaqActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_faq);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Get data from strings.xml
        String[] questions = getResources().getStringArray(R.array.faq_questions);
        String[] answers = getResources().getStringArray(R.array.faq_answers);

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view_faq);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FaqAdapter adapter = new FaqAdapter(questions, answers);
        recyclerView.setAdapter(adapter);
    }
}
package com.hackathon.nyaymitra.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hackathon.nyaymitra.R;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqViewHolder> {

    private final String[] questions;
    private final String[] answers;

    public FaqAdapter(String[] questions, String[] answers) {
        this.questions = questions;
        this.answers = answers;
    }

    @NonNull
    @Override
    public FaqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_faq, parent, false);
        return new FaqViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FaqViewHolder holder, int position) {
        holder.tvQuestion.setText(questions[position]);
        holder.tvAnswer.setText(answers[position]);
    }

    @Override
    public int getItemCount() {
        return questions.length;
    }

    static class FaqViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion;
        TextView tvAnswer;

        FaqViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_faq_question);
            tvAnswer = itemView.findViewById(R.id.tv_faq_answer);
        }
    }
}
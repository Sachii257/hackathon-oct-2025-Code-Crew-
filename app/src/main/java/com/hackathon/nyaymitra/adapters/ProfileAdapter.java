package com.hackathon.nyaymitra.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hackathon.nyaymitra.R;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private final String[] options;
    private final int[] icons;
    private final OnOptionClickListener listener;

    // Interface for click events
    public interface OnOptionClickListener {
        void onOptionClick(String option);
    }

    public ProfileAdapter(String[] options, int[] icons, OnOptionClickListener listener) {
        this.options = options;
        this.icons = icons;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_profile_option, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        String option = options[position];
        int icon = icons[position];
        holder.bind(option, icon, listener);
    }

    @Override
    public int getItemCount() {
        return options.length;
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;

        ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_option_icon);
            tvName = itemView.findViewById(R.id.tv_option_name);
        }

        void bind(String option, int icon, OnOptionClickListener listener) {
            tvName.setText(option);
            ivIcon.setImageResource(icon);
            itemView.setOnClickListener(v -> listener.onOptionClick(option));
        }
    }
}
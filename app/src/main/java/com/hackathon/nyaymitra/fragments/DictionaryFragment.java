package com.hackathon.nyaymitra.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hackathon.nyaymitra.R;
import com.hackathon.nyaymitra.activities.ClientContentActivity;
import com.hackathon.nyaymitra.activities.LawyerContentActivity;

public class DictionaryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dictionary, container, false);

        ImageButton lawyerButton = view.findViewById(R.id.lawyer_button);
        ImageButton clientButton = view.findViewById(R.id.client_button);

        lawyerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LawyerContentActivity.class);
                startActivity(intent);
            }
        });

        clientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ClientContentActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}

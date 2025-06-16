package com.level.hub.launcher.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.level.hub.R;

public class DialogFragment extends Fragment {

    private static final String ARG_MESSAGE = "message";
    private static final String ARG_ONLY_OK = "showOnlyOk";
    private DialogListener listener;

    public interface DialogListener {
        void onYesClicked();
        void onNoClicked();
        void onOkClicked();
    }

    public static DialogFragment newInstance(String message, boolean showOnlyOk) {
        DialogFragment fragment = new DialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        args.putBoolean(ARG_ONLY_OK, showOnlyOk);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DialogListener) {
            listener = (DialogListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement DialogListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textDialog = view.findViewById(R.id.text_dialog);
        Button btnYes = view.findViewById(R.id.btn_yes);
        Button btnNo = view.findViewById(R.id.btn_no);
        Button btnOk = view.findViewById(R.id.btn_ok);

        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        view.startAnimation(fadeIn);

        if (getArguments() != null) {
            textDialog.setText(getArguments().getString(ARG_MESSAGE));

            boolean showOnlyOk = getArguments().getBoolean(ARG_ONLY_OK, false);
            if(showOnlyOk) {
                btnYes.setVisibility(View.GONE);
                btnNo.setVisibility(View.GONE);
                btnOk.setVisibility(View.VISIBLE);
            }
        }

        btnYes.setOnClickListener(v -> {
            if (listener != null) listener.onYesClicked();
            close();
        });

        btnNo.setOnClickListener(v -> {
            if (listener != null) listener.onNoClicked();
            close();
        });

        btnOk.setOnClickListener(v -> {
            if (listener != null) listener.onOkClicked();
            close();
        });
    }

    private void close() {
        requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }
}
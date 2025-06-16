package com.rockstargames.gtasa.gui;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.level.hub.R;
import com.rockstargames.gtasa.gui.util.Utils;

import java.util.Formatter;

public class LoadingScreen {

    ConstraintLayout loadLayout;
    Activity aactivity;
    TextView percentText;
    ProgressBar progressBar;

    public LoadingScreen(Activity activity) {
        aactivity = activity;
        loadLayout = aactivity.findViewById(R.id.br_serverselect_layout);

        percentText = aactivity.findViewById(R.id.loading_percent);
        progressBar = aactivity.findViewById(R.id.progressBar);

        loadLayout.setVisibility(View.GONE);
    }

    public void Update(int percent) {
        if (percent <= 100) {
            loadLayout.setVisibility(View.VISIBLE);
            percentText.setText(new Formatter().format("%d%s", Integer.valueOf(percent), "%").toString());
            progressBar.setProgress(percent);
        } else {
            Hide();
        }
    }

    public void Show() {
        Utils.ShowLayout(loadLayout, true);
    }

    public void Hide() {
        Utils.HideLayout(loadLayout, true);
    }
}

package com.level.hub.launcher.fragments;

import static com.level.hub.launcher.config.Config.gameFiles;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.joom.paranoid.Obfuscate;
import com.level.hub.R;
import com.level.hub.launcher.activity.MainActivity;
import com.level.hub.launcher.util.SharedPreferenceCore;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

@Obfuscate
public class SettingsFragment extends Fragment {

    JSONObject settingsJson = new JSONObject();
    SwitchCompat mFPSSwitch, mCUTSwitch;
    SeekBar mFPSSeekBar;
    TextView mFPSText;
    File jsonFile;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_settings, viewGroup, false);

        ((MainActivity) getActivity()).hideKeyboard(getActivity());

        mFPSSwitch = view.findViewById(R.id.fps_switch);
        mFPSSeekBar = view.findViewById(R.id.fps_seekbar);
        mFPSText = view.findViewById(R.id.fps_count);

        mCUTSwitch = view.findViewById(R.id.cutout_switch);

        Button close = view.findViewById(R.id.close);
        close.setOnClickListener(v -> {
            HomeFragment homeFragment = new HomeFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            transaction.replace(R.id.fragment_container, homeFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        jsonFile = new File(gameFiles + "multiplayer/settings.json");
        loadSettingsJson();

        mFPSSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            new SharedPreferenceCore().setBoolean(requireContext(), "FPS_DISPLAY", b);
            try {
                settingsJson.getJSONObject("settings").put("fpscounter", b ? 1 : 0);
                saveSettingsJson();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        mCUTSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            new SharedPreferenceCore().setBoolean(requireContext(), "CUTOUT", b);
            try {
                settingsJson.getJSONObject("settings").put("cutout", b ? 1 : 0);
                saveSettingsJson();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        mFPSSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int realProgress = 30;
                switch (progress) {
                    case 0:
                        realProgress = 30;
                        break;
                    case 1:
                        realProgress = 60;
                        break;
                    case 2:
                        realProgress = 90;
                        break;
                    case 3:
                        realProgress = 120;
                        break;
                }

                new SharedPreferenceCore().setInt(requireContext(), "FPS_LIMIT", realProgress);
                try {
                    settingsJson.getJSONObject("settings").put("fps", realProgress);
                    saveSettingsJson();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mFPSText.setText(String.valueOf(realProgress));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        return view;
    }

    private void loadSettingsJson() {
        if (jsonFile.exists()) {
            try (FileInputStream fis = new FileInputStream(jsonFile)) {
                byte[] data = new byte[(int) jsonFile.length()];
                fis.read(data);
                String jsonStr = new String(data);
                settingsJson = new JSONObject(jsonStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveSettingsJson() {
        try (FileOutputStream fos = new FileOutputStream(jsonFile)) {
            fos.write(settingsJson.toString(4).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            mFPSSwitch.setChecked(settingsJson.getJSONObject("settings").getInt("fpscounter") == 1);
            mCUTSwitch.setChecked(settingsJson.getJSONObject("settings").getInt("cutout") == 1);

            int fps = settingsJson.getJSONObject("settings").getInt("fps");
            switch (fps) {
                case 30:
                    mFPSSeekBar.setProgress(0);
                    break;
                case 60:
                    mFPSSeekBar.setProgress(1);
                    break;
                case 90:
                    mFPSSeekBar.setProgress(2);
                    break;
                case 120:
                    mFPSSeekBar.setProgress(3);
                    break;
            }

            mFPSText.setText(String.valueOf(fps));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
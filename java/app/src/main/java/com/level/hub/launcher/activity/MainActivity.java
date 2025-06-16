package com.level.hub.launcher.activity;

import static com.level.hub.launcher.config.Config.gameFiles;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.joom.paranoid.Obfuscate;
import com.level.hub.R;
import com.level.hub.launcher.config.Config;
import com.level.hub.launcher.fragments.HomeFragment;
import com.level.hub.launcher.util.ConfigValidator;
import com.level.hub.launcher.util.SignatureChecker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

@Obfuscate
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideSystemUI();

        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }

        Config.currentContext = this;

        ConfigValidator.validateConfigFiles(this);

        if (!SignatureChecker.isSignatureValid(this, getPackageName())) {
            Toast.makeText(this, "Use original launcher! No remake", Toast.LENGTH_LONG).show();
            return;
        }

        File configPsf = new File(gameFiles, "config.psf");
        File jsonFile = new File(gameFiles, "multiplayer/settings.json");

        if (!configPsf.exists()) {
            try {
                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("config.psf");
                OutputStream out = new FileOutputStream(configPsf);

                byte[] buffer = new byte[1024];
                int length;

                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!jsonFile.exists()) {
            try {
                JSONObject defaultSettings = new JSONObject();
                JSONObject client = new JSONObject();
                client.put("name", "");
                client.put("password", "");

                JSONObject settings = new JSONObject();
                settings.put("Font", "arial.ttf");
                settings.put("FontSize", 30);
                settings.put("FontOutline", 2);
                settings.put("ChatMaxMessages", 8);
                settings.put("HealthBarWidth", 60);
                settings.put("HealthBarHeight", 10);
                settings.put("CameraCycleSize", 90);
                settings.put("CameraCycleX", 1810);
                settings.put("CameraCycleY", 400);
                settings.put("fps", 60);
                settings.put("cutout", 0);
                settings.put("androidKeyboard", 0);
                settings.put("fpscounter", 0);
                settings.put("radarrect", 0);
                settings.put("hud", 1);
                settings.put("dialog", 1);

                defaultSettings.put("client", client);
                defaultSettings.put("settings", settings);

                FileOutputStream fileOutputStream = new FileOutputStream(jsonFile);
                OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
                writer.write(defaultSettings.toString());
                writer.flush();
                writer.close();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        File file = new File(gameFiles + "/download/update.apk");
        if (file.exists()) {
            file.delete();
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            getWindow().getInsetsController().hide(WindowInsets.Type.statusBars());
        }
    }
}
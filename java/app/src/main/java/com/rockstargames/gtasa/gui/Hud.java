package com.rockstargames.gtasa.gui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.BuildConfig;
import com.level.hub.R;
import com.nvidia.devtech.NvEventQueueActivity;
import com.rockstargames.gtasa.Preferences;
import com.rockstargames.gtasa.gui.util.Utils;

import java.text.DecimalFormat;

public class Hud {
    private final Activity activity;
    private ConstraintLayout hudLayout;
    private ProgressBar hudHealth, hudArmour, hudFuel;
    private TextView hudMoney;
    private ImageView hudWeapon, hudRadar, hudMenu, hudSeat;
    private boolean isHudSetPos = false;
    private int radarWidth, screenHeight, screenWidth, radarHeight;
    private float realPrctX,realPrctY, gtaX, gtaY;

    public Hud(Activity activity) {
        this.activity = activity;
        initializeViews();
    }

    private void initializeViews() {
        hudLayout = activity.findViewById(R.id.hud_main);
        hudLayout.setVisibility(View.GONE);

        hudHealth = activity.findViewById(R.id.hud_health_pb);
        hudArmour = activity.findViewById(R.id.hud_armour_pb);
        hudFuel = activity.findViewById(R.id.hud_fuel_pb);
        hudMoney = activity.findViewById(R.id.hud_balance_text);
        hudWeapon = activity.findViewById(R.id.hud_weapon);
        hudRadar = activity.findViewById(R.id.radar_hude);
        hudMenu = activity.findViewById(R.id.menu);
        hudSeat = activity.findViewById(R.id.widgetgetin);
        hudSeat.setVisibility(View.GONE);
        setClickListeners();
    }

    private void setClickListeners() {
        hudMenu.setOnClickListener(view -> {
            NvEventQueueActivity.getInstance().sendJson(1);
        });
        hudSeat.setOnClickListener(view -> NvEventQueueActivity.getInstance().sendG());
    }

    public void UpdateHudInfo(int health, int armour, int hunger, int weaponId, int ammo, int playerId, int money, int wanted) {
        hudArmour.setProgress(Math.min(armour, 100));
        hudHealth.setProgress(Math.min(health, 100));
        hudFuel.setProgress(Math.min(ammo, 100));

        String formattedMoney = new DecimalFormat("#,###").format(money);
        hudMoney.setText(formattedMoney);

        int weaponResId = activity.getResources().getIdentifier("weapon_" + weaponId, "drawable", activity.getPackageName());
        hudWeapon.setImageResource(weaponResId);

        hudWeapon.setOnClickListener(v -> NvEventQueueActivity.getInstance().onWeaponChanged());
    }

    public void displayInfo() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        @SuppressLint("DefaultLocale") String info = String.format("%s | Size dp - %dx%d | Dpi - %d | IP: %s | Branch: %s | Nick: %s | Hash: %s",
                BuildConfig.VERSION_NAME,
                (int) (displayMetrics.widthPixels / displayMetrics.density),
                (int) (displayMetrics.heightPixels / displayMetrics.density),
                displayMetrics.densityDpi,
                "164.132.219.35",
                "stage-client",
                Preferences.getNick(),
                "7f1dcc2b");

        activity.runOnUiThread(() -> ((TextView) activity.findViewById(R.id.test_info)).setText(info));
    }

    public void ShowHud() {
        Utils.ShowLayout(hudLayout, false);
        //displayInfo();
        hudRadar.post(() -> {
            if (!isHudSetPos) {
                ShowTextures(true);
                isHudSetPos = true;
            }
        });
    }

    public void HideHud() {
        Utils.HideLayout(hudLayout, false);
        hudRadar.post(() -> {
            if (isHudSetPos) {
                ShowTextures(false);
                isHudSetPos = false;
            }
        });
    }

    private void ShowTextures(boolean isOn) {
        if (isOn) {
            hudRadar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    hudRadar.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    radarWidth = hudRadar.getWidth() / 8;
                    radarHeight = hudRadar.getHeight() / 8;
                    screenWidth = hudLayout.getWidth();
                    screenHeight = hudLayout.getHeight();
                    realPrctX = ((hudRadar.getX() + (hudRadar.getWidth() / 2)) / screenWidth) * 100;
                    realPrctY = ((hudRadar.getY() + (hudRadar.getHeight() / 2.25f)) / screenHeight) * 100;

                    gtaX = (640 * (realPrctX / 100f));
                    gtaY = (480 * (realPrctY / 100f));

                    NvEventQueueActivity.getInstance().SetRadarBgPos(hudRadar.getX(), hudRadar.getY(), hudRadar.getWidth(), hudRadar.getHeight());
                    NvEventQueueActivity.getInstance().SetRadarPos(gtaX, gtaY, radarWidth, radarHeight);
                    activity.runOnUiThread(() -> hudRadar.setVisibility(View.INVISIBLE));
                    NvEventQueueActivity.getInstance().SetRadarEnabled(true);

                    float logoHeight = 170.0f;
                    float logoX = 10.0f;
                    float logoY = screenHeight - logoHeight - 10.0f;

                    NvEventQueueActivity.getInstance().SetLogoPos(logoX, logoY);
                    // LOGO 20.000000, 940.000000
                }
            });
        } else {
            NvEventQueueActivity.getInstance().SetRadarBgPos(10000, 0, 0, 0); // за пределами экрана (сосешь?)
            NvEventQueueActivity.getInstance().SetRadarPos(10000, 0, 0, 0);
            NvEventQueueActivity.getInstance().SetLogoPos(10000, 0);
        }
    }

    public void ShowSpeed() {
    }
    public void HideSpeed() {
    }

    public void ShowG() {
        hudSeat.setVisibility(View.VISIBLE);

    }
    public void HideG() {
        hudSeat.setVisibility(View.GONE);
    }
}
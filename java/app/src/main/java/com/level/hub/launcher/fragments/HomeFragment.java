package com.level.hub.launcher.fragments;

import static com.level.hub.launcher.config.Config.gameFiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.joom.paranoid.Obfuscate;
import com.level.hub.R;
import com.level.hub.launcher.activity.MainActivity;
import com.level.hub.launcher.adapters.NewsAdapter;
import com.rockstargames.gtasa.Preferences;
import com.rockstargames.gtasa.core.GTASA;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Obfuscate
public class HomeFragment extends Fragment {

    private ImageView news;
    private TextView newsHeader;
    private TextView newsCaption;
    private EditText NickName;

    String img = NewsAdapter.getNewsImage();
    String header = NewsAdapter.getNewsHeader();
    String caption = NewsAdapter.getNewsCaption();

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        if (getActivity() instanceof MainActivity) {
            MainActivity.hideKeyboard(getActivity());
        }

        news = view.findViewById(R.id.news_image);
        newsHeader = view.findViewById(R.id.news_header);
        newsCaption = view.findViewById(R.id.news_caption);
        NickName = view.findViewById(R.id.nick);

        updateNewsUI(img, header, caption);

        setPlayButton(view);
        setExternalLinks(view);
        setSettingsButton(view);

        loadNickName();

        view.requestFocus();
        NickName.setCursorVisible(false);
        NickName.clearFocus();

        NickName.setOnEditorActionListener((v, actionId, event) -> {
            if (isActionDone(actionId, event)) {
                hideKeyboard();
                saveNickName(NickName.getText().toString());
                view.requestFocus();
                NickName.setCursorVisible(false);
                NickName.clearFocus();
                return true;
            }
            return false;
        });

        NickName.setOnTouchListener((v, event) -> {
            v.performClick();
            NickName.setCursorVisible(true);
            NickName.setInputType(InputType.TYPE_CLASS_TEXT);
            NickName.requestFocus();
            return false;
        });

        return view;
    }

    private void updateNewsUI(String imageUrl, String header, String caption) {
        newsHeader.setText(header);
        newsCaption.setText(caption);
        Glide.with(this)
                .load(imageUrl)
                .into(news);
    }

    private void setPlayButton(View view) {
        Button playButton = view.findViewById(R.id.play);
        if (playButton != null) {
            Animation clickAnim = AnimationUtils.loadAnimation(getContext(), R.anim.button_click);

            playButton.setOnClickListener(v -> {
                clickAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        handlePlayButtonClick();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });

                v.startAnimation(clickAnim);
            });
        }
    }

    private void setExternalLinks(View view) {
        Button telegram = view.findViewById(R.id.telegram);
        Button youtube = view.findViewById(R.id.youtube);
        Button support = view.findViewById(R.id.support);

        Animation clickAnim = AnimationUtils.loadAnimation(getContext(), R.anim.button_click);

        setButtonClickListener(telegram, "https://t.me/psychobye", clickAnim);
        setButtonClickListener(youtube, "https://github.com/psychobye", clickAnim);
        setButtonClickListener(support, "https://t.me/psychobye", clickAnim);
    }

    private void setSettingsButton(View view) {
        Button settingsButton = view.findViewById(R.id.settings);
        if (settingsButton != null) {
            Animation clickAnim = AnimationUtils.loadAnimation(getContext(), R.anim.button_click);

            settingsButton.setOnClickListener(v -> {
                clickAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        openSettingsFragment();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });

                v.startAnimation(clickAnim);
            });
        }
    }

    private void openSettingsFragment() {
        if (getActivity() == null) return;

        SettingsFragment settingsFragment = new SettingsFragment();

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        transaction.setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out
        );

        transaction.replace(R.id.fragment_container, settingsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void handlePlayButtonClick() {
        if (getActivity() == null) return;

        File externalFilesDir = getActivity().getExternalFilesDir(null);
        if (externalFilesDir == null) {
            tost("Failed to access external storage.");
            return;
        }

        File americanDxt = new File(externalFilesDir, gameFiles + "Text/american.gxt");
        File russianFont = new File(externalFilesDir, gameFiles + "Textures/fonts/RussianFont.png");

        /*if (!americanDxt.exists() || !russianFont.exists()) {
            tost("Required files not found.");
            return;
        }*/

        Intent intent = new Intent(getActivity(), GTASA.class);

        if (getActivity() != null) {
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void setButtonClickListener(Button button, String url, Animation anim) {
        if (button != null) {
            button.setOnClickListener(v -> {
                v.startAnimation(anim);
                openLink(url);
            });
        }
    }

    private void openLink(String url) {
        if (getActivity() == null) return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    private boolean isActionDone(int actionId, KeyEvent event) {
        return actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
    }

    private void loadNickName() {
        try {
            File file = new File(gameFiles + "multiplayer/settings.json");
            if (!file.exists()) return;

            String jsonText = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(jsonText);
            JSONObject client = json.getJSONObject("client");

            String nickName = client.getString("name");
            if (nickName != null && !nickName.isEmpty()) {
                NickName.setText(nickName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveNickName(String nickName) {
        if (!checkValidNick(nickName)) return;

        try {
            File file = new File(gameFiles + "multiplayer/settings.json");
            JSONObject json;

            if (file.exists()) {
                String jsonText = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                json = new JSONObject(jsonText);
            } else {
                json = new JSONObject();
                json.put("client", new JSONObject());
            }

            JSONObject client = json.getJSONObject("client");
            client.put("name", nickName);

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json.toString(4));
            }

            tost("ваш новый никнейм успешно сохранён!");
            Preferences.setNick(nickName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkValidNick(String nickName) {
        if (nickName.isEmpty()) {
            tost("Введите ник");
            return false;
        }

        String nickRegex = "^[A-Z][a-zA-Z]+_[A-Z][a-zA-Z]+$";
        if (!nickName.matches(nickRegex)) {
            tost("Введите ник в формате, например: Egor_Kreed, с одной заглавной буквой в каждой части, разделённых символом \"_\".");
            return false;
        }

        return true;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(NickName.getWindowToken(), 0);
        }
    }

    private void tost(@NonNull String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
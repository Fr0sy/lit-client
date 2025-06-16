package com.rockstargames.gtasa.core;

import static com.level.hub.launcher.config.Config.gameFiles;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class Utils extends AppCompatActivity {

    public static Object curre;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    public static boolean getGameFile(String path)
    {
        File file = new File(gameFiles+path);
        return file.exists();
    }

    static int installgametype = 0;

    public static int getType(){ return installgametype;}
    public static int setType(int value){return installgametype = value;}
}

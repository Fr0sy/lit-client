package com.rockstargames.gtasa.core;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;

import com.bytedance.shadowhook.ShadowHook;
import com.rockstargames.gtasa.gui.util.Utils;
import com.wardrumstudios.utils.WarMedia;

public class GTASA extends WarMedia {
    public static GTASA gtasaSelf = null;
    static String vmVersion;
    private boolean once = false;

    private static final String KEY = "q6WcyAP4";
    private static final String EXT = ".mtk";
    private static final String CHECK = "VER";
    private static final String RES_DIR = "res/";

    static {
        ShadowHook.init(new ShadowHook.ConfigBuilder()
                .setMode(ShadowHook.Mode.UNIQUE)
                .build());

        vmVersion = null;
        System.out.println("**** Loading SO's");
        try
        {
            vmVersion = System.getProperty("java.vm.version");
            System.out.println("vmVersion " + vmVersion);
            System.loadLibrary("ImmEmulatorJ");
        } catch (ExceptionInInitializerError | UnsatisfiedLinkError e) {
        }

        System.loadLibrary("GTASA");
        System.loadLibrary("samp");
    }

    public static void staticEnterSocialClub()
    {
        gtasaSelf.EnterSocialClub();
    }

    public static void staticExitSocialClub() {
        gtasaSelf.ExitSocialClub();
    }

    public void AfterDownloadFunction() {

    }

    public void EnterSocialClub() {

    }

    public void ExitSocialClub() {

    }

    public boolean ServiceAppCommand(String str, String str2)
    {
        return false;
    }

    public int ServiceAppCommandValue(String str, String str2)
    {
        return 0;
    }

    public native void main();
    public native void encrypt();

    public void onActivityResult(int i, int i2, Intent intent)
    {
        super.onActivityResult(i, i2, intent);
    }

    public void onConfigurationChanged(Configuration configuration)
    {
        super.onConfigurationChanged(configuration);
    }

    @Override
    public void onCreate(Bundle bundle) {
        if (!once) {
            once = true;
        }

        System.out.println("GTASA onCreate");
        gtasaSelf = this;
        wantsMultitouch = true;
        wantsAccelerometer = true;
        super.onCreate(bundle);
        Utils.currentContext = this;

        /*new Thread(() -> {
            File resFolder = new File(gameFiles, RES_DIR);
            File[] files = resFolder.listFiles((dir, name) -> name.endsWith(EXT));
            if (files != null) {
                for (File file : files) {
                    try {
                        RandomAccessFile raf = new RandomAccessFile(file, "r");
                        byte[] header = new byte[3];
                        raf.read(header);
                        raf.close();

                        if (!new String(header).equals(CHECK)) {
                            decrypt(file);
                            Log.d("LEVEL", "расшифровываем " + file.getName());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();*/
    }

    @Override
    public void onDestroy() {
        System.out.println("GTASA onDestroy");

        cleanupEGL();
        terminateSampLibrary();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        } else {
            System.exit(0);
        }

        super.onDestroy();
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent)
    {
        return super.onKeyDown(i, keyEvent);
    }

    public void onPause()
    {
        System.out.println("GTASA onPause");
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            encrypt();
        }
    }

    public void onRestart()
    {
        System.out.println("GTASA onRestart");
        super.onRestart();
    }

    public void onResume()
    {
        System.out.println("GTASA onResume");
        super.onResume();
    }

    public void onStart()
    {
        System.out.println("GTASA onStart");
        super.onStart();
    }

    public void onStop()
    {
        System.out.println("GTASA onStop");
        super.onStop();
    }

    public native void setCurrentScreenSize(int i, int i2);
}
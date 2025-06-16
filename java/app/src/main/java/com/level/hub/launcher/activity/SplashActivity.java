package com.level.hub.launcher.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.joom.paranoid.Obfuscate;
import com.level.hub.R;
import com.level.hub.launcher.adapters.NewsAdapter;
import com.level.hub.launcher.config.Config;
import com.level.hub.launcher.fragments.DialogFragment;
import com.level.hub.launcher.services.UpdateService;
import com.level.hub.launcher.util.SharedPreferenceCore;
import com.level.hub.launcher.util.SignatureChecker;
import com.level.hub.launcher.util.Util;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
@Obfuscate
public class SplashActivity extends AppCompatActivity implements DialogFragment.DialogListener {

	private final String[] permissions = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"};

	public int mGpuType;

	SharedPreferences prefs = null;

	AlertDialog.Builder builder;

	private static final int MAX_RETRIES = 3;
	private int retryCount = 0;
	private static final int RETRY_DELAY_MS = 3000;

	private final ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			mService = new Messenger(iBinder);
			checkUpdate();
		}

		public void onServiceDisconnected(ComponentName componentName) {
			mService = null;
		}
	};


	public IncomingHandler mInHandler;
	public Messenger mMessenger;
	public Messenger mService;

	private boolean mIsBind;
	private boolean isNewsLoaded = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		hideSystemUI();

		if(!SignatureChecker.isSignatureValid(this, getPackageName()))
		{
			((TextView)findViewById(R.id.launcher_orig_text)).setText("using not original launcher");
		}
		else {
			Config.currentContext = this;

			prefs = getSharedPreferences("com.level.hub", MODE_PRIVATE);

			builder = new AlertDialog.Builder(this);

			if (!Util.isNetworkConnected(Config.currentContext)) {
				dialog("Отсутствует подключение к интернету\nПожалуйста, подключитесь к сети, чтобы продолжить.", true);
				return;
			}

			mInHandler = new IncomingHandler();
			mMessenger = new Messenger(mInHandler);

			GLSurfaceView.Renderer mGlRenderer = new GLSurfaceView.Renderer() {
				@Override
				public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
					UpdateActivity.eGPUType egputype;
					String glGetString = gl10.glGetString(GL10.GL_EXTENSIONS);
					String glGetString2 = gl10.glGetString(GL10.GL_EXTENSIONS);
					if (glGetString2.contains("GL_IMG_texture_compression_pvrtc")) {
						egputype = UpdateActivity.eGPUType.PVR;
						mGpuType = 3;
					} else if (glGetString2.contains("GL_EXT_texture_compression_dxt1") || glGetString2.contains("GL_EXT_texture_compression_s3tc") || glGetString2.contains("GL_AMD_compressed_ATC_texture")) {
						egputype = UpdateActivity.eGPUType.DXT;
						mGpuType = 1;
					} else {
						egputype = UpdateActivity.eGPUType.ETC;
						mGpuType = 2;
					}
					Log.e("LEVEL", "GPU name: " + glGetString);
					Log.e("LEVEL", "GPU type: " + egputype.name());

					if (isPermissionsGranted()) {
						mIsBind = bindService(new Intent(SplashActivity.this, UpdateService.class), mConnection, Context.BIND_AUTO_CREATE);
					} else {
						ActivityCompat.requestPermissions(SplashActivity.this,
								permissions,
								1);
					}
				}

				@Override
				public void onSurfaceChanged(GL10 gl10, int i, int i1) {

				}

				@Override
				public void onDrawFrame(GL10 gl10) {

				}
			};

			initLoad();

			ImageView load = findViewById(R.id.load);

			Glide.with(this)
					.load(R.drawable.load_gif)
					.into(load);

			ConstraintLayout gpuLayout = findViewById(R.id.gpu);
			GLSurfaceView mGlSurfaceView = new GLSurfaceView(this);
			mGlSurfaceView.setRenderer(mGlRenderer);
			gpuLayout.addView(mGlSurfaceView);
		}
	}

	private void initLoad() {
		loadNewsWithRetry();
	}


	private void loadNewsWithRetry() {
		NewsAdapter.loadNews(this, new NewsAdapter.NewsLoadListener() {
			@Override
			public void onNewsLoaded(String imageUrl, String header, String caption) {
				isNewsLoaded = true;
				retryCount = 0;
			}

			@Override
			public void onError(String error) {
				Log.d("LEVEL", "news load error: " + error);
				if (retryCount < MAX_RETRIES) {
					retryCount++;
					Log.d("LEVEL", "retrying... (" + retryCount + ")");
					new android.os.Handler().postDelayed(() -> loadNewsWithRetry(), RETRY_DELAY_MS);
				} else {
					Log.d("LEVEL", "failed after retries, continue anyway");
					isNewsLoaded = true; // даём стартануть, даже если упало
					startMainActivity();
				}
			}
		});
	}

	public class IncomingHandler extends Handler {
		public IncomingHandler() {}

		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			if (msg.what == 4) {
				UpdateActivity.UpdateStatus valueOf = UpdateActivity.UpdateStatus.valueOf(msg.getData().getString("status", ""));
				if (valueOf == UpdateActivity.UpdateStatus.Undefined) {
					Message obtain = Message.obtain((Handler) null, 5);
					obtain.replyTo = mMessenger;
					try {
						mService.send(obtain);
					} catch (RemoteException e5) {
						e5.printStackTrace();
					}
				} else if (valueOf == UpdateActivity.UpdateStatus.CheckUpdate) {
					Message obtain2 = Message.obtain((Handler) null, 4);
					obtain2.replyTo = mMessenger;
					try {
						mService.send(obtain2);
					} catch (RemoteException e6) {
						e6.printStackTrace();
					}
				}
			} else if (msg.what == 5) {
				UpdateActivity.GameStatus valueOf2 = UpdateActivity.GameStatus.valueOf(msg.getData().getString("status", ""));
				Log.i("LEVEL", "gameStatus = " + valueOf2);

				if (valueOf2 == UpdateActivity.GameStatus.UpdateRequired) {
					if(!new SharedPreferenceCore().getBoolean(SplashActivity.this, "MODIFIED_DATA")) {
						if(!isFinishing()) {
							String sizeText = formatSize(UpdateService.mUpdateGameDataSize);
							dialog("Доступно обновление (" + sizeText + ")" + "\nХотите обновить?", false);
						}
					}
					else {
						startMainActivity();
					}
				}
				else if (valueOf2 == UpdateActivity.GameStatus.GameUpdateRequired) {
					Intent intent = new Intent(SplashActivity.this, UpdateActivity.class);
					overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
					intent.putExtra("mode", UpdateActivity.UpdateMode.GameUpdate.name());
					startActivity(intent);
					finish();
				}
				else {
					startMainActivity();
				}

			}
		}
	}

	private void dialog(String message, boolean showOnlyOk) {
		getSupportFragmentManager().beginTransaction()
				.add(android.R.id.content, DialogFragment.newInstance(message, showOnlyOk))
				.commit();
	}

	@Override
	public void onYesClicked() {
		Intent intent = new Intent(SplashActivity.this, UpdateActivity.class);
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		intent.putExtra("mode", UpdateActivity.UpdateMode.GameDataUpdate.name());
		startActivity(intent);
		finish();
	}

	@Override
	public void onNoClicked() {
		startActivity(new Intent(SplashActivity.this, MainActivity.class));
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		finish();
	}

	@Override
	public void onOkClicked() {
		finish();
	}

	public static String formatSize(long bytes) {
		if (bytes < 1024) {
			return bytes + " B";
		} else if (bytes < 1024 * 1024) {
			float kb = bytes / 1024f;
			return String.format("%.1f KB", kb);
		} else if (bytes < 1024 * 1024 * 1024) {
			float mb = bytes / (1024f * 1024f);
			return String.format("%.1f MB", mb);
		} else {
			float gb = bytes / (1024f * 1024f * 1024f);
			return String.format("%.1f GB", gb);
		}
	}

	private void startMainActivity() {
		if(!isNewsLoaded) {
			Log.d("LEVEL", "isNewsLoaded = false");
			return;
		}

		startActivity(new Intent(SplashActivity.this, MainActivity.class));
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		finish();
	}

	public void checkUpdate() {
		Log.d("LEVEL", "checkUpdate");
		Message obtain = Message.obtain((Handler) null, 0);
		obtain.getData().putInt("gputype", mGpuType);
		obtain.replyTo = mMessenger;
		try {
			mService.send(obtain);
		} catch (RemoteException e5) {
			e5.printStackTrace();
		}
	}

	public boolean isPermissionsGranted()
	{
		int size = permissions.length;

		for (int i = 0; i < size; i++) {
			if (ContextCompat.checkSelfPermission(this, permissions[i])
					== PackageManager.PERMISSION_GRANTED) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode != 1) {
			return;
		}
		if (grantResults.length <= 0 || grantResults[0] != 0) {
			Toast.makeText(this, "Permissions not granted!", Toast.LENGTH_LONG).show();
		} else {
			mIsBind = bindService(new Intent(this, UpdateService.class), mConnection, Context.BIND_AUTO_CREATE);
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mIsBind) {
			unbindService(mConnection);
			mIsBind = false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (prefs.getBoolean("firstrun", true)) {
			new SharedPreferenceCore().setInt(getApplicationContext(), "FPS_LIMIT", 60);
			new SharedPreferenceCore().setInt(getApplicationContext(), "MESSAGE_COUNT", 6);
			new SharedPreferenceCore().setBoolean(getApplicationContext(), "AIM", false);
			new SharedPreferenceCore().setBoolean(getApplicationContext(), "MODIFIED_DATA", false);
			new SharedPreferenceCore().setBoolean(getApplicationContext(), "AML", false);
			new SharedPreferenceCore().setBoolean(getApplicationContext(), "CLEO", false);
			new SharedPreferenceCore().setBoolean(getApplicationContext(), "MLOADER", false);
			new SharedPreferenceCore().setInt(getApplicationContext(), "VERSION", 0);
			prefs.edit().putBoolean("firstrun", false).commit();
		}
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
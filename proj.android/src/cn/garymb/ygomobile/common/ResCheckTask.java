package cn.garymb.ygomobile.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.setting.Settings;
import cn.garymb.ygomobile.utils.DatabaseUtils;
import cn.garymb.ygomobile.utils.FileOpsUtils;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class ResCheckTask extends AsyncTask<Void, Integer, Integer> {
	
	public static final int RES_ERR_NO_FONT_AVAIL = -1;
	
	public interface ResCheckListener {
		void onResCheckFinished(int result);
	}
	
	private static final int CORE_CONFIG_COPY_COUNT = 3;

	private static final String TAG = "ResCheckTask";
	
	private StaticApplication mApp;
	
	private SharedPreferences mSettingsPref;
	
	private ProgressDialog mWaitDialog;
	
	private ResCheckListener mListener;
	
	public ResCheckTask(Context context) {
		mApp = StaticApplication.peekInstance();
		mSettingsPref = PreferenceManager.getDefaultSharedPreferences(mApp);
		
		mWaitDialog = new ProgressDialog(context);
		mWaitDialog.setMessage(mApp.getResources().getString(
				R.string.checking_resource));
		mWaitDialog.setCancelable(false);
	}
	
	public void setResCheckListener(ResCheckListener listener) {
		mListener = listener;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mWaitDialog.show();
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		mWaitDialog.dismiss();
		if (mListener != null) {
			mListener.onResCheckFinished(result);
			mListener = null;
		}
	}

	@Override
	protected Integer doInBackground(Void... params) {
		String newConfigVersion = null, currentConfigVersion = null;
		SharedPreferences sp = mApp.getSharedPreferences(Constants.PREF_FILE_COMMON,
				Context.MODE_PRIVATE);
		currentConfigVersion = sp.getString(Constants.PREF_KEY_DATA_VERSION, "");
		try {
			newConfigVersion = mApp.getAssets().list(Constants.CORE_CONFIG_PATH)[0];
		} catch (IOException e) {
			e.printStackTrace();
		}
		boolean needsUpdate = !currentConfigVersion.equals(newConfigVersion);
		publishProgress(R.string.updating_fonts);
		initFontList();
		saveCoreConfigVersion(newConfigVersion);
		publishProgress(R.string.updating_decks);
		checkAndCopyNewDeckFiles(needsUpdate);
		publishProgress(R.string.updating_config);
		checkAndCopyCoreConfig(needsUpdate);
		publishProgress(R.string.updating_skin);
		checkAndCopyGameSkin();
		publishProgress(R.string.updating_card_data_base);
		DatabaseUtils.checkAndCopyFromInternalDatabase(mApp, mApp.getDataBasePath(),
				needsUpdate);
		publishProgress(R.string.updating_scripts);
		checkAndCopyScripts(needsUpdate);
		return 0;
	}
	
	private void checkAndCopyScripts(boolean isUpdateNeeded) {
		File scriptDir = new File(mApp.getCompatExternalFilesDir(), "/scripts");
		File scriptFile = new File(scriptDir, "main.zip");
		if (!scriptDir.exists()) {
			scriptDir.mkdirs();
		}
		if (isUpdateNeeded || !scriptFile.exists()) {
			int assetcopycount = 0;
			while (assetcopycount++ < CORE_CONFIG_COPY_COUNT) {
				try {
					FileOpsUtils.assetsCopy(mApp, "main.zip",
							scriptFile.toString(), false);
				} catch (IOException e) {
					Log.w(TAG, "copy scripts failed, retry count = "
							+ assetcopycount);
					continue;
				}
			}
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		mWaitDialog.setMessage(mApp.getResources().getString(
				values[0]));
	}
	
	private void checkAndCopyNewDeckFiles(boolean isUpdateNeeded) {
		File deckDir = new File(mApp.getResourcePath(), Constants.CORE_DECK_PATH);
		if (!deckDir.exists()) {
			deckDir.mkdirs();
		}
		if (isUpdateNeeded) {
			int assetcopycount = 0;
			while (assetcopycount++ < CORE_CONFIG_COPY_COUNT) {
				try {
					FileOpsUtils.assetsCopy(mApp, Constants.CORE_DECK_PATH,
							deckDir.toString(), false);
				} catch (IOException e) {
					Log.w(TAG, "copy core skin failed, retry count = "
							+ assetcopycount);
					continue;
				}
			}
		}
		
	}
	
	private void initFontList() {
		File systemFontDir = new File(Constants.SYSTEM_FONT_DIR);
		ArrayList<String> fontsPath = new ArrayList<String>();
		String[] fonts = systemFontDir.list();
		for (String name : fonts) {
			Log.i(TAG, "load system font : " + name);
			fontsPath.add(new File(systemFontDir, name).toString());
		}
		// load extra font
		File extraDir = new File(mApp.getDefaultResPath() + Constants.FONT_DIRECTORY);
		if (extraDir.exists() && extraDir.exists()) {
			fonts = extraDir.list();
			boolean isFontHit = false;
			String currentFont = mSettingsPref.getString(
					Settings.KEY_PREF_GAME_FONT_NAME, Constants.SYSTEM_FONT_DIR  + Constants.DEFAULT_FONT_NAME);
			for (String name : fonts) {
				Log.i(TAG, "load user define font : " + name);
				fontsPath.add(new File(extraDir, name).toString());
				if (currentFont.equals(name)) {
					isFontHit = true;
				}
			}
			// for update compatability.
			if (isFontHit) {
				mSettingsPref
						.edit()
						.putString(Settings.KEY_PREF_GAME_FONT_NAME,
								new File(extraDir, currentFont).toString())
						.commit();
			}
		} else {
			mSettingsPref
					.edit()
					.putString(Settings.KEY_PREF_GAME_FONT_NAME,
							Constants.SYSTEM_FONT_DIR  + Constants.DEFAULT_FONT_NAME).commit();
		}
		mApp.setFontList(fontsPath);
	}
	
	private void saveCoreConfigVersion(String version) {
		SharedPreferences sp = mApp.getSharedPreferences(Constants.PREF_FILE_COMMON,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(Constants.PREF_KEY_DATA_VERSION, version);
		editor.commit();
		mApp.setCoreConfigVersion(version);
	}
	
	private void checkAndCopyCoreConfig(boolean needsUpdate) {
		File internalCacheDir = mApp.getCacheDir();
		if (internalCacheDir != null) {
			File coreConfigDir = new File(internalCacheDir,
					Constants.CORE_CONFIG_PATH);
			if (coreConfigDir != null && coreConfigDir.exists()
					&& coreConfigDir.isDirectory() && !needsUpdate) {
				return;
			}
			if (needsUpdate
					|| (coreConfigDir != null && coreConfigDir.exists() && !coreConfigDir
							.isDirectory())) {
				coreConfigDir.delete();
			}
			// we need to copy from configs from assets;
			int assetcopycount = 0;
			while (assetcopycount++ < CORE_CONFIG_COPY_COUNT) {
				try {
					FileOpsUtils.assetsCopy(mApp, Constants.CORE_CONFIG_PATH,
							coreConfigDir.getAbsolutePath(), false);
					break;
				} catch (IOException e) {
					Log.w(TAG, "copy core config failed, retry count = "
							+ assetcopycount);
					continue;
				}
			}
		}
	}
	
	private void checkAndCopyGameSkin() {
		File coreSkinDir = new File(mApp.getCoreSkinPath());
		if (coreSkinDir != null && coreSkinDir.exists()
				&& coreSkinDir.isDirectory()) {
			return;
		}
		if (coreSkinDir != null && coreSkinDir.exists()
				&& !coreSkinDir.isDirectory()) {
			coreSkinDir.delete();
		}
		// we need to copy from configs from assets;
		int assetcopycount = 0;
		while (assetcopycount++ < CORE_CONFIG_COPY_COUNT) {
			try {
				FileOpsUtils.assetsCopy(mApp, Constants.CORE_SKIN_PATH,
						coreSkinDir.getAbsolutePath(), false);
				break;
			} catch (IOException e) {
				Log.w(TAG, "copy core skin failed, retry count = "
						+ assetcopycount);
				continue;
			}
		}
	}

}

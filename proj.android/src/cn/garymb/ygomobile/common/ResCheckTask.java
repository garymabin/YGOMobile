package cn.garymb.ygomobile.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.core.BaseTask.TaskStatusCallback;
import cn.garymb.ygomobile.core.SimpleDownloadTask;
import cn.garymb.ygomobile.data.wrapper.IBaseJob;
import cn.garymb.ygomobile.data.wrapper.SimpleDownloadJob;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import cn.garymb.ygomobile.setting.Settings;
import cn.garymb.ygomobile.utils.DatabaseUtils;
import cn.garymb.ygomobile.utils.FileOpsUtils;
import cn.garymb.ygomobile.widget.ProgressUpdateDialog;
import de.greenrobot.event.EventBus;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class ResCheckTask extends AsyncTask<Void, Integer, Integer> {
	
	public static final int RES_ERR_NO_FONT_AVAIL = -1;
	
	private static final int RES_CHECK_TYPE_MESSAGE_UPDATE = 1;
	private static final int RES_CHECK_TYPE_PROGRESS_UPDATE = 2;
	
	public interface ResCheckListener {
		void onResCheckFinished(int result);
	}
	
	private static final int CORE_CONFIG_COPY_COUNT = 3;

	private static final String TAG = "ResCheckTask";
	
	private StaticApplication mApp;
	private Activity mContext;
	
	private SharedPreferences mSettingsPref;
	
	private ProgressDialog mWaitDialog;
	private ProgressUpdateDialog mProgressUpdateDialog;
	
	private ResCheckListener mListener;
	
	private Object mLock = new Object();
	
	public ResCheckTask(Activity context) {
		mContext = context;
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
		publishProgress(RES_CHECK_TYPE_MESSAGE_UPDATE, R.string.updating_fonts);
		initFontList();
		saveCoreConfigVersion(newConfigVersion);
		publishProgress(RES_CHECK_TYPE_MESSAGE_UPDATE, R.string.updating_decks);
		checkAndCopyNewDeckFiles(needsUpdate);
		publishProgress(RES_CHECK_TYPE_MESSAGE_UPDATE, R.string.updating_config);
		checkAndCopyCoreConfig(needsUpdate);
		publishProgress(RES_CHECK_TYPE_MESSAGE_UPDATE, R.string.updating_skin);
		checkAndCopyGameSkin();
		publishProgress(R.string.updating_card_data_base);
		DatabaseUtils.checkAndCopyFromInternalDatabase(mApp, mApp.getDataBasePath(),
				needsUpdate);
		publishProgress(RES_CHECK_TYPE_MESSAGE_UPDATE, R.string.updating_scripts);
		checkAndCopyScripts(needsUpdate);
		publishProgress(RES_CHECK_TYPE_MESSAGE_UPDATE, R.string.updating_dirs);
		checkDirs();
		return 0;
	}
	
	private void checkDirs() {
		String[] dirs = {"script", "single", "deck", "replay", "fonts"};
		File dirFile = null;
		for (String dir : dirs) {
			dirFile = new File(mApp.getResourcePath(), dir);
			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}
		}
		
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
		if (values[0] == RES_CHECK_TYPE_MESSAGE_UPDATE) {
			mWaitDialog.setMessage(mApp.getResources().getString(
					values[1]));
		} else if (values[0] == RES_CHECK_TYPE_PROGRESS_UPDATE){
			if(values[0] == 0) {
				mWaitDialog.dismiss();
				if (mProgressUpdateDialog == null) {
					View content = mContext.getLayoutInflater().inflate(R.layout.image_dl_dialog, null);
					mProgressUpdateDialog = new ProgressUpdateDialog(mApp, null, content, null);
				}
				if (!mProgressUpdateDialog.isShowing()) {
					mProgressUpdateDialog.show();
				}
			} else {
				if (mProgressUpdateDialog != null) {
					mProgressUpdateDialog.dismiss();
				}
				mWaitDialog.show();
			}
			
		}
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
	
	private boolean initFontList() {
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
			File defaultFont = new File(Constants.SYSTEM_FONT_DIR + Constants.DEFAULT_FONT_NAME);
			if (defaultFont.exists()) {
				mSettingsPref
					.edit()
					.putString(Settings.KEY_PREF_GAME_FONT_NAME,
							Constants.SYSTEM_FONT_DIR  + Constants.DEFAULT_FONT_NAME).commit();
			} else {
//				File wqyFont = new File(extraDir, "WQYMicroHei.TTF");
//				SimpleDownloadJob job = new SimpleDownloadJob(ResourcesConstants.FONTS_DOWNLOAD_URL, wqyFont.getAbsolutePath());
//				SimpleDownloadTask task = new SimpleDownloadTask(StaticApplication.peekInstance().getOkHttpClient());
//				EventBus.getDefault().register(mProgressUpdateDialog.getController());
//				task.setTaskStatusCallback(new TaskStatusCallback() {
//					@Override
//					public void onTaskFinish(int type, int result) {
//						if (result == IBaseJob.STATUS_SUCCESS) {
//							synchronized (mLock) {
//								mLock.notifyAll();
//								publishProgress(RES_CHECK_TYPE_PROGRESS_UPDATE, 1);
//							}
//						}
//					}
//				});
//				task.addJob(job);
//				publishProgress(RES_CHECK_TYPE_PROGRESS_UPDATE, 0);
//				synchronized (mLock) {
//					try {
//						mLock.wait();
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
				publishProgress(RES_CHECK_TYPE_MESSAGE_UPDATE, R.string.no_avail_font_file);
			}
		}
		mApp.setFontList(fontsPath);
		return true;
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

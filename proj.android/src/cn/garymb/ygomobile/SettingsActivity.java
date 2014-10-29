package cn.garymb.ygomobile;

import java.io.File;
import java.util.List;

import com.soundcloud.android.crop.Crop;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.common.CardDBCopyTask;
import cn.garymb.ygomobile.common.CardDBResetTask;
import cn.garymb.ygomobile.common.CardDBCopyTask.CardDBCopyListener;
import cn.garymb.ygomobile.common.CardDBResetTask.CardDBResetListener;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.common.ImageCopyTask;
import cn.garymb.ygomobile.common.ImageCopyTask.ImageCopyListener;
import cn.garymb.ygomobile.core.Controller;
import cn.garymb.ygomobile.model.data.ImageItemInfoHelper;
import cn.garymb.ygomobile.model.data.VersionInfo;
import cn.garymb.ygomobile.setting.Settings;
import cn.garymb.ygomobile.utils.DeviceUtils;
import cn.garymb.ygomobile.utils.FileOpsUtils;
import cn.garymb.ygomobile.widget.AppUpdateDialog;
import cn.garymb.ygomobile.widget.FileChooseController;
import cn.garymb.ygomobile.widget.FileChooseDialog;
import cn.garymb.ygomobile.widget.ImagePreviewDialog;
import cn.garymb.ygomobile.widget.SimpleDialog;
import cn.garymb.ygomobile.widget.WebViewDialog;
import cn.garymb.ygomobile.widget.filebrowser.FileBrowser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements
		OnPreferenceChangeListener, OnPreferenceClickListener, OnClickListener,
		android.view.View.OnClickListener, ImageCopyListener, Callback,
		CardDBCopyListener, CardDBResetListener {

	public static class EventHandler extends Handler {
		public EventHandler(Callback back) {
			super(back);
		}
	}

	private static final int DIALOG_ID_OPENSOURCE = 0;
	private static final int DIALOG_ID_VERSION = 1;
	private static final int DIALOG_ID_FOLDER_CHOOSE = 2;
	private static final int DIALOG_ID_IMAGE_PREVIEW = 3;
	private static final int DIALOG_ID_APP_UPDATE = 4;
	private static final int DIALOG_ID_CARD_DB_FILE_CHOOSE = 5;
	private static final int DIALOG_ID_CARD_DB_RESET = 6;

	private static final int MSG_TYPE_CHECK_UPDATE = 0;

	private Preference mGameResPath;
	private Preference mVersionPref;
	private Preference mOpensourcePref;
	private ListPreference mOGLESPreference;
	private ListPreference mCardQualityPreference;
	private ListPreference mFontNamePreference;
	private Preference mCoverDiyPreference;
	private Preference mCardBackBiyPreference;
	private Preference mCardDBDiyPreference;
	private Preference mAppUpdatePreference;

	private Preference mCardDBResetPrefernece;

	private EventHandler mHandler = new EventHandler(this);

	private Bundle mImageParam;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.action_settings);
		String action = getIntent().getAction();
		if (action != null) {
			if (action.equals(Constants.SETTINGS_ACTION_COMMON)) {
				addPreferencesFromResource(R.xml.preference_common);
				mGameResPath = findPreference(Settings.KEY_PREF_GAME_RESOURCE_PATH);
				if (TextUtils.isEmpty(mGameResPath.getSummary())) {
					mGameResPath.setSummary(StaticApplication.peekInstance()
							.getResourcePath());
				}
				mGameResPath.setOnPreferenceClickListener(this);
			} else if (action.equals(Constants.SETTINGS_ACTION_GAME)) {
				addPreferencesFromResource(R.xml.preference_game);
				mOGLESPreference = (ListPreference) findPreference(Settings.KEY_PREF_GAME_OGLES_CONFIG);
				mOGLESPreference.setSummary(mOGLESPreference.getEntry());
				mOGLESPreference.setOnPreferenceChangeListener(this);

				mCardQualityPreference = (ListPreference) findPreference(Settings.KEY_PREF_GAME_IMAGE_QUALITY);
				mCardQualityPreference.setSummary(mCardQualityPreference
						.getEntry());
				mCardQualityPreference.setOnPreferenceChangeListener(this);

				mFontNamePreference = (ListPreference) findPreference(Settings.KEY_PREF_GAME_FONT_NAME);
				File fontsPath = new File(StaticApplication.peekInstance()
						.getResourcePath(), Constants.FONT_DIRECTORY);
				mFontNamePreference.setEntries(fontsPath.list());
				mFontNamePreference.setEntryValues(fontsPath.list());
				if (TextUtils.isEmpty(mFontNamePreference.getValue())) {
					mFontNamePreference.setValue(Constants.DEFAULT_FONT_NAME);
				}
				mFontNamePreference.setSummary(mFontNamePreference.getValue());

				mCoverDiyPreference = findPreference(Settings.KEY_PREF_GAME_DIY_COVER);
				mCoverDiyPreference.setOnPreferenceClickListener(this);
				mCardBackBiyPreference = findPreference(Settings.KEY_PREF_GAME_DIY_CARD_BACK);
				mCardBackBiyPreference.setOnPreferenceClickListener(this);
				mCardDBDiyPreference = findPreference(Settings.KEY_PREF_GAME_DIY_CARD_DB);
				mCardDBDiyPreference.setOnPreferenceClickListener(this);
				mCardDBResetPrefernece = findPreference(Settings.KEY_PREF_GAME_RESET_CARD_DB);
				mCardDBResetPrefernece.setOnPreferenceClickListener(this);
			} else if (action.equals(Constants.SETTINGS_ACTION_ABOUT)) {
				addPreferencesFromResource(R.xml.preference_about);
				mVersionPref = findPreference(Settings.KEY_PREF_ABOUT_VERSION);
				mVersionPref.setOnPreferenceClickListener(this);
				mOpensourcePref = findPreference(Settings.KEY_PREF_ABOUT_OPENSOURCE);
				mOpensourcePref.setOnPreferenceClickListener(this);
				mAppUpdatePreference = findPreference(Settings.KEY_PREF_ABOUT_CHECK_UPDATE);
				mAppUpdatePreference.setOnPreferenceClickListener(this);
				mVersionPref.setSummary(StaticApplication.peekInstance()
						.getVersionName());
			}
		} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			// Load the legacy preferences headers
			addPreferencesFromResource(R.xml.preference_headers_legacy);
		}
	}

	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id, Bundle args) {
		Dialog dlg = null;
		if (id == DIALOG_ID_OPENSOURCE) {
			dlg = new WebViewDialog(this, new WebView(this), null, args);
		} else if (id == DIALOG_ID_VERSION) {
			dlg = new WebViewDialog(this, new WebView(this), null, args);
		} else if (id == DIALOG_ID_FOLDER_CHOOSE) {
			View view = getLayoutInflater().inflate(
					R.layout.file_browser_layout, null);
			dlg = new FileChooseDialog(this, view, this, args);
		} else if (id == DIALOG_ID_IMAGE_PREVIEW) {
			View view = getLayoutInflater().inflate(
					R.layout.image_preview_content, null);
			dlg = new ImagePreviewDialog(this, view, this, this, args);
		} else if (id == DIALOG_ID_APP_UPDATE) {
			View view = getLayoutInflater().inflate(
					R.layout.app_update_content, null);
			return new AppUpdateDialog(this, this, view, args);
		} else if (id == DIALOG_ID_CARD_DB_RESET) {
			return new SimpleDialog(this, this, null, args);
		} else {
			dlg = super.onCreateDialog(id);
		}
		return dlg;
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	protected boolean isValidFragment(String fragmentName) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			return Constants.SETTINGS_FARGMENT_ABOUT.equals(fragmentName)
					|| Constants.SETTINGS_FARGMENT_COMMON.equals(fragmentName)
					|| Constants.SETTINGS_FARGMENT_GAME.equals(fragmentName);
		} else {
			return true;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle("image_param", mImageParam);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		mImageParam = state.getBundle("image_param");
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals(Settings.KEY_PREF_GAME_OGLES_CONFIG)) {
			mOGLESPreference.setValue((String) newValue);
			mOGLESPreference.setSummary(mOGLESPreference.getEntry());
		} else if (preference.getKey().equals(
				Settings.KEY_PREF_GAME_IMAGE_QUALITY)) {
			mCardQualityPreference.setValue((String) newValue);
			mCardQualityPreference
					.setSummary(mCardQualityPreference.getEntry());
		} else if (preference.getKey().equals(Settings.KEY_PREF_GAME_FONT_NAME)) {
			mFontNamePreference.setValue((String) newValue);
			mFontNamePreference.setSummary(mFontNamePreference.getEntry());
		}
		return false;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.equals(mOpensourcePref)) {
			Bundle bundle = new Bundle();
			bundle.putString("url", "file:///android_asset/licenses.html");
			bundle.putInt("titleRes", R.string.settings_about_opensource_pref);
			showDialog(DIALOG_ID_OPENSOURCE, bundle);
		} else if (preference.equals(mVersionPref)) {
			Bundle bundle = new Bundle();
			bundle.putString("url", "file:///android_asset/changelog.html");
			bundle.putInt("titleRes", R.string.settings_about_change_log);
			showDialog(DIALOG_ID_VERSION, bundle);
		} else if (preference.getKey().equals(
				Settings.KEY_PREF_GAME_RESOURCE_PATH)) {
			Bundle bundle = new Bundle();
			bundle.putString("root", "/"/*
										 * Environment.getExternalStorageDirectory
										 * () .getAbsolutePath()
										 */);
			bundle.putString("current", StaticApplication.peekInstance()
					.getResourcePath());
			showDialog(DIALOG_ID_FOLDER_CHOOSE, bundle);
		} else if (preference.getKey().equals(Settings.KEY_PREF_GAME_DIY_COVER)) {
			Bundle bundle = new Bundle();
			bundle.putString("url", StaticApplication.peekInstance()
					.getCoreSkinPath()
					+ File.separator
					+ Constants.CORE_SKIN_COVER);
			bundle.putIntArray("orig_size", Constants.CORE_SKIN_COVER_SIZE);
			bundle.putInt("title_res", R.string.settings_game_cover);
			mImageParam = bundle;
			showDialog(DIALOG_ID_IMAGE_PREVIEW, bundle);
		} else if (preference.getKey().equals(
				Settings.KEY_PREF_GAME_DIY_CARD_BACK)) {
			Bundle bundle = new Bundle();
			bundle.putString("url", StaticApplication.peekInstance()
					.getCoreSkinPath()
					+ File.separator
					+ Constants.CORE_SKIN_CARD_BACK);
			bundle.putIntArray("orig_size", Constants.CORE_SKIN_CARD_BACK_SIZE);
			bundle.putInt("title_res", R.string.settings_game_card_back);
			mImageParam = bundle;
			showDialog(DIALOG_ID_IMAGE_PREVIEW, bundle);
		} else if (preference.getKey().equals(
				Settings.KEY_PREF_ABOUT_CHECK_UPDATE)) {
			Controller.peekInstance().asyncCheckUpdate(
					Message.obtain(mHandler, MSG_TYPE_CHECK_UPDATE));
		} else if (preference.getKey().equals(
				Settings.KEY_PREF_GAME_DIY_CARD_DB)) {
			Bundle bundle = new Bundle();
			bundle.putString("root", "/");
			bundle.putInt("mode", FileBrowser.BROWSE_MODE_FILES);
			showDialog(DIALOG_ID_CARD_DB_FILE_CHOOSE, bundle);
		} else if (preference.getKey().equals(
				Settings.KEY_PREF_GAME_RESET_CARD_DB)) {
			Bundle bundle = new Bundle();
			bundle.putString(
					"message",
					getResources().getString(
							R.string.settings_game_reset_card_db_confirm));
			bundle.putString(
					"title",
					getResources().getString(
							R.string.settings_game_reset_card_db));
			showDialog(DIALOG_ID_CARD_DB_RESET, bundle);
		}
		return false;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			if (dialog instanceof FileChooseDialog) {
				String newUrl = ((FileChooseController) ((FileChooseDialog) dialog)
						.getController()).getUrl();
				int mode = ((FileChooseController) ((FileChooseDialog) dialog)
						.getController()).getMode();
				if (mode == FileBrowser.BROWSE_MODE_DIRS) {
					StaticApplication.peekInstance().setResourcePath(newUrl);
					mGameResPath.setSummary(newUrl);
				} else {
					CardDBCopyTask task = new CardDBCopyTask(this);
					task.setCardDBCopyListener(this);
					if (Build.VERSION.SDK_INT >= 11) {
						task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
								newUrl);
					} else {
						task.execute(newUrl);
					}

				}
			} else if (dialog instanceof SimpleDialog) {
				CardDBResetTask task = new CardDBResetTask(this);
				task.setCardDBResetListener(this);
				if (Build.VERSION.SDK_INT >= 11) {
					task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} else {
					task.execute();
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		dismissDialog(DIALOG_ID_IMAGE_PREVIEW);
		Crop.pickImage(this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent result) {
		if (requestCode == Crop.REQUEST_PICK
				&& resultCode == Activity.RESULT_OK) {
			beginCrop(result.getData());
		} else if (requestCode == Crop.REQUEST_CROP) {
			handleCrop(resultCode, result);
		}
	}

	private void handleCrop(int resultCode, Intent result) {
		if (resultCode == Activity.RESULT_OK) {
			setNewImage(Crop.getOutput(result));
		} else if (resultCode == Crop.RESULT_ERROR) {
			Toast.makeText(this, Crop.getError(result).getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void setNewImage(Uri uri) {
		String path = uri.toString();
		if (path.startsWith(ImageItemInfoHelper.FILE_PREFIX)) {
			path = FileOpsUtils.getFilePathFromUrl(path);
		} else if (path.startsWith(ImageItemInfoHelper.MEIDA_PREFIX)) {
			ContentResolver cr = StaticApplication.peekInstance()
					.getContentResolver();
			String[] projection = { MediaStore.MediaColumns.DATA };
			Cursor cursor = cr.query(uri, projection, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				path = cursor.getString(0);
			} else {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		mImageParam.putString("src_url", path);
		if (Build.VERSION.SDK_INT >= 11) {
			ImageCopyTask task = new ImageCopyTask(this);
			task.setImageCopyListener(this);
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mImageParam);
		} else {
			ImageCopyTask task = new ImageCopyTask(this);
			task.setImageCopyListener(this);
			task.execute(mImageParam);
		}
	}

	private void beginCrop(Uri source) {
		Uri outputUri = Uri.fromFile(new File(getCacheDir(), "cropped"));
		new Crop(source)
				.withAspect((int) DeviceUtils.getScreenHeight(),
						(int) DeviceUtils.getScreenWidth()).output(outputUri)
				.start(this);
	}

	@Override
	public void onImageCopyFinished(Bundle result) {
		showDialog(DIALOG_ID_IMAGE_PREVIEW, result);
	}

	@Override
	public boolean handleMessage(Message msg) {
		int type = msg.what;
		if (type == MSG_TYPE_CHECK_UPDATE) {
			VersionInfo info = (VersionInfo) msg.obj;
			if (info != null) {
				if (info.version <= StaticApplication.peekInstance()
						.getVersionCode()) {
					Toast.makeText(
							this,
							getResources().getString(
									R.string.settings_about_no_update),
							Toast.LENGTH_SHORT).show();
				} else {
					Bundle bundle = new Bundle();
					bundle.putInt("version", info.version);
					bundle.putInt("titleRes",
							R.string.settings_about_new_version);
					bundle.putString("url", info.url);
					showDialog(DIALOG_ID_APP_UPDATE, bundle);
				}
			}
		}
		return false;
	}

	@Override
	public void onCardDBCopyFinished(int result) {
		final Resources res = getResources();
		String errorMessage;
		if (result == CardDBCopyTask.COPY_DB_TASK_FAILED) {
			errorMessage = res.getString(R.string.loading_card_failed);
		} else if (result == CardDBCopyTask.COPY_DB_TASK_FILE_NOT_EXIST) {
			errorMessage = res.getString(R.string.loading_card_file_not_found);
		} else {
			errorMessage = res.getString(R.string.loading_card_success);
		}
		Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onCardDBResetFinished(Boolean result) {
		Toast.makeText(
				this,
				result ? getResources().getString(R.string.reset_card_success)
						: getResources().getString(R.string.reset_card_failed),
				Toast.LENGTH_SHORT).show();
	}
}

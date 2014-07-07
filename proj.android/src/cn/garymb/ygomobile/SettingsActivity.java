package cn.garymb.ygomobile;

import java.io.File;
import java.util.List;

import com.github.johnpersano.supertoasts.SuperToast;
import com.soundcloud.android.crop.Crop;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.common.AppUpdateTask;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.common.ImageCopyTask;
import cn.garymb.ygomobile.common.ImageCopyTask.ImageCopyListener;
import cn.garymb.ygomobile.core.Controller;
import cn.garymb.ygomobile.model.data.ImageItemInfoHelper;
import cn.garymb.ygomobile.model.data.VersionInfo;
import cn.garymb.ygomobile.setting.Settings;
import cn.garymb.ygomobile.utils.DeviceUtils;
import cn.garymb.ygomobile.utils.FileOpsUtils;
import cn.garymb.ygomobile.widget.AppUpdateController;
import cn.garymb.ygomobile.widget.AppUpdateDialog;
import cn.garymb.ygomobile.widget.FileChooseController;
import cn.garymb.ygomobile.widget.FileChooseDialog;
import cn.garymb.ygomobile.widget.ImagePreviewDialog;
import cn.garymb.ygomobile.widget.WebViewDialog;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
		android.view.View.OnClickListener, ImageCopyListener, Callback {

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

	private static final int MSG_TYPE_CHECK_UPDATE = 0;

	private Preference mGameResPath;
	private Preference mVersionPref;
	private Preference mOpensourcePref;
	private ListPreference mOGLESPreference;
	private ListPreference mCardQualityPreference;
	private ListPreference mFontNamePreference;
	private Preference mCoverDiyPreference;
	private Preference mAppUpdatePreference;

	private EventHandler mHandler = new EventHandler(this);

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
			bundle.putString("root", Environment.getExternalStorageDirectory()
					.getAbsolutePath());
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
			showDialog(DIALOG_ID_IMAGE_PREVIEW, bundle);
		} else if (preference.getKey().equals(
				Settings.KEY_PREF_ABOUT_CHECK_UPDATE)) {
			Controller.peekInstance().asyncCheckUpdate(
					Message.obtain(mHandler, MSG_TYPE_CHECK_UPDATE));
		}
		return false;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			if (dialog instanceof FileChooseDialog) {
				String newUrl = ((FileChooseController) ((FileChooseDialog) dialog)
						.getController()).getUrl();
				StaticApplication.peekInstance().setResourcePath(newUrl);
				mGameResPath.setSummary(newUrl);
			} else if (dialog instanceof AppUpdateDialog) {
				String url = ((AppUpdateController) ((AppUpdateDialog) dialog)
						.getController()).getDownloadUrl();
				new AppUpdateTask(this).execute(url);
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
		if (Build.VERSION.SDK_INT >= 11) {
			ImageCopyTask task = new ImageCopyTask(this);
			task.setImageCopyListener(this);
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path,
					StaticApplication.peekInstance().getCoreSkinPath()
							+ File.separator + Constants.CORE_SKIN_COVER);
		} else {
			ImageCopyTask task = new ImageCopyTask(this);
			task.setImageCopyListener(this);
			task.execute(path, StaticApplication.peekInstance()
					.getCoreSkinPath()
					+ File.separator
					+ Constants.CORE_SKIN_COVER);
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
	public void onCopyFinished(Bundle result) {
		showDialog(DIALOG_ID_IMAGE_PREVIEW, result);
	}

	@Override
	public boolean handleMessage(Message msg) {
		int type = msg.what;
		if (type == MSG_TYPE_CHECK_UPDATE) {
			VersionInfo info = (VersionInfo) msg.obj;
			if (info != null) {
				if (info.version <= StaticApplication.peekInstance().getVersionCode()) {
					SuperToast.create(
							this,
							getResources().getString(
									R.string.settings_about_no_update),
							SuperToast.Duration.VERY_SHORT).show();
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
}

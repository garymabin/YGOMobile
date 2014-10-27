package cn.garymb.ygomobile.fragment.setting;

import java.io.File;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.soundcloud.android.crop.Crop;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.CardDBCopyTask;
import cn.garymb.ygomobile.common.CardDBCopyTask.CardDBCopyListener;
import cn.garymb.ygomobile.common.CardDBResetTask;
import cn.garymb.ygomobile.common.CardDBResetTask.CardDBResetListener;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.common.ImageCopyTask;
import cn.garymb.ygomobile.common.ImageCopyTask.ImageCopyListener;
import cn.garymb.ygomobile.model.data.ImageItemInfoHelper;
import cn.garymb.ygomobile.setting.Settings;
import cn.garymb.ygomobile.utils.FileOpsUtils;
import cn.garymb.ygomobile.widget.BaseDialog;
import cn.garymb.ygomobile.widget.FileChooseController;
import cn.garymb.ygomobile.widget.FileChooseDialog;
import cn.garymb.ygomobile.widget.ImagePreviewDialog;
import cn.garymb.ygomobile.widget.SimpleDialog;
import cn.garymb.ygomobile.widget.filebrowser.FileBrowser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GameSettingsFragment extends EventDialogPreferenceFragment
		implements OnPreferenceChangeListener, OnPreferenceClickListener,
		OnClickListener, android.content.DialogInterface.OnClickListener,
		ImageCopyListener, CardDBCopyListener, CardDBResetListener {

	private static final int DIALOG_TYPE_IMAGE_PREVIEW = 0;
	private static final int DIALOG_TYPE_CARD_DB_DIY = 1;
	private static final int DIALOG_TYPE_CARD_DB_RESET = 2;

	private ListPreference mOGLESPreference;

	private ListPreference mCardQualityPreference;

	private ListPreference mFontNamePreference;

	private Preference mCoverDiyPreference;

	private Preference mCardDBDiyPreference;

	private Preference mCardBackDiyPreference;

	private Preference mCardDBResetPreference;

	private Bundle mImageBundle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_game);
		if (savedInstanceState != null) {
			mImageBundle = savedInstanceState.getParcelable("image_param");
		}
		mOGLESPreference = (ListPreference) findPreference(Settings.KEY_PREF_GAME_OGLES_CONFIG);
		mOGLESPreference.setSummary(mOGLESPreference.getEntry());
		mOGLESPreference.setOnPreferenceChangeListener(this);

		mCardQualityPreference = (ListPreference) findPreference(Settings.KEY_PREF_GAME_IMAGE_QUALITY);
		mCardQualityPreference.setSummary(mCardQualityPreference.getEntry());
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

		mCardBackDiyPreference = findPreference(Settings.KEY_PREF_GAME_DIY_CARD_BACK);
		mCardBackDiyPreference.setOnPreferenceClickListener(this);

		mCardDBDiyPreference = findPreference(Settings.KEY_PREF_GAME_DIY_CARD_DB);
		mCardDBDiyPreference.setOnPreferenceClickListener(this);

		mCardDBResetPreference = findPreference(Settings.KEY_PREF_GAME_RESET_CARD_DB);
		mCardDBResetPreference.setOnPreferenceClickListener(this);
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
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle("image_param", mImageBundle);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals(Settings.KEY_PREF_GAME_DIY_COVER)) {
			Bundle bundle = new Bundle();
			bundle.putString("url", StaticApplication.peekInstance()
					.getCoreSkinPath()
					+ File.separator
					+ Constants.CORE_SKIN_COVER);
			bundle.putInt("title_res", R.string.settings_game_cover);
			bundle.putIntArray("orig_size", Constants.CORE_SKIN_COVER_SIZE);
			mImageBundle = bundle;
			showDialog(DIALOG_TYPE_IMAGE_PREVIEW, bundle);
		} else if (preference.getKey().equals(
				Settings.KEY_PREF_GAME_DIY_CARD_DB)) {
			Bundle bundle = new Bundle();
			bundle.putString("root", "/"/*
										 * Environment.getExternalStorageDirectory
										 * ().getAbsolutePath()
										 */);
			bundle.putInt("mode", FileBrowser.BROWSE_MODE_FILES);
			showDialog(DIALOG_TYPE_CARD_DB_DIY, bundle);
		} else if (preference.getKey().equals(
				Settings.KEY_PREF_GAME_DIY_CARD_BACK)) {
			Bundle bundle = new Bundle();
			bundle.putString("url", StaticApplication.peekInstance()
					.getCoreSkinPath()
					+ File.separator
					+ Constants.CORE_SKIN_CARD_BACK);
			bundle.putInt("title_res", R.string.settings_game_card_back);
			bundle.putIntArray("orig_size", Constants.CORE_SKIN_CARD_BACK_SIZE);
			mImageBundle = bundle;
			showDialog(DIALOG_TYPE_IMAGE_PREVIEW, bundle);
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
			showDialog(DIALOG_TYPE_CARD_DB_RESET, bundle);
		}
		return false;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			if (dialog instanceof FileChooseDialog) {
				String newUrl = ((FileChooseController) getDialog()
						.getController()).getUrl();
				CardDBCopyTask task = new CardDBCopyTask(getActivity());
				task.setCardDBCopyListener(this);
				if (Build.VERSION.SDK_INT >= 11) {
					task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
							newUrl);
				} else {
					task.execute(newUrl);
				}
			} else if (dialog instanceof SimpleDialog) {
				CardDBResetTask task = new CardDBResetTask(getActivity());
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
		dismissDialog();
		Crop.pickImage(this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent result) {
		if (requestCode == Crop.REQUEST_PICK
				&& resultCode == Activity.RESULT_OK) {
			beginCrop(result.getData(), mImageBundle);
		} else if (requestCode == Crop.REQUEST_CROP) {
			handleCrop(resultCode, result);
		}
	}

	private void handleCrop(int resultCode, Intent result) {
		if (resultCode == Activity.RESULT_OK) {
			setNewImage(Crop.getOutput(result), result.getExtras());
		} else if (resultCode == Crop.RESULT_ERROR) {
			Toast.makeText(getActivity(), Crop.getError(result).getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void beginCrop(Uri source, Bundle param) {
		Uri outputUri = Uri.fromFile(new File(getActivity().getCacheDir(),
				"cropped"));
		int[] sizeArray = param.getIntArray("orig_size");
		new Crop(source).withAspect(sizeArray[0], sizeArray[1])
				.output(outputUri).start(getActivity(), this, param);
	}

	private void setNewImage(Uri uri, Bundle param) {
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
		ImageCopyTask task = new ImageCopyTask(getActivity());
		task.setImageCopyListener(this);
		param.putString("src_url", path);
		if (Build.VERSION.SDK_INT >= 11) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, param);
		} else {
			task.execute(param);
		}
	}

	@Override
	public BaseDialog onCreateDialog(int type, Bundle param) {
		BaseDialog dlg = null;
		if (type == DIALOG_TYPE_IMAGE_PREVIEW) {
			View view = LayoutInflater.from(getActivity()).inflate(
					R.layout.image_preview_content, null);
			dlg = new ImagePreviewDialog(getActivity(), view, this, this, param);
		} else if (type == DIALOG_TYPE_CARD_DB_DIY) {
			View view = LayoutInflater.from(getActivity()).inflate(
					R.layout.file_browser_layout, null);
			dlg = new FileChooseDialog(getActivity(), view, this, param);
		} else if (type == DIALOG_TYPE_CARD_DB_RESET) {
			dlg = new SimpleDialog(getActivity(), this, null, param);
		}
		return dlg;
	}

	@Override
	public void onImageCopyFinished(Bundle dstPath) {
		showDialog(DIALOG_TYPE_IMAGE_PREVIEW, dstPath);
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
		SuperActivityToast.create(getActivity(), errorMessage,
				SuperToast.Duration.MEDIUM).show();
		mImageBundle = null;
	}

	@Override
	public void onCardDBResetFinished(Boolean result) {
		SuperActivityToast.create(
				getActivity(),
				result ? getResources().getString(R.string.reset_card_success)
						: getResources().getString(R.string.reset_card_failed),
				SuperToast.Duration.MEDIUM).show();
		mImageBundle = null;
	}
}

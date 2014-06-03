package cn.garymb.ygomobile.fragment.setting;

import java.io.File;

import com.soundcloud.android.crop.Crop;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.common.ImageCopyTask;
import cn.garymb.ygomobile.common.ImageCopyTask.ImageCopyListener;
import cn.garymb.ygomobile.model.data.ImageItemInfoHelper;
import cn.garymb.ygomobile.setting.Settings;
import cn.garymb.ygomobile.utils.FileOpsUtils;
import cn.garymb.ygomobile.widget.BaseDialog;
import cn.garymb.ygomobile.widget.ImagePreviewDialog;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
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
public class GameSettingsFragment extends DialogPreferenceFragment implements
		OnPreferenceChangeListener, OnPreferenceClickListener, OnClickListener,
		android.content.DialogInterface.OnClickListener, ImageCopyListener {

	private ListPreference mOGLESPreference;

	private ListPreference mCardQualityPreference;

	private ListPreference mFontNamePreference;

	private Preference mCoverDiyPreference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_game);

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
		if (preference.getKey().equals(Settings.KEY_PREF_GAME_DIY_COVER)) {
			Bundle bundle = new Bundle();
			bundle.putString("url", StaticApplication.peekInstance()
					.getCoreSkinPath() + File.separator + Constants.CORE_SKIN_COVER);
			bundle.putIntArray("orig_size", Constants.CORE_SKIN_COVER_SIZE);
			showDialog(0, bundle);
		}
		return false;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
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
			beginCrop(result.getData());
		} else if (requestCode == Crop.REQUEST_CROP) {
			handleCrop(resultCode, result);
		}
	}

	private void handleCrop(int resultCode, Intent result) {
		if (resultCode == Activity.RESULT_OK) {
			setNewImage(Crop.getOutput(result));
		} else if (resultCode == Crop.RESULT_ERROR) {
			Toast.makeText(getActivity(), Crop.getError(result).getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void beginCrop(Uri source) {
		Uri outputUri = Uri.fromFile(new File(getActivity().getCacheDir(),
				"cropped"));
		new Crop(source).withAspect(1024, 640).output(outputUri)
				.withMaxSize(1024, 640).start(getActivity(), this);
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
			ImageCopyTask task = new ImageCopyTask(getActivity());
			task.setImageCopyListener(this);
			task.executeOnExecutor(
					AsyncTask.THREAD_POOL_EXECUTOR, path, StaticApplication
							.peekInstance().getCoreSkinPath()
							+ File.separator
							+ Constants.CORE_SKIN_COVER);
		} else {
			ImageCopyTask task = new ImageCopyTask(getActivity());
			task.setImageCopyListener(this);
			task.execute(path, StaticApplication.peekInstance()
					.getCoreSkinPath()
					+ File.separator
					+ Constants.CORE_SKIN_COVER);
		}
	}

	@Override
	public BaseDialog onCreateDialog(int type, Bundle param) {
		View view = LayoutInflater.from(getActivity()).inflate(
				R.layout.image_preview_content, null);
		return new ImagePreviewDialog(getActivity(), view, this, this, param);
	}

	@Override
	public void onCopyFinished(Bundle dstPath) {
		showDialog(0, dstPath);
	}
}

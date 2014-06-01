package cn.garymb.ygomobile;

import java.io.File;
import java.util.List;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.setting.Settings;
import cn.garymb.ygomobile.utils.DeviceUtils;
import cn.garymb.ygomobile.widget.FileChooseController;
import cn.garymb.ygomobile.widget.FileChooseDialog;
import cn.garymb.ygomobile.widget.filebrowser.FolderNavigator.NavigateItemChangeListener;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener, OnClickListener {
	
	private Preference mGameResPath;
	private Preference mVersionPref;
	private Preference mOpensourcePref;
	private ListPreference mOGLESPreference;
	private ListPreference mCardQualityPreference;
	private ListPreference mFontNamePreference;
	private AlertDialog mDialog;
	
	@SuppressWarnings("deprecation")
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
					mGameResPath.setSummary(StaticApplication.peekInstance().getResourcePath());
				}
				mGameResPath.setOnPreferenceClickListener(this);
			} else if (action.equals(Constants.SETTINGS_ACTION_GAME)) {
				addPreferencesFromResource(R.xml.preference_game);
				mOGLESPreference = (ListPreference) findPreference(Settings.KEY_PREF_GAME_OGLES_CONFIG);
				mOGLESPreference.setSummary(mOGLESPreference.getEntry());
				mOGLESPreference.setOnPreferenceChangeListener(this);
				
				mCardQualityPreference = (ListPreference) findPreference(Settings.KEY_PREF_GAME_IMAGE_QUALITY);
				mCardQualityPreference.setSummary(mCardQualityPreference.getEntry());
				mCardQualityPreference.setOnPreferenceChangeListener(this);
				
				mFontNamePreference = (ListPreference) findPreference(Settings.KEY_PREF_GAME_FONT_NAME);
				File fontsPath = new File(StaticApplication.peekInstance().getResourcePath(), Constants.FONT_DIRECTORY);
				mFontNamePreference.setEntries(fontsPath.list());
				mFontNamePreference.setEntryValues(fontsPath.list());
				if (TextUtils.isEmpty(mFontNamePreference.getValue())) {
					mFontNamePreference.setValue(Constants.DEFAULT_FONT_NAME);
				}
				mFontNamePreference.setSummary(mFontNamePreference.getValue());
			} else  if (action.equals(Constants.SETTINGS_ACTION_ABOUT)) {
				addPreferencesFromResource(R.xml.preference_about);
				mVersionPref = findPreference(Settings.KEY_PREF_ABOUT_VERSION);
				mVersionPref.setOnPreferenceClickListener(this);
				mOpensourcePref = findPreference(Settings.KEY_PREF_ABOUT_OPENSOURCE);
				mOpensourcePref.setOnPreferenceClickListener(this);
				Context context = StaticApplication.peekInstance();
				try {
					mVersionPref.setSummary(context.getPackageManager().getPackageInfo(
							context.getPackageName(), 0).versionName);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
			}
		} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			// Load the legacy preferences headers
			addPreferencesFromResource(R.xml.preference_headers_legacy);
		}
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
	}
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	protected boolean isValidFragment(String fragmentName) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			return Constants.SETTINGS_FARGMENT_ABOUT.equals(fragmentName) ||
					Constants.SETTINGS_FARGMENT_COMMON.equals(fragmentName) ||
					Constants.SETTINGS_FARGMENT_GAME.equals(fragmentName);
		} else {
			return true;
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.getKey().equals(Settings.KEY_PREF_GAME_OGLES_CONFIG)) {
			mOGLESPreference.setValue((String) newValue);
			mOGLESPreference.setSummary(mOGLESPreference.getEntry());
		} else if(preference.getKey().equals(Settings.KEY_PREF_GAME_IMAGE_QUALITY)) {
			mCardQualityPreference.setValue((String) newValue);
			mCardQualityPreference.setSummary(mCardQualityPreference.getEntry());
		} else if (preference.getKey().equals(Settings.KEY_PREF_GAME_FONT_NAME)) {
			mFontNamePreference.setValue((String) newValue);
			mFontNamePreference.setSummary(mFontNamePreference.getEntry());
		}
		return false;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		AlertDialog dlg = null;
		if (preference.equals(mOpensourcePref)) {
			mDialog = dlg = DeviceUtils.createOpenSourceDialog(this);
			mDialog.show();

		} else if (preference.equals(mVersionPref)) {
			mDialog = dlg = DeviceUtils.createChangeLogDialog(this);
			mDialog.show();
		} else if (preference.getKey().equals(Settings.KEY_PREF_GAME_RESOURCE_PATH)) {
			Bundle bundle = new Bundle();
			bundle.putString("root", StaticApplication.sRootPair.second);
			bundle.putString("current", StaticApplication.peekInstance().getResourcePath());
			mDialog = new FileChooseDialog(this, this, bundle);
			mDialog.show();
		}
		if (dlg != null) {
			final Resources res = getResources();
			// Title
			final int titleId = res
					.getIdentifier("alertTitle", "id", "android");
			final View title = dlg.findViewById(titleId);
			if (title != null) {
				((TextView) title).setTextColor(res
						.getColor(R.color.apptheme_color));
			}
			// Title divider
			final int titleDividerId = res.getIdentifier("titleDivider", "id",
					"android");
			final View titleDivider = dlg.findViewById(titleDividerId);
			if (titleDivider != null) {
				titleDivider.setBackgroundColor(res
						.getColor(R.color.apptheme_color));
			}
		}
		return false;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			if (mDialog instanceof FileChooseDialog) {
				String newUrl = ((FileChooseController)((FileChooseDialog) mDialog).getController()).getUrl();
				StaticApplication.peekInstance().setResourcePath(newUrl);
				mGameResPath.setSummary(newUrl);		
			}
		}
	}
}

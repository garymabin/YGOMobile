package cn.garymb.ygomobile.fragment.setting;

import java.io.File;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.setting.Settings;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.text.TextUtils;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GameSettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener {
	
	private ListPreference mOGLESPreference;
	
	private ListPreference mCardQualityPreference;
	
	private ListPreference mFontNamePreference;
	
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
		File fontsPath = new File(StaticApplication.peekInstance().getResourcePath(), Constants.FONT_DIRECTORY);
		mFontNamePreference.setEntries(fontsPath.list());
		mFontNamePreference.setEntryValues(fontsPath.list());
		if (TextUtils.isEmpty(mFontNamePreference.getValue())) {
			mFontNamePreference.setValue(Constants.DEFAULT_FONT_NAME);
		}
		mFontNamePreference.setSummary(mFontNamePreference.getValue());
		
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
}

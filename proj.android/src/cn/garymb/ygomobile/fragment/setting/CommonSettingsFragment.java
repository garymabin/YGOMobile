package cn.garymb.ygomobile.fragment.setting;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.setting.Settings;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.text.TextUtils;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CommonSettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener {
	
	private EditTextPreference mResPathPreference;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_common);
		mResPathPreference = (EditTextPreference) findPreference(Settings.KEY_PREF_GAME_RESOURCE_PATH);
		if (TextUtils.isEmpty(mResPathPreference.getText())) {
			mResPathPreference.setText(StaticApplication.peekInstance().getDefaultResPath());
		}
		mResPathPreference.setSummary(mResPathPreference.getText());
		mResPathPreference.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals(Settings.KEY_PREF_GAME_RESOURCE_PATH)) {
			mResPathPreference.setSummary((CharSequence) newValue);
			mResPathPreference.setText((String) newValue);
		}
		return false;
	}
}

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
	
	private EditTextPreference mCardPathPreference;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_common);
		mCardPathPreference = (EditTextPreference) findPreference(Settings.KEY_PREF_COMMON_CARD_PATH);
		if (TextUtils.isEmpty(mCardPathPreference.getText())) {
			mCardPathPreference.setText(StaticApplication.peekInstance().getDefaultImageCacheRootPath());
		}
		mCardPathPreference.setSummary(mCardPathPreference.getText());
		mCardPathPreference.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals(Settings.KEY_PREF_COMMON_CARD_PATH)) {
			mCardPathPreference.setSummary((CharSequence) newValue);
			mCardPathPreference.setText((String) newValue);
		}
		return false;
	}
}

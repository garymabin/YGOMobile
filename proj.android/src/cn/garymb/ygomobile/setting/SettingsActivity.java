package cn.garymb.ygomobile.setting;

import java.util.List;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.common.Constants;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsActivity extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.action_settings);
		String action = getIntent().getAction();
		if (action != null) {
			if (action.equals(Constants.SETTINGS_ACTION_COMMON)) {
				addPreferencesFromResource(R.xml.preference_common);
			} else if (action.equals(Constants.SETTINGS_ACTION_GAME)) {
				addPreferencesFromResource(R.xml.preference_game);
			} else  if (action.equals(Constants.SETTINGS_ACTION_ABOUT)) {
				addPreferencesFromResource(R.xml.preference_about);
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

}

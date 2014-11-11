package cn.garymb.ygomobile;

import cn.garymb.ygomobile.fragment.setting.AboutSettingsFragment;
import cn.garymb.ygomobile.fragment.setting.CommonSettingsFragment;
import cn.garymb.ygomobile.fragment.setting.GameSettingsFragment;
import cn.garymb.ygomobile.setting.Settings;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

public class SettingsActivity extends ActionBarActivity {

	public class SettingsFragment extends PreferenceFragment implements
			OnPreferenceClickListener {

		private Preference mAboutPreference;

		private Preference mGamePreference;

		private Preference mCommonPreference;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_headers_legacy);

			mCommonPreference = findPreference(Settings.KEY_PREF_COMMON_SETTINGS);
			mCommonPreference.setOnPreferenceClickListener(this);

			mGamePreference = findPreference(Settings.KEY_PREF_GAME_SETTINGS);
			mGamePreference.setOnPreferenceClickListener(this);

			mAboutPreference = findPreference(Settings.KEY_PREF_ABOUT_SETTINGS);
			mAboutPreference.setOnPreferenceClickListener(this);
		}

		@Override
		public boolean onPreferenceClick(Preference preference) {
			PreferenceFragment fragment = null;
			if (Settings.KEY_PREF_COMMON_SETTINGS.equals(preference.getKey())) {
				fragment = new CommonSettingsFragment();
			} else if (Settings.KEY_PREF_GAME_SETTINGS.equals(preference
					.getKey())) {
				fragment = new GameSettingsFragment();
			} else {
				fragment = new AboutSettingsFragment();
			}
			getFragmentManager().beginTransaction()
					.replace(R.id.content_frame, fragment).addToBackStack(null)
					.commit();
			return true;
		}
	}

	private Toolbar mToolBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		mToolBar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolBar);
		setTitle(R.string.action_settings);
		initActionBar();
		getFragmentManager().beginTransaction()
				.replace(R.id.content_frame, new SettingsFragment()).commit();
	}

	private void initActionBar() {
		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayShowTitleEnabled(true);
	}
}

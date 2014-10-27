package cn.garymb.ygomobile.fragment.setting;

import com.umeng.update.UmengUpdateAgent;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.setting.Settings;
import cn.garymb.ygomobile.widget.BaseDialog;
import cn.garymb.ygomobile.widget.WebViewDialog;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.webkit.WebView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AboutSettingsFragment extends EventDialogPreferenceFragment implements OnPreferenceClickListener {

	private Preference mVersionPref;
	private Preference mOpensourcePref;
	private Preference mCheckUpdatePref;
	
	private static final int DIALOG_TYPE_VERSION = 0;
	private static final int DIALOG_TYPE_OPEN_SOURCE = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_about);
		mVersionPref = findPreference(Settings.KEY_PREF_ABOUT_VERSION);
		mVersionPref.setOnPreferenceClickListener(this);
		mOpensourcePref = findPreference(Settings.KEY_PREF_ABOUT_OPENSOURCE);
		mOpensourcePref.setOnPreferenceClickListener(this);
		mCheckUpdatePref = findPreference(Settings.KEY_PREF_ABOUT_CHECK_UPDATE);
		mCheckUpdatePref.setOnPreferenceClickListener(this);
		mVersionPref.setSummary(StaticApplication.peekInstance().getVersionName());
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.equals(mOpensourcePref)) {
			Bundle bundle = new Bundle();
			bundle.putString("url", "file:///android_asset/licenses.html");
			bundle.putInt("titleRes", R.string.settings_about_opensource_pref);
			showDialog(DIALOG_TYPE_OPEN_SOURCE, bundle);

		} else if (preference.equals(mVersionPref)) {
			Bundle bundle = new Bundle();
			bundle.putString("url", "file:///android_asset/changelog.html");
			bundle.putInt("titleRes", R.string.settings_about_change_log);
			showDialog(DIALOG_TYPE_VERSION, bundle);
		} else if (preference.equals(mCheckUpdatePref)) {
			UmengUpdateAgent.forceUpdate(getActivity());
		}
		return false;
	}
	
	@Override
	public BaseDialog onCreateDialog(int type, Bundle param) {
		if (type == DIALOG_TYPE_OPEN_SOURCE || type == DIALOG_TYPE_VERSION) {
			return new WebViewDialog(getActivity(), new WebView(getActivity()), null, param);
		}
		return null;
	}
}

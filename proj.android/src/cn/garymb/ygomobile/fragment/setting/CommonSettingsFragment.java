package cn.garymb.ygomobile.fragment.setting;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.setting.Settings;
import cn.garymb.ygomobile.widget.FileChooseController;
import cn.garymb.ygomobile.widget.FileChooseDialog;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.text.TextUtils;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CommonSettingsFragment extends PreferenceFragment implements
		OnPreferenceClickListener, OnClickListener {

	private Preference mResPathPreference;

	private FileChooseDialog mDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_common);
		mResPathPreference = findPreference(Settings.KEY_PREF_GAME_RESOURCE_PATH);
		if (TextUtils.isEmpty(mResPathPreference.getSummary())) {
			mResPathPreference.setSummary(StaticApplication.peekInstance()
					.getResourcePath());
		}
		mResPathPreference.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals(Settings.KEY_PREF_GAME_RESOURCE_PATH)) {
			Bundle bundle = new Bundle();
			bundle.putString("root", StaticApplication.sRootPair.second);
			bundle.putString("current", StaticApplication.peekInstance().getResourcePath());
			mDialog = new FileChooseDialog(getActivity(), this, bundle);
			mDialog.show();
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
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			String newUrl = ((FileChooseController) mDialog.getController())
					.getUrl();
			StaticApplication.peekInstance().setResourcePath(newUrl);
			mResPathPreference.setSummary(newUrl);
		}
	}
}

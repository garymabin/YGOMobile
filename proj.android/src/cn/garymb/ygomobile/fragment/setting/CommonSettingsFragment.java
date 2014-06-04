package cn.garymb.ygomobile.fragment.setting;


import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.setting.Settings;
import cn.garymb.ygomobile.widget.BaseDialog;
import cn.garymb.ygomobile.widget.FileChooseController;
import cn.garymb.ygomobile.widget.FileChooseDialog;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CommonSettingsFragment extends EventDialogPreferenceFragment implements
		OnPreferenceClickListener, OnClickListener {

	private Preference mResPathPreference;

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
			bundle.putString("root", Environment.getExternalStorageDirectory().getAbsolutePath());
			bundle.putString("current", StaticApplication.peekInstance().getResourcePath());
			showDialog(0, bundle);
		}
		return false;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			String newUrl = ((FileChooseController) getDialog().getController())
					.getUrl();
			StaticApplication.peekInstance().setResourcePath(newUrl);
			mResPathPreference.setSummary(newUrl);
		}
	}

	@Override
	public BaseDialog onCreateDialog(int type, Bundle param) {
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.file_browser_layout, null);
		return new FileChooseDialog(getActivity(), view, this, param);
	}
}

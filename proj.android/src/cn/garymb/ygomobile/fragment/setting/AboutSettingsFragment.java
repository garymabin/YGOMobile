package cn.garymb.ygomobile.fragment.setting;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.setting.Settings;
import cn.garymb.ygomobile.utils.DeviceUtils;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AboutSettingsFragment extends PreferenceFragment implements OnPreferenceClickListener {

	private Preference mVersionPref;
	private Preference mOpensourcePref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

	@Override
	public boolean onPreferenceClick(Preference preference) {
		AlertDialog dlg = null;
		if (preference.equals(mOpensourcePref)) {
			dlg = DeviceUtils.createOpenSourceDialog(getActivity());
			dlg.show();

		} else if (preference.equals(mVersionPref)) {
			dlg = DeviceUtils.createChangeLogDialog(getActivity());
			dlg.show();
		}
		if (dlg != null) {
			final Resources res = getActivity().getResources();
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
}

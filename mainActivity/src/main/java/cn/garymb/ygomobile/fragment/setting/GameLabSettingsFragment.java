package cn.garymb.ygomobile.fragment.setting;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.PendulumScaleCopyTask;
import cn.garymb.ygomobile.setting.Settings;
import cn.garymb.ygomobile.widget.BaseDialog;

public class GameLabSettingsFragment extends EventDialogPreferenceFragment implements OnPreferenceClickListener {
	
	private CheckBoxPreference mPendulumPreference;

	@Override
	public BaseDialog onCreateDialog(int type, Bundle param) {
		return null;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_lab);
		mPendulumPreference = (CheckBoxPreference) findPreference(Settings.KEY_PREF_GAME_LAB_PENDULUM_SCALE);
		mPendulumPreference.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (Settings.KEY_PREF_GAME_LAB_PENDULUM_SCALE.equals(preference.getKey())) {
			if (mPendulumPreference.isChecked()) {
				if (Build.VERSION.SDK_INT >= 11) {
					new PendulumScaleCopyTask(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "extra/pendulumscale",
							StaticApplication.peekInstance().getCoreSkinPath() + "/extra/");
				} else {
					new PendulumScaleCopyTask(getActivity()).execute("extra/pendulumscale",
							StaticApplication.peekInstance().getCoreSkinPath() + "/extra/");
				}
			}
		}
		return false;
	}

}

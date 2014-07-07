package cn.garymb.ygomobile.fragment.setting;

import com.github.johnpersano.supertoasts.SuperToast;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.AppUpdateTask;
import cn.garymb.ygomobile.core.Controller;
import cn.garymb.ygomobile.model.data.VersionInfo;
import cn.garymb.ygomobile.setting.Settings;
import cn.garymb.ygomobile.widget.AppUpdateController;
import cn.garymb.ygomobile.widget.AppUpdateDialog;
import cn.garymb.ygomobile.widget.BaseDialog;
import cn.garymb.ygomobile.widget.WebViewDialog;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AboutSettingsFragment extends EventDialogPreferenceFragment implements OnPreferenceClickListener, OnClickListener {

	private Preference mVersionPref;
	private Preference mOpensourcePref;
	private Preference mCheckUpdatePref;
	
	private static final int DIALOG_TYPE_VERSION = 0;
	private static final int DIALOG_TYPE_OPEN_SOURCE = 1;
	private static final int DIALOG_TYPE_APP_UPDATE = 2;
	
	private static final int MSG_TYPE_CHECK_UPDATE = 0;
	
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
			Controller.peekInstance().asyncCheckUpdate(Message.obtain(mHandler, MSG_TYPE_CHECK_UPDATE));
		}
		return false;
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		if (super.handleMessage(msg)) {
			return true;
		}
		int type = msg.what;
		if (type == MSG_TYPE_CHECK_UPDATE) {
			VersionInfo info = (VersionInfo) msg.obj;
			if (info != null) {
				if (info.version <= StaticApplication.peekInstance().getVersionCode()) {
					SuperToast.create(getActivity(), getResources().getString(R.string.settings_about_no_update),
							SuperToast.Duration.VERY_SHORT).show();
				} else {
					Bundle bundle = new Bundle();
					bundle.putInt("version", info.version);
					bundle.putInt("titleRes", R.string.settings_about_new_version);
					bundle.putString("url", info.url);
					showDialog(DIALOG_TYPE_APP_UPDATE, bundle);
				}
			}
		}
		return false;
	}

	@Override
	public BaseDialog onCreateDialog(int type, Bundle param) {
		if (type == DIALOG_TYPE_OPEN_SOURCE || type == DIALOG_TYPE_VERSION) {
			return new WebViewDialog(getActivity(), new WebView(getActivity()), null, param);
		} else if (type == DIALOG_TYPE_APP_UPDATE) {
			View view = LayoutInflater.from(getActivity()).inflate(R.layout.app_update_content, null);
			return new AppUpdateDialog(getActivity(), this, view, param);
		}
		return null;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == Dialog.BUTTON_POSITIVE) {
			dismissDialog();
			String url = ((AppUpdateController)getDialog().getController()).getDownloadUrl();
			new AppUpdateTask(getActivity()).execute(url);
		}
	}
}

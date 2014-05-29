package cn.garymb.ygomobile.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.webkit.WebView;
import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;

public class DeviceUtils {

	public static float getScreenWidth() {
		return StaticApplication.peekInstance().getScreenWidth();
	}

	public static float getDensity() {
		return StaticApplication.peekInstance().getDensity();
	}

	public static final AlertDialog createOpenSourceDialog(Context paramContext) {
		WebView localWebView = new WebView(paramContext);
		localWebView.loadUrl("file:///android_asset/licenses.html");
		AlertDialog dlg =  new AlertDialog.Builder(paramContext).setTitle(R.string.settings_about_opensource_pref)
				.setView(localWebView).setPositiveButton(R.string.button_ok, null)
				.create();
        return dlg;
	}
}

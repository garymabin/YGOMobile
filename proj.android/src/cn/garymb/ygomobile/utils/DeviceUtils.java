package cn.garymb.ygomobile.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;

public class DeviceUtils {

	public static float getScreenWidth() {
		return StaticApplication.peekInstance().getScreenWidth();
	}
	
	public static float getScreenHeight() {
		return StaticApplication.peekInstance().getScreenHeight();
	}

	public static float getDensity() {
		return StaticApplication.peekInstance().getDensity();
	}
	
	public static float getXScale() {
		return StaticApplication.peekInstance().getXScale();
	}
	
	public static float getYScale() {
		return StaticApplication.peekInstance().getYScale();
	}

	public static final AlertDialog createOpenSourceDialog(Context paramContext) {
		WebView localWebView = new WebView(paramContext);
		localWebView.loadUrl("file:///android_asset/licenses.html");
		AlertDialog dlg =  new AlertDialog.Builder(paramContext).setTitle(R.string.settings_about_opensource_pref)
				.setView(localWebView).setPositiveButton(R.string.button_ok, null)
				.create();
        return dlg;
	}
	
	public static final AlertDialog createChangeLogDialog(Context paramContext) {
		WebView localWebView = new WebView(paramContext);
		localWebView.loadUrl("file:///android_asset/changelog.html");
		localWebView.getSettings().setDefaultTextEncodingName("utf-8");
		AlertDialog dlg =  new AlertDialog.Builder(paramContext).setTitle(R.string.settings_about_change_log)
				.setView(localWebView).setPositiveButton(R.string.button_ok, null)
				.create();
        return dlg;
	}
	
	public static final AlertDialog createFileBrowseDialog(Context paramContext, OnClickListener listener) {
		View view = LayoutInflater.from(paramContext).inflate(R.layout.file_browser_layout, null);
		AlertDialog dlg =  new AlertDialog.Builder(paramContext).setView(view).setPositiveButton(R.string.button_ok, listener)
				.setNegativeButton(R.string.button_cancel, listener)
				.create();
        return dlg;
	}
}

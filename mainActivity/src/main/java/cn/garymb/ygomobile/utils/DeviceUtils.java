package cn.garymb.ygomobile.utils;

import java.io.File;
import java.net.URI;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.model.data.ResourcesConstants;

public class DeviceUtils {

	public static float getScreenWidth() {
		return StaticApplication.peekInstance().getScreenWidth();
	}
	
	public static float getSmallerSize() {
		return StaticApplication.peekInstance().getSmallerSize();
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
	
	/**
	 * 调用系统安装界面
	 * @param context Context
	 * @param packageName 待安装的目标程序packagePath
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static void reqSystemInstall(Context context, String packagePath) {
		if (TextUtils.isEmpty(packagePath))
			return;
		File targetFile = new File(packagePath);
		if (!targetFile.exists() || targetFile.isDirectory())
			return;
		
		Uri packageURI = Uri.fromFile(targetFile);      
		Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
		intent.setData(packageURI);
		intent.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);
		try {
			if (context instanceof Activity && Build.VERSION.SDK_INT >= 14) {
				intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
				((Activity) context).startActivityForResult(intent, 0);
			} else {
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		} catch (ActivityNotFoundException anfe) { }
	}
	
	/**
	 * 调用系统安装界面
	 * @param context Context
	 * @param packageName 待安装的目标程序packagePath
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static void reqSystemInstall(Context context, URI uri) {
		if (uri == null)
			return;
		File targetFile = new File(uri);
		if (!targetFile.exists() || targetFile.isDirectory())
			return;
		
		Uri packageURI = Uri.fromFile(targetFile);      
		Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
		intent.setData(packageURI);
		intent.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);
		try {
			if (context instanceof Activity && Build.VERSION.SDK_INT >= 14) {
				intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
				((Activity) context).startActivityForResult(intent, 0);
			} else {
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		} catch (ActivityNotFoundException anfe) { }
	}
	
	public static boolean testUpdateVersion(String url) {
		Context context = StaticApplication.peekInstance();
		String base = context.getExternalFilesDir(ResourcesConstants.VERSION_UPDATE_CACHE_DIR).getAbsolutePath();
		File file = new File(base, url.substring(url.lastIndexOf('/') + 1, url.length()));
		if (file.exists()) {
			PackageManager pm = context.getPackageManager();
			PackageInfo info = pm.getPackageArchiveInfo(url,PackageManager.GET_SIGNATURES);
			if (info != null) {
				try {
					Signature[] sig = info.signatures;
					PackageInfo currentInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
					if (currentInfo.signatures[0].equals(sig[0])){
						return true;
					}
				} catch (NameNotFoundException e) {
					return false;
				}
			}
		}
		return false;
	}
	
}

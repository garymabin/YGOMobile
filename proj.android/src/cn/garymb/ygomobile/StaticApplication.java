/*
 * StaticApplication.java
 *
 *  Created on: 2014年2月24日
 *      Author: mabin
 */
package cn.garymb.ygomobile;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Environment;
import android.util.Log;

import org.acra.*;
import org.acra.annotation.ReportsCrashes;

import static org.acra.ReportField.*;

import com.github.nativehandler.NativeCrashHandler;

@ReportsCrashes(
		formKey = "", // will not be used
		customReportContent = { APP_VERSION_NAME, ANDROID_VERSION, PHONE_MODEL, CUSTOM_DATA, STACK_TRACE, USER_CRASH_DATE, LOGCAT, BUILD, TOTAL_MEM_SIZE, DISPLAY, DUMPSYS_MEMINFO, DEVICE_FEATURES, ENVIRONMENT }, 
        mailTo = "garymabin@gmail.com",
        includeDropBoxSystemTags = true,
        mode = ReportingInteractionMode.DIALOG,
        resDialogText = R.string.crashed,
        resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
        resDialogTitle = R.string.crash_title, // optional. default is your application name
        resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
        resDialogOkToast = R.string.crash_dialog_ok_toast
        
)
public class StaticApplication extends Application {
	
	private static StaticApplication INSANCE;
	
	static {
		System.loadLibrary("YGOMobile");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		new NativeCrashHandler().registerForNativeCrash(this);
		ACRA.init(this);
		CrashSender sender = new CrashSender(this);
		ACRA.getErrorReporter().setReportSender(sender);
		INSANCE = this;
	}
	
	public static StaticApplication peekInstance() {
		return INSANCE;
	}
	
	public void setResourcePath(String path) {
		SharedPreferences sp = getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(Constants.RESOURCE_PATH, path);
		editor.commit();
	}
	
	public String getResourcePath() {
		SharedPreferences sp = getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
        return sp.getString(Constants.RESOURCE_PATH, Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.WORKING_DIRECTORY);
	}
	
	public int getOpenglVersion() {
		SharedPreferences sp = getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
		return sp.getInt(Constants.OPENGL_PATH, 1);
	}
	
	public void setOpenglVersion(int version) {
		SharedPreferences sp = getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(Constants.OPENGL_PATH, version);
		editor.commit();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Application#onTerminate()
	 */
	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
		Log.d("StaticApplication", "onTerminate");
	}
	
	public byte[] getSignInfo() {
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
			Signature[] signs = pi.signatures;
			Signature sign = signs[0];
			return parseSignature(sign.toByteArray());
		} catch (Exception e) {
		}
		return null;
	}
	
	private byte[] parseSignature(byte[] signature) {
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(signature));
			byte[] buffer = cert.getEncoded();
			return Arrays.copyOf(buffer, 16);
		} catch (Exception e) {
		}
		return null;
	}

}

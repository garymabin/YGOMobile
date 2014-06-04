package cn.garymb.ygomobile.common;

import java.io.File;

import cn.garymb.ygomobile.core.Controller;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import cn.garymb.ygomobile.utils.DeviceUtils;
import cn.garymb.ygomobile.utils.FileOpsUtils;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;

public class AppUpdateTask extends AsyncTask<String, Void, Void> {
	
	private Context mContext;
	
	public AppUpdateTask(Context context) {
		mContext = context;
	}

	@Override
	protected Void doInBackground(String... params) {
		String url = params[0];
		String testPath =
				mContext.getExternalFilesDir(ResourcesConstants.VERSION_UPDATE_CACHE_DIR) + 
				File.separator + url.substring(url.lastIndexOf("/") + 1, url.length());
		if (testUpdateVersion(testPath)){
			DeviceUtils.reqSystemInstall(mContext, testPath);
		} else {
			FileOpsUtils.recursiveDetele(FileOpsUtils.getFilePathFromUrl(url));
			Controller.peekInstance().downloadNewAppVersion(url);
		}
		return null;
	}
	
	public boolean testUpdateVersion(String url) {
		String base = mContext.getExternalFilesDir(ResourcesConstants.VERSION_UPDATE_CACHE_DIR).getAbsolutePath();
		File file = new File(base, url.substring(url.lastIndexOf('/') + 1, url.length()));
		if (file.exists()) {
			PackageManager pm = mContext.getPackageManager();
			PackageInfo info = pm.getPackageArchiveInfo(url,PackageManager.GET_SIGNATURES);
			if (info != null) {
				try {
					Signature[] sig = info.signatures;
					PackageInfo currentInfo = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_SIGNATURES);
					if (currentInfo != null && sig != null && currentInfo.signatures[0].equals(sig[0])){
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

package cn.garymb.ygomobile.core;

import java.util.HashMap;
import java.util.Map;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.model.data.ResourcesConstants;

public class DownloadHandler {
	
	private Context mContext;
	private DownloadManager mDM;
	
	private DownloadReceiver mReceiver;
	
	private Map<Long, String> mDownloadIDs;
	
	public DownloadHandler(StaticApplication app) {
		mContext = app;
		mDM = (DownloadManager) app.getSystemService(Context.DOWNLOAD_SERVICE);
		mDownloadIDs = new HashMap<Long, String>();
		mReceiver = new DownloadReceiver(mContext, mDM, mDownloadIDs);
	}

	public void enqueueDownload(String url) {
		if (mDownloadIDs.containsValue(url)) {
			Log.i("test", "ignore duplicated download task");
			return;
		}
		Request req = new Request(Uri.parse(url));
		String saveFileName = url.substring(url.lastIndexOf("/") + 1, url.length());
		req.setTitle(saveFileName);
		req.setVisibleInDownloadsUi(false);
		req.setDestinationInExternalFilesDir(mContext,
				ResourcesConstants.VERSION_UPDATE_CACHE_DIR, saveFileName);
		mDownloadIDs.put(mDM.enqueue(req), url);
	}
	
	public boolean isDownloadsEmpty() {
		return mDownloadIDs.size() == 0;
	}
	
	/* package */ void register() {
		mReceiver.register();
	}

	/* package */ void unregister() {
		mReceiver.unregister();
	}

}

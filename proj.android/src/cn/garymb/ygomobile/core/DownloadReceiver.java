package cn.garymb.ygomobile.core;

import java.net.URI;
import java.util.Map;

import cn.garymb.ygomobile.utils.DeviceUtils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

public class DownloadReceiver extends BroadcastReceiver {

	private static final String TAG = "DownloadReceiver";

	private Context mContext;
	
	private Map<Long, String> mDownloadIDs;
	
	private DownloadManager mDM;
	
	public DownloadReceiver(Context context, DownloadManager dm, Map<Long, String> downloadIDs) {
		mDM = dm;
		mContext = context;
		mDownloadIDs = downloadIDs;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive download complete!");
		long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
		if (!mDownloadIDs.containsKey(id)) {
			return;
		}
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(id);
		Cursor cursor = mDM.query(query);
		if (cursor == null || !cursor.moveToFirst()) {
			return;
		}
		int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
		if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
		    Toast.makeText(mContext, "Download Failed", Toast.LENGTH_SHORT).show();
		    mDownloadIDs.remove(id);
			if (mDownloadIDs.size() == 0) {
				unregister();
			}
		    return;
		}

		int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
		String uriString = cursor.getString(uriIndex);
		DeviceUtils.reqSystemInstall(mContext, URI.create(uriString));
		mDownloadIDs.remove(id);
		if (mDownloadIDs.size() == 0) {
			unregister();
		}
	}
	
	public void register() {
		final IntentFilter filter = new IntentFilter();
		filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		mContext.registerReceiver(this, filter);
	}
	
	public void unregister() {
		mContext.unregisterReceiver(this);
	}
}

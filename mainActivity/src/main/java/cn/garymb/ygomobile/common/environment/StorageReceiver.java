package cn.garymb.ygomobile.common.environment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * @author mabin
 * 
 */
public class StorageReceiver extends BroadcastReceiver {

	/**
	 * @author mabin
	 * 
	 */
	public interface OnStorageListener {
		void onMounted();

		void onUnmounted();
	}

	static final String TAG = "StorageReceiver";
	static final boolean DEBUG = false;

	private OnStorageListener mListener;
	private Context mContext;

	public StorageReceiver(Context context) {
		mContext = context;
	}

	/**
	 * 注册
	 */
	public void register(OnStorageListener listener) {
		mListener = listener;
		final IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		filter.addDataScheme("file");
		mContext.registerReceiver(this, filter);

		if (DEBUG)
			Log.d(TAG, "StorageReceiver registered.");
	}

	/**
	 *	取消注册 
	 */
	public void unregister() {
		mContext.unregisterReceiver(this);
		if (DEBUG) {
			Log.d(TAG, "StorageReceiver unregistered.");
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (DEBUG)
			Log.d(TAG, action);
		if (mListener == null) {
			return;
		}
		if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
			mListener.onMounted();
		} else {
			mListener.onUnmounted();
		}
	}
}

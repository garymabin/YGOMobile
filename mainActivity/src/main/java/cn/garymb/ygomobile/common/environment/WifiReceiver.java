package cn.garymb.ygomobile.common.environment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * @author mabin
 * 
 */
public class WifiReceiver extends BroadcastReceiver {
	private static final String TAG = "WifiReceiver";

	private OnWifiStateChangeListener mListener;
	private Context mContext;

	/**
	 * @author mabin
	 * 
	 */
	public interface OnWifiStateChangeListener {
		void onWifiConnected();

		void onWifiDisconnected();
	}

	/**
	 * 
	 */
	public WifiReceiver(Context context) {
		mContext = context;
	}

	public void register(OnWifiStateChangeListener listener) {
		mListener = listener;
		final IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		mContext.registerReceiver(this, filter);
	}

	public void unregister() {
		mContext.unregisterReceiver(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.i(TAG, "onReceive: action = " + action);
		if (mListener == null) {
			return;
		}
		if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
			NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if (null != info) {
				if (info.isConnected()) {
					mListener.onWifiConnected();
					return;
				}
			}
			mListener.onWifiDisconnected();
		} else if (WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION
				.equals(action)) {
			boolean isConnected = intent.getBooleanExtra(
					WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
			if (isConnected) {
				mListener.onWifiConnected();
			} else {
				mListener.onWifiDisconnected();
			}
		}
	}

}

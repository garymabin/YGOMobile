package cn.garymb.ygomobile.net;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

/**
 * @author mabin
 * 
 */
public class NetworkStatusManager {
	private static final String TAG = "NetworkStatusManager";

	public static final int NETWORK_TYPE_WIFI = ConnectivityManager.TYPE_WIFI;
	public static final int NETWORK_TYPE_MOBILE = ConnectivityManager.TYPE_MOBILE;
	//FIXME: -1 might be used to some signigicant value for Android in later future.
	public static final int NETWORK_TYPE_UNKNOWN = -1;
	
	public static final int NETWORK_OK = 1;
	public static final int NETWORK_NO_CONNECTION = 2;
	public static final int NETWORK_BLOCKED = 3;
	public static final int NETWORK_CANNOT_USE_ROAMING = 4;
	public static final int NETWORK_TYPE_DISALLOWED_BY_REQUESTOR = 5;
	public static final int NETWORK_UNUSABLE_DUE_TO_SIZE = 6;
	public static final int NETWORK_RECOMMENDED_UNUSABLE_DUE_TO_SIZE = 7;

	private ConnectivityManager mCM;
	private WifiManager mWM;
	private static NetworkStatusManager INSTANCE;

	private NetworkStatusManager(Context context) {
		mCM = (ConnectivityManager) context.getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		mWM = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}

	synchronized public static NetworkStatusManager peekInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new NetworkStatusManager(context);
		}
		return INSTANCE;
	}

	/**
	 * 
	 * @author: mabin
	 * @param
	 * @param
	 * @return
	 **/
	public boolean isWifiConnected() {
		boolean isWifiEnabled = mWM.isWifiEnabled();
		NetworkInfo ni = mCM.getActiveNetworkInfo();
		if (isWifiEnabled && null != ni && ni.isConnected()
				&& ni.getType() == ConnectivityManager.TYPE_WIFI) {
			Log.i(TAG, "Wi-fi connected now");
			Log.i(TAG, ni.toString());
			return true;
		}
		return false;
	}
	
	public int getActiveNetworkType() {
		NetworkInfo ni = mCM.getActiveNetworkInfo();
		if (ni != null && ni.isConnected()) {
			if (ni.getType() == ConnectivityManager.TYPE_MOBILE) {
				return NETWORK_TYPE_MOBILE;
			} else if(ni.getType() == ConnectivityManager.TYPE_WIFI) {
				return NETWORK_TYPE_WIFI;
			} else {
				return NETWORK_TYPE_UNKNOWN;
			}
		} else {
			return NETWORK_TYPE_UNKNOWN;
		}
	}

	public NetworkInfo getActiveNetworkInfo() {
		return mCM.getActiveNetworkInfo();
	}

	/**
	 * Returns whether this download is allowed to use the network.
	 * 
	 * @return one of the NETWORK_* constants
	 */
	@SuppressLint("NewApi")
	public int checkCanUseNetwork() {
		final NetworkInfo info = mCM.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			return NETWORK_NO_CONNECTION;
		}
		if (Build.VERSION.SDK_INT >= 14) {
			if (DetailedState.BLOCKED.equals(info.getDetailedState())) {
				return NETWORK_BLOCKED;
			}
		}
		// FIXME: igore roaming stuff!
		/*
		 * if (!isRoamingAllowed() && mSystemFacade.isNetworkRoaming()) { return
		 * NETWORK_CANNOT_USE_ROAMING; }
		 */
		// FIXME: ignore metered stuff!
		/*
		 * if (!mAllowMetered && mSystemFacade.isActiveNetworkMetered()) {
		 * return NETWORK_TYPE_DISALLOWED_BY_REQUESTOR; }
		 */
		return NETWORK_OK;
	}

	/**
	 * @return a non-localized string appropriate for logging corresponding to
	 *         one of the NETWORK_* constants.
	 */
	public String getLogMessageForNetworkError(int networkError) {
		Log.d(TAG, "getLogMessageForNetworkError() : networkError = "
				+ networkError);
		switch (networkError) {
		case NETWORK_RECOMMENDED_UNUSABLE_DUE_TO_SIZE:
			return "download size exceeds recommended limit for mobile network";

		case NETWORK_UNUSABLE_DUE_TO_SIZE:
			return "download size exceeds limit for mobile network";

		case NETWORK_NO_CONNECTION:
			return "no network connection available";

		case NETWORK_CANNOT_USE_ROAMING:
			return "download cannot use the current network connection because it is roaming";

		case NETWORK_TYPE_DISALLOWED_BY_REQUESTOR:
			return "download was requested to not use the current network type";

		case NETWORK_BLOCKED:
			return "network is blocked for requesting application";

		default:
			return "unknown error with network connectivity";
		}
	}

}

package cn.garymb.ygomobile.common.environment;



import cn.garymb.ygomobile.net.NetworkStatusManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.net.ConnectivityManagerCompat;
import android.util.Log;

/**
 * @author mabin
 *
 */
public class NetworkReceiver extends BroadcastReceiver {
	/**
	 * @author mabin
	 * 
	 */
	public interface OnNetworkStateChangeListener {
		void onNetworkStatusChanged(int type, boolean isConnected);
	}
	
	
	private static final String TAG = "NetworkReceiver";

	private OnNetworkStateChangeListener mListener;
	private Context mContext;
	private ConnectivityManager mCM;
	private int mNetType = NetworkStatusManager.NETWORK_TYPE_UNKNOWN;
	
	/**
	 * 
	 */
	public NetworkReceiver(Context context) {
		mContext = context;
		mCM = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	public void register(OnNetworkStateChangeListener listener) {
		mListener = listener;
		final IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mContext.registerReceiver(this, filter);
	}
	
	public void unregister() {
		mContext.unregisterReceiver(this);
	}
	

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive: action = " + intent.getAction());
		NetworkInfo info = ConnectivityManagerCompat.getNetworkInfoFromBroadcast(mCM, intent);
		setAvaiableNetworkType(info);
		Log.i(TAG, "onReceive: type = " + mNetType);
		if (info != null && info.isConnected()) {
			mListener.onNetworkStatusChanged(mNetType, true);
		} else {
			if (info != null) {
				mListener.onNetworkStatusChanged(mNetType, false);
			} else {
				mListener.onNetworkStatusChanged(mNetType, false);
			}
		}
	}

	/**
	 * @author: mabin
	 * @return
	**/
	private int setAvaiableNetworkType(NetworkInfo info) {
		int newType;
		if (info != null) {
			switch (info.getType()) {
			case ConnectivityManager.TYPE_WIFI:
				newType = NetworkStatusManager.NETWORK_TYPE_WIFI;
				break;
			case ConnectivityManager.TYPE_MOBILE:
				newType = NetworkStatusManager.NETWORK_TYPE_MOBILE;
				break;
			default:
				newType = NetworkStatusManager.NETWORK_TYPE_UNKNOWN;
				break;
			}
			mNetType = newType;
		}
		return mNetType;
	}

}

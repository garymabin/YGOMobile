package cn.garymb.ygomobile.common.environment;

import java.util.Observer;


import cn.garymb.ygomobile.common.environment.NetworkReceiver.OnNetworkStateChangeListener;
import cn.garymb.ygomobile.common.environment.StorageReceiver.OnStorageListener;
import cn.garymb.ygomobile.common.environment.WifiReceiver.OnWifiStateChangeListener;
import cn.garymb.ygomobile.net.NetworkStatusManager;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
 * @author mabin
 * 
 */
public class EnvironmentObservable implements OnStorageListener,
		OnWifiStateChangeListener, OnNetworkStateChangeListener {
	private static final String TAG = "EnvironmentObservable";

	private Context mContext;
	private StorageReceiver mStorageReceiver;
	private StorageObservable mStorageObservable;
	
	private WifiObservable mWifiObservable;
	private WifiReceiver mWifiReceiver; 
	
	private NetworkObservable mNetworkObservable;
	private NetworkReceiver mNetworkReceiver;
	
	private boolean isWifiConected = true;
	private boolean isStorageAvailable = true;
	private boolean isNetworkConected = true;
	private int netType = NetworkStatusManager.NETWORK_TYPE_UNKNOWN;

	/**
	 * 
	 */
	public EnvironmentObservable(Context context) {
		mContext = context;
		mStorageReceiver = new StorageReceiver(mContext);
		mStorageObservable = new StorageObservable();
		mWifiObservable = new WifiObservable();
		mWifiReceiver = new WifiReceiver(mContext);
		mNetworkReceiver = new NetworkReceiver(mContext);
		mNetworkObservable = new NetworkObservable();
	}

	public void addStorageObserver(Observer o) {
		Log.i(TAG, "addStorageObserver: E");
		synchronized (mStorageObservable) {
			if (mStorageObservable.countObservers() == 0) {
				mStorageReceiver.register(this);
			}
			mStorageObservable.addObserver(o);
		}
	}

	public void removeStorageObserver(Observer o) {
		Log.i(TAG, "removeStorageObserver: E");
		synchronized (mStorageObservable) {
			if (mStorageObservable.countObservers() == 1) {
				mStorageReceiver.unregister();
			}
			mStorageObservable.deleteObserver(o);
		}
	}
	
	public void addNetworkObserver(Observer o) {
		Log.i(TAG, "addWifiObserver: E");
		synchronized (mWifiObservable) {
			if (mNetworkObservable.countObservers() == 0) {
				mNetworkReceiver.register(this);
			}
			mNetworkObservable.addObserver(o);
		}
	}
	
	public void removeNetworkObserver(Observer o) {
		Log.i(TAG, "removeNetworkObserver: E");
		synchronized (mNetworkObservable) {
			if (mNetworkObservable.countObservers() == 1) {
				mNetworkReceiver.unregister();
			}
			mNetworkObservable.deleteObserver(o);
		}
	}
	
	

	public void addWifiObserver(Observer o) {
		Log.i(TAG, "addWifiObserver: E");
		synchronized (mWifiObservable) {
			if (mWifiObservable.countObservers() == 0) {
				mWifiReceiver.register(this);
			}
			mWifiObservable.addObserver(o);
		}
	}

	public void removeWifiObserver(Observer o) {
		Log.i(TAG, "removeWifiObserver: E");
		synchronized (mWifiObservable) {
			if (mWifiObservable.countObservers() == 1) {
				mWifiReceiver.unregister();
			}
			mWifiObservable.deleteObserver(o);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cn.mabin.lanfileshare.logic.StorageReceiver.OnStorageListener#onMounted()
	 */
	@Override
	public void onMounted() {
		Log.i(TAG, "onMounted: isStorageAvailable = " + isStorageAvailable);
		if (!isStorageAvailable) {
			isStorageAvailable = true;
			mStorageObservable.fireFastNotify(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cn.mabin.lanfileshare.logic.StorageReceiver.OnStorageListener#onUnmounted
	 * ()
	 */
	@Override
	public void onUnmounted() {
		Log.i(TAG, "onUnmounted: isStorageAvailable = " + isStorageAvailable);
		if (isStorageAvailable) {
			isStorageAvailable = false;
			mStorageObservable.fireFastNotify(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.mabin.lanfileshare.logic.WifiReceiver.OnWifiStateChangeListener#
	 * onWifiConnected()
	 */
	@Override
	public void onWifiConnected() {
		Log.i(TAG, "onWifiConnected: isWifiConected = " + isWifiConected);
		if (!isWifiConected) {
			isWifiConected = true;
			mWifiObservable.fireFastNotify(true);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.mabin.lanfileshare.logic.WifiReceiver.OnWifiStateChangeListener#
	 * onWifiDisconnected()
	 */
	@Override
	public void onWifiDisconnected() {
		Log.i(TAG, "onWifiDisconnected: isWifiConected = " + isWifiConected);
		if (isWifiConected) {
			isWifiConected = false;
			mWifiObservable.fireFastNotify(false);
		}
	}

	/* (non-Javadoc)
	 * @see com.uc.news.reader.common.environment.NetworkReceiver.OnNetworkStateChangeListener#onNetworkStatusChanged(int, boolean)
	 */
	@Override
	public void onNetworkStatusChanged(int type, boolean isConnected) {
		Log.i(TAG, "onNetworkStatusChanged: type = " + type + " isConnected = " + isConnected);
		if (netType != type || isConnected != isNetworkConected) {
			Bundle data = new Bundle();
			data.putInt(NetworkObservable.DATA_KEY_NETWORK_TYPE, type);
			data.putBoolean(NetworkObservable.DATA_KEY_NETWORK_STATUS, isConnected);
			mNetworkObservable.fireFastNotify(data);
		}
	}

}

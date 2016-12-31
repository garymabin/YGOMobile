package cn.garymb.ygomobile.common.environment;

import java.util.Observer;

import cn.garymb.ygomobile.utils.FastObservable;



/**
 * @author mabin
 *
 */
public class NetworkObservable extends FastObservable {
	
	public static final String DATA_KEY_NETWORK_TYPE = "wifiobservable.network.type";
	public static final String DATA_KEY_NETWORK_STATUS = "wifiobservable.network.status";
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observable#addObserver(java.util.Observer)
	 */
	@Override
	public void addObserver(Observer observer) {
		super.addObserver(observer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observable#deleteObserver(java.util.Observer)
	 */
	@Override
	public void deleteObserver(Observer observer) {
		super.deleteObserver(observer);
	}
}

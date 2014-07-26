package cn.garymb.ygomobile.common.environment;

import java.util.Observer;

import cn.garymb.ygomobile.utils.FastObservable;



/**
 * @author mabin
 * 
 */
public class WifiObservable extends FastObservable {
	
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
	public synchronized void deleteObserver(Observer observer) {
		super.deleteObserver(observer);
	}
}

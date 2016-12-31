package cn.garymb.ygomobile.utils;

import java.util.Observable;

/**
 * @author mabin
 * 
 */
public class FastObservable extends Observable {
	public void doClearChanged() {
		clearChanged();
	}

	public void doSetChanged() {
		setChanged();
	}

	public void fireFastNotify() {

		fireFastNotify(null);

	}

	public void fireFastNotify(Object data) {
		setChanged();
		notifyObservers(data);
	}
}

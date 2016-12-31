package cn.garymb.ygomobile.common.environment;

/**
 * @author mabin
 * 
 */
public interface NetworkChangeListener {
	void onNetworkStatusChanged(int type, boolean isConnected);
}

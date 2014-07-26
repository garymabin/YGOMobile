package cn.garymb.ygomobile.net;

import org.apache.http.HttpHost;

import android.content.Context;
import android.os.Build;

/**
 * @author mabin
 * 
 */
public class ProxyHelper {
	private static final boolean IS_ICS_OR_NOT = Build.VERSION.SDK_INT >= 15;

	@SuppressWarnings("deprecation")
	public static HttpHost getHttpProxy(Context context) {
		String proxyAddress = null;
		int proxyPort;
		if (IS_ICS_OR_NOT) {
			proxyAddress = System.getProperty("http.proxyHost");
			String portStr = System.getProperty("http.proxyPort");
			proxyPort = Integer.parseInt((portStr != null ? portStr : "-1"));
		} else {
			proxyAddress = android.net.Proxy.getHost(context);
			proxyPort = android.net.Proxy.getPort(context);
		}
		if (proxyAddress == null || proxyPort == -1) {
			return null;
		}
		return new HttpHost(proxyAddress, proxyPort);
	}
}

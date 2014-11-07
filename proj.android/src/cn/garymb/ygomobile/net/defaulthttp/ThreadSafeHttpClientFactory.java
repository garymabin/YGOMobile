package cn.garymb.ygomobile.net.defaulthttp;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import cn.garymb.ygomobile.net.ProxyHelper;

import android.content.Context;
import android.net.SSLCertificateSocketFactory;
import android.net.SSLSessionCache;

/**
 * @author mabin
 * 
 */
public class ThreadSafeHttpClientFactory {
	private static final int CONNECTION_TIMEOUT = 60 * 1000;
	private static final int SOCKET_TIMEOUT = 2 * 60 * 1000;

	private Context mContext;

	/**
	 * 
	 */
	public ThreadSafeHttpClientFactory(Context context) {
		mContext = context;
	}

	public HttpClient getHttpClient() {
		return createHttpClient();
	}

	/**
	 * 
	 * @author: mabin
	 * @param
	 * @param
	 * @return
	 **/
	private HttpClient createHttpClient() {
		AbstractHttpClient httpClient = new DefaultHttpClient() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.apache.http.impl.client.DefaultHttpClient#
			 * createClientConnectionManager()
			 */
			@Override
			protected ClientConnectionManager createClientConnectionManager() {
				SchemeRegistry registry = new SchemeRegistry();
				registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
				registry.register(new Scheme("https", getHttpsSocketFactory(),443));
				HttpParams params = getParams();
				HttpConnectionParams.setConnectionTimeout(params,CONNECTION_TIMEOUT);
				HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);
				HttpProtocolParams.setUserAgent(params, getUserAgent());
				ConnRouteParams.setDefaultProxy(params,ProxyHelper.getHttpProxy(mContext));
				return new ThreadSafeClientConnManager(params, registry);
			}
		};
		return httpClient;
	}

	private SocketFactory getHttpsSocketFactory() {
		SSLSessionCache cache = new SSLSessionCache(mContext);
		return SSLCertificateSocketFactory.getHttpSocketFactory(CONNECTION_TIMEOUT, cache);
	}

	private String getUserAgent() {
		String ua = System.getProperty("http.agent");
		return ua;
	}
}

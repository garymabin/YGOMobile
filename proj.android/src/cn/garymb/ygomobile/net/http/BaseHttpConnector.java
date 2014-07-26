package cn.garymb.ygomobile.net.http;

import java.io.InputStream;

import org.apache.http.client.HttpClient;

import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;
import cn.garymb.ygomobile.net.IBaseConnector;
import cn.garymb.ygomobile.utils.HttpUtils;

import android.util.Log;

/**
 * @author mabin
 * 
 */
public abstract class BaseHttpConnector implements IBaseConnector{
	private static final String TAG = "BaseHttpConnector";
	protected static final String RESULT_OK = "true";

	protected HttpClient mClient;

	/**
	 * 
	 */
	public BaseHttpConnector(HttpClient client) {
		mClient = client;
	}

	public void get(BaseDataWrapper wrapper) throws InterruptedException {
		Log.d(TAG, "start to connect, url = " + wrapper.getUrl(0));
		InputStream is = HttpUtils.doGet(mClient, wrapper.getUrl(0));
		if (null != is) {
			handleResponse(is, wrapper);
		}
	}

	/**
	 * 
	 * @author: mabin
	 * @throws InterruptedException 
	 **/
	protected abstract void handleResponse(InputStream data,
			BaseDataWrapper wrapper) throws InterruptedException;

	public synchronized void post(String url) {

	}
}

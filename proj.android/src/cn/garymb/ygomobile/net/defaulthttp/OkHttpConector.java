package cn.garymb.ygomobile.net.defaulthttp;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import cn.garymb.ygomobile.data.wrapper.BaseRequestWrapper;
import cn.garymb.ygomobile.data.wrapper.IBaseWrapper;
import cn.garymb.ygomobile.net.IBaseConnector;
import cn.garymb.ygomobile.utils.HttpUtils;

public class OkHttpConector implements IBaseConnector {

	private static final String TAG = "OkHttpConector";
	private OkHttpClient mClient;

	public OkHttpConector(OkHttpClient client) {
		mClient = client;
	}

	@Override
	public void get(BaseRequestWrapper wrapper) throws InterruptedException {
		do {
			Log.d(TAG, "start to connect, url = " + wrapper.getUrl(0) + " retryCount = " + wrapper.getRetryCount());
			InputStream is = HttpUtils.doOkGet(mClient, wrapper.getUrl(0));
			if (null != is) {
				int result = wrapper.parse(is);
				try {
					is.close();
				} catch (IOException e) {
				}
				if (result == IBaseWrapper.TASK_STATUS_CANCELED || 
						result == IBaseWrapper.TASK_STATUS_SUCCESS) {
					break;
				} else {
					continue;
				}
			}
		} while (wrapper.increaseRetryCount() <= BaseRequestWrapper.MAX_RETRY_COUNT);
	}
}

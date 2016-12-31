package cn.garymb.ygomobile.net.defaulthttp;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import cn.garymb.ygomobile.data.wrapper.BaseRequestJob;

public class DownloadOkHttpConnector extends OkHttpConector {
	
	public DownloadOkHttpConnector(OkHttpClient client) {
		super(client);
	}

	public void get(BaseRequestJob wrapper) throws InterruptedException {
		Response resp = null;
		try {
			Request req = new Request.Builder()
					.url(wrapper.getUrl(0))
					.addHeader("Accept",
							"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
					.addHeader("Accept-Encoding", "gzip,deflate,sdch")
					.addHeader("Accept-Language",
							"zh-CN,zh;q=0.8,en;q=0.6,zh-TW;q=0.4")
					.addHeader("Content-Type",
							"application/x-www-form-urlencoded").build();
			resp = mClient.newCall(req).execute();
			int statusCode = resp.code();
			if (statusCode < 200 || statusCode >= 300) {
				Log.d("HttpUtils", "status code = " + statusCode);
				return;
			}
			if (null != resp) {
				wrapper.parse(resp);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}		
	};

}

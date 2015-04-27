package cn.garymb.ygomobile.core;

import cn.garymb.ygomobile.data.wrapper.IBaseJob.JobStatusCallback;
import cn.garymb.ygomobile.net.IBaseConnector;
import cn.garymb.ygomobile.net.defaulthttp.DownloadOkHttpConnector;

import com.squareup.okhttp.OkHttpClient;

public class SimpleDownloadThread extends DefaultWorkThread<OkHttpClient> {

	public SimpleDownloadThread(JobStatusCallback callback, OkHttpClient client) {
		super(callback, client);
	}

	@Override
	protected IBaseConnector initConnector(OkHttpClient client) {
		return new DownloadOkHttpConnector(client);
	}

}

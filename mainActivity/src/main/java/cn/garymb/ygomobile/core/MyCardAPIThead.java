package cn.garymb.ygomobile.core;

import okhttp3.OkHttpClient;

import cn.garymb.ygomobile.data.wrapper.IBaseJob.JobStatusCallback;
import cn.garymb.ygomobile.net.IBaseConnector;
import cn.garymb.ygomobile.net.defaulthttp.OkHttpConector;

public class MyCardAPIThead extends DefaultWorkThread<OkHttpClient> {

	public MyCardAPIThead(JobStatusCallback callback, OkHttpClient client) {
		super(callback, client);
	}

	@Override
	protected IBaseConnector initConnector(OkHttpClient client) {
		return new OkHttpConector(client);
	}
}

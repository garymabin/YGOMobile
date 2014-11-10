package cn.garymb.ygomobile.core;

import com.squareup.okhttp.OkHttpClient;

import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.net.IBaseConnector;
import cn.garymb.ygomobile.net.defaulthttp.OkHttpConector;

public class MyCardAPIThead extends DefaultWorkThread<OkHttpClient> {
	
	public MyCardAPIThead(TaskStatusCallback callback, OkHttpClient client) {
		super(callback, client);
	}

	@Override
	protected IBaseConnector initConnector(OkHttpClient client) {
		return new OkHttpConector(client);
	}
}

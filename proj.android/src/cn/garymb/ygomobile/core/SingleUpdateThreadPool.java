package cn.garymb.ygomobile.core;

import java.util.concurrent.BlockingQueue;

import org.apache.http.client.HttpClient;

import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;
import cn.garymb.ygomobile.net.http.DataHttpConnector;

public class SingleUpdateThreadPool extends SingleHttpThreadPool {

	public SingleUpdateThreadPool(BlockingQueue<BaseDataWrapper> queue, TaskStatusCallback callback, HttpClient client) {
		super(queue, callback);
		// TODO Auto-generated constructor stub
		mConnector = new DataHttpConnector(client);
	}
}

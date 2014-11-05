package cn.garymb.ygomobile.net.http;


import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.BlockingDeque;

import org.acra.util.HttpRequest;
import org.apache.http.impl.nio.client.CloseableHttpPipeliningClient;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.nio.protocol.BasicAsyncRequestProducer;

import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;
import cn.garymb.ygomobile.data.wrapper.PipeliningImageWrapper;
import cn.garymb.ygomobile.net.IBaseConnector;

public class PipeliningHttpConnector implements IBaseConnector {
	
	private BlockingDeque<BaseDataWrapper> mInternalQueue;
	private CloseableHttpPipeliningClient mClient;
	
	private WeakReference<List<? extends BasicAsyncRequestProducer>> mProducerRef;
	
	private WeakReference<List<? extends AsyncCharConsumer<Boolean>>> mConsumerRef;
	
	public PipeliningHttpConnector(CloseableHttpPipeliningClient client,
			List<? extends BasicAsyncRequestProducer> producer, List<? extends AsyncCharConsumer<Boolean>> consumer) {
		mClient = client;
		mProducerRef = new WeakReference<List<? extends BasicAsyncRequestProducer>>(producer);
		mConsumerRef = new WeakReference<List<? extends AsyncCharConsumer<Boolean>>>(consumer);
	}

	@Override
	public void get(BaseDataWrapper wrapper) throws InterruptedException {
 		if (wrapper instanceof PipeliningImageWrapper) {
 			int size = wrapper.size();
 			for (int i = 1; i < size; i++) {
 				mProducerRef.get().add(new CustomAsyncRequestProducer(targetHost, request));
 				mConsumerRef.get().add(new CustomAsyncConsumer(request));
 			}
 		}
	}


}

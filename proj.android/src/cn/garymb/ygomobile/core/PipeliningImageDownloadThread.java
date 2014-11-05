package cn.garymb.ygomobile.core;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.nio.client.CloseableHttpPipeliningClient;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.nio.protocol.BasicAsyncRequestProducer;
import org.apache.http.protocol.HttpContext;

import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;
import cn.garymb.ygomobile.data.wrapper.PipeliningImageWrapper;
import cn.garymb.ygomobile.net.IBaseConnector;
import cn.garymb.ygomobile.net.http.PipeliningHttpConnector;

public class PipeliningImageDownloadThread extends BaseThread {


	public static class CustomAsyncConsumer extends AsyncCharConsumer<Boolean> {

		@Override
		protected void onCharReceived(CharBuffer arg0, IOControl arg1)
				throws IOException {
			
		}

		@Override
		protected Boolean buildResult(HttpContext arg0) throws Exception {
			return null;
		}

		@Override
		protected void onResponseReceived(HttpResponse arg0)
				throws HttpException, IOException {
		}

	}

	public static class CustomAsyncRequestProducer extends
			BasicAsyncRequestProducer {

		public CustomAsyncRequestProducer(HttpHost arg0, HttpRequest arg1) {
			super(arg0, arg1);
		}

	}

	protected volatile boolean isRunning = true;
	private IBaseConnector mConnector;
	private BlockingQueue<BaseDataWrapper> mQueue;
	
	private List<? extends BasicAsyncRequestProducer> mProducer;
	
	private List<? extends AsyncCharConsumer<Boolean>> mConsumer;

	private static final int MAX_PIPELINE_COUNT = 100;

	public PipeliningImageDownloadThread(BlockingQueue<BaseDataWrapper> queue,
			TaskStatusCallback callback, CloseableHttpPipeliningClient client) {
		super(callback);
		mProducer = new ArrayList<CustomAsyncRequestProducer>(MAX_PIPELINE_COUNT);
		mConsumer = new ArrayList<CustomAsyncConsumer>(MAX_PIPELINE_COUNT);
		mConnector = new PipeliningHttpConnector(client, mProducer, mConsumer);
		mQueue = queue;
	}

	@Override
	public void run() {
		BaseDataWrapper wrapper = null;
		while (isRunning && !isInterrupted()) {
			try {
				List<BaseDataWrapper> wrappers = new ArrayList<BaseDataWrapper>(
						MAX_PIPELINE_COUNT);
				wrapper = mQueue.take();
				wrappers.add(wrapper);
				for (int i = 0; i < MAX_PIPELINE_COUNT - 1; i++) {
					wrapper = mQueue.poll();
					if (wrapper == null) {
						break;
					}
					wrappers.add(wrapper);
				}
				if (isInterrupted()) {
					throw new InterruptedException();
				}
				PipeliningImageWrapper pipeWrapper = new PipeliningImageWrapper(
						IBaseConnection.CONNECTION_TYPE_IMAGE_DOWNLOAD,
						wrappers);
				mConnector.get(pipeWrapper);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void terminate() {
		if (isRunning) {
			interrupt();
			isRunning = false;
		}
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

}

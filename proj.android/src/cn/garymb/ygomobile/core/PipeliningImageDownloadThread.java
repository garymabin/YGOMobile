package cn.garymb.ygomobile.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.http.HC4.HttpRequest;
import org.apache.http.HC4.HttpHost;
import org.apache.http.HC4.HttpResponse;
import org.apache.http.HC4.entity.ContentType;
import org.apache.http.HC4.impl.nio.client.CloseableHttpPipeliningClient;
import org.apache.http.HC4.nio.client.methods.ZeroCopyConsumer;
import org.apache.http.HC4.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.HC4.nio.protocol.BasicAsyncRequestProducer;

import android.util.Log;

import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.data.wrapper.BaseRequestWrapper;
import cn.garymb.ygomobile.data.wrapper.IBaseWrapper;
import cn.garymb.ygomobile.data.wrapper.ImageDownloadWrapper;
import cn.garymb.ygomobile.data.wrapper.PipeliningImageWrapper;
import cn.garymb.ygomobile.model.data.ImageItemInfoHelper;
import cn.garymb.ygomobile.net.IBaseConnector;
import cn.garymb.ygomobile.net.hc4thttp.PipeliningHttpConnector;

public class PipeliningImageDownloadThread extends BaseThread {

	public static final String TAG = "PipeliningImageDownloadThread";

	public static class ImageDownloadConsumer extends ZeroCopyConsumer<BaseRequestWrapper> {

		private BaseRequestWrapper mWrapper;
		private TaskStatusCallback mCallback;
		
		public ImageDownloadConsumer(File file, BaseRequestWrapper wrapper)
				throws FileNotFoundException {
			super(file);
			mWrapper = wrapper;
		}
		
		public void setTaskstatusCallback(TaskStatusCallback callback) {
			mCallback = callback;
		}

		@Override
		protected BaseRequestWrapper process(HttpResponse arg0, File arg1, ContentType arg2)
				throws Exception {
			File targetFile = null;
			mWrapper.setResult(IBaseWrapper.TASK_STATUS_FAILED);
			if (mWrapper instanceof ImageDownloadWrapper) {
				targetFile = new File(
						ImageItemInfoHelper
								.getImagePath(((ImageDownloadWrapper) mWrapper)
										.getImageItem()));
				if (arg1 != null) {
					arg1.renameTo(targetFile);
					arg1.delete();
				}
				Exception e = getException();
				if (e == null) {
					mWrapper.setResult(IBaseWrapper.TASK_STATUS_SUCCESS);
				} else {
					mWrapper.increaseRetryCount();
				}
			}
			if (mCallback != null) {
				mCallback.onTaskFinish(mWrapper);
			}
			mCallback = null;
			return mWrapper;
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
	private BlockingQueue<BaseRequestWrapper> mQueue;

	private List<BasicAsyncRequestProducer> mProducer;

	private List<AbstractAsyncResponseConsumer<BaseRequestWrapper>> mConsumer;

	private static final int MAX_PIPELINE_COUNT = 100;
	
	public PipeliningImageDownloadThread(BlockingQueue<BaseRequestWrapper> queue,
			TaskStatusCallback callback, CloseableHttpPipeliningClient client) {
		super(callback);
		mProducer = new ArrayList<BasicAsyncRequestProducer>(MAX_PIPELINE_COUNT);
		mConsumer = new ArrayList<AbstractAsyncResponseConsumer<BaseRequestWrapper>>(
				MAX_PIPELINE_COUNT);
		mConnector = new PipeliningHttpConnector(client, mProducer, mConsumer);
		mQueue = queue;
		((PipeliningHttpConnector) mConnector).setTaskStatusCallback(callback);
	}

	@Override
	public void run() {
		BaseRequestWrapper wrapper = null;
		while (isRunning && !isInterrupted()) {
			try {
				List<BaseRequestWrapper> wrappers = new ArrayList<BaseRequestWrapper>(
						MAX_PIPELINE_COUNT);
				wrapper = mQueue.take();
				if (wrapper != null) {
					wrappers.add(wrapper);
					Log.i(TAG, "add new task into list, index = 0 tid = " + Thread.currentThread().getId());
				}
				for (int i = 1; i < MAX_PIPELINE_COUNT; i++) {
					wrapper = mQueue.poll();
					if (wrapper == null) {
						break;
					}
					Log.i(TAG, "add new task into list, index = "+i+" tid = " + Thread.currentThread().getId());
					wrappers.add(wrapper);
				}
				if (isInterrupted()) {
					throw new InterruptedException();
				}
				PipeliningImageWrapper pipeWrapper = new PipeliningImageWrapper(
						IBaseConnection.CONNECTION_TYPE_IMAGE_DOWNLOAD,
						wrappers);
				mConnector.get(pipeWrapper);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void terminate() {
		if (isRunning) {
			interrupt();
			isRunning = false;
			((PipeliningHttpConnector) mConnector).cancel();
			((PipeliningHttpConnector) mConnector).setTaskStatusCallback(null);
		}
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

}

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

import cn.garymb.ygomobile.data.wrapper.BaseRequestJob;
import cn.garymb.ygomobile.data.wrapper.IBaseJob;
import cn.garymb.ygomobile.data.wrapper.IBaseJob.JobStatusCallback;
import cn.garymb.ygomobile.data.wrapper.ImageDownloadJob;
import cn.garymb.ygomobile.data.wrapper.PipeliningImageWrapper;
import cn.garymb.ygomobile.model.data.ImageItemInfoHelper;
import cn.garymb.ygomobile.net.IBaseConnector;
import cn.garymb.ygomobile.net.hc4thttp.PipeliningHttpConnector;

public class PipeliningImageDownloadThread extends BaseThread {

	public static final String TAG = "PipeliningImageDownloadThread";

	public static class ImageDownloadConsumer extends ZeroCopyConsumer<BaseRequestJob> {

		private BaseRequestJob mWrapper;
		private JobStatusCallback mCallback;
		
		public ImageDownloadConsumer(File file, BaseRequestJob wrapper)
				throws FileNotFoundException {
			super(file);
			mWrapper = wrapper;
		}
		
		public void setJobstatusCallback(JobStatusCallback callback) {
			mCallback = callback;
		}

		@Override
		protected BaseRequestJob process(HttpResponse arg0, File arg1, ContentType arg2)
				throws Exception {
			File targetFile = null;
			mWrapper.setResult(IBaseJob.STATUS_FAILED);
			if (mWrapper instanceof ImageDownloadJob) {
				targetFile = new File(
						ImageItemInfoHelper
								.getDownloadImagePath(((ImageDownloadJob) mWrapper)
										.getImageItem()));
				if (arg1 != null) {
					arg1.renameTo(targetFile);
					arg1.delete();
				}
				Exception e = getException();
				if (e == null) {
					mWrapper.setResult(IBaseJob.STATUS_SUCCESS);
				} else {
					mWrapper.increaseRetryCount();
				}
			}
			if (mCallback != null) {
				mCallback.onJobFinish(mWrapper);
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

	protected volatile boolean isRunning = false;
	private IBaseConnector mConnector;
	private BlockingQueue<BaseRequestJob> mQueue;

	private List<BasicAsyncRequestProducer> mProducer;

	private List<AbstractAsyncResponseConsumer<BaseRequestJob>> mConsumer;

	private static final int MAX_PIPELINE_COUNT = 200;
	
	public PipeliningImageDownloadThread(BlockingQueue<BaseRequestJob> queue,
			JobStatusCallback callback, CloseableHttpPipeliningClient client) {
		super(callback);
		mProducer = new ArrayList<BasicAsyncRequestProducer>(MAX_PIPELINE_COUNT);
		mConsumer = new ArrayList<AbstractAsyncResponseConsumer<BaseRequestJob>>(
				MAX_PIPELINE_COUNT);
		mConnector = new PipeliningHttpConnector(client, mProducer, mConsumer);
		mQueue = queue;
		((PipeliningHttpConnector) mConnector).setJobStatusCallback(callback);
	}
	
	@Override
	public synchronized void start() {
		isRunning = true;
		super.start();
	}

	@Override
	public void run() {
		BaseRequestJob wrapper = null;
		while (isRunning && !isInterrupted()) {
			try {
				List<BaseRequestJob> wrappers = new ArrayList<BaseRequestJob>(
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
						IBaseTask.TASK_TYPE_IMAGE_DOWNLOAD,
						wrappers);
				mConnector.get(pipeWrapper);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void terminate() {
		if (isRunning) {
			((PipeliningHttpConnector) mConnector).cancel();
			((PipeliningHttpConnector) mConnector).setJobStatusCallback(null);
			isRunning = false;
			interrupt();
		}
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

}

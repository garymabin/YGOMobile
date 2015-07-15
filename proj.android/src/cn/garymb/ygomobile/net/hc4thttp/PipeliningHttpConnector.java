package cn.garymb.ygomobile.net.hc4thttp;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.http.HC4.HttpHost;
import org.apache.http.HC4.impl.nio.client.CloseableHttpPipeliningClient;
import org.apache.http.HC4.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.HC4.nio.protocol.BasicAsyncRequestProducer;
import org.apache.http.HC4.client.methods.HttpGet;

import android.util.Log;

import cn.garymb.ygomobile.core.PipeliningImageDownloadThread.CustomAsyncRequestProducer;
import cn.garymb.ygomobile.core.PipeliningImageDownloadThread.ImageDownloadConsumer;
import cn.garymb.ygomobile.data.wrapper.BaseRequestJob;
import cn.garymb.ygomobile.data.wrapper.IBaseJob.JobStatusCallback;
import cn.garymb.ygomobile.data.wrapper.ImageDownloadJob;
import cn.garymb.ygomobile.data.wrapper.PipeliningImageWrapper;
import cn.garymb.ygomobile.model.data.ImageItemInfoHelper;
import cn.garymb.ygomobile.net.IBaseConnector;

public class PipeliningHttpConnector implements IBaseConnector {

	private static final String TAG = "PipeliningHttpConnector";

	private CloseableHttpPipeliningClient mClient = null;

	private WeakReference<List<BasicAsyncRequestProducer>> mProducerRef;

	private WeakReference<List<AbstractAsyncResponseConsumer<BaseRequestJob>>> mConsumerRef;

	private JobStatusCallback mCallback;
	
	private Future<List<BaseRequestJob>> mFuture = null;
	
	private volatile boolean isCanceled = false;

	public PipeliningHttpConnector(CloseableHttpPipeliningClient client, List<BasicAsyncRequestProducer> producer,
			List<AbstractAsyncResponseConsumer<BaseRequestJob>> consumer) {
		mProducerRef = new WeakReference<List<BasicAsyncRequestProducer>>(
				producer);
		mConsumerRef = new WeakReference<List<AbstractAsyncResponseConsumer<BaseRequestJob>>>(
				consumer);
		mClient = client;
		mClient.start();
	}

	public void setJobStatusCallback(JobStatusCallback callback) {
		mCallback = callback;
	}

	@Override
	public void get(BaseRequestJob wrapper) throws InterruptedException {
		if (!isCanceled && wrapper instanceof PipeliningImageWrapper) {
			Log.w(TAG, "beging pipelining tid: " + Thread.currentThread().getId());
			int size = wrapper.size();
			HttpHost host = null;
			try {
				host = new HttpHost(getDomainName(wrapper.getUrl(0)));
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
			mProducerRef.get().clear();
			mConsumerRef.get().clear();
			for (int i = 0; i < size; i++) {
				String url = null;
				try {
					url = getResourcePath(wrapper.getUrl(i));
				} catch (URISyntaxException e) {
					e.printStackTrace();
					continue;
				}
				HttpGet request = new HttpGet(url);
				try {
					mProducerRef.get().add(
							new CustomAsyncRequestProducer(host, request));
					BaseRequestJob imageWrapper = ((PipeliningImageWrapper) wrapper)
							.getInnerWrapper(i);
					File targetFile = new File(
							ImageItemInfoHelper
									.getImageDownloadTempPath(((ImageDownloadJob) imageWrapper)
											.getImageItem()));
					ImageDownloadConsumer consumer = new ImageDownloadConsumer(
							targetFile, imageWrapper);
					consumer.setJobstatusCallback(mCallback);
					mConsumerRef.get().add(consumer);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					Log.w(TAG, "add to list failed: url = " + url);
					continue;
				}
			}
			mFuture = mClient.execute(host,
					mProducerRef.get(), mConsumerRef.get(), null);
			try {
				mFuture.get();
			} catch (Exception e) {
				e.printStackTrace();
			} 
			((PipeliningImageWrapper) wrapper).clear();
			System.out.println("Shutting down");
		}
	}

	public static String getDomainName(String url) throws URISyntaxException {
		URI uri = new URI(url);
		String domain = uri.getHost();
		return domain.startsWith("www.") ? domain.substring(4) : domain;
	}

	public static String getResourcePath(String url) throws URISyntaxException {
		URI uri = new URI(url);
		return uri.getPath();
	}
	
	public void cancel() {
		if (mFuture != null) {
			mFuture.cancel(true);
		}
		mClient = null;
		isCanceled = true;
	}
}

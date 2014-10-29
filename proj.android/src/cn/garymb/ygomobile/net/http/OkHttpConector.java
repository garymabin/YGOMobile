package cn.garymb.ygomobile.net.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;
import cn.garymb.ygomobile.data.wrapper.IBaseWrapper;
import cn.garymb.ygomobile.net.IBaseConnector;
import cn.garymb.ygomobile.utils.HttpUtils;

public class OkHttpConector implements IBaseConnector {
	
	private static final String TAG = "OkHttpConector";
	private OkHttpClient mClient;
	
	
	public OkHttpConector(OkHttpClient client) {
		mClient = client;
	}


	@Override
	public void get(BaseDataWrapper wrapper) throws InterruptedException {
		Log.d(TAG, "start to connect, url = " + wrapper.getUrl(0));
		InputStream is = HttpUtils.doOkGet(mClient, wrapper.getUrl(0));
		if (null != is) {
			wrapper.parse(is);
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * 
	 * @author: mabin
	 * @return
	 **/
	protected void handleResponse(InputStream data, BaseDataWrapper wrapper)
			throws InterruptedException {
		int status = IBaseWrapper.TASK_STATUS_SUCCESS;
		StringBuilder out = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(data));
		int len = -1;
		char[] buffer = new char[Constants.IO_BUFFER_SIZE];
		try {
			while ((!Thread.currentThread().isInterrupted() && (len = reader
					.read(buffer, 0, Constants.IO_BUFFER_SIZE)) != -1))
				out.append(buffer, 0, len);
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			status = IBaseWrapper.TASK_STATUS_FAILED;
			wrapper.setResult(status);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			status = IBaseWrapper.TASK_STATUS_FAILED;
			wrapper.setResult(status);
		} finally {
			buffer = null;
			if (data != null) {
				try {
					data.close();
				} catch (IOException e) {
					//just in case
				}
			}
			out.delete(0, out.length());
			out = null;
			System.gc();
		}
	}

}

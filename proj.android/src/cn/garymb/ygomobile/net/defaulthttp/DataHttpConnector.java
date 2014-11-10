package cn.garymb.ygomobile.net.defaulthttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.client.HttpClient;

import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.data.wrapper.BaseRequestWrapper;
import cn.garymb.ygomobile.data.wrapper.IBaseWrapper;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import cn.garymb.ygomobile.utils.HttpUtils;

import android.util.Log;

/**
 * @author mabin
 * 
 */
public class DataHttpConnector extends BaseHttpConnector implements
		ResourcesConstants {

	private static final String TAG = "UpdateHttpConnector";

	/**
	 * @param client
	 */
	public DataHttpConnector(HttpClient client) {
		super(client);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.uc.addon.indoorsmanwelfare.net.http.BaseHttpConnector#get(com.uc.
	 * addon.indoorsmanwelfare.model.data.wrapper.BaseDataWrapper)
	 */
	@Override
	public void get(BaseRequestWrapper wrapper) {
		// TODO Auto-generated method stub
		int i = 0;
		String url = wrapper.getUrl(0);
		while (null != url) {
			InputStream is = HttpUtils.doGet(mClient, url);
			if (null != is) {
				try {
					handleResponse(is, wrapper);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
			}
			url = wrapper.getUrl(++i);
		}
	}

	/**
	 * 
	 * @author: mabin
	 * @return
	 **/
	@Override
	protected void handleResponse(InputStream data, BaseRequestWrapper wrapper)
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
			Log.d(TAG, out.toString());
//			wrapper.parse(out);
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

package cn.garymb.ygomobile.net.http;

import java.io.InputStream;

import org.apache.http.client.HttpClient;

import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;

/**
 * @author mabin
 * 
 */
public class ImageDownloadHttpConnector extends BaseHttpConnector {

	/**
	 * @param client
	 */
	public ImageDownloadHttpConnector(HttpClient client) {
		super(client);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.uc.addon.indoorsmanwelfare.net.http.BaseHttpConnector#handleResponse
	 * (java.io.InputStream,
	 * com.uc.addon.indoorsmanwelfare.model.data.IBaseWrapper)
	 */
	@Override
	protected void handleResponse(InputStream data, BaseDataWrapper wrapper) {
	}

}

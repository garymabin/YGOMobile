package cn.garymb.ygomobile.data.wrapper;

import java.io.InputStream;
import java.util.ArrayList;

import android.os.Bundle;


/**
 * @author mabin
 * 
 */
public abstract class BaseDataWrapper implements IBaseWrapper {
	
	protected ArrayList<String> mUrls;
	protected int mResult;
	
	protected int mRequestType;
	
	protected Bundle mParam;
	
	/**
	 * 
	 */
	public BaseDataWrapper(int requestType) {
		mUrls = new ArrayList<String>();
		mRequestType = requestType;
	}

	
	public abstract int parse(InputStream in);

	@Override
	public void recyle() {
		// TODO Auto-generated method stub
	}

	public int getResult() {
		return mResult;
	}

	public void setResult(int result) {
		mResult = result;
	}
	
	public Bundle getParam() {
		return mParam;
	}
	
	public void setParam(Bundle param) {
		mParam = param;
	}
	
	/* (non-Javadoc)
	 * @see com.uc.addon.indoorsmanwelfare.model.data.wrapper.IBaseWrapper#getUrl(int)
	 */
	@Override
	public String getUrl(int index) {
		// TODO Auto-generated method stub
		if (index >= mUrls.size()) {
			return null;
		} else {
			return mUrls.get(index);
		}
	}
	
	@Override
	public int getRequestType() {
		return mRequestType;
	}
}
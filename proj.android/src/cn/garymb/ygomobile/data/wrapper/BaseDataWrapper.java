package cn.garymb.ygomobile.data.wrapper;

import java.util.ArrayList;

import android.os.Bundle;


/**
 * @author mabin
 * 
 */
public abstract class BaseDataWrapper implements IBaseWrapper {
	
	public static final int MAX_RETRY_COUNT = 3;
	
	protected ArrayList<String> mUrls;
	protected int mResult;
	
	protected int mRequestType;
	
	protected Bundle mParam;
	
	protected int mRetryCount = 0;
	
	/**
	 * 
	 */
	public BaseDataWrapper(int requestType) {
		mUrls = new ArrayList<String>();
		mRequestType = requestType;
		mRetryCount = 0;
	}

	
	public abstract int parse(Object in);

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
	
	public int size() {
		return mUrls.size();
	}
	
	@Override
	public int getRequestType() {
		return mRequestType;
	}
	
	public boolean isFailed() {
		return mResult == TASK_STATUS_FAILED && mRetryCount > MAX_RETRY_COUNT;
	}
	
	public int getRetryCount() {
		return mRetryCount;
	}
	
	public int increaseRetryCount() {
		return ++mRetryCount;
	}
}
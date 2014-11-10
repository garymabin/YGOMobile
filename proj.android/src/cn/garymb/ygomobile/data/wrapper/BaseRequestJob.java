package cn.garymb.ygomobile.data.wrapper;

import java.util.ArrayList;

import android.os.Message;


/**
 * @author mabin
 * 
 */
public abstract class BaseRequestJob implements IBaseJob {
	
	public static final int MAX_RETRY_COUNT = 3;
	
	protected ArrayList<String> mUrls;
	protected int mResult;
	
	protected Message mParam;
	
	protected int mRetryCount = 0;
	
	/**
	 * 
	 */
	public BaseRequestJob() {
		mUrls = new ArrayList<String>();
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
	
	public Message getParam() {
		return mParam;
	}
	
	public void setParam(Message param) {
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
	
	public boolean isFailed() {
		return mResult == STATUS_FAILED && mRetryCount > MAX_RETRY_COUNT;
	}
	
	public int getRetryCount() {
		return mRetryCount;
	}
	
	public int increaseRetryCount() {
		return ++mRetryCount;
	}
}
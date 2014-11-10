package cn.garymb.ygomobile.data.wrapper;

import java.util.List;

public class PipeliningImageWrapper extends BaseRequestWrapper {
	
	private List<BaseRequestWrapper> mWrappers;

	public PipeliningImageWrapper(int requestType, List<BaseRequestWrapper> wrappers) {
		super(requestType);
		for (BaseRequestWrapper wrapper : wrappers) {
			mUrls.add(wrapper.getUrl(0));
		}
		mWrappers = wrappers;
	}

	@Override
	public int parse(Object in) {
		return TASK_STATUS_FAILED;
	}
	
	public BaseRequestWrapper getInnerWrapper(int index) {
		return mWrappers.get(index);
	}
	
	public void clear() {
		mWrappers.clear();
		mWrappers = null;
	}

}

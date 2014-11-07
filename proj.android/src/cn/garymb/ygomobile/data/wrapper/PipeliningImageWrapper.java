package cn.garymb.ygomobile.data.wrapper;

import java.util.List;

public class PipeliningImageWrapper extends BaseDataWrapper {
	
	private List<BaseDataWrapper> mWrappers;

	public PipeliningImageWrapper(int requestType, List<BaseDataWrapper> wrappers) {
		super(requestType);
		for (BaseDataWrapper wrapper : wrappers) {
			mUrls.add(wrapper.getUrl(0));
		}
		mWrappers = wrappers;
	}

	@Override
	public int parse(Object in) {
		return TASK_STATUS_FAILED;
	}
	
	public BaseDataWrapper getInnerWrapper(int index) {
		return mWrappers.get(index);
	}
	
	public void clear() {
		mWrappers.clear();
		mWrappers = null;
	}

}

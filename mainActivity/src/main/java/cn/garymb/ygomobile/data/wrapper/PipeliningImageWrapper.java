package cn.garymb.ygomobile.data.wrapper;

import java.util.List;

public class PipeliningImageWrapper extends BaseRequestJob {
	
	private List<BaseRequestJob> mWrappers;

	public PipeliningImageWrapper(int requestType, List<BaseRequestJob> wrappers) {
		super();
		for (BaseRequestJob wrapper : wrappers) {
			mUrls.add(wrapper.getUrl(0));
		}
		mWrappers = wrappers;
	}

	@Override
	public int parse(Object in) {
		return STATUS_FAILED;
	}
	
	public BaseRequestJob getInnerWrapper(int index) {
		return mWrappers.get(index);
	}
	
	public void clear() {
		mWrappers.clear();
		mWrappers = null;
	}

}

package cn.garymb.ygomobile.data.wrapper;

import java.io.InputStream;
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
	public int parse(InputStream in) {
		return 0;
	}

}

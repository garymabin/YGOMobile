package cn.garymb.ygomobile.data.wrapper;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class VersionDataWrapper extends BaseDataWrapper {
	private List<JSONObject> mData;

	public VersionDataWrapper(int requestType) {
		super(requestType);
		mData = new ArrayList<JSONObject>();
//		mUrls.add(ResourcesConstants)
	}

	@Override
	public void parse(StringBuilder out) {
		
	}

}

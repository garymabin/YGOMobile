package cn.garymb.ygomobile.data.wrapper;


import org.json.JSONArray;
import org.json.JSONException;

import cn.garymb.ygomobile.model.data.ResourcesConstants;
import cn.garymb.ygomobile.model.data.VersionInfo;

public class VersionDataWrapper extends BaseDataWrapper {

	private VersionInfo mVersionInfo;

	public VersionDataWrapper(int requestType) {
		super(requestType);
		mUrls.add(ResourcesConstants.UPDATE_SERVER_URL + ResourcesConstants.VERSION_UPDATE_URL);
	}

	@Override
	public void parse(StringBuilder out) {
		try {
			JSONArray array = new JSONArray(out.toString());
			mVersionInfo = new VersionInfo();
			mVersionInfo.initFromJsonData(array.getJSONObject(0));
		} catch (JSONException e) {
		}
	}
	
	public VersionInfo getVersionInfo() {
		return mVersionInfo;
	}

}

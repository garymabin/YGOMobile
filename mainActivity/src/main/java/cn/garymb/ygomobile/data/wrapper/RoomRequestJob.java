package cn.garymb.ygomobile.data.wrapper;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.garymb.ygomobile.model.data.BaseInfo;
import cn.garymb.ygomobile.ygo.YGORoomInfo;

public class RoomRequestJob extends BaseRequestJob {

	private List<JSONObject> mData;

	public RoomRequestJob() {
		mData = new ArrayList<JSONObject>();
		mUrls.add(ROOM_LIST_URL);
	}

	@Override
	public int parse(Object in) {
		StringBuilder out = (StringBuilder) in;
		int result = STATUS_SUCCESS;
		try {
			JSONArray array = new JSONArray(out.toString());
			for (int i = 0; i < out.length(); i++) {
				mData.add(array.getJSONObject(i));
			}
		} catch (JSONException e) {
			result = STATUS_FAILED;
		}
		return result;
	}

	public int size() {
		return mData.size();
	}

	public BaseInfo getItem(int index) {
		BaseInfo info = new YGORoomInfo();
		try {
			info.fromJSONData(mData.get(index));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return info;
	}
}

package cn.garymb.ygomobile.model.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcelable;

public abstract class BaseInfo implements Cloneable, ResourcesConstants, Parcelable{
	
	public String id;
	
	@Override
	protected BaseInfo clone() {
		try {
			return (BaseInfo)super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public void initFromJsonData(JSONObject data) throws JSONException {
		id = data.getString(JSON_KEY_ID);
	}

}

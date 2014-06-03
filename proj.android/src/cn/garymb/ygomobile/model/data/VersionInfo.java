package cn.garymb.ygomobile.model.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;

public class VersionInfo extends BaseInfo {
	
	public String version;
	public String url;
	
	@Override
	public void initFromJsonData(JSONObject data) throws JSONException {
		version = data.getString(ResourcesConstants.JSON_KEY_VERSION);
		url = data.getString(ResourcesConstants.JSON_KEY_VERSION_URL);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		version = dest.readString();
		url = dest.readString();
	}
	
	public static final Creator<VersionInfo> CREATOR = new Creator<VersionInfo>() {
		
		@Override
		public VersionInfo[] newArray(int size) {
			return new VersionInfo[size];
		}
		
		@Override
		public VersionInfo createFromParcel(Parcel source) {
			VersionInfo info = new VersionInfo();
			info.version = source.readString();
			info.url = source.readString();
			return info;
		}
	};

}

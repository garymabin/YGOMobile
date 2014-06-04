package cn.garymb.ygomobile.model.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;

public class VersionInfo extends BaseInfo {
	
	public int version;
	public String url;
	
	@Override
	public void initFromJsonData(JSONObject data) throws JSONException {
		version = data.getInt(ResourcesConstants.JSON_KEY_VERSION);
		url = data.getString(ResourcesConstants.JSON_KEY_VERSION_URL);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(version);
		dest.writeString(url);
	}
	
	public static final Creator<VersionInfo> CREATOR = new Creator<VersionInfo>() {
		
		@Override
		public VersionInfo[] newArray(int size) {
			return new VersionInfo[size];
		}
		
		@Override
		public VersionInfo createFromParcel(Parcel source) {
			VersionInfo info = new VersionInfo();
			info.version = source.readInt();
			info.url = source.readString();
			return info;
		}
	};

}

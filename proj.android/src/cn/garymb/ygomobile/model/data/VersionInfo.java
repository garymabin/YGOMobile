package cn.garymb.ygomobile.model.data;

import java.nio.ByteBuffer;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;

public class VersionInfo extends BaseInfo {
	
	public int version;
	public String url;
	
	@Override
	public void fromJSONData(JSONObject data) throws JSONException {
		version = data.getInt(ResourcesConstants.JSON_KEY_VERSION);
		url = data.getString(ResourcesConstants.JSON_KEY_VERSION_URL);
	}
	

	@Override
	protected void readFromParcel(Parcel source) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ByteBuffer toByteBuffer(ByteBuffer buffer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fromByteBuffer(ByteBuffer buffer) {
		// TODO Auto-generated method stub
		
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

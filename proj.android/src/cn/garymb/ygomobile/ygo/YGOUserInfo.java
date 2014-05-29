package cn.garymb.ygomobile.ygo;

import org.json.JSONException;
import org.json.JSONObject;

import cn.garymb.ygomobile.model.data.BaseInfo;

import android.os.Parcel;

public class YGOUserInfo extends BaseInfo {
	
	public String name;
	public int playerID;
	public boolean certified;
	
	@Override
	protected YGOUserInfo clone() {
		return (YGOUserInfo)super.clone();
	}
	
	@Override
	public void initFromJsonData(JSONObject data) throws JSONException {
		super.initFromJsonData(data);
		name = data.getString(JSON_KEY_NAME);
		playerID = data.getInt(JSON_KEY_USER_PLAYER_ID);
		certified = data.getBoolean(JSON_KEY_USER_CERTIFIED);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeInt(playerID);
		dest.writeInt(certified ? 1 : 0);
	}
	
	public static final Creator<YGOUserInfo> CREATOR = new Creator<YGOUserInfo>() {

		@Override
		public YGOUserInfo createFromParcel(Parcel source) {
			YGOUserInfo info = new YGOUserInfo();
			info.id = source.readString();
			info.name = source.readString();
			info.playerID = source.readInt();
			info.certified = source.readInt() > 0;
			return null;
		}

		@Override
		public YGOUserInfo[] newArray(int size) {
			return new YGOUserInfo[size];
		}
	};

}

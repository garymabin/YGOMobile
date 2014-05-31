package cn.garymb.ygomobile.ygo;

import org.json.JSONException;
import org.json.JSONObject;

import cn.garymb.ygomobile.model.data.BaseInfo;

import android.os.Parcel;

public class YGOServerInfo extends BaseInfo {
	
	public YGOServerInfo() {
	}
	
	public YGOServerInfo(String id, String userName, String name, String ip, int port) {
		this.id = id;
		this.userName = userName;
		this.name = name;
		ipAddrString = ip;
		this.port = port;
	}
	
	public String userName;
	public String name;
	public String ipAddrString;
	public String serverInfoString = "";
	public int port;
	public boolean auth;
	public int maxRooms;
	
	@Override
	public YGOServerInfo clone() {
		return (YGOServerInfo)super.clone();
	}
	
	@Override
	public void initFromJsonData(JSONObject data) throws JSONException {
		super.initFromJsonData(data);
		name = data.getString(JSON_KEY_NAME);
		ipAddrString = data.getString(JSON_KEY_SERVER_IP_ADDR);
		port = data.getInt(JSON_KEY_SERVER_PORT);
		auth = data.getBoolean(JSON_KEY_SERVER_AUTH);
		maxRooms = data.getInt(JSON_KEY_SERVER_MAX_ROOMS);
		//TODO:
		userName = "player";
		serverInfoString = "";
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof YGOServerInfo) {
			if (((YGOServerInfo) o).id.equals(id)) {
				return true;
			}
			return false;
		} else {
			return super.equals(o);
		}
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	public static final Creator<YGOServerInfo> CREATOR = new Creator<YGOServerInfo>() {

		@Override
		public YGOServerInfo createFromParcel(Parcel source) {
			YGOServerInfo info = new YGOServerInfo();
			info.id = source.readString();
			info.name = source.readString();
			info.ipAddrString = source.readString();
			info.userName = source.readString();
			info.serverInfoString = source.readString();
			info.port = source.readInt();
			info.auth = source.readInt() > 0 ? true : false;
			info.maxRooms = source.readInt();
			return info;
		}

		@Override
		public YGOServerInfo[] newArray(int size) {
			return new YGOServerInfo[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(ipAddrString);
		dest.writeString(userName);
		dest.writeString(serverInfoString);
		dest.writeInt(port);
		dest.writeInt(auth ? 1 : 0);
		dest.writeInt(maxRooms);
	}

}

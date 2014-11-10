package cn.garymb.ygomobile.ygo;

import java.nio.ByteBuffer;

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
	public void fromJSONData(JSONObject data) throws JSONException {
		super.fromJSONData(data);
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
	protected void readFromParcel(Parcel source) {
		this.id = source.readString();
		this.name = source.readString();
		this.ipAddrString = source.readString();
		this.userName = source.readString();
		this.serverInfoString = source.readString();
		this.port = source.readInt();
		this.auth = source.readInt() > 0 ? true : false;
		this.maxRooms = source.readInt();		
	}

	@Override
	public ByteBuffer toByteBuffer(ByteBuffer buffer) {
		return buffer;
	}

	@Override
	public void fromByteBuffer(ByteBuffer buffer) {
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
			info.readFromParcel(source);
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

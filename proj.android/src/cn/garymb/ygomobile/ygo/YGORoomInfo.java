package cn.garymb.ygomobile.ygo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.garymb.ygomobile.model.data.BaseInfo;

import android.os.Parcel;

public class YGORoomInfo extends BaseInfo {
	
	public String name;
	public boolean status;
	public int serverId;
	
	public List<YGOUserInfo> mUsers = new ArrayList<YGOUserInfo>();
	public int mode = 0;
	public int rule = -1;
	public boolean privacy = false;
	public int startLp = -1;
	public int startHand = -1;
	public int drawCount = -1;
	public boolean enablePriority = false;
	public boolean noDeckCheck = false;
	public boolean noDeckShuffle = false;
	
	public boolean deleted = false;
	
	private boolean isCompleteInfo;
	
	@Override
	public void fromJSONData(JSONObject data) throws JSONException {
		super.fromJSONData(data);
		name = data.getString(JSON_KEY_NAME);
		status = GAME_STATUS_START.equals(data.getString(JSON_KEY_ROOM_STATUS));
		serverId = data.getInt(JSON_KEY_ROOM_SERVER_ID);
		JSONArray usersArray = data.getJSONArray(JSON_KEY_ROOM_USERS);
		for (int i = 0; i < usersArray.length(); i ++) {
			YGOUserInfo info = new YGOUserInfo();
			info.fromJSONData(usersArray.getJSONObject(i));
			mUsers.add(info);
		}
		if (data.has(JSON_KEY_ROOM_MODE)) {
			mode = data.getInt(JSON_KEY_ROOM_MODE);
		}
		if (data.has(JSON_KEY_ROOM_PRIVACY)) {
			privacy = data.getBoolean(JSON_KEY_ROOM_PRIVACY);
		}
		if (data.has(JSON_KEY_ROOM_RULE)) {
			rule = data.getInt(JSON_KEY_ROOM_RULE);
			isCompleteInfo = true;
		}
		if (data.has(JSON_KEY_ROOM_START_LP)) {
			startLp = data.getInt(JSON_KEY_ROOM_START_LP);
			isCompleteInfo = true;
		}
		if (data.has(JSON_KEY_ROOM_START_HAND)) {
			startHand = data.getInt(JSON_KEY_ROOM_START_HAND);
			isCompleteInfo = true;
		}
		if (data.has(JSON_KEY_ROOM_DRAW_COUNT)) {
			drawCount = data.getInt(JSON_KEY_ROOM_DRAW_COUNT);
			isCompleteInfo = true;
		}
		if (data.has(JSON_KEY_ROOM_ENABLE_PRIORITY)) {
			enablePriority = data.getBoolean(JSON_KEY_ROOM_ENABLE_PRIORITY);
			isCompleteInfo = true;
		}
		if (data.has(JSON_KEY_ROOM_NO_CHECK_DECK)) {
			noDeckCheck = data.getBoolean(JSON_KEY_ROOM_NO_CHECK_DECK);
			isCompleteInfo = true;
		}
		if (data.has(JSON_KEY_ROOM_NO_SHUFFLE_DECK)) {
			noDeckShuffle = data.getBoolean(JSON_KEY_ROOM_NO_SHUFFLE_DECK);
			isCompleteInfo = true;
		}
		if (data.has(JSON_KEY_ROOM_DELETED)) {
			deleted = data.getBoolean(JSON_KEY_ROOM_DELETED);
		}
	}
	
	@Override
	protected void readFromParcel(Parcel source) {
		this.id = source.readString();
		this.name = source.readString();
		this.serverId = source.readInt();
		this.mUsers = source.createTypedArrayList(YGOUserInfo.CREATOR);
		this.mode = source.readInt();
		this.rule = source.readInt();
		this.privacy = source.readInt() > 0;
		this.startLp = source.readInt();
		this.startHand = source.readInt();
		this.drawCount = source.readInt();
		this.enablePriority = source.readInt() > 0;
		this.noDeckCheck = source.readInt() > 0;
		this.noDeckShuffle = source.readInt() > 0;
		this.deleted = source.readInt() > 0;
		this.isCompleteInfo = source.readInt() > 0;
	}

	@Override
	public ByteBuffer toByteBuffer(ByteBuffer buffer) {
		return buffer;
	}

	@Override
	public void fromByteBuffer(ByteBuffer buffer) {
	}
	
	public boolean isCompleteInfo() {
		return isCompleteInfo;
	}
	
	@Override
	public YGORoomInfo clone() {
		YGORoomInfo info = (YGORoomInfo)super.clone();
		info.mUsers = new ArrayList<YGOUserInfo>();
		for (YGOUserInfo item : mUsers) {
			info.mUsers.add(item.clone());
		}
		return info;
	}
	
	public static final Creator<YGORoomInfo> CREATOR = new Creator<YGORoomInfo>() {

		@Override
		public YGORoomInfo createFromParcel(Parcel source) {
			YGORoomInfo info = new YGORoomInfo();
			info.readFromParcel(source);
			return info;
		}

		@Override
		public YGORoomInfo[] newArray(int size) {
			return new YGORoomInfo[size];
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
		dest.writeInt(serverId);
		dest.writeTypedList(mUsers);
		dest.writeInt(mode);
		dest.writeInt(rule);
		dest.writeInt(privacy ? 1 :0);
		dest.writeInt(startLp);
		dest.writeInt(startHand);
		dest.writeInt(drawCount);
		dest.writeInt(enablePriority ? 1 : 0);
		dest.writeInt(noDeckCheck ? 1 : 0);
		dest.writeInt(noDeckShuffle ? 1 : 0);
		dest.writeInt(deleted ? 1 : 0);
		dest.writeInt(isCompleteInfo ? 1 : 0);
	}
}

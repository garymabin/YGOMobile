package cn.garymb.ygodata;

import java.nio.ByteBuffer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author mabin
 *
 */
public class YGOGameOptions implements Parcelable {
	
	public static final String YGO_GAME_OPTIONS_BUNDLE_KEY = "cn.garymb.ygomobile.ygogameoptions";
	public static final int MAX_BYTE_BUFFER_SIZE = 8192;
	
	public String mServerAddr;
	
	public String mName;
	
	public String mRoomName;
	
	public String mRoomPasswd;
	
	public int mPort;
	
	public int mMode;
	
	public int mRule;
	
	public int mStartLP;
	
	public int mStartHand;
	
	public int mDrawCount;
	
	public boolean mEnablePriority;
	
	public boolean mNoDeckCheck;
	
	public boolean mNoDeckShuffle;
	
	private boolean isCompleteOptions;
	
	public boolean isCompleteOptions() {
		return isCompleteOptions;
	}

	public void setCompleteOptions(boolean isCompleteOptions) {
		this.isCompleteOptions = isCompleteOptions;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mServerAddr);
		dest.writeString(mName);
		dest.writeString(mRoomName);
		dest.writeString(mRoomPasswd);
		dest.writeInt(mPort);
		dest.writeInt(mMode);
		dest.writeInt(isCompleteOptions ? 1 : 0);
		dest.writeInt(mRule);
		dest.writeInt(mStartLP);
		dest.writeInt(mStartHand);
		dest.writeInt(mDrawCount);
		dest.writeInt(mEnablePriority ? 1 : 0);
		dest.writeInt(mNoDeckCheck ? 1 : 0);
		dest.writeInt(mNoDeckShuffle ? 1 : 0);
	}
	
	public static final Creator<YGOGameOptions> CREATOR = new Creator<YGOGameOptions>() {

		@Override
		public YGOGameOptions createFromParcel(Parcel source) {
			YGOGameOptions options = new YGOGameOptions();
			options.mServerAddr = source.readString();
			options.mName = source.readString();
			options.mRoomName = source.readString();
			options.mRoomPasswd = source.readString();
			options.mPort = source.readInt();
			options.mMode = source.readInt();
			options.isCompleteOptions = source.readInt() == 1;
			options.mRule = source.readInt();
			options.mStartLP = source.readInt();
			options.mStartHand = source.readInt();
			options.mDrawCount = source.readInt();
			options.mEnablePriority = source.readInt() == 1;
			options.mNoDeckCheck = source.readInt() == 1;
			options.mNoDeckShuffle = source.readInt() == 1;
			return options;
		}

		@Override
		public YGOGameOptions[] newArray(int size) {
			return new YGOGameOptions[size];
		}
	};
	
	public String toString() {
		StringBuilder builder = new StringBuilder("YGOGameOptions: ");
		builder.append("serverAddr: ").append(mServerAddr == null ? "(unspecified)" : mServerAddr).
		append(", port: ").append(mPort).
		append(", roomName: ").append(mRoomName == null ? "(unspecified)" : mRoomName.toString()).
		append(", roomPassword: ").append(mRoomPasswd == null ? "(unspecified)" : mRoomPasswd.toString()).
		append(", userName: ").append(mName == null ? "(unspecified)" : mName.toString()).
		append(", mode: ").append(mMode).
		append(", isCompleteRequest").append(isCompleteOptions).
		append(", rule: ").append(mRule).
		append(", startlp: ").append(mStartLP).
		append(", startHand: ").append(mStartHand).
		append(", drawCount: ").append(mDrawCount).
		append(", enablePriority: ").append(mEnablePriority).
		append(", noDeckCheck: ").append(mNoDeckCheck).
		append(", noDeckShuffle: ").append(mNoDeckShuffle);
		return builder.toString();
	}
	
	public ByteBuffer toByteBuffer() {
		ByteBuffer buffer = ByteBuffer.allocateDirect(MAX_BYTE_BUFFER_SIZE);
		buffer.putInt(Integer.reverseBytes(mServerAddr.getBytes().length));
		buffer.put(mServerAddr.getBytes());
		buffer.putInt(Integer.reverseBytes(mName.getBytes().length));
		buffer.put(mName.getBytes());
		buffer.putInt(Integer.reverseBytes(mRoomName.getBytes().length));
		buffer.put(mRoomName.getBytes());
		if (mRoomPasswd == null || mRoomPasswd.equals("")) {
			buffer.putInt(Integer.reverseBytes(0));
		} else {
			buffer.putInt(Integer.reverseBytes(mRoomPasswd.getBytes().length));
			buffer.put(mRoomPasswd.getBytes());
		}
		buffer.putInt(Integer.reverseBytes(mPort));
		buffer.putInt(Integer.reverseBytes(mMode));
		buffer.putInt(Integer.reverseBytes(isCompleteOptions ? 1 : 0));
		if (isCompleteOptions) {
			buffer.putInt(Integer.reverseBytes(mRule));
			buffer.putInt(Integer.reverseBytes(mStartLP));
			buffer.putInt(Integer.reverseBytes(mStartHand));
			buffer.putInt(Integer.reverseBytes(mDrawCount));
			buffer.putInt(Integer.reverseBytes(mEnablePriority ? 1 : 0));
			buffer.putInt(Integer.reverseBytes(mNoDeckCheck ? 1 : 0));
			buffer.putInt(Integer.reverseBytes(mNoDeckShuffle ? 1 : 0));
		}
		return buffer;
	}

}

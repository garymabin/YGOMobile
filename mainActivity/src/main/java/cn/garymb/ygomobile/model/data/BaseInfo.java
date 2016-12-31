package cn.garymb.ygomobile.model.data;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import cn.garymb.ygomobile.common.Constants;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public abstract class BaseInfo implements Cloneable, ResourcesConstants, Parcelable {
	
	private static final String TAG = "BaseInfo";
	
	public static final float BYTE_BUFFER_GROW_SCALE = 1.25f;
	
	public static final float ARRAY_BUFFER_GROW_SCALE = 2f;
	
	public static final int ARRAY_BUFFER_INIT_SIZE = 4096;
	
	private static byte[] sRawArray = new byte[ARRAY_BUFFER_INIT_SIZE]; 
	
	abstract protected void readFromParcel(Parcel source);
	
	abstract public ByteBuffer toByteBuffer(ByteBuffer buffer);
	
	abstract public void fromByteBuffer(ByteBuffer buffer);
	
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
	
	
	public void fromJSONData(JSONObject data) throws JSONException {
		id = data.getString(JSON_KEY_ID);
	}
	
	private static ByteBuffer expandByteBuffer(ByteBuffer buffer) {
		ByteBuffer container;
		container = ByteBuffer.allocate((int)(buffer.capacity() * BYTE_BUFFER_GROW_SCALE));
		Log.i(TAG, "byte buffer grow to " + container.capacity());
		container.put(buffer.array(), 0, buffer.position());
		buffer.clear();
		return container;
	}
	
	private static void adjustRawArray(int size) {
		int toAdjust = size;
		if (size > sRawArray.length) {
			toAdjust = (int) ((size / ARRAY_BUFFER_INIT_SIZE) + 1) * ARRAY_BUFFER_INIT_SIZE;
		} else if (size < sRawArray.length / 2) {
			toAdjust = sRawArray.length / 2;
		}
		byte[] newArray = new byte[toAdjust];
		sRawArray = newArray;
	}
	
	protected static ByteBuffer putBaseInfo(ByteBuffer buffer, BaseInfo info) {
		if (info != null) {
			String classid = info.getClass().getName();
			int index = classid.lastIndexOf('.');
			String className = classid.substring(index + 1, classid.length());
			buffer = putString(buffer, className);
			buffer = info.toByteBuffer(buffer);
		} else {
			buffer = putString(buffer, null);
		}
		return buffer;
	}
	
	protected static BaseInfo getBaseInfo(ByteBuffer buffer) {
		BaseInfo info = null;
		Class<?> cls = null;
		try {
			String className = getString(buffer);
			cls = Class.forName(BaseInfo.class.getPackage().getName() + "."
					+ className);
//			if (RoomInfo.class.equals(cls)) {
//				info = new RoomInfo();
//				((RoomInfo) info).fromByteBuffer(buffer);
//			} else if (ImageItem.class.equals(cls)) {
//				info = new ImageItem();
//				((ImageItem) info).fromByteBuffer(buffer);
//			} else if (AnchorInfo.class.equals(cls)) {
//				info = new AnchorInfo();
//				((AnchorInfo) info).fromByteBuffer(buffer);
//			} else if (BadgeInfo.class.equals(cls)) {
//				info = new BadgeInfo();
//				((BadgeInfo) info).fromByteBuffer(buffer);
//			} else if (UserInfo.class.equals(cls)) {
//				info = new UserInfo();
//				((UserInfo) info).fromByteBuffer(buffer);
//			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return info;
	}
	
	@SuppressWarnings("unchecked")
	protected static List<? extends BaseInfo> getBaseInfoList(ByteBuffer buffer) {
		List<? extends BaseInfo> list = Collections.emptyList();
		int size = getInt(buffer);
		if (size != 0) {
			list = new LinkedList<BaseInfo>();
		}
		for (int i = 0; i < size; i++) {
			BaseInfo info = getBaseInfo(buffer); 
			((LinkedList<BaseInfo>) list).addLast(info);
		}
		return list;
	}
	
	protected static ByteBuffer putBaseInfoList(ByteBuffer buffer, List<? extends BaseInfo> infoList) {
		if (infoList != null) {
			buffer = putInt(buffer,  infoList.size());
			for (BaseInfo info : infoList) {
				buffer = putBaseInfo(buffer, info);
			}
		} else {
			buffer = putInt(buffer, 0);
		}
		return buffer;
	}
	
	protected static ByteBuffer putInt(ByteBuffer buffer, int value) {
		ByteBuffer container = buffer;
		if (buffer.remaining() < 4) {
			container = expandByteBuffer(buffer);
			buffer = container;
		}
		container.putInt(value);
		return buffer;
	}
	
	protected static ByteBuffer putLong(ByteBuffer buffer, long value) {
		ByteBuffer container = buffer;
		if (buffer.remaining() < 8) {
			container = expandByteBuffer(buffer);
			buffer = container;
		}
		container.putLong(value);
		return buffer;
	}
	
	protected static ByteBuffer putBoolean(ByteBuffer buffer, boolean value) {
		ByteBuffer container = buffer;
		if (buffer.remaining() < 1) {
			container = expandByteBuffer(buffer);
			buffer = container;
		}
		container.put(value ? (byte)0 : (byte)1);
		return buffer;
	}
	
	protected static ByteBuffer putString(ByteBuffer buffer, String content) {
		ByteBuffer container = buffer;
		if (content != null) {
			int currentPos = container.position();
			try {
				int length = content.getBytes(Constants.DEFAULT_ENCODING).length;
				while (length + 4 > buffer.remaining()) {
					container = expandByteBuffer(buffer);
					buffer = container;
				}
				buffer = putInt(buffer, length);
				container.put(content.getBytes(Constants.DEFAULT_ENCODING));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				container.position(currentPos);
				container = putInt(container, 0);
			}
		} else {
			container = putInt(container, 0);
		}
		return container;
	}
	
	protected static String getString(ByteBuffer buffer) {
		int size = buffer.getInt();
		if (size != 0) {
			adjustRawArray(size);
			buffer.get(sRawArray, 0, size);
			try {
				return new String(sRawArray, 0, size, Constants.DEFAULT_ENCODING);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}
	
	protected static int getInt(ByteBuffer buffer) {
		return buffer.getInt();
	}
	
	protected static boolean getBoolean(ByteBuffer buffer) {
		return buffer.get() > (byte)0;
	}
	
	protected static long getLong(ByteBuffer buffer) {
		return buffer.getLong();
	}

}

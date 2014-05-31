package cn.garymb.ygomobile.model.data;

import java.util.HashMap;
import java.util.Map;


import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.ygo.YGORoomInfo;
import cn.garymb.ygomobile.ygo.YGOServerInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.SparseArray;


public class DataStore {
	
	public static final int MODIFIABLE_SERVER_INFO_START = 0x1000;
	public static final int MODIFIABLE_SERVER_CHECKMATE_SERVER = 0x1001;
	public static final int USER_DEFINE_SERVER_INFO_START = 0x1002;
	
	private static final String DEFAULT_CHECKMATE_SERVER_NAME = "checkmate";
	private static final String DEFAULT_CHECKMATE_SERVER_ADDR = "173.224.211.158";
	private static final int DEFAULT_CHECKMATE_SERVER_PORT = 21001;
	
	private static final String TAG = "DataStore";
	
	private SparseArray<YGOServerInfo> mServers;
	
	private Context mContext;
	
	
	public DataStore(Context context) {
		mContext = context;
		mServers = new SparseArray<YGOServerInfo>();
		LoadModifiableServers();
	}

	private void LoadModifiableServers() {
		SharedPreferences sp = mContext.getSharedPreferences(Constants.PREF_FILE_SERVER_LIST,
				Context.MODE_PRIVATE);
		//add checkmate server.
		loadNewServer(sp, MODIFIABLE_SERVER_CHECKMATE_SERVER, DEFAULT_CHECKMATE_SERVER_NAME, DEFAULT_CHECKMATE_SERVER_ADDR, DEFAULT_CHECKMATE_SERVER_PORT);
		int size = sp.getInt(Constants.PREF_KEY_USER_DEF_SERVER_SIZE, 0);
		//add user define server.
		for (int i = 0; i < size; i ++) {
			loadNewServer(sp, i + USER_DEFINE_SERVER_INFO_START, "", "", 0);
		}
	}
	
	public void removeServer(int groupId) {
		mServers.remove(groupId);
		SharedPreferences sp = mContext.getSharedPreferences(Constants.PREF_FILE_SERVER_LIST,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = mContext.getSharedPreferences(Constants.PREF_FILE_SERVER_LIST,
				Context.MODE_PRIVATE).edit();
		int size = sp.getInt(Constants.PREF_KEY_USER_DEF_SERVER_SIZE, 0);
		editor.putInt(Constants.PREF_KEY_USER_DEF_SERVER_SIZE, --size);
		editor.remove(Constants.PREF_KEY_SERVER_NAME + groupId);
		editor.remove(Constants.PREF_KEY_SERVER_ADDR + groupId);
		editor.remove(Constants.PREF_KEY_SERVER_PORT + groupId);
		editor.commit();
	}
	
	public void addNewServer(YGOServerInfo info) {
		mServers.put(Integer.parseInt(info.id), info);
		SharedPreferences sp = mContext.getSharedPreferences(Constants.PREF_FILE_SERVER_LIST,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = mContext.getSharedPreferences(Constants.PREF_FILE_SERVER_LIST,
				Context.MODE_PRIVATE).edit();
		int size = sp.getInt(Constants.PREF_KEY_USER_DEF_SERVER_SIZE, 0);
		editor.putInt(Constants.PREF_KEY_USER_DEF_SERVER_SIZE, ++size);
		editor.putString(Constants.PREF_KEY_SERVER_NAME + info.id, info.name);
		editor.putString(Constants.PREF_KEY_SERVER_ADDR + info.id, info.ipAddrString);
		editor.putInt(Constants.PREF_KEY_SERVER_PORT + info.id, info.port);
		editor.commit();
	}
	
	private void loadNewServer(SharedPreferences sp, int index, String defname, String defAddr, int defPort) {
		if (index < MODIFIABLE_SERVER_INFO_START) {
			Log.w(TAG, "can not add a server index less than 0x1000");
		}
		String server = sp.getString(Constants.PREF_KEY_SERVER_ADDR + index, defAddr);
		String name = sp.getString(Constants.PREF_KEY_SERVER_NAME + index, defname);
		int port  = sp.getInt(Constants.PREF_KEY_SERVER_PORT + index, defPort);
		YGOServerInfo info = new YGOServerInfo(index + "", name, server, port);
		mServers.put(index, info);
	}
	
	public synchronized SparseArray<YGOServerInfo> getServers() {
		if (mServers.get(0) == null) {
			mServers.put(0, new YGOServerInfo("0",
					ResourcesConstants.DEFAULT_MC_SERVER_NAME, ResourcesConstants.DEFAULT_MC_SERVER_ADDR, ResourcesConstants.DEFAULT_MC_SERVER_PORT));
		}
		return mServers;
	}
	
}

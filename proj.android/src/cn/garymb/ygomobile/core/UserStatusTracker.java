package cn.garymb.ygomobile.core;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.data.wrapper.IBaseWrapper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;

public class UserStatusTracker extends Handler {
	
	public static final int LOGIN_STATUS_LOG_OUT = -1;
	public static final int LOGIN_STATUS_LOGGING = 0;
	public static final int LOGIN_STATUS_LOGGED_IN = 1;
	public static final int LOGIN_STATUS_LOGIN_FAILED = 2;
	
	private List<WeakReference<Handler>> mNotifyTargets;
	
	private Context mContext;
	
	public UserStatusTracker(Context context) {
		mNotifyTargets = new ArrayList<WeakReference<Handler>>();
		mContext = context;
	}
	
	public boolean isUserInfoSaved() {
		return false;
	}
	
	public boolean isAutoLoginEnabled() {
		return false;
	}
	
	/* package */  int getLoginStatus() {
		SharedPreferences sp = mContext.getSharedPreferences(Constants.PREF_FILE_COMMON, Context.MODE_PRIVATE);
		return sp.getInt(Constants.PREF_KEY_LOGIN_STATUS, LOGIN_STATUS_LOG_OUT);
	}
	
	/* package */  String getLoginName() {
		SharedPreferences sp = mContext.getSharedPreferences(Constants.PREF_FILE_COMMON, Context.MODE_PRIVATE);
		return sp.getString(Constants.PREF_KEY_LOGIN_NAME, "");
	}
	
	/* package */  void changeLoginStatus(int status, boolean isNotify) {
		SharedPreferences sp = mContext.getSharedPreferences(Constants.PREF_FILE_COMMON, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt(Constants.PREF_KEY_LOGIN_STATUS, status);
		editor.commit();
		if (isNotify) {
			notifyTarget(mNotifyTargets, Constants.MSG_ID_LOGIN, status);
		}
	}
	
	/* package */  void setLoginName(String name) {
		SharedPreferences sp = mContext.getSharedPreferences(Constants.PREF_FILE_COMMON, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString(Constants.PREF_KEY_LOGIN_NAME, name);
		editor.commit();
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case Constants.MSG_ID_LOGIN:
			int result = IBaseWrapper.TASK_STATUS_SUCCESS;
			if (msg.arg2 != IBaseWrapper.TASK_STATUS_SUCCESS) {
				result = LOGIN_STATUS_LOGIN_FAILED;
			} else {
				result = LOGIN_STATUS_LOGGED_IN;
			}
			changeLoginStatus(result, true);
			break;

		default:
			break;
		}
		super.handleMessage(msg);
	}
	
	private void notifyTarget(List<WeakReference<Handler>> list, int msgType, int result) {
		for (WeakReference<Handler> item : list) {
			Handler h = item.get();
			if (h != null) {
				h.sendMessage(Message.obtain(null, msgType, result, 0));
			}
		}
	}
	
	/* package */  void registerForLoginStatusChange(Handler h) {
		WeakReference<Handler> ref = new WeakReference<Handler>(h);
		mNotifyTargets.add(ref);
	}
	
	/* package */  void unregisterForLoginStatusChange(Handler h) {
		for (WeakReference<Handler> item : mNotifyTargets) {
			if (h == item.get()) {
				mNotifyTargets.remove(item);
				item = null;
				break;
			}
		}
	}
	
}

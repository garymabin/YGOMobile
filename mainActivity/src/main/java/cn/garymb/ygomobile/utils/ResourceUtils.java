package cn.garymb.ygomobile.utils;

import android.content.Context;

public class ResourceUtils {

	private static Context mContext;
	
	public static String[] getStringArray(int paramInt){
		return mContext.getResources().getStringArray(paramInt);
	}
	
	public static void init(Context paramContext){
		mContext = paramContext;
	}
}

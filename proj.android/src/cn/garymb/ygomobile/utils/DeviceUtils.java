package cn.garymb.ygomobile.utils;

import cn.garymb.ygomobile.StaticApplication;

public class DeviceUtils {

	public static float getScreenWidth() {
		return StaticApplication.peekInstance().getScreenWidth();
	}
	
	public static float getScreenHeight() {
		return StaticApplication.peekInstance().getScreenHeight();
	}

	public static float getDensity() {
		return StaticApplication.peekInstance().getDensity();
	}
	
	public static float getXScale() {
		return StaticApplication.peekInstance().getXScale();
	}
	
	public static float getYScale() {
		return StaticApplication.peekInstance().getYScale();
	}
}

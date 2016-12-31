package cn.garymb.ygomobile.common;

import java.io.UnsupportedEncodingException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import cn.garymb.ygomobile.core.IrrlichtBridge;

public final class FileDownloadHelper {

	public static String getPrivateDownloadUrl(String url) {
		String resultUrl = url + "?e=" + System.currentTimeMillis() / 1000;
		String token = IrrlichtBridge.getAccessKey() + ":" + urlSafeBase64Encode(hMACSHA1(IrrlichtBridge.getSecretKey(), resultUrl)); 
		return resultUrl + "&token=" + token;
	}

	private static String hMACSHA1(String key, String datas) {
		String reString = "";
		try {
			byte[] data = key.getBytes("UTF-8");
			SecretKey secretKey = new SecretKeySpec(data, "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(secretKey);
			byte[] text = datas.getBytes("UTF-8");
			byte[] text1 = mac.doFinal(text);
			reString = Base64.encodeToString(text1, Base64.DEFAULT);
		} catch (Exception e) {
		}
		return reString;
	}
	
	private static String urlSafeBase64Encode(String url) {
		String encodeUrl = null;
		try {
			encodeUrl = Base64.encodeToString(url.getBytes("UTF-8"), Base64.URL_SAFE);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return encodeUrl;
	}

}

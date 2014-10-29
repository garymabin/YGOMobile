package cn.garymb.ygomobile.core.images;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.core.Controller;
import cn.garymb.ygomobile.model.data.ImageItem;
import cn.garymb.ygomobile.model.data.ImageItemInfoHelper;

import android.text.TextUtils;
import android.util.Log;

public class ImageDownloader {

	private OkHttpClient mClient;
	/**
	 * @param cacheManager
	 */
	public ImageDownloader() {
		mClient = StaticApplication.peekInstance().getOkHttpClient();
	}

	/**
	 * 
	 */
	/* package */void execute(ImageFileDownloadTaskHolder holder) {
		int resultCode = ImageFileDownloadTaskHolder.RET_DOWNLOAD_SUCCEED;

		if (StaticApplication.peekInstance().getMobileNetworkPref()
				&& !Controller.peekInstance().isWifiConnected()) {
			holder.setDownloadResult(ImageFileDownloadTaskHolder.RET_DOWNLOAD_FAILED);
			return;
		}

		InputStream in = null;
		FileOutputStream fos = null;
		ImageItem item = holder.getImageItem();
		String destPath = null;
		String targetUrl = null;
		switch (holder.getImageType()) {
		case Constants.IMAGE_TYPE_THUMNAIL:
			destPath = ImageItemInfoHelper.getThumnailPath(item);
			targetUrl = ImageItemInfoHelper.getThumnailUrl(item);
			break;
		case Constants.IMAGE_TYPE_ORIGINAL:
			destPath = ImageItemInfoHelper.getImagePath(item);
			targetUrl = ImageItemInfoHelper.getImageUrl(item);
			break;
		}

		if (TextUtils.isEmpty(destPath) || TextUtils.isEmpty(targetUrl)) {
			holder.setDownloadResult(ImageFileDownloadTaskHolder.RET_DOWNLOAD_FAILED);
			return;
		}
		// ------------ 2014.04.18 ---------------------------

		File destFile = new File(destPath);
		if (destFile.exists()) {
			holder.setDownloadResult(resultCode);
			return;
		}
		File tmpFile = new File(destPath + "tmp");
		try {
			Request.Builder builder = new Request.Builder().url(targetUrl);
			setHeader(builder);
			Response resp = null;
			ResponseBody body = null;
			InputStream respStream = null;
			Log.d("HttpUtils", "==IMG==before execute get: uri = " + targetUrl);
			resp = mClient.newCall(builder.build()).execute();
			Log.d("HttpUtils", "==IMG==after execute get");
			if (resp == null) {
				holder.setDownloadResult(-1);
				return;
			}
			int statusCode = resp.code();
			Log.d("HttpUtils", "==IMG==status code = " + statusCode);
			if (statusCode != 200) {
				holder.setDownloadResult(-1);
				return;
			}
			body = resp.body();
			if (body != null && (respStream = body.byteStream()) != null) {
				in = respStream;
			}
			fos = new FileOutputStream(tmpFile);
			int readCount = 0;
			byte[] buffer = new byte[Constants.IO_BUFFER_SIZE];
			while (!holder.isCancelRequested
					&& (readCount = in.read(buffer)) != -1) {
				fos.write(buffer, 0, readCount);
			}
			fos.flush();
			if (holder.isCancelRequested) {
				if (tmpFile != null) {
					tmpFile.delete();
				}
				resultCode = ImageFileDownloadTaskHolder.RET_DOWNLOAD_CANCELED;
			}
			Log.d("HttpUtils", "==IMG==File saved." + destPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			resultCode = ImageFileDownloadTaskHolder.RET_DOWNLOAD_FAILED;
		} catch (IOException e) {
			e.printStackTrace();
			resultCode = ImageFileDownloadTaskHolder.RET_DOWNLOAD_FAILED;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e2) {
			}
		}
		holder.setDownloadResult(resultCode);
		if (tmpFile != null) {
			if (resultCode == ImageFileDownloadTaskHolder.RET_DOWNLOAD_SUCCEED) {
				tmpFile.renameTo(destFile);
			} else {
				tmpFile.delete();
			}
		}
	}

	/* package */void cancel(ImageFileDownloadTaskHolder holder) {
		holder.isCancelRequested = true;
	}

	private void setHeader(Request.Builder builder) {
		builder.addHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		builder.addHeader("Accept-Encoding", "gzip,deflate,sdch");
		builder.addHeader("Accept-Language",
				"zh-CN,zh;q=0.8,en;q=0.6,zh-TW;q=0.4");
		builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
	}
}

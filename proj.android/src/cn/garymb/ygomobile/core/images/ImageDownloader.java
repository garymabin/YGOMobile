package cn.garymb.ygomobile.core.images;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.core.Controller;
import cn.garymb.ygomobile.model.data.ImageItem;
import cn.garymb.ygomobile.model.data.ImageItemInfoHelper;

import android.text.TextUtils;
import android.util.Log;


public class ImageDownloader {
	
	private HttpClient mClient;
	private static final String GZIP = "gzip";

	/**
	 * @param cacheManager
	 */
	public ImageDownloader() {
		mClient = StaticApplication.peekInstance().getHttpClient();
	}

	/**
	 * 
	 */
	/* package */ void execute(ImageFileDownloadTaskHolder holder) {
		int resultCode = ImageFileDownloadTaskHolder.RET_DOWNLOAD_SUCCEED;
		
		if (StaticApplication.peekInstance().getMobileNetworkPref() && !Controller.peekInstance().isWifiConnected()) {
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
		//------------ 2014.04.18 ---------------------------
		
		File destFile = new File(destPath);
		if (destFile.exists()) {
			holder.setDownloadResult(resultCode);
			return;
		}
		File tmpFile = new File(destPath + "tmp");
		try {
			HttpGet request = new HttpGet(targetUrl);
			setHeader(request);
			HttpResponse resp = null;
			HttpEntity entity = null;
			GZIPInputStream gzipRespStream = null;
			InputStream respStream = null;
			Log.d("HttpUtils", "==IMG==before execute get: uri = " + targetUrl);
			resp = mClient.execute(request);
			Log.d("HttpUtils", "==IMG==after execute get");
			StatusLine status = null;
			if (resp == null || (status = resp.getStatusLine()) == null) {
				holder.setDownloadResult(-1);
				return;
			}
			int statusCode = status.getStatusCode();
			Log.d("HttpUtils", "==IMG==status code = " + statusCode);
			if (statusCode != 200) {
				holder.setDownloadResult(-1);
				return;
			}
			entity = resp.getEntity();
			if (entity != null && (respStream = entity.getContent()) != null) {
				if (isGZipContent(entity)) {
					Log.d("HttpUtils", "receive gzip content!");
					gzipRespStream = new GZIPInputStream(respStream);
					in = gzipRespStream;
				} else {
					in = respStream;
				}
			}
			
			fos = new FileOutputStream(tmpFile);
			int readCount = 0;
			byte[] buffer = new byte[Constants.IO_BUFFER_SIZE];
			while (!holder.isCancelRequested && (readCount = in.read(buffer)) != -1) {
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
	
	/* package */ void cancel(ImageFileDownloadTaskHolder holder) {
		holder.isCancelRequested = true;
	}
	
	private void setHeader(HttpUriRequest request) {
		if (request == null)
			return;
		request.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		request.setHeader("Accept-Encoding", "gzip,deflate,sdch");
		request.setHeader("Accept-Language","zh-CN,zh;q=0.8,en;q=0.6,zh-TW;q=0.4");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");
	}
	
	private boolean isGZipContent(HttpEntity entity) {
		if (entity == null)
			return false;

		Header encoding = entity.getContentEncoding();
		return encoding != null && GZIP.equalsIgnoreCase(encoding.getValue());
	}


}

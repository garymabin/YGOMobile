package cn.garymb.ygomobile.data.wrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import android.util.Log;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.model.data.DownloadProgressEvent;
import cn.garymb.ygomobile.utils.HttpUtils;
import de.greenrobot.event.EventBus;
//import de.greenrobot.event.EventBus;

public class SimpleDownloadJob extends BaseRequestJob {
	
	private static final String TAG = "SimpleDownloadJob";
	private String mSavedPath;
	
	public SimpleDownloadJob(String url, String path) {
		mUrls.add(url);
		mSavedPath = path;
	}

	@Override
	public int parse(Object in) {
		int result = STATUS_FAILED;
		if (in instanceof Response) {
			result = STATUS_SUCCESS;
			FileOutputStream fos = null;
			File destFile = new File(mSavedPath);
			File tmpFile = new File(destFile + "tmp");
			try {
				fos = new FileOutputStream(tmpFile);
				int readCount = 0;
				
				Response resp = (Response) in;
				ResponseBody body = resp.body();
				InputStream is = null;
				if (body != null && (is = body.byteStream()) != null) {
					if (HttpUtils.isGZipContent(resp)) {
						is = new GZIPInputStream(is);
					}
				}
				byte[] buffer = new byte[Constants.IO_BUFFER_SIZE];
				long lastReportTime = System.currentTimeMillis();
				long totalSize = Integer.parseInt(resp.header("Content-Length"));
				long downloadSize = 0;
				Log.i(TAG, "totalLength = " + totalSize);
				EventBus.getDefault().post(new DownloadProgressEvent(totalSize, downloadSize));
				while (!Thread.currentThread().isInterrupted()
						&& (readCount = is.read(buffer)) != -1) {
					fos.write(buffer, 0, readCount);
					fos.flush();
					downloadSize += readCount;
					long now = System.currentTimeMillis();
					if (now - lastReportTime > 1000) {
						EventBus.getDefault().post(new DownloadProgressEvent(totalSize, downloadSize));
						lastReportTime = now;
					}
				}
				if (Thread.currentThread().isInterrupted()) {
					if (tmpFile != null) {
						tmpFile.delete();
					}
					throw new InterruptedException();
				}
			} catch (IOException e) {
				result = STATUS_FAILED;
			} catch (InterruptedException e1) {
				result = STATUS_CANCELED;
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						// just ignore
					}
				}
				if (tmpFile != null) {
					if (result == STATUS_SUCCESS) {
						tmpFile.renameTo(destFile);
					}
					tmpFile.delete();
				}
			}
		}
		setResult(result);
		return result;
	}

}

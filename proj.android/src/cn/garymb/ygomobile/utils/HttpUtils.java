package cn.garymb.ygomobile.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import cn.garymb.ygomobile.common.Constants;

import android.util.Log;

public class HttpUtils {

	private static final String GZIP = "gzip";

	public static InputStream doGet(HttpClient client, String uri) {
		HttpGet request = new HttpGet(uri);
		setHeader(request);
		HttpResponse resp = null;
		HttpEntity entity = null;
		GZIPInputStream gzipRespStream = null;
		InputStream respStream = null;

		try {
			resp = client.execute(request);
			StatusLine status = null;
			if (resp == null || (status = resp.getStatusLine()) == null)
				return null;
			int statusCode = status.getStatusCode();
			if (statusCode < 200 || statusCode >= 300) {
				Log.d("HttpUtils", "status code = " + statusCode);
				return null;
			}
			entity = resp.getEntity();
			InputStream in = null;
			if (entity != null && (respStream = entity.getContent()) != null) {
				if (isGZipContent(entity)) {
					Log.d("HttpUtils", "receive gzip content!");
					gzipRespStream = new GZIPInputStream(respStream);
					in = gzipRespStream;
				} else {
					in = respStream;
				}
			}
			return in;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static InputStream doOkGet(OkHttpClient client, String uri) {
		Response resp = null;
		ResponseBody body = null;
		InputStream respStream = null;

		try {
			Request req = new Request.Builder()
					.url(uri)
					.addHeader("Accept",
							"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
					.addHeader("Accept-Encoding", "gzip,deflate,sdch")
					.addHeader("Accept-Language",
							"zh-CN,zh;q=0.8,en;q=0.6,zh-TW;q=0.4")
					.addHeader("Content-Type",
							"application/x-www-form-urlencoded").build();
			resp = client.newCall(req).execute();
			int statusCode = resp.code();
			if (statusCode < 200 || statusCode >= 300) {
				Log.d("HttpUtils", "status code = " + statusCode);
				return null;
			}
			body = resp.body();
			InputStream in = null;
			if (body != null && (respStream = body.byteStream()) != null) {
				in = respStream;
			}
			return in;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean doGet(HttpClient client, String uri, StringBuilder out) {

		HttpGet request = new HttpGet(uri);
		setHeader(request);
		HttpResponse resp = null;
		HttpEntity entity = null;
		GZIPInputStream gzipRespStream = null;
		InputStream respStream = null;

		try {
			resp = client.execute(request);
			StatusLine status = null;
			if (resp == null || (status = resp.getStatusLine()) == null)
				return false;
			int statusCode = status.getStatusCode();
			if (statusCode < 200 || statusCode >= 300)
				return false;

			entity = resp.getEntity();
			if (entity != null && (respStream = entity.getContent()) != null) {
				InputStream in;
				if (isGZipContent(entity)) {
					gzipRespStream = new GZIPInputStream(respStream);
					in = gzipRespStream;
				} else {
					in = respStream;
				}
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in));
				int len = -1;
				char[] buffer = new char[Constants.IO_BUFFER_SIZE];
				while (!Thread.currentThread().isInterrupted()
						&& (len = reader.read(buffer, 0,
								Constants.IO_BUFFER_SIZE)) != -1)
					out.append(buffer, 0, len);
				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedException();
				}
			}

			return true;
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (entity != null)
					entity.consumeContent();
				if (respStream != null)
					respStream.close();
			} catch (IOException e) {
			}
		}
	}

	public static boolean doPost(HttpClient client, String uri,
			byte postData[], StringBuilder out) {
		// 创建HTTP请求
		HttpPost request = new HttpPost(uri);
		setHeader(request);
		if (postData != null)
			request.setEntity(new ByteArrayEntity(postData));

		HttpResponse resp = null;
		HttpEntity entity = null;
		GZIPInputStream gzipRespStream = null;
		InputStream respStream = null;

		try {
			resp = client.execute(request);
			StatusLine status = null;
			if (resp == null || (status = resp.getStatusLine()) == null)
				return false;
			int statusCode = status.getStatusCode();
			if (statusCode < 200 || statusCode >= 300)
				return false;

			entity = resp.getEntity();
			if (entity != null && (respStream = entity.getContent()) != null) {
				InputStream in;
				if (isGZipContent(entity)) {
					gzipRespStream = new GZIPInputStream(respStream);
					in = gzipRespStream;
				} else {
					in = respStream;
				}
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in));
				int len = -1;
				char[] buffer = new char[Constants.IO_BUFFER_SIZE];
				while ((len = reader.read(buffer, 0, Constants.IO_BUFFER_SIZE)) != -1)
					out.append(buffer, 0, len);
			}

			return true;
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (entity != null)
					entity.consumeContent();
				if (gzipRespStream != null)
					gzipRespStream.close();
				if (respStream != null)
					respStream.close();
			} catch (IOException e) {
			}

			ClientConnectionManager manager = client.getConnectionManager();
			if (manager != null)
				manager.shutdown();
		}
	}

	private static boolean isGZipContent(HttpEntity entity) {
		if (entity == null)
			return false;

		Header encoding = entity.getContentEncoding();
		return encoding != null && GZIP.equalsIgnoreCase(encoding.getValue());
	}

	private static void setHeader(HttpUriRequest request) {
		if (request == null)
			return;
		request.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		request.setHeader("Accept-Encoding", "gzip,deflate,sdch");
		request.setHeader("Accept-Language",
				"zh-CN,zh;q=0.8,en;q=0.6,zh-TW;q=0.4");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");
	}

	/**
	 * 下载文件<br>
	 * 
	 * @deprecated 不支持续传，代理服务器等特性
	 * @param origUrl
	 *            URL
	 * @param file
	 *            用于保存的文件
	 * @return 下载成功返回true，失败返回false
	 */
	public static boolean downloadFile(String url, File file) {
		InputStream instream = null;
		OutputStream outstream = null;

		File dir = new File(file.getParent());
		File tmpFile = new File(dir, "_tmp");
		if (tmpFile.exists())
			tmpFile.delete();

		HttpParams httpParams = new BasicHttpParams();
		httpParams.setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT,
				Constants.TRANSACT_TIMEOUT);
		httpParams.setIntParameter(HttpConnectionParams.SO_TIMEOUT,
				Constants.TRANSACT_TIMEOUT);
		DefaultHttpClient client = new DefaultHttpClient(httpParams);
		HttpGet request = new HttpGet(url);
		HttpResponse resp = null;
		HttpEntity entity = null;

		try {
			resp = client.execute(request);
			StatusLine status = null;
			if (resp == null || (status = resp.getStatusLine()) == null)
				return false;
			int statusCode = status.getStatusCode();
			if (statusCode < 200 || statusCode >= 300)
				return false;
			entity = resp.getEntity();
			instream = entity.getContent();

			if (instream == null)
				return false;

			int count = 0;
			outstream = new FileOutputStream(tmpFile);
			byte[] buffer = new byte[Constants.IO_BUFFER_SIZE];
			while ((count = instream.read(buffer, 0, Constants.IO_BUFFER_SIZE)) >= 0) {
				outstream.write(buffer, 0, count);
			}

			tmpFile.renameTo(file);
			return true;

		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (instream != null)
					instream.close();
				if (outstream != null)
					outstream.close();
				if (entity != null)
					entity.consumeContent();
			} catch (IOException e) {
			}

			ClientConnectionManager manager = client.getConnectionManager();
			if (manager != null)
				manager.shutdown();
		}
	}

}

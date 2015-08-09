package cn.garymb.ygomobile.model.data;

import java.io.File;

import cn.garymb.ygomobile.StaticApplication;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

public final class ImageItemInfoHelper {
	public final static String HTTP_PREFIX = "http://";
	public final static String HTTPS_PREFIX = "https://";

	public final static String MEIDA_PREFIX = "content://";
	public final static String FILE_PREFIX = "file://";

	public final static String ABSOLUTE_PATH_PREFIX = "/";

	public static final String AVATAR_IMAGE_PRIFIX = "avatar";

	public static final String PNG_IMAGE_SUFFIX = ".png";
	public static final String JPG_IMAGE_SUFFIX = ".jpg";
	public static final String BMP_IMAGE_SUFFIX = ".bmp";
	public static final String INVALID_IMAGE_SUFFIX = ".";

	public static final String[] SUPPORTED_SUFFIXES = { JPG_IMAGE_SUFFIX,
			PNG_IMAGE_SUFFIX, BMP_IMAGE_SUFFIX };

	private static final String TMP_SUFFIX = ".tmp";

	private static String BASE_URL = ResourcesConstants.CARD_IMAGE_DOWNLOAD_URL;

	public static void init(String url) {
		int index = url.lastIndexOf(':');
		BASE_URL = url.substring(0, index);
	}

	public static boolean isImageExist(ImageItem item) {
		final String path = getImagePath(item);
		if (path == null)
			return false;
		File file = new File(path);
		if (file.exists() && file.isFile())
			return true;

		return false;
	}

	public static String getImagePath(ImageItem item) {
		if (item == null)
			return null;

		// if id equals file's url, just return it
		if (item.id.startsWith(MEIDA_PREFIX)) {
			ContentResolver cr = StaticApplication.peekInstance()
					.getContentResolver();
			String[] projection = { MediaStore.MediaColumns.DATA };
			Cursor cursor = cr.query(Uri.parse(item.id), projection, null,
					null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String path = cursor.getString(0);
				cursor.close();
				return path;
			} else {
				if (cursor != null) {
					cursor.close();
				}
				return null;
			}
		} else if (item.id.startsWith(ABSOLUTE_PATH_PREFIX)) {
			return item.id;
		} else {
			StringBuilder sb = new StringBuilder();
			for (String suffix : SUPPORTED_SUFFIXES) {
				sb.append(StaticApplication.peekInstance().getCardImagePath());
				if (!new File(sb.toString()).exists()) {
					new File(sb.toString()).mkdirs();
				}
				sb.append(item.id).append(suffix);
				File f = new File(sb.toString());
				if (f.exists()) {
					break;
				} else {
					sb.delete(0, sb.length());
				}
			}
			if (sb.toString().endsWith(INVALID_IMAGE_SUFFIX)) {
				sb.deleteCharAt(sb.length() - 1).append(JPG_IMAGE_SUFFIX);
			}
			return sb.toString();
		}
	}

	public static String getDownloadImagePath(ImageItem item) {
		if (item == null)
			return null;

		StringBuilder sb = new StringBuilder();
		sb.append(StaticApplication.peekInstance().getCardImagePath());
		if (!new File(sb.toString()).exists()) {
			new File(sb.toString()).mkdirs();
		}
		sb.append(item.id).append(JPG_IMAGE_SUFFIX);
		return sb.toString();
	}

	public static String getImageDownloadTempPath(ImageItem item) {
		return getDownloadImagePath(item) + TMP_SUFFIX;
	}

	public static String getImageUrl(ImageItem item) {
		String url = item.urlSegment;

		if (url != null
				&& (url.startsWith(HTTP_PREFIX) || url.startsWith(HTTPS_PREFIX)))
			return url;

		url = TextUtils.isEmpty(BASE_URL) ? null : BASE_URL + item.id
				+ JPG_IMAGE_SUFFIX;
		return url;
	}

}

/*
 * FileOpsUtils.java
 *
 *  Created on: 2013年9月24日
 *      Author: mabin
 */
package cn.garymb.ygomobile.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.List;

import cn.garymb.ygomobile.Constants;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

/**
 * @author mabin
 * 
 */
public final class FileOpsUtils {

	public static String formatTime(long timeInMiniSeconds) {
		return DateFormat.format("yy-MM-dd ahh:mm", timeInMiniSeconds)
				.toString();
	}

	public static void assetsCopy(Context context, String assetsPath,
			String dirPath, boolean isSmart) throws IOException {
		AssetManager am = context.getAssets();
		String[] list = am.list(assetsPath);
		if (list.length == 0) {
			File file = new File(dirPath);
			if (!isSmart || !file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
				InputStream in = am.open(assetsPath);
				FileOutputStream fout = new FileOutputStream(file);
				write(in, fout);
			}
		} else {
			for (String path : list) {
				assetsCopy(context, join(assetsPath, path),
						join(dirPath, path), isSmart);
			}
		}
	}

	private static void write(InputStream in, OutputStream out)
			throws IOException {
		byte[] buf = new byte[Constants.IO_BUFFER_SIZE];
		int count;
		try {
			while ((count = in.read(buf)) != -1) {
				out.write(buf, 0, count);
			}
			out.flush();
		} finally {
			in.close();
			out.close();
		}
	}

	// Joins two path components, adding a separator only if necessary.
	private static String join(String prefix, String suffix) {
		int prefixLength = prefix.length();
		boolean haveSlash = (prefixLength > 0 && prefix
				.charAt(prefixLength - 1) == File.separatorChar);
		if (!haveSlash) {
			haveSlash = (suffix.length() > 0 && suffix.charAt(0) == File.separatorChar);
		}
		return haveSlash ? (prefix + suffix)
				: (prefix + File.separatorChar + suffix);
	}

	public static String formatReadableFileSize(long size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size
				/ Math.pow(1024, digitGroups))
				+ " " + units[digitGroups];
	}

	/**
	 * 
	 * @author: mabin
	 * @return
	 **/
	public static String getExtension(File file) {
		// TODO Auto-generated method stub
		String name = file.getName();
		int i = name.lastIndexOf('.');
		int p = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
		return i > p ? name.substring(i + 1) : "";
	}

	public static String encodeFilename(File file) throws IOException {
		String filename = URLEncoder.encode(getFilename(file),
				Constants.ENCODING);
		return filename;
	}

	private static String getFilename(File file) {
		return file.isFile() ? file.getName() : file.getName() + ".zip";
	}

	@SuppressLint("DefaultLocale")
	public static String getExtName(String filename) {
		int position = filename.lastIndexOf(".");
		if (position >= 0)
			return filename.substring(position + 1).toLowerCase();
		else
			return "";
	}

	public static void listFiles(List<File> list, File file, String base) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (null != files && files.length > 0) {
				for (File f : files) {
					listFiles(list, f, base + "/" + f.getName());
				}
			}
		} else {
			list.add(file);
		}
	}

	public static void clearDir(String dirUrl) {
		if (dirUrl == null || dirUrl.equals("")) {
			return;
		}
		File dir = new File(dirUrl);
		if (!dir.isDirectory()) {
			return;
		}
		for (File file : dir.listFiles()) {
			if (!file.isDirectory()) {
				file.delete();
			}
		}
	}

	public static String getFilePathFromUrl(String uriString) {
		String filePath = uriString.replace("file://", "");
		String prefix = Environment.getExternalStorageDirectory().getPath();
		filePath = filePath.replace(prefix, "/mnt/sdcard");
		return filePath;
	}

	public static String getUrlFromFromPath(String path) {
		String prefix = Environment.getExternalStorageDirectory().getPath();
		prefix = "file:/".concat(prefix);
		String url = path.replace("/mnt/sdcard", prefix);
		return url;
	}

	public static void recursiveDetele(String path) {
		if (TextUtils.isEmpty(path)) {
			return;
		}
		File dir = new File(path);
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				File temp = new File(dir, children[i]);
				if (temp.isDirectory()) {
					recursiveDetele(temp.getPath());
				} else {
					boolean b = temp.delete();
					if (b == false) {
						Log.d("recursiveDetele", "DELETE FAIL");
					}
				}
			}

		}
		dir.delete();
	}

	public static String getFilterUrlFromFromPath(String path) {
		String prefix = Environment.getExternalStorageDirectory().getPath();
		prefix = prefix.replaceFirst("/", "");
		String url = path.replace("/mnt/sdcard", prefix);
		return url;
	}

	public static void copyFileUsingFileChannels(File source, File dest)
			throws IOException {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			inputChannel.close();
			outputChannel.close();
		}
	}
}

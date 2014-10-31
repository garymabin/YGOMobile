package cn.garymb.ygomobile.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.garymb.ygomobile.MainActivity;
import cn.garymb.ygomobile.R;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

/**
 * 管理应用程序的通知栏
 */
public class NotificationMgr {

	private static final int ID_DOWNLOAD_STATUS = 1;
	
	public static void showDownloadStatus(Context context, Bitmap largeIcon, List<String> labels) {
		if (labels == null || labels.size() <= 0)
			return;
		
		int count = labels.size();
		String spilit = context.getString(R.string.image_download_label);
		String title = String.format(Locale.getDefault(), context.getString(R.string.image_is_downloading), count);
		String text = buildLabelsText(labels, spilit);
		
		Intent intent = new Intent(context, MainActivity.class);
		intent.setAction(Constants.ACTION_VIEW_DOWNLOAD_STATUS);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		NotificationManager nm = (NotificationManager) context.getSystemService(
				Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentIntent(pendingIntent);
		builder.setContentTitle(title);
		builder.setContentText(text);
		builder.setTicker(title);
		builder.setAutoCancel(false);
		builder.setOngoing(true);
		builder.setSmallIcon(R.drawable.ic_download_statusbar);
		builder.setLargeIcon(largeIcon);
		nm.notify(ID_DOWNLOAD_STATUS, builder.build());
	}
	
	public static void showDownloadSuccess(Context context, Bitmap largeIcon, String label) {
		String title = String.format(Locale.getDefault(),
				context.getString(R.string.images_download_success), label);
		String text = context.getString(R.string.click_to_view);
		
		NotificationManager nm = (NotificationManager) context.getSystemService(
				Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setAutoCancel(true);
		builder.setContentTitle(title);
		builder.setContentText(text);
		builder.setTicker(title);
		builder.setSmallIcon(R.drawable.ic_download_statusbar);
		builder.setLargeIcon(largeIcon);
		nm.notify(ID_DOWNLOAD_STATUS, builder.build());
	}
	
	public static void cancelDownloadStatus(Context context) {
		NotificationManager nm = (NotificationManager) context.getSystemService(
				Context.NOTIFICATION_SERVICE);
		nm.cancel(ID_DOWNLOAD_STATUS);
	}
	
	private static String buildLabelsText(List<String> labels, String spilit) {
		if (labels == null || labels.size() == 0)
			return "";
		
		StringBuilder builder = new StringBuilder();
		for (String label : labels) {
			if (TextUtils.isEmpty(label))
				continue;
			builder.append(label + spilit);
		}
		
		if (builder.length() == 0)
			return "";

		builder.deleteCharAt(builder.length() - 1);
		return builder.toString();
	}
}

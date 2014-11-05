package cn.garymb.ygomobile.common;

import java.util.Locale;

import cn.garymb.ygomobile.MainActivity;
import cn.garymb.ygomobile.R;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * 管理应用程序的通知栏
 */
public class NotificationMgr {

	private static final int ID_DOWNLOAD_STATUS = 1;
	
	public static void showDownloadStatus(Context context, int count) {
		
		String spilit = context.getString(R.string.image_download_label);
		String title = String.format(Locale.getDefault(), context.getString(R.string.image_is_downloading), count);
		
		Intent intent = new Intent(context, MainActivity.class);
		intent.setAction(Constants.ACTION_VIEW_DOWNLOAD_STATUS);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		NotificationManager nm = (NotificationManager) context.getSystemService(
				Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentIntent(pendingIntent);
		builder.setContentTitle(spilit);
		builder.setContentText(title);
		builder.setTicker(title);
		builder.setAutoCancel(false);
		builder.setOngoing(true);
		builder.setSmallIcon(R.drawable.ic_download_statusbar);
		nm.notify(ID_DOWNLOAD_STATUS, builder.build());
	}
	
	public static void showDownloadSuccess(Context context) {
		String title = String.format(Locale.getDefault(),
				context.getString(R.string.images_download_success));
		
		NotificationManager nm = (NotificationManager) context.getSystemService(
				Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setAutoCancel(true);
		builder.setContentTitle(title);
		builder.setTicker(title);
		builder.setSmallIcon(R.drawable.ic_download_statusbar);
		nm.notify(ID_DOWNLOAD_STATUS, builder.build());
	}
	
	public static void cancelDownloadStatus(Context context) {
		NotificationManager nm = (NotificationManager) context.getSystemService(
				Context.NOTIFICATION_SERVICE);
		nm.cancel(ID_DOWNLOAD_STATUS);
	}
}

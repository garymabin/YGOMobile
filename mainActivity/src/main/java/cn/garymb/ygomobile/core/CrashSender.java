/*
 * CrashSender.java
 *
 *  Created on: 2014年3月18日
 *      Author: mabin
 */
package cn.garymb.ygomobile.core;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import android.content.Context;
import android.content.Intent;

/**
 * @author mabin
 * 
 */
public class CrashSender implements ReportSender  {
	
	private final Context mContext;

	/**
	 * @param ctx
	 */
	public CrashSender(Context ctx) {
		mContext = ctx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.acra.sender.EmailIntentSender#send(org.acra.collector.CrashReportData
	 * )
	 */
	@Override
	public void send(CrashReportData errorContent) throws ReportSenderException {
		final String subject = mContext.getPackageName() + " Crash Report" + errorContent.get(ReportField.USER_CRASH_DATE);
		final String body = buildBody(errorContent);

		final Intent emailIntent = new Intent(
				android.content.Intent.ACTION_SEND);
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		emailIntent.setType("text/plain");
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				new String[] { ACRA.getConfig().mailTo() });
		mContext.startActivity(emailIntent);
	}

	private String buildBody(CrashReportData errorContent) {
		ReportField[] fields = ACRA.getConfig().customReportContent();
		if (fields.length == 0) {
			fields = ACRAConstants.DEFAULT_MAIL_REPORT_FIELDS;
		}

		final StringBuilder builder = new StringBuilder();
		for (ReportField field : fields) {
			builder.append(field.toString()).append("=");
			builder.append(errorContent.get(field));
			builder.append('\n');
		}
		return builder.toString();
	}

}

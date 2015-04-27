package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.model.data.DownloadProgressEvent;
import cn.garymb.ygomobile.utils.FileOpsUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressUpdateDialogController extends BaseDialogConfigController {

	private TextView mProgressPercentText;
	private TextView mProgressText;
	private ProgressBar mProgressBar;

	public ProgressUpdateDialogController(DialogConfigUIBase configUI, View view) {
		super(configUI, view);
		mProgressText = (TextView) view.findViewById(R.id.dl_progress_text);
		mProgressPercentText = (TextView) view
				.findViewById(R.id.dl_progress_percent_text);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar2);
	}

	public void onEvent(DownloadProgressEvent event) {
		final long totalSize = event.getTotalSize();
		final long currentSize = event.getCurrentSize();
		mProgressText.setText(FileOpsUtils.formatReadableFileSize(event
				.getCurrentSize())
				+ "/"
				+ FileOpsUtils.formatReadableFileSize(event.getTotalSize()));
		float progress = (float) ((currentSize * 100.0f) / (totalSize * 1.0f));
		mProgressBar.setProgress((int) progress);
		mProgressPercentText.setText(String.format("%2.1f%%", progress));
	}

}

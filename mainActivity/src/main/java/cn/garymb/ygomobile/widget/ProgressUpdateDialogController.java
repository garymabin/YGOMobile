package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.core.BaseTask;
import cn.garymb.ygomobile.core.SimpleDownloadTask;
import cn.garymb.ygomobile.model.data.DownloadProgressEvent;
import cn.garymb.ygomobile.utils.FileOpsUtils;

import java.lang.ref.WeakReference;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressUpdateDialogController extends BaseDialogConfigController {

	private TextView mProgressPercentText;
	private TextView mProgressText;
	private ProgressBar mProgressBar;

	private WeakReference<BaseTask> mAttachedTask;

	public ProgressUpdateDialogController(DialogConfigUIBase configUI, View view) {
		super(configUI, view);
		mProgressText = (TextView) view.findViewById(R.id.dl_progress_text);
		mProgressPercentText = (TextView) view.findViewById(R.id.dl_progress_percent_text);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar2);
		configUI.setTitle(R.string.font_downloading);
		view.findViewById(R.id.download_stop_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAttachedTask != null) {
					BaseTask t = mAttachedTask.get();
					if (t != null) {
						t.purge();
					}
				}
			}
		});
	}

	public void onEventMainThread(DownloadProgressEvent event) {
		Log.d("test", "onEventMainThread");
		final long totalSize = event.getTotalSize();
		final long currentSize = event.getCurrentSize();
		mProgressText.setText(FileOpsUtils.formatReadableFileSize(event.getCurrentSize()) + "/"
				+ FileOpsUtils.formatReadableFileSize(event.getTotalSize()));
		float progress = (float) ((currentSize * 100.0f) / (totalSize * 1.0f));
		mProgressBar.setProgress((int) progress);
		mProgressPercentText.setText(String.format("%2.1f%%", progress));
	}

	public void setCurrentTask(SimpleDownloadTask task) {
		mAttachedTask = new WeakReference<BaseTask>(task);
	}

}

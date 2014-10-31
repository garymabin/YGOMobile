package cn.garymb.ygomobile.fragment;

import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.core.Controller;
import cn.garymb.ygomobile.data.wrapper.IBaseWrapper;

import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.ISimpleDialogListener;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

public class ImageDLStatusDlgFragment extends SimpleDialogFragment implements
		Observer, OnClickListener {

	private ImageView mDLStopButton;

	private TextView mProgressPercentView;

	private TextView mProgressView;

	private ProgressBar mProgressBar;

	@Override
	public void onPause() {
		super.onPause();
		Controller.peekInstance().registerForImageDownload(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		Controller.peekInstance().unregisterForImageDownload(this);
	}

	@SuppressLint("InflateParams")
	@Override
	public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
		View view = LayoutInflater.from(getActivity()).inflate(
				R.layout.image_dl_dialog, null);
		mDLStopButton = (ImageView) view
				.findViewById(R.id.download_stop_button);
		mProgressPercentView = (TextView) view
				.findViewById(R.id.dl_progress_percent_text);
		mProgressView = (TextView) view.findViewById(R.id.dl_progress_text);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar2);
		mDLStopButton.setOnClickListener(this);

		builder.setTitle(R.string.image_download_label);
		builder.setView(view);
		builder.setPositiveButton(R.string.button_hide,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ISimpleDialogListener listener = getDialogListener();
						if (listener != null) {
							listener.onPositiveButtonClicked(0);
						}
						dismiss();
					}
				});
		builder.setNegativeButton(R.string.button_stop,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ISimpleDialogListener listener = getDialogListener();
						if (listener != null) {
							listener.onNegativeButtonClicked(0);
						}
						dismiss();
					}
				});
		return builder;
	}

	@Override
	public void update(Observable observable, Object data) {
		if (data instanceof Message) {
			Message msg = (Message) data;
			int status = msg.arg2;
			if (status == IBaseWrapper.TASK_STATUS_SUCCESS) {
				Bundle param = (Bundle) msg.obj;
				int count = param.getInt("total_count");
				int current = param.getInt("current_count");
				float progress = (float) ((current * 100.0f) / (count * 1.0));
				mProgressBar.setProgress((int) progress);
				mProgressPercentView.setText(String.format("%2f%", progress));
				mProgressView.setText(getResources().getString(
						R.string.image_count_progress, current, count));
			} else {
				mProgressPercentView.setText(R.string.image_dl_failed);
			}
		}
	}

	@Override
	public void onClick(View v) {
		ISimpleDialogListener listener = getDialogListener();
		if (listener != null) {
			listener.onNegativeButtonClicked(0);
		}
		dismiss();
	}

}

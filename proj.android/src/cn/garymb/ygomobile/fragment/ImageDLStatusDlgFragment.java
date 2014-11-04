package cn.garymb.ygomobile.fragment;

import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.core.Controller;

import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.ISimpleDialogListener;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

public class ImageDLStatusDlgFragment extends SimpleDialogFragment implements
		Observer, OnClickListener {

	private ImageView mDLStopButton;

	private TextView mProgressPercentView;

	private TextView mProgressView;

	private ProgressBar mProgressBar;

	public static ImageDLStatusDlgFragment newInstance(Bundle bundle,
			int requestCode) {
		ImageDLStatusDlgFragment f = new ImageDLStatusDlgFragment();
		f.setArguments(bundle);
		f.mRequestCode = requestCode;
		return f;
	}
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dlg = super.onCreateDialog(savedInstanceState);
		dlg.setCancelable(false);
		return dlg;
	}
	
	
	@Override
	public void onPause() {
		super.onPause();
		Controller.peekInstance().unregisterForImageDownload(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		Controller.peekInstance().registerForImageDownload(this);
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
							listener.onPositiveButtonClicked(mRequestCode);
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
							listener.onNegativeButtonClicked(mRequestCode);
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
			int count = msg.arg2;
			int current = msg.arg1;
			float progress = (float) ((current * 100.0f) / (count * 1.0));
			mProgressBar.setProgress((int) progress);
			mProgressPercentView.setText(String.format("%2.1f%%", progress));
			mProgressView.setText(getResources().getString(
					R.string.image_count_progress, current, count));
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

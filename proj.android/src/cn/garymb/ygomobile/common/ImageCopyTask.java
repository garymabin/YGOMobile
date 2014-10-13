package cn.garymb.ygomobile.common;

import java.io.File;
import java.io.IOException;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.model.Model;
import cn.garymb.ygomobile.utils.FileOpsUtils;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

public class ImageCopyTask extends AsyncTask<Bundle, Void, Bundle> {
	
	public interface ImageCopyListener {
		void onImageCopyFinished(Bundle result);
	}

	private ProgressDialog mWaitDialog;
	
	private ImageCopyListener mListener;
	
	public ImageCopyTask(Context context) {
		mWaitDialog = new ProgressDialog(context);
		mWaitDialog.setMessage(context.getResources().getString(R.string.copying_image));
		mWaitDialog.setCancelable(false);
	}
	
	public void setImageCopyListener(ImageCopyListener listener) {
		mListener = listener;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mWaitDialog.show();
	}

	@Override
	protected Bundle doInBackground(Bundle... params) {
		try {
			String src = params[0].getString("src_url");
			String dst = params[0].getString("url");
			FileOpsUtils.copyFileUsingFileChannels(new File(src), new File(dst));
			Model.peekInstance().removeBitmap(dst, Constants.IMAGE_TYPE_ORIGINAL);
		} catch (IOException e) {
			e.printStackTrace();
			params[0] = null;
		}
		return params[0];
	}
	
	@Override
	protected void onPostExecute(Bundle result) {
		mWaitDialog.dismiss();
		mWaitDialog = null;
		if (mListener != null && result != null) {
			mListener.onImageCopyFinished(result);
		}
		mListener = null;
	}
}

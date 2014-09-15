package cn.garymb.ygomobile.common;

import java.io.File;
import java.io.IOException;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.model.Model;
import cn.garymb.ygomobile.utils.BitmapUtils;
import cn.garymb.ygomobile.utils.FileOpsUtils;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

public class ImageCopyTask extends AsyncTask<String, Void, Bundle> {
	
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
	protected Bundle doInBackground(String... params) {
		Bundle bundle = null;
		try {
			bundle = new Bundle();
			FileOpsUtils.copyFileUsingFileChannels(new File(params[0]),
					new File(params[1]));
			Model.peekInstance().removeBitmap(params[1], Constants.IMAGE_TYPE_ORIGINAL);
			bundle.putString("url", params[1]);
			bundle.putIntArray("orig_size", BitmapUtils.decodeImageSize(params[1]));
		} catch (IOException e) {
			e.printStackTrace();
			bundle = null;
		}
		return bundle;
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

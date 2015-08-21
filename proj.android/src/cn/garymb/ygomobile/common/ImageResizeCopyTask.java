package cn.garymb.ygomobile.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.model.Model;
import cn.garymb.ygomobile.utils.FileOpsUtils;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import android.os.AsyncTask;
import android.os.Bundle;

public class ImageResizeCopyTask extends AsyncTask<Bundle, Void, Bundle> {
	
	public interface ImageCopyListener {
		void onImageCopyFinished(Bundle result);
	}

	private ProgressDialog mWaitDialog;
	
	private ImageCopyListener mListener;
	
	public ImageResizeCopyTask(Context context) {
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
			boolean isResizeNeeded = params[0].getBoolean("force_resize", false);
			if (isResizeNeeded) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true; 
				BitmapFactory.decodeFile(src, options);
				int[] targetSize = params[0].getIntArray("orig_size");
				float xScale = targetSize[0] * 1.0f / options.outWidth;
				float yScale = targetSize[1] * 1.0f / options.outHeight;
				if (Math.abs(yScale - 1.0f) > 1e-6 && Math.abs(xScale - 1.0f) > 1e-6 ) {
					options.inJustDecodeBounds = false;
					Bitmap b = BitmapFactory.decodeFile(src, options);
					Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, targetSize[0], targetSize[1], false);
					scaledBitmap.compress(CompressFormat.JPEG, 100, new FileOutputStream(dst));
				} else {
					FileOpsUtils.copyFileUsingFileChannels(new File(src), new File(dst));
				}
			} else {
				FileOpsUtils.copyFileUsingFileChannels(new File(src), new File(dst));
			}
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

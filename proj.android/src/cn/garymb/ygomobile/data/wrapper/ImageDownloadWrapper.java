package cn.garymb.ygomobile.data.wrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import android.content.ContentValues;

import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.model.data.ImageItem;
import cn.garymb.ygomobile.model.data.ImageItemInfoHelper;
import cn.garymb.ygomobile.provider.YGOImages.Images;
import cn.garymb.ygomobile.provider.YGOImagesDataBaseHelper;

public class ImageDownloadWrapper extends BaseDataWrapper {

	private WeakReference<YGOImagesDataBaseHelper> mHelperRef;
	private ImageItem mItem;

	public ImageDownloadWrapper(int requestType, ImageItem item) {
		super(requestType);
		mItem = item;
	}
	
	public void setDataBaseHelper(YGOImagesDataBaseHelper helper) {
		mHelperRef = new WeakReference<YGOImagesDataBaseHelper>(helper);
	}

	@Override
	public int parse(InputStream in) {
		int result = TASK_STATUS_SUCCESS;
		FileOutputStream fos = null;
		File destFile = new File(ImageItemInfoHelper.getImagePath(mItem));
		File tmpFile = new File(destFile + "tmp");
		try {
			fos = new FileOutputStream(tmpFile);
			int readCount = 0;
			byte[] buffer = new byte[Constants.IO_BUFFER_SIZE];
			while (!Thread.currentThread().isInterrupted()
					&& (readCount = in.read(buffer)) != -1) {
				fos.write(buffer, 0, readCount);
			}
			fos.flush();
			if (Thread.currentThread().isInterrupted()) {
				if (tmpFile != null) {
					tmpFile.delete();
				}
				throw new InterruptedException();
			}
		} catch (IOException e) {
			result = TASK_STATUS_FAILED;
		} catch (InterruptedException e1) {
			result = TASK_STATUS_CANCELED;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// just ignore
				}
			}
		}
		if (tmpFile != null) {
			if (result == TASK_STATUS_SUCCESS) {
				tmpFile.renameTo(destFile);
				YGOImagesDataBaseHelper helper = mHelperRef.get();
				if (helper != null) {
					synchronized (helper) {
						ContentValues values = new ContentValues();
						values.put(Images.STATUS, result);
						helper.update(YGOImagesDataBaseHelper.TABLE_IMAGES,
								values, Images._ID + "=?",
								new String[] { mItem.id });
					}
				}
			} else {
				tmpFile.delete();
			}
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		return mItem.hashCode();
	}
}

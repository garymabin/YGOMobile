package cn.garymb.ygomobile.data.wrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.model.data.ImageItem;
import cn.garymb.ygomobile.model.data.ImageItemInfoHelper;

public class ImageDownloadWrapper extends BaseDataWrapper {

	private ImageItem mItem;

	public ImageDownloadWrapper(int requestType, ImageItem item) {
		super(requestType);
		mItem = item;
		mUrls.add(ImageItemInfoHelper.getImageUrl(item));
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

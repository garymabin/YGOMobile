package cn.garymb.ygomobile.data.wrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.model.data.ImageItem;
import cn.garymb.ygomobile.model.data.ImageItemInfoHelper;

public class ImageDownloadJob extends BaseRequestJob {

	private ImageItem mItem;

	public ImageDownloadJob(int requestType, ImageItem item) {
		super();
		mItem = item;
		mUrls.add(ImageItemInfoHelper.getImageUrl(item));
	}

	@Override
	public int parse(Object in) {
		int result = STATUS_FAILED;
		if (in instanceof InputStream) {
			result = STATUS_SUCCESS;
			FileOutputStream fos = null;
			File destFile = new File(ImageItemInfoHelper.getImagePath(mItem));
			File tmpFile = new File(destFile + "tmp");
			try {
				fos = new FileOutputStream(tmpFile);
				int readCount = 0;

				byte[] buffer = new byte[Constants.IO_BUFFER_SIZE];
				while (!Thread.currentThread().isInterrupted()
						&& (readCount = ((InputStream) in).read(buffer)) != -1) {
					fos.write(buffer, 0, readCount);
					fos.flush();
				}
				if (Thread.currentThread().isInterrupted()) {
					if (tmpFile != null) {
						tmpFile.delete();
					}
					throw new InterruptedException();
				}
			} catch (IOException e) {
				result = STATUS_FAILED;
			} catch (InterruptedException e1) {
				result = STATUS_CANCELED;
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						// just ignore
					}
				}
				if (tmpFile != null) {
					if (result == STATUS_SUCCESS) {
						tmpFile.renameTo(destFile);
					}
					tmpFile.delete();
				}
			}
		}
		setResult(result);
		return result;
	}

	@Override
	public int hashCode() {
		return mItem.hashCode();
	}

	public ImageItem getImageItem() {
		return mItem;
	}
}

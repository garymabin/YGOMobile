package cn.garymb.ygomobile.model.data;

public class DownloadProgressEvent {
	
	private long mTotalSize = 0;
	
	private long mCurrentSize = 0;
	
	public DownloadProgressEvent(long totalSize, long currentSize) {
		this.setTotalSize(totalSize);
		this.setCurrentSize(currentSize);
	}

	public long getTotalSize() {
		return mTotalSize;
	}

	private void setTotalSize(long mTotalSize) {
		this.mTotalSize = mTotalSize;
	}

	public long getCurrentSize() {
		return mCurrentSize;
	}

	private void setCurrentSize(long mCurrentSize) {
		this.mCurrentSize = mCurrentSize;
	}

}

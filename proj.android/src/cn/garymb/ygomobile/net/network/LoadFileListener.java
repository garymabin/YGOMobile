package cn.garymb.ygomobile.net.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.os.Looper;
import android.os.Message;

class LoadFileListener extends LoadListener {
	// 文件保存的路径
	private String mSavePath;
	// 写文件用的OutputStream
	private PrintStream mOuputStream;
	private RandomAccessFile mFile;
	private long mReciveLen;

	// =========================================================================
	// Public functions
	// =========================================================================

	public static LoadFileListener getFileLoadListener(Context context, String url, String savePath,
			boolean synchronous, HttpTaskListener listener, int taskid, Looper looper) {
		return new LoadFileListener(context, url, savePath, synchronous, listener, taskid, looper);
	}

	LoadFileListener(Context context, String url, String savePath, boolean synchronous,
			HttpTaskListener listener, int taskid, Looper looper) {
		super(context, url, synchronous, listener, taskid, looper);
		mSavePath = savePath;
	}

	@Override
	protected void handleData(Message msg) {
		int totalLen = msg.arg2;

		if (totalLen != mContentLength && mContentLength > 0)
			totalLen = (int) mContentLength;

		if ((mStatusCode >= 301 && mStatusCode <= 303) || mStatusCode == 307) {
			// 重定向
			return;
		} else if (mStatusCode >= 400) {
			// Error
			return;
		}
		
		if (mOuputStream != null) {
			ByteArrayBuffer buffer = (ByteArrayBuffer) msg.obj;
			try {
				mFile.seek(mReciveLen);
				mOuputStream.write(buffer.buffer(), 0, buffer.length());
				if (mOuputStream.checkError()) {
					onWriteError();
					return;
				}
				mReciveLen += buffer.length();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (mTaskListener != null) {
			HttpTaskEventArg arg = new HttpTaskEventArg();
			arg.mlen = (int) mReciveLen;
			arg.mTotal = totalLen;
			arg.buffer = null;
			mTaskListener.onHttpTaskEvent(m_taskid, HttpTaskListener.HTTPTASK_EVENT_DATARECIVE, arg);
		}
	}

	@Override
	protected void handleHeaders(Headers headers) {
		super.handleHeaders(headers);
		if (mCancelled)
			return;

		if (mStatusCode == 206) {
			if (mReciveLen != mRangeStart) {
				// Log.e("Game", "Last range must be error");
				mReciveLen = mRangeStart;
			}
		}
	}

	// Handle the status callback on the WebCore thread.
	@Override
	protected void handleStatus(int major, int minor, int code, String reason) {
		super.handleStatus(major, minor, code, reason);
		if (mCancelled)
			return;

		if (mStatusCode == 206 && mReciveLen > 0) {
			return;
		}

		File file = new File(mSavePath);
		try {
			if (!file.exists())
				file.createNewFile();
			mFile = new RandomAccessFile(file, "rw");

			// Attach a print stream to output stream.
			mOuputStream = new PrintStream(new FileOutputStream(mFile.getFD()));
			mReciveLen = 0;
		} catch (IOException e) {
			failOpenFile();
		}
	}

	@Override
	protected void handleEndData() {
		if (mStatusCode != 206 || mReciveLen >= mContentLength) {
			if (mOuputStream != null) {
				mOuputStream.flush();
				mOuputStream.close();
				try {
					mFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		super.handleEndData();
	}

	@Override
	protected boolean handlePartialContent() {
		if (mReciveLen < mContentLength) {
			Network network = Network.getInstance(getContext());
			long begin = mReciveLen;
			String range = "bytes=" + begin + "-";
			mRequestHeaders.put("Range", range);
			if (network.requestURL(mMethod, mRequestHeaders, mPostData, this)) {
				return true;
			} else {
				return false;
			}
		} else if (mContentLength == mReciveLen) {
			mTaskListener.onHttpTaskEvent(m_taskid, HttpTaskListener.HTTPTASK_EVENT_END, null);
			return true;
		} else {
			return false;
		}
	}

	private void failOpenFile() {
		cancel();

		HttpTaskEventArg arg = new HttpTaskEventArg();
		arg.mErrorId = HttpTaskListener.FILE_ERROR;
		mTaskListener.onHttpTaskEvent(m_taskid, HttpTaskListener.HTTPTASK_EVENT_FAIL, arg);
		return;
	}
	
	private void onWriteError() {
		cancel();
		
		HttpTaskEventArg arg = new HttpTaskEventArg();
		arg.mErrorId = ERROR_IO;
		mTaskListener.onHttpTaskEvent(m_taskid, HttpTaskListener.HTTPTASK_EVENT_FAIL, arg);
	}
}
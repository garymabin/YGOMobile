package cn.garymb.ygomobile.core;

import android.os.Message;

import com.squareup.okhttp.OkHttpClient;

import cn.garymb.ygomobile.data.wrapper.BaseRequestJob;

public class SimpleDownloadTask extends BaseTask {
	
	protected IBaseThread mWorkThread;
	private OkHttpClient mOkHttpClient;
	
	public SimpleDownloadTask(OkHttpClient client) {
		mOkHttpClient = client;
	}
	

	@Override
	public void addJob(BaseRequestJob wrapper) {
		if (mWorkThread == null || !mWorkThread.isRunning()) {
			mWorkThread = new SimpleDownloadThread(this, mOkHttpClient);
			((DefaultWorkThread<?>) mWorkThread).setWrapper(wrapper);
		}
	}

	@Override
	public void purge() {
		if (mWorkThread != null) {
			mWorkThread.terminate();
		}
	}

	@Override
	public int getType() {
		return TASK_TYPE_DOWNLOAD;
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public int getJobCount() {
		return 1;
	}

	@Override
	public void execute() {
		if (mWorkThread != null) {
			mWorkThread.start();
		}
	}

	@Override
	public void onJobFinish(BaseRequestJob wrapper) {
		Message msg = wrapper.getParam();
		if (msg != null) {
			msg.arg1 = wrapper.getResult();
			msg.sendToTarget();
		}
		mTaskCallback.onTaskFinish(getType(), wrapper.getResult());
	}

	@Override
	public void onJobContinue(BaseRequestJob wrapper) {
	}

}

package cn.garymb.ygomobile.core;

import android.os.Message;

import com.squareup.okhttp.OkHttpClient;

import cn.garymb.ygomobile.data.wrapper.BaseRequestJob;

public class MyCardAPITask extends BaseTask {
	
	protected IBaseThread mWorkThread;
	private OkHttpClient mOkHttpClient;

	public MyCardAPITask(OkHttpClient okClient) {
		mOkHttpClient = okClient;
	}

	@Override
	public void addJob(BaseRequestJob wrapper) {
		if (mWorkThread == null || !mWorkThread.isRunning()) {
			mWorkThread = new MyCardAPIThead(this, mOkHttpClient);
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
		return TASK_TYPE_MYCARD_API;
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
	public void onJobFinish(BaseRequestJob wrapper) {
		Message msg = wrapper.getParam();
		msg.arg1 = wrapper.getResult();
		msg.sendToTarget();
		mTaskCallback.onTaskFinish(TASK_TYPE_MYCARD_API, wrapper.getResult());
	}

	@Override
	public void onJobContinue(BaseRequestJob wrapper) {
	}

	@Override
	public void execute() {
		if (mWorkThread != null) {
			mWorkThread.start();
		}
	}

}

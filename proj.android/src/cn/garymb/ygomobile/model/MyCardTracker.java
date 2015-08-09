package cn.garymb.ygomobile.model;

import com.squareup.okhttp.OkHttpClient;

import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.core.BaseTask;
import cn.garymb.ygomobile.core.IBaseTask;
import cn.garymb.ygomobile.core.ImageDownloadTask;
import cn.garymb.ygomobile.core.MyCardAPITask;
import cn.garymb.ygomobile.core.BaseTask.TaskStatusCallback;
import cn.garymb.ygomobile.data.wrapper.BaseRequestJob;
import cn.garymb.ygomobile.data.wrapper.CardImageUrlWrapper;
import cn.garymb.ygomobile.data.wrapper.IBaseJob;
import cn.garymb.ygomobile.model.data.CardImageUrlInfo;
import cn.garymb.ygomobile.model.data.DataStore;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.SparseArrayCompat;

public class MyCardTracker extends Handler implements TaskStatusCallback {

	public static final int STATE_INITIAL = 0;
	public static final int STATE_PREPARED = 2;
	public static final int STATE_FAILED = 3;

	private static final int INTERNAL_MESSAGE_TYPE_TASK_FINISH = 0x10;
	private static final int INTERNAL_MESSAGE_TYPE_CARD_IMAGE_URL = 0x11;
	
	
	private SparseArrayCompat<BaseTask> mTasks;
	private StaticApplication mApp;

	private int mState;
	private DataStore mStore;
	private OkHttpClient mOkHttpClient;


	public MyCardTracker(StaticApplication app, DataStore store) {
		mApp = app;
		mOkHttpClient = mApp.getOkHttpClient();
		mTasks = new SparseArrayCompat<BaseTask>();
		mStore = store;
		
		setState(STATE_PREPARED);
	}

	private void setState(int state) {
		if (mState != state) {
			mState = state;
		}
	}

	public BaseTask newTask(int type, Handler target) {
		BaseTask task = null;
		switch (type) {
		case IBaseTask.TASK_TYPE_MYCARD_API:
			task = new MyCardAPITask(mOkHttpClient);
			break;
		case IBaseTask.TASK_TYPE_IMAGE_DOWNLOAD:
			task = new ImageDownloadTask(mApp, target);
			break;
		default:
			break;
		}
		mTasks.put(task.getType(), task);
		return task;
	}

	public void cleanupTask(int type) {
		if (type == IBaseTask.TASK_TYPE_NONE) {
			return;
		}
		IBaseTask task = mTasks.get(type);
		if (task != null) {
			task.purge();
		}
		mTasks.remove(type);
	}

	public BaseTask getTask(int type) {
		if (type == IBaseTask.TASK_TYPE_NONE) {
			return null;
		}
		return mTasks.get(type);
	}

	public void execute(int type) {
		if (mState == STATE_INITIAL || mState == STATE_FAILED) {
			BaseTask task = newTask(IBaseTask.TASK_TYPE_MYCARD_API, this);
			BaseRequestJob job = new CardImageUrlWrapper(0);
			Message msg =Message.obtain(this,
					INTERNAL_MESSAGE_TYPE_CARD_IMAGE_URL);
			msg.setData(new Bundle());
			job.setParam(msg);
			task.addJob(job);
			task.setNextTask(type);
			task.setTaskStatusCallback(this);
			task.execute();
			setState(STATE_PREPARED);
			return;
		} else if (mState == STATE_PREPARED) {
			BaseTask task = getTask(type);
			if (task != null) {
				task.execute();
			}
		} else {
			// FIXME: if we are now retriving, should add more status to task.
		}
	}

	@Override
	public void handleMessage(Message msg) {
		if (msg.what == INTERNAL_MESSAGE_TYPE_TASK_FINISH) {
			int type = msg.arg1;
			int result = msg.arg2;
			if (result == IBaseJob.STATUS_SUCCESS) {
				if (type == IBaseTask.TASK_TYPE_MYCARD_API) {
					setState(STATE_PREPARED);
				}
				BaseTask task = getTask(type);
				if (task != null) {
					int nextTaskType = task.getNextTask();
					BaseTask nextTask = getTask(nextTaskType);
					if (nextTask != null) {
						nextTask.execute();
					}
					task.purge();
				}
			}
		} else if (msg.what == INTERNAL_MESSAGE_TYPE_CARD_IMAGE_URL) {
			if (msg.arg1 == IBaseJob.STATUS_SUCCESS) {
				CardImageUrlInfo info = msg.peekData().getParcelable(
						Constants.REQUEST_RESULT_KEY_CARDIMAGE_URL);
				mStore.updateCardImageURL(info);
			}
		}
	}

	@Override
	public void onTaskFinish(int type, int result) {
		sendMessage(Message.obtain(null, INTERNAL_MESSAGE_TYPE_TASK_FINISH,
				type, result));
	}
}

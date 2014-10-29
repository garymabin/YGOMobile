package cn.garymb.ygomobile.core;

import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;

import android.os.Message;
import android.support.v4.util.SparseArrayCompat;

public class UpdateController implements TaskStatusCallback {

	private static final int UPDATE_TYPE_CHECK_UPDATE = 0x0;
	private static final int UPDATE_MAX_TYPE = UPDATE_TYPE_CHECK_UPDATE + 1;

	private SparseArrayCompat<Message> mUpdateMessages;

	private IBaseConnection mInstantConnection;

	public UpdateController(StaticApplication app) {
		mUpdateMessages = new SparseArrayCompat<Message>(UPDATE_MAX_TYPE);
		mInstantConnection = new InstantConnection(app, this);
	}

	@Override
	public void onTaskFinish(BaseDataWrapper wrapper) {
		int key = -1;
		Message msg = mUpdateMessages.get(key);
		if (msg != null) {
			msg.sendToTarget();
			mUpdateMessages.remove(key);
		}
	}

	@Override
	public void onTaskContinue(BaseDataWrapper wrapper) {
	}

}

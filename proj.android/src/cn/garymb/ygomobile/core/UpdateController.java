package cn.garymb.ygomobile.core;



import cn.garymb.ygomobie.model.Model;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.core.IBaseConnection.TaskStatusCallback;
import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;

import android.os.Message;
import android.support.v4.util.SparseArrayCompat;

public class UpdateController implements TaskStatusCallback {
	
	public static boolean isServerUpdated; 
	
	private static final int UPDATE_TYPE_SERVER_LIST = 0x0;
	
	private static final int UPDATE_TYPE_ROOM_LIST = 0x1;
	
	private static final int UPDATE_TYPE_LOGIN = 0x2;
	
	private static final int UPDATE_MAX_TYPE = UPDATE_TYPE_LOGIN + 1;
	
	private Model mModel;
	
	private SparseArrayCompat<Message> mUpdateMessages;
	
	private IBaseConnection mInstantConnection;
	
	public UpdateController(StaticApplication app) {
		mModel = Model.peekInstance();
		mUpdateMessages = new SparseArrayCompat<Message>(UPDATE_MAX_TYPE);
		mInstantConnection = new InstantConnection(app, this);
	}
	
	@Override
	public void onTaskFinish(BaseDataWrapper wrapper) {
	}

	@Override
	public void onTaskContinue(BaseDataWrapper wrapper) {
	}

}

package cn.garymb.ygomobile.core;

import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.data.wrapper.BaseRequestJob;
import cn.garymb.ygomobile.data.wrapper.IBaseJob;
import cn.garymb.ygomobile.data.wrapper.IBaseJob.JobStatusCallback;
import cn.garymb.ygomobile.data.wrapper.RoomRequestJob;
import cn.garymb.ygomobile.data.wrapper.ServerRequestJob;
import cn.garymb.ygomobile.model.Model;

import android.content.Context;
import android.os.Message;
import android.support.v4.util.SparseArrayCompat;

public class UpdateController implements JobStatusCallback {

	public static boolean isServerUpdated; 
	
	private static final int UPDATE_TYPE_SERVER_LIST = 0x0;
	
	private static final int UPDATE_TYPE_ROOM_LIST = 0x1;
	
	private static final int UPDATE_TYPE_LOGIN = 0x2;
	
	private static final int UPDATE_MAX_TYPE = UPDATE_TYPE_LOGIN + 1;
	
	private Model mModel;
	
	private SparseArrayCompat<Message> mUpdateMessages;
	
	private IBaseTask mMoeConnection;
	
	private Context mContext;
	
	public UpdateController(StaticApplication app) {
		mContext = app;
		mModel = Model.peekInstance();
		mUpdateMessages = new SparseArrayCompat<Message>(UPDATE_MAX_TYPE);
		mMoeConnection = new WebSocketConnection(this);
	}
	
//	/* package */ void asyncUpdateMycardServer(Message msg) {
//		mUpdateMessages.put(UPDATE_TYPE_SERVER_LIST, msg);
//		ServerRequestJob wrapper = new ServerRequestJob(Constants.REQUEST_TYPE_UPDATE_SERVER);
//		mInstantConnection.addTask(wrapper);
//	}
	
	/* package */ void asyncUpdateRoomList(Message msg) {
		mUpdateMessages.put(UPDATE_TYPE_ROOM_LIST, msg);
		RoomRequestJob wrapper = new RoomRequestJob();
		mMoeConnection.addJob(wrapper);
	}
	
	/* package */ void stopUpdateRoomList() {
		// TODO Auto-generated method stub
		mMoeConnection.purge();
	}



	@Override
	public void onJobContinue(BaseRequestJob wrapper) {
		int key = -1;
		if (wrapper instanceof ServerRequestJob) {
			key = UPDATE_TYPE_SERVER_LIST;
		} else if (wrapper instanceof RoomRequestJob) {
			key = UPDATE_TYPE_ROOM_LIST;
		}
		Message msg = mUpdateMessages.get(key);
		if (msg != null) {
			if (wrapper.getResult() == IBaseJob.STATUS_SUCCESS) {
				mModel.updateData(wrapper);
			}
			msg.arg2 = wrapper.getResult();
			msg.sendToTarget();
			mUpdateMessages.remove(key);
		}

	}

	@Override
	public void onJobFinish(BaseRequestJob wrapper) {
		int key = -1;
		if (wrapper instanceof ServerRequestJob) {
			key = UPDATE_TYPE_SERVER_LIST;
		} else if (wrapper instanceof RoomRequestJob) {
			key = UPDATE_TYPE_ROOM_LIST;
		}
		Message msg = mUpdateMessages.get(key);
		if (msg != null) {
			if (wrapper.getResult() == IBaseJob.STATUS_SUCCESS) {
				mModel.updateData(wrapper);
			}
			Message reply = Message.obtain(msg);
			reply.arg2 = wrapper.getResult();
			reply.sendToTarget();
		}
	}

}

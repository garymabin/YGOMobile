package cn.garymb.ygomobile.core;

import cn.garymb.ygomobile.data.wrapper.BaseRequestJob;
import cn.garymb.ygomobile.data.wrapper.IBaseJob.JobStatusCallback;
import cn.garymb.ygomobile.net.websocket.WebSocketConnector;


public class WebSocketConnection implements IBaseTask {
	
	private IBaseThread mUpdateThread;
	private WebSocketConnector mConnector;
	
	public WebSocketConnection(JobStatusCallback callback) {
		mConnector = new WebSocketConnector();
		mUpdateThread = new WebSocketThread(callback, mConnector);
		mUpdateThread.start();
	}

	@Override
	public void addJob(BaseRequestJob wrapper) {
		((WebSocketThread)mUpdateThread).executeTask(wrapper);
	}

	@Override
	public void purge() {
		mUpdateThread.terminate();
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getJobCount() {
		return 1;
	}

	@Override
	public void execute() {
	}

}

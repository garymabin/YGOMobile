package cn.garymb.ygomobile.core;

import cn.garymb.ygomobie.model.IDataObserver;
import cn.garymb.ygomobie.model.Model;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.actionbar.ActionBarController;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.net.NetworkStatusManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;

public class Controller {
	
	private static Controller INSTANCE;
	
	private UserStatusTracker mLoginStatusTracker;
	
	private UpdateController mUpdateController;
	
	private ActionBarController mActionBarController;
	
	private NetworkStatusManager mNetworkManager;
	
	private Model mModel;
	
	private Controller(StaticApplication app) {
		mModel = Model.peekInstance();
		mUpdateController = new UpdateController(app);
		mActionBarController = new ActionBarController();
		mLoginStatusTracker = new UserStatusTracker(app);
		mNetworkManager = NetworkStatusManager.peekInstance(app);
	}

	public static Controller peekInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Controller(StaticApplication.peekInstance());
		}
		return INSTANCE;
		
	}

	public void asyncUpdateMycardServer(Message msg) {
		mUpdateController.asyncUpdateMycardServer(msg);
	}

	public void asyncLogin(Bundle data) {
		mUpdateController.asyncLogin(Message.obtain(mLoginStatusTracker, Constants.MSG_ID_LOGIN, data));
		String name = data.getString(Constants.BUNDLE_KEY_USER_NAME);
		mLoginStatusTracker.changeLoginStatus(UserStatusTracker.LOGIN_STATUS_LOGGING, true);
		mLoginStatusTracker.setLoginName(name);
	}
	
	public void asyncLogout() {
		//TODO: @zh99998 how to log out.
		mLoginStatusTracker.changeLoginStatus(UserStatusTracker.LOGIN_STATUS_LOG_OUT, true);
	}
	
	public String getLoginName() {
		return mLoginStatusTracker.getLoginName();
	}
	
	public int getLoginStatus() {
		return mLoginStatusTracker.getLoginStatus();
	}
	
	public void registerForLoginStatusChange(Handler h) {
		mLoginStatusTracker.registerForLoginStatusChange(h);
	}
	
	public void unregisterForLoginStatusChange(Handler h) {
		mLoginStatusTracker.unregisterForLoginStatusChange(h);
	}
	
	public boolean handleActionBarEvent(MenuItem item) {
		return mActionBarController.handleAction(item);
	}

	public void registerForActionNew(Handler h) {
		mActionBarController.registerForActionNew(h);
	}

	public void unregisterForActionNew(Handler h) {
		mActionBarController.unregisterForActionNew(h);
	}

	public void registerForActionPlay(Handler h) {
		mActionBarController.registerForActionPlay(h);
	}

	public void unregisterForActionPlay(Handler h) {
		mActionBarController.unregisterForActionPlay(h);
	}
	
	public void registerForActionSearch(Handler h) {
		mActionBarController.registerForActionSearch(h);
	}

	public void unregisterForActionSearch(Handler h) {
		mActionBarController.unregisterForActionSearch(h);
	}
	
	public void registerForActionFilter(Handler h) {
		mActionBarController.registerForActionFilter(h);
	}
	
	public void unregisterForActionFilter(Handler h) {
		mActionBarController.unregisterForActionFilter(h);
	}
	
	public void registerForActionSettings(Handler h) {
		mActionBarController.registerForActionSettings(h);
	}
	
	public void unregisterForActionSettings(Handler h) {
		mActionBarController.unregisterForActionSettings(h);
	}
	
	public void registerForActionSupport(Handler h) {
		mActionBarController.registerForActionSupport(h);
	}
	
	public void unregisterForActionSupport(Handler h) {
		mActionBarController.unregisterForActionSupport(h);
	}
	
	public void registerForActionReset(Handler h) {
		mActionBarController.registerForActionReset(h);
	}
	
	public void unregisterForActionReset(Handler h) {
		mActionBarController.unregisterForActionReset(h);
	}

	/**
	 * 
	 * @return
	**/
	public boolean isWifiConnected() {
		return mNetworkManager.isWifiConnected();
	}

	/**
	 * Create a Message object, in fact, this invoke {@link #buildMessage(int, int, int, Object)}
	 * @param what 
	 * 				type of this message
	 * @return
	 */
	public static Message buildMessage(int what) {
		return buildMessage(what, 0, 0, null);
	}
	
	/**
	 * Create a Message object.
	 * @param what	type of this message
	 * @param arg1	first int arg for this message
	 * @param arg2	second int arg for this message
	 * @param obj	Object arg for this message.<font color=red> If this message need to be 
	 * 			transfered by handler, this Object should be Parcelable
	 * @return
	 */
	public static Message buildMessage(int what, int arg1, int arg2, Object obj) {
		Message msg = new Message();
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		msg.obj = obj;
		return msg;
	}

	public void requestDataOperation(IDataObserver observer, Message msg) {
		mModel.requestDataOperation(observer, msg);
	}
	
	public void registerDataObserver(IDataObserver observer) {
		mModel.registerDataObserver(observer);
	}
	
	public void unregisterDataObserver(IDataObserver observer) {
		mModel.unregisterDataObserver(observer);
	}
}

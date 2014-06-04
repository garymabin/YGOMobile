package cn.garymb.ygomobie.model;

import java.util.HashSet;
import java.util.Set;


import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;
import cn.garymb.ygomobile.model.data.DataStore;
import cn.garymb.ygomobile.model.data.ImageItem;
import cn.garymb.ygomobile.ygo.YGOArrayStore;
import cn.garymb.ygomobile.ygo.YGOServerInfo;

import android.graphics.Bitmap;
import android.os.Message;
import android.util.SparseArray;


public class Model {
	
	private static Model INSTANCE;
	
	private DataStore mDataStore;
	
	private YGOArrayStore mYGOArrayStore;
	
	private ImageModelHelper mImgModelHelper;
	
	private Set<IDataObserver> mObserverList;
	
	private Model(StaticApplication app) {
		mDataStore = new DataStore(app);
		mImgModelHelper = new ImageModelHelper();
		mYGOArrayStore = new YGOArrayStore(app.getResources());
		mObserverList = new HashSet<IDataObserver>();
	}

	public static Model peekInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Model(StaticApplication.peekInstance());
		}
		return INSTANCE;
		
	}

	public SparseArray<YGOServerInfo> getServers() {
		return mDataStore.getServers();
	}
	
	public void addNewServer(YGOServerInfo info) {
		mDataStore.addNewServer(info);
	}

	/*package*/ boolean hasDataObserver(IDataObserver ob) {
		if (mObserverList == null)
			return false;
		
		synchronized (mObserverList) {
			return mObserverList.contains(ob);
		}
	}
	
	public void registerDataObserver(IDataObserver o) {
		synchronized (mObserverList) {
			mObserverList.add(o);
		}
	}

	public void unregisterDataObserver(IDataObserver o) {
		synchronized (mObserverList) {
			mObserverList.remove(o);
			mImgModelHelper.onDataObserverUnregistered(o);
		}
	}
	
	public Bitmap getBitmap(ImageItem item, int type) {
		return mImgModelHelper.getBitmap(item, type);
	}
	
	public String getYGOCardType(int code) {
		return mYGOArrayStore.getCardType(code);
	}
	
	public String getYGOCardRace(int code) {
		return mYGOArrayStore.getCardRace(code);
	}
	
	public String getYGOCardAttr(int code) {
		return mYGOArrayStore.getCardAttr(code);
	}
	
	public String getYGOCardOT(int code) {
		return mYGOArrayStore.getCardOT(code);
	}

	public void requestDataOperation(IDataObserver observer, Message msg) {
		mImgModelHelper.requestDataOperation(observer, msg);
	}

	public void removeServer(int groupId) {
		mDataStore.removeServer(groupId);
	}
}

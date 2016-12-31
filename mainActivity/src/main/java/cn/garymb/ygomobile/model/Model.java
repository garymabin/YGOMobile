package cn.garymb.ygomobile.model;

import java.util.HashSet;
import java.util.Observer;
import java.util.List;
import java.util.Set;


import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.core.IBaseTask;
import cn.garymb.ygomobile.data.wrapper.BaseRequestJob;
import cn.garymb.ygomobile.model.data.DataStore;
import cn.garymb.ygomobile.model.data.ImageItem;
import cn.garymb.ygomobile.provider.YGOImagesDataBaseHelper;
import cn.garymb.ygomobile.ygo.YGOArrayStore;
import cn.garymb.ygomobile.ygo.YGORoomInfo;
import cn.garymb.ygomobile.ygo.YGOServerInfo;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Message;
import android.util.SparseArray;


public class Model {
	
	private static Model INSTANCE;
	
	private DataStore mDataStore;
	
	private YGOArrayStore mYGOArrayStore;
	
	private ImageModelHelper mImgModelHelper;
	
	private YGOImagesDataBaseHelper mImageDataBaseHelper;
	
	private Set<IDataObserver> mObserverList;

	private MyCardTracker mUpdateController;

	private CardImageDownloadManager mImageManager;
	
	private Model(StaticApplication app) {
		mDataStore = new DataStore(app);
		mImgModelHelper = new ImageModelHelper();
		mYGOArrayStore = new YGOArrayStore(app.getResources());
		mObserverList = new HashSet<IDataObserver>();
		mImageDataBaseHelper = new YGOImagesDataBaseHelper(app);
		mUpdateController = new MyCardTracker(app, mDataStore);
		mImageManager = new CardImageDownloadManager(app, mUpdateController);
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
	
	public DataStore getDataStore() {
		return mDataStore;
	}

	/*package*/ boolean hasDataObserver(IDataObserver ob) {
		if (mObserverList == null)
			return false;
		
		synchronized (mObserverList) {
			return mObserverList.contains(ob);
		}
	}
	
	public void registerImageObserver(IDataObserver o) {
		synchronized (mObserverList) {
			mObserverList.add(o);
		}
	}

	public void unregisterImageObserver(IDataObserver o) {
		synchronized (mObserverList) {
			mObserverList.remove(o);
			mImgModelHelper.onDataObserverUnregistered(o);
		}
	}
	
	public YGOImagesDataBaseHelper getImageDataBaseHelper() {
		return mImageDataBaseHelper;
	}
	
	public Bitmap getBitmap(ImageItem item, int type) {
		return mImgModelHelper.getBitmap(item, type);
	}
	
	public void removeBitmap(String id, int type) {
		mImgModelHelper.removeBitmap(id, type);
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
		if (msg.what == Constants.REQUEST_TYPE_DOWNLOAD_IMAGE || msg.what == Constants.REQUEST_TYPE_LOAD_BITMAP) {
			mImgModelHelper.requestDataOperation(observer, msg);
		} else {
		}
	}

	public void removeServer(int groupId) {
		mDataStore.removeServer(groupId);
	}

	public IBaseTask createOrGetDownloadConnection() {
		return mImageManager.createOrGetDownloadConnection();
	}

	public void cleanupDownloadConnection() {
		mImageManager.cleanupDownloadConnection();
	}

	public void setTotalDownloadCount(int count) {
		mImageManager.setTotalDownloadCount(count);
		
	}

	public void exeuteDownload(Context context) {
		mImageManager.exeuteDownload(context);
	}

	public void registerForImageDownload(Observer o) {
		mImageManager.registerForImageDownload(o);
		
	}

	public void unregisterForImageDownload(Observer o) {
		mImageManager.unregisterForImageDownload(o);
	}
	
	public void updateData(BaseRequestJob wrapper) {
		mDataStore.updateData(wrapper);
	}

	public List<YGORoomInfo> getRooms() {
		return mDataStore.getRooms();
	}

	public YGOServerInfo getMyCardServer() {
		return mDataStore.getMyCardServer();
	}

	public void asyncUpdateMycardServer(Message msg) {
	}

	public void asyncUpdateRoomList(Message msg) {
		// TODO Auto-generated method stub
		
	}

	public void stopUpdateRoomList() {
		
	}
}

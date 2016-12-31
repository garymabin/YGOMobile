package cn.garymb.ygomobile.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;


import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.controller.Controller;
import cn.garymb.ygomobile.core.images.BitmapHolder;
import cn.garymb.ygomobile.core.images.ImageFileDownloadManager;
import cn.garymb.ygomobile.core.images.ImageFileDownloadTaskHolder;
import cn.garymb.ygomobile.core.images.ImageFileDownloadedCallback;
import cn.garymb.ygomobile.core.images.ImageLoadManager;
import cn.garymb.ygomobile.core.images.ImageLoadedCallback;
import cn.garymb.ygomobile.model.data.ImageItem;

import android.graphics.Bitmap;
import android.os.Message;
import android.support.v4.util.LruCache;


public class ImageModelHelper {
	
	final static int MAX_OBSERVER_CACHE_SIZE = 200;
	
	LruCache<String, IDataObserver> mObservers = new LruCache<String, IDataObserver>(MAX_OBSERVER_CACHE_SIZE);
	
	private ImageLoadManager mLoadManager = new ImageLoadManager();
	private ImageFileDownloadManager mDownloadManager;
	
	ImageLoadedCallback mLoadCallback = new ImageLoadedCallback() {
		@Override
		public void onImageLoaded(BitmapHolder holder) {
			if (holder == null || !holder.isBitmapLoaded())
				return;
			
			int type = holder.getImageType();
			final String id = holder.getId();
			IDataObserver responser = null;
			synchronized (mObservers) {
				responser = mObservers.remove(id);
			}
			
			if (responser != null) {
				Message msg = Controller.buildMessage(Constants.REQUEST_TYPE_LOAD_BITMAP,
						type, 0, holder);
				responser.notifyDataUpdate(msg);
			}
		}
	};
	
	ImageFileDownloadedCallback mDownloadCallback = new ImageFileDownloadedCallback() {
		@Override
		public void onImageFileDownloaded(ImageFileDownloadTaskHolder holder) {
			if (holder == null)
				return;
			
			IDataObserver observer = mObservers.get(holder.getId());
			if (observer == null || !Model.peekInstance().hasDataObserver(observer))
				return;
			
			Message msg = Controller.buildMessage(Constants.REQUEST_TYPE_DOWNLOAD_IMAGE,
					holder.getImageType(), holder.result(), holder.getImageItem());
			observer.notifyDataUpdate(msg);
		}
	};
	
	
	public ImageModelHelper() {
		mLoadManager.setImageLoadedCallback(mLoadCallback);
		mDownloadManager = new ImageFileDownloadManager();
		mDownloadManager.setDownloadCallback(mDownloadCallback);
	}

	public void requestDataOperation(IDataObserver ob, Message msg) {
		if (msg == null)
			return;
		
		switch (msg.what) {
		case Constants.REQUEST_TYPE_LOAD_BITMAP:
			requestLoadBitmap(ob, msg);
			break;
		case Constants.REQUEST_TYPE_DOWNLOAD_IMAGE:
			requestImageDownload(ob, msg);
			break;
		case Constants.REQUEST_TYPE_RESET_LOAD_QUEUE:
			resetLoadQueue(msg);
			break;
		case Constants.REQUEST_TYPE_CHANGE_IMAGE_LOAD_PRIORITY:
			changeLoadPriority(msg);
			break;
		case Constants.REQUEST_TYPE_RESET_DOWNLOAD_QUEUE:
			resetDownloadQueue(msg);
			break;
		case Constants.REQUEST_TYPE_CLEAR_BITMAP_CACHE:
			resetBitmapCache(msg);
			break;
		}
	}

	private void requestImageDownload(IDataObserver ob, Message msg) {
		if (msg == null || msg.obj == null || !(msg.obj instanceof ImageItem))
			return;

		ImageItem item = (ImageItem) msg.obj;
		ImageFileDownloadTaskHolder holder = mDownloadManager.addDownload(item, 
					msg.arg1, (msg.arg2 == Constants.BITMAP_LOAD_TYPE_PRELOAD));
		
		if (holder == null)
			return;
		
		mObservers.put(holder.getId(), ob);
	}

	/**
	 * @param ob
	 * @param msg
	 */
	private void requestLoadBitmap(IDataObserver ob, Message msg) {
		if (msg == null || msg.obj == null || !(msg.obj instanceof ImageItem))
			return;
		
		ImageItem item = (ImageItem) msg.obj;
		int type = msg.arg1;
		
		BitmapHolder holder = new BitmapHolder(item, type);
		Bitmap bmp = mLoadManager.getBitmap(holder);

		//if hit cache, just notifyDataUpdate and return
		if (bmp != null) {
			holder.setBitmap(bmp);
			msg = Controller.buildMessage(Constants.REQUEST_TYPE_LOAD_BITMAP, type, 0, holder);

			
			if (ob != null && Model.peekInstance().hasDataObserver(ob))
				ob.notifyDataUpdate(msg);
			return;
		}
		
		//reach here, means no cache hit, add the observer to observers map and
		//then add task or pre-load task to load manager.
		synchronized (mObservers) {
			mObservers.put(holder.getId(), ob);
			
			boolean isPreload = (msg.arg2 == Constants.BITMAP_LOAD_TYPE_PRELOAD); 
			mLoadManager.addLoadTask(holder, isPreload);
		}
	}
	
	private void resetLoadQueue(Message msg) {
	}
	
	private void changeLoadPriority(Message msg) {
		if (msg == null)
			return;
		
		mLoadManager.changeLoadPriority(msg.arg1);
	}

	private void resetDownloadQueue(Message msg) {
	}

	/**
	 * 清除指定的Bitmap缓存，具体实现见 {@link ImageLoadManager#resetCache(int)}
	 * @param msg
	 */
	private void resetBitmapCache(Message msg) {
		if (msg == null)
			return;
		
		mLoadManager.resetCache(msg.arg1);
	}

	/* package */ void onDataObserverUnregistered(IDataObserver o) {
		if (o == null)
			return;
		
		LinkedList<String> keys = new LinkedList<String>();
		synchronized (mObservers) {
			Map<String, IDataObserver> map = mObservers.snapshot();
			Iterator<String> it = map.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				if (o.equals(map.get(key)))
					keys.add(key);
			}
			
			for (String key : keys) {
				mObservers.remove(key);
			}
		}
	}

	public Bitmap getBitmap(ImageItem item, int type) {
		BitmapHolder holder = new BitmapHolder(item, type);
		return mLoadManager.getBitmap(holder);
	}
	
	public void removeBitmap(String id, int type) {
		mLoadManager.removeBitmap(id, type);
	}
}

package cn.garymb.ygomobile.core.images;

import java.util.Iterator;
import java.util.LinkedList;

import cn.garymb.ygomobile.model.data.ImageItem;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.util.LruCache;


public class ImageFileDownloadManager implements Callback {
	
	class ImageFileDownloadThreadImpl extends Thread {
		@Override
		public void run() {
			while (true) {	
				//TODO 修改判断flag exit
				//if (state == STATE_STOP)	return;
				
				ImageFileDownloadTaskHolder holder = null;
				synchronized (mSyncObject) {
					if (!mDownloadTasks.isEmpty()) {
						holder = mDownloadTasks.removeFirst();
					} else if (!mPreDownloadTasks.isEmpty()) {
						holder = mPreDownloadTasks.removeFirst();
					} else {
						try {
	//						mHandler.sendEmptyMessage(MSG_QUEUE_EMPTY);
							mSyncObject.wait();
						} catch (InterruptedException e) {
							continue;
						}
					}
				}
				
				if (holder != null) {
					mImageDownloader.execute(holder);
					
					if (holder.isTaskFinished()) {
						synchronized (mSyncObject) {
							mDownloadedTasks.add(holder);
						}
						mHandler.sendEmptyMessage(MSG_DOWNLOADED);
					}
				}
			}
			
		}
	}
	
	private LinkedList<ImageFileDownloadTaskHolder> mDownloadTasks;
	private LinkedList<ImageFileDownloadTaskHolder> mPreDownloadTasks;
	private LinkedList<ImageFileDownloadTaskHolder> mDownloadedTasks;
	
	private LruCache<String, ImageFileDownloadTaskHolder> mHolderCache;
	
	private Handler mHandler;
	
	private ImageFileDownloadedCallback mCallback = null; 

	private ImageFileDownloadThreadImpl mThread = null;
	
	private Object mSyncObject = new Object();
	
	private ImageDownloader mImageDownloader;
	
	final static int MSG_DOWNLOADED = 1;
	
	final static int MAX_HOLDER_CACHE_SIZE = 100;
	
	public ImageFileDownloadManager() {
		mDownloadTasks = new LinkedList<ImageFileDownloadTaskHolder>();
		mPreDownloadTasks = new LinkedList<ImageFileDownloadTaskHolder>();
		mDownloadedTasks = new LinkedList<ImageFileDownloadTaskHolder>();
		
		mHolderCache = new LruCache<String, ImageFileDownloadTaskHolder>(MAX_HOLDER_CACHE_SIZE);
		
		mHandler = new Handler(this);
		mImageDownloader = new ImageDownloader();
	}
	
	public void setDownloadCallback(ImageFileDownloadedCallback callback) {
		mCallback = callback;
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_DOWNLOADED:
			onDownloadDone();
			break;
		}
		return false;
	}

	/**
	 * When some download task finished, this method would be invoked. 
	 * 
	 */
	private void onDownloadDone() {
		synchronized (mSyncObject) {
			Iterator<ImageFileDownloadTaskHolder> it = mDownloadedTasks.iterator();
			while (it.hasNext()) {
				ImageFileDownloadTaskHolder holder = it.next();
				
				//从HolderCache中移除对应的对象
				mHolderCache.remove(holder.getId());
				
				if (mCallback != null)
					mCallback.onImageFileDownloaded(holder);
			}
			
			//遍历完成后，清空已完成队列
			mDownloadedTasks.clear();
		}
	}

	public ImageFileDownloadTaskHolder addDownload(ImageItem item, int type, boolean preload) {
		ImageFileDownloadTaskHolder holder = findTaskHolder(item, type);
		
		synchronized (mSyncObject) {
			if (mDownloadTasks.contains(holder)) {
				mDownloadTasks.remove(holder);
			}
			
			if (mPreDownloadTasks.contains(holder)) {
				mPreDownloadTasks.remove(holder);
			}
			
			if (mDownloadedTasks.contains(holder)) {
				//此处不需要callback，只要在已完成队列，就一定会被正常流程callback
				return holder;
			}
			
			if (preload) {
				mPreDownloadTasks.addFirst(holder);
			} else {
				mDownloadTasks.addFirst(holder);
			}
		}
		
		synchronized (mSyncObject) {
			if (mThread == null) {
				mThread = new ImageFileDownloadThreadImpl();
				mThread.start();
			} else {
				mSyncObject.notify();
			}
		}
		
		return holder;
	}

	private ImageFileDownloadTaskHolder findTaskHolder(ImageItem item, int type) {
		final String key = ImageFileDownloadTaskHolder.getHolderId(item, type);
		
		synchronized (mHolderCache) {
			ImageFileDownloadTaskHolder holder = mHolderCache.get(key);
			if (holder == null) {
				holder = new ImageFileDownloadTaskHolder(item, type);
				mHolderCache.put(key, holder);
			}
			
			return holder;
		}
	}
	
}

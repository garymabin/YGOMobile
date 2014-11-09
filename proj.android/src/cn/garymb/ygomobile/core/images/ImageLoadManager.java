package cn.garymb.ygomobile.core.images;

import java.util.Iterator;
import java.util.LinkedList;

import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.model.data.ImageItem;
import cn.garymb.ygomobile.model.data.ImageItemInfoHelper;
import cn.garymb.ygomobile.utils.BitmapLruCache;
import cn.garymb.ygomobile.utils.BitmapUtils;
import cn.garymb.ygomobile.utils.LruCache;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;

public class ImageLoadManager implements Callback {

	class ImageLoadThreadImpl extends Thread {
		@Override
		public void run() {
			while (true) {
				BitmapHolder holder = null;
				synchronized (mSyncObject) {
					if (!mLoadQueue.isEmpty()) {
						holder = mLoadQueue.removeFirst();
					} else if (!mPreLoadQueue.isEmpty()) {
						holder = mPreLoadQueue.removeFirst();
					} else {
						try {
							mSyncObject.wait();
						} catch (InterruptedException e) {
							continue;
						}
					}
				} // end of synchronized (mSyncObject)

				if (holder != null) {
					// TODO load bitmap
					final ImageItem item = holder.getImageItem();
					final String path = ImageItemInfoHelper.getImagePath(item);

					int[] resolution = new int[] { item.width, item.height };

					Bitmap bmp = BitmapUtils.createNewBitmapAndCompressByFile(
							path, resolution, true);

					if (bmp != null) {
						holder.setBitmap(bmp);
						if (holder.getImageType() == Constants.IMAGE_TYPE_ORIGINAL) {
							mOriginalCache.put(item.id, bmp);
						} else {
							mThumnailCache.put(item.id, bmp);
						}
					}

					if (holder.isBitmapLoaded()) {
						synchronized (mSyncObject) {
							mLoadedQueue.add(holder);
						}
						mHandler.sendEmptyMessage(MSG_LOADED);
					}
				}
			}
		}
	}

	final static int MAX_THUMNAIL_BMP_CACHE_SIZE = 60;
	final static int MAX_ORIGINAL_BMP_CACHE_SIZE = 20;
	final static int MAX_LOAD_QUEUE_SIZE = 30;
	final static int MAX_PRELOAD_QUEUE_SIZE = 20;

	final static int MAX_ORIGINAL_BMP_CACHE_BYTES_SIZE = 8 * 1024 * 1024;

	LruCache<String, Bitmap> mThumnailCache = new BitmapLruCache<String>(
			MAX_THUMNAIL_BMP_CACHE_SIZE, 0);
	LruCache<String, Bitmap> mOriginalCache = new BitmapLruCache<String>(
			MAX_ORIGINAL_BMP_CACHE_SIZE, MAX_ORIGINAL_BMP_CACHE_BYTES_SIZE);

	LinkedList<BitmapHolder> mLoadQueue = new LinkedList<BitmapHolder>();
	LinkedList<BitmapHolder> mPreLoadQueue = new LinkedList<BitmapHolder>();
	LinkedList<BitmapHolder> mLoadedQueue = new LinkedList<BitmapHolder>();

	private ImageLoadThreadImpl mThread = null;

	private Object mSyncObject = new Object();

	final static int MSG_LOADED = 0;

	private Handler mHandler = new Handler(this);

	private ImageLoadedCallback mCallback;

	public void setImageLoadedCallback(ImageLoadedCallback callback) {
		mCallback = callback;
	}

	/**
	 * Try to get bitmap from local cache.
	 * 
	 * @param holder
	 * @return bitmap object if hit cache, null otherwise.
	 */
	public Bitmap getBitmap(BitmapHolder holder) {
		if (holder == null)
			return null;

		ImageItem item = holder.getImageItem();
		if (item == null)
			return null;

		if (holder.getImageType() == Constants.IMAGE_TYPE_ORIGINAL)
			return mOriginalCache.get(item.id);
		else
			return mThumnailCache.get(item.id);
	}

	/**
	 * Try to reove bitmap from local cache.
	 * 
	 * @param id
	 * @return bitmap object if hit cache, null otherwise.
	 */
	public void removeBitmap(String id, int type) {
		if (id == null)
			return;

		if (type == Constants.IMAGE_TYPE_ORIGINAL)
			mOriginalCache.remove(id);
		else
			mThumnailCache.get(id);
	}

	/**
	 * 添加一个加载Bitmap的任务
	 * 
	 * @param holder
	 * @param isPreload
	 *            标示是否是预加载任务
	 */
	public void addLoadTask(BitmapHolder holder, boolean isPreload) {
		synchronized (mSyncObject) {
			if (isPreload)
				mPreLoadQueue.addFirst(holder);
			else
				mLoadQueue.addFirst(holder);

			if (mThread == null) {
				mThread = new ImageLoadThreadImpl();
				mThread.start();
			} else {
				mSyncObject.notify();
			}
		}
	}

	private void onBitmapLoaded() {
		synchronized (mSyncObject) {
			Iterator<BitmapHolder> it = mLoadedQueue.iterator();
			while (it.hasNext()) {
				BitmapHolder holder = it.next();

				if (mCallback != null)
					mCallback.onImageLoaded(holder);
			}

			mLoadedQueue.clear();
		}
	}

	public void onDestroy() {
		synchronized (mSyncObject) {
			mLoadQueue.clear();
			mPreLoadQueue.clear();
			mLoadedQueue.clear();

			mThumnailCache.evictAll();
			mOriginalCache.evictAll();
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_LOADED:
			onBitmapLoaded();
			break;
		}
		return false;
	}

	/**
	 * 清除指定的Bitmap缓存
	 * 
	 * @param type
	 *            {@code IMAGE_TYPE_ORIGINAL} or {@code IMAGE_TYPE_THUMNAIL}
	 */
	public void resetCache(int type) {
		if (Constants.IMAGE_TYPE_ORIGINAL == type) {
			mOriginalCache.evictAll();
		} else if (Constants.IMAGE_TYPE_THUMNAIL == type) {
			mThumnailCache.evictAll();
		}
	}

	/**
	 * 改变加载队列的优先级，将预加载队列提升为加载队列或加载队列降级为预加载队列。
	 * 
	 * @param type
	 *            {@code LOAD_TYPE_LOAD} or {@code LOAD_TYPE_PRELOAD}
	 */
	public void changeLoadPriority(int type) {
		synchronized (mSyncObject) {
			if (Constants.BITMAP_LOAD_TYPE_LOAD == type
					&& !mLoadQueue.isEmpty()) {
				mPreLoadQueue.addAll(0, mLoadQueue);
				mLoadQueue.clear();

				// 控制预加载队列长度
				while (mPreLoadQueue.size() > MAX_PRELOAD_QUEUE_SIZE)
					mPreLoadQueue.removeLast();

			} else if (Constants.BITMAP_LOAD_TYPE_PRELOAD == type
					&& !mPreLoadQueue.isEmpty()) {
				mLoadQueue.addAll(0, mPreLoadQueue);
				mPreLoadQueue.clear();

				// 控制加载队列长度
				while (mLoadQueue.size() > MAX_LOAD_QUEUE_SIZE)
					mLoadQueue.removeLast();
			}
		}
	}
}

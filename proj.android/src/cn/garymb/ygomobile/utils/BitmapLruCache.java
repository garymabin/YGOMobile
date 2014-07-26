package cn.garymb.ygomobile.utils;

import android.graphics.Bitmap;

public class BitmapLruCache<K> extends LruCache<K, Bitmap> {
	
	protected long mMaxByteSize = -1;
	protected long mByteSize = 0;
	
	public BitmapLruCache(int maxSize, long maxByteSize) {
		super(maxSize);
		
		mByteSize = 0;
		mMaxByteSize = maxByteSize;
	}

	@Override
	public Bitmap put(K key, Bitmap value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }
        
        //仅在设置了Max Byte Size的时候去计算内存占用大小
        if (mMaxByteSize > 0) {
        	long bytes = ((long) value.getRowBytes()) * value.getHeight();
	        while (bytes + mByteSize > mMaxByteSize && size() >= 3) //加一个条件size>=3防止回收正在使用的bmp
	        	trimToSize(size() - 1);
	        mByteSize += bytes;
        }
        return super.put(key, value);
	}
}

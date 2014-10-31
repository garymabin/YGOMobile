package cn.garymb.ygomobile.core.images;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;


import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.model.data.ImageItem;

import android.graphics.Bitmap;



public class BitmapHolder {
	/** 
	 * 用于唯一标示一个图片加载任务，需要区分缩略图和原图,构造时
	 * 以ImageItem.id_Model.DRT_REQUEST_IMAGE_BITMAP为格式创建
	 * 在ImageModelHelper的映射表中，能够和ImageFileDownloadHolder类型的请求区分开
	 */
	private String mId;
	private ImageItem mImageItem;
	private Reference<Bitmap> mRef;
	
	private int mImageType;
	
	public BitmapHolder(ImageItem item, int type) {
		this.mImageItem = item;
		this.mRef = null;
		mImageType = type;
		
		mId = item.id + "_" + Constants.REQUEST_TYPE_LOAD_BITMAP;
	}
	
	public String getId() {
		return mId;
	}

	public void setBitmap(Bitmap paramBitmap) {
		this.mRef = new SoftReference<Bitmap>(paramBitmap);
	}

	public boolean isBitmapLoaded() {
		if ((this.mRef != null) && (this.mRef.get() != null))
			return true;
		
		return false;
	}

	public Bitmap getBitmap() {
		if (this.mRef != null)
			return (Bitmap) this.mRef.get();
		return null;
	}

	public ImageItem getImageItem() {
		return this.mImageItem;
	}
	
	public int getImageType() {
		return mImageType;
	}
}

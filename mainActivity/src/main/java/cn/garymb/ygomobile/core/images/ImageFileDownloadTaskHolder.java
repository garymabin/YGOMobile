package cn.garymb.ygomobile.core.images;


import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.model.data.ImageItem;

import android.text.TextUtils;

public class ImageFileDownloadTaskHolder {
	/** 
	 * 用于唯一标示一个图片下载任务，需要区分缩略图和原图,构造时
	 * 以ImageItem.id_Model.DRT_REQUEST_IMAGE_DOWNLOAD_type为格式创建
	 * 在ImageModelHelper的映射表中，能够和BitmapHolder类型的请求区分开
	 */
	String mId;

	ImageItem mImageItem;
	
	int mImageType;	
	
	int mDownloadResult = RET_NOT_DOWNLOADED;
	
	/*package*/ volatile boolean isCancelRequested = false;
	
	final static int RET_MIN	= 0;
	final static int RET_MAX	= 3;
	
	public static final int RET_NOT_DOWNLOADED 		= 0;
	public static final int RET_DOWNLOAD_SUCCEED	= 1;
	public static final int RET_DOWNLOAD_FAILED 	= 2;
	public static final int RET_DOWNLOAD_CANCELED	= 3;
	
	private static final String NULL_ITEM_ID = "nullitem";
	
	/**
	 * 
	 * @param item
	 * @param type 
	 */
	ImageFileDownloadTaskHolder(ImageItem item, int type) {
		mImageItem = item;
		mImageType = type;
		
		mId = getHolderId(item, type);
	}
	
	public String getId() {
		return mId;
	}

	public boolean isTaskFinished() {
		return (mDownloadResult != RET_NOT_DOWNLOADED) && 
				(mDownloadResult != RET_DOWNLOAD_CANCELED);
	}
	
	/*package*/ void setDownloadResult(int ret) {
		if (ret < RET_MIN || ret > RET_MAX)
			ret = RET_MIN;
		
		mDownloadResult = ret;
	}

	public ImageItem getImageItem() {
		return mImageItem;
	}
	
	public int getImageType() {
		return mImageType;
	}

	public int result() {
		return mDownloadResult;
	}

	public static String getHolderId(ImageItem item, int type) {
		if (item == null || TextUtils.isEmpty(item.id))
			return NULL_ITEM_ID + "_" + Constants.REQUEST_TYPE_DOWNLOAD_IMAGE + "_" + type;
		return item.id + "_" + Constants.REQUEST_TYPE_DOWNLOAD_IMAGE + "_" + type;
	}
}

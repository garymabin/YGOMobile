package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.controller.Controller;
import cn.garymb.ygomobile.core.images.BitmapHolder;
import cn.garymb.ygomobile.core.images.PreviewImageItemController;
import cn.garymb.ygomobile.model.IDataObserver;
import cn.garymb.ygomobile.model.Model;
import cn.garymb.ygomobile.model.data.ImageItem;
import cn.garymb.ygomobile.model.data.ImageItemInfoHelper;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class ImagePreviewController extends BaseDialogConfigController
		implements IDataObserver, OnClickListener {

	private PreviewImageItemController mPreviewImageItemController;
	private ImageItem mImageItem;

	private ViewGroup mPreviewPanel;

	private OnClickListener mOnClickListener;

	private float mMaxImagePreviewHeight;
	private float mMaxImagePreviewWidth;

	private int mPreviewImageHeight;
	private int mPreviewImageWidth;

	public ImagePreviewController(DialogConfigUIBase configUI, View view,
			Bundle param) {
		super(configUI, view);
		final Resources res = configUI.getContext().getResources();
		mMaxImagePreviewHeight = res
				.getDimensionPixelSize(R.dimen.image_preview_limit_height);
		mMaxImagePreviewWidth = res
				.getDimensionPixelSize(R.dimen.image_preview_limit_width);
		float limitScale = mMaxImagePreviewHeight / mMaxImagePreviewWidth;
		int[] origSize = param.getIntArray("orig_size");
		float origScale = origSize[1] / (origSize[0] * 1.0f);
		if (Float.compare(limitScale, origScale) > 0) {
			float scale = mMaxImagePreviewWidth / origSize[0];
			mPreviewImageWidth = (int) mMaxImagePreviewWidth;
			mPreviewImageHeight = (int) (origSize[1] * scale);
		} else {
			float scale = mMaxImagePreviewHeight / origSize[1];
			mPreviewImageWidth = (int) (origSize[0] * scale);
			mPreviewImageHeight = (int) mMaxImagePreviewHeight;
		}
		String url = param.getString("url");
		mPreviewPanel = (ViewGroup) view
				.findViewById(R.id.preview_image_layout);
		mPreviewPanel.setOnClickListener(this);
		mPreviewPanel.setTag(param);
		mPreviewImageItemController = new PreviewImageItemController(
				configUI.getContext(),
				(ImageView) view.findViewById(R.id.image_preview));

		mImageItem = new ImageItem(url, mPreviewImageHeight, mPreviewImageWidth);
		Bitmap cardImage = Model.peekInstance().getBitmap(mImageItem,
				Constants.IMAGE_TYPE_ORIGINAL);
		if (cardImage != null) {
			mPreviewImageItemController.setBitmap(cardImage, false);
		} else {
			mPreviewImageItemController.setImageItem(mImageItem);
			requestImage(mImageItem, false);
		}
		int titleRes = param.getInt("title_res");
		if (titleRes != 0) {
			configUI.setTitle(titleRes);
		}
		configUI.setPositiveButton(res.getString(R.string.button_ok));
	}

	/* package */void setOnClickListener(OnClickListener listener) {
		mOnClickListener = listener;
	}

	private void requestImage(ImageItem item, boolean isPreload) {
		// 确定加载的类型
		int type = isPreload ? Constants.BITMAP_LOAD_TYPE_PRELOAD
				: Constants.BITMAP_LOAD_TYPE_LOAD;

		// 已下载就加载
		if (ImageItemInfoHelper.isImageExist(item)) {
			Message msg = Controller.buildMessage(
					Constants.REQUEST_TYPE_LOAD_BITMAP,
					Constants.IMAGE_TYPE_ORIGINAL, type, item);
			Controller.peekInstance().requestDataOperation(this, msg);
		} else {
			String msg = mConfigUI.getContext().getResources()
					.getString(R.string.image_not_load_toast);
			Toast.makeText(mConfigUI.getContext(), msg,
					Toast.LENGTH_SHORT);
			Log.w("wtf", "Why can't we locate a image already exist on disk?");
		}
	}

	@Override
	public void notifyDataUpdate(Message msg) {
		if (msg.obj != null && msg.obj instanceof BitmapHolder)
			onBitmapLoaded((BitmapHolder) msg.obj);
	}

	private void onBitmapLoaded(BitmapHolder holder) {
		if (holder == null)
			return;

		final ImageItem item = holder.getImageItem();
		if (item == null)
			return;

		mPreviewImageItemController.setBitmap(holder.getBitmap(), true);
	}

	@Override
	public void onClick(View v) {
		mOnClickListener.onClick(v);
	}
}

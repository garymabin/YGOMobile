package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.controller.Controller;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

public class ImagePreviewDialog extends BaseDialog {

	private android.view.View.OnClickListener mNewImageClickListener;

	public ImagePreviewDialog(Context context, View view,
			DialogInterface.OnClickListener listener,
			android.view.View.OnClickListener imagelistener, Bundle param) {
		super(context, listener, view, param);
		mNewImageClickListener = imagelistener;
	}

	@Override
	public void show() {
		super.show();
		Controller.peekInstance().registerImageObserver(
				(ImagePreviewController) mController);
		((ImagePreviewController) mController)
				.setOnClickListener(mNewImageClickListener);
	}

	@Override
	public void dismiss() {
		super.dismiss();
		Controller.peekInstance().unregisterImageObserver(
				(ImagePreviewController) mController);
		((ImagePreviewController) mController).setOnClickListener(null);
		mNewImageClickListener = null;
	}

	@Override
	protected BaseDialogConfigController createController(View view) {
		return new ImagePreviewController(this, view, mParam);
	}

}

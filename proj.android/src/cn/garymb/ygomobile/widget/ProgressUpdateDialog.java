package cn.garymb.ygomobile.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

public class ProgressUpdateDialog extends BaseDialog {

	public ProgressUpdateDialog(Context context, OnClickListener listener,
			View view, Bundle param) {
		super(context, listener, view, param);
		createController(view);
	}

	@Override
	protected BaseDialogConfigController createController(View view) {
		if (mController == null) {
			mController = new ProgressUpdateDialogController(this, view);
		}
		return mController;
	}

}

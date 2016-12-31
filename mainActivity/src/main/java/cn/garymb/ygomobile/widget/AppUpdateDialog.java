package cn.garymb.ygomobile.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

public class AppUpdateDialog extends BaseDialog {

	public AppUpdateDialog(Context context, OnClickListener listener,
			View view, Bundle param) {
		super(context, listener, view, param);
	}

	@Override
	protected BaseDialogConfigController createController(View view) {
		return new AppUpdateController(this, view, mParam);
	}

}

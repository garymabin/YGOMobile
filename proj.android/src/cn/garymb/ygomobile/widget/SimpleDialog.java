package cn.garymb.ygomobile.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

public class SimpleDialog extends BaseDialog {

	public SimpleDialog(Context context, OnClickListener listener,
			View view, Bundle param) {
		super(context, listener, view, param);
	}

	@Override
	protected BaseDialogConfigController createController(View view) {
		return new SimpleDialogConfigController(this, view, mParam);
	}
}

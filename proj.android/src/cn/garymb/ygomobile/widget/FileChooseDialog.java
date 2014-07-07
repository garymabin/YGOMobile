package cn.garymb.ygomobile.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

public class FileChooseDialog extends BaseDialog {
	
	public FileChooseDialog(Context context, View view, OnClickListener listener, Bundle param) {
		super(context, listener, view, param);
	}
	
	@Override
	protected BaseDialogConfigController createController(View view) {
		return new FileChooseController(this, view, mParam);
	}

}

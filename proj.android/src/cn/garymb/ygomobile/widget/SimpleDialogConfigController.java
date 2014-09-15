package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

public class SimpleDialogConfigController extends BaseDialogConfigController {

	public SimpleDialogConfigController(DialogConfigUIBase configUI, View view, Bundle mParam) {
		super(configUI, view);
		final Resources res = configUI.getContext().getResources();
		String title = mParam.getString("title");
		String message = mParam.getString("message");
		configUI.setTitle(title);
		if (configUI instanceof BaseDialog) {
			((BaseDialog) configUI).setMessage(message);
		}
		configUI.setPositiveButton(res.getString(R.string.button_ok));
		configUI.setCancelButton(res.getString(R.string.button_cancel));
	}

}

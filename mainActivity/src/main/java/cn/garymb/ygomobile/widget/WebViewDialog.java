package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class WebViewDialog extends BaseDialog {
	
	public WebViewDialog(Context context, WebView view, OnClickListener listener, Bundle param) {
		super(context, listener,view, param);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected BaseDialogConfigController createController(View view) {
		String url = mParam.getString("url");
		int titleRes = mParam.getInt("titleRes");
		((WebView)view).loadUrl(url);
		setTitle(titleRes);
		setButton(BUTTON_POSITIVE, getContext().getResources().getString(R.string.button_ok), mListener);
		((WebView)view).getSettings().setDefaultTextEncodingName("utf-8");
		return null;
	}

}

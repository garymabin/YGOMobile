package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class AppUpdateController extends BaseDialogConfigController {
	
	private WebView mWebView;
	private String mDownloadUrl;

	public AppUpdateController(DialogConfigUIBase configUI, View view, Bundle param) {
		super(configUI, view);
		mWebView = (WebView) view.findViewById(R.id.web_content);
		final Resources res = configUI.getContext().getResources();
		int titleRes = param.getInt("titleRes");
		int versionCode = param.getInt("version");
		mDownloadUrl = ResourcesConstants.UPDATE_SERVER_URL + "/ygomobile/"+ versionCode + param.getString("url"); 
		mWebView.loadUrl(ResourcesConstants.UPDATE_SERVER_URL + "/ygomobile/" + versionCode + "/changelog.html");
		mWebView.getSettings().setDefaultTextEncodingName("utf-8");
		configUI.setTitle(titleRes);
		configUI.setCancelButton(res.getString(R.string.button_cancel));
		configUI.setPositiveButton(res.getString(R.string.button_update));
	}
	
	public String getDownloadUrl() {
		return mDownloadUrl;
	}

}

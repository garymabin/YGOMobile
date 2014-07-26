package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.widget.filebrowser.FileBrowser;
import cn.garymb.ygomobile.widget.filebrowser.FolderNavigator;
import cn.garymb.ygomobile.widget.filebrowser.FolderNavigator.NavigateItemChangeListener;
import cn.garymb.ygomobile.widget.filebrowser.SharingItemBase.SharingItemSelectListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

public class FileChooseController extends BaseDialogConfigController implements SharingItemSelectListener, NavigateItemChangeListener {
	
	private String mUrl;
	private FileBrowser mFileBrowser;
	private FolderNavigator mNavigator;

	public FileChooseController(DialogConfigUIBase configUI, View view, Bundle param) {
		super(configUI, view);
		
		final Resources res = configUI.getContext().getResources();
		String rootDir = param.getString("root");
		mUrl = param.getString("current");
		configUI.setTitle(R.string.choose_folder);
		mFileBrowser = (FileBrowser) view.findViewById(R.id.file_browser);
		mNavigator = (FolderNavigator) view.findViewById(R.id.folder_navigator);
		mNavigator.setNavigateItemChangeListener(this);
		
		mFileBrowser.setOnBrowserListener(mNavigator);
		mFileBrowser.setItemSelectListener(this);
		mFileBrowser.browse(rootDir);
		
		configUI.setPositiveButton(res.getString(R.string.button_ok));
		configUI.setCancelButton(res.getString(R.string.button_cancel));
	}

	@Override
	public void enableSubmitIfAppropriate() {
		Button positive = mConfigUI.getPosiveButton();
		if (positive == null)
			return;
		if (TextUtils.isEmpty(mUrl)) {
			positive.setEnabled(false);
		} else {
			positive.setEnabled(true);
		}
	}

	@Override
	public void onFileSelectionChanged(String url, boolean isSelected) {
		if (isSelected) {
			mUrl = url;
		}
		enableSubmitIfAppropriate();
		mFileBrowser.refresh();
	}

	@Override
	public boolean isFileSelected(String url) {
		return url.equals(mUrl);
	}
	
	public String getUrl() {
		return mUrl;
	}

	@Override
	public void onItemChange(String path) {
		mFileBrowser.browse(path);		
	}

}

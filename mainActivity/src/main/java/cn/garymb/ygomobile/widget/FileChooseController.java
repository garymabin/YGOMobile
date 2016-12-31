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
	
	private int mMode;

	public FileChooseController(DialogConfigUIBase configUI, View view, Bundle param) {
		super(configUI, view);
		
		final Resources res = configUI.getContext().getResources();
		String rootDir = param.getString("root");
		mUrl = param.getString("current");
		mMode = param.getInt("mode", FileBrowser.BROWSE_MODE_DIRS);
		if (mMode == FileBrowser.BROWSE_MODE_ALL) {
			configUI.setTitle(R.string.choose_all);
		} else if (mMode == FileBrowser.BROWSE_MODE_DIRS) {
			configUI.setTitle(R.string.choose_folder); 
		} else if (mMode == FileBrowser.BROWSE_MODE_FILES) {
			configUI.setTitle(R.string.choose_file);
		}
		mFileBrowser = (FileBrowser) view.findViewById(R.id.file_browser);
		mNavigator = (FolderNavigator) view.findViewById(R.id.folder_navigator);
		mNavigator.setNavigateItemChangeListener(this);
		
		mFileBrowser.setBrowserMode(mMode);
		mFileBrowser.setOnBrowserListener(mNavigator);
		mFileBrowser.setItemSelectListener(this);
		mFileBrowser.browse(rootDir);
		
		configUI.setPositiveButton(res.getString(R.string.button_ok));
		configUI.setCancelButton(res.getString(R.string.button_cancel));
	}

	@Override
	public int enableSubmitIfAppropriate() {
		Button positive = mConfigUI.getPositiveButton();
		if (positive == null)
			return 0;
		if (TextUtils.isEmpty(mUrl)) {
			positive.setEnabled(false);
		} else {
			positive.setEnabled(true);
		}
		return 0;
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
	
	public int getMode() {
		return mMode;
	}

	@Override
	public void onItemChange(String path) {
		mFileBrowser.browse(path);		
	}

}

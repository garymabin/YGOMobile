/*
 * CheckActivity.java
 *
 *  Created on: 2014年3月10日
 *      Author: mabin
 */
package cn.garymb.ygomobile;

import java.io.File;

import cn.garymb.ygomobile.core.StaticApplication;
import cn.garymb.ygomobile.fragment.NavigatorFragment;
import cn.garymb.ygomobile.fragment.NavigatorFragment.NavigateItemChangeListener;
import cn.garymb.ygomobile.util.Constants;
import cn.garymb.ygomobile.widget.filebrowser.FileBrowser;
import cn.garymb.ygomobile.widget.filebrowser.SharingItemBase.SharingItemSelectListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

/**
 * @author mabin
 *
 */
public class CheckActivity extends FragmentActivity implements SharingItemSelectListener, NavigateItemChangeListener, android.view.View.OnClickListener {
	
	public static Pair<String, String> sRootPair;
	
	static {
		sRootPair = Pair.create(StaticApplication.peekInstance().getResources()
				.getString(R.string.file_navigator_root_dir),
				"/");
	}
	
	private FileBrowser mFileView;
	private ViewGroup mFileBrowserView;
	private NavigatorFragment mNavigatorFragment;
	private String mUrl = "";
	
	private Button mConfirmButton;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.check_base);
		initViews();
		mNavigatorFragment = (NavigatorFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigator_fragment);
		mFileBrowserView.setVisibility(View.GONE);
		checkAvailable(StaticApplication.peekInstance().getResourcePath());
	}

	/**
	 * 
	 * @return
	**/
	protected void checkAvailable(String path) {
		final int errcode = checkResourceDirectory(path);
		if (errcode == Constants.RESOURCE_ERROR_NONE) {
			startGame(path);
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.choose_resource_dir);
			builder.setMessage(formatMessage(errcode));
			builder.setPositiveButton(R.string.button_ok, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if (errcode == Constants.RESOURCE_ERROR_SDCARD_NOT_AVAIL) {
						CheckActivity.this.finish();
					}
					mFileBrowserView.setVisibility(View.VISIBLE);
					
				}
			});
			builder.setNegativeButton(R.string.button_cancel,  new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					CheckActivity.this.finish();
				}
			});
			builder.show();
		}
	}

	private void initViews() {
		// TODO Auto-generated method stub
		LayoutInflater inflater = LayoutInflater.from(this);
		ViewGroup extraView = (ViewGroup) findViewById(R.id.extra_panel);
		mFileBrowserView = (ViewGroup)inflater.inflate(R.layout.file_browser_layout, extraView);
		mFileView = (FileBrowser) mFileBrowserView.findViewById(R.id.file_browser);
		mFileView.setItemSelectListener(this);
		
		mConfirmButton = (Button) findViewById(R.id.button_bottom_right);
		mConfirmButton.setOnClickListener(this);
		
		mConfirmButton.setEnabled(false);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mFileView.setOnBrowserListener(mNavigatorFragment);
		mFileView.refresh();
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mFileView.setOnBrowserListener(null);
	}

	/**
	 * 
	 * @return
	**/
	private int formatMessage(int errcode) {
		// TODO Auto-generated method stub
		int resID  = R.string.res_error_unknown;
		switch (errcode) {
		case Constants.RESOURCE_ERROR_SDCARD_NOT_AVAIL:
			resID = R.string.res_error_sdcard_not_avail;
			break;
		case Constants.RESOURCE_ERROR_NOT_EXIST:
			resID = R.string.res_error_not_exist;
			break;
		case Constants.RESOURCE_ERROR_CONFIG_FILE_NOT_EXIST:
			resID = R.string.res_error_config_not_exist;
			break;
		case Constants.RESOURCE_ERROR_CARDS_DB_FILE_NOT_EXIST:
			resID = R.string.res_error_db_file_not_exist;
			break;
		default:
			break;
		}
		return resID;
	}

	/**
	 * 
	 * @return
	**/
	protected void startGame(String path) {
		StaticApplication.peekInstance().setResourcePath(path);
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), YGOMobileActivity.class);
		startActivity(intent);
		finish();
	}
	
	private int checkResourceDirectory(String path) {
		String sdcardStatus = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(sdcardStatus) &&
				!Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdcardStatus)) {
			return Constants.RESOURCE_ERROR_SDCARD_NOT_AVAIL;
		}
		File workingDir = new File(path);
		if (!workingDir.exists()) {
			return Constants.RESOURCE_ERROR_NOT_EXIST;
		}
		File configFile = new File(workingDir, Constants.CONFIG_FILE);
		if (!configFile.exists()) {
			return Constants.RESOURCE_ERROR_CONFIG_FILE_NOT_EXIST;
		}
		File cardsDBFile = new File(workingDir, Constants.CARD_DB_FILE);
		if (!cardsDBFile.exists()) {
			return Constants.RESOURCE_ERROR_CARDS_DB_FILE_NOT_EXIST;
		}
		return Constants.RESOURCE_ERROR_NONE;
	}

	@Override
	public void onFileSelectionChanged(String url, boolean isSelected) {
		if (url != null && isSelected) {
			mUrl = url;
		} else {
			mUrl = "";
		}
		mConfirmButton.setEnabled(!mUrl.equals(""));
		mFileView.refresh();
		
	}

	@Override
	public boolean isFileSelected(String url) {
		return mUrl.equals(url);
	}

	@Override
	public void onItemChange(String path) {
		mFileView.browse(path);
	}
	
	@Override
	public void onBackPressed() {
		if (!mFileView.isRootDir()) {
			mFileView.toParentDir();
		} else {
			finish();
		}
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		checkAvailable(mUrl);
	}

}

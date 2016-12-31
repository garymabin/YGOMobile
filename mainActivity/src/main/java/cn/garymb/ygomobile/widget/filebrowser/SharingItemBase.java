/*
 * SharingItemBase.java
 *
 *  Created on: 2014年3月15日
 *      Author: mabin
 */package cn.garymb.ygomobile.widget.filebrowser;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import cn.garymb.ygomobile.R;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * @author mabin
 *
 */
public abstract class SharingItemBase implements Observer, ISharingItemInterface{

	public static interface SharingItemSelectListener {
		public void onFileSelectionChanged(String url, boolean isSelected);

		public boolean isFileSelected(String url);
	}

	private static final String TAG = "SharingItemBase";
	private SharingItemSelectListener mListener;
	/**/ String mUrl;

	public void setListener(SharingItemSelectListener listener) {
		mListener = listener;
	}
	
	protected abstract boolean getChecked();
	
	protected abstract void setChecked(boolean isChecked);
	
	protected abstract void toggle();
	
	protected abstract Context getContext();

	protected void toggleCheckMark() {
		Log.i(TAG,
				"toggleCheckMark(), mUrl = " + mUrl);
		File file = new File(mUrl);
		if (file.exists() && file.canRead()) {
			toggle();
			mListener.onFileSelectionChanged(mUrl, getChecked());
		} else {
			Toast.makeText(getContext(), R.string.access_denied,Toast.LENGTH_SHORT).show();
		}
		
	}
	
	private void setCheckMark(boolean isChecked, boolean isChanged) {
		Log.i(TAG,
				"setCheckMark(), mUrl = " + mUrl + " isChanged = " + isChanged);
		File file = new File(mUrl);
		if (file.exists() && file.canRead()) {
			setChecked(isChecked);
			if (isChanged) {
				mListener.onFileSelectionChanged(mUrl, getChecked());
			}
		} 
	}


	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		setCheckMark((Boolean)data, getChecked() != ((Boolean)data).booleanValue());
	}

}

/*
 * SharingItemDelegate.java
 *
 *  Created on: 2014年3月15日
 *      Author: mabin
 */package cn.garymb.ygomobile.widget.filebrowser;

import android.content.Context;
import android.util.Log;

/**
 * @author mabin
 *
 */
public class SharingItemDelegate extends SharingItemBase {
	
	private static final String TAG = "SharingItemDelegate";
	
	private ISelectable mSelectable;
	private Context mContext;
	private boolean initialized = false;
	
	/**
	 * 
	 */
	public SharingItemDelegate(Context context) {
		mContext = context;
	}
	
	public void setSelectable(ISelectable s) {
		if (!initialized) {
			mSelectable = s;
			initialized = true;
		} else {
			Log.w(TAG,
					getClass().getName() + "try to reset slectable");
		}
	}

	/* (non-Javadoc)
	 * @see cn.garymb.ygomoile.widget.filebrowser.SharingItemBase#getChecked()
	 */
	@Override
	protected boolean getChecked() {
		if (initialized) {
			return mSelectable.getSelected();
		} else {
			Log.w(TAG,
					getClass().getName() + " has not initialized");
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see cn.garymb.ygomoile.widget.filebrowser.SharingItemBase#setChecked(boolean)
	 */
	@Override
	protected void setChecked(boolean isChecked) {
		if (initialized) {
			mSelectable.setSelected(isChecked);
		} else {
			Log.w(TAG,
					getClass().getName() + " has not initialized");
		}
	}

	/* (non-Javadoc)
	 * @see cn.garymb.ygomoile.widget.filebrowser.SharingItemBase#toggle()
	 */
	@Override
	protected void toggle() {
		if (initialized) {
			mSelectable.setSelected(!mSelectable.getSelected());
		} else {
			Log.w(TAG, getClass().getName() + " has not initialized");
		}
		
	}

	/* (non-Javadoc)
	 * @see cn.garymb.ygomoile.widget.filebrowser.SharingItemBase#getContext()
	 */
	@Override
	protected Context getContext() {
		return mContext;
	}
	
	/* (non-Javadoc)
	 * @see cn.garymb.ygomoile.widget.filebrowser.ISharingItemInterface#toggoleBackground()
	 */
	@Override
	public void toggoleBackground(boolean isPressed) {
	}

	@Override
	public void setUrl(String url) {
		mUrl = url;
	}

}

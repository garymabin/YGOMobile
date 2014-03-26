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
		// TODO Auto-generated constructor stub
		
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
	 * @see cn.mabin.lanfileshare.ui.widget.SharingItemBase#getChecked()
	 */
	@Override
	protected boolean getChecked() {
		// TODO Auto-generated method stub
		if (initialized) {
			return mSelectable.getSelected();
		} else {
			Log.w(TAG,
					getClass().getName() + " has not initialized");
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see cn.mabin.lanfileshare.ui.widget.SharingItemBase#setChecked(boolean)
	 */
	@Override
	protected void setChecked(boolean isChecked) {
		// TODO Auto-generated method stub
		if (initialized) {
			mSelectable.setSelected(isChecked);
		} else {
			Log.w(TAG,
					getClass().getName() + " has not initialized");
		}
	}

	/* (non-Javadoc)
	 * @see cn.mabin.lanfileshare.ui.widget.SharingItemBase#toggle()
	 */
	@Override
	protected void toggle() {
		// TODO Auto-generated method stub
		if (initialized) {
			mSelectable.setSelected(!mSelectable.getSelected());
		} else {
			Log.w(TAG, getClass().getName() + " has not initialized");
		}
		
	}

	/* (non-Javadoc)
	 * @see cn.mabin.lanfileshare.ui.widget.SharingItemBase#getContext()
	 */
	@Override
	protected Context getContext() {
		// TODO Auto-generated method stub
		return mContext;
	}
	
	/* (non-Javadoc)
	 * @see cn.mabin.lanfileshare.ui.widget.ISharingItemInterface#toggoleBackground()
	 */
	@Override
	public void toggoleBackground() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setUrl(String url) {
		mUrl = url;
	}

}

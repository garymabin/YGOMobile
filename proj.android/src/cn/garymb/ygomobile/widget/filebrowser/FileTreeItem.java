/*
 * FileTreeItem.java
 *
 *  Created on: 2014年3月15日
 *      Author: mabin
 */package cn.garymb.ygomobile.widget.filebrowser;

import java.util.Observable;
import java.util.Observer;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.widget.filebrowser.SharingItemBase.SharingItemSelectListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * @author mabin
 *
 */
public class FileTreeItem extends RelativeLayout implements Observer, ISharingItemInterface {
	
	private SelectableItem mItem;
	private static float CHECKMARK_AREA = -1;
	/**
	 * @param context
	 */
	public FileTreeItem(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public FileTreeItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public FileTreeItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init(context);
	}
	
	protected SharingItemDelegate mDelegate;

	public void setListener(SharingItemSelectListener listener) {
		mDelegate.setListener(listener);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mItem = (SelectableItem) findViewById(R.id.sharing_checkbox);
		mDelegate.setSelectable(mItem);
	}
	
	public void setSelectbleVisibility(boolean visible) {
		if (mItem != null) {
			mItem.setVisibility(visible? View.VISIBLE : View.INVISIBLE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (mItem.getVisibility() == View.INVISIBLE) {
			return false;
		}
		boolean handled = false;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			handled = true;
			break;

		case MotionEvent.ACTION_CANCEL:
			break;

		case MotionEvent.ACTION_UP:
			if (event.getX() < CHECKMARK_AREA) {
				mDelegate.toggleCheckMark();
				toggoleBackground();
			} else {
				performClick();
				return false;
			}
			handled = true;
			break;
		}

		if (handled) {
			postInvalidate();
		} else {
			handled = super.onTouchEvent(event);
		}

		return handled;
	}
	
	public void toggoleBackground() {
		if (mDelegate.getChecked()) {
			setBackgroundColor(getResources().getColor(R.color.list_background_selcted));
		} else {
			setBackgroundColor(getResources().getColor(R.color.white));
		}
	}

	/**
	 * 
	 * @author: mabin
	 * @param
	 * @param
	 * @return
	 **/
	protected void init(Context context) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		if (CHECKMARK_AREA == -1) {
			CHECKMARK_AREA = getResources().getDimensionPixelSize(
					R.dimen.checkmark_area);
		}
		mDelegate = new SharingItemDelegate(context);
	}


	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		mDelegate.update(observable, data);
	}

	@Override
	public void setUrl(String url) {
		// TODO Auto-generated method stub
		mDelegate.setUrl(url);
	}
}

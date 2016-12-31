/*
 * NavigatorListItem.java
 *
 *  Created on: 2014年3月15日
 *      Author: mabin
 */package cn.garymb.ygomobile.widget.filebrowser;

import cn.garymb.ygomobile.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author mabin
 *
 */
public class NavigatorListItem extends RelativeLayout {

	private TextView mDirName;

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public NavigatorListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public NavigatorListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 */
	public NavigatorListItem(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see android.view.View#onFinishInflate()
	 */
	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		mDirName = (TextView)findViewById(R.id.navigator_folder_name);
	}
	
	/* (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		boolean handled = false;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDirName.setTextColor(getResources().getColor(R.color.navigator_dir_text_color_selected));
			handled = true;
			break;
		case MotionEvent.ACTION_CANCEL:
			mDirName.setTextColor(getResources().getColor(R.color.navigator_dir_text_color));
			handled = false;
			break;

		case MotionEvent.ACTION_UP:
			mDirName.setTextColor(getResources().getColor(R.color.navigator_dir_text_color));
			handled = true;
			break;
		}
		if (!handled) {
			super.onTouchEvent(event);
		}
		return handled;
	}

}

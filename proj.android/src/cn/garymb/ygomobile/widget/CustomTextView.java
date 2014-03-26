/*
 * CustomTextView.java
 *
 *  Created on: 2014年3月15日
 *      Author: mabin
 */
package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * @author mabin
 * 
 */
public class CustomTextView extends TextView {

	/**
	 * @param context
	 */
	public CustomTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public CustomTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.TextView#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		boolean handled = false;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			toggoleTextColor(true);
			handled = true;
			break;

		case MotionEvent.ACTION_CANCEL:
			toggoleTextColor(false);
			break;

		case MotionEvent.ACTION_UP:
			toggoleTextColor(false);
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

	/**
	 * 
	 * @return
	 **/
	private void toggoleTextColor(boolean pressed) {
		// TODO Auto-generated method stub
		setTextColor(pressed ? getResources().getColor(R.color.navigator_dir_text_color_selected) :
			getResources().getColor(android.R.color.white));
	}

}

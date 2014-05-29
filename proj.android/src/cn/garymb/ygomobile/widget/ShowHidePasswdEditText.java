package cn.garymb.ygomobile.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

public class ShowHidePasswdEditText extends EditText {

	protected Drawable drawableRight;

	protected int actionX;
	protected int actionY;

	@Override
	public void setCompoundDrawables(Drawable left, Drawable top,
			Drawable right, Drawable bottom) {
		if (right != null) {
			drawableRight = right;
		}
		super.setCompoundDrawables(left, top, right, bottom);
	}

	public ShowHidePasswdEditText(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public ShowHidePasswdEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ShowHidePasswdEditText(Context context) {
		super(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Rect bounds;
		if (drawableRight != null) {
			actionX = (int) event.getX();
			actionY = (int) event.getY();
			bounds = null;
			bounds = drawableRight.getBounds();

			int x, y;
			int extraTapArea = 13;

			/**
			 * IF USER CLICKS JUST OUT SIDE THE RECTANGLE OF THE DRAWABLE THAN
			 * ADD X AND SUBTRACT THE Y WITH SOME VALUE SO THAT AFTER
			 * CALCULATING X AND Y CO-ORDINATE LIES INTO THE DRAWBABLE BOUND. -
			 * this process help to increase the tappable area of the rectangle.
			 */
			x = (int) (actionX + extraTapArea);
			y = (int) (actionY - extraTapArea);

			/**
			 * Since this is right drawable subtract the value of x from the
			 * width of view. so that width - tappedarea will result in x
			 * co-ordinate in drawable bound.
			 */
			x = getWidth() - x;

			/*
			 * x can be negative if user taps at x co-ordinate just near the
			 * width. e.g views width = 300 and user taps 290. Then as per
			 * previous calculation 290 + 13 = 303. So subtract X from
			 * getWidth() will result in negative value. So to avoid this add
			 * the value previous added when x goes negative.
			 */

			if (x <= 0) {
				x += extraTapArea;
			}

			/*
			 * If result after calculating for extra tappable area is negative.
			 * assign the original value so that after subtracting extratapping
			 * area value doesn't go into negative value.
			 */

			if (y <= 0)
				y = actionY;

			/**
			 * If drawble bounds contains the x and y points then move ahead.
			 */
			int action = event.getAction();
			if (bounds.contains(x, y)) {
				if (action == MotionEvent.ACTION_DOWN) {
					int pos = getSelectionEnd();
					setInputType(InputType.TYPE_CLASS_TEXT
							| InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					if (pos > 0) {
						setSelection(pos);
					}
				} else if (action == MotionEvent.ACTION_UP
						|| action == MotionEvent.ACTION_CANCEL) {
					int pos = getSelectionEnd();
					setInputType(InputType.TYPE_CLASS_TEXT
							| InputType.TYPE_TEXT_VARIATION_PASSWORD);
					if (pos > 0) {
						setSelection(pos);
					}
				}
				event.setAction(MotionEvent.ACTION_CANCEL);
				return true;
			}
			if (action == MotionEvent.ACTION_UP
					|| action == MotionEvent.ACTION_CANCEL) {
				int pos = getSelectionEnd();
				setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_PASSWORD);
				if (pos > 0) {
					setSelection(pos);
				}
			}
			return super.onTouchEvent(event);
		} else {
			return super.onTouchEvent(event);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		drawableRight = null;
		super.finalize();
	}

}

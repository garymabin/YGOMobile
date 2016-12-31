package cn.garymb.ygomobile.widget;


import cn.garymb.ygomobile.utils.DeviceUtils;
import cn.garymb.ygomobile.widget.ClickableImageEditText.DrawableClickListener.DrawablePosition;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

public class ClickableImageEditText extends EditText {

	public interface DrawableClickListener {
		public static enum DrawablePosition {
			TOP, BOTTOM, LEFT, RIGHT
		};
		public void onClick(DrawablePosition target);
	}

	protected Drawable drawableRight;
	protected Drawable drawableLeft;
	protected Drawable drawableTop;
	protected Drawable drawableBottom;

	protected int actionX;
	protected int actionY;
	
	private DrawableClickListener clickListener;

	public ClickableImageEditText(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public ClickableImageEditText(Context context, AttributeSet attrs) {
		this(context, attrs, -1);
	}

	public ClickableImageEditText(Context context) {
		this(context, null, -1);
	}
	

	@Override
	public void setCompoundDrawables(Drawable left, Drawable top,
			Drawable right, Drawable bottom) {
		if (left != null) {
			drawableLeft = left;
		}
		if (right != null) {
			drawableRight = right;
		}
		if (top != null) {
			drawableTop = top;
		}
		if (bottom != null) {
			drawableBottom = bottom;
		}
		super.setCompoundDrawables(left, top, right, bottom);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Rect bounds;
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			actionX = (int) event.getX();
			actionY = (int) event.getY();
			if (drawableBottom != null
					&& drawableBottom.getBounds().contains(actionX, actionY)) {
				clickListener.onClick(DrawablePosition.BOTTOM);
				return super.onTouchEvent(event);
			}

			if (drawableTop != null
					&& drawableTop.getBounds().contains(actionX, actionY)) {
				clickListener.onClick(DrawablePosition.TOP);
				return super.onTouchEvent(event);
			}

			// this works for left since container shares 0,0 origin with bounds
			if (drawableLeft != null) {
				bounds = null;
				bounds = drawableLeft.getBounds();

				int x, y;
				int extraTapArea = (int) (13 * DeviceUtils.getDensity() + 0.5);

				x = actionX;
				y = actionY;

				if (!bounds.contains(actionX, actionY)) {
					/** Gives the +20 area for tapping. */
					x = (int) (actionX - extraTapArea);
					y = (int) (actionY - extraTapArea);

					if (x <= 0)
						x = actionX;
					if (y <= 0)
						y = actionY;

					/** Creates square from the smallest value */
					if (x < y) {
						y = x;
					}
				}

				if (bounds.contains(x, y) && clickListener != null) {
					clickListener
							.onClick(DrawableClickListener.DrawablePosition.LEFT);
					event.setAction(MotionEvent.ACTION_CANCEL);
					return false;

				}
			}

			if (drawableRight != null) {

				bounds = null;
				bounds = drawableRight.getBounds();

				int x, y;
				int extraTapArea = 13;

				/**
				 * IF USER CLICKS JUST OUT SIDE THE RECTANGLE OF THE DRAWABLE
				 * THAN ADD X AND SUBTRACT THE Y WITH SOME VALUE SO THAT AFTER
				 * CALCULATING X AND Y CO-ORDINATE LIES INTO THE DRAWBABLE
				 * BOUND. - this process help to increase the tappable area of
				 * the rectangle.
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
				 * getWidth() will result in negative value. So to avoid this
				 * add the value previous added when x goes negative.
				 */

				if (x <= 0) {
					x += extraTapArea;
				}

				/*
				 * If result after calculating for extra tappable area is
				 * negative. assign the original value so that after subtracting
				 * extratapping area value doesn't go into negative value.
				 */

				if (y <= 0)
					y = actionY;

				/**
				 * If drawble bounds contains the x and y points then move
				 * ahead.
				 */
				if (bounds.contains(x, y) && clickListener != null) {
					clickListener
							.onClick(DrawableClickListener.DrawablePosition.RIGHT);
					event.setAction(MotionEvent.ACTION_CANCEL);
					return false;
				}
				return super.onTouchEvent(event);
			}

		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void finalize() throws Throwable {
		drawableRight = null;
		drawableBottom = null;
		drawableLeft = null;
		drawableTop = null;
		super.finalize();
	}

	public void setDrawableClickListener(DrawableClickListener listener) {
		this.clickListener = listener;
	}

}

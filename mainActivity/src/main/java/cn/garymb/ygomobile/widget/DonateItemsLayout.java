package cn.garymb.ygomobile.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class DonateItemsLayout extends LinearLayout {

	public interface ItemSelectListener {
		void onDonateItemSelected(int id);
	}

	private static final int MAX_DONATE_INIT_ITEMS = 5;

	private static final String TAG = "DonateItemsLayout";

	private List<DonateItem> mItems;

	private int mTouchedPostion;
	
	private ItemSelectListener mListener;

	public DonateItemsLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DonateItemsLayout(Context context) {
		super(context);
		init();
	}

	private void init() {
		mItems = new ArrayList<DonateItem>(MAX_DONATE_INIT_ITEMS);
	}
	
	public void setItemSelectedListener(ItemSelectListener listener) {
		mListener = listener;
	}


	@Override
	public void addView(View child) {
		if (!(child instanceof DonateItem)) {
			Log.w(TAG, "DonateItemsLayout must use DonateItem as child!");
			return;
		}
		addToItemsList((DonateItem) child);
		super.addView(child);
	}

	@Override
	public void addView(View child, int width, int height) {
		if (!(child instanceof DonateItem)) {
			Log.w(TAG, "DonateItemsLayout must use DonateItem as child!");
			return;
		}
		addToItemsList((DonateItem) child);
		super.addView(child, width, height);
	}

	@Override
	public void addView(View child, android.view.ViewGroup.LayoutParams params) {
		if (!(child instanceof DonateItem)) {
			Log.w(TAG, "DonateItemsLayout must use DonateItem as child!");
			return;
		}
		addToItemsList((DonateItem) child);
		super.addView(child, params);
	}

	private void addToItemsList(DonateItem item) {
		mItems.add(item);
	}

	@Override
	public void removeAllViews() {
		super.removeAllViews();
		mItems.clear();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean handled = true;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchedPostion = findMotionRow((int)event.getY());
			if (mTouchedPostion == -1) {
				handled = false;
			} else {
				mItems.get(mTouchedPostion).setPressed(true);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			handled = false;
			for (DonateItem item : mItems) {
				if (item != null) {
					item.setPressed(false);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			for (DonateItem item : mItems) {
				if (item != null) {
					item.setPressed(false);
					item.setChecked(false);
				}
			}
			if (mTouchedPostion != -1) {
				mItems.get(mTouchedPostion).setChecked(true);
				if (mListener != null) {
					mListener.onDonateItemSelected(mItems.get(mTouchedPostion).getId());
				}
			}
			break;
		default:
			break;
		}
		if (handled) {
			postInvalidate();
		} else  {
			handled = super.onTouchEvent(event);
		}
		return handled;
	}

	int findMotionRow(int y) {
		int childCount = getChildCount();
		if (childCount > 0) {
			for (int i = childCount - 1; i >= 0; i--) {
				View v = getChildAt(i);
				if (y >= v.getTop()) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public void setCurrentChoice(int position) {
		for (DonateItem item : mItems) {
			if (item != null) {
				item.setChecked(false);
			}
		}
		mItems.get(position).setChecked(true);
	}

}

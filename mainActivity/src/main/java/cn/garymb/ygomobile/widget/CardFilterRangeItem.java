package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.ygo.ICardFilter;
import cn.garymb.ygomobile.ygo.YGOCardSelectionBuilder;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CardFilterRangeItem extends LinearLayout implements ICardFilter {

	private static final String TAG = "CardFilterDialogItem";

	private static final String PREF_KEY_LAST_MAX = "max";

	private static final String PREF_KEY_LAST_MIN = "min";

	private int mMax = -1;

	private int mMin = -1;

	private ICardFilter mCardFilterDelegate;

	private OnCardFilterChangeListener mListener;

	private TextView mDes;

	public CardFilterRangeItem(Context context) {
		this(context, null);
	}

	public CardFilterRangeItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mDes = (TextView) findViewById(R.id.des);
	}
	
	@Override
	public void setId(int id) {
		super.setId(id);
		loadLastRange();
		if (mDes != null) {
			fillDesText();
		}
	}

	private void fillDesText() {
		if (mMax != -1 && mMin != -1) {
			if (mMax != -1 || mMin != -1) {
				mDes.setText("...");
			}
		}
	}
	
	public int getMax() {
		return mMax;
	}
	
	public int getMin() {
		return mMin;
	}

	@Override
	public void onFilter(int type, int arg1, int arg2, Object obj) {
		if (mCardFilterDelegate != null) {
			mCardFilterDelegate.onFilter(type, arg1, arg2, obj);
		}
	}

	@Override
	public void resetFilter() {
		mMax = mMin = 0;
		saveLastRange();
		mListener = null;
		if (mCardFilterDelegate != null) {
			mCardFilterDelegate.resetFilter();
		}
	}

	private void saveLastRange() {
		SharedPreferences.Editor editor = getContext().getSharedPreferences(
				TAG + getId(), Context.MODE_PRIVATE).edit();
		editor.putInt(PREF_KEY_LAST_MAX, mMax);
		editor.putInt(PREF_KEY_LAST_MIN, mMin);
		editor.commit();
	}

	private void loadLastRange() {
		SharedPreferences sp = getContext().getSharedPreferences(TAG + getId(),
				Context.MODE_PRIVATE);
		mMax = sp.getInt(PREF_KEY_LAST_MAX, -1);
		mMin = sp.getInt(PREF_KEY_LAST_MIN, -1);
	}

	public void setRange(int type, int from, int to) {
		if (mMax != to || mMin != from) {
			mMax = to;
			mMin = from;
			saveLastRange();
			fillDesText();
			onFilter(type, from, to, null);
			mListener.onChange(YGOCardSelectionBuilder.SELECTION_SEGMENT_TOTAL, buildSelection());
		}
	}

	@Override
	public String buildSelection() {
		if (mCardFilterDelegate != null) {
			return mCardFilterDelegate.buildSelection();
		}
		return null;
	}

	public void setCardFilterChangeListener(OnCardFilterChangeListener listener) {
		mListener = listener;
	}

	public void setCardFilterDelegate(ICardFilter filter) {
		mCardFilterDelegate = filter;
	}

}

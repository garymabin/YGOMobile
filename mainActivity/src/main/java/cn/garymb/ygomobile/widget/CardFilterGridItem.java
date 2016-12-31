package cn.garymb.ygomobile.widget;

import java.util.ArrayList;
import java.util.List;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.ygo.ICardFilter;
import cn.garymb.ygomobile.ygo.YGOCardSelectionBuilder;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CardFilterGridItem extends LinearLayout implements ICardFilter{
	
	private static final String PREF_KEY_SELECTION = "selection";
	private static final String PREF_KEY_SELECTION_COUNT = "selection_count";
	private static final String TAG = "CardFilterGridItem";

	private List<Integer> mSelection = new ArrayList<Integer>();
	
	private ICardFilter mCardFilterDelegate;

	private OnCardFilterChangeListener mListener;

	private TextView mDes;

	public CardFilterGridItem(Context context) {
		this(context, null);
	}

	public CardFilterGridItem(Context context, AttributeSet attrs) {
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
		loadLastSelection();
		if (mDes != null) {
			fillDesText();
		}
	}

	private void fillDesText() {
		if (mSelection.size() != 0) {
			mDes.setText("...");
		}
	}
	

	@Override
	public void onFilter(int type, int arg1, int arg2, Object obj) {
		if (mCardFilterDelegate != null) {
			mCardFilterDelegate.onFilter(type, arg1, arg2, obj);
		}
	}

	@Override
	public void resetFilter() {
		mSelection.clear();
		saveLastSelection();
		mListener = null;
		if (mCardFilterDelegate != null) {
			mCardFilterDelegate.resetFilter();
		}
	}

	private void saveLastSelection() {
		SharedPreferences.Editor editor = getContext().getSharedPreferences(
				TAG + getId(), Context.MODE_PRIVATE).edit();
		int size = mSelection.size();
		editor.putInt(PREF_KEY_SELECTION_COUNT, size);
		for (int i = 0; i < size; i++) {
			editor.putInt(PREF_KEY_SELECTION + i, mSelection.get(i));	
		}
		editor.commit();
	}

	private void loadLastSelection() {
		SharedPreferences sp = getContext().getSharedPreferences(TAG + getId(),
				Context.MODE_PRIVATE);
 		int size = sp.getInt(PREF_KEY_SELECTION_COUNT, 0);
 		for(int i = 0; i < size; i++) {
 			mSelection.add(i, sp.getInt(PREF_KEY_SELECTION + i, 0));
 		}
	}

	public void setSelection(int type, List<Integer> selection) {
		if (selection != null && !mSelection.equals(selection)) {
			mSelection.clear();
			mSelection.addAll(selection);
			saveLastSelection();
			fillDesText();
			onFilter(type, -1, -1, mSelection);
			mListener.onChange(YGOCardSelectionBuilder.SELECTION_SEGMENT_TOTAL, buildSelection());
		}
	}
	
	public List<Integer> getSelection() {
		return mSelection;
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

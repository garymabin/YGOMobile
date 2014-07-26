package cn.garymb.ygomobile.ygo;

import android.text.TextUtils;

public class YGOCardSelectionBuilder {
	
	public static final int SELECTION_SEGMENT_TOTAL = 0;
	public static final int SELECTION_SEGMENT_SEARCH = 1;
	
	private StringBuilder mSelectionBuilder;
	
	private String mTotalSelection;
	
	public YGOCardSelectionBuilder() {
		mSelectionBuilder = new StringBuilder();
	}
	
	public YGOCardSelectionBuilder setSelection(int type, String selection) {
		switch (type) {
		case SELECTION_SEGMENT_TOTAL:
			mSelectionBuilder.delete(0, mSelectionBuilder.length());
			if (!TextUtils.isEmpty(selection)) {
				mTotalSelection = selection;
				mSelectionBuilder.append(selection);
			}
			break;
		case SELECTION_SEGMENT_SEARCH:
			mSelectionBuilder.delete(0, mSelectionBuilder.length());
			if (!TextUtils.isEmpty(mTotalSelection)) {
				mSelectionBuilder.append(mTotalSelection).append(" AND ");
			}
			if (!TextUtils.isEmpty(selection)) {
				mSelectionBuilder.append(selection);
			} else {
				if (mSelectionBuilder.length() > 4) {
					mSelectionBuilder.delete(mSelectionBuilder.length() - 4, mSelectionBuilder.length());
				}
			}
		default:
			break;
		}
		return this;
	}
	
	@Override
	public String toString() {
		return mSelectionBuilder.length() == 0 ? null : mSelectionBuilder.toString();
	}
	

}

package cn.garymb.ygomobile.widget.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;

public class MyBooleanValuePreference extends Preference {

	boolean mChecked;

	public MyBooleanValuePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyBooleanValuePreference(Context context) {
		super(context);
	}

	public void setChecked(boolean checked) {
		// Always persist/notify the first time; don't assume the field's
		// default of false.
		final boolean changed = mChecked != checked;
		if (changed) {
			mChecked = checked;
			persistBoolean(checked);
			if (changed) {
				notifyChanged();
			}
		}
	}

	/**
	 * Returns the checked state.
	 * 
	 * @return The checked state.
	 */
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getBoolean(index, false);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		setChecked(restoreValue ? getPersistedBoolean(mChecked)
				: (Boolean) defaultValue);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			// No need to save instance state since it's persistent
			return superState;
		}

		final SavedState myState = new SavedState(superState);
		myState.checked = isChecked();
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		setChecked(myState.checked);
	}

	static class SavedState extends BaseSavedState {
		boolean checked;

		public SavedState(Parcel source) {
			super(source);
			checked = source.readInt() == 1;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(checked ? 1 : 0);
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

}

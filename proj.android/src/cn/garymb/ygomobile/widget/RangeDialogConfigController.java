package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import android.content.Context;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RangeDialogConfigController extends BaseDialogConfigController implements TextWatcher {
	
	public static final int RANGE_DIALOG_TYPE_ATK = 0;
	public static final int RANGE_DIALOG_TYPE_DEF = 1;
	
	private EditText mFrom;
	private EditText mTo;

	public RangeDialogConfigController(DialogConfigUIBase configUI, View view, int type, int max, int min) {
		super(configUI, view);
		final Context context = configUI.getContext();
		final Resources res = context.getResources();
		
		mFrom = (EditText) view.findViewById(R.id.edit_text_from);
		mTo = (EditText) view.findViewById(R.id.edit_text_to);
		
		mFrom.addTextChangedListener(this);
		mTo.addTextChangedListener(this);
		
		if (min != -1) {
			mFrom.setText(min + "");
		}
		
		if (max != -1) {
			mTo.setText(max + "");
		}
		
		mConfigUI.setPositiveButton(res.getString(R.string.action_filter));
		mConfigUI.setCancelButton(res.getString(R.string.button_cancel));
		
		if (type == RANGE_DIALOG_TYPE_ATK) {
			configUI.setTitle(R.string.action_filter_atk);
		} else if (type == RANGE_DIALOG_TYPE_DEF) {
			configUI.setTitle(R.string.action_filter_def);
		}
	}
	
	public int getMaxValue() {
		String fromString = mFrom.getText().toString();
		String toString = mTo.getText().toString();
		if (TextUtils.isEmpty(fromString)) {
			return Integer.parseInt(toString);
		}
		if (TextUtils.isEmpty(toString)) {
			return Integer.parseInt(fromString);
		}
		
		int from = Integer.parseInt(fromString);
		int to = Integer.parseInt(toString);
		return from > to ? from : to;
		
	}
	
	public int getMinValue() {
		String fromString = mFrom.getText().toString().trim();
		String toString = mTo.getText().toString().trim();
		if (TextUtils.isEmpty(fromString)) {
			return Integer.parseInt(toString);
		}
		if (TextUtils.isEmpty(toString)) {
			return Integer.parseInt(fromString);
		}
		
		int from = Integer.parseInt(fromString);
		int to = Integer.parseInt(toString);
		return from > to ? to : from;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		enableSubmitIfAppropriate();
	}

	@Override
	public int enableSubmitIfAppropriate() {
		Button positive = mConfigUI.getPositiveButton();
		if (positive == null)
			return 0;
		if (TextUtils.isEmpty(mFrom.getText().toString().trim()) && TextUtils.isEmpty(mTo.getText().toString().trim())) {
			positive.setEnabled(false);
		} else  {
			positive.setEnabled(true);
		}
		return 0;
	}

}

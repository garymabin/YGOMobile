package cn.garymb.ygomobile.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;

public class BaseDialog extends AlertDialog implements DialogConfigUIBase{

	protected View mView;
	protected BaseDialogConfigController mController;
	protected DialogInterface.OnClickListener mListener;

	public BaseDialog(Context context, DialogInterface.OnClickListener listener) {
		super(context);
		mListener = listener;
	}

	@Override
	public BaseDialogConfigController getController() {
		return mController;
	}

	@Override
	public void setPositiveButton(CharSequence text) {
		setButton(BUTTON_POSITIVE, text, mListener);
	}

	@Override
	public void setCancelButton(CharSequence text) {
		setButton(BUTTON_NEGATIVE, text, mListener);
	}

	@Override
	public Button getPosiveButton() {
		return getButton(BUTTON_POSITIVE);
	}

	@Override
	public Button getCancelButton() {
		return getButton(BUTTON_NEGATIVE);
	}

}
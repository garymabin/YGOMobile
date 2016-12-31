package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public abstract class BaseDialog extends AlertDialog implements DialogConfigUIBase {
	
	private static final String SAVED_STATE_PARAM = "basedialog.saved.param";

	protected BaseDialogConfigController mController;
	protected DialogInterface.OnClickListener mListener;
	protected Bundle mParam;
	protected View mView;
	
	private boolean mIsControllerInitialized = false;
	
	
	public BaseDialog(Context context,
			DialogInterface.OnClickListener listener, View view, Bundle param) {
		super(context);
		mView = view;
		mListener = listener;
		mParam = param;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setView(mView);
		if (mParam != null) {
			mController = createController(mView);
			mIsControllerInitialized = true;
		}
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		mParam = savedInstanceState.getBundle(SAVED_STATE_PARAM);
		if (!mIsControllerInitialized) {
			mController = createController(mView);
			mIsControllerInitialized = true;
		}
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	@Override
	public Bundle onSaveInstanceState() {
		Bundle bundle = super.onSaveInstanceState();
		bundle.putBundle(SAVED_STATE_PARAM, mParam);
		return bundle;
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
	public Button getPositiveButton() {
		return getButton(BUTTON_POSITIVE);
	}

	@Override
	public Button getCancelButton() {
		return getButton(BUTTON_NEGATIVE);
	}

	@Override
	public void show() {
		super.show();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			final Resources res = getContext().getResources();
			// Title
			final int titleId = res
					.getIdentifier("alertTitle", "id", "android");
			final View title = findViewById(titleId);
			if (title != null) {
				((TextView) title).setTextColor(res
						.getColor(R.color.apptheme_color));
			}
			// Title divider
			final int titleDividerId = res.getIdentifier("titleDivider", "id",
					"android");
			final View titleDivider = findViewById(titleDividerId);
			if (titleDivider != null) {
				titleDivider.setBackgroundColor(res
						.getColor(R.color.apptheme_color));
			}
		}
	}
	
	@Override
	public void dismiss() {
		super.dismiss();
		mView = null;
		mIsControllerInitialized = false;
	}
	
	protected abstract BaseDialogConfigController createController(View view);
}
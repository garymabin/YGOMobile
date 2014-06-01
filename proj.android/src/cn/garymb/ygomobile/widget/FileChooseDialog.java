package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FileChooseDialog extends AlertDialog implements DialogConfigUIBase{
	
	private OnClickListener mListener;
	
	private FileChooseController mController;
	
	private Bundle mParam;

	public FileChooseDialog(Context context, OnClickListener listener, Bundle param) {
		super(context);
		mListener = listener;
		mParam = param;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.file_browser_layout, null);
		setView(view);
		mController = new FileChooseController(this, view, mParam);
		super.onCreate(savedInstanceState);
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
	
	@Override
	public void show() {
		super.show();
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

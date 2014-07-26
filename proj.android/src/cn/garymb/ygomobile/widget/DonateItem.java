package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

public class DonateItem extends LinearLayout {
	
	private ImageView mIcon;
	
	private TextView mDescriptionView;
	
	private RadioButton mRadioButton;
	
	private boolean isInfateFinished;

	public DonateItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DonateItem(Context context) {
		super(context);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mIcon = (ImageView) findViewById(R.id.donate_method_icon);
		mDescriptionView  = (TextView) findViewById(R.id.donate_method_des);
		mRadioButton = (RadioButton) findViewById(R.id.donate_method_radio_button);
		isInfateFinished = true;
	}
	
	public void setIcon(int resId) {
		if (isInfateFinished) {
			mIcon.setImageResource(resId);
		}
	}
	
	public void setDescription(int resId) {
		if (isInfateFinished) {
			mDescriptionView.setText(resId);
		}
	}
	
	public void setChecked(boolean isChecked) {
		mRadioButton.setChecked(isChecked);
	}
	
	public boolean isChecked() {
		return mRadioButton.isChecked();
	}
}

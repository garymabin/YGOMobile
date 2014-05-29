package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

public class DonateDialog extends BaseDialog {
	
	public DonateDialog(Context context, OnClickListener listener) {
		super(context, listener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mView = LayoutInflater.from(getContext()).inflate(R.layout.donate_content, null);
		setView(mView);
		mController = new DonateDialogConfigController(this, mView);
		super.onCreate(savedInstanceState);
	}

}

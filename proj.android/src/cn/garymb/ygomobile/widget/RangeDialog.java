package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import android.content.Context;
import android.os.Bundle;

public class RangeDialog extends BaseDialog {
	
	private int mType;
	
	private int mCurrentMax;
	
	private int mCurrentMin;

	public RangeDialog(Context context, OnClickListener listener, int type, Bundle bundle) {
		super(context, listener);
		mType = type;
		mCurrentMax = bundle.getInt("max");
		mCurrentMin = bundle.getInt("min");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mView = getLayoutInflater().inflate(R.layout.range_dialog_content, null);
		setView(mView);
		mController = new RangeDialogConfigController(this, mView, mType, mCurrentMax, mCurrentMin);
		super.onCreate(savedInstanceState);
	}

}

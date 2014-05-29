package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import android.content.Context;
import android.os.Bundle;

public class ServerCreateDialog extends BaseDialog {
	
	private Bundle mParam;

	public ServerCreateDialog(Context context, OnClickListener listener, Bundle bundle) {
		super(context, listener);
		mParam = bundle;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mView = getLayoutInflater().inflate(R.layout.create_server_content, null);
		setView(mView);
		mController = new ServerDialogController(this, mView, mParam);
		super.onCreate(savedInstanceState);
		((ServerDialogController) mController).enableSubmitIfAppropriate();
	}

}

package cn.garymb.ygomobile.common;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.utils.DatabaseUtils;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class CardDBResetTask extends AsyncTask<Void, Void, Boolean> {

	public interface CardDBResetListener {
		void onCardDBResetFinished(Boolean result);
	}

	private ProgressDialog mWaitDialog;

	private CardDBResetListener mListener;

	public CardDBResetTask(Context context) {
		mWaitDialog = new ProgressDialog(context);
		mWaitDialog.setMessage(context.getResources().getString(
				R.string.reseting_card_db));
		mWaitDialog.setCancelable(false);
	}

	public void setCardDBResetListener(CardDBResetListener listener) {
		mListener = listener;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mWaitDialog.show();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		String dataBasePath = StaticApplication.peekInstance().getDataBasePath();
		Boolean result = DatabaseUtils.checkAndCopyFromInternalDatabase(StaticApplication.peekInstance(), dataBasePath, true);
		return result;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		mWaitDialog.dismiss();
		mWaitDialog = null;
		if (mListener != null && result != null) {
			mListener.onCardDBResetFinished(result);
		}
		mListener = null;
	}

}

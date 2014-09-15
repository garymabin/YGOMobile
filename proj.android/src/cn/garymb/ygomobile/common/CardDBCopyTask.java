package cn.garymb.ygomobile.common;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.utils.DatabaseUtils;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class CardDBCopyTask extends AsyncTask<String, Void, Boolean> {

	public interface CardDBCopyListener {
		void onCardDBCopyFinished(Boolean result);
	}

	private ProgressDialog mWaitDialog;

	private CardDBCopyListener mListener;

	public CardDBCopyTask(Context context) {
		mWaitDialog = new ProgressDialog(context);
		mWaitDialog.setMessage(context.getResources().getString(
				R.string.loading_card_db));
		mWaitDialog.setCancelable(false);
	}

	public void setCardDBCopyListener(CardDBCopyListener listener) {
		mListener = listener;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mWaitDialog.show();
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String dataBasePath = StaticApplication.peekInstance().getDataBasePath();
		Boolean result = DatabaseUtils.checkAndCopyFromExternalDatabase(StaticApplication.peekInstance(),
				params[0], dataBasePath, true);
		if (!result) {
			mWaitDialog.setMessage(StaticApplication.peekInstance()
					.getResources().getString(R.string.resume_card_db));
			DatabaseUtils.checkAndCopyFromInternalDatabase(
					StaticApplication.peekInstance(), dataBasePath, true);
		}
		return result;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		mWaitDialog.dismiss();
		mWaitDialog = null;
		if (mListener != null && result != null) {
			mListener.onCardDBCopyFinished(result);
		}
		mListener = null;
	}

}

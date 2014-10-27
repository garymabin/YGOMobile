package cn.garymb.ygomobile.common;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.utils.DatabaseUtils;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

public class CardDBCopyTask extends AsyncTask<String, Integer, Integer> {
	
	public static final int COPY_DB_TASK_SUCCESS = 0;
	
	public static final int COPY_DB_TASK_FAILED = -1;
	
	public static final int COPY_DB_TASK_FILE_NOT_EXIST = -2;

	public interface CardDBCopyListener {
		void onCardDBCopyFinished(int result);
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
	protected Integer doInBackground(String... params) {
		String dataBasePath = StaticApplication.peekInstance()
				.getDataBasePath();
		Integer result = COPY_DB_TASK_SUCCESS;
		if (TextUtils.isEmpty(params[0])) {
			result = COPY_DB_TASK_FILE_NOT_EXIST;
		} else {
			if (!DatabaseUtils.checkAndCopyFromExternalDatabase(
					StaticApplication.peekInstance(), params[0],
					dataBasePath, true)) {
				result = COPY_DB_TASK_FAILED;
			} 
		}
		publishProgress(result);
		if (result == COPY_DB_TASK_FAILED) {
			DatabaseUtils.checkAndCopyFromInternalDatabase(
					StaticApplication.peekInstance(), dataBasePath, true);
		}
		return result;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		if (values[0] == COPY_DB_TASK_FAILED) {
			mWaitDialog.setMessage(StaticApplication.peekInstance()
					.getResources().getString(R.string.resume_card_db));
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		mWaitDialog.dismiss();
		mWaitDialog = null;
		if (mListener != null && result != null) {
			mListener.onCardDBCopyFinished(result);
		}
		mListener = null;
	}

}

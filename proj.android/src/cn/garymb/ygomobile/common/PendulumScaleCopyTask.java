package cn.garymb.ygomobile.common;

import java.io.File;
import java.io.IOException;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.utils.FileOpsUtils;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class PendulumScaleCopyTask extends AsyncTask<String, Void, Void> {

	private ProgressDialog mWaitDialog;

	private Context mContext;

	public PendulumScaleCopyTask(Context context) {
		mContext = context;
		mWaitDialog = new ProgressDialog(context);
		mWaitDialog.setMessage(context.getResources().getString(
				R.string.copying_image));
		mWaitDialog.setCancelable(false);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mWaitDialog.show();
	}

	@Override
	protected Void doInBackground(String... params) {
		checkAndCopyGameSkin(params[0], params[1]);
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		mWaitDialog.dismiss();
		mWaitDialog = null;
	}

	private void checkAndCopyGameSkin(String assetPath, String path) {
		File coreSkinDir = new File(path);
		if (coreSkinDir != null && coreSkinDir.exists()
				&& coreSkinDir.isDirectory()) {
			return;
		}
		if (coreSkinDir != null && coreSkinDir.exists()
				&& !coreSkinDir.isDirectory()) {
			coreSkinDir.delete();
		}
		// we need to copy from configs from assets;
		int assetcopycount = 0;
		while (assetcopycount++ < 3) {
			try {
				FileOpsUtils.assetsCopy(mContext, assetPath,
						coreSkinDir.getAbsolutePath(), false);
				break;
			} catch (IOException e) {
				continue;
			}
		}
	}
}

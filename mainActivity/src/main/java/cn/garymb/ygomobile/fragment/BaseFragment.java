package cn.garymb.ygomobile.fragment;


import cn.garymb.ygomobile.MainActivity;
import cn.garymb.ygomobile.common.Constants;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

public abstract class BaseFragment extends Fragment implements Handler.Callback, Constants, FragmentNavigationListener{

	public interface OnActionBarChangeCallback {
		void onActionBarChange(int msgType, int action, int arg1, Object extra);
	}

	/**
	 * @author mabin
	 * 
	 */
	public static class DataHandler extends Handler {
		/**
		 * 
		 */
		public DataHandler(Looper looper, Callback callback) {
			super(looper, callback);
		}
	}
	
	public static final String ARG_ITEM_TITLE = "basefragment.title";

	protected MainActivity mActivity;
	protected DataHandler mHandler;
	protected OnActionBarChangeCallback mActionBarCallback;
	
	private String mTitle;
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle param = getArguments();
		if (param != null) {
			mTitle = param.getString(ARG_ITEM_TITLE);
			setTitle();
		}
	}

	protected void setTitle() {
		if (mTitle != null) {
			mActivity.setTitle(mTitle);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
		mHandler = new DataHandler(mActivity.getMainLooper(), this);
		if (activity instanceof OnActionBarChangeCallback) {
			mActionBarCallback = (OnActionBarChangeCallback) activity;
		}
	}
	
	public void showDialog(Bundle params, Fragment target, int requestCode) {
		// DialogFragment.show() will take care of adding the fragment
		// in a transaction. We also want to remove any currently showing
		// dialog, so make our own transaction and take care of that here.
		FragmentTransaction ft = getChildFragmentManager().beginTransaction();
		Fragment prev = getChildFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		CustomDialogFragment newFragment = CustomDialogFragment
				.newInstance(params);
		newFragment.setTargetFragment(target, requestCode);
		newFragment.show(ft, "dialog");
	}
	
	public void showDialog(Bundle params) {
		// DialogFragment.show() will take care of adding the fragment
		// in a transaction. We also want to remove any currently showing
		// dialog, so make our own transaction and take care of that here.
		FragmentTransaction ft = getChildFragmentManager().beginTransaction();
		Fragment prev = getChildFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		CustomDialogFragment newFragment = CustomDialogFragment
				.newInstance(params);
		newFragment.show(ft, "dialog");
	}

	@Override
	public void onEventFromChild(int requestCode, int eventType, int arg1, int arg2, Object data) {
	}

	@Override
	public boolean handleMessage(Message msg) {
		return false;
	}
}

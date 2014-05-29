package cn.garymb.ygomobile.fragment;

import java.util.List;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobie.model.Model;
import cn.garymb.ygomobile.MainActivity;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import cn.garymb.ygomobile.widget.BaseDialog;
import cn.garymb.ygomobile.widget.DonateDialog;
import cn.garymb.ygomobile.widget.DonateDialogConfigController;
import cn.garymb.ygomobile.widget.GridSelectionDialog;
import cn.garymb.ygomobile.widget.GridSelectionDialogController;
import cn.garymb.ygomobile.widget.RangeDialog;
import cn.garymb.ygomobile.widget.RangeDialogConfigController;
import cn.garymb.ygomobile.widget.ServerCreateDialog;
import cn.garymb.ygomobile.widget.ServerDialogController;
import cn.garymb.ygomobile.ygo.YGOServerInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

/**
 * @author mabin
 * 
 */
public class CommonDialogFragment extends DialogFragment implements
		OnTouchListener, ResourcesConstants, OnClickListener {


	private static final String TAG = "CommonDialogFragment";

	private MainActivity mActivity;

	private int mDialogMode;

	public static CommonDialogFragment newInstance(Bundle bundle) {
		CommonDialogFragment f = new CommonDialogFragment();
		f.setArguments(bundle);
		return f;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.DialogFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		final Resources res = getResources();
        // Title
        final int titleId = res.getIdentifier("alertTitle", "id", "android");
        final View title = getDialog().findViewById(titleId);
        if (title != null) {
            ((TextView) title).setTextColor(res.getColor(R.color.apptheme_color));
        }

        // Title divider
        final int titleDividerId = res.getIdentifier("titleDivider", "id", "android");
        final View titleDivider = getDialog().findViewById(titleDividerId);
        if (titleDivider != null) {
            titleDivider.setBackgroundColor(res.getColor(R.color.apptheme_color));
        }
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDialogMode = getArguments().getInt(MODE_OPTIONS);
		setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onViewCreated(android.view.View,
	 * android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		view.setOnTouchListener(this);
		super.onViewCreated(view, savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dlg = null;
		switch (mDialogMode) {
		case ResourcesConstants.DIALOG_MODE_DONATE:
			dlg = new DonateDialog(mActivity, this);
			break;
		case ResourcesConstants.DIALOG_MODE_FILTER_ATK:
			dlg = new RangeDialog(mActivity, this, RangeDialogConfigController.RANGE_DIALOG_TYPE_ATK, getArguments());
			break;
		case ResourcesConstants.DIALOG_MODE_FILTER_DEF:
			dlg = new RangeDialog(mActivity, this, RangeDialogConfigController.RANGE_DIALOG_TYPE_DEF, getArguments());
			break;
		case ResourcesConstants.DIALOG_MODE_FILTER_LEVEL:
			dlg = new GridSelectionDialog(mActivity, this, R.array.card_level, getArguments());
			break;
		case ResourcesConstants.DIALOG_MODE_FILTER_EFFECT:
			dlg = new GridSelectionDialog(mActivity, this, R.array.card_effect, getArguments());
			break;
		case ResourcesConstants.DIALOG_MODE_ADD_NEW_SERVER:
		case ResourcesConstants.DIALOG_MODE_EDIT_SERVER:
			dlg = new ServerCreateDialog(mActivity, this, getArguments());
			break;
		default:
			break;
		}
		return dlg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View,
	 * android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == AlertDialog.BUTTON_POSITIVE) {
			switch (mDialogMode) {
			case ResourcesConstants.DIALOG_MODE_DONATE: {
				Intent intent = new Intent();
				int method = ((DonateDialogConfigController) ((DonateDialog)dialog).getController()).getAlipayDonateMethod();
				switch (method) {
				case DonateDialogConfigController.DONATE_METHOD_ALIPAY_MOBILE_APP_INSTALLED:
					ComponentName component = new ComponentName("com.eg.android.AlipayGphone", "com.eg.android.AlipayGphone.AlipayLogin");
					intent.setComponent(component);
					intent.setAction(Intent.ACTION_VIEW);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					String alipayVersionString = ((DonateDialogConfigController) ((DonateDialog)dialog).getController()).getAlipayVersionString();
					Log.d(TAG, "alipay version = " + alipayVersionString);
					String urlString = String.format("alipayqr://platformapi/startapp?saId=10000007&clientVersion=%s&qrcode=%s",
							alipayVersionString, ResourcesConstants.DONATE_URL_MOBILE);
					intent.setData(Uri.parse(urlString));
					break;
				case DonateDialogConfigController.DONATE_METHOD_ALIPAY_MOBILE_APP_NOT_INSTALLED:
					intent.setAction(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(ResourcesConstants.DONATE_URL_MOBILE));
					break;
				case DonateDialogConfigController.DONATE_METHOD_ALIPAY_WAP:
					intent.setAction(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(ResourcesConstants.DONATE_URL_WAP));
					break;
				default:
					break;
				}
				startActivity(intent);
				break;
			}
			case ResourcesConstants.DIALOG_MODE_FILTER_ATK:
			case ResourcesConstants.DIALOG_MODE_FILTER_DEF: {
				int max = ((RangeDialogConfigController)((BaseDialog)getDialog()).getController()).getMaxValue();
				int min = ((RangeDialogConfigController)((BaseDialog)getDialog()).getController()).getMinValue();;
				((BaseFragment)getTargetFragment()).onEventFromChild(getTargetRequestCode(), FragmentNavigationListener.FRAGMENT_NAVIGATION_CARD_EVENT, min, max, null);
				break;
			}
			case ResourcesConstants.DIALOG_MODE_FILTER_LEVEL:
			case ResourcesConstants.DIALOG_MODE_FILTER_EFFECT: {
				List<Integer> list = ((GridSelectionDialogController)((BaseDialog)getDialog()).getController()).getSelections();
				((BaseFragment)getTargetFragment()).onEventFromChild(getTargetRequestCode(), FragmentNavigationListener.FRAGMENT_NAVIGATION_CARD_EVENT, -1, -1, list);
				break;
			}
			case ResourcesConstants.DIALOG_MODE_ADD_NEW_SERVER:
			case ResourcesConstants.DIALOG_MODE_EDIT_SERVER:
				YGOServerInfo info = ((ServerDialogController)((BaseDialog)getDialog()).getController()).getServerInfo();
				Model.peekInstance().addNewServer(info);
				((BaseFragment)getTargetFragment()).onEventFromChild(getTargetRequestCode(), FragmentNavigationListener.FRAGMENT_NAVIGATION_DUEL_CREATE_SERVER_EVENT, -1, -1, null);
			default:
				break;
			}
			
		} else if (which == AlertDialog.BUTTON_NEGATIVE) {
		}
		
	}
}

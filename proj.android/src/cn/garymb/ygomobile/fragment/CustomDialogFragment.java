package cn.garymb.ygomobile.fragment;

import java.util.List;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import cn.garymb.ygodata.YGOGameOptions;
import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.YGOMobileActivity;
import cn.garymb.ygomobile.core.Controller;
import cn.garymb.ygomobile.model.Model;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import cn.garymb.ygomobile.widget.AppUpdateController;
import cn.garymb.ygomobile.widget.BaseDialogConfigController;
import cn.garymb.ygomobile.widget.DialogConfigUIBase;
import cn.garymb.ygomobile.widget.DonateDialogConfigController;
import cn.garymb.ygomobile.widget.GridSelectionDialogController;
import cn.garymb.ygomobile.widget.RangeDialogConfigController;
import cn.garymb.ygomobile.widget.RoomDialogConfigController;
import cn.garymb.ygomobile.widget.ServerDialogController;
import cn.garymb.ygomobile.ygo.YGOServerInfo;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

public class CustomDialogFragment extends SimpleDialogFragment implements OnClickListener, ResourcesConstants, OnTouchListener, OnShowListener {
	
	public class SimpleDialogConfigUiBase implements DialogConfigUIBase {
		
		private Builder mBuilder;
		
		public SimpleDialogConfigUiBase(Builder builder) {
			mBuilder = builder;
		}

		@Override
		public Context getContext() {
			return getActivity();
		}

		@Override
		public BaseDialogConfigController getController() {
			return mController;
		}

		@Override
		public void setPositiveButton(CharSequence text) {
			mBuilder.setPositiveButton(text, CustomDialogFragment.this);
		}

		@Override
		public void setCancelButton(CharSequence text) {
			mBuilder.setNegativeButton(text, CustomDialogFragment.this);
		}

		@Override
		public Button getPosiveButton() {
			return getPositiveButton();
		}

		@Override
		public Button getCancelButton() {
			return getNegativeButton();
		}

		@Override
		public void setTitle(CharSequence text) {
			mBuilder.setTitle(text);
		}

		@Override
		public void setTitle(int resId) {
			mBuilder.setTitle(resId);
		}

	}
	
	public static CustomDialogFragment newInstance(Bundle bundle) {
		CustomDialogFragment f = new CustomDialogFragment();
		f.setArguments(bundle);
		return f;
	}

	private static final String TAG = "CommonDialogFragment";

	private BaseDialogConfigController mController;
	
	private SimpleDialogConfigUiBase mSimpleUiWrapper;
	
	private int mDialogMode;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDialogMode = getArguments().getInt(MODE_OPTIONS);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dlg = super.onCreateDialog(savedInstanceState);
		dlg.setOnShowListener(this);
		return dlg;
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
	
	@Override
	protected Builder build(Builder builder) {
		mSimpleUiWrapper = new SimpleDialogConfigUiBase(builder);
		View content = null;
		LayoutInflater inflater = builder.getLayoutInflater();
		switch (mDialogMode) {
		case ResourcesConstants.DIALOG_MODE_CREATE_ROOM:
		case ResourcesConstants.DIALOG_MODE_QUICK_JOIN:
		case ResourcesConstants.DIALOG_MODE_JOIN_GAME: {
			Bundle param = getArguments();
			content = inflater.inflate(R.layout.room_detail_content, null);
			mController = new RoomDialogConfigController(mSimpleUiWrapper, content, param);
			break;
		}
		case ResourcesConstants.DIALOG_MODE_DONATE:
			content = inflater.inflate(R.layout.donate_content, null);
			mController = new DonateDialogConfigController(mSimpleUiWrapper, content);
			break;
		case ResourcesConstants.DIALOG_MODE_FILTER_ATK: {
			content = inflater.inflate(R.layout.range_dialog_content, null);
			Bundle param = getArguments();
			int max = param.getInt("max");
			int min = param.getInt("min");
			mController = new RangeDialogConfigController(mSimpleUiWrapper, content, 
					RangeDialogConfigController.RANGE_DIALOG_TYPE_ATK, max, min);
			break;
		}
		case ResourcesConstants.DIALOG_MODE_FILTER_DEF: {
			content = inflater.inflate(R.layout.range_dialog_content, null);
			Bundle param = getArguments();
			int max = param.getInt("max");
			int min = param.getInt("min");
			mController = new RangeDialogConfigController(mSimpleUiWrapper, content, 
					RangeDialogConfigController.RANGE_DIALOG_TYPE_DEF, max, min);
			break;
		}
		case ResourcesConstants.DIALOG_MODE_FILTER_LEVEL: {
			content = inflater.inflate(R.layout.grid_slection_dialog_content, null);
			Bundle param = getArguments();
			List<Integer> selection = param.getIntegerArrayList("selection");
			mController = new GridSelectionDialogController(mSimpleUiWrapper, content, R.array.card_level, 
					GridSelectionDialogController.GRID_SELECTION_TYPE_LEVEL, selection);
			break;
		}
		case ResourcesConstants.DIALOG_MODE_FILTER_EFFECT: {
			content = inflater.inflate(R.layout.grid_slection_dialog_content, null);
			Bundle param = getArguments();
			List<Integer> selection = param.getIntegerArrayList("selection");
			mController = new GridSelectionDialogController(mSimpleUiWrapper, content, R.array.card_effect, 
					GridSelectionDialogController.GRID_SELECTION_TYPE_EFFECT, selection);
		}
			break;
		case ResourcesConstants.DIALOG_MODE_ADD_NEW_SERVER:
		case ResourcesConstants.DIALOG_MODE_EDIT_SERVER:
			content = inflater.inflate(R.layout.create_server_content, null);
			mController = new ServerDialogController(mSimpleUiWrapper, content, getArguments());
			break;
		case ResourcesConstants.DIALOG_MODE_DIRECTORY_CHOOSE:
			content = inflater.inflate(R.layout.file_browser_layout, null);
			mController = new ServerDialogController(mSimpleUiWrapper, content, getArguments());
			break;
		case ResourcesConstants.DIALOG_MODE_APP_UPDATE:
			content = inflater.inflate(R.layout.app_update_content, null);
			mController = new AppUpdateController(mSimpleUiWrapper, content, getArguments());
			break;
		default:
			break;
		}
		builder.setView(content);
		return builder;
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.sdl__positive_button) {
			switch (mDialogMode) {
			case ResourcesConstants.DIALOG_MODE_CREATE_ROOM:
			case ResourcesConstants.DIALOG_MODE_QUICK_JOIN:
			case ResourcesConstants.DIALOG_MODE_JOIN_GAME: {
				Intent intent = new Intent(getActivity(), YGOMobileActivity.class);
				YGOGameOptions options = ((RoomDialogConfigController) mController).getGameOption() ;
				if (mDialogMode == DIALOG_MODE_CREATE_ROOM) {
					options.mServerAddr = Model.peekInstance().getMyCardServer().ipAddrString;
					options.mPort = Model.peekInstance().getMyCardServer().port;
					options.mName = Controller.peekInstance().getLoginName();
				} else if (mDialogMode == DIALOG_MODE_JOIN_GAME) {
				} else if (mDialogMode == DIALOG_MODE_QUICK_JOIN) {
					Fragment f = getTargetFragment();
					((RoomListFragment)f).handleMessage(Message.obtain(null, getTargetRequestCode(), 0, 0, options));
					return;
				}
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				intent.putExtra(YGOGameOptions.YGO_GAME_OPTIONS_BUNDLE_KEY, options);
				startActivity(intent);
				break;
			}
			case ResourcesConstants.DIALOG_MODE_DONATE: {
				Intent intent = new Intent();
				int method = ((DonateDialogConfigController) mController).getAlipayDonateMethod();
				switch (method) {
				case DonateDialogConfigController.DONATE_METHOD_ALIPAY_MOBILE_APP_INSTALLED:
					ComponentName component = new ComponentName("com.eg.android.AlipayGphone", "com.eg.android.AlipayGphone.AlipayLogin");
					intent.setComponent(component);
					intent.setAction(Intent.ACTION_VIEW);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					String alipayVersionString = ((DonateDialogConfigController) mController).getAlipayVersionString();
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
				int max = ((RangeDialogConfigController) mController).getMaxValue();
				int min = ((RangeDialogConfigController) mController).getMinValue();;
				((BaseFragment)getTargetFragment()).onEventFromChild(getTargetRequestCode(), FragmentNavigationListener.FRAGMENT_NAVIGATION_CARD_EVENT, min, max, null);
				break;
			}
			case ResourcesConstants.DIALOG_MODE_FILTER_LEVEL:
			case ResourcesConstants.DIALOG_MODE_FILTER_EFFECT: {
				List<Integer> list = ((GridSelectionDialogController) mController).getSelections();
				((BaseFragment)getTargetFragment()).onEventFromChild(getTargetRequestCode(), FragmentNavigationListener.FRAGMENT_NAVIGATION_CARD_EVENT, -1, -1, list);
				break;
			}
			case ResourcesConstants.DIALOG_MODE_ADD_NEW_SERVER:
			case ResourcesConstants.DIALOG_MODE_EDIT_SERVER:
				YGOServerInfo info = ((ServerDialogController) mController).getServerInfo();
				Model.peekInstance().addNewServer(info);
				((BaseFragment)getTargetFragment()).onEventFromChild(getTargetRequestCode(), FragmentNavigationListener.FRAGMENT_NAVIGATION_DUEL_CREATE_SERVER_EVENT, -1, -1, null);
			case ResourcesConstants.DIALOG_MODE_DIRECTORY_CHOOSE:
				break;
			default:
				break;
			}
		}
		dismiss();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}

	@Override
	public void onShow(DialogInterface dialog) {
		mController.enableSubmitIfAppropriate();
	}

}

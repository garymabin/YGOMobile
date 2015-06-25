/*
 * YGOMobileActivity.java
 *
 *  Created on: 2014年2月24日
 *      Author: mabin
 */
package cn.garymb.ygomobile;

import java.nio.ByteBuffer;

import cn.garymb.ygodata.YGOGameOptions;
import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.controller.NetworkController;
import cn.garymb.ygomobile.core.IrrlichtBridge;
import cn.garymb.ygomobile.utils.DeviceUtils;
import cn.garymb.ygomobile.widget.ComboBoxCompat;
import cn.garymb.ygomobile.widget.EditWindowCompat;
import cn.garymb.ygomobile.widget.overlay.OverlayOvalView;
import cn.garymb.ygomobile.widget.overlay.OverlayRectView;
import cn.garymb.ygomobile.widget.overlay.OverlayOvalView.OnDuelOptionsSelectListener;
import android.app.NativeActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * @author mabin
 * 
 */
public class YGOMobileActivity extends NativeActivity implements
		OnEditorActionListener, OnClickListener, OnDuelOptionsSelectListener,
		OnDismissListener {
	/**
	 * @author mabin
	 * 
	 */
	public class EventHandler extends Handler {
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_ID_TOGGLE_IME: {
				String hint = (String) msg.obj;
				boolean isShow = msg.arg1 == 1;
				if (isShow) {
					if (mOverlayShowRequest) {
						mOverlayView.hide();
						mChainOverlayView.hide();
					}
					mGlobalEditText.fillContent(hint);
					mGlobalEditText.showAtLocation(mContentView,
							Gravity.BOTTOM, 0, 0);
				} else {
					mGlobalEditText.dismiss();
				}
				break;
			}
			case MSG_ID_TOGGLE_COMBOBOX: {
				boolean isShow = msg.arg1 == 1;
				if (isShow) {
					String[] items = ((Bundle) msg.obj)
							.getStringArray(BUNDLE_KEY_COMBO_BOX_CONTENT);
					mGlobalComboBox.fillContent(items);
					mGlobalComboBox.showAtLocation(mContentView,
							Gravity.BOTTOM, 0, 0);
				}
				break;
			}
			case MSG_ID_PERFORM_HAPTICFEEDBACK: {
				mContentView.performHapticFeedback(
						HapticFeedbackConstants.LONG_PRESS,
						HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
				break;
			}
			case MSG_ID_TOGGLE_OVERLAY: {
				boolean isShow = msg.arg1 == 1;
				if (isShow) {
					mOverlayView.showAtScreen(0, 0);
					mChainOverlayView.showAtScreen(sChainControlXPostion,
							sChainControlYPostion);
				} else {
					mOverlayView.removeFromScreen();
					mChainOverlayView.removeFromScreen();
				}
			}
			default:
				break;
			}
			super.handleMessage(msg);
		}

	}

	private static final int CHAIN_CONTROL_PANEL_X_POSITION_LEFT_EDGE = 205;

	private static final int CHAIN_CONTROL_PANEL_Y_REVERT_POSITION = 100;

	public static final String TAG = "YGOMobile";
	public static final int MSG_ID_TOGGLE_IME = 0x0;
	public static final int MSG_ID_TOGGLE_COMBOBOX = 0x1;
	public static final int MSG_ID_PERFORM_HAPTICFEEDBACK = 0x2;
	public static final int MSG_ID_TOGGLE_OVERLAY = 0x3;

	public static final String BUNDLE_KEY_COMBO_BOX_CONTENT = "cn.garymb.ygomobile.combobox.content";
	public static final String BUNDLE_KEY_COMBO_BOX_LABEL = "cn.garymb.ygomobile.combobox.label";

	public static final int MENU_LOG_SAVE = Menu.FIRST;

	private volatile int mCompatGUIMode;

	private EditWindowCompat mGlobalEditText;
	private ComboBoxCompat mGlobalComboBox;
	private EventHandler mHandler;
	private PowerManager mPM;
	private WakeLock mLock;
	private View mContentView;
	private volatile boolean mOverlayShowRequest = false;
	private OverlayRectView mChainOverlayView;
	private OverlayOvalView mOverlayView;
	private NetworkController mNetController;

	private static int sChainControlXPostion;
	private static int sChainControlYPostion;

	private int currentApiVersion;

	static {
		final Resources res = StaticApplication.peekInstance().getResources();
		sChainControlXPostion = (int) (CHAIN_CONTROL_PANEL_X_POSITION_LEFT_EDGE * DeviceUtils
				.getXScale());
		sChainControlYPostion = (int) (DeviceUtils.getSmallerSize()
				- CHAIN_CONTROL_PANEL_Y_REVERT_POSITION
				* DeviceUtils.getYScale() - (res
				.getDimensionPixelSize(R.dimen.chain_control_button_height) * 2 + res
				.getDimensionPixelSize(R.dimen.chain_control_margin)));
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (mLock == null) {
			mLock = mPM.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
		}
		mLock.acquire();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.NativeActivity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mOverlayShowRequest) {
			mOverlayView.removeFromScreen();
			mChainOverlayView.removeFromScreen();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.NativeActivity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mOverlayShowRequest) {
			mOverlayView.showAtScreen(0, 0);
			mChainOverlayView.showAtScreen(sChainControlXPostion,
					sChainControlYPostion);
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		if (hasFocus) {
			if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
				getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE
								| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
								| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_FULLSCREEN
								| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			}
			mContentView.setHapticFeedbackEnabled(true);
		} else {
			mContentView.setHapticFeedbackEnabled(false);
		}
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (mLock != null) {
			mLock.release();
		}
	}

	/**
	 * Called from C++ world to initialize irrlicht handle.
	 * 
	 * @param handle
	 */
	public void setNativeHandle(int handle) {
		IrrlichtBridge.sNativeHandle = handle;
		return;
	}
	
	public ByteBuffer getNativeInitOptions() {
		return NativeInitOptions.fromSettingsPref(
				PreferenceManager.getDefaultSharedPreferences(StaticApplication
						.peekInstance())).toNativeBuffer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.NativeActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		currentApiVersion = android.os.Build.VERSION.SDK_INT;
		final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
			getWindow().getDecorView().setSystemUiVisibility(flags);
			final View decorView = getWindow().getDecorView();
			decorView
					.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

						@Override
						public void onSystemUiVisibilityChange(int visibility) {
							if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
								decorView.setSystemUiVisibility(flags);
							}
						}
					});
		}
		mHandler = new EventHandler();
		setRequestedOrientation(StaticApplication.peekInstance()
				.getGameScreenOritation());

		initExtraView();
		mPM = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mNetController = new NetworkController(getApplicationContext());
		handleExternalCommand();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		setIntent(intent);
		handleExternalCommand();
	}

	private void handleExternalCommand() {
		Intent intent = getIntent();
		YGOGameOptions options = intent
				.getParcelableExtra(YGOGameOptions.YGO_GAME_OPTIONS_BUNDLE_KEY);
		if (options != null) {
			Log.d(TAG, "receive from mycard:" + options.toString());
			ByteBuffer buffer = options.toByteBuffer();
			IrrlichtBridge.joinGame(buffer, buffer.position());
		}
	}

	private void initExtraView() {
		mContentView = getWindow().getDecorView().findViewById(
				android.R.id.content);
		mGlobalComboBox = new ComboBoxCompat(this);
		mGlobalComboBox.setButtonListener(this);
		mGlobalEditText = new EditWindowCompat(this);
		mGlobalEditText.setEditActionListener(this);
		mGlobalEditText.setOnDismissListener(this);

		mChainOverlayView = new OverlayRectView(this);
		mOverlayView = new OverlayOvalView(this);
		mChainOverlayView.setDuelOpsListener(this);
		mOverlayView.setDuelOpsListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.NativeActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/**
	 * Called from C++ world to make an IME compat for irrlicht.
	 * 
	 * @param hint
	 * @param isShow
	 */
	public void toggleIME(String hint, boolean isShow) {
		Log.i(TAG, "toggleIME： hint = " + hint + " isShow = " + isShow);
		mHandler.sendMessage(Message.obtain(null, MSG_ID_TOGGLE_IME, isShow ? 1
				: 0, 0, hint));
	}

	/**
	 * Called from C++ world to make an ComboxBox compat for irrlicht.
	 * 
	 * @param items
	 * @param isShow
	 * @param mode
	 */
	public void showComboBoxCompat(String[] items, boolean isShow, int mode) {
		mCompatGUIMode = mode;
		Log.i(TAG, "showComboBoxCompat： isShow = " + isShow);
		Bundle data = new Bundle();
		data.putStringArray(BUNDLE_KEY_COMBO_BOX_CONTENT, items);
		mHandler.sendMessage(Message.obtain(null, MSG_ID_TOGGLE_COMBOBOX,
				isShow ? 1 : 0, 0, data));
	}

	/**
	 * Called from C++ world to make an Hapic feedback.
	 * 
	 * @param items
	 * @param isShow
	 * @param mode
	 */
	public void performHapticFeedback() {
		mHandler.sendEmptyMessage(MSG_ID_PERFORM_HAPTICFEEDBACK);
	}

	/**
	 * Called from C++ world to make an trick.
	 * 
	 * @param items
	 * @param isShow
	 * @param mode
	 */
	public byte[] performTrick() {
		return ((StaticApplication) getApplication()).getSignInfo();
	}

	/**
	 * Called from C++ world to show or hide overlay view
	 * 
	 * @param items
	 * @param isShow
	 * @param mode
	 */
	public void toggleOverlayView(boolean isShow) {
		if (mOverlayShowRequest != isShow) {
			mOverlayShowRequest = isShow;
			mHandler.sendMessage(Message.obtain(null, MSG_ID_TOGGLE_OVERLAY,
					isShow ? 1 : 0, 0));
		}
	}

	/**
	 * Called from C++ world to fetch Wi-fi ip address.
	 * 
	 * @param items
	 * @param isShow
	 * @param mode
	 */
	public int getLocalAddress() {
		return mNetController.getIPAddress();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.TextView.OnEditorActionListener#onEditorAction(android
	 * .widget.TextView, int, android.view.KeyEvent)
	 */
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		final String text = v.getText().toString();
		IrrlichtBridge.insertText(text);
		mGlobalEditText.dismiss();
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.cancel) {
		} else if (v.getId() == R.id.submit) {
			int idx = mGlobalComboBox.getCurrentSelection();
			Log.d(TAG, "showComboBoxCompat: receive selection: " + idx);
			if (mCompatGUIMode == Constants.COMPAT_GUI_MODE_COMBOBOX) {
				IrrlichtBridge.setComboBoxSelection(idx);
			} else if (mCompatGUIMode == Constants.COMPAT_GUI_MODE_CHECKBOXES_PANEL) {
				IrrlichtBridge.setCheckBoxesSelection(idx);
			}
		}
		mGlobalComboBox.dismiss();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.garymb.ygomobile.widget.overlay.DuelOverlayView.
	 * OnDuelOptionsSelectListener#onDuelOptionsSelected(int)
	 */
	@Override
	public void onDuelOptionsSelected(int mode, boolean action) {
		// TODO Auto-generated method stub
		switch (mode) {
		case Constants.MODE_CANCEL_CHAIN_OPTIONS:
			Log.d(TAG, "Constants.MODE_CANCEL_CHAIN_OPTIONS: " + action);
			IrrlichtBridge.cancelChain();
			break;
		case Constants.MODE_REFRESH_OPTION:
			Log.d(TAG, "Constants.MODE_REFRESH_OPTION: " + action);
			// IrrlichtBridge.refreshTexture();
			break;
		case Constants.MODE_REACT_CHAIN_OPTION:
			Log.d(TAG, "Constants.MODE_REACT_CHAIN_OPTION: " + action);
			IrrlichtBridge.reactChain(action);
			break;
		case Constants.MODE_IGNORE_CHAIN_OPTION:
			Log.d(TAG, "Constants.MODE_IGNORE_CHAIN_OPTION: " + action);
			IrrlichtBridge.ignoreChain(action);
			break;
		default:
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.PopupWindow.OnDismissListener#onDismiss()
	 */
	@Override
	public void onDismiss() {
		// TODO Auto-generated method stub
		if (mOverlayShowRequest) {
			mOverlayView.show();
			mChainOverlayView.show();
		}
	}
}

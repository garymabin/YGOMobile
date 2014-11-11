package cn.garymb.ygomobile;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.controller.actionbar.ActionBarCreator;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.common.ImageDLAddTask;
import cn.garymb.ygomobile.common.ImageDLCheckTask;
import cn.garymb.ygomobile.common.ImageDLAddTask.ImageDLAddListener;
import cn.garymb.ygomobile.common.ImageDLCheckTask.ImageDLCheckListener;
import cn.garymb.ygomobile.controller.Controller;
import cn.garymb.ygomobile.core.DownloadService;
import cn.garymb.ygomobile.core.IBaseTask;
import cn.garymb.ygomobile.fragment.BaseFragment;
import cn.garymb.ygomobile.fragment.CardDetailFragment;
import cn.garymb.ygomobile.fragment.CardWikiFragment;
import cn.garymb.ygomobile.fragment.FreeDuelTabFragment;
import cn.garymb.ygomobile.fragment.BaseFragment.OnActionBarChangeCallback;
import cn.garymb.ygomobile.fragment.ImageDLStatusDlgFragment;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import cn.garymb.ygomobile.model.data.VersionInfo;
import cn.garymb.ygomobile.setting.Settings;

import com.google.analytics.tracking.android.EasyTracker;
import com.umeng.update.UmengUpdateAgent;

import eu.inmite.android.lib.dialogs.ISimpleDialogListener;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements
		OnActionBarChangeCallback, Handler.Callback, Constants,
		OnNavigationListener, ISimpleDialogListener, ImageDLCheckListener {

	public static class EventHandler extends Handler {
		public EventHandler(Callback back) {
			super(back);
		}
	}

	private ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			DownloadService.ServiceBinder binder = (DownloadService.ServiceBinder) service;
			if (binder == null) {
				finish();
				return;
			}
		}
	};

	private static final int DUEL_INDEX_FREE_MODE = 0;
	private static final int DUEL_INDEX_CARD_WIKI = 1;

	private static final String TAG = "MainActivity";

	private boolean isExit;

	private Controller mController;

	private ActionBar mActionBar;

	private ActionBarCreator mActionBarCreator;

	private EventHandler mHandler;

	private Menu mMenu;

	private FragmentManager mFragmentManager;

	private String[] mDuelList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFragmentManager = getSupportFragmentManager();
		setContentView(R.layout.activity_main);
		setTitle(R.string.app_name);
		mController = Controller.peekInstance();
		mActionBarCreator = new ActionBarCreator(this);
		mHandler = new EventHandler(this);
		mDuelList = getResources().getStringArray(R.array.duel_list);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
		initActionBar();
		mActionBar
				.setListNavigationCallbacks(new ArrayAdapter<String>(this,
						android.R.layout.simple_spinner_dropdown_item,
						mDuelList), this);
		mActionBar.setSelectedNavigationItem(DUEL_INDEX_FREE_MODE);
		UmengUpdateAgent.setDeltaUpdate(false);
		UmengUpdateAgent.update(this);
		boolean isFirstRun = checkFirstRunAfterInstall();
		if (isFirstRun && !checkDiyCardDataBase()) {
			SimpleDialogFragment.createBuilder(this, mFragmentManager)
					.setMessage(R.string.card_img_check_hint)
					.setTitle(R.string.card_img_update_title)
					.setPositiveButtonText(R.string.button_update)
					.setNegativeButtonText(R.string.button_cancel)
					.setRequestCode(0).show();
		}
		Intent service = new Intent(this, DownloadService.class);
		bindService(service, mServiceConn, Context.BIND_AUTO_CREATE);
		showImageDownloadStatus(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		showImageDownloadStatus(intent);
	}

	private void showImageDownloadStatus(Intent intent) {
		String action = intent.getAction();
		if (Constants.ACTION_VIEW_DOWNLOAD_STATUS.equals(action)) {
			ImageDLStatusDlgFragment newFragment = ImageDLStatusDlgFragment
					.newInstance(null, 1);
			newFragment.show(mFragmentManager, "dialog");
		}
	}

	private boolean checkFirstRunAfterInstall() {
		boolean isFirstRun = false;
		SharedPreferences prefs = getSharedPreferences(PREF_FILE_COMMON,
				MODE_PRIVATE);
		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(),
					PackageManager.GET_META_DATA);
			if (prefs.getLong(PREF_KEY_VERSION_CHECK, 0) < pInfo.versionCode) {
				isFirstRun = true;
				SharedPreferences.Editor editor = prefs.edit();
				editor.putLong(PREF_KEY_VERSION_CHECK, pInfo.versionCode);
				editor.commit();
			}
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "Error reading versionCode");
			e.printStackTrace();
		}
		return isFirstRun;
	}

	@Override
	protected void onResume() {
		mController.registerForActionCardImageDL(mHandler);
		mController.registerForActionSettings(mHandler);
		mController.registerForActionSupport(mHandler);
		super.onResume();
	}

	@Override
	protected void onPause() {
		mController.unregisterForActionCardImageDL(mHandler);
		mController.unregisterForActionSettings(mHandler);
		mController.unregisterForActionSupport(mHandler);
		super.onPause();
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(getApplicationContext()).activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(getApplicationContext()).activityStop(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConn);
		mServiceConn = null;
	}

	private void initActionBar() {
		mActionBar = getSupportActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		mActionBarCreator.createMenu(menu);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		mActionBarCreator.createMenu(menu);
		return super.onCreateOptionsMenu(menu);
	}

	public Menu getMenu() {
		return mMenu;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return Controller.peekInstance().handleActionBarEvent(item);
	}

	public void navigateToChildFragment(Bundle param, int id, int requestCode,
			boolean isReplace) {
		Fragment fragment = null;
		switch (id) {
		case FRAGMENT_ID_CARD_DETAIL:
			fragment = CardDetailFragment.newInstance(param);
			break;
		default:
			break;
		}
		// Insert the fragment by adding a new fragment
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		Fragment parent = mFragmentManager.findFragmentById(R.id.content_frame);
		fragment.setTargetFragment(parent, requestCode);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		if (isReplace) {
			ft.replace(R.id.content_frame, fragment).addToBackStack(null)
					.commit();
		} else {
			ft.add(R.id.content_frame, fragment).addToBackStack(null).commit();
		}
	}

	@Override
	public void onActionBarChange(int msgType, int action, int arg1,
			Object extra) {
		switch (msgType) {
		case Constants.ACTION_BAR_CHANGE_TYPE_PAGE_CHANGE:
			if (action == FRAGMENT_ID_DUEL) {
				mActionBarCreator = new ActionBarCreator(this).setNew(true,
						arg1).setPlay(true);

			} else if (action == FRAGMENT_ID_CARD_WIKI) {
				mActionBarCreator = new ActionBarCreator(this).setFilter(true)
						.setSearch(true, arg1).setReset(true);
			} else {
				mActionBarCreator = new ActionBarCreator(this);
			}
			break;
		default:
			break;
		}
		supportInvalidateOptionsMenu();
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case Constants.ACTION_BAR_EVENT_TYPE_SETTINGS:
			Log.d(TAG, "receive settings click action");
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
		case Constants.ACTION_BAR_EVENT_TYPE_CARD_IAMGE_DL:
			Log.d(TAG, "receive card image click action");
			if (!checkDiyCardDataBase()) {
				IBaseTask connection = Controller.peekInstance()
						.createOrGetDownloadConnection();
				if (!connection.isRunning()) {
					ImageDLCheckTask task = new ImageDLCheckTask(this);
					task.setImageDLCheckListener(this);
					if (Build.VERSION.SDK_INT >= 11) {
						task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					} else {
						task.execute();
					}
				} else {
					Toast.makeText(this,
							R.string.card_image_already_downloading_hint,
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this,
						R.string.card_image_dl_not_avail,
						Toast.LENGTH_SHORT).show();
			}
			break;
		case Constants.ACTION_BAR_EVENT_TYPE_DONATE: {
			BaseFragment fragment = (BaseFragment) mFragmentManager
					.findFragmentById(R.id.content_frame);
			Bundle bundle = new Bundle();
			bundle.putInt(ResourcesConstants.MODE_OPTIONS,
					ResourcesConstants.DIALOG_MODE_DONATE);
			fragment.showDialog(bundle);
			break;
		}
		case Constants.MSG_ID_EXIT_CONFIRM_ALARM:
			isExit = false;
			break;
		case Constants.REQUEST_TYPE_CHECK_UPDATE: {
			VersionInfo info = (VersionInfo) msg.obj;
			if (info != null) {
				if (info.version > StaticApplication.peekInstance()
						.getVersionCode()) {
					Bundle bundle = new Bundle();
					bundle.putInt("version", info.version);
					bundle.putInt("titleRes",
							R.string.settings_about_new_version);
					bundle.putString("url", info.url);
					bundle.putInt(ResourcesConstants.MODE_OPTIONS,
							ResourcesConstants.DIALOG_MODE_APP_UPDATE);
					BaseFragment current = (BaseFragment) mFragmentManager
							.findFragmentById(R.id.content_frame);
					current.showDialog(bundle);
				}
			}
			break;
		}
		default:
			break;
		}
		return true;
	}

	private boolean checkDiyCardDataBase() {
		SharedPreferences sp = StaticApplication.peekInstance().getApplicationSettings();
		return sp.getBoolean(Settings.KEY_PREF_GAME_DIY_CARD_DB, false);
	}

	@Override
	public void finish() {
		if (!isExit) {
			isExit = true;
			Toast.makeText(this, getResources().getString(R.string.exit_hint),
					Toast.LENGTH_SHORT).show();
			mHandler.sendEmptyMessageDelayed(
					Constants.MSG_ID_EXIT_CONFIRM_ALARM, 2000);
		} else {
			super.finish();
		}
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		switchState(position);
		return false;
	}

	private void switchState(int position) {
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		Fragment fragment;
		if (position == DUEL_INDEX_CARD_WIKI) {
			fragment = new CardWikiFragment();
		} else {
			fragment = new FreeDuelTabFragment();
		}
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		mFragmentManager.popBackStackImmediate();
		ft.replace(R.id.content_frame, fragment);
		ft.commitAllowingStateLoss();
	}

	@Override
	public void onPositiveButtonClicked(int requestCode) {
		if (requestCode == 0) {
			ImageDLCheckTask task = new ImageDLCheckTask(this);
			task.setImageDLCheckListener(this);
			if (Build.VERSION.SDK_INT >= 11) {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				task.execute();
			}
		} else if (requestCode == 1) {
		}
	}

	@Override
	public void onNegativeButtonClicked(int requestCode) {
		if (requestCode == 1) {
			Controller.peekInstance().cleanupDownloadConnection();
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, DownloadService.class);
			intent.setAction(DownloadService.ACTION_STOP_TASK);
			startService(intent);
		}
	}

	@Override
	public void onDLCheckComplete(Bundle result) {
		if (result != null) {
			ImageDLAddTask task = new ImageDLAddTask(this, Controller
					.peekInstance().createOrGetDownloadConnection());
			task.setImageDLAddListener(new ImageDLAddListener() {
				@Override
				public void onDLAddComplete(Bundle result) {
					Intent intent = new Intent();
					intent.setClass(MainActivity.this, DownloadService.class);
					intent.setAction(DownloadService.ACTION_START_BATCH_TASK);
					startService(intent);
				}
			});
			if (Build.VERSION.SDK_INT >= 11) {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, result);
			} else {
				task.execute(result);
			}
		} else {
			Toast.makeText(this, R.string.card_image_already_updated, Toast.LENGTH_SHORT).show();
		}

	}
}

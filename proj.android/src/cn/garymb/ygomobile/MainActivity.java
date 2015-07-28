package cn.garymb.ygomobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.avast.android.dialogs.iface.INegativeButtonDialogListener;
import com.avast.android.dialogs.iface.IPositiveButtonDialogListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.umeng.update.UmengUpdateAgent;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.controller.actionbar.ActionBarCreator;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.common.ImageDLAddTask;
import cn.garymb.ygomobile.common.ImageDLCheckTask;
import cn.garymb.ygomobile.common.ResCheckTask;
import cn.garymb.ygomobile.common.ImageDLAddTask.ImageDLAddListener;
import cn.garymb.ygomobile.common.ImageDLCheckTask.ImageDLCheckListener;
import cn.garymb.ygomobile.common.ResCheckTask.ResCheckListener;
import cn.garymb.ygomobile.controller.Controller;
import cn.garymb.ygomobile.core.DownloadService;
import cn.garymb.ygomobile.core.IBaseTask;
import cn.garymb.ygomobile.fragment.BaseFragment;
import cn.garymb.ygomobile.fragment.CardDeckFragment;
import cn.garymb.ygomobile.fragment.CardDetailFragment;
import cn.garymb.ygomobile.fragment.CardWikiFragment;
import cn.garymb.ygomobile.fragment.CustomDialogFragment;
import cn.garymb.ygomobile.fragment.ServerListFragment;
import cn.garymb.ygomobile.fragment.BaseFragment.OnActionBarChangeCallback;
import cn.garymb.ygomobile.fragment.ProgressDlgFragment;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import cn.garymb.ygomobile.setting.Settings;
import cn.garymb.ygomobile.model.Model;
import cn.garymb.ygomobile.ygo.YGOServerInfo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity  implements
		OnActionBarChangeCallback, Handler.Callback, Constants,
		IPositiveButtonDialogListener, INegativeButtonDialogListener, ImageDLCheckListener, ResCheckListener {
	

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

	/**
	 * @author mabin
	 * 
	 */
	public class DrawerItemClickListener implements OnItemClickListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.widget.AdapterView.OnItemClickListener#onItemClick(android
		 * .widget.AdapterView, android.view.View, int, long)
		 */
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position != -1) {
				selectItem(position + 1);
			}
		}

	}

	private static final int DUEL_INDEX_CARD_WIKI = 1;

	private static final String IMAGE_TAG = "image";
	private static final String TEXT_TAG = "text";

	private static final String TAG = "MainActivity";

	private boolean isExit;

	private Controller mController;

	private ActionBar mActionBar;

	private ActionBarCreator mActionBarCreator;

	private EventHandler mHandler;

	private Menu mMenu;

	private FragmentManager mFragmentManager;

	private Integer[] mDrawerImageArray = { R.drawable.ic_drawer_duel,
			R.drawable.ic_drawer_card_wiki, R.drawable.ic_drawer_card_deck };
	private int[] viewTo = { R.id.drawer_item_image, R.id.drawer_item_text };
	private String[] dataFrom = { IMAGE_TAG, TEXT_TAG };

	private List<Map<String, Object>> mDrawerListData = new ArrayList<Map<String, Object>>();

	private String[] mFragmentItems;
	private LinearLayout mLeftDrawer;
	private ListView mDrawerList;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;

	private Toolbar mToolBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFragmentManager = getSupportFragmentManager();
		setContentView(R.layout.activity_main);
		setTitle(R.string.app_name);
		mController = Controller.peekInstance();
		mActionBarCreator = new ActionBarCreator(this);
		mHandler = new EventHandler(this);
		initView();
		mToolBar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolBar);
		initActionBar();
		UmengUpdateAgent.update(this);
		ResCheckTask task = new ResCheckTask(this);
		task.setResCheckListener(this);
		if (Build.VERSION.SDK_INT >= 11) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}
	}
	
	private void initActionBar() {
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(true);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		showImageDownloadStatus(intent);
	}

	private void showImageDownloadStatus(Intent intent) {
		String action = intent.getAction();
		if (Constants.ACTION_VIEW_DOWNLOAD_STATUS.equals(action)) {
			ProgressDlgFragment newFragment = ProgressDlgFragment
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

	private void initView() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name);
		
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		mFragmentItems = getResources().getStringArray(R.array.fragment_items);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		int size = mDrawerImageArray.length;
		for (int i = 0; i < size; i++) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put(IMAGE_TAG, mDrawerImageArray[i]);
			item.put(TEXT_TAG, mFragmentItems[i]);
			mDrawerListData.add(item);
		}
		mDrawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mDrawerList.setAdapter(new SimpleAdapter(this, mDrawerListData,
				R.layout.drawer_list_item, dataFrom, viewTo) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				ImageView icon = (ImageView) v
						.findViewById(R.id.drawer_item_image);
				TextView text = (TextView) v
						.findViewById(R.id.drawer_item_text);
				if (mDrawerList.isItemChecked(position)) {
					icon.setSelected(true);
					text.setSelected(true);
				} else {
					icon.setSelected(false);
					text.setSelected(false);
				}
				return v;
			}
		});
		mLeftDrawer = (LinearLayout) findViewById(R.id.left_layout);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		selectItem(1);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
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
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
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
			} else if (action == FRAGMENT_ID_CARD_DETAIL) {
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
				Toast.makeText(this, R.string.card_image_dl_not_avail,
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
		default:
			break;
		}
		return true;
	}

	private boolean checkDiyCardDataBase() {
		SharedPreferences sp = StaticApplication.peekInstance()
				.getApplicationSettings();
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
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	protected void navigateToFragment(int id) {
		Fragment fragment = null;
		switch (id) {
		case FRAGMENT_ID_DUEL:
			fragment = new ServerListFragment();
			break;
		case FRAGMENT_ID_CARD_WIKI:
			fragment = new CardWikiFragment();
			break;
		case FRAGMENT_ID_CARD_DECK:
			fragment = new CardDeckFragment();
			break;
		default:
			break;
		}
		Bundle args = new Bundle();
		args.putString(BaseFragment.ARG_ITEM_TITLE, mFragmentItems[id - 1]);
		fragment.setArguments(args);
		// Insert the fragment by replacing any existing fragment
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		mFragmentManager.popBackStack();
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		transaction.replace(R.id.content_frame, fragment).commit();
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
			Toast.makeText(this, R.string.card_image_already_updated,
					Toast.LENGTH_SHORT).show();
		}
	}

	public YGOServerInfo getServer() {
		return Model.peekInstance().getMyCardServer();
	}
	
	public Toolbar getToolbar() {
		return mToolBar;
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		// Highlight the selected item, update the title, and close the drawer
		navigateToFragment(position);
		mDrawerList.setItemChecked(position - 1, true);
		mDrawerLayout.closeDrawer(mLeftDrawer);
	}

	public void setDrawerEnabled(boolean isEnabled) {
		if (isEnabled) {
			mDrawerToggle.setDrawerIndicatorEnabled(true);
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		} else {
			mDrawerToggle.setDrawerIndicatorEnabled(false);
			mDrawerLayout.closeDrawers();
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}
	}

	@Override
	public void onResCheckFinished(int result) {
		boolean isFirstRun = checkFirstRunAfterInstall();
		if (isFirstRun && !checkDiyCardDataBase()) {
			CustomDialogFragment.createBuilder(this, mFragmentManager)
					.setMessage(R.string.card_img_check_hint)
					.setTitle(R.string.card_img_update_title)
					.setPositiveButtonText(R.string.button_update)
					.setNegativeButtonText(R.string.button_cancel)
					.setRequestCode(0).showAllowingStateLoss();
		}
		Intent service = new Intent(this, DownloadService.class);
		bindService(service, mServiceConn, Context.BIND_AUTO_CREATE);
		showImageDownloadStatus(getIntent());		
	}
}

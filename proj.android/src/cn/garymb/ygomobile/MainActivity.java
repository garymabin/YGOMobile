package cn.garymb.ygomobile;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.actionbar.ActionBarCreator;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.core.Controller;
import cn.garymb.ygomobile.fragment.BaseFragment;
import cn.garymb.ygomobile.fragment.CardDetailFragment;
import cn.garymb.ygomobile.fragment.CardWikiFragment;
import cn.garymb.ygomobile.fragment.FreeDuelTabFragment;
import cn.garymb.ygomobile.fragment.BaseFragment.OnActionBarChangeCallback;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import cn.garymb.ygomobile.model.data.VersionInfo;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.google.analytics.tracking.android.EasyTracker;
import com.umeng.update.UmengUpdateAgent;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;

public class MainActivity extends ActionBarActivity implements
		OnActionBarChangeCallback, Handler.Callback, Constants,
		OnNavigationListener {

	public static class EventHandler extends Handler {
		public EventHandler(Callback back) {
			super(back);
		}
	}

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
		supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.activity_main);
		setTitle(R.string.app_name);
		mController = Controller.peekInstance();
		mActionBarCreator = new ActionBarCreator(this);
		mHandler = new EventHandler(this);
		mDuelList = getResources().getStringArray(R.array.duel_list);
		initActionBar();
		mActionBar
				.setListNavigationCallbacks(new ArrayAdapter<String>(this,
						android.R.layout.simple_spinner_dropdown_item,
						mDuelList), this);
		mActionBar.setSelectedNavigationItem(DUEL_INDEX_FREE_MODE);
		UmengUpdateAgent.setDeltaUpdate(false);
		UmengUpdateAgent.update(this);
	}

	@Override
	protected void onResume() {
		mController.registerForActionSettings(mHandler);
		mController.registerForActionSupport(mHandler);
		super.onResume();
	}

	@Override
	protected void onPause() {
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

	private void initActionBar() {
		mActionBar = getSupportActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);
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

	@Override
	public void finish() {
		if (!isExit) {
			isExit = true;
			SuperActivityToast.create(this,
					getResources().getString(R.string.exit_hint),
					SuperToast.Duration.MEDIUM).show();
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
		mFragmentManager.popBackStack();
		ft.replace(R.id.content_frame, fragment);
		ft.commitAllowingStateLoss();
	}
}

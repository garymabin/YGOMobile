package cn.garymb.ygomobile;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobie.model.Model;
import cn.garymb.ygomobile.actionbar.ActionBarCreator;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.core.Controller;
import cn.garymb.ygomobile.fragment.BaseFragment;
import cn.garymb.ygomobile.fragment.CardDetailFragment;
import cn.garymb.ygomobile.fragment.CardWikiFragment;
import cn.garymb.ygomobile.fragment.FreeDuelTabFragment;
import cn.garymb.ygomobile.fragment.BaseFragment.OnActionBarChangeCallback;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import cn.garymb.ygomobile.ygo.YGOServerInfo;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.google.analytics.tracking.android.EasyTracker;

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
import android.widget.ArrayAdapter;

public class MainActivity extends ActionBarActivity implements
		OnActionBarChangeCallback, Handler.Callback, Constants, OnNavigationListener {

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
		setContentView(R.layout.activity_main);
		initActionBar();
		setTitle(R.string.app_name);
		mController = Controller.peekInstance();
		mActionBarCreator = new ActionBarCreator(this);
		mHandler = new EventHandler(this);
		mDuelList = getResources().getStringArray(R.array.duel_list);
		mActionBar.setListNavigationCallbacks(
				new ArrayAdapter<String>(this,
						android.R.layout.simple_spinner_dropdown_item,
						mDuelList), this);
		mActionBar.setSelectedNavigationItem(
				DUEL_INDEX_FREE_MODE);
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
		// TODO Auto-generated method stub
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
		case Constants.ACTION_BAR_EVENT_TYPE_DONATE:
			BaseFragment fragment = (BaseFragment) mFragmentManager
					.findFragmentById(R.id.content_frame);
			Bundle bundle = new Bundle();
			bundle.putInt(ResourcesConstants.MODE_OPTIONS,
					ResourcesConstants.DIALOG_MODE_DONATE);
			fragment.showDialog(bundle);
			break;
		case Constants.MSG_ID_EXIT_CONFIRM_ALARM:
			isExit = false;
			break;
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
		ft.replace(R.id.content_frame, fragment);
		ft.commit();
	}
}

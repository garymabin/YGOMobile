package cn.garymb.ygomobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.actionbar.ActionBarCreator;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.core.Controller;
import cn.garymb.ygomobile.fragment.BaseFragment;
import cn.garymb.ygomobile.fragment.CardDeckFragment;
import cn.garymb.ygomobile.fragment.CardDetailFragment;
import cn.garymb.ygomobile.fragment.CardImageFragment;
import cn.garymb.ygomobile.fragment.CardWikiFragment;
import cn.garymb.ygomobile.fragment.DuelFragment;
import cn.garymb.ygomobile.fragment.FreeDuelTabFragment;
import cn.garymb.ygomobile.fragment.BaseFragment.OnActionBarChangeCallback;
import cn.garymb.ygomobile.model.Model;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import cn.garymb.ygomobile.ygo.YGOServerInfo;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.google.analytics.tracking.android.EasyTracker;
import com.umeng.update.UmengUpdateAgent;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements
		OnActionBarChangeCallback, Handler.Callback, Constants {

	public static class EventHandler extends Handler {
		public EventHandler(Callback back) {
			super(back);
		}
	}

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
			R.drawable.ic_drawer_card_wiki, R.drawable.ic_drawer_card_deck, R.drawable.ic_drawer_card_image};
	private int[] viewTo = { R.id.drawer_item_image, R.id.drawer_item_text };
	private String[] dataFrom = { IMAGE_TAG, TEXT_TAG };

	private List<Map<String, Object>> mDrawerListData = new ArrayList<Map<String, Object>>();

	private String[] mFragmentItems;
	private LinearLayout mLeftDrawer;
	private ListView mDrawerList;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;

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
		initActionBar();
		initView();
		mController.asyncUpdateMycardServer(mHandler
				.obtainMessage(Constants.MSG_ID_UPDATE_SERVER));
		UmengUpdateAgent.setDeltaUpdate(false);
		UmengUpdateAgent.update(this);
	}

	private void initView() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_navigation_drawer, R.string.app_name,
				R.string.app_name);
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
			public View getView(int position, View convertView,
					ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				ImageView icon = (ImageView) v.findViewById(R.id.drawer_item_image);
				TextView text = (TextView) v.findViewById(R.id.drawer_item_text);
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
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(true);
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
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

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
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
			fragment = new DuelFragment();
			break;
		case FRAGMENT_ID_CARD_WIKI:
			fragment = new CardWikiFragment();
			break;
		case FRAGMENT_ID_CARD_DECK:
			fragment = new CardDeckFragment();
			break;
		case FRAGMENT_ID_CARD_IMAGE:
			fragment = new CardImageFragment();
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

	public YGOServerInfo getServer() {
		return Model.peekInstance().getMyCardServer();
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		// Highlight the selected item, update the title, and close the drawer
		navigateToFragment(position);
		mDrawerList.setItemChecked(position - 1, true);
		mDrawerLayout.closeDrawer(mLeftDrawer);
	}

}

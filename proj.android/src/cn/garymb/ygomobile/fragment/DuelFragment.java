package cn.garymb.ygomobile.fragment;

import java.util.List;

//import com.viewpagerindicator.TabPageIndicator;

import cn.garymb.ygodata.YGOGameOptions;
import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.YGOMobileActivity;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.core.Controller;
import cn.garymb.ygomobile.data.wrapper.IBaseJob;
import cn.garymb.ygomobile.model.Model;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import cn.garymb.ygomobile.ygo.YGORoomInfo;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class DuelFragment extends BaseFragment {

	public class RoomTabPageAdapter extends FragmentPagerAdapter {

		public RoomTabPageAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0) {
			Log.d(TAG, "getItem: " + arg0);
			Fragment ft;
			switch (arg0%mDuelModeArray.length) {
			case ROOM_TAG_INDEX_SINGLE_MODE:
				ft = RoomPageFragment.newInstance(ROOM_TAG_INDEX_SINGLE_MODE);
				break;
			case ROOM_TAG_INDEX_MATCH_MODE:
				ft = RoomPageFragment.newInstance(ROOM_TAG_INDEX_MATCH_MODE);
				break;
			case ROOM_TAG_INDEX_TAG_MODE:
				ft = RoomPageFragment.newInstance(ROOM_TAG_INDEX_TAG_MODE);
				break;
			default:
				ft = RoomPageFragment.newInstance(ROOM_TAG_INDEX_SINGLE_MODE);
				break;
			}
			mFragments.put(arg0, (RoomPageFragment) ft);
			return ft;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			Log.d(TAG, "destroyItem: " + position);
			super.destroyItem(container, position, object);
			mFragments.remove(position);
		}

		@Override
		public int getCount() {
			return mDuelModeArray.length;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return mDuelModeArray[position%mDuelModeArray.length];
		}

	}

	private static final int REQUEST_ID_DUEL = 0;

	private static final String TAG = "DuelFragment";
	
	private static final int REQUEST_CODE_QUICK_JOIN = 0x1001;
	
	private static final int ROOM_TAG_INDEX_SINGLE_MODE = 0;
	private static final int ROOM_TAG_INDEX_MATCH_MODE = 1;
	private static final int ROOM_TAG_INDEX_TAG_MODE = 2;

	private String[] mDuelModeArray;

	private ViewPager mViewPager;
	
//	private TabPageIndicator mTabIndicator;

	private SparseArrayCompat<RoomPageFragment> mFragments = new SparseArrayCompat<RoomPageFragment>(3);

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity.onActionBarChange(
				Constants.ACTION_BAR_CHANGE_TYPE_PAGE_CHANGE, FRAGMENT_ID_DUEL,
				0, null);
		final Resources res = getResources();
		mDuelModeArray = res.getStringArray(R.array.duel_mode);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Controller.peekInstance().asyncUpdateRoomList(mHandler
				.obtainMessage(Constants.MSG_ID_UPDATE_ROOM_LIST));
		Controller.peekInstance().registerForActionNew(mHandler);
		Controller.peekInstance().registerForActionPlay(mHandler);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Controller.peekInstance().stopUpdateRoomList();
		Controller.peekInstance().unregisterForActionNew(mHandler);
		Controller.peekInstance().unregisterForActionPlay(mHandler);
	}
	
	public void onDetach() {
		super.onDetach();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.duel_panel, container, false);
		mViewPager = (ViewPager) view.findViewById(R.id.mViewPager);
		mViewPager.setAdapter(new RoomTabPageAdapter(getChildFragmentManager()));
//		mTabIndicator = (TabPageIndicator) view.findViewById(R.id.tabs);
//		mTabIndicator.setViewPager(mViewPager, 0);
		return view;
	}

	@Override
	public void onEventFromChild(int requestCode, int eventType, int arg1,
			int arg2, Object data) {
		if (requestCode == REQUEST_ID_DUEL) {
			if (eventType == FRAGMENT_NAVIGATION_DUEL_FREE_MODE_EVENT) {
				mActivity.getSupportActionBar().setSelectedNavigationItem(1);
			} else if (eventType == FRAGMENT_NAVIGATION_DUEL_LOGIN_ATTEMP_EVENT) {
				Bundle bundle = new Bundle();
				bundle.putString("username", Controller.peekInstance()
						.getLoginName());
				bundle.putInt("userstatus", arg1);
				mActivity.navigateToChildFragment(bundle,
						FRAGMENT_ID_USER_LOGIN, REQUEST_ID_DUEL, true);
			} else if (eventType == FRAGMENT_NAVIGATION_DUEL_LOGIN_SUCCEED_EVENT) {
//				FragmentManager fm = mActivity.getSupportFragmentManager();
//				fm.popBackStackImmediate();
//				FragmentTransaction ft = getChildFragmentManager()
//						.beginTransaction();
//				Fragment fragment = new RoomListFragment();
//				ft.replace(R.id.duel_panel, fragment);
//				ft.commitAllowingStateLoss();
			}
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		if (!isResumed()) {
			return false;
		}
		switch (msg.what) {
		case Constants.MSG_ID_UPDATE_ROOM_LIST:
			if (msg.arg2 == IBaseJob.STATUS_SUCCESS) {
				List<YGORoomInfo> data = Model.peekInstance().getRooms();
				int key = mFragments.keyAt(mViewPager.getCurrentItem());
				if (key != -1) {
					RoomPageFragment f = mFragments.get(key);
					if (f != null) {
						f.setData(data);
					}
				}
			} else if (msg.arg2 == IBaseJob.STATUS_FAILED) {
				Controller.peekInstance().asyncUpdateRoomList(mHandler
						.obtainMessage(Constants.MSG_ID_UPDATE_ROOM_LIST));
			}
			break;
		case Constants.ACTION_BAR_EVENT_TYPE_NEW: {
			Log.i(TAG, "receive action bar new click event");
			Bundle bundle = new Bundle();
			bundle.putInt(ResourcesConstants.MODE_OPTIONS, ResourcesConstants.DIALOG_MODE_CREATE_ROOM);
			showDialog(bundle);
			break;
		}
		case Constants.ACTION_BAR_EVENT_TYPE_PLAY: {
			Log.i(TAG, "receive action bar play click event");
			Bundle bundle = new Bundle();
			bundle.putInt(ResourcesConstants.MODE_OPTIONS, ResourcesConstants.DIALOG_MODE_QUICK_JOIN);
			showDialog(bundle, this, REQUEST_CODE_QUICK_JOIN);
			break;
		}
		case REQUEST_CODE_QUICK_JOIN: {
			YGOGameOptions options = (YGOGameOptions) msg.obj;
			String[] nameSegments = options.mRoomName.split("$");
			options.mRoomName = nameSegments[0];
			boolean isPrivate = nameSegments.length > 1;
			if (isPrivate) {
				options.mRoomPasswd = nameSegments[1];
			} else {
				options.mRoomPasswd = "";
			}
			List<YGORoomInfo> data = Model.peekInstance().getRooms();
			YGORoomInfo target = null;
			for (YGORoomInfo info : data) {
				if (info.name.equals(options.mRoomName) && isPrivate == info.privacy) {
					target = info;
					break;
				}
			}
			if (target != null) {
				options.mName = Controller.peekInstance().getLoginName();
				options.mMode = target.mode;
				options.mServerAddr = mActivity.getServer().ipAddrString;
				options.mPort = mActivity.getServer().port;
				options.mRoomName = target.name;
				options.setCompleteOptions(target.isCompleteInfo());
				if (target.isCompleteInfo()) {
					options.mDrawCount = target.drawCount == -1 ? 1 : target.drawCount;
					options.mEnablePriority = target.enablePriority;
					options.mNoDeckCheck = target.noDeckCheck;
					options.mNoDeckShuffle = target.noDeckShuffle;
					options.mRule = target.rule == -1 ? 0 : target.rule;
					options.mStartHand = target.startHand == -1 ? 5 : target.startHand;
					options.mStartLP = target.startLp == -1 ? 8000 : target.startLp;
				} 
				Intent intent = new Intent(getActivity(), YGOMobileActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				intent.putExtra(YGOGameOptions.YGO_GAME_OPTIONS_BUNDLE_KEY, options);
				startActivity(intent);
			} else {
				Toast.makeText(mActivity, getResources().getString(R.string.quick_join_error),
						Toast.LENGTH_SHORT).show();
			}
			break;
		}
		default:
			break;
		}
		return true;
	}
}

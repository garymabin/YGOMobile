package cn.garymb.ygomobile.fragment;

import cn.garymb.ygomobile.R;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class FreeDuelTabFragment extends TabFragment {
	
	public class FreeDuelFragmentAdapter extends FragmentPagerAdapter {

		public FreeDuelFragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int paramInt) {
			Fragment fragment = null;
			if (paramInt == FREE_DUEL_INDEX_SERVER_LIST) {
				fragment = new ServerListFragment();
			} else if (paramInt == FREE_DUEL_INDEX_LAN_MODE) {
				fragment = new LANModeFragment();
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return mTabs.length;
		}

	}
	
	private static final int FREE_DUEL_INDEX_SERVER_LIST = 0;
	private static final int FREE_DUEL_INDEX_LAN_MODE = 1;

	private String[] mTabs;

	@Override
	protected FragmentPagerAdapter initFragmentAdapter() {
		return new FreeDuelFragmentAdapter(getChildFragmentManager());
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mTabs = getResources().getStringArray(R.array.free_mode);
		mTabCount = mTabs.length;
	}
	
	@Override
	protected void initTab() {
		super.initTab();
		int i = 0;
		for (String title : mTabs) {
			addTab(i++, title, mTabCount);
		}
	}
}

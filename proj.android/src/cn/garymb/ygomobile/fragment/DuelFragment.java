package cn.garymb.ygomobile.fragment;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.common.Constants;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class DuelFragment extends BaseFragment implements OnNavigationListener {

	private String[] mDuelList;

	private static final int DUEL_INDEX_FREE_MODE = 0;
	private static final int DUEL_INDEX_CARD_WIKI = 1;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mDuelList = getResources().getStringArray(R.array.duel_list);
		mActivity.getSupportActionBar().setListNavigationCallbacks(
				new ArrayAdapter<String>(mActivity,
						android.R.layout.simple_spinner_dropdown_item,
						mDuelList), this);
		mActivity.getSupportActionBar().setSelectedNavigationItem(
				DUEL_INDEX_FREE_MODE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.duel_panel, null);
		return view;
	}

	@Override
	public boolean onNavigationItemSelected(int position, long arg1) {
		if (position == DUEL_INDEX_CARD_WIKI) {
			mActivity.onActionBarChange(
					Constants.ACTION_BAR_CHANGE_TYPE_PAGE_CHANGE,
					FRAGMENT_ID_CARD_WIKI, 0, null);
		} else if (position == DUEL_INDEX_FREE_MODE) {
			mActivity.onActionBarChange(
					Constants.ACTION_BAR_CHANGE_TYPE_PAGE_CHANGE,
					FRAGMENT_ID_DUEL, R.string.action_new_server, null);
		}
		switchState(position);
		return true;
	}

	private void switchState(int position) {
		FragmentTransaction ft = getChildFragmentManager().beginTransaction();
		Fragment fragment;
		if (position == DUEL_INDEX_CARD_WIKI) {
			fragment = new CardWikiFragment();
		} else {
			fragment = new FreeDuelTabFragment();
		}
		ft.replace(R.id.duel_panel, fragment);
		ft.commitAllowingStateLoss();
	}
}

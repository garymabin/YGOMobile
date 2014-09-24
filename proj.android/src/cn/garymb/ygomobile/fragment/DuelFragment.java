package cn.garymb.ygomobile.fragment;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.core.Controller;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DuelFragment extends BaseFragment {

	private static final int REQUEST_ID_DUEL = 0;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity.onActionBarChange(
				Constants.ACTION_BAR_CHANGE_TYPE_PAGE_CHANGE, FRAGMENT_ID_DUEL,
				0, null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.duel_panel, null);
		FragmentTransaction ft = getChildFragmentManager().beginTransaction();
		Fragment fragment;
		fragment = new RoomListFragment();
		ft.replace(R.id.duel_panel, fragment);
		ft.commitAllowingStateLoss();
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
				FragmentManager fm = mActivity.getSupportFragmentManager();
				fm.popBackStackImmediate();
				FragmentTransaction ft = getChildFragmentManager()
						.beginTransaction();
				Fragment fragment = new RoomListFragment();
				ft.replace(R.id.duel_panel, fragment);
				ft.commitAllowingStateLoss();
			}
		}
	}
}

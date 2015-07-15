package cn.garymb.ygomobile.fragment;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.provider.YGOCards;
import cn.garymb.ygomobile.widget.adapter.CardDetailAdapter;
import android.app.Activity;
import android.database.CursorWindow;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

public class CardDetailFragment extends BaseFragment implements
		OnTouchListener, OnPageChangeListener {

	private static final String TAG = "CardDetailFragment";

	private ViewPager mViewPager;
	private CardDetailAdapter<CardDetailPagerFragment> mAdapter;

	private String[] mCommonProjection;
	private int mInitPos;

	private CursorWindow mWindow;

	public static CardDetailFragment newInstance(Bundle param) {
		CardDetailFragment fragment = new CardDetailFragment();
		fragment.setArguments(param);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle param = getArguments();
		mCommonProjection = param
				.getStringArray(CardWikiFragment.BUNDLE_KEY_PROJECTION);
		mInitPos = param.getInt(CardWikiFragment.BUNDLE_KEY_INIT_POSITON);
		mWindow = param
				.getParcelable(CardWikiFragment.BUNDLE_KEY_CURSOR_WINDOW);
		// FIXME: this may cause some problem.
		param.remove(CardWikiFragment.BUNDLE_KEY_CURSOR_WINDOW);
		setHasOptionsMenu(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			getFragmentManager().popBackStack(null, 0);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		Log.d(TAG, "onDetach : E");
		super.onDetach();
	}

	@Override
	public boolean handleMessage(Message msg) {
		return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.card_detail, null);
		
		mViewPager = (ViewPager) view.findViewById(R.id.card_pager);
		mAdapter = new CardDetailAdapter<CardDetailPagerFragment>(
				getChildFragmentManager(), CardDetailPagerFragment.class,
				mCommonProjection, mWindow);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOnPageChangeListener(this);
		view.setOnTouchListener(this);
		mActivity.onActionBarChange(
				Constants.ACTION_BAR_CHANGE_TYPE_PAGE_CHANGE,
				FRAGMENT_ID_CARD_DETAIL, mInitPos, null);
		mViewPager.setCurrentItem(mInitPos);
		if (mWindow != null) {
			mActivity.setTitle(mWindow.getString(mInitPos, YGOCards.COMMON_DATA_PROJECTION_NAME_INDEX));
		}
		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		int currentSecltion = mViewPager.getCurrentItem();
		((BaseFragment) getTargetFragment()).onEventFromChild(
				getTargetRequestCode(), FRAGMENT_NAVIGATION_BACK_EVENT,
				currentSecltion - mInitPos, -1, null);
	}

	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		mActivity.setTitle(mWindow.getString(arg0,
				YGOCards.COMMON_DATA_PROJECTION_NAME_INDEX));
	}
}

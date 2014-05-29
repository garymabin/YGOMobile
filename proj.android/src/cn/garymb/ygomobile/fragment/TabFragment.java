package cn.garymb.ygomobile.fragment;

import cn.garymb.ygomobile.R;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 * @author mabin
 * 
 */
public abstract class TabFragment extends BaseFragment {

	protected ImageView mNavIndicator;
	protected ViewPager mViewPager;
	private int mIndicatorWidth;
	private HorizontalScrollView mHsv;
	private RadioGroup rg_nav_content;
	protected FragmentPagerAdapter mAdapter;
	private LayoutInflater mInflater;
	private int currentIndicatorLeft;
	
	protected int mTabCount;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mInflater = LayoutInflater.from(mActivity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View contentView = inflater.inflate(R.layout.tab_fragment, null);

		mHsv = (HorizontalScrollView) contentView.findViewById(R.id.mHsv);

		rg_nav_content = (RadioGroup) contentView
				.findViewById(R.id.rg_nav_content);

		mNavIndicator = (ImageView) contentView
				.findViewById(R.id.iv_nav_indicator);

		mViewPager = (ViewPager) contentView.findViewById(R.id.mViewPager);
		initView();
		setUpListener();
		return contentView;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		resizeNavTab(newConfig);
	}

	private void initView() {
		resizeNavTab(getResources().getConfiguration());
		initTab();
		mAdapter = initFragmentAdapter();
		mViewPager.setAdapter(mAdapter);
	}

	private void resizeNavTab(Configuration config) {
		DisplayMetrics dm = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		mIndicatorWidth = dm.widthPixels / (mTabCount > 4 ? 4 : mTabCount);

		LayoutParams cursor_Params = mNavIndicator.getLayoutParams();
		cursor_Params.width = mIndicatorWidth;// 初始化滑动下标的宽
		mNavIndicator.setLayoutParams(cursor_Params);
		int size = rg_nav_content.getChildCount();
		for (int i = 0; i < size; i++) {
			View view = rg_nav_content.getChildAt(i);
			view.setLayoutParams(new LinearLayout.LayoutParams(mIndicatorWidth,
				LayoutParams.MATCH_PARENT));
		}
		rg_nav_content.invalidate();
		
	}

	private void setUpListener() {

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {

				if (rg_nav_content != null
						&& rg_nav_content.getChildCount() > position) {
					((RadioButton) rg_nav_content.getChildAt(position))
							.performClick();
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

		rg_nav_content
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {

						if (rg_nav_content.getChildAt(checkedId) != null) {

							TranslateAnimation animation = new TranslateAnimation(
									currentIndicatorLeft,
									((RadioButton) rg_nav_content
											.getChildAt(checkedId)).getLeft(),
									0f, 0f);
							animation.setInterpolator(new LinearInterpolator());
							animation.setDuration(100);
							animation.setFillAfter(true);

							// 执行位移动画
							mNavIndicator.startAnimation(animation);

							mViewPager.setCurrentItem(checkedId); // ViewPager
																	// 跟随一起 切换

							// 记录当前 下标的距最左侧的 距离
							currentIndicatorLeft = ((RadioButton) rg_nav_content
									.getChildAt(checkedId)).getLeft();

							mHsv.smoothScrollTo(
									(checkedId > 1 ? ((RadioButton) rg_nav_content
											.getChildAt(checkedId)).getLeft()
											: 0)
											- ((RadioButton) rg_nav_content
													.getChildAt(mTabCount > 2 ? 2 : 1)).getLeft(),
									0);
						}
					}
				});
	}

	protected abstract FragmentPagerAdapter initFragmentAdapter();

	protected void initTab() {
		rg_nav_content.removeAllViews();
	}

	protected void addTab(int index, String text, int totalLength) {
		RadioButton rb = (RadioButton) mInflater.inflate(
				R.layout.nav_radiogroup_item, null);
		rb.setId(index);
		rb.setText(text);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mIndicatorWidth,
				LayoutParams.MATCH_PARENT);
		lp.weight = (float) (1.0 / totalLength);
		rb.setLayoutParams(lp);
		rg_nav_content.addView(rb);
	}

}

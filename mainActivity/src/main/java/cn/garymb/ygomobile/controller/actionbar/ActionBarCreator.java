package cn.garymb.ygomobile.controller.actionbar;

import cn.garymb.ygomobile.R;
import android.content.Context;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class ActionBarCreator {

	private Context mContext;

	public ActionBarCreator(AppCompatActivity activity) {
		mContext = activity;
	}

	private boolean mDownloadImage = false;

	private boolean mLoading = false;

	private boolean mSearch = false;

	private boolean mNew = false;
	private int mNewRes = 0;

	private boolean mSettings = true;

	private boolean mPlay = false;

	private boolean mFilter = false;

	private boolean mSupport = true;

	private boolean mReset = false;

	private int mSearchResId;

	public ActionBarCreator setLoading(boolean loading) {
		mLoading = loading;
		return this;
	}

	public ActionBarCreator setSearch(boolean search, int resID) {
		mSearch = search;
		mSearchResId = resID;
		return this;
	}

	public ActionBarCreator setNew(boolean actionNew, int resID) {
		mNew = actionNew;
		mNewRes = resID;
		return this;
	}

	public ActionBarCreator setPlay(boolean play) {
		mPlay = play;
		return this;
	}

	public ActionBarCreator setFilter(boolean filter) {
		mFilter = filter;
		return this;
	}

	public ActionBarCreator setReset(boolean reset) {
		mReset = reset;
		return this;
	}

	public boolean isFilterEnabled() {
		return mFilter;
	}

	public void createMenu(final Menu menu) {
		int index = 0;
		menu.clear();
		if (mSettings) {
			MenuItem item = menu.add(Menu.NONE, R.id.action_settings, index++,
					R.string.action_settings);
			MenuItemCompat.setShowAsAction(item,
					MenuItemCompat.SHOW_AS_ACTION_NEVER);
		}

		if (mSupport) {
			MenuItem item = menu.add(Menu.NONE, R.id.action_support, index++,
					R.string.action_support);
			MenuItemCompat.setShowAsAction(item,
					MenuItemCompat.SHOW_AS_ACTION_NEVER);
		}

		if (mDownloadImage) {
			MenuItem item = menu.add(Menu.NONE, R.id.action_check_dl_image,
					index++, R.string.action_dl_card_image);
			MenuItemCompat.setShowAsAction(item,
					MenuItemCompat.SHOW_AS_ACTION_NEVER);
		}

		if (mReset) {
			MenuItem item = menu.add(Menu.NONE, R.id.action_reset, index++,
					R.string.action_reset).setIcon(R.drawable.ic_action_reset);
			MenuItemCompat.setShowAsAction(item,
					MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		}

		if (mFilter) {
			MenuItem item = menu.add(Menu.NONE, R.id.action_filter, index++,
					R.string.action_filter).setIcon(
					R.drawable.ic_action_empty_filter);
			MenuItemCompat
					.setShowAsAction(
							item,
							MenuItemCompat.SHOW_AS_ACTION_ALWAYS
									| MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		}
		if (mLoading) {
			MenuItem item = menu.add(Menu.NONE, R.id.action_loading, index++,
					"");
			MenuItemCompat.setActionView(item,
					R.layout.actionbar_loading_progress);
			MenuItemCompat
					.setShowAsAction(
							item,
							MenuItemCompat.SHOW_AS_ACTION_ALWAYS
									| MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
			MenuItemCompat.expandActionView(item);
		}
		if (mPlay) {
			MenuItem item = menu.add(Menu.NONE, R.id.action_play, index++,
					mContext.getResources().getString(R.string.action_play))
					.setIcon(R.drawable.ic_action_play);
			MenuItemCompat.setShowAsAction(item,
					MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		}
		if (mSearch) {
			MenuItem item = menu.add(Menu.NONE, R.id.action_search, index++,
					mContext.getResources().getString(R.string.action_search))
					.setIcon(R.drawable.ic_action_search);
			MenuItemCompat
					.setShowAsAction(
							item,
							MenuItemCompat.SHOW_AS_ACTION_ALWAYS
									| MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
			MenuItemCompat.setActionView(item, mSearchResId);
		}
		if (mNew) {
			MenuItem item = menu.add(
					Menu.NONE,
					R.id.action_new,
					index++,
					mContext.getResources().getString(
							mNewRes == 0 ? R.string.action_new_room : mNewRes))
					.setIcon(R.drawable.ic_action_new);
			MenuItemCompat.setShowAsAction(item,
					MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		}
	}
}

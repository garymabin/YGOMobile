package cn.garymb.ygomobile.fragment;

import java.util.ArrayList;
import java.util.List;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.common.ComplexCursorLoader;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.controller.Controller;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import cn.garymb.ygomobile.provider.YGOCards;
import cn.garymb.ygomobile.utils.ResourceUtils;
import cn.garymb.ygomobile.widget.CardFilterActionBarView;
import cn.garymb.ygomobile.widget.CardFilterGridItem;
import cn.garymb.ygomobile.widget.CardFilterMenuItem;
import cn.garymb.ygomobile.widget.CardFilterRangeItem;
import cn.garymb.ygomobile.widget.CardFilterSearchActionView;
import cn.garymb.ygomobile.widget.GridSelectionDialogController;
import cn.garymb.ygomobile.widget.OnCardFilterChangeListener;
import cn.garymb.ygomobile.widget.adapter.CardAdapter;
import cn.garymb.ygomobile.ygo.ICardFilter;
import cn.garymb.ygomobile.ygo.YGOCardFilter;
import cn.garymb.ygomobile.ygo.YGOCardSelectionBuilder;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindow;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class CardWikiFragment extends BaseFragment implements
		LoaderCallbacks<Cursor>, ActionMode.Callback, OnItemClickListener, OnCardFilterChangeListener, OnClickListener {

	public static final String BUNDLE_KEY_CURSOR_WINDOW = "cardwikifragment.bundle.key.cursor.window";
	public static final String BUNDLE_KEY_PROJECTION = "cardwikifragment.bundle.key.projection";
	public static final String BUNDLE_KEY_INIT_POSITON = "cardwikifragment.bundle.key.init.pos";
	
	private static final String BUNDLE_KEY_INTERNAL_SELECTION = "selection";
	
	private static final int QUERY_SOURCE_LOADER_ID = 0;
	
	private static final int REQUEST_ID_CARD_DETAIL = 0;
	private static final int REQUEST_ID_CARD_FILTER_ATK = 1;
	private static final int REQUEST_ID_CARD_FILTER_DEF = 2;
	private static final int REQUEST_ID_CARD_FILTER_LEVEL = 3;
	private static final int REQUEST_ID_CARD_FILTER_EFFECT = 4;
	
	private static final String TAG = "CardWikiFragment";
	private ComplexCursorLoader mCursorLoader;

	private String[] mProjects = YGOCards.COMMON_DATA_PROJECTION;
	private String[] mProjects_id = YGOCards.COMMON_DATA_PROJECTION_ID;
	
	private String mSelection;
	
	private String[] mSelectionExtra;
	
	private String mSortOrder;

	private Uri mContentUri = YGOCards.CONTENT_URI;

	private CardAdapter mAdapter;
	
	private CardFilterActionBarView mActionBarView;
	private ListView mListView;
	private Context mContext;

	private ActionMode mActionMode;
	
	private CursorWindow mCursorWindow;
	
	private YGOCardSelectionBuilder mSelectionBuilder;
	private ICardFilter mCardFilter;
	
	private CardFilterMenuItem mTypePanel;
	private CardFilterMenuItem mRacePanel;
	private CardFilterMenuItem mPropPanel;
	private CardFilterMenuItem mOTPanel;
	
	private CardFilterRangeItem mAtkPanel;
	private CardFilterRangeItem mDefPanel;
	
	private CardFilterGridItem mLevelPanel;
	private CardFilterGridItem mEffectPanel;
	
	
	private CardFilterSearchActionView mSearchView;
	
	private int mSavedPosition;
	private int mSavedYPixelFromItemTop;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Handler.Callback#handleMessage(android.os.Message)
	 */
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case Constants.ACTION_BAR_EVENT_TYPE_SEARCH:
			Log.i(TAG, "receive action bar search click event");
			mSearchView = (CardFilterSearchActionView) MenuItemCompat.getActionView(mActivity.getMenu().findItem(R.id.action_search));
			View searchPlate = mSearchView.findViewById(R.id.search_plate);
			searchPlate.setBackgroundResource(R.drawable.apptheme_search_edit_text_holo_light);
			mSearchView.setOnCardFilterListener(this);
			mSearchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus) {
						MenuItemCompat.collapseActionView(mActivity.getMenu().findItem(R.id.action_search));
					}
				}
			});
			mSearchView.setQueryHint(mActivity.getResources().getString(R.string.card_search_hint));
			break;
		case Constants.ACTION_BAR_EVENT_TYPE_FILTER:
			Log.i(TAG, "receive action bar filter click event");
			mActionMode = mActivity.startSupportActionMode(this);
			mActionBarView = (CardFilterActionBarView) LayoutInflater.from(mActivity).inflate(R.layout.custom_actionbar_view, null);
			mActionMode.setCustomView(mActionBarView);
			mTypePanel = mActionBarView.addNewPopupMenu(R.menu.filter_type,
					R.string.action_filter_string_type, new int[] {
							R.array.card_type_none, R.array.card_monster_type,
							R.array.card_spell_type, R.array.card_trap_type },
					this, false);
			mTypePanel.setCardFilterDelegate(mCardFilter);
			mRacePanel = mActionBarView.addNewPopupMenu(R.menu.filter_race,
					R.string.action_filter_string_race,
					new int[] { R.array.card_race }, this, false);
			mRacePanel.setCardFilterDelegate(mCardFilter);
			mPropPanel = mActionBarView.addNewPopupMenu(R.menu.filter_property,
					R.string.action_filter_string_property,
					new int[] { R.array.card_attr }, this, false);
			mPropPanel.setCardFilterDelegate(mCardFilter);
			mOTPanel = mActionBarView.addNewPopupMenu(R.menu.filter_ot,
					R.string.action_filter_string_ot,
					new int[] { R.array.card_limit }, this, false);
			mOTPanel.setCardFilterDelegate(mCardFilter);
			
			mAtkPanel = mActionBarView.addNewPopupRangeDialog(
					R.string.action_filter_atk, this,
					this, true);
			mAtkPanel.setCardFilterDelegate(mCardFilter);
			mDefPanel = mActionBarView.addNewPopupRangeDialog(
					R.string.action_filter_def, this,
					this, true);
			mDefPanel.setCardFilterDelegate(mCardFilter);
			mLevelPanel = mActionBarView.addNewPopupGridDialog(R.string.action_filter_level, this, this, true);
			mLevelPanel.setCardFilterDelegate(mCardFilter);
			mEffectPanel = mActionBarView.addNewPopupGridDialog(R.string.action_filter_effect, this, this, true);
			mEffectPanel.setCardFilterDelegate(mCardFilter);
			break;
		case Constants.ACTION_BAR_EVENT_TYPE_RESET:
			if (isAdded()) {
				resetFilters();
				getLoaderManager().restartLoader(QUERY_SOURCE_LOADER_ID, null,
						this);
			}
		default:
			break;
		}
		return false;
	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onResume() {
		super.onResume();
		Controller.peekInstance().registerForActionSearch(mHandler);
		Controller.peekInstance().registerForActionFilter(mHandler);
		Controller.peekInstance().registerForActionReset(mHandler);
	}

	@Override
	public void onPause() {
		super.onPause();
		Controller.peekInstance().unregisterForActionSearch(mHandler);
		Controller.peekInstance().unregisterForActionFilter(mHandler);
		Controller.peekInstance().unregisterForActionReset(mHandler);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		refreshActionBar();
		mCardFilter = new YGOCardFilter();
		mSelectionBuilder = new YGOCardSelectionBuilder();
		mSelection = null;
		mSelectionExtra = null;
		mSortOrder = mProjects[5] + " desc";
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mAdapter.onFragmentInactive();
		resetFilters();
	}


	private void resetFilters() {
		if (mTypePanel != null) {
			mTypePanel.resetFilter();
		}
		if (mRacePanel != null) {
			mRacePanel.resetFilter();
		}
		if (mPropPanel != null) {
			mPropPanel.resetFilter();
		}
		if (mAtkPanel != null) {
			mAtkPanel.resetFilter();
		}
		if (mDefPanel != null) {
			mDefPanel.resetFilter();
		}
		if (mLevelPanel != null) {
			mLevelPanel.resetFilter();
		}
		if (mEffectPanel != null) {
			mEffectPanel.resetFilter();
		}
	}

	private void refreshActionBar() {
		mActivity.onActionBarChange(
				Constants.ACTION_BAR_CHANGE_TYPE_PAGE_CHANGE,
				FRAGMENT_ID_CARD_WIKI, R.layout.card_filter_actionbar_searchview, null);
		setTitle();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContext = getActivity().getApplicationContext();
		ResourceUtils.init(mContext);

		mListView = (ListView) inflater.inflate(R.layout.common_list, null);

		mAdapter = new CardAdapter(mContext, mProjects_id,  null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER, mListView);
		mAdapter.onFragmentActive();
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		initCursorLoader();
		return mListView;
	}
	
	private void initCursorLoader() {
		getLoaderManager().initLoader(QUERY_SOURCE_LOADER_ID, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle bundle) {
		if (bundle != null) {
			mSelection = bundle.getString(BUNDLE_KEY_INTERNAL_SELECTION);
		} else {
			mSelection = null;
		}
		mCursorLoader = new ComplexCursorLoader(mContext, mContentUri, mProjects,
				mSelection, mSelectionExtra, mSortOrder);
		return mCursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		mAdapter.swapCursor(arg1);
		mCursorWindow = mCursorLoader.getCursorWindow();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);
	}

	@Override
	public boolean onCreateActionMode(ActionMode paramActionMode, Menu paramMenu) {
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode paramActionMode,
			Menu paramMenu) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mActivity.getToolbar().setVisibility(View.GONE);
		} else {
			mActivity.getToolbar().setVisibility(View.INVISIBLE);
		}
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode paramActionMode,
			MenuItem paramMenuItem) {
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode paramActionMode) {
		mActionMode = null;
		mActivity.getToolbar().setVisibility(View.VISIBLE);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Bundle bundle = new Bundle();
		bundle.putStringArray(BUNDLE_KEY_PROJECTION, mProjects);
		bundle.putInt(BUNDLE_KEY_INIT_POSITON, position);
		bundle.putParcelable(BUNDLE_KEY_CURSOR_WINDOW, mCursorWindow);
		mSavedPosition = mListView.getFirstVisiblePosition();
		View v = mListView.getChildAt(0);
		mSavedYPixelFromItemTop = (v == null) ? 0 : v.getTop();
		mActivity.navigateToChildFragment(bundle, FRAGMENT_ID_CARD_DETAIL, REQUEST_ID_CARD_DETAIL, false);
		mActivity.setDrawerEnabled(false);
		if (mActionMode != null) {
			mActionMode.finish();
		}
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressWarnings("unchecked")
	@Override
	public void onEventFromChild(int requestCode, int eventType, int arg1, int arg2, Object data) {
		if (REQUEST_ID_CARD_DETAIL == requestCode) {
			mActivity.setDrawerEnabled(true);
			if (eventType == FRAGMENT_NAVIGATION_BACK_EVENT) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					mListView.smoothScrollToPositionFromTop(mSavedPosition + arg1, mSavedYPixelFromItemTop);
				} else {
					mListView.setSelectionFromTop(mSavedPosition + arg1, mSavedYPixelFromItemTop);
				}
				refreshActionBar();
			}
		} else if (REQUEST_ID_CARD_FILTER_ATK == requestCode) {
			mAtkPanel.setRange(ICardFilter.CARD_FILTER_ATK, arg1, arg2);
		} else if (REQUEST_ID_CARD_FILTER_DEF == requestCode) {
			mDefPanel.setRange(ICardFilter.CARD_FILTER_DEF, arg1, arg2);
		} else if (REQUEST_ID_CARD_FILTER_LEVEL == requestCode) {
			mLevelPanel.setSelection(ICardFilter.CARD_FILTER_LEVEL, (List<Integer>) data);
		} else if (REQUEST_ID_CARD_FILTER_EFFECT == requestCode) {
			mEffectPanel.setSelection(ICardFilter.CARD_FILTER_EFFECT, (List<Integer>) data);
		}
	}


	@Override
	public void onChange(int type, String newSelection) {
		Bundle bundle = new Bundle();
		bundle.putString(BUNDLE_KEY_INTERNAL_SELECTION, mSelectionBuilder.setSelection(type, newSelection).toString());
		getLoaderManager().restartLoader(QUERY_SOURCE_LOADER_ID,
				bundle, this);
	}


	@Override
	public void onClick(View v) {
		if (v.equals(mAtkPanel)) {
			Bundle bundle = new Bundle();
			bundle.putInt(ResourcesConstants.MODE_OPTIONS, ResourcesConstants.DIALOG_MODE_FILTER_ATK);
			bundle.putInt("max", mAtkPanel.getMax());
			bundle.putInt("min", mAtkPanel.getMin());
			showDialog(bundle, this, REQUEST_ID_CARD_FILTER_ATK);
		} else if (v.equals(mDefPanel)) {
			Bundle bundle = new Bundle();
			bundle.putInt("max", mDefPanel.getMax());
			bundle.putInt("min", mDefPanel.getMin());
			bundle.putInt(ResourcesConstants.MODE_OPTIONS, ResourcesConstants.DIALOG_MODE_FILTER_DEF);
			showDialog(bundle, this, REQUEST_ID_CARD_FILTER_DEF);
		} else if (v.equals(mLevelPanel)) {
			Bundle bundle = new Bundle();
			bundle.putInt(ResourcesConstants.MODE_OPTIONS, ResourcesConstants.DIALOG_MODE_FILTER_LEVEL);
			bundle.putIntegerArrayList("selection", (ArrayList<Integer>) mLevelPanel.getSelection());
			bundle.putInt("type", GridSelectionDialogController.GRID_SELECTION_TYPE_LEVEL);
			showDialog(bundle, this, REQUEST_ID_CARD_FILTER_LEVEL);
		} else if (v.equals(mEffectPanel)) {
			Bundle bundle = new Bundle();
			bundle.putInt(ResourcesConstants.MODE_OPTIONS, ResourcesConstants.DIALOG_MODE_FILTER_EFFECT);
			bundle.putIntegerArrayList("selection", (ArrayList<Integer>) mEffectPanel.getSelection());
			bundle.putInt("type", GridSelectionDialogController.GRID_SELECTION_TYPE_EFFECT);
			showDialog(bundle, this, REQUEST_ID_CARD_FILTER_EFFECT);
		}
		
	}

}

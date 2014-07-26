/*
 * NavigatorFragment.java
 *
 *  Created on: 2014年3月10日
 *      Author: mabin
 */
package cn.garymb.ygomobile.widget.filebrowser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.widget.HorizontalListView;
import cn.garymb.ygomobile.widget.filebrowser.FileBrowser.OnBrowserListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.SimpleAdapter;

/**
 * @author mabin
 * 
 */
public class FolderNavigator extends FrameLayout implements OnItemClickListener,
		OnBrowserListener {

	public FolderNavigator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FolderNavigator(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public FolderNavigator(Context context) {
		super(context);
	}

	/**
	 * @author mabin
	 * 
	 */
	public interface NavigateItemChangeListener {
		void onItemChange(String path);
	}

	private static final String KEY_FOLDER = "folder";
	private static final String KEY_ARROW = "arrow";
	private SimpleAdapter mAdapter;
	private LinkedList<Map<String, Object>> mItemList;
	private NavigateItemChangeListener mListener;
	private HorizontalListView mListView;
	private String[] mDataFrom = { KEY_FOLDER, KEY_ARROW };
	private int[] mViewTo = { R.id.navigator_folder_name, R.id.navigator_arow };
	private String mCurrentDir;
	
	public void setNavigateItemChangeListener(NavigateItemChangeListener listener) {
		mListener = listener;
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mListView = (HorizontalListView) findViewById(R.id.horizontalListView1);
		mItemList = new LinkedList<Map<String, Object>>();
		mAdapter = new SimpleAdapter(getContext(), mItemList,
				R.layout.navigator_item, mDataFrom, mViewTo);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);

		Map<String, Object> item = new HashMap<String, Object>();
		item.put(KEY_FOLDER, StaticApplication.sRootPair.first);
		item.put(KEY_ARROW, R.drawable.ic_dir_divider_head);
		mItemList.add(item);
		mCurrentDir = StaticApplication.sRootPair.second;
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		performItemClick(arg2);
	}

	/**
	 * 
	 * @author: mabin
	 * 
	 **/
	protected void performItemClick(int pos) {
		String newDir = formatFilePath(pos);
		if (!mCurrentDir.equals(newDir)) {
			mCurrentDir = newDir;
			for (int position = pos + 1, size = mItemList.size(); position < size; position++) {
				mItemList.removeLast();
			}
			Map<String, Object> item = mItemList.get(pos);
			swithToHeadIcon(item, R.drawable.ic_dir_divider_head);
			mAdapter.notifyDataSetChanged();
			mListener.onItemChange(mCurrentDir);
			mListView.setSelectionFromLeft(mItemList.size() - 1, 0);
		}
	}

	/**
	 * 
	 * @author: mabin
	 * 
	 **/
	private String formatFilePath(int position) {
		StringBuilder pathBuider = new StringBuilder();
		pathBuider.append(StaticApplication.sRootPair.second);
		for (int p = 1; p < position + 1; p++) {
			pathBuider.append("/");
			pathBuider.append(mItemList.get(p).get(KEY_FOLDER));
		}
		return pathBuider.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cn.mabin.lanfileshare.ui.widget.FileBrowser.OnBrowserListener#onFileItemClick
	 * (java.lang.String)
	 */
	@Override
	public void onFileItemClick(String filename) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cn.mabin.lanfileshare.ui.widget.FileBrowser.OnBrowserListener#onDirItemClick
	 * (java.lang.String)
	 */
	@Override
	public void onDirItemClick(String path) {
		// TODO Auto-generated method stub
		int index = path.lastIndexOf('/');
		Map<String, Object> previousItem = mItemList.getLast();
		swithToHeadIcon(previousItem, R.drawable.ic_dir_divider);
		Map<String, Object> newItem = new HashMap<String, Object>();
		newItem.put(KEY_FOLDER, path.substring(index + 1, path.length()));
		newItem.put(KEY_ARROW, R.drawable.ic_dir_divider_head);

		mItemList.add(newItem);
		int lastPosition = mItemList.size() - 1;
		mCurrentDir = formatFilePath(lastPosition);
		mListener.onItemChange(mCurrentDir);
		mAdapter.notifyDataSetChanged();
		mListView.setSelectionFromLeft(lastPosition, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cn.mabin.lanfileshare.ui.widget.FileBrowser.OnBrowserListener#toParentDir
	 * ()
	 */
	@Override
	public void toParentDir(String path) {
		// TODO Auto-generated method stub
		mItemList.removeLast();
		Map<String, Object> previousItem = mItemList.getLast();
		swithToHeadIcon(previousItem, R.drawable.ic_dir_divider);
		int lastIndex = mItemList.size() - 1;
		mCurrentDir = formatFilePath(lastIndex);
		mListener.onItemChange(mCurrentDir);
		mAdapter.notifyDataSetChanged();
		mListView.setSelectionFromLeft(lastIndex, 0);
	}

	/**
	 * 
	 * @author: mabin
	 * 
	 **/
	protected void swithToHeadIcon(Map<String, Object> previousItem,
			int drawableRes) {
		previousItem.put(KEY_ARROW, drawableRes);
	}

	public void invalidateViews() {
		mAdapter.notifyDataSetChanged();
	}

	public void setVisible(boolean isVisible) {
		mListView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
	}

	public String getCurrentDir() {
		return mCurrentDir;
	}
}

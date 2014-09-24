package cn.garymb.ygomobile.fragment;

import java.util.List;

import cn.garymb.ygodata.YGOGameOptions;
import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.core.Controller;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import cn.garymb.ygomobile.widget.adapter.RoomAdapter;
import cn.garymb.ygomobile.ygo.YGORoomInfo;
import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class RoomPageFragment extends BaseFragment implements OnItemClickListener, ResourcesConstants {
	
	private static final String TAG = "RoomPageFragment";
	
	private ListView mContentView;
	private RoomAdapter mAdapter;
	private List<YGORoomInfo> mData;
	
	private boolean isDataBinded = false;
	
	public static RoomPageFragment newInstance(int index) {
		RoomPageFragment fragment = new RoomPageFragment();
		
		Bundle data = new Bundle();
		data.putInt("index", index);
		fragment.setArguments(data);
		return fragment;
	}
	
	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach: E");
		super.onAttach(activity);
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onPause: E");
		super.onPause();
		mData = null;
		isDataBinded = false;
		if (mData != null) {
			mData.clear();
		}
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onResume: E");
		super.onResume();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onCreateView: E");
		mContentView = (ListView) inflater.inflate(R.layout.common_list, null);
		mContentView.setOnItemClickListener(this);
		isDataBinded = false;
		return mContentView;
	}
	
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onDetach: E");
		super.onDetach();
	}

	/* (non-Javadoc)
	 * @see android.os.Handler.Callback#handleMessage(android.os.Message)
	 */
	@Override
	public boolean handleMessage(Message msg) {
		return false;
	}

	/**
	 * 
	 * @return
	**/
	/*package*/ void setData(List<YGORoomInfo> data) {
		mData = data;
		if (mAdapter == null) {
			Log.d(TAG, "create new adapter " + getArguments().getInt("index", 0));
			mAdapter = new RoomAdapter(mData, mActivity, getArguments().getInt("index", 0));
		} else {
			mAdapter.setData(mData);
			mAdapter.notifyDataSetChanged();
		}
		if (mContentView != null && !isDataBinded && !isDetached()) {
			Log.d(TAG, "bind view data with index " + getArguments().getInt("index", 0));
			mContentView.setAdapter(mAdapter);
			mContentView.setDivider(null);
			mContentView.setDividerHeight(4);
			isDataBinded = true;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		YGORoomInfo info = (YGORoomInfo) mAdapter.getItem(position);
		Bundle data = new Bundle();
		YGOGameOptions options = new YGOGameOptions();
		options.mName = Controller.peekInstance().getLoginName();
		options.mMode = info.mode;
		options.mServerAddr = mActivity.getServer() == null ?  ""  :mActivity.getServer().ipAddrString;
		options.mPort = mActivity.getServer().port;
		options.mRoomName = info.name;
		options.mRoomPasswd = "";
		options.setCompleteOptions(info.isCompleteInfo());
		if (info.isCompleteInfo()) {
			options.mDrawCount = info.drawCount == -1 ? 1 : info.drawCount;
			options.mEnablePriority = info.enablePriority;
			options.mNoDeckCheck = info.noDeckCheck;
			options.mNoDeckShuffle = info.noDeckShuffle;
			options.mRule = info.rule == -1 ? 0 : info.rule;
			options.mStartHand = info.startHand == -1 ? 5 : info.startHand;
			options.mStartLP = info.startLp == -1 ? 8000 : info.startLp;
		}
		data.putParcelable(GAME_OPTIONS, options);
		data.putBoolean(PRIVATE_OPTIONS, info.privacy);
		data.putInt(ResourcesConstants.MODE_OPTIONS, ResourcesConstants.DIALOG_MODE_JOIN_GAME);
		showDialog(data);
	}

}

package cn.garymb.ygomobile.fragment;

import java.util.List;

import cn.garymb.ygodata.YGOGameOptions;
import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.core.Controller;
import cn.garymb.ygomobile.model.Model;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import cn.garymb.ygomobile.widget.adapter.RoomAdapter;
import cn.garymb.ygomobile.ygo.YGORoomInfo;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class RoomPageFragment extends BaseFragment implements
		OnItemClickListener, ResourcesConstants {

	private ListView mContentView;
	private RoomAdapter mAdapter;

	public static RoomPageFragment newInstance(int index) {
		RoomPageFragment fragment = new RoomPageFragment();

		Bundle data = new Bundle();
		data.putInt("index", index);
		fragment.setArguments(data);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContentView = (ListView) inflater.inflate(R.layout.common_list, null);
		mAdapter = new RoomAdapter(mActivity, getArguments().getInt("index", 0));
		mContentView.setOnItemClickListener(this);
		mContentView.setAdapter(mAdapter);
		setData(Model.peekInstance().getRooms());
		return mContentView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Handler.Callback#handleMessage(android.os.Message)
	 */
	@Override
	public boolean handleMessage(Message msg) {
		return false;
	}

	/* package */void setData(List<YGORoomInfo> data) {
		mAdapter.setData(data);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		YGORoomInfo info = (YGORoomInfo) mAdapter.getItem(position);
		Bundle data = new Bundle();
		YGOGameOptions options = new YGOGameOptions();
		options.mName = Controller.peekInstance().getLoginName();
		options.mMode = info.mode;
		options.mServerAddr = mActivity.getServer() == null ? "" : mActivity
				.getServer().ipAddrString;
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
		data.putInt(ResourcesConstants.MODE_OPTIONS,
				ResourcesConstants.DIALOG_MODE_JOIN_GAME);
		showDialog(data);
	}

}

package cn.garymb.ygomobile.fragment;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.SuperToast.OnClickListener;
import com.github.johnpersano.supertoasts.util.OnClickWrapper;

import cn.garymb.ygodata.YGOGameOptions;
import cn.garymb.ygomobile.R;
import cn.garymb.ygomobie.model.Model;
import cn.garymb.ygomobile.StaticApplication;
import cn.garymb.ygomobile.YGOMobileActivity;
import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.core.Controller;
import cn.garymb.ygomobile.model.data.DataStore;
import cn.garymb.ygomobile.model.data.ResourcesConstants;
import cn.garymb.ygomobile.widget.ServerOperationPanel;
import cn.garymb.ygomobile.widget.ServerOperationPanel.ServerOperationListener;
import cn.garymb.ygomobile.ygo.YGOServerInfo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;

public class ServerListFragment extends BaseFragment implements ServerOperationListener, OnGroupExpandListener, OnClickListener {

	public static class ServerAdapter extends BaseExpandableListAdapter {

		private SparseArray<YGOServerInfo> mServers;
		private LayoutInflater mInflater;
		private ServerOperationListener mListener;

		public ServerAdapter(LayoutInflater inflater,
				SparseArray<YGOServerInfo> servers,
				ServerOperationListener listener) {
			mServers = servers;
			mInflater = inflater;
			mListener = listener;
		}

		@Override
		public int getGroupCount() {
			return mServers.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		@Override
		public YGOServerInfo getGroup(int groupPosition) {
			return mServers.get(mServers.keyAt(groupPosition));
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return null;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return mServers.keyAt(groupPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.server_item, null);
			}
			YGOServerInfo info = getGroup(groupPosition);
			((TextView) convertView.findViewById(R.id.server_user_name))
			.setText(info.userName);
			((TextView) convertView.findViewById(R.id.server_name))
					.setText(info.name);
			((TextView) convertView.findViewById(R.id.server_addr))
					.setText(info.ipAddrString);
			((TextView) convertView.findViewById(R.id.server_port))
					.setText(info.port + "");
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			View v = convertView;

			if (v == null) {
				v = mInflater.inflate(R.layout.server_ops_expand_view, parent,
						false);
				((ServerOperationPanel) v.findViewById(R.id.handle_panel))
						.setServerOperationListener(mListener);
			}
			((ServerOperationPanel) v.findViewById(R.id.handle_panel))
					.setGroupPosition(groupPosition);
			long id = getGroupId(groupPosition);
			View deletePanel = v.findViewById(R.id.delete_panel);
			if (id < DataStore.USER_DEFINE_SERVER_INFO_START) {
				deletePanel.setVisibility(View.GONE);
			} else {
				deletePanel.setVisibility(View.VISIBLE);
			}
			return v;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}
	
	private static final int REQUEST_CODE_SERVER = 0;

	private ExpandableListView mListView;

	private ServerAdapter mAdapter;

	private float mScreenWidth;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity.onActionBarChange(
				Constants.ACTION_BAR_CHANGE_TYPE_PAGE_CHANGE,
				FRAGMENT_ID_DUEL, R.string.action_new_server, null);
		mScreenWidth = StaticApplication.peekInstance().getScreenWidth();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mListView = (ExpandableListView) inflater.inflate(
				R.layout.common_expanable_list, null);
		mAdapter = new ServerAdapter(inflater, Model.peekInstance()
				.getServers(), this);
		mListView.setAdapter(mAdapter);
		setIndicator();
		mListView.setOnGroupExpandListener(this);
		return mListView;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private void setIndicator() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			mListView.setIndicatorBoundsRelative((int) (mScreenWidth - 80),
					(int) (mScreenWidth - 20));
		} else {
			mListView.setIndicatorBounds((int) (mScreenWidth - 80),
					(int) (mScreenWidth - 20));
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		resizeIndicator(newConfig);
	}

	private void resizeIndicator(Configuration newConfig) {
		DisplayMetrics dm = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		mScreenWidth = dm.widthPixels;
		setIndicator();
	}

	@Override
	public void onResume() {
		super.onResume();
		Controller.peekInstance().registerForActionNew(mHandler);
		Controller.peekInstance().registerForActionPlay(mHandler);
	}

	@Override
	public void onPause() {
		super.onPause();
		Controller.peekInstance().unregisterForActionNew(mHandler);
		Controller.peekInstance().unregisterForActionPlay(mHandler);
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case Constants.ACTION_BAR_EVENT_TYPE_NEW:
			Bundle bundle = new Bundle();
			bundle.putInt(ResourcesConstants.MODE_OPTIONS,
					ResourcesConstants.DIALOG_MODE_ADD_NEW_SERVER);
			int index = (int) mAdapter.getGroupId(mAdapter.getGroupCount() - 1);
			bundle.putInt("index", ++index);
			showDialog(bundle, this, REQUEST_CODE_SERVER);
			break;
		case Constants.ACTION_BAR_EVENT_TYPE_PLAY:
			Intent intent = new Intent(getActivity(), YGOMobileActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent);
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public void onEventFromChild(int requestCode, int eventType, int arg1,
			int arg2, Object data) {
		if (requestCode == REQUEST_CODE_SERVER) {
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onOperation(int operationId, int position) {
		if (operationId == ServerOperationPanel.SERVER_OPERATION_CONNECT) {
			YGOServerInfo info = mAdapter.getGroup(position);
			YGOGameOptions options = new YGOGameOptions();
			options.mServerAddr = info.ipAddrString;
			options.mPort = info.port;
			options.mName = info.userName;
			Intent intent = new Intent(getActivity(), YGOMobileActivity.class);
			intent.putExtra(YGOGameOptions.YGO_GAME_OPTIONS_BUNDLE_KEY, options);
			startActivity(intent);
		} else if (operationId == ServerOperationPanel.SERVER_OPERATION_EDIT) {
			Bundle bundle = new Bundle();
			bundle.putInt(ResourcesConstants.MODE_OPTIONS,
					ResourcesConstants.DIALOG_MODE_EDIT_SERVER);
			int index = (int) mAdapter.getGroupId(position);
			bundle.putParcelable("server", mAdapter.getGroup(position));
			bundle.putInt("index", index);
			showDialog(bundle, this, REQUEST_CODE_SERVER);
		} else if (operationId == ServerOperationPanel.SERVER_OPERATION_DELETE) {
			YGOServerInfo info = mAdapter.getGroup(position);
			SuperActivityToast superActivityToast = new SuperActivityToast(mActivity, SuperToast.Type.BUTTON);
			superActivityToast.setDuration(SuperToast.Duration.EXTRA_LONG);
			superActivityToast.setText(getResources().getString(R.string.toast_delete, info.name));
			superActivityToast.setButtonIcon(SuperToast.Icon.Dark.UNDO, "UNDO");
			superActivityToast.setOnClickWrapper(new OnClickWrapper(info.id, this), info);
			superActivityToast.show();
			int index = (int)mAdapter.getGroupId(position);
			Model.peekInstance().removeServer(index);
			mAdapter.notifyDataSetChanged();
		}
		mListView.collapseGroup(position);
	}

	@Override
	public void onGroupExpand(int groupPosition) {
		int len = mAdapter.getGroupCount();
		for (int i = 0; i < len; i++) {
			if (i != groupPosition) {
				mListView.collapseGroup(i);
			}
		}
	}

	@Override
	public void onClick(View view, Parcelable token) {
		Model.peekInstance().addNewServer((YGOServerInfo) token);
		mAdapter.notifyDataSetChanged();
	}
}

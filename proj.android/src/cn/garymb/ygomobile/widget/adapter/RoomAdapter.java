package cn.garymb.ygomobile.widget.adapter;

import java.util.LinkedList;
import java.util.List;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.ygo.YGORoomInfo;
import cn.garymb.ygomobile.ygo.YGOUserInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RoomAdapter extends BaseAdapter {

	public static class ViewHolder {
		public ImageView mLockImage;
		public ImageView mCustomImage;
		public TextView mTitle;
		public TextView mProperty;
		public TextView mStatus;
	}

	private List<YGORoomInfo> mDataList;
	private Context mContext;

	private int mFilter;

	public RoomAdapter(Context context, int filter) {
		mContext = context;
		mFilter = filter;
	}

	public void setData(List<YGORoomInfo> lists) {
		mDataList = new LinkedList<YGORoomInfo>();
		for (YGORoomInfo info : lists) {
			if (info.mode == mFilter) {
				if (!info.status) {
					((LinkedList<YGORoomInfo>)mDataList).addFirst(info.clone());
				} else {
					((LinkedList<YGORoomInfo>)mDataList).addLast(info.clone());
				}
			}
		}
	}

	@Override
	public int getCount() {
		return mDataList == null ? 0 :mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataList == null ? null :mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.room_list_item, null);
			ViewHolder holder = new ViewHolder();
			holder.mTitle = (TextView) convertView
					.findViewById(R.id.item_list_name);
			holder.mLockImage = (ImageView) convertView
					.findViewById(R.id.item_flag_image);
			holder.mCustomImage = (ImageView) convertView
					.findViewById(R.id.item_list_icon);
			holder.mProperty = (TextView) convertView
					.findViewById(R.id.item_property_text);
			holder.mStatus = (TextView) convertView
					.findViewById(R.id.item_list_status);
			convertView.setTag(holder);
		}
		YGORoomInfo info = mDataList.get(position);
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.mCustomImage.setImageResource(R.drawable.logo);
		holder.mTitle.setText(info.name);
		holder.mProperty
				.setText(generatePropertyString(info));
		holder.mStatus.setText(info.status ? mContext.getString(R.string.ongoing)
				: mContext.getString(R.string.pending));
		if (info.privacy) {
			holder.mLockImage.setVisibility(View.VISIBLE);
		} else {
			holder.mLockImage.setVisibility(View.INVISIBLE);
		}
		return convertView;
	}

	private String generatePropertyString(YGORoomInfo roomInfo) {
		StringBuilder builder = new StringBuilder();
		for (YGOUserInfo userInfo : roomInfo.mUsers) {
			builder.append(userInfo.name + " | ");
		}
		if (builder.length() > 2) {
			builder.delete(builder.length() - 2, builder.length());
		}
		return builder.toString();
	}
}

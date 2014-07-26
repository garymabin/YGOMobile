package cn.garymb.ygomobile.widget;

import java.util.ArrayList;
import java.util.List;

import cn.garymb.ygomobile.R;

import android.content.Context;
import android.content.res.Resources;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class GridSelectionDialogController extends BaseDialogConfigController implements OnClickListener {
	
	public final class GridSelectionAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		
		public GridSelectionAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mGridString.length;
		}

		@Override
		public String getItem(int position) {
			return mGridString[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.grid_item, null);
				convertView.setOnClickListener(GridSelectionDialogController.this);
			}
			convertView.setTag(position);
			((TextView)convertView.findViewById(R.id.cardSelectionLabel1)).setText(mGridString[position]);
			if (mSelectionMap.get(position)) {
				((CardSelectionLabel)convertView).setSelect(true);
			} else {
				((CardSelectionLabel)convertView).setSelect(false);
			}
			return convertView;
		}

	}

	public static final int GRID_SELECTION_TYPE_LEVEL = 0;
	public static final int GRID_SELECTION_TYPE_EFFECT = 1;

	private GridView mGridView;
	
	private BaseAdapter mAdapter;
	
	private SparseBooleanArray mSelectionMap;
	
	private int mSize;
	
	private String[] mGridString;

	public GridSelectionDialogController(DialogConfigUIBase configUI, View view, int gridRes, int type, List<Integer> initSelection) {
		super(configUI, view);
		final Context context = configUI.getContext();
		final Resources res = context.getResources();
		mSelectionMap = new SparseBooleanArray();
		mGridView = (GridView) view.findViewById(R.id.grid_view);
		mGridString = context.getResources().getStringArray(gridRes);
		mSize = mGridString.length;
		for (int i = 0; i < mSize; i++) {
			if (initSelection != null && initSelection.contains(i)) {
				mSelectionMap.append(i, true);
			} else {
				mSelectionMap.append(i, false);
			}
			
		}
		mAdapter = new GridSelectionAdapter(context);
		mGridView.setAdapter(mAdapter);
		if (type == GRID_SELECTION_TYPE_LEVEL) {
			mConfigUI.setTitle(R.string.action_filter_level);
		} else if (type == GRID_SELECTION_TYPE_EFFECT) {
			mConfigUI.setTitle(R.string.action_filter_effect);
		}
		mConfigUI.setCancelButton(res.getString(R.string.button_cancel));
		mConfigUI.setPositiveButton(res.getString(R.string.action_filter));
	}

	public List<Integer> getSelections() {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < mSize; i++) {
			if (mSelectionMap.get(i)) {
				list.add(i);
			}
			
		}
		return list;
	}

	@Override
	public void onClick(View v) {
		int position = (Integer) v.getTag();
		mSelectionMap.put(position, !mSelectionMap.get(position));
	}
}

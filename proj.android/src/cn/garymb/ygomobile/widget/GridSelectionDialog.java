package cn.garymb.ygomobile.widget;

import java.util.List;

import cn.garymb.ygomobile.R;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

public class GridSelectionDialog extends BaseDialog {
	
	private int mGridRes;
	
	private List<Integer> mInitSelection;
	
	private int mType;
	
	public GridSelectionDialog(Context context, DialogInterface.OnClickListener listener, int gridRes, Bundle param) {
		super(context, listener);
		mGridRes = gridRes;
		mInitSelection = param.getIntegerArrayList("selection");
		mType = param.getInt("type");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mView = LayoutInflater.from(getContext()).inflate(R.layout.grid_slection_dialog_content, null);
		setView(mView);
		mController = new GridSelectionDialogController(this, mView, mGridRes, mType, mInitSelection);
		super.onCreate(savedInstanceState);
	}

}

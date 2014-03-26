/*
 * CommonDialogFragment.java
 *
 *  Created on: 2014年3月18日
 *      Author: mabin
 */
package cn.garymb.ygomobile.fragment;

import cn.garymb.ygomobile.R;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author mabin
 *
 */
public class CommonDialogFragment extends DialogFragment implements OnTouchListener {
	
	public static final int WELFARE_DIALOG_TYPE_RADAR = 0;
	public static final int WELFARE_DIALOG_TYPE_UPDATE = 1;
	
	private ViewGroup mContentView;
	private OnClickListener mOnClickListener;
	private TextView mFirstView;
	private TextView mSecondView;
	private int mTitleRes;
	private String mItem1;
	private String mItem2;
	

    /**
     * Create a new instance of CommonDialogFragment, providing "num"
     * as an argument.
     */
    public static CommonDialogFragment newInstance(Bundle bundle) {
    	CommonDialogFragment f = new CommonDialogFragment();

        // Supply num input as an argument.
        f.setArguments(bundle);

        return f;
    }
    
    public void setOnclickListener(OnClickListener l) {
    	mOnClickListener = l;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitleRes = getArguments().getInt("title");
        mItem1 = getArguments().getString("item1");
        mItem2 = getArguments().getString("item2");
        setStyle(CommonDialogFragment.STYLE_NO_FRAME, R.style.CustomDialog);
    }
    
    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onViewCreated(android.view.View, android.os.Bundle)
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	view.setOnTouchListener(this);
    	super.onViewCreated(view, savedInstanceState);
    }
    
    /* (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	Dialog dlg =  super.onCreateDialog(savedInstanceState);
    	dlg.setTitle(mTitleRes);
    	return dlg;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	mContentView = (ViewGroup) inflater.inflate(R.layout.single_choice, null);
    	mFirstView = (TextView) mContentView.findViewById(R.id.choice_1);
    	mSecondView = (TextView) mContentView.findViewById(R.id.choice_2);
    	mFirstView.setText(mItem1);
    	mFirstView.setText(mItem2);
    	if (mOnClickListener != null) {
    		mFirstView.setOnClickListener(mOnClickListener);
    		mSecondView.setOnClickListener(mOnClickListener);
    	}
        return mContentView;
    }

	/* (non-Javadoc)
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}
}

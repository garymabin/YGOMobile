/*
 * SelectableItem.java
 *
 *  Created on: 2014年3月15日
 *      Author: mabin
 */package cn.garymb.ygomobile.widget.filebrowser;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author mabin
 *
 */
public class SelectableItem extends ImageView implements ISelectable {

	/**
	 * @param context
	 */
	public SelectableItem(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		
	}
	/**
	 * @param context
	 * @param attrs
	 */
	public SelectableItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		
	}
	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public SelectableItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub

	}

	/* (non-Javadoc)
	 * @see cn.mabin.lanfileshare.ui.widget.ISelectable#setSelected(java.lang.Boolean)
	 */
	@Override
	public void setSelected(Boolean isSelected) {
		// TODO Auto-generated method stub
		super.setSelected(isSelected);
	}

	/* (non-Javadoc)
	 * @see cn.mabin.lanfileshare.ui.widget.ISelectable#getSelected()
	 */
	@Override
	public boolean getSelected() {
		// TODO Auto-generated method stub
		return super.isSelected();
	}

}

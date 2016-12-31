/*
 * SelectableItem.java
 *
 *  Created on: 2014年3月15日
 *      Author: mabin
 */package cn.garymb.ygomobile.widget.filebrowser;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

/**
 * @author mabin
 *
 */
public class SelectableItem extends CheckBox implements ISelectable {

	/**
	 * @param context
	 */
	public SelectableItem(Context context) {
		super(context);
		
	}
	/**
	 * @param context
	 * @param attrs
	 */
	public SelectableItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}
	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public SelectableItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/* (non-Javadoc)
	 * @see cn.garymb.ygomoile.widget.filebrowser.ISelectable#setSelected(java.lang.Boolean)
	 */
	@Override
	public void setSelected(Boolean isSelected) {
		super.setChecked(isSelected);
	}

	/* (non-Javadoc)
	 * @see cn.garymb.ygomoile.widget.filebrowser.ISelectable#getSelected()
	 */
	@Override
	public boolean getSelected() {
		return super.isChecked();
	}

}

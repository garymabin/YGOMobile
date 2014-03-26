/*
 * ISharingItemInterface.java
 *
 *  Created on: 2014年3月15日
 *      Author: mabin
 */package cn.garymb.ygomobile.widget.filebrowser;

import java.util.Observable;

import cn.garymb.ygomobile.widget.filebrowser.SharingItemBase.SharingItemSelectListener;


/**
 * @author mabin
 *
 */
public interface ISharingItemInterface {
	public void setListener(SharingItemSelectListener listener);
	public void setUrl(String url);
	public void toggoleBackground();
}

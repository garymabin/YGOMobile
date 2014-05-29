package cn.garymb.ygomobie.model;

import android.os.Message;

/**
 * @author mabin
 *
 */
public interface IDataObserver {
	void notifyDataUpdate(Message msg);
}

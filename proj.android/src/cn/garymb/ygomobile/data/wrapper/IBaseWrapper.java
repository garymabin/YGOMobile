package cn.garymb.ygomobile.data.wrapper;

import cn.garymb.ygomobile.model.data.ResourcesConstants;


/**
 * @author mabin
 * 
 */
public interface IBaseWrapper extends ResourcesConstants {

	public static final int TASK_STATUS_SUCCESS = 0;
	public static final int TASK_STATUS_FAILED = 1;
	public static final int TASK_STATUS_CANCELED = 2;

	/**
	 * 
	 * @brief clear all data and force GC. Never use this object after recycle()
	 * @author: mabin
	 * 
	 */
	void recyle();

	/**
	 * 
	 * @brief get certain url to connect
	 * @author: mabin
	 * 
	 */
	String getUrl(int index);
	
	/**
	 * @brief return requestType
	 * @return
	 */
	int getRequestType();
}

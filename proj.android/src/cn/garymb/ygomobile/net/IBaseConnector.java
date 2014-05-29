package cn.garymb.ygomobile.net;

import cn.garymb.ygomobile.data.wrapper.BaseDataWrapper;


public interface IBaseConnector {
	
	void get(BaseDataWrapper wrapper) throws InterruptedException;
	
}

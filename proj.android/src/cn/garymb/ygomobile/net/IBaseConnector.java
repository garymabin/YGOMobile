package cn.garymb.ygomobile.net;

import cn.garymb.ygomobile.data.wrapper.BaseRequestWrapper;


public interface IBaseConnector {
	
	void get(BaseRequestWrapper wrapper) throws InterruptedException;
	
}

package cn.garymb.ygomobile.net;

import cn.garymb.ygomobile.data.wrapper.BaseRequestJob;


public interface IBaseConnector {
	
	void get(BaseRequestJob wrapper) throws InterruptedException;
	
}

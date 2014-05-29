package cn.garymb.ygomobile.core;

public interface IBaseThread {
	
	public static final Object sServerLock = new Object();
	
	/**
	 * Terminate the thread;
	 */
	void terminate();
	
	/**
	 * Start the thread;
	 */
	void start();
	
	boolean isRunning();
}

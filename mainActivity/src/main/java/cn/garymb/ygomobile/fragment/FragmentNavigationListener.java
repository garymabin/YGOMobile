package cn.garymb.ygomobile.fragment;


public interface FragmentNavigationListener {
	
	public static final int FRAGMENT_NAVIGATION_BACK_EVENT = 0x0;
	
	public static final int FRAGMENT_NAVIGATION_CARD_EVENT = 0x1;
	
	public static final int FRAGMENT_NAVIGATION_DUEL_LOGIN_ATTEMP_EVENT = 0x2;
	
	public static final int FRAGMENT_NAVIGATION_DUEL_FREE_MODE_EVENT = 0x3;
	
	public static final int FRAGMENT_NAVIGATION_DUEL_LOGIN_SUCCEED_EVENT = 0x4;
	
	public static final int FRAGMENT_NAVIGATION_DUEL_CREATE_SERVER_EVENT = 0x5;

	void onEventFromChild(int requestCode, int eventType, int arg1, int arg2, Object data);
}

package cn.garymb.ygomobile.controller.actionbar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.common.Constants;

import android.os.Handler;
import android.view.MenuItem;

public class ActionBarController {

	private List<WeakReference<Handler>> mActionNewList;
	private List<WeakReference<Handler>> mActionFilterList;
	private List<WeakReference<Handler>> mActionSettingsList;
	private List<WeakReference<Handler>> mActionCardImageDLList;
	private List<WeakReference<Handler>> mActionPlayList;
	private List<WeakReference<Handler>> mActionSearchList;
	private List<WeakReference<Handler>> mActionSupportList;
	private List<WeakReference<Handler>> mActionResetList;

	public ActionBarController() {
		mActionNewList = new ArrayList<WeakReference<Handler>>(3);
		mActionSettingsList = new ArrayList<WeakReference<Handler>>(3);
		mActionCardImageDLList = new ArrayList<WeakReference<Handler>>(3);
		mActionPlayList = new ArrayList<WeakReference<Handler>>(3);
		mActionSearchList = new ArrayList<WeakReference<Handler>>(3);
		mActionFilterList = new ArrayList<WeakReference<Handler>>(3);
		mActionSupportList = new ArrayList<WeakReference<Handler>>(3);
		mActionResetList = new ArrayList<WeakReference<Handler>>(3);
	}

	public boolean handleAction(MenuItem item) {
		boolean handled = true;
		switch (item.getItemId()) {
		case R.id.action_settings:
			notifyTarget(mActionSettingsList,
					Constants.ACTION_BAR_EVENT_TYPE_SETTINGS);
			break;
		case R.id.action_check_dl_image:
			notifyTarget(mActionCardImageDLList, Constants.ACTION_BAR_EVENT_TYPE_CARD_IAMGE_DL);
			break;
		case R.id.action_new:
			notifyTarget(mActionNewList, Constants.ACTION_BAR_EVENT_TYPE_NEW);
			break;
		case R.id.action_play:
			notifyTarget(mActionPlayList, Constants.ACTION_BAR_EVENT_TYPE_PLAY);
			break;
		case R.id.action_search:
			notifyTarget(mActionSearchList,
					Constants.ACTION_BAR_EVENT_TYPE_SEARCH);
			break;
		case R.id.action_filter:
			notifyTarget(mActionFilterList,
					Constants.ACTION_BAR_EVENT_TYPE_FILTER);
			break;
		case R.id.action_support:
			notifyTarget(mActionSupportList,
					Constants.ACTION_BAR_EVENT_TYPE_DONATE);
			break;
		case R.id.action_reset:
			notifyTarget(mActionResetList, Constants.ACTION_BAR_EVENT_TYPE_RESET);
			break;
		default:
			handled = false;
			break;
		}
		return handled;
	}

	private void notifyTarget(List<WeakReference<Handler>> list, int msgType) {
		for (WeakReference<Handler> item : list) {
			Handler h = item.get();
			if (h != null) {
				h.sendEmptyMessage(msgType);
			}
		}
	}

	public void registerForActionNew(Handler h) {
		WeakReference<Handler> ref = new WeakReference<Handler>(h);
		mActionNewList.add(ref);
	}

	public void registerForActionSearch(Handler h) {
		WeakReference<Handler> ref = new WeakReference<Handler>(h);
		mActionSearchList.add(ref);
	}

	public void unregisterForActionSearch(Handler h) {
		for (WeakReference<Handler> item : mActionSearchList) {
			if (h == item.get()) {
				mActionSearchList.remove(item);
				item = null;
				break;
			}
		}
	}

	public void registerForActionFilter(Handler h) {
		WeakReference<Handler> ref = new WeakReference<Handler>(h);
		mActionFilterList.add(ref);
	}

	public void unregisterForActionFilter(Handler h) {
		for (WeakReference<Handler> item : mActionFilterList) {
			if (h == item.get()) {
				mActionFilterList.remove(item);
				item = null;
				break;
			}
		}
	}

	public void unregisterForActionNew(Handler h) {
		for (WeakReference<Handler> item : mActionNewList) {
			if (h == item.get()) {
				mActionNewList.remove(item);
				item = null;
				break;
			}
		}
	}

	public void registerForActionPlay(Handler h) {
		WeakReference<Handler> ref = new WeakReference<Handler>(h);
		mActionPlayList.add(ref);
	}

	public void unregisterForActionPlay(Handler h) {
		for (WeakReference<Handler> item : mActionPlayList) {
			if (h == item.get()) {
				mActionPlayList.remove(item);
				item = null;
				break;
			}
		}
	}

	public void registerForActionSettings(Handler h) {
		WeakReference<Handler> ref = new WeakReference<Handler>(h);
		mActionSettingsList.add(ref);
	}

	public void unregisterForActionSettings(Handler h) {
		for (WeakReference<Handler> item : mActionSettingsList) {
			if (h == item.get()) {
				mActionSettingsList.remove(item);
				item = null;
				break;
			}
		}
	}
	
	public void registerForActionCardImageDL(Handler h) {
		WeakReference<Handler> ref = new WeakReference<Handler>(h);
		mActionCardImageDLList.add(ref);
	}

	public void unregisterForActionCardImageDL(Handler h) {
		for (WeakReference<Handler> item : mActionCardImageDLList) {
			if (h == item.get()) {
				mActionCardImageDLList.remove(item);
				item = null;
				break;
			}
		}
	}

	public void registerForActionSupport(Handler h) {
		WeakReference<Handler> ref = new WeakReference<Handler>(h);
		mActionSupportList.add(ref);
	}

	public void unregisterForActionSupport(Handler h) {
		for (WeakReference<Handler> item : mActionSupportList) {
			if (h == item.get()) {
				mActionSupportList.remove(item);
				item = null;
				break;
			}
		}
	}
	
	public void registerForActionReset(Handler h) {
		WeakReference<Handler> ref = new WeakReference<Handler>(h);
		mActionResetList.add(ref);		
	}

	public void unregisterForActionReset(Handler h) {
		for (WeakReference<Handler> item : mActionSupportList) {
			if (h == item.get()) {
				mActionResetList.remove(item);
				item = null;
				break;
			}
		}		
	}
}

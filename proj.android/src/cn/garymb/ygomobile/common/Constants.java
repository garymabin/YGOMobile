package cn.garymb.ygomobile.common;

/**
 * @author mabin
 *
 */
public interface Constants {
	
	/*change this will affect C++ code, be careful!*/
	public static final String WORKING_DIRECTORY = "/ygocore/";
	
	/*change this will affect C++ code, be careful!*/
	public static final int COMPAT_GUI_MODE_COMBOBOX = 0;
	
	/*change this will affect C++ code, be careful!*/
	public static final int COMPAT_GUI_MODE_CHECKBOXES_PANEL = 1;
	
	
	public static final String CONFIG_FILE = "system.conf";
	public static final String CARD_DB_FILE = "cards.cdb";
	
	public static final int RESOURCE_ERROR_SDCARD_NOT_AVAIL = -1;
	public static final int RESOURCE_ERROR_NOT_EXIST = -2;
	public static final int RESOURCE_ERROR_CONFIG_FILE_NOT_EXIST = -3;
	public static final int RESOURCE_ERROR_CARDS_DB_FILE_NOT_EXIST = -4;
	public static final int RESOURCE_ERROR_NONE = 0;

	public static final int IO_BUFFER_SIZE = 8192;

	public static final String ENCODING = "UTF-8";

	public static final String INTENT_EXTRA_PATH_KEY = "ygomobile.extra.path";

	public static final String OPENGL_PATH = "opengl";
	public static final String CARD_QUALITY_PATH = "card_quality";
	
	//Virual Help overlay handle ops
	
	public static final int MODE_CANCEL_CHAIN_OPTIONS = 0;
	public static final int MODE_REFRESH_OPTION = 1;
	public static final int MODE_IGNORE_CHAIN_OPTION = 2;
	public static final int MODE_REACT_CHAIN_OPTION = 3;
	
	public static final String SYSTEM_FONT_DIR = "/system/fonts/";
	public static final String FONT_DIRECTORY = "/fonts/";
	public static final String DEFAULT_FONT_NAME = "DroidSansFallback.ttf";
	public static final String CARD_IMAGE_DIRECTORY = "/pics/";
	public static final String THUMBNAIL_IMAGE_DIRECTORY = "/thumbnail/";
	
	public static final String CORE_CONFIG_PATH = "core";
	
	public static final String CORE_SKIN_PATH = "textures";
	
	public static final String CORE_EXTRA_PATH = "extra";
	
	public static final String CORE_DECK_PATH = "deck";
	
	public static final String CORE_SKIN_COVER = "bg.jpg";
	public static final String CORE_SKIN_CARD_BACK = "cover.jpg";
	public static final int[]  CORE_SKIN_COVER_SIZE = new int[]{1024, 640};
	public static final int[]  CORE_SKIN_CARD_BACK_SIZE = new int[]{177, 254};
	
	public static final String DEFAULT_DECK_NAME = "new.ydk";
	
	public static final String DEFAULT_OGLES_CONFIG = "1";
	
	public static final String DEFAULT_CARD_QUALITY_CONFIG = "1";
	
	public static final int TRANSACT_TIMEOUT = 2 * 60 * 1000;
	
	public static final int FRAGMENT_ID_DUEL = 1;
	public static final int FRAGMENT_ID_CARD_WIKI = 2;
	public static final int FRAGMENT_ID_CARD_DECK = 3;
	public static final int FRAGMENT_ID_CARD_IMAGE = 4;
	public static final int FRAGMENT_ID_CARD_DETAIL = 8;
	public static final int FRAGMENT_ID_USER_LOGIN = 9;
	
	
	public static final int MSG_DOWN_EVENT_TASK_LIST_CHANGED = 0x1000;
	public static final int MSG_DOWN_EVENT_STATUS_CHANGED = 0x1001;
	public static final int MSG_DOWN_EVENT_PROGRESS = 0x1002;
	
	
	/**
	 * preference name
	 */
	//for compatiablity
	public static final String PREF_FILE = "preferred-config";
	public static final String RESOURCE_PATH = "resource";
	
	public static final String PREF_FILE_COMMON = "pref_common";
	public static final String PREF_KEY_DATA_VERSION = "pref_data_ver";
	public static final String PREF_KEY_EXTRA_VERSION = "pref_extra_ver";
	public static final String PREF_KEY_LAST_DECK = "pref_last_deck";
	public static final String PREF_KEY_UPDATE_CHECK = "pref_last_update_check";
	
	public static final String PREF_FILE_DOWNLOAD_TASK = "pref_download_task";
	
	public static final String PREF_FILE_SERVER_LIST = "pref_server_list";
	
	public static final String PREF_KEY_USER_DEF_SERVER_SIZE = "pref_server_size";
	public static final String PREF_KEY_USER_NAME = "pref_user_name_";
	public static final String PREF_KEY_SERVER_NAME = "pref_server_name_";
	public static final String PREF_KEY_SERVER_ADDR = "pref_server_addr_";
	public static final String PREF_KEY_SERVER_PORT = "pref_server_port_";
	public static final String PREF_KEY_SERVER_INFO = "pref_server_info_";
	
	public static final String PREF_KEY_VERSION_CHECK = "pref_version_check";
	
	public static final long DAILY_MILLSECONDS = 24 * 3600 * 1000;
	
	
	
	public static final int ACTION_BAR_CHANGE_TYPE_PAGE_CHANGE = 0x1000;
	public static final int ACTION_BAR_CHANGE_TYPE_DATA_LOADING = 0x1001;
	
	public static final int ACTION_BAR_EVENT_TYPE_NEW = 0x2000;
	public static final int ACTION_BAR_EVENT_TYPE_SETTINGS = 0x2001;
	public static final int ACTION_BAR_EVENT_TYPE_SEARCH = 0x2002;
	public static final int ACTION_BAR_EVENT_TYPE_PLAY = 0x2003;
	public static final int ACTION_BAR_EVENT_TYPE_FILTER = 0x2004;
	public static final int ACTION_BAR_EVENT_TYPE_DONATE = 0x2005;
	public static final int ACTION_BAR_EVENT_TYPE_PERSONAL_CENTER = 0x2006;
	public static final int ACTION_BAR_EVENT_TYPE_RESET = 0x2006;
	public static final int ACTION_BAR_EVENT_TYPE_CARD_IAMGE_DL = 0x2007;
	
	public static final int REQUEST_TYPE_UPDATE_SERVER = 0x3000;
	public static final int REQUEST_TYPE_UPDATE_ROOM = 0x3001;
	public static final int REQUEST_TYPE_LOGIN = 0x3002;
	
	public static final int REQUEST_TYPE_DOWNLOAD_IMAGE = 0x3003;

	public static final int REQUEST_TYPE_LOAD_BITMAP = 0x3004;
	
	public static final int REQUEST_TYPE_CHANGE_IMAGE_LOAD_PRIORITY = 0x3005;
	
	public static final int REQUEST_TYPE_RESET_LOAD_QUEUE = 0x3006;
	
	public static final int REQUEST_TYPE_RESET_DOWNLOAD_QUEUE = 0x3007;
	
	public static final int REQUEST_TYPE_CLEAR_BITMAP_CACHE = 0x3008;
	
	public static final int REQUEST_TYPE_MYCARD_API_GET_CARDIMAGE_URL = 0x3009;
	public static final String REQUEST_RESULT_KEY_CARDIMAGE_URL = "request.result.cardimage.url";
	
	
	
	public static final int IMAGE_DL_EVENT_TYPE_DOWNLOAD_COMPLETE = 0x4000;
	
	
	public static final String BUNDLE_KEY_USER_NAME = "bundle.key.user.name";
	public static final String BUNDLE_KEY_USER_PW = "bundle.key.user.pw";
	
	
	
	public static final int IMAGE_TYPE_THUMNAIL = 0;
	public static final int IMAGE_TYPE_ORIGINAL = 1;
	
	public static final int BITMAP_LOAD_TYPE_PRELOAD = 0;
	public static final int BITMAP_LOAD_TYPE_LOAD = 1;
	
	public static final int MSG_ID_UPDATE_ROOM_LIST = 0;
	public static final int MSG_ID_UPDATE_SERVER = 1;
	public static final int MSG_ID_EXIT_CONFIRM_ALARM = 3;
	
	
	public static final String SETTINGS_ACTION_COMMON = "cn.garymb.ygomobile.prefs.PREFS_COMMON";
	public static final String SETTINGS_ACTION_GAME = "cn.garymb.ygomobile.prefs.PREFS_GAME";
	public static final String SETTINGS_ACTION_ABOUT = "cn.garymb.ygomobile.prefs.PREFS_ABOUT";
	
	public static final String SETTINGS_FARGMENT_COMMON = "cn.garymb.ygomobile.fragment.setting.CommonSettingsFragment";
	public static final String SETTINGS_FARGMENT_GAME = "cn.garymb.ygomobile.fragment.setting.GameSettingsFragment";
	public static final String SETTINGS_FARGMENT_ABOUT = "cn.garymb.ygomobile.fragment.setting.AboutSettingsFragment";
	public static final String SETTINGS_FARGMENT_GAME_LAB = "cn.garymb.ygomobile.fragment.setting.GameLabSettingsFragment";

	public static final String ACTION_VIEW_DOWNLOAD_STATUS = "action_view_download_status";

	public static final String ACTION_VIEW_UPDATE = "action_view_update";

	public static final String ACTION_NEW_CLIENT_VERSION = "action_new_client_version";

	public static final String DEFAULT_ENCODING = "utf-8";

}

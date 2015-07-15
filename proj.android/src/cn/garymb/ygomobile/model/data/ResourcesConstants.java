package cn.garymb.ygomobile.model.data;

/**
 * @author mabin
 *
 */
public interface ResourcesConstants {
	
	public static final String FORUM_URL = "https://forum.my-card.in/";
	
	public static final String IMAGE_URL = "http://images-en.my-card.in/lq/";
	
	public static final String WIKI_SEARCH_URL = "http://www.ourocg.cn/S.aspx?key=";
	
	public static final String SERVER_LIST_URL = "http://my-card.in/servers.json";
	public static final String ROOM_LIST_URL = "ws://my-card.in/rooms.json";
	public static final String FONTS_DOWNLOAD_URL = "https://github.com/garymabin/YGOMobile-misc/raw/master/WQYMicroHei.TTF";
	
	public static final String UPDATE_SERVER_URL = "http://23.252.108.13";
	
	public static final String VERSION_UPDATE_URL = "/ygomobile/version.json";
	
	public static final String VERSION_UPDATE_CACHE_DIR = "updates";
	
	public static final String CARD_IMAGES_NAME = "images";
	
	public static final String DONATE_URL_WAP = "http://shenghuo.alipay.com/send/payment/fill.htm?optEmail=garymabin@hotmail.com";
	
	public static final String DONATE_URL_MOBILE = "https://qr.alipay.com/apjod7orwpzd7jy734";
	
	public static final String MYCARD_API_BASE = "http://my-card.in";
	
	public static final String SERVER_URL = MYCARD_API_BASE + "/servers.json";
	
	public static final String CARDIMAGE_URL = MYCARD_API_BASE + "/cards/image.json";
	
	public static final String DEFAULT_MC_SERVER_NAME = "MyCard";
	
	public static final String DEFAULT_MC_SERVER_ADDR = "182.254.142.247";
	
	public static final int DEFAULT_MC_SERVER_PORT = 7911;
	
	public static final String JSON_KEY_ID = "id";
	public static final String JSON_KEY_NAME = "name";
	
	/**
	 * For version info
	 */
	public static final String JSON_KEY_VERSION = "version";
	public static final String JSON_KEY_VERSION_URL = "url";
	
	/**
	 * For server info.
	 */
	public static final String JSON_KEY_SERVER_IP_ADDR = "ip";
	public static final String JSON_KEY_SERVER_PORT = "port";
	public static final String JSON_KEY_SERVER_AUTH = "auth";
	public static final String JSON_KEY_SERVER_INDEX_URL = "index";
	public static final String JSON_KEY_SERVER_MAX_ROOMS = "max_rooms";
	public static final String JSON_KEY_SERVER_TYPE = "server_type";
	
	/**
	 * For room info
	 */
	public static final String JSON_KEY_ROOM_STATUS = "status";
	public static final String JSON_KEY_ROOM_SERVER_ID = "server_id";
	public static final String JSON_KEY_ROOM_MODE = "mode";
	public static final String JSON_KEY_ROOM_USERS = "users";
	
	//Optional
	public static final String JSON_KEY_ROOM_PRIVACY = "private";
	public static final String JSON_KEY_ROOM_RULE = "rule";
	public static final String JSON_KEY_ROOM_START_LP = "start_lp";
	public static final String JSON_KEY_ROOM_START_HAND = "start_hand";
	public static final String JSON_KEY_ROOM_DRAW_COUNT = "draw_count";
	public static final String JSON_KEY_ROOM_ENABLE_PRIORITY = "enable_priority";
	public static final String JSON_KEY_ROOM_NO_CHECK_DECK = "no_check_deck";
	public static final String JSON_KEY_ROOM_NO_SHUFFLE_DECK = "no_shuffle_deck";
	public static final String JSON_KEY_ROOM_DELETED = "_deleted";
	
	/**
	 * For user info
	 */
	public static final String JSON_KEY_USER_CERTIFIED = "certified";
	public static final String JSON_KEY_USER_PLAYER_ID = "player";
	
	/**
	 * For card image
	 */
	public static final String JSON_KEY_ZH_IMAGE_URL = "url";
	public static final String JSON_KEY_ZH_THUMBNAIL_URL = "thumbnail_url";
	public static final String JSON_KEY_EN_IMAGE_URL = "en";
	public static final String JSON_KEY_EN_LQ_IMAGE_URL = "en-lq";
	
	
	public static final int GAME_MODE_SINGLE = 0;
	public static final int GAME_MODE_MATCH = 1;
	public static final int GAME_MODE_TAG = 2;
	
	
	public static final String GAME_STATUS_START = "start";
	public static final String GAME_STATUS_WAIT = "wait";
	
	public static final int GAME_RULE_OCG_ONLY = 0;
	public static final int GAME_RULE_TCG_ONLY = 1;
	public static final int GAME_RULE_OCG_TCG = 2;
	
	
	public static final String MODE_OPTIONS = "mode.options";
	public static final String GAME_OPTIONS = "game.options";
	public static final String PRIVATE_OPTIONS = "private.options";
	
	public static final int DIALOG_MODE_CREATE_ROOM = 0;
	public static final int DIALOG_MODE_QUICK_JOIN = 1;
	public static final int DIALOG_MODE_JOIN_GAME = 2;
	public static final int DIALOG_MODE_DONATE = 3;
	public static final int DIALOG_MODE_FILTER_ATK = 4;
	public static final int DIALOG_MODE_FILTER_DEF = 5;
	public static final int DIALOG_MODE_FILTER_LEVEL = 6;
	public static final int DIALOG_MODE_FILTER_EFFECT = 7;
	public static final int DIALOG_MODE_ADD_NEW_SERVER = 8;
	public static final int DIALOG_MODE_EDIT_SERVER = 9;
	public static final int DIALOG_MODE_DIRECTORY_CHOOSE = 10;
	public static final int DIALOG_MODE_APP_UPDATE = 11;
	
	public static final String ROOM_INFO_NAME = "room.info.name";
	public static final String ROOM_INFO_RULE = "room.info.rule";
	public static final String ROOM_INFO_MODE = "room.info.mode";
	public static final String ROOM_INFO_LIFEPOINTS = "room.info.lp";
	public static final String ROOM_INFO_INITIALHAND = "room.info.inithand";
	public static final String ROOM_INFO_DRAWCARDS = "room.info.drawcards";

}

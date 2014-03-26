/*
 * Contants.java
 *
 *  Created on: 2014年3月2日
 *      Author: mabin
 */
package cn.garymb.ygomobile;

public final class Constants {
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

	public static final String PREF_FILE = "preferred-config";

	public static final String RESOURCE_PATH = "resource";
	public static final String OPENGL_PATH = "opengl";
	
	//Virual Help overlay handle ops
	
	public static final int MODE_CANCEL_CHAIN_OPTIONS = 0;
	public static final int MODE_REFRESH_OPTION = 1;
	public static final int MODE_IGNORE_CHAIN_OPTION = 2;
	public static final int MODE_REACT_CHAIN_OPTION = 3;

}

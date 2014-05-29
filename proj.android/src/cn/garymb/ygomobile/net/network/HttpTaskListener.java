package cn.garymb.ygomobile.net.network;

public interface HttpTaskListener
{
    public static final int HTTPTASK_EVENT_START = 100;
    public static final int HTTPTASK_EVENT_CANCEL = 110;
    public static final int HTTPTASK_EVENT_END = 120;
    public static final int HTTPTASK_EVENT_FAIL = 130;
    public static final int HTTPTASK_EVENT_DATARECIVE = 140;
    public static final int HTTPTASK_EVENT_REDIRECT = 150;
    
    public static final int ERROR = -1;
    /** Server or proxy hostname lookup failed */
    public static final int ERROR_LOOKUP = -2;
    /** Unsupported authentication scheme (ie, not basic or digest) */
    public static final int ERROR_UNSUPPORTED_AUTH_SCHEME = -3;
    /** User authentication failed on server */
    public static final int ERROR_AUTH = -4;
    /** User authentication failed on proxy */
    public static final int ERROR_PROXYAUTH = -5;
    /** Could not connect to server */
    public static final int ERROR_CONNECT = -6;
    /** Failed to write to or read from server */
    public static final int ERROR_IO = -7;
    /** Connection timed out */
    public static final int ERROR_TIMEOUT = -8;
    /** Too many redirects */
    public static final int ERROR_REDIRECT_LOOP = -9;
    /** Unsupported URI scheme (ie, not http, https, etc) */
    public static final int ERROR_UNSUPPORTED_SCHEME = -10;
    /** Failed to perform SSL handshake */
    public static final int ERROR_FAILED_SSL_HANDSHAKE = -11;
    /** Bad URL */
    public static final int ERROR_BAD_URL = -12;
    /** Generic file error for file:/// loads */
    public static final int FILE_ERROR = -13;
    /** File not found error for file:/// loads */
    public static final int FILE_NOT_FOUND_ERROR = -14;
    /** Too many requests queued */
    public static final int TOO_MANY_REQUESTS_ERROR = -15;
    /** Out of Memory*/
    public static final int ERROR_OUT_OF_MEMORY = -16;
    
	public void onHttpTaskEvent(int taskid, int type, HttpTaskEventArg arg );
}
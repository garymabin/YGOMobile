package cn.garymb.ygomobile.net.network;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.text.TextUtils;

/**
 * HTTP任务管理类，提供HTTP上传，下载功能 同时支持多个任务
 **/
public class HttpTaskMgr {
    // Use a singleton.
    private static final String DEFAULT_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.57 Safari/537.36";
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    public static final int HTTPTASK_INVALID_ID = -1;
    
    private static HttpTaskMgr mManager;
    
    private Context mContext;
    private int mTaskid = 0;
    private boolean mConnectionSetup = false;

    private WeakHashMap<LoadListener, Integer> mListenerMap = new WeakHashMap<LoadListener, Integer>();
    private List<SoftReference<ConnectionSetupNotify>> mConSetupListener = new ArrayList<SoftReference<ConnectionSetupNotify>>();

    public static synchronized HttpTaskMgr instance(Context context) {
        if (mManager == null && context != null) {
            mManager = new HttpTaskMgr(context);
        }
        return mManager;
    }

    /**
     * 功能：发送HTTP请求
     * 
     * @param url
     *            : 请求的地址
     * @param savePath
     *            : 服务器返回的内容保存的地址，如果是null，则是保存到内存
     * @param bGet
     *            : 为true时是GET请求，为false时是POST请求
     * @param postData
     *            : post请求时，postData存放POST的数据
     * @param listener
     *            : HTTP任务状态通知的接口
     * @return：请求成功时返回Task的id，失败时返回HTTPTASK_INVALID_ID
     **/
    public int sendRequest(final String url, final String savePath, boolean bGet, byte[] postData,
            HttpTaskListener listener, long startPos, HashMap<String, String> userHeaders) {
        synchronized (this) {
            if (sWorkThread == null) {
                sWorkThread = new HandlerThread("LoadListener WorkThread");
                sWorkThread.start();
                
                // Wait for thread start
                SystemClock.sleep(50);
            }
        }

        String method = null;
        LoadListener loader = null;

        int taskid = newTaskId();
        HashMap<String, String> headers = new HashMap<String, String>();
        populateStaticHeaders(headers);
        
        if (bGet) {
            method = new String("GET");
            appendHeader(headers, userHeaders);

            if (savePath == null) {
                loader = LoadListener.getLoadListener(mContext, url, false, listener, taskid,
                        sWorkThread.getLooper());
            } else {
                addContentRange(headers, startPos);
                loader = LoadFileListener.getFileLoadListener(mContext, url, savePath, false,
                        listener, taskid, sWorkThread.getLooper());
            }

        } else {
            appendHeader(headers, userHeaders);

            method = new String("POST");
            loader = LoadListener.getLoadListener(mContext, url, false, listener, taskid,
                    sWorkThread.getLooper());
        }

        int error = 0;
        boolean ret = false;

        try {
            ret = Network.getInstance(mContext).requestURL(method, headers, postData, loader);
        } catch (android.net.ParseException ex) {
            error = EventHandler.ERROR_BAD_URL;
        } catch (java.lang.RuntimeException ex) {
            ex.printStackTrace();
            error = EventHandler.ERROR_BAD_URL;
        }

        if (!ret || error != 0) {
            return HTTPTASK_INVALID_ID;
        }

        addTask(taskid, loader);
        return taskid;
    }

    /**
     * 添加用户指定的头字段
     * 
     * @param headers
     */
    private void appendHeader(HashMap<String, String> origin, HashMap<String, String> append) {
    	if (origin == null || append == null)
    		return;
    	
        for (String key : append.keySet()) {
        	origin.put(key, append.get(key));
        }
    }

    /**
     * 功能：取消一个HTTP请求
     * 
     * @param taskid
     *            : 从sendRequest获取的taskid
     **/
    synchronized public void cancel(int taskid) {
        Iterator<Entry<LoadListener, Integer>> i = mListenerMap.entrySet().iterator();
        while (i.hasNext()) {
            Entry<LoadListener, Integer> entry = i.next();
            LoadListener sender = entry.getKey();
            if (entry.getValue() == taskid) {
                sender.cancel();
                break;
            }
        }
    }

    /**
     * 功能：HTTP请求完成后，获取HTTP响应的数据
     * 
     * @param taskid
     *            : 从sendRequest获取的taskid
     * @return：服务器返回的数据
     **/
    synchronized public ByteArrayBuffer readResponseData(int taskid) {
        Iterator<Entry<LoadListener, Integer>> i = mListenerMap.entrySet().iterator();
        while (i.hasNext()) {
            Entry<LoadListener, Integer> entry = i.next();
            LoadListener sender = entry.getKey();
            if (entry.getValue() == taskid) {
                return sender.getDataBuffer();
            }
        }
        return null;
    }

    private void populateStaticHeaders(HashMap<String, String> headers) {
        // Accept header should already be there as they are built by WebCore,
        // but in the case they are missing, add some.
        String accept = headers.get("Accept");
        String ua = headers.get("User-Agent");
        String contentType = headers.get("Content-Type");
        
        if (TextUtils.isEmpty(accept))
            headers.put("Accept", DEFAULT_ACCEPT);
        if (TextUtils.isEmpty(ua))
        	headers.put("User-Agent", DEFAULT_USER_AGENT);
        if (TextUtils.isEmpty(contentType))
        	headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
    }

    private void addContentRange(HashMap<String, String> headers, long startPos) {
        long start = startPos;
        String range = "bytes=" + start + "-";
        headers.put("Range", range);
    }

    private HttpTaskMgr(Context context) {
        if (mContext == null) {
            mContext = context.getApplicationContext();
        }
    }

    synchronized public int newTaskId() {
        mTaskid++;
        if (mTaskid < 0) {
            mTaskid = 0;
        }

        return mTaskid;
    }

    synchronized private void addTask(int taskid, LoadListener sender) {
        mListenerMap.put(sender, taskid);
    }

    /**
     * 功能：添加网络连通通知,本函数非线程安全,只能在UI线程执行
     * 
     * @param listener
     *            : 监听者
     **/
    public void addConnectionListener(ConnectionSetupNotify listener) {
        mConSetupListener.add(new SoftReference<ConnectionSetupNotify>(listener));
    }

    /**
     * 功能：取消连接建立的事件监听
     **/
    public void removeConnectionListener(ConnectionSetupNotify listener) {
        for (Iterator<SoftReference<ConnectionSetupNotify>> it = mConSetupListener.iterator(); it
                .hasNext();) {
            SoftReference<ConnectionSetupNotify> refer = it.next();
            if (refer.get() == null || refer.get() == listener) {
                refer.get().onConnectionSetup();
                it.remove();
            }
        }
    }

    public boolean isConnectionSetup() {
        return mConnectionSetup;
    }

    public void setConnectionSetup() {
        mConnectionSetup = true;
        notifyConnectionSetup();
    }

    private void notifyConnectionSetup() {
        for (Iterator<SoftReference<ConnectionSetupNotify>> it = mConSetupListener.iterator(); it
                .hasNext();) {
            SoftReference<ConnectionSetupNotify> listener = it.next();
            if (listener.get() != null) {
                listener.get().onConnectionSetup();
            } else {
                it.remove();
            }
        }
    }

    private static HandlerThread sWorkThread = null;

}
/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.garymb.ygomobile.net.network;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.net.ParseException;
import android.net.http.SslCertificate;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

@SuppressWarnings("all")
class LoadListener extends Handler implements EventHandler {

    private static final String LOGTAG = "webkit";

    // Messages used internally to communicate state between the
    // Network thread and the WebCore thread.
    private static final int MSG_CONTENT_HEADERS = 100;
    private static final int MSG_CONTENT_DATA = 110;
    private static final int MSG_CONTENT_FINISHED = 120;
    private static final int MSG_CONTENT_ERROR = 130;
    private static final int MSG_LOCATION_CHANGED = 140;
    private static final int MSG_LOCATION_CHANGED_REQUEST = 150;
    private static final int MSG_STATUS = 160;
    private static final int MSG_SSL_CERTIFICATE = 170;
    private static final int MSG_SSL_ERROR = 180;

    // Standard HTTP status codes in a more representative format
    private static final int HTTP_OK = 200;
    private static final int HTTP_MOVED_PERMANENTLY = 301;
    private static final int HTTP_FOUND = 302;
    private static final int HTTP_SEE_OTHER = 303;
    private static final int HTTP_NOT_MODIFIED = 304;
    private static final int HTTP_TEMPORARY_REDIRECT = 307;
    private static final int HTTP_AUTH = 401;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_PROXY_AUTH = 407;    
    private static final int HTTP_MAX_RETRY_COUNT = 0;
    
    protected int mRetryCount = 0;

    private final ByteArrayBuffer  mDataBuffer;

    private String   mUrl;
    private WebAddress mUri;
    private String   mOriginalUrl;
    protected Context  mContext;
    private String   mMimeType;
    private String   mEncoding;
    private String   mTransferEncoding;
    protected int      mStatusCode;
    public long mContentLength; // Content length of the incoming data
    protected long mRangeStart = 0;
    protected long mRangeEnd = 0;
    protected boolean  mCancelled;  // The request has been cancelled.
    private int      mErrorID = OK;
    private String   mErrorDescription;
    private RequestHandle mRequestHandle;

    // Request data. It is only valid when we are doing a load from the
    // cache. It is needed if the cache returns a redirect
    protected String mMethod;
    protected Map<String, String> mRequestHeaders;
    protected byte[] mPostData;
    // Flag to indicate that this load is synchronous.
    private boolean mSynchronous;
    private Vector<Message> mMessageQueue;

    private Headers mHeaders;
    
    protected HttpTaskListener mTaskListener;
    protected int m_taskid = 0;

    // =========================================================================
    // Public functions
    // =========================================================================

    public static LoadListener getLoadListener(
            Context context, String url, boolean synchronous,
            HttpTaskListener listener, int taskid, Looper looper) {

        return new LoadListener(context, url, synchronous, listener, taskid, looper);
    }

    public static int getNativeLoaderCount() {
    	return 0;
    }

    LoadListener(Context context, String url, boolean synchronous, 
    		HttpTaskListener listener, int taskid, Looper looper) {
    	// added by qiaozhi 2012.6.29 LoadListener��һ��Handler�������߳��д���
    	super(looper);
    	
        mContext = context;
        mTaskListener = listener;
        m_taskid = taskid;
        setUrl(url);
        mSynchronous = synchronous;
        if (synchronous) {
            mMessageQueue = new Vector<Message>();
        }
        
        mDataBuffer = new ByteArrayBuffer( 1024 );
    }

    /**
     * We keep a count of refs to the nativeLoader so we do not create
     * so many LoadListeners that the GREFs blow up
     */
    private void clearNativeLoader() {
    }

    /*
     * This message handler is to facilitate communication between the network
     * thread and the browser thread.
     */
    public void handleMessage(Message msg) {
    	if( mCancelled )
    		return;
        switch (msg.what) {
            case MSG_CONTENT_HEADERS:
                /*
                 * This message is sent when the LoadListener has headers
                 * available. The headers are sent onto WebCore to see what we
                 * should do with them.
                 */
                handleHeaders((Headers) msg.obj);
                break;

            case MSG_CONTENT_DATA:
                /*
                 * This message is sent when the LoadListener has data available
                 * in it's data buffer. This data buffer could be filled from a
                 * file (this thread) or from http (Network thread).
                 */
            	handleData( msg );
                if ( !ignoreCallbacks() ) {
                    commitLoad();
                }
                break;

            case MSG_CONTENT_FINISHED:
                /*
                 * This message is sent when the LoadListener knows that the
                 * load is finished. This message is not sent in the case of an
                 * error.
                 *
                 */
                handleEndData();
                break;

            case MSG_CONTENT_ERROR:
                /*
                 * This message is sent when a load error has occured. The
                 * LoadListener will clean itself up.
                 */
                handleError(msg.arg1, (String) msg.obj);
                break;

            case MSG_LOCATION_CHANGED:
                /*
                 * This message is sent from LoadListener.endData to inform the
                 * browser activity that the location of the top level page
                 * changed.
                 */
                doRedirect();
                break;

            case MSG_LOCATION_CHANGED_REQUEST:
                /*
                 * This message is sent from endData on receipt of a 307
                 * Temporary Redirect in response to a POST -- the user must
                 * confirm whether to continue loading. If the user says Yes,
                 * we simply call MSG_LOCATION_CHANGED. If the user says No,
                 * we call MSG_CONTENT_FINISHED.
                 */
                break;

            case MSG_STATUS:
                /*
                 * This message is sent from the network thread when the http
                 * stack has received the status response from the server.
                 */
                HashMap status = (HashMap) msg.obj;
                handleStatus(((Integer) status.get("major")).intValue(),
                        ((Integer) status.get("minor")).intValue(),
                        ((Integer) status.get("code")).intValue(),
                        (String) status.get("reason"));
                break;

            case MSG_SSL_CERTIFICATE:
                /*
                 * This message is sent when the network thread receives a ssl
                 * certificate.
                 */
                handleCertificate((SslCertificate) msg.obj);
                break;

            case MSG_SSL_ERROR:
                /*
                 * This message is sent when the network thread encounters a
                 * ssl error.
                 */
                break;
        }
    }

     protected void handleData(Message msg) {
    	int len = msg.arg1;
    	int totalLen = msg.arg2;
		ByteArrayBuffer buffer = (ByteArrayBuffer) msg.obj;
		try {
			synchronized (mDataBuffer) {
				mDataBuffer.append(buffer.buffer(), 0, buffer.length());
			}
		} catch (OutOfMemoryError error) {
			cancel();

			HttpTaskEventArg arg = new HttpTaskEventArg();
			arg.mErrorId = HttpTaskListener.ERROR_OUT_OF_MEMORY;
			mTaskListener.onHttpTaskEvent(m_taskid,
					HttpTaskListener.HTTPTASK_EVENT_FAIL, arg);
			return;
		}
    	
		if (mTaskListener != null) {
			if ((mStatusCode >= 301 && mStatusCode <= 303) || mStatusCode == 307) {
				// �ض���
			} else {
				HttpTaskEventArg arg = new HttpTaskEventArg();
				arg.mlen = len;
				arg.mTotal = totalLen;
				arg.buffer = buffer.buffer();
				mTaskListener.onHttpTaskEvent(m_taskid, HttpTaskListener.HTTPTASK_EVENT_DATARECIVE, arg);
			}
		}
	}

    /* package */ boolean isSynchronous() {
        return mSynchronous;
    }

    /**
     * @return True iff the load has been cancelled
     */
    public boolean cancelled() {
        return mCancelled;
    }

    /**
     * Parse the headers sent from the server.
     * @param headers gives up the HeaderGroup
     * IMPORTANT: as this is called from network thread, can't call native
     * directly
     */
    public void headers(Headers headers) {
        sendMessageInternal(obtainMessage(MSG_CONTENT_HEADERS, headers));
    }

    // Does the header parsing work on the WebCore thread.
    protected void handleHeaders(Headers headers) {
        if (mCancelled) 
            return;
        mHeaders = headers;

        long contentLength = headers.getContentLength();
        if (contentLength != Headers.NO_CONTENT_LENGTH) {
            mContentLength = contentLength;
        } else {
            mContentLength = 0;
        }
        
		if (mStatusCode == 206) {
			getContentRangeParams(headers);
		}

        String contentType = headers.getContentType();
        if (contentType != null) {
            parseContentTypeHeader(contentType);

            // If we have one of "generic" MIME types, try to deduce
            // the right MIME type from the file extension (if any):
            if (mMimeType.equals("text/plain") ||
                    mMimeType.equals("application/octet-stream")) {

                // for attachment, use the filename in the Content-Disposition
                // to guess the mimetype
                String contentDisposition = headers.getContentDisposition();
                String url = null;
                if (contentDisposition != null) {
                    url = URLUtil.parseContentDisposition(contentDisposition);
                }
                if (url == null) {
                    url = mUrl;
                }
                String newMimeType = guessMimeTypeFromExtension(url);
                if (newMimeType != null) {
                    mMimeType = newMimeType;
                }
            } else if (mMimeType.equals("text/vnd.wap.wml")) {
                // As we don't support wml, render it as plain text
                //mMimeType = "text/plain";
                mMimeType = "text/vnd.wap.wml";
            } else {
                // It seems that xhtml+xml and vnd.wap.xhtml+xml mime
                // subtypes are used interchangeably. So treat them the same.
                if (mMimeType.equals("application/vnd.wap.xhtml+xml")) {
                    mMimeType = "application/xhtml+xml";
                }
            }
        } else {
            /* Often when servers respond with 304 Not Modified or a
               Redirect, then they don't specify a MIMEType. When this
               occurs, the function below is called.  In the case of
               304 Not Modified, the cached headers are used rather
               than the headers that are returned from the server. */
            guessMimeType();
        }

        // is it an authentication request?
        boolean mustAuthenticate = (mStatusCode == HTTP_AUTH ||
                mStatusCode == HTTP_PROXY_AUTH);
        // is it a proxy authentication request?
        boolean isProxyAuthRequest = (mStatusCode == HTTP_PROXY_AUTH);
        // is this authentication request due to a failed attempt to
        // authenticate ealier?
        //mAuthFailed = false;
        commitHeadersCheckRedirect();
        
		if (mTaskListener != null) {
			if ((mStatusCode >= 301 && mStatusCode <= 303) || mStatusCode == 307) {
				// 重定向
			} else {
				mTaskListener.onHttpTaskEvent(m_taskid, HttpTaskListener.HTTPTASK_EVENT_START, null);
			}
		}
    }

	private void getContentRangeParams(Headers headers) {
		String range = headers.getRaw("Content-Range");
		int space = range.indexOf(' ');
		int dash = range.indexOf('-');
		int sep = range.indexOf('/');
		if (space > 0 && dash > 0) {
			mRangeStart = getIntegerFromString(range, space + 1, dash);
		}
		if (dash > 0 && sep > 0) {
			mRangeEnd = getIntegerFromString(range, dash + 1, sep);
		}
		if (sep > 0 && sep < range.length()) {
			mContentLength = getIntegerFromString(range, sep + 1, range.length());
		}
	}
	
	private int getIntegerFromString(String range, int start, int end) {
		int resultInt = 0;
		try {
			String target = range.substring(start, end);
			resultInt = Integer.parseInt(target);
		} catch (Exception e) {
			resultInt = 0;
		}
		return resultInt;
	}

    /**
     * @return True iff this loader is in the proxy-authenticate state.
     */
    boolean proxyAuthenticate() {
        return false;
    }

    /**
     * Report the status of the response.
     * TODO: Comments about each parameter.
     * IMPORTANT: as this is called from network thread, can't call native
     * directly
     */
    public void status(int majorVersion, int minorVersion,
            int code, /* Status-Code value */ String reasonPhrase) {
        HashMap status = new HashMap();
        status.put("major", majorVersion);
        status.put("minor", minorVersion);
        status.put("code", code);
        status.put("reason", reasonPhrase);
        synchronized( mDataBuffer )
        {
        	if( mDataBuffer != null )
        		mDataBuffer.clear();
        }
        mMimeType = "";
        mEncoding = "";
        mTransferEncoding = "";
        sendMessageInternal(obtainMessage(MSG_STATUS, status));
    }

    // Handle the status callback on the WebCore thread.
    protected void handleStatus(int major, int minor, int code, String reason) {
        if (mCancelled) 
            return;
        
        mStatusCode = code;
    }

    /**
     * Implementation of certificate handler for EventHandler.
     * Called every time a resource is loaded via a secure
     * connection. In this context, can be called multiple
     * times if we have redirects
     * @param certificate The SSL certifcate
     * IMPORTANT: as this is called from network thread, can't call native
     * directly
     */
    public void certificate(SslCertificate certificate) {
        sendMessageInternal(obtainMessage(MSG_SSL_CERTIFICATE, certificate));
    }

    // Handle the certificate on the WebCore thread.
    private void handleCertificate(SslCertificate certificate) {
        // if this is the top-most main-frame page loader
    }

    /**
     * Implementation of error handler for EventHandler.
     * Subclasses should call this method to have error fields set.
     * @param id The error id described by EventHandler.
     * @param description A string description of the error.
     * IMPORTANT: as this is called from network thread, can't call native
     * directly
     */
    public void error(int id, String description) {
        sendMessageInternal(obtainMessage(MSG_CONTENT_ERROR, id, 0, description));
    }

    // Handle the error on the WebCore thread.
    private void handleError(int id, String description) {
        mErrorID = id;
        mErrorDescription = description;
        detachRequestHandle();
        notifyError();
        tearDown();
        
        HttpTaskEventArg arg = new HttpTaskEventArg();
        arg.mErrorId = id;
        mTaskListener.onHttpTaskEvent( m_taskid, HttpTaskListener.HTTPTASK_EVENT_FAIL, arg );
    }

    /**
     * Add data to the internal collection of data. This function is used by
     * the data: scheme, about: scheme and http/https schemes.
     * @param data A byte array containing the content.
     * @param length The length of data.
     * IMPORTANT: as this is called from network thread, can't call native
     * directly
     * XXX: Unlike the other network thread methods, this method can do the
     * work of decoding the data and appending it to the data builder because
     * mDataBuilder is a thread-safe structure.
     */
    public void data(byte[] data, int length) {

    	// Send a message whenever data comes in after a write to WebCore
        Message msg = obtainMessage(MSG_CONTENT_DATA);
        msg.arg1 = length;
        msg.arg2 = (int) mContentLength;
        ByteArrayBuffer recivBuffer = new ByteArrayBuffer( length );
		recivBuffer.append(data, 0, length );
		msg.obj = recivBuffer;
        sendMessageInternal( msg );
    }
    
	private void retry() {
		mRetryCount++;

		Network network = Network.getInstance(getContext());
		if (!network.requestURL(mMethod, mRequestHeaders, mPostData, LoadListener.this)) {
			HttpTaskEventArg arg = new HttpTaskEventArg();
			arg.mErrorId = HttpTaskListener.ERROR_BAD_URL;
			mTaskListener.onHttpTaskEvent(m_taskid, HttpTaskListener.HTTPTASK_EVENT_FAIL, arg);
			return;
		}
	}

    /**
     * Event handler's endData call. Send a message to the handler notifying
     * them that the data has finished.
     * IMPORTANT: as this is called from network thread, can't call native
     * directly
     */
    public void endData() {
        sendMessageInternal(obtainMessage(MSG_CONTENT_FINISHED));
    }

	// Handle the end of data.
	protected void handleEndData() {
		if (mCancelled)
			return;

		if (mStatusCode >= 400 && mStatusCode < 500 && mRetryCount < LoadListener.HTTP_MAX_RETRY_COUNT) {
			retry();
			return;
		}

		// modified by qiaozhi 2012.6.14 处理wml数据重试必须添加重试的次数，防止死循环的可能
		// 处理CMWAP的收费提示页面
		// if( mMimeType.equals("text/vnd.wap.wml") ){
		if (mMimeType.equals("text/vnd.wap.wml") && mRetryCount < LoadListener.HTTP_MAX_RETRY_COUNT) {
			// 目前不会处理到wml格式的数据,碰到wml格式时，认为是CMWAP收费提示页面
			retry();
			return;
		}
		// --------------- 2012.6.14 ----------------------

		switch (mStatusCode) {
		// added by zhangxm,
		case HTTP_OK:
			if (Looper.getMainLooper().getThread().equals(Thread.currentThread())) {
				// 只在UI线程执行这个操作
				HttpTaskMgr.instance(getContext()).setConnectionSetup();
			}
			mTaskListener.onHttpTaskEvent(m_taskid, HttpTaskListener.HTTPTASK_EVENT_END, null);
			break;
		case HTTP_MOVED_PERMANENTLY:
			// 301 - permanent redirect
			// mPermanent = true;
		case HTTP_FOUND:
		case HTTP_SEE_OTHER:
		case HTTP_TEMPORARY_REDIRECT:
			// 301, 302, 303, and 307 - redirect
			if (mStatusCode == HTTP_TEMPORARY_REDIRECT) {
				if (mRequestHandle != null && mRequestHandle.getMethod().equals("POST")) {
					sendMessageInternal(obtainMessage(MSG_LOCATION_CHANGED_REQUEST));
				} else if (mMethod != null && mMethod.equals("POST")) {
					sendMessageInternal(obtainMessage(MSG_LOCATION_CHANGED_REQUEST));
				} else {
					sendMessageInternal(obtainMessage(MSG_LOCATION_CHANGED));
				}
			} else {
				sendMessageInternal(obtainMessage(MSG_LOCATION_CHANGED));
			}
			return;
		case HTTP_AUTH:
		case HTTP_PROXY_AUTH:
			// According to rfc2616, the response for HTTP_AUTH must include
			// WWW-Authenticate header field and the response for
			// HTTP_PROXY_AUTH must include Proxy-Authenticate header field.
			HttpTaskEventArg arg = new HttpTaskEventArg();
			arg.mErrorId = HttpTaskListener.ERROR_AUTH;
			mTaskListener.onHttpTaskEvent(m_taskid,
					HttpTaskListener.HTTPTASK_EVENT_FAIL, arg);
			break; // use default
		case 206:
			if (!handlePartialContent()) {
				HttpTaskEventArg arg2 = new HttpTaskEventArg();
				arg2.mErrorId = HttpTaskListener.ERROR_BAD_URL;
				mTaskListener.onHttpTaskEvent(m_taskid, HttpTaskListener.HTTPTASK_EVENT_FAIL, arg2);
				break;
			} else {
				return;
			}
		default:
			HttpTaskEventArg arg1 = new HttpTaskEventArg();
			arg1.mErrorId = HttpTaskListener.ERROR;
			mTaskListener.onHttpTaskEvent(m_taskid, HttpTaskListener.HTTPTASK_EVENT_FAIL, arg1);
			break;
		}
		
		detachRequestHandle();
		tearDown();
	}
    
	protected boolean handlePartialContent() {
		return false;
	}

 
    /**
     * This is called when a request can be satisfied by the cache, however,
     * the cache result could be a redirect. In this case we need to issue
     * the network request.
     * @param method
     * @param headers
     * @param postData
     */
    void setRequestData(String method, Map<String, String> headers, 
            byte[] postData) {
        mMethod = method;
        mRequestHeaders = headers;
        mPostData = postData;
    }

    /**
     * @return The current URL associated with this load.
     */
    String url() {
        return mUrl;
    }

    /**
     * @return The current WebAddress associated with this load.
     */
    WebAddress getWebAddress() {
        return mUri;
    }

    /**
     * @return URL hostname (current URL).
     */
    String host() {
        if (mUri != null) {
            return mUri.mHost;
        }

        return null;
    }

    /**
     * @return The original URL associated with this load.
     */
    String originalUrl() {
        if (mOriginalUrl != null) {
            return mOriginalUrl;
        } else {
            return mUrl;
        }
    }

    void attachRequestHandle(RequestHandle requestHandle) {
        mRequestHandle = requestHandle;
    }

    void detachRequestHandle() {
        mRequestHandle = null;
    }

    /*
     * Reset the cancel flag. This is used when we are resuming a stopped
     * download. To suspend a download, we cancel it. It can also be cancelled
     * when it has run out of disk space. In this situation, the download
     * can be resumed.
     */
    void resetCancel() {
        mCancelled = false;
    }

    String mimeType() {
        return mMimeType;
    }

    String transferEncoding() {
        return mTransferEncoding;
    }

    /*
     * Return the size of the content being downloaded. This represents the
     * full content size, even under the situation where the download has been
     * resumed after interruption.
     *
     * @ return full content size
     */
    long contentLength() {
        return mContentLength;
    }

    // Commit the headers if the status code is not a redirect.
    private void commitHeadersCheckRedirect() {
        if (mCancelled) 
            return;

        // do not call webcore if it is redirect. According to the code in
        // InspectorController::willSendRequest(), the response is only updated
        // when it is not redirect.
        if ((mStatusCode >= 301 && mStatusCode <= 303) || mStatusCode == 307) {
            return;
        }

        commitHeaders();
    }

    // This commits the headers without checking the response status code.
    private void commitHeaders() {
    }

    /**
     * Commit the load.  It should be ok to call repeatedly but only before
     * tearDown is called.
     */
    private void commitLoad() {
        if (mCancelled) 
            return;
    }

    /**
     * Tear down the load. Subclasses should clean up any mess because of
     * cancellation or errors during the load.
     */
    void tearDown() {
    }

    /**
     * Helper for getting the error ID.
     * @return errorID.
     */
    private int getErrorID() {
        return mErrorID;
    }

    /**
     * Return the error description.
     * @return errorDescription.
     */
    private String getErrorDescription() {
        return mErrorDescription;
    }

    /**
     * Notify the loader we encountered an error.
     */
    void notifyError() {
    }

    /**
     * Cancel a request.
     * FIXME: This will only work if the request has yet to be handled. This
     * is in no way guarenteed if requests are served in a separate thread.
     * It also causes major problems if cancel is called during an
     * EventHandler's method call.
     */
    public void cancel() {
        if (mRequestHandle != null) {
            mRequestHandle.cancel();
            mRequestHandle = null;
        }

        // mCacheResult = null;
        mCancelled = true;
        mTaskListener.onHttpTaskEvent( m_taskid, HttpTaskListener.HTTPTASK_EVENT_CANCEL, null);

        clearNativeLoader();
    }

    // This count is transferred from RequestHandle to LoadListener when
    // loading from the cache so that we can detect redirect loops that switch
    // between the network and the cache.
    private int mCacheRedirectCount;

    /*
     * Perform the actual redirection. This involves setting up the new URL,
     * informing WebCore and then telling the Network to start loading again.
     */
    private void doRedirect() {
        // as cancel() can cancel the load before doRedirect() is
        // called through handleMessage, needs to check to see if we
        // are canceled before proceed
        if (mCancelled) {
            return;
        }
        
        String redirectTo = mHeaders.getLocation();
        if( redirectTo.length() > 5 && redirectTo.substring(0, 5).compareToIgnoreCase("https") == 0 ){
        	HttpTaskEventArg arg = new HttpTaskEventArg();
            arg.mErrorId = HttpTaskListener.ERROR_UNSUPPORTED_SCHEME;
            mTaskListener.onHttpTaskEvent( m_taskid, HttpTaskListener.HTTPTASK_EVENT_FAIL, arg );
            return;
        }
        	

        // Do the same check for a redirect loop that
        // RequestHandle.setupRedirect does.
        if (mCacheRedirectCount >= RequestHandle.MAX_REDIRECT_COUNT) {
            return;
        }

        if (redirectTo != null) {
            // nativeRedirectedToUrl() may call cancel(), e.g. when redirect
            // from a https site to a http site, check mCancelled again
            if (mCancelled) {
                return;
            }
            if (redirectTo == null) {
                //Log.d(LOGTAG, "Redirection failed for "
                //        + mHeaders.getLocation());
                cancel();
                return;
            } else if (!URLUtil.isNetworkUrl(redirectTo)) {
                clearNativeLoader();
                return;
            }

            if (mOriginalUrl == null) {
                mOriginalUrl = mUrl;
            }

            // This will strip the anchor
            setUrl(redirectTo);

            // Redirect may be in the cache
            if (mRequestHeaders == null) {
                mRequestHeaders = new HashMap<String, String>();
            }
            // mRequestHandle can be null when the request was satisfied
            // by the cache, and the cache returned a redirect
            if (mRequestHandle != null) {
            	try {
                    mRequestHandle.setupRedirect(mUrl, mStatusCode,
                            mRequestHeaders);
            	} catch (RuntimeException e) {
            		e.printStackTrace();
					HttpTaskEventArg arg = new HttpTaskEventArg();
					arg.mErrorId = HttpTaskListener.ERROR_BAD_URL;
					mTaskListener.onHttpTaskEvent(m_taskid, HttpTaskListener.HTTPTASK_EVENT_FAIL, arg);
            	}
            } else {
                // If the original request came from the cache, there is no
                // RequestHandle, we have to create a new one through
                // Network.requestURL.
                Network network = Network.getInstance(getContext());
                if (!network.requestURL(mMethod, mRequestHeaders, mPostData, this)) {
                	HttpTaskEventArg arg = new HttpTaskEventArg();
                    arg.mErrorId = HttpTaskListener.ERROR_BAD_URL;
                    mTaskListener.onHttpTaskEvent( m_taskid, HttpTaskListener.HTTPTASK_EVENT_FAIL, arg );
                    return;
                }
            }
            
            HttpTaskEventArg arg = new HttpTaskEventArg();
            arg.buffer = mUrl.getBytes();
			mTaskListener.onHttpTaskEvent(m_taskid, HttpTaskListener.HTTPTASK_EVENT_REDIRECT, arg);
       	} else {
            commitHeaders();
            commitLoad();
            tearDown();
        }
    }

    /**
     * Parses the content-type header.
     * The first part only allows '-' if it follows x or X.
     */
    private static final Pattern CONTENT_TYPE_PATTERN =
            Pattern.compile("^((?:[xX]-)?[a-zA-Z\\*]+/[\\w\\+\\*-]+[\\.[\\w\\+-]+]*)$");

    /* package */ void parseContentTypeHeader(String contentType) {

        if (contentType != null) {
            int i = contentType.indexOf(';');
            if (i >= 0) {
                mMimeType = contentType.substring(0, i);

                int j = contentType.indexOf('=', i);
                if (j > 0) {
                    i = contentType.indexOf(';', j);
                    if (i < j) {
                        i = contentType.length();
                    }
                    mEncoding = contentType.substring(j + 1, i);
                } else {
                    mEncoding = contentType.substring(i + 1);
                }
                // Trim excess whitespace.
                mEncoding = mEncoding.trim().toLowerCase(Locale.getDefault());

                if (i < contentType.length() - 1) {
                    // for data: uri the mimeType and encoding have
                    // the form image/jpeg;base64 or text/plain;charset=utf-8
                    // or text/html;charset=utf-8;base64
                    mTransferEncoding =
                            contentType.substring(i + 1).trim().toLowerCase(Locale.getDefault());
                }
            } else {
                mMimeType = contentType;
            }

            // Trim leading and trailing whitespace
            mMimeType = mMimeType.trim();

            try {
                Matcher m = CONTENT_TYPE_PATTERN.matcher(mMimeType);
                if (m.find()) {
                    mMimeType = m.group(1);
                } else {
                    guessMimeType();
                }
            } catch (IllegalStateException ex) {
                guessMimeType();
            }
        }
        // Ensure mMimeType is lower case.
        mMimeType = mMimeType.toLowerCase(Locale.getDefault());
    }

    /**
     * If the content is a redirect or not modified we should not send
     * any data into WebCore as that will cause it create a document with
     * the data, then when we try to provide the real content, it will assert.
     *
     * @return True iff the callback should be ignored.
     */
    private boolean ignoreCallbacks() {
        return (mCancelled || /*mAuthHeader != null ||*/
                // Allow 305 (Use Proxy) to call through.
                (mStatusCode > 300 && mStatusCode < 400 && mStatusCode != 305));
    }

    /**
     * Sets the current URL associated with this load.
     */
    void setUrl(String url) {
        if (url != null) {
            mUri = null;
            if (URLUtil.isNetworkUrl(url)) {
                mUrl = URLUtil.stripAnchor(url);
                try {
                    mUri = new WebAddress(mUrl);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                mUrl = url;
            }
        }
    }

    /**
     * Guesses MIME type if one was not specified. Defaults to 'text/html'. In
     * addition, tries to guess the MIME type based on the extension.
     *
     */
    private void guessMimeType() {
        // Data urls must have a valid mime type or a blank string for the mime
        // type (implying text/plain).
        if (URLUtil.isDataUrl(mUrl) && mMimeType.length() != 0) {
            cancel();
           // final String text = mContext.getString(R.string.httpErrorBadUrl);
           // handleError(EventHandler.ERROR_BAD_URL, text);
        } else {
            // Note: This is ok because this is used only for the main content
            // of frames. If no content-type was specified, it is fine to
            // default to text/html.
            mMimeType = "text/html";
            String newMimeType = guessMimeTypeFromExtension(mUrl);
            if (newMimeType != null) {
                mMimeType = newMimeType;
            }
        }
    }

    /**
     * guess MIME type based on the file extension.
     */
    private String guessMimeTypeFromExtension(String url) {
    	return null;
    }

    /**
     * Either send a message to ourselves or queue the message if this is a
     * synchronous load.
     */
    private void sendMessageInternal(Message msg) {
        if (mSynchronous) {
            mMessageQueue.add(msg);
        } else {
            sendMessage(msg);
        }
    }

    /**
     * Cycle through our messages for synchronous loads.
     */
    /* package */ void loadSynchronousMessages() {
        // Note: this can be called twice if it is a synchronous network load,
        // and there is a cache, but it needs to go to network to validate. If 
        // validation succeed, the CacheLoader is used so this is first called 
        // from http thread. Then it is called again from WebViewCore thread 
        // after the load is completed. So make sure the queue is cleared but
        // don't set it to null.
//        for (int size = mMessageQueue.size(); size > 0; size--) {
    	while (mMessageQueue.size() > 0) {
    		Message msg = mMessageQueue.remove(0);
            handleMessage(msg);
        }
    }
    
    Context getContext() {
        return mContext;
    }
    
    ByteArrayBuffer getDataBuffer( )
    {
    	synchronized( mDataBuffer )
    	{
    		return mDataBuffer;
    	}
    }
}

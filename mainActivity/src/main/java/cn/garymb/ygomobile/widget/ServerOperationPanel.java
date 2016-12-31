package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class ServerOperationPanel extends LinearLayout {
	
	public interface ServerOperationListener {
		void onOperation(int operationId, int position);
	}
	
	public static final int SERVER_OPERATION_INVALID = -1;
	public static final int SERVER_OPERATION_CONNECT = 0;
	public static final int SERVER_OPERATION_EDIT = 1;
	public static final int SERVER_OPERATION_DELETE = 2;

	private View mConnectPanel;
	private View mEditPanel;
	private View mDeletePanel;
	
	private static Rect sOutRect = new Rect();
	private static int[] sLocation = new int[2];
	
	private ServerOperationListener mListener;
	
	private int mGroupPosition;
	private View mOperationView;
	private int mOperationIndex;

	public ServerOperationPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ServerOperationPanel(Context context) {
		super(context);
	}
	
	public void setServerOperationListener(ServerOperationListener listener) {
		mListener = listener;
	}
	
	public void setGroupPosition(int position) {
		mGroupPosition = position;
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mConnectPanel = findViewById(R.id.connect_panel);
		mEditPanel = findViewById(R.id.edit_panel);
		mDeletePanel = findViewById(R.id.delete_panel);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean handled = false;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			int actionX = (int) event.getRawX();
			int actionY = (int) event.getRawY();
			mOperationIndex = caculateClickItem(actionX, actionY);
			if (mOperationIndex == SERVER_OPERATION_CONNECT) {
				mOperationView = mConnectPanel;
			} else if (mOperationIndex == SERVER_OPERATION_EDIT) {
				mOperationView = mEditPanel;
			} else if (mOperationIndex == SERVER_OPERATION_DELETE) {
				mOperationView = mDeletePanel;
			}
			if (mOperationView != null) {
				mOperationView.setPressed(true);
			}
			handled = true;
			break;
		}
		case MotionEvent.ACTION_UP:
			if (mOperationView != null) {
				mOperationView.performClick();
				mOperationView.setPressed(false);
				mListener.onOperation(mOperationIndex, mGroupPosition);
				handled = true;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			if (mOperationView != null) {
				mOperationView.setPressed(false);
			}
			mOperationIndex = SERVER_OPERATION_INVALID;
			mOperationView = null;
			handled = false;
			break;
		default:
			break;
		}
		if (handled) {
			return true;
		} else {
			return super.onTouchEvent(event);
		}
	}

	private int caculateClickItem(int actionX, int actionY) {
		if (mConnectPanel.getVisibility() == View.VISIBLE && inViewBounds(mConnectPanel, actionX, actionY)) {
			return SERVER_OPERATION_CONNECT;
		}
		if (mEditPanel.getVisibility() == View.VISIBLE && inViewBounds(mEditPanel, actionX, actionY)) {
			return SERVER_OPERATION_EDIT;
		}
		if (mDeletePanel.getVisibility() == View.VISIBLE && inViewBounds(mDeletePanel, actionX, actionY)) {
			return SERVER_OPERATION_DELETE;
		}
		return SERVER_OPERATION_INVALID;
	}
	
	private boolean inViewBounds(View view, int x, int y){
        view.getDrawingRect(sOutRect);
        view.getLocationOnScreen(sLocation);
        sOutRect.offset(sLocation[0], sLocation[1]);
        return sOutRect.contains(x, y);
    }

}

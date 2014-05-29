/*
 Copyright 2011 jawsware international

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package cn.garymb.ygomobile.widget.overlay;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.Constants;

import com.jawsware.core.share.OverlayView;

public class DuelOverlayView extends OverlayView {

	/**
	 * @author mabin
	 * 
	 */
	public interface OnDuelOptionsSelectListener {
		void onDuelOptionsSelected(int mode, boolean action);
	}

	private TextView mInfo;
	private ViewGroup mDetailContent;

	private Point mInitialTouchPoint;
	private OnDuelOptionsSelectListener mListener;

	private int mMoveThresold;
	private boolean mIsSwitchMode = false;

	private boolean mIsIgnoreChainHold = false;
	private boolean mIsReactChainHold = false;

	private int mMode;

	private static Rect tempRect = new Rect();

	public DuelOverlayView(Context context) {
		super(context, R.layout.overlay);
		mMoveThresold = getResources().getDimensionPixelSize(
				R.dimen.options_switch_move_threshold);
	}

	@Override
	protected void onInflateView() {
		mInitialTouchPoint = new Point();
		mInfo = (TextView) this.findViewById(R.id.textview_info);
		mDetailContent = (ViewGroup) findViewById(R.id.overlay_detail);
		mMode = Constants.MODE_CANCEL_CHAIN_OPTIONS;
	}

	public void setDuelOpsListener(OnDuelOptionsSelectListener listener) {
		mListener = listener;
	}

	@Override
	protected void onTouchEvent_Up(MotionEvent event) {
		mInitialTouchPoint.x = 0;
		mInitialTouchPoint.y = 0;
		mDetailContent.setVisibility(View.GONE);
		handlePressEffect(false);
		if (mIsSwitchMode) {
			int index = caculateCurrentSelection((int) event.getX(),
					(int) event.getY());
			Log.d("test", index + "");
			if (index != -1) {
				mInfo.setText(getResources().getStringArray(
						R.array.duel_options)[index]);
				mMode = index;
			}
			mIsSwitchMode = false;
		} else {
			if (mMode == Constants.MODE_IGNORE_CHAIN_OPTION
					|| mMode == Constants.MODE_REACT_CHAIN_OPTION) {
				if (mIsIgnoreChainHold) {
					mListener.onDuelOptionsSelected(mMode, false);
					mIsIgnoreChainHold = false;
				} else if (mIsReactChainHold) {
					mListener.onDuelOptionsSelected(mMode, false);
					mIsReactChainHold = false;
				}
				return;
			} else {
				mListener.onDuelOptionsSelected(mMode, false);
			}
		}
	}

	/**
	 * 
	 * @return
	 **/
	protected void handlePressEffect(boolean isPressed) {
		setPressed(isPressed);
		if (!isPressed) {
			mInfo.setTextColor(getResources().getColor(android.R.color.white));
		} else {
			mInfo.setTextColor(getResources().getColor(
					R.color.navigator_dir_text_color_selected));
		}
	}

	private int caculateCurrentSelection(int x, int y) {
		mDetailContent.getHitRect(tempRect);
		if (tempRect.contains(x, y)) {
			int relativeX = x > tempRect.centerX() ? tempRect.right - x : x
					- tempRect.left;
			int relativeY = y > tempRect.centerY() ? tempRect.bottom - y : y
					- tempRect.top;
			if (y < tempRect.centerY() && relativeX > relativeY) {
				return Constants.MODE_CANCEL_CHAIN_OPTIONS;
			} else if (y > tempRect.centerY() && relativeX > relativeY) {
				return Constants.MODE_REFRESH_OPTION;
			} else if (x < tempRect.centerX() && relativeX < relativeY) {
				return Constants.MODE_IGNORE_CHAIN_OPTION;
			} else if (x > tempRect.centerX() && relativeX < relativeY) {
				return Constants.MODE_REACT_CHAIN_OPTION;
			}
		}
		return -1;
	}

	@Override
	protected void onTouchEvent_Move(MotionEvent event) {
		if ((Math.abs((int) event.getX() - mInitialTouchPoint.x) > mMoveThresold || Math
				.abs((int) event.getY() - mInitialTouchPoint.y) > mMoveThresold)
				&& !mIsSwitchMode) {
			mDetailContent.setVisibility(View.VISIBLE);
			mIsSwitchMode = true;
			if (mMode == Constants.MODE_IGNORE_CHAIN_OPTION
					|| mMode == Constants.MODE_REACT_CHAIN_OPTION) {
				if (mIsIgnoreChainHold) {
					mListener.onDuelOptionsSelected(mMode, false);
					mIsIgnoreChainHold = false;
				} else if (mIsReactChainHold) {
					mListener.onDuelOptionsSelected(mMode, false);
					mIsReactChainHold = false;
				}
			}
		}
	}

	@Override
	protected void onTouchEvent_Press(MotionEvent event) {
		mInitialTouchPoint.x = (int) event.getX();
		mInitialTouchPoint.y = (int) event.getY();
		handlePressEffect(true);
	}

	@Override
	public boolean onTouchEvent_LongPress() {
		if (!mIsSwitchMode) {
			if (mMode == Constants.MODE_REACT_CHAIN_OPTION
					&& !mIsReactChainHold) {
				mIsReactChainHold = true;
				mListener.onDuelOptionsSelected(mMode, true);
				return true;
			} else if (mMode == Constants.MODE_IGNORE_CHAIN_OPTION
					&& !mIsReactChainHold) {
				mIsIgnoreChainHold = true;
				mListener.onDuelOptionsSelected(mMode, true);
				return true;
			}
		}
		return false;
	}

}

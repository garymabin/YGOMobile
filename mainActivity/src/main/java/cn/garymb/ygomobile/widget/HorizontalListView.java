/*
 * HorizontalListView.java v1.5.2
 * 
 * Copyright (c) 2011
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package cn.garymb.ygomobile.widget;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;


import java.util.LinkedList;
import java.util.Queue;

/**
 * Base on dinocore1 source code.
 * https://github.com/dinocore1/DevsmartLib-Android/tree/master/demo/src/com/devsmart/demo <br>
 * In my practice, i get into trouble on dynamic loading, select to special item ,scrolling listener and so on. <br>
 * So i decide to solve the problems.
 * @author bin
 */
public class HorizontalListView extends AdapterView<ListAdapter> {

    /**
     * Regular layout - usually an unsolicited layout from the view system
     */
    static final int LAYOUT_NORMAL = 0x00;

    /**
     * Make a mSelectedItem appear in a specific location and build the rest of
     * the views from there. The top is specified by mSpecificTop.
     */
    static final int LAYOUT_SPECIFIC = 0x04;

    static final int LAYOUT_FREEZE = 0x08;

    /**
     * Controls how the next layout will happen
     */
    int mLayoutMode = LAYOUT_NORMAL;


    public boolean mAlwaysOverrideTouch = true;

    protected ListAdapter mAdapter;
    protected Scroller mScroller;
    private GestureDetector mGesture;

    private int mLeftViewIndex = -1;
    private int mRightViewIndex = 0;

    private int mMaxX = Integer.MAX_VALUE;
    private int mMinX = Integer.MIN_VALUE;
    protected int mCurrentX;
    protected int mNextX;
    private int mDisplayOffset = 0;

    private Queue<View> mRemovedViewQueue = new LinkedList<View>();

    private OnItemSelectedListener mOnItemSelected;
    private OnItemClickListener mOnItemClicked;
    private OnItemLongClickListener mOnItemLongClicked;
    private OnScrollListener mOnScrolled;

    private boolean mDataChanged = false;

    private int mFirstPosition = 0;

    public HorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private synchronized void initView() {
        mLeftViewIndex = -1;
        mRightViewIndex = 0;
        mDisplayOffset = 0;
        mCurrentX = 0;
        mNextX = 0;
        mFirstPosition = 0;
        mSpecificPosition = 0;
        mSpecificLeft = 0;
        mMaxX = Integer.MAX_VALUE;
        mMinX = Integer.MIN_VALUE;
        mScroller = new Scroller(getContext());
        if (!isInEditMode()) {
            mGesture = new GestureDetector(getContext(), mOnGesture);
        }
    }

    private synchronized void initViewForSpecific() {
        mLeftViewIndex = mSpecificPosition - 1;
        mRightViewIndex = mSpecificPosition + 1;
        mFirstPosition = mSpecificPosition;
        mDisplayOffset = 0;
        mCurrentX = 0;
        mNextX = 0;
        mMaxX = Integer.MAX_VALUE;
        mMinX = Integer.MIN_VALUE;
        if (!isInEditMode()) {
            mGesture = new GestureDetector(getContext(), mOnGesture);
        }
    }

    @Override
    public void setOnItemSelectedListener(
            OnItemSelectedListener listener) {
        mOnItemSelected = listener;
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClicked = listener;
    }

    @Override
    public void setOnItemLongClickListener(
            OnItemLongClickListener listener) {
        mOnItemLongClicked = listener;
    }

    public void setOnScrollListener(OnScrollListener listener) {
        mOnScrolled = listener;
    }

    private DataSetObserver mDataObserver = new DataSetObserver() {

        @Override
        public void onChanged() {
            synchronized (HorizontalListView.this) {
                mDataChanged = true;
            }
            invalidate();
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            reset();
            invalidate();
            requestLayout();
        }

    };

    private int mSpecificLeft;

    private int mSpecificPosition;

    private int mScrollStatus;

    private boolean mIsCancelOrUp;

    private boolean mIsLayoutDirty;

    private int mFreezePosInAdapter = -1;

    private View mFreezeChild;

    @Override
    public ListAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public View getSelectedView() {
        return getChildAt(mSpecificPosition - getFirstVisiblePosition());
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataObserver);
        }
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mDataObserver);
        mDataChanged = true;
        requestLayout();
//		reset();
    }

    private synchronized void reset() {
        initView();
        removeAllViewsInLayout();
        requestLayout();
    }

    @Override
    public int getFirstVisiblePosition() {
        return mFirstPosition;
    }

    @Override
    public int getLastVisiblePosition() {
        return mFirstPosition + getChildCount() - 1;
    }

    @Override
    public void setSelection(int position) {
        setSelectionFromLeft(position, 0);
    }

    /**
     * Sets the selected item and positions the selection y pixels from the left edge
     * of the ListView. (If in touch mode, the item will not be selected but it will
     * still be positioned appropriately.)
     *
     * @param position Index (starting at 0) of the data item to be selected.
     * @param x        The distance from the left edge of the ListView (plus padding) that the
     *                 item will be positioned.
     */
    public void setSelectionFromLeft(int position, int x) {
        if (setSelectionFrom(position, x) >= 0) {
            requestLayout();
        }
    }

    private int setSelectionFrom(int position, int x) {
        if (mAdapter == null) return -1;
        if (position < 0 || position >= mAdapter.getCount()) return -1;

        if (!isInTouchMode()) {
            position = lookForSelectablePosition(position, true);
        }

        if (position >= 0) {
            mLayoutMode |= LAYOUT_SPECIFIC;
            mSpecificPosition = position;
            mSpecificLeft = getPaddingLeft() + x;
        }
        return position;
    }

    /**
     * has setSelection request.
     * @return true is to select. otherwise is not.
     * @see #setSelection(int)
     * @see #setSelectionFromLeft(int, int)
     */
    public boolean isLayoutRequestedBySelection() {
        return isFlagContain(mLayoutMode, LAYOUT_SPECIFIC);
    }

    @Override
    public void requestLayout() {
        mIsLayoutDirty = true;
        super.requestLayout();
    }

    /**
     * request freeze a child. usually to keep current child position on refresh data action which will be change data position.
     * @param child
     * @param posInAdapter
     */
    public void requestChildFreeze(View child, int posInAdapter) {
        mLayoutMode |= LAYOUT_FREEZE;
        if (!mIsLayoutDirty) {
            setSelectionFromLeft(posInAdapter, child.getLeft());
        } else {
            mFreezeChild = child;
            mFreezePosInAdapter = posInAdapter;
        }
    }

    /**
     * has freeze request.
     * @return true is to freeze. otherwise is not.
     * @see #requestChildFreeze(android.view.View, int)
     */
    public boolean isLayoutRequestByFreeze() {
        return isFlagContain(mLayoutMode, LAYOUT_FREEZE);
    }

    /**
     * Find a position that can be selected (i.e., is not a separator).
     *
     * @param position The starting position to look at.
     * @param lookDown Whether to look down for other positions.
     * @return The next selectable position starting at position and then searching either up or
     *         down. Returns {@link #INVALID_POSITION} if nothing can be found.
     */
    private int lookForSelectablePosition(int position, boolean lookDown) {
        final ListAdapter adapter = mAdapter;
        if (adapter == null || isInTouchMode()) {
            return INVALID_POSITION;
        }

        final int count = adapter.getCount();
        if (!adapter.areAllItemsEnabled()) {
            if (lookDown) {
                position = Math.max(0, position);
                while (position < count && !adapter.isEnabled(position)) {
                    position++;
                }
            } else {
                position = Math.min(position, count - 1);
                while (position >= 0 && !adapter.isEnabled(position)) {
                    position--;
                }
            }

            if (position < 0 || position >= count) {
                return INVALID_POSITION;
            }
            return position;
        } else {
            if (position < 0 || position >= count) {
                return INVALID_POSITION;
            }
            return position;
        }
    }

    private void addAndMeasureChild(final View child, int viewPos) {
        LayoutParams params = (LayoutParams) child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.MATCH_PARENT);
        }

        addViewInLayout(child, viewPos, params, true);

        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(),
                MeasureSpec.EXACTLY);
        int childHeightSpec = ViewGroup.getChildMeasureSpec(heightMeasureSpec,
                getPaddingTop() + getPaddingBottom(), params.height);
        int childWidthSpec = 0;
        if (params.width == LayoutParams.MATCH_PARENT) {
            childWidthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(),
                    MeasureSpec.EXACTLY);
        } else if (params.width == LayoutParams.WRAP_CONTENT) {
            childWidthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        } else {
            childWidthSpec = MeasureSpec.makeMeasureSpec(params.width, MeasureSpec.EXACTLY);
        }
        child.measure(childWidthSpec, childHeightSpec);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top,
                            int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mAdapter == null) {
            return;
        }

        if (isLayoutRequestByFreeze()) {
            mLayoutMode &= ~LAYOUT_FREEZE;
            if (mIsLayoutDirty) {
                mFirstPosition = mFreezePosInAdapter;
                //Util.v("Freeze pos = " + mFreezePosInAdapter);
                //Util.v("Freeze left = " + (mFreezeChild == null ? 0 : mFreezeChild.getLeft()));
                setSelectionFrom(mFreezePosInAdapter, (mFreezeChild == null ? 0 : mFreezeChild.getLeft()));
            }
        }

        if (mIsLayoutDirty) mIsLayoutDirty = false;

        if (mDataChanged) {
            if (isLayoutRequestedBySelection()) {
                initViewForSpecific();
            } else {
                int oldCurrentX = mCurrentX;
                initView();
                removeAllViewsInLayout();
                mNextX = oldCurrentX;
            }
            mDataChanged = false;
        }

        if (mScroller.computeScrollOffset()) {
            int scrollx = mScroller.getCurrX();
            mNextX = scrollx;
        }

        if (mNextX <= mMinX) {
            mNextX = mMinX;
            mScroller.forceFinished(true);
        }
        if (mNextX >= mMaxX) {
            mNextX = mMaxX;
            mScroller.forceFinished(true);
        }

        int dx = 0;
        if (isFlagContain(mLayoutMode, LAYOUT_SPECIFIC)) {
            removeAllViewsInLayout();
            initViewForSpecific();

            fillSpecificV2(mSpecificPosition, mSpecificLeft);
            positionItems(mSpecificLeft);

            if (mScroller.computeScrollOffset()) {
                int finalX = mScroller.getFinalX();
                finalX = Math.min(finalX, mMaxX);
                finalX = Math.max(finalX, mMinX);
                mScroller.setFinalX(finalX);
            }

            mLayoutMode &= ~LAYOUT_SPECIFIC;
        } else {
            dx = mCurrentX - mNextX;
            removeNonVisibleItems(dx);
            fillList(dx);
            positionItems(dx);

            if (mMinX == 0 || mMaxX == 0) {
                mNextX = mCurrentX;
                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }
            }
        }

        mCurrentX = mNextX;

        if (!mScroller.isFinished()) {
            post(new Runnable() {

                @Override
                public void run() {
                    requestLayout();
                }
            });
        } else {
            reportScroll(OnScrollListener.SCROLL_IDLE);
            reportScrollState(OnScrollListener.SCROLL_IDLE);
        }
    }

    private void fillList(final int dx) {
        int edge = 0;
        View child = getChildAt(getChildCount() - 1);
        if (child != null) {
            edge = child.getRight();
        }
        fillListRight(edge, dx);

        edge = 0;
        child = getChildAt(0);
        if (child != null) {
            edge = child.getLeft();
        }
        fillListLeft(edge, dx);
    }

    private void fillSpecificV2(int position, int delta) {
        View child = mAdapter.getView(position, mRemovedViewQueue.poll(), this);
        if (child == null) return;
        addAndMeasureChild(child, -1);

        if (child != null) {

            int leftEdge = delta, rightEdge = delta + child.getMeasuredWidth();
            if (leftEdge + child.getMeasuredWidth() < 0 || rightEdge > getMeasuredWidth()) {
                mSpecificLeft = 0;
                leftEdge = 0;
                rightEdge = child.getMeasuredWidth();
            }

            fillListRight(rightEdge, 0);
            int widthDelta = getMeasuredWidth() - getChildrenWidth(0, getChildCount()) - leftEdge;
            int childCountAfterFillRight = getChildCount();
            if (widthDelta > 0) {
                // move to right edge if not fill right screen.
                leftEdge += widthDelta;
                mSpecificLeft += widthDelta;
            }
            fillListLeft(leftEdge, 0);
            widthDelta = leftEdge - getChildrenWidth(0, getChildCount() - childCountAfterFillRight);
            if (widthDelta > 0) {
                // move to left edge if not fill left screen.
                mSpecificLeft -= widthDelta;
            }
        }
    }

    int getChildrenWidth(int start, int end) {
        int allWidth = 0;
        for (int i = start; i < end; i++) {
            allWidth += getChildAt(i).getMeasuredWidth();
        }
        return allWidth;
    }

    private void fillListRight(int rightEdge, final int dx) {
        if (mRightViewIndex >= mAdapter.getCount()) {
            mMaxX = mCurrentX + rightEdge - getWidth();
        }
        while (rightEdge + dx < getWidth()
                && mRightViewIndex < mAdapter.getCount()) {

            View child = mAdapter.getView(mRightViewIndex,
                    mRemovedViewQueue.poll(), this);
            addAndMeasureChild(child, -1);
            rightEdge += child.getMeasuredWidth();

            if (mRightViewIndex == mAdapter.getCount() - 1) {
                mMaxX = mCurrentX + rightEdge - getWidth();
            }

            mRightViewIndex++;
        }
        if (mMaxX < 0) mMaxX = 0;
    }

    private void fillListLeft(int leftEdge, final int dx) {
        if (mLeftViewIndex < 0) {
            mMinX = mCurrentX + leftEdge;
        }
        while (leftEdge + dx > 0 && mLeftViewIndex >= 0) {
            View child = mAdapter.getView(mLeftViewIndex,
                    mRemovedViewQueue.poll(), this);
            addAndMeasureChild(child, 0);
            leftEdge -= child.getMeasuredWidth();
            if (mLeftViewIndex == 0) {
                mMinX = mCurrentX + leftEdge;
            }
            mLeftViewIndex--;
            mDisplayOffset -= child.getMeasuredWidth();
        }
        if (mMinX > 0) mMinX = 0;
        mFirstPosition = mLeftViewIndex + 1;
    }

    private void removeNonVisibleItems(final int dx) {
        View child = getChildAt(0);
        while (child != null && child.getRight() + dx <= 0) {
            mDisplayOffset += child.getMeasuredWidth();
            mRemovedViewQueue.offer(child);
            removeViewInLayout(child);
            mLeftViewIndex++;
            child = getChildAt(0);
        }

        child = getChildAt(getChildCount() - 1);
        while (child != null && child.getLeft() + dx >= getWidth()) {
            mRemovedViewQueue.offer(child);
            removeViewInLayout(child);
            mRightViewIndex--;
            child = getChildAt(getChildCount() - 1);
        }
    }

    private void positionItems(final int dx) {
        if (getChildCount() > 0) {
            mDisplayOffset += dx;
            int left = mDisplayOffset;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                int childWidth = child.getMeasuredWidth();
                child.layout(left, 0, left + childWidth,
                        child.getMeasuredHeight());
                left += childWidth;
            }
        }
    }
    
    private boolean isFlagContain(int sourceFlag, int compareFlag) {
        return (sourceFlag & compareFlag) == compareFlag;
    }

    /**
     * scroll to parameter x
     * @param x the destiny
     */
    public synchronized void scrollTo(int x) {
        mScroller.startScroll(mNextX, 0, x - mNextX, 0);
        requestLayout();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled = super.dispatchTouchEvent(ev);
        handled |= mGesture.onTouchEvent(ev);
        mIsCancelOrUp = ev.getAction() == MotionEvent.ACTION_CANCEL ||
                ev.getAction() == MotionEvent.ACTION_UP ? true : false;
        return handled;
    }

    public boolean isScrollFinish() {
        return mScroller.isFinished() && mIsCancelOrUp;
    }

    public boolean isCancelOrUpNow() {
        return mIsCancelOrUp;
    }

    void reportScroll(int status) {
        if (null != mOnScrolled && status != mScrollStatus) {
            final int first = getFirstVisiblePosition();
            final int visibleCount = getLastVisiblePosition() - first;
            final int count = mAdapter.getCount();
            mOnScrolled.onScroll(this, first, visibleCount, count);
        }
    }

    void reportScrollState(int status) {
        if (null != mOnScrolled && status != mScrollStatus) {
            mScrollStatus = status;
            mOnScrolled.onScrollStateChanged(this, status);
        }
    }

    protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                              float velocityY) {
        synchronized (HorizontalListView.this) {
            reportScrollState(OnScrollListener.SCROLL_FLING);
            mScroller.fling(mNextX, 0, (int) -velocityX, 0, mMinX, mMaxX, 0, 0);
            mScroller.computeScrollOffset();
        }
        requestLayout();

        return true;
    }

    protected boolean onDown(MotionEvent e) {
        mScroller.forceFinished(true);
        reportScrollState(OnScrollListener.SCROLL_IDLE);
        return true;
    }

    protected boolean onScroll(MotionEvent e1, MotionEvent e2,
                               float distanceX, float distanceY) {
        synchronized (HorizontalListView.this) {
            reportScrollState(OnScrollListener.SCROLL_TOUCH_SCROLL);
            // get big value when freeze. so keep it small.
            if (Math.abs((int) distanceX) > 30) {
                mNextX += distanceX > 0 ? 1 : -1;
            } else {
                mNextX += (int) distanceX;
            }
        }
        requestLayout();

        return true;
    }

    private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            return HorizontalListView.this.onDown(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            return HorizontalListView.this
                    .onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            return HorizontalListView.this.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (isEventWithinView(e, child)) {
                    if (mOnItemClicked != null) {
                        mOnItemClicked.onItemClick(HorizontalListView.this,
                                child, mLeftViewIndex + 1 + i,
                                mAdapter.getItemId(mLeftViewIndex + 1 + i));
                    }
                    if (mOnItemSelected != null) {
                        mOnItemSelected.onItemSelected(HorizontalListView.this,
                                child, mLeftViewIndex + 1 + i,
                                mAdapter.getItemId(mLeftViewIndex + 1 + i));
                    }
                    break;
                }

            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (isEventWithinView(e, child)) {
                    if (mOnItemLongClicked != null) {
                        mOnItemLongClicked.onItemLongClick(
                                HorizontalListView.this, child, mLeftViewIndex
                                + 1 + i,
                                mAdapter.getItemId(mLeftViewIndex + 1 + i));
                    }
                    break;
                }
            }
        }

        private boolean isEventWithinView(MotionEvent e, View child) {
            Rect viewRect = new Rect();
            int[] childPosition = new int[2];
            child.getLocationOnScreen(childPosition);
            int left = childPosition[0];
            int right = left + child.getWidth();
            int top = childPosition[1];
            int bottom = top + child.getHeight();
            viewRect.set(left, top, right, bottom);
            return viewRect.contains((int) e.getRawX(), (int) e.getRawY());
        }
    };

    public static interface OnScrollListener {

        public final static int SCROLL_IDLE = 0;
        public final static int SCROLL_TOUCH_SCROLL = 1;
        public final static int SCROLL_FLING = 2;

        /**
         * Callback method to be invoked while the list view is being scrolled or flown. If the
         * view is being scrolled, this method will be called before the next frame of the scroll is
         * rendered. if it's being flying, it will be invoked at flying finish.</br>
         * In particular, it will be called before any calls to
         * {@link android.widget.BaseAdapter#getView(int, android.view.View, android.view.ViewGroup)}.
         *
         * @param view        The view whose scroll state is being reported
         * @param status The current scroll state. One of {@link #SCROLL_IDLE},
         *                    {@link #SCROLL_TOUCH_SCROLL} or {@link #SCROLL_FLING}.
         */
        void onScrollStateChanged(AdapterView<?> view, int status);

        /**
         * Callback method to be invoked when the list has been scrolled or flown. This will be
         * called after the scroll or fly has completed
         *
         * @param view             The view whose scroll state is being reported
         * @param firstVisibleItem the index of the first visible cell (ignore if
         *                         visibleItemCount == 0)
         * @param visibleItemCount the number of visible cells
         * @param totalItemCount   the number of items in the list adaptor
         */
        public void onScroll(AdapterView<?> view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount);
    }

}
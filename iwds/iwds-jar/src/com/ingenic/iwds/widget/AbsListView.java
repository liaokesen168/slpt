/*
 * Copyright (C) 2015 Ingenic Semiconductor
 * 
 * LiJinWen(Kevin)<kevin.jwli@ingenic.com>
 * 
 * Elf/iwds-jar
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.ingenic.iwds.widget;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.StateSet;
import android.view.ActionMode;
import android.view.HapticFeedbackConstants;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnTouchModeChangeListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Adapter;
import android.widget.Checkable;
import android.widget.ListAdapter;
import android.widget.OverScroller;

import com.ingenic.iwds.HardwareList;
import com.ingenic.iwds.appwidget.IWidgetService;

/**
 * Base class that can be used to implement virtualized lists of items. A list does not have a
 * spatial definition here. For instance, subclases of this class can display the content of the
 * list in a grid, in a carousel, as stack, etc.
 * 
 * @attr ref android.R.styleable#AbsListView_listSelector
 * @attr ref android.R.styleable#AbsListView_drawSelectorOnTop
 * @attr ref android.R.styleable#AbsListView_stackFromBottom
 * @attr ref android.R.styleable#AbsListView_scrollingCache
 * @attr ref android.R.styleable#AbsListView_textFilterEnabled
 * @attr ref android.R.styleable#AbsListView_transcriptMode
 * @attr ref android.R.styleable#AbsListView_cacheColorHint
 * @attr ref android.R.styleable#AbsListView_fastScrollEnabled
 * @attr ref android.R.styleable#AbsListView_smoothScrollbar
 * @attr ref android.R.styleable#AbsListView_choiceMode
 */
/**
 * 用于实现条目的虚拟列表的基类。 这里的列表没有空间的定义。 例如，该类的子类可以以网格的形式、走马灯的形式显示，或者作为堆栈等等。
 */
public abstract class AbsListView extends AdapterView<ListAdapter> implements
        ViewTreeObserver.OnTouchModeChangeListener,
        LocalRemoteViewsAdapter.LocalRemoteAdapterConnectionCallback {

    private static final String TAG = "AbsListView";
    /**
     * Sentinel value for no current active pointer. Used by {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;

    private static final boolean PROFILE_SCROLLING = false;

    private static final boolean PROFILE_FLINGING = false;

    /**
     * How many positions in either direction we will search to try to find a checked item with a
     * stable ID that moved position across a data set change. If the item isn't found it will be
     * unselected.
     */
    private static final int CHECK_POSITION_SEARCH_DISTANCE = 20;

    /**
     * Used to request a layout when we changed touch mode
     */
    private static final int TOUCH_MODE_UNKNOWN = -1;
    private static final int TOUCH_MODE_ON = 0;
    private static final int TOUCH_MODE_OFF = 1;

    /**
     * Indicates that we are not in the middle of a touch gesture
     */
    static final int TOUCH_MODE_REST = -1;

    /**
     * Indicates we just received the touch event and we are waiting to see if the it is a tap or a
     * scroll gesture.
     */
    static final int TOUCH_MODE_DOWN = 0;

    /**
     * Indicates the touch has been recognized as a tap and we are now waiting to see if the touch
     * is a longpress
     */
    static final int TOUCH_MODE_TAP = 1;

    /**
     * Indicates we have waited for everything we can wait for, but the user's finger is still down
     */
    static final int TOUCH_MODE_DONE_WAITING = 2;

    /**
     * Indicates the touch gesture is a scroll
     */
    static final int TOUCH_MODE_SCROLL = 3;

    /**
     * Indicates the view is in the process of being flung
     */
    static final int TOUCH_MODE_FLING = 4;

    /**
     * Indicates the touch gesture is an overscroll - a scroll beyond the beginning or end.
     */
    static final int TOUCH_MODE_OVERSCROLL = 5;

    /**
     * Indicates the view is being flung outside of normal content bounds and will spring back.
     */
    static final int TOUCH_MODE_OVERFLING = 6;

    /**
     * Used for smooth scrolling at a consistent rate
     */
    static final Interpolator sLinearInterpolator = new LinearInterpolator();

    /**
     * Regular layout - usually an unsolicited layout from the view system
     */
    static final int LAYOUT_NORMAL = 0;

    /**
     * Show the first item
     */
    static final int LAYOUT_FORCE_TOP = 1;

    /**
     * Force the selected item to be on somewhere on the screen
     */
    static final int LAYOUT_SET_SELECTION = 2;

    /**
     * Show the last item
     */
    static final int LAYOUT_FORCE_BOTTOM = 3;

    /**
     * Make a mSelectedItem appear in a specific location and build the rest of the views from
     * there. The top is specified by mSpecificTop.
     */
    static final int LAYOUT_SPECIFIC = 4;

    /**
     * Layout to sync as a result of a data change. Restore mSyncPosition to have its top at
     * mSpecificTop
     */
    static final int LAYOUT_SYNC = 5;

    /**
     * Layout as a result of using the navigation keys
     */
    static final int LAYOUT_MOVE_SELECTION = 6;

    /**
     * Content height divided by this is the overscroll limit.
     */
    static final int OVERSCROLL_LIMIT_DIVISOR = 3;

    /**
     * Normal list that does not indicate choices
     */
    /**
     * 不显示选择状态的普通列表。
     */
    public static final int CHOICE_MODE_NONE = 0;

    /**
     * The list allows up to one choice
     */
    /**
     * 允许单选的列表。
     */
    public static final int CHOICE_MODE_SINGLE = 1;

    /**
     * The list allows multiple choices
     */
    /**
     * 允许多选的列表。
     */
    public static final int CHOICE_MODE_MULTIPLE = 2;

    /**
     * The list allows multiple choices in a modal selection mode
     */
    /**
     * 处于定制的选择模式的多选列表。
     */
    public static final int CHOICE_MODE_MULTIPLE_MODAL = 3;

    /**
     * Disables the transcript mode.
     * 
     * @see #setTranscriptMode(int)
     */
    /**
     * 禁用跳转模式。
     * 
     * @see #setTranscriptMode(int)
     */
    public static final int TRANSCRIPT_MODE_DISABLED = 0;
    /**
     * The list will automatically scroll to the bottom when a data set change notification is
     * received and only if the last item is already visible on screen.
     * 
     * @see #setTranscriptMode(int)
     */
    /**
     * 仅当最后的条目在屏幕上可见，并且收到数据集变更消息时列表将自动滚动到底部。
     * 
     * @see #setTranscriptMode(int)
     */
    public static final int TRANSCRIPT_MODE_NORMAL = 1;
    /**
     * The list will automatically scroll to the bottom, no matter what items are currently visible.
     * 
     * @see #setTranscriptMode(int)
     */
    /**
     * 无视当前可见条目，总是自动滚动到列表的底部。
     * 
     * @see #setTranscriptMode(int)
     */
    public static final int TRANSCRIPT_MODE_ALWAYS_SCROLL = 2;

    private Runnable mClearScrollingCache;

    private int mTouchSlop;
    private float mDensityScale;

    private int mMinimumVelocity;
    private int mMaximumVelocity;

    /**
     * Optional callback to notify client when scroll position has changed
     */
    private OnScrollListener mOnScrollListener;

    private int mLastAccessibilityScrollEventFromIndex;
    private int mLastAccessibilityScrollEventToIndex;

    /**
     * ID of the active pointer. This is used to retain consistency during drags/flings if multiple
     * pointers are used.
     */
    private int mActivePointerId = INVALID_POINTER;

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;

    /**
     * The last scroll state reported to clients through {@link OnScrollListener}.
     */
    private int mLastScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    private boolean mScrollProfilingStarted = false;
    private boolean mFlingProfilingStarted = false;

    /**
     * An estimate of how many pixels are between the top of the list and the top of the first
     * position in the adapter, based on the last time we saw it. Used to hint where to draw edge
     * glows.
     */
    private int mFirstPositionDistanceGuess;

    /**
     * An estimate of how many pixels are between the bottom of the list and the bottom of the last
     * position in the adapter, based on the last time we saw it. Used to hint where to draw edge
     * glows.
     */
    private int mLastPositionDistanceGuess;

    /**
     * The select child's view (from the adapter's getView) is enabled.
     */
    private boolean mIsChildViewEnabled;

    /**
     * Handles one frame of a fling
     */
    private FlingRunnable mFlingRunnable;

    /**
     * Track the item count from the last time we handled a data change.
     */
    private int mLastHandledItemCount;

    /**
     * The saved state that we will be restoring from when we next sync. Kept here so that if we
     * happen to be asked to save our state before the sync happens, we can return this existing
     * data rather than losing it.
     */
    private SavedState mPendingSync;

    /**
     * This view is in transcript mode -- it shows the bottom of the list when the data changes
     */
    private int mTranscriptMode;

    /**
     * Tracked on measurement in transcript mode. Makes sure that we can still pin to the bottom
     * correctly on resizes.
     */
    private boolean mForceTranscriptScroll;

    /**
     * Indicates that this list is always drawn on top of a solid, single-color, opaque background
     */
    private int mCacheColorHint;

    /**
     * Indicates whether to use pixels-based or position-based scrollbar properties.
     */
    private boolean mSmoothScrollbarEnabled = true;

    /**
     * Acts upon click
     */
    private AbsListView.PerformClick mPerformClick;

    /**
     * Delayed action for touch mode.
     */
    private Runnable mTouchModeReset;

    private int mLastTouchMode = TOUCH_MODE_UNKNOWN;

    /**
     * Rectangle used for hit testing children
     */
    private Rect mTouchFrame;

    /**
     * Used for determining when to cancel out of overscroll.
     */
    private int mDirection = 0;

    /**
     * The last CheckForTap runnable we posted, if any
     */
    private Runnable mPendingCheckForTap;

    /**
     * The last CheckForLongPress runnable we posted, if any
     */
    private CheckForLongPress mPendingCheckForLongPress;

    private float mVelocityScale = 1.0f;

    /**
     * The last CheckForKeyLongPress runnable we posted, if any
     */
    private CheckForKeyLongPress mPendingCheckForKeyLongPress;

    /**
     * Used for interacting with list items from an accessibility service.
     */
    private ListItemAccessibilityDelegate mAccessibilityDelegate;

    private LocalRemoteViewsAdapter mRemoteAdapter;

    private boolean mDeferNotifyDataSetChanged = false;

    boolean mScrollingCacheEnabled;

    /**
     * When the view is scrolling, this flag is set to true to indicate subclasses that the drawing
     * cache was enabled on the children
     */
    boolean mCachingStarted;
    boolean mCachingActive;

    /**
     * Maximum distance to overscroll by during edge effects
     */
    int mOverscrollDistance;

    /**
     * Maximum distance to overfling during edge effects
     */
    int mOverflingDistance;

    /**
     * The adapter containing the data to be displayed by this view
     */
    ListAdapter mAdapter;

    /**
     * If mAdapter != null, whenever this is true the adapter has stable IDs.
     */
    boolean mAdapterHasStableIds;

    /**
     * Controls if/how the user may choose/check items in the list
     */
    int mChoiceMode = CHOICE_MODE_NONE;

    /**
     * Controls CHOICE_MODE_MULTIPLE_MODAL. null when inactive.
     */
    ActionMode mChoiceActionMode;

    /**
     * Wrapper for the multiple choice mode callback; AbsListView needs to perform a few extra
     * actions around what application code does.
     */
    MultiChoiceModeWrapper mMultiChoiceModeCallback;

    /**
     * Running state of which positions are currently checked
     */
    SparseBooleanArray mCheckStates;

    /**
     * Running state of which IDs are currently checked. If there is a value for a given key, the
     * checked state for that ID is true and the value holds the last known position in the adapter
     * for that id.
     */
    LongSparseArray<Integer> mCheckedIdStates;

    /**
     * Running count of how many items are currently checked
     */
    int mCheckedItemCount;

    /**
     * This view's padding
     */
    Rect mListPadding = new Rect();

    /**
     * One of TOUCH_MODE_REST, TOUCH_MODE_DOWN, TOUCH_MODE_TAP, TOUCH_MODE_SCROLL, or
     * TOUCH_MODE_DONE_WAITING
     */
    int mTouchMode = TOUCH_MODE_REST;

    /**
     * Handles scrolling between positions within the list.
     */
    PositionScroller mPositionScroller;

    /**
     * The position of the view that received the down motion event
     */
    int mMotionPosition;

    /**
     * The offset to the top of the mMotionPosition view when the down motion event was received
     */
    int mMotionViewOriginalTop;

    /**
     * Controls how the next layout will happen
     */
    int mLayoutMode = LAYOUT_NORMAL;

    /**
     * The position to resurrect the selected position to.
     */
    int mResurrectToPosition = INVALID_POSITION;

    /**
     * The offset in pixels form the top of the AdapterView to the top of the currently selected
     * view. Used to save and restore state.
     */
    int mSelectedTop = 0;

    /**
     * The desired offset to the top of the mMotionPosition view after a scroll
     */
    int mMotionViewNewTop;

    /**
     * The current position of the selector in the list.
     */
    int mSelectorPosition = INVALID_POSITION;

    /**
     * Defines the selector's location and dimension at drawing time
     */
    Rect mSelectorRect = new Rect();

    /**
     * The selection's left padding
     */
    int mSelectionLeftPadding = 0;

    /**
     * The selection's top padding
     */
    int mSelectionTopPadding = 0;

    /**
     * The selection's right padding
     */
    int mSelectionRightPadding = 0;

    /**
     * The selection's bottom padding
     */
    int mSelectionBottomPadding = 0;

    Runnable mPositionScrollAfterLayout;

    /**
     * Indicates whether the list is stacked from the bottom edge or the top edge.
     */
    boolean mStackFromBottom;

    /**
     * Track if we are currently attached to a window.
     */
    boolean mIsAttached;

    /**
     * The drawable used to draw the selector
     */
    Drawable mSelector;

    /**
     * Maximum distance to record overscroll
     */
    int mOverscrollMax;

    /**
     * Indicates whether the list selector should be drawn on top of the children or behind
     */
    boolean mDrawSelectorOnTop = false;

    /**
     * Should be used by subclasses to listen to changes in the dataset
     */
    AdapterDataSetObserver mDataSetObserver;

    /**
     * The X value associated with the the down motion event
     */
    int mMotionX;

    /**
     * The Y value associated with the the down motion event
     */
    int mMotionY;

    /**
     * Y value from on the previous motion event (if any)
     */
    int mLastY;

    /**
     * How far the finger moved before we started scrolling
     */
    int mMotionCorrection;

    /**
     * Subclasses must retain their measure spec from onMeasure() into this member
     */
    int mWidthMeasureSpec = 0;

    /**
     * The top scroll indicator
     */
    View mScrollUp;

    /**
     * The down scroll indicator
     */
    View mScrollDown;

    /**
     * The data set used to store unused views that should be reused during the next layout to avoid
     * creating new ones
     */
    final RecycleBin mRecycler = new RecycleBin();

    final boolean[] mIsScrap = new boolean[1];

    /**
     * Interface definition for a callback to be invoked when the list or grid has been scrolled.
     */
    /**
     * 为了在列表或网格滚动时执行回调函数而定义的接口。
     */
    public interface OnScrollListener {

        /**
         * The view is not scrolling. Note navigating the list using the trackball counts as being
         * in the idle state since these transitions are not animated.
         */
        /**
         * 视图没有滚动。 注意，使用轨迹球滚动时，在滚动停止之前，一直处于空闲状态。
         */
        public static int SCROLL_STATE_IDLE = 0;

        /**
         * The user is scrolling using touch, and their finger is still on the screen
         */
        /**
         * 用户通过触控滚动，并且手指没有离开屏幕。
         */
        public static int SCROLL_STATE_TOUCH_SCROLL = 1;

        /**
         * The user had previously been scrolling using touch and had performed a fling. The
         * animation is now coasting to a stop
         */
        /**
         * 用户之前通过触控滚动并执行了快速滚动。 滚动动画正滑向停止点。
         */
        public static int SCROLL_STATE_FLING = 2;

        /**
         * Callback method to be invoked while the list view or grid view is being scrolled. If the
         * view is being scrolled, this method will be called before the next frame of the scroll is
         * rendered. In particular, it will be called before any calls to
         * {@link Adapter#getView(int, View, ViewGroup)}.
         * 
         * @param view The view whose scroll state is being reported
         * 
         * @param scrollState The current scroll state. One of {@link #SCROLL_STATE_IDLE},
         *        {@link #SCROLL_STATE_TOUCH_SCROLL} or {@link #SCROLL_STATE_IDLE}.
         */
        /**
         * 当列表视图或网格视图正在滚动是执行的回调函数。 如果视图正在滚动， 该方法会在渲染下一帧之前调用该方法。 就是说，会在调用任何
         * {@link Adapter#getView(int, View, ViewGroup)}方法之前调用。
         * 
         * @param view 报告滚动状态的对象视图。
         * @param scrollState 当前滚动状态。值为{@link #SCROLL_STATE_IDLE}、
         *        {@link #SCROLL_STATE_TOUCH_SCROLL}或{@link #SCROLL_STATE_IDLE}。
         */
        public void onScrollStateChanged(AbsListView view, int scrollState);

        /**
         * Callback method to be invoked when the list or grid has been scrolled. This will be
         * called after the scroll has completed
         * 
         * @param view The view whose scroll state is being reported
         * @param firstVisibleItem the index of the first visible cell (ignore if visibleItemCount
         *        == 0)
         * @param visibleItemCount the number of visible cells
         * @param totalItemCount the number of items in the list adaptor
         */
        /**
         * 当列表或网格的滚动已经完成时调用的回调函数。 会在滚动完成后调用。
         * 
         * @param view 报告滚动状态的对象视图。
         * @param firstVisibleItem 第一个可见单元格的索引（如果 visibleItemCount == 0 则忽略该参数）。
         * @param visibleItemCount 可见单元格数。
         * @param totalItemCount 列表适配器中的条目数。
         */
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount);
    }

    /**
     * The top-level view of a list item can implement this interface to allow itself to modify the
     * bounds of the selection shown for that item.
     */
    /**
     * 列表条目的顶级视图可以实现该接口，允许其改变自己的边界状态，以显示条目的选中状态。
     */
    public interface SelectionBoundsAdjuster {
        /**
         * Called to allow the list item to adjust the bounds shown for its selection.
         * 
         * @param bounds On call, this contains the bounds the list has selected for the item (that
         *        is the bounds of the entire view). The values can be modified as desired.
         */
        /**
         * 回调函数，以允许列表条目调整其边界，以表示选中状态。
         * 
         * @param bounds 调用时，包含列表选中条目的边界（整个条目视图的边界）。 其值可以任意编辑。
         */
        public void adjustListItemSelectionBounds(Rect bounds);
    }

    /**
     * 构造方法
     */
    public AbsListView(Context context) {
        super(context);

        initAbsListView();
        setVerticalScrollBarEnabled(false);
    }

    /**
     * 构造方法
     */
    public AbsListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 构造方法
     */
    public AbsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initAbsListView();
    }

    private void initAbsListView() {
        // Setting focusable in touch mode will set the focusable property to true
        setClickable(true);
        setFocusableInTouchMode(true);
        setWillNotDraw(false);
        setAlwaysDrawnWithCacheEnabled(false);

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mOverscrollDistance = configuration.getScaledOverscrollDistance();
        mOverflingDistance = configuration.getScaledOverflingDistance();

        mDensityScale = getContext().getResources().getDisplayMetrics().density;

        // 圆屏状态下需要不断刷新子View以实现Item动画效果
        if (HardwareList.IsCircularScreen()) {
            setOnScrollListener(null);
        }
    }

    /**
     * Indicates whether the children's drawing cache is used during a scroll. By default, the
     * drawing cache is enabled but this will consume more memory.
     * 
     * @return true if the scrolling cache is enabled, false otherwise
     * 
     * @see #setScrollingCacheEnabled(boolean)
     * @see View#setDrawingCacheEnabled(boolean)
     */
    /**
     * 指示滚动时是否使用子视图的绘图缓存。 默认为使用绘图缓存，这会占用更多的内存。
     * 
     * @return 如果启用了滚动缓存返回true，否则返回false。
     * @see #setScrollingCacheEnabled(boolean)
     * @see View#setDrawingCacheEnabled(boolean)
     */
    public boolean isScrollingCacheEnabled() {
        return mScrollingCacheEnabled;
    }

    /**
     * Enables or disables the children's drawing cache during a scroll. By default, the drawing
     * cache is enabled but this will use more memory.
     * 
     * When the scrolling cache is enabled, the caches are kept after the first scrolling. You can
     * manually clear the cache by calling
     * {@link android.view.ViewGroup#setChildrenDrawingCacheEnabled(boolean)}.
     * 
     * @param enabled true to enable the scroll cache, false otherwise
     * 
     * @see #isScrollingCacheEnabled()
     * @see View#setDrawingCacheEnabled(boolean)
     */
    /**
     * 启用或停止在滚动时使用子视图的绘图缓存。默认为使用绘图缓存，这会占用更多的内存。
     * 
     * 当启用滚动缓存时，首次滚动后会保留缓存。 你可以通过调用
     * {@link android.view.ViewGroup#setChildrenDrawingCacheEnabled(boolean)}手动清除缓存。
     * 
     * @param enabled 启用滚动缓存时为true，否则为false。
     * @see #isScrollingCacheEnabled()
     * @see View#setDrawingCacheEnabled(boolean)
     */
    public void setScrollingCacheEnabled(boolean enabled) {
        if (mScrollingCacheEnabled && !enabled) {
            clearScrollingCache();
        }
        mScrollingCacheEnabled = enabled;
    }

    private void clearScrollingCache() {
        if (!isHardwareAccelerated()) {
            if (mClearScrollingCache == null) {
                mClearScrollingCache = new Runnable() {
                    public void run() {
                        if (mCachingStarted) {
                            mCachingStarted = mCachingActive = false;
                            setChildrenDrawnWithCacheEnabled(false);
                            if ((getPersistentDrawingCache() & PERSISTENT_SCROLLING_CACHE) == 0) {
                                setChildrenDrawingCacheEnabled(false);
                            }
                            if (!isAlwaysDrawnWithCacheEnabled()) {
                                invalidate();
                            }
                        }
                    }
                };
            }
            post(mClearScrollingCache);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAdapter(ListAdapter adapter) {
        if (adapter != null) {
            mAdapterHasStableIds = mAdapter.hasStableIds();
            if (mChoiceMode != CHOICE_MODE_NONE && mAdapterHasStableIds && mCheckedIdStates == null) {
                mCheckedIdStates = new LongSparseArray<Integer>();
            }
        }

        if (mCheckStates != null) {
            mCheckStates.clear();
        }

        if (mCheckedIdStates != null) {
            mCheckedIdStates.clear();
        }
    }

    /**
     * Returns the number of items currently selected. This will only be valid if the choice mode is
     * not {@link #CHOICE_MODE_NONE} (default).
     * 
     * <p>
     * To determine the specific items that are currently selected, use one of the
     * <code>getChecked*</code> methods.
     * 
     * @return The number of items currently selected
     * 
     * @see #getCheckedItemPosition()
     * @see #getCheckedItemPositions()
     * @see #getCheckedItemIds()
     */
    /**
     * 返回当前选中的条目数。该函数只有在选择模式不为{@link #CHOICE_MODE_NONE}(缺省值)时有效。
     * <p>
     * 要确定指定条目是否当前处于选中状态，可以使用<code>getChecked*</code>方法。
     * 
     * @return 当前选中的条目数。
     * @see #getCheckedItemPosition()
     * @see #getCheckedItemPositions()
     * @see #getCheckedItemIds()
     */
    public int getCheckedItemCount() {
        return mCheckedItemCount;
    }

    /**
     * Returns the checked state of the specified position. The result is only valid if the choice
     * mode has been set to {@link #CHOICE_MODE_SINGLE} or {@link #CHOICE_MODE_MULTIPLE}.
     * 
     * @param position The item whose checked state to return
     * @return The item's checked state or <code>false</code> if choice mode is invalid
     * 
     * @see #setChoiceMode(int)
     */
    /**
     * 返回指定位置的选中状态。只有当选择模式为{@link #CHOICE_MODE_SINGLE}或{@link #CHOICE_MODE_MULTIPLE}时有效。
     * 
     * @param position 要检查选择状态的条目的位置。
     * @return 条目的选择状态，选择模式无效时返回<code>false</code>
     * @see #setChoiceMode(int)
     */
    public boolean isItemChecked(int position) {
        if (mChoiceMode != CHOICE_MODE_NONE && mCheckStates != null) {
            return mCheckStates.get(position);
        }

        return false;
    }

    /**
     * Returns the currently checked item. The result is only valid if the choice mode has been set
     * to {@link #CHOICE_MODE_SINGLE}.
     * 
     * @return The position of the currently checked item or {@link #INVALID_POSITION} if nothing is
     *         selected
     * 
     * @see #setChoiceMode(int)
     */
    /**
     * 返回当前处于选中状态的条目位置。只有当选择模式为{@link #CHOICE_MODE_SINGLE}时有效。
     * 
     * @return 当前选中条目的位置或无选中条目是返回{@link #INVALID_POSITION}
     * @see #setChoiceMode(int)
     */
    public int getCheckedItemPosition() {
        if (mChoiceMode == CHOICE_MODE_SINGLE && mCheckStates != null && mCheckStates.size() == 1) {
            return mCheckStates.keyAt(0);
        }

        return INVALID_POSITION;
    }

    /**
     * Returns the set of checked items in the list. The result is only valid if the choice mode has
     * not been set to {@link #CHOICE_MODE_NONE}.
     * 
     * @return A SparseBooleanArray which will return true for each call to get(int position) where
     *         position is a position in the list, or <code>null</code> if the choice mode is set to
     *         {@link #CHOICE_MODE_NONE}.
     */
    /**
     * 在列表中返回选中条目的集合。 只有当选择模式不为{@link #CHOICE_MODE_NONE}时有效。
     * 
     * @return 对每个元素调用 get(int position) 均返回true的 SparseBooleanArray； 如果选择模式为
     *         {@link #CHOICE_MODE_NONE}则返回<code>null</code>。
     */
    public SparseBooleanArray getCheckedItemPositions() {
        if (mChoiceMode != CHOICE_MODE_NONE) {
            return mCheckStates;
        }
        return null;
    }

    /**
     * Returns the set of checked items ids. The result is only valid if the choice mode has not
     * been set to {@link #CHOICE_MODE_NONE} and the adapter has stable IDs. (
     * {@link ListAdapter#hasStableIds()} == {@code true})
     * 
     * @return A new array which contains the id of each checked item in the list.
     */
    /**
     * 返回选中条目的 ID 的集合。只有当选择模式不为{@link #CHOICE_MODE_NONE}，并且适配器拥有静态 ID 时有效。(
     * {@link ListAdapter#hasStableIds()} == {@code true})
     * 
     * @return 包含列表中所有选中条目的 ID 的新的数组。
     */
    public long[] getCheckedItemIds() {
        if (mChoiceMode == CHOICE_MODE_NONE || mCheckedIdStates == null || mAdapter == null) {
            return new long[0];
        }

        final LongSparseArray<Integer> idStates = mCheckedIdStates;
        final int count = idStates.size();
        final long[] ids = new long[count];

        for (int i = 0; i < count; i++) {
            ids[i] = idStates.keyAt(i);
        }

        return ids;
    }

    /**
     * Clear any choices previously set
     */
    /**
     * 清除之前的选择状态。
     */
    public void clearChoices() {
        if (mCheckStates != null) {
            mCheckStates.clear();
        }
        if (mCheckedIdStates != null) {
            mCheckedIdStates.clear();
        }
        mCheckedItemCount = 0;
    }

    /**
     * Sets the checked state of the specified position. The is only valid if the choice mode has
     * been set to {@link #CHOICE_MODE_SINGLE} or {@link #CHOICE_MODE_MULTIPLE}.
     * 
     * @param position The item whose checked state is to be checked
     * @param value The new checked state for the item
     */
    /**
     * 设置指定位置的条目为选中状态。只有当选择模式为{@link #CHOICE_MODE_SINGLE}或{@link #CHOICE_MODE_MULTIPLE}时有效。
     * 
     * @param position 要设置选择状态的条目位置。
     * @param value 新的选中状态 。
     */
    public void setItemChecked(int position, boolean value) {
        if (mChoiceMode == CHOICE_MODE_NONE) {
            return;
        }

        // Start selection mode if needed. We don't need to if we're unchecking something.
        if (value && mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL && mChoiceActionMode == null) {
            if (mMultiChoiceModeCallback == null || !mMultiChoiceModeCallback.hasWrappedCallback()) {
                throw new IllegalStateException("AbsListView: attempted to start selection mode "
                        + "for CHOICE_MODE_MULTIPLE_MODAL but no choice mode callback was "
                        + "supplied. Call setMultiChoiceModeListener to set a callback.");
            }
            mChoiceActionMode = startActionMode(mMultiChoiceModeCallback);
        }

        if (mChoiceMode == CHOICE_MODE_MULTIPLE || mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
            boolean oldValue = mCheckStates.get(position);
            mCheckStates.put(position, value);
            if (mCheckedIdStates != null && mAdapter.hasStableIds()) {
                if (value) {
                    mCheckedIdStates.put(mAdapter.getItemId(position), position);
                } else {
                    mCheckedIdStates.delete(mAdapter.getItemId(position));
                }
            }
            if (oldValue != value) {
                if (value) {
                    mCheckedItemCount++;
                } else {
                    mCheckedItemCount--;
                }
            }
            if (mChoiceActionMode != null) {
                final long id = mAdapter.getItemId(position);
                mMultiChoiceModeCallback.onItemCheckedStateChanged(mChoiceActionMode, position, id,
                        value);
            }
        } else {
            boolean updateIds = mCheckedIdStates != null && mAdapter.hasStableIds();
            // Clear all values if we're checking something, or unchecking the currently
            // selected item
            if (value || isItemChecked(position)) {
                mCheckStates.clear();
                if (updateIds) {
                    mCheckedIdStates.clear();
                }
            }
            // this may end up selecting the value we just cleared but this way
            // we ensure length of mCheckStates is 1, a fact getCheckedItemPosition relies on
            if (value) {
                mCheckStates.put(position, true);
                if (updateIds) {
                    mCheckedIdStates.put(mAdapter.getItemId(position), position);
                }
                mCheckedItemCount = 1;
            } else if (mCheckStates.size() == 0 || !mCheckStates.valueAt(0)) {
                mCheckedItemCount = 0;
            }
        }

        // Do not generate a data change while we are in the layout phase
        if (!mInLayout && !mBlockLayoutRequests) {
            mDataChanged = true;
            rememberSyncState();
            requestLayout();
        }
    }

    @Override
    public boolean performItemClick(View view, int position, long id) {
        boolean handled = false;
        boolean dispatchItemClick = true;

        if (mChoiceMode != CHOICE_MODE_NONE) {
            handled = true;
            boolean checkedStateChanged = false;

            if (mChoiceMode == CHOICE_MODE_MULTIPLE
                    || (mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL && mChoiceActionMode != null)) {
                boolean checked = !mCheckStates.get(position, false);
                mCheckStates.put(position, checked);
                if (mCheckedIdStates != null && mAdapter.hasStableIds()) {
                    if (checked) {
                        mCheckedIdStates.put(mAdapter.getItemId(position), position);
                    } else {
                        mCheckedIdStates.delete(mAdapter.getItemId(position));
                    }
                }
                if (checked) {
                    mCheckedItemCount++;
                } else {
                    mCheckedItemCount--;
                }
                if (mChoiceActionMode != null) {
                    mMultiChoiceModeCallback.onItemCheckedStateChanged(mChoiceActionMode, position,
                            id, checked);
                    dispatchItemClick = false;
                }
                checkedStateChanged = true;
            } else if (mChoiceMode == CHOICE_MODE_SINGLE) {
                boolean checked = !mCheckStates.get(position, false);
                if (checked) {
                    mCheckStates.clear();
                    mCheckStates.put(position, true);
                    if (mCheckedIdStates != null && mAdapter.hasStableIds()) {
                        mCheckedIdStates.clear();
                        mCheckedIdStates.put(mAdapter.getItemId(position), position);
                    }
                    mCheckedItemCount = 1;
                } else if (mCheckStates.size() == 0 || !mCheckStates.valueAt(0)) {
                    mCheckedItemCount = 0;
                }
                checkedStateChanged = true;
            }

            if (checkedStateChanged) {
                updateOnScreenCheckedViews();
            }
        }

        if (dispatchItemClick) {
            handled |= super.performItemClick(view, position, id);
        }

        return handled;
    }

    /**
     * Perform a quick, in-place update of the checked or activated state on all visible item views.
     * This should only be called when a valid choice mode is active.
     */
    private void updateOnScreenCheckedViews() {
        final int firstPos = mFirstPosition;
        final int count = getChildCount();
        final boolean useActivated =
                getContext().getApplicationInfo().targetSdkVersion >= android.os.Build.VERSION_CODES.HONEYCOMB;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            final int position = firstPos + i;

            if (child instanceof Checkable) {
                ((Checkable) child).setChecked(mCheckStates.get(position));
            } else if (useActivated) {
                child.setActivated(mCheckStates.get(position));
            }
        }
    }

    /**
     * @see #setChoiceMode(int)
     * 
     * @return The current choice mode
     */
    /**
     * 获取当前选择模式
     * 
     * @see #setChoiceMode(int)
     * @return 返回当前选择模式。
     */
    public int getChoiceMode() {
        return mChoiceMode;
    }

    /**
     * Defines the choice behavior for the List. By default, Lists do not have any choice behavior (
     * {@link #CHOICE_MODE_NONE}). By setting the choiceMode to {@link #CHOICE_MODE_SINGLE}, the
     * List allows up to one item to be in a chosen state. By setting the choiceMode to
     * {@link #CHOICE_MODE_MULTIPLE}, the list allows any number of items to be chosen.
     * 
     * @param choiceMode One of {@link #CHOICE_MODE_NONE}, {@link #CHOICE_MODE_SINGLE}, or
     *        {@link #CHOICE_MODE_MULTIPLE}
     */
    /**
     * 定义列表的选择行为。默认列表没有选择行为({@link #CHOICE_MODE_NONE})。通过设置choiceMode为{@link #CHOICE_MODE_SINGLE}
     * ，使列表允许最多一个条目处于选中状态。通过设置choiceMode为{@link #CHOICE_MODE_MULTIPLE}，使列表允许任意数量的条目处于选中状态。
     * 
     * @param choiceMode {@link #CHOICE_MODE_NONE}、{@link #CHOICE_MODE_SINGLE}、
     *        {@link CHOICE_MODE_MULTIPLE}之一。
     */
    public void setChoiceMode(int choiceMode) {
        mChoiceMode = choiceMode;
        if (mChoiceActionMode != null) {
            mChoiceActionMode.finish();
            mChoiceActionMode = null;
        }
        if (mChoiceMode != CHOICE_MODE_NONE) {
            if (mCheckStates == null) {
                mCheckStates = new SparseBooleanArray();
            }
            if (mCheckedIdStates == null && mAdapter != null && mAdapter.hasStableIds()) {
                mCheckedIdStates = new LongSparseArray<Integer>();
            }
            // Modal multi-choice mode only has choices when the mode is active. Clear them.
            if (mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
                clearChoices();
                setLongClickable(true);
            }
        }
    }

    /**
     * Set a {@link MultiChoiceModeListener} that will manage the lifecycle of the selection
     * {@link ActionMode}. Only used when the choice mode is set to
     * {@link #CHOICE_MODE_MULTIPLE_MODAL}.
     * 
     * @param listener Listener that will manage the selection mode
     * 
     * @see #setChoiceMode(int)
     */
    /**
     * 设置用于管理选择{@link ActionMode}生命周期的{@link MultiChoiceModeListener}。仅用于选择模式为
     * {@link #CHOICE_MODE_MULTIPLE_MODAL}时。
     * 
     * @param listener 用于管理选择模式的监听器。
     * @see #setChoiceMode(int)
     */
    public void setMultiChoiceModeListener(MultiChoiceModeListener listener) {
        if (mMultiChoiceModeCallback == null) {
            mMultiChoiceModeCallback = new MultiChoiceModeWrapper();
        }
        mMultiChoiceModeCallback.setWrapped(listener);
    }

    /**
     * When smooth scrollbar is enabled, the position and size of the scrollbar thumb is computed
     * based on the number of visible pixels in the visible items. This however assumes that all
     * list items have the same height. If you use a list in which items have different heights, the
     * scrollbar will change appearance as the user scrolls through the list. To avoid this issue,
     * you need to disable this property.
     * 
     * When smooth scrollbar is disabled, the position and size of the scrollbar thumb is based
     * solely on the number of items in the adapter and the position of the visible items inside the
     * adapter. This provides a stable scrollbar as the user navigates through a list of items with
     * varying heights.
     * 
     * @param enabled Whether or not to enable smooth scrollbar.
     * 
     * @see #setSmoothScrollbarEnabled(boolean)
     * @attr ref android.R.styleable#AbsListView_smoothScrollbar
     */
    /**
     * 当平滑滚动启用时，滚动条把手的位置和大小基于可见条目的可见像素数来计算。该处里假定所有列表条目具有相同的高度。如果你使用条目高度不同的类表，滚动条会在用户滚动过程中改变大小。
     * 为了避免这种情况，应该禁用该特性。当平滑滚动被禁用后，滚动条把手的大小和位置只是基于适配器中的条目数，以及适配器中的可见条目来确定。这样可以为使用可变高条目列表的用户，提供稳定的滚动条。
     * 
     * @param enabled 是否启用平滑滚动。
     */
    public void setSmoothScrollbarEnabled(boolean enabled) {
        mSmoothScrollbarEnabled = enabled;
    }

    /**
     * Returns the current state of the fast scroll feature.
     * 
     * @return True if smooth scrollbar is enabled is enabled, false otherwise.
     * 
     * @see #setSmoothScrollbarEnabled(boolean)
     */
    /**
     * 返回平滑滚动特性的当前状态。
     * 
     * @return 如果平滑滚动启用返回true，否则返回false。
     * @see #setSmoothScrollbarEnabled(boolean)
     */
    public boolean isSmoothScrollbarEnabled() {
        return mSmoothScrollbarEnabled;
    }

    /**
     * Set the listener that will receive notifications every time the list scrolls.
     * 
     * @param l the scroll listener
     */
    /**
     * 设置每次列表滚动时收到消息的监听器。
     * 
     * @param l 滚动监听器。
     */
    public void setOnScrollListener(OnScrollListener l) {
        if (HardwareList.IsCircularScreen()) {
            mOnScrollListener = new RoundListOnScrollListener(l, this);
        } else {
            mOnScrollListener = l;
        }
        invokeOnItemScrollListener();
    }

    /**
     * Notify our scroll listener (if there is one) of a change in scroll state
     */
    void invokeOnItemScrollListener() {
        // if (mFastScroller != null) {
        // mFastScroller.onScroll(this, mFirstPosition, getChildCount(), mItemCount);
        // }
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(this, mFirstPosition, getChildCount(), mItemCount);
        }

        onScrollChanged(0, 0, 0, 0); // dummy values, View's implementation does not use these.
    }

    /**
     * 继承自{@link View#sendAccessibilityEvent(int)}
     */
    @Override
    public void sendAccessibilityEvent(int eventType) {
        // Since this class calls onScrollChanged even if the mFirstPosition and the
        // child count have not changed we will avoid sending duplicate accessibility
        // events.
        if (eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            final int firstVisiblePosition = getFirstVisiblePosition();
            final int lastVisiblePosition = getLastVisiblePosition();
            if (mLastAccessibilityScrollEventFromIndex == firstVisiblePosition
                    && mLastAccessibilityScrollEventToIndex == lastVisiblePosition) {
                return;
            } else {
                mLastAccessibilityScrollEventFromIndex = firstVisiblePosition;
                mLastAccessibilityScrollEventToIndex = lastVisiblePosition;
            }
        }
        super.sendAccessibilityEvent(eventType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AbsListView.class.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AbsListView.class.getName());
        if (isEnabled()) {
            if (getFirstVisiblePosition() > 0) {
                info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                info.setScrollable(true);
            }
            if (getLastVisiblePosition() < getCount() - 1) {
                info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                info.setScrollable(true);
            }
        }
    }

    /**
     * 继承自{@link View#performAccessibilityAction(int, Bundle)}
     */
    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (super.performAccessibilityAction(action, arguments)) {
            return true;
        }
        switch (action) {
        case AccessibilityNodeInfo.ACTION_SCROLL_FORWARD: {
            if (isEnabled() && getLastVisiblePosition() < getCount() - 1) {
                final int viewportHeight = getHeight() - mListPadding.top - mListPadding.bottom;
                smoothScrollBy(viewportHeight, PositionScroller.SCROLL_DURATION);
                return true;
            }
        }
            return false;
        case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD: {
            if (isEnabled() && mFirstPosition > 0) {
                final int viewportHeight = getHeight() - mListPadding.top - mListPadding.bottom;
                smoothScrollBy(-viewportHeight, PositionScroller.SCROLL_DURATION);
                return true;
            }
        }
            return false;
        }
        return false;
    }

    /**
     * 继承自{@link View#getFocusedRect(Rect)}
     */
    @Override
    public void getFocusedRect(Rect r) {
        View view = getSelectedView();
        if (view != null && view.getParent() == this) {
            // the focused rectangle of the selected view offset into the
            // coordinate space of this view.
            view.getFocusedRect(r);
            offsetDescendantRectToMyCoords(view, r);
        } else {
            // otherwise, just the norm
            super.getFocusedRect(r);
        }
    }

    /**
     * Indicates whether the content of this view is pinned to, or stacked from, the bottom edge.
     * 
     * @return true if the content is stacked from the bottom edge, false otherwise
     */
    /**
     * 指示该视图的内容是否固定于、或从底边开始堆砌。
     * 
     * @return 如果内容从底边开始堆砌，返回真；否则返回假。
     */
    public boolean isStackFromBottom() {
        return mStackFromBottom;
    }

    /**
     * When stack from bottom is set to true, the list fills its content starting from the bottom of
     * the view.
     * 
     * @param stackFromBottom true to pin the view's content to the bottom edge, false to pin the
     *        view's content to the top edge
     */
    /**
     * 当栈从底部开始设置为真时，列表从底部开始向上填充视图。
     * 
     * @param stackFromBottom 为true时，视图内容固定于底部；为false时，固定在顶部。
     */
    public void setStackFromBottom(boolean stackFromBottom) {
        if (mStackFromBottom != stackFromBottom) {
            mStackFromBottom = stackFromBottom;
            requestLayoutIfNecessary();
        }
    }

    void requestLayoutIfNecessary() {
        if (getChildCount() > 0) {
            resetList();
            requestLayout();
            invalidate();
        }
    }

    /**
     * The list is empty. Clear everything out.
     */
    void resetList() {
        removeAllViewsInLayout();
        mFirstPosition = 0;
        mDataChanged = false;
        mPositionScrollAfterLayout = null;
        mNeedSync = false;
        mPendingSync = null;
        mOldSelectedPosition = INVALID_POSITION;
        mOldSelectedRowId = INVALID_ROW_ID;
        setSelectedPositionInt(INVALID_POSITION);
        setNextSelectedPositionInt(INVALID_POSITION);
        mSelectedTop = 0;
        mSelectorPosition = INVALID_POSITION;
        mSelectorRect.setEmpty();
        invalidate();
    }

    /**
     * 继承自{@link View#onFocusChanged(boolean, int, Rect)}
     */
    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus && mSelectedPosition < 0 && !isInTouchMode()) {
            if (!mIsAttached && mAdapter != null) {
                // Data may have changed while we were detached and it's valid
                // to change focus while detached. Refresh so we don't die.
                mDataChanged = true;
                mOldItemCount = mItemCount;
                mItemCount = mAdapter.getCount();
            }
            resurrectSelection();
        }
    }

    /**
     * Attempt to bring the selection back if the user is switching from touch to trackball mode
     * 
     * @return Whether selection was set to something.
     */
    boolean resurrectSelection() {
        final int childCount = getChildCount();

        if (childCount <= 0) {
            return false;
        }

        int selectedTop = 0;
        int selectedPos;
        int childrenTop = mListPadding.top;
        int childrenBottom = getBottom() - getTop() - mListPadding.bottom;
        final int firstPosition = mFirstPosition;
        final int toPosition = mResurrectToPosition;
        boolean down = true;

        if (toPosition >= firstPosition && toPosition < firstPosition + childCount) {
            selectedPos = toPosition;

            final View selected = getChildAt(selectedPos - mFirstPosition);
            selectedTop = selected.getTop();
            int selectedBottom = selected.getBottom();

            // We are scrolled, don't get in the fade
            if (selectedTop < childrenTop) {
                selectedTop = childrenTop + getVerticalFadingEdgeLength();
            } else if (selectedBottom > childrenBottom) {
                selectedTop =
                        childrenBottom - selected.getMeasuredHeight()
                                - getVerticalFadingEdgeLength();
            }
        } else {
            if (toPosition < firstPosition) {
                // Default to selecting whatever is first
                selectedPos = firstPosition;
                for (int i = 0; i < childCount; i++) {
                    final View v = getChildAt(i);
                    final int top = v.getTop();

                    if (i == 0) {
                        // Remember the position of the first item
                        selectedTop = top;
                        // See if we are scrolled at all
                        if (firstPosition > 0 || top < childrenTop) {
                            // If we are scrolled, don't select anything that is
                            // in the fade region
                            childrenTop += getVerticalFadingEdgeLength();
                        }
                    }
                    if (top >= childrenTop) {
                        // Found a view whose top is fully visisble
                        selectedPos = firstPosition + i;
                        selectedTop = top;
                        break;
                    }
                }
            } else {
                final int itemCount = mItemCount;
                down = false;
                selectedPos = firstPosition + childCount - 1;

                for (int i = childCount - 1; i >= 0; i--) {
                    final View v = getChildAt(i);
                    final int top = v.getTop();
                    final int bottom = v.getBottom();

                    if (i == childCount - 1) {
                        selectedTop = top;
                        if (firstPosition + childCount < itemCount || bottom > childrenBottom) {
                            childrenBottom -= getVerticalFadingEdgeLength();
                        }
                    }

                    if (bottom <= childrenBottom) {
                        selectedPos = firstPosition + i;
                        selectedTop = top;
                        break;
                    }
                }
            }
        }

        mResurrectToPosition = INVALID_POSITION;
        removeCallbacks(mFlingRunnable);
        if (mPositionScroller != null) {
            mPositionScroller.stop();
        }
        mTouchMode = TOUCH_MODE_REST;
        clearScrollingCache();
        mSpecificTop = selectedTop;
        selectedPos = lookForSelectablePosition(selectedPos, down);
        if (selectedPos >= firstPosition && selectedPos <= getLastVisiblePosition()) {
            mLayoutMode = LAYOUT_SPECIFIC;
            updateSelectorState();
            setSelectionInt(selectedPos);
            invokeOnItemScrollListener();
        } else {
            selectedPos = INVALID_POSITION;
        }
        reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);

        return selectedPos >= 0;
    }

    void updateSelectorState() {
        if (mSelector != null) {
            if (shouldShowSelector()) {
                mSelector.setState(getDrawableState());
            } else {
                mSelector.setState(StateSet.NOTHING);
            }
        }
    }

    /**
     * Indicates whether this view is in a state where the selector should be drawn. This will
     * happen if we have focus but are not in touch mode, or we are in the middle of displaying the
     * pressed state for an item.
     * 
     * @return True if the selector should be shown
     */
    boolean shouldShowSelector() {
        return (hasFocus() && !isInTouchMode()) || touchModeDrawsInPressedState();
    }

    /**
     * @return True if the current touch mode requires that we draw the selector in the pressed
     *         state.
     */
    boolean touchModeDrawsInPressedState() {
        // FIXME use isPressed for this
        switch (mTouchMode) {
        case TOUCH_MODE_TAP:
        case TOUCH_MODE_DONE_WAITING:
            return true;
        default:
            return false;
        }
    }

    /**
     * 继承自{@link View#requestLayout()}
     */
    @Override
    public void requestLayout() {
        if (!mBlockLayoutRequests && !mInLayout) {
            super.requestLayout();
        }
    }

    /**
     * 垂直方向的滚动条滑块在滚动条滑道中的长度。
     * 
     * @see #computeVerticalScrollOffset()
     * @see #computeVerticalScrollRange()
     * @return 滑块的在滑道中的长度。
     */
    @Override
    protected int computeVerticalScrollExtent() {
        final int count = getChildCount();
        if (count > 0) {
            if (mSmoothScrollbarEnabled) {
                int extent = count * 100;

                View view = getChildAt(0);
                final int top = view.getTop();
                int height = view.getHeight();
                if (height > 0) {
                    extent += (top * 100) / height;
                }

                view = getChildAt(count - 1);
                final int bottom = view.getBottom();
                height = view.getHeight();
                if (height > 0) {
                    extent -= ((bottom - getHeight()) * 100) / height;
                }

                return extent;
            } else {
                return 1;
            }
        }
        return 0;
    }

    /**
     * 计算垂直方向滚动条的滑块的偏移。此值用来计算滚动条轨迹的滑块的位置。
     * 
     * @see #computeVerticalScrollExtent()
     * @see #computeVerticalScrollRange()
     * @return 滚动条的滑块垂直方向的偏移。
     */
    @Override
    protected int computeVerticalScrollOffset() {
        final int firstPosition = mFirstPosition;
        final int childCount = getChildCount();
        if (firstPosition >= 0 && childCount > 0) {
            if (mSmoothScrollbarEnabled) {
                final View view = getChildAt(0);
                final int top = view.getTop();
                int height = view.getHeight();
                if (height > 0) {
                    return Math.max(firstPosition * 100 - (top * 100) / height
                            + (int) ((float) getScrollY() / getHeight() * mItemCount * 100), 0);
                }
            } else {
                int index;
                final int count = mItemCount;
                if (firstPosition == 0) {
                    index = 0;
                } else if (firstPosition + childCount == count) {
                    index = count;
                } else {
                    index = firstPosition + childCount / 2;
                }
                return (int) (firstPosition + childCount * (index / (float) count));
            }
        }
        return 0;
    }

    /**
     * 计算垂直方向可滚动范围。
     * 
     * @see #computeHorizontalScrollExtent()
     * @see AbsListView#computeHorizontalScrollOffset()
     * @return 可滚动范围。
     */
    @Override
    protected int computeVerticalScrollRange() {
        int result;
        if (mSmoothScrollbarEnabled) {
            result = Math.max(mItemCount * 100, 0);
            int scrollY = getScrollY();
            if (scrollY != 0) {
                // Compensate for overscroll
                result += Math.abs((int) ((float) scrollY / getHeight() * mItemCount * 100));
            }
        } else {
            result = mItemCount;
        }
        return result;
    }

    /**
     * 继承自{@link View#getTopFadingEdgeStrength()}
     */
    @Override
    protected float getTopFadingEdgeStrength() {
        final int count = getChildCount();
        final float fadeEdge = super.getTopFadingEdgeStrength();
        if (count == 0) {
            return fadeEdge;
        } else {
            if (mFirstPosition > 0) {
                return 1.0f;
            }

            final int top = getChildAt(0).getTop();
            final float fadeLength = (float) getVerticalFadingEdgeLength();
            int paddingTop = getPaddingTop();
            return top < paddingTop ? (float) -(top - paddingTop) / fadeLength : fadeEdge;
        }
    }

    /**
     * 继承自{@link View#getBottomFadingEdgeStrength()}
     */
    @Override
    protected float getBottomFadingEdgeStrength() {
        final int count = getChildCount();
        final float fadeEdge = super.getBottomFadingEdgeStrength();
        if (count == 0) {
            return fadeEdge;
        } else {
            if (mFirstPosition + count - 1 < mItemCount - 1) {
                return 1.0f;
            }

            final int bottom = getChildAt(count - 1).getBottom();
            final int height = getHeight();
            final float fadeLength = (float) getVerticalFadingEdgeLength();
            int paddingBottom = getPaddingBottom();
            return bottom > height - paddingBottom ? (float) (bottom - height + paddingBottom)
                    / fadeLength : fadeEdge;
        }
    }

    /**
     * 继承自{@link View#onMeasure(int, int)}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mSelector == null) {
            useDefaultSelector();
        }
        final Rect listPadding = mListPadding;
        listPadding.left = mSelectionLeftPadding + getPaddingLeft();
        listPadding.top = mSelectionTopPadding + getPaddingTop();
        listPadding.right = mSelectionRightPadding + getPaddingRight();
        listPadding.bottom = mSelectionBottomPadding + getPaddingBottom();

        // Check if our previous measured size was at a point where we should scroll later.
        if (mTranscriptMode == TRANSCRIPT_MODE_NORMAL) {
            final int childCount = getChildCount();
            final int listBottom = getHeight() - getPaddingBottom();
            final View lastChild = getChildAt(childCount - 1);
            final int lastBottom = lastChild != null ? lastChild.getBottom() : listBottom;
            mForceTranscriptScroll =
                    mFirstPosition + childCount >= mLastHandledItemCount
                            && lastBottom <= listBottom;
        }
    }

    private void useDefaultSelector() {
        setSelector(getResources().getDrawable(android.R.drawable.list_selector_background));
    }

    /**
     * Set a Drawable that should be used to highlight the currently selected item.
     * 
     * @param resID A Drawable resource to use as the selection highlight.
     * 
     * @attr ref android.R.styleable#AbsListView_listSelector
     */
    /**
     * 设置用于将当前选择条目设置为高亮的可绘制对象。
     * 
     * @param resID 作为选择高亮的可绘制对象资源。
     * @see #setSelector(Drawable)
     * @see #getSelector()
     */
    public void setSelector(int resID) {
        setSelector(getResources().getDrawable(resID));
    }

    /**
     * 设置用于将当前选择条目设置为高亮的可绘制对象。
     * 
     * @param sel 作为选择高亮的可绘制对象。
     * @see #setSelector(int)
     * @see #getSelector()
     */
    public void setSelector(Drawable sel) {
        if (mSelector != null) {
            mSelector.setCallback(null);
            unscheduleDrawable(mSelector);
        }
        mSelector = sel;
        Rect padding = new Rect();
        sel.getPadding(padding);
        mSelectionLeftPadding = padding.left;
        mSelectionTopPadding = padding.top;
        mSelectionRightPadding = padding.right;
        mSelectionBottomPadding = padding.bottom;
        sel.setCallback(this);
        updateSelectorState();
    }

    /**
     * Returns the selector {@link android.graphics.drawable.Drawable} that is used to draw the
     * selection in the list.
     * 
     * @return the drawable used to display the selector
     */
    /**
     * 返回用于在列表中绘制选择器的{@link android.graphics.drawable.Drawable}。
     * 
     * @return 用于显示选择器的可绘制对象。
     */
    public Drawable getSelector() {
        return mSelector;
    }

    /**
     * Sets the selector state to "pressed" and posts a CheckForKeyLongPress to see if this is a
     * long press.
     */
    void keyPressed() {
        if (!isEnabled() || !isClickable()) {
            return;
        }

        Drawable selector = mSelector;
        Rect selectorRect = mSelectorRect;
        if (selector != null && (isFocused() || touchModeDrawsInPressedState())
                && !selectorRect.isEmpty()) {

            final View v = getChildAt(mSelectedPosition - mFirstPosition);

            if (v != null) {
                if (v.hasFocusable()) return;
                v.setPressed(true);
            }
            setPressed(true);

            final boolean longClickable = isLongClickable();
            Drawable d = selector.getCurrent();
            if (d != null && d instanceof TransitionDrawable) {
                if (longClickable) {
                    ((TransitionDrawable) d).startTransition(ViewConfiguration
                            .getLongPressTimeout());
                } else {
                    ((TransitionDrawable) d).resetTransition();
                }
            }
            if (longClickable && !mDataChanged) {
                if (mPendingCheckForKeyLongPress == null) {
                    mPendingCheckForKeyLongPress = new CheckForKeyLongPress();
                }
                mPendingCheckForKeyLongPress.rememberWindowAttachCount();
                postDelayed(mPendingCheckForKeyLongPress, ViewConfiguration.getLongPressTimeout());
            }
        }
    }

    /**
     * Subclasses should NOT override this method but {@link #layoutChildren()} instead.
     */
    /**
     * 子类不能重写该方法，重写{@link #layoutChildren()}代替
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mInLayout = true;
        if (changed) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                getChildAt(i).forceLayout();
            }
            mRecycler.markChildrenDirty();
        }

        // if (mFastScroller != null && mItemCount != mOldItemCount) {
        // mFastScroller.onItemCountChanged(mOldItemCount, mItemCount);
        // }

        layoutChildren();
        mInLayout = false;

        mOverscrollMax = (b - t) / OVERSCROLL_LIMIT_DIVISOR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getSelectedView() {
        if (mItemCount > 0 && mSelectedPosition >= 0) {
            return getChildAt(mSelectedPosition - mFirstPosition);
        } else {
            return null;
        }
    }

    /**
     * List padding is the maximum of the normal view's padding and the padding of the selector.
     * 
     * @see View#getPaddingTop()
     * @see #getSelector()
     * 
     * @return The top list padding.
     */
    /**
     * 列表内边距是普通视图内边距和选择器内边距的最大值。
     * 
     * @see View#getPaddingTop()
     * @see #getSelector()
     * @return 列表顶部的内边距。
     */
    public int getListPaddingTop() {
        return mListPadding.top;
    }

    /**
     * List padding is the maximum of the normal view's padding and the padding of the selector.
     * 
     * @see View#getPaddingBottom()
     * @see #getSelector()
     * 
     * @return The bottom list padding.
     */
    /**
     * 列表内边距是普通视图内边距和选择器内边距的最大值。
     * 
     * @see View#getPaddingBottom()
     * @see #getSelector()
     * @return 列表底部的内边距。
     */
    public int getListPaddingBottom() {
        return mListPadding.bottom;
    }

    /**
     * List padding is the maximum of the normal view's padding and the padding of the selector.
     * 
     * @see View#getPaddingLeft()
     * @see #getSelector()
     * 
     * @return The left list padding.
     */
    /**
     * 列表内边距是普通视图内边距和选择器内边距的最大值。
     * 
     * @see android.view.View#getPaddingLeft()
     * @see #getSelector()
     * @return 列表左侧的内边距。
     */
    public int getListPaddingLeft() {
        return mListPadding.left;
    }

    /**
     * List padding is the maximum of the normal view's padding and the padding of the selector.
     * 
     * @see android.view.View#getPaddingRight()
     * @see #getSelector()
     * 
     * @return The right list padding.
     */
    /**
     * 列表内边距是普通视图内边距和选择器内边距的最大值。
     * 
     * @see View#getPaddingRight()
     * @see #getSelector()
     * @return 列表右侧的内边距。
     */
    public int getListPaddingRight() {
        return mListPadding.right;
    }

    /**
     * Get a view and have it show the data associated with the specified position. This is called
     * when we have already discovered that the view is not available for reuse in the recycle bin.
     * The only choices left are converting an old view or making a new one.
     * 
     * @param position The position to display
     * @param isScrap Array of at least 1 boolean, the first entry will become true if the returned
     *        view was taken from the scrap heap, false if otherwise.
     * 
     * @return A view displaying the data associated with the specified position
     */
    View obtainView(int position, boolean[] isScrap) {
        isScrap[0] = false;
        View scrapView;

        scrapView = mRecycler.getTransientStateView(position);
        if (scrapView != null) {
            return scrapView;
        }

        scrapView = mRecycler.getScrapView(position);

        View child;
        if (scrapView != null) {
            child = mAdapter.getView(position, scrapView, this);

            if (child.getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
                child.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
            }

            if (child != scrapView) {
                mRecycler.addScrapView(scrapView, position);
                if (mCacheColorHint != 0) {
                    child.setDrawingCacheBackgroundColor(mCacheColorHint);
                }
            } else {
                isScrap[0] = true;
                dispatchFinishTemporaryDetachOfHide(child);// child.dispatchFinishTemporaryDetach();
            }
        } else {
            child = mAdapter.getView(position, null, this);

            if (child.getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
                child.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
            }

            if (mCacheColorHint != 0) {
                child.setDrawingCacheBackgroundColor(mCacheColorHint);
            }
        }

        if (mAdapterHasStableIds) {
            final ViewGroup.LayoutParams vlp = child.getLayoutParams();
            LayoutParams lp;
            if (vlp == null) {
                lp = (LayoutParams) generateDefaultLayoutParams();
            } else if (!checkLayoutParams(vlp)) {
                lp = (LayoutParams) generateLayoutParams(vlp);
            } else {
                lp = (LayoutParams) vlp;
            }
            lp.itemId = mAdapter.getItemId(position);
            child.setLayoutParams(lp);
        }

        AccessibilityManager manager = getAccessibilityManager();

        if (manager != null && manager.isEnabled()) {// (AccessibilityManager.getInstance(context)).isEnabled()
            if (mAccessibilityDelegate == null) {
                mAccessibilityDelegate = new ListItemAccessibilityDelegate();
            }
            if (getAccessibilityDelegateOfHide(child) == null) {// child.getAccessibilityDelegate();
                child.setAccessibilityDelegate(mAccessibilityDelegate);
            }
        }

        return child;
    }

    void dispatchFinishTemporaryDetachOfHide(View view) {
        Class<?> cls = view.getClass();
        try {
            Method method = cls.getMethod("dispatchFinishTemporaryDetach");
            method.setAccessible(true);
            method.invoke(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    AccessibilityManager getAccessibilityManager() {
        Class<?> cls = AccessibilityManager.class;
        try {
            Method method = cls.getMethod("getInstance", Context.class);
            method.setAccessible(true);
            return (AccessibilityManager) method.invoke(cls, getContext());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    AccessibilityDelegate getAccessibilityDelegateOfHide(View view) {
        Class<?> cls = view.getClass();
        try {
            Method method = cls.getDeclaredMethod("getAccessibilityDelegate");
            method.setAccessible(true);
            return (AccessibilityDelegate) method.invoke(view);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 画图的时候调用此方法来画出子视图。在其子视图被画出来之前（在其视图被画出来之后）可能由其派生类控制。
     * 
     * @param canvas 需要画出的视图上的画布。
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        int saveCount = 0;
        final boolean clipToPadding =
                (getGroupFlagsOfNotPublic() & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK;
        if (clipToPadding) {
            saveCount = canvas.save();
            final int scrollX = getScrollY();
            final int scrollY = getScrollY();
            canvas.clipRect(scrollX + getLeft(), scrollY + getTop(), scrollX + getRight()
                    - getLeft() - getPaddingRight(), scrollY + getBottom() - getTop()
                    - getPaddingBottom());
            int groupFlags = getGroupFlagsOfNotPublic();
            groupFlags &= ~CLIP_TO_PADDING_MASK;
            setGroupFlagsOfNotPublic(groupFlags);
            // mGroupFlags &= ~CLIP_TO_PADDING_MASK;
        }

        final boolean drawSelectorOnTop = mDrawSelectorOnTop;
        if (!drawSelectorOnTop) {
            drawSelector(canvas);
        }

        super.dispatchDraw(canvas);

        if (drawSelectorOnTop) {
            drawSelector(canvas);
        }

        if (clipToPadding) {
            canvas.restoreToCount(saveCount);
            int groupFlags = getGroupFlagsOfNotPublic();
            groupFlags |= CLIP_TO_PADDING_MASK;
            setGroupFlagsOfNotPublic(groupFlags);
            // mGroupFlags |= CLIP_TO_PADDING_MASK;
        }
    }

    private void drawSelector(Canvas canvas) {
        if (!mSelectorRect.isEmpty()) {
            final Drawable selector = mSelector;
            selector.setBounds(mSelectorRect);
            selector.draw(canvas);
        }
    }

    private void setGroupFlagsOfNotPublic(int groupFlags) {
        Class<?> cls = getClass();
        try {
            Field field = cls.getDeclaredField("mGroupFlags");
            field.setAccessible(true);
            field.setInt(this, groupFlags);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 继承自{@link View#isPaddingOffsetRequired()}
     */
    @Override
    protected boolean isPaddingOffsetRequired() {
        return (getGroupFlagsOfNotPublic() & CLIP_TO_PADDING_MASK) != CLIP_TO_PADDING_MASK;
    }

    /**
     * 继承自{@link View#getLeftPaddingOffset()}
     */
    @Override
    protected int getLeftPaddingOffset() {
        return (getGroupFlagsOfNotPublic() & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK ? 0
                : -getPaddingLeft();
    }

    /**
     * 继承自{@link View#getTopPaddingOffset()}
     */
    @Override
    protected int getTopPaddingOffset() {
        return (getGroupFlagsOfNotPublic() & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK ? 0
                : -getPaddingTop();
    }

    /**
     * 继承自{@link View#getRightPaddingOffset()}
     */
    @Override
    protected int getRightPaddingOffset() {
        return (getGroupFlagsOfNotPublic() & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK ? 0
                : getPaddingRight();
    }

    /**
     * 继承自{@link View#getBottomPaddingOffset()}
     */
    @Override
    protected int getBottomPaddingOffset() {
        return (getGroupFlagsOfNotPublic() & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK ? 0
                : getPaddingBottom();
    }

    /**
     * 继承自{@link View#onSizeChanged(int, int, int, int)}
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (getChildCount() > 0) {
            mDataChanged = true;
            rememberSyncState();
        }

        // if (mFastScroller != null) {
        // mFastScroller.onSizeChanged(w, h, oldw, oldh);
        // }
    }

    /**
     * Controls whether the selection highlight drawable should be drawn on top of the item or
     * behind it.
     * 
     * @param onTop If true, the selector will be drawn on the item it is highlighting. The default
     *        is false.
     * 
     * @attr ref android.R.styleable#AbsListView_drawSelectorOnTop
     */
    /**
     * 控制选择高亮可绘制对象应该在条目的前面绘制还是在后面绘制。
     * 
     * @param onTop 如果为true，选择器的高亮在条目上面显示。默认值为false。
     */
    public void setDrawSelectorOnTop(boolean onTop) {
        mDrawSelectorOnTop = onTop;
    }

    /**
     * 设置滚动指标
     * 
     * @param up 顶部指标
     * @param down 底部指标
     */
    public void setScrollIndicators(View up, View down) {
        mScrollUp = up;
        mScrollDown = down;
    }

    /**
     * 继承自{@link ViewGroup#drawableStateChanged()}
     */
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateSelectorState();
    }

    /**
     * 继承自{@link ViewGroup#onCreateDrawableState(int)}
     */
    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        // If the child view is enabled then do the default behavior.
        if (mIsChildViewEnabled) {
            // Common case
            return super.onCreateDrawableState(extraSpace);
        }

        // The selector uses this View's drawable state. The selected child view
        // is disabled, so we need to remove the enabled state from the drawable
        // states.
        final int enabledState = ENABLED_STATE_SET[0];

        // If we don't have any extra space, it will return one of the static state arrays,
        // and clearing the enabled state on those arrays is a bad thing! If we specify
        // we need extra space, it will create+copy into a new array that safely mutable.
        int[] state = super.onCreateDrawableState(extraSpace + 1);
        int enabledPos = -1;
        for (int i = state.length - 1; i >= 0; i--) {
            if (state[i] == enabledState) {
                enabledPos = i;
                break;
            }
        }

        // Remove the enabled state
        if (enabledPos >= 0) {
            System.arraycopy(state, enabledPos + 1, state, enabledPos, state.length - enabledPos
                    - 1);
        }

        return state;
    }

    /**
     * 继承自{@link View#verifyDrawable(Drawable)}
     */
    @Override
    public boolean verifyDrawable(Drawable dr) {
        return mSelector == dr || super.verifyDrawable(dr);
    }

    /**
     * 继承自{@link ViewGroup#jumpDrawablesToCurrentState()}
     */
    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mSelector != null) mSelector.jumpToCurrentState();
    }

    /**
     * 继承自{@link View#onAttachedToWindow()}
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        final ViewTreeObserver treeObserver = getViewTreeObserver();
        treeObserver.addOnTouchModeChangeListener(this);
        // if (mTextFilterEnabled && mPopup != null && !mGlobalLayoutListenerAddedFilter) {
        // treeObserver.addOnGlobalLayoutListener(this);
        // }

        if (mAdapter != null && mDataSetObserver == null) {
            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);

            // Data may have changed while we were detached. Refresh.
            mDataChanged = true;
            mOldItemCount = mItemCount;
            mItemCount = mAdapter.getCount();
        }
        mIsAttached = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // Dismiss the popup in case onSaveInstanceState() was not invoked
        // dismissPopup();

        // Detach any view left in the scrap heap
        mRecycler.clear();

        final ViewTreeObserver treeObserver = getViewTreeObserver();
        treeObserver.removeOnTouchModeChangeListener(this);
        // if (mTextFilterEnabled && mPopup != null) {
        // treeObserver.removeOnGlobalLayoutListener(this);
        // mGlobalLayoutListenerAddedFilter = false;
        // }

        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
            mDataSetObserver = null;
        }

        // if (mScrollStrictSpan != null) {
        // mScrollStrictSpan.finish();
        // mScrollStrictSpan = null;
        // }
        //
        // if (mFlingStrictSpan != null) {
        // mFlingStrictSpan.finish();
        // mFlingStrictSpan = null;
        // }

        if (mFlingRunnable != null) {
            removeCallbacks(mFlingRunnable);
        }

        if (mPositionScroller != null) {
            mPositionScroller.stop();
        }

        if (mClearScrollingCache != null) {
            removeCallbacks(mClearScrollingCache);
        }

        if (mPerformClick != null) {
            removeCallbacks(mPerformClick);
        }

        if (mTouchModeReset != null) {
            removeCallbacks(mTouchModeReset);
            mTouchModeReset.run();
        }
        mIsAttached = false;
    }

    /**
     * 继承自{@link View#onWindowFocusChanged(boolean)}
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        final int touchMode = isInTouchMode() ? TOUCH_MODE_ON : TOUCH_MODE_OFF;

        if (!hasWindowFocus) {
            setChildrenDrawingCacheEnabled(false);
            if (mFlingRunnable != null) {
                removeCallbacks(mFlingRunnable);
                // let the fling runnable report it's new state which
                // should be idle
                mFlingRunnable.endFling();
                if (mPositionScroller != null) {
                    mPositionScroller.stop();
                }
                if (getScrollY() != 0) {
                    setScrollY(0);
                    invalidateParentCachesOfHide();// invalidateParentCaches();
                    finishGlows();
                    invalidate();
                }
            }
            // Always hide the type filter
            // dismissPopup();

            if (touchMode == TOUCH_MODE_OFF) {
                // Remember the last selected element
                mResurrectToPosition = mSelectedPosition;
            }
        } else {
            // if (mFiltered && !mPopupHidden) {
            // // Show the type filter only if a filter is in effect
            // showPopup();
            // }

            // If we changed touch mode since the last time we had focus
            if (touchMode != mLastTouchMode && mLastTouchMode != TOUCH_MODE_UNKNOWN) {
                // If we come back in trackball mode, we bring the selection back
                if (touchMode == TOUCH_MODE_OFF) {
                    // This will trigger a layout
                    resurrectSelection();

                    // If we come back in touch mode, then we want to hide the selector
                } else {
                    hideSelector();
                    mLayoutMode = LAYOUT_NORMAL;
                    layoutChildren();
                }
            }
        }

        mLastTouchMode = touchMode;
    }

    private void invalidateParentCachesOfHide() {
        try {
            Class<?> cls = Class.forName("android.view.View");
            Method method = cls.getDeclaredMethod("invalidateParentCaches");
            method.setAccessible(true);
            method.invoke(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finishGlows() {
        // if (mEdgeGlowTop != null) {
        // mEdgeGlowTop.finish();
        // mEdgeGlowBottom.finish();
        // }
    }

    boolean performLongPress(final View child, final int longPressPosition, final long longPressId) {
        // CHOICE_MODE_MULTIPLE_MODAL takes over long press.
        if (mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
            if (mChoiceActionMode == null
                    && (mChoiceActionMode = startActionMode(mMultiChoiceModeCallback)) != null) {
                setItemChecked(longPressPosition, true);
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
            return true;
        }

        boolean handled = false;
        if (mOnItemLongClickListener != null) {
            handled =
                    mOnItemLongClickListener.onItemLongClick(AbsListView.this, child,
                            longPressPosition, longPressId);
        }
        if (!handled) {
            // mContextMenuInfo = createContextMenuInfo(child, longPressPosition, longPressId);
            handled = super.showContextMenuForChild(AbsListView.this);
        }
        if (handled) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
        return handled;
    }

    /**
     * 继承自{@link View#onKeyDown(int, KeyEvent)}
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    /**
     * 继承自{@link View#onKeyUp(int, KeyEvent)}
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_CENTER:
        case KeyEvent.KEYCODE_ENTER:
            if (!isEnabled()) {
                return true;
            }
            if (isClickable() && isPressed() && mSelectedPosition >= 0 && mAdapter != null
                    && mSelectedPosition < mAdapter.getCount()) {

                final View view = getChildAt(mSelectedPosition - mFirstPosition);
                if (view != null) {
                    performItemClick(view, mSelectedPosition, mSelectedRowId);
                    view.setPressed(false);
                }
                setPressed(false);
                return true;
            }
            break;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 调用所有子视图的{@link View#setPressed(boolean)}方法。
     * 
     * @param pressed 是否按压状态。
     */
    @Override
    protected void dispatchSetPressed(boolean pressed) {
        // Don't dispatch setPressed to our children. We call setPressed on ourselves to
        // get the selector in the right state, but we don't want to press each child.
    }

    /**
     * Maps a point to a position in the list.
     * 
     * @param x X in local coordinate
     * @param y Y in local coordinate
     * @return The position of the item which contains the specified point, or
     *         {@link #INVALID_POSITION} if the point does not intersect an item.
     */
    /**
     * 将坐标点转换为列表中的位置。
     * 
     * @param x 本地坐标系的 X。
     * @param y 本地坐标系的 Y。
     * @return 包含指定点的条目的位置，如果点不再任何条目上返回{@link #INVALID_POSITION}。
     */
    public int pointToPosition(int x, int y) {
        Rect frame = mTouchFrame;
        if (frame == null) {
            mTouchFrame = new Rect();
            frame = mTouchFrame;
        }

        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return mFirstPosition + i;
                }
            }
        }
        return INVALID_POSITION;
    }

    /**
     * Maps a point to a the rowId of the item which intersects that point.
     * 
     * @param x X in local coordinate
     * @param y Y in local coordinate
     * @return The rowId of the item which contains the specified point, or {@link #INVALID_ROW_ID}
     *         if the point does not intersect an item.
     */
    /**
     * 将坐标点转换为列表条目的行ID。
     * 
     * @param x 本地坐标系的 X。
     * @param y 本地坐标系的 Y。
     * @return 包含指定点的条目的条目的行ID；如果点不再任何条目上返回{@link #INVALID_ROW_ID}。
     */
    public long pointToRowId(int x, int y) {
        int position = pointToPosition(x, y);
        if (position >= 0) {
            return mAdapter.getItemId(position);
        }
        return INVALID_ROW_ID;
    }

    /**
     * 由{@link OnTouchModeChangeListener#onTouchModeChanged(boolean)}定义
     */
    @Override
    public void onTouchModeChanged(boolean isInTouchMode) {
        if (isInTouchMode) {
            // Get rid of the selection when we enter touch mode
            hideSelector();
            // Layout, but only if we already have done so previously.
            // (Otherwise may clobber a LAYOUT_SYNC layout that was requested to restore
            // state.)
            if (getHeight() > 0 && getChildCount() > 0) {
                // We do not lose focus initiating a touch (since AbsListView is focusable in
                // touch mode). Force an initial layout to get rid of the selection.
                layoutChildren();
            }
            updateSelectorState();
        } else {
            int touchMode = mTouchMode;
            if (touchMode == TOUCH_MODE_OVERSCROLL || touchMode == TOUCH_MODE_OVERFLING) {
                if (mFlingRunnable != null) {
                    mFlingRunnable.endFling();
                }
                if (mPositionScroller != null) {
                    mPositionScroller.stop();
                }

                if (getScrollY() != 0) {
                    setScrollY(0);
                    invalidateParentCachesOfHide();// invalidateParentCaches();
                    finishGlows();
                    invalidate();
                }
            }
        }
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * 继承自{@link View#onTouchEvent(MotionEvent)}
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            // A disabled view that is clickable still consumes the touch
            // events, it just doesn't respond to them.
            return isClickable() || isLongClickable();
        }

        if (mPositionScroller != null) {
            mPositionScroller.stop();
        }

        if (!mIsAttached) {
            // Something isn't right.
            // Since we rely on being attached to get data set change notifications,
            // don't risk doing anything where we might try to resync and find things
            // in a bogus state.
            return false;
        }

        // if (mFastScroller != null) {
        // boolean intercepted = mFastScroller.onTouchEvent(ev);
        // if (intercepted) {
        // return true;
        // }
        // }

        final int action = ev.getAction();

        View v;

        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(ev);

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN: {
            switch (mTouchMode) {
            case TOUCH_MODE_OVERFLING: {
                mFlingRunnable.endFling();
                if (mPositionScroller != null) {
                    mPositionScroller.stop();
                }
                mTouchMode = TOUCH_MODE_OVERSCROLL;
                mMotionX = (int) ev.getX();
                mMotionY = mLastY = (int) ev.getY();
                mMotionCorrection = 0;
                mActivePointerId = ev.getPointerId(0);
                mDirection = 0;
                break;
            }

            default: {
                mActivePointerId = ev.getPointerId(0);
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                int motionPosition = pointToPosition(x, y);
                if (!mDataChanged) {
                    if ((mTouchMode != TOUCH_MODE_FLING) && (motionPosition >= 0)
                            && (getAdapter().isEnabled(motionPosition))) {
                        // User clicked on an actual view (and was not stopping a fling).
                        // It might be a click or a scroll. Assume it is a click until
                        // proven otherwise
                        mTouchMode = TOUCH_MODE_DOWN;
                        // FIXME Debounce
                        if (mPendingCheckForTap == null) {
                            mPendingCheckForTap = new CheckForTap();
                        }
                        postDelayed(mPendingCheckForTap, ViewConfiguration.getTapTimeout());
                    } else {
                        if (mTouchMode == TOUCH_MODE_FLING) {
                            // Stopped a fling. It is a scroll.
                            createScrollingCache();
                            mTouchMode = TOUCH_MODE_SCROLL;
                            mMotionCorrection = 0;
                            motionPosition = findMotionRow(y);
                            mFlingRunnable.flywheelTouch();
                        }
                    }
                }

                if (motionPosition >= 0) {
                    // Remember where the motion event started
                    v = getChildAt(motionPosition - mFirstPosition);
                    mMotionViewOriginalTop = v.getTop();
                }
                mMotionX = x;
                mMotionY = y;
                mMotionPosition = motionPosition;
                mLastY = Integer.MIN_VALUE;
                break;
            }
            }

            if (performButtonActionOnTouchDownOfHide(ev)) {// performButtonActionOnTouchDown(ev)
                if (mTouchMode == TOUCH_MODE_DOWN) {
                    removeCallbacks(mPendingCheckForTap);
                }
            }
            break;
        }

        case MotionEvent.ACTION_MOVE: {
            int pointerIndex = ev.findPointerIndex(mActivePointerId);
            if (pointerIndex == -1) {
                pointerIndex = 0;
                mActivePointerId = ev.getPointerId(pointerIndex);
            }
            final int y = (int) ev.getY(pointerIndex);

            if (mDataChanged) {
                // Re-sync everything if data has been changed
                // since the scroll operation can query the adapter.
                layoutChildren();
            }

            switch (mTouchMode) {
            case TOUCH_MODE_DOWN:
            case TOUCH_MODE_TAP:
            case TOUCH_MODE_DONE_WAITING:
                // Check if we have moved far enough that it looks more like a
                // scroll than a tap
                startScrollIfNeeded(y);
                break;
            case TOUCH_MODE_SCROLL:
            case TOUCH_MODE_OVERSCROLL:
                scrollIfNeeded(y);
                break;
            }
            break;
        }

        case MotionEvent.ACTION_UP: {
            switch (mTouchMode) {
            case TOUCH_MODE_DOWN:
            case TOUCH_MODE_TAP:
            case TOUCH_MODE_DONE_WAITING:
                final int motionPosition = mMotionPosition;
                final View child = getChildAt(motionPosition - mFirstPosition);

                final float x = ev.getX();
                final boolean inList = x > mListPadding.left && x < getWidth() - mListPadding.right;

                if (child != null && !child.hasFocusable() && inList) {
                    if (mTouchMode != TOUCH_MODE_DOWN) {
                        child.setPressed(false);
                    }

                    if (mPerformClick == null) {
                        mPerformClick = new PerformClick();
                    }

                    final AbsListView.PerformClick performClick = mPerformClick;
                    performClick.mClickMotionPosition = motionPosition;
                    performClick.rememberWindowAttachCount();

                    mResurrectToPosition = motionPosition;

                    if (mTouchMode == TOUCH_MODE_DOWN || mTouchMode == TOUCH_MODE_TAP) {
                        final Handler handler = getHandler();
                        if (handler != null) {
                            handler.removeCallbacks(mTouchMode == TOUCH_MODE_DOWN
                                    ? mPendingCheckForTap : mPendingCheckForLongPress);
                        }
                        mLayoutMode = LAYOUT_NORMAL;
                        if (!mDataChanged && mAdapter.isEnabled(motionPosition)) {
                            mTouchMode = TOUCH_MODE_TAP;
                            setSelectedPositionInt(mMotionPosition);
                            layoutChildren();
                            child.setPressed(true);
                            positionSelector(mMotionPosition, child);
                            setPressed(true);
                            if (mSelector != null) {
                                Drawable d = mSelector.getCurrent();
                                if (d != null && d instanceof TransitionDrawable) {
                                    ((TransitionDrawable) d).resetTransition();
                                }
                            }
                            if (mTouchModeReset != null) {
                                removeCallbacks(mTouchModeReset);
                            }
                            mTouchModeReset = new Runnable() {
                                @Override
                                public void run() {
                                    mTouchModeReset = null;
                                    mTouchMode = TOUCH_MODE_REST;
                                    child.setPressed(false);
                                    setPressed(false);
                                    if (!mDataChanged) {
                                        performClick.run();
                                    }
                                }
                            };
                            postDelayed(mTouchModeReset,
                                    ViewConfiguration.getPressedStateDuration());
                        } else {
                            mTouchMode = TOUCH_MODE_REST;
                            updateSelectorState();
                        }
                        return true;
                    } else if (!mDataChanged && mAdapter.isEnabled(motionPosition)) {
                        performClick.run();
                    }
                }
                mTouchMode = TOUCH_MODE_REST;
                updateSelectorState();
                break;
            case TOUCH_MODE_SCROLL:
                final int childCount = getChildCount();
                if (childCount > 0) {
                    final int firstChildTop = getChildAt(0).getTop();
                    final int lastChildBottom = getChildAt(childCount - 1).getBottom();
                    final int contentTop = mListPadding.top;
                    final int contentBottom = getHeight() - mListPadding.bottom;
                    if (mFirstPosition == 0 && firstChildTop >= contentTop
                            && mFirstPosition + childCount < mItemCount
                            && lastChildBottom <= getHeight() - contentBottom) {
                        mTouchMode = TOUCH_MODE_REST;
                        reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                    } else {
                        final VelocityTracker velocityTracker = mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);

                        final int initialVelocity =
                                (int) (velocityTracker.getYVelocity(mActivePointerId) * mVelocityScale);
                        // Fling if we have enough velocity and we aren't at a boundary.
                        // Since we can potentially overfling more than we can overscroll, don't
                        // allow the weird behavior where you can scroll to a boundary then
                        // fling further.
                        if (Math.abs(initialVelocity) > mMinimumVelocity
                                && !((mFirstPosition == 0 && firstChildTop == contentTop
                                        - mOverscrollDistance) || (mFirstPosition + childCount == mItemCount && lastChildBottom == contentBottom
                                        + mOverscrollDistance))) {
                            if (mFlingRunnable == null) {
                                mFlingRunnable = new FlingRunnable();
                            }
                            reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);

                            mFlingRunnable.start(-initialVelocity);
                        } else {
                            mTouchMode = TOUCH_MODE_REST;
                            reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                            if (mFlingRunnable != null) {
                                mFlingRunnable.endFling();
                            }
                            if (mPositionScroller != null) {
                                mPositionScroller.stop();
                            }
                        }
                    }
                } else {
                    mTouchMode = TOUCH_MODE_REST;
                    reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                }
                break;

            case TOUCH_MODE_OVERSCROLL:
                if (mFlingRunnable == null) {
                    mFlingRunnable = new FlingRunnable();
                }
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                final int initialVelocity = (int) velocityTracker.getYVelocity(mActivePointerId);

                reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
                if (Math.abs(initialVelocity) > mMinimumVelocity) {
                    mFlingRunnable.startOverfling(-initialVelocity);
                } else {
                    mFlingRunnable.startSpringback();
                }

                break;
            }

            setPressed(false);

            // if (mEdgeGlowTop != null) {
            // mEdgeGlowTop.onRelease();
            // mEdgeGlowBottom.onRelease();
            // }

            // Need to redraw since we probably aren't drawing the selector anymore
            invalidate();

            final Handler handler = getHandler();
            if (handler != null) {
                handler.removeCallbacks(mPendingCheckForLongPress);
            }

            recycleVelocityTracker();

            mActivePointerId = INVALID_POINTER;

            if (PROFILE_SCROLLING) {
                if (mScrollProfilingStarted) {
                    Debug.stopMethodTracing();
                    mScrollProfilingStarted = false;
                }
            }

            // if (mScrollStrictSpan != null) {
            // mScrollStrictSpan.finish();
            // mScrollStrictSpan = null;
            // }
            break;
        }

        case MotionEvent.ACTION_CANCEL: {
            switch (mTouchMode) {
            case TOUCH_MODE_OVERSCROLL:
                if (mFlingRunnable == null) {
                    mFlingRunnable = new FlingRunnable();
                }
                mFlingRunnable.startSpringback();
                break;

            case TOUCH_MODE_OVERFLING:
                // Do nothing - let it play out.
                break;

            default:
                mTouchMode = TOUCH_MODE_REST;
                setPressed(false);
                View motionView = this.getChildAt(mMotionPosition - mFirstPosition);
                if (motionView != null) {
                    motionView.setPressed(false);
                }
                clearScrollingCache();

                final Handler handler = getHandler();
                if (handler != null) {
                    handler.removeCallbacks(mPendingCheckForLongPress);
                }

                recycleVelocityTracker();
            }

            // if (mEdgeGlowTop != null) {
            // mEdgeGlowTop.onRelease();
            // mEdgeGlowBottom.onRelease();
            // }
            mActivePointerId = INVALID_POINTER;
            break;
        }

        case MotionEvent.ACTION_POINTER_UP: {
            onSecondaryPointerUp(ev);
            final int x = mMotionX;
            final int y = mMotionY;
            final int motionPosition = pointToPosition(x, y);
            if (motionPosition >= 0) {
                // Remember where the motion event started
                v = getChildAt(motionPosition - mFirstPosition);
                mMotionViewOriginalTop = v.getTop();
                mMotionPosition = motionPosition;
            }
            mLastY = y;
            break;
        }

        case MotionEvent.ACTION_POINTER_DOWN: {
            // New pointers take over dragging duties
            final int index = ev.getActionIndex();
            final int id = ev.getPointerId(index);
            final int x = (int) ev.getX(index);
            final int y = (int) ev.getY(index);
            mMotionCorrection = 0;
            mActivePointerId = id;
            mMotionX = x;
            mMotionY = y;
            final int motionPosition = pointToPosition(x, y);
            if (motionPosition >= 0) {
                // Remember where the motion event started
                v = getChildAt(motionPosition - mFirstPosition);
                mMotionViewOriginalTop = v.getTop();
                mMotionPosition = motionPosition;
            }
            mLastY = y;
            break;
        }
        }

        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex =
                (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mMotionX = (int) ev.getX(newPointerIndex);
            mMotionY = (int) ev.getY(newPointerIndex);
            mMotionCorrection = 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private void createScrollingCache() {
        if (mScrollingCacheEnabled && !mCachingStarted && !isHardwareAccelerated()) {
            setChildrenDrawnWithCacheEnabled(true);
            setChildrenDrawingCacheEnabled(true);
            mCachingStarted = mCachingActive = true;
        }
    }

    private boolean performButtonActionOnTouchDownOfHide(MotionEvent ev) {
        Class<?> cls = View.class;// getClass();
        // while ("android.view.View".equals(cls.getName())) {
        // cls = cls.getSuperclass();
        // }

        try {
            Method method =
                    cls.getDeclaredMethod("performButtonActionOnTouchDown", MotionEvent.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(this, ev);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean startScrollIfNeeded(int y) {
        // Check if we have moved far enough that it looks more like a
        // scroll than a tap
        final int deltaY = y - mMotionY;
        final int distance = Math.abs(deltaY);
        final boolean overscroll = getScrollY() != 0;
        if (overscroll || distance > mTouchSlop) {
            createScrollingCache();
            if (overscroll) {
                mTouchMode = TOUCH_MODE_OVERSCROLL;
                mMotionCorrection = 0;
            } else {
                mTouchMode = TOUCH_MODE_SCROLL;
                mMotionCorrection = deltaY > 0 ? mTouchSlop : -mTouchSlop;
            }
            final Handler handler = getHandler();
            // Handler should not be null unless the AbsListView is not attached to a
            // window, which would make it very hard to scroll it... but the monkeys
            // say it's possible.
            if (handler != null) {
                handler.removeCallbacks(mPendingCheckForLongPress);
            }
            setPressed(false);
            View motionView = getChildAt(mMotionPosition - mFirstPosition);
            if (motionView != null) {
                motionView.setPressed(false);
            }
            reportScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
            // Time to start stealing events! Once we've stolen them, don't let anyone
            // steal from us
            final ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
            scrollIfNeeded(y);
            return true;
        }

        return false;
    }

    private void scrollIfNeeded(int y) {
        final int rawDeltaY = y - mMotionY;
        final int deltaY = rawDeltaY - mMotionCorrection;
        int incrementalDeltaY = mLastY != Integer.MIN_VALUE ? y - mLastY : deltaY;

        if (mTouchMode == TOUCH_MODE_SCROLL) {
            if (PROFILE_SCROLLING) {
                if (!mScrollProfilingStarted) {
                    Debug.startMethodTracing("AbsListViewScroll");
                    mScrollProfilingStarted = true;
                }
            }

            // if (mScrollStrictSpan == null) {
            // // If it's non-null, we're already in a scroll.
            // mScrollStrictSpan = StrictMode.enterCriticalSpan("AbsListView-scroll");
            // }

            if (y != mLastY) {
                // We may be here after stopping a fling and continuing to scroll.
                // If so, we haven't disallowed intercepting touch events yet.
                // Make sure that we do so in case we're in a parent that can intercept.
                if ((getGroupFlagsOfNotPublic() & 0x80000) == 0 && Math.abs(rawDeltaY) > mTouchSlop) {// mGroupFlags
                                                                                                      // &
                                                                                                      // FLAG_DISALLOW_INTERCEPT
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }

                final int motionIndex;
                if (mMotionPosition >= 0) {
                    motionIndex = mMotionPosition - mFirstPosition;
                } else {
                    // If we don't have a motion position that we can reliably track,
                    // pick something in the middle to make a best guess at things below.
                    motionIndex = getChildCount() / 2;
                }

                int motionViewPrevTop = 0;
                View motionView = this.getChildAt(motionIndex);
                if (motionView != null) {
                    motionViewPrevTop = motionView.getTop();
                }

                // No need to do all this work if we're not going to move anyway
                boolean atEdge = false;
                if (incrementalDeltaY != 0) {
                    atEdge = trackMotionScroll(deltaY, incrementalDeltaY);
                }

                // Check to see if we have bumped into the scroll limit
                motionView = this.getChildAt(motionIndex);
                if (motionView != null) {
                    // Check if the top of the motion view is where it is
                    // supposed to be
                    final int motionViewRealTop = motionView.getTop();
                    if (atEdge) {
                        // Apply overscroll

                        int overscroll =
                                -incrementalDeltaY - (motionViewRealTop - motionViewPrevTop);
                        overScrollBy(0, overscroll, 0, getScrollY(), 0, 0, 0, mOverscrollDistance,
                                true);
                        if (Math.abs(mOverscrollDistance) == Math.abs(getScrollY())) {
                            // Don't allow overfling if we're at the edge.
                            if (mVelocityTracker != null) {
                                mVelocityTracker.clear();
                            }
                        }

                        final int overscrollMode = getOverScrollMode();
                        if (overscrollMode == OVER_SCROLL_ALWAYS
                                || (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && !contentFits())) {
                            mDirection = 0; // Reset when entering overscroll.
                            mTouchMode = TOUCH_MODE_OVERSCROLL;
                            if (rawDeltaY > 0) {
                                // mEdgeGlowTop.onPull((float) overscroll / getHeight());
                                // if (!mEdgeGlowBottom.isFinished()) {
                                // mEdgeGlowBottom.onRelease();
                                // }
                                invalidate();// invalidate(mEdgeGlowTop.getBounds(false));
                            } else if (rawDeltaY < 0) {
                                // mEdgeGlowBottom.onPull((float) overscroll / getHeight());
                                // if (!mEdgeGlowTop.isFinished()) {
                                // mEdgeGlowTop.onRelease();
                                // }
                                invalidate();// invalidate(mEdgeGlowBottom.getBounds(true));
                            }
                        }
                    }
                    mMotionY = y;
                }
                mLastY = y;
            }
        } else if (mTouchMode == TOUCH_MODE_OVERSCROLL) {
            if (y != mLastY) {
                final int oldScroll = getScrollY();
                final int newScroll = oldScroll - incrementalDeltaY;
                int newDirection = y > mLastY ? 1 : -1;

                if (mDirection == 0) {
                    mDirection = newDirection;
                }

                int overScrollDistance = -incrementalDeltaY;
                if ((newScroll < 0 && oldScroll >= 0) || (newScroll > 0 && oldScroll <= 0)) {
                    overScrollDistance = -oldScroll;
                    incrementalDeltaY += overScrollDistance;
                } else {
                    incrementalDeltaY = 0;
                }

                if (overScrollDistance != 0) {
                    overScrollBy(0, overScrollDistance, 0, getScrollY(), 0, 0, 0,
                            mOverscrollDistance, true);
                    final int overscrollMode = getOverScrollMode();
                    if (overscrollMode == OVER_SCROLL_ALWAYS
                            || (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && !contentFits())) {
                        if (rawDeltaY > 0) {
                            // mEdgeGlowTop.onPull((float) overScrollDistance / getHeight());
                            // if (!mEdgeGlowBottom.isFinished()) {
                            // mEdgeGlowBottom.onRelease();
                            // }
                            invalidate();// invalidate(mEdgeGlowTop.getBounds(false));
                        } else if (rawDeltaY < 0) {
                            // mEdgeGlowBottom.onPull((float) overScrollDistance / getHeight());
                            // if (!mEdgeGlowTop.isFinished()) {
                            // mEdgeGlowTop.onRelease();
                            // }
                            invalidate();// invalidate(mEdgeGlowBottom.getBounds(true));
                        }
                    }
                }

                if (incrementalDeltaY != 0) {
                    // Coming back to 'real' list scrolling
                    if (getScrollY() != 0) {
                        setScrollY(0);
                        invalidateParentIfNeeded();
                    }

                    trackMotionScroll(incrementalDeltaY, incrementalDeltaY);

                    mTouchMode = TOUCH_MODE_SCROLL;

                    // We did not scroll the full amount. Treat this essentially like the
                    // start of a new touch scroll
                    final int motionPosition = findClosestMotionRow(y);

                    mMotionCorrection = 0;
                    View motionView = getChildAt(motionPosition - mFirstPosition);
                    mMotionViewOriginalTop = motionView != null ? motionView.getTop() : 0;
                    mMotionY = y;
                    mMotionPosition = motionPosition;
                }
                mLastY = y;
                mDirection = newDirection;
            }
        }
    }

    /**
     * {is hide}
     */
    private void invalidateParentIfNeeded() {
        ViewParent parent = getParent();
        if (isHardwareAccelerated() && parent instanceof View) {
            ((View) parent).invalidate();
        }
    }

    /**
     * Find the row closest to y. This row will be used as the motion row when scrolling.
     * 
     * @param y Where the user touched
     * @return The position of the first (or only) item in the row closest to y
     */
    int findClosestMotionRow(int y) {
        final int childCount = getChildCount();
        if (childCount == 0) {
            return INVALID_POSITION;
        }

        final int motionRow = findMotionRow(y);
        return motionRow != INVALID_POSITION ? motionRow : mFirstPosition + childCount - 1;
    }

    /**
     * 继承自{@link View#onOverScrolled(int, int, boolean, boolean)}
     */
    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        int oldScrollX = getScrollX();
        int oldScrollY = getScrollY();
        if (oldScrollY != scrollY) {
            onScrollChanged(oldScrollY, scrollY, oldScrollX, oldScrollX);
            setScrollY(scrollY);
            invalidateParentIfNeeded();

            awakenScrollBars();
        }
    }

    /**
     * 继承自{@link View#onGenericMotionEvent(MotionEvent)}
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
            switch (event.getAction()) {
            case MotionEvent.ACTION_SCROLL: {
                if (mTouchMode == TOUCH_MODE_REST) {
                    final float vscroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                    if (vscroll != 0) {
                        final int delta = (int) (vscroll * getVerticalScrollFactorOfHide());// getVerticalScrollFactor()
                        if (!trackMotionScroll(delta, delta)) {
                            return true;
                        }
                    }
                }
            }
            }
        }
        return super.onGenericMotionEvent(event);
    }

    private float getVerticalScrollFactorOfHide() {
        Class<?> cls = getClass();
        try {
            Method method = cls.getDeclaredMethod("getVerticalScrollFactor");
            method.setAccessible(true);
            return ((Float) method.invoke(this)).floatValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 继承自{@link ViewGroup#requestDisallowInterceptTouchEvent(boolean)}
     */
    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    /**
     * 继承自{@link ViewGroup#onInterceptTouchEvent(MotionEvent)}
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        View v;

        if (mPositionScroller != null) {
            mPositionScroller.stop();
        }

        if (!mIsAttached) {
            // Something isn't right.
            // Since we rely on being attached to get data set change notifications,
            // don't risk doing anything where we might try to resync and find things
            // in a bogus state.
            return false;
        }

        // if (mFastScroller != null) {
        // boolean intercepted = mFastScroller.onInterceptTouchEvent(ev);
        // if (intercepted) {
        // return true;
        // }
        // }

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN: {
            int touchMode = mTouchMode;
            if (touchMode == TOUCH_MODE_OVERFLING || touchMode == TOUCH_MODE_OVERSCROLL) {
                mMotionCorrection = 0;
                return true;
            }

            final int x = (int) ev.getX();
            final int y = (int) ev.getY();
            mActivePointerId = ev.getPointerId(0);

            int motionPosition = findMotionRow(y);
            if (touchMode != TOUCH_MODE_FLING && motionPosition >= 0) {
                // User clicked on an actual view (and was not stopping a fling).
                // Remember where the motion event started
                v = getChildAt(motionPosition - mFirstPosition);
                mMotionViewOriginalTop = v.getTop();
                mMotionX = x;
                mMotionY = y;
                mMotionPosition = motionPosition;
                mTouchMode = TOUCH_MODE_DOWN;
                clearScrollingCache();
            }
            mLastY = Integer.MIN_VALUE;
            initOrResetVelocityTracker();
            mVelocityTracker.addMovement(ev);
            if (touchMode == TOUCH_MODE_FLING) {
                return true;
            }
            break;
        }

        case MotionEvent.ACTION_MOVE: {
            switch (mTouchMode) {
            case TOUCH_MODE_DOWN:
                int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex == -1) {
                    pointerIndex = 0;
                    mActivePointerId = ev.getPointerId(pointerIndex);
                }
                final int y = (int) ev.getY(pointerIndex);
                initVelocityTrackerIfNotExists();
                mVelocityTracker.addMovement(ev);
                if (startScrollIfNeeded(y)) {
                    return true;
                }
                break;
            }
            break;
        }

        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP: {
            mTouchMode = TOUCH_MODE_REST;
            mActivePointerId = INVALID_POINTER;
            recycleVelocityTracker();
            reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
            break;
        }

        case MotionEvent.ACTION_POINTER_UP: {
            onSecondaryPointerUp(ev);
            break;
        }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    /**
     * 向views中添加可触控视图，这些可触控视图是该视图的子类 （如果该视图可触控， 也可以添加该视图）。
     * 
     * @param views 现在为止的可触控视图。
     */
    @Override
    public void addTouchables(ArrayList<View> views) {
        final int count = getChildCount();
        final int firstPosition = mFirstPosition;
        final ListAdapter adapter = mAdapter;

        if (adapter == null) {
            return;
        }

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (adapter.isEnabled(firstPosition + i)) {
                views.add(child);
            }
            child.addTouchables(views);
        }
    }

    /**
     * The amount of friction applied to flings. The default value is
     * {@link ViewConfiguration#getScrollFriction}.
     * 
     * @return A scalar dimensionless value representing the coefficient of friction.
     */
    /**
     * 应用到快速滑动的摩擦系数。默认值为{@link ViewConfiguration#getScrollFriction()}。
     * 
     * @param friction 代表摩擦系数的无量纲值。
     */
    public void setFriction(float friction) {
        if (mFlingRunnable == null) {
            mFlingRunnable = new FlingRunnable();
        }
        mFlingRunnable.mScroller.setFriction(friction);
    }

    /**
     * Sets a scale factor for the fling velocity. The initial scale factor is 1.0.
     * 
     * @param scale The scale factor to multiply the velocity by.
     */
    /**
     * 设置快速滑动速度的缩放因子。 初始缩放因子为 1.0。
     * 
     * @param scale 应用在速度上的缩放因子。
     */
    public void setVelocityScale(float scale) {
        mVelocityScale = scale;
    }

    /**
     * Smoothly scroll to the specified adapter position. The view will scroll such that the
     * indicated position is displayed.
     * 
     * @param position Scroll to this adapter position.
     */
    /**
     * 平滑滚动到指定适配器位置。视图会滚动以显示指定位置的视图。
     * 
     * @param position 滚动到的适配器位置。
     */
    public void smoothScrollToPosition(int position) {
        if (mPositionScroller == null) {
            mPositionScroller = new PositionScroller();
        }
        mPositionScroller.start(position);
    }

    /**
     * Smoothly scroll to the specified adapter position. The view will scroll such that the
     * indicated position is displayed <code>offset</code> pixels from the top edge of the view. If
     * this is impossible, (e.g. the offset would scroll the first or last item beyond the
     * boundaries of the list) it will get as close as possible. The scroll will take
     * <code>duration</code> milliseconds to complete.
     * 
     * @param position Position to scroll to
     * @param offset Desired distance in pixels of <code>position</code> from the top of the view
     *        when scrolling is finished
     * @param duration Number of milliseconds to use for the scroll
     */
    /**
     * 平滑滚动到指定的适配器位置。指定位置的视图会滚动到相对顶边偏移<code>offset</code>
     * 像素的位置显示。如果无法做到（比如该偏移量会使首尾条目超越列表边缘），会滚动到尽量接近的位置。 滚动需要持续<code>duration</code>毫秒来完成。
     * 
     * @param position 滚动到的位置
     * @param offset 滚动结束时，指定<code>position</code>条目距离视图顶部的像素数
     * @param duration 滚动执行的毫秒数
     */
    public void smoothScrollToPositionFromTop(int position, int offset, int duration) {
        if (mPositionScroller == null) {
            mPositionScroller = new PositionScroller();
        }
        mPositionScroller.startWithOffset(position, offset, duration);
    }

    /**
     * Smoothly scroll to the specified adapter position. The view will scroll such that the
     * indicated position is displayed <code>offset</code> pixels from the top edge of the view. If
     * this is impossible, (e.g. the offset would scroll the first or last item beyond the
     * boundaries of the list) it will get as close as possible.
     * 
     * @param position Position to scroll to
     * @param offset Desired distance in pixels of <code>position</code> from the top of the view
     *        when scrolling is finished
     */
    /**
     * 平滑滚动到指定的适配器位置。指定位置的视图会滚动到相对顶边偏移
     * <code>offset</code像素的位置显示。 如果无法做到（比如该偏移量会使首尾条目超越列表边缘），会滚动到尽量接近的位置。
     * 
     * @param position 滚动到的位置
     * @param offset 滚动结束时，指定<code>position</code>条目距离视图顶部的像素数
     */
    public void smoothScrollToPositionFromTop(int position, int offset) {
        if (mPositionScroller == null) {
            mPositionScroller = new PositionScroller();
        }
        mPositionScroller.startWithOffset(position, offset);
    }

    /**
     * Smoothly scroll to the specified adapter position. The view will scroll such that the
     * indicated position is displayed, but it will stop early if scrolling further would scroll
     * boundPosition out of view.
     * 
     * @param position Scroll to this adapter position.
     * @param boundPosition Do not scroll if it would move this adapter position out of view.
     */
    /**
     * 平滑滚动到指定的适配器位置。视图会滚动到指定位置显示出来，如果滚动会使boundPosition滚动到视图外，滚动会先被停止。
     * 
     * @param position 要滚动到的适配器位置。
     * @param boundPosition 如果要将这个适配器位置移出视图，滚动会停止。
     */
    public void smoothScrollToPosition(int position, int boundPosition) {
        if (mPositionScroller == null) {
            mPositionScroller = new PositionScroller();
        }
        mPositionScroller.start(position, boundPosition);
    }

    /**
     * Smoothly scroll by distance pixels over duration milliseconds.
     * 
     * @param distance Distance to scroll in pixels.
     * @param duration Duration of the scroll animation in milliseconds.
     */
    /**
     * 平滑滚动distance个像素，持续duration毫秒。
     * 
     * @param distance 滚动的距离，像素数。
     * @param duration 滚动动画持续的时间，毫秒。
     */
    public void smoothScrollBy(int distance, int duration) {
        smoothScrollBy(distance, duration, false);
    }

    void smoothScrollBy(int distance, int duration, boolean linear) {
        if (mFlingRunnable == null) {
            mFlingRunnable = new FlingRunnable();
        }

        // No sense starting to scroll if we're not going anywhere
        final int firstPos = mFirstPosition;
        final int childCount = getChildCount();
        final int lastPos = firstPos + childCount;
        final int topLimit = getPaddingTop();
        final int bottomLimit = getHeight() - getPaddingBottom();

        if (distance == 0
                || mItemCount == 0
                || childCount == 0
                || (firstPos == 0 && getChildAt(0).getTop() == topLimit && distance < 0)
                || (lastPos == mItemCount && getChildAt(childCount - 1).getBottom() == bottomLimit && distance > 0)) {
            mFlingRunnable.endFling();
            if (mPositionScroller != null) {
                mPositionScroller.stop();
            }
        } else {
            reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
            mFlingRunnable.startScroll(distance, duration, linear);
        }
    }

    /**
     * Allows RemoteViews to scroll relatively to a position.
     */
    void smoothScrollByOffset(int position) {
        int index = -1;
        if (position < 0) {
            index = getFirstVisiblePosition();
        } else if (position > 0) {
            index = getLastVisiblePosition();
        }

        if (index > -1) {
            View child = getChildAt(index - getFirstVisiblePosition());
            if (child != null) {
                Rect visibleRect = new Rect();
                if (child.getGlobalVisibleRect(visibleRect)) {
                    // the child is partially visible
                    int childRectArea = child.getWidth() * child.getHeight();
                    int visibleRectArea = visibleRect.width() * visibleRect.height();
                    float visibleArea = (visibleRectArea / (float) childRectArea);
                    final float visibleThreshold = 0.75f;
                    if ((position < 0) && (visibleArea < visibleThreshold)) {
                        // the top index is not perceivably visible so offset
                        // to account for showing that top index as well
                        ++index;
                    } else if ((position > 0) && (visibleArea < visibleThreshold)) {
                        // the bottom index is not perceivably visible so offset
                        // to account for showing that bottom index as well
                        --index;
                    }
                }
                smoothScrollToPosition(Math.max(0, Math.min(getCount(), index + position)));
            }
        }
    }

    /**
     * Fires an "on scroll state changed" event to the registered
     * {@link android.widget.AbsListView.OnScrollListener}, if any. The state change is fired only
     * if the specified state is different from the previously known state.
     * 
     * @param newState The new scroll state.
     */
    void reportScrollStateChange(int newState) {
        if (newState != mLastScrollState) {
            if (mOnScrollListener != null) {
                mLastScrollState = newState;
                mOnScrollListener.onScrollStateChanged(this, newState);
            }
        }
    }

    /**
     * @return true if all list content currently fits within the view boundaries
     */
    private boolean contentFits() {
        final int childCount = getChildCount();
        if (childCount == 0) return true;
        if (childCount != mItemCount) return false;

        return getChildAt(0).getTop() >= mListPadding.top
                && getChildAt(childCount - 1).getBottom() <= getHeight() - mListPadding.bottom;
    }

    /**
     * Subclasses must override this method to layout their children.
     */
    /**
     * 子类必须重写该方法来控制其子视图的布局
     */
    protected void layoutChildren() {}

    void updateScrollIndicators() {
        if (mScrollUp != null) {
            boolean canScrollUp;
            // 0th element is not visible
            canScrollUp = mFirstPosition > 0;

            // ... Or top of 0th element is not visible
            if (!canScrollUp) {
                if (getChildCount() > 0) {
                    View child = getChildAt(0);
                    canScrollUp = child.getTop() < mListPadding.top;
                }
            }

            mScrollUp.setVisibility(canScrollUp ? View.VISIBLE : View.INVISIBLE);
        }

        if (mScrollDown != null) {
            boolean canScrollDown;
            int count = getChildCount();

            // Last item is not visible
            canScrollDown = (mFirstPosition + count) < mItemCount;

            // ... Or bottom of the last element is not visible
            if (!canScrollDown && count > 0) {
                View child = getChildAt(count - 1);
                canScrollDown = child.getBottom() > getBottom() - mListPadding.bottom;
            }

            mScrollDown.setVisibility(canScrollDown ? View.VISIBLE : View.INVISIBLE);
        }
    }

    /**
     * Track a motion scroll
     * 
     * @param deltaY Amount to offset mMotionView. This is the accumulated delta since the motion
     *        began. Positive numbers mean the user's finger is moving down the screen.
     * @param incrementalDeltaY Change in deltaY from the previous event.
     * @return true if we're already at the beginning/end of the list and have nothing to do.
     */
    boolean trackMotionScroll(int deltaY, int incrementalDeltaY) {
        final int childCount = getChildCount();
        if (childCount == 0) {
            return true;
        }

        final int firstTop = getChildAt(0).getTop();
        final int lastBottom = getChildAt(childCount - 1).getBottom();

        final Rect listPadding = mListPadding;

        // "effective padding" In this case is the amount of padding that affects
        // how much space should not be filled by items. If we don't clip to padding
        // there is no effective padding.
        int effectivePaddingTop = 0;
        int effectivePaddingBottom = 0;
        if ((getGroupFlagsOfNotPublic() & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK) {
            effectivePaddingTop = listPadding.top;
            effectivePaddingBottom = listPadding.bottom;
        }

        // FIXME account for grid vertical spacing too?
        final int spaceAbove = effectivePaddingTop - firstTop;
        final int end = getHeight() - effectivePaddingBottom;
        final int spaceBelow = lastBottom - end;

        final int height = getHeight() - getPaddingBottom() - getPaddingTop();
        if (deltaY < 0) {
            deltaY = Math.max(-(height - 1), deltaY);
        } else {
            deltaY = Math.min(height - 1, deltaY);
        }

        if (incrementalDeltaY < 0) {
            incrementalDeltaY = Math.max(-(height - 1), incrementalDeltaY);
        } else {
            incrementalDeltaY = Math.min(height - 1, incrementalDeltaY);
        }

        final int firstPosition = mFirstPosition;

        // Update our guesses for where the first and last views are
        if (firstPosition == 0) {
            mFirstPositionDistanceGuess = firstTop - listPadding.top;
        } else {
            mFirstPositionDistanceGuess += incrementalDeltaY;
        }
        if (firstPosition + childCount == mItemCount) {
            mLastPositionDistanceGuess = lastBottom + listPadding.bottom;
        } else {
            mLastPositionDistanceGuess += incrementalDeltaY;
        }

        final boolean cannotScrollDown =
                (firstPosition == 0 && firstTop >= listPadding.top && incrementalDeltaY >= 0);
        final boolean cannotScrollUp =
                (firstPosition + childCount == mItemCount
                        && lastBottom <= getHeight() - listPadding.bottom && incrementalDeltaY <= 0);

        if (cannotScrollDown || cannotScrollUp) {
            return incrementalDeltaY != 0;
        }

        final boolean down = incrementalDeltaY < 0;

        final boolean inTouchMode = isInTouchMode();
        if (inTouchMode) {
            hideSelector();
        }

        final int headerViewsCount = getHeaderViewsCount();
        final int footerViewsStart = mItemCount - getFooterViewsCount();

        int start = 0;
        int count = 0;

        if (down) {
            int top = -incrementalDeltaY;
            if ((getGroupFlagsOfNotPublic() & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK) {
                top += listPadding.top;
            }
            for (int i = 0; i < childCount; i++) {
                final View child = getChildAt(i);
                if (child.getBottom() >= top) {
                    break;
                } else {
                    count++;
                    int position = firstPosition + i;
                    if (position >= headerViewsCount && position < footerViewsStart) {
                        mRecycler.addScrapView(child, position);
                    }
                }
            }
        } else {
            int bottom = getHeight() - incrementalDeltaY;
            if ((getGroupFlagsOfNotPublic() & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK) {
                bottom -= listPadding.bottom;
            }
            for (int i = childCount - 1; i >= 0; i--) {
                final View child = getChildAt(i);
                if (child.getTop() <= bottom) {
                    break;
                } else {
                    start = i;
                    count++;
                    int position = firstPosition + i;
                    if (position >= headerViewsCount && position < footerViewsStart) {
                        mRecycler.addScrapView(child, position);
                    }
                }
            }
        }

        mMotionViewNewTop = mMotionViewOriginalTop + deltaY;

        mBlockLayoutRequests = true;

        if (count > 0) {
            detachViewsFromParent(start, count);
            mRecycler.removeSkippedScrap();
        }

        // invalidate before moving the children to avoid unnecessary invalidate
        // calls to bubble up from the children all the way to the top
        if (!awakenScrollBars()) {
            invalidate();
        }

        offsetChildrenTopAndBottomOfHide(incrementalDeltaY);// offsetChildrenTopAndBottom(incrementalDeltaY);

        if (down) {
            mFirstPosition += count;
        }

        final int absIncrementalDeltaY = Math.abs(incrementalDeltaY);
        if (spaceAbove < absIncrementalDeltaY || spaceBelow < absIncrementalDeltaY) {
            fillGap(down);
        }

        if (!inTouchMode && mSelectedPosition != INVALID_POSITION) {
            final int childIndex = mSelectedPosition - mFirstPosition;
            if (childIndex >= 0 && childIndex < getChildCount()) {
                positionSelector(mSelectedPosition, getChildAt(childIndex));
            }
        } else if (mSelectorPosition != INVALID_POSITION) {
            final int childIndex = mSelectorPosition - mFirstPosition;
            if (childIndex >= 0 && childIndex < getChildCount()) {
                positionSelector(INVALID_POSITION, getChildAt(childIndex));
            }
        } else {
            mSelectorRect.setEmpty();
        }

        mBlockLayoutRequests = false;

        invokeOnItemScrollListener();

        return false;
    }

    int getGroupFlagsOfNotPublic() {
        Class<?> cls = getClass();
        try {
            Field field = cls.getDeclaredField("mGroupFlags");
            field.setAccessible(true);
            return field.getInt(this);
        } catch (Exception e) {
            return 0;
        }
    }

    void offsetChildrenTopAndBottomOfHide(int offset) {
        Class<?> cls = ViewGroup.class;// getClass();
        try {
            Method method = cls.getMethod("offsetChildrenTopAndBottom", int.class);
            method.setAccessible(true);
            method.invoke(this, offset);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void hideSelector() {
        if (mSelectedPosition != INVALID_POSITION) {
            if (mLayoutMode != LAYOUT_SPECIFIC) {
                mResurrectToPosition = mSelectedPosition;
            }
            if (mNextSelectedPosition >= 0 && mNextSelectedPosition != mSelectedPosition) {
                mResurrectToPosition = mNextSelectedPosition;
            }
            setSelectedPositionInt(INVALID_POSITION);
            setNextSelectedPositionInt(INVALID_POSITION);
            mSelectedTop = 0;
        }
    }

    /**
     * Returns the number of header views in the list. Header views are special views at the top of
     * the list that should not be recycled during a layout.
     * 
     * @return The number of header views, 0 in the default implementation.
     */
    /**
     * 返回列表中的列表头视图数量。列表头视图是显示于列表顶部、在布局时不能再利用的特殊视图。
     * 
     * @return 列表中的列表头视图数量；缺省实现时，其数量为 0。
     */
    int getHeaderViewsCount() {
        return 0;
    }

    /**
     * Returns the number of footer views in the list. Footer views are special views at the bottom
     * of the list that should not be recycled during a layout.
     * 
     * @return The number of footer views, 0 in the default implementation.
     */
    /**
     * 返回列表中的列表尾视图数量。 列表尾视图是显示于列表底部、在布局时不能再利用的特殊视图。
     * 
     * @return 列表中的列表尾视图数量；缺省实现时，其数量为 0。
     */
    int getFooterViewsCount() {
        return 0;
    }

    /**
     * Fills the gap left open by a touch-scroll. During a touch scroll, children that remain on
     * screen are shifted and the other ones are discarded. The role of this method is to fill the
     * gap thus created by performing a partial layout in the empty space.
     * 
     * @param down true if the scroll is going down, false if it is going up
     */
    abstract void fillGap(boolean down);

    void positionSelector(int position, View sel) {
        if (position != INVALID_POSITION) {
            mSelectorPosition = position;
        }

        final Rect selectorRect = mSelectorRect;
        selectorRect.set(sel.getLeft(), sel.getTop(), sel.getRight(), sel.getBottom());
        if (sel instanceof SelectionBoundsAdjuster) {
            ((SelectionBoundsAdjuster) sel).adjustListItemSelectionBounds(selectorRect);
        }
        positionSelector(selectorRect.left, selectorRect.top, selectorRect.right,
                selectorRect.bottom);

        final boolean isChildViewEnabled = mIsChildViewEnabled;
        if (sel.isEnabled() != isChildViewEnabled) {
            mIsChildViewEnabled = !isChildViewEnabled;
            if (getSelectedItemPosition() != INVALID_POSITION) {
                refreshDrawableState();
            }
        }
    }

    private void positionSelector(int l, int t, int r, int b) {
        mSelectorRect.set(l - mSelectionLeftPadding, t - mSelectionTopPadding, r
                + mSelectionRightPadding, b + mSelectionBottomPadding);
    }

    /**
     * @return A position to select. First we try mSelectedPosition. If that has been clobbered by
     *         entering touch mode, we then try mResurrectToPosition. Values are pinned to the range
     *         of items available in the adapter
     */
    int reconcileSelectedPosition() {
        int position = mSelectedPosition;
        if (position < 0) {
            position = mResurrectToPosition;
        }
        position = Math.max(0, position);
        position = Math.min(position, mItemCount - 1);
        return position;
    }

    /**
     * Find the row closest to y. This row will be used as the motion row when scrolling
     * 
     * @param y Where the user touched
     * @return The position of the first (or only) item in the row containing y
     */
    abstract int findMotionRow(int y);

    /**
     * Causes all the views to be rebuilt and redrawn.
     */
    /**
     * 使所有的视图重新构建并重绘。
     */
    public void invalidateViews() {
        mDataChanged = true;
        rememberSyncState();
        requestLayout();
        invalidate();
    }

    /**
     * If there is a selection returns false. Otherwise resurrects the selection and returns true if
     * resurrected.
     */
    boolean resurrectSelectionIfNeeded() {
        if (mSelectedPosition < 0 && resurrectSelection()) {
            updateSelectorState();
            return true;
        }
        return false;
    }

    /**
     * Makes the item at the supplied position selected.
     * 
     * @param position the position of the new selection
     */
    abstract void setSelectionInt(int position);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleDataChanged() {
        int count = mItemCount;
        int lastHandledItemCount = mLastHandledItemCount;
        mLastHandledItemCount = mItemCount;

        if (mChoiceMode != CHOICE_MODE_NONE && mAdapter != null && mAdapter.hasStableIds()) {
            confirmCheckedPositionsById();
        }

        // TODO: In the future we can recycle these views based on stable ID instead.
        mRecycler.clearTransientStateViews();

        if (count > 0) {
            int newPos;
            int selectablePos;

            // Find the row we are supposed to sync to
            if (mNeedSync) {
                // Update this first, since setNextSelectedPositionInt inspects it
                mNeedSync = false;
                mPendingSync = null;

                if (mTranscriptMode == TRANSCRIPT_MODE_ALWAYS_SCROLL) {
                    mLayoutMode = LAYOUT_FORCE_BOTTOM;
                    return;
                } else if (mTranscriptMode == TRANSCRIPT_MODE_NORMAL) {
                    if (mForceTranscriptScroll) {
                        mForceTranscriptScroll = false;
                        mLayoutMode = LAYOUT_FORCE_BOTTOM;
                        return;
                    }
                    final int childCount = getChildCount();
                    final int listBottom = getHeight() - getPaddingBottom();
                    final View lastChild = getChildAt(childCount - 1);
                    final int lastBottom = lastChild != null ? lastChild.getBottom() : listBottom;
                    if (mFirstPosition + childCount >= lastHandledItemCount
                            && lastBottom <= listBottom) {
                        mLayoutMode = LAYOUT_FORCE_BOTTOM;
                        return;
                    }
                    // Something new came in and we didn't scroll; give the user a clue that
                    // there's something new.
                    awakenScrollBars();
                }

                switch (mSyncMode) {
                case SYNC_SELECTED_POSITION:
                    if (isInTouchMode()) {
                        // We saved our state when not in touch mode. (We know this because
                        // mSyncMode is SYNC_SELECTED_POSITION.) Now we are trying to
                        // restore in touch mode. Just leave mSyncPosition as it is (possibly
                        // adjusting if the available range changed) and return.
                        mLayoutMode = LAYOUT_SYNC;
                        mSyncPosition = Math.min(Math.max(0, mSyncPosition), count - 1);

                        return;
                    } else {
                        // See if we can find a position in the new data with the same
                        // id as the old selection. This will change mSyncPosition.
                        newPos = findSyncPosition();
                        if (newPos >= 0) {
                            // Found it. Now verify that new selection is still selectable
                            selectablePos = lookForSelectablePosition(newPos, true);
                            if (selectablePos == newPos) {
                                // Same row id is selected
                                mSyncPosition = newPos;

                                if (mSyncHeight == getHeight()) {
                                    // If we are at the same height as when we saved state, try
                                    // to restore the scroll position too.
                                    mLayoutMode = LAYOUT_SYNC;
                                } else {
                                    // We are not the same height as when the selection was saved,
                                    // so
                                    // don't try to restore the exact position
                                    mLayoutMode = LAYOUT_SET_SELECTION;
                                }

                                // Restore selection
                                setNextSelectedPositionInt(newPos);
                                return;
                            }
                        }
                    }
                    break;
                case SYNC_FIRST_POSITION:
                    // Leave mSyncPosition as it is -- just pin to available range
                    mLayoutMode = LAYOUT_SYNC;
                    mSyncPosition = Math.min(Math.max(0, mSyncPosition), count - 1);

                    return;
                }
            }

            if (!isInTouchMode()) {
                // We couldn't find matching data -- try to use the same position
                newPos = getSelectedItemPosition();

                // Pin position to the available range
                if (newPos >= count) {
                    newPos = count - 1;
                }
                if (newPos < 0) {
                    newPos = 0;
                }

                // Make sure we select something selectable -- first look down
                selectablePos = lookForSelectablePosition(newPos, true);

                if (selectablePos >= 0) {
                    setNextSelectedPositionInt(selectablePos);
                    return;
                } else {
                    // Looking down didn't work -- try looking up
                    selectablePos = lookForSelectablePosition(newPos, false);
                    if (selectablePos >= 0) {
                        setNextSelectedPositionInt(selectablePos);
                        return;
                    }
                }
            } else {

                // We already know where we want to resurrect the selection
                if (mResurrectToPosition >= 0) {
                    return;
                }
            }

        }

        // Nothing is selected. Give up and reset everything.
        mLayoutMode = mStackFromBottom ? LAYOUT_FORCE_BOTTOM : LAYOUT_FORCE_TOP;
        mSelectedPosition = INVALID_POSITION;
        mSelectedRowId = INVALID_ROW_ID;
        mNextSelectedPosition = INVALID_POSITION;
        mNextSelectedRowId = INVALID_ROW_ID;
        mNeedSync = false;
        mPendingSync = null;
        mSelectorPosition = INVALID_POSITION;
        checkSelectionChanged();
    }

    void confirmCheckedPositionsById() {
        // Clear out the positional check states, we'll rebuild it below from IDs.
        mCheckStates.clear();

        boolean checkedCountChanged = false;
        for (int checkedIndex = 0; checkedIndex < mCheckedIdStates.size(); checkedIndex++) {
            final long id = mCheckedIdStates.keyAt(checkedIndex);
            final int lastPos = mCheckedIdStates.valueAt(checkedIndex);

            final long lastPosId = mAdapter.getItemId(lastPos);
            if (id != lastPosId) {
                // Look around to see if the ID is nearby. If not, uncheck it.
                final int start = Math.max(0, lastPos - CHECK_POSITION_SEARCH_DISTANCE);
                final int end = Math.min(lastPos + CHECK_POSITION_SEARCH_DISTANCE, mItemCount);
                boolean found = false;
                for (int searchPos = start; searchPos < end; searchPos++) {
                    final long searchId = mAdapter.getItemId(searchPos);
                    if (id == searchId) {
                        found = true;
                        mCheckStates.put(searchPos, true);
                        mCheckedIdStates.setValueAt(checkedIndex, searchPos);
                        break;
                    }
                }

                if (!found) {
                    mCheckedIdStates.delete(id);
                    checkedIndex--;
                    mCheckedItemCount--;
                    checkedCountChanged = true;
                    if (mChoiceActionMode != null && mMultiChoiceModeCallback != null) {
                        mMultiChoiceModeCallback.onItemCheckedStateChanged(mChoiceActionMode,
                                lastPos, id, false);
                    }
                }
            } else {
                mCheckStates.put(lastPos, true);
            }
        }

        if (checkedCountChanged && mChoiceActionMode != null) {
            mChoiceActionMode.invalidate();
        }
    }

    /**
     * What is the distance between the source and destination rectangles given the direction of
     * focus navigation between them? The direction basically helps figure out more quickly what is
     * self evident by the relationship between the rects...
     * 
     * @param source the source rectangle
     * @param dest the destination rectangle
     * @param direction the direction
     * @return the distance between the rectangles
     */
    static int getDistance(Rect source, Rect dest, int direction) {
        int sX, sY; // source x, y
        int dX, dY; // dest x, y
        switch (direction) {
        case View.FOCUS_RIGHT:
            sX = source.right;
            sY = source.top + source.height() / 2;
            dX = dest.left;
            dY = dest.top + dest.height() / 2;
            break;
        case View.FOCUS_DOWN:
            sX = source.left + source.width() / 2;
            sY = source.bottom;
            dX = dest.left + dest.width() / 2;
            dY = dest.top;
            break;
        case View.FOCUS_LEFT:
            sX = source.left;
            sY = source.top + source.height() / 2;
            dX = dest.right;
            dY = dest.top + dest.height() / 2;
            break;
        case View.FOCUS_UP:
            sX = source.left + source.width() / 2;
            sY = source.top;
            dX = dest.left + dest.width() / 2;
            dY = dest.bottom;
            break;
        case View.FOCUS_FORWARD:
        case View.FOCUS_BACKWARD:
            sX = source.right + source.width() / 2;
            sY = source.top + source.height() / 2;
            dX = dest.left + dest.width() / 2;
            dY = dest.top + dest.height() / 2;
            break;
        default:
            throw new IllegalArgumentException("direction must be one of "
                    + "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT, "
                    + "FOCUS_FORWARD, FOCUS_BACKWARD}.");
        }
        int deltaX = dX - sX;
        int deltaY = dY - sY;
        return deltaY * deltaY + deltaX * deltaX;
    }

    /**
     * 继承自{@link ViewGroup#generateDefaultLayoutParams()}
     */
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0);
    }

    /**
     * 继承自{@link ViewGroup#generateLayoutParams(ViewGroup.LayoutParams)}
     */
    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    /**
     * 继承自{@link ViewGroup#generateLayoutParams(AttributeSet)}
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new AbsListView.LayoutParams(getContext(), attrs);
    }

    /**
     * 检查布局参数。
     * 
     * @param p 待检查布局参数。
     * @return 布局参数可正常使用返回true，否则返回false。
     */
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof AbsListView.LayoutParams;
    }

    /**
     * Puts the list or grid into transcript mode. In this mode the list or grid will always scroll
     * to the bottom to show new items.
     * 
     * @param mode the transcript mode to set
     * 
     * @see #TRANSCRIPT_MODE_DISABLED
     * @see #TRANSCRIPT_MODE_NORMAL
     * @see #TRANSCRIPT_MODE_ALWAYS_SCROLL
     */
    /**
     * 设置列表或网格的跳转模式。在这种模式下的列表或网格会滚动至底部显示的新项目。
     * 
     * @param mode 设置的模式
     * @see #TRANSCRIPT_MODE_DISABLED
     * @see #TRANSCRIPT_MODE_NORMAL
     * @see #TRANSCRIPT_MODE_ALWAYS_SCROLL
     */
    public void setTranscriptMode(int mode) {
        mTranscriptMode = mode;
    }

    /**
     * Returns the current transcript mode.
     * 
     * @return {@link #TRANSCRIPT_MODE_DISABLED}, {@link #TRANSCRIPT_MODE_NORMAL} or
     *         {@link #TRANSCRIPT_MODE_ALWAYS_SCROLL}
     */
    /**
     * 取得当前的跳转模式
     * 
     * @return {@link #TRANSCRIPT_MODE_DISABLED}、{@link #TRANSCRIPT_MODE_NORMAL}、
     *         {@link #TRANSCRIPT_MODE_ALWAYS_SCROLL}之一
     */
    public int getTranscriptMode() {
        return mTranscriptMode;
    }

    /**
     * 继承自{@link View#getSolidColor()}
     */
    @Override
    public int getSolidColor() {
        return mCacheColorHint;
    }

    /**
     * When set to a non-zero value, the cache color hint indicates that this list is always drawn
     * on top of a solid, single-color, opaque background.
     * 
     * Zero means that what's behind this object is translucent (non solid) or is not made of a
     * single color. This hint will not affect any existing background drawable set on this view (
     * typically set via {@link #setBackgroundDrawable(Drawable)}).
     * 
     * @param color The background color
     */
/**
     * 当设置为非零值时，缓存颜色意味着该列表总是在固定的、单色、不透明的背景上绘制。
     * 
     * 零意味着该对象的后面是渐变（非固定）色或者不是单色。该颜色暗示不影响已经设置在该视图上的既存背景可绘制对象（一般是通过{@link #setBackgroundDrawable(Drawable)设置的）。
     * @param color 颜色暗示 
     */
    public void setCacheColorHint(int color) {
        if (color != mCacheColorHint) {
            mCacheColorHint = color;
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                getChildAt(i).setDrawingCacheBackgroundColor(color);
            }
            mRecycler.setCacheColorHint(color);
        }
    }

    /**
     * When set to a non-zero value, the cache color hint indicates that this list is always drawn
     * on top of a solid, single-color, opaque background
     * 
     * @return The cache color hint
     */
    /**
     * 如果该值为非零，表示该视图总是在固定的、单色、不透明的背景上绘制。
     * 
     * @return 缓存的颜色。
     */
    public int getCacheColorHint() {
        return mCacheColorHint;
    }

    /**
     * Move all views (excluding headers and footers) held by this AbsListView into the supplied
     * List. This includes views displayed on the screen as well as views stored in AbsListView's
     * internal view recycler.
     * 
     * @param views A list into which to put the reclaimed views
     */
    /**
     * 将该AbsListView中的所有视图（不包含头尾视图）移到提供的列表中。这些视图包括显示在屏幕上的以及放入AbsListView内部视图回收器的视图。
     * 
     * @param views 用于填充视图的列表。
     */
    public void reclaimViews(List<View> views) {
        int childCount = getChildCount();
        RecyclerListener listener = mRecycler.mRecyclerListener;

        // Reclaim views on screen
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            AbsListView.LayoutParams lp = (AbsListView.LayoutParams) child.getLayoutParams();
            // Don't reclaim header or footer views, or views that should be ignored
            if (lp != null && mRecycler.shouldRecycleViewType(lp.viewType)) {
                views.add(child);
                child.setAccessibilityDelegate(null);
                if (listener != null) {
                    // Pretend they went through the scrap heap
                    listener.onMovedToScrapHeap(child);
                }
            }
        }
        mRecycler.reclaimScrapViews(views);
        removeAllViewsInLayout();
    }

    /**
     * Sets the recycler listener to be notified whenever a View is set aside in the recycler for
     * later reuse. This listener can be used to free resources associated to the View.
     * 
     * @param listener The recycler listener to be notified of views set aside in the recycler.
     * 
     * @see android.widget.AbsListView.RecycleBin
     * @see android.widget.AbsListView.RecyclerListener
     */
    /**
     * 设置当视图被放入回收器等待被重用时得到通知的回收监听器。 该监听器用于释放关联到视图的资源。
     * 
     * @param listener 视图被放入回收器时收到通知的回收监听器。
     * @see AbsListView.RecycleBin
     * @see AbsListView.RecyclerListener
     */
    public void setRecyclerListener(RecyclerListener listener) {
        mRecycler.mRecyclerListener = listener;
    }

    /**
     * 继承自{@link View#onSaveInstanceState()}
     */
    @Override
    public Parcelable onSaveInstanceState() {
        /*
         * This doesn't really make sense as the place to dismiss the popups, but there don't seem
         * to be any other useful hooks that happen early enough to keep from getting complaints
         * about having leaked the window.
         */
        // dismissPopup();

        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);

        if (mPendingSync != null) {
            // Just keep what we last restored.
            ss.selectedId = mPendingSync.selectedId;
            ss.firstId = mPendingSync.firstId;
            ss.viewTop = mPendingSync.viewTop;
            ss.position = mPendingSync.position;
            ss.height = mPendingSync.height;
            ss.filter = mPendingSync.filter;
            ss.inActionMode = mPendingSync.inActionMode;
            ss.checkedItemCount = mPendingSync.checkedItemCount;
            ss.checkState = mPendingSync.checkState;
            ss.checkIdState = mPendingSync.checkIdState;
            return ss;
        }

        boolean haveChildren = getChildCount() > 0 && mItemCount > 0;
        long selectedId = getSelectedItemId();
        ss.selectedId = selectedId;
        ss.height = getHeight();

        if (selectedId >= 0) {
            // Remember the selection
            ss.viewTop = mSelectedTop;
            ss.position = getSelectedItemPosition();
            ss.firstId = INVALID_POSITION;
        } else {
            if (haveChildren && mFirstPosition > 0) {
                // Remember the position of the first child.
                // We only do this if we are not currently at the top of
                // the list, for two reasons:
                // (1) The list may be in the process of becoming empty, in
                // which case mItemCount may not be 0, but if we try to
                // ask for any information about position 0 we will crash.
                // (2) Being "at the top" seems like a special case, anyway,
                // and the user wouldn't expect to end up somewhere else when
                // they revisit the list even if its content has changed.
                View v = getChildAt(0);
                ss.viewTop = v.getTop();
                int firstPos = mFirstPosition;
                if (firstPos >= mItemCount) {
                    firstPos = mItemCount - 1;
                }
                ss.position = firstPos;
                ss.firstId = mAdapter.getItemId(firstPos);
            } else {
                ss.viewTop = 0;
                ss.firstId = INVALID_POSITION;
                ss.position = 0;
            }
        }

        ss.filter = null;
        // if (mFiltered) {
        // final EditText textFilter = mTextFilter;
        // if (textFilter != null) {
        // Editable filterText = textFilter.getText();
        // if (filterText != null) {
        // ss.filter = filterText.toString();
        // }
        // }
        // }

        ss.inActionMode = mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL && mChoiceActionMode != null;

        if (mCheckStates != null) {
            ss.checkState = mCheckStates.clone();
        }
        if (mCheckedIdStates != null) {
            final LongSparseArray<Integer> idState = new LongSparseArray<Integer>();
            final int count = mCheckedIdStates.size();
            for (int i = 0; i < count; i++) {
                idState.put(mCheckedIdStates.keyAt(i), mCheckedIdStates.valueAt(i));
            }
            ss.checkIdState = idState;
        }
        ss.checkedItemCount = mCheckedItemCount;

        if (mRemoteAdapter != null) {
            mRemoteAdapter.saveRemoteViewsCache();
        }
        return ss;
    }

    /**
     * 继承自{@link View#onRestoreInstanceState(Parcelable)}
     */
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;

        super.onRestoreInstanceState(ss.getSuperState());
        mDataChanged = true;

        mSyncHeight = ss.height;

        if (ss.selectedId >= 0) {
            mNeedSync = true;
            mPendingSync = ss;
            mSyncRowId = ss.selectedId;
            mSyncPosition = ss.position;
            mSpecificTop = ss.viewTop;
            mSyncMode = SYNC_SELECTED_POSITION;
        } else if (ss.firstId >= 0) {
            setSelectedPositionInt(INVALID_POSITION);
            // Do this before setting mNeedSync since setNextSelectedPosition looks at mNeedSync
            setNextSelectedPositionInt(INVALID_POSITION);
            mSelectorPosition = INVALID_POSITION;
            mNeedSync = true;
            mPendingSync = ss;
            mSyncRowId = ss.firstId;
            mSyncPosition = ss.position;
            mSpecificTop = ss.viewTop;
            mSyncMode = SYNC_FIRST_POSITION;
        }

        // setFilterText(ss.filter);

        if (ss.checkState != null) {
            mCheckStates = ss.checkState;
        }

        if (ss.checkIdState != null) {
            mCheckedIdStates = ss.checkIdState;
        }

        mCheckedItemCount = ss.checkedItemCount;

        if (ss.inActionMode && mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL
                && mMultiChoiceModeCallback != null) {
            mChoiceActionMode = startActionMode(mMultiChoiceModeCallback);
        }

        requestLayout();
    }

    static class SavedState extends BaseSavedState {
        long selectedId;
        long firstId;
        int viewTop;
        int position;
        int height;
        String filter;
        boolean inActionMode;
        int checkedItemCount;
        SparseBooleanArray checkState;
        LongSparseArray<Integer> checkIdState;

        /**
         * Constructor called from {@link AbsListView#onSaveInstanceState()}
         */
        SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            selectedId = in.readLong();
            firstId = in.readLong();
            viewTop = in.readInt();
            position = in.readInt();
            height = in.readInt();
            filter = in.readString();
            inActionMode = in.readByte() != 0;
            checkedItemCount = in.readInt();
            checkState = in.readSparseBooleanArray();
            final int N = in.readInt();
            if (N > 0) {
                checkIdState = new LongSparseArray<Integer>();
                for (int i = 0; i < N; i++) {
                    final long key = in.readLong();
                    final int value = in.readInt();
                    checkIdState.put(key, value);
                }
            }
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeLong(selectedId);
            out.writeLong(firstId);
            out.writeInt(viewTop);
            out.writeInt(position);
            out.writeInt(height);
            out.writeString(filter);
            out.writeByte((byte) (inActionMode ? 1 : 0));
            out.writeInt(checkedItemCount);
            out.writeSparseBooleanArray(checkState);
            final int N = checkIdState != null ? checkIdState.size() : 0;
            out.writeInt(N);
            for (int i = 0; i < N; i++) {
                out.writeLong(checkIdState.keyAt(i));
                out.writeInt(checkIdState.valueAt(i));
            }
        }

        @Override
        public String toString() {
            return "AbsListView.SavedState{" + Integer.toHexString(System.identityHashCode(this))
                    + " selectedId=" + selectedId + " firstId=" + firstId + " viewTop=" + viewTop
                    + " position=" + position + " height=" + height + " filter=" + filter
                    + " checkState=" + checkState + "}";
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    /**
     * 返回一个从{@link AccessibilityService}的视图的点上得到的代表这个视图的{@link AccessibilityNodeInfo}。
     * 这个方法负责从一个可重用的实例池中获取一个辅助节点信息（{@link AccessibilityNodeInfo}）实例以及在这个视图初始化前调用
     * {@link View#onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo)}
     * 
     * 注意：客户端必须负责通过调用{@link AccessibilityNodeInfo#recycle()}来回收得到的实例以减少对象的创建。
     * 
     * @return 一个被填充的{@link AccessibilityNodeInfo}。
     */
    @Override
    public AccessibilityNodeInfo createAccessibilityNodeInfo() {
        // If the data changed the children are invalid since the data model changed.
        // Hence, we pretend they do not exist. After a layout the children will sync
        // with the model at which point we notify that the accessibility state changed,
        // so a service will be able to re-fetch the views.
        if (mDataChanged) {
            return null;
        }
        return super.createAccessibilityNodeInfo();
    }

    class ListItemAccessibilityDelegate extends AccessibilityDelegate {

        @Override
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(host, info);

            final int position = getPositionForView(host);
            final ListAdapter adapter = getAdapter();

            if ((position == INVALID_POSITION) || (adapter == null)) {
                return;
            }

            if (!isEnabled() || !adapter.isEnabled(position)) {
                return;
            }

            if (position == getSelectedItemPosition()) {
                info.setSelected(true);
                info.addAction(AccessibilityNodeInfo.ACTION_CLEAR_SELECTION);
            } else {
                info.addAction(AccessibilityNodeInfo.ACTION_SELECT);
            }

            if (isClickable()) {
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
                info.setClickable(true);
            }

            if (isLongClickable()) {
                info.addAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
                info.setLongClickable(true);
            }

        }

        /**
         * 继承自{@link AccessibilityDelegate#performAccessibilityAction(View, int, Bundle)}
         */
        @Override
        public boolean performAccessibilityAction(View host, int action, Bundle arguments) {
            if (super.performAccessibilityAction(host, action, arguments)) {
                return true;
            }

            final int position = getPositionForView(host);
            final ListAdapter adapter = getAdapter();

            if ((position == INVALID_POSITION) || (adapter == null)) {
                // Cannot perform actions on invalid items.
                return false;
            }

            if (!isEnabled() || !adapter.isEnabled(position)) {
                // Cannot perform actions on disabled items.
                return false;
            }

            final long id = getItemIdAtPosition(position);

            switch (action) {
            case AccessibilityNodeInfo.ACTION_CLEAR_SELECTION: {
                if (getSelectedItemPosition() == position) {
                    setSelection(INVALID_POSITION);
                    return true;
                }
            }
                return false;
            case AccessibilityNodeInfo.ACTION_SELECT: {
                if (getSelectedItemPosition() != position) {
                    setSelection(position);
                    return true;
                }
            }
                return false;
            case AccessibilityNodeInfo.ACTION_CLICK: {
                if (isClickable()) {
                    return performItemClick(host, position, id);
                }
            }
                return false;
            case AccessibilityNodeInfo.ACTION_LONG_CLICK: {
                if (isLongClickable()) {
                    return performLongPress(host, position, id);
                }
            }
                return false;
            }

            return false;
        }
    }

    /**
     * A base class for Runnables that will check that their view is still attached to the original
     * window as when the Runnable was created.
     * 
     */
    private class WindowRunnnable {
        private int mOriginalAttachCount;

        public void rememberWindowAttachCount() {
            mOriginalAttachCount = getWindowAttachCount();
        }

        public boolean sameWindow() {
            return hasWindowFocus() && getWindowAttachCount() == mOriginalAttachCount;
        }
    }

    private class PerformClick extends WindowRunnnable implements Runnable {
        int mClickMotionPosition;

        public void run() {
            // The data has changed since we posted this action in the event queue,
            // bail out before bad things happen
            if (mDataChanged) return;

            final ListAdapter adapter = mAdapter;
            final int motionPosition = mClickMotionPosition;
            if (adapter != null && mItemCount > 0 && motionPosition != INVALID_POSITION
                    && motionPosition < adapter.getCount() && sameWindow()) {
                final View view = getChildAt(motionPosition - mFirstPosition);
                // If there is no view, something bad happened (the view scrolled off the
                // screen, etc.) and we should cancel the click
                if (view != null) {
                    performItemClick(view, motionPosition, adapter.getItemId(motionPosition));
                }
            }
        }
    }

    private class CheckForLongPress extends WindowRunnnable implements Runnable {
        public void run() {
            final int motionPosition = mMotionPosition;
            final View child = getChildAt(motionPosition - mFirstPosition);
            if (child != null) {
                final int longPressPosition = mMotionPosition;
                final long longPressId = mAdapter.getItemId(mMotionPosition);

                boolean handled = false;
                if (sameWindow() && !mDataChanged) {
                    handled = performLongPress(child, longPressPosition, longPressId);
                }
                if (handled) {
                    mTouchMode = TOUCH_MODE_REST;
                    setPressed(false);
                    child.setPressed(false);
                } else {
                    mTouchMode = TOUCH_MODE_DONE_WAITING;
                }
            }
        }
    }

    private class CheckForKeyLongPress extends WindowRunnnable implements Runnable {
        public void run() {
            if (isPressed() && mSelectedPosition >= 0) {
                int index = mSelectedPosition - mFirstPosition;
                View v = getChildAt(index);

                if (!mDataChanged) {
                    boolean handled = false;
                    if (sameWindow()) {
                        handled = performLongPress(v, mSelectedPosition, mSelectedRowId);
                    }
                    if (handled) {
                        setPressed(false);
                        v.setPressed(false);
                    }
                } else {
                    setPressed(false);
                    if (v != null) v.setPressed(false);
                }
            }
        }
    }

    final class CheckForTap implements Runnable {
        public void run() {
            if (mTouchMode == TOUCH_MODE_DOWN) {
                mTouchMode = TOUCH_MODE_TAP;
                final View child = getChildAt(mMotionPosition - mFirstPosition);
                if (child != null && !child.hasFocusable()) {
                    mLayoutMode = LAYOUT_NORMAL;

                    if (!mDataChanged) {
                        child.setPressed(true);
                        setPressed(true);
                        layoutChildren();
                        positionSelector(mMotionPosition, child);
                        refreshDrawableState();

                        final int longPressTimeout = ViewConfiguration.getLongPressTimeout();
                        final boolean longClickable = isLongClickable();

                        if (mSelector != null) {
                            Drawable d = mSelector.getCurrent();
                            if (d != null && d instanceof TransitionDrawable) {
                                if (longClickable) {
                                    ((TransitionDrawable) d).startTransition(longPressTimeout);
                                } else {
                                    ((TransitionDrawable) d).resetTransition();
                                }
                            }
                        }

                        if (longClickable) {
                            if (mPendingCheckForLongPress == null) {
                                mPendingCheckForLongPress = new CheckForLongPress();
                            }
                            mPendingCheckForLongPress.rememberWindowAttachCount();
                            postDelayed(mPendingCheckForLongPress, longPressTimeout);
                        } else {
                            mTouchMode = TOUCH_MODE_DONE_WAITING;
                        }
                    } else {
                        mTouchMode = TOUCH_MODE_DONE_WAITING;
                    }
                }
            }
        }
    }

    boolean mIsCubiced = false;

    abstract int cubicDistance(int distance);

    /**
     * Responsible for fling behavior. Use {@link #start(int)} to initiate a fling. Each frame of
     * the fling is handled in {@link #run()}. A FlingRunnable will keep re-posting itself until the
     * fling is done.
     * 
     */
    private class FlingRunnable implements Runnable {
        /**
         * Tracks the decay of a fling scroll
         */
        private final OverScroller mScroller;

        /**
         * Y value reported by mScroller on the previous fling
         */
        private int mLastFlingY;

        private final Runnable mCheckFlywheel = new Runnable() {
            public void run() {
                final int activeId = mActivePointerId;
                final VelocityTracker vt = mVelocityTracker;
                final OverScroller scroller = mScroller;
                if (vt == null || activeId == INVALID_POINTER) {
                    return;
                }

                vt.computeCurrentVelocity(1000, mMaximumVelocity);
                final float yvel = -vt.getYVelocity(activeId);

                if (Math.abs(yvel) >= mMinimumVelocity
                        && isScrollingInDirectionOfHide(scroller, 0, yvel)) { // scroller.isScrollingInDirection(0,
                                                                              // yvel)
                    // Keep the fling alive a little longer
                    postDelayed(this, FLYWHEEL_TIMEOUT);
                } else {
                    endFling();
                    mTouchMode = TOUCH_MODE_SCROLL;
                    reportScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                }
            }

            private boolean isScrollingInDirectionOfHide(OverScroller scroller, float xvel,
                    float yvel) {
                Class<?> cls = scroller.getClass();
                try {
                    Method method =
                            cls.getDeclaredMethod("isScrollingInDirection", float.class,
                                    float.class);
                    method.setAccessible(true);
                    return (Boolean) method.invoke(scroller, xvel, yvel);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };

        private static final int FLYWHEEL_TIMEOUT = 40; // milliseconds

        FlingRunnable() {
            mScroller = new OverScroller(getContext());
        }

        private void setInterpolatorOfNotPublic(Interpolator interpolator) {
            OverScroller scroller = mScroller;
            Class<?> cls = scroller.getClass();

            try {
                Method method = cls.getDeclaredMethod("setInterpolator", Interpolator.class);
                method.setAccessible(true);
                method.invoke(scroller, interpolator);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void start(int initialVelocity) {
            int initialY = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
            mLastFlingY = initialY;
            // @Kevin fix scroll
            int distance = cubicDistance(velocity2Distance(initialVelocity));

            if (mIsCubiced) {
                startScroll(distance, getDuration(distance), false);
                return;
            }
            setInterpolatorOfNotPublic(null);// mScroller.setInterpolator(null);
            mScroller.fling(0, initialY, 0, initialVelocity, 0, Integer.MAX_VALUE, 0,
                    Integer.MAX_VALUE);

            mTouchMode = TOUCH_MODE_FLING;
            postOnAnimation(this);

            if (PROFILE_FLINGING) {
                if (!mFlingProfilingStarted) {
                    Debug.startMethodTracing("AbsListViewFling");
                    mFlingProfilingStarted = true;
                }
            }

            // if (mFlingStrictSpan == null) {
            // mFlingStrictSpan = StrictMode.enterCriticalSpan("AbsListView-fling");
            // }
        }

        void startSpringback() {
            if (mScroller.springBack(0, getScrollY(), 0, 0, 0, 0)) {
                mTouchMode = TOUCH_MODE_OVERFLING;
                invalidate();
                postOnAnimation(this);
            } else {
                mTouchMode = TOUCH_MODE_REST;
                reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
            }
        }

        void startOverfling(int initialVelocity) {
            setInterpolatorOfNotPublic(null);// mScroller.setInterpolator(null);
            mScroller.fling(0, getScrollY(), 0, initialVelocity, 0, 0, Integer.MIN_VALUE,
                    Integer.MAX_VALUE, 0, getHeight());
            mTouchMode = TOUCH_MODE_OVERFLING;
            invalidate();
            postOnAnimation(this);
        }

        void edgeReached(int delta) {
            mScroller.notifyVerticalEdgeReached(getScrollY(), 0, mOverflingDistance);
            final int overscrollMode = getOverScrollMode();
            if (overscrollMode == OVER_SCROLL_ALWAYS
                    || (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && !contentFits())) {
                mTouchMode = TOUCH_MODE_OVERFLING;
                final int vel = (int) mScroller.getCurrVelocity();
                // if (delta > 0) {
                // mEdgeGlowTop.onAbsorb(vel);
                // } else {
                // mEdgeGlowBottom.onAbsorb(vel);
                // }
            } else {
                mTouchMode = TOUCH_MODE_REST;
                if (mPositionScroller != null) {
                    mPositionScroller.stop();
                }
            }
            invalidate();
            postOnAnimation(this);
        }

        void startScroll(int distance, int duration, boolean linear) {
            int initialY = distance < 0 ? Integer.MAX_VALUE : 0;
            mLastFlingY = initialY;
            setInterpolatorOfNotPublic(linear ? sLinearInterpolator : null);// mScroller.setInterpolator(linear
                                                                            // ? sLinearInterpolator
                                                                            // : null);
            mScroller.startScroll(0, initialY, 0, distance, duration);
            mTouchMode = TOUCH_MODE_FLING;
            postOnAnimation(this);
        }

        void endFling() {
            mIsCubiced = false;
            mTouchMode = TOUCH_MODE_REST;

            removeCallbacks(this);
            removeCallbacks(mCheckFlywheel);

            reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
            clearScrollingCache();
            mScroller.abortAnimation();

            // if (mFlingStrictSpan != null) {
            // mFlingStrictSpan.finish();
            // mFlingStrictSpan = null;
            // }
        }

        void flywheelTouch() {
            postDelayed(mCheckFlywheel, FLYWHEEL_TIMEOUT);
        }

        public void run() {
            switch (mTouchMode) {
            default:
                endFling();
                return;

            case TOUCH_MODE_SCROLL:
                if (mScroller.isFinished()) {
                    return;
                }
                // Fall through
            case TOUCH_MODE_FLING: {
                if (mDataChanged) {
                    layoutChildren();
                }

                if (mItemCount == 0 || getChildCount() == 0) {
                    endFling();
                    return;
                }

                final OverScroller scroller = mScroller;
                boolean more = scroller.computeScrollOffset();
                final int y = scroller.getCurrY();

                // Flip sign to convert finger direction to list items direction
                // (e.g. finger moving down means list is moving towards the top)
                int delta = mLastFlingY - y;

                int paddingBottom = getPaddingBottom();
                int paddingTop = getPaddingTop();
                // Pretend that each frame of a fling scroll is a touch scroll
                if (delta > 0) {
                    // List is moving towards the top. Use first view as mMotionPosition
                    mMotionPosition = mFirstPosition;
                    final View firstView = getChildAt(0);
                    mMotionViewOriginalTop = firstView.getTop();

                    // Don't fling more than 1 screen
                    delta = Math.min(getHeight() - paddingBottom - paddingTop - 1, delta);
                } else {
                    // List is moving towards the bottom. Use last view as mMotionPosition
                    int offsetToLast = getChildCount() - 1;
                    mMotionPosition = mFirstPosition + offsetToLast;

                    final View lastView = getChildAt(offsetToLast);
                    mMotionViewOriginalTop = lastView.getTop();

                    // Don't fling more than 1 screen
                    delta = Math.max(-(getHeight() - paddingBottom - paddingTop - 1), delta);
                }

                // Check to see if we have bumped into the scroll limit
                View motionView = getChildAt(mMotionPosition - mFirstPosition);
                int oldTop = 0;
                if (motionView != null) {
                    oldTop = motionView.getTop();
                }

                // Don't stop just because delta is zero (it could have been rounded)
                final boolean atEdge = trackMotionScroll(delta, delta);
                final boolean atEnd = atEdge && (delta != 0);
                if (atEnd) {
                    if (motionView != null) {
                        // Tweak the scroll for how far we overshot
                        int overshoot = -(delta - (motionView.getTop() - oldTop));
                        overScrollBy(0, overshoot, 0, getScrollY(), 0, 0, 0, mOverflingDistance,
                                false);
                    }
                    if (more) {
                        edgeReached(delta);
                    }
                    break;
                }

                if (more && !atEnd) {
                    if (atEdge) invalidate();
                    mLastFlingY = y;
                    postOnAnimation(this);
                } else {
                    endFling();

                    if (PROFILE_FLINGING) {
                        if (mFlingProfilingStarted) {
                            Debug.stopMethodTracing();
                            mFlingProfilingStarted = false;
                        }

                        // if (mFlingStrictSpan != null) {
                        // mFlingStrictSpan.finish();
                        // mFlingStrictSpan = null;
                        // }
                    }
                }
                break;
            }

            case TOUCH_MODE_OVERFLING: {
                final OverScroller scroller = mScroller;
                if (scroller.computeScrollOffset()) {
                    final int scrollY = getScrollY();
                    final int currY = scroller.getCurrY();
                    final int deltaY = currY - scrollY;
                    if (overScrollBy(0, deltaY, 0, scrollY, 0, 0, 0, mOverflingDistance, false)) {
                        final boolean crossDown = scrollY <= 0 && currY > 0;
                        final boolean crossUp = scrollY >= 0 && currY < 0;
                        if (crossDown || crossUp) {
                            int velocity = (int) scroller.getCurrVelocity();
                            if (crossUp) velocity = -velocity;

                            // Don't flywheel from this; we're just continuing things.
                            scroller.abortAnimation();
                            start(velocity);
                        } else {
                            startSpringback();
                        }
                    } else {
                        invalidate();
                        postOnAnimation(this);
                    }
                } else {
                    endFling();
                }
                break;
            }
            }
        }
    }

    class PositionScroller implements Runnable {
        private static final int SCROLL_DURATION = 200;

        private static final int MOVE_DOWN_POS = 1;
        private static final int MOVE_UP_POS = 2;
        private static final int MOVE_DOWN_BOUND = 3;
        private static final int MOVE_UP_BOUND = 4;
        private static final int MOVE_OFFSET = 5;

        private int mMode;
        private int mTargetPos;
        private int mBoundPos;
        private int mLastSeenPos;
        private int mScrollDuration;
        private final int mExtraScroll;

        private int mOffsetFromTop;

        PositionScroller() {
            mExtraScroll = ViewConfiguration.get(getContext()).getScaledFadingEdgeLength();
        }

        void start(final int position) {
            stop();

            if (mDataChanged) {
                // Wait until we're back in a stable state to try this.
                mPositionScrollAfterLayout = new Runnable() {
                    @Override
                    public void run() {
                        start(position);
                    }
                };
                return;
            }

            final int childCount = getChildCount();
            if (childCount == 0) {
                // Can't scroll without children.
                return;
            }

            final int firstPos = mFirstPosition;
            final int lastPos = firstPos + childCount - 1;

            int viewTravelCount;
            int clampedPosition = Math.max(0, Math.min(getCount() - 1, position));
            if (clampedPosition < firstPos) {
                viewTravelCount = firstPos - clampedPosition + 1;
                mMode = MOVE_UP_POS;
            } else if (clampedPosition > lastPos) {
                viewTravelCount = clampedPosition - lastPos + 1;
                mMode = MOVE_DOWN_POS;
            } else {
                scrollToVisible(clampedPosition, INVALID_POSITION, SCROLL_DURATION);
                return;
            }

            if (viewTravelCount > 0) {
                mScrollDuration = SCROLL_DURATION / viewTravelCount;
            } else {
                mScrollDuration = SCROLL_DURATION;
            }
            mTargetPos = clampedPosition;
            mBoundPos = INVALID_POSITION;
            mLastSeenPos = INVALID_POSITION;

            postOnAnimation(this);
        }

        void start(final int position, final int boundPosition) {
            stop();

            if (boundPosition == INVALID_POSITION) {
                start(position);
                return;
            }

            if (mDataChanged) {
                // Wait until we're back in a stable state to try this.
                mPositionScrollAfterLayout = new Runnable() {
                    @Override
                    public void run() {
                        start(position, boundPosition);
                    }
                };
                return;
            }

            final int childCount = getChildCount();
            if (childCount == 0) {
                // Can't scroll without children.
                return;
            }

            final int firstPos = mFirstPosition;
            final int lastPos = firstPos + childCount - 1;

            int viewTravelCount;
            int clampedPosition = Math.max(0, Math.min(getCount() - 1, position));
            if (clampedPosition < firstPos) {
                final int boundPosFromLast = lastPos - boundPosition;
                if (boundPosFromLast < 1) {
                    // Moving would shift our bound position off the screen. Abort.
                    return;
                }

                final int posTravel = firstPos - clampedPosition + 1;
                final int boundTravel = boundPosFromLast - 1;
                if (boundTravel < posTravel) {
                    viewTravelCount = boundTravel;
                    mMode = MOVE_UP_BOUND;
                } else {
                    viewTravelCount = posTravel;
                    mMode = MOVE_UP_POS;
                }
            } else if (clampedPosition > lastPos) {
                final int boundPosFromFirst = boundPosition - firstPos;
                if (boundPosFromFirst < 1) {
                    // Moving would shift our bound position off the screen. Abort.
                    return;
                }

                final int posTravel = clampedPosition - lastPos + 1;
                final int boundTravel = boundPosFromFirst - 1;
                if (boundTravel < posTravel) {
                    viewTravelCount = boundTravel;
                    mMode = MOVE_DOWN_BOUND;
                } else {
                    viewTravelCount = posTravel;
                    mMode = MOVE_DOWN_POS;
                }
            } else {
                scrollToVisible(clampedPosition, boundPosition, SCROLL_DURATION);
                return;
            }

            if (viewTravelCount > 0) {
                mScrollDuration = SCROLL_DURATION / viewTravelCount;
            } else {
                mScrollDuration = SCROLL_DURATION;
            }
            mTargetPos = clampedPosition;
            mBoundPos = boundPosition;
            mLastSeenPos = INVALID_POSITION;

            postOnAnimation(this);
        }

        void startWithOffset(int position, int offset) {
            startWithOffset(position, offset, SCROLL_DURATION);
        }

        void startWithOffset(final int position, int offset, final int duration) {
            stop();

            if (mDataChanged) {
                // Wait until we're back in a stable state to try this.
                final int postOffset = offset;
                mPositionScrollAfterLayout = new Runnable() {
                    @Override
                    public void run() {
                        startWithOffset(position, postOffset, duration);
                    }
                };
                return;
            }

            final int childCount = getChildCount();
            if (childCount == 0) {
                // Can't scroll without children.
                return;
            }

            offset += getPaddingTop();

            mTargetPos = Math.max(0, Math.min(getCount() - 1, position));
            mOffsetFromTop = offset;
            mBoundPos = INVALID_POSITION;
            mLastSeenPos = INVALID_POSITION;
            mMode = MOVE_OFFSET;

            final int firstPos = mFirstPosition;
            final int lastPos = firstPos + childCount - 1;

            int viewTravelCount;
            if (mTargetPos < firstPos) {
                viewTravelCount = firstPos - mTargetPos;
            } else if (mTargetPos > lastPos) {
                viewTravelCount = mTargetPos - lastPos;
            } else {
                // On-screen, just scroll.
                final int targetTop = getChildAt(mTargetPos - firstPos).getTop();
                smoothScrollBy(targetTop - offset, duration, true);
                return;
            }

            // Estimate how many screens we should travel
            final float screenTravelCount = (float) viewTravelCount / childCount;
            mScrollDuration =
                    screenTravelCount < 1 ? duration : (int) (duration / screenTravelCount);
            mLastSeenPos = INVALID_POSITION;

            postOnAnimation(this);
        }

        /**
         * Scroll such that targetPos is in the visible padded region without scrolling boundPos out
         * of view. Assumes targetPos is onscreen.
         */
        void scrollToVisible(int targetPos, int boundPos, int duration) {
            final int firstPos = mFirstPosition;
            final int childCount = getChildCount();
            final int lastPos = firstPos + childCount - 1;
            final int paddedTop = mListPadding.top;
            final int paddedBottom = getHeight() - mListPadding.bottom;

            if (targetPos < firstPos || targetPos > lastPos) {
                Log.w(TAG, "scrollToVisible called with targetPos " + targetPos + " not visible ["
                        + firstPos + ", " + lastPos + "]");
            }
            if (boundPos < firstPos || boundPos > lastPos) {
                // boundPos doesn't matter, it's already offscreen.
                boundPos = INVALID_POSITION;
            }

            final View targetChild = getChildAt(targetPos - firstPos);
            final int targetTop = targetChild.getTop();
            final int targetBottom = targetChild.getBottom();
            int scrollBy = 0;

            if (targetBottom > paddedBottom) {
                scrollBy = targetBottom - paddedBottom;
            }
            if (targetTop < paddedTop) {
                scrollBy = targetTop - paddedTop;
            }

            if (scrollBy == 0) {
                return;
            }

            if (boundPos >= 0) {
                final View boundChild = getChildAt(boundPos - firstPos);
                final int boundTop = boundChild.getTop();
                final int boundBottom = boundChild.getBottom();
                final int absScroll = Math.abs(scrollBy);

                if (scrollBy < 0 && boundBottom + absScroll > paddedBottom) {
                    // Don't scroll the bound view off the bottom of the screen.
                    scrollBy = Math.max(0, boundBottom - paddedBottom);
                } else if (scrollBy > 0 && boundTop - absScroll < paddedTop) {
                    // Don't scroll the bound view off the top of the screen.
                    scrollBy = Math.min(0, boundTop - paddedTop);
                }
            }

            smoothScrollBy(scrollBy, duration);
        }

        void stop() {
            removeCallbacks(this);
        }

        public void run() {
            final int listHeight = getHeight();
            final int firstPos = mFirstPosition;

            switch (mMode) {
            case MOVE_DOWN_POS: {
                final int lastViewIndex = getChildCount() - 1;
                final int lastPos = firstPos + lastViewIndex;

                if (lastViewIndex < 0) {
                    return;
                }

                if (lastPos == mLastSeenPos) {
                    // No new views, let things keep going.
                    postOnAnimation(this);
                    return;
                }

                final View lastView = getChildAt(lastViewIndex);
                final int lastViewHeight = lastView.getHeight();
                final int lastViewTop = lastView.getTop();
                final int lastViewPixelsShowing = listHeight - lastViewTop;
                final int extraScroll =
                        lastPos < mItemCount - 1 ? Math.max(mListPadding.bottom, mExtraScroll)
                                : mListPadding.bottom;

                final int scrollBy = lastViewHeight - lastViewPixelsShowing + extraScroll;
                smoothScrollBy(scrollBy, mScrollDuration, true);

                mLastSeenPos = lastPos;
                if (lastPos < mTargetPos) {
                    postOnAnimation(this);
                }
                break;
            }

            case MOVE_DOWN_BOUND: {
                final int nextViewIndex = 1;
                final int childCount = getChildCount();

                if (firstPos == mBoundPos || childCount <= nextViewIndex
                        || firstPos + childCount >= mItemCount) {
                    return;
                }
                final int nextPos = firstPos + nextViewIndex;

                if (nextPos == mLastSeenPos) {
                    // No new views, let things keep going.
                    postOnAnimation(this);
                    return;
                }

                final View nextView = getChildAt(nextViewIndex);
                final int nextViewHeight = nextView.getHeight();
                final int nextViewTop = nextView.getTop();
                final int extraScroll = Math.max(mListPadding.bottom, mExtraScroll);
                if (nextPos < mBoundPos) {
                    smoothScrollBy(Math.max(0, nextViewHeight + nextViewTop - extraScroll),
                            mScrollDuration, true);

                    mLastSeenPos = nextPos;

                    postOnAnimation(this);
                } else {
                    if (nextViewTop > extraScroll) {
                        smoothScrollBy(nextViewTop - extraScroll, mScrollDuration, true);
                    }
                }
                break;
            }

            case MOVE_UP_POS: {
                if (firstPos == mLastSeenPos) {
                    // No new views, let things keep going.
                    postOnAnimation(this);
                    return;
                }

                final View firstView = getChildAt(0);
                if (firstView == null) {
                    return;
                }
                final int firstViewTop = firstView.getTop();
                final int extraScroll =
                        firstPos > 0 ? Math.max(mExtraScroll, mListPadding.top) : mListPadding.top;

                smoothScrollBy(firstViewTop - extraScroll, mScrollDuration, true);

                mLastSeenPos = firstPos;

                if (firstPos > mTargetPos) {
                    postOnAnimation(this);
                }
                break;
            }

            case MOVE_UP_BOUND: {
                final int lastViewIndex = getChildCount() - 2;
                if (lastViewIndex < 0) {
                    return;
                }
                final int lastPos = firstPos + lastViewIndex;

                if (lastPos == mLastSeenPos) {
                    // No new views, let things keep going.
                    postOnAnimation(this);
                    return;
                }

                final View lastView = getChildAt(lastViewIndex);
                final int lastViewHeight = lastView.getHeight();
                final int lastViewTop = lastView.getTop();
                final int lastViewPixelsShowing = listHeight - lastViewTop;
                final int extraScroll = Math.max(mListPadding.top, mExtraScroll);
                mLastSeenPos = lastPos;
                if (lastPos > mBoundPos) {
                    smoothScrollBy(-(lastViewPixelsShowing - extraScroll), mScrollDuration, true);
                    postOnAnimation(this);
                } else {
                    final int bottom = listHeight - extraScroll;
                    final int lastViewBottom = lastViewTop + lastViewHeight;
                    if (bottom > lastViewBottom) {
                        smoothScrollBy(-(bottom - lastViewBottom), mScrollDuration, true);
                    }
                }
                break;
            }

            case MOVE_OFFSET: {
                if (mLastSeenPos == firstPos) {
                    // No new views, let things keep going.
                    postOnAnimation(this);
                    return;
                }

                mLastSeenPos = firstPos;

                final int childCount = getChildCount();
                final int position = mTargetPos;
                final int lastPos = firstPos + childCount - 1;

                int viewTravelCount = 0;
                if (position < firstPos) {
                    viewTravelCount = firstPos - position + 1;
                } else if (position > lastPos) {
                    viewTravelCount = position - lastPos;
                }

                // Estimate how many screens we should travel
                final float screenTravelCount = (float) viewTravelCount / childCount;

                final float modifier = Math.min(Math.abs(screenTravelCount), 1.f);
                if (position < firstPos) {
                    final int distance = (int) (-getHeight() * modifier);
                    final int duration = (int) (mScrollDuration * modifier);
                    smoothScrollBy(distance, duration, true);
                    postOnAnimation(this);
                } else if (position > lastPos) {
                    final int distance = (int) (getHeight() * modifier);
                    final int duration = (int) (mScrollDuration * modifier);
                    smoothScrollBy(distance, duration, true);
                    postOnAnimation(this);
                } else {
                    // On-screen, just scroll.
                    final int targetTop = getChildAt(position - firstPos).getTop();
                    final int distance = targetTop - mOffsetFromTop;
                    final int duration =
                            (int) (mScrollDuration * ((float) Math.abs(distance) / getHeight()));
                    smoothScrollBy(distance, duration, true);
                }
                break;
            }

            default:
                break;
            }
        }
    }

    /**
     * A MultiChoiceModeListener receives events for {@link AbsListView#CHOICE_MODE_MULTIPLE_MODAL}.
     * It acts as the {@link ActionMode.Callback} for the selection mode and also receives
     * {@link #onItemCheckedStateChanged(ActionMode, int, long, boolean)} events when the user
     * selects and deselects list items.
     */
/**
     * MultiChoiceModeListener为{@link #CHOICE_MODE_MULTIPLE_MODAL接收事件。作为选择模式的回调函数（
     * 
     * @link ActionMode.Callback}），当用户选中/解除列表条目选中状态时，接收
     *       {@link #onItemCheckedStateChanged(ActionMode, int, long, boolean)}事件。
     */
    public interface MultiChoiceModeListener extends ActionMode.Callback {
        /**
         * Called when an item is checked or unchecked during selection mode.
         * 
         * @param mode The {@link ActionMode} providing the selection mode
         * @param position Adapter position of the item that was checked or unchecked
         * @param id Adapter ID of the item that was checked or unchecked
         * @param checked <code>true</code> if the item is now checked, <code>false</code> if the
         *        item is now unchecked.
         */
        /**
         * 处于选择模式时，选中/解除时调用。
         * 
         * @param mode 提供选择模式的{@link ActionMode}
         * @param position 选中/解除条目的适配器位置
         * @param id 选中/解除条目的适配器ID
         * @param checked 如果条目处于选中状态则为<code>true</code>；处于解除状态则为<code>false</code>。
         */
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                boolean checked);
    }

    class MultiChoiceModeWrapper implements MultiChoiceModeListener {
        private MultiChoiceModeListener mWrapped;

        public void setWrapped(MultiChoiceModeListener wrapped) {
            mWrapped = wrapped;
        }

        public boolean hasWrappedCallback() {
            return mWrapped != null;
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (mWrapped.onCreateActionMode(mode, menu)) {
                // Initialize checked graphic state?
                setLongClickable(false);
                return true;
            }
            return false;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return mWrapped.onPrepareActionMode(mode, menu);
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return mWrapped.onActionItemClicked(mode, item);
        }

        public void onDestroyActionMode(ActionMode mode) {
            mWrapped.onDestroyActionMode(mode);
            mChoiceActionMode = null;

            // Ending selection mode means deselecting everything.
            clearChoices();

            mDataChanged = true;
            rememberSyncState();
            requestLayout();

            setLongClickable(true);
        }

        public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                boolean checked) {
            mWrapped.onItemCheckedStateChanged(mode, position, id, checked);

            // If there are no items selected we no longer need the selection mode.
            if (getCheckedItemCount() == 0) {
                mode.finish();
            }
        }
    }

    /**
     * AbsListView extends LayoutParams to provide a place to hold the view type.
     */
    /**
     * AbsListView 扩展了 LayoutParams 以提供空间来保存试图类型。
     */
    public static class LayoutParams extends ViewGroup.LayoutParams {
        /**
         * View type for this view, as returned by
         * {@link android.widget.Adapter#getItemViewType(int) }
         */
        int viewType;

        /**
         * When this boolean is set, the view has been added to the AbsListView at least once. It is
         * used to know whether headers/footers have already been added to the list view and whether
         * they should be treated as recycled views or not.
         */
        boolean recycledHeaderFooter;

        /**
         * When an AbsListView is measured with an AT_MOST measure spec, it needs to obtain children
         * views to measure itself. When doing so, the children are not attached to the window, but
         * put in the recycler which assumes they've been attached before. Setting this flag will
         * force the reused view to be attached to the window rather than just attached to the
         * parent.
         */
        boolean forceAdd;

        /**
         * The position the view was removed from when pulled out of the scrap heap.
         * 
         * @hide
         */
        int scrappedFromPosition;

        /**
         * The ID the view represents
         */
        long itemId = -1;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(int w, int h, int viewType) {
            super(w, h);
            this.viewType = viewType;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    /**
     * A RecyclerListener is used to receive a notification whenever a View is placed inside the
     * RecycleBin's scrap heap. This listener is used to free resources associated to Views placed
     * in the RecycleBin.
     * 
     * @see android.widget.AbsListView.RecycleBin
     * @see android.widget.AbsListView#setRecyclerListener(android.widget.AbsListView.RecyclerListener)
     */
    /**
     * RecyclerListener 是用于接收视图被移动到待回收堆中时的消息的监听器。该监听器用于释放分配到被放入回收站的视图的资源。
     */
    public static interface RecyclerListener {
        /**
         * Indicates that the specified View was moved into the recycler's scrap heap. The view is
         * not displayed on screen any more and any expensive resource associated with the view
         * should be discarded.
         * 
         * @param view
         */
        /**
         * 指示指定的视图被移动到待回收堆中。 视图不再显示在屏幕上，关联到该视图的贵重的资源应该被释放。
         * 
         * @param view
         */
        void onMovedToScrapHeap(View view);
    }

    /**
     * The RecycleBin facilitates reuse of views across layouts. The RecycleBin has two levels of
     * storage: ActiveViews and ScrapViews. ActiveViews are those views which were onscreen at the
     * start of a layout. By construction, they are displaying current information. At the end of
     * layout, all views in ActiveViews are demoted to ScrapViews. ScrapViews are old views that
     * could potentially be used by the adapter to avoid allocating views unnecessarily.
     * 
     * @see android.widget.AbsListView#setRecyclerListener(android.widget.AbsListView.RecyclerListener)
     * @see android.widget.AbsListView.RecyclerListener
     */
    class RecycleBin {
        private RecyclerListener mRecyclerListener;

        /**
         * The position of the first view stored in mActiveViews.
         */
        private int mFirstActivePosition;

        /**
         * Views that were on screen at the start of layout. This array is populated at the start of
         * layout, and at the end of layout all view in mActiveViews are moved to mScrapViews. Views
         * in mActiveViews represent a contiguous range of Views, with position of the first view
         * store in mFirstActivePosition.
         */
        private View[] mActiveViews = new View[0];

        /**
         * Unsorted views that can be used by the adapter as a convert view.
         */
        private ArrayList<View>[] mScrapViews;

        private int mViewTypeCount;

        private ArrayList<View> mCurrentScrap;

        private ArrayList<View> mSkippedScrap;

        private SparseArray<View> mTransientStateViews;
        private LongSparseArray<View> mTransientStateViewsById;

        public void setViewTypeCount(int viewTypeCount) {
            if (viewTypeCount < 1) {
                throw new IllegalArgumentException("Can't have a viewTypeCount < 1");
            }
            // noinspection unchecked
            ArrayList<View>[] scrapViews = new ArrayList[viewTypeCount];
            for (int i = 0; i < viewTypeCount; i++) {
                scrapViews[i] = new ArrayList<View>();
            }
            mViewTypeCount = viewTypeCount;
            mCurrentScrap = scrapViews[0];
            mScrapViews = scrapViews;
        }

        public void markChildrenDirty() {
            if (mViewTypeCount == 1) {
                final ArrayList<View> scrap = mCurrentScrap;
                final int scrapCount = scrap.size();
                for (int i = 0; i < scrapCount; i++) {
                    scrap.get(i).forceLayout();
                }
            } else {
                final int typeCount = mViewTypeCount;
                for (int i = 0; i < typeCount; i++) {
                    final ArrayList<View> scrap = mScrapViews[i];
                    final int scrapCount = scrap.size();
                    for (int j = 0; j < scrapCount; j++) {
                        scrap.get(j).forceLayout();
                    }
                }
            }
            if (mTransientStateViews != null) {
                final int count = mTransientStateViews.size();
                for (int i = 0; i < count; i++) {
                    mTransientStateViews.valueAt(i).forceLayout();
                }
            }
            if (mTransientStateViewsById != null) {
                final int count = mTransientStateViewsById.size();
                for (int i = 0; i < count; i++) {
                    mTransientStateViewsById.valueAt(i).forceLayout();
                }
            }
        }

        public boolean shouldRecycleViewType(int viewType) {
            return viewType >= 0;
        }

        /**
         * Clears the scrap heap.
         */
        void clear() {
            if (mViewTypeCount == 1) {
                final ArrayList<View> scrap = mCurrentScrap;
                final int scrapCount = scrap.size();
                for (int i = 0; i < scrapCount; i++) {
                    removeDetachedView(scrap.remove(scrapCount - 1 - i), false);
                }
            } else {
                final int typeCount = mViewTypeCount;
                for (int i = 0; i < typeCount; i++) {
                    final ArrayList<View> scrap = mScrapViews[i];
                    final int scrapCount = scrap.size();
                    for (int j = 0; j < scrapCount; j++) {
                        removeDetachedView(scrap.remove(scrapCount - 1 - j), false);
                    }
                }
            }
            if (mTransientStateViews != null) {
                mTransientStateViews.clear();
            }
            if (mTransientStateViewsById != null) {
                mTransientStateViewsById.clear();
            }
        }

        /**
         * Fill ActiveViews with all of the children of the AbsListView.
         * 
         * @param childCount The minimum number of views mActiveViews should hold
         * @param firstActivePosition The position of the first view that will be stored in
         *        mActiveViews
         */
        void fillActiveViews(int childCount, int firstActivePosition) {
            if (mActiveViews.length < childCount) {
                mActiveViews = new View[childCount];
            }
            mFirstActivePosition = firstActivePosition;

            final View[] activeViews = mActiveViews;
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                AbsListView.LayoutParams lp = (AbsListView.LayoutParams) child.getLayoutParams();
                // Don't put header or footer views into the scrap heap
                if (lp != null && lp.viewType != ITEM_VIEW_TYPE_HEADER_OR_FOOTER) {
                    // Note: We do place AdapterView.ITEM_VIEW_TYPE_IGNORE in active views.
                    // However, we will NOT place them into scrap views.
                    activeViews[i] = child;
                }
            }
        }

        /**
         * Get the view corresponding to the specified position. The view will be removed from
         * mActiveViews if it is found.
         * 
         * @param position The position to look up in mActiveViews
         * @return The view if it is found, null otherwise
         */
        View getActiveView(int position) {
            int index = position - mFirstActivePosition;
            final View[] activeViews = mActiveViews;
            if (index >= 0 && index < activeViews.length) {
                final View match = activeViews[index];
                activeViews[index] = null;
                return match;
            }
            return null;
        }

        View getTransientStateView(int position) {
            if (mAdapter != null && mAdapterHasStableIds && mTransientStateViewsById != null) {
                long id = mAdapter.getItemId(position);
                View result = mTransientStateViewsById.get(id);
                mTransientStateViewsById.remove(id);
                return result;
            }
            if (mTransientStateViews != null) {
                final int index = mTransientStateViews.indexOfKey(position);
                if (index >= 0) {
                    View result = mTransientStateViews.valueAt(index);
                    mTransientStateViews.removeAt(index);
                    return result;
                }
            }
            return null;
        }

        /**
         * Dump any currently saved views with transient state.
         */
        void clearTransientStateViews() {
            if (mTransientStateViews != null) {
                mTransientStateViews.clear();
            }
            if (mTransientStateViewsById != null) {
                mTransientStateViewsById.clear();
            }
        }

        /**
         * @return A view from the ScrapViews collection. These are unordered.
         */
        View getScrapView(int position) {
            if (mViewTypeCount == 1) {
                return retrieveFromScrap(mCurrentScrap, position);
            } else {
                int whichScrap = mAdapter.getItemViewType(position);
                if (whichScrap >= 0 && whichScrap < mScrapViews.length) {
                    return retrieveFromScrap(mScrapViews[whichScrap], position);
                }
            }
            return null;
        }

        /**
         * Put a view into the ScrapViews list. These views are unordered.
         * 
         * @param scrap The view to add
         */
        void addScrapView(View scrap, int position) {
            AbsListView.LayoutParams lp = (AbsListView.LayoutParams) scrap.getLayoutParams();
            if (lp == null) {
                return;
            }

            lp.scrappedFromPosition = position;

            // Don't put header or footer views or views that should be ignored
            // into the scrap heap
            int viewType = lp.viewType;
            final boolean scrapHasTransientState = scrap.hasTransientState();
            if (!shouldRecycleViewType(viewType) || scrapHasTransientState) {
                if (viewType != ITEM_VIEW_TYPE_HEADER_OR_FOOTER && scrapHasTransientState) {
                    if (mSkippedScrap == null) {
                        mSkippedScrap = new ArrayList<View>();
                    }
                    mSkippedScrap.add(scrap);
                }
                if (scrapHasTransientState) {
                    dispatchStartTemporaryDetachOfHide(scrap);// scrap.dispatchStartTemporaryDetach();
                    if (mAdapter != null && mAdapterHasStableIds) {
                        if (mTransientStateViewsById == null) {
                            mTransientStateViewsById = new LongSparseArray<View>();
                        }
                        mTransientStateViewsById.put(lp.itemId, scrap);
                    } else if (!mDataChanged) {
                        // avoid putting views on transient state list during a data change;
                        // the layout positions may be out of sync with the adapter positions
                        if (mTransientStateViews == null) {
                            mTransientStateViews = new SparseArray<View>();
                        }
                        mTransientStateViews.put(position, scrap);
                    }
                }
                return;
            }

            dispatchStartTemporaryDetachOfHide(scrap);// scrap.dispatchStartTemporaryDetach();
            if (mViewTypeCount == 1) {
                mCurrentScrap.add(scrap);
            } else {
                mScrapViews[viewType].add(scrap);
            }

            scrap.setAccessibilityDelegate(null);
            if (mRecyclerListener != null) {
                mRecyclerListener.onMovedToScrapHeap(scrap);
            }
        }

        private void dispatchStartTemporaryDetachOfHide(View view) {
            Class<?> cls = view.getClass();
            try {
                Method method = cls.getMethod("dispatchStartTemporaryDetach");
                method.setAccessible(true);
                method.invoke(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Finish the removal of any views that skipped the scrap heap.
         */
        void removeSkippedScrap() {
            if (mSkippedScrap == null) {
                return;
            }
            final int count = mSkippedScrap.size();
            for (int i = 0; i < count; i++) {
                removeDetachedView(mSkippedScrap.get(i), false);
            }
            mSkippedScrap.clear();
        }

        /**
         * Move all views remaining in mActiveViews to mScrapViews.
         */
        void scrapActiveViews() {
            final View[] activeViews = mActiveViews;
            final boolean hasListener = mRecyclerListener != null;
            final boolean multipleScraps = mViewTypeCount > 1;

            ArrayList<View> scrapViews = mCurrentScrap;
            final int count = activeViews.length;
            for (int i = count - 1; i >= 0; i--) {
                final View victim = activeViews[i];
                if (victim != null) {
                    final AbsListView.LayoutParams lp =
                            (AbsListView.LayoutParams) victim.getLayoutParams();
                    int whichScrap = lp.viewType;

                    activeViews[i] = null;

                    final boolean scrapHasTransientState = victim.hasTransientState();
                    if (!shouldRecycleViewType(whichScrap) || scrapHasTransientState) {
                        // Do not move views that should be ignored
                        if (whichScrap != ITEM_VIEW_TYPE_HEADER_OR_FOOTER && scrapHasTransientState) {
                            removeDetachedView(victim, false);
                        }
                        if (scrapHasTransientState) {
                            if (mAdapter != null && mAdapterHasStableIds) {
                                if (mTransientStateViewsById == null) {
                                    mTransientStateViewsById = new LongSparseArray<View>();
                                }
                                long id = mAdapter.getItemId(mFirstActivePosition + i);
                                mTransientStateViewsById.put(id, victim);
                            } else {
                                if (mTransientStateViews == null) {
                                    mTransientStateViews = new SparseArray<View>();
                                }
                                mTransientStateViews.put(mFirstActivePosition + i, victim);
                            }
                        }
                        continue;
                    }

                    if (multipleScraps) {
                        scrapViews = mScrapViews[whichScrap];
                    }
                    dispatchStartTemporaryDetachOfHide(victim);// victim.dispatchStartTemporaryDetach();
                    lp.scrappedFromPosition = mFirstActivePosition + i;
                    scrapViews.add(victim);

                    victim.setAccessibilityDelegate(null);
                    if (hasListener) {
                        mRecyclerListener.onMovedToScrapHeap(victim);
                    }
                }
            }

            pruneScrapViews();
        }

        /**
         * Makes sure that the size of mScrapViews does not exceed the size of mActiveViews. (This
         * can happen if an adapter does not recycle its views).
         */
        private void pruneScrapViews() {
            final int maxViews = mActiveViews.length;
            final int viewTypeCount = mViewTypeCount;
            final ArrayList<View>[] scrapViews = mScrapViews;
            for (int i = 0; i < viewTypeCount; ++i) {
                final ArrayList<View> scrapPile = scrapViews[i];
                int size = scrapPile.size();
                final int extras = size - maxViews;
                size--;
                for (int j = 0; j < extras; j++) {
                    removeDetachedView(scrapPile.remove(size--), false);
                }
            }

            if (mTransientStateViews != null) {
                for (int i = 0; i < mTransientStateViews.size(); i++) {
                    final View v = mTransientStateViews.valueAt(i);
                    if (!v.hasTransientState()) {
                        mTransientStateViews.removeAt(i);
                        i--;
                    }
                }
            }
            if (mTransientStateViewsById != null) {
                for (int i = 0; i < mTransientStateViewsById.size(); i++) {
                    final View v = mTransientStateViewsById.valueAt(i);
                    if (!v.hasTransientState()) {
                        mTransientStateViewsById.removeAt(i);
                        i--;
                    }
                }
            }
        }

        /**
         * Puts all views in the scrap heap into the supplied list.
         */
        void reclaimScrapViews(List<View> views) {
            if (mViewTypeCount == 1) {
                views.addAll(mCurrentScrap);
            } else {
                final int viewTypeCount = mViewTypeCount;
                final ArrayList<View>[] scrapViews = mScrapViews;
                for (int i = 0; i < viewTypeCount; ++i) {
                    final ArrayList<View> scrapPile = scrapViews[i];
                    views.addAll(scrapPile);
                }
            }
        }

        /**
         * Updates the cache color hint of all known views.
         * 
         * @param color The new cache color hint.
         */
        void setCacheColorHint(int color) {
            if (mViewTypeCount == 1) {
                final ArrayList<View> scrap = mCurrentScrap;
                final int scrapCount = scrap.size();
                for (int i = 0; i < scrapCount; i++) {
                    scrap.get(i).setDrawingCacheBackgroundColor(color);
                }
            } else {
                final int typeCount = mViewTypeCount;
                for (int i = 0; i < typeCount; i++) {
                    final ArrayList<View> scrap = mScrapViews[i];
                    final int scrapCount = scrap.size();
                    for (int j = 0; j < scrapCount; j++) {
                        scrap.get(j).setDrawingCacheBackgroundColor(color);
                    }
                }
            }
            // Just in case this is called during a layout pass
            final View[] activeViews = mActiveViews;
            final int count = activeViews.length;
            for (int i = 0; i < count; ++i) {
                final View victim = activeViews[i];
                if (victim != null) {
                    victim.setDrawingCacheBackgroundColor(color);
                }
            }
        }
    }

    static View retrieveFromScrap(ArrayList<View> scrapViews, int position) {
        int size = scrapViews.size();
        if (size > 0) {
            // See if we still have a view for this position.
            for (int i = 0; i < size; i++) {
                View view = scrapViews.get(i);
                if (((AbsListView.LayoutParams) view.getLayoutParams()).scrappedFromPosition == position) {
                    scrapViews.remove(i);
                    return view;
                }
            }
            return scrapViews.remove(size - 1);
        } else {
            return null;
        }
    }

    int velocity2Distance(int velocity) {
        float flingFriction = ViewConfiguration.getScrollFriction();
        return (int) (getSplineFlingDistance(flingFriction, velocity) * Math.signum(velocity));
    }

    private float getPhysicalCoeff() {
        final float ppi = getResources().getDisplayMetrics().density * 160.0f;
        return SensorManager.GRAVITY_EARTH // g (m/s^2)
                * 39.37f // inch/meter
                * ppi * 0.84f;
    }

    private double getSplineDeceleration(float flingFriction, int velocity) {
        final float INFLEXION = 0.35f;
        return Math.log(INFLEXION * Math.abs(velocity) / (flingFriction * getPhysicalCoeff()));
    }

    private double getSplineFlingDistance(float flingFriction, int velocity) {
        final double l = getSplineDeceleration(flingFriction, velocity);
        float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return flingFriction * getPhysicalCoeff() * Math.exp(DECELERATION_RATE / decelMinusOne * l);
    }

    /* Returns the duration, expressed in milliseconds */
    private int getSplineFlingDuration(int velocity) {
        float flingFriction = ViewConfiguration.getScrollFriction();
        final double l = getSplineDeceleration(flingFriction, velocity);
        float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return (int) (1000.0 * Math.exp(l / decelMinusOne));
    }

    private int getVelocity(int distance) {
        float INFLEXION = 0.35f;
        return (int) (getSplineFlingVelocity(Math.abs(distance)) / INFLEXION);
    }

    private double getSplineFlingVelocity(int distance) {
        float flingFriction = ViewConfiguration.getScrollFriction();
        float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        float p = getPhysicalCoeff();
        return Math.log(distance / p / flingFriction) * decelMinusOne * p * flingFriction
                / DECELERATION_RATE;
    }

    private int getDuration(int distance) {
        int v = getVelocity(distance);
        return getSplineFlingDuration(v);
    }

    public void setRemoteViewsAdapter(IWidgetService service, Intent intent) {
        if (mRemoteAdapter != null) {
            Intent.FilterComparison fcNew = new Intent.FilterComparison(intent);
            Intent.FilterComparison fcOld =
                    new Intent.FilterComparison(mRemoteAdapter.getServiceIntent());

            if (fcNew.equals(fcOld)) {
                return;
            }
        }

        mDeferNotifyDataSetChanged = false;
        mRemoteAdapter = new LocalRemoteViewsAdapter(service, intent, this);
        if (mRemoteAdapter.isDataReady()) {
            setAdapter(mRemoteAdapter);
        }
    }

    void setVisibleRangeHint(int start, int end) {
        if (mRemoteAdapter != null) {
            mRemoteAdapter.setVisibleRangeHint(start, end);
        }
    }

    @Override
    public void deferNotifyDataSetChanged() {
        mDeferNotifyDataSetChanged = true;
    }

    @Override
    public void onLocalRemoteAdapterConnected() {
        if (mRemoteAdapter != mAdapter) {
            setAdapter(mRemoteAdapter);

            if (mDeferNotifyDataSetChanged) {
                mRemoteAdapter.notifyDataSetChanged();
                mDeferNotifyDataSetChanged = false;
            }
        }
    }

    @Override
    public void onLocalRemoteAdapterDisconnected() {}
}

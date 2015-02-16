package com.tom.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

/**
 * Pull up to load more data for ListView </br>上拉加载更多控件
 * 
 */
public class SimpleLoadMoreListView extends ListView implements OnScrollListener {

	/**
	 * allow to pull up to load more data
	 */
	private boolean pullUpLoading = false;

	private boolean CanLoadMore = true;

	private OnPullThreadStatusListener ThreadStatusListener;
	private OnLoadMoreListener LoadMoreListener;
	private View FooterView;

	public SimpleLoadMoreListView(Context context) {
		super(context);
		init(context);
	}

	public SimpleLoadMoreListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SimpleLoadMoreListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		this.setOnScrollListener(this);
	}

	@Override
	public void addFooterView(View v) {
		FooterView = v;
		FooterView.setVisibility(View.GONE);
		super.addFooterView(v);
	}

	public OnPullThreadStatusListener getThreadStatusListener() {
		return ThreadStatusListener;
	}

	public void setThreadStatusListener(
			OnPullThreadStatusListener threadStatusListener) {
		ThreadStatusListener = threadStatusListener;
	}

	public OnLoadMoreListener getLoadMoreListener() {
		return LoadMoreListener;
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		LoadMoreListener = loadMoreListener;
	}

	/**
	 * get FooterView ,</br> default new a FooterView if null
	 * 
	 * @return
	 */
	public View getFooterView() {
		if (FooterView == null) {
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
					40);
			FooterView = new View(getContext());
			FooterView.setLayoutParams(params);
		}
		return FooterView;
	}

	/**
	 * allow to load more data
	 * @return
	 */
	private boolean isCanLoadMore() {
		return CanLoadMore;
	}

	/**
	 * set can load more data
	 * 
	 * @param canLoadMore
	 */
	public void setCanLoadMore(boolean canLoadMore) {
		CanLoadMore = canLoadMore;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		/**
		 * when scroll to final item,and set to can pull up
		 */
		pullUpLoading = (firstVisibleItem + visibleItemCount == totalItemCount)
				&& totalItemCount > 0;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// allow to pull up
		if (pullUpLoading && isCanLoadMore()) {
			setFooterViewVisible(); // here set FooterView is visible to show loading status on pages

			if (!getThreadStatusListener().pullUPThreadStatusIsAlive()) { // loading data thread is alive
				if (pullUpLoading
						&& scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// start to load data
					if (getLoadMoreListener() != null) { // listener is not null
						getLoadMoreListener().onLoadMore();
					}
				}
			} else {
				// Waiting load
			}
		} else {
			setFooterViewGone();
		}
	}
	
	/**
	 * set FooterView is visible
	 */
	private void setFooterViewVisible(){
		getFooterView().setVisibility(View.VISIBLE);
		getFooterView().setPadding(0, 0, 0, 0);
	}
	
	/**
	 * set FooterView is gone, and exists a blank area instead of the view, so set padding
	 */
	private void setFooterViewGone(){
		getFooterView().setVisibility(View.GONE); 
		getFooterView().setPadding(0, -getFooterView().getHeight(), 0, 0);
	}

	/**
	 * on listener to load data thread
	 */
	public static interface OnPullThreadStatusListener {
		/**
		 * check the thread status for pulling up to load data
		 * 
		 * @return true,refer to current thread is alive;
		 */
		public abstract boolean pullUPThreadStatusIsAlive();
	}

	/**
	 * on listener to load more data
	 * 
	 */
	public static interface OnLoadMoreListener {
		/**
		 * load more data, also can set footer view status
		 */
		public abstract void onLoadMore();
	}
}

package com.tom.activity;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.tom.simpleloadmorelistview.R;
import com.tom.view.SimpleLoadMoreListView;
import com.tom.view.SimpleLoadMoreListView.OnLoadMoreListener;
import com.tom.view.SimpleLoadMoreListView.OnPullThreadStatusListener;

/**
 * 上拉加载更多测试
 * 
 * @author tom
 * @time 2015-02-16 09:35:08
 * 
 */
public class PullUptoRefreshTestActivity extends Activity implements
		OnLoadMoreListener, OnPullThreadStatusListener {
	private static final String TAG = "PullUptoRefreshTestActivity";

	private RelativeLayout mFooterView;
	private TextView mTextView;
	private Thread mThread;

	private static final String savedInstanceState_key_data = "data";
	private static final String savedInstanceState_key_countPage = "countPage";
	private static final String savedInstanceState_key_currentPage = "currentPage";
	private static final String savedInstanceState_key_toPage = "toPage";

	// 源数据
	private static ArrayList<String> mListItems;
	private ArrayAdapter<String> mAdapter;

	// 设置总页数
	private int countPage = 3;
	private int currentPage = 1;
	private int toPage = 1;
	private SimpleLoadMoreListView mListView;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null && savedInstanceState.containsKey(savedInstanceState_key_data)) {
			reStoreData(savedInstanceState);
		} else {
			initData();
		}

		setContentView(init(this));
	}

	private void reStoreData(Bundle savedInstanceState) {
		countPage = savedInstanceState.getInt(savedInstanceState_key_countPage);
		currentPage = savedInstanceState
				.getInt(savedInstanceState_key_currentPage);
		toPage = savedInstanceState.getInt(savedInstanceState_key_toPage);
	}

	private void initData() {
		// 1.添加数据源
		mListItems = new ArrayList<String>();
		for (int i = 1; i <= 15; i++) {
			mListItems.add("Item " + Integer.toString(i));
		}
	}

	/**
	 * 初始化控件
	 * 
	 * @param context
	 * @return
	 */
	@SuppressLint("InflateParams")
	public SimpleLoadMoreListView init(Context context) {
		// 2.定义一个LoadMoreListView
		mListView = new SimpleLoadMoreListView(context);

		// 3.添加自己的FooterView布局
		mFooterView = (RelativeLayout) LayoutInflater.from(
				getApplicationContext()).inflate(R.layout.load_more_footer,
				null);
		mTextView = (TextView) mFooterView.findViewById(R.id.load_more_tv);
		mAdapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_list_item_1, mListItems);

		// 4.设置FooterView，必须在调用setAdapter()之前调用
		mListView.addFooterView(mFooterView);

		// 5.设置适配器
		mListView.setAdapter(mAdapter);
		// 6.添加加载更多监听
		mListView.setLoadMoreListener(this);
		// 7.添加进程状态监听
		mListView.setThreadStatusListener(this);
		return mListView;
	}

	@Override
	public boolean pullUPThreadStatusIsAlive() {
		return mThread != null && mThread.isAlive();
	}

	@Override
	public void onLoadMore() {
		startLoadData(toPage);
	}

	private static final int LOAD_DATA = 1;
	private static final int LOAD_DATA_ERROR = 2;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == LOAD_DATA) { // 成功
				for (int i = 0; i < 5; i++) {
					mListItems.add("item new " + i);
				}
				mAdapter.notifyDataSetChanged();

				// 成功，请求下一页
				if (countPage >= toPage + 1) {
					toPage++;
				} else {
					mListView.setCanLoadMore(false); // 加载结束
					Toast.makeText(getApplicationContext(), "加载完成",
							Toast.LENGTH_SHORT).show();
				}

			} else if (msg.what == LOAD_DATA_ERROR) { // 失败
				Toast.makeText(getApplicationContext(), "加载出错",
						Toast.LENGTH_SHORT).show();
				// 加载出错设置FooterView隐藏
				mFooterView.setVisibility(View.GONE);

				// 失败，重新请求
				toPage = countPage;
			}

		};
	};

	/**
	 * 开始加载数据方法
	 * 
	 * @param toPage
	 *            请求页数
	 */
	private void startLoadData(int toPage) {
		mTextView.setText("正在加载..." + "第" + toPage + "页");
		mThread = new Thread() {
			public void run() {
				try {
					// 为了测试延时效果
					sleep(5000);
					// throw new NullPointerException(); //出错测试
					getResult();
				} catch (Exception e) {
					e.printStackTrace();
					getResultError();
				}
			}
		};
		mThread.start();
	}

	/**
	 * 获取数据处理
	 */
	private void getResult() {
		currentPage = toPage;
		Message msg = mHandler.obtainMessage();
		msg.what = LOAD_DATA;
		mHandler.sendMessage(msg);
	}

	/**
	 * 获取数据错误处理
	 */
	private void getResultError() {
		currentPage = toPage;
		Message msg = mHandler.obtainMessage();
		msg.what = LOAD_DATA_ERROR;
		mHandler.sendMessage(msg);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putStringArrayList(savedInstanceState_key_data, mListItems);
		outState.putInt(savedInstanceState_key_countPage, countPage);
		outState.putInt(savedInstanceState_key_currentPage, currentPage);
		outState.putInt(savedInstanceState_key_toPage, toPage);
		super.onSaveInstanceState(outState);
	}
}

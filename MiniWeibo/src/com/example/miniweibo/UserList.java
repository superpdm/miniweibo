package com.example.miniweibo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.miniweibo.RTPullListView.OnRefreshListener;

/**
 * PullListView
 * 
 * @author Ryan
 * 
 */
public class UserList extends Activity {

	private RTPullListView pullListView;
	private ProgressBar moreProgressBar;

	List<Map<String, Object>> datalist = null;
	// private List<String> dataList;
	private SimpleAdapter adapter;

	ActionBar actionBar = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_list);
		pullListView = (RTPullListView) this.findViewById(R.id.user_weibo_list);

		datalist = new ArrayList<Map<String, Object>>();
		actionBar = getActionBar();

		// adapter = new ArrayAdapter<String>(this,
		// android.R.layout.simple_list_item_1, dataList);
		adapter = new SimpleAdapter(this, datalist, R.layout.user_list_item,
				new String[] { "user_photo", "user_name" }, new int[] {
						R.id.user_photo, R.id.user_name });
		// setListAdapter(adapter);
		pullListView.setAdapter(adapter);

		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.user_list_footer, null);
		RelativeLayout footerView = (RelativeLayout) view
				.findViewById(R.id.list_footview);
		pullListView.addFooterView(footerView);
		moreProgressBar = (ProgressBar) view.findViewById(R.id.footer_progress);
		moreProgressBar.setVisibility(View.VISIBLE);
		footerView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				moreProgressBar.setVisibility(View.VISIBLE);
				loadData(GlobalUtil.LOAD_SUCCESS);
			}
		});

		pullListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				if (id <= -1)
					return;
				int realID = (int) id;
				Intent intent = new Intent(UserList.this, UserDetail.class);

				intent.putExtra("user", (Serializable) datalist.get(realID));
				intent.putExtra(GlobalUtil.source_code,
						GlobalUtil.SOURCE_USER_LIST);

				startActivity(intent);
			}

		});
		pullListView
				.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int position, long id) {

						Intent intent = new Intent(UserList.this,
								ComposeMessage.class);
						startActivity(intent);
						return false;
					}

				});

		// 下拉刷新监听器
		pullListView.setonRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				loadData(GlobalUtil.LOAD_SUCCESS);
			}
		});

		loadData(GlobalUtil.LOAD_SUCCESS);
	}

	// 结果处理
	private Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			ArrayList<Map<String, Object>> data = (ArrayList<Map<String, Object>>) msg
					.getData().getSerializable("data");
			if (data != null && data.size() != 0) {
				datalist.clear();
				datalist.addAll(data);
			}

			super.handleMessage(msg);
			switch (msg.what) {

			case GlobalUtil.LOAD_SUCCESS:
				moreProgressBar.setVisibility(View.GONE);
				adapter.notifyDataSetChanged();
				pullListView.onRefreshComplete();
				pullListView.setSelectionAfterHeaderView();
				break;
			default:
				break;
			}
		}

	};

	private void loadData(final int code) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					int user_id = MyTab.user.getInt("user_id");
					JSONArray users = DBWeibo.getUsers();

					ArrayList<Map<String, Object>> dList = new ArrayList<Map<String, Object>>();

					for (int i = 0; i < users.length(); i++) {
						JSONObject user = users.getJSONObject(i);
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("user_photo", R.drawable.message);
						map.put("user_name", user.getString("user_name"));
						map.put("user_id", user.getInt("user_id"));
						dList.add(map);
					}
					Message message = new Message();
					Bundle bundle = new Bundle();
					bundle.putSerializable("data", dList);
					message.setData(bundle);
					message.what = code;
					myHandler.sendMessage(message);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					myHandler.sendEmptyMessage(GlobalUtil.LOAD_FAILED);
				}
			}
		}).start();

	}
}

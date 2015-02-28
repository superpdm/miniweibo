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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import com.example.miniweibo.RTPullListView.OnRefreshListener;

/**
 * PullListView
 * 
 * @author Ryan
 * 
 */
public class UserDetail extends Activity {

	private RTPullListView pullListView;
	private ProgressBar moreProgressBar;

	List<Map<String, Object>> datalist = null;
	// private List<String> dataList;
	private SimpleAdapter adapter;

	public static Map<String, Object> user;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weibo_list);
		pullListView = (RTPullListView) this.findViewById(R.id.weibo_list);

		datalist = new ArrayList<Map<String, Object>>();

		user = (Map<String, Object>) getIntent().getSerializableExtra("user");
		adapter = new AsyLoadPicAdapter(this, datalist,
				R.layout.weibo_list_item, new String[] { "user_photo",
						"user_name", "pub_time", "content_text", /*
																 * "content_image",
																 */
						"comment_num" }, new int[] { R.id.user_photo,
						R.id.user_name, R.id.pub_time, R.id.content_text, /*
																		 * R.id.
																		 * content_image
																		 * ,
																		 */
						R.id.comment_num });

		pullListView.setAdapter(adapter);

		// 添加listview底部获取更多按钮（可自定义）
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.weibo_list_foot, null);
		RelativeLayout footerView = (RelativeLayout) view
				.findViewById(R.id.list_footview);
		moreProgressBar = (ProgressBar) view.findViewById(R.id.footer_progress);
		pullListView.addFooterView(footerView);

		moreProgressBar.setVisibility(View.VISIBLE);

		ActionBar actionBar=getActionBar();
		actionBar.setTitle(user.get("user_name").toString()+"的微博");
		loadData(GlobalUtil.LOAD_SUCCESS,false);
		

		pullListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				if (id <= -1)
					return;
				int realID = (int) id;
				Intent intent = new Intent(UserDetail.this, WeiboDetail.class);

				intent.putExtra("weibo_detail",
						(Serializable) datalist.get(realID));
				intent.putExtra(GlobalUtil.source_code,
						GlobalUtil.SOURCE_WEIBO_LIST);

				startActivity(intent);
			}

		});
		// 下拉刷新监听器
		pullListView.setonRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {

				loadData(GlobalUtil.LOAD_SUCCESS,false);

			}
		});

		// 获取跟多监听器
		footerView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				moreProgressBar.setVisibility(View.VISIBLE);

				loadData(GlobalUtil.LOAD_MORE_SUCCESS,true);

			}
		});
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		ActionBar actionBar = getActionBar();

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.user_detail_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent=null;
		switch (item.getItemId()) {
		case android.R.id.home:

			return true;
		case R.id.send_msg:
			intent = new Intent(this, ComposeMessage.class);
			intent.putExtra(GlobalUtil.source_code, GlobalUtil.SOURCE_USER_DETAIL);
			intent.putExtra(GlobalUtil.op_code, GlobalUtil.OP_SENDMSG);
			startActivity(intent);
			return true;
		
	        
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	// 结果处理
	private Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			ArrayList<Map<String, Object>> data = (ArrayList<Map<String, Object>>) msg
					.getData().getSerializable("data");
			if (data != null && data.size() != 0) {
				if(msg.what==GlobalUtil.LOAD_SUCCESS)
					datalist.clear();
				
				datalist.addAll(data);
			}
			switch (msg.what) {
			case GlobalUtil.LOAD_SUCCESS:
				moreProgressBar.setVisibility(View.GONE);
				adapter.notifyDataSetChanged();
				pullListView.onRefreshComplete();
				pullListView.setSelectionAfterHeaderView();
				
				break;
			case GlobalUtil.LOAD_MORE_SUCCESS:
				moreProgressBar.setVisibility(View.GONE);
				adapter.notifyDataSetChanged();
				break;

			case GlobalUtil.LOAD_FAILED:
				moreProgressBar.setVisibility(View.GONE);
				pullListView.setSelectionAfterHeaderView();

			default:
				break;
			}
		}

	};

	private void loadData(final int code,final boolean isMore) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				JSONArray jsonArray = null;
				try {
					int user_id = Integer.parseInt(user.get("user_id")
							.toString());
					
					String created_at="";
					if(isMore && !datalist.isEmpty())
						created_at=datalist.get(datalist.size()-1).get("pub_time").toString();
					
					jsonArray = DBWeibo.getWeiboByUser(user_id, 10,created_at);

					ArrayList<Map<String, Object>> dList = new ArrayList<Map<String, Object>>();

					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject weibo = jsonArray.getJSONObject(i);
						Map<String, Object> map = new HashMap<String, Object>();

						map.put("user_photo", R.drawable.message);
						map.put("weibo_id", weibo.get("weibo_id"));
						map.put("user_name", weibo.get("user_name"));
						map.put("content_text", weibo.get("content"));
						map.put("user_id", weibo.get("user_id"));

						map.put("mid_pic",
								weibo.get("mid_pic".toString().trim()));
						map.put("small_pic",
								weibo.get("small_pic".toString().trim()));
						map.put("origin_pic",
								weibo.get("origin_pic".toString().trim()));

						String urlImage = weibo.get("mid_pic").toString()
								.trim();

						if (urlImage.equals(""))
							urlImage = weibo.get("small_pic").toString().trim();
						if (urlImage.equals(""))
							urlImage = weibo.get("origin_pic").toString()
									.trim();

						map.put("content_image", urlImage);

						map.put("pub_time", weibo.get("pub_time"));
						map.put("comment_num",
								"评论" + weibo.getInt("comment_count") + "   转发"
										+ weibo.getInt("forward_count")
										+ "   赞" + weibo.getInt("like_count"));
						dList.add(map);
					}
					Message message = new Message();
					Bundle bundle = new Bundle();
					bundle.putSerializable("data", dList);
					message.setData(bundle);
					message.what = code;
					myHandler.sendMessage(message);
				} catch (Exception e) {
					e.printStackTrace();
					myHandler.sendEmptyMessage(GlobalUtil.LOAD_FAILED);
				}

			}
		}).start();

	}
}
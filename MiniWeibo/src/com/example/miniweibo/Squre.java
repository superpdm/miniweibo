package com.example.miniweibo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
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
public class Squre extends Activity {

	private RTPullListView pullListView;

	List<Map<String, Object>> datalist = null;
	private SimpleAdapter adapter;
	private ProgressBar moreProgressBar;
	private String curType = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weibo_list);
		pullListView = (RTPullListView) this.findViewById(R.id.weibo_list);

		datalist = new ArrayList<Map<String, Object>>();

		adapter = new AsyLoadPicAdapter(this, datalist, R.layout.squre_item,
				new String[] { "user_photo", "user_name", "pub_time",
						"content_text", /*
										 * "content_image",
										 */
						"comment_num" }, new int[] { R.id.user_photo,
						R.id.user_name, R.id.pub_time, R.id.content_text, /*
																		 * R.id.
																		 * content_image
																		 * ,
																		 */
						R.id.comment_num });
		// setListAdapter(adapter);
		pullListView.setAdapter(adapter);

		// 添加listview头部的分类
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.squre_footer, null);
		RelativeLayout headerView = (RelativeLayout) view
				.findViewById(R.id.list_footview);
		pullListView.addHeaderView(headerView);
		setHeader(headerView);
		
		//添加listview底部的获取更多
		view = inflater.inflate(R.layout.squre_header, null);
		RelativeLayout footerView = (RelativeLayout) view
				.findViewById(R.id.list_footview);
		pullListView.addFooterView(footerView);
		moreProgressBar = (ProgressBar) view.findViewById(R.id.footer_progress);
		moreProgressBar.setVisibility(View.VISIBLE);
		footerView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				moreProgressBar.setVisibility(View.VISIBLE);
				loadData(GlobalUtil.LOAD_MORE_SUCCESS, curType, true);
			}
		});
		

		loadData(GlobalUtil.LOAD_SUCCESS, "",false);

		pullListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				if (id <= -1)
					return;
				int realID = (int) id;
				Intent intent = new Intent(Squre.this, WeiboDetail.class);

				intent.putExtra("weibo_detail",
						(Serializable) datalist.get(realID));
				intent.putExtra(GlobalUtil.source_code, GlobalUtil.SOURCE_SQURE);

				startActivity(intent);
			}

		});
		// 下拉刷新监听器
		pullListView.setonRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				
				loadData(GlobalUtil.LOAD_SUCCESS, curType,false);

			}
		});

	}

	private void setHeader(View headerViewerView) {
		((Button) headerViewerView.findViewById(R.id.btnAll))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						curType = "";
						loadData(GlobalUtil.LOAD_SUCCESS, curType,false);
					}
				});
		((Button) headerViewerView.findViewById(R.id.btnDesign))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						curType = "服装设计";
						loadData(GlobalUtil.LOAD_SUCCESS, curType,false);
					}
				});
		((Button) headerViewerView.findViewById(R.id.btnMarket))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						curType = "市场动向";
						loadData(GlobalUtil.LOAD_SUCCESS, curType,false);
					}
				});
		((Button) headerViewerView.findViewById(R.id.btnPolicy))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						curType = "政策法规";
						loadData(GlobalUtil.LOAD_SUCCESS, curType,false);
					}
				});
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

	private void loadData(final int code, final String type,final boolean isMore) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				JSONArray jsonArray = null;
				try {
					String created_at="";
					if(isMore && !datalist.isEmpty())
						created_at=datalist.get(datalist.size()-1).get("pub_time").toString();
					
					jsonArray = DBWeibo.getLatestWeibo(10, true, type,created_at);

					ArrayList<Map<String, Object>> dList = new ArrayList<Map<String, Object>>();

					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject weibo = jsonArray.getJSONObject(i);
						Map<String, Object> map = new HashMap<String, Object>();

						map.put("user_photo", R.drawable.message);
						map.put("weibo_id", weibo.get("weibo_id"));
						map.put("user_name", weibo.get("user_name"));
						map.put("content_text", weibo.get("content"));
						map.put("user_id", weibo.getInt("user_id"));

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
								"   转发" + weibo.getInt("forward_count"));

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
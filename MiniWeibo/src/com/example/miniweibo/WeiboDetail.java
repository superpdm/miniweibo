package com.example.miniweibo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.bool;
import android.R.integer;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class WeiboDetail extends Activity {
	private ListView listView;
	private List<Map<String, Object>> datalist = null;
	private SimpleAdapter adapter;
	private ActionBar actionBar = null;
	public static Map<String, Object> weibo_detail = null;
	private Handler myHandler = null;
	public static int source_code = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weibo_detail);

		weibo_detail = (Map<String, Object>) getIntent().getExtras()
				.getSerializable("weibo_detail");
		source_code = getIntent().getIntExtra(GlobalUtil.source_code, 0);

		
		actionBar = getActionBar();
		actionBar.setTitle("微博内容");
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);

		// actionBar.
		datalist = new ArrayList<Map<String, Object>>();

		listView = (ListView) findViewById(R.id.comment_list);

		View header = getLayoutInflater().inflate(R.layout.weibo_detail_header,
				null);
		setHeader(header);
		myHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				ArrayList<Map<String, Object>> data = (ArrayList<Map<String, Object>>) msg
						.getData().getSerializable("data");
				if (data != null && data.size() != 0) {
					datalist.clear();
					datalist.addAll(data);
				}
				switch (msg.what) {
				case GlobalUtil.LOAD_SUCCESS:
					adapter.notifyDataSetChanged();
					break;

				default:
					break;
				}
				super.handleMessage(msg);
			}

		};
		listView.addHeaderView(header);
		if (source_code != GlobalUtil.SOURCE_SQURE)
			loadData(GlobalUtil.LOAD_SUCCESS);

		adapter = new SimpleAdapter(this, datalist, R.layout.weibo_detail_item,
				new String[] { "user_photo", "user_name", "comment_time",
						"content_text" }, new int[] { R.id.user_photo,
						R.id.user_name, R.id.comment_time, R.id.content_text });
		listView.setAdapter(adapter);

		
	}

	public boolean setHeader(View header) {
		ImageView imageContent = (ImageView) header
				.findViewById(R.id.content_image);
		TextView textContent = (TextView) header
				.findViewById(R.id.content_text);
		TextView textPubtime = (TextView) header.findViewById(R.id.pub_time);
		TextView textComment = (TextView) header.findViewById(R.id.comment_num);

		TextView txtUserName=(TextView)header.findViewById(R.id.user_name);
		
		String strUrl = weibo_detail.get("origin_pic").toString();

		if (strUrl.trim().equals(""))
			imageContent.setVisibility(View.GONE);
		else
			new AsyLoadPicTask(imageContent, strUrl).execute();

		textContent.setText(weibo_detail.get("content_text").toString());
		textPubtime.setText(weibo_detail.get("pub_time").toString());
		if (source_code != GlobalUtil.SOURCE_SQURE)
			textComment.setText(weibo_detail.get("comment_num").toString());
		else
			textComment.setText("");

		txtUserName.setText(weibo_detail.get("user_name").toString());
		return true;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.weibo_detail_menu, menu);

		if (source_code == GlobalUtil.SOURCE_SQURE)// 去掉微博里面的评论功能，只能转发
		{
			menu.findItem(R.id.comment).setVisible(false);
			menu.findItem(R.id.like).setVisible(false);
		}
		return true;
	}

	public void loadData(final int code) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					JSONArray comments = DBWeibo.getWeiboComments(Integer
							.parseInt(weibo_detail.get("weibo_id").toString()));
					ArrayList<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
					for (int i = 0; i < comments.length(); i++) {
						JSONObject comment = comments.getJSONObject(i);
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("user_photo", R.drawable.message);
						map.put("user_name", comment.get("user_name"));
						map.put("content_text", comment.get("comment_text"));
						map.put("comment_time", comment.get("comment_time"));
						datas.add(map);
					}
					Message message = new Message();
					Bundle bundle = new Bundle();
					bundle.putSerializable("data", datas);
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in Action Bar clicked; go home
			finish();
			return true;
		case R.id.comment:
			intent = new Intent(this, ComposeMessage.class);
			intent.putExtra(GlobalUtil.source_code,
					GlobalUtil.SOURCE_WEIBO_DETAIL);
			intent.putExtra(GlobalUtil.op_code, GlobalUtil.OP_COMMENT);
			
			startActivity(intent);
			return true;
		case R.id.forward:
			intent = new Intent(this, ComposeMessage.class);
			intent.putExtra(GlobalUtil.source_code,
					GlobalUtil.SOURCE_WEIBO_DETAIL);
			intent.putExtra(GlobalUtil.op_code, GlobalUtil.OP_FORWARD);
			startActivity(intent);
			return true;
		case R.id.like:
			likeWeibo();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void likeWeibo() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				int weibo_id = Integer.parseInt(weibo_detail.get("weibo_id")
						.toString());
				boolean res = DBWeibo.likeWeibo(weibo_id);
			}
		}).start();

	}

}

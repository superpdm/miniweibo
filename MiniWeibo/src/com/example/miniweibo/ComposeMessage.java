package com.example.miniweibo;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class ComposeMessage extends Activity {

	ActionBar actionBar = null;

	public static int source_code = 0;
	public static int op_code = 0;
	private EditText contentEditText = null;

	//private WeiboDetail weiboDetail=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compose_message);

		contentEditText = (EditText) findViewById(R.id.content_text);
		actionBar = getActionBar();
		actionBar.setTitle("Ð´ÄÚÈÝ");
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);

		source_code = getIntent().getIntExtra(GlobalUtil.source_code, 0);
		op_code = getIntent().getIntExtra(GlobalUtil.op_code, 0);
		
		//weiboDetail=(WeiboDetail) getIntent().getSerializableExtra("class");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.compose_message_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in Action Bar clicked; go home
			finish();
			return true;
		case R.id.send_msg:

			if (op_code == GlobalUtil.OP_COMMENT)
				commentWeibo();
			if (op_code == GlobalUtil.OP_FORWARD)
				forwardWeibo();

			if (op_code == GlobalUtil.OP_SENDMSG)
				sendMSG();

			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void sendMSG() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Map<String, Object> to_user = UserDetail.user;
					int to_id = Integer.parseInt(to_user.get("user_id")
							.toString());
					String content = contentEditText.getText().toString();
					String created_at = GlobalUtil
							.getTimeByFormat("yyyy-MM-dd HH:mm:ss");

					JSONObject from_user = MyTab.user;
					int from_id = from_user.getInt("user_id");
					int weibo_id = 0;
					DBWeibo.sendMessage(content, created_at, from_id, to_id,
							weibo_id, GlobalUtil.MSG_OP_WORD);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void forwardWeibo() {
		// if (WeiboDetail.source_code == GlobalUtil.SOURCE_WEIBO_LIST)
		new Thread(new Runnable() {

			@Override
			public void run() {
				Map<String, Object> weibo_detail = WeiboDetail.weibo_detail;
				String created_at = GlobalUtil
						.getTimeByFormat("yyyy-MM-dd HH:mm:ss");
				String content = "@" + weibo_detail.get("user_name") + ":"
						+ contentEditText.getText().toString() + "//"
						+ weibo_detail.get("content_text").toString();

				String source = "forward";
				String pic = weibo_detail.get("origin_pic").toString();
				Location location = MyTab.getLocation();

				double lat = 0;
				double lon = 0;
				if (location != null) {
					lat = location.getLatitude();
					lon = location.getLongitude();
				}
				int user_id;
				try {
					user_id = MyTab.user.getInt("user_id");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					user_id = 0;
				}
				int forward_id = Integer.parseInt(weibo_detail.get("weibo_id")
						.toString());

				DBWeibo.pubWeibo(created_at, content, source, pic, lat, lon,
						user_id, forward_id);

				int from_id = user_id;
				int to_id = Integer.parseInt(WeiboDetail.weibo_detail.get(
						"user_id").toString());

				DBWeibo.sendMessage(content, created_at, from_id, to_id,
						forward_id, GlobalUtil.MSG_OP_FORWARD);

			}
		}).start();
	}

	private void commentWeibo() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				int weibo_id = Integer.parseInt(WeiboDetail.weibo_detail.get(
						"weibo_id").toString());
				String comment_time = GlobalUtil
						.getTimeByFormat("yyyy-MM-dd HH:mm:ss");
				int user_id = 0;
				try {
					user_id = MyTab.user.getInt("user_id");
				} catch (JSONException e) {
					e.printStackTrace();
					return;
				}
				String comment_text = contentEditText.getText().toString();
				DBWeibo.commentWeibo(weibo_id, user_id, comment_text,
						comment_time);

				int from_id = user_id;
				int to_id = Integer.parseInt(WeiboDetail.weibo_detail.get(
						"user_id").toString());

				DBWeibo.sendMessage(comment_text, comment_time, from_id, to_id,
						weibo_id, GlobalUtil.MSG_OP_COMMENT);
				//weiboDetail.loadData(GlobalUtil.LOAD_SUCCESS);
			}
		}).start();

	}

}

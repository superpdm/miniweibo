package com.example.miniweibo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;

import com.example.miniweibo.R.string;

import android.R.integer;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Matrix.ScaleToFit;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.SyncStateContract.Constants;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

public class ComposeWeibo extends Activity {

	public static final int MSG_ADD_GEO = 0;

	ActionBar actionBar = null;
	Location location = null;
	ImageView imageView = null;
	Menu menu = null;
	Handler myHandle = null;
	EditText weiboContent = null;
	String picUrl = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compose_weibo);

		weiboContent = (EditText) findViewById(R.id.content_text);

		actionBar = getActionBar();
		actionBar.setTitle("发微博");
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);

		myHandle = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_ADD_GEO:
					String addr = msg.getData().getString("address");
					weiboContent.append("#" + addr + "#");
					break;

				default:
					break;
				}
				super.handleMessage(msg);
			}

		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.compose_weibo_menu, menu);

		imageView = new ImageView(this);
		imageView.setScaleType(ScaleType.CENTER_INSIDE);
		imageView.setAdjustViewBounds(true);
		imageView.setMaxHeight(60);
		imageView.setMaxHeight(60);

		this.menu = menu;

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 0) {
				Uri aUri = data.getData();
				imageView.setImageURI(aUri);
				menu.findItem(R.id.add_pic).setActionView(imageView);
				ContentResolver contentResolver = getContentResolver();
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = managedQuery(aUri, proj, // Which columns to
															// return
						null, // WHERE clause; which rows to return (all rows)
						null, // WHERE clause selection arguments (none)
						null); // Order-by clause (ascending by name)

				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				final String imagePath = cursor.getString(column_index);
				String posix = imagePath.substring(imagePath.lastIndexOf('.'));
				final String serverPath=GlobalUtil.MD5Encode(imagePath.getBytes());
				picUrl=serverPath+posix;
				new Thread(new Runnable() {

					@Override
					public void run() {
						WebService.uploadFile(imagePath,
								serverPath);
					}
				}).start();

			}

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void pubWeibo() {
		new Thread(new Runnable() {

			@Override
			public void run() {

				String created_at = GlobalUtil
						.getTimeByFormat("yyyy-MM-dd HH:mm:ss");
				String content = weiboContent.getText().toString();

				String source = "original";
				String pic = picUrl;
				location = MyTab.getLocation();

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
				int forward_id = 0;

				DBWeibo.pubWeibo(created_at, content, source, pic, lat, lon,
						user_id, forward_id);

			}
		}).start();

		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in Action Bar clicked; go home
			this.finish();
			return true;
		case R.id.send_msg:
			Toast.makeText(this, "send message", Toast.LENGTH_SHORT).show();
			intent = new Intent(this, WeiboDetail.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.send_weibo:
			pubWeibo();
			return true;

		case R.id.add_geo:
			location = MyTab.getLocation();
			if (location == null)
				Toast.makeText(ComposeWeibo.this, "定位出现问题", Toast.LENGTH_SHORT)
						.show();
			else
				new Thread(new Runnable() {

					@Override
					public void run() {
						String res = DBWeibo.getAddrText(location);
						Message message = new Message();
						message.what = MSG_ADD_GEO;
						Bundle data = new Bundle();
						data.putString("address", res);
						message.setData(data);
						myHandle.sendMessage(message);

					}
				}).start();

			return true;
		case R.id.add_pic:
			intent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent, 0);

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}

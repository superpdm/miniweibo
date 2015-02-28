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
import android.R.menu;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.example.miniweibo.RTPullListView.OnRefreshListener;

/**
 * PullListView
 * @author Ryan
 *
 */
public class MessageList extends Activity {

	
	private RTPullListView pullListView;
	
	List<Map<String, Object>> datalist=null;
	//private List<String> dataList;
	private SimpleAdapter adapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_list);
        pullListView = (RTPullListView) this.findViewById(R.id.message_list);
        
        datalist = new ArrayList<Map<String, Object>>();
		
        loadData(GlobalUtil.LOAD_SUCCESS);
		
		adapter = new SimpleAdapter(this,datalist,R.layout.message_list_item,
                new String[]{"user_name","msg_type","pub_time","msg_content"},
                new int[]{R.id.user_name,R.id.msg_type,R.id.pub_time,R.id.msg_content});

		pullListView.setAdapter(adapter);
		
		//下拉刷新监听器
		pullListView.setonRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				loadData(GlobalUtil.LOAD_SUCCESS);	
			}
		});
		pullListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long id) {
				if(id<=-1)
					return;
				Map<String, Object> curObject=datalist.get((int)id);
				
				int op_type=Integer.parseInt(curObject.get("op_type").toString());
				if(op_type==GlobalUtil.MSG_OP_WORD)
					return ;
				final int weibo_id=Integer.parseInt(curObject.get("weibo_id").toString());
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							JSONObject weibo=DBWeibo.getWeiboByID(weibo_id);
							Map<String, Object> weibo_detail=new HashMap<String, Object>();
							weibo_detail.put("user_photo", R.drawable.message);
							weibo_detail.put("weibo_id", weibo.get("weibo_id"));
							weibo_detail.put("user_name", weibo.get("user_name"));
							weibo_detail.put("content_text", weibo.get("content"));
							weibo_detail.put("user_id", weibo.getInt("user_id"));
							weibo_detail.put("mid_pic",
									weibo.get("mid_pic".toString().trim()));
							weibo_detail.put("small_pic",
									weibo.get("small_pic".toString().trim()));
							weibo_detail.put("origin_pic",
									weibo.get("origin_pic".toString().trim()));

							String urlImage = weibo.get("mid_pic").toString()
									.trim();

							if (urlImage.equals(""))
								urlImage = weibo.get("small_pic").toString().trim();
							if (urlImage.equals(""))
								urlImage = weibo.get("origin_pic").toString()
										.trim();

							weibo_detail.put("content_image", urlImage);

							weibo_detail.put("pub_time", weibo.get("pub_time"));
							weibo_detail.put("comment_num",
									"评论" + weibo.getInt("comment_count") + "   转发"
											+ weibo.getInt("forward_count")
											+ "   赞" + weibo.getInt("like_count"));
							
							Intent intent=new Intent(MessageList.this,WeiboDetail.class);
							intent.putExtra("weibo_detail", (Serializable)weibo_detail);
							intent.putExtra(GlobalUtil.source_code, GlobalUtil.SOURCE_MSG_LIST);
							startActivity(intent);
						} catch (JSONException e) {
							
							e.printStackTrace();
						}
						
					}
				}).start();
				
			}
			
		});
		
    }
    
    //结果处理
    private Handler myHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			ArrayList<Map<String, Object>> data = (ArrayList<Map<String, Object>>) msg
					.getData().getSerializable("data");
			if (data!=null && data.size() != 0) {
				datalist.clear();
				datalist.addAll(data);
			}
			switch (msg.what) {
			case GlobalUtil.LOAD_SUCCESS:
				adapter.notifyDataSetChanged();
				pullListView.onRefreshComplete();
				pullListView.setSelectionfoot();
				break;

			default:
				break;
			}
		}
    	
    };
    private void loadData(final int code)
    {
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				try{
				JSONObject user=MyTab.user;
		    	int user_id=user.getInt("user_id");
		    	JSONArray msgs=DBWeibo.getMessage(user_id, 10);
		    	
		    	ArrayList<Map<String, Object>> dList=new ArrayList<Map<String,Object>>();
		    	for (int i = 0; i < msgs.length(); i++) {
					Map<String, Object> map = new HashMap<String, Object>();
					JSONObject msg=msgs.getJSONObject(i);
					int weibo_id=msg.getInt("weibo_id");
					int op_type=msg.getInt("op_type");
					
					if(op_type==GlobalUtil.MSG_OP_COMMENT)
						map.put("msg_type", "    评论了你 的微博");
					else if(op_type==GlobalUtil.MSG_OP_FORWARD)
						map.put("msg_type", "    转发了你的微博");
					else
						map.put("msg_type", "    发送了一个消息");
					map.put("weibo_id", weibo_id);
					map.put("op_type", op_type);
			        map.put("user_name", msg.getString("from_name"));
			        map.put("pub_time", msg.getString("created_at"));
			        map.put("msg_content",msg.getString("content"));
			        dList.add(map);
				}
		    	Message message=new Message();
				Bundle bundle=new Bundle();
				bundle.putSerializable("data", dList);
				message.setData(bundle);
				message.what=code;
				myHandler.sendMessage(message);
				}catch(Exception e)
				{
					e.printStackTrace();
					myHandler.sendEmptyMessage(GlobalUtil.LOAD_FAILED);
				}
			}
		}).start();
    	
    	
    }
}
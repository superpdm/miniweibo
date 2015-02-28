package com.example.miniweibo;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.R.string;
import android.location.Location;
import android.net.Uri;

class WeiboContent {
	Uri userPhoto;
	String userName;
	String contentText;
	String urlImage;
	String pubTime;
	int likeCount;
	int commentCount;
	int forwardCount;
	String type;

	public WeiboContent() {
	}

}

public class DBWeibo {
	public static JSONArray getLatestWeibo(int num, boolean is_raw, String type,String created_at)
			throws Exception {
		JSONArray list = null;
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("num", String.valueOf(num));
		args.put("is_raw", String.valueOf(is_raw));
		args.put("type", type);
		args.put("created_at", created_at);

		String result = WebService.sendRequest("getLatestWeibo", args);

		result = delteTags(result);
		JSONObject obj = new JSONObject(result);

		list = obj.getJSONArray("weibo_list");

		return list;
	}

	public static JSONArray getWeiboComments(int weibo_id) throws JSONException {
		JSONArray list = null;
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("weibo_id", String.valueOf(weibo_id));
		String result = WebService.sendRequest("getWeiboComments", args);

		result = delteTags(result);
		JSONObject obj = new JSONObject(result);

		list = obj.getJSONArray("weibo_comments");

		return list;
	}

	public static boolean pubWeibo(String created_at, String content,
			String source, String pic, double lat, double lon, int user_id,
			int forward_id) {
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("created_at", created_at);
		args.put("content", content);
		args.put("source", source);
		args.put("pic", pic);
		args.put("lat", String.valueOf(lat));
		args.put("lon", String.valueOf(lon));
		args.put("user_id", String.valueOf(user_id));
		args.put("forward_id", String.valueOf(forward_id));

		String result = WebService.sendRequest("pubWeibo", args);

		result = delteTags(result);

		if (result.equals("true"))
			return true;
		else
			return false;
	}

	public static JSONObject signIN(String email, String password)
			throws Exception {
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("email", email);
		args.put("password", password);

		String result = WebService.sendRequest("signIN", args);
		result = delteTags(result);
		if (result == "null")
			return null;
		else
			return new JSONObject(result);
	}

	public static boolean commentWeibo(int weibo_id, int user_id,
			String comment_text, String comment_time) {
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("user_id", String.valueOf(user_id));
		args.put("weibo_id", String.valueOf(weibo_id));
		args.put("comment_text", comment_text);
		args.put("comment_time", comment_time);

		String result = WebService.sendRequest("commentWeibo", args);
		result = delteTags(result);
		if (result == "true")
			return true;
		else
			return false;
	}

	public static boolean likeWeibo(int weibo_id) {
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("weibo_id", String.valueOf(weibo_id));

		String result = WebService.sendRequest("likeWeibo", args);
		result = delteTags(result);
		if (result == "true")
			return true;
		else
			return false;
	}

	public static JSONArray getWeiboByUser(int user_id, int num,String created_at)
			throws JSONException {
		JSONArray list = null;
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("num", String.valueOf(num));
		args.put("user_id", String.valueOf(user_id));
		args.put("created_at", created_at);
		
		String result = WebService.sendRequest("getWeiboByUser", args);

		result = delteTags(result);
		JSONObject obj = new JSONObject(result);

		list = obj.getJSONArray("weibo_list");

		return list;
	}

	public static JSONArray getUsers() throws JSONException {
		HashMap<String, String> args = new HashMap<String, String>();

		String result = WebService.sendRequest("getUsers", args);

		result = delteTags(result);

		JSONArray users = new JSONArray(result);

		return users;
	}

	public static boolean sendMessage(String content, String created_at,
			int from_id, int to_id, int weibo_id,int op_type)  {
		
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("content", content);
		args.put("created_at", created_at);
		args.put("from_id", String.valueOf(from_id));
		args.put("to_id", String.valueOf(to_id));
		args.put("weibo_id", String.valueOf(weibo_id));
		args.put("op_type", String.valueOf(op_type));
		
		String result = WebService.sendRequest("sendMessage", args);

		result = delteTags(result);
		if(result.trim().equals("true"))
			return true;
		else
			return false;
	}
	public static JSONArray getMessage(int user_id,int num) throws JSONException {
		
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("user_id", String.valueOf(user_id));
		args.put("num",String.valueOf(num));
		
		
		String result = WebService.sendRequest("getMessage", args);

		result = delteTags(result);
		if(result.equals("null") || result.equals(""))
			return null;
		JSONArray msgs=new JSONArray(result);
		
		return msgs;
	}
public static JSONObject getWeiboByID(int weibo_id) throws JSONException {
		
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("weibo_id", String.valueOf(weibo_id));
		
		
		String result = WebService.sendRequest("getWeiboByID", args);

		result = delteTags(result);
		if(result.equals("null") || result.equals(""))
			return null;
		JSONObject weibo=new JSONObject(result);
		
		return weibo;
	}

	// handle tags
	static String delteTags(String input) {
		int i = 0;
		while (input.charAt(i++) != '<')
			;
		while (input.charAt(i++) != '>')
			;

		while (input.charAt(i++) != '<')
			;
		while (input.charAt(i++) != '>')
			;

		int j = input.length() - 1;
		while (input.charAt(j--) != '>')
			;
		while (input.charAt(j--) != '<')
			;
		j++;
		if(i>j)
			return "";
		input = input.substring(i, j);
		return input;

	}

	// get text address from baidu
	public static String getAddrText(Location location) {
		try {
			String addrString = WebService.getAddr(location);
			JSONObject obj = new JSONObject(addrString);
			String addr = obj.getJSONObject("result").getString(
					"formatted_address");
			return addr;
		} catch (Exception e) {
			return null;
		}
	}
}

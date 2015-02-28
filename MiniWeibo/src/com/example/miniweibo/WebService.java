package com.example.miniweibo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.location.Location;
import android.util.Base64;

public class WebService {
	private static String serverIP;
	private static String baseURL = "http://" + serverIP + "/test.asmx/";

	public static void setServerIP(String ip) {
		serverIP = ip;
		baseURL = "http://" + serverIP + "/test.asmx/";
	}
	public static String getServerIP()
	{
		return serverIP;
	}

	static public String sendRequest(String func, HashMap<String, String> args) {
		String url = baseURL + func;
		if (!args.isEmpty()) {
			url += "?";

			String keyValue = "";
			Set<String> names = args.keySet();
			for (String name : names) {
				String value = args.get(name);
				try {
					name = URLEncoder.encode(name, "UTF-8");
					value = URLEncoder.encode(value, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				keyValue += "&" + name + "=" + value;
			}
			url += keyValue.substring(1);
		}
		String resString = doGet(url);
		return resString;
	}

	static public String uploadFile(String fileName, String severPath) {
		HttpResponse response;
		try {
			String posix = fileName.substring(fileName.lastIndexOf('.'));
			File file = new File(fileName);
			long len = file.length();

			FileInputStream fis = new FileInputStream(fileName);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			int count = 0;
			while ((count = fis.read(buffer)) >= 0) {
				baos.write(buffer, 0, count);
			}
			String data = new String(Base64.encode(baos.toByteArray(),
					Base64.DEFAULT), "UTF-8"); // 进行Base64编码

			HttpPost httppost = new HttpPost(baseURL + "/GetPicNew");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

			nameValuePairs
					.add(new BasicNameValuePair("file", severPath + posix));

			nameValuePairs.add(new BasicNameValuePair("fileData", data));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

			response = new DefaultHttpClient().execute(httppost);

			return severPath + posix;
		} catch (Exception e) {
			return null;
		}
	}

	public static String doGet(String url) {
		HttpGet get = new HttpGet(url);
		HttpClient client = new DefaultHttpClient();

		String resString = "";
		try {
			HttpResponse response = client.execute(get);// 执行Post方法
			resString = EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return resString;
	}

	public static String getAddr(Location location) {

		String url = "http://api.map.baidu.com/geocoder?output=json&location=";
		url += location.getLatitude();
		url += ",";
		url += location.getLongitude();
		url += "&key=37492c0ee6f924cb5e934fa08c6b1676";

		String resString = doGet(url);
		return resString;
	}

}
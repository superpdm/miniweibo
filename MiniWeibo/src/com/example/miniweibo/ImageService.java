package com.example.miniweibo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.DigestUtils;

import android.net.Uri;
import android.os.Environment;

public class ImageService {
	static String cacheName = "cache";

	private ImageService(){}
	public static Uri getImageURI(String strUrl) throws Exception {

		String name = GlobalUtil.MD5Encode(strUrl.getBytes())
				+ strUrl.substring(strUrl.lastIndexOf("."));
		File path = Environment.getExternalStorageDirectory();
		File cache = new File(path, cacheName);
		File file = new File(cache, name);
		if(!cache.isDirectory() || !cache.exists())
			cache.mkdirs();
		// ���ͼƬ���ڱ��ػ���Ŀ¼����ȥ����������
		if (file.exists()) {
			return Uri.fromFile(file);// Uri.fromFile(path)��������ܵõ��ļ���URI
		} else {
			// �������ϻ�ȡͼƬ
			if (!strUrl.startsWith("http://"))
				strUrl = "http://" + WebService.getServerIP() + "/pic/" + strUrl;
			URL url = new URL(strUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			if (conn.getResponseCode() == 200) {

				InputStream is = conn.getInputStream();
				FileOutputStream fos = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len);
				}
				is.close();
				fos.close();
				// ����һ��URI����
				return Uri.fromFile(file);
			}
		}
		return null;
	}

	public static Uri getImageUriFromLocal(String strUrl) {
		String name = GlobalUtil.MD5Encode(strUrl.getBytes())
				+ strUrl.substring(strUrl.lastIndexOf("."));
		File path = Environment.getExternalStorageDirectory();
		File cache = new File(path, cacheName);
		File file = new File(cache, name);
		// ���ͼƬ���ڱ��ػ���Ŀ¼����ȥ����������
		if (file.exists()) {
			return Uri.fromFile(file);// Uri.fromFile(path)��������ܵõ��ļ���URI
		} else
			return null;

	}

}

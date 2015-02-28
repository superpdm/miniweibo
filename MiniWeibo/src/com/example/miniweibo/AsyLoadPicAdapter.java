package com.example.miniweibo;

import java.util.List;
import java.util.Map;

import android.R.integer;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

public class AsyLoadPicAdapter extends SimpleAdapter {
	private List<Map<String, Object>> datalist =null;
	public AsyLoadPicAdapter(Context context,
			List<Map<String, Object>> data, int resource, String[] from,
			int[] to) {
		super(context, data, resource, from, to);

		datalist=data;
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View res=super.getView(position, convertView, parent);
		ImageView imageView=(ImageView)res.findViewById(R.id.content_image);
		String strUrl=(String) datalist.get(position).get("content_image");

		if(!strUrl.trim().equals(""))
		{
			imageView.setVisibility(View.VISIBLE);
			Uri uri=ImageService.getImageUriFromLocal(strUrl);
			
			if(uri==null)
				new AsyLoadPicTask(imageView, strUrl).execute();
			else
				imageView.setImageURI(uri);
		}
		else 
		{
			imageView.setVisibility(View.GONE);
		}
		return res;
	}

}

package com.example.miniweibo;

import java.net.URI;

import android.net.Uri;
import android.os.AsyncTask;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class AsyLoadPicTask extends AsyncTask<String, Integer, Uri> {

	ImageView imageView=null;
	String strUrl=null;
	public AsyLoadPicTask(ImageView imageView,String url) {
		super();
		this.imageView=imageView;
		this.strUrl=url;
	}

	@Override
	protected Uri doInBackground(String... arg0) {
		try {
			Uri uri=ImageService.getImageURI(strUrl);
			return uri;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Uri result) {
		// TODO Auto-generated method stub
		if(result!=null && imageView!=null)
		{
		
			imageView.setImageURI(result);
			
		}
		super.onPostExecute(result);
	}

}

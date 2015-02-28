package com.example.miniweibo;

import java.security.spec.MGF1ParameterSpec;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MyTab extends TabActivity implements OnCheckedChangeListener {
	private RadioGroup mainTab;
	private TabHost tabhost;
	private Intent iHome;
	private Intent iNews;
	private Intent iInfo;
	private Intent iSearch;
	private Intent iMore;
	private static LocationManager locationManager = null;
	private static String locationProvider;
	private static LocationListener locationListener = null;

	public static JSONObject user=null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.main);

		

		mainTab = (RadioGroup) findViewById(R.id.main_tab);
		mainTab.setOnCheckedChangeListener(this);
		tabhost = getTabHost();

		iHome = new Intent(this, WeiboList.class);
		tabhost.addTab(tabhost
				.newTabSpec("iHome")
				.setIndicator(getResources().getString(R.string.main_home),
						getResources().getDrawable(R.drawable.weibo_list))
				.setContent(iHome));

		iNews = new Intent(this, MessageList.class);
		tabhost.addTab(tabhost
				.newTabSpec("iNews")
				.setIndicator(getResources().getString(R.string.main_home),
						getResources().getDrawable(R.drawable.message))
				.setContent(iNews));

		iInfo = new Intent(this, UserList.class);
		tabhost.addTab(tabhost
				.newTabSpec("iInfo")
				.setIndicator(getResources().getString(R.string.main_home),
						getResources().getDrawable(R.drawable.group))
				.setContent(iInfo));

		iSearch = new Intent(this, Squre.class);
		tabhost.addTab(tabhost
				.newTabSpec("iSearch")
				.setIndicator(getResources().getString(R.string.main_home),
						getResources().getDrawable(R.drawable.squre))
				.setContent(iSearch));

		iMore = new Intent(this, Settings.class);
		tabhost.addTab(tabhost
				.newTabSpec("iMore")
				.setIndicator(getResources().getString(R.string.main_home),
						getResources().getDrawable(R.drawable.setting))
				.setContent(iMore));
		
		turnOnLocation();
		// action bar
		ActionBar actionBar = getActionBar();
		actionBar.show();
		actionBar.setDisplayShowTitleEnabled(true);
		
		

		
		String strUser=getIntent().getStringExtra("user");
		try {
			user=new JSONObject(strUser);
			actionBar.setTitle(user.getString("user_name"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_page_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent=null;
		switch (item.getItemId()) {
		case android.R.id.home:

			return true;
		case R.id.compose_weibo:
			intent = new Intent(this, ComposeWeibo.class);
			startActivity(intent);
			return true;
		
	        
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.radio_button0:
			this.tabhost.setCurrentTabByTag("iHome");
			break;
		case R.id.radio_button1:
			this.tabhost.setCurrentTabByTag("iNews");
			break;
		case R.id.radio_button2:
			this.tabhost.setCurrentTabByTag("iInfo");
			break;
		case R.id.radio_button3:
			this.tabhost.setCurrentTabByTag("iSearch");
			break;
		case R.id.radio_button4:
			this.tabhost.setCurrentTabByTag("iMore");
			break;
		}
	}

	public static Location getLocation() {
		Location location = null;
		location=locationManager.getLastKnownLocation(locationProvider);
		
		return location;
	}

	private void turnOnLocation() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE); // 精度要求：ACCURACY_FINE(高)ACCURACY_COARSE(低)

		criteria.setAltitudeRequired(false); // 不要求海拔信息
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(false); // 是否允许付费
		criteria.setPowerRequirement(Criteria.POWER_LOW); // 对电量的要求
															// (HIGH、MEDIUM)

		locationProvider = locationManager.getBestProvider(criteria, true);
		locationProvider=LocationManager.GPS_PROVIDER;
		locationListener = new MyListener();
		locationManager.requestLocationUpdates(locationProvider, 3000, 10,
				locationListener);
		Location location=locationManager.getLastKnownLocation(locationProvider);
		
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				Location location=locationManager.getLastKnownLocation(locationProvider);
//				while(location  == null)   
//				{   
//				  locationManager.requestLocationUpdates(locationProvider, 6000, 1, locationListener);   
//				}  
//			}
//		}).start();
	}

	
	@Override
	protected void onResume() {
		//turnOnLocation();
		super.onResume();
	}
	


	@Override
	protected void onPause() {
//		if (locationManager != null) { 
//            locationManager.removeUpdates(locationListener); 
//
//        } 
		super.onPause();
	}



	private class MyListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			Location sLocation=location;
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

	}
}

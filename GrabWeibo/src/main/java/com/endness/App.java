package com.endness;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import weibo4j.model.WeiboException;

public class App 
{

	private static Log log=LogFactory.getLog(App.class);

	private static String access_token = "2.00c3qzqB0I9ch84cbaf87978OCRBtC";

	public static void main( String[] args ) throws WeiboException, IOException, InterruptedException, SQLException
    {
		
		while(true)
		{
			try{
				DBHelper.openCon();
				WeiboHelp weiboHelp=new WeiboHelp(access_token);
				//weiboHelp.loadKeywords();
				ArrayList<String> sqls=weiboHelp.getPublic(200,0);
				DBHelper.openCon();
				
				for (String sql : sqls) {
					log.info(sql);
					System.out.println(sql);
					DBHelper.ExecStat(sql);
				}
				Thread.sleep(1000*20);
		}catch (Exception e) {
			e.printStackTrace();
		}
    }
    }
}

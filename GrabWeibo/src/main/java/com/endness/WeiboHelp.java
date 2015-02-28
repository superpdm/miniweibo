package com.endness;

import java.awt.RenderingHints.Key;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import weibo4j.Timeline;
import weibo4j.model.Status;
import weibo4j.model.StatusWapper;
import weibo4j.model.WeiboException;

public class WeiboHelp {
	private String access_token = null;
	private Timeline tm = new Timeline();
	private static Log log=LogFactory.getLog(WeiboHelp.class);
	public WeiboHelp(String ac){
		access_token=ac;
		tm.setToken(access_token);
	}
	public ArrayList<String> getPublic(int count,int baseApp) throws WeiboException
	{
		StatusWapper status = tm.getPublicTimeline(count, baseApp);
		ArrayList<String> sqlList=new ArrayList<String>();
		for (Status s : status.getStatuses()) {
			StringBuffer sqlb=composeSQL(s);
			if(sqlb==null)
			{
				log.warn("sql is null");
				System.out.println("sql is null");
				continue;
				
			}
			sqlList.add(sqlb.toString());
			
		}
		return sqlList;
	}
	private StringBuffer composeSQL(Status s)
	{
		StringBuffer sql=new StringBuffer();
		// recursion
    	if (s.getRetweetedStatus() != null) {
    		return composeSQL(s.getRetweetedStatus());
    	}

    	HashMap<String, ArrayList<KeyWord>> keywords=null;
		try {
				keywords = loadKeywords();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	// iterate over keywords to check match

    	String text=s.getText();
    	
    	String resType=getMatch(text, keywords);
		
		// if no match, return
		if (resType.equals(""))
			return null;
		
		// output to filestream
		if (s.getUser() == null)
			return null;

		sql.append("insert into WEIBO_RAW values(");
		sql.append("null"+",'");
		sql.append(s.getCreatedAt().toLocaleString()+"','");
		sql.append(s.getText()
				.replaceAll("[\\f\\n\\r\\t\\v]","")
				.replaceAll("'","''")
				+"','");
		sql.append(s.getSource()+"','");
		sql.append(s.getThumbnailPic()+"','");
		sql.append(s.getBmiddlePic()+"','");
		sql.append(s.getOriginalPic()+"',");
		sql.append(s.getLatitude()+",");
		sql.append(s.getLongitude()+",");
		sql.append(0+",");		//抓数据的爬虫id=0
		sql.append(0+",");		//抓数据的爬虫id=0
		sql.append(0+",'"); 		//赞的数目为0
		sql.append(resType+"')");	//类型
					
			return sql;

	}
	public  HashMap<String, ArrayList<KeyWord>> loadKeywords() throws Exception
	{
		HashMap<String, ArrayList<KeyWord>> keywords=new HashMap<String, ArrayList<KeyWord>>();
    	BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream("keywords.txt")));

    	String tmpString="";
    	
    	while((tmpString=reader.readLine())!=null)
    	{
    		tmpString=tmpString.trim();
    		if(tmpString.equals(""))
    			continue;
    		String type="";
    		if(!tmpString.endsWith(":")&& !tmpString.endsWith("："))
    			throw new KeywordFormatException();
    		type=tmpString.substring(0,tmpString.length()-1).trim();
    			
    		while((tmpString=reader.readLine().trim()).equals("")) ;
    		if(!tmpString.equals("{"))
    			throw new KeywordFormatException();
    		
    		ArrayList<KeyWord> words=new ArrayList<KeyWord>();
    		while((tmpString=reader.readLine().trim())!=null )
        	{
        		if(tmpString.equals(""))
        			continue;
        		if(tmpString.equals("}"))
        			break;
        		String[] strs=tmpString.split(" ");
        		KeyWord keyWord=new KeyWord();
        		for (int i = 0; i < strs.length; i++) {
        			strs[i]=strs[i].trim();
        			if(!strs[i].startsWith("-"))
        				keyWord.includes.add(strs[i]);
        			else {
						keyWord.excludes.add(strs[i].substring(1));
					}
    			}
        		words.add(keyWord);
        	
        	}
    		keywords.put(type, words);
    	}
    	
    		return keywords;
	}

	private String getMatch(String text,HashMap<String, ArrayList<KeyWord>> keywords)
	{
		String resType = "";
		if(keywords==null)
			return resType;
		Iterator<String> itType=keywords.keySet().iterator();
    	while(itType.hasNext())
    	{
    		String type=itType.next();
    		ArrayList<KeyWord> words=keywords.get(type);
    		Iterator<KeyWord> itWords = words.iterator();
    		while (itWords.hasNext()) {
    			KeyWord keyword = itWords.next();
    			boolean isMatch=true;
    			for (String word : keyword.includes) {
    				if(!text.contains(word)){
    					isMatch=false;
    					break;
    				}
    			}
    			if(isMatch)
    				for (String word : keyword.excludes) {
        				if(text.contains(word)){
        					isMatch=false;
        					break;
        				}
        			}
    			if(isMatch)
    			{
    				resType=type;
    				break;
    			}

    		}
    	}
    	return resType;
		
	}
}
class KeywordFormatException extends Exception
{
	
}

class KeyWord
{
	public ArrayList<String> includes=new ArrayList<String>();
	public ArrayList<String> excludes=new ArrayList<String>();
}
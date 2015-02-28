package com.endness;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DBHelper {
    
	private static Connection connection=null;

	public static String DB_PASSWORD ;
	public static String DB_USERNAME ;
    /** Oracle数据库连接URL*/
	public static String DB_URL ;
    
    /** Oracle数据库连接驱动*/
    private final static String DB_DRIVER = "com.mysql.jdbc.Driver";
    private static Log log=LogFactory.getLog(WeiboHelp.class);
    
    static{
    	BufferedReader bReader;
		try {
			bReader = new BufferedReader(new FileReader("db.cfg"));
			DB_URL=bReader.readLine().trim();
			DB_USERNAME=bReader.readLine().trim();
			DB_PASSWORD=bReader.readLine().trim();
		
		} catch (Exception e) {
			log.error(e.getStackTrace());
		}
		
    }
    private  DBHelper() throws Exception {
		connection=getConnection();
		
	}
    
    public static ResultSet ExecQuery(String sql) throws SQLException
    {
    	if(connection==null )
    	{
    		openCon();
    	}
    	Statement stmt=connection.createStatement();

    	ResultSet rset = stmt.executeQuery(sql);
    	
    	return rset;
    }
    public static boolean ExecStat(String sql) throws SQLException
    {
    	openCon();
    	Statement stmt=connection.createStatement();

    	//ResultSet rset = stmt.executeQuery(sql);
    	boolean ret=stmt.execute(sql);
    	closeConnection(connection);
    	
    	return ret;
    }
    public static boolean openCon()
    {
    	try{
	    	if(connection==null)
	    	{
	    		connection=getConnection();
	    	}
	    	if(connection.isClosed())
	    		connection=getConnection();
    	}
    	catch(Exception e)
    	{
    		return false;
    	}
    	return true;
    }
    /**
     * 获取数据库连接
     * @return
     */
    private static Connection getConnection(){
        /** 声明Connection连接对象*/
        Connection conn = null;
        try{
            /** 使用Class.forName()方法自动创建这个驱动程序的实例且自动调用DriverManager来注册它*/
            Class.forName(DB_DRIVER);
            /** 通过DriverManager的getConnection()方法获取数据库连接*/
            conn = DriverManager.getConnection(DB_URL,DB_USERNAME,DB_PASSWORD);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return conn;
    }
    
    /**
     * 关闭数据库连接
     * 
     * @param connect
     */
    private static void closeConnection(Connection conn){
        try{
            if(conn!=null){
                /** 判断当前连接连接对象如果没有被关闭就调用关闭方法*/
                if(!conn.isClosed()){
                    conn.close();
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
}
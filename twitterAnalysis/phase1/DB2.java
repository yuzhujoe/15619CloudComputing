package model;

import java.sql.*;
import java.util.ArrayList;


public class DB2 {	
	//query 
	public static ArrayList<String> queryTweets(String userid, String tweet_time) throws SQLException{
		
		StringBuffer sb = new StringBuffer();
		String sql = "select id, sentiment, text from temp.allTweets where user_id = '"+userid+"' and create_at = '"+tweet_time+"'";
		//String sql = "select id, sentiment, text from ccdatabase.Query2 where user_id = '"+userid+"' and create_at = '"+tweet_time+"'";

		sb.append(sql);
		
		ArrayList<String> arr = new ArrayList<String>();
		ConnectionPool pool = ConnectionPool.getInstance();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = pool.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sb.toString());
			
			while (rs.next()){
				String temp = rs.getString("id");
				temp +=":";
				temp += rs.getString("sentiment");
				temp +=":";
				temp +=rs.getString("text");
				arr.add(temp);
			}	
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return arr;
		
	}
	
//	public static void main(String[] args){  
//		ArrayList<String> arr = new ArrayList<String>();
//		try {
//			arr = queryTweets("177965281", "2014-05-06+18:09:24");
//			System.out.println(arr.size());
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		String dataStr = "";
//		for(int i=0;i<arr.size();i++){
//			dataStr=arr.get(i);
//		}
//		System.out.println(dataStr);
//        
//    }  
//	
}


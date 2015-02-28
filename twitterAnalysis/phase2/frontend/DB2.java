package model;

import java.sql.*;
import java.util.ArrayList;


public class DB2 {	
	//query 
	@SuppressWarnings("finally")
	public static ArrayList<String> queryQ2(String userId, String time){
		
		//StringBuffer sb = new StringBuffer();
		//String sql = "select result from tweet.TweetQ2 where combinekey = \""+userIdTime+"\"";
		//String sql = "select id, sentiment, text from ccdatabase.Query2 where user_id = '"+userid+"' and create_at = '"+tweet_time+"'";

		//sb.append(sql);
		
		ArrayList<String> arr = new ArrayList<String>();
		ConnectionPool pool = ConnectionPool.getInstance();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = pool.getConnection();
			stmt = conn.prepareStatement("select result from tweet.TweetQ2 where userid = ? and time = ?");
			stmt.setString(1, userId);
			stmt.setString(2, time);

			rs = stmt.executeQuery();
			
			while (rs.next()){
				String temp = rs.getString("result");
				arr.add(temp);
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }finally {
            if (stmt != null)
            {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        
            if (rs != null)
            {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        
            if (conn != null)
            {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            return arr;
        }
	}
	
public static ArrayList<String> queryQ3(String userid){
		
	ArrayList<String> arr = new ArrayList<String>();
	
	if(userid!=null && userid!=""){
		//StringBuffer sb = new StringBuffer();
		String sql = "select retweets from tweet.TweetQ3 where userid = "+userid;
		
		//sb.append(sql);
		
		ConnectionPool pool = ConnectionPool.getInstance();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = pool.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			while (rs.next()){
				String temp = rs.getString("retweets");
				arr.add(temp);
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }finally{
            if (stmt != null)
            {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        
            if (rs != null)
            {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        
            if (conn != null)
            {
                try {
                	conn.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
		return arr;
	}
	else{
		return arr;
	}
		
}

public static ArrayList<String> queryQ4(String combineKey, String m, String n){
	 
	ArrayList<String> arr = new ArrayList<String>();
	
	if(m!=null && m!="" && n!=null && n!=""){
		int mInt = Integer.parseInt(m);
		int nInt = Integer.parseInt(n);
		ConnectionPool pool = ConnectionPool.getInstance();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = pool.getConnection();
			stmt = conn.prepareStatement("select tweets from tweet.TweetQ4 where datelocation = ? and rank >= ? and rank <= ?");
			stmt.setString(1, combineKey);
			stmt.setInt(2, mInt);
			stmt.setInt(3, nInt);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
//		StringBuffer sb = new StringBuffer();
//		String sql = "select tweets from tweet.TweetQ4 where datelocation = \""+combineKey+"\" "+"and rank >="+ mInt+" and rank <= "+nInt;
		//String sql = "select id, sentiment, text from ccdatabase.Query2 where user_id = '"+userid+"' and create_at = '"+tweet_time+"'";

//		sb.append(sql);
		try {
			
			rs = stmt.executeQuery();
			
			while (rs.next()){
				String temp = rs.getString("tweets");
				arr.add(temp);
			}	
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }finally{
	
            if (stmt != null)
            {
                try {
                    stmt.close();
                }
                catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
    
            if (rs != null)
            {
                try {
                    rs.close();
                }
                catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
    
            if (conn != null)
            {
                try {
                    conn.close();
                }
                catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
		return arr;
	}
	else{
		return arr;
	}

}
	
}


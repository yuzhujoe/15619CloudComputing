package service;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.DB2;

public class queryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final BigInteger tempX = new BigInteger("6876766832351765396496377534476050002970857483815262918450355869850085167053394672634315391224052153");
	private static HashMap<String, String> hashmap = new HashMap<String, String>();
	private static HashMap<String, String> hashmap2 = new HashMap<String, String>();
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
	
	public queryServlet() {
		super();
		//Hbase.init();
		//DB.init();
		// TODO Auto-generated constructor stub
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	
		StringBuffer uri = request.getRequestURL();
		String querySplit = uri.substring(uri.length()-2, uri.length());
		
		if(querySplit.equals("q1")){
			String key = request.getParameter("key");
			String Ystr = hashmap.get(key);
			if(Ystr==null){
				BigInteger tempKey = new BigInteger(key);
				BigInteger tempY = tempKey.divide(tempX);
				Ystr = tempY.toString();
				hashmap.put(key, Ystr);
			}
			
			Date curDate = new Date(System.currentTimeMillis());//get current time  
			String str = formatter.format(curDate);  
			
			PrintWriter out = response.getWriter();
			out.println(Ystr+'\n'+"HitforBrains,6919-6751-9752,0437-8596-0002,3990-0401-3708"+'\n'+str+'\n');
		}
		else if(querySplit.equals("q2")){
			String userid = request.getParameter("userid");
			String tweet_time = request.getParameter("tweet_time");
			tweet_time = tweet_time.replace(" ", "+");
			String userIdTime = userid+tweet_time;
			response.setContentType("text/plain;charset=UTF-8");
			
			String result = hashmap2.get(userIdTime);
			if(result == null){
				ArrayList<String> arr= new ArrayList<String>();
				
				//arr = Hbase.queryTweets(userIdTime);
	            try {
					arr=DB2.queryTweets(userid, tweet_time);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
	            String dataStr = "";
				for(int i=0;i<arr.size();i++){
					dataStr += arr.get(i);
				}
				hashmap2.put(userIdTime, dataStr);
				result = dataStr;
			}
			
			//dataStr=dataStr.replaceAll("(?:\r\n|\n|\r)*$", ""); 
			PrintWriter out = response.getWriter();
			out.print("HitforBrains,6919-6751-9752,0437-8596-0002,3990-0401-3708"+'\n'+result+"\n");
		}
		else{
			
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}


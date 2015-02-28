package service;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import model.Hbase;

public class queryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final BigInteger tempX = new BigInteger("6876766832351765396496377534476050002970857483815262918450355869850085167053394672634315391224052153");
	private static final int MAXLEN = 50000000;
	private static HashMap<String, String> hashmap = new HashMap<String, String>();
	private static HashMap<String, String> hashmap2 = new HashMap<String, String>();
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
	
	public queryServlet() {
		super();
		
	}
	
//	public void init(){
///*		try {
////			query6.store();
//		} catch (NumberFormatException |  e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}*/
//	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	
		StringBuffer uri = request.getRequestURL();
		String querySplit = uri.substring(uri.length()-2, uri.length());
		
		if(querySplit.equals("q1")){
			String key = request.getParameter("key");
			//String Ystr = hashmap.get(key);
			//if(Ystr==null){
				BigInteger tempKey = new BigInteger(key);
				BigInteger tempY = tempKey.divide(tempX);
				String Ystr = tempY.toString();
//				if(hashmap.size() < 60000000){
//					hashmap.put(key, Ystr);
//				}
			//}
			
			Date curDate = new Date(System.currentTimeMillis());//get current time  
			String str = formatter.format(curDate);  
			
			PrintWriter out = response.getWriter();
			out.println(Ystr+'\n'+"HitforBrains,6919-6751-9752,0437-8596-0002,3990-0401-3708"+'\n'+str+'\n');
			out.flush();
			out.close();
			
//			String key = request.getParameter("key");
//			BigInteger tempKey = new BigInteger(key);
//			BigInteger tempY = tempKey.divide(tempX);
//			String Ystr = tempY.toString();
//			
//			Date curDate = new Date(System.currentTimeMillis());//get current time  
//			String str = formatter.format(curDate);  
//			
//			PrintWriter out = response.getWriter();
//			out.println(Ystr+'\n'+"HitforBrains,6919-6751-9752,0437-8596-0002,3990-0401-3708"+'\n'+str+'\n');
//			out.flush();
//			out.close();
			
		}
		else if(querySplit.equals("q2")){
//			String userid = request.getParameter("userid");
//			String tweet_time = request.getParameter("tweet_time");
//			String userIdTime = userid+tweet_time;
//			String result = "";
//			response.setContentType("text/plain;charset=UTF-8");
//			
//			String hashValue;
//			if((hashValue = hashmap2.get(userIdTime)) != null){
//				result = hashValue;
//			}else{
//				result = Hbase.queryQ2(userIdTime).toString();
//				if(hashmap2.size() < 20000000){
//					hashmap2.put(userIdTime, result);
//				}
//			}
//						
//			PrintWriter out = response.getWriter();
//			out.print("HitforBrains,6919-6751-9752,0437-8596-0002,3990-0401-3708"+'\n'+result+"\n");
//			out.flush();
//			out.close();
			
			String userid = request.getParameter("userid");
			String tweet_time = request.getParameter("tweet_time");
			String userIdTime = userid+tweet_time;
			response.setContentType("text/plain;charset=UTF-8");
			String result = Hbase.queryQ2(userIdTime).toString();
			PrintWriter out = response.getWriter();
			out.print("HitforBrains,6919-6751-9752,0437-8596-0002,3990-0401-3708"+'\n'+result+"\n");
			out.flush();
			out.close();
			
		}
		else if(querySplit.equals("q3")){
			String userid = request.getParameter("userid");
			response.setContentType("text/plain;charset=UTF-8");
			
			String dataStr = Hbase.queryQ3(userid).toString();
			PrintWriter out = response.getWriter();
			out.print("HitforBrains,6919-6751-9752,0437-8596-0002,3990-0401-3708"+'\n'+dataStr);
			out.flush();
			out.close();
		}
			
		else if(querySplit.equals("q4")){
			String date = request.getParameter("date");
			String location = request.getParameter("location");
			String m = request.getParameter("m");
			String n = request.getParameter("n");
			
			response.setContentType("text/plain;charset=UTF-8");
			String combineKey = date+location;
			String dataStr = Hbase.queryQ4(combineKey, m, n);
			
			PrintWriter out = response.getWriter();
			out.print("HitforBrains,6919-6751-9752,0437-8596-0002,3990-0401-3708"+'\n'+dataStr);
			out.flush();
			out.close();
		}
		
		else if(querySplit.equals("q5")){
			String m = request.getParameter("m");
			String n = request.getParameter("n");
			
			response.setContentType("text/plain;charset=UTF-8");
			String dataStr = Hbase.queryQ5(m, n).toString();
			
			PrintWriter out = response.getWriter();
			out.print("HitforBrains,6919-6751-9752,0437-8596-0002,3990-0401-3708"+'\n'+dataStr);
			out.flush();
			out.close();
		}
		else if(querySplit.equals("q6")){
			String m = request.getParameter("m");
			String n = request.getParameter("n");
			
			response.setContentType("text/plain;charset=UTF-8");
			int dataStr = Hbase.queryQ6(m, n);
			
			PrintWriter out = response.getWriter();
			out.print("HitforBrains,6919-6751-9752,0437-8596-0002,3990-0401-3708"+'\n'+dataStr+"\n");
			out.flush();
			out.close();
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


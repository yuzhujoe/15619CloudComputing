package model;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

public class Hbase 
{
	//private static HTablePool pool ;
    
    public static StringBuffer queryQ2(String userid_create_at) throws IOException{
    	
    	HTableManager hTableManager = HTableManager.getInstance();
		HTableInterface tweetTable_q2 = hTableManager.getHTable("Tweet_Q2");
		
	//	conf = HBaseConfiguration.create();
	//	pool = new HTablePool(conf, 1000);  
	//	hTable.
	//	tweetTable_q2 = ((HTablePool) hTable).getTable("Tweet_Q2"); 
		
    	StringBuffer arr = new StringBuffer();
    	//get
    	Get g = new Get(Bytes.toBytes(userid_create_at));
    	Result r = tweetTable_q2.get(g);
    	
    	for(KeyValue kv:r.raw()){   
            arr.append(Bytes.toString(kv.getValue()).replace("\\\n", "\n"));
         }  
    	
    	if(tweetTable_q2!=null){
        	tweetTable_q2.close();
        }
    	return arr;
    } 
    
    public static StringBuffer queryQ3(String userid) throws IOException{
    	
    	HTableManager hTableManager = HTableManager.getInstance();
		HTableInterface tweetTable_q3 = hTableManager.getHTable("Tweet_Q3");
		
    	StringBuffer arr = new StringBuffer();
    	//get
    	Get g = new Get(Bytes.toBytes(userid));
    	Result r = tweetTable_q3.get(g);
        
        if(tweetTable_q3!=null){
            tweetTable_q3.close();
        }

    	for(KeyValue kv:r.raw()){   
            arr.append(Bytes.toString(kv.getValue()).replace("\\\n", "\n")+"\n");
         } 
        return arr;
    }  
    
    
    public static String queryQ4(String combineKey, String m, String n) throws IOException{
    	
    	String result = "";
    	String rankString = "";
    	
    	if(m!=null && m!="" && n!=null && n!=""){
    		int mInt = Integer.parseInt(m);
    		int nInt = Integer.parseInt(n);
    		
    		HTableManager hTableManager = HTableManager.getInstance();
    		HTableInterface tweetTable_q4 = hTableManager.getHTable("Tweet_Q4");
    		
    		Get g = new Get(Bytes.toBytes(combineKey));
        	Result r = tweetTable_q4.get(g);
        	
            tweetTable_q4.close();
        	for(KeyValue kv:r.raw()){   
        		rankString += Bytes.toString(kv.getValue());
             } 
        	String[] tableResult = rankString.split("\t");
        	if(mInt > tableResult.length){
        		return result;
        	}
        	
        	else{
        		
        		if(nInt > tableResult.length){
        			nInt = tableResult.length;
        		}
        		
        		for(int i=mInt-1; i<nInt; i++){
        			result += tableResult[i]+"\n";
        		}
        		return result;
        	}
        	
    	}
    	else{
    		return result;
    	}
    	
    	
//        StringBuilder sb = new StringBuilder();
//    	if(m!=null && m!="" && n!=null && n!=""){
//    		//m=String.format("%0"+(5-m.length())+"d",0)+m;
//    		//n=String.format("%0"+(5-n.length())+"d",0)+n;
//    		int mInt = Integer.parseInt(m);
//    		int nInt = Integer.parseInt(n)+1;
//    		
//    		String str_m = String.format("%0"+5+"d", mInt);
//            String str_n = String.format("%0"+5+"d", nInt);
//            
//    		HTableManager hTableManager = HTableManager.getInstance();
//    		HTableInterface tweetTable_q4 = hTableManager.getHTable("Tweet_Q4_1");
//    		
//    		Scan s = new Scan();
//            s.setStartRow(Bytes.toBytes(combineKey+str_m));
//            s.setStopRow(Bytes.toBytes(combineKey+str_n));
//            
//            ResultScanner rs =  tweetTable_q4.getScanner(s);
//            for (Result r : rs) 
//            {  
//            	for(KeyValue kv:r.raw()){   
//            		sb.append((Bytes.toString(kv.getValue()))).append("\n");
//                 }  
//           
//            }  
//            if(rs!=null){
//            	rs.close();
//            }
//            if(tweetTable_q4!=null){
//            	tweetTable_q4.close();
//            }
//            
//            
//    		return sb.toString();
//    		
//    	}
//    	else{
//    		return sb.toString();
//    	}
//    	
    	
    }
    
    public static StringBuffer queryQ5(String m, String n) throws IOException{
    	
    	StringBuffer result= new StringBuffer(m+"\t"+n+"\t"+"WINNER"+"\n");
    	
    	if(m!=null && m!="" && n!=null && n!=""){
    		
    		HTableManager hTableManager = HTableManager.getInstance();
    		HTableInterface tweetTable_q5 = hTableManager.getHTable("Tweet_Q5");
    		
    		Get get1 = new Get(Bytes.toBytes(m));
            Get get2 = new Get(Bytes.toBytes(n));
            Result r1 = tweetTable_q5.get(get1);  
            Result r2 = tweetTable_q5.get(get2); 
           
            tweetTable_q5.close();
            int score1 = 0;
            int score2 = 0;
            int flag1 = 0;
            int flag2 = 0;
            if(r1.raw().length < 2){
                flag1 = 1;
            }
            if(r2.raw().length < 2){
                flag2 = 1;
            }
            
            for(int i=0; i<4; i++){
                if(flag1 == 1 && flag2 == 1){
                    result.append(0+"\t"+0+"\t"+"X"+"\n");
                }else if(flag1 == 1 && flag2 == 0){
                    score2 = Bytes.toInt(r2.raw()[i].getValue());
                    if(score2 == 0){
                        result.append(0+"\t"+score2+"\t"+"X"+"\n");
                    }else{
                        result.append(0+"\t"+score2+"\t"+n+"\n");
                    }
                }else if(flag1 == 0 && flag2 == 1){
                    score1 = Bytes.toInt(r1.raw()[i].getValue());
                    if(score1 == 0){
                        result.append(score1+"\t"+0+"\t"+"X"+"\n");
                    }else{
                        result.append(score1+"\t"+0+"\t"+m+"\n");
                    }
                }else{
                    score1 = Bytes.toInt(r1.raw()[i].getValue());
                    score2 = Bytes.toInt(r2.raw()[i].getValue());
                    if(score1<score2){
                        result.append(score1+"\t"+score2+"\t"+n+"\n");
                    }
                    else if(score1>score2){
                      result.append(score1+"\t"+score2+"\t"+m+"\n");
                    }
                    else{
                        result.append(score1+"\t"+score2+"\tX\n");
                    }
                }
            }
/*            for(int i=0; i<4; i++){
            	score1 = Bytes.toInt(r1.raw()[i].getValue());
            	score2 = Bytes.toInt(r2.raw()[i].getValue());
            	if(score1<score2){
            		result.append(score1+"\t"+score2+"\t"+n+"\n");
            	}
            	else if(score1>score2){
            		result.append(score1+"\t"+score2+"\t"+m+"\n");
            	}
            	else{
            		result.append(score1+"\t"+score2+"\tX\n");
            	}
            }
*/        	return result;
    	}
    	else{
    		return result;
    	}
    }
    
    public static int queryQ6(String m, String n) throws IOException{
    	/*		String result = "";
    			
    			if(m!=null && m!="" && n!=null && n!=""){
    	    		long mInt = Integer.parseInt(m);
    	    		long nInt = Integer.parseInt(n);
    	    		
    	    		if(mInt > nInt){
    	    			return "0"+"\n";
    	    		}
    	    		else{
    	    			while(hashmap6.get(nInt)==null){
    	    				nInt--;
    	    			}
    	    			int nResult = hashmap6.get(nInt);
    	    			
    	    			mInt--;
    	    			while(hashmap6.get(mInt)==null){
    	    				mInt--;
    	    			} 
    	    			int mResult = hashmap6.get(mInt);
    	    			System.out.println(nResult-mResult);
    	    			return (nResult-mResult)+"\n";
    	    		}
    	    		
    			}
    			else{
    				return result;	
    			}*/
    			
    			StringBuilder result=new StringBuilder();
    	    	
    	    	if(m!=null && m!="" && n!=null && n!=""){
    	    		
    	    		long mInt = Long.parseLong(m);
    	    		long nInt = Long.parseLong(n);
    	    		
    	    		long max = Long.parseLong("2594921026");
    	    		
    	    		if(mInt > nInt){
    	    			return 0;
    	    		}
    	    		
    	    		else{
    	    			if(nInt > max){
    	    				nInt = max;
    	    			}
    	 
    	    			HTableManager hTableManager = HTableManager.getInstance();
    	        		HTableInterface tweetTable_q6 = hTableManager.getHTable("Tweet_Q6");
    	        		
    	        		String str_m = String.format("%0"+10+"d", mInt);
    	                String str_n = String.format("%0"+10+"d", nInt+1);
    	                
    	        		Scan scan1 = new Scan(Bytes.toBytes(str_m));
    	                Scan scan2 = new Scan(Bytes.toBytes(str_n));
    	                scan1.setBatch(1);
    	                scan2.setBatch(1);
    	                    
    	                ResultScanner rs1 =  tweetTable_q6.getScanner(scan1);
    	                ResultScanner rs2 =  tweetTable_q6.getScanner(scan2);
    	                
    	                Result r1 = rs1.next();
    	                Result r2 = rs2.next(); 
    	                
                        tweetTable_q6.close();
    	                int value1 = 0;
    	                int value2 = 0;
    	                for (KeyValue keyValue : r1.raw()) {  
    	                	value1 = Bytes.toInt(keyValue.getValue());
    	                }
    	            	
    	                for (KeyValue keyValue : r2.raw()) {
    	                	value2 = Bytes.toInt(keyValue.getValue());
    	                }
    	                rs1.close();
    	                rs2.close();
    	                
    	                return (value2 - value1);
    	    		}
    	    		
    	    	}
    	    	else{
    	    		return 0;
    	    	}
    			
    		}
    
}

class HTableManager 
{
    private static HTableManager instance;
    private static HTablePool hTablePool;

    /*
     * Private Constructor
     */
    private HTableManager() 
    {
        Configuration config = HBaseConfiguration.create(); 
        //config.set("hbase.zookeeper.quorum", "ip-172-31-18-248.ec2.internal");
//        config.set("hbase.regionserver.global.memstore.upperLimit","0.25");
//        config.set("hbase.regionserver.global.memstore.lowerLimit","0.2");
//        config.set("hfile.block.cache.size","0.5");
//        config.set("hbase.regionserver.handler.count","1000");
        config.addResource(new Path("/home/ubuntu/hbase-site.xml"));
        hTablePool = new HTablePool(config, 1000);     
    }
    /*
     * @return The HbaseTableManager instance
     */
    public static HTableManager getInstance() 
    {
        if (instance == null) {
            instance = new HTableManager();
        }
        return instance;
    }

    /*
     * Method used to retrieve a HTable instance.
     * 
     * @param tableName The table name
     * @return The HTableInterface instance
     * @throws IOException 
     */
    public synchronized HTableInterface getHTable(String tableName) throws IOException 
    {
        return hTablePool.getTable(tableName);
    }
}

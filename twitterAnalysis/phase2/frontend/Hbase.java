package model;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

public class Hbase 
{
	//private static HTablePool pool ;
    
    public static ArrayList<String> queryTweets(String userid_create_at) throws IOException{
    	
    	HTableManager hTableManager = HTableManager.getInstance();
		HTableInterface tweetTable_q2 = hTableManager.getHTable("Tweet_Q2");
		
	//	conf = HBaseConfiguration.create();
	//	pool = new HTablePool(conf, 1000);  
	//	hTable.
	//	tweetTable_q2 = ((HTablePool) hTable).getTable("Tweet_Q2"); 
		
    	ArrayList<String> arr = new ArrayList<String>();
    	//get
    	Get g = new Get(Bytes.toBytes(userid_create_at));
    	Result r = tweetTable_q2.get(g);
    	
    	for(KeyValue kv:r.raw()){   
            arr.add(Bytes.toString(kv.getValue()));
         }  
   
    	return arr;
    } 
    
    public static ArrayList<String> queryQ3(String userid) throws IOException{
    	
    	HTableManager hTableManager = HTableManager.getInstance();
		HTableInterface tweetTable_q3 = hTableManager.getHTable("Tweet_Q3");
		
    	ArrayList<String> arr = new ArrayList<String>();
    	//get
    	Get g = new Get(Bytes.toBytes(userid));
    	Result r = tweetTable_q3.get(g);
    	
    	for(KeyValue kv:r.raw()){   
            arr.add(Bytes.toString(kv.getValue()).replaceAll("\\\\n", "\n"));
         }  
   
    	return arr;
    }  
    
    
    public static ArrayList<String> queryQ4(String combineKey, String m, String n) throws IOException{
    	
    	ArrayList<Get> arrGet = new ArrayList<Get>();
    	ArrayList<String> arrResult = new ArrayList<String>();
    	
    	if(m!=null && m!="" && n!=null && n!=""){
    		int mInt = Integer.parseInt(m);
    		int nInt = Integer.parseInt(n);
    		
    		HTableManager hTableManager = HTableManager.getInstance();
    		HTableInterface tweetTable_q4 = hTableManager.getHTable("Tweet_Q4");
    		
    		for(int i=mInt;i<nInt+1; i++){
    			Get g = new Get(Bytes.toBytes(combineKey+i));
    			arrGet.add(g);
    		}
        	//get
        	Result[] r = tweetTable_q4.get(arrGet);
        	for (int i = 0;i < r.length;i++)
            {
            	for (KeyValue kv : r[i].raw()) 
                {  
            		arrResult.add(Bytes.toString(kv.getValue()));
                }
            }
        
        	return arrResult;
    	}
    	else{
    		return arrResult;
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
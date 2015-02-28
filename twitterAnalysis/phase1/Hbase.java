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
    	String temp = "";
    	
    	for(KeyValue kv:r.raw()){  
    		
            temp += new String(kv.getValue());  
            arr.add(temp.replaceAll("\\?\\?", "\\?"));
         }  
   
    	return arr;
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
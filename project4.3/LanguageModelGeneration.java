import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

public class LanguageModelGeneration {
	public static class Map extends Mapper<LongWritable, Text, Text, Text>{
		int t;
		public void setup(Context context){
			Configuration conf = context.getConfiguration();
			t = conf.getInt("t", 0);	
		}
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
			
			String eachline = value.toString().trim();
			if (eachline != null && eachline.length() > 0) {
				//Separate phrase and count
				String[] linewords = eachline.split("\t");
				//Generate key and value of map
				if (linewords.length>1){
					int count_phrase = Integer.parseInt(linewords[linewords.length-1]);
					if(count_phrase>=t){
						String[] phrases = linewords[0].split("\\s+");
						//Generate key
						String phraseKey = "";
						if(phrases.length == 1){
							return;
						}
						else{
							for(int i=0; i<phrases.length-1; i++){
								phraseKey += phrases[i]+" ";
							}
						}
						//Generate value
						String phraseValue = phrases[phrases.length-1];
						if(phraseKey!=null && phraseKey.length()>0){
							context.write(new Text(phraseKey.trim()), new Text(phraseValue+"="+count_phrase));
						}
					}
				}
	            
			}

		}
	}
	
	public static class Reduce extends TableReducer<Text, Text, Text>{
		//class for value and count
		public class ValueCount implements Comparable<ValueCount>{
			String value;
			int count;
			
			ValueCount(String value, int count){
				this.value = value;
				this.count = count;
			}
			
			@Override
			public int compareTo(ValueCount vc) {
				// TODO Auto-generated method stub
				//First compare count. If count is same, then compare value.
				if (this.count < vc.count) {
					return -1;
				} else if (this.count > vc.count) {
				    return 1;
				} else if (this.value.compareTo(vc.value) > 0) {
				    return -1;
				} else if (this.value.compareTo(vc.value) < 0) {
				    return 1;
				} else {
				    return 0;
				}

			}
		}
		
		int n = 5;
		SortedSet<ValueCount> sort;
		public void setup(Context context){
			sort = new TreeSet<ValueCount>(){
				/**
				 * override add method in SortedSet
				 */
				private static final long serialVersionUID = 1L;

				public boolean add(ValueCount vc){
					boolean iffull = false;
					iffull = super.add(vc);
					if(size()>n){
						remove(last());
					}
					return iffull;
				}
			};
			//get configuration
			Configuration conf = context.getConfiguration();
			//get n
			n = conf.getInt("n", 5);
			
		}
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException{
			//change values to ValueCount, and add ValueCount to sort set
			for(Text value:values){
				String[] valueWord = value.toString().trim().split("=");
				ValueCount valuecount = new ValueCount(valueWord[0], Integer.parseInt(valueWord[1]));
				sort.add(valuecount);
			}
			int qualifier = 0;
			while(sort.size()!=0){
				ValueCount valueCount = sort.first();
				qualifier ++;
				byte[] rowkey = Bytes.toBytes(key.toString());
				byte[] familyname = Bytes.toBytes("d");
				byte[] columnqualifier = Bytes.toBytes(qualifier);
				byte[] columnvalue = Bytes.toBytes(valueCount.value);
				Put p = new Put(rowkey);
				p.add(familyname, columnqualifier, columnvalue);
				context.write(null, p);
				sort.remove(sort.first());
			}
			
		}
		
	}
	
	

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		// TODO Auto-generated method stub
		String inputfile = args[0];
		//String outputfile = args[1];
		String t = args[1];
		String n = args[2];
		String table_name = "LMG";

		
		//Configuration conf = new Configuration();
		Configuration conf = HBaseConfiguration.create();
		conf.set("t", t);
		conf.set("n", n);
		Job job = new Job(conf, "Language Model Generation");
		job.setJarByClass(LanguageModelGeneration.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		
		//job.setInputFormatClass(TextInputFormat.class);
	    //job.setOutputFormatClass(TextOutputFormat.class);

	    FileInputFormat.setInputPaths(job, inputfile);
        //TextOutputFormat.setOutputPath(job, new Path(outputfile));

        TableMapReduceUtil.initTableReducerJob(table_name, Reduce.class, job);
        job.waitForCompletion(true);

	}
	

}

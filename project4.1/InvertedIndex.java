package invertedIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class InvertedIndex {

	public static class Map extends Mapper<LongWritable, Text, Text, Text>{
		
		HashSet<String>  stopWordSetMapper = new HashSet<String>();
		
		public void setup(Context context) throws IOException, InterruptedException{
			super.setup(context);
			URI[] uri = DistributedCache.getCacheFiles(context.getConfiguration());
		    FileSystem fileSystem = FileSystem.get(context.getConfiguration());
		    FSDataInputStream dataInputStream = fileSystem.open(new Path(uri[0].getPath()));
		    ObjectInputStream objectInputStream = new ObjectInputStream(dataInputStream);
		    try {
		    	stopWordSetMapper = (HashSet<String>) objectInputStream.readObject();
		    } catch (ClassNotFoundException e) {
		        e.printStackTrace();
		    } 
		    
		    if (objectInputStream != null) {
		    	objectInputStream.close();
		    }
		    

		}
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
			//get every input file name
			FileSplit fs = (FileSplit) context.getInputSplit();
	        String location = fs.getPath().getName(); //location means the filename of every input file
	        String eachline = value.toString().trim().toLowerCase();
	        
	        if (eachline != null && eachline.length() > 0) {
	        	//replace punctuation and split with blank
	            String[] linewords = eachline.replaceAll("\\p{P}", " ").trim()
	                    .split("\\s+");
	            if (stopWordSetMapper != null) {
	                for (String word : linewords) {
	                	//remove stop words
	                    if (!stopWordSetMapper.contains(word)) {
	                        context.write(new Text(word), new Text(location));
	                    }
	                }
	            }
	        }

		}
	}
	
	public static class Reduce extends Reducer<Text, Text, Text, Text>{
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException{
			String outputString = new String();
			HashSet<String> locationSet = new HashSet<String>();
			
			//change values to hashset
			for(Text value:values){
				locationSet.add(value.toString());
			}
			//set filenames
			for(String location:locationSet){
				outputString += location;
			}
			context.write(new Text(key+":"), new Text(outputString));
		}
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, URISyntaxException {
		// TODO Auto-generated method stub
	
		String inputfile = args[0];
		String outputfile = args[1];
		String stopWordsCache = args[2];//location of stop words file
		
		Configuration conf = new Configuration();
		
		//set stop word
		String outputPath = "stopwordset";
		HashSet<String>  stopWordSet = new HashSet<String>();
		if(stopWordsCache!=null){
			BufferedReader br = new BufferedReader(new FileReader(stopWordsCache));
			String singleWord;
			while ((singleWord=br.readLine())!=null){
				stopWordSet.add(singleWord.trim().toLowerCase());
			}
			br.close();
		}
		FileSystem fileSystem = FileSystem.get(conf);
	    FSDataOutputStream dataOutputStream = fileSystem.create(new Path(outputPath));
	    ObjectOutputStream objectOutputStream = new ObjectOutputStream(dataOutputStream);
	    objectOutputStream.writeObject(stopWordSet);
	    objectOutputStream.close();
	    fileSystem.close();
	    
	    //set distributedCache
		DistributedCache.addCacheFile(new URI(outputPath), conf);
		
		Job job = new Job(conf, "inverted index");
		job.setJarByClass(InvertedIndex.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		
		job.setInputFormatClass(TextInputFormat.class);
	    job.setOutputFormatClass(TextOutputFormat.class);

	    TextInputFormat.setInputPaths(job, inputfile);
        TextOutputFormat.setOutputPath(job, new Path(outputfile));

        job.waitForCompletion(true);

	}

}

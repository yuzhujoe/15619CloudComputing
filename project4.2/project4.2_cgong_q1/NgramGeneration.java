import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class NgramGeneration {

	public static class Map extends Mapper<LongWritable, Text, Text, NullWritable>{
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
			
			String eachline = value.toString().trim();
	        if (eachline != null && eachline.length() > 0) {
	        	//replace non alphabet and split with blank
	            String[] linewords = eachline.replaceAll("[^A-Za-z]", " ").toLowerCase().trim()
	                    .split("\\s+");
	            //generate NGram phases
	            for(int i=1; i<6;i++){
	            	Text everyPhrase = new Text();
	            	for(int j=0; j<linewords.length-i+1; j++){
	            		StringBuffer sb = new StringBuffer();
	            		if(linewords[j].trim().length()>0){
	            			for(int k=0; k<i; k++){
	            				sb.append(linewords[j+k]+" ");
	            			}
	            			everyPhrase.set(sb.toString().trim());
	            			context.write(everyPhrase, NullWritable.get());
	            		}
	            	}
	            }
	            
	        }

		}
	}
	
	public static class Reduce extends Reducer<Text, NullWritable, Text, IntWritable>{
		public void reduce(Text key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException{
			//count number
			int number = 0;
			for (NullWritable value:values) {
	            number++;
	        }
			IntWritable output = new IntWritable(number);
	        context.write(key, output);
		}
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, URISyntaxException {
		// TODO Auto-generated method stub
	
		String inputfile = args[0];
		String outputfile = args[1];
		
		Configuration conf = new Configuration();
		Job job = new Job(conf, "Ngram Generation");
		job.setJarByClass(NgramGeneration.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(NullWritable.class);
		
		job.setInputFormatClass(TextInputFormat.class);
	    job.setOutputFormatClass(TextOutputFormat.class);

	    TextInputFormat.setInputPaths(job, inputfile);
        TextOutputFormat.setOutputPath(job, new Path(outputfile));

        job.waitForCompletion(true);

	}

}

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class RunInstance {
	static Properties properties;
	static BasicAWSCredentials bawsc;
	static AmazonEC2Client ec2;
	static String imgIdLoadGenerator = "ami-1810b270";
	static String imgIdDateCenter = "ami-324ae85a";
	static String dns_loadGenerator;
	static String dns_datacenter;

	public static void main(String[] args) throws Exception {
		
		//Get the Account ID and Secret Key
		properties = new Properties();
	    properties.load(RunInstance.class.getResourceAsStream("/AwsCredentials.properties"));
		
	    bawsc = new BasicAWSCredentials(properties.getProperty("accessKey"), properties.getProperty("secretKey"));
	    
	    //Create an Amazon EC2 Client
	    ec2 = new AmazonEC2Client(bawsc);
	    
	    //get start time
	    long startTime=System.currentTimeMillis();
	    
	    //Launch an m3.medium load generator with tag
	    Instance instanceLoadGenerator = createInstance(imgIdLoadGenerator);
	    
	    //launch an m3.medium data center instance with tag
	    Instance instanceDataCenter = createInstance(imgIdDateCenter);
	    
	    //waiting for the first two instances to get started
	    Thread.sleep(60000);
	    
	    //get load generator instance dns and data center dns
	    dns_loadGenerator = getDNS(instanceLoadGenerator.getInstanceId());
	    dns_datacenter = getDNS(instanceDataCenter.getInstanceId());
	    
	    System.out.println("dns_loadGenerator: "+dns_loadGenerator);
	    System.out.println("dns_datacenter: "+dns_datacenter);
	    
	    //activate both instances
	    Thread.sleep(30000);
	    
	    activateInstance(dns_loadGenerator);
	    activateInstance(dns_datacenter);
	    
	    //submit data center instances 
	    submitInstance(dns_loadGenerator, dns_datacenter);
	    
	    //save every average rps of all connected instances into ArrayList
	    Thread.sleep(65000);
	    
	    //compute the cumulativeRPS
	    while(getCumulativeAverageRPS(dns_loadGenerator)<3600){
	    	Instance instance_new = createInstance(imgIdDateCenter);
	    	Thread.sleep(65000);//get the newest rps of every minute
	    	if (getCumulativeAverageRPS(dns_loadGenerator)>3600){
	    		break;
	    	}
	    	String dns_datacenter_new = getDNS(instance_new.getInstanceId());
	    	Thread.sleep(35000);//waiting for activate
	    	activateInstance(dns_loadGenerator);
		    activateInstance(dns_datacenter_new);
		    submitInstance(dns_loadGenerator, dns_datacenter_new);
		    //getEveryAverageRPS(dns_loadGenerator);
		    long endTime=System.currentTimeMillis();
		    if((endTime-startTime)>2400000){
		    	System.out.println("Time is over 40min");
		    	break;
		    	
		    }
		    Thread.sleep(20000);//in the loop, total of these three sleeps is 12000ms, which is the gap of two instances
	    }
	    
	    System.out.printf("The End");
	
	}
	
	public static Instance createInstance(String imageId){
		//Create Instance Request
	    RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
	    
	    //Configure Instance Request 
	    runInstancesRequest.withImageId(imageId)
	    .withInstanceType("m3.medium")
	    .withMinCount(1)
	    .withMaxCount(1)
	    .withKeyName("15619PJ1SA")
	    .withSecurityGroups("launch-wizard-1");
	    
	    //Launch Instance
	    RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
	    
	    //Get Instance ID of Instance:
	    Instance instance = runInstancesResult.getReservation().getInstances().get(0);
	    
	    //Add a Tag to the Instance
	    CreateTagsRequest createTagsRequest = new CreateTagsRequest();
	    createTagsRequest.withResources(instance.getInstanceId()).withTags(new Tag("Project","2.1"));
	    ec2.createTags(createTagsRequest);
	    
	    return instance;
	}
	
	//get load generator instance and data center instance dns
	public static String getDNS(String instanceid){
			
		 ArrayList<String> list = new ArrayList<String>() ;
		 list.add(instanceid);
		 DescribeInstancesRequest request = new DescribeInstancesRequest();
		 request.setInstanceIds(list);
		    
		 DescribeInstancesResult result = ec2.describeInstances(request);
		 String dns = result.getReservations().get(0).getInstances().get(0).getPublicDnsName();
		 return dns;
			
		}
	
	public static void activateInstance(String dns){
		String urlStr = "http://"+dns+"/username?username=cgong"; 
		try {
			URL url = new URL(urlStr);
			HttpURLConnection connect = (HttpURLConnection)url.openConnection();
			System.out.println(connect.getResponseMessage());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//submit the data center instances's DNS name
	public static void submitInstance(String loadGeneratorDNS, String dataCenterDNS){
		String urlStr = "http://"+loadGeneratorDNS+"/part/one/i/want/more?dns="+dataCenterDNS+"&testId=gcr"; 
		try {
			URL url = new URL(urlStr);
			HttpURLConnection connect = (HttpURLConnection)url.openConnection();
			System.out.println(connect.getResponseMessage());
		
			BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			String inputLine;
			while((inputLine = in.readLine()) != null)
				System.out.println(inputLine);
			in.close();
			//InputStream stream = connect.getInputStream();
		
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Double getCumulativeAverageRPS(String loadGeneratorDNS){
		String urlStr = "http://"+loadGeneratorDNS+"/view-logs?name=result_cgong_gcr.txt"; 
		double rpsMax = 0;
		try {
			
			URL url = new URL(urlStr);
			HttpURLConnection connect = (HttpURLConnection)url.openConnection();
			//System.out.println(connect.getResponseMessage());
			int status = connect.getResponseCode();
			System.out.println(status);
			boolean isError = status >= 400;
			InputStream is = isError ? connect.getErrorStream() : connect.getInputStream();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String inputLine;
			int minuteCount = 0;
			
			while((inputLine = in.readLine()) != null){
				System.out.println("line: "+inputLine);
				//get the average rps
				if (inputLine.contains("minute")){
					double rpsMinuteCount = 0;
					minuteCount++;
					System.out.println("minute: "+minuteCount);
					while((inputLine = in.readLine()).contains("amazonaws.com")){
						String[] strList = inputLine.split(":");
						String str = strList[2];
						System.out.println("str "+str);
						String[] str2List = str.split("\\[");	
						String str2 = str2List[0];
						String str_rps = str2.trim();
						System.out.println("str_rps "+str_rps);
					    Double rps = Double.parseDouble(str_rps);
					    rpsMinuteCount += rps;
						System.out.println("rps: "+rps);
						System.out.println("rps count "+rpsMinuteCount);
						if (rpsMax < rpsMinuteCount){
							rpsMax = rpsMinuteCount;
						}	
					}	
				}
			}
				
			in.close();
			System.out.println("rpsMax "+rpsMax);
			//return rpsMax;
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rpsMax; 
		
	}
	
	

	
}

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.DeleteAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.DeleteLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.DescribeLaunchConfigurationsRequest;
import com.amazonaws.services.autoscaling.model.DescribeLaunchConfigurationsResult;
import com.amazonaws.services.autoscaling.model.EnableMetricsCollectionRequest;
import com.amazonaws.services.autoscaling.model.InstanceMonitoring;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyResult;
import com.amazonaws.services.autoscaling.model.UpdateAutoScalingGroupRequest;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest;
import com.amazonaws.services.cloudwatch.model.DescribeAlarmsResult;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.EnableAlarmActionsRequest;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.AddTagsRequest;
import com.amazonaws.services.elasticloadbalancing.model.AddTagsResult;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckRequest;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckResult;
import com.amazonaws.services.elasticloadbalancing.model.ConnectionDraining;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.HealthCheck;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerAttributes;
import com.amazonaws.services.elasticloadbalancing.model.ModifyLoadBalancerAttributesRequest;
import com.amazonaws.services.elasticloadbalancing.model.ModifyLoadBalancerAttributesResult;
import com.amazonaws.services.elasticloadbalancing.model.Tag;


public class RunAutoscale_cgong {
	static Properties properties;
	static BasicAWSCredentials bawsc;
	static AmazonElasticLoadBalancingClient elb;
	static AmazonAutoScalingClient aasc;
	static AmazonEC2Client ec2;
	static AmazonCloudWatchClient acwc;
	static String elbDNSName;
	static String dns_loadGenerator;

	public static void main(String[] args) throws IOException, InterruptedException{
		//Get the Account ID and Secret Key
		properties = new Properties();
		properties.load(RunAutoscale_cgong.class.getResourceAsStream("/AwsCredentials.properties"));
				
		bawsc = new BasicAWSCredentials(properties.getProperty("accessKey"), properties.getProperty("secretKey"));
	    
		//Create an Amazon Elastic Load Balancing CLient
		elb = new AmazonElasticLoadBalancingClient(bawsc);
		createLoadBalancer();
		System.out.println("DNS_ELB "+elbDNSName);
		
		//create an auto scaling group
		aasc = new AmazonAutoScalingClient(bawsc);
		
		//Create an cloud watch client
		acwc = new AmazonCloudWatchClient(bawsc);
		createLauchConfigurationAndAutoScalingGroup();
		
		//Create an Amazon EC2 Client
	    ec2 = new AmazonEC2Client(bawsc);
	    
	    //Launch an m3.medium load generator with tag
	    Instance instanceLoadGenerator = createLoadGenerator();
	    
	    //Activate Load generator
	    Thread.sleep(65000);
	    //get load generator instance dns 
	    dns_loadGenerator = getDNS(instanceLoadGenerator.getInstanceId());
	    System.out.println("dns_loadGenerator: "+dns_loadGenerator);
	    activateLoadGenerator(dns_loadGenerator);
	    
	    
	    
	    //warm up
	    //wait for some time to warm up
	    Thread.sleep(240000);
	    //warm up five times
	    for(int i=0; i<5; i++){
	    	warmUp(dns_loadGenerator, elbDNSName);
	    }
	    
	    
	    //start test
	    startTest(dns_loadGenerator, elbDNSName);
	    
	    //run
	    Thread.sleep(2430000);
	    
	    //terminate
	    terminate();
	    
		
		
	}
	
	public static CreateLoadBalancerResult createLoadBalancer() throws InterruptedException{
		//create load balancer
		CreateLoadBalancerRequest lbRequest = new CreateLoadBalancerRequest();
		lbRequest.setLoadBalancerName("loader");
		List<Listener> listeners = new ArrayList<Listener>(1);
		listeners.add(new Listener("HTTP", 80, 80));
		lbRequest.withAvailabilityZones("us-east-1a");
		lbRequest.setListeners(listeners);
		lbRequest.withSecurityGroups("sg-1ebce67b");
		CreateLoadBalancerResult lbResult = elb.createLoadBalancer(lbRequest);
		
		//Get Load Balancer DNS Name
		Thread.sleep(1000);
		elbDNSName = lbResult.getDNSName();
		
		//Modify the Connection Draining attribute of the load balancer by setting the attribute value to false
		ModifyLoadBalancerAttributesRequest barequest = new ModifyLoadBalancerAttributesRequest();
		LoadBalancerAttributes lba = new LoadBalancerAttributes();
		ConnectionDraining cd = new ConnectionDraining();
		cd.setEnabled(false);
		lba.setConnectionDraining(cd);
		barequest.setLoadBalancerName("loader");
		barequest.setLoadBalancerAttributes(lba);
		ModifyLoadBalancerAttributesResult mlbar = elb.modifyLoadBalancerAttributes(barequest);
		
		System.out.println("Disable Connection Draining "+mlbar.getLoadBalancerAttributes().getConnectionDraining().getEnabled());
		
		//add tag to load balancer
		AddTagsRequest addTagsRequest = new AddTagsRequest();
		Collection<Tag> tag = new ArrayList<Tag>();
		Tag tag_pj2 = new Tag();
		tag_pj2.setKey("Project");
		tag_pj2.setValue("2.2");
		tag.add(tag_pj2);
		addTagsRequest.setTags(tag);
		Collection<String> loader = new ArrayList<String>();
		loader.add("loader");
		addTagsRequest.setLoadBalancerNames(loader);
		AddTagsResult addResult = elb.addTags(addTagsRequest);
		System.out.println("Add Tag "+ addResult.toString());
		
		//set up the health check page
		HealthCheck healthcheck = new HealthCheck();
		healthcheck.setTarget("HTTP:80/heartbeat?username=cgong");
		healthcheck.setTimeout(5);
		healthcheck.setInterval(30);
		healthcheck.setUnhealthyThreshold(5);
		healthcheck.setHealthyThreshold(5);
		
		ConfigureHealthCheckRequest chcr = new ConfigureHealthCheckRequest();
		chcr.setHealthCheck(healthcheck);
		chcr.setLoadBalancerName("loader");
		ConfigureHealthCheckResult chcresult = elb.configureHealthCheck(chcr);
		System.out.println("Get health check page"+chcresult.getHealthCheck().getTarget());
		
		System.out.println("Created load balancer loader");
		
		return lbResult;
	}
	
	public static void createLauchConfigurationAndAutoScalingGroup(){
		//Create a Launch Configuration for the instances
		CreateLaunchConfigurationRequest clcRequest = new CreateLaunchConfigurationRequest();
		clcRequest.setLaunchConfigurationName("launchConfiguration");
		clcRequest.setImageId("ami-ec14ba84");
		clcRequest.setInstanceType("m3.medium");
		clcRequest.setKeyName("15619PJ1SA");
		Collection<String> securityGroup = new ArrayList<String>();
		securityGroup.add("launch-wizard-1");
		clcRequest.setSecurityGroups(securityGroup);
		InstanceMonitoring instanceMonitoring = new InstanceMonitoring();
		instanceMonitoring.setEnabled(true);
		clcRequest.setInstanceMonitoring(instanceMonitoring);
		
		//create auto scaling group
		CreateAutoScalingGroupRequest casgrequest = new CreateAutoScalingGroupRequest();
		
		//set parameters
		casgrequest.setAutoScalingGroupName("autoScalingGroup");
		casgrequest.setMinSize(3);
		casgrequest.setMaxSize(4);
		casgrequest.setDesiredCapacity(3);
		casgrequest.withAvailabilityZones("us-east-1a");
		//casgrequest.setDefaultCooldown(120);
		//add tag
		Collection<com.amazonaws.services.autoscaling.model.Tag> tag = new ArrayList<com.amazonaws.services.autoscaling.model.Tag>();
		com.amazonaws.services.autoscaling.model.Tag tag_pj2 = new com.amazonaws.services.autoscaling.model.Tag();
		tag_pj2.setKey("Project");
		tag_pj2.setValue("2.2");
		tag.add(tag_pj2);
		casgrequest.setTags(tag);
		Collection<String> loader = new ArrayList<String>();
		loader.add("loader");
		casgrequest.setLoadBalancerNames(loader);
		casgrequest.setHealthCheckType("ELB");
		casgrequest.setHealthCheckGracePeriod(300);
		
		casgrequest.setLaunchConfigurationName(clcRequest.getLaunchConfigurationName());
		
		EnableMetricsCollectionRequest enableMetricsCollectionRequest = new EnableMetricsCollectionRequest();
		enableMetricsCollectionRequest.setAutoScalingGroupName("autoScalingGroup");
		enableMetricsCollectionRequest.setGranularity("1Minute");
		
		aasc.createLaunchConfiguration(clcRequest);
		aasc.createAutoScalingGroup(casgrequest);
		aasc.enableMetricsCollection(enableMetricsCollectionRequest);
		
		//verify Launch Configuration
		DescribeLaunchConfigurationsRequest dlcRequest = new DescribeLaunchConfigurationsRequest();
		Collection<String> launchConfigurationNames = new ArrayList<String>();
		launchConfigurationNames.add("launchConfiguration");
		dlcRequest.setLaunchConfigurationNames(launchConfigurationNames);
		DescribeLaunchConfigurationsResult dlcResult = aasc.describeLaunchConfigurations(dlcRequest);
		System.out.println("Launch Configuratons "+dlcResult.toString());
		
		//verify Auto Scaling Group
		DescribeAutoScalingGroupsRequest saxgRequest = new DescribeAutoScalingGroupsRequest();
		Collection<String> autoScalingGroupNames = new ArrayList<String>();
		autoScalingGroupNames.add("autoScalingGroup");
		saxgRequest.setAutoScalingGroupNames(autoScalingGroupNames);
		DescribeAutoScalingGroupsResult sasgResult = aasc.describeAutoScalingGroups(saxgRequest);
		System.out.println("Auto Scaling Groups "+sasgResult.toString());
		
		//Create Scaling Out policy
		PutScalingPolicyRequest pspRequestOut = new PutScalingPolicyRequest();
		pspRequestOut.setPolicyName("myScaleOutPolicy");
		pspRequestOut.setAutoScalingGroupName("autoScalingGroup");
		pspRequestOut.setScalingAdjustment(1);
		pspRequestOut.setAdjustmentType("ChangeInCapacity");
		pspRequestOut.setCooldown(120);
		PutScalingPolicyResult pspResultOut = aasc.putScalingPolicy(pspRequestOut);
		String scalingOutPolicyARN = pspResultOut.getPolicyARN();
		System.out.println("Scaling Out Policy "+pspResultOut.toString());
		
		//Create Scaling In policy
		PutScalingPolicyRequest pspRequestIn = new PutScalingPolicyRequest();
		pspRequestIn.setPolicyName("myScaleInPolicy");
		pspRequestIn.setAutoScalingGroupName("autoScalingGroup");
		pspRequestIn.setScalingAdjustment(-1);
		pspRequestIn.setAdjustmentType("ChangeInCapacity");
		pspRequestIn.setCooldown(120);
		PutScalingPolicyResult pspResultIn = aasc.putScalingPolicy(pspRequestIn);
		String scalingInPolicyARN = pspResultIn.getPolicyARN();
		System.out.println("Scaling In Policy "+pspResultIn.toString());
				
		//Create CloudWatch Alarm Scale Out
		PutMetricAlarmRequest pmaRequest = new PutMetricAlarmRequest();
		pmaRequest.setAlarmName("AddCapacity");
		pmaRequest.setMetricName("CPUUtilization");
		pmaRequest.setNamespace("AWS/EC2");
		pmaRequest.setStatistic("Average");
		pmaRequest.setPeriod(120);
		pmaRequest.setThreshold(90.00);
		pmaRequest.setComparisonOperator("GreaterThanThreshold");
		Collection<Dimension> dimCollection = new ArrayList<Dimension>();
		Dimension dim = new Dimension();
		dim.setName("AutoScalingGroupName");
		dim.setValue("autoScalingGroup");
		dimCollection.add(dim);
		pmaRequest.setDimensions(dimCollection);
		pmaRequest.setEvaluationPeriods(1);
		Collection<String> scalingOutARN = new ArrayList<String>();
		scalingOutARN.add(scalingOutPolicyARN);
		pmaRequest.setAlarmActions(scalingOutARN);
		
		System.out.println("Cloud watch add capacity "+pmaRequest.toString());
		System.out.println("Dimension "+pmaRequest.getDimensions().get(0).getName());
		
		acwc.putMetricAlarm(pmaRequest);
		
		
		
		DescribeAlarmsRequest daRequest = new DescribeAlarmsRequest();
		Collection<String> alarmNamesOut = new ArrayList<String>();
		alarmNamesOut.add("AddCapacity");
		daRequest.setAlarmNames(alarmNamesOut);
		DescribeAlarmsResult daResult = acwc.describeAlarms(daRequest);
		System.out.println("Create CloudWatch Alarm Add Capacity "+daResult.toString());
		
		//Create CloudWatch Alarm Scale In
		PutMetricAlarmRequest pmaRequestIn = new PutMetricAlarmRequest();
		pmaRequestIn.setAlarmName("DecreaseCapacity");
		pmaRequestIn.setMetricName("CPUUtilization");
		pmaRequestIn.setNamespace("AWS/EC2");
		pmaRequestIn.setStatistic("Average");
		pmaRequestIn.setPeriod(120);
		pmaRequestIn.setThreshold(60.00);
		pmaRequestIn.setComparisonOperator("LessThanThreshold");
		Collection<Dimension> dimCollectionIn = new ArrayList<Dimension>();
		Dimension dimIn = new Dimension();
		dimIn.setName("AutoScalingGroupName");
		dimIn.setValue("autoScalingGroup");
		dimCollectionIn.add(dimIn);
		pmaRequestIn.setDimensions(dimCollectionIn);
		pmaRequestIn.setEvaluationPeriods(1);
		Collection<String> scalingInARN = new ArrayList<String>();
		scalingInARN.add(scalingInPolicyARN);
		pmaRequestIn.setAlarmActions(scalingInARN);
				
		acwc.putMetricAlarm(pmaRequestIn);
				
		DescribeAlarmsRequest daRequestIn = new DescribeAlarmsRequest();
		Collection<String> alarmNamesIn = new ArrayList<String>();
		alarmNamesIn.add("DecreaseCapacity");
		daRequest.setAlarmNames(alarmNamesIn);
		DescribeAlarmsResult daResultIn = acwc.describeAlarms(daRequestIn);
		System.out.println("Create CloudWatch Alarm decrease Capacity "+daResultIn.toString());
		
	}
	
	//create load generator
	public static Instance createLoadGenerator(){
		//Create Instance Request
	    RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
	    
	    //Configure Instance Request 
	    runInstancesRequest.withImageId("ami-562d853e")
	    .withInstanceType("m3.medium")
	    .withMinCount(1)
	    .withMaxCount(1)
	    .withKeyName("15619PJ1SA")
	    .withMonitoring(true)
	    .withSecurityGroups("launch-wizard-1");
	    
	    //Launch Instance
	    RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
	    
	    //Get Instance ID of Instance:
	    Instance instance = runInstancesResult.getReservation().getInstances().get(0);
	    
	    //Add a Tag to the Instance
	    CreateTagsRequest createTagsRequest = new CreateTagsRequest();
	    com.amazonaws.services.ec2.model.Tag tag_LoadGenerator = new com.amazonaws.services.ec2.model.Tag();
	    tag_LoadGenerator.setKey("Project");
	    tag_LoadGenerator.setValue("2.2");
	    createTagsRequest.withResources(instance.getInstanceId()).withTags(tag_LoadGenerator);
	    ec2.createTags(createTagsRequest);
	    
	    return instance;
	}
	
	//get load generator instance DNS
	public static String getDNS(String instanceid){
				
		ArrayList<String> list = new ArrayList<String>() ;
		list.add(instanceid);
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		request.setInstanceIds(list);
			    
		DescribeInstancesResult result = ec2.describeInstances(request);
		String dns = result.getReservations().get(0).getInstances().get(0).getPublicDnsName();
		return dns;
				
	}
	
	//activate load generator
	public static void activateLoadGenerator(String loadGeneratorDNS){
		String urlStr = "http://"+loadGeneratorDNS+"/username?username=cgong"; 
		try {
			URL url = new URL(urlStr);
			HttpURLConnection connect = (HttpURLConnection)url.openConnection();
			System.out.println("Activate Load Generator "+connect.getResponseMessage());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//warm up
	public static void warmUp(String loadGeneratorDNS, String elbDNS) throws InterruptedException{
		
		for(int i=0;; i++){
			String urlStr = "http://"+loadGeneratorDNS+"/warmup?dns="+elbDNS+"&testId=gcr"; 
			
			try {
				URL url = new URL(urlStr);
				HttpURLConnection connect = (HttpURLConnection)url.openConnection();
				System.out.println("Warm up "+connect.getResponseMessage());
				
				BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
				String inputLine;
				String outputLine = "";
				Thread.sleep(30000);
				while((inputLine = in.readLine()) != null){
					System.out.println(inputLine);
					outputLine += inputLine;
				}	
				in.close();
				//in case warm up is not waiting enough time and the browser shows "Invalid DNS..."
				if(!outputLine.contains("Invalid")){
					Thread.sleep(351000);
					System.out.println(outputLine);
					break;
					
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
		
	//start test
	public static void startTest(String loadGeneratorDNS, String elbDNS){
		String urlStr = "http://"+loadGeneratorDNS+"/begin-phase-2?dns="+elbDNS+"&testId=gcrstarttest"; 
		try {
			URL url = new URL(urlStr);
			HttpURLConnection connect = (HttpURLConnection)url.openConnection();
			System.out.println("Start test "+connect.getResponseMessage());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Terminate the data center instances
	public static void terminate() throws InterruptedException{
		UpdateAutoScalingGroupRequest uasgRequest = new UpdateAutoScalingGroupRequest();
	
		//Delete the instances in auto scaling group to zero
		uasgRequest.withAutoScalingGroupName("autoScalingGroup")
		.withMaxSize(0)
		.withMinSize(0)
		.withDesiredCapacity(0);
		
		aasc.updateAutoScalingGroup(uasgRequest);
		System.out.println("Terminate Instances");
		Thread.sleep(180000);
		//delete auto scaling group
		DeleteAutoScalingGroupRequest dasgRequest = new DeleteAutoScalingGroupRequest();
		dasgRequest.withAutoScalingGroupName("autoScalingGroup");
		aasc.deleteAutoScalingGroup(dasgRequest);
		System.out.println("Terminate Auto Scaling Group");
		
		//delete launch configuration
		DeleteLaunchConfigurationRequest dlcRequest = new DeleteLaunchConfigurationRequest();
		dlcRequest.setLaunchConfigurationName("launchConfiguration");
		aasc.deleteLaunchConfiguration(dlcRequest);

		System.out.println("Terminate Launch Configuration");
	}
	
	
}

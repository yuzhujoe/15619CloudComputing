README

In the RunME.java file, I have implemented all the functionality required.
First, create a load balancer, modify the connection draining attribute, add tag, and set up the health check page. Then I create a launch configuration, an auto scaling group, add tag, verify launch configuration, verify auto scaling group, and create scaling out policy, scaling in policy, and create Cloud Watch Alarm for scaling out policy and scaling in policy, as well as linking ARNs to the auto scaling group for email notification. 
Third, I create a load generator and then get the DNS of it.
Forth, the load generator is activated and warm up is implemented for three times.
Finally, start the test and terminate all the resources, including editing the auto scaling group to zero instances, deleting SNS, deleting auto scaling group, deleting launch configuration and delete the load generator as well as the ELB.

In the email.txt file, it includes the emails received from SNS.

For the test, the traffic pattern token is 9cb9ed4f35cf. The total ‘earning’ is $6.48. The instance-hours is 71. The mean rps is 2248.94. And the ‘profit’ is 6.48-71*0.07 = $1.51 (I used the m3.medium instances, and the price is $0.07).
At last, the grade validation token is 92cfaa020512.

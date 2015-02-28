#!/usr/bin/python
import boto
import boto.ec2
import boto.ec2.cloudwatch
import os
import time

execute = os.popen("/usr/bin/wget -q -O - http://169.254.169.254/latest/meta-data/instance-id").read()
instanceId = execute

cloudWatch = boto.ec2.cloudwatch.connect_to_region('us-east-1', aws_access_key_id='AKIAJ3N4356JPDMF3MPQ', aws_secret_access_key='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')

last_query_execute = os.popen("/usr/bin/mysql --execute=\"show status like \'Queries\'\"").read()
last_query_ind = last_query_execute.find("Queries")
last_query = last_query_execute[last_query_ind+7:]
last_query_num = (int)(last_query)

last_uptime_execute = os.popen("/usr/bin/mysql --execute=\"show status like \'Uptime\'\"").read()
last_uptime_ind = last_uptime_execute.find("Uptime")
last_uptime = last_uptime_execute[last_uptime_ind+6:]
last_uptime_num = (int)(last_uptime)

time.sleep(60)

while(True):
    query_execute = os.popen("/usr/bin/mysql --execute=\"show status like \'Queries\'\"").read()
    query_ind = query_execute.find("Queries")
    query = query_execute[query_ind+7:]
    query_num = (int)(query)
   
    uptime_execute = os.popen("/usr/bin/mysql --execute=\"show status like \'Uptime\'\"").read()
    uptime_ind = uptime_execute.find("Uptime")
    uptime = uptime_execute[uptime_ind+6:]
    uptime_num = (int)(uptime)

    diff_query = query_num - last_query_num - 6
    diff_uptime = uptime_num - last_uptime_num
    result = diff_query /diff_uptime/16/143.33*100
    print result
    cloudWatch.put_metric_data("myTPS","TPS",result,'','Percent',dict(InstanceID=instanceId),'')
    last_query_num = query_num
    last_uptime_num = uptime_num
    time.sleep(60)








#!/usr/bin/python

#from operator import itemgetter
import sys

fileResult = open ( 'result', 'w')
current_pagetitle = None
current_access = 0
current_filename = None
current_pagetitle_month = None
current_access_month = 0
pagetitle = None
filename = None
pagetitle_month = None

# input comes from standard input
for line in sys.stdin:

    line = line.strip()
    pagetitle, access, filename = line.split('\t')

    try:
        access = int(access)
    except ValueError:
        continue

    #check the current_pagetitle processed is the same as the previous one in the same date
    if current_pagetitle == pagetitle and current_filename == filename:
        current_access += access

    #when have a new word, output the previous pagetitle and count
    else:
        if current_pagetitle:
            #write result to STDOUT
            print '%s\t%s\t%s' % (current_pagetitle, current_access, current_filename)
            #fileResult.write(current_pagetitle+ '\t' + str(current_access) + '\t' + current_filename+'\n')
        current_access = access
        current_pagetitle = pagetitle
        current_filename = filename

#output the last pagetitle if needed
if current_pagetitle == pagetitle and current_filename == filename:
    print '%s\t%s\t%s' % (current_pagetitle, current_access, filename)
    #fileResult.write('%s\t%s\t%s\n' % (current_pagetitle, current_access, current_filename))


#read daily result file and calculate the month data
#fileHandle = open ( 'result', 'r')
#eachline = fileHandle.readline()
for line in sys.stdin:
    line = line.strip('\n')
    pagetitle_month, access_month, filename = line.split('\t')

    try:
        access_month = int(access_month)
    except ValueError:
        continue

    #check the current_pagetitle processed is the same as the previous one in the same date
    if current_pagetitle_month == pagetitle_month:
        current_access_month += access_month

    #when have a new word, output the previous pagetitle and count
    else:
        if current_pagetitle_month and current_access_month > 100000:
            #write result to STDOUT
            print '%s\t%s\t%s' % (current_pagetitle_month, current_access_month)
        current_access_month = access_month
        current_pagetitle_month = pagetitle_month

#output the last pagetitle if needed
if current_pagetitle_month == pagetitle_month and current_access_month > 100000:
    print '%s\t%s\t%s' % (current_pagetitle_month, current_access_month)

    


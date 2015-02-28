#!/bin/bash
#merge two files into one
awk ' BEGIN {FS = ","} NR==FNR {a[$1]=$2 FS $3 FS $4 FS $5 FS $6 FS $7 FS $8 FS $9 FS $10 FS $11; next} {print $0", "a[$1]}' million_songs_metadata.csv  million_songs_sales_data.csv | LC_ALL='C' sort -t ',' -k 1 > million_songs_metadata_and_sales.csv

#find the artists with maximum sales
awk -F"," '{a[$7]+=$3;}END{for(i in a) print i, a[i]}' million_songs_metadata_and_sales.csv | LC_ALL='C' sort -k 2 -n -r > tempfile

line=$(head -1 tempfile)
echo $line

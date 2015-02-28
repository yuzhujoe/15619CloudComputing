#!/bin/bash

echo enter artist_name
read artist_name
echo enter start_year
read start_year
echo enter end_year
read end_year

#check input data
test  -z "$artist_name" && echo "artist name is null" && exit 0
test  -z "$start_year" && echo "start year is null" && exit 0
test  -z "$end_year" && echo "end year is null" && exit 0

IFS=","
set -f
shopt -s nocasematch

while read line
do 
    #change the line into an array
    stringLine=$line 
    array=($stringLine)

    # check artist_name, start_year and end_year
    if [[ "${array[6]}" =~ ${artist_name} ]] && [ "${array[10]}" -ge $start_year ] && [ "${array[10]}" -le $end_year ]      
    then
        echo ${array[6]} ${array[10]} >> newFile
    fi
               
done < "million_songs_metadata.csv"
set +f
shopt -u nocasematch



#encoding=utf-8 
import re
import sys
import os

#filter files of one day
folder = "/Users/chenrangong/Documents/private//Cloud Computing/project1.1_cgong_q1"
dailyfilelist = glob.glob(os.path.join(folder, '*-20140701-*'))
for file in dailyfilelist:
	


 
#open file
fileHandle = open ( 'pagecounts-20140701-000000', 'r')
fileResult = open ( 'result', 'w')
result = {}

#exclution of some symbols
exTitleStart = ["Media:", "Special:", "Talk:", "User:", "User_talk:", "Project:",
        "Project_talk:", "File:", "File_talk:", "MediaWiki:", "MediaWiki_talk:",
        "Template:", "Template_talk:", "Help:", "Help_talk:", "Category:",
        "Category_talk:", "Portal:", "Wikipedia:", "Wikipedia_talk:"]
exBoilerplate = ["404_error/", "Main_Page", "Hypertext_Transfer_Protocol", "Favicon.ico", "Search"]
exImg = [".jpg", ".gif", ".png", ".JPG", ".GIF", ".PNG", ".txt", ".ico"]

#define a function to filter by line
def checkLine(line):
    strlist = line.split(' ')
    if len(strlist) == 4:
        projectName = strlist[0]
        pageTitle = strlist[1]
        numberOfAccess = strlist[2]
        if len(projectName) == 2: #check no suffix                
            if (projectName == 'en' ): #check start with en
                for titleStart in exTitleStart: #check exclusion of started title
                    if pageTitle.startswith(titleStart):
                        return 0
                if (re.match(r'[a-z]', pageTitle[0])): #check page title between a and z
                    return 0
                for img in exImg: # check exclusion of image
                    if pageTitle.endswith(img):
                        return 0
                for boiler in exBoilerplate: #check exclution of boilerplate articles
                    if pageTitle == boiler:
                        return 0
                # insert result into a dictionary
		result[pageTitle] = int(numberOfAccess)
                return 1
            else:
                return 0
        else:
            return 0
    else:
        return 0
# end of checkLine()

### MAIN FUNCTION

#define a variable to count the number of lines
count = 0
line = fileHandle.readline()
while line:
    line = fileHandle.readline()
    count = count + checkLine(line) 
    
print count

#sort the dictionary
resultSorted = sorted(result.iteritems(), key=lambda d:d[1], reverse=True)

#write the result into a file
for key, value in resultSorted:
    fileResult.write(key+ '\t' + str(value)+'\n')
    
#file close
fileResult.close()
fileHandle.close()




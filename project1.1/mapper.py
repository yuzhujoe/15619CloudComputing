#!/usr/bin/python2
#encoding=utf-8
import sys
import re
import os

#exclution of some symbols
exTitleStart = ["Media:", "Special:", "Talk:", "User:", "User_talk:", "Project:",
        "Project_talk:", "File:", "File_talk:", "MediaWiki:", "MediaWiki_talk:",
        "Template:", "Template_talk:", "Help:", "Help_talk:", "Category:",
        "Category_talk:", "Portal:", "Wikipedia:", "Wikipedia_talk:"]
exBoilerplate = ["404_error/", "Main_Page", "Hypertext_Transfer_Protocol", "Favicon.ico", "Search"]
exImg = [".jpg", ".gif", ".png", ".JPG", ".GIF", ".PNG", ".txt", ".ico"]

#define a function to filter by line
def checkLine(line, filename):
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
                
                print pageTitle+'\t'+numberOfAccess+'\t'+filename
                return 1
            else:
                return 0
        else:
            return 0
    else:
        return 0
# end of checkLine()


def main(argv):
    #filename = os.environ["map_input_file"]
    filename = "pagecounts-20140701-000000"
    filename = filename[15:19]
    line = sys.stdin.readline()
    try:
        while line:
            checkLine(line, filename)
            line = sys.stdin.readline()
    except "end of file":
        return None
if __name__ == "__main__":
    main(sys.argv)


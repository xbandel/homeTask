1) Download the Chrome driver https://sites.google.com/a/chromium.org/chromedriver/downloads
2) Use Google Chrome version 53
3) Download maven - https://maven.apache.org/download.cgi

After that you will be able to run the tests from command line using the command like:
mvn clean test -Dwebdriver.chrome.driver=/home/xeniya/tmp/chromedriver


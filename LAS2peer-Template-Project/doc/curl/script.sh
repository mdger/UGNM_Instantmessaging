#!/bin/sh
URL='http://localhost:8080/im'
echo "curl test script for las2peer service"

#echo "Test Get Message Method"
#curl -v -X GET $URL/message/single/bobby --user alice:pwalice

#echo "Test POST Message Method"
#curl -v -X POST $URL/message/single/bobby --user alice:pwalice -H "Content-Type:application/json" -d "{\"message\":\"Funktioniert es?\",\"timestamp\":\"2014-11-23 13:00:00\"}"

#echo "Test DELETE Message Method"
#curl -v -X DELETE $URL/message/single -H "Content-Type:application/json" -d "{\"messageID\":25}"

echo "Test GET Unread Message Method"
curl -v -X GET $URL/message/single/unread --user alice:pwalice

echo 
echo "PRESS RETURN TO CONTINUE..."
read
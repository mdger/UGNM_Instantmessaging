#!/bin/sh
URL='http://localhost:8080/im'
echo "curl test script for las2peer service"

echo "Test Get Message Method"
curl -v -X GET $URL/message/single/bobby --user alice:pwalice



echo 
echo "PRESS RETURN TO CONTINUE..."
read
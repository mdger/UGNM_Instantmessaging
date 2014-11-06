#!/bin/sh
URL='http://localhost:8080/example'
echo "curl test script for las2peer service"

echo "test authentication with test user alice"
#curl -v -X GET $URL/validate --user alice:pwalice
curl -v -X GET $URL/message/group/TestGroup
#curl -v -X localhost:8080/example
echo 
echo "PRESS RETURN TO CONTINUE..."
read

echo "more curl commandlines..."



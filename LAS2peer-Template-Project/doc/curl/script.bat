REM	curl test script for las2peer service

REM Test Get Message Method
curl -v -X GET http://localhost:8080/im/message/single/bobby --user alice:pwalice


PAUSE
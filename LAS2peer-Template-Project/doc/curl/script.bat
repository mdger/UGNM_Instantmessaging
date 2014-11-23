REM curl test script for las2peer service

::REM Test GET Message Method
::curl -v -X GET http://localhost:8080/im/message/single/bobby --user alice:pwalice

::REM Test POST Message Method
::curl -v -X POST http://localhost:8080/im/message/single/bobby --user alice:pwalice -H "Content-Type:application/json" -d "{\"message\":\"Funktioniert es?\",\"timestamp\":\"2014-11-23 13:00:00\"}"

REM Test DELETE Message Method
curl -v -X DELETE http://localhost:8080/im/message/single -H "Content-Type:application/json" -d "{\"messageID\":26}"

::REM Test GET Unread Message Method
::curl -v -X GET http://localhost:8080/im/message/single/unread --user alice:pwalice

::REM Test GET Unread Message Method
::curl -v -X PUT http://localhost:8080/im/message/single/unread -H "Content-Type:application/json" -d "{\"messageID\":26}"

PAUSE
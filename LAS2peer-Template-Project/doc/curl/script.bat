REM curl test script for las2peer service

::REM Test GET Message Method
::curl -v -X GET http://localhost:8080/im/message/single/bobby --user alice:pwalice

REM Test POST Message Method
curl -v -X POST http://localhost:8080/im/message/single/bobby --user alice:pwalice -H "Content-Type:application/json" -d "{\"message\":\"Funktioniert es?\",\"timestamp\":\"2014-11-23 13:00:00\"}"

PAUSE
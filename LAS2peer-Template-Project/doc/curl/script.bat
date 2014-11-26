REM curl test script for las2peer service

Set URL=http://localhost:8080/im

::REM Test GET Message Method
::curl -v -X GET %URL%/message/single/bobby --user alice:pwalice

::REM Test POST Message Method
::curl -v -X POST %URL%/message/single/bobby --user alice:pwalice -H "Content-Type:application/json" -d "{\"message\":\"Funktioniert es?\",\"timestamp\":\"2014-11-23 13:00:00\"}"

::REM Test DELETE Message Method
::curl -v -X DELETE %URL%/message/single -H "Content-Type:application/json" -d "{\"messageID\":26}"

::REM Test GET Unread Message Method
::curl -v -X GET %URL%/message/single/unread --user alice:pwalice

::REM Test PUT Unread Message Method
::curl -v -X PUT %URL%/message/single/unread -H "Content-Type:application/json" -d "{\"messageID\":26}"

::REM Test GET Request Method
::curl -v -X GET %URL%/profile/contact/request --user joey:pwjoey

::REM Test POST Request Method
::curl -v -X POST %URL%/profile/contact/request --user alice:pwalice -H "Content-Type:application/json" -d "{\"username\":\"joey\"}"

::REM Test DELETE Request Method
::curl -v -X DELETE %URL%/profile/contact/request --user joey:pwjoey -H "Content-Type:application/json" -d "{\"username\":\"alice\"}"

::REM Test GET Contact Method
::curl -v -X GET %URL%/profile/contact --user alice:pwalice

::REM Test POST Contact Method
::curl -v -X POST %URL%/profile/contact --user joey:pwjoey -H "Content-Type:application/json" -d "{\"username\":\"alice\"}"

::REM Test DELETE Contact Method
::curl -v -X DELETE %URL%/profile/contact --user joey:pwjoey -H "Content-Type:application/json" -d "{\"username\":\"alice\"}"

::REM Test GET Profile Method
::curl -v -X GET %URL%/profile/bobby --user alice:pwalice

::REM Test POST Profile Method
::curl -v -X POST %URL%/profile/ --user alice:pwalice -H "Content-Type:application/json" -d "{\"email\":\"TestUser@somewhere.de\",\"telephone\":12345678,\"imageLink\":\"www.somewhere.com/image1.jpg\",\"nickname\":\"Nick1\",\"visible\":1}"

::REM Test PUT Profile Method
::curl -v -X PUT %URL%/profile/ --user alice:pwalice -H "Content-Type:application/json" -d "{\"email\":\"Alice@somewhere.com\",\"telephone\":87654321,\"imageLink\":\"www.somewhere.com/Alice.jpg\",\"nickname\":\"Alli\",\"visible\":1}"

::REM Test GET Group Method
::curl -v -X GET %URL%/group/TestGroup

::REM Test POST Group Method
::curl -v -X POST %URL%/group/NewGroup --user alice:pwalice -H "Content-Type:application/json" -d "{\"description\":\"This is a description!\",\"imagelink\":\"www.image/link/image.jpg\"}"

::REM Test PUT Group Method
::curl -v -X PUT %URL%/group/NewGroup --user alice:pwalice -H "Content-Type:application/json" -d "{\"description\":\"This is a new description!\",\"imagelink\":\"www.image/link/image1.jpg\"}"

REM Test DELETE Group Method
curl -v -X DELETE %URL%/group/NewGroup --user alice:pwalice

PAUSE
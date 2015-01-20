![LAS2peer](https://github.com/rwth-acis/LAS2peer/blob/master/img/logo/bitmap/las2peer-logo-128x128.png)

Introduction
-----------------------

This is the repository for the Instantmessaging WebApp of AsiaTee, Ausstaedter, rNzqe and yingli1. The service is built 

upon Las2Peer as a middleware and HTML5 (CSS/JS) as Frontend.

Instantmessenger
-----------------------

Users will have to create an account by inputting an email, a username and a password. After creating an account,

the user will be able to customize his profile and participate in different communication channels (groupchats) and add

other users on the platform to communicate with them exclusively. 

If you are interested in Las2Peer, please visit the official Las2Peer Rep (https://raw.githubusercontent.com/rwth-acis/LAS2peer)


##Running Service

###Requirements

* [Java Development Kit 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Apache Ant](http://ant.apache.org/)

###Building

Running *ant all* from command prompt will build the project.

###Starting Service Server

Running  ./bin/start_network.bat (Windows) or ./bin/start_network.sh (Linux) will start a local Server on Port 8080 

After that you can send Requests to Service Server *http://localhost:8080/im/**

You can find a description of every Resource at /doc/api.md or [on Github](https://github.com/AsiaTee/UGNM_Instantmessaging/blob/master/LAS2peer-Template-Project/doc/api.md)

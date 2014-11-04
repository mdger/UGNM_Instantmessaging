Your Service API Documentation
==

This service offers...

In the following we describe each resource including its supported operations in detail.

AuthResource
--
__URL Template:__ /resource/{id}

__Operations:__

* __Retrieve resource:__ retrieves a resource given its identifier.

    * __HTTP Operation:__ GET
    * __Consumes:__ -
    * __Produces:__ application/json; a JSON string in the following form `{'field1':'field1_val'}` (describe fields)
	* __Parameters:__ path parameter 'id'
	* __HTTP Status Codes:__
	    * Success: 200
		* Errors:
			* 404: resource does not exist

* __Update resource__

    * __HTTP Method:__ PUT
    * __Consumes:__ application/json; content in the form `{'field1':'field1_val'}` (describe fields)
    * __Produces:__ -
    * __Parameter:__ authorization header, path parameter 'id'
	* __HTTP Status Codes:__
	    * Success: 200
	    * Errors:
	        * 400: content data in invalid format
	        * 404: resource does not exist

* ...

IM Documentation
--

AccResource
--

__URL Template:__ /accounts/{username}/{password}

__Operations:__

* __Change Password:__ Changes password

    * __HTTP Operation:__ PUT
    * __Consumes:__ content in the form `{'newpassword'}`
    * __Produces:__ -
	* __Parameters:__ path parameter 'username','password'
	* __HTTP Status Codes:__
	    * Success: 200
		* Errors:
	        * 400: content data in invalid format
			
* __Change Online State:__ Sets the visible state of the user. 

    * __HTTP Method:__ PUT
    * __Consumes:__ content in the form `{'currentstate'}`
    * __Produces:__ -
    * __Parameter:__ path parameter 'username'
	* __HTTP Status Codes:__
	    * Success: 200
	    * Errors: -

AccProfileResource
--

__URL Template:__ /accounts/{username}/profile

__Operations:__

* __Change Nickname:__ Changes the profile name.

    * __HTTP Operation:__ PUT
    * __Consumes:__ content in the form `{'newname'}`
    * __Produces:__ -
	* __Parameters:__ path parameter 'username'
	* __HTTP Status Codes:__
	    * Success: 200
		* Errors:
	        * 400: content data in invalid format
			
* __Change Email Address:__ Changes the email address.

    * __HTTP Operation:__ PUT
    * __Consumes:__ content in the form `{'maxmustermann@rwth-aachen.de'}`
    * __Produces:__ -
	* __Parameters:__ path parameter 'username'
	* __HTTP Status Codes:__
	    * Success: 200
		* Errors:
	        * 400: content data in invalid format

* __Change Phone Number:__ Changes the phone number.

    * __HTTP Operation:__ PUT
    * __Consumes:__ content in the form `{'12345678'}`
    * __Produces:__ -
	* __Parameters:__ path parameter 'username'
	* __HTTP Status Codes:__
	    * Success: 200
		* Errors:
	        * 400: content data in invalid format

* __Change Profile Picture:__ Changes the profile picture of the actual user.

    * __HTTP Operation:__ PUT
    * __Consumes:__ content in the form `{'http://google.com/img/nicepic.jpg'}`
    * __Produces:__ -
	* __Parameters:__ path parameter 'username'
	* __MIME-Type:__ JPG, PNG, GIF
	* __HTTP Status Codes:__
	    * Success: 200
		* Errors:
	        * 400: content data in invalid format
			
* __Change Profile Picture:__ Changes the profile picture of the actual user.

    * __HTTP Operation:__ PUT
    * __Consumes:__ content in the form `{'http://google.com/img/nicepic.jpg'}`
    * __Produces:__ -
	* __Parameters:__ path parameter 'username'
	* __HTTP Status Codes:__
	    * Success: 200
		* Errors:
	        * 400: content data in invalid format			
			
* __Change Profile Visibility:__ Changes the availability of the user´s profile information for other users.

    * __HTTP Operation:__ PUT
    * __Consumes:__ content in the form `{'onlyforfriends'}`
    * __Produces:__ -
	* __Parameters:__ path parameter 'username'
	* __HTTP Status Codes:__
	    * Success: 200
		* Errors: -	
		
ContactRequestResource
--

__URL Template:__ /accounts/{username}

__Operations:__

* __Contact Request:__ Sends an invitation to another user.

    * __HTTP Operation:__ PUT
    * __Consumes:__ content in the form `{'username','otheruser'}`
    * __Produces:__ -
	* __Parameters:__ path parameter 'username'
	* __HTTP Status Codes:__
	    * Success: 200
		* Errors:
			* 404: resource does not exist

MessageResource
--

__URL Template:__ /messages/ 

__Operations:__

* __Send Single Message:__ Sends a message to another user.

    * __HTTP Operation:__ PUT
    * __Consumes:__ content in the form `{'username','otheruser','message lorem ipsum'}`
    * __Produces:__ -
	* __Parameters:__ path parameter 'username'
	* __HTTP Status Codes:__
	    * Success: 200
		* Errors: -

* __Send Group Message:__ Sends a message to a group of users.

    * __HTTP Operation:__ PUT
    * __Consumes:__ content in the form `{'username','user1,user2,user3,user4,...,userN','message lorem ipsum'}`
    * __Produces:__ -
	* __Parameters:__ path parameter 'username'
	* __HTTP Status Codes:__
	    * Success: 200
		* Errors: -
					
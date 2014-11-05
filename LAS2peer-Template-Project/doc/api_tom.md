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
	* __Parameter:__ path parameter 'id'
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

Profile Resource
--
__URL Template:__ /profile/{name}

__Operations:__

* __Retrieve Profile:__ Retrieves a profile given its name
	* __HHTP Method:__ GET
	* __Consumes:__ -
	* __Produces:__ application/json; a JSON string in the following form `{'email':'email_val', 'telephone':'telephone_val', 'imageLink':'imageLink_val', 'nickname':'nickname_val', 'visible':'visible_val'}` ('email'=The email adress of the contact, 'telephone'=The telephone number of the contact, 'imageLink'=The link of the profile image, 'nickname'=The nickname of the account, 'visible'=The visibility of the profile)
	* __Parameter:__ authorization header, path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 401: no authorization
			* 404: resource does not exist
			
* __Update Profile:__ Updates a profile given its name
 	* __HHTP Method:__ PUT
	* __Consumes:__ application/json; a JSON string in the following form `{'email':'email_val', 'telephone':'telephone_val', 'imageLink':'imageLink_val', 'nickname':'nickname_val', 'visible':'visible_var'}` ('email'=The email adress of the contact, 'telephone'=The telephone number of the contact, 'imageLink'=The link of the profile image, 'nickname'=The nickname of the account, 'visible'=The visibility of the profile)
	* __Produces:__ -
	* __Parameter:__ authorization header, path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: content data in invalid format
			* 401: no authorization
			* 404: resource does not exist

* __Delete Profile:__ Deletes a profile given its name
 	* __HHTP Method:__ DELETE
	* __Consumes:__ -
	* __Produces:__ -
	* __Parameter:__ authorization header, path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 401: no authorization
			* 404: resource does not exist

Group Resource
--
__URL Template:__ /group/{name}

__Operations:__

* __Retrieve Group:__ Retrieves a group given its name
	* __HHTP Method:__ GET
	* __Consumes:__ -
	* __Produces:__ application/json; a JSON string in the following form `{'name':'name_val', 'founder':'founder_val', 'description':'description_val', 'imageLink':'imageLink_val', 'member':['username':'username_val']}` ('name'=The name of the group, 'founder'=The name of the founder of the group, 'description'=The description of the group, 'imageLink'=The link to the profile image of the group, 'member'=The members of the group, except the founder, 'username'=The username of a member)
	* __Parameter:__ authorization header, path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 404: resource does not exist
			
* __Update Group:__ Updates a group given its name
 	* __HHTP Method:__ PUT
	* __Consumes:__ application/json; a JSON string in the following form `{'name':'name_val', 'founder':'founder_val', 'description':'description_val', 'imageLink':'imageLink_val', 'member':['username':'username_val']}` ('name'=The name of the group, 'founder'=The name of the founder of the group, 'description'=The description of the group, 'imageLink'=The link to the profile image of the group)
	* __Produces:__ -
	* __Parameter:__ authorization header, path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: content data in invalid format
			* 401: no authorization
			* 404: resource does not exist

* __Delete Group:__ Deletes a group given its name
 	* __HHTP Method:__ DELETE
	* __Consumes:__ -
	* __Produces:__ -
	* __Parameter:__ authorization header, path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 401: no authorization
			* 404: resource does not exist
			
Contact Resource
--
__URL Template:__ /contact/{name}

__Operations:__

* __Retrieve Contact:__ Retrieves the contacts for an account given its name
	* __HHTP Method:__ GET
	* __Consumes:__ -
	* __Produces:__ application/json; a JSON string in the following form `{'contact':['nickname':'nickname_val', 'username':'username_val']}` ('contact'=The contacts of the user, 'nickname'=The nickname of one contact, 'username'=The username of the contact)
	* __Parameter:__ authorization header, path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 401: no authorization
			* 404: resource does not exist

Request Resource
--
__URL Template:__ /request/{name}

__Operations:__

* __Retrieve Request:__ Retrieves the contact requests for an account given its name
	* __HHTP Method:__ GET
	* __Consumes:__ -
	* __Produces:__ application/json; a JSON string in the following form `{'request':['nickname':'nickname_val', 'username':'username_val']}` ('request'=A request one user has done to this account, 'nickname'=The nickname of the account who requests, 'username'=The username of the account who requests)
	* __Parameter:__ authorization header, path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 401: no authorization
			* 404: resource does not exist
			
* __Update Request:__ Sends a request to the contact given its name
 	* __HHTP Method:__ PUT
	* __Consumes:__ application/json; a JSON string in the following form `{'username':'username_val'}` ('username'=The username of the account who requests)
	* __Produces:__ -
	* __Parameter:__ authorization header, path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: content data in invalid format
			* 401: no authorization
			* 404: resource does not exist

* __Delete Contact:__ Deletes a contact given its name
 	* __HHTP Method:__ DELETE
	* __Consumes:__ -
	* __Produces:__ -
	* __Parameter:__ authorization header, path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 401: no authorization
			* 404: resource does not exist
			
Single Message Resource
--
__URL Template:__ /message/single/{name}

__Operations:__

* __Retrieve Single Message:__ Retrieves the single messages for an conversation with a contact given its name
	* __HHTP Method:__ GET
	* __Consumes:__ -
	* __Produces:__ application/json; a JSON string in the following form `{'message':['text':'text_val', 'timestamp':'timestamp_val', 'sender':'sender_val' ]}` ('message'=The messages of a conversation, 'text'=The message text, 'timestamp'=The time the message was sent, 'sender'=The sender of the message)
	* __Parameter:__ path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 404: resource does not exist
			
* __Send Single Message:__ Sends a message to the contact given its name
 	* __HHTP Method:__ PUT
	* __Consumes:__ application/json; a JSON string in the following form `{'message':'message_val', 'timestamp':'timestamp_val'}` ('message'=The text of the message, 'timestamp'=The time the message was sent)
	* __Produces:__ -
	* __Parameter:__ path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: content data in invalid format
			* 404: resource does not exist

Group Message Resource
--
__URL Template:__ /message/group/{name}

__Operations:__

* __Retrieve Group Message:__ Retrieves the messages for an conversation within a group given its name
	* __HHTP Method:__ GET
	* __Consumes:__ -
	* __Produces:__ application/json; a JSON string in the following form `{'message':['text':'text_val', 'timestamp':'timestamp_val', 'sender':'sender_val' ]}` ('message'=The messages of a conversation, 'text'=The message text, 'timestamp'=The time the message was sent, 'sender'=The sender of the message)
	* __Parameter:__ path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 404: resource does not exist
			
* __Send Group Message:__ Sends a message to a group given its name
 	* __HHTP Method:__ PUT
	* __Consumes:__ application/json; a JSON string in the following form `{'message':'message_val', 'timestamp':'timestamp_val'}` ('message'=The text of the message, 'timestamp'=The time the message was sent)
	* __Produces:__ -
	* __Parameter:__ authorization header, path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: content data in invalid format
			* 401: no authorization
			* 404: resource does not exist

Invitation Resource
--
__URL Template:__ /invitation/{name}

__Operations:__

* __Retrieve invitations:__ Retrieves the invitation to groups for a contact given its name
	* __HHTP Method:__ GET
	* __Consumes:__ -
	* __Produces:__ application/json; a JSON string in the following form `{'group':['groupname':'groupname_val', 'founder':'founder_val']}` ('group'=A group founded, 'groupname'=The name of the group, 'founder'=The founder of the group)
	* __Parameter:__ authorization header, path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 401: no authorization
			* 404: resource does not exist
			
* __Send invitation:__ Sends an invitation to the contact given its name
 	* __HHTP Method:__ PUT
	* __Consumes:__ application/json; a JSON string in the following form `{'groupname':'groupname_val'}` ('groupname'=The name of the group the contact should be invited to)
	* __Produces:__ -
	* __Parameter:__ authorization header, path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: content data in invalid format
			* 401: no authorization
			* 404: resource does not exist
			
* __Delete Group Member:__ Deletes a Member out of a group
 	* __HHTP Method:__ DELETE
	* __Consumes:__ -
	* __Produces:__ -
	* __Parameter:__ authorization header, path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 401: no authorization
			* 404: resource does not exist

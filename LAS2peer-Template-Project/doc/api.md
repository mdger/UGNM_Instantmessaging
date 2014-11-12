Instant Messenger Service API Documentation
==

This service offers the possibility to build an own Instant Messenger. It supports contacts groups various messages and so on.

In the following we describe each resource including its supported operations in detail.

Profile Resource
--
__URL Template:__ /profile

__Operations:__

* __Create Profile:__ Creates a profile
 	* __HTTP Method:__ POST
	* __Consumes:__ application/json; a JSON string in the following form `{'email':'email_val', 'telephone':'telephone_val', 'imageLink':'imageLink_val', 'nickname':'nickname_val', 'visible':'visible_val'}` ('email'=The email adress of the contact, 'telephone'=The telephone number of the contact, 'imageLink'=The link of the profile image, 'nickname'=The nickname of the account, 'visible'=The visibility of the profile)
	* __Produces:__ -
	* __Parameter:__ authorization header
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: Content data in invalid format
			* 403: Access denied
			* 409: Could not create
			
			
* __Get Profile:__ Retrieves a profile given its username
	* __HTTP Method:__ GET
	* __Consumes:__ application/json; a JSON string in the following form `{'username':'username_val'}` ('username'= the username of the profile to be retrieved')
	* __Produces:__ application/json; a JSON string in the following form `{'email':'email_val', 'telephone':'telephone_val', 'imageLink':'imageLink_val', 'nickname':'nickname_val', 'visible':'visible_val'}` ('email'=The email address of the contact, 'telephone'=The telephone number of the contact, 'imageLink'=The link of the profile image, 'nickname'=The nickname of the account, 'visible'=The visibility of the profile 1-Everyone 0-OnlyContacts)
	* __Parameter:__ authorization header
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 401: Restricted content
			* 403: Access denied 
			* 404: Resource does not exist
			
						
			
* __Update Profile:__ Updates a profile of the active User
 	* __HTTP Method:__ PUT
	* __Consumes:__ application/json; a JSON string in the following form `{'email':'email_val', 'telephone':'telephone_val', 'imageLink':'imageLink_val', 'nickname':'nickname_val', 'visible':'visible_val'}` ('email'=The email address of the contact, 'telephone'=The telephone number of the contact, 'imageLink'=The link of the profile image, 'nickname'=The nickname of the account, 'visible'=The visibility of the profile 1-Everyone 0-OnlyContacts)
	* __Produces:__ -
	* __Parameter:__ 
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: Content data in invalid format
			* 404: Resource does not exist
			
			
* __Delete Profile:__ Deletes the profile of the active User
 	* __HTTP Method:__ DELETE
	* __Consumes:__ -
	* __Produces:__ -
	* __Parameter:__ 
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 404: Resource does not exist
			

			


Group Resource
--
__URL Template:__ /group/{groupname}

__Operations:__

* __Get Group:__ Retrieves a group given its name
	* __HTTP Method:__ GET
	* __Consumes:__ -
	* __Produces:__ application/json; a JSON string in the following form `{'name':'name_val', 'founder':'founder_val', 'description':'description_val', 'imageLink':'imageLink_val', 'member':['username':'username_val']}` ('name'=The name of the group, 'founder'=The name of the founder of the group, 'description'=The description of the group, 'imageLink'=The link to the profile image of the group, 'member'=The members of the group, except the founder, 'username'=The username of a member)
	* __Parameter:__ path parameter 'groupname'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 404: Resource does not exist
			
	
* __Create Group:__ Creates a group given its name
 	* __HTTP Method:__ POST
	* __Consumes:__ application/json; a JSON string in the following form `{'name':'name_val', 'founder':'founder_val', 'description':'description_val', 'imageLink':'imageLink_val'}` ('name'=The name of the group, 'founder'=The name of the founder of the group, 'description'=The description of the group, 'imageLink'=The link to the profile image of the group)
	* __Produces:__ -
	* __Parameter:__ path parameter 'groupname'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: Content data in invalid format
			* 409: Could not create
			
			
* __Update Group:__ Updates a group given its name
 	* __HTTP Method:__ PUT
	* __Consumes:__ application/json; a JSON string in the following form `{'name':'name_val', 'founder':'founder_val', 'description':'description_val', 'imageLink':'imageLink_val'}` ('name'=The name of the group, 'founder'=The name of the founder of the group, 'description'=The description of the group, 'imageLink'=The link to the profile image of the group)
	* __Produces:__ -
	* __Parameter:__ 
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: Content data in invalid format
			* 403: Access denied
			* 404: Resource does not exist
			

* __Delete Group:__ Deletes a group given its name
 	* __HHTP Method:__ DELETE
	* __Consumes:__ -
	* __Produces:__ -
	* __Parameter:__ 
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 403: Access denied
			* 404: Resource does not exist
			
			
Contact Resource
--

__URL Template:__ profile/contact/

__Operations:__

* __Get Contact:__ Retrieves the contacts for the active User
	* __HTTP Method:__ GET
	* __Consumes:__ -
	* __Produces:__ application/json; a JSON string in the following form `{'contact':['nickname':'nickname_val', 'username':'username_val']}` ('contact'=The contacts of the user, 'nickname'=The nickname of one contact, 'username'=The username of the contact)
	* __Parameter:__ 
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 404: Resource does not exist


__URL Template:__ profile/contact/{username}

__Operations:__
			
* __Create Contact:__ Creates a Contact If the active User received a request from the contact
 	* __HTTP Method:__ POST
	* __Consumes:__ application/json; a JSON string in the following form `{'username':'username'}` ('username'=The user who should be added)
	* __Produces:__ -
	* __Parameter:__ authorization header, path parameter 'username'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: Content data in invalid format
			* 409: Could not create
			
			
* __Delete Contact:__ Deletes a contact given its name
 	* __HTTP Method:__ DELETE
	* __Consumes:__ -
	* __Produces:__ -
	* __Parameter:__ 
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 404: Resource does not exist

Request Resource
--

__URL Template:__ profile/contact/request/

__Operations:__

* __Get Request:__ Retrieves the contact requests for the active User
	* __HTTP Method:__ GET
	* __Consumes:__ -
	* __Produces:__ application/json; a JSON string in the following form `{'request':['nickname':'nickname_val', 'username':'username_val']}` ('request'=A request one user has done to this account, 'nickname'=The nickname of the account who requests, 'username'=The username of the account who requests)
	* __Parameter:__ 
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 404: resource does not exist
			
			
* __Create Request:__ Sends a request to the contact given its username
 	* __HTTP Method:__ POST
	* __Consumes:__ application/json; a JSON string in the following form `{'username':'username_val'}` ('username'=The username of the account to be requested
	* __Produces:__ -
	* __Parameter:__ 
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: Content data in invalid format
			* 404: Resource does not exist
			* 409: Could not create
			

* __Delete Request:__ Deletes a request given the other username to be deleted
 	* __HTTP Method:__ DELETE
	* __Consumes:__ application/json; a JSON string in the following form `{'username':'username_val'}` ('username'=The username of the account to be requested
	* __Produces:__ -
	* __Parameter:__ 
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 403: Access denied
			* 404: Resource does not exist
			
			
Single Message Resource
--
__URL Template:__ /message/single/{username}

__Operations:__

* __Get Single Message:__ Retrieves the single messages for an conversation with a contact given its name
	* __HTTP Method:__ GET
	* __Consumes:__ -
	* __Produces:__ application/json; a JSON string in the following form `{'message':['text':'text_val', 'timestamp':'timestamp_val', 'sender':'sender_val' ]}` ('message'=The messages of a conversation, 'text'=The message text, 'timestamp'=The time the message was sent, 'sender'=The sender of the message)
	* __Parameter:__ authorization header, path parameter 'username' (username of conversation partner)
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 403: Access denied
			* 404: Resource does not exist
			
			
			
* __Send Single Message:__ Sends a message to the contact given its username
 	* __HTTP Method:__ POST
	* __Consumes:__ application/json; a JSON string in the following form `{'message':'message_val', 'timestamp':'timestamp_val'}` ('message'=The text of the message, 'timestamp'=The time the message was sent)
	* __Produces:__ -
	* __Parameter:__ authorization header, path parameter 'username' (username of conversation partner)
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: Content data in invalid format
			* 403: Access denied
			* 404: Resource does not exist
			
* __Delete Message:__ Deletes a message given the name of its sender
 	* __HTTP Method:__ DELETE
	* __Consumes:__ -
	* __Produces:__ -
	* __Parameter:__ path parameter 'username' (username of conversation partner)
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 404: Resource does not exist
			
/* Beim ersten Testen wird diese Resource nicht ber¨¹cksichtig. Es kommt evtl. ¨¹berarbeitung
Group Message Resource
--
__URL Template:__ /message/group/{groupname}

__Operations:__

* __Retrieve Group Message:__ Retrieves the messages for an conversation within a group given its name and deletes messages older than 90 days
	* __HTTP Method:__ GET
	* __Consumes:__ -
	* __Produces:__ application/json; a JSON string in the following form `{'message':['messageID':'messageID_val', 'text':'text_val', 'timestamp':'timestamp_val', 'sender':'sender_val' ]}` ('messageID'=The ID of the message, 'message'=The messages of a conversation, 'text'=The message text, 'timestamp'=The time the message was sent, 'sender'=The sender of the message)
	* __Parameter:__ authorization header, path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 403: Access denied
			* 404: Resource does not exist
			
			
* __Send Group Message:__ Sends a message to a group given its name
 	* __HTTP Method:__ POST
	* __Consumes:__ application/json; a JSON string in the following form `{'message':'message_val', 'timestamp':'timestamp_val'}` ('message'=The text of the message, 'timestamp'=The time the message was sent)
	* __Produces:__ -
	* __Parameter:__ authorization header, path parameter 'name'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: Content data in invalid format
			* 403: Access denied
			* 404: Resource does not exist
			* 409: Could not create
Beim ersten Testen wird diese Resource nicht ber¨¹cksichtig. Es kommt evtl. ¨¹berarbeitung*/			

Unread Message Resource
--
__URL Template:__ /message/single/unread/{contact_username}

__Operations:__

* __Get Unread Messages:__ Retrieves all the unread messages from a conversation
	* __HTTP Method:__ GET
	* __Consumes:__ -
	* __Produces:__ application/json; a JSON string in the following form `{'message':['messageID':'messageID_val','text':'text_val', 'timestamp':'timestamp_val', 'sender':'sender_val']}` ('message'=The messages of a conversation, 'text'=The message text, 'timestamp'=The time the message was sent, 'sender'=The sender of the message)
	* __Parameter__: authorization header, path parameter 'contact_username'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 403: Access denied
			* 404: Resource does not exist
			
			
* __Set unread to read:__ Sets a message that was unread to the status read
 	* __HTTP Method:__ PUT
	* __Consumes:__ application/json; a JSON string in the following form `{'messageID':'messageID_val'}` ('messageID'=The ID of the message)
	* __Produces:__ -
	* __Parameter:__ authorization header, path parameter 'contact_username'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: Content data in invalid format
			* 403: Access denied
			* 404: Resource does not exist

Member Resource
--
__URL Template:__ /group/member/{username}

__Operations:__

* __Get memberships:__ Retrieves the list of groups for a contact given its username
	* __HTTP Method:__ GET
	* __Consumes:__ -
	* __Produces:__ application/json; a JSON string in the following form `{'group':['groupname':'groupname_val', 'founder':'founder_val'}` ('group'=A group founded, 'groupname'=The name of the group, 'founder'=The founder of the group)
	* __Parameter:__ authorization header, path parameter 'username'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 403: Access denied
			* 404: Resource does not exist
			
			
* __Add Member:__ Adds a member to a group
 	* __HTTP Method:__ POST
	* __Consumes:__ application/json; a JSON string in the following form `{'groupname':'groupname_val'}` ('groupname'=The name of the group the contact should be added to)
	* __Produces:__ -
	* __Parameter:__ authorization header, path parameter 'username'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: Content data in invalid format
			* 403: Access denied
			* 404: Resource does not exist
			* 409: Could not create
			

* __Delete Member:__ Deletes a member out of a group given its name
 	* __HTTP Method:__ DELETE
	* __Consumes:__ application/json; a JSON string in the following form `{'groupname':'groupname_val'}` ('groupname'=The name of the group the user should be deleted from)
	* __Produces:__ -
	* __Parameter:__ authorization header, path parameter 'username'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: content data in invalid format
			* 403: Access denied
			* 404: resource does not exist

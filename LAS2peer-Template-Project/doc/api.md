Instant Messenger Service API Documentation
==

This service offers the possibility to build an own Instant Messenger. It supports contacts groups various messages and so on.

In the following we describe each resource including its supported operations in detail.

Profile Resource
--
__URL Template:__ /profile

__Description:__ The profile resource is used to manage the profile information of the user. Every User can choose a Nickname, which will displayed in Conversations. It may also specify additional information , such as email address , phone number and a link to an image.And he can control whether their profile information is public , or only visible to his contacts. 

__Operations:__

* __Create Profile:__ Creates a profile for the active User.
	* __HTTP Method:__ POST
 	* __Consumes:__ application/json; a JSON string in the following form `{'email':'email_val', 'telephone':'telephone_val', 'imageLink':'imageLink_val', 'nickname':'nickname_val', 'visible':'visible_val'}` ('email'=The email address of the user, 'telephone'=The telephone number of the user, 'imageLink'=The link of the profile image, 'nickname'=The nickname of the user, 'visible'=The visibility of the profile (1=public, 0=only contacts))
 	* __Produces:__ -
 	* __Parameter:__ authorization header
 	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: Content data in invalid format
			* 403: Access denied
			* 409: Could not create
			
			
* __Get Profile:__ Retrieves the profile of a user. Given by his username coded in JSON.
	* __HTTP Method:__ GET
 	* __Consumes:__ application/json; a JSON string in the following form `{'username':'username_val'}` ('username'= the username of the profile to be retrieved')
 	* __Produces:__ application/json; a JSON string in the following form `{'email':'email_val', 'telephone':'telephone_val', 'imageLink':'imageLink_val', 'nickname':'nickname_val', 'visible':'visible_val'}` ('email'=The email address of the user, 'telephone'=The telephone number of the user, 'imageLink'=The link of the profile image, 'nickname'=The nickname of the user, 'visible'=The visibility of the profile 1-Everyone 0-OnlyContacts)
 	* __Parameter:__ authorization header
 	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 401: Restricted content
			* 403: Access denied 
			* 404: Resource does not exist
			
						
			
* __Update Profile:__ Updates the profile Information of the active User.
 	* __HTTP Method:__ PUT
	* __Consumes:__ application/json; a JSON string in the following form `{'email':'email_val', 'telephone':'telephone_val', 'imageLink':'imageLink_val', 'nickname':'nickname_val', 'visible':'visible_val'}` ('email'=The email address of the user, 'telephone'=The telephone number of the user, 'imageLink'=The link of the profile image, 'nickname'=The nickname of the user, 'visible'=The visibility of the profile 1-Everyone 0-OnlyContacts)
	* __Produces:__ -
	* __Parameter:__ 
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: Content data in invalid format
			* 404: Resource does not exist
			
			
* __Delete Profile:__ Deletes the profile of the active User.
 	* __HTTP Method:__ DELETE
	* __Consumes:__ -
	* __Produces:__ -
	* __Parameter:__ 
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 404: Resource does not exist
			
			
Contact Resource
--

__URL Template:__ profile/contact/

__Description:__ This resource manage the contact-lists of users. Users can communicate only if they are contacts.

__Operations:__

* __Get Contact:__ Retrieves all contacts of the active User.
	* __HTTP Method:__ GET
	* __Consumes:__ -
	* __Produces:__ application/json; a JSON string in the following form `{'contact':['nickname':'nickname_val', 'username':'username_val']}` ('contact'=The contacts of the user, 'nickname'=The nickname of one contact, 'username'=The username of the contact)
	* __Parameter:__ 
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 404: Resource does not exist

			
* __Create Contact:__ Creates a Contact If the active User received a request from the contact.
 	* __HTTP Method:__ POST
	* __Consumes:__ application/json; a JSON string in the following form `{'username':'username'}` ('username'=The user who should be added)
	* __Produces:__ -
	* __Parameter:__ authorization header
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: Content data in invalid format
			* 409: Could not create
			
			
* __Delete Contact:__ Deletes a contact from the contact list, given by his name coded in JSON.
 	* __HTTP Method:__ DELETE
	* __Consumes:__ application/json; a JSON string in the following form `{'username':'username'}` ('username'=The user who should be deleted)
	* __Produces:__ -
	* __Parameter:__ 
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 404: Resource does not exist

Request Resource
--

__Description:__ This Resource manage the Contact-Requests. To take someone in your contact list , you have to send a contact request. These can be accepted or rejected.

__URL Template:__ profile/contact/request/

__Operations:__

* __Get Request:__ Retrieves all open contact requests for the active User.
	* __HTTP Method:__ GET
 	* __Consumes:__ -
 	* __Produces:__ application/json; a JSON string in the following form `{'request':['nickname':'nickname_val', 'username':'username_val']}` ('request'=A request one user has done to this account, 'nickname'=The nickname of the account who requests, 'username'=The username of the account who requests)
 	* __Parameter:__ 
 	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 404: resource does not exist
			
			
* __Create Request:__ Sends a contact request to a other user given by his username coded in JSON.
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
			

* __Delete Request:__ Deletes a contact request given the other username to be deleted coded in JSON
 	* __HTTP Method:__ DELETE
	* __Consumes:__ application/json; a JSON string in the following form `{'username':'username_val'}` ('username'=The username of the account to be deleted.
	* __Produces:__ -
	* __Parameter:__ 
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 403: Access denied
			* 404: Resource does not exist
			
			
Single Message Resource
--
__Description:__ This resource is used to manage messages that are send between two users.

__URL Template:__ /message/single/{username}

__Operations:__

* __Get Single Message:__ Retrieves the single messages for an conversation with a contact given its username as path parameter
	* __HTTP Method:__ GET
	 * __Consumes:__ -
	 * __Produces:__ application/json; a JSON string in the following form `{'message':['messageID':'messageID_val', 'text':'text_val', 'timestamp':'timestamp_val', 'sender':'sender_val' ]}` ('messageID': The id of the message in the database, 'message'=The messages of a conversation, 'text'=The message text, 'timestamp'=The time the message was sent, 'sender'=The sender of the message)
	 * __Parameter:__ authorization header, path parameter 'username' (username of conversation partner)
	 * __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 403: Access denied
			* 404: Resource does not exist			
			
			
* __Send Single Message:__ Sends a single message to the contact given its username as path parameter
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
			
			
* __Delete Message:__ Deletes a distinct message you got from a single-message-conversation given the name of its sender as path parameter and the id of the message
 	* __HTTP Method:__ DELETE
	* __Consumes:__ application/json; a JSON string in the following form `{'messageID':'messageID_val}`
	* __Produces:__ -
	* __Parameter:__ content parameter 'messageID'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: Content data in invalid format
			* 404: Resource does not exist
			

Unread Message Resource
--
__Description:__ This resource is used to manage single messages that are unread

__URL Template:__ /message/single/unread/

__Operations:__

* __Get Unread Messages:__ Retrieves all the unread messages for a user
	* __HTTP Method:__ GET
	* __Consumes:__ -
	* __Produces:__ application/json; a JSON string in the following form `{'message':['messageID':'messageID_val','text':'text_val', 'timestamp':'timestamp_val', 'sender':'sender_val']}` ('message'=The messages of a conversation, 'text'=The message text, 'timestamp'=The time the message was sent, 'sender'=The sender of the message)
	* __Parameter__: authorization header
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 403: Access denied
			* 404: Resource does not exist
			
		
* __Set unread to read:__ Sets a message that was unread to the status read
 	* __HTTP Method:__ PUT
	* __Consumes:__ application/json; a JSON string in the following form `{'messageID':'messageID_val'}` ('messageID': The ID of the message)
	* __Produces:__ -
	* __Parameter:__ content parameter with the message ID of the distinct message
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: Content data in invalid format
			* 404: Resource does not exist



/* Beim ersten Testen wird diese Resource nicht beruecksichtig. Es kommt evtl. eine Überarbeitung
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
Beim ersten Testen wird diese Resource nicht beruecksichtig. Es kommt evtl. ï¿½ï¿½berarbeitung*/			




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
	* __Consumes:__ application/json; a JSON string in the following form `{'description':'description_val', 'imageLink':'imageLink_val'}` ('description'=The description of the group, 'imageLink'=The link to the profile image of the group)
	* __Produces:__ -
	* __Parameter:__ authorization header, path parameter 'groupname'
	* __HTTP Status Codes:__
		* Success: 200
		* Errors:
			* 400: Content data in invalid format
			* 409: Could not create
			
			
* __Update Group:__ Updates a group given its name
 	* __HTTP Method:__ PUT
	* __Consumes:__ application/json; a JSON string in the following form `{'description':'description_val', 'imageLink':'imageLink_val'}` ('description'=The description of the group, 'imageLink'=The link to the profile image of the group)
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

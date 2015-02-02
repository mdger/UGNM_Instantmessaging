    
    // create new instance of TemplateServiceClient, given its endpoint URL
    var client = new TemplateServiceClient("http://localhost:8080/im");	
    
    // function defined as response to a click on the first button (see below)
    function getExample() {
      client.getMethod(
        function(data,type) {
          // this is the success callback
          console.log(data);
          $("#getExampleOutput").html(data);
        },
        function(error) {
          // this is the error callback
          console.log(error);
          $("#getExampleOutput").html(error);
        }
      );
    }
    
    // function defined as response to a click on the second button (see below)
    function postExample(input) {
      client.postMethod(input,
        function(data,type) {
          // this is the success callback
          console.log(data);
          $("#postExampleOutput").html(data);
        },
        function(error) {
          // this is the error callback
          console.log(error);
          $("#postExampleOutput").html(error);
        }
      );
    }
	
/**
* Get Users
*/
TemplateServiceClient.prototype.getUsers = function(successCallback, errorCallback) {
	this.sendRequest("GET",
		"profile",
		"",
		"application/json",
		{},
		successCallback,
		errorCallback
	);
};

/** 
* import OpenID Connect Button
*/
    (function() {
      var po = document.createElement('script');
      po.type = 'text/javascript';
      po.async = true;
      po.src = 'js/oidc-button.js';
      var s = document.getElementsByTagName('script')[0]; 
      s.parentNode.insertBefore(po, s);
    })();
    
    // OpenID Connect Button: implement a callback function
    function signinCallback(result) {
      if(result === "success"){
        // authenticated
		
		// OpenID Connect user info
		$("#uname").html(oidc_userinfo.name);
		$("#email").html(oidc_userinfo.email);
		$("#sub").html(oidc_userinfo.sub);
		$(".authenticated").removeClass("hidden");
		// Tries to get Profile with oidc_userinfo.preferred_username, if not, create Profile automatically.
		var usrname = oidc_userinfo.preferred_username;
		client.getProfile(
		usrname,
        function(data,type) {
          console.log(data);
        },
        function(error) {
			  var content = "{\"username\":" + oidc_userinfo.preferred_username + ",\"email\":" + oidc_userinfo.email + ",\"telephone\": 00000000,\"imageLink\":\"imagelink\",\"nickname\":\"Chatter\",\"visible\":1}";
			  client.postProfile(
				content,
				function(data,type) {
				  console.log(data);
				},
				function(error) {
				  console.log(error);
				}
			  );
        }		
		);
	
      } else {
        // anonymous
      }
    }
	
    function getProfileInfo() {
	var content = oidc_userinfo.preferred_username;
      client.getProfile(
		content,
        function(data,type) {
          // this is the success callback
          console.log(data);
		  $("#profile_username").html(oidc_userinfo.preferred_username);
          $("#pr_mail").html(data.email);
		  if(data.visible == 1) {
			$("#pr_vis").html("visible");
		  } else {
			$("#pr_vis").html("not visible");
		  }
          $("#pr_tel").html(data.telephone);
          $("#pr_img").html("<img src=" + data.imageLink + ">");
		  $("#pr_nick").html(data.nickname);
		  
		$('#ch_mail').val(data.email);
		$('#ch_tel').val(data.telephone);
		$('#ch_img').val(data.imageLink);
		$('#ch_nick').val(data.nickname);
        },
        function(error) {
          // this is the error callback
          console.log(error);
          $("#getExampleOutput").html(error);
        }
      );
    }
	
    function updateProfileInfo(input) {
      client.updateProfile(
		input,
        function(data,type) {
          // this is the success callback
          console.log(data);
        },
        function(error) {
          // this is the error callback
          console.log(error);
        }
      );
    }	
    
    //Show Profile Information at #otherProfileModal 
    function showOtherProfile(profileName) {
    	var content = profileName.toString();
    	console.log(profileName)
          client.getProfile(
    		content,
            function(data,type) {
              // this is the success callback
              console.log(data);
    		  $("#otherProfileUserName").html(profileName);
              $("#otherProfileMail").html(data.email);    		 
              $("#otherProfileTelephone").html(data.telephone);
             // $("#otherProfileImage").html("<img src=" + data.imageLink + ">");
    		  $("#otherProfileNickName").html(data.nickname);
            },
            function(error) {
              // this is the error callback
              console.log(error);              
            }
          );
        }
    
    //Show Single Chat History at #chatMessages
    function showSingleMessages(userName) {
          client.getSingleMessages(
    		userName,
            function(data,type) {
             
			  // All Messages received
              $("#chatMessages").empty();
              for (var i = 0; i < data.messages.length; i++) {
    		    $("#chatMessages").append("<li><div class=\"chatname\"> " +
    		      data.messages[i].sender + " <short class=\"timestamp\">" + data.messages[i].timestamp +
    		    "</short></div><p class=\"chatMessage\">" +
    		      data.messages[i].text +
    		    "</p>" +
    		    "</li>");   
              }
              $("#chatInput").html(
              "<input id=\"chatMessageInput\" type=\"text\" class=\"form-control input-sm\" placeholder=\"Type your message here...\" />"+
              "<span class=\"input-group-btn\">"+
                 "<button class=\"btn btn-warning btn-sm\" id=\"sendMessageButton\" onClick=\"sendSingleMessage('"+data.contact+"')\";>send</button>"+
              "</span>");
			  $('#emoteBox').removeClass("hidden");
			  $('.chatMessages').emoticonize();
			},
            function(error) {
              // this is the error callback
            	//Show Empty chatWindow
              console.log(error);
              var contactName = error.replace("No messages found for contact ","'");
              contactName = contactName.replace("!","'");
              console.log(contactName);
              $("#chatMessages").empty();             
    		  $("#chatMessages").append("<li> <p> </p> </li>");
              $("#chatInput").html(
              "<input id=\"chatMessageInput\" type=\"text\" class=\"form-control input-sm\" placeholder=\"Type your message here...\" />"+
              "<span class=\"input-group-btn\">"+
                 "<button class=\"btn btn-warning btn-sm\" id=\"sendMessageButton\" onClick=\"sendSingleMessage("+contactName+")\";>send</button>"+
              "</span>");
		      $('.chatMessages').emoticonize();			  
            }
          );
        }
		
		// Send Message on Enter
		  $('#chatMessageInput').on("keyup keydown", function() {
			if(e.keyCode == 13) {
			  sendSingleMessage(data.contact);
			}
		  });		
		
    
  //Send Single Message 
    function sendSingleMessage(userName) {
    	 var text = $("#chatMessageInput").val();
         var content = "{\"message\":\"" + text + "\",\"timestamp\":\"" + "2014-01-01 00:00:00" + "\"}";	
          client.sendSingleMessage(
    		userName,
    		content,
            function(data,type) {
    		  var contact = data.replace("Message to ", "");
    		  contact = contact.replace(" was sent!", "");
    		  showSingleMessages(contact);
			  $('.chatMessages').emoticonize();
            },
            function(error) {
              // this is the error callback
              console.log(error);              
			  $('.chatMessages').emoticonize();    
			}
          );
        }
    
    
    
    //Ask if really want to delete contact at #deleteContactModal
    function showDeleteContact(profileName) { 
              $("#deleteContactInformation").html("Are you sure, that you want to delete "+ profileName + " as friend?");
              $("#deleteContactButtons").html("" +
            		"<button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\" onClick=\"deleteContact(\'"+profileName+"\')\" >Yes</button>"+
              		"<button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">No</button>");  		     
     }
    
    function deleteContact(username) {
        client.deleteContact(
          username,
          function(data,type) {
            // Contact successfully deleted
  			console.log(data);
          },
          function(error) {
            // Error while deleting contact
            console.log(error);
          }
        );
      }   
    
    	
    function getUsersList() {
      client.getUsers(
        function(data,type) {
          // this is the success callback
			$("#userList").empty();
			for (var i = 0; i < data.length; i++) {
				// This block will be executed 100 times.
				$("#userList").append("<li><a href=\"#\" data-toggle=\"modal\" data-target=\"#otherProfileModal\" onClick=\"showOtherProfile(\'"+data[i].username+"\')\" >" + data[i].username + " ("  + data[i].nickname + ") <button onClick=\"addContactButtonizer(\'"+data[i].username+"\')\" id=\"addContactButton\" type=\"button\" class=\"btn btn-primary\">Add " + data[i].username + "</button></a> </li>");
			}		
			console.log(data);
        },
        function(error) {
          // this is the error callback
          console.log(error);
        }
      );
    }   
    
    function getContactList() {
        client.getContacts(
          function(data,type) {
            // Show Contacts
        	$("#contactList").empty();  
  			for (var i = 0; i < data.length; i++) {
  				$("#contactList").append("<li> " +
  						"<button type=\"button\" class=\"btn btn-warning\" id='dLabel' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>" +
  						data[i].nickname +
  						"</button>"+
  							"<ul class='dropdown-menu' role='menu' aria-labelledby='dLabel'>"+
  								"<li>"+
  								"<a href=\"#\" onClick=\"showSingleMessages(\'"+data[i].username+"\')\" > start chat </a>"+ 
  								"</li>"+
  								"<li>"+
  								"<a href=\"#\" data-toggle=\"modal\" data-target=\"#otherProfileModal\" onClick=\"showOtherProfile(\'"+data[i].username+"\')\" > show profile </a>"+
  								"</li>"+
  								"<li>"+
  								"<a href=\"#\" data-toggle=\"modal\" data-target=\"#deleteContactModal\" onClick=\"showDeleteContact(\'"+data[i].username+"\')\" > delete contact </a>"+
  								"</li>"+
  							"</ul>" +
  						"</li>");
  			}		
  			console.log(data);
          },
          function(error) {
            // No contacts found
        	  $("#contactList").empty();
        	  $("#contactList").append("You have no contacts");
            console.log(error);
          }
        );
      }   
    
    
       
	// Sends Form Input into the Service methode updateProfile
	$( "#submitButton" ).click(function() {
		var email = $("#ch_mail").val();
		var tele  = $("#ch_tel").val();
		var img   = $("#ch_img").val();
		var nick  = $("#ch_nick").val();
		var visi  = $("#ch_vis").val();
       	var input = "{\"userName\":\"" + oidc_userinfo.preferred_username + "\",\"email\":\"" + email + "\",\"telephone\":" + tele + ",\"imageLink\":\"" + img + "\",\"nickname\":\"" + nick + "\",\"visible\":" + visi + "}";							
		updateProfileInfo(input);
	});				
	
	// Sends Contact Request Form Input
	$( "#sendRequestButton" ).click(function() {
		var nameOfContact = $("#cr_name").val();
       	var input = "{\"myusername\":\"" + oidc_userinfo.preferred_username + "\",\"username\":\"" + nameOfContact + "\"}";							
		client.postRequest(
		input,
        function(data,type) {
          // this is the success callback
          console.log(data);
        },
        function(error) {
          // this is the error callback
          console.log(error);
        }
      );
	});					
	
	// Sends Contact Request by Button
	function addContactButtonizer(contactUser) {
       	var input = "{\"myusername\":\"" + oidc_userinfo.preferred_username + "\",\"username\":\"" + contactUser + "\"}";							
		client.postRequest(
		input,
        function(data,type) {
          // this is the success callback
          console.log(data);
        },
        function(error) {
          // this is the error callback
          console.log(error);
        }
      );		
	}
	
	// Get Contact Requests
	$( "#pendingRequestsButton" ).click(function() {       							
		
		client.getRequests(
        function(data,type) {
        	//New Requests found
        	$("#requestList").empty();
        	if(data.lenght == 1) {        		
        		$("#requestList").append("<p>You have a new request</p>");	
        	} else {
        		$("#requestList").append("<p>You have "+ data.length +" new requests</p>");
        	}
			for (var i = 0; i < data.length; i++) {				
				$("#requestList").append("" +
				"<li id=\"contactRequest"+data[i].username+"\">"	+ 				
				"<a href=\"#\" data-toggle=\"modal\" data-target=\"#otherProfileModal\" onClick=\"showOtherProfile(\'"+data[i].username+"\')\" >"+ data[i].nickname + " ("  + data[i].username + ") </a>"+
				"<button type=\"button\" class=\"btn btn-default\" onClick=\"acceptRequest(\'"+data[i].username+"\')\" >accept</button>"+
				"<button type=\"button\" class=\"btn btn-default\" onClick=\"rejectRequest(\'"+data[i].username+"\')\" >reject</button>"+
				"</li>");
			}		  		  
          console.log(data);
        },
        function(error) {
        	//NO new Requests found
        	$("#requestList").empty();
        	$("#requestList").append("You have no new contact-requests");
          console.log(error);
        }
      );
	});		
	
	
	 // Accept Contact Request to create a contact
	 function acceptRequest(username) {
	        client.createContact(
	          username,
	          function(data,type) {
	            // Contact successfully created
	  			console.log(data);
	          },
	          function(error) {
	            // Error while creating contact
	            console.log(error);
	          }
	        );
	        $("#contactRequest"+username).remove();	        
	      }   
	 
	// Reject a Contact Request to delete the request
	 function rejectRequest(username) {
		 	var input = "{\"username\":\"" + username + "\"}";
	        client.deleteRequest(
	          input,
	          function(data,type) {
	            // Request successfully deleted
	  			console.log(data);
	          },
	          function(error) {
	            // Error while deleting request
	            console.log(error);
	          }
	        );
	        $("#contactRequest"+username).remove();
	      }   
	 
	 // Filter Search Function
	 
	 function searchFilter(searchInput) {
		var value = $(searchInput).val();
        $("#userList > li > a").each(function() {
            if ($(this).text().search(value) > -1) {
                $(this).show();
            }
            else {
                $(this).hide();
            }
        });		
	 }
	 
	 function addForm(text) {
		
		$('#chatMessageInput').val($('#chatMessageInput').val() + " " + text + " ");
	 }
    
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
	
    function getUsersList() {
      client.getUsers(
        function(data,type) {
          // this is the success callback
			for (var i = 0; i < data.length; i++) {
				// This block will be executed 100 times.
				$("#userList").append("<li>" + data[i].nickname + " ("  + data[i].username + ")</li>");
				// Note: The last log will be "Currently at 99".
			}		  
        },
        function(error) {
          // this is the error callback
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
	
	// Sends Contact Request
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
	
	// Sends Contact Request
	$( "#pendingRequestsButton" ).click(function() {
       	var input = "{\"username\":\"" + oidc_userinfo.preferred_username + "\"}";							
		client.getRequest(
		input,
        function(data,type) {
          // this is the success callback
			for (var i = 0; i < data.length; i++) {
				// This block will be executed 100 times.
				$("#requestList").append("<li>" + data[i].username + "</li>");
				// Note: The last log will be "Currently at 99".
			}		  		  
          console.log(data);
        },
        function(error) {
          // this is the error callback
          console.log(error);
        }
      );
	});								
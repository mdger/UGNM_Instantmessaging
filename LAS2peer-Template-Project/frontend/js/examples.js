    
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
		console.log(oidc_userinfo);
		$("#uname").html(oidc_userinfo.name);
		$("#email").html(oidc_userinfo.email);
		$("#sub").html(oidc_userinfo.sub);
		$(".authenticated").removeClass("hidden");
			  var content = "{\"email\":" + oidc_userinfo.email + ",\"telephone\": 00000000,\"imageLink\":\"imagelink\",\"nickname\":\"Chatter\",\"visible\":1}";
			  client.postProfile(
				content,
				function(data,type) {
				  // this is the success callback
				  console.log(data);
				  $("#getExampleOutput").html("yae");
				},
				function(error) {
				  // this is the error callback
				  console.log(error);
				  $("#getExampleOutput").html("nawo");
				}
			  );	
      } else {
        // anonymous
      }
    }
	
    function getProfileInfo() {
	var content = oidc_userinfo.preferred_username
      var profile_str = client.getProfile(
		content,
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
	  $("#profile_info").html("" + profile_str + "");
    }	
    
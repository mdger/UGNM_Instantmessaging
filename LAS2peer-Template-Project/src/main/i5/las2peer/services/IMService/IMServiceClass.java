package i5.las2peer.services.IMService;

import i5.las2peer.api.Service;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.ContentParam;
import i5.las2peer.restMapper.annotations.DELETE;
import i5.las2peer.restMapper.annotations.GET;
import i5.las2peer.restMapper.annotations.POST;
import i5.las2peer.restMapper.annotations.PUT;
import i5.las2peer.restMapper.annotations.Path;
import i5.las2peer.restMapper.annotations.PathParam;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.restMapper.tools.ValidationResult;
import i5.las2peer.restMapper.tools.XMLCheck;
import i5.las2peer.security.Context;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.IMService.database.DatabaseManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

/**
 * LAS2peer Service
 * 
 * This is a template for a very basic LAS2peer service
 * that uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 */
@Path("example")
@Version("0.1")
public class IMServiceClass extends Service {

	private String jdbcDriverClassName;
	private String jdbcLogin;
	private String jdbcPass;
	private String jdbcUrl;
	private String jdbcSchema;
	private DatabaseManager dbm;

	public IMServiceClass() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
		// instantiate a database manager to handle database connection pooling and credentials
		dbm = new DatabaseManager(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
	}

	/**
	 * Simple function to validate a user login.
	 * Basically it only serves as a "calling point" and does not really validate a user
	 * (since this is done previously by LAS2peer itself, the user does not reach this method
	 * if he or she is not authenticated).
	 * 
	 */
	@GET
	@Path("validate")
	public HttpResponse validateLogin() {
		String returnString = "";
		returnString += "You are " + ((UserAgent) getActiveAgent()).getLoginName() + " and your login is valid!";
		
		HttpResponse res = new HttpResponse(returnString);
		res.setStatus(200);
		return res;
	}

	/**
	 * Another example method.
	 * 
	 * @param myInput
	 * 
	 */
	@POST
	@Path("myMethodPath/{input}")
	public HttpResponse exampleMethod(@PathParam("input") String myInput) {
		String returnString = "";
		returnString += "You have entered " + myInput + "!";
		
		HttpResponse res = new HttpResponse(returnString);
		res.setStatus(200);
		return res;
		
	}

	/**
	 * Example method that shows how to retrieve a user email address from a database 
	 * and return an HTTP response including a JSON object.
	 * 
	 * WARNING: THIS METHOD IS ONLY FOR DEMONSTRATIONAL PURPOSES!!! 
	 * IT WILL REQUIRE RESPECTIVE DATABASE TABLES IN THE BACKEND, WHICH DON'T EXIST IN THE TEMPLATE.
	 * 
	 */
	@GET
	@Path("getUserEmail/{username}")
	public HttpResponse getUserEmail(@PathParam("username") String username) {
		String result = "";
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		try {
			// get connection from connection pool
			conn = dbm.getConnection();
			
			// prepare statement
			stmnt = conn.prepareStatement("SELECT email FROM users WHERE username = ?;");
			stmnt.setString(1, username);
			
			// retrieve result set
			rs = stmnt.executeQuery();
			
			// process result set
			if (rs.next()) {
				result = rs.getString(1);
				
				// setup resulting JSON Object
				JSONObject ro = new JSONObject();
				ro.put("email", result);

				// return HTTP Response on success
				HttpResponse r = new HttpResponse(ro.toJSONString());
				r.setStatus(200);
				return r;
				
			} else {
				result = "No result for username " + username;
				
				// return HTTP Response on error
				HttpResponse er = new HttpResponse(result);
				er.setStatus(404);
				return er;
			}
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
			return er;
		} finally {
			// free resources
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (stmnt != null) {
				try {
					stmnt.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
		}
	}

	/**
	 * Example method that shows how to change a user email address in a database.
	 * 
	 * WARNING: THIS METHOD IS ONLY FOR DEMONSTRATIONAL PURPOSES!!! 
	 * IT WILL REQUIRE RESPECTIVE DATABASE TABLES IN THE BACKEND, WHICH DON'T EXIST IN THE TEMPLATE.
	 * 
	 */
	@POST
	@Path("setUserEmail/{username}/{email}")
	public HttpResponse setUserEmail(@PathParam("username") String username, @PathParam("email") String email) {
		
		String result = "";
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		try {
			conn = dbm.getConnection();
			stmnt = conn.prepareStatement("UPDATE users SET email = ? WHERE username = ?;");
			stmnt.setString(1, email);
			stmnt.setString(2, username);
			int rows = stmnt.executeUpdate(); // same works for insert
			result = "Database updated. " + rows + " rows affected";
			
			// return 
			HttpResponse r = new HttpResponse(result);
			r.setStatus(200);
			return r;
			
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
			return er;
		} finally {
			// free resources if exception or not
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (stmnt != null) {
				try {
					stmnt.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
		}
	}
	
	/**
	 * Retrieve Profile
	 * Retrieves a profile 
	 * 
	 * @param UserName of the Profile to be Retrieved 
	 * @result Profile Data
	 */
	@GET
	@Path("profile/{name}")
	public HttpResponse retrieveProfil(@PathParam("name") String userName) {
		String[] result = new String[6];
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		try {
			// get connection from connection pool
			conn = dbm.getConnection();
			
			// prepare statement
			stmnt = conn.prepareStatement("SELECT EMail, Telephone, ImageLink, NickName, Visible FROM AccountProfile WHERE UserName = ?;");
			stmnt.setString(1, userName);
			
			// retrieve result set
			rs = stmnt.executeQuery();
			
			// process result set
			if (rs.next()) {
				for (int i=1; i<=5; i++) {
					result[i] = rs.getString(i);
				}
				
				// setup resulting JSON Object
				JSONObject ro = new JSONObject();
				ro.put("email", result[1]);
				ro.put("telephone", result[2]);
				ro.put("imageLink", result[3]);
				ro.put("nickname", result[4]);
				ro.put("visible", result[5]);
				
				// return HTTP Response on success
				HttpResponse r = new HttpResponse(ro.toJSONString());
				r.setStatus(200);
				return r;
				
			} else {
				result[0] = "No result for username " + userName;
				
				// return HTTP Response on error
				HttpResponse er = new HttpResponse(result[0]);
				er.setStatus(404);
				return er;
			}
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
			return er;
		} finally {
			// free resources
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (stmnt != null) {
				try {
					stmnt.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
		}
	}

	/**
	 * Update Profile	
	 * Updates a profile 
	 * 
	 * @param name UserName of the Profile to be updated 
	 * @param content Data for updating the Profile encoded as JSON-String
	 * @return Code if the sending was successfully
	 */
	@PUT
	@Path("profile/{name}")
	public HttpResponse updateProfile(@PathParam("name") String userName, @ContentParam String content) {		
		
		try 
		{
			// convert string content to JSON object 
			JSONObject profileObject = (JSONObject) JSONValue.parse(content);
			String mail = (String) profileObject.get("email");
			String tele = (String) profileObject.get("telephone");
			String image = (String) profileObject.get("imageLink");
			String visible = (String) profileObject.get("visible");				
		
			String result = "";
			Connection conn = null;
			PreparedStatement stmnt = null;
			ResultSet rs = null;
				
		
			try {
				conn = dbm.getConnection();
				stmnt = conn.prepareStatement("UPDATE AccountProfile SET EMail = ?, Telephone = ?, ImageLink = ?, Visible = ? WHERE UserName = ?;");
				stmnt.setString(1, mail);
				stmnt.setString(2, tele);
				stmnt.setString(3, image);
				stmnt.setString(4, visible);
				stmnt.setString(5, userName);
				int rows = stmnt.executeUpdate(); 
				result = "Database updated. " + rows + " rows affected";
				
				// return 
				HttpResponse r = new HttpResponse(result);
				r.setStatus(200);
				return r;
				
			} catch (Exception e) {
				// return HTTP Response on error
				HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
				er.setStatus(500);
				return er;
			} finally {
				// free resources if exception or not
				if (rs != null) {
					try {
						rs.close();
					} catch (Exception e) {
						Context.logError(this, e.getMessage());
						
						// return HTTP Response on error
						HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
						er.setStatus(500);
						return er;
					}
				}
				if (stmnt != null) {
					try {
						stmnt.close();
					} catch (Exception e) {
						Context.logError(this, e.getMessage());
						
						// return HTTP Response on error
						HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
						er.setStatus(500);
						return er;
					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (Exception e) {
						Context.logError(this, e.getMessage());
						
						// return HTTP Response on error
						HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
						er.setStatus(500);
						return er;
					}
				}
			}
		}
			catch (Exception e)
			{
				Context.logError(this, e.getMessage());
				
				// return HTTP Response on error
				HttpResponse er = new HttpResponse("Content data in invalid format: " + e.getMessage());
				er.setStatus(400);
				return er;
			}
	}


/**
 * Delete Profile 
 * Deletes a profile 
 * 
 *@param UserName of the Profile to be deleted.
 */
@DELETE
@Path("profile/{name}")
public HttpResponse deleteProfile(@PathParam("name") String userName) {
	
	String result = "";
	Connection conn = null;
	PreparedStatement stmnt = null;
	ResultSet rs = null;
	try {
		conn = dbm.getConnection();
		stmnt = conn.prepareStatement("DELETE FROM AccountProfile WHERE UserName = ?;");
		stmnt.setString(1, userName);
		int rows = stmnt.executeUpdate(); // same works for insert
		result = "Database updated. " + rows + " rows affected";
		
		// return 
		HttpResponse r = new HttpResponse(result);
		r.setStatus(200);
		return r;
		
	} catch (Exception e) {
		// return HTTP Response on error
		HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
		er.setStatus(500);
		return er;
	} finally {
		// free resources if exception or not
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception e) {
				Context.logError(this, e.getMessage());
				
				// return HTTP Response on error
				HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
				er.setStatus(500);
				return er;
			}
		}
		if (stmnt != null) {
			try {
				stmnt.close();
			} catch (Exception e) {
				Context.logError(this, e.getMessage());
				
				// return HTTP Response on error
				HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
				er.setStatus(500);
				return er;
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				Context.logError(this, e.getMessage());
				
				// return HTTP Response on error
				HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
				er.setStatus(500);
				return er;
			}
		}
	}
}


	
	/**
	 * This method returns the profile of an account. 
	 * @param username The name of the user which profile should be shown
	 * @return The data of the profile in the HTTP Response type 
	 */
	@GET
	@Path("message/single/{name}")
	public HttpResponse getSingleMessages(@PathParam("name") String userName)
	{
		String agentName = ((UserAgent) getActiveAgent()).getLoginName();
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		
		try 
		{
			// get connection from connection pool
			conn = dbm.getConnection();
			
			// prepare statement
			stmnt = conn.prepareStatement("SELECT Nessage, MessageTimeStamp, Sender FROM Message, SendingSingle WHERE (Sender = ? AND Receiver = ?) OR (Sender = ? AND Receiver = ?);");
			stmnt.setString(1, userName);
			stmnt.setString(2, agentName);
			stmnt.setString(3, agentName);
			stmnt.setString(4, userName);
			
			// prepare JSONArray
			JSONArray messageArray = new JSONArray();
			
			// retrieve result set
			rs = stmnt.executeQuery();
			boolean dataFound = false;
			// extract all the messages and put them first in a JSON object and after that in a list
			while (rs.next())
			{
				if(!dataFound) dataFound = true;
				JSONObject messageObject = new JSONObject();
				messageObject.put("text", rs.getString(1));
				messageObject.put("timestamp", rs.getString(2));
				messageObject.put("Sender", rs.getString(3));
				messageArray.add(messageObject);
			}
			
			if (dataFound)
			{
				// setup resulting JSON Object
				JSONObject jsonResult = new JSONObject();
				jsonResult.put("message", messageArray);
				
				// return HTTP response
				HttpResponse r = new HttpResponse(jsonResult.toJSONString());
				r.setStatus(200);
				return r;
			}
			else 
			{
				String error = "No messages found for contact " + userName + "!";
				
				// return HTTP Response on error
				HttpResponse er = new HttpResponse(error);
				er.setStatus(404);
				return er;
			}
		} 
		catch (Exception e) 
		{
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
			return er;
		} 
		finally 
		{
			// free resources
			if (rs != null) 
			{
				try 
				{
					rs.close();
				}
				catch (Exception e) 
				{
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (stmnt != null) 
			{
				try 
				{
					stmnt.close();
				}
				catch (Exception e) 
				{
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (conn != null) 
			{
				try 
				{
					conn.close();
				}
				catch (Exception e) 
				{
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
		}
	}

	@PUT
	@Path("message/single/{name}")
	/**
	 * This method sends a message from a user to a different user
	 * @param userName The name of the user who gets the message
	 * @param content The content of the message encoded as JSON string
	 * @return Code if the sending was successfully
	 */
	public HttpResponse sendSingleMessage(@PathParam("name") String userName, @ContentParam String content)
	{
		try 
		{
			// convert string content to JSON object to get the message content
			JSONObject messageObject = (JSONObject) JSONValue.parse(content);
			String message = (String) messageObject.get("message");
			String timeStamp = (String) messageObject.get("timestamp");
			
			String result = "";
			Connection conn = null;
			PreparedStatement stmnt = null;
			PreparedStatement stmnt1 = null;
			ResultSet rs = null;
			try {
				conn = dbm.getConnection();
				
				// insert the message in the message table
				stmnt = conn.prepareStatement("INSERT INTO Message (Message, MessageTimeStamp, WasRead) VALUES ('?', '?', ?);", Statement.RETURN_GENERATED_KEYS);
				stmnt.setString(1, message);
				stmnt.setString(2, timeStamp);
				stmnt.setInt(3, 0);
				int rows = stmnt.executeUpdate();
				result = "Database updated. " + rows + " rows in table \"Message\" affected";
				stmnt.getGeneratedKeys().next();
				
				// retrieve the message ID for saving it in the sending single table
				int messageID = stmnt.getGeneratedKeys().getInt(1);
				
				// insert the message ID, sender and receiver in the sending single table
				stmnt1 = conn.prepareStatement("INSERT INTO SendingSingle (Sender, Receiver, MessageID) VALUES ('?', '?', ?);");
				stmnt1.setString(1, ((UserAgent) getActiveAgent()).getLoginName());
				stmnt1.setString(2, userName);
				stmnt1.setInt(3, messageID);
				rows = stmnt1.executeUpdate();
				result += "\n\rDatabase updated. " + rows + " rows in table \"SendingSingle\" affected";
				
				// return 
				HttpResponse r = new HttpResponse(result);
				r.setStatus(200);
				return r;
				
			} catch (Exception e) {
				// return HTTP Response on error
				HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
				er.setStatus(500);
				return er;
			} finally {
				// free resources if exception or not
				if (rs != null) {
					try {
						rs.close();
					} catch (Exception e) {
						Context.logError(this, e.getMessage());
						
						// return HTTP Response on error
						HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
						er.setStatus(500);
						return er;
					}
				}
				if (stmnt != null) {
					try {
						stmnt.close();
					} catch (Exception e) {
						Context.logError(this, e.getMessage());
						
						// return HTTP Response on error
						HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
						er.setStatus(500);
						return er;
					}
				}
				if (stmnt1 != null) {
					try {
						stmnt1.close();
					} catch (Exception e) {
						Context.logError(this, e.getMessage());
						
						// return HTTP Response on error
						HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
						er.setStatus(500);
						return er;
					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (Exception e) {
						Context.logError(this, e.getMessage());
						
						// return HTTP Response on error
						HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
						er.setStatus(500);
						return er;
					}
				}
			}
		}
		catch (Exception e)
		{
			Context.logError(this, e.getMessage());
			
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Content data in invalid format: " + e.getMessage());
			er.setStatus(400);
			return er;
		}
	}
	
	/**
	 * This method returns the contact list of a certain user. 
	 * @param name The name of the user whose contact list should be displayed
	 * @return The data (username and nickname) of the contacts in the HTTP Response type 
	 */@GET
	 
	 
@Path("contact/{name}")
Public HttpResponse getContact(@PathParam("name") String name) {
	String result ="";
	Connection conn = null;
	PreparedStatement stmnt = null;
	ResultSet rs = null;
	try {
		// get connection from connection pool
		conn = dbm.getConnection();
		
		// prepare statement
		stmnt = conn.prepareStatement("select ap.UserName, ap.NickName from Contact as c, AccountProfile as ap
where c.This_UserName= ? AND c.Contact_UserName=ap.UserName;");
		stmnt.setString(1, name);
		stmnt.setString(2, name);
		
		//prepare JSONArray
		JSONArray contactArray = new JSONArray();
		
		// retrieve result set
		rs = stmnt.executeQuery();
		
		//differentiate situations 1) with contacts and 2) without contacts
		boolean dataFound = false;
		// process result set
		// extract all the messages and put them first in a JSON object and after that in a list
			while (rs.next())
			{
				if(!dataFound) dataFound = true;
				JSONObject contactObject = new JSONObject();
				contactObject.put("contact username", rs.getString(1));
				contactObject.put("contact nickname", rs.getString(2));
				contactArray.add(contactObject);
			}
			
			if (dataFound)
			{
				// setup resulting JSON Object
				JSONObject jsonResult = new JSONObject();
				jsonResult.put("contact list", contactArray);
				
				// return HTTP response
				HttpResponse r = new HttpResponse(jsonResult.toJSONString());
				r.setStatus(200);
				return r;
			}
			else 
			{
				result = "No contact list found for " + name + "!";
				
				// return HTTP Response on error
				HttpResponse er = new HttpResponse(result);
				er.setStatus(404);
				return er;
			}
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
			return er;
		} finally {
			// free resources
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (stmnt != null) {
				try {
					stmnt.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
		}
	}	
	
	/**
	 * Method for debugging purposes.
	 * Here the concept of restMapping validation is shown.
	 * It is important to check, if all annotations are correct and consistent.
	 * Otherwise the service will not be accessible by the WebConnector.
	 * Best to do it in the unit tests.
	 * To avoid being overlooked/ignored the method is implemented here and not in the test section.
	 * @return  true, if mapping correct
	 */
	public boolean debugMapping() {
		String XML_LOCATION = "./restMapping.xml";
		String xml = getRESTMapping();

		try {
			RESTMapper.writeFile(XML_LOCATION, xml);
		} catch (IOException e) {
			e.printStackTrace();
		}

		XMLCheck validator = new XMLCheck();
		ValidationResult result = validator.validate(xml);

		if (result.isValid())
			return true;
		return false;
	}

	/**
	 * This method is needed for every RESTful application in LAS2peer. There is no need to change!
	 * 
	 * @return the mapping
	 */
	public String getRESTMapping() {
		String result = "";
		try {
			result = RESTMapper.getMethodsAsXML(this.getClass());
		} catch (Exception e) {

			e.printStackTrace();
		}
		return result;
	}

}

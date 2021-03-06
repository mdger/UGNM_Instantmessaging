package i5.las2peer.services.IMService;

import i5.las2peer.api.Service;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.Consumes;
import i5.las2peer.restMapper.annotations.ContentParam;
import i5.las2peer.restMapper.annotations.DELETE;
import i5.las2peer.restMapper.annotations.GET;
import i5.las2peer.restMapper.annotations.POST;
import i5.las2peer.restMapper.annotations.PUT;
import i5.las2peer.restMapper.annotations.Path;
import i5.las2peer.restMapper.annotations.PathParam;
import i5.las2peer.restMapper.annotations.Produces;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

/**
 * IM Service
 * 
 * This is a IM Service for users to communicate with each other.
 * Enjoy it!
 * 
 */
@Path("im")
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
	 * Retrieves a profile 
	 * 
	 * @param userName - username of the profile to be retrieved 
	 * @return           profile data of the retrieved profile
	*/ 
	@GET
	@Path("profile/{username}")
	@Produces("application/json")
	public HttpResponse getProfile(@PathParam("username") String userName) {
		String agentName = ((UserAgent)getActiveAgent()).getLoginName();
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
			if(rs.next()) 
			{
				// is profile visible
				if(rs.getInt(5) == 1 || areContacts(userName, agentName) || userName.equals(agentName)) 
				{
					// setup resulting JSON Object
					JSONObject ro = new JSONObject();
					ro.put("email", rs.getString(1));
					ro.put("telephone", rs.getString(2));
					ro.put("imageLink", rs.getString(3));
					ro.put("nickname", rs.getString(4));
					ro.put("visible", rs.getInt(5));
					
					// return HTTP Response on success
					HttpResponse r = new HttpResponse(ro.toJSONString());
					r.setStatus(200);
					return r;
				}
				else
				{
					String error = "The profile of the user " + userName + " is not visible!";
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse(error);
					er.setStatus(401);
					return er;
				}	
			} 
			else 
			{
				String error = "No result for username " + userName;
				
				// return HTTP Response on error
				HttpResponse er = new HttpResponse(error);
				er.setStatus(404);
				return er;
			}	
		} catch (Exception e) 
		{
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
			return er;
		} finally 
		{
			// free resources
			HttpResponse response = freeRessources(conn, stmnt, rs);
			if(response.getStatus() != 200)
				return response;
		}
	}

	/**	
	 * Creates a Profile
	 * 
	 * @param  content - data for creating the profile encoded as JSON-String
	 * @return           code if the sending was successfully
	 */
	@POST
	@Path("profile")
	@Consumes("application/json")
	public HttpResponse createProfile(@ContentParam String content) {		
		try 
		{
			String agentName = ((UserAgent) getActiveAgent()).getLoginName();
			// convert string content to JSON object 
			JSONObject profileObject = (JSONObject) JSONValue.parse(content);
			String usrname = (String) profileObject.get("username");			
			String mail = (String) profileObject.get("email");
			int tele = (int) profileObject.get("telephone");
			String image = (String) profileObject.get("imageLink");
			String nickName = (String) profileObject.get("nickname");
			int visible = (int) profileObject.get("visible");				
		
			String result = "";
			Connection conn = null;
			PreparedStatement stmnt = null;
			
			try 
			{
				conn = dbm.getConnection();
				try 
				{	
					//Create Profile				
					stmnt = conn.prepareStatement("INSERT INTO AccountProfile (UserName, EMail, Telephone, ImageLink, NickName, Visible) VALUES (?, ?, ?, ?, ?, ?)");
					stmnt.setString(1, usrname);
					stmnt.setString(2, mail);
					stmnt.setInt(3, tele);
					stmnt.setString(4, image);
					stmnt.setString(5, nickName);
					stmnt.setInt(6, visible);
					
					int rows = stmnt.executeUpdate();
					if(rows == 1)
						result = "Profile created successfully!";
					else
					{
						result = "Profile could not be created!";
						// return HTTP Response on error
						HttpResponse er = new HttpResponse(result);
						er.setStatus(409);
						return er;
					}					
					
				} catch (SQLException e) 
				{
					if (e.getErrorCode() == 1062) {
						HttpResponse er = new HttpResponse("The profile already exists!");					
						er.setStatus(409);
						return er;
					}
					else 
					{
						HttpResponse er = new HttpResponse("Internal Error");					
						er.setStatus(500);
					}
				}
				
				// Return Result
				HttpResponse r = new HttpResponse(result);
				r.setStatus(200);
				return r;

					
			} catch (Exception e) 
			{
				// return HTTP Response on error
				HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
				er.setStatus(500);
				return er;
			} finally 
			{
				// free resources
				HttpResponse response = freeRessources(conn, stmnt);
				if(response.getStatus() != 200)
					return response;
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
	 * Updates a profile 
	 * 
	 * @param content - data for updating the profile encoded as JSON-String
	 * @return          Code if the sending was successfully
	*/
	@PUT
	@Path("profile")
	@Consumes("application/json")
	public HttpResponse updateProfile(@ContentParam String content) {		
		try 
		{
			// String agentName = ((UserAgent) getActiveAgent()).getLoginName();
			// convert string content to JSON object 
			JSONObject profileObject = (JSONObject) JSONValue.parse(content);
			String agentName = (String) profileObject.get("userName");
			String mail = (String) profileObject.get("email");
			String tele = (String) profileObject.get("telephone");
			String image = (String) profileObject.get("imageLink");
			String nickName = (String) profileObject.get("nickname");
			int visible = (int) profileObject.get("visible");				
		
			String result = "";
			Connection conn = null;
			PreparedStatement stmnt = null;
			ResultSet rs = null;
				
			
			try {
					conn = dbm.getConnection();
					stmnt = conn.prepareStatement("UPDATE AccountProfile SET EMail = ?, Telephone = ?, ImageLink = ?, NickName = ?, Visible = ? WHERE UserName = ?;");
					stmnt.setString(1, mail);
					stmnt.setString(2, tele);
					stmnt.setString(3, image);
					stmnt.setString(4, nickName);
					stmnt.setInt(5, visible);
					stmnt.setString(6,agentName);
					int rows = stmnt.executeUpdate();
					if(rows == 1)
						result = "Profile updated successfully";
					else
					{
						result = "Profile was not found";
						// return HTTP Response on error
						HttpResponse er = new HttpResponse(result);
						er.setStatus(409);
						return er;
					}
					
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
				// free resources
				HttpResponse response = freeRessources(conn, stmnt, rs);
				if(response.getStatus() != 200)
					return response;
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
	* Deletes a profile  
	* 
	*
	*/ 
		@DELETE
		@Path("profile")
		public HttpResponse deleteProfile() {
			String agentName = ((UserAgent) getActiveAgent()).getLoginName();
			String result = "";
			Connection conn = null;
			PreparedStatement stmnt = null;
			ResultSet rs = null;
			try {
					conn = dbm.getConnection();
					stmnt = conn.prepareStatement("DELETE FROM AccountProfile WHERE UserName = ?;");
					stmnt.setString(1, agentName);
					int rows = stmnt.executeUpdate(); 
					if(rows == 1)
						result = "Profile deleted successfully!";
					else
					{
						result = "Resource was not found";
						// return HTTP Response on error
						HttpResponse er = new HttpResponse(result);
						er.setStatus(404);
						return er;
				}
				
					// return 
					HttpResponse r = new HttpResponse(result);
					r.setStatus(200);
					return r;
	
			}
			catch (Exception e)
			{
				Context.logError(this, e.getMessage());
				
				HttpResponse er = new HttpResponse("Content data in invalid format: " + e.getMessage());
				er.setStatus(400);
	 			return er;
	 			
			} finally {
				// free resources
				HttpResponse response = freeRessources(conn, stmnt, rs);
				if(response.getStatus() != 200)
					return response;
	 		}
	 	}
		
	
	
	/**
	 * Retrieves a group given its name
	 * 
	 * @param groupName - information of a group to be retrieved 
	 * @return            group data
	*/ 
	@GET
	@Path("group/{groupname}")
	@Produces("application/json")
	public HttpResponse getGroup(@PathParam("groupname") String groupName) {
		Connection conn = null;
		Connection conn1 = null;
		PreparedStatement stmnt = null;
		PreparedStatement stmnt1 = null; 
		ResultSet rs = null;
		ResultSet rs1 = null;
		
		try {
			// get connection from connection pool
			conn = dbm.getConnection();
			conn1 = dbm.getConnection();
			// prepare statement for the group
			stmnt = conn.prepareStatement("SELECT GroupName, FounderName, Description, ImageLink FROM Groups WHERE GroupName = ?;");
			stmnt.setString(1, groupName);
			// retrieve result set for the group
			rs = stmnt.executeQuery();
			// process result set
			String agentName = ((UserAgent) getActiveAgent()).getLoginName();
			if (rs.next()) 
			{
				if(rs.getString(2).equals(agentName) || isMemberOf(agentName, groupName))
				{
					// setup resulting JSON Object
					JSONObject ro = new JSONObject();
					ro.put("name", rs.getString(1));
					ro.put("founder", rs.getString(2));
					ro.put("description", rs.getString(3));
					ro.put("imageLink", rs.getString(4));
					// prepare statement for the members
					
					stmnt1 = conn1.prepareStatement("SELECT UserName FROM MemberOf WHERE GroupName = ?;");
					stmnt1.setString(1, groupName);
					
					// retrieve result set for the members
					rs1 = stmnt1.executeQuery();
					
					JSONArray memberArray = new JSONArray();
						
					// add the members in an array
					while(rs1.next())
					{
						JSONObject member = new JSONObject();
						member.put("username", rs1.getString(1));
						memberArray.add(member);
					}
					// put the members in the JSON object 
					ro.put("member", memberArray);
					
					// return HTTP Response on success
					HttpResponse r = new HttpResponse(ro.toJSONString());
					r.setStatus(200);
					return r;	
				}
				else
				{
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Access denied. User is no member of this group");
					er.setStatus(403);
					return er;
				}
			} 
			else 
			{
				// return HTTP Response on error
				HttpResponse er = new HttpResponse("Group does not exist: " + groupName);
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
			HttpResponse response = freeRessources(conn, stmnt, rs);
			if(response.getStatus() != 200)
				return response;
			HttpResponse response1 = freeRessources(conn1, stmnt1, rs1);
			if(response1.getStatus() != 200)
				return response1;
		}
	}

	/**	
	 * Creates a Group
	 * 
	 * @param groupName - name of group to be created
	 * @param content   - data for creating the group encoded as JSON-String
	 * @return            code if the sending was successfully
	 * 
	*/ 
	@POST
	@Path("group/{groupname}")
	@Consumes("application/json")
	public HttpResponse createGroup(@PathParam("groupname") String groupName, @ContentParam String content) {		
		try 
		{
			// convert string content to JSON object 
			JSONObject profileObject = (JSONObject) JSONValue.parse(content);
			String founder = ((UserAgent) getActiveAgent()).getLoginName();
			String desc = (String) profileObject.get("description");
			String img = (String) profileObject.get("imagelink");
		
			String result = "";
			Connection conn = null;
			PreparedStatement stmnt = null;
			
			try {
				conn = dbm.getConnection();
				
				try {	
			
					//Create Profile				
					stmnt = conn.prepareStatement("INSERT INTO Groups (GroupName, FounderName, Description, ImageLink) VALUES (?, ?, ?, ?)");
					stmnt.setString(1, groupName);
					stmnt.setString(2, founder);
					stmnt.setString(3, desc);
					stmnt.setString(4, img);
					
					int rows = stmnt.executeUpdate();
					if(rows == 1)
						result = "The Group was created successfully";
					else
					{
						// return HTTP Response on error
						HttpResponse er = new HttpResponse("The Group could not be created!");
						er.setStatus(409);
						return er;
					}					
					
				} catch (SQLException e) {
					if (e.getErrorCode() == 1022) {
						HttpResponse er = new HttpResponse("The Group with the following name " + groupName + " already exists!");					
						er.setStatus(409);
						return er;
					}
				}
				
				addMember(groupName, founder);
				// Return Result
				HttpResponse r = new HttpResponse(result);
				r.setStatus(200);
				return r;

					
			} catch (Exception e) {
				// return HTTP Response on error
				HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
				er.setStatus(500);
				return er;
			} finally {
				// free resources
				HttpResponse response = freeRessources(conn, stmnt);
				if(response.getStatus() != 200)
					return response;
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
	 * Updates a Group  
	 * 
	 * @param groupName - name of group to be updated 
	 * @param content -   data for updating the profile encoded as JSON-String
	 * @return            code if the sending was successfully
	 * 
	*/ 
	@PUT
	@Path("group/{groupname}")
	@Consumes("application/json")
	public HttpResponse updateGroup(@PathParam("groupname") String groupName, @ContentParam String content) {		
		try 
		{
			// convert string content to JSON object 
			JSONObject profileObject = (JSONObject) JSONValue.parse(content);
			String founder = ((UserAgent) getActiveAgent()).getLoginName();
			String desc = (String) profileObject.get("description");
			String iLink = (String) profileObject.get("imagelink");		
		
			String result = "";
			Connection conn = null;
			PreparedStatement stmnt = null;
			ResultSet rs = null;
			
			try
			{
				conn = dbm.getConnection();
				stmnt = conn.prepareStatement("SELECT FounderName FROM Groups WHERE GroupName = ?;");
				stmnt.setString(1, groupName);
				rs = stmnt.executeQuery();
				if(rs.next())
				{
					if(!founder.equals(rs.getString(1)))
					{
						HttpResponse er = new HttpResponse("Groups can only be changed by the founder!");
						er.setStatus(403);
						return er;
					}
				}
				else
				{
					HttpResponse er = new HttpResponse("Group does not exist!");
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
				HttpResponse response = freeRessources(conn, stmnt, rs);
				if(response.getStatus() != 200)
					return response;
			}
			
			try {
				conn = dbm.getConnection();
				stmnt = conn.prepareStatement("UPDATE Groups SET GroupName = ?, Description = ?, ImageLink = ? WHERE GroupName = ?;");
				stmnt.setString(1, groupName);
				stmnt.setString(2, desc);
				stmnt.setString(3, iLink);
				stmnt.setString(4, groupName);
				int rows = stmnt.executeUpdate(); 
				if(rows == 1)
					result = "Group updated successfully";
				else
				{
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Resource does not exist!");
					er.setStatus(404);
					return er;
				}
				
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
				// free resources
				HttpResponse response = freeRessources(conn, stmnt, rs);
				if(response.getStatus() != 200)
					return response;
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
	 * Deletes a group 
	 * 
	 *@param groupName - name of group to be deleted.
	 *@return            code if sending was successfully
	*/
	@DELETE
	@Path("group/{groupname}")
	public HttpResponse deleteGroup(@PathParam("groupname") String groupName) {
		Connection conn = null;
		PreparedStatement stmnt = null;
		PreparedStatement stmnt1 = null;
		PreparedStatement stmnt2 = null;
		PreparedStatement stmnt3 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
			
		try {
			conn = dbm.getConnection();
			stmnt = conn.prepareStatement("SELECT FounderName FROM Groups WHERE GroupName = ?");
			stmnt.setString(1, groupName);
			rs = stmnt.executeQuery();
			String founder = "";
			if(rs.next())
				founder = rs.getString(1);
			else
			{
				HttpResponse r = new HttpResponse("Resource does not exist!");
				r.setStatus(404);
				return r;
			}
			
			if(((UserAgent)getActiveAgent()).getLoginName().equals(founder))
			{
				stmnt2 = conn.prepareStatement("SELECT UserName FROM MemberOf WHERE GroupName = ?;");
				stmnt2.setString(1, groupName);
				rs1 = stmnt.executeQuery();
				while(rs1.next())
				{
					stmnt3 = conn.prepareStatement("DELETE FROM MemberOf WHERE GroupName = ? AND UserName = ?;");
					stmnt3.setString(1, groupName);
					stmnt3.setString(2, rs1.getString(1));
					stmnt3.executeUpdate();
				}
				stmnt1 = conn.prepareStatement("DELETE FROM Groups WHERE GroupName = ?;");
				stmnt1.setString(1, groupName);
				stmnt1.executeUpdate(); 
				// return 
				HttpResponse r = new HttpResponse("Group deleted successfully");
				r.setStatus(200);
				return r;
			}
			else
			{
				// return HTTP Response on error
				HttpResponse er = new HttpResponse("You are not authorized to delete the group " + groupName + "!");
				er.setStatus(403);
				return er;
			}
			
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
			return er;
		} finally {
			// free resources
			HttpResponse response = freeRessources(conn, stmnt, rs);
			if(response.getStatus() != 200)
				return response;
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
		}
	}
	
	/**
	 * This method returns the contact list of a certain user. 
	 * 
	 * @return The data (username and nickname) of the contacts in the HTTP Response type 
	 * 
	*/ 
	@GET	 
	@Path("profile/contact")
	@Produces("application/json")
	public HttpResponse getContacts() {
		String agentName = ((UserAgent) getActiveAgent()).getLoginName();
		String result ="";
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
			try {		
				// get connection from connection pool
				conn = dbm.getConnection();
				
				// prepare statement
				stmnt = conn.prepareStatement("SELECT UserName, NickName from Contact, AccountProfile WHERE (FirstUser = ? AND SecondUser = UserName) OR (SecondUser = ? AND FirstUser = UserName);");
				stmnt.setString(1, agentName);
				stmnt.setString(2, agentName);
				
				//prepare JSONArray
				JSONArray contactArray = new JSONArray();
				
				// retrieve result set
				rs = stmnt.executeQuery();
				
				//differentiate situations 1) with contacts and 2) without contacts
				boolean dataFound = false;
				// process result set
				// extract all the contacts and put them first in a JSON object and after that in a list
				while (rs.next())
				{
					if(!dataFound) dataFound = true;
					JSONObject contactObject = new JSONObject();
					contactObject.put("username", rs.getString(1));
					contactObject.put("nickname", rs.getString(2));
					contactArray.add(contactObject);
				}
				
				if (dataFound)
				{
					// return HTTP response
					HttpResponse r = new HttpResponse(contactArray.toJSONString());
					r.setStatus(200);
					return r;
				}
				else 
				{
					result = "No contact list found for " + agentName + "!";
					
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
				HttpResponse response = freeRessources(conn, stmnt, rs);
				if(response.getStatus() != 200)
					return response;
			}
		}

	
	/**
	 * This method accept a contact-request the active user got from an different user
	 * 
	 * @param userName - username of the user requested
	 * @return           code if the sending was successfully
	 * 
	*/ 
	@POST
	@Path("profile/contact/{user}")
	public HttpResponse createContact(@PathParam("user") String userName)
	{	    
	    String result = "";
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;					
		
		try {
		  
		    String agentName = (String) ((UserAgent) getActiveAgent()).getLoginName();
			
			//Exists a Open Request?
			if(!(existsOpenRequest(userName, agentName))) {
				HttpResponse er = new HttpResponse(agentName + " was not requested from " + userName);
				er.setStatus(400);
				return er;
				
			} else {			
				
				conn = dbm.getConnection();
					
				// Insert Contact into Contact Table
				stmnt = conn.prepareStatement("INSERT INTO Contact (FirstUser, SecondUser) VALUES (?, ?);");				
				stmnt.setString(1, userName);
				stmnt.setString(2, agentName);
					
				int rows = stmnt.executeUpdate();
				if (rows > 0) {
					result = "Contacts updated. " + userName + " and " + agentName + " are Contacts now";
						
					//Delete Request from Request Table
					deleteRequest("{\"username\":\"" + userName + "\"}");
						
					// return 
					HttpResponse r = new HttpResponse(result);
					r.setStatus(200);
					return r;
					
				} else {					
				  HttpResponse r = new HttpResponse("The database could not be updated");
				  r.setStatus(500);
				  return r;
						
				}
			}
				
		} catch (Exception e) {			
		  // return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
			return er;
			
		} finally {		  
			// free resources
			HttpResponse response = freeRessources(conn, stmnt, rs);
			if(response.getStatus() != 200)
				return response;
			}
		}		

	
	/**
	 * This method deletes a contact from the contact list
	 * @param userName - name of the contact which should be deleted
	 * @return           code if deleting was successfully
	 * 
	*/
	@DELETE 
	@Path("profile/contact/{user}")
	public HttpResponse deleteContact(@PathParam("user") String userName) {
		
	    String result ="";
		String agentName = ((UserAgent)getActiveAgent()).getLoginName();
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		
		
			try {
				// get connection from connection pool
				conn = dbm.getConnection();
				
				// prepare statement
				stmnt = conn.prepareStatement("DELETE FROM Contact WHERE (FirstUser = ? AND SecondUser = ?) OR (FirstUser = ? AND SecondUser = ?);");
				stmnt.setString(1, userName);
				stmnt.setString(2, agentName);
				stmnt.setString(3, agentName);
				stmnt.setString(4, userName);
				int rows = stmnt.executeUpdate();
				if(rows ==1)
				{
					result = "Contact deleted succesfully!";
					//return
					HttpResponse r = new HttpResponse(result);
					r.setStatus(200);
					return r;
				}
				else
				{
					result = "Contact could not be deleted!";
					//return
					HttpResponse r = new HttpResponse(result);
					r.setStatus(404);
					return r;
				}
			} catch (Exception e) {
				// return HTTP Response on error
				HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
				er.setStatus(500);
				return er;
			} finally {
				// free resources
				HttpResponse response = freeRessources(conn, stmnt, rs);
				if(response.getStatus() != 200)
					return response;
			}		
	}
	
	
	
	/**
	 * This method returns messages which were send to an account. 
	 * @param userName - name of the user who got the messages
	 * @return           the messages for the user as HTTP Response type 
	*/ 
	@GET
	@Produces("application/json")
	@Path("message/single/{username}")
	public HttpResponse getSingleMessages(@PathParam("username") String userName)
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
			stmnt = conn.prepareStatement("SELECT Message.MessageID, Message, MessageTimeStamp, Sender, NickName FROM Message, SendingSingle, AccountProfile WHERE ((Sender = ? AND Receiver = ?) OR (Sender = ? AND Receiver = ?)) AND Message.MessageID = SendingSingle.MessageID AND SendingSingle.Sender = AccountProfile.UserName;");
			stmnt.setString(1, userName);
			stmnt.setString(2, agentName);
			stmnt.setString(3, agentName);
			stmnt.setString(4, userName);
			
			// prepare JSONArray
			JSONArray messageArray = new JSONArray();
			JSONArray sortedMessageArray = new JSONArray();
			List<JSONObject> jsonValues = new ArrayList<JSONObject>();
						
			// retrieve result set
			rs = stmnt.executeQuery();
			boolean dataFound = false;
			// extract all the messages and put them first in a JSON object and after that in a list
			while (rs.next())
			{
				if(!dataFound) dataFound = true;
				JSONObject messageObject = new JSONObject();
				messageObject.put("senderNickName", rs.getString(5));
				messageObject.put("sender", rs.getString(4));
				messageObject.put("timestamp", rs.getString(3));
				messageObject.put("text", rs.getString(2));
				messageObject.put("messageID", rs.getString(1));

				messageArray.add(messageObject);				
				jsonValues.add(messageObject);			
				
			}
			
			if (dataFound)
			{    
			    
			  //Sort Array
			  Collections.sort(jsonValues, new Comparator<JSONObject>() {
			      
			      private static final String KEY_NAME = "timestamp";

			        @Override
			        public int compare(JSONObject a, JSONObject b) {
			            String valA = new String();
			            String valB = new String();
			            
			            valA = (String) a.get(KEY_NAME);
			            valB = (String) b.get(KEY_NAME);			           

			            return valA.compareTo(valB);
			        }
			    });
			    
			    //Sorted ArrayList to Message Array
			    for (int i = 0; i < jsonValues.size(); i++) {
		          sortedMessageArray.add(jsonValues.get(i));
		        }
			  
			    //create JSONObject with user of conversation and messeages array
                JSONObject jsonResult = new JSONObject();
                jsonResult.put("contact", userName);
                jsonResult.put("messages", sortedMessageArray);
			    
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
			HttpResponse response = freeRessources(conn, stmnt, rs);
			if(response.getStatus() != 200)
				return response;
		}
	}
	
	/**
   	 * Deletes a message 
 	 * 
 	 * @param content - the id of the message in a JSON string
 	 * @return          code if deleting was successfully  
 	 * 
 	*/ 
	@DELETE
	@Consumes("application/json")
	@Path("message/single")
	public HttpResponse deleteMessage(@ContentParam String content) 
	{
		try
		{
			// convert string content to JSON object to get the message content
			JSONObject messageObject = (JSONObject) JSONValue.parse(content);
			int messageID = (int) messageObject.get("messageID");		
			
			String result = "";
			Connection conn = null;
			PreparedStatement stmnt = null;
			PreparedStatement stmnt1 = null;
			try 
			{
					conn = dbm.getConnection();
					stmnt = conn.prepareStatement("DELETE FROM SendingSingle WHERE MessageID = ?;");
					stmnt.setInt(1, messageID);
					stmnt1 = conn.prepareStatement("DELETE FROM Message WHERE MessageID = ?;");
					stmnt1.setInt(1, messageID);
					int rows = stmnt.executeUpdate();
					rows += stmnt1.executeUpdate();
					if(rows > 1)
						result = "Messages deleted successfully!";
					else
					{
						result = "No Message was found";
						// return HTTP Response on error
						HttpResponse er = new HttpResponse(result);
						er.setStatus(404);
						return er;
					}
					
					// return 
					HttpResponse r = new HttpResponse(result);
					r.setStatus(200);
					return r;
	
			} catch (Exception e) 
			{
				// return HTTP Response on error
				HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
				er.setStatus(500);
				return er;
			} finally 
			{
				// free resources
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
				HttpResponse response = freeRessources(conn, stmnt);
				if(response.getStatus() != 200)
					return response;
			}
		} catch (Exception e)
		{
			Context.logError(this, e.getMessage());
			
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Content data in invalid format: " + e.getMessage());
			er.setStatus(400);
			return er;
		}
	}

	/**
	 * This method sends a message from a user to a different user
	 * @param userName - the name of the user who gets the message
	 * @param content  - the content of the message encoded as JSON string
	 * @return           code if the sending was successfully
	 **/
	 
	@POST
	@Path("message/single/{username}")
	@Consumes("application/json")
	public HttpResponse sendSingleMessage(@PathParam("username") String userName, @ContentParam String content)
	{
		try 
		{
			// convert string content to JSON object to get the message content
			JSONObject messageObject = (JSONObject) JSONValue.parse(content);
			String message = (String) messageObject.get("message");
						
			String result = "";
			Connection conn = null;
			PreparedStatement stmnt = null;
			PreparedStatement stmnt1 = null;
			ResultSet rs = null;
			
			int messageID = -1;
			try 
			{
				conn = dbm.getConnection();
				
				// insert the message in the message table
				stmnt = conn.prepareStatement("INSERT INTO Message (Message, WasRead) VALUES (?, ?);", Statement.RETURN_GENERATED_KEYS);
				stmnt.setString(1, message);
				stmnt.setInt(2, 0);
				stmnt.executeUpdate();
				// retrieve the message ID for saving it in the sending single table
				rs = stmnt.getGeneratedKeys();
				if(rs.next())
					messageID = rs.getInt(1);
				else
				{
					HttpResponse er = new HttpResponse("Internal error. Database could not be updated!");
					er.setStatus(500);
					return er;
				}
				// insert the message ID, sender and receiver in the sending single table
				stmnt1 = conn.prepareStatement("INSERT INTO SendingSingle (Sender, Receiver, MessageID) VALUES (?, ?, ?);");
				stmnt1.setString(1, ((UserAgent) getActiveAgent()).getLoginName());
				stmnt1.setString(2, userName);
				stmnt1.setInt(3, messageID);
				stmnt1.executeUpdate();
				result = "Message to " + userName + " was sent!";
				
				// return 
				HttpResponse r = new HttpResponse(result);
				r.setStatus(200);
				return r;
				
			} catch (Exception e) 
			{
				// return HTTP Response on error
				HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
				er.setStatus(500);
				return er;
			} finally 
			{
				// free resources
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
				HttpResponse response = freeRessources(conn, stmnt, rs);
				if(response.getStatus() != 200)
					return response;
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
	 * This method returns messages which are unread for an account. 
	 * @return The unread messages for the user as HTTP Response type 
	*/ 
	@GET
	@Produces("application/json")
	@Path("message/single/unread")
	public HttpResponse getUnreadMessages()
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
			stmnt = conn.prepareStatement("SELECT Message.MessageID, Message, MessageTimeStamp, Sender FROM Message, SendingSingle "
					+ "WHERE (Sender = ? OR Receiver = ?) AND Message.MessageID = SendingSingle.MessageID AND WasRead = 0;");
			stmnt.setString(1, agentName);
			stmnt.setString(2, agentName);
			
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
				messageObject.put("sender", rs.getString(4));
				messageObject.put("timestamp", rs.getString(3));
				messageObject.put("text", rs.getString(2));
				messageObject.put("messageID", rs.getString(1));
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
				String error = "No unread messages found for contact " + agentName + "!";
				
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
			HttpResponse response = freeRessources(conn, stmnt, rs);
			if(response.getStatus() != 200)
				return response;
		}
	}
	
	/**
	 * This method updates the status of a message from unread to read
	 * @param content - the content of the message encoded as JSON string
	 * @return          code if the sending was successfully
	 **/
	 
	@PUT
	@Consumes("application/json")
	@Path("message/single/unread")
	public HttpResponse updateReadSingleMessage(@ContentParam String content)
	{
		try 
		{
			// convert string content to JSON object to get the message content
			JSONObject messageObject = (JSONObject) JSONValue.parse(content);
			int messageID = (int) messageObject.get("messageID");
			
			String result = "";
			Connection conn = null;
			PreparedStatement stmnt = null;
			
			try 
			{
				conn = dbm.getConnection();
				
				// insert the message in the message table
				stmnt = conn.prepareStatement("UPDATE Message SET WasRead = 1 WHERE MessageID = ?");
				stmnt.setInt(1, messageID);
				stmnt.executeUpdate();
				result = "Message was set to read!";
				
				// return 
				HttpResponse r = new HttpResponse(result);
				r.setStatus(200);
				return r;
				
			} catch (Exception e) 
			{
				// return HTTP Response on error
				HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
				er.setStatus(500);
				return er;
			} finally 
			{
				// free resources
				HttpResponse response = freeRessources(conn, stmnt);
				if(response.getStatus() != 200)
					return response;
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
	 * This method returns the messages in a group. 
	 * 
	 * @param groupName - name of the group in which the messages are sent
	 * @return            the messages sent in a group as JSON String 
	 * 
	**/
	@GET
	@Path("message/group/{groupname}")
	@Produces("application/json")
	public HttpResponse getGroupMessages(@PathParam("groupname") String groupName)
	{
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		
		try 
		{
			
			String userName = ((UserAgent) getActiveAgent()).getLoginName();
									
			//Is Member of Group?
			if(!isMemberOf(userName, groupName)) {				
				HttpResponse er = new HttpResponse("Access denied. No Group Member");
				er.setStatus(403);
				return er;				
			}
			
			//Get GroupMessages from Database
			conn = dbm.getConnection();

			stmnt = conn.prepareStatement("SELECT Message, MessageTimeStamp, Sender FROM Message, SendingGroup WHERE Receiver = ? AND Message.MessageID = SendingGroup.MessageID;");
			stmnt.setString(1, groupName);
		
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
				messageObject.put("sender", rs.getString(3));
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
				// return HTTP Response No Messages was found
				HttpResponse er = new HttpResponse("No messages found for contact " + groupName + "!");
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
			HttpResponse response = freeRessources(conn, stmnt, rs);
			if(response.getStatus() != 200)
				return response;
		}
	}
	
	
	
	/** This method sends a message to a group
	 * 
	 * @param groupName - name of the group the message will be sent
	 * @param content   - content of the message encoded as JSON string
	 * @return            code if the sending was successfully
	 * 
	**/	
	@PUT
	@Path("message/group/{groupname}")
	@Consumes("application/json")
	public HttpResponse sendGroupMessage(@PathParam("groupname") String groupName, @ContentParam String content)
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
			
			int messageID = -1;
			String userName = ((UserAgent) getActiveAgent()).getLoginName();
			
			//Is Member of Group?
			if(!isMemberOf(userName, groupName)) {				
				HttpResponse er = new HttpResponse("Access denied. No Group Member");
				er.setStatus(403);
				return er;				
			}
			
			
			try {
				conn = dbm.getConnection();
				
				// insert the message in the message table
				stmnt = conn.prepareStatement("INSERT INTO Message (Message, MessageTimeStamp, WasRead) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
				stmnt.setString(1, message);
				stmnt.setString(2, timeStamp);
				stmnt.setInt(3, 0);
				stmnt.executeUpdate();
				result = "Message was created";
				
				// retrieve the message ID for saving it in the sending single table
				rs = stmnt.getGeneratedKeys();
				if(rs.next())
					messageID = rs.getInt(1);
				else
				{
					HttpResponse er = new HttpResponse("Internal error. Database could not be updated!");
					er.setStatus(500);
					return er;
				}
				
				// insert the message ID, sender and receiver in the sending group table
				stmnt1 = conn.prepareStatement("INSERT INTO SendingGroup (Sender, Receiver, MessageID) VALUES (?, ?, ?);");
				stmnt1.setString(1, userName);
				stmnt1.setString(2, groupName);
				stmnt1.setInt(3, messageID);
				stmnt1.executeUpdate();
				result += "\n\rMessage was sent";
				
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
		 * This method returns all contact requests to a certain user.
		 *  
		 * @return the data (username and nickname) of the user who has sent a contact request in the HTTP Response type 
		 * 
	*/
	@GET
	@Path("profile/contact/request")
	@Produces("application/json")
	public HttpResponse getRequests() {
		String agentName = ((UserAgent) getActiveAgent()).getLoginName();		
		
		String result ="";
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		try {
			// get connection from connection pool
			conn = dbm.getConnection();
			
			// prepare statement
			stmnt = conn.prepareStatement("select ap.UserName, ap.NickName from ContactRequest as cr, AccountProfile as ap where cr.Receiver= ? AND cr.Sender=ap.UserName;");
			stmnt.setString(1, agentName);
			
			//prepare JSONArray
			JSONArray contactArray = new JSONArray();
			
			// retrieve result set
			rs = stmnt.executeQuery();
			
			//differentiate situations 1) with contacts and 2) without contacts
			boolean dataFound = false;
			// process result set
			// extract all the requests and put them first in a JSON object and after that in a list
				while (rs.next())
				{
					if(!dataFound) dataFound = true;
					JSONObject contactObject = new JSONObject();
					contactObject.put("username", rs.getString(1));
					contactObject.put("nickname", rs.getString(2));
					contactArray.add(contactObject);
				}
				
				if (dataFound)
				{					
					// return HTTP response
					HttpResponse r = new HttpResponse(contactArray.toJSONString());
					r.setStatus(200);
					return r;
				}
				else 
				{
					result = "No requests found for " + agentName + "!";
					
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
			HttpResponse response = freeRessources(conn, stmnt, rs);
			if(response.getStatus() != 200)
				return response;
		}
	}
	

/**
 * This method sends a contact-request from the active user to a different user
 * 
 * @param content - the content of the message encoded as JSON string
 * @return          code if the sending was successfully
 * 
 */	
@POST
@Path("profile/contact/request")
@Consumes("application/json")
public HttpResponse createRequest(@ContentParam String content)
{
	try 
	{
		String result = "";
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;		
		
		JSONObject contentObject = (JSONObject) JSONValue.parse(content);
		String requestUserName = (String) contentObject.get("username");
		String activeUserName = (String) contentObject.get("myusername");;
		
		//Already Contacts?
		if (areContacts(activeUserName, requestUserName)) {			
			HttpResponse er = new HttpResponse( activeUserName + " and " + requestUserName + " are contacts already");
			er.setStatus(400);			
			return er;
			
		} else {
			
			//Already A Request was send?
			if (existsOpenRequest(activeUserName, requestUserName)) {			
				HttpResponse er = new HttpResponse("A Request already have been send");
				er.setStatus(400);			
				return er;
				
			} else {
			
		
				try {
					conn = dbm.getConnection();
					
					// insert the request into the request table
					stmnt = conn.prepareStatement("INSERT INTO ContactRequest (Sender, Receiver) VALUES (?, ?);");			
					stmnt.setString(1, activeUserName);
					stmnt.setString(2, requestUserName);
					int rows = stmnt.executeUpdate();
					
					//return
					HttpResponse r = new HttpResponse(result);
					if (rows > 0) {
						result = "Contact Request was sended succesfully";				
						r.setStatus(200);
					} else {
						result = "Contact Request could not be sended";				
						r.setStatus(409);		
					}			
					return r;
					
				} catch (Exception e) {
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				} finally {
					// free resources
					HttpResponse response = freeRessources(conn, stmnt, rs);
					if(response.getStatus() != 200)
						return response;
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
 * Deletes a Request given its name. 
 * 
 * @param content - name of requesting user encoded as JSON-String
 * @return          code if sending was successfully
 */
@DELETE
@Consumes("application/json")
@Path("profile/contact/request")
public HttpResponse deleteRequest(@ContentParam String content) {
	
	String activeUserName = ((UserAgent) getActiveAgent()).getLoginName();
	String result = "";
	Connection conn = null;
	PreparedStatement stmnt = null;
	ResultSet rs = null;
	
	try {
	
		JSONObject contentObject = (JSONObject) JSONValue.parse(content);
		String requestUserName = (String) contentObject.get("username");
	
		try {
			conn = dbm.getConnection();
			stmnt = conn.prepareStatement("DELETE FROM ContactRequest WHERE (Sender = ? AND Receiver = ?);");
			stmnt.setString(1, requestUserName);
			stmnt.setString(2, activeUserName);
			int rows = stmnt.executeUpdate(); 
			
			//return
			HttpResponse r = new HttpResponse(result);
			if (rows > 0) {
				result = "Contact Request was deleted succesfully";				
				r.setStatus(200);
			} else {
				result = "There is no contact request from " + requestUserName + " which could be deleted";				
				r.setStatus(409);		
			}			
			return r;
			
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
			return er;
		} finally {
			// free resources
			HttpResponse response = freeRessources(conn, stmnt, rs);
			if(response.getStatus() != 200)
				return response;
		}
	}
	catch (Exception e)
	{
		Context.logError(this, e.getMessage());
		HttpResponse er = new HttpResponse("Content data in invalid format: " + e.getMessage());
		er.setStatus(400);
		return er;
	}
}

	
	/**	
	 * Retrieves all Groups where the user has a Membership 	
	 * @return all Groups where the user has a membership in the HTTP Response type 	
	 */

	@GET
	@Produces("application/json")
	@Path("group/member")
	public HttpResponse getMemberships() {
		
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		String userName = ((UserAgent) getActiveAgent()).getLoginName();
		
		try {
			// get connection from connection pool
			conn = dbm.getConnection();
			
			// prepare statement
			stmnt = conn.prepareStatement("SELECT GroupName FROM MemberOf WHERE UserName = ?;");
			stmnt.setString(1, userName);
			
			// prepare JSONArray
			JSONArray groupArray = new JSONArray();
			
			// retrieve result set
			rs = stmnt.executeQuery();
			
			// process result set
			boolean dataFound = false;
			while (rs.next())
			{				
				// extract all the messages and put them first in a JSON object and after that in a list
				JSONObject resultObject = new JSONObject();
				resultObject.put("groupname", rs.getString(1));
				groupArray.add(resultObject);				
								
				//Result found
				if(!dataFound) dataFound = true;
			}
			
			//Check if some result found
			if (dataFound) {
				
				// setup resulting JSON Object
				JSONObject jsonResult = new JSONObject();
				jsonResult.put("group", groupArray);
				
				// return HTTP Response on success
				HttpResponse r = new HttpResponse(jsonResult.toJSONString());
				r.setStatus(200);
				return r;				

			} else {
				String result = "No groups found for username " + userName;
				
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
			HttpResponse response = freeRessources(conn, stmnt, rs);
			if(response.getStatus() != 200)
				return response;
		}
	}
	
	/**
	 * addMember	 
	 * Adds a User to an existing Group
	 * @param userName  - name of the new member to be added
	 * @param groupName - name of the group where the user have to be added encoded as JSON-String
	 * 
	 */
	@POST
	@Path("group/{groupname}/member/{username}")
	public HttpResponse addMember(@PathParam("groupname") String groupName, @PathParam("username") String userName) {	
			Connection conn = null;
			PreparedStatement stmnt = null;
			ResultSet rs = null;
			PreparedStatement stmnt1 = null;	
			String agentName = ((UserAgent) getActiveAgent()).getLoginName();
			
			try {
				conn = dbm.getConnection();
				stmnt = conn.prepareStatement("SELECT FounderName FROM Groups WHERE GroupName = ?");
				stmnt.setString(1, groupName);
				rs = stmnt.executeQuery();
				if(rs.next())
				{
					if(!agentName.equals(rs.getString(1)))
					{
						HttpResponse r = new HttpResponse("User is not authorized to add members to this group!");
						r.setStatus(403);
						return r;
					}
				}
				
				stmnt1 = conn.prepareStatement("INSERT INTO MemberOf (UserName, GroupName) VALUES (?, ?);");
				stmnt1.setString(1, userName);
				stmnt1.setString(2, groupName);

				int rows = stmnt1.executeUpdate(); 
				// return
				if (rows != 0)
				{
					HttpResponse r = new HttpResponse("Member " + userName + " was added to the group " + groupName + "!");
					r.setStatus(200);
					return r;
				}
				else
				{
					HttpResponse r = new HttpResponse("Member could not be added!");
					r.setStatus(409);
					return r;
				}
				
			} catch (Exception e) {
				// return HTTP Response on error
				HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
				er.setStatus(500);
				return er;
			} finally {
				// free resources
				HttpResponse response = freeRessources(conn, stmnt, rs);
				if(response.getStatus() != 200)
				return response;
			}
	}
	
	/**
	 * Delete Member
	 * Deletes a Group Member
	 * 
	 *@param userName  - name of the member to be deleted.
	 *@param groupName - name of the group  
	 */
	@DELETE
	@Consumes("application/json")
	@Path("group/{groupname}/member/{username}")
	public HttpResponse deleteMember(@PathParam("groupname") String groupName, @PathParam("username") String userName) {

	try 
	{
		String agentName = ((UserAgent) getActiveAgent()).getLoginName();
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		PreparedStatement stmnt1 = null;
		
		try {
			conn = dbm.getConnection();
			stmnt = conn.prepareStatement("SELECT FounderName FROM Groups WHERE GroupName = ?");
			stmnt.setString(1, groupName);
			rs = stmnt.executeQuery();
			if(rs.next())
			{
				if(!agentName.equals(rs.getString(1)))
				{
					HttpResponse r = new HttpResponse("User is not authorized to add members to this group!");
					r.setStatus(403);
					return r;
				}
			}
			
			stmnt1 = conn.prepareStatement("DELETE FROM MemberOf WHERE UserName = ? AND GroupName = ?;");
			stmnt1.setString(1, userName);
			stmnt1.setString(2, groupName);
			stmnt1.executeUpdate(); 
			
			// return 
			HttpResponse r = new HttpResponse("Member successfully deleted!");
			r.setStatus(200);
			return r;
			
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
			return er;
		} finally {
			// free resources
			HttpResponse response = freeRessources(conn, stmnt, rs);
			if(response.getStatus() != 200)
				return response;
			if (stmnt1 != null) {
				try {
					stmnt1.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
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
	
	/**
	 * This method proves if two user have a open Contact Request 
	 * @param sender 
	 * @param receiver
	 * @return Are they have a open contact request with sender as Sender and receiver as Receiver ?
	 */
	private boolean existsOpenRequest(String sender, String receiver)
	{
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		
		try {
			conn = dbm.getConnection();
			stmnt = conn.prepareStatement("SELECT RequestID FROM ContactRequest WHERE (Sender = ? AND Receiver = ?);");
			stmnt.setString(1, sender);
			stmnt.setString(2, receiver);
			rs = stmnt.executeQuery();
			if (rs.next()) {
				return true;			
			} 
			return false;
			
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
		} finally {
			// free resources if exception or not			
			if (stmnt != null) {
				try {
					stmnt.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
				}
			}
		}
		return false;
	}
	
	/**
	 * This method proves if two user are contacts to each other 
	 * @param firstUser  - the first user
	 * @param secondUser - the second user
	 * @return Are they contacts?
	 */
	private boolean areContacts(String firstUser, String secondUser)
	{
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		try {
			conn = dbm.getConnection();
			stmnt = conn.prepareStatement("SELECT * FROM Contact WHERE (FirstUser = ? AND SecondUser = ?) OR (FirstUser = ? AND SecondUser = ?);");
			stmnt.setString(1, firstUser);
			stmnt.setString(2, secondUser);
			stmnt.setString(3, secondUser);
			stmnt.setString(4, firstUser);
			rs = stmnt.executeQuery(); 
			if (!rs.next()) 
				return false;
			else 
				return true;
			
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
		} finally {
			// free resources if exception or not
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
				}
			}
			if (stmnt != null) {
				try {
					stmnt.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
				}
			}
		}
		return false;
	}
	
	/**
	 * This method proves if a user is Member of a group
	 * @param user  - name of the user
	 * @param group - name of the group
	 * @return is the user member of the group ?
	 */
	private boolean isMemberOf(String user, String group)
	{
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		
		try {
			conn = dbm.getConnection();
			stmnt = conn.prepareStatement("SELECT MemberID FROM MemberOf WHERE (UserName = ? AND GroupName = ?);");
			stmnt.setString(1, user);
			stmnt.setString(2, group);
			rs = stmnt.executeQuery();
			if (rs.next()) {
				return true;			
			} 
			return false;
			
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
		} finally {
			// free resources if exception or not			
			if (stmnt != null) {
				try {
					stmnt.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
				}
			}
		}
		return false;
	}
	
		
	
	/**
	 * This method frees the database requesting resources
	 * @param conn  - The connection which will be closed
	 * @param stmnt - The statement which will be closed
	 * @param rs    - The result set which will be closed
	 * @return Successfully closed? Else failure code in the HTTP response data type
	 */
	private HttpResponse freeRessources(Connection conn, PreparedStatement stmnt, ResultSet rs)
	{
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
		return new HttpResponse("", 200);
	}
	
	/**
	 * This method frees the database requesting resources
	 * @param conn  - The connection which will be closed
	 * @param stmnt - The statement which will be closed
	 * @return Successfully closed? Else failure code in the HTTP response data type
	 */
	private HttpResponse freeRessources(Connection conn, PreparedStatement stmnt)
	{
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
		return new HttpResponse("", 200);
	}

	/**
	 * Retrieves all Users 
	 * @return data (username, nickname, visible) of all users
	*/ 
	@GET
	@Path("profile")
	@Produces("application/json")
	public HttpResponse getUsers() {
		
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		
		try {
			// get connection from connection pool
			conn = dbm.getConnection();			
						 
			// prepare statement
			stmnt = conn.prepareStatement("SELECT UserName, NickName, Visible FROM AccountProfile WHERE Visible = 1;");
			
			// retrieve result set
			rs = stmnt.executeQuery();	
			
			JSONArray userArray = new JSONArray();
			// process result set
			while(rs.next()) 
			{
				// setup resulting JSON Object
				JSONObject ro = new JSONObject();
				ro.put("username", rs.getString(1));
				ro.put("nickname", rs.getString(2));
				userArray.add(ro);
				
				// return HTTP Response on success
				// HttpResponse r = new HttpResponse(ro.toJSONString());
				// r.setStatus(200);
				// return r;
			}
			

			// return HTTP Response on success
			HttpResponse r = new HttpResponse(userArray.toJSONString());
			r.setStatus(200);
			return r;
			
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
			return er;
		} finally {
			// free resources
			HttpResponse response = freeRessources(conn, stmnt, rs);
			if(response.getStatus() != 200)
				return response;
		}
	}
}

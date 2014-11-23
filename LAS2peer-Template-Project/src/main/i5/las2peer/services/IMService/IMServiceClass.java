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
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Timestamp;

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
	 * @param userName of the Profile to be Retrieved 
	 * @result Profile Data
	*/ 
	@GET
	@Path("profile")
	@Consumes("application/json")
	@Produces("application/json")
	public HttpResponse getProfile(@ContentParam String content) {
		
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		
		try {
			// get connection from connection pool
			conn = dbm.getConnection();			
						 
			JSONObject profileObject = (JSONObject) JSONValue.parse(content);
			String userName = (String) profileObject.get("username");
			
			// prepare statement
			stmnt = conn.prepareStatement("SELECT EMail, Telephone, ImageLink, NickName, Visible FROM AccountProfile WHERE UserName = ?;");
			stmnt.setString(1, userName);
			
			// retrieve result set
			rs = stmnt.executeQuery();
			
			// check if users are contacts
			if(areContacts(userName, ((UserAgent)getActiveAgent()).getLoginName()))
			{		
				// process result set
				if(rs.next()) 
				{
					// is profile visible
					if(rs.getInt(5) == 1)
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
					
				} else {
					String error = "No result for username " + userName;
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse(error);
					er.setStatus(404);
					return er;
				}
			}
			else
			{
				String error = "The user " + userName + " is no contact!";
				
				// return HTTP Response on error
				HttpResponse er = new HttpResponse(error);
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
		}
	}

	/**	
	 * Creates a Profile
	 * 
	 * @param content Data for creating the Profile encoded as JSON-String
	 * @return Code if the sending was successfully
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
				
				try {	
			
					//Create Profile				
					stmnt = conn.prepareStatement("INSERT INTO AccountProfile (UserName, EMail, Telephone, ImageLink, NickName, Visible) VALUES (?, ?, ?, ?, ?, ?)");
					stmnt.setString(1, agentName);
					stmnt.setString(2, mail);
					stmnt.setString(3, tele);
					stmnt.setString(4, image);
					stmnt.setString(5, nickName);
					stmnt.setInt(6, visible);
					
					int rows = stmnt.executeUpdate();
					if(rows == 1)
						result = "Profile created successfully";
					else
					{
						result = "Profile could not be created!";
						// return HTTP Response on error
						HttpResponse er = new HttpResponse(result);
						er.setStatus(409);
						return er;
					}					
					
				} catch (SQLException e) {
					if (e.getErrorCode() == 1062) {
						HttpResponse er = new HttpResponse("Das Profil ist bereits vorhanden");					
						er.setStatus(409);
						return er;
					} else {
						HttpResponse er = new HttpResponse("Datenbank Fehler");					
						er.setStatus(500);
					}
				}
				
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
	 * Updates a profile 
	 * 
	 * @param userName UserName of the Profile to be updated 
	 * @param content Data for updating the Profile encoded as JSON-String
	 * @return Code if the sending was successfully
	*/
	@PUT
	@Path("profile")
	@Consumes("application/json")
	public HttpResponse updateProfile(@ContentParam String content) {		
		try 
		{
			String agentName = ((UserAgent) getActiveAgent()).getLoginName();
			// convert string content to JSON object 
			JSONObject profileObject = (JSONObject) JSONValue.parse(content);
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
					stmnt.setString(6, agentName);
					int rows = stmnt.executeUpdate();
					if(rows == 1)
						result = "Profile updated successfully";
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
	 * Retrieves a group given its name
	 * 
	 * @param groupName Information of a group to be retrieved 
	 * @result Group Data
	*/ 
	@GET
	@Path("group/{groupname}")
	@Produces("application/json")
	public HttpResponse getGroup(@PathParam("groupname") String groupName) {
		Connection conn = null;
		PreparedStatement stmnt = null;
		PreparedStatement stmnt1 = null; 
		ResultSet rs = null;
		ResultSet rs1 = null;
		try {
			// get connection from connection pool
			conn = dbm.getConnection();
			
			// prepare statement for the group
			stmnt = conn.prepareStatement("SELECT GroupName, FounderName, Description, ImageLink FROM Groups WHERE GroupName = ?;");
			stmnt.setString(1, groupName);
			
			// retrieve result set for the group
			rs = stmnt.executeQuery();
			
			// process result set
			if (rs.next()) 
			{
				// prepare statement for the members
				stmnt1 = conn.prepareStatement("SELECT UserName FROM MemberOf WHERE GroupName = ?;");
				stmnt1.setString(1, groupName);
				
				// retrieve result set for the members
				rs1 = stmnt.executeQuery();
				
				// setup resulting JSON Object
				JSONObject ro = new JSONObject();
				ro.put("name", rs.getString(1));
				ro.put("founder", rs.getString(2));
				ro.put("description", rs.getString(3));
				ro.put("imageLink", rs.getString(4));
				
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
				
			} else {
				String result = "Group does not exist: " + groupName;
				
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
	 * Creates a Group
	 * 
	 * @param content Data for creating the Profile encoded as JSON-String
	 * @return Code if the sending was successfully
	*/ 
	@POST
	@Path("group/{groupname}")
	@Consumes("application/json")
	public HttpResponse createGroup(@PathParam("groupname") String groupName,@ContentParam String content) {		
		try 
		{
			// convert string content to JSON object 
			JSONObject profileObject = (JSONObject) JSONValue.parse(content);
			String gName = (String) profileObject.get("GroupName");
			String founder = (String) profileObject.get("FounderName");
			String desc = (String) profileObject.get("Description");
			String img = (String) profileObject.get("ImageLink");
		
			String result = "";
			Connection conn = null;
			PreparedStatement stmnt = null;
			ResultSet rs = null;
				
			
			try {
				conn = dbm.getConnection();
				
				try {	
			
					//Create Profile				
					stmnt = conn.prepareStatement("INSERT INTO Groups (GroupName, FounderName, Description, ImageLink) VALUES (?, ?, ?, ?)");
					stmnt.setString(1, gName);
					stmnt.setString(2, founder);
					stmnt.setString(3, desc);
					stmnt.setString(4, img);
					
					int rows = stmnt.executeUpdate();
					if(rows == 1)
						result = "The Group was created successfully";
					else
					{
						result = "The Group could not be created!";
						// return HTTP Response on error
						HttpResponse er = new HttpResponse(result);
						er.setStatus(409);
						return er;
					}					
					
				} catch (SQLException e) {
					if (e.getErrorCode() == 1022) {
						HttpResponse er = new HttpResponse("The Group with the following name " + gName + " already exists!");					
						er.setStatus(409);
						return er;
					}
				}
				
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
	 * Updates a Group  
	 * 
	 * @param groupName of the Group to be updated 
	 * @param content Data for updating the Profile encoded as JSON-String
	 * @return Code if the sending was successfully
	*/ 
	@PUT
	@Path("group/{groupname}")
	@Consumes("application/json")
	public HttpResponse updateGroup(@PathParam("groupname") String groupName, @ContentParam String content) {		
		try 
		{
			// convert string content to JSON object 
			JSONObject profileObject = (JSONObject) JSONValue.parse(content);
			String name = (String) profileObject.get("name");
			String founder = (String) profileObject.get("founder");
			String desc = (String) profileObject.get("description");
			String iLink = (String) profileObject.get("imageLink");		
		
			String result = "";
			Connection conn = null;
			PreparedStatement stmnt = null;
			ResultSet rs = null;
				
			if(((UserAgent)getActiveAgent()).getLoginName().equals(founder))
			{
				try {
					conn = dbm.getConnection();
					stmnt = conn.prepareStatement("UPDATE Groups SET GroupName = ?, FounderName = ?, Description = ?, ImageLink = ? WHERE GroupName = ?;");
					stmnt.setString(1, name);
					stmnt.setString(2, founder);
					stmnt.setString(3, desc);
					stmnt.setString(4, iLink);
					stmnt.setString(5, groupName);
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
			else
			{
				result = "You are not authorized to update the group " + groupName + "!";
				// return HTTP Response on error
				HttpResponse er = new HttpResponse(result);
				er.setStatus(403);
				return er;
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
	 *@param groupName of the Profile to be deleted.
	*/
	@DELETE
	@Path("group/{groupname}")
	public HttpResponse deleteGroup(@PathParam("groupname") String groupName) {
	
		String result = "";
		Connection conn = null;
		PreparedStatement stmnt = null;
		PreparedStatement stmnt1 = null;
		ResultSet rs = null;
			
		try {
			conn = dbm.getConnection();
			stmnt = conn.prepareStatement("SELECT FounderName FROM Groups WHERE GroupName = ?");
			rs = stmnt.executeQuery();
			String founder = "";
			if(rs.next())
				founder = rs.getString(1);
			else
			{
				result = "Resource does not exist!";
				HttpResponse r = new HttpResponse(result);
				r.setStatus(404);
				return r;
			}
			
			if(((UserAgent)getActiveAgent()).getLoginName().equals(founder))
			{
				stmnt1 = conn.prepareStatement("DELETE FROM Groups WHERE GroupName = ?;");
				stmnt1.setString(1, groupName);
				stmnt1.executeUpdate(); 
				result = "Group deleted successfully";
				
				// return 
				HttpResponse r = new HttpResponse(result);
				r.setStatus(200);
				return r;
			}
			else
			{
				result = "You are not authorized to delete the group " + groupName + "!";
				// return HTTP Response on error
				HttpResponse er = new HttpResponse(result);
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
	 * @param userName The name of the user whose contact list should be displayed
	 * @return The data (username and nickname) of the contacts in the HTTP Response type 
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
				stmnt = conn.prepareStatement("SELECT UserName, NickName from Contact, AccountProfile WHERE (FirstUser = ? AND SecondUser = UserName) & _"
						+ "OR (SecondUser = ? AND FirstUser = UserName);");
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
					contactObject.put("nickname", rs.getString(1));
					contactObject.put("username", rs.getString(2));
					contactArray.add(contactObject);
				}
				
				if (dataFound)
				{
					// setup resulting JSON Object
					JSONObject jsonResult = new JSONObject();
					jsonResult.put("contact", contactArray);
					
					// return HTTP response
					HttpResponse r = new HttpResponse(jsonResult.toJSONString());
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
	 * @param content the username of the user requested
	 * @return Code if the sending was successfully
	*/ 
	@PUT
	@Consumes("application/json")
	@Path("profile/contact")
	public HttpResponse createContact(@ContentParam String content)
	{
		try 
		{
			String result = "";
			Connection conn = null;
			PreparedStatement stmnt = null;
			ResultSet rs = null;		
			
			JSONObject contentObject = (JSONObject) JSONValue.parse(content);
			String userName = (String) contentObject.get("username");
			String agentName = (String) ((UserAgent) getActiveAgent()).getLoginName();
			
			try {
				conn = dbm.getConnection();
				
				// Insert Contact into Contact Table
				stmnt = conn.prepareStatement("INSERT INTO Contact (FirstUser, SecondUser) VALUES (?, ?);");				
				stmnt.setString(1, userName);
				stmnt.setString(2, agentName);
				
				int rows = stmnt.executeUpdate();
				result = "Contacts updated. " + userName + " and " + agentName + " are Contacts now";
				
					//Delete Request from Request Table
					HttpResponse zr = deleteRequest("{\"username\":\"" + userName + "\"}");
				
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
	 * This method deletes a contact from the contact list
	 * @param userName The name of the contact which should be deleted
	 * @return Success or not
	*/
	@DELETE 
	@Path("profile/contact")
	public HttpResponse deleteContact(@ContentParam String content) {
		String result ="";
		String agentName = ((UserAgent)getActiveAgent()).getLoginName();
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		
		// convert string content to JSON object 
					JSONObject contactObject = (JSONObject) JSONValue.parse(content);
					String userName = (String) contactObject.get("username");
		
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
	 * @param username The name of the user who got the messages
	 * @return The messages for the user as HTTP Response type 
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
			stmnt = conn.prepareStatement("SELECT Message.MessageID, Message, MessageTimeStamp, Sender FROM Message, SendingSingle WHERE ((Sender = ? AND Receiver = ?) OR (Sender = ? AND Receiver = ?)) AND Message.MessageID = SendingSingle.MessageID;");
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
 	 * @param content The id of the message in a JSON string
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
	 * @param userName The name of the user who gets the message
	 * @param content The content of the message encoded as JSON string
	 * @return Code if the sending was successfully
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
			Timestamp timeStamp = Timestamp.valueOf((String) messageObject.get("timestamp"));
			
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
				stmnt = conn.prepareStatement("INSERT INTO Message (Message, MessageTimeStamp, WasRead) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
				stmnt.setString(1, message);
				stmnt.setTimestamp(2, timeStamp);
				stmnt.setInt(3, 0);
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
	 * @param content The content of the message encoded as JSON string
	 * @return Code if the sending was successfully
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
			PreparedStatement stmnt1 = null;
			
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
	
	/** vor��bergehend auslassen
	 * This method returns the messages in a group. 
	 * @param groupName The name of the group in which the messages are sent
	 * @return The messages sent in a group as JSON String 
	 
	@GET
	@Path("message/group/{name}")
	public HttpResponse getGroupMessages(@PathParam("name") String groupName)
	{
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		
		try 
		{
			// get connection from connection pool
			conn = dbm.getConnection();
			
			// prepare statement
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
				String error = "No messages found for contact " + groupName + "!";
				
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
	*/ 
	
	/* voruebergehend auskommentiert
	/** This method sends a message to a group
	 * @param userName The name of the group the message will be sent
	 * @param content The content of the message encoded as JSON string
	 * @return Code if the sending was successfully

	@PUT
	@Path("message/group/{name}")
	@Consumes("application/json")
	public HttpResponse sendGroupMessage(@PathParam("name") String groupName, @ContentParam String content)
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
				stmnt = conn.prepareStatement("INSERT INTO Message (Message, MessageTimeStamp, WasRead) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
				stmnt.setString(1, message);
				stmnt.setString(2, timeStamp);
				stmnt.setInt(3, 0);
				int rows = stmnt.executeUpdate();
				result = "Database updated. " + rows + " rows in table \"Message\" affected";
				stmnt.getGeneratedKeys().next();
				
				// retrieve the message ID for saving it in the sending group table
				int messageID = stmnt.getGeneratedKeys().getInt(1);
				
				// insert the message ID, sender and receiver in the sending group table
				stmnt1 = conn.prepareStatement("INSERT INTO SendingGroup (Sender, Receiver, MessageID) VALUES (?, ?, ?);");
				stmnt1.setString(1, ((UserAgent) getActiveAgent()).getLoginName());
				stmnt1.setString(2, groupName);
				stmnt1.setInt(3, messageID);
				rows = stmnt1.executeUpdate();
				result += "\n\rDatabase updated. " + rows + " rows in table \"SendingGroup\" affected";
				
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
	voruebergehend auskommentiert */
	

	
	
	 /**
		 * This method returns all contact requests to a certain user. 
		 * @param name The name of the user to whom contact request were sent
		 * @return The data (username and nickname) of the user who has sent a contact request in the HTTP Response type 
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
			stmnt = conn.prepareStatement("select ap.UserName, ap.NickName from ContactRequest as cr, AccountProfile as ap where cr.To_UserName= ? AND cr.From_UserName=ap.UserName;");
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
					// setup resulting JSON Object
					JSONObject jsonResult = new JSONObject();
					jsonResult.put("request", contactArray);
					
					// return HTTP response
					HttpResponse r = new HttpResponse(jsonResult.toJSONString());
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
 * This method sends a contact-request from the active user to a different user
 * @param content The content of the message encoded as JSON string
 * @return Code if the sending was successfully
 */
@PUT
@Path("profile/contact/request")
@Consumes("application/json")
public HttpResponse createRequest(@ContentParam String content)
{
	try 
	{
		String result = "";
		Connection conn = null;
		PreparedStatement stmnt = null;
		PreparedStatement stmnt1 = null;
		ResultSet rs = null;		
		
		JSONObject contentObject = (JSONObject) JSONValue.parse(content);
		String userName = (String) contentObject.get("username");
		
		try {
			conn = dbm.getConnection();
			
			// insert the request into the request table
			stmnt = conn.prepareStatement("INSERT INTO ContactRequest (RequestID, From_UserName, To_UserName) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
			stmnt.getGeneratedKeys().next();
			int requestID = stmnt.getGeneratedKeys().getInt(1);			
			stmnt.setInt(1, requestID);
			stmnt.setString(2, ((UserAgent) getActiveAgent()).getLoginName());
			stmnt.setString(3, userName);
			int rows = stmnt.executeUpdate();
			result = "Database updated. " + rows + " rows in table \"Message\" affected";
			
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
 * Delete Request
 * Deletes a Request given its name. 
 * 
 *@param UserName of the Profile to be deleted.
 */
@DELETE
@Consumes("application/json")
@Path("profile/contact/request")
public HttpResponse deleteRequest(@ContentParam String content) {
	
	String agentName = ((UserAgent) getActiveAgent()).getLoginName();
	String result = "";
	Connection conn = null;
	PreparedStatement stmnt = null;
	ResultSet rs = null;
	
	JSONObject contentObject = (JSONObject) JSONValue.parse(content);
	String userName = (String) contentObject.get("username");
	
	try {
		conn = dbm.getConnection();
		stmnt = conn.prepareStatement("DELETE FROM ContactRequest WHERE (To_UserName = ? OR From_UserName = ?);");
		stmnt.setString(1, agentName);
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

	
	/**
	 * getMembership
	 * Retrieves all Groups where the user has a Membership 
	 * 
	 * @param UserName of the User to get his groups
	 * @result Profile Data
	 */
	@GET
	@Path("member/{name}")
	public HttpResponse getMemberships(@PathParam("name") String userName) {
		
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		
		try {
			// get connection from connection pool
			conn = dbm.getConnection();
			
			// prepare statement
			stmnt = conn.prepareStatement("SELECT g.GroupName, g.FounderName FROM Groups As g, MemberOf As m WHERE m.UserName = ? AND m.GroupName = g.GroupName;");
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
				resultObject.put("founder", rs.getString(2));
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
	 * addMember	 
	 * Adds a User to an existing Group
	 * @param name The username of the new member to be added
	 * @param content The groupname of the group where the user have to be added encoded as JSON-String
	 */
	@PUT
	@Consumes("application/json")
	@Path("member/{name}")
	public HttpResponse addMember(@PathParam("name") String userName, @ContentParam String content) {
		
		try 
		{
			// convert string content to JSON object 
			JSONObject contentObject = (JSONObject) JSONValue.parse(content);
			String groupName = (String) contentObject.get("groupname");
		
			String result = "";
			Connection conn = null;
			PreparedStatement stmnt = null;
			ResultSet rs = null;				
		
			try {
				conn = dbm.getConnection();
				stmnt = conn.prepareStatement("INSERT INTO MemberOf (UserName, GroupName) VALUES (?, ?);");
				stmnt.setString(1, userName);
				stmnt.setString(2, groupName);

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
	 * Delete Member
	 * Deletes a Group Member
	 * 
	 *@param UserName of the Profile to be deleted.
	 *@param content groupname 
	 */
	@DELETE
	@Consumes("application/json")
	@Path("member/{name}")
	public HttpResponse deleteMember(@PathParam("name") String userName, @ContentParam String content) {

	try 
	{
		// convert string content to JSON object 
		JSONObject contentObject = (JSONObject) JSONValue.parse(content);
		String groupName = (String) contentObject.get("groupname");	
		
		String result = "";
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		
		try {
			conn = dbm.getConnection();
			stmnt = conn.prepareStatement("DELETE FROM MemberOf WHERE UserName = ? AND GroupName = ?;");
			stmnt.setString(1, userName);
			stmnt.setString(2, groupName);
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
	 * This method proves if two user are contacts to each other 
	 * @param firstUser The first user
	 * @param secondUser The second user
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
			int rows = stmnt.executeUpdate(); 
			if (rows == 0) 
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
	 * This method frees the database requesting resources
	 * @param conn The connection which will be closed
	 * @param stmnt The statement which will be closed
	 * @param rs The result set which will be closed
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
	 * @param conn The connection which will be closed
	 * @param stmnt The statement which will be closed
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
}

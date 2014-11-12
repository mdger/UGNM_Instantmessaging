package i5.las2peer.services.IMService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.security.ServiceAgent;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.IMService.IMServiceClass;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.webConnector.WebConnector;
import i5.las2peer.webConnector.client.ClientResponse;
import i5.las2peer.webConnector.client.MiniClient;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.acl.Group;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Example Test Class demonstrating a basic JUnit test structure.
 * 
 * 
 *
 */
public class ServiceTest {
	
	private static final String HTTP_ADDRESS = "http://127.0.0.1";
	private static final int HTTP_PORT = WebConnector.DEFAULT_HTTP_PORT;
	
	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;
	
	private static UserAgent testAgent;
	private static final String testPass = "adamspass";
	
	private static UserAgent testAgent2;
	private static final String testPass2 = "adamspass";
	
	private static UserAgent testAgent3;
	private static final String testPass3 = "adamspass";
	
	private static final String testServiceClass = "i5.las2peer.services.IMService.IMServiceClass";
	
	private static final String mainPath = "im/";
	
	
	/**
	 * Called before the tests start.
	 * 
	 * Sets up the node and initializes connector and users that can be used throughout the tests.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void startServer() throws Exception {
		
		//start node
		node = LocalNode.newNode();
		node.storeAgent(MockAgentFactory.getAdam());
		node.launch();
		
		ServiceAgent testService = ServiceAgent.generateNewAgent(testServiceClass, "a pass");
		testService.unlockPrivateKey("a pass");
		
		node.registerReceiver(testService);
		
		//start connector
		logStream = new ByteArrayOutputStream ();
		
		connector = new WebConnector(true,HTTP_PORT,false,1000);
		connector.setSocketTimeout(10000);
		connector.setLogStream(new PrintStream (logStream));
		connector.start ( node );
        Thread.sleep(1000); //wait a second for the connector to become ready
		testAgent = MockAgentFactory.getAdam();
		
        connector.updateServiceList();
        //avoid timing errors: wait for the repository manager to get all services before continuing
        try
        {
            System.out.println("waiting..");
            Thread.sleep(10000);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
		
	}
	
	
	
	
	
	
	
	
	/**
	 * Called after the tests have finished.
	 * Shuts down the server and prints out the connector log file for reference.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void shutDownServer () throws Exception {
		
		connector.stop();
		node.shutDown();
		
        connector = null;
        node = null;
        
        LocalNode.reset();
		
		System.out.println("Connector-Log:");
		System.out.println("--------------");
		
		System.out.println(logStream.toString());
		
    }
	
	
	
	@Test
	public void testCeateProfile()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("POST", mainPath +"profile", "\"email\":\"test@mail.de\", \"telephone\":\"123456\", \"imageLink\":\"imageUrl\", \"nickname\":'TestNickName\", \"visible\":\"1\""); 
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains("successfully")); 
			System.out.println("Result of 'testCreateProfile': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	
	@Test
	public void testUpdateProfile()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("PUT", mainPath +"profile", "{\"email\":\"test@mail.de\", \"telephone\":\"111111\", \"imageLink\":\"imageUrl\", \"nickname\":'NewNickName\", \"visible\":\"}"); 
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains("successfully")); 
			System.out.println("Result of 'testUpdateProfile': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	
	/**
	 * Tests GET PROFILE
	 */
	
	@Test
	public void testGetProfile()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"profile", "{'username':'" + testAgent.getLoginName() + "'}"); 
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains("NewNickName")); 
            assertTrue(result.getResponse().trim().contains("test@mail.de")); 
			System.out.println("Result of 'testGetProfile': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	
	@Test
	public void testDeleteProfile()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("DELETE", mainPath +"profile", ""); 
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains("successfully")); 
			System.out.println("Result of 'testUpdateProfile': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	

	@Test
	public void testCeateContact()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("POST", mainPath +"profile/contact", "{\"username\":\"testAgent2.getLoginName()\"}"); 
            assertEquals(200, result.getHttpCode());
			System.out.println("Result of 'testCreateContact': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	
	
	@Test
	public void testGetContact()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"profile/contact", ""); 
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains(testAgent.getLoginName())); 
			System.out.println("Result of 'testCreateContact': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	
	@Test
	public void testDeleteContact()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("DELETE", mainPath +"profile/contact", ""); 
            assertEquals(200, result.getHttpCode());
            System.out.println("Result of 'testCreateContact': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	
	//TODO testCreateRequest
	//TODO testGetRequest
	//TODO testDeleteRequest
	
	//TODO testGetSingleMessage
	//TODO testSendSingleMessage
	
	//TODO testGetUnreadMessages
	//TODO testSetUnreadMessages
	
	//TODO testCreateGroup
	public void testCreateGroup()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("POST", mainPath +"group/groupname", ""); //testInput is the pathParam
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains("testInput")); //"testInput" name is part of response
			System.out.println("Result of 'testExampleMethod': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	//TODO testUpdateGroup
	public void testUpdateGroup()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("PUT", mainPath +"group/groupname", ""); //testInput is the pathParam
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains("testInput")); //"testInput" name is part of response
			System.out.println("Result of 'testExampleMethod': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	//TODO testGetGroup
	public void testGetGroup()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"group/groupname", ""); //testInput is the pathParam
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains("testInput")); //"testInput" name is part of response
			System.out.println("Result of 'testExampleMethod': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	//TODO testDeleteGroup
	public void testDeleteGroup()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("DELETE", mainPath +"group/groupname", ""); //testInput is the pathParam
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains("testInput")); //"testInput" name is part of response
			System.out.println("Result of 'testExampleMethod': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	
	//TODO testGetMemberships
	//TODO testAddMember
	//TODO testDeleteMember
	
	
	/**
	 * 
	 * Tests the validate method.
	 * 
	 */
	
	public void testValidateLogin()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"validate", "");
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains("adam")); //login name is part of response
			System.out.println("Result of 'testValidateLogin': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	
	
	/**
	 * 
	 * Test the example method that consumes one path parameter
	 * which we give the value "testInput" in this test.
	 * 
	 */
	
	public void testExampleMethod()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("POST", mainPath +"myMethodPath/testInput", ""); //testInput is the pathParam
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains("testInput")); //"testInput" name is part of response
			System.out.println("Result of 'testExampleMethod': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }

	/**
	 * Test the ServiceClass for valid rest mapping.
	 * Important for development.
	 */
	@Test
	public void testDebugMapping()
	{
		IMServiceClass cl = new IMServiceClass();
		assertTrue(cl.debugMapping());
	}
}

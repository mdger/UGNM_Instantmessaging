package i5.las2peer.services.IMService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.restMapper.data.Pair;
import i5.las2peer.security.ServiceAgent;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.IMService.IMServiceClass;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.webConnector.WebConnector;
import i5.las2peer.webConnector.client.ClientResponse;
import i5.las2peer.webConnector.client.MiniClient;
import i5.las2peer.services.IMService.database.DatabaseManager;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;

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
	private static String agentName1 = "";
	
	private static UserAgent testAgent2;
	private static final String testPass2 = "evaspass";
	private static String agentName2 = "";
	
	private static UserAgent testAgent3;
	private static final String testPass3 = "abelspass";
	private static String agentName3 = "";
			
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
		testAgent2 = MockAgentFactory.getEve();
		testAgent3 = MockAgentFactory.getAbel();
		
		agentName1 = testAgent.getLoginName();
		agentName2 = testAgent2.getLoginName();
		agentName3 = testAgent3.getLoginName();
		
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
	
	//TODO Init Database
		
	
	
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
	
	//TODO Reset Database
	
	
	
	@Test
	public void testProfile()
	{
	
		testCreateProfile();
		testUpdateProfile();
		testDeleteProfile();		
		
	}
	
	
	/**
	 * Test POST Profile
	 * Erstellt ein Profil für testAgent.
	 * 
	 * test Wird ein Falsch Formatierten JSON erkannt.
	 * test Wird erfolgreich ein Profil erstellt.
	 * test Lässt sich ein zweites Profil für testAgent anlegen (sollte nicht sein)
	 * 
	 * @throws Exception
	 */
	
	//@Test
	public void testCreateProfile()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			
			c.setLogin(Long.toString(testAgent.getId()), testPass);
			
            ClientResponse result3=c.sendRequest("POST", mainPath +"profile", "{\"Irgendwas\":\"Anderes\"}", "application/json", "*/*", new Pair[]{}); 
            assertEquals(400, result3.getHttpCode());            
			System.out.println("'CreateProfile'-Falsches Format erkannt: " + result3.getResponse().trim());		
						
            ClientResponse result=c.sendRequest("POST", mainPath +"profile", "{\"email\":\"test@mail.de\", \"telephone\":\"123456\", \"imageLink\":\"imageUrl\", \"nickname\":\"TestNickName\", \"visible\":1}", "application/json", "*/*", new Pair[]{}); 
            assertEquals(200, result.getHttpCode());            
			System.out.println("'CreateProfile'-Profil für testAgent erstellt: " + result.getResponse().trim());
						
			ClientResponse result2=c.sendRequest("POST", mainPath +"profile", "{\"email\":\"ntest@mail.de\", \"telephone\":\"111111\", \"imageLink\":\"imageUrl\", \"nickname\":\"TestNickName\", \"visible\":1}", "application/json", "*/*", new Pair[]{});
			assertEquals(409, result2.getHttpCode());            
			System.out.println("'CreateProfile'-Kein Zweites konnte angelegt werden" + result2.getResponse().trim());
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}					
    }	
	
	/**
	 * Test UPDATE Profile
	 * Verändert das Profil von testAgent
	 * 
	 * test	Wird Falsche Eingabe erkannt
	 * test Wird es erfolgreich verändert
	 * 
	 * @throws Exception
	 */
	
	//@Test
	public void testUpdateProfile()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
			
			ClientResponse result3=c.sendRequest("PUT", mainPath +"profile", "{\"Irgendwas\":\"Anderes\"}", "application/json", "*/*", new Pair[]{}); 
	        assertEquals(400, result3.getHttpCode());            
			System.out.println("'UpdateProfile'-wrong entry detected: " + result3.getResponse().trim());				
			
            ClientResponse result=c.sendRequest("PUT", mainPath +"profile", "{\"email\":\"test@mail.de\", \"telephone\":\"1111111\", \"imageLink\":\"imageUrl\", \"nickname\":\"NewNickName\", \"visible\":1}", "application/json", "*/*", new Pair[]{}); 
            assertEquals(200, result.getHttpCode());
			System.out.println("Result of 'testUpdateProfile': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	
	/**
	 * Test GET Profile
	 * Fragt nach dem Profil von testAgent.
	 * 
	 * test Wird es ordentlich angezeigt
	 * 
	 * @throws Exception
	 */
	
	//@Test
	public void testGetProfile()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            
			ClientResponse result=c.sendRequest("GET", mainPath +"profile", "{\"username\":\"adam\"}", "application/json", "application/json", new Pair[]{}); 
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
	
	//@Test
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
	

	/**
	 * Test POST Request
	 * testAgent schickt eine Contact-Request an testAgent2
	 * testAgent schickt eine Contact-Request an testAgent3
	 * 
	 * test Kommen sie erfolgreich an
	 * 
	 * @throws Exception
	 */
	//@Test
	public void testCreateRequest()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
			ClientResponse result=c.sendRequest("POST", mainPath +"profile/contact/request", "{\"username\":\"" + testAgent2.getLoginName() + "\"}"); 
            assertEquals(200, result.getHttpCode());
            System.out.println("Result of 'testCreateRequest': " + result.getResponse().trim());
            
            result=c.sendRequest("POST", mainPath +"profile/contact/request", "{\"username\":\"" + testAgent3.getLoginName() + "\"}"); 
            assertEquals(200, result.getHttpCode());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	
	/**
	 * Test GET Request
	 * testAgent2 überprüft seine Contact-Requests 
	 * 
	 * test Kann er sie sehen
	 * 
	 * @throws Exception
	 */
	//@Test
	public void testGetRequest()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent2.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"profile/contact/request", ""); 
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains(testAgent.getLoginName())); 
            System.out.println("Result of 'testGetRequest': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}		
    }

	/**
	 * Test DELETE Request
	 * testAgent2 löscht die Kontaktanfrage von testAgent  
	 * 
	 * @throws Exception
	 */
	//@Test
	public void testDeleteRequest()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent2.getId()), testPass);
            ClientResponse result=c.sendRequest("DELETE", mainPath +"profile/contact/request", "{\"username\":\"" + testAgent.getLoginName() + "\"}"); 
            assertEquals(200, result.getHttpCode());            
            System.out.println("Result of 'testDeleteRequest': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}		
    }
	
	
	
	
	/**
	 * Test POST Contact
	 * testAgent3 fügt testAgent1 seinen Kontakten hinzu.

	 * 
	 * 	test Fügt Agent3 ihn erfolgreich hinzu

	 * 
	 * @throws Exception
	 */
	//@Test
	public void testCreateContact()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("POST", mainPath +"profile/contact", "{\"username\":\"" + testAgent3.getLoginName() + "\"}"); 
            assertEquals(200, result.getHttpCode());
			System.out.println("Result of 'testCreateContact': " + result.getResponse().trim());
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	
	
	/**
	 * Test GET Contact
	 * Zeigt die Kontakte von testAgent
	 * 
	 * @throws Exception
	 */
	//@Test
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
	
	
	
	/**
	 * Test POST SingleMessage
	 * testAgent schickt eine Nachricht an testAgent3
	 * 	test testAgnet 3 sollte sie erhalten
	 * 
	 * @throws Exception
	 */
	//@Test
	public void testSendSingleMessage()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("POST", mainPath +"message/single/" + testAgent3.getLoginName(), "{\"message\":\"Hey Agent3 :)\", \"timestamp\":\"2014-1-1 00:00:00.000 \"}"); 
            assertEquals(200, result.getHttpCode());
			System.out.println("Result of 'testSendSingleMessage': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	
	/**
	 * Test GET SingleMessage
	 * testAgent3 überprüft seine Nachrichten
	 * 	test testAgent3 sollte eine von testAgent erhalten haben
	 * 
	 * @throws Exception
	 */
	//@Test
	public void testGetSingleMessage()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent3.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath + "message/single/" + testAgent.getLoginName(), ""); 
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains("Hey"));            
			System.out.println("Result of 'testGetSingleMessage': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	
	
	
	/**
	 * Test DELETE Contact
	 * testAgent löscht testAgent3 aus seiner Kontaktliste.
	 * 
	 * @throws Exception
	 */
	//@Test
	public void testDeleteContact()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("DELETE", mainPath +"profile/contact", "{\"username\":\"" + testAgent3.getLoginName() + "\"}"); 
            assertEquals(200, result.getHttpCode());
            System.out.println("Result of 'testCreateContact': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	

	//TODO testDeleteMessages
	//TODO testGetUnreadMessages
	//TODO testSetUnreadMessages
	
	//@Test
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
	//@Test
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
			System.out.println("Result of 'testUpdateGroup': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	//@Test
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
			System.out.println("Result of 'testGetGroup': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }
	//@Test
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
			System.out.println("Result of 'testDeleteGroup': " + result.getResponse().trim());
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

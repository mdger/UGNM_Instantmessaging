package i5.las2peer.services.IMService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.security.ServiceAgent;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.IMService.database.DatabaseManager;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.webConnector.WebConnector;
import i5.las2peer.webConnector.client.ClientResponse;
import i5.las2peer.webConnector.client.MiniClient;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Example Test Class demonstrating a basic JUnit test structure.
 * 
 * 
 *
 */
public class ServiceTest {

	protected static final String HTTP_ADDRESS = "http://127.0.0.1";
	protected static final int HTTP_PORT = WebConnector.DEFAULT_HTTP_PORT;

	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	private static UserAgent testAgent;
	protected static final String adamPass = "adamspass";

	private static UserAgent testAgent2;
	protected static final String evePass = "evespass";

	private static UserAgent testAgent3;
	protected static final String abelPass = "abelspass";

	private static final String testServiceClass = "i5.las2peer.services.IMService.IMServiceClass";

	protected static final String mainPath = "im/";

	private static String jdbcDriverClassName;
	private static String jdbcLogin;
	private static String jdbcPass;
	private static String jdbcUrl;
	private static String jdbcSchema;
	protected static DatabaseManager dbm;

	// Return Information for Test Agents
	protected static String getAdamID() {
		return (String) Long.toString(testAgent.getId());
	}

	protected static String getAdamName() {
		return (String) testAgent.getLoginName();
	}

	protected static String getEveID() {
		return (String) Long.toString(testAgent2.getId());
	}

	protected static String getEveName() {
		return (String) testAgent2.getLoginName();
	}

	protected static String getAbelID() {
		return (String) Long.toString(testAgent3.getId());
	}

	protected static String getAbelName() {
		return (String) testAgent3.getLoginName();
	}

	/**
	 * Called before the tests start.
	 * 
	 * Sets up the node and initializes connector and users that can be used
	 * throughout the tests.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void startServer() throws Exception {

		// start node
		node = LocalNode.newNode();
		node.storeAgent(MockAgentFactory.getAdam());
		node.storeAgent(MockAgentFactory.getEve());
		node.storeAgent(MockAgentFactory.getAbel());
		node.launch();

		ServiceAgent testService = ServiceAgent.generateNewAgent(testServiceClass, "a pass");
		testService.unlockPrivateKey("a pass");

		node.registerReceiver(testService);

		// start connector
		logStream = new ByteArrayOutputStream();

		connector = new WebConnector(true, HTTP_PORT, false, 1000);
		connector.setSocketTimeout(10000);
		connector.setLogStream(new PrintStream(logStream));
		connector.start(node);
		Thread.sleep(1000); // wait a second for the connector to become ready
		testAgent = MockAgentFactory.getAdam();
		testAgent2 = MockAgentFactory.getEve();
		testAgent3 = MockAgentFactory.getAbel();

		connector.updateServiceList();
		// avoid timing errors: wait for the repository manager to get all
		// services before continuing
		try {
			System.out.println("waiting..");
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@BeforeClass
	public static void dataBaseConnect() throws Exception {

		// read properties
		Properties props = new Properties();
		FileInputStream in = new FileInputStream("etc/i5.las2peer.services.IMService.IMServiceClass.properties");
		props.load(in);
		in.close();
		jdbcDriverClassName = props.getProperty("jdbcDriverClassName");
		jdbcSchema = props.getProperty("jdbcSchema");
		jdbcUrl = props.getProperty("jdbcUrl");
		jdbcLogin = props.getProperty("jdbcLogin");
		jdbcPass = props.getProperty("jdbcPass");

		// database connection
		dbm = new DatabaseManager(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);

	}

	@Before
	public void writeTestData() throws Exception {

		// DataBase Connection

		Connection conn = null;
		PreparedStatement stmnt = null;
		conn = dbm.getConnection();

		/**
		 * Create Profiles eve visibility = 0 adam visibility = 1 Create
		 * ContactRequest adam to eve
		 */
		try {
			stmnt = conn.prepareStatement("INSERT INTO AccountProfile VALUES (\"" + getAdamName()
					+ "\", \"test@mail.de\", 2222, \"test\", \"NewNickName\", 1), (\"" + getEveName()
					+ "\", \"test\", 2222, \"test\", \"test\", 0);");
			int rows = stmnt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}

		if (stmnt != null) {
			stmnt.close();
		}
		if (conn != null) {
			conn.close();
		}

	}

	/**
	 * Called after the tests have finished. Shuts down the server and prints
	 * out the connector log file for reference.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void shutDownServer() throws Exception {

		connector.stop();
		node.shutDown();

		connector = null;
		node = null;

		LocalNode.reset();

		System.out.println("Connector-Log:");
		System.out.println("--------------");

		System.out.println(logStream.toString());

	}

	/**
	 * Called after the tests have finished. Deletes TestData from Database
	 * 
	 * @throws Exception
	 */
	@After
	public void deleteData() throws Exception {

		Connection conn = null;
		PreparedStatement stmnt = null;
		conn = dbm.getConnection();

		try {

			stmnt = conn.prepareStatement("DELETE FROM Contact WHERE FirstUser='" + getAdamName() + "' OR FirstUser='"
					+ getEveName() + "' OR FirstUser = '" + getAbelName() + "'");
			int rows = stmnt.executeUpdate();

			stmnt = conn.prepareStatement("DELETE FROM ContactRequest WHERE Sender='" + getAdamName() + "' OR Sender='"
					+ getEveName() + "' OR Sender = '" + getAbelName() + "'");
			rows = stmnt.executeUpdate();

			stmnt = conn.prepareStatement("DELETE FROM AccountProfile WHERE UserName='" + getAdamName()
					+ "' OR UserName='" + getEveName() + "' OR UserName = '" + getAbelName() + "'");
			rows = stmnt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}

		if (stmnt != null) {
			stmnt.close();
		}
		if (conn != null) {
			conn.close();
		}

	}

	// @Test
	public void testCreateGroup() {
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);

		try {
			c.setLogin(Long.toString(testAgent.getId()), adamPass);
			ClientResponse result = c.sendRequest("POST", mainPath + "group/groupname", ""); // testInput
			// is the
			// pathParam
			assertEquals(200, result.getHttpCode());
			assertTrue(result.getResponse().trim().contains("testInput")); // "testInput"
			// name
			// is
			// part
			// of
			// response
			System.out.println("Result of 'testExampleMethod': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}

	}

	// @Test
	public void testUpdateGroup() {
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);

		try {
			c.setLogin(Long.toString(testAgent.getId()), adamPass);
			ClientResponse result = c.sendRequest("PUT", mainPath + "group/groupname", ""); // testInput
			// is the
			// pathParam
			assertEquals(200, result.getHttpCode());
			assertTrue(result.getResponse().trim().contains("testInput")); // "testInput"
			// name
			// is
			// part
			// of
			// response
			System.out.println("Result of 'testUpdateGroup': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}

	}

	// @Test
	public void testGetGroup() {
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);

		try {
			c.setLogin(Long.toString(testAgent.getId()), adamPass);
			ClientResponse result = c.sendRequest("GET", mainPath + "group/groupname", ""); // testInput
			// is the
			// pathParam
			assertEquals(200, result.getHttpCode());
			assertTrue(result.getResponse().trim().contains("testInput")); // "testInput"
			// name
			// is
			// part
			// of
			// response
			System.out.println("Result of 'testGetGroup': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}

	}

	// @Test
	public void testDeleteGroup() {
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);

		try {
			c.setLogin(Long.toString(testAgent.getId()), adamPass);
			ClientResponse result = c.sendRequest("DELETE", mainPath + "group/groupname", ""); // testInput
			// is the
			// pathParam
			assertEquals(200, result.getHttpCode());
			assertTrue(result.getResponse().trim().contains("testInput")); // "testInput"
			// name
			// is
			// part
			// of
			// response
			System.out.println("Result of 'testDeleteGroup': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}

	}

	// TODO testGetMemberships
	// TODO testAddMember
	// TODO testDeleteMember

	/**
	 * Test the ServiceClass for valid rest mapping. Important for development.
	 */
	@Test
	public void testDebugMapping() {
		IMServiceClass cl = new IMServiceClass();
		assertTrue(cl.debugMapping());
	}
}

package i5.las2peer.services.IMService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import i5.las2peer.restMapper.data.Pair;
import i5.las2peer.webConnector.client.ClientResponse;
import i5.las2peer.webConnector.client.MiniClient;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MessageTest extends ServiceTest {

	@Before
	public void writeTestData() throws Exception {

		// DataBase Connection

		Connection conn = null;
		PreparedStatement stmnt = null;
		conn = dbm.getConnection();

		/**
		 * Create Profiles eve visibility = 0 adam visibility = 1 Create
		 * ContactRequest adam to eve Contacts adam and abel
		 */
		try {
			stmnt = conn.prepareStatement("INSERT INTO AccountProfile VALUES (\"" + getAdamName()
					+ "\", \"test@mail.de\", 2222, \"test\", \"NewNickName\", 1), (\"" + getEveName()
					+ "\", \"test\", 2222, \"test\", \"test\", 0), (\"" + getAbelName()
					+ "\", \"test@mail.de\", 2222, \"test\", \"NewNickName\", 1);");
			int rows = stmnt.executeUpdate();

			stmnt = conn.prepareStatement("INSERT INTO Contact (FirstUser, SecondUser) VALUES (\"" + getAdamName()
					+ "\", \"" + getEveName() + "\");");
			rows = stmnt.executeUpdate();

			stmnt = conn
					.prepareStatement("INSERT INTO Message (MessageID, Message, MessageTimeStamp, WasRead) VALUES (42, \"Hey Nachricht yo\",\"2014-01-01 00:00:00\", 1);");
			rows = stmnt.executeUpdate();

			stmnt = conn
					.prepareStatement("INSERT INTO SendingSingle (Sender, Receiver, MessageID) VALUES (\"adam\", \"eve1st\", 42);");
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

	@After
	public void deleteData() throws Exception {

		Connection conn = null;
		PreparedStatement stmnt = null;
		conn = dbm.getConnection();

		try {
			stmnt = conn.prepareStatement("DELETE FROM SendingSingle WHERE Sender='" + getAdamName() + "' OR Sender='"
					+ getEveName() + "' OR Sender = '" + getAbelName() + "'");
			int rows = stmnt.executeUpdate();

			stmnt = conn.prepareStatement("DELETE FROM Message WHERE MessageTimeStamp=\"2014-01-01 00:00:00\"");
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

		super.deleteData();
	}

	/**
	 * Test POST SingleMessage testAgent schickt eine Nachricht an testAgent3
	 * test testAgnet 3 sollte sie erhalten
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSendSingleMessage() {
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);

		try {
			c.setLogin(getAdamID(), adamPass);
			ClientResponse result = c.sendRequest("POST", mainPath + "message/single/" + getEveName(),
					"{\"message\":\"Hey Eve ;)\", \"timestamp\":\"2014-1-1 00:00:00.000 \"}", "application/json",
					"*/*", new Pair[] {});
			assertEquals(200, result.getHttpCode());
			System.out.println("Result of 'testSendSingleMessage': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}

	}

	/**
	 * Test GET SingleMessage testAgent3 überprüft seine Nachrichten test
	 * testAgent3 sollte eine von testAgent erhalten haben
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetSingleMessage() {
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);

		try {
			c.setLogin(getEveID(), evePass);
			ClientResponse result = c.sendRequest("GET", mainPath + "message/single/" + getAdamName(), "", "*/*",
					"application/json", new Pair[] {});
			assertEquals(200, result.getHttpCode());
			assertTrue(result.getResponse().trim().contains("Hey"));
			System.out.println("Result of 'testGetSingleMessage': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}

	}

}

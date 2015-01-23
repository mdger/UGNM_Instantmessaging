package i5.las2peer.services.IMService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import i5.las2peer.restMapper.data.Pair;
import i5.las2peer.webConnector.client.ClientResponse;
import i5.las2peer.webConnector.client.MiniClient;

import org.junit.Test;

public class ProfileTest extends ServiceTest {

	/**
	 * Test POST Profile Erstellt ein Profil für testAgent.
	 * 
	 * test Wird ein Falsch Formatierten JSON erkannt. test Wird erfolgreich ein
	 * Profil erstellt. test Lässt sich ein zweites Profil für testAgent
	 * anlegen (sollte nicht sein)
	 * 
	 * @throws Exception
	 */

	@Test
	public void testCreateProfile() {
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);

		try {

			c.setLogin(getAbelID(), abelPass);

			ClientResponse result3 = c.sendRequest("POST", mainPath + "profile", "{\"Irgendwas\":\"Anderes\"}",
					"application/json", "*/*", new Pair[] {});
			assertEquals(400, result3.getHttpCode());
			System.out.println("'CreateProfile'-Falsches Format erkannt: " + result3.getResponse().trim());

			ClientResponse result = c
					.sendRequest(
							"POST",
							mainPath + "profile",
							"{\"username\":\"abel\",\"email\":\"test@mail.de\", \"telephone\":123456, \"imageLink\":\"imageUrl\", \"nickname\":\"TestNickName\", \"visible\":1}",
							"application/json", "*/*", new Pair[] {});
			assertEquals(200, result.getHttpCode());
			assertTrue(existsInDatabase("AccountProfile", "UserName", getAbelName()));
			System.out.println("'CreateProfile'-Profil für testAgent erstellt: " + result.getResponse().trim());

			ClientResponse result2 = c
					.sendRequest(
							"POST",
							mainPath + "profile",
							"{\"username\":\"abel\",\"email\":\"ntest@mail.de\", \"telephone\":111111, \"imageLink\":\"imageUrl\", \"nickname\":\"TestNickName\", \"visible\":1}",
							"application/json", "*/*", new Pair[] {});
			assertEquals(409, result2.getHttpCode());
			System.out.println("'CreateProfile'-Kein Zweites konnte angelegt werden" + result2.getResponse().trim());

		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}
	}

	/**
	 * Test UPDATE Profile 
	 * 
	 * test updateProfile of adam as active with invalid json content - response 400
	 * test updateProfile of adam as active - response 200
	 * 
	 * @throws Exception
	 */

	@Test
	public void testUpdateProfile() {
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);

		try {
			c.setLogin(getAdamID(), adamPass);

			ClientResponse result3 = c.sendRequest("PUT", mainPath + "profile", "{\"Irgendwas\":\"Anderes\"}",
					"application/json", "*/*", new Pair[] {});
			assertEquals(400, result3.getHttpCode());
			System.out.println("'UpdateProfile'-wrong entry detected: " + result3.getResponse().trim());

	         ClientResponse result2 = c.sendRequest("PUT", mainPath + "profile", "{\"userName\":\"adam\", \"email\":\"test@mail.de\", \"telephone\":1111111, \"imageLink\":\"imageUrl\", \"nickname\":\"NewNickName\", \"visible\":1}",
                 "application/json", "*/*", new Pair[] {});
	         assertEquals(200, result2.getHttpCode());
	         assertTrue(existsInDatabase("AccountProfile", "UserName", "NickName", getAdamName(), "NewNickName"));
	         System.out.println("'UpdateProfile'-Succesfully updated: " + result2.getResponse().trim());

		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}

	}

	/**
	 * Test GET Profile Fragt nach dem Profil von testAgent.
	 * 
	 * test Wird es ordentlich angezeigt
	 * 
	 * @throws Exception
	 */

	@Test
	public void testGetProfile() {
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);

		try {
			c.setLogin(getAdamID(), adamPass);

			ClientResponse result = c.sendRequest("GET", mainPath + "profile/adam", "", "*/*", "application/json",
					new Pair[] {});
			assertEquals(200, result.getHttpCode());
			assertTrue(result.getResponse().trim().contains("NickName"));
			assertTrue(result.getResponse().trim().contains("test@mail.de"));
			System.out.println("Result of 'testGetProfile': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}

	}

	@Test
	public void testDeleteProfile() {
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);

		try {
			c.setLogin(getEveID(), evePass);
			ClientResponse result = c.sendRequest("DELETE", mainPath + "profile", "", "*/*", "*/*", new Pair[] {});
			assertEquals(200, result.getHttpCode());
			assertFalse(existsInDatabase("AccountProfile", "UserName", getEveName()));
			System.out.println("Result of 'testUpdateProfile': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}

	}

}

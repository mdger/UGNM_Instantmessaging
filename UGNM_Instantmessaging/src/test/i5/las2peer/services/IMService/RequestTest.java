package i5.las2peer.services.IMService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import i5.las2peer.restMapper.data.Pair;
import i5.las2peer.webConnector.client.ClientResponse;
import i5.las2peer.webConnector.client.MiniClient;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class RequestTest extends ServiceTest {

  @Before
  public void writeTestData() throws Exception {

    // DataBase Connection

    Connection conn = null;
    PreparedStatement stmnt = null;
    conn = dbm.getConnection();

    /**
     * Create Profiles eve visibility = 0 adam visibility = 1 Create ContactRequest adam to eve
     */
    try {
      stmnt =
          conn.prepareStatement("INSERT INTO AccountProfile VALUES (\"" + getAdamName()
              + "\", \"test@mail.de\", 2222, \"test\", \"NewNickName\", 1), (\"" + getEveName()
              + "\", \"test\", 2222, \"test\", \"test\", 0), (\"" + getAbelName()
              + "\", \"test@mail.de\", 2222, \"test\", \"NewNickName\", 1);");
      int rows = stmnt.executeUpdate();

      stmnt =
          conn.prepareStatement("INSERT INTO ContactRequest (Sender, Receiver) VALUES (\""
              + getAdamName() + "\", \"" + getEveName() + "\");");
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

  /**
   * Test POST Request testAgent schickt eine Contact-Request an testAgent2 testAgent schickt eine
   * Contact-Request an testAgent3
   * 
   * test Kommen sie erfolgreich an
   * 
   * @throws Exception
   */
  @Ignore
  public void testCreateRequest() {
    MiniClient c = new MiniClient();
    c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);

    try {
      c.setLogin(getEveID(), evePass);
      ClientResponse result =
          c.sendRequest("POST", mainPath + "profile/contact/request", "{\"username\":\""
              + getAbelName() + "\"}", "application/json", "*/*", new Pair[] {});
      assertEquals(200, result.getHttpCode());
      System.out.println("Result of 'testCreateRequest': " + result.getResponse().trim());
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception: " + e);
    }
  }

  /**
   * Test GET Request testAgent2 überprüft seine Contact-Requests
   * 
   * test Kann er sie sehen
   * 
   * @throws Exception
   */
  @Test
  public void testGetRequest() {
    MiniClient c = new MiniClient();
    c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);

    try {
      c.setLogin(getEveID(), evePass);
      ClientResponse result =
          c.sendRequest("GET", mainPath + "profile/contact/request", "", "*/*", "application/json",
              new Pair[] {});
      assertEquals(200, result.getHttpCode());
      System.out.println("Result of 'testGetRequest': " + result.getResponse().trim());

    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception: " + e);
    }
  }

  /**
   * Test DELETE Request Eve delete the Request from Adam
   * 
   * @throws Exception
   */
  @Test
  public void testDeleteRequest() {
    MiniClient c = new MiniClient();
    c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);

    try {
      c.setLogin(getEveID(), evePass);
      ClientResponse result =
          c.sendRequest("DELETE", mainPath + "profile/contact/request", "{\"username\":\"" + getAbelName() + "\"}", "application/json", "*/*", new Pair[] {});
      assertEquals(200, result.getHttpCode());
      System.out.println("Result of 'testDeleteRequest': " + result.getResponse().trim());
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception: " + e);
    }
  }

}

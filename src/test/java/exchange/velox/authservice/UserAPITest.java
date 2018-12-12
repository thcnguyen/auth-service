package exchange.velox.authservice;

import exchange.velox.authservice.dto.UserRole;
import exchange.velox.authservice.dto.UserSessionDTO;
import exchange.velox.authservice.util.JsonUtils;
import net.etalia.crepuscolo.utils.HandledHttpException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;

public class UserAPITest extends SpringBootBaseWebTest {

    @Autowired
    private RestClient client;

    @Before
    public void setUp() {
    }

    @Test
    public void login() throws Exception {
        // admin login
        UserSessionDTO admin = _login("admin@velox.exchange", "admin");
        Assert.assertNotNull(admin);
        Assert.assertEquals(UserRole.ADMIN.name(), admin.getRole());
        Assert.assertEquals(0, admin.getPermissions().size());

        // CA login
        UserSessionDTO credit = _login("credit@velox.exchange", "credit");
        Assert.assertNotNull(credit);
        Assert.assertEquals(UserRole.CREDIT_ANALYST.name(), credit.getRole());
        Assert.assertEquals(0, credit.getPermissions().size());

        // data1 login
        UserSessionDTO data1 = _login("data1@velox.exchange", "data1");
        Assert.assertNotNull(data1);
        Assert.assertEquals(UserRole.DATA_ENTRY.name(), data1.getRole());
        Assert.assertEquals(0, data1.getPermissions().size());

        // seller login
        UserSessionDTO _seller1 = _login("foo.bar@velox.exchange", "foo.bar");
        Assert.assertNotNull(_seller1);
        Assert.assertEquals(UserRole.SELLER.name(), _seller1.getRole());
        Assert.assertEquals(4, _seller1.getPermissions().size());

        // bidder individual
        UserSessionDTO bidderIndividual = _login("bidder.individual@velox.exchange", "bidder.individual");
        Assert.assertNotNull(bidderIndividual);
        Assert.assertEquals(UserRole.BIDDER.name(), bidderIndividual.getRole());
        Assert.assertTrue(
                    bidderIndividual.getPermissions().containsAll(Arrays.asList("WITHDRAW", "INVITE_USER", "BID")));

        // bidder company login
        UserSessionDTO bidderCompany = _login("bidder.company@velox.exchange", "bidder.company");
        Assert.assertNotNull(bidderCompany);
        Assert.assertEquals(UserRole.BIDDER.name(), bidderCompany.getRole());
        Assert.assertTrue(bidderCompany.getPermissions().containsAll(Arrays.asList("WITHDRAW", "INVITE_USER", "BID")));
    }

    @Test
    public void givenValidToken_WhenCheckVaildToken_ThenStatus200() throws Exception {
        // seller login
        UserSessionDTO _seller1 = _login("foo.bar@velox.exchange", "foo.bar");
        Assert.assertNotNull(_seller1);
        Assert.assertEquals(UserRole.SELLER.name(), _seller1.getRole());
        Assert.assertEquals(4, _seller1.getPermissions().size());
        String token = _seller1.getToken();
        Assert.assertNotNull(_checkTokenValid(token));
    }

    @Test
    public void givenInvalidToken_WhenCheckVaildToken_ThenStatus401() {
        String token = "da3404fe967ee2abacc59e1737b644f7ee1066ab4afd1d717415a52108377146a82bf78b6418af024423867923ed438" +
                    "eed04f74d709c93a429880de27a008fcc372ef9dc45cb5c83076f874763557334963d70d6dc34a5c93eff4d302c6001f87abcf894";
        try {
            _checkTokenValid(token);
        } catch (HttpClientErrorException e) {
            Assert.assertEquals(e.getStatusCode().value(), 401);
            Assert.assertEquals(JsonUtils.readToMap(e.getResponseBodyAsString()).get("message"), "Invalid Token");

        }
    }

    @Test
    public void givenValidToken_WhenLogout_ThenStatus204() throws Exception {
        // seller login
        UserSessionDTO _seller1 = _login("foo.bar@velox.exchange", "foo.bar");
        Assert.assertNotNull(_seller1);
        Assert.assertEquals(UserRole.SELLER.name(), _seller1.getRole());
        Assert.assertEquals(4, _seller1.getPermissions().size());
        _logout(_seller1.getToken());
    }

    @Test
    public void givenInvalidToken_WhenLogout_ThenStatus404() throws Exception {
        String token = "da3404fe967ee2abacc59e1737b644f7ee1066ab4afd1d717415a52108377146a82bf78b6418af024423867923ed438" +
                    "eed04f74d709c93a429880de27a008fcc372ef9dc45cb5c83076f874763557334963d70d6dc34a5c93eff4d302c6001f87abcf894";
        try {
            _logout(token);
        } catch (HttpClientErrorException e) {
            Assert.assertEquals(e.getStatusCode().value(), 404);
            Assert.assertEquals(JsonUtils.readToMap(e.getResponseBodyAsString()).get("message"), "Not Found");
        }
    }

    protected UserSessionDTO _login(String email, String plainPassword) throws Exception {
        String authorization = "Velox_" + email + ":" + md5(plainPassword);
        return client.login(authorization);
    }

    protected UserSessionDTO _checkTokenValid(String token) throws HandledHttpException {
        return client.token(token);
    }

    protected void _logout(String token) {
        client.logout(token);
    }
}

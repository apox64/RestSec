import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static io.restassured.authentication.FormAuthConfig.springSecurity;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

public class JuiceShop {

    // LOGIN CREDENTIALS
    private String email = "admin@juice-sh.op";
    private String password = "admin123";

    @Before
    public void initTarget() {
        RestAssured.baseURI = "http://192.168.99.100";
        RestAssured.port = 32769;
        RestAssured.basePath = "/rest"; //Leave this as "/rest" for juice-shop
        RestAssured.proxy("127.0.0.1",8080); //Comment out, if you don't use a proxy
    }

    @Test
    //TODO: write better GET test to see if target is reachable. if not, then break and end all other tests
    public void GET_targetOnline() {
        RestAssured.given().
                when().
                get("/product/search").
                then().
                statusCode(200);
    }

    @Test
    public void GET_simpleSearch() {
        RestAssured.given().
                param("q","orange").
                when().
                get("/product/search").
                then().
                statusCode(200).
                body("data[0].name", Matchers.equalTo("Orange Juice (1000ml)")).
                body("data[0].price", equalTo(2.99f));
    }

    @Ignore("JSON Schema not adapted yet.")
    @Test
    //TODO: WRITE PROPER JSON-SCHEMA TO MATCH ONLY CORRECT ANSWERS FROM JUICESHOP
    public void GET_responseMatchesProductSchema() {
        RestAssured.given().
                param("q","orange").
                when().
                get("/product/search").
                then().
                assertThat().
                body(matchesJsonSchemaInClasspath("juiceshop-product-schema.json"));
    }

    @Ignore("Not implemented yet.")
    @Test
    //TODO: RETURNS TRUE?
    public void GET_cookies() {
        Response response = when().get("/user/login");
        // Get all cookies as simple name-value pairs
        Map<String, String> allCookies = response.getCookies();
        // Get a single cookie value:
        //String cookieValue = response.getCookie("cookieName");
    }

    @Test
    public void POST_basicAuthHTTPBody() {
        RestAssured.given().
                request().body("{\"email\":\""+email+"\",\"password\":\""+password+"\"}").
                contentType(ContentType.JSON).
                when().
                post("/user/login").
                then().
                statusCode(200);
    }

    @Ignore("Use this test only if you do basic auth via HTTP Header.")
    @Test
    public void POST_basicAuthHTTPHeader() {
        RestAssured.given().
                auth().preemptive().basic(email,password).
                when().
                post("/user/login").
                then().
                statusCode(200);
    }

    @Ignore("Not implemented yet.")
    @Test
    public void GET_CSRFProtection() {
        /*
        given().
                auth().form("John", "Doe", formAuthConfig().withAutoDetectionOfCsrf()).
        when().
                get("/formAuth").
        then().
                statusCode(200);
        */

        given().
                auth().form("John", "Doe", springSecurity().withCsrfFieldName("_csrf")).
                when().
                get("/formAuth").
                then().
                statusCode(200);

    }
}
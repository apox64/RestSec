import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static io.restassured.authentication.FormAuthConfig.springSecurity;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

//@DisplayName("Testing OWASP JuiceShop")
public class JuiceShop {

    private String email;
    private String password;

    @Before
    public void initTarget() throws IOException {

        Properties properties = new Properties();

        try(InputStream stream = getClass().getClassLoader().getResourceAsStream("config.properties")){
            //InputStreamReader isr = new InputStreamReader(stream, "UTF-8");
            //properties.load(isr);
            properties.load(stream);
        }

        RestAssured.baseURI = properties.getProperty("base-uri");
        RestAssured.port = Integer.parseInt(properties.getProperty("port"));
        RestAssured.basePath = properties.getProperty("base-path");
        RestAssured.proxy("127.0.0.1",8080); //Comment out, if you don't use a proxy

        this.email = properties.getProperty("username");
        this.password = properties.getProperty("password");

        this.password = new String(password.getBytes("ISO-8859-1"),"UTF-8");

    }

    @Test
    //@DisplayName("Is specified juice-shop online?")
    //TODO: write better GET test to see if target is reachable. if not, then break and end all other tests
    public void targetOnline() {
        RestAssured.given().
                when().
                get("/product/search").
                then().
                statusCode(200);
    }

    @Test
    //@DisplayName("performing simple search with expected result")
    public void simpleSearchMatchesExpectedResult() {
        RestAssured.given().
                param("q","orange").
                when().
                get("/product/search").
                then().
                statusCode(200).
                body("data[0].name", Matchers.equalTo("Orange Juice (1000ml)")).
                body("data[0].price", equalTo(2.99f));
    }

    @Test
    //@DisplayName("matching response to defined JSON schema")
    public void responseMatchesJSONProductSchema() {
        RestAssured.given().
                param("q","orange").
                when().
                get("/product/search").
                then().
                assertThat().
                body(matchesJsonSchemaInClasspath("juiceshop-product-schema.json"));
    }

    @Test
    //@DisplayName("authenticating: basic, JSON, HTTP body")
    public void authBasicJSONHTTPBody() {
        RestAssured.given().
                request().body("{\"email\":\""+email+"\",\"password\":\""+password+"\"}").
                contentType(ContentType.JSON).
                when().
                post("/user/login").
                then().
                statusCode(200);
    }

    @Ignore("juice-shop doesn't authenticate via HTTP header.")
    @Test
    //TODO: Move to different file.
    //@DisplayName("authenticating: basic, base64, HTTP header")
    public void authBasicBase64HTTPHeader() {
        RestAssured.given().
                auth().preemptive().basic(email,password).
                when().
                post("/user/login").
                then().
                statusCode(200);
    }

    @Ignore("Not implemented yet.")
    @Test
    //@DisplayName("getting cookies")
    public void showCookies() {
        Response response = when().get("/user/login");
        // Get all cookies as simple name-value pairs
        Map<String, String> allCookies = response.getCookies();
        // Get a single cookie value:
        //String cookieValue = response.getCookie("cookieName");
    }

    @Ignore("Not implemented yet.")
    @Test
    //@DisplayName("looking for CSRF protection cookie")
    public void csrfProtectionTokenReturned() {
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
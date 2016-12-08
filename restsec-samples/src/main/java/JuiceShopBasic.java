import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;

//@DisplayName("Testing OWASP JuiceShop v.2.18.0")
public class JuiceShopBasic {

    private static String email;
    private static String password;
    private static String loginPath = "rest/user/login";

    @BeforeClass
    //NOTE: Load Properties (set URL, Port, Proxy, Admin-Login)
    public static void initTarget() throws IOException {

        Properties properties = new Properties();

        try(InputStream stream = JuiceShopBasic.class.getClassLoader().getResourceAsStream("config.properties")){
            properties.load(stream);
        }

        // Load config
        RestAssured.baseURI = properties.getProperty("base-uri");
        RestAssured.port = Integer.parseInt(properties.getProperty("port"));
        RestAssured.basePath = properties.getProperty("base-path");

        if (!properties.getProperty("proxy_ip").equals("")) {
            RestAssured.proxy(properties.getProperty("proxy_ip"), Integer.parseInt(properties.getProperty("proxy_port")));
        }

        email = properties.getProperty("username");
        password = properties.getProperty("password");
    }

    @Test
    //@DisplayName("does api respond?")
    public void apiReachable() {
        RestAssured.given().
                when().
                get("rest/product/search").
                then().
                statusCode(200);
    }

    @Test
    //@DisplayName("performing simple search with expected result")
    public void searchMatchesExpectedResult() {
        RestAssured.given().
                param("q","orange").
                when().
                get("rest/product/search").
                then().
                statusCode(200).
                body("data[0].name", Matchers.equalTo("Orange Juice (1000ml)")).
                body("data[0].price", equalTo(2.99f));
    }

    @Test
    //@DisplayName("matching response to defined JSON schema")
    public void responseMatchesJSONSchema() {
        RestAssured.given().
                param("q","orange").
                when().
                get("rest/product/search").
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
                post("rest/user/login").
                then().
                statusCode(200);
    }

    @Test(expected = AssertionError.class)
    //NOTE: Checks only if payload can be stored, not if it's executed.
    public void xssStoredPayloadAccepted_Simple_Tier3() {

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

        given().
                request().
                    body(payloadXXS3).
                    contentType(ContentType.JSON).
                when().
                    put("api/Products/1").
                then().
                    statusCode(200);
                // XXS Payload was accepted by the server.

        System.err.println(">>> Server accepted the payload : 200 OK");

    }

    @Test(expected = AssertionError.class)
    //NOTE: Checks only if payload can be stored, not if it's executed.
    public void xssStoredPayloadAccepted_Nested_Tier4() {

        String payloadXXS4 = "{\"comment\":\"<<script>alert(\\\"XSS4\\\")</script>script>alert(\\\"XSS4\\\")<</script>/script>\",\"rating\":0}";

        given().
                request().
                body(payloadXXS4).
                contentType(ContentType.JSON).
                when().
                post("api/Feedbacks/").
                then().
                statusCode(200);

        System.err.println(">>> Server accepted the payload : 200 OK");

        // <<script>alert("XSS4")</script>script>alert("XSS4")<</script>/script>
    }

    @Ignore
    @Test(expected = AssertionError.class)
    //NOTE: No Content-Type header at all. 401/406 expected.
    //TODO: rest-assured always adds a content-type header (text/plain)
    public void noContentTypeHeader(){
        given().
                request().
                body("{\"email\":\""+email+"\",\"password\":\""+password+"\"}").
                when().
                post(loginPath).
                then().
                statusCode(200);
        System.err.println(">>> Server accepted not using a content-type header : 200 OK");
    }

    @Test(expected = AssertionError.class)
    //NOTE: Gives a Content-Type Header, but sends other data.
    public void mismatchingContentTypeHeader(){
        given().
                request().
                body("{\"email\":\""+email+"\",\"password\":\""+password+"\"}").
                contentType(ContentType.URLENC).
                when().
                post(loginPath).
                then().
                statusCode(200);
        System.err.println(">>> Server accepted the mismatching ContentType Header with the actual content : 200 OK");
    }

    @Test
    //NOTE: Does the server copy the clients Accept-Header in the response Content-Type? 401/406 expected.
    public void invalidAcceptHeaderReflected() {

        Response response = given().
                request().
                header("Accept","myHeader").
                body("{\"email\":\""+email+"\",\"password\":\""+password+"\"}").
                contentType(ContentType.JSON).
                when().
                post(loginPath);

        if (response.getHeader("Content-Type").equals("myHeader")) {
            Assert.fail("Fake Request Accept-Header was reflected in Response Content-Type. Missing server-side validation?");
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

}

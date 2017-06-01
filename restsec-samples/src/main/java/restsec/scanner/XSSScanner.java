package restsec.scanner;

import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restsec.AttackSet;
import restsec.Authentication;
import restsec.CallbackServer;
import restsec.config.Configuration;
import restsec.Evaluator;
import restsec.config.ScannerType;

import java.io.FileReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;

public class XSSScanner implements Scanner {

    private static Configuration config = new Configuration();
    private static final Logger LOGGER = LoggerFactory.getLogger(XSSScanner.class);
    private CallbackServer callbackServer = new CallbackServer();

    private String attackSetFile = "";
    private String payloadsFile = "";
    private JSONObject payloads = new JSONObject();

    XSSScanner(String attackSetFile, String payloadsFile) {
            this.attackSetFile = attackSetFile;
            this.payloadsFile = payloadsFile;
    }

    @Override
    public void scan(String targetURL, AttackSet attackSet) {

        JSONParser parser = new JSONParser();
        try{
            LOGGER.info(attackSet.size()+" attackable endpoint(s) loaded from file: "+attackSetFile);
            payloads = (JSONObject) parser.parse(new FileReader(payloadsFile));
            LOGGER.info(payloads.size() + " payload(s) loaded from file: " + payloadsFile);
            LOGGER.info("--> " + attackSet.size() * payloads.size() + " total attacks");
        } catch (Exception e) {
            e.printStackTrace();
        }

        LOGGER.info("Trying XSS payloads ...");

        callbackServer.startCallbackServer();

        for (Object attackPoint : attackSet.keySet()) {
            String endpoint = attackPoint.toString();
            JSONArray httpVerbsArray = (JSONArray) attackSet.get(endpoint);
            String host = config.getBaseURI()+":"+config.getPort();
            tryVerbsForGivenURL(host+endpoint, httpVerbsArray);
        }

        ScannerUtils.printPackageStatistics(ScannerType.XSS);

        callbackServer.stopCallbackServer();

    }

    private void tryVerbsForGivenURL(String url, JSONArray httpVerbsArray) {
        for (Object httpVerbsObject : httpVerbsArray) {
            String httpVerb = (String) httpVerbsObject;
            tryAllPayloadsForGivenVerb(url, httpVerb);
        }
    }

    private void tryAllPayloadsForGivenVerb(String url, String httpVerb) {

        for (Object payloadObject : payloads.keySet()) {
            String payloadID = (String) payloadObject;

            // unescaping forward slashes for payload: replacing \/ with /
            String payload = payloads.get(payloadID).toString().replace("\\/","/");

            // Filtering endpoints with curly brackets (numbers) - Not supported yet.
            if (url.contains("{")) {
                LOGGER.info("Skipping " + url + " (Curly bracket not yet implemented!");
            } else {
                LOGGER.info("Trying " + httpVerb + " on " + url + " (Payload: \"" + payloadID + "\") ... ");
                ScannerUtils.numberOfSentPackets++;
                try {
                    payload = updateXSSPayloadWithCallbackValues(payload);
                    sendPacket(url, httpVerb, payload);
                    LOGGER.info("Accepted.");
                    ScannerUtils.acceptedPackets++;
                    if (callbackServer.hasAlertOnReload(config.getBaseURI()+":"+config.getPort()+"/")) {
                        Evaluator.writeVulnerabilityToResultsFile("XSS (alert)", url, payload, "-");
                    }
                } catch (AssertionError ae) {
                    LOGGER.info("Rejected. (Server Status Code does not match expected Code)");
                    ScannerUtils.rejectedPackets++;
                } catch (ConnectException ce) {
                    if (config.getBoolUseProxy()) {
                        LOGGER.warn("Connection timed out. Proxy and Target reachable?");
                    } else {
                        LOGGER.warn("Connection timed out. Target reachable?");
                    }
                    System.exit(0);
                }
            }
        }
    }

    private void sendPacket(String url, String httpMethod, String payload) throws ConnectException {

        String juiceToken = Authentication.getIDTokenForJuiceShop_BodyAuth();

        int expectedResponseCode = 200;

        switch (httpMethod) {
            case "POST":
                given().
                        header(new Header("Authorization", "Bearer "+juiceToken)).cookie("token", juiceToken).
                        request().
                        body(payload).
                        contentType(ContentType.JSON).
                        when().
                        post(url).
                        then().
                        statusCode(expectedResponseCode);
                break;
            case "PATCH":
                given().
                        header(new Header("Authorization", "Bearer "+juiceToken)).cookie("token", juiceToken).
                        request().
                        body(payload).
                        contentType(ContentType.JSON).
                        when().
                        patch(url).
                        then().
                        statusCode(expectedResponseCode);
                break;
            case "PUT":
                given().
                        header(new Header("Authorization", "Bearer "+juiceToken)).cookie("token", juiceToken).
                        request().
                        body(payload).
                        contentType(ContentType.JSON).
                        when().
                        put(url).
                        then().
                        statusCode(expectedResponseCode);
                break;
            case "DELETE":
                given().
                        header(new Header("Authorization", "Bearer "+juiceToken)).cookie("token", juiceToken).
                        request().
                        body(payload).
                        contentType(ContentType.JSON).
                        when().
                        delete(url).
                        then().
                        statusCode(expectedResponseCode);
                break;
            default:
                LOGGER.warn("Requested HTTP method not implemented.");
        }
    }

    public static String updateXSSPayloadWithCallbackValues(String payload){
        String regex = "script.*(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d{1,5}).*script";
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(payload);
        while (matcher.find()) {
            try {
                payload = payload.replace(matcher.group(1), String.valueOf(InetAddress.getLocalHost().getHostAddress()));
            } catch (UnknownHostException uhe) {
                uhe.printStackTrace();
            }
            payload = payload.replace(matcher.group(2), String.valueOf(config.getJettyCallbackPort()));
        }
        LOGGER.info("Payload updated with Callback Server IP and port.");
        return payload;
    }
}

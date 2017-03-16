package restsec;

import io.restassured.http.ContentType;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;

class Scanner {

    private static final Logger logger = Logger.getLogger(Scanner.class);

    private String attackSetFile = "";
    private JSONObject attackSet = new JSONObject();
    private String payloadsFile = "";
    private JSONObject payloads = new JSONObject();
    private final CallbackPage callbackPage = new CallbackPage();
    private String scanFor = "";
    private int numberOfSentPackets = 0;
    private int acceptedPackets = 0;
    private int rejectedPackets = 0;

    private static Configuration config;

    Scanner(String attackSetFile, String payloadsFile, String scanFor) {
        this.attackSetFile = attackSetFile;
        this.payloadsFile = payloadsFile;
        this.scanFor = scanFor;
        config = new Configuration();
    }

    public void scan() {
        JSONParser parser = new JSONParser();
        try{
            //noinspection ConstantConditions
            attackSet = (JSONObject) parser.parse(new FileReader(attackSetFile));

            int attackSetSize = 0;
            for (Object key : attackSet.keySet()) {
                JSONArray httpMethods = (JSONArray) attackSet.get(key);
                attackSetSize += httpMethods.size();
            }

            logger.info(attackSet.size()+" attackable endpoint(s) loaded from file: "+attackSetFile);
            //noinspection ConstantConditions
            payloads = (JSONObject) parser.parse(new FileReader(payloadsFile));
            logger.info(payloads.size()+" payload(s) loaded from file: "+payloadsFile);
            logger.info("--> "+attackSetSize*payloads.size()+" total attacks");
        } catch (Exception e) {
            e.printStackTrace();
        }

        switch (scanFor.toLowerCase()) {
            case "xss":
                scanXSS();
                break;
            case "sqli":
                scanSQLi();
                break;
            case "all":
                scanXSS();
                scanSQLi();
            default:
                logger.error("Unknown scan type.");
                break;
        }
    }

    private void scanXSS() {
        logger.info("Trying XSS payloads ...");

        callbackPage.startTestPageServer();

        for (Object attackPoint : attackSet.keySet()) {
            String endpoint = attackPoint.toString();
            JSONArray httpVerbsArray = (JSONArray) attackSet.get(endpoint);
            String host = config.getBaseURI()+":"+config.getPort();
            tryVerbsForGivenURL(host+endpoint, httpVerbsArray);
        }

        printPackageStatistics();

        callbackPage.stopTestPageServer();

    }

    private void scanSQLi(){
        logger.info("Trying SQLi ...");
        int numberOfSentPackets = 0;
        int acceptedPackets = 0;
        int rejectedPackets = 0;

        System.out.println("----------------------------\nrestsec.Scanner: Stats for SQLi Scan:");
        //noinspection ConstantConditions
        if (numberOfSentPackets == 0) {
            System.out.println("No packets sent.");
        } else {
            System.out.println("----------------------------\n"+acceptedPackets+" packets accepted. "+
                    rejectedPackets+" packets rejected. ("+(acceptedPackets/numberOfSentPackets)+"% accepted).");
        }
        System.out.println("----------------------------");
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
                logger.info("Skipping " + url + " (Curly bracket not yet implemented!");
            } else {
                logger.info("Trying " + httpVerb + " on " + url + " (Payload: \"" + payloadID + "\") ... ");
                numberOfSentPackets++;
                try {
                    payload = updatePayloadWithCallbackValues(payload);
                    sendPacket(url, httpVerb, payload);
                    logger.info("Accepted.");
                    acceptedPackets++;
                    if (callbackPage.hasAlertOnReload(config.getBaseURI()+":"+config.getPort()+"/")) {
                        Evaluator.writeVulnerabilityToFile("XSS (alert)", url, payload, "-");
                    }
                } catch (AssertionError ae) {
                    logger.info("Rejected. (Server Status Code does not match expected Code)");
                    rejectedPackets++;
                } catch (ConnectException ce) {
                    if (config.getBoolUseProxy()) {
                        logger.fatal("Connection timed out. Proxy and Target reachable?");
                    } else {
                        logger.fatal("Connection timed out. Target reachable?");
                    }
                    System.exit(0);
                }
            }
        }
    }

    private void sendPacket(String url, String httpMethod, String payload) throws ConnectException {

            int expectedResponseCode = 200;

            switch (httpMethod) {
                case "POST":
                    given().
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
                            request().
                            body(payload).
                            contentType(ContentType.JSON).
                            when().
                            delete(url).
                            then().
                            statusCode(expectedResponseCode);
                    break;
                default:
                    logger.warn("Requested HTTP method not implemented.");
            }
    }

    private void printPackageStatistics() {
        System.out.println("----------------------------\nrestsec.Scanner: Stats for XSS Scan:\n----------------------------\n" + numberOfSentPackets + " packets sent. ");
        if (numberOfSentPackets != 0) {
            System.out.println(acceptedPackets + " packets accepted. (" + (acceptedPackets * 100 / numberOfSentPackets) + "%).\n----------------------------");
        }
        System.out.println("----------------------------");
    }

    private String updatePayloadWithCallbackValues(String payload){
        String regex = "script.*(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d*).*script";
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
        return payload;
    }

}

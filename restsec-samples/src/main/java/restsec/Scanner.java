package restsec;// restsec.Scanner needs input:
// - Attack Set (from restsec.Parser, etc.)
// - Payloads (from payloads/)

import io.restassured.http.ContentType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static io.restassured.RestAssured.given;

class Scanner implements Runnable {

    private JSONObject attackSet = new JSONObject();
    private JSONObject payloads = new JSONObject();
    private final CallbackPage callbackPage = new CallbackPage();
    private String attackSetFile = "";
    private String payloadsFile = "";
    private String scanFor = "";

    private String baseURL = "http://127.0.0.1:80";

    Scanner(String attackSetFile, String payloadsFile, String scanFor) {
        this.attackSetFile = attackSetFile;
        this.payloadsFile = payloadsFile;
        this.scanFor = scanFor;
    }

    public void run() {
        JSONParser parser = new JSONParser();
        try{
            //noinspection ConstantConditions
            attackSet = (JSONObject) parser.parse(new FileReader(attackSetFile));

            int attackSetSize = 0;
            for (Object key : attackSet.keySet()) {
                JSONArray httpMethods = (JSONArray) attackSet.get(key);
                attackSetSize += httpMethods.size();
            }

            System.out.println("restsec.Scanner: "+attackSet.size()+" attackable endpoints loaded from file: "+attackSetFile);
            //noinspection ConstantConditions
            payloads = (JSONObject) parser.parse(new FileReader(payloadsFile));
            System.out.println("restsec.Scanner: "+payloads.size()+" payloads loaded from file: "+payloadsFile);
            System.out.println("restsec.Scanner: "+attackSetSize*payloads.size()+" total attacks");
            System.out.println("restsec.Scanner: Loading properties (baseURI, port, basePath, proxy ip, proxy port) ... ");
            loadProperties();
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
                System.err.println("restsec.Scanner: Unknown scan type.");
                break;
        }
    }

    private void loadProperties() throws IOException {
        Properties properties = new Properties();

        try(InputStream stream = Scanner.class.getClassLoader().getResourceAsStream("config.properties")){
            properties.load(stream);
        }

        // Load config
        /*RestAssured.baseURI = properties.getProperty("base-uri");
        RestAssured.port = Integer.parseInt(properties.getProperty("port"));
        RestAssured.basePath = properties.getProperty("base-path");
        */

        this.baseURL = properties.getProperty("base-uri") + ":" + properties.getProperty("port") + properties.getProperty("base-path");

        /*
        if (!properties.getProperty("proxy_ip").equals("")) {
            RestAssured.proxy(properties.getProperty("proxy_ip"), Integer.parseInt(properties.getProperty("proxy_port")));
        }
        */

        System.out.println("Done.");

    }

    private void scanXSS() {
        System.out.println("restsec.Scanner: Trying XSS payloads ...");

        int numberOfSentPackets = 0;
        int acceptedPackets = 0;
        int rejectedPackets = 0;

        //Where the executed payloads will call back to.
        callbackPage.startTestPageServer();

        for (Object o : attackSet.keySet()) {

            String resource = o.toString();

            JSONArray httpVerbsArray = (JSONArray) attackSet.get(resource);

            for (Object aHttpVerbsArray : httpVerbsArray) {
                String httpVerb = (String) aHttpVerbsArray;

                for (Object o1 : payloads.keySet()) {
                    String payloadName = (String) o1;
                    JSONObject payloadObject = (JSONObject) payloads.get(payloadName);

                    // unescaping forward slashes for payload: replacing \/ with /
                    String payload = payloadObject.toString().replace("\\/", "/");

                    // Filtering endpoints with curly brackets (numbers) - Not supported yet.
                    if (resource.contains("{")) {
                        System.out.println("restsec.Scanner: Skipping " + resource + " (Curly bracket not yet implemented!");
                    } else {
                        System.out.println("restsec.Scanner: Trying " + httpVerb + " on " + resource + " (Payload: \"" + payloadName + "\") ... ");
                        numberOfSentPackets++;
                        try {
                            forgeRequest(resource, httpVerb, payload, 200);
                            //System.out.println("Accepted. (200 OK)");
                            System.out.println("Accepted.");
                            acceptedPackets++;
                            //callbackPage.reloadResource(baseURL+resource);
                            if (callbackPage.hasAlertOnReload(baseURL)) {
                                Evaluator.writeVulnerabilityToFile("XSS (alert)", resource, payload, "-");
                            }

                        } catch (AssertionError ae) {
                            //System.err.println(" Rejected. (Server Status Code does not match expected Code)");
                            System.err.println(" Rejected.");
                            rejectedPackets++;
                        }

                    }

                }

            }

        }

        System.out.println("----------------------------\nrestsec.Scanner: Stats for XSS Scan:\n----------------------------\n" + numberOfSentPackets + " packets sent. ");
        if (numberOfSentPackets != 0) {
            System.out.println(acceptedPackets + " packets accepted. (" + (acceptedPackets * 100 / numberOfSentPackets) + "%).\n----------------------------");
        }
        System.out.println("----------------------------");
        callbackPage.stopTestPageServer();

    }

    private void scanSQLi(){
        System.out.println("restsec.Scanner: Trying SQLi ...");
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

    private void forgeRequest(String targetEndpoint, String httpMethod, String payload, int expectedResponseCode) {

        switch (httpMethod) {
            case "POST":
                given().
                        request().
                        body(payload).
                        contentType(ContentType.JSON).
                        when().
                        post(targetEndpoint).
                        then().
                        statusCode(expectedResponseCode);
                break;
            case "PATCH":
                given().
                        request().
                        body(payload).
                        contentType(ContentType.JSON).
                        when().
                        patch(targetEndpoint).
                        then().
                        statusCode(expectedResponseCode);
                break;
            case "PUT":
                given().
                        request().
                        body(payload).
                        contentType(ContentType.JSON).
                        when().
                        put(targetEndpoint).
                        then().
                        statusCode(expectedResponseCode);
                break;
            case "DELETE":
                given().
                        request().
                        body(payload).
                        contentType(ContentType.JSON).
                        when().
                        delete(targetEndpoint).
                        then().
                        statusCode(expectedResponseCode);
                break;
            default:
                System.err.println("Unknown HTTP method.");

        }

    }

}

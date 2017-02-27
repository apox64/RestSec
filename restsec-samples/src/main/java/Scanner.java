// Scanner needs input:
// - Attack Set (from Parser, etc.)
// - Payloads (from payloads/)

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import static io.restassured.RestAssured.defaultParser;
import static io.restassured.RestAssured.given;

public class Scanner {

    private JSONObject attackSet = new JSONObject();
    private JSONObject payloads = new JSONObject();
    private CallbackPage callbackPage = new CallbackPage();

    private String baseURL = "http://127.0.0.1:80";

    public Scanner(String attackSetFile, String payloadsFile) {
        JSONParser parser = new JSONParser();
        try{
            attackSet = (JSONObject) parser.parse(new FileReader(getClass().getClassLoader().getResource(attackSetFile).getFile()));
            System.out.println("Scanner: "+attackSet.size()+" possible attacks loaded from file: "+attackSetFile);
            payloads = (JSONObject) parser.parse(new FileReader(getClass().getClassLoader().getResource(payloadsFile).getFile()));
            System.out.println("Scanner: "+payloads.size()+" payloads loaded from file: "+payloadsFile);
            System.err.println("Scanner: Loading properties (baseURI, port, basePath, proxy ip, proxy port) ... ");
            loadProperties();
        } catch (Exception e) {
            e.printStackTrace();
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


    public void scanXSS() {
        System.out.println("Scanner: Trying XSS payloads ...");

        int numberOfSentPackets = 0;
        int acceptedPackets = 0;
        int rejectedPackets = 0;

        //Where the executed payloads will call back to.
        callbackPage.startTestPageServer();

        Iterator resourceIterator = attackSet.keySet().iterator();

        while (resourceIterator.hasNext()) {

            String resource = resourceIterator.next().toString();

            JSONArray httpVerbsArray = (JSONArray) attackSet.get(resource);

            for (int i = 0; i < httpVerbsArray.size(); i++) {
                String httpVerb = (String) httpVerbsArray.get(i);

                Iterator payloadsIterator = payloads.keySet().iterator();
                while (payloadsIterator.hasNext()) {
                    String payloadName = (String) payloadsIterator.next();
                    JSONObject payloadObject = (JSONObject) payloads.get(payloadName);

                    // unescaping forward slashes for payload: replacing \/ with /
                    String payload = payloadObject.toString().replace("\\/", "/");

                    // Filtering endpoints with curly brackets (numbers) - Not supported yet.
                    if (resource.contains("{")) {
                        System.out.println("Scanner: Skipping " + resource + " (Curly bracket not yet implemented!");
                    } else {
                        System.out.print("Scanner: Trying " + httpVerb + " on " + resource + " (Payload: \"" + payloadName + "\") ... ");
                        numberOfSentPackets++;
                        try {
                            forgeRequest(resource, httpVerb, payload, 200);
                            //System.out.println("Accepted. (200 OK)");
                            System.out.println("Accepted.");
                            acceptedPackets++;
                            //callbackPage.reloadResource(baseURL+resource);
                            callbackPage.reloadResource(baseURL);

                        } catch (AssertionError ae) {
                            //System.err.println(" Rejected. (Server Status Code does not match expected Code)");
                            System.err.println(" Rejected.");
                            rejectedPackets++;
                        }

                    }

                }

            }

        }

        System.out.println("----------------------------\nScanner: Stats for XSS Scan:\n----------------------------\n"+acceptedPackets+" packets accepted. "+
                rejectedPackets+" packets rejected. ("+(acceptedPackets*100/numberOfSentPackets)+"% accepted).\n----------------------------");

        callbackPage.stopTestPageServer();

    }

    public void scanSQLi(){
        System.out.println("Scanner: Trying SQLi ...");
        int numberOfSentPackets = 0;
        int acceptedPackets = 0;
        int rejectedPackets = 0;

        System.out.println("----------------------------\nScanner: Stats for SQLi Scan:");
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

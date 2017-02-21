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
            System.out.print("Scanner: Loading properties (baseURI, port, basePath, proxy ip, proxy port) ... ");
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
        RestAssured.baseURI = properties.getProperty("base-uri");
        RestAssured.port = Integer.parseInt(properties.getProperty("port"));
        RestAssured.basePath = properties.getProperty("base-path");

        this.baseURL = properties.getProperty("base-uri") + ":" + properties.getProperty("port");// + properties.getProperty("base-path");

        if (!properties.getProperty("proxy_ip").equals("")) {
            RestAssured.proxy(properties.getProperty("proxy_ip"), Integer.parseInt(properties.getProperty("proxy_port")));
        }

        System.out.println("Done.");

    }

    public void scanAll(){
        System.out.println("Scanner: Trying XSS payloads ...");
        scanForXSS();
        System.out.println("Scanner: XSS: Done.");
    }

    public void scanForXSS() {
        //iterate through all possible combinations

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
                        System.out.print("Scanner: Trying: " + httpVerb + " on " + resource + " with \"" + payloadName + "\" ... ");
                        try {
                            forgeRequest(resource, payload, 200);
                            System.out.println("Accepted. (200 OK)");

                            //callbackPage.reloadResource(baseURL+resource);
                            //callbackPage.reloadResource(baseURL);
                            callbackPage.reloadResource(baseURL);

                        } catch (AssertionError ae) {
                            System.err.println(" Rejected. (Server Status Code does not match expected Code)");
                        }

                    }

                }

            }

        }

        //Stopping the server again.
        callbackPage.stopTestPageServer();

    }

    //create payload DONE
    //inject payload DONE
    //execute payload
    // --> 1. open jetty server
    // --> 2. refresh desired page (selenium? webtester?)
    //TODO: Continue Here
    //evaluate result (jetty server log)

    private void forgeRequest(String targetEndpoint, String payload, int expectedResponseCode) {
        RestAssured.basePath = "";

        try {
            given().
                    request().
                    body(payload).
                    contentType(ContentType.JSON).
                    when().
                    put(targetEndpoint).
                    then().
                    statusCode(expectedResponseCode);
        } catch (Exception e) {
            System.err.println("Could not send request. Is proxy on?");
            System.exit(0);
        }


    }

}

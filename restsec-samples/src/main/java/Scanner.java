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

    public Scanner(String attackSetFile, String payloadsFile) {
        JSONParser parser = new JSONParser();
        try{
            attackSet = (JSONObject) parser.parse(new FileReader(getClass().getClassLoader().getResource(attackSetFile).getFile()));
            System.out.println("Scanner: attackSet loaded from file: "+attackSetFile);
            payloads = (JSONObject) parser.parse(new FileReader(getClass().getClassLoader().getResource(payloadsFile).getFile()));
            System.out.println("Scanner: payloads loaded from file: "+payloadsFile);
            System.out.println("Scanner: Loading properties (baseURI, port, basePath, proxy ip, proxy port) ...");
            loadProperties();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadProperties() throws IOException {
        Properties properties = new Properties();

        try(InputStream stream = Scanner.class.getClassLoader().getResourceAsStream("config.properties")){
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

    public void scanAll(){
        System.out.println("Scanner: Trying XSS payloads ...");
        scanForXSS();
    }

    private void scanForXSS(){
        //iterate through all possible combinations

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

                    // Filtering endpoints with curly brackets (numbers)
                    if (resource.contains("{")) {
                        System.err.println("Scanner: Skipping " + resource + " (due to curly bracket). Not yet implemented!");
                    } else {
                        System.out.println("Scanner: Trying: " + httpVerb + " on " + resource + " with " + payloadName);
                        forgeRequest(resource, payload, 200);
                    }

                }

            }

        }

    }

    //create payload DONE
    //inject payload DONE
    //TODO: Continue Here
    //execute payload
    // --> 1. open jetty server
    // --> 2. refresh desired page (selenium? webtester?)
    //evaluate result (jetty server log)

    private void forgeRequest(String targetEndpoint, String payload, int expectedResponseCode) {
        RestAssured.basePath = "";

        given().
                request().
                body(payload).
                contentType(ContentType.JSON).
         when().
                put(targetEndpoint).
         then().
                statusCode(expectedResponseCode);

    }

}

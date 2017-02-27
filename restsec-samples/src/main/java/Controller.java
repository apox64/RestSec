import io.restassured.RestAssured;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class Controller {


    private static String entryPointHATEOAS = "";
    private static String swaggerLocation = "";
    private static boolean allHTTPMethods = false;
    private static String documentationType = "";
    private static String scanForVulnerabilityTypes = "all";


    public Controller() {
        try {
            loadProperties();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private String baseURL = "http://127.0.0.1:80";

    public static void main (String[] args) throws Exception {

        new Controller();

        //Starting a Parser with desired arguments (Thread)
        Thread parser = null;

        switch (documentationType.toLowerCase()) {
            case "swagger" :
                System.out.println("Controller: Using Swagger Documentation : "+swaggerLocation+" (All HTTP Methods: "+allHTTPMethods+")");
                parser = new Thread(new Parser(swaggerLocation, "Swagger", allHTTPMethods));
                break;
            case "hateoas" :
                System.out.println("Controller: Following HATEOAS links on : "+entryPointHATEOAS);
                parser = new Thread(new Parser(entryPointHATEOAS, "HATEOAS", allHTTPMethods));
                break;
            default:
                System.err.println("Documentation Type not supported.");
                System.exit(0);
                break;
        }

        System.err.println(">>> Controller: Starting parser thread ... ");
        parser.start();
        // Waiting for Parser to finish
        parser.join();
        System.err.println(">>> Controller: Parser thread finished.");


        //Starting a scanner
        switch (scanForVulnerabilityTypes.toLowerCase()) {
            case "xss":
                Scanner scannerXSS = new Scanner("attackable/attackable.json", "payloads/xss.json");
                scannerXSS.scanXSS();
                break;
            case "sqli":
                Scanner scannerSQLi = new Scanner("attackable/attackable.json", "payloads/sqli.json");
                scannerSQLi.scanSQLi();
                break;
            case "all":
                //Loop over all payload files in "payloads" folder.
                final File folder = new File("restsec-samples/src/main/resources/payloads/");
                for (final File fileEntry : folder.listFiles()) {
                    Scanner scanner = new Scanner("attackable/attackable.json", "payloads/"+fileEntry.getName());
                    scanner.scanXSS();
                    scanner.scanSQLi();
                }
                break;
            default:
                System.err.println("Controller: Did not recognize scan type in config.properties : XSS / SQLi / all");
                System.exit(0);
        }

        // Starting an Evaluator
        Evaluator evaluator = new Evaluator();
        evaluator.evaluateLogfile();

        System.err.println(">>> RestSec terminated.");
    }

    private void loadProperties() throws IOException {
        System.out.print("Controller: Loading properties ... ");
        Properties properties = new Properties();

        try(InputStream stream = Scanner.class.getClassLoader().getResourceAsStream("config.properties")){
            properties.load(stream);
        }

        // Load config for Rest-assured
        //System.out.print("Config for Rest-Assured: ");
        RestAssured.baseURI = properties.getProperty("base-uri");
        //System.out.print("baseURI : "+RestAssured.baseURI);
        RestAssured.port = Integer.parseInt(properties.getProperty("port"));
        //System.out.print(" port : "+RestAssured.port);
        RestAssured.basePath = properties.getProperty("base-path");
        //System.out.println(" basePath : "+RestAssured.basePath);

        this.baseURL = properties.getProperty("base-uri") + ":" + properties.getProperty("port");// + properties.getProperty("base-path");

        if (!properties.getProperty("proxy_ip").equals("")) {
            RestAssured.proxy(properties.getProperty("proxy_ip"), Integer.parseInt(properties.getProperty("proxy_port")));
            //System.out.println("proxy : "+RestAssured.proxy);
        } else {
            //System.out.println("proxy : no proxy set in config.properties");
        }

        // Load config for scan modes and documentation type
        documentationType = properties.getProperty("documentationType");
        entryPointHATEOAS = properties.getProperty("entryPointHATEOAS");
        swaggerLocation = properties.getProperty("swaggerLocation");
        allHTTPMethods = Boolean.parseBoolean(properties.getProperty("allHTTPMethods"));
        scanForVulnerabilityTypes = properties.getProperty("scanForVulnerabilityTypes");

        System.out.println("Done.");

    }

}

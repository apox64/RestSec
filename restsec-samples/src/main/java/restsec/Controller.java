package restsec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

public class Controller {

    private static String entryPointHATEOAS = "";
    private static String swaggerLocation = "";
    private static String documentationType = "";
    private static String scanForVulnerabilityTypes = "all";
    private static String xssPayloadsFile = "";
    private static String sqliPayloadsFile = "";
    private static boolean allHTTPMethods = false;
    private static boolean deleteOldResultsFile = true;

    public Controller() {
            loadProperties();
    }

    public static void main (String[] args) throws Exception {

        new Controller();

        if (deleteOldResultsFile) {
            Files.deleteIfExists(new File("restsec-samples/src/main/resources/results/results.json").toPath());
            System.out.println("restsec.Controller: results.json deleted.");
        }

        Thread parser = null;

        switch (documentationType.toLowerCase()) {
            case "swagger" :
                System.out.println("restsec.Controller: Using Swagger Documentation : "+swaggerLocation+" (All HTTP Methods: "+allHTTPMethods+")");
                parser = new Thread(new Parser(swaggerLocation, "Swagger", allHTTPMethods));
                break;
            case "hateoas" :
                System.out.println("restsec.Controller: Following HATEOAS links on : "+entryPointHATEOAS);
                parser = new Thread(new Parser(entryPointHATEOAS, "HATEOAS", allHTTPMethods));
                break;
            default:
                System.err.println("Documentation Type not supported.");
                System.exit(0);
                break;
        }

        System.err.println(">>> restsec.Controller: Starting parser thread ... ");
        parser.start();
        // Waiting for restsec.Parser to finish
        parser.join();
        System.err.println(">>> restsec.Controller: restsec.Parser thread finished.");

        //Starting a restsec.Scanner
        switch (scanForVulnerabilityTypes.toLowerCase()) {
            case "xss":
                Scanner scannerXSS = new Scanner("attackable/attackable.json", xssPayloadsFile);
                scannerXSS.scanXSS();
                break;
            case "sqli":
                Scanner scannerSQLi = new Scanner("attackable/attackable.json", sqliPayloadsFile);
                scannerSQLi.scanSQLi();
                break;
            case "all":
                //Loop over all payload files in "payloads" folder.
                final File folder = new File("restsec-samples/src/main/resources/payloads/");
                //noinspection ConstantConditions
                for (final File fileEntry : folder.listFiles()) {
                    Scanner scanner = new Scanner("attackable/attackable.json", "payloads/"+fileEntry.getName());
                    scanner.scanXSS();
                    scanner.scanSQLi();
                }
                break;
            default:
                System.err.println("restsec.Controller: Did not recognize scan type in config.properties : XSS / SQLi / all");
                System.exit(0);
        }

        // Starting an restsec.Evaluator (static)
        Evaluator.evaluateLogfile();

        System.err.println(">>> RestSec terminated.");
    }

    private void loadProperties() {
        System.out.print("restsec.Controller: Loading properties ... ");
        Properties properties = new Properties();

        try(InputStream stream = Scanner.class.getClassLoader().getResourceAsStream("config.properties")){
            properties.load(stream);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        /*
        // Load config for Rest-assured
        //System.out.print("Config for Rest-Assured: ");
        RestAssured.baseURI = properties.getProperty("base-uri");
        //System.out.print("baseURI : "+RestAssured.baseURI);
        RestAssured.port = Integer.parseInt(properties.getProperty("port"));
        //System.out.print(" port : "+RestAssured.port);
        RestAssured.basePath = properties.getProperty("base-path");
        //System.out.println(" basePath : "+RestAssured.basePath);

        String baseURL = properties.getProperty("base-uri") + ":" + properties.getProperty("port");

        if (!properties.getProperty("proxy_ip").equals("")) {
            RestAssured.proxy(properties.getProperty("proxy_ip"), Integer.parseInt(properties.getProperty("proxy_port")));
            System.out.println("proxy : "+RestAssured.proxy);
        } else {
            System.out.println("proxy : no proxy set in config.properties");
        }
        */

        // Load config for scan modes and documentation type
        documentationType = properties.getProperty("documentationType");
        entryPointHATEOAS = properties.getProperty("entryPointHATEOAS");
        swaggerLocation = properties.getProperty("swaggerLocation");
        allHTTPMethods = Boolean.parseBoolean(properties.getProperty("allHTTPMethods"));
        scanForVulnerabilityTypes = properties.getProperty("scanForVulnerabilityTypes");
        xssPayloadsFile = properties.getProperty("xssPayloadsFile");
        sqliPayloadsFile = properties.getProperty("sqliPayloadsFile");
        deleteOldResultsFile = Boolean.parseBoolean(properties.getProperty("deleteOldResultsFile"));

    }

}

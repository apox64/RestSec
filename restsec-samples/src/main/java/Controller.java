import io.restassured.RestAssured;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class Controller {

    private static boolean useHATEOAS = false;
    private static boolean useSwagger = false;
    private static String entryPoint = "";
    private static String swaggerLocation = "";
    private static boolean bruteforce = false;


    public Controller() {
        try {
            loadProperties();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private String baseURL = "http://127.0.0.1:80";

    public static void main (String[] args) throws Exception {

        Logger logger = Logger.getLogger(Controller.class.getName());
        logger.info("RestSec started.");

        new Controller();

        //Starting a Parser with desired arguments (Thread)

        // @TODO: Change this entry point application specific (currently running:
        // Swagger: JuiceShop (with (pseudo) Swagger Documentation)
        // HATEOAS: https://github.com/corsoft/spring-hateoas-demo

        // Parser:
        // Swagger: String link, bool bruteforce
        // HATEOAS: String entryPoint (Parser will follow links)

        Thread parser = null;

        if (useSwagger && !useHATEOAS) {
            System.out.println("Controller: Using Swagger Documentation : "+swaggerLocation+" (Bruteforce: "+bruteforce+")");
            parser = new Thread(new Parser(swaggerLocation, bruteforce));
        } else if (!useSwagger && useHATEOAS) {
            System.out.println("Controller: Using HATEOAS. Starting on : "+entryPoint);
            parser = new Thread(new Parser(entryPoint));
        } else {
            System.err.println("Controller: config.properties doesn't make sense.");
            System.exit(0);
        }

        System.err.println(">>> Controller: Starting parser thread ... ");
        parser.start();
        // Waiting for Parser to finish
        parser.join();
        System.err.println(">>> Controller: Parser thread finished.");

        // Starting a Scanner
        Scanner scanner = new Scanner("attackable/attackable.json","payloads/xss.json");
        scanner.scanAll();

        // Starting an Evaluator
        Evaluator evaluator = new Evaluator();
        try {
            evaluator.evaluateLogfile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("RestSec terminated.");
    }

    private void loadProperties() throws IOException {
        System.out.println("Controller: Loading properties ... ");
        Properties properties = new Properties();

        try(InputStream stream = Scanner.class.getClassLoader().getResourceAsStream("config.properties")){
            properties.load(stream);
        }

        // Load config for Rest-assured
        System.out.print("Config for Rest-Assured: ");
        RestAssured.baseURI = properties.getProperty("base-uri");
        System.out.print("baseURI : "+RestAssured.baseURI);
        RestAssured.port = Integer.parseInt(properties.getProperty("port"));
        System.out.print(" port : "+RestAssured.port);
        RestAssured.basePath = properties.getProperty("base-path");
        System.out.println(" basePath : "+RestAssured.basePath);

        this.baseURL = properties.getProperty("base-uri") + ":" + properties.getProperty("port");// + properties.getProperty("base-path");

        if (!properties.getProperty("proxy_ip").equals("")) {
            RestAssured.proxy(properties.getProperty("proxy_ip"), Integer.parseInt(properties.getProperty("proxy_port")));
            System.out.println("proxy : "+RestAssured.proxy);
        } else {
            System.out.println("proxy : no proxy set in config.properties");
        }

        // Load config for scan modes and documentation type
        useHATEOAS = Boolean.parseBoolean(properties.getProperty("useHATEOAS"));
        useSwagger = Boolean.parseBoolean(properties.getProperty("useSwagger"));
        entryPoint = properties.getProperty("entryPoint");
        swaggerLocation = properties.getProperty("swaggerLocation");
        bruteforce = Boolean.parseBoolean(properties.getProperty("bruteforce"));

        System.out.println("Done.");

    }

}

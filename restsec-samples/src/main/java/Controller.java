import io.restassured.RestAssured;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class Controller {

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
        //Thread parser = new Thread(new Parser("docs_swagger/swagger-juiceshop-short.json", false));
        Thread parser = new Thread(new Parser("http://192.168.99.100:3000/rest/product/search?q=undefined"));
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

        // Load config
        RestAssured.baseURI = properties.getProperty("base-uri");
        System.out.println("baseURI : "+RestAssured.baseURI);
        RestAssured.port = Integer.parseInt(properties.getProperty("port"));
        System.out.println("port : "+RestAssured.port);
        RestAssured.basePath = properties.getProperty("base-path");
        System.out.println("basePath : "+RestAssured.basePath);

        this.baseURL = properties.getProperty("base-uri") + ":" + properties.getProperty("port");// + properties.getProperty("base-path");

        if (!properties.getProperty("proxy_ip").equals("")) {
            RestAssured.proxy(properties.getProperty("proxy_ip"), Integer.parseInt(properties.getProperty("proxy_port")));
            System.out.println("proxy : "+RestAssured.proxy);
        } else {
            System.out.println("proxy : no proxy set in config");
        }

        System.out.println("Done.");

    }

}

package restsec;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

class Controller {

    private static String entryPointHATEOAS = "";
    private static String swaggerLocation = "";
    private static String documentationType = "";
    private static String scanForVulnerabilityTypes = "all";
    private static String xssPayloadsFile = "";
    private static String sqliPayloadsFile = "";
    private static boolean allHTTPMethods = false;
    private static boolean deleteOldResultsFile = true;
    private static final Logger logger = Logger.getLogger(Controller.class);


    private Controller() {
            loadProperties();
    }

    public static void main (String[] args) throws Exception {

        new Controller();

        if (deleteOldResultsFile) {
            Files.deleteIfExists(new File("src/main/resources/results/results.json").toPath());
            logger.info("results.json deleted.");
        }

        switch (documentationType.toLowerCase()) {
            case "swagger" :
                logger.info("Using Swagger Documentation : "+swaggerLocation+" (All HTTP Methods: "+allHTTPMethods+")");
                Thread parserSwagger = new Thread(new Parser(swaggerLocation, allHTTPMethods));
                logger.info("Starting parser thread ... ");
                parserSwagger.start();
                parserSwagger.join();
                logger.info("Parser thread finished.");
                break;
            case "hateoas" :
                logger.info("Following HATEOAS links on : "+entryPointHATEOAS);
                Thread parserHATEOAS = new Thread(new Parser(entryPointHATEOAS));
                logger.info("Starting parser thread ... ");
                parserHATEOAS.start();
                parserHATEOAS.join();
                logger.info("Parser thread finished.");
                break;
            default:
                logger.warn("Documentation Type not supported.");
                System.exit(0);
                break;
        }

        //Starting a restsec.Scanner
        switch (scanForVulnerabilityTypes.toLowerCase()) {
            case "xss":
                Thread scannerXSS = new Thread(new Scanner("src/main/resources/attackable/attackset.json", xssPayloadsFile, "xss"));
                scannerXSS.start();
                scannerXSS.join();
                break;
            case "sqli":
                Thread scannerSQLi = new Thread(new Scanner("src/main/resources/attackable/attackset.json", sqliPayloadsFile, "sqli"));
                scannerSQLi.start();
                scannerSQLi.join();
                break;
            case "all":
                //Loop over all payload files in "payloads" folder.
                final File folder = new File("src/main/resources/payloads/");
                //noinspection ConstantConditions
                for (final File fileEntry : folder.listFiles()) {
                    Thread scanner = new Thread(new Scanner("src/main/resources/attackable/attackset.json", "src/main/resources/payloads/"+fileEntry.getName(), "all"));
                    scanner.start();
                    scanner.join();
                }
                break;
            default:
                logger.warn("Did not recognize scan type in config.properties : XSS / SQLi / all");
                System.exit(0);
        }

        Evaluator.evaluateLogfile();

        logger.info("RestSec terminated.");
    }

    private void loadProperties() {
        logger.info("Loading properties ... ");
        Properties properties = new Properties();

        try(InputStream stream = Scanner.class.getClassLoader().getResourceAsStream("config.properties")){
            properties.load(stream);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

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

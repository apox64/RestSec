package restsec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restsec.crawler.Crawler;
import restsec.crawler.CrawlerFactory;

import java.io.File;
import java.nio.file.Files;

class Controller {

    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    private static Configuration config;

    private Controller() {
    }

    public static void main(String[] args) throws Exception {

        CrawlerFactory crawlerFactory = new CrawlerFactory(new Configuration());
        Crawler crawler = crawlerFactory.createCrawler();
        crawler.crawl();

        /*
        new CallbackPage()

        Scanner scanner = new Scanner();

        AttackSet attackSet = crawler.crawl(target);
        scanner.scan(target, attackSet);
        */

//      new Controller();

        //TODO : Currently exiting here, so you don't have to stop the programm everytime
        Thread.sleep(2000);
        System.exit(0);

        config = new Configuration();

        if (config.getBoolDeleteOldResultsFile()) {
            Files.deleteIfExists(new File("src/main/resources/results/results.json").toPath());
            LOGGER.info("results.json deleted.");
        }

        /*
        //Start a Crawler
        switch (config.getDocumentationType().toLowerCase()) {
            case "swagger" :
                LOGGER.info("Using Swagger Documentation : "+config.getSwaggerFileLocation()+" (All HTTP Methods: "+config.getBoolUseAllHTTPMethods()+")");
                new Crawler(config.getSwaggerFileLocation(), config.getBoolUseAllHTTPMethods());
                break;
            case "hateoas" :
                LOGGER.info("Following HATEOAS links on : "+config.getHATEOASEntryPoint());
                new Crawler(config.getHATEOASEntryPoint());
                LOGGER.info("Starting parser thread ... ");
                LOGGER.info("Crawler thread finished.");
                break;
            default:
                LOGGER.warn("Documentation Type not supported.");
                System.exit(0);
                break;
        }
        */

        //Starting a Scanner
        switch (config.getVulnerabilityScanType().toLowerCase()) {
            case "xss":
                new Scanner("src/main/resources/attackable/attackset.json", config.getXSSPayloadsFileLocation(), "xss").scan();
                break;
            case "sqli":
                new Scanner("src/main/resources/attackable/attackset.json", config.getSQLiPayloadsFileLocation(), "sqli");
                break;
            case "all":
                //Loop over all payload files in "payloads" folder.
                final File folder = new File("src/main/resources/payloads/");
                //noinspection ConstantConditions
                for (final File fileEntry : folder.listFiles()) {
                    new Scanner("src/main/resources/attackable/attackset.json", "src/main/resources/payloads/" + fileEntry.getName(), "all");
                }
                break;
            default:
                LOGGER.warn("Did not recognize scan type in config.properties : XSS / SQLi / all");
                System.exit(0);
        }

        //Evaluate the Jetty Logfile
        Evaluator.evaluateJettyLogfile();

        LOGGER.info("RestSec terminated.");
        System.exit(0);
    }

}

package restsec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restsec.config.Configuration;
import restsec.crawler.Crawler;
import restsec.crawler.CrawlerFactory;
import restsec.scanner.HTTPSecurityHeadersScanner;
import restsec.scanner.Scanner;
import restsec.scanner.ScannerFactory;

class Controller {

    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    public static void main(String[] args) throws Exception {

        Configuration config = new Configuration();

        // deleting existing results file
        if (config.getBoolDeleteOldResultsFile()) {
            Evaluator.deleteOldResultsFile();
        }

        CrawlerFactory crawlerFactory = new CrawlerFactory(config);
        Crawler crawler = crawlerFactory.createCrawler();
        AttackSet attackSet = crawler.crawl(config.getTargetURLAsString());

        //TODO: Who offers the method "writeAttackSetToFile"?
        new AttackSet().writeAttackSetToFile(attackSet, config.getAttackSetFileLocation());

        ScannerFactory scannerFactory = new ScannerFactory(config);
        Scanner scanner = scannerFactory.createScanner();
        scanner.scan(config.getTargetURLAsString(), attackSet);

        if (config.getPerformBasicSecurityHeaderTests()) {
            HTTPSecurityHeadersScanner httpSecurityHeadersScanner = new HTTPSecurityHeadersScanner();
            httpSecurityHeadersScanner.scanForSecurityHeaders(config.getBasePath());
        }

        Evaluator evaluator = new Evaluator();
        evaluator.evaluateJettyLogfile();

        Reporting reporting = new Reporting();
        reporting.generateReport();

        LOGGER.info("RestSec terminated.");
        System.exit(0);
    }

}

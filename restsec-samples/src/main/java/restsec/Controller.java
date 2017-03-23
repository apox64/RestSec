package restsec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restsec.config.Configuration;
import restsec.crawler.Crawler;
import restsec.crawler.CrawlerFactory;
import restsec.scanner.Scanner;
import restsec.scanner.ScannerFactory;

class Controller {

    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    public static void main(String[] args) throws Exception {

        Configuration config = new Configuration();

        CrawlerFactory crawlerFactory = new CrawlerFactory(config);
        Crawler crawler = crawlerFactory.createCrawler();
        AttackSet attackSet = crawler.crawl(config.getTargetURLAsString());

        //TODO: Who offers the method "writeAttackSetToFile"?
        new AttackSet().writeAttackSetToFile(attackSet, config.getAttackSetFileLocation());

        /*
        new CallbackServer()

        AttackSet attackSet = crawler.crawl(target);
        scanner.scan(target, attackSet);
        */

        //TODO : Currently exiting here, so you don't have to stop the programm everytime

        ScannerFactory scannerFactory = new ScannerFactory(config);
        Scanner scanner = scannerFactory.createScanner();
        scanner.scan();

//        System.exit(0);

        Evaluator evaluator = new Evaluator(config);
        evaluator.evaluateJettyLogfile();

        LOGGER.info("RestSec terminated.");
        System.exit(0);
    }

}

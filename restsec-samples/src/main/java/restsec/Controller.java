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

    private Controller() {
    }

    public static void main(String[] args) throws Exception {

        CrawlerFactory crawlerFactory = new CrawlerFactory(new Configuration());
        Crawler crawler = crawlerFactory.createCrawler();
        crawler.crawl();

        /*
        new CallbackPage()

        AttackSet attackSet = crawler.crawl(target);
        scanner.scan(target, attackSet);
        */

        //TODO : Currently exiting here, so you don't have to stop the programm everytime
//        Thread.sleep(2000);
//        System.exit(0);

        ScannerFactory scannerFactory = new ScannerFactory(new Configuration());
        Scanner scanner = scannerFactory.createScanner();
        scanner.scan();

        Evaluator.evaluateJettyLogfile();

        LOGGER.info("RestSec terminated.");
        System.exit(0);
    }

}

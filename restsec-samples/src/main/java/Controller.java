import java.io.IOException;
import java.util.logging.Logger;

public class Controller {
    // Create Parser
    // Create one or more Scanners

    public static void main (String[] args) {

        Logger logger = Logger.getLogger(Controller.class.getName());
        logger.info("RestSec started.");

        // Starting a Parser
        Parser parser = new Parser();
        parser.parseSwaggerJSON("docs_swagger/swagger-juiceshop-short.json", false);

        //Wait until parser has finished. Replace with wait() / notify()
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

}

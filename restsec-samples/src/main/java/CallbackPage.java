
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class CallbackPage {

    //opens jetty server and waits for input

    //if you change the port, you have to change the payload value as well
    private static int port = 5555;
    private static Server server = new Server(port);
    private ChromeDriver chromeDriver;

    Logger logger = Logger.getLogger(CallbackPage.class.getName());

    public CallbackPage() {
        configureJettyLogging(true);
    }

    private static void configureJettyLogging(boolean deleteOldLogs) {

        // Setting Jetty logger implementation and level (DEBUG | INFO | WARN | IGNORE)
        //TODO: Reduce Jetty console Logging to warnings only (reduced terminal output)
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.JavaUtilLog");
        System.setProperty("org.eclipse.jetty.util.log.class.LEVEL", "WARN");

        if (deleteOldLogs) {
            File directory = new File("restsec-samples/src/main/resources/jetty-logs/");

            for(File f : directory.listFiles()) {
                f.delete();
            }
        }

        NCSARequestLog requestLog = new NCSARequestLog("restsec-samples/src/main/resources/jetty-logs/jetty-yyyy_mm_dd.request.log");

        requestLog.setAppend(false);
        requestLog.setExtended(false);
        requestLog.setLogTimeZone("GMT");
        requestLog.setLogLatency(true);
        requestLog.setRetainDays(90);

        server.setRequestLog(requestLog);

    }

    public void startTestPageServer() {
        try {
            server.start();
            System.out.println("CallbackPage: Jetty Server started. Listening on "+port+" ... ");
            logger.info("Jetty Server started on port "+port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopTestPageServer() {
        try {
            server.stop();
            System.out.println("CallbackPage: Jetty Server stopped.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setWebDriver() {
        //Drivers for other OS can be downloaded here: https://sites.google.com/a/chromium.org/chromedriver/downloads
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\DMD\\Development\\RestSec\\restsec-samples\\chromedriver-win32.exe");
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        Proxy proxy = new Proxy();
        Properties properties = new Properties();
        InputStream stream = Scanner.class.getClassLoader().getResourceAsStream("config.properties");
        try {
            properties.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        proxy.setHttpProxy(properties.getProperty("proxy_ip")+":"+properties.getProperty("proxy_port"));
        capabilities.setCapability("proxy", proxy);
        this.chromeDriver = new ChromeDriver(capabilities);
    }

    //this lets you actually execute the stored xss payload by reloading the page (with selenium webdriver)
    public void reloadResource(String url){
        System.out.print("CallbackPage: Creating ChromeDriver ... ");
        setWebDriver();
        System.out.print("CallbackPage: Getting URL: " + url + " ... ");
        chromeDriver.get(url);
        System.out.println("Done.");

        try {
            System.out.print("CallbackPage: Sleeping for 2 seconds ... ");
            Thread.sleep(2000);
            System.out.println("Done.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.print("CallbackPage: Refreshing ... ");

        //click alert automatically, if there is one
        try {
            WebDriverWait wait = new WebDriverWait(chromeDriver, 1);
            System.out.print("CallbackPage: Waiting for alert ... ");
            wait.until(ExpectedConditions.alertIsPresent());
            System.out.println("Alert found (payload worked!)");
            chromeDriver.switchTo().alert().accept();
        } catch (TimeoutException te){
            System.out.println("No alert found.");
        }

        System.out.print("CallbackPage: Refreshing the page ... ");
        chromeDriver.navigate().refresh();
        System.out.println("Done.");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        chromeDriver.close();
        System.out.println("Done.");
    }

}

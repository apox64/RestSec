package restsec;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.apache.log4j.Level.WARN;
import static org.eclipse.jetty.util.log.AbstractLogger.LEVEL_WARN;


class CallbackPage {

    //if you change the port, you have to change the payload value as well (otherwise payload won't call back
    private static final int port = 5555;
    private static final Server server = new Server(port);
    private static ChromeDriver chromeDriver;
    private static final boolean deleteOldLogs = true;

    private static final Logger logger = Logger.getLogger(CallbackPage.class);

//    private static Logger logger = Logger.getLogger(CallbackPage.class.getName());

    CallbackPage() {
        configureJettyLogging(deleteOldLogs);
    }

    private static void configureJettyLogging(boolean deleteOldLogs) {

//        Setting Jetty logger implementation and level (DEBUG | INFO | WARN | IGNORE)
//        TODO: Reduce Jetty console Logging to warnings only (reduced terminal output)
//        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.JavaUtilLog");
        System.setProperty("org.eclipse.jetty.util.log.LEVEL", "WARN");
        System.setProperty("org.eclipse.jetty.util.log.WARN","true");

        if (deleteOldLogs) {
            File directory = new File("src/main/resources/jetty-logs/");

            //noinspection ConstantConditions
            for (File f : directory.listFiles()) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
        }

        NCSARequestLog requestLog = new NCSARequestLog("src/main/resources/jetty-logs/jetty-yyyy_mm_dd.request.log");

        requestLog.setAppend(false);
        requestLog.setExtended(false);
        requestLog.setLogTimeZone("GMT");
        requestLog.setLogLatency(true);
        requestLog.setRetainDays(31);

        server.setRequestLog(requestLog);

    }

    void startTestPageServer() {
        try {
            server.start();
            logger.info("Jetty Server started. Listening on " + port + " ... ");
//            logger.info("Jetty Server started on port "+port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void stopTestPageServer() {
        try {
            server.stop();
            logger.info("Jetty Server stopped.");
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
        proxy.setHttpProxy(properties.getProperty("proxy_ip") + ":" + properties.getProperty("proxy_port"));
        capabilities.setCapability("proxy", proxy);

        //silence webdriver
        System.setProperty("webdriver.chrome.silentOutput", "true");
        Logger.getLogger("org.openqa.selenium.remote.ProtocolHandshake").setLevel(Level.OFF);

        chromeDriver = new ChromeDriver(capabilities);

    }

    //this lets you actually execute the stored xss payload by reloading the page (with selenium webdriver)
    boolean hasAlertOnReload(String url) throws TimeoutException {
        boolean hasAlert = false;

        logger.info("Creating ChromeDriver ... ");
        setWebDriver();
        logger.info("Getting URL: " + url + " ... ");
        chromeDriver.manage().timeouts().pageLoadTimeout(3, TimeUnit.SECONDS);

        try {
            chromeDriver.get(url);
        } catch (TimeoutException te) {
            logger.warn("Target unreachable.");
            chromeDriver.close();
            System.exit(0);
        }

        //click alert automatically, if there is one
        try {
            logger.info("Waiting for alert ... ");
            WebDriverWait wait = new WebDriverWait(chromeDriver, 1);
            wait.until(ExpectedConditions.alertIsPresent());
            logger.info("Alert found (payload worked!)");
            hasAlert = true;
            chromeDriver.switchTo().alert().accept();
        } catch (TimeoutException te) {
            logger.info("No alert found.");
        }

        //TODO: Refresh might actually not even be necessary (reopen on next test does the same)

        chromeDriver.close();

        return hasAlert;

    }
}


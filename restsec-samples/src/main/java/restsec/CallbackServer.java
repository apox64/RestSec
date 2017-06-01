package restsec;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import restsec.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


public class CallbackServer {

    private static int port;
    private static Server server;
    private static WebDriver chromeDriver;

    private static Configuration config;

    private static final Logger logger = Logger.getLogger(CallbackServer.class);

    public CallbackServer() {
        config = new Configuration();
        port = config.getJettyCallbackPort();
        server = new Server(port);
        configureJettyLogging(config.getBoolDeleteOldJettyLogs());
    }

    /*
    public CallbackServer(Configuration config) {

    }
    */

    private static void configureJettyLogging(boolean deleteOldLogs) {

        System.setProperty("org.eclipse.jetty.util.log.LEVEL", "WARN");
        System.setProperty("org.eclipse.jetty.util.log.WARN","true");

        if (deleteOldLogs) {
            //File directory = new File("logs/");
            File directory = new File("target/jetty-logs/");
            directory.mkdirs();

            //noinspection ConstantConditions
            for (File f : directory.listFiles()) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
        }

        NCSARequestLog requestLog = new NCSARequestLog("target/jetty-logs/jetty-yyyy_mm_dd.request.log");

        requestLog.setAppend(false);
        requestLog.setExtended(false);
        requestLog.setLogTimeZone("GMT");
        requestLog.setLogLatency(true);
        requestLog.setRetainDays(31);

        server.setRequestLog(requestLog);

    }

    public void startCallbackServer() {
        try {
            server.start();
            String localhost = InetAddress.getLocalHost().getHostAddress();
            logger.info("Jetty Server started. Listening on " + localhost + ":" + port + " ... ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopCallbackServer() {
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
        DesiredCapabilities desiredCapabilities = DesiredCapabilities.chrome();

        logger.info("Use Proxy: "+config.getBoolUseProxy());
        if (config.getBoolUseProxy()) {
            Proxy proxy = new Proxy();
            proxy.setHttpProxy(config.getProxyIP() + ":" + config.getProxyPort());
            desiredCapabilities.setCapability("proxy", proxy);
            logger.info("Using proxy: "+desiredCapabilities.getCapability("proxy").toString());
        }

        //silence webdriver logs
        LoggingPreferences loggingPreferences = new LoggingPreferences();
        loggingPreferences.enable(LogType.DRIVER, java.util.logging.Level.OFF);
        System.setProperty("webdriver.chrome.silentOutput", "true");
        Logger.getLogger("org.openqa.selenium.remote.ProtocolHandshake").setLevel(Level.OFF);
        desiredCapabilities.setCapability(CapabilityType.LOGGING_PREFS, loggingPreferences);

        //TODO: Find a way to run Chrome headless (xvfb?)

        chromeDriver = new ChromeDriver(desiredCapabilities);

    }

    // reload the page
    public boolean hasAlertOnReload(String url) throws TimeoutException {
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

            // if there is no alert after a given time, then it has to be some other XSS payload
            // TODO : or payload didn't execute
            chromeDriver.manage().addCookie(new Cookie(
                            "token",
                            Authentication.getIDTokenForJuiceShop_BodyAuth()
                    )
            );
            chromeDriver.navigate().to(url);
        }

        chromeDriver.close();

        return hasAlert;

    }
}


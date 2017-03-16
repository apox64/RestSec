package restsec;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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
import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


class CallbackPage {

    private static int port;
    private static Server server;
    private static ChromeDriver chromeDriver;

    private static Configuration config;

    private static final Logger logger = Logger.getLogger(CallbackPage.class);

    CallbackPage() {
        config = new Configuration();
        port = config.getJettyCallbackPort();
        server = new Server(port);
        configureJettyLogging(config.getBoolDeleteOldJettyLogs());
    }

    private static void configureJettyLogging(boolean deleteOldLogs) {

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
            String localhost = InetAddress.getLocalHost().getHostAddress();
            logger.info("Jetty Server started. Listening on " + localhost + ":" + port + " ... ");
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

        Properties properties = new Properties();
        InputStream stream = Scanner.class.getClassLoader().getResourceAsStream("config.properties");
        try {
            properties.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Use Proxy: "+config.getBoolUseProxy());
        if (config.getBoolUseProxy()) {
            Proxy proxy = new Proxy();
            proxy.setHttpProxy(config.getProxyIP() + ":" + config.getProxyPort());
            capabilities.setCapability("proxy", proxy);
            logger.info("Using proxy: "+capabilities.getCapability("proxy"));
        }

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


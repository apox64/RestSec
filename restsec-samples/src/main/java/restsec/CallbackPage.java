package restsec;

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
import java.util.concurrent.TimeUnit;


class CallbackPage {

    //if you change the port, you have to change the payload value as well (otherwise payload won't call back
    private static final int port = 5555;
    private static final Server server = new Server(port);
    private static ChromeDriver chromeDriver;
    private static final boolean deleteOldLogs = true;

//    private static Logger logger = Logger.getLogger(CallbackPage.class.getName());

    CallbackPage() {
        configureJettyLogging(deleteOldLogs);
    }

    private static void configureJettyLogging(boolean deleteOldLogs) {

//        Setting Jetty logger implementation and level (DEBUG | INFO | WARN | IGNORE)
//        TODO: Reduce Jetty console Logging to warnings only (reduced terminal output)
//        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.JavaUtilLog");
        System.setProperty("org.eclipse.jetty.util.log.class.LEVEL", "WARN");


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
            System.out.println("restsec.CallbackPage: Jetty Server started. Listening on " + port + " ... ");
//            logger.info("Jetty Server started on port "+port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void stopTestPageServer() {
        try {
            server.stop();
            System.out.println("restsec.CallbackPage: Jetty Server stopped.");
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
        chromeDriver = new ChromeDriver(capabilities);
    }

    //this lets you actually execute the stored xss payload by reloading the page (with selenium webdriver)
    boolean hasAlertOnReload(String url) {
        boolean hasAlert = false;

        System.out.print("restsec.CallbackPage: Creating ChromeDriver ... ");
        setWebDriver();
        System.out.print("restsec.CallbackPage: Getting URL: " + url + " ... ");
        chromeDriver.manage().timeouts().pageLoadTimeout(3, TimeUnit.SECONDS);
        chromeDriver.get(url);

        //click alert automatically, if there is one
        try {
            System.out.print("restsec.CallbackPage: Waiting for alert ... ");
            WebDriverWait wait = new WebDriverWait(chromeDriver, 1);
            wait.until(ExpectedConditions.alertIsPresent());
            System.out.println("Alert found (payload worked!)");
            hasAlert = true;
            chromeDriver.switchTo().alert().accept();
        } catch (TimeoutException te) {
            System.out.println("No alert found.");
        }

        //TODO: Refresh might actually not even be necessary (reopen on next test does the same)
        /*
        System.out.print("restsec.CallbackPage: Refreshing the page ... ");
        chromeDriver.navigate().refresh();

        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Done.");
        */

        chromeDriver.close();

        return hasAlert;

    }
}


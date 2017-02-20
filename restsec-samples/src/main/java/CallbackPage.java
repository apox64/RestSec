
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;

public class CallbackPage {

    //opens jetty server and waits for input

    //if you change the port, you have to change the payload value as well
    private static int port = 5555;
    private static Server server = new Server(port);
    private ChromeDriver chromeDriver;

    public CallbackPage() {
        configureJettyLogging();
    }

    private static void configureJettyLogging() {

        NCSARequestLog requestLog = new NCSARequestLog("restsec-samples/src/main/resources/jetty-logs/jetty-yyyy_mm_dd.request.log");

        requestLog.setAppend(true);
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
        proxy.setHttpProxy("127.0.0.1:8080");
        capabilities.setCapability("proxy", proxy);
        this.chromeDriver = new ChromeDriver(capabilities);
    }

    //this lets you actually execute the stored xss payload by reloading the page (with selenium webdriver)
    public void reloadResource(String url){
        System.out.print("CallbackPage: Creating ChromeDriver ... ");
        setWebDriver();
        System.out.println("Done.");
        System.out.println("CallbackPage: Reloading: " + url + " ... ");
        chromeDriver.get(url);

        try {
            System.out.println("CallbackPage: Sleeping for 2 seconds ...");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.print("CallbackPage: Refreshing ... ");

        //click alert automatically, if there is one
        try {
            WebDriverWait wait = new WebDriverWait(chromeDriver, 2);
            wait.until(ExpectedConditions.alertIsPresent());
            System.out.println("Alert found (payload worked!)");
            chromeDriver.switchTo().alert().accept();
        } catch (TimeoutException te){
            System.out.println("No alert found.");
        }

        chromeDriver.navigate().refresh();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        chromeDriver.close();
        System.out.println("Done.");
    }

}
